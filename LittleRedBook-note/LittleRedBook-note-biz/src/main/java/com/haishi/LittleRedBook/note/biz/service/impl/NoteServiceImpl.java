package com.haishi.LittleRedBook.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.haishi.LittleRedBook.count.dto.response.FindNoteCountsByIdRspDTO;
import com.haishi.LittleRedBook.note.biz.constant.MQConstants;
import com.haishi.LittleRedBook.note.biz.constant.RedisKeyConstants;
import com.haishi.LittleRedBook.note.biz.domain.dataobject.NoteCollectionDO;
import com.haishi.LittleRedBook.note.biz.domain.dataobject.NoteDO;
import com.haishi.LittleRedBook.note.biz.domain.dataobject.NoteLikeDO;
import com.haishi.LittleRedBook.note.biz.domain.mapper.NoteCollectionDOMapper;
import com.haishi.LittleRedBook.note.biz.domain.mapper.NoteDOMapper;
import com.haishi.LittleRedBook.note.biz.domain.mapper.NoteLikeDOMapper;
import com.haishi.LittleRedBook.note.biz.domain.mapper.TopicDOMapper;
import com.haishi.LittleRedBook.note.biz.enums.*;
import com.haishi.LittleRedBook.note.biz.model.dto.CollectUnCollectNoteMqDTO;
import com.haishi.LittleRedBook.note.biz.model.dto.LikeUnlikeNoteMqDTO;
import com.haishi.LittleRedBook.note.biz.model.dto.NoteOperateMqDTO;
import com.haishi.LittleRedBook.note.biz.model.vo.request.*;
import com.haishi.LittleRedBook.note.biz.model.vo.response.FindNoteDetailResponse;
import com.haishi.LittleRedBook.note.biz.model.vo.response.FindNoteIsLikedAndCollectedRspVO;
import com.haishi.LittleRedBook.note.biz.model.vo.response.FindPublishedNoteListRspVO;
import com.haishi.LittleRedBook.note.biz.model.vo.response.NoteItemRspVO;
import com.haishi.LittleRedBook.note.biz.rpc.CountRpcService;
import com.haishi.LittleRedBook.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.haishi.LittleRedBook.note.biz.rpc.KeyValueRpcService;
import com.haishi.LittleRedBook.note.biz.rpc.UserRpcService;
import com.haishi.LittleRedBook.note.biz.service.NoteService;
import com.haishi.LittleRedBook.note.biz.util.LuaUtils;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByIdResponse;
import com.haishi.framework.biz.context.holder.LoginUserContextHolder;
import com.haishi.framework.commons.exception.BizException;
import com.haishi.framework.commons.response.Response;
import com.haishi.framework.commons.util.DateUtils;
import com.haishi.framework.commons.util.JsonUtils;
import com.haishi.framework.commons.util.NumberUtils;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NoteServiceImpl implements NoteService {

    @Resource
    private CountRpcService countRpcService;

    @Resource
    private NoteCollectionDOMapper noteCollectionDOMapper;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private NoteLikeDOMapper noteLikeDOMapper;

    @Resource
    private NoteDOMapper noteDOMapper;

    @Resource
    private TopicDOMapper topicDOMapper;

    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;

    @Resource
    private KeyValueRpcService keyValueRpcService;

    @Resource
    private UserRpcService userRpcService;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 笔记详情本地缓存
     */
    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder().initialCapacity(10000) // 设置初始容量为 10000 个条目
            .maximumSize(10000) // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(1, TimeUnit.HOURS) // 设置缓存条目在写入后 1 小时过期
            .build();


    @Override
    public Response<?> publishNote(PublishNoteRequest publishNoteRequest) {
        //笔记类型
        Integer type = publishNoteRequest.getType();
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

        // 若非图文、视频，抛出业务业务异常
        if (Objects.isNull(noteTypeEnum)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        String imgUris = null;
        Boolean isContentEmpty = true;
        String videoUri = null;

        switch (noteTypeEnum) {
            case IMAGE_TEXT:
                List<String> imgUriList = publishNoteRequest.getImgUris();
                // 校验图片是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                // 校验图片数量
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于 8 张");
                // 将图片链接拼接，以逗号分隔
                imgUris = StringUtils.join(imgUriList, ",");

                break;

            case VIDEO:
                videoUri = publishNoteRequest.getVideoUri();
                // 校验视频链接是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");

                break;

            default:
                break;
        }

        //生成分布式笔记id
        String snowflakeId = distributedIdGeneratorRpcService.getSnowflakeId();
        // 笔记内容 UUID
        String contentUuid = null;

        // 笔记内容
        String content = publishNoteRequest.getContent();

        // 若用户填写了笔记内容
        if (StringUtils.isNotBlank(content)) {
            // 内容是否为空，置为 false，即不为空
            isContentEmpty = false;
        }

        //不管有没有笔记内容都生成一条记录,将笔记uuid入库
        // 生成笔记内容 UUID
        contentUuid = UUID.randomUUID().toString();
        // RPC: 调用 KV 键值服务，存储短文本
        boolean isSavedSuccess = keyValueRpcService.saveNoteContent(contentUuid, content == null ? "" : content);

        // 若存储失败，抛出业务异常，提示用户发布笔记失败
        if (!isSavedSuccess) {
            throw new BizException(ResponseCodeEnum.NOTE_PUBLISH_FAIL);
        }

        // 话题
        Long topicId = publishNoteRequest.getTopicId();
        String topicName = null;
        if (Objects.nonNull(topicId)) {
            // 获取话题名称
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);
        }

        // 发布者用户 ID
        Long creatorId = LoginUserContextHolder.getUserId();

        // 构建笔记 DO 对象
        NoteDO noteDO = NoteDO.builder().id(Long.valueOf(snowflakeId)).isContentEmpty(isContentEmpty).creatorId(creatorId).imgUris(imgUris).title(publishNoteRequest.getTitle()).topicId(publishNoteRequest.getTopicId()).topicName(topicName).type(type).visible(NoteVisibleEnum.PUBLIC.getCode()).createTime(LocalDateTime.now()).updateTime(LocalDateTime.now()).status(NoteStatusEnum.NORMAL.getCode()).isTop(Boolean.FALSE).videoUri(videoUri).contentUuid(contentUuid).build();


        // 删除个人主页 - 已发布笔记列表缓存
        // TODO: 应采取灵活的策略，如果是大V, 应该直接更新缓存，而不是直接删除；普通用户则可直接删除
        String publishedNoteListRedisKey = RedisKeyConstants.buildPublishedNoteListKey(creatorId);
        redisTemplate.delete(publishedNoteListRedisKey);

        try {
            // 笔记入库存储
            noteDOMapper.insert(noteDO);
        } catch (Exception e) {
            log.error("==> 笔记存储失败", e);

            // RPC: 笔记保存失败，则删除笔记内容
            if (StringUtils.isNotBlank(contentUuid)) {
                keyValueRpcService.deleteNoteContent(contentUuid);
            }
        }

        // 延迟双删：发送延迟消息
        sendDelayDeleteRedisCacheMQ(MQConstants.TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE, MessageBuilder.withPayload(String.valueOf(creatorId)).build());


        // 发送 MQ
        // 构建消息体 DTO
        NoteOperateMqDTO noteOperateMqDTO = NoteOperateMqDTO.builder().creatorId(creatorId).noteId(Long.valueOf(snowflakeId)).type(NoteOperateEnum.PUBLISH.getCode()) // 发布笔记
                .build();

        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(noteOperateMqDTO)).build();

        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_NOTE_OPERATE + ":" + MQConstants.TAG_NOTE_PUBLISH;

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记发布】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记发布】MQ 发送异常: ", throwable);
            }
        });

        return Response.success();

    }


    private void sendDelayDeleteRedisCacheMQ(String topic, Message<String> message) {
        rocketMQTemplate.asyncSend(topic, message, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("## 延时删除 Redis 已发布笔记缓存消息发送成功...");
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("## 延时删除 Redis 已发布笔记缓存消息发送失败...", e);
                    }
                }, 3000, // 超时时间
                1 // 延迟级别，1 表示延时 1s
        );
    }

    /**
     * 笔记详情
     *
     * @param findNoteDetailRequest
     * @return
     */
    @SneakyThrows
    @Override
    public Response<FindNoteDetailResponse> findNoteDetail(FindNoteDetailRequest findNoteDetailRequest) {

        Long noteId = findNoteDetailRequest.getId();
        Long userId = LoginUserContextHolder.getUserId();

        // 先从本地缓存中查询
        String findNoteDetailResponseStrLocalCache = LOCAL_CACHE.getIfPresent(noteId);
        if (StringUtils.isNotBlank(findNoteDetailResponseStrLocalCache)) {
            FindNoteDetailResponse findNoteDetailResponse = JsonUtils.parseObject(findNoteDetailResponseStrLocalCache, FindNoteDetailResponse.class);
            log.info("==> 命中了本地缓存；{}", findNoteDetailResponseStrLocalCache);
            checkNoteVisibleFromResponse(userId, findNoteDetailResponse);
            return Response.success(findNoteDetailResponse);
        }

        // 从 Redis 缓存中获取
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        String noteDetailJson = redisTemplate.opsForValue().get(noteDetailRedisKey);

        // 若缓存中有该笔记的数据，则直接返回
        if (StringUtils.isNotBlank(noteDetailJson)) {
            FindNoteDetailResponse findNoteDetailResponse = JsonUtils.parseObject(noteDetailJson, FindNoteDetailResponse.class);

            //异步放入本地缓存
            threadPoolTaskExecutor.execute(() -> {
                LOCAL_CACHE.put(noteId, Objects.isNull(findNoteDetailResponse) ? "null" : JsonUtils.toJsonString(findNoteDetailResponse));
            });

            // 可见性校验
            if (Objects.nonNull(findNoteDetailResponse)) {
                Integer visible = findNoteDetailResponse.getVisible();
                checkNoteVisible(visible, userId, findNoteDetailResponse.getCreatorId());
            }
            return Response.success(findNoteDetailResponse);
        }

        //查询笔记
        NoteDO noteDO = noteDOMapper.selectByPrimaryKey(noteId);

        // 若该笔记不存在，则抛出业务异常
        if (Objects.isNull(noteDO)) {
            threadPoolTaskExecutor.execute(() -> {
                // 防止缓存穿透，将空数据存入 Redis 缓存 (过期时间不宜设置过长)
                // 保底1分钟 + 随机秒数
                long expireSeconds = 60 + RandomUtil.randomInt(60);
                redisTemplate.opsForValue().set(noteDetailRedisKey, "null", expireSeconds, TimeUnit.SECONDS);
            });
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 可见性校验
        Integer visible = noteDO.getVisible();
        checkNoteVisible(visible, userId, noteDO.getCreatorId());

        //RPC:查询作者信息
        Long creatorId = noteDO.getCreatorId();
        CompletableFuture<FindUserByIdResponse> userResultFuture = CompletableFuture.supplyAsync(() -> userRpcService.findById(creatorId), threadPoolTaskExecutor);

        //RPC:查询笔记内容
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);
        if (Objects.equals(noteDO.getIsContentEmpty(), Boolean.FALSE)) {
            contentResultFuture = CompletableFuture.supplyAsync(() -> keyValueRpcService.findNoteContent(noteDO.getContentUuid()), threadPoolTaskExecutor);
        }

        CompletableFuture<String> finalContentResultFuture = contentResultFuture;

        CompletableFuture<FindNoteDetailResponse> resultFuture = CompletableFuture.allOf(userResultFuture, contentResultFuture).thenApply(s -> {
            // 获取 Future 返回的结果
            FindUserByIdResponse response = userResultFuture.join();
            String content = finalContentResultFuture.join();

            // 笔记类型
            Integer noteType = noteDO.getType();
            // 图文笔记图片链接(字符串)
            String imgUrisStr = noteDO.getImgUris();
            // 图文笔记图片链接(集合)
            List<String> imgUris = null;
            // 如果查询的是图文笔记，需要将图片链接的逗号分隔开，转换成集合
            if (Objects.equals(noteType, NoteTypeEnum.IMAGE_TEXT.getCode()) && StringUtils.isNotBlank(imgUrisStr)) {
                imgUris = List.of(imgUrisStr.split(","));
            }

            // 构建返参 VO 实体类
            return FindNoteDetailResponse.builder().id(noteDO.getId()).type(noteDO.getType()).title(noteDO.getTitle()).content(content).imgUris(imgUris).topicId(noteDO.getTopicId()).topicName(noteDO.getTopicName()).creatorId(userId).creatorName(response.getNickName()).avatar(response.getAvatar()).videoUri(noteDO.getVideoUri()).updateTime(noteDO.getUpdateTime()).visible(noteDO.getVisible()).build();

        });

        // 获取拼装后的 FindNoteDetailRspVO
        FindNoteDetailResponse findNoteDetailResponse = resultFuture.get();


        // 异步线程中将笔记详情存入 Redis
        threadPoolTaskExecutor.execute(() -> {
            String noteResponseJson = JsonUtils.toJsonString(findNoteDetailResponse);
            // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
            redisTemplate.opsForValue().set(noteDetailRedisKey, noteResponseJson, expireSeconds, TimeUnit.SECONDS);
        });

        return Response.success(findNoteDetailResponse);
    }


    /**
     * 笔记更新
     *
     * @param request
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> updateNote(UpdateNoteRequest request) {
        //笔记id
        Long noteId = request.getId();
        //笔记类型
        Integer type = request.getType();
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);
        //评论内容
        String content = request.getContent();
        // 话题
        Long topicId = request.getTopicId();

        // 若非图文、视频，抛出业务业务异常
        if (Objects.isNull(noteTypeEnum)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        String imgUris = null;
        String videoUri = null;
        switch (noteTypeEnum) {
            case IMAGE_TEXT: // 图文笔记
                List<String> imgUriList = request.getImgUris();
                // 校验图片是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                // 校验图片数量
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于 8 张");

                imgUris = StringUtils.join(imgUriList, ",");
                break;
            case VIDEO: // 视频笔记
                videoUri = request.getVideoUri();
                // 校验视频链接是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            default:
                break;
        }

        // 当前登录用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();
        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);

        // 笔记不存在
        if (Objects.isNull(selectNoteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 判断权限：非笔记发布者不允许更新笔记
        if (!Objects.equals(currUserId, selectNoteDO.getCreatorId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        // 删除 Redis 缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        String publishedNoteListRedisKey = RedisKeyConstants.buildPublishedNoteListKey(currUserId);
        redisTemplate.delete(Arrays.asList(noteDetailRedisKey, publishedNoteListRedisKey));


        CompletableFuture<Boolean> updateKV = CompletableFuture.supplyAsync(() -> {
            // 笔记内容更新
            // 查询此篇笔记内容对应的 UUID
            NoteDO noteDO1 = noteDOMapper.selectByPrimaryKey(noteId);
            String contentUuid = noteDO1.getContentUuid();

            // 笔记内容是否更新成功
            boolean isUpdateContentSuccess = false;
            if (StringUtils.isBlank(content)) {
                // 若笔记内容为空，则删除 K-V 存储
//            isUpdateContentSuccess = keyValueRpcService.deleteNoteContent(contentUuid);
                //更改：为空则清空笔记内容
                isUpdateContentSuccess = keyValueRpcService.saveNoteContent(contentUuid, "");
            } else {
                // 调用 K-V 更新短文本
                isUpdateContentSuccess = keyValueRpcService.saveNoteContent(contentUuid, content);
            }
            return isUpdateContentSuccess;
        }, threadPoolTaskExecutor);


        String topicName = null;
        if (Objects.nonNull(topicId)) {
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);

            // 判断一下提交的话题, 是否是真实存在的
            if (StringUtils.isBlank(topicName)) throw new BizException(ResponseCodeEnum.TOPIC_NOT_FOUND);
        }

        // 更新笔记元数据表 t_note

        NoteDO noteDO = NoteDO.builder().id(noteId).isContentEmpty(StringUtils.isBlank(content)).imgUris(imgUris).title(request.getTitle()).topicId(request.getTopicId()).topicName(topicName).type(type).updateTime(LocalDateTime.now()).videoUri(videoUri).build();

        noteDOMapper.updateByPrimaryKey(noteDO);

        Boolean isUpdateContentSuccess = false;
        try {
            isUpdateContentSuccess = updateKV.join(); // 异常会被包装成 CompletionException
        } catch (CompletionException e) {
            throw new BizException(ResponseCodeEnum.NOTE_UPDATE_FAIL);
        }
        // 如果更新失败，抛出业务异常，回滚事务
        if (!isUpdateContentSuccess) {
            throw new BizException(ResponseCodeEnum.NOTE_UPDATE_FAIL);
        }


        // 一致性保证：延迟双删策略
        // 异步发送延时消息
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(Arrays.asList(noteId, currUserId))).build();
        sendDelayDeleteRedisCacheMQ(MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE, message);

        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        SendResult sendResult = rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info(sendResult.toString());
        log.info("====> MQ：删除笔记本地缓存发送成功...");


        return Response.success();
    }

    /**
     * 删除笔记
     *
     * @param deleteNoteRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> deleteNote(DeleteNoteRequest deleteNoteRequest) {
        // 笔记 ID
        Long noteId = deleteNoteRequest.getId();

        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);

        // 判断笔记是否存在
        if (Objects.isNull(selectNoteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 判断权限：非笔记发布者不允许删除笔记
        Long currUserId = LoginUserContextHolder.getUserId();
        if (!Objects.equals(currUserId, selectNoteDO.getCreatorId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        // 删除缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        String publishedNoteListRedisKey = RedisKeyConstants.buildPublishedNoteListKey(currUserId);
        redisTemplate.delete(Arrays.asList(noteDetailRedisKey, publishedNoteListRedisKey));

        // 逻辑删除
        NoteDO noteDO = NoteDO.builder().id(noteId).status(NoteStatusEnum.DELETED.getCode()).updateTime(LocalDateTime.now()).build();

        int count = noteDOMapper.updateByPrimaryKeySelective(noteDO);

        // 若影响的行数为 0，则表示该笔记不存在
        if (count == 0) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 延迟双删
        sendDelayDeleteRedisCacheMQ(MQConstants.TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE, MessageBuilder.withPayload(String.valueOf(currUserId)).build());

        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        SendResult sendResult = rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info(sendResult.toString());
        log.info("====> MQ：删除笔记本地缓存发送成功...");

        // 发送 MQ
        // 构建消息体 DTO
        NoteOperateMqDTO noteOperateMqDTO = NoteOperateMqDTO.builder().creatorId(selectNoteDO.getCreatorId()).noteId(noteId).type(NoteOperateEnum.DELETE.getCode()) // 删除笔记
                .build();

        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(noteOperateMqDTO)).build();

        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_NOTE_OPERATE + ":" + MQConstants.TAG_NOTE_DELETE;

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记删除】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记删除】MQ 发送异常: ", throwable);
            }
        });

        return Response.success();
    }


    /**
     * 笔记仅对自己可见
     *
     * @param updateNoteVisibleOnlyMeRequest
     * @return
     */
    @Override
    public Response<?> visibleOnlyMe(UpdateNoteVisibleOnlyMeRequest updateNoteVisibleOnlyMeRequest) {
        // 笔记 ID
        Long noteId = updateNoteVisibleOnlyMeRequest.getId();

        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);


        // 笔记不存在
        if (Objects.isNull(selectNoteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 判断权限：非笔记发布者不允许更新笔记
        Long currUserId = LoginUserContextHolder.getUserId();
        if (!Objects.equals(currUserId, selectNoteDO.getCreatorId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        // 构建更新 DO 实体类
        NoteDO noteDO = NoteDO.builder().id(noteId).visible(NoteVisibleEnum.PRIVATE.getCode()) // 可见性设置为仅对自己可见
                .updateTime(LocalDateTime.now()).build();

        // 执行更新 SQL
        int count = noteDOMapper.updateVisibleOnlyMe(noteDO);

        // 若影响的行数为 0，则表示该笔记无法修改为仅自己可见
        if (count == 0) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_VISIBLE_ONLY_ME);
        }

        DeleteNoteCache(noteId);

        return Response.success();
    }

    //TODO: 提成自调用不影响@Transactional的方法(获取代理对象)
    private void checkHasPermissionOfNote(Long noteId) {
        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);

        // 笔记不存在
        if (Objects.isNull(selectNoteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 判断权限：非笔记发布者不允许更新笔记
        Long currUserId = LoginUserContextHolder.getUserId();
        if (!Objects.equals(currUserId, selectNoteDO.getCreatorId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }
    }

    /**
     * 笔记置顶 / 取消置顶
     *
     * @param topNoteRequest
     * @return
     */
    @Override
    public Response<?> topNote(TopNoteRequest topNoteRequest) {
        // 笔记 ID
        Long noteId = topNoteRequest.getId();
        // 是否置顶
        Boolean isTop = topNoteRequest.getIsTop();

        // 当前登录用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();

        // 构建置顶/取消置顶 DO 实体类
        NoteDO noteDO = NoteDO.builder().id(noteId).isTop(isTop).updateTime(LocalDateTime.now()).creatorId(currUserId) // 只有笔记所有者，才能置顶/取消置顶笔记
                .build();

        int count = noteDOMapper.updateIsTop(noteDO);

        if (count == 0) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        DeleteNoteCache(noteId);

        return Response.success();
    }


    /**
     * 点赞笔记
     *
     * @param likeNoteRequest
     * @return
     */
    @Override
    public Response<?> likeNote(LikeNoteRequest likeNoteRequest) {

        Long noteId = likeNoteRequest.getId();

        // 1. 校验被点赞的笔记是否存在，若存在，则获取发布者用户 ID
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);

        // 2. 判断目标笔记，是否已经点赞过
        Long userId = LoginUserContextHolder.getUserId();

        // Roaring Bitmap Key
        String rbitmapUserNoteLikeListKey = RedisKeyConstants.buildRBitmapUserNoteLikeListKey(userId);

        // lua脚本
        DefaultRedisScript<Long> script = LuaUtils.getLuaScript("/lua/rbitmap_note_like_check.lua", Long.class);
        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteLikeListKey), noteId);

        NoteLikeLuaResultEnum noteLikeLuaResultEnum = NoteLikeLuaResultEnum.valueOf(result);

        // 用户点赞列表 ZSet Key
        String userNoteLikeZSetKey = RedisKeyConstants.buildUserNoteLikeZSetKey(userId);

        switch (noteLikeLuaResultEnum) {
            // Redis 中布隆过滤器不存在
            case NOT_EXIST -> {
                // 从数据库中校验笔记是否被点赞，并异步初始化布隆过滤器，设置过期时间
                int count = noteLikeDOMapper.selectNoteIsLiked(userId, noteId);

                // 保底1天+随机秒数
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

                // 目标笔记已经被点赞
                if (count > 0) {
                    // 异步初始化 Roaring Bitmap
                    threadPoolTaskExecutor.submit(() -> batchAddNoteLike2RBitmapAndExpire(userId, expireSeconds, rbitmapUserNoteLikeListKey));
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_LIKED);
                }

                // 若目标笔记未被点赞，查询当前用户是否有点赞其他笔记，有则同步初始化 Roaring Bitmap
                batchAddNoteLike2RBitmapAndExpire(userId, expireSeconds, rbitmapUserNoteLikeListKey);

                //lua脚本
                script = LuaUtils.getLuaScript("/lua/rbitmap_add_note_like_and_expire.lua", Long.class);
                redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteLikeListKey), noteId, expireSeconds);
            }
            // 目标笔记已经被点赞 可能是布隆过滤器误判
            case NOTE_LIKED -> {
                //roaring map不會被誤判
//                // 校验 ZSet 列表中是否包含被点赞的笔记ID
//                Double score = redisTemplate.opsForZSet().score(userNoteLikeZSetKey, noteId);
//
//                if (Objects.nonNull(score)) {
//                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_LIKED);
//                }
//
//                // 若 Score 为空，则表示 ZSet 点赞列表中不存在，查询数据库校验
//                int count = noteLikeDOMapper.selectNoteIsLiked(userId, noteId);
//
//                if (count > 0) {
//                    // 数据库里面有点赞记录，而 Redis 中 ZSet 不存在，需要重新异步初始化 ZSet
//                    asynInitUserNoteLikesZSet(userId, userNoteLikeZSetKey);

                throw new BizException(ResponseCodeEnum.NOTE_ALREADY_LIKED);
//                }
            }
        }

        // 3. 更新用户 ZSET 点赞列表
        LocalDateTime now = LocalDateTime.now();
        // Lua 脚本
        LuaUtils.getLuaScript("/lua/note_like_check_and_update_zset.lua", Long.class);
        // 执行 Lua 脚本，拿到返回结果
        result = redisTemplate.execute(script, Collections.singletonList(userNoteLikeZSetKey), noteId, DateUtils.localDateTime2Timestamp(now));

        // 若 ZSet 列表不存在，需要重新初始化
        if (Objects.equals(result, NoteLikeLuaResultEnum.NOT_EXIST.getCode())) {
            // 查询当前用户最新点赞的 100 篇笔记
            List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectLikedByUserIdAndLimit(userId, 100);

            if (CollUtil.isNotEmpty(noteLikeDOS)) {
                // 保底1天+随机秒数
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                // 构建 Lua 参数
                Object[] luaArgs = buildNoteLikeZSetLuaArgs(noteLikeDOS, expireSeconds);

                // Lua 脚本
                DefaultRedisScript<Long> script2 = LuaUtils.getLuaScript("/lua/batch_add_note_like_zset_and_expire.lua", Long.class);

                redisTemplate.execute(script2, Collections.singletonList(userNoteLikeZSetKey), luaArgs);

                // 再次调用 note_like_check_and_update_zset.lua 脚本，将点赞的笔记添加到 zset 中
                redisTemplate.execute(script, Collections.singletonList(userNoteLikeZSetKey), noteId, DateUtils.localDateTime2Timestamp(now));
            }
        }

        // 4. 发送 MQ, 将点赞数据落库
        LikeUnlikeNoteMqDTO likeUnlikeNoteMqDTO = LikeUnlikeNoteMqDTO.builder().noteId(noteId).userId(userId).type(LikeUnlikeNoteTypeEnum.LIKE.getCode()).createTime(now).noteCreatorId(creatorId) // 笔记发布者 ID
                .build();

        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(likeUnlikeNoteMqDTO)).build();

        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_LIKE_OR_UNLIKE + ":" + MQConstants.TAG_LIKE;

        String hashKey = String.valueOf(userId);

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记点赞】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记点赞】MQ 发送异常: ", throwable);
            }
        });


        return Response.success();
    }

    /**
     * 初始化笔记点赞 Roaring Bitmap
     *
     * @param userId
     * @param expireSeconds
     * @param rbitmapUserNoteLikeListKey
     */
    private void batchAddNoteLike2RBitmapAndExpire(Long userId, long expireSeconds, String rbitmapUserNoteLikeListKey) {
        try {
            // 异步全量同步一下，并设置过期时间
            List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectByUserId(userId);

            if (CollUtil.isNotEmpty(noteLikeDOS)) {
                DefaultRedisScript<Long> script = LuaUtils.getLuaScript("/lua/rbitmap_batch_add_note_like_and_expire.lua", Long.class);

                // 构建 Lua 参数
                List<Object> luaArgs = Lists.newArrayList();
                noteLikeDOS.forEach(noteLikeDO -> luaArgs.add(noteLikeDO.getNoteId())); // 将每个点赞的笔记 ID 传入
                luaArgs.add(expireSeconds);  // 最后一个参数是过期时间（秒）
                redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteLikeListKey), luaArgs.toArray());
            }
        } catch (Exception e) {
            log.error("## 异步初始化【笔记点赞】Roaring Bitmap 异常: ", e);
        }
    }


    /**
     * 取消点赞笔记
     *
     * @param unlikeNoteRequest
     * @return
     */
    @Override
    public Response<?> unlikeNote(UnlikeNoteRequest unlikeNoteRequest) {
        // 笔记ID
        Long noteId = unlikeNoteRequest.getId();

        // 1. 校验笔记是否真实存在，若存在，则获取发布者用户 ID
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);

        // 2. 校验笔记是否被点赞过
        // 当前登录用户ID
        Long userId = LoginUserContextHolder.getUserId();

        // Roaring Bitmap Key
        String rbitmapUserNoteLikeListKey = RedisKeyConstants.buildRBitmapUserNoteLikeListKey(userId);

        DefaultRedisScript<Long> script = LuaUtils.getLuaScript("/lua/rbitmap_note_unlike_check.lua", Long.class);

        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteLikeListKey), noteId);

        NoteUnlikeLuaResultEnum noteUnlikeLuaResultEnum = NoteUnlikeLuaResultEnum.valueOf(result);

        switch (noteUnlikeLuaResultEnum) {
            // 布隆过滤器不存在
            case NOT_EXIST -> {
                // 异步初始化 Roaring Bitmap
                threadPoolTaskExecutor.submit(() -> {
                    // 保底1天+随机秒数
                    long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                    batchAddNoteLike2RBitmapAndExpire(userId, expireSeconds, rbitmapUserNoteLikeListKey);
                });

                // 从数据库中校验笔记是否被点赞
                int count = noteLikeDOMapper.selectNoteIsLiked(userId, noteId);

                // 未点赞，无法取消点赞操作，抛出业务异常
                if (count == 0) throw new BizException(ResponseCodeEnum.NOTE_NOT_LIKED);
            }
            // 校验目标笔记未被点赞（判断绝对正确）
            case NOTE_NOT_LIKED -> throw new BizException(ResponseCodeEnum.NOTE_NOT_LIKED);
        }


        // 3. 能走到这里，说明布隆过滤器判断已点赞，直接删除 ZSET 中已点赞的笔记 ID
        // 用户点赞列表 ZSet Key
        String userNoteLikeZSetKey = RedisKeyConstants.buildUserNoteLikeZSetKey(userId);

        redisTemplate.opsForZSet().remove(userNoteLikeZSetKey, noteId);

        // 4. 发送 MQ, 数据更新落库
        // 构建消息体 DTO
        LikeUnlikeNoteMqDTO likeUnlikeNoteMqDTO = LikeUnlikeNoteMqDTO.builder().userId(userId).noteId(noteId).type(LikeUnlikeNoteTypeEnum.UNLIKE.getCode()) // 取消点赞笔记
                .createTime(LocalDateTime.now()).noteCreatorId(creatorId) // 笔记发布者 ID
                .build();

        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(likeUnlikeNoteMqDTO)).build();

        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_LIKE_OR_UNLIKE + ":" + MQConstants.TAG_UNLIKE;

        String hashKey = String.valueOf(userId);

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记取消点赞】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记取消点赞】MQ 发送异常: ", throwable);
            }
        });

        return Response.success();
    }


    /**
     * 收藏笔记
     *
     * @param collectNoteReqVO
     * @return
     */
    @Override
    public Response<?> collectNote(CollectNoteReqVO collectNoteReqVO) {
        // 笔记ID
        Long noteId = collectNoteReqVO.getId();

        // 1. 校验被收藏的笔记是否存在
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);

        // 2. 判断目标笔记，是否已经收藏过
        // 当前登录用户ID
        Long userId = LoginUserContextHolder.getUserId();

        // Roaring Bitmap Key
        String rbitmapUserNoteCollectListKey = RedisKeyConstants.buildRBitmapUserNoteCollectListKey(userId);

        DefaultRedisScript<Long> script = LuaUtils.getLuaScript("/lua/rbitmap_note_collect_check.lua", Long.class);

        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteCollectListKey), noteId);

        NoteCollectLuaResultEnum noteCollectLuaResultEnum = NoteCollectLuaResultEnum.valueOf(result);

        String userNoteCollectZSetKey = RedisKeyConstants.buildUserNoteCollectZSetKey(userId);

        switch (noteCollectLuaResultEnum) {
            // Redis 中Roaring Bitmap不存在
            case NOT_EXIST -> {
                // 从数据库中校验笔记是否被收藏，并异步初始化布隆过滤器，设置过期时间
                int count = noteCollectionDOMapper.selectNoteIsCollected(userId, noteId);

                // 保底1天+随机秒数
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

                // 目标笔记已经被收藏
                if (count > 0) {
                    // 异步初始化Roaring Bitmap
                    threadPoolTaskExecutor.submit(() -> batchAddNoteCollect2RBitmapAndExpire(userId, expireSeconds, rbitmapUserNoteCollectListKey));
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_COLLECTED);
                }

                // 若目标笔记未被收藏，查询当前用户是否有收藏其他笔记，有则同步初始化 Roaring Bitmap
                batchAddNoteCollect2RBitmapAndExpire(userId, expireSeconds, rbitmapUserNoteCollectListKey);

                // 添加当前收藏笔记 ID 到Roaring Bitmap 中
                // Lua 脚本路径
                script = LuaUtils.getLuaScript("/lua/rbitmap_add_note_collect_and_expire.lua", Long.class);
                redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteCollectListKey), noteId, expireSeconds);
            }
            // 目标笔记已经被收藏
            case NOTE_COLLECTED -> {
                // 校验 ZSet 列表中是否包含被收藏的笔记ID
//                Double score = redisTemplate.opsForZSet().score(userNoteCollectZSetKey, noteId);
//
//                if (Objects.nonNull(score)) {
//                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_COLLECTED);
//                }
//
//                // 若 Score 为空，则表示 ZSet 收藏列表中不存在，查询数据库校验
//                int count = noteCollectionDOMapper.selectNoteIsCollected(userId, noteId);
//
//                if (count > 0) {
//                    // 数据库里面有收藏记录，而 Redis 中 ZSet 未初始化，需要重新异步初始化 ZSet
//                    threadPoolTaskExecutor.execute(() -> {
//                        initUserNoteCollectsZSet(userId, userNoteCollectZSetKey);
//                    });
//
//                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_COLLECTED);
//                }

                throw new BizException(ResponseCodeEnum.NOTE_ALREADY_COLLECTED);
            }
        }

        // 3. 更新用户 ZSET 收藏列表
        LocalDateTime now = LocalDateTime.now();
        // Lua 脚本
        script = LuaUtils.getLuaScript("/lua/note_collect_check_and_update_zset.lua", Long.class);
        // 执行 Lua 脚本，拿到返回结果
        result = redisTemplate.execute(script, Collections.singletonList(userNoteCollectZSetKey), noteId, DateUtils.localDateTime2Timestamp(now));

        // 若 ZSet 列表不存在，需要重新初始化
        if (Objects.equals(result, NoteCollectLuaResultEnum.NOT_EXIST.getCode())) {
            initUserNoteCollectsZSet(userId, userNoteCollectZSetKey);
        }

        // 4. 发送 MQ, 将收藏数据落库
        // 构建消息体 DTO
        CollectUnCollectNoteMqDTO collectUnCollectNoteMqDTO = CollectUnCollectNoteMqDTO.builder().userId(userId).noteId(noteId).type(CollectUnCollectNoteTypeEnum.COLLECT.getCode()) // 收藏笔记
                .createTime(now).noteCreatorId(creatorId) // 笔记发布者 ID
                .build();

        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(collectUnCollectNoteMqDTO)).build();

        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_COLLECT_OR_UN_COLLECT + ":" + MQConstants.TAG_COLLECT;

        String hashKey = String.valueOf(userId);

        // 异步发送顺序 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记收藏】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记收藏】MQ 发送异常: ", throwable);
            }
        });

        return Response.success();
    }


    /**
     * 取消收藏笔记
     *
     * @param unCollectNoteReqVO
     * @return
     */
    @Override
    public Response<?> unCollectNote(UnCollectNoteReqVO unCollectNoteReqVO) {
        // 笔记ID
        Long noteId = unCollectNoteReqVO.getId();

        // 1. 校验笔记是否真实存在
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);

        // 2. 校验笔记是否被收藏过
        // 当前登录用户ID
        Long userId = LoginUserContextHolder.getUserId();

        // Roaring Bitmap Key
        String rbitmapUserNoteCollectListKey = RedisKeyConstants.buildRBitmapUserNoteCollectListKey(userId);

        DefaultRedisScript<Long> script = LuaUtils.getLuaScript("/lua/rbitmap_note_uncollect_check.lua", Long.class);
        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteCollectListKey), noteId);

        NoteUnCollectLuaResultEnum noteUnCollectLuaResultEnum = NoteUnCollectLuaResultEnum.valueOf(result);

        switch (noteUnCollectLuaResultEnum) {
            // Roaring Bitmap 不存在
            case NOT_EXIST -> {
                // 异步初始化 Roaring Bitmap
                threadPoolTaskExecutor.submit(() -> {
                    // 保底1天+随机秒数
                    long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                    batchAddNoteCollect2RBitmapAndExpire(userId, expireSeconds, rbitmapUserNoteCollectListKey);
                });

                // 从数据库中校验笔记是否被收藏
                int count = noteCollectionDOMapper.selectNoteIsCollected(userId, noteId);

                // 未收藏，无法取消收藏操作，抛出业务异常
                if (count == 0) throw new BizException(ResponseCodeEnum.NOTE_NOT_COLLECTED);
            }
            // 校验目标笔记未被收藏
            case NOTE_NOT_COLLECTED -> throw new BizException(ResponseCodeEnum.NOTE_NOT_COLLECTED);
        }

        // 3. 删除 ZSET 中已收藏的笔记 ID
        // 能走到这里，说明 Roaring Bitmap 判断已收藏，直接删除 ZSET 中已收藏的笔记 ID
        // 用户收藏列表 ZSet Key
        String userNoteCollectZSetKey = RedisKeyConstants.buildUserNoteCollectZSetKey(userId);

        redisTemplate.opsForZSet().remove(userNoteCollectZSetKey, noteId);


        // 4. 发送 MQ, 数据更新落库
        // 构建消息体 DTO
        CollectUnCollectNoteMqDTO unCollectNoteMqDTO = CollectUnCollectNoteMqDTO.builder().userId(userId).noteId(noteId).type(CollectUnCollectNoteTypeEnum.UN_COLLECT.getCode()) // 取消收藏笔记
                .createTime(LocalDateTime.now()).noteCreatorId(creatorId) // 笔记发布者 ID
                .build();

        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(unCollectNoteMqDTO)).build();

        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_COLLECT_OR_UN_COLLECT + ":" + MQConstants.TAG_UN_COLLECT;

        String hashKey = String.valueOf(userId);

        // 异步发送顺序 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记取消收藏】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记取消收藏】MQ 发送异常: ", throwable);
            }
        });

        return Response.success();
    }


    /**
     * 初始化笔记收藏布隆过滤器
     *
     * @param userId
     * @param expireSeconds
     * @param rbitmapUserNoteCollectListKey
     */
    private void batchAddNoteCollect2RBitmapAndExpire(Long userId, long expireSeconds, String rbitmapUserNoteCollectListKey) {
        try {
            // 异步全量同步一下，并设置过期时间
            List<NoteCollectionDO> noteCollectionDOS = noteCollectionDOMapper.selectByUserId(userId);

            if (CollUtil.isNotEmpty(noteCollectionDOS)) {
                DefaultRedisScript<Long> script = LuaUtils.getLuaScript("/lua/rbitmap_batch_add_note_collect_and_expire.lua", Long.class);

                // 构建 Lua 参数
                List<Object> luaArgs = Lists.newArrayList();
                noteCollectionDOS.forEach(noteCollectionDO -> luaArgs.add(noteCollectionDO.getNoteId())); // 将每个收藏的笔记 ID 传入
                luaArgs.add(expireSeconds);  // 最后一个参数是过期时间（秒）
                redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteCollectListKey), luaArgs.toArray());
            }
        } catch (Exception e) {
            log.error("## 异步初始化【笔记收藏】Roaring Bitmap 异常: ", e);
        }
    }


    /**
     * 获取是否点赞、收藏数据
     *
     * @param findNoteIsLikedAndCollectedReqVO
     * @return
     */
    @Override
    public Response<FindNoteIsLikedAndCollectedRspVO> isLikedAndCollectedData(FindNoteIsLikedAndCollectedReqVO findNoteIsLikedAndCollectedReqVO) {
        Long noteId = findNoteIsLikedAndCollectedReqVO.getNoteId();

        // 已登录的用户 ID
        Long currUserId = LoginUserContextHolder.getUserId();

        // 默认未点赞、未收藏
        boolean isLiked = false;
        boolean isCollected = false;

        // 若当前用户已登录
        if (Objects.nonNull((currUserId))) {
            // 1. 校验是否点赞
            isLiked = checkNoteIsLiked(noteId, currUserId);

            // 2. 校验是否收藏
            isCollected = checkNoteIsCollected(noteId, currUserId);
        }

        return Response.success(FindNoteIsLikedAndCollectedRspVO.builder().noteId(noteId).isLiked(isLiked).isCollected(isCollected).build());
    }


    /**
     * 用户主页 - 查询已发布的笔记列表
     *
     * @param findPublishedNoteListReqVO
     * @return
     */
    @Override
    public Response<FindPublishedNoteListRspVO> findPublishedNoteList(FindPublishedNoteListReqVO findPublishedNoteListReqVO) {

        // 目标用户ID
        Long userId = findPublishedNoteListReqVO.getUserId();
        // 游标
        Long cursor = findPublishedNoteListReqVO.getCursor();

        // 返参 VO
        FindPublishedNoteListRspVO findPublishedNoteListRspVO = null;

        // 优先查询缓存
        // 构建 Redis Key
        String publishedNoteListRedisKey = RedisKeyConstants.buildPublishedNoteListKey(userId);
        // 若游标为空，表示查询的是第一页
        if (Objects.isNull(cursor)) {
            String publishedNoteListJson = redisTemplate.opsForValue().get(publishedNoteListRedisKey);

            if (StringUtils.isNotBlank(publishedNoteListJson)) {
                try {
                    log.info("## 已发布笔记列表命中了 Redis 缓存...");
                    // Json 字符串转 VO 集合
                    List<NoteItemRspVO> noteItemRspVOS = JsonUtils.parseList(publishedNoteListJson, NoteItemRspVO.class);
                    // 按笔记 ID 降序，最新发布的笔记排最前面
                    List<NoteItemRspVO> sortedList = noteItemRspVOS.stream().sorted(Comparator.comparing(NoteItemRspVO::getNoteId).reversed()).toList();

                    // 过滤出最早发布的笔记 ID，充当下一页的游标
                    Optional<Long> earliestNoteId = noteItemRspVOS.stream().map(NoteItemRspVO::getNoteId).min(Long::compareTo);

                    findPublishedNoteListRspVO = FindPublishedNoteListRspVO.builder().notes(sortedList).nextCursor(earliestNoteId.orElse(null)).build();
                    return Response.success(findPublishedNoteListRspVO);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }


        // 缓存无，则查询数据库
        List<NoteDO> noteDOS = noteDOMapper.selectPublishedNoteListByUserIdAndCursor(userId, cursor);


        if (CollUtil.isNotEmpty(noteDOS)) {
            // DO 转 VO
            List<NoteItemRspVO> noteVOS = noteDOS.stream().map(noteDO -> {
                // 获取封面图片
                String cover = StringUtils.isNotBlank(noteDO.getImgUris()) ? StringUtils.split(noteDO.getImgUris(), ",")[0] : null;

                NoteItemRspVO noteItemRspVO = NoteItemRspVO.builder().noteId(noteDO.getId()).type(noteDO.getType()).creatorId(noteDO.getCreatorId()).cover(cover).videoUri(noteDO.getVideoUri()).title(noteDO.getTitle()).build();
                return noteItemRspVO;
            }).toList();

            // Feign 调用用户服务，获取博主的用户头像、昵称
            CompletableFuture<FindUserByIdResponse> userFuture = CompletableFuture.supplyAsync(() -> {
                Optional<Long> creatorIdOptional = noteDOS.stream().map(NoteDO::getCreatorId).findAny();
                return userRpcService.findById(creatorIdOptional.get());
            }, threadPoolTaskExecutor);

            // Feign 调用计数服务，批量获取笔记点赞数
            CompletableFuture<List<FindNoteCountsByIdRspDTO>> noteCountFuture = CompletableFuture.supplyAsync(() -> {
                List<Long> noteIds = noteDOS.stream().map(NoteDO::getId).toList();
                return countRpcService.findByNoteIds(noteIds);
            }, threadPoolTaskExecutor);

            // 等待所有任务完成，并合并结果
            CompletableFuture.allOf(userFuture, noteCountFuture).join();


            try {
                // 获取 Future 返回结果
                FindUserByIdResponse findUserByIdRspDTO = userFuture.get();
                List<FindNoteCountsByIdRspDTO> findNoteCountsByIdRspDTOS = noteCountFuture.get();

                if (Objects.nonNull(findUserByIdRspDTO)) {
                    // 循环 VO 集合，分别设置头像、昵称
                    noteVOS.forEach(noteItemRspVO -> {
                        noteItemRspVO.setAvatar(findUserByIdRspDTO.getAvatar());
                        noteItemRspVO.setNickname(findUserByIdRspDTO.getNickName());
                    });
                }

                if (CollUtil.isNotEmpty(findNoteCountsByIdRspDTOS)) {
                    // DTO 集合转 Map
                    Map<Long, FindNoteCountsByIdRspDTO> noteIdAndDTOMap = findNoteCountsByIdRspDTOS.stream().collect(Collectors.toMap(FindNoteCountsByIdRspDTO::getNoteId, dto -> dto));

                    // 循环设置 VO 集合，设置每篇笔记的点赞量
                    noteVOS.forEach(noteItemRspVO -> {
                        Long currNoteId = noteItemRspVO.getNoteId();
                        FindNoteCountsByIdRspDTO findNoteCountsByIdRspDTO = noteIdAndDTOMap.get(currNoteId);
                        noteItemRspVO.setLikeTotal((Objects.nonNull(findNoteCountsByIdRspDTO) && Objects.nonNull(findNoteCountsByIdRspDTO.getLikeTotal())) ? NumberUtils.formatNumberString(findNoteCountsByIdRspDTO.getLikeTotal()) : "0");
                    });
                }
            } catch (Exception e) {
                log.error("## 并发调用错误: ", e);
            }


            // 过滤出最早发布的笔记 ID，充当下一页的游标
            Optional<Long> earliestNoteId = noteDOS.stream().map(NoteDO::getId).min(Long::compareTo);

            findPublishedNoteListRspVO = FindPublishedNoteListRspVO.builder().notes(noteVOS).nextCursor(earliestNoteId.orElse(null)).build();


            // 同步第一页已发布笔记到 Redis
            syncFirstPagePublishedNoteList2Redis(noteVOS, publishedNoteListRedisKey);

        }

        return Response.success(findPublishedNoteListRspVO);
    }

    /**
     * 同步第一页已发布笔记到 Redis
     *
     * @param noteVOS
     * @param publishedNoteListRedisKey
     */
    private void syncFirstPagePublishedNoteList2Redis(List<NoteItemRspVO> noteVOS, String publishedNoteListRedisKey) {
        if (CollUtil.isEmpty(noteVOS)) return;
        // 异步同步缓存
        threadPoolTaskExecutor.submit(() -> {
            // 过期时间，一小时以内（保底30分钟+随机秒数）
            long expireSeconds = 60 * 30 + RandomUtil.randomInt(60 * 30);
            redisTemplate.opsForValue().set(publishedNoteListRedisKey, JsonUtils.toJsonString(noteVOS), expireSeconds, TimeUnit.SECONDS);
        });
    }

    /**
     * 校验当前用户是否点赞笔记
     *
     * @param noteId
     * @param currUserId
     * @return
     */
    private boolean checkNoteIsLiked(Long noteId, Long currUserId) {
        // 是否点赞
        boolean isLiked = false;

        // Roaring Bitmap Key
        String rbitmapUserNoteLikeListKey = RedisKeyConstants.buildRBitmapUserNoteLikeListKey(currUserId);

        DefaultRedisScript<Long> script = LuaUtils.getLuaScript("/lua/rbitmap_note_like_only_check.lua", Long.class);

        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteLikeListKey), noteId);

        NoteLikeLuaResultEnum noteLikeLuaResultEnum = NoteLikeLuaResultEnum.valueOf(result);

        switch (noteLikeLuaResultEnum) {
            // Redis 中 Roaring Bitmap 不存在
            case NOT_EXIST -> {
                // 从数据库中校验笔记是否被点赞，并异步初始化 Roaring Bitmap，设置过期时间
                int count = noteLikeDOMapper.selectNoteIsLiked(currUserId, noteId);

                // 保底1天+随机秒数
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

                // 目标笔记已经被点赞
                if (count > 0) {
                    // 异步初始化 Roaring Bitmap
                    threadPoolTaskExecutor.submit(() -> batchAddNoteLike2RBitmapAndExpire(currUserId, expireSeconds, rbitmapUserNoteLikeListKey));
                    isLiked = true;
                }
            }
            case NOTE_LIKED -> isLiked = true; // Roaring Bitmap 判断已点赞
        }

        return isLiked;
    }


    /**
     * 校验当前用户是否收藏笔记
     *
     * @param noteId
     * @param currUserId
     * @return
     */
    private boolean checkNoteIsCollected(Long noteId, Long currUserId) {
        // 是否收藏
        boolean isCollected = false;

        // Roaring Bitmap Key
        String rbitmapUserNoteCollectListKey = RedisKeyConstants.buildRBitmapUserNoteCollectListKey(currUserId);

        DefaultRedisScript<Long> script = LuaUtils.getLuaScript("/lua/rbitmap_note_collect_only_check.lua", Long.class);

        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteCollectListKey), noteId);

        NoteCollectLuaResultEnum noteCollectLuaResultEnum = NoteCollectLuaResultEnum.valueOf(result);

        switch (noteCollectLuaResultEnum) {
            // Redis 中 Roaring Bitmap 不存在
            case NOT_EXIST -> {
                // 从数据库中校验笔记是否被收藏，并异步初始化布隆过滤器，设置过期时间
                int count = noteCollectionDOMapper.selectNoteIsCollected(currUserId, noteId);

                // 保底1天+随机秒数
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

                // 目标笔记已经被收藏
                if (count > 0) {
                    // 异步初始化 Roaring Bitmap
                    threadPoolTaskExecutor.submit(() -> batchAddNoteCollect2RBitmapAndExpire(currUserId, expireSeconds, rbitmapUserNoteCollectListKey));
                    isCollected = true;
                }
            }
            // 目标笔记已经被收藏
            case NOTE_COLLECTED -> isCollected = true;
        }

        return isCollected;
    }


    /**
     * 校验笔记是否存在，若存在，则获取笔记的发布者 ID
     *
     * @param noteId
     */
    private Long checkNoteIsExistAndGetCreatorId(Long noteId) {
        // 先从本地缓存校验
        String findNoteDetailRspVOStrLocalCache = LOCAL_CACHE.getIfPresent(noteId);
        // 解析 Json 字符串为 VO 对象
        FindNoteDetailResponse findNoteDetailRspVO = JsonUtils.parseObject(findNoteDetailRspVOStrLocalCache, FindNoteDetailResponse.class);

        // 若本地缓存没有
        if (Objects.isNull(findNoteDetailRspVO)) {
            // 再从 Redis 中校验
            String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);

            String noteDetailJson = redisTemplate.opsForValue().get(noteDetailRedisKey);

            // 解析 Json 字符串为 VO 对象
            findNoteDetailRspVO = JsonUtils.parseObject(noteDetailJson, FindNoteDetailResponse.class);

            // 都不存在，再查询数据库校验是否存在
            if (Objects.isNull(findNoteDetailRspVO)) {

                // 笔记发布者用户 ID
                Long creatorId = noteDOMapper.selectCreatorIdByNoteId(noteId);

                // 若数据库中也不存在，提示用户
                if (Objects.isNull(creatorId)) {
                    throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
                }

                // 若数据库中存在，异步同步一下缓存
                threadPoolTaskExecutor.execute(() -> {
                    FindNoteDetailRequest findNoteDetailReqVO = FindNoteDetailRequest.builder().id(noteId).build();
                    findNoteDetail(findNoteDetailReqVO);
                });
                return creatorId;
            }
        }
        return findNoteDetailRspVO.getCreatorId();
    }

    /**
     * 异步初始化布隆过滤器
     *
     * @param userId
     * @param expireSeconds
     * @param bloomUserNoteLikeListKey
     */
//    private void batchAddNoteLike2BloomAndExpire(Long userId, long expireSeconds, String bloomUserNoteLikeListKey) {
//        try {
//            // 全量同步一下，并设置过期时间
//            List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectByUserId(userId);
//
//            if (CollUtil.isNotEmpty(noteLikeDOS)) {
//                DefaultRedisScript<Long> script = LuaUtils.getLuaScript("/lua/bloom_batch_add_note_like_and_expire.lua", Long.class);
//
//                // 构建 Lua 参数
//                List<Object> luaArgs = Lists.newArrayList();
//                noteLikeDOS.forEach(noteLikeDO -> luaArgs.add(noteLikeDO.getNoteId())); // 将每个点赞的笔记 ID 传入
//                luaArgs.add(expireSeconds);  // 最后一个参数是过期时间（秒）
//                redisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), luaArgs.toArray());
//            }
//        } catch (Exception e) {
//            log.error("## 异步初始化布隆过滤器异常: ", e);
//        }
//    }

    /**
     * 删除redis缓存和发送MQ删除所有分布式本地缓存
     *
     * @param noteId
     */
    private void DeleteNoteCache(Long noteId) {
        CompletableFuture<Boolean> result = CompletableFuture.supplyAsync(() -> {
            // 删除 Redis 缓存
            String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
            redisTemplate.delete(noteDetailRedisKey);

            // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
            SendResult sendResult = rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
            log.info(sendResult.toString());
            log.info("====> MQ：删除笔记本地缓存发送成功...");
            return true;
        }, threadPoolTaskExecutor);
        try {
            result.join();
        } catch (Exception e) {
            log.error("删除笔记缓存失败", e);
            throw new BizException(ResponseCodeEnum.NOTE_UPDATE_FAIL);
        }
    }

    /**
     * 校验笔记的可见性（针对 VO 实体类）
     *
     * @param userId
     * @param findNoteDetailResponse
     */
    private void checkNoteVisibleFromResponse(Long userId, FindNoteDetailResponse findNoteDetailResponse) {
        if (Objects.nonNull(findNoteDetailResponse)) {
            Integer visible = findNoteDetailResponse.getVisible();
            checkNoteVisible(visible, userId, findNoteDetailResponse.getCreatorId());
        }
    }

    /**
     * 校验笔记的可见性
     *
     * @param visible   是否可见
     * @param userId    当前用户 ID
     * @param creatorId 笔记创建者
     */
    private void checkNoteVisible(Integer visible, Long userId, Long creatorId) {
        if (Objects.equals(visible, NoteVisibleEnum.PRIVATE.getCode()) && !Objects.equals(userId, creatorId)) { // 仅自己可见, 并且访问用户为笔记创建者
            throw new BizException(ResponseCodeEnum.NOTE_PRIVATE);
        }
    }

    /**
     * 删除本地笔记缓存
     *
     * @param noteId
     */
    public void deleteNoteLocalCache(Long noteId) {
        LOCAL_CACHE.invalidate(noteId);
    }


    /**
     * 异步初始化用户点赞笔记 ZSet
     *
     * @param userId
     * @param userNoteLikeZSetKey
     */
    private void asynInitUserNoteLikesZSet(Long userId, String userNoteLikeZSetKey) {
        threadPoolTaskExecutor.execute(() -> {
            // 判断用户笔记点赞 ZSET 是否存在
            boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(userNoteLikeZSetKey));

            // 不存在，则重新初始化
            if (!hasKey) {
                // 查询当前用户最新点赞的 100 篇笔记
                List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectLikedByUserIdAndLimit(userId, 100);
                if (CollUtil.isNotEmpty(noteLikeDOS)) {
                    // 保底1天+随机秒数
                    long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                    // 构建 Lua 参数
                    Object[] luaArgs = buildNoteLikeZSetLuaArgs(noteLikeDOS, expireSeconds);
                    // lua脚本
                    DefaultRedisScript<Long> script2 = LuaUtils.getLuaScript("/lua/batch_add_note_like_zset_and_expire.lua", Long.class);

                    redisTemplate.execute(script2, Collections.singletonList(userNoteLikeZSetKey), luaArgs);
                }
            }
        });
    }


    /**
     * 构建 Lua 脚本参数
     *
     * @param noteLikeDOS
     * @param expireSeconds
     * @return
     */
    private static Object[] buildNoteLikeZSetLuaArgs(List<NoteLikeDO> noteLikeDOS, long expireSeconds) {
        int argsLength = noteLikeDOS.size() * 2 + 1; // 每个笔记点赞关系有 2 个参数（score 和 value），最后再跟一个过期时间
        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (NoteLikeDO noteLikeDO : noteLikeDOS) {
            luaArgs[i] = DateUtils.localDateTime2Timestamp(noteLikeDO.getCreateTime()); // 点赞时间作为 score
            luaArgs[i + 1] = noteLikeDO.getNoteId();          // 笔记ID 作为 ZSet value
            i += 2;
        }

        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }

    /**
     * 初始化笔记收藏布隆过滤器
     *
     * @param userId
     * @param expireSeconds
     * @param bloomUserNoteCollectListKey
     */
    private void batchAddNoteCollect2BloomAndExpire(Long userId, long expireSeconds, String bloomUserNoteCollectListKey) {
        try {
            // 异步全量同步一下，并设置过期时间
            List<NoteCollectionDO> noteCollectionDOS = noteCollectionDOMapper.selectByUserId(userId);

            if (CollUtil.isNotEmpty(noteCollectionDOS)) {
                DefaultRedisScript<Long> script = LuaUtils.getLuaScript("/lua/bloom_batch_add_note_collect_and_expire.lua", Long.class);

                // 构建 Lua 参数
                List<Object> luaArgs = Lists.newArrayList();
                noteCollectionDOS.forEach(noteCollectionDO -> luaArgs.add(noteCollectionDO.getNoteId())); // 将每个收藏的笔记 ID 传入
                luaArgs.add(expireSeconds);  // 最后一个参数是过期时间（秒）
                redisTemplate.execute(script, Collections.singletonList(bloomUserNoteCollectListKey), luaArgs.toArray());
            }
        } catch (Exception e) {
            log.error("## 异步初始化【笔记收藏】布隆过滤器异常: ", e);
        }
    }

    /**
     * 异步初始化用户收藏笔记 ZSet
     *
     * @param userId
     * @param userNoteCollectZSetKey
     */
    private void initUserNoteCollectsZSet(Long userId, String userNoteCollectZSetKey) {
        // 判断用户笔记收藏 ZSET 是否存在
        boolean hasKey = redisTemplate.hasKey(userNoteCollectZSetKey);

        // 不存在，则重新初始化
        if (!hasKey) {
            // 查询当前用户最新收藏的 300 篇笔记
            List<NoteCollectionDO> noteCollectionDOS = noteCollectionDOMapper.selectCollectedByUserIdAndLimit(userId, 300);
            if (CollUtil.isNotEmpty(noteCollectionDOS)) {
                // 保底1天+随机秒数
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                // 构建 Lua 参数
                Object[] luaArgs = buildNoteCollectZSetLuaArgs(noteCollectionDOS, expireSeconds);

                DefaultRedisScript<Long> script = LuaUtils.getLuaScript("/lua/batch_add_note_collect_zset_and_expire.lua", Long.class);
                redisTemplate.execute(script, Collections.singletonList(userNoteCollectZSetKey), luaArgs);
            }
        }
    }


    /**
     * 构建笔记收藏 ZSET Lua 脚本参数
     *
     * @param noteCollectionDOS
     * @param expireSeconds
     * @return
     */
    private static Object[] buildNoteCollectZSetLuaArgs(List<NoteCollectionDO> noteCollectionDOS, long expireSeconds) {
        int argsLength = noteCollectionDOS.size() * 2 + 1; // 每个笔记收藏关系有 2 个参数（score 和 value），最后再跟一个过期时间
        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (NoteCollectionDO noteCollectionDO : noteCollectionDOS) {
            luaArgs[i] = DateUtils.localDateTime2Timestamp(noteCollectionDO.getCreateTime()); // 收藏时间作为 score
            luaArgs[i + 1] = noteCollectionDO.getNoteId();          // 笔记ID 作为 ZSet value
            i += 2;
        }

        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }


}
