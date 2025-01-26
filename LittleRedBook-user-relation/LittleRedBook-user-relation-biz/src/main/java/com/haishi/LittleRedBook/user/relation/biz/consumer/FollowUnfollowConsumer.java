package com.haishi.LittleRedBook.user.relation.biz.consumer;


import com.google.common.util.concurrent.RateLimiter;
import com.haishi.LittleRedBook.user.relation.biz.constant.MQConstants;
import com.haishi.LittleRedBook.user.relation.biz.constant.RedisKeyConstants;
import com.haishi.LittleRedBook.user.relation.biz.domain.dataobject.FansDO;
import com.haishi.LittleRedBook.user.relation.biz.domain.dataobject.FollowingDO;
import com.haishi.LittleRedBook.user.relation.biz.domain.mapper.FansDOMapper;
import com.haishi.LittleRedBook.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.haishi.LittleRedBook.user.relation.biz.model.dto.FollowUserMqDTO;
import com.haishi.framework.commons.util.DateUtils;
import com.haishi.framework.commons.util.JsonUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;



@Component
@RocketMQMessageListener(consumerGroup = "LittleRedBook_group", // Group 组
        topic = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW // 消费的 Topic 主题
)
@Slf4j
public class FollowUnfollowConsumer implements RocketMQListener<Message> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private FollowingDOMapper followingDOMapper;

    @Resource
    private FansDOMapper fansDOMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RateLimiter rateLimiter;

    @Override
    public void onMessage(Message message) {

        // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
        rateLimiter.acquire();


        String bodyJsonStr = new String(message.getBody());
        String tags = message.getTags();
        log.info("==> FollowUnfollowConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);

        // 根据 MQ 标签，判断操作类型
        if (Objects.equals(tags, MQConstants.TAG_FOLLOW)) {
            // 关注
            handleFollowTagMessage(bodyJsonStr);
        } else if (Objects.equals(tags, MQConstants.TAG_UNFOLLOW)) {
            // 取关
            // TODO
        }

    }

    /**
     * 关注
     * @param bodyJsonStr
     */
    private void handleFollowTagMessage(String bodyJsonStr) {

        FollowUserMqDTO followUserMqDTO = JsonUtils.parseObject(bodyJsonStr, FollowUserMqDTO.class);

        // 判空
        if (Objects.isNull(followUserMqDTO)) return;

        // 幂等性：通过联合唯一索引保证

        Long userId = followUserMqDTO.getUserId();
        Long followUserId = followUserMqDTO.getFollowUserId();
        LocalDateTime createTime = followUserMqDTO.getCreateTime();

        Boolean execute = transactionTemplate.execute((status) -> {
            try {
                // 关注成功需往数据库添加两条记录
                // 关注表：一条记录
                int count = followingDOMapper.insert(FollowingDO.builder()
                        .userId(userId)
                        .followingUserId(followUserId)
                        .createTime(createTime)
                        .build());

                // 粉丝表：一条记录
                if (count > 0) {
                    fansDOMapper.insert(FansDO.builder()
                            .userId(followUserId)
                            .fansUserId(userId)
                            .createTime(createTime)
                            .build());
                }
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("关注表插入失败（可能为重复消费）", e);
            }
            return false;
        });

        log.info("## 数据库添加记录结果：{}", execute);

        // 若数据库操作成功，更新 Redis 中被关注用户的 ZSet 粉丝列表
        if (execute) {
            // Lua 脚本
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_update_fans_zset.lua")));
            script.setResultType(Long.class);

            // 时间戳
            long timestamp = DateUtils.localDateTime2Timestamp(createTime);

            // 构建被关注用户的粉丝列表 Redis Key
            String fansRedisKey = RedisKeyConstants.buildUserFansKey(followUserId);
            // 执行脚本
            redisTemplate.execute(script, Collections.singletonList(fansRedisKey), userId, timestamp);
        }
    }

}
