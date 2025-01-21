package com.haishi.LittleRedBook.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.haishi.LittleRedBook.note.biz.constant.MQConstants;
import com.haishi.LittleRedBook.note.biz.constant.RedisKeyConstants;
import com.haishi.LittleRedBook.note.biz.domain.dataobject.NoteDO;
import com.haishi.LittleRedBook.note.biz.domain.mapper.NoteDOMapper;
import com.haishi.LittleRedBook.note.biz.domain.mapper.TopicDOMapper;
import com.haishi.LittleRedBook.note.biz.enums.NoteStatusEnum;
import com.haishi.LittleRedBook.note.biz.enums.NoteTypeEnum;
import com.haishi.LittleRedBook.note.biz.enums.NoteVisibleEnum;
import com.haishi.LittleRedBook.note.biz.enums.ResponseCodeEnum;
import com.haishi.LittleRedBook.note.biz.model.vo.request.*;
import com.haishi.LittleRedBook.note.biz.model.vo.response.FindNoteDetailResponse;
import com.haishi.LittleRedBook.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.haishi.LittleRedBook.note.biz.rpc.KeyValueRpcService;
import com.haishi.LittleRedBook.note.biz.rpc.UserRpcService;
import com.haishi.LittleRedBook.note.biz.service.NoteService;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByIdResponse;
import com.haishi.framework.biz.context.holder.LoginUserContextHolder;
import com.haishi.framework.commons.exception.BizException;
import com.haishi.framework.commons.response.Response;
import com.haishi.framework.commons.util.JsonUtils;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NoteServiceImpl implements NoteService {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

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
    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量为 10000 个条目
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
        NoteDO noteDO = NoteDO.builder()
                .id(Long.valueOf(snowflakeId))
                .isContentEmpty(isContentEmpty)
                .creatorId(creatorId)
                .imgUris(imgUris)
                .title(publishNoteRequest.getTitle())
                .topicId(publishNoteRequest.getTopicId())
                .topicName(topicName)
                .type(type)
                .visible(NoteVisibleEnum.PUBLIC.getCode())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .status(NoteStatusEnum.NORMAL.getCode()).isTop(Boolean.FALSE)
                .videoUri(videoUri)
                .contentUuid(contentUuid)
                .build();


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

        return Response.success();

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
                LOCAL_CACHE.put(noteId,
                        Objects.isNull(findNoteDetailResponse) ? "null" : JsonUtils.toJsonString(findNoteDetailResponse));
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
        CompletableFuture<FindUserByIdResponse> userResultFuture = CompletableFuture
                .supplyAsync(() -> userRpcService.findById(creatorId), threadPoolTaskExecutor);

        //RPC:查询笔记内容
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);
        if (Objects.equals(noteDO.getIsContentEmpty(), Boolean.FALSE)) {
            contentResultFuture = CompletableFuture
                    .supplyAsync(() -> keyValueRpcService.findNoteContent(noteDO.getContentUuid()), threadPoolTaskExecutor);
        }

        CompletableFuture<String> finalContentResultFuture = contentResultFuture;

        CompletableFuture<FindNoteDetailResponse> resultFuture = CompletableFuture
                .allOf(userResultFuture, contentResultFuture)
                .thenApply(s -> {
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
                    if (Objects.equals(noteType, NoteTypeEnum.IMAGE_TEXT.getCode())
                            && StringUtils.isNotBlank(imgUrisStr)) {
                        imgUris = List.of(imgUrisStr.split(","));
                    }

                    // 构建返参 VO 实体类
                    return FindNoteDetailResponse.builder()
                            .id(noteDO.getId())
                            .type(noteDO.getType())
                            .title(noteDO.getTitle())
                            .content(content)
                            .imgUris(imgUris)
                            .topicId(noteDO.getTopicId())
                            .topicName(noteDO.getTopicName())
                            .creatorId(userId)
                            .creatorName(response.getNickName())
                            .avatar(response.getAvatar())
                            .videoUri(noteDO.getVideoUri())
                            .updateTime(noteDO.getUpdateTime())
                            .visible(noteDO.getVisible())
                            .build();

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

        CompletableFuture<Boolean> updateKV= CompletableFuture.supplyAsync(()->{
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

        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .isContentEmpty(StringUtils.isBlank(content))
                .imgUris(imgUris)
                .title(request.getTitle())
                .topicId(request.getTopicId())
                .topicName(topicName)
                .type(type)
                .updateTime(LocalDateTime.now())
                .videoUri(videoUri)
                .build();

        noteDOMapper.updateByPrimaryKey(noteDO);

        Boolean isUpdateContentSuccess = updateKV.join();
        // 如果更新失败，抛出业务异常，回滚事务
        if (!isUpdateContentSuccess) {
            throw new BizException(ResponseCodeEnum.NOTE_UPDATE_FAIL);
        }


        DeleteNoteCache(noteId);


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

        // 逻辑删除
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .status(NoteStatusEnum.DELETED.getCode())
                .updateTime(LocalDateTime.now())
                .build();

        int count = noteDOMapper.updateByPrimaryKeySelective(noteDO);

        // 若影响的行数为 0，则表示该笔记不存在
        if (count == 0) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        DeleteNoteCache(noteId);

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

        // 构建更新 DO 实体类
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .visible(NoteVisibleEnum.PRIVATE.getCode()) // 可见性设置为仅对自己可见
                .updateTime(LocalDateTime.now())
                .build();

        // 执行更新 SQL
        int count = noteDOMapper.updateVisibleOnlyMe(noteDO);

        // 若影响的行数为 0，则表示该笔记无法修改为仅自己可见
        if (count == 0) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_VISIBLE_ONLY_ME);
        }

        DeleteNoteCache(noteId);

        return Response.success();
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
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .isTop(isTop)
                .updateTime(LocalDateTime.now())
                .creatorId(currUserId) // 只有笔记所有者，才能置顶/取消置顶笔记
                .build();

        int count = noteDOMapper.updateIsTop(noteDO);

        if (count == 0) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        DeleteNoteCache(noteId);

        return Response.success();
    }

    /**
     * 删除redis缓存和发送MQ删除所有分布式本地缓存
     * @param noteId
     */
    private void DeleteNoteCache(Long noteId) {
        threadPoolTaskExecutor.execute(()->{
            // 删除 Redis 缓存
            String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
            redisTemplate.delete(noteDetailRedisKey);

            // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
            SendResult sendResult = rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
            log.info(sendResult.toString());
            log.info("====> MQ：删除笔记本地缓存发送成功...");
        });
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
     * @param noteId
     */
    public void deleteNoteLocalCache(Long noteId) {
        LOCAL_CACHE.invalidate(noteId);
    }
}
