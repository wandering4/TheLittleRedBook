package com.haishi.LittleRedBook.user.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.haishi.LittleRedBook.count.dto.response.FindUserCountsByIdRspDTO;
import com.haishi.LittleRedBook.oss.api.FileFeignApi;
import com.haishi.LittleRedBook.user.biz.constant.MQConstants;
import com.haishi.LittleRedBook.user.biz.constant.RedisKeyConstants;
import com.haishi.LittleRedBook.user.biz.constant.RoleConstants;
import com.haishi.LittleRedBook.user.biz.domain.dataobject.RoleDO;
import com.haishi.LittleRedBook.user.biz.domain.dataobject.UserRoleDO;
import com.haishi.LittleRedBook.user.biz.domain.mapper.RoleDOMapper;
import com.haishi.LittleRedBook.user.biz.domain.mapper.UserRoleDOMapper;
import com.haishi.LittleRedBook.user.biz.model.vo.request.FindUserProfileReqVO;
import com.haishi.LittleRedBook.user.biz.model.vo.response.FindUserProfileRspVO;
import com.haishi.LittleRedBook.user.biz.rpc.CountRpcService;
import com.haishi.LittleRedBook.user.biz.rpc.DistributedIdGeneratorRpcService;
import com.haishi.LittleRedBook.user.dto.req.*;
import com.haishi.LittleRedBook.user.biz.domain.dataobject.UserDO;
import com.haishi.LittleRedBook.user.biz.domain.mapper.UserDOMapper;
import com.haishi.LittleRedBook.user.biz.enums.ResponseCodeEnum;
import com.haishi.LittleRedBook.user.biz.enums.SexEnum;
import com.haishi.LittleRedBook.user.biz.model.vo.request.UpdateUserInfoRequest;
import com.haishi.LittleRedBook.user.biz.rpc.OssRpcService;
import com.haishi.LittleRedBook.user.biz.service.UserService;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByIdResponse;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByPhoneRspDTO;
import com.haishi.framework.biz.context.holder.LoginUserContextHolder;
import com.haishi.framework.commons.enums.DeletedEnum;
import com.haishi.framework.commons.enums.StatusEnum;
import com.haishi.framework.commons.exception.BizException;
import com.haishi.framework.commons.response.Response;
import com.haishi.framework.commons.util.DateUtils;
import com.haishi.framework.commons.util.JsonUtils;
import com.haishi.framework.commons.util.NumberUtils;
import com.haishi.framework.commons.util.ParamUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description 用户业务
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserDOMapper userDOMapper;

    @Resource
    private FileFeignApi fileFeignApi;

    @Resource
    private OssRpcService ossRpcService;

    @Resource
    private UserRoleDOMapper userRoleDOMapper;

    @Resource
    private RoleDOMapper roleDOMapper;

    @Resource
    private CountRpcService countRpcService;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 用户主页信息本地缓存
     */
    private static final Cache<Long, FindUserProfileRspVO> PROFILE_LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量为 10000 个条目
            .maximumSize(10000) // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(5, TimeUnit.MINUTES) // 设置缓存条目在写入后 5 分钟过期
            .build();


    /**
     * 用户信息本地缓存
     */
    private static final Cache<Long, FindUserByIdResponse> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量为 10000 个条目
            .maximumSize(10000) // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(1, TimeUnit.HOURS) // 设置缓存条目在写入后 1 小时过期
            .build();


    /**
     * 更新用户信息
     *
     * @param updateUserInfoRequest
     * @return
     */
    @Override
    public Response<?> updateUserInfo(UpdateUserInfoRequest updateUserInfoRequest) {

        // 被更新的用户 ID
        Long userId = updateUserInfoRequest.getUserId();
        // 当前登录的用户 ID
        Long loginUserId = LoginUserContextHolder.getUserId();

        // 非号主本人，无法修改其个人信息
        if (!Objects.equals(loginUserId, userId)) {
            throw new BizException(ResponseCodeEnum.CANT_UPDATE_OTHER_USER_PROFILE);
        }

        UserDO userDO = new UserDO();
        //获取当前用户id
        userDO.setId(userId);

        boolean needUpdate = false;

        // 头像
        MultipartFile avatarFile = updateUserInfoRequest.getAvatar();
        if (ObjectUtils.isNotEmpty(avatarFile)) {
            //调用对象存储服务上传文件
            String avatar = ossRpcService.uploadFile(avatarFile);
            log.info("==> 调用 oss 服务成功，上传头像，url：{}", avatar);

            if (StringUtils.isBlank(avatar)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }

            userDO.setAvatar(avatar);
            needUpdate = true;
        }

        // 昵称
        String nickname = updateUserInfoRequest.getNickname();
        if (StringUtils.isNotBlank(nickname)) {
            Preconditions.checkArgument(ParamUtils.checkNickname(nickname), ResponseCodeEnum.NICK_NAME_VALID_FAIL.getErrorMessage());
            userDO.setNickname(nickname);
            needUpdate = true;
        }

        // 小红书账号
        String accountId = updateUserInfoRequest.getAccountId();
        if (StringUtils.isNotBlank(accountId)) {
            Preconditions.checkArgument(ParamUtils.checkAccountId(accountId), ResponseCodeEnum.ACCOUNT_ID_VALID_FAIL.getErrorMessage());
            userDO.setAccountId(accountId);
            needUpdate = true;
        }

        // 性别
        Integer sex = updateUserInfoRequest.getSex();
        if (Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL.getErrorMessage());
            userDO.setSex(sex);
            needUpdate = true;
        }

        // 生日
        LocalDate birthday = updateUserInfoRequest.getBirthday();
        if (Objects.nonNull(birthday)) {
            userDO.setBirthday(birthday);
            needUpdate = true;
        }

        // 个人简介
        String introduction = updateUserInfoRequest.getIntroduction();
        if (StringUtils.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamUtils.checkLength(introduction, 100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL.getErrorMessage());
            userDO.setIntroduction(introduction);
            needUpdate = true;
        }

        // 背景图
        MultipartFile backgroundImgFile = updateUserInfoRequest.getBackgroundImg();
        if (Objects.nonNull(backgroundImgFile)) {
            //调用对象存储服务上传文件
            String backgroundImg = ossRpcService.uploadFile(backgroundImgFile);
            log.info("==> 调用 oss 服务成功，上传背景图，url：{}", backgroundImg);

            // 若上传背景图失败，则抛出业务异常
            if (StringUtils.isBlank(backgroundImg)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_BACKGROUND_IMG_FAIL);
            }

            userDO.setBackgroundImg(backgroundImg);
            needUpdate = true;
        }

        if (needUpdate) {

            // 删除用户缓存
            deleteUserRedisCache(userId);

            // 更新用户信息
            userDO.setUpdateTime(LocalDateTime.now());
            userDOMapper.updateByPrimaryKeySelective(userDO);

            // 延时双删
            sendDelayDeleteUserRedisCacheMQ(userId);

        }
        return Response.success();


    }

    /**
     * 删除 Redis 中的用户缓存
     * @param userId
     */
    private void deleteUserRedisCache(Long userId) {
        // 构建 Redis Key
        String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);
        String userProfileRedisKey = RedisKeyConstants.buildUserProfileKey(userId);

        // 批量删除
        redisTemplate.delete(Arrays.asList(userInfoRedisKey, userProfileRedisKey));
    }

    /**
     * 异步发送延时消息
     * @param userId
     */
    private void sendDelayDeleteUserRedisCacheMQ(Long userId) {
        Message<String> message = MessageBuilder.withPayload(String.valueOf(userId))
                .build();

        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELAY_DELETE_USER_REDIS_CACHE, message,
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("## 延时删除 Redis 用户缓存消息发送成功...");
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("## 延时删除 Redis 用户缓存消息发送失败...", e);
                    }
                },
                3000, // 超时时间
                1 // 延迟级别，1 表示延时 1s
        );
    }

    /**
     * 用户注册
     *
     * @param registerUserRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> register(RegisterUserRequest registerUserRequest) {
        String phone = registerUserRequest.getPhone();

        // 先判断该手机号是否已被注册
        UserDO userDO1 = userDOMapper.selectByPhone(phone);

        log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtils.toJsonString(userDO1));

        // 若已注册，则直接返回用户 ID
        if (Objects.nonNull(userDO1)) {
            return Response.success(userDO1.getId());
        }

        // 否则注册新用户
        // 获取全局自增的账号 ID
        String bookId = distributedIdGeneratorRpcService.getLittleRedBookId();

        // RPC: 调用分布式 ID 生成服务生成用户 ID
        String userIdStr = distributedIdGeneratorRpcService.getUserId();
        Long userId = Long.valueOf(userIdStr);

        UserDO userDO = UserDO.builder()
                .id(userId)
                .phone(phone)
                .accountId(bookId) // 自动生成小红书号 ID
                .nickname("小红书" + bookId) // 自动生成昵称, 如：小红书10000
                .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                .build();

        // 添加入库
        userDOMapper.insert(userDO);

        // 获取刚刚添加入库的用户 ID
        userId = userDO.getId();

        // 给该用户分配一个默认角色
        UserRoleDO userRoleDO = UserRoleDO.builder()
                .userId(userId)
                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue())
                .build();
        userRoleDOMapper.insert(userRoleDO);

        RoleDO roleDO = roleDOMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);

        // 将该用户的角色 ID 存入 Redis 中
        List<String> roles = new ArrayList<>(1);
        roles.add(roleDO.getRoleKey());

        String userRolesKey = RedisKeyConstants.buildUserRoleKey(userId);
        redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

        return Response.success(userId);
    }

    /**
     * 根据手机号查询用户信息
     *
     * @param findUserByPhoneRequest
     * @return
     */
    @Override
    public Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneRequest findUserByPhoneRequest) {
        String phone = findUserByPhoneRequest.getPhone();

        UserDO userDO = userDOMapper.selectByPhone(phone);

        // 判空
        if (Objects.isNull(userDO)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参
        FindUserByPhoneRspDTO findUserByPhoneRspDTO = FindUserByPhoneRspDTO.builder()
                .id(userDO.getId())
                .password(userDO.getPassword())
                .build();

        return Response.success(findUserByPhoneRspDTO);
    }

    @Override
    public Response<?> updatePassword(UpdateUserPasswordRequest updateUserPasswordRequest) {

        // 获取当前请求对应的用户 ID
        Long userId = LoginUserContextHolder.getUserId();

        UserDO userDO = UserDO.builder()
                .id(userId)
                .password(updateUserPasswordRequest.getEncodePassword())
                .updateTime(LocalDateTime.now())
                .build();
        userDOMapper.updateByPrimaryKeySelective(userDO);

        return Response.success();
    }

    /**
     * 根据用户 ID 查询用户信息
     *
     * @param findUserByIdRequest
     * @return
     */
    @Override
    public Response<FindUserByIdResponse> findById(FindUserByIdRequest findUserByIdRequest) {
        Long userId = findUserByIdRequest.getId();

        // 先从本地缓存中查询
        FindUserByIdResponse findUserByIdRspDTOLocalCache = LOCAL_CACHE.getIfPresent(userId);
        if (Objects.nonNull(findUserByIdRspDTOLocalCache)) {
            log.info("==> 命中了本地缓存；{}", findUserByIdRspDTOLocalCache);
            return Response.success(findUserByIdRspDTOLocalCache);
        }

        // redis缓存
        String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);
        String userInfoRedisValue = (String) redisTemplate.opsForValue().get(userInfoRedisKey);

        // 若 Redis 缓存中存在该用户信息
        if (StringUtils.isNotBlank(userInfoRedisValue)) {
            // 将存储的 Json 字符串转换成对象，并返回
            FindUserByIdResponse findUserByIdRspDTO = JsonUtils.parseObject(userInfoRedisValue, FindUserByIdResponse.class);

            // 异步线程中将用户信息存入本地缓存
            threadPoolTaskExecutor.submit(() -> {
                if (Objects.nonNull(findUserByIdRspDTO)) {
                    // 写入本地缓存
                    LOCAL_CACHE.put(userId, findUserByIdRspDTO);
                }
            });

            return Response.success(findUserByIdRspDTO);
        }

        // 否则, 从数据库中查询
        UserDO userDO = userDOMapper.selectByPrimaryKey(userId);

        // 判空
        if (Objects.isNull(userDO)) {
            threadPoolTaskExecutor.execute(() -> {
                // 防止缓存穿透，将空数据存入 Redis 缓存 (过期时间不宜设置过长)
                // 保底1分钟 + 随机秒数 避免缓存穿透
                long expireSeconds = 60 + RandomUtil.randomInt(60);
                redisTemplate.opsForValue().set(userInfoRedisKey, "null", expireSeconds, TimeUnit.SECONDS);
            });
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参
        FindUserByIdResponse findUserByIdResponse = FindUserByIdResponse.builder()
                .id(userDO.getId())
                .nickName(userDO.getNickname())
                .avatar(userDO.getAvatar())
                .introduction(userDO.getIntroduction())
                .build();

        // 异步将用户信息存入 Redis 缓存，提升响应速度
        threadPoolTaskExecutor.submit(() -> {
            // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
            redisTemplate.opsForValue()
                    .set(userInfoRedisKey, JsonUtils.toJsonString(findUserByIdResponse), expireSeconds, TimeUnit.SECONDS);
        });

        return Response.success(findUserByIdResponse);
    }


    /**
     * 批量根据用户 ID 查询用户信息
     *
     * @param findUsersByIdsReqDTO
     * @return
     */
    @Override
    public Response<List<FindUserByIdResponse>> findByIds(FindUsersByIdsReqDTO findUsersByIdsReqDTO) {
        // 需要查询的用户 ID 集合
        List<Long> userIds = findUsersByIdsReqDTO.getIds();

        // 构建 Redis Key 集合
        List<String> redisKeys = userIds.stream()
                .map(RedisKeyConstants::buildUserInfoKey)
                .toList();

        // 先从 Redis 缓存中查, multiGet 批量查询提升性能
        List<Object> redisValues = redisTemplate.opsForValue().multiGet(redisKeys);
        // 如果缓存中不为空
        if (CollUtil.isNotEmpty(redisValues)) {
            // 过滤掉为空的数据
            redisValues = redisValues.stream().filter(Objects::nonNull).toList();
        }

        // 返参
        List<FindUserByIdResponse> findUserByIdRspDTOS = Lists.newArrayList();

        // 将过滤后的缓存集合，转换为 DTO 返参实体类
        if (CollUtil.isNotEmpty(redisValues)) {
            findUserByIdRspDTOS = redisValues.stream()
                    .map(value -> JsonUtils.parseObject(String.valueOf(value), FindUserByIdResponse.class))
                    .collect(Collectors.toList());
        }

        // 如果被查询的用户信息，都在 Redis 缓存中, 则直接返回
        if (CollUtil.size(userIds) == CollUtil.size(findUserByIdRspDTOS)) {
            return Response.success(findUserByIdRspDTOS);
        }

        // 还有另外两种情况：一种是缓存里没有用户信息数据，还有一种是缓存里数据不全，需要从数据库中补充
        // 筛选出缓存里没有的用户数据，去查数据库
        List<Long> userIdsNeedQuery = null;

        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
            // 将 findUserInfoByIdRspDTOS 集合转 Map
            Map<Long, FindUserByIdResponse> map = findUserByIdRspDTOS.stream()
                    .collect(Collectors.toMap(FindUserByIdResponse::getId, p -> p));

            // 筛选出需要查 DB 的用户 ID
            userIdsNeedQuery = userIds.stream()
                    .filter(id -> Objects.isNull(map.get(id)))
                    .toList();
        } else { // 缓存中一条用户信息都没查到，则提交的用户 ID 集合都需要查数据库
            userIdsNeedQuery = userIds;
        }

        // 从数据库中批量查询
        List<UserDO> userDOS = userDOMapper.selectByIds(userIdsNeedQuery);

        List<FindUserByIdResponse> findUserByIdRspDTOS2 = null;

        // 若数据库查询的记录不为空
        if (CollUtil.isNotEmpty(userDOS)) {
            // DO 转 DTO
            findUserByIdRspDTOS2 = userDOS.stream()
                    .map(userDO -> FindUserByIdResponse.builder()
                            .id(userDO.getId())
                            .nickName(userDO.getNickname())
                            .avatar(userDO.getAvatar())
                            .introduction(userDO.getIntroduction())
                            .build())
                    .collect(Collectors.toList());


            // 异步线程将用户信息同步到 Redis 中
            List<FindUserByIdResponse> finalFindUserByIdRspDTOS = findUserByIdRspDTOS2;
            threadPoolTaskExecutor.submit(() -> {
                // DTO 集合转 Map
                Map<Long, FindUserByIdResponse> map = finalFindUserByIdRspDTOS.stream()
                        .collect(Collectors.toMap(FindUserByIdResponse::getId, p -> p));

                // 执行 pipeline 操作
                redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
                    for (UserDO userDO : userDOS) {
                        Long userId = userDO.getId();

                        // 用户信息缓存 Redis Key
                        String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);

                        // DTO 转 JSON 字符串
                        FindUserByIdResponse findUserInfoByIdRspDTO = map.get(userId);
                        String value = JsonUtils.toJsonString(findUserInfoByIdRspDTO);

                        // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
                        long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                        redisTemplate.opsForValue().set(userInfoRedisKey, value, expireSeconds, TimeUnit.SECONDS);
                    }
                    return null;
                });
            });
        }

        // 合并数据
        if (CollUtil.isNotEmpty(findUserByIdRspDTOS2)) {
            findUserByIdRspDTOS.addAll(findUserByIdRspDTOS2);
        }

        return Response.success(findUserByIdRspDTOS);
    }


    /**
     * 获取用户主页信息
     *
     * @param findUserProfileReqVO
     * @return
     */
    @Override
    public Response<FindUserProfileRspVO> findUserProfile(FindUserProfileReqVO findUserProfileReqVO) {
        // 要查询的用户 ID
        Long userId = findUserProfileReqVO.getUserId();

        // 若入参中用户 ID 为空，则查询当前登录用户
        if (Objects.isNull(userId)) {
            userId = LoginUserContextHolder.getUserId();
        }

        // 1. 优先查本地缓存
        if (!Objects.equals(userId, LoginUserContextHolder.getUserId())) { // 如果是用户本人查看自己的主页，则不走本地缓存（对本人保证实时性）
            FindUserProfileRspVO userProfileLocalCache = PROFILE_LOCAL_CACHE.getIfPresent(userId);
            if (Objects.nonNull(userProfileLocalCache)) {
                log.info("## 用户主页信息命中本地缓存: {}", JsonUtils.toJsonString(userProfileLocalCache));
                return Response.success(userProfileLocalCache);
            }
        }

        // 2. 查询 Redis 缓存
        String userProfileRedisKey = RedisKeyConstants.buildUserProfileKey(userId);

        String userProfileJson = (String) redisTemplate.opsForValue().get(userProfileRedisKey);

        if (StringUtils.isNotBlank(userProfileJson)) {
            FindUserProfileRspVO findUserProfileRspVO = JsonUtils.parseObject(userProfileJson, FindUserProfileRspVO.class);
            // 异步同步到本地缓存
            syncUserProfile2LocalCache(userId, findUserProfileRspVO);
            // 如果是博主本人查看，保证计数的实时性
            authorGetActualCountData(userId, findUserProfileRspVO);
            return Response.success(findUserProfileRspVO);
        }

        //3. 若 Redis 中无缓存，再查询数据库
        UserDO userDO = userDOMapper.selectByPrimaryKey(userId);

        if (Objects.isNull(userDO)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参 VO
        FindUserProfileRspVO findUserProfileRspVO = FindUserProfileRspVO.builder()
                .userId(userDO.getId())
                .avatar(userDO.getAvatar())
                .nickname(userDO.getNickname())
                .accountId(userDO.getAccountId())
                .sex(userDO.getSex())
                .introduction(userDO.getIntroduction())
                .build();

        // 计算年龄
        LocalDate birthday = userDO.getBirthday();
        findUserProfileRspVO.setAge(Objects.isNull(birthday) ? 0 : DateUtils.calculateAge(birthday));


        // RPC: Feign 调用计数服务
        // 关注数、粉丝数、收藏与点赞总数；发布的笔记数，获得的点赞数、收藏数
        rpcCountServiceAndSetData(userId, findUserProfileRspVO);

        // 异步同步到 Redis 中
        syncUserProfile2Redis(userProfileRedisKey, findUserProfileRspVO);

        // 异步同步到本地缓存
        syncUserProfile2LocalCache(userId, findUserProfileRspVO);

        return Response.success(findUserProfileRspVO);
    }

    /**
     * 作者本人获取真实的计数数据（保证实时性）
     * @param userId
     * @param findUserProfileRspVO
     */
    private void authorGetActualCountData(Long userId, FindUserProfileRspVO findUserProfileRspVO) {
        if (Objects.equals(userId, LoginUserContextHolder.getUserId())) { // 如果是博主本人
            rpcCountServiceAndSetData(userId, findUserProfileRspVO);
        }
    }

    /**
     * Feign 调用计数服务, 并设置计数数据
     * @param userId
     * @param findUserProfileRspVO
     */
    private void rpcCountServiceAndSetData(Long userId, FindUserProfileRspVO findUserProfileRspVO) {
        FindUserCountsByIdRspDTO findUserCountsByIdRspDTO = countRpcService.findUserCountById(userId);

        if (Objects.nonNull(findUserCountsByIdRspDTO)) {
            Long fansTotal = findUserCountsByIdRspDTO.getFansTotal();
            Long followingTotal = findUserCountsByIdRspDTO.getFollowingTotal();
            Long likeTotal = findUserCountsByIdRspDTO.getLikeTotal();
            Long collectTotal = findUserCountsByIdRspDTO.getCollectTotal();
            Long noteTotal = findUserCountsByIdRspDTO.getNoteTotal();

            findUserProfileRspVO.setFansTotal(NumberUtils.formatNumberString(fansTotal));
            findUserProfileRspVO.setFollowingTotal(NumberUtils.formatNumberString(followingTotal));
            findUserProfileRspVO.setLikeAndCollectTotal(NumberUtils.formatNumberString(likeTotal + collectTotal));
            findUserProfileRspVO.setNoteTotal(NumberUtils.formatNumberString(noteTotal));
            findUserProfileRspVO.setLikeTotal(NumberUtils.formatNumberString(likeTotal));
            findUserProfileRspVO.setCollectTotal(NumberUtils.formatNumberString(collectTotal));
        }
    }


    /**
     * 异步同步到 Redis 中
     *
     * @param userProfileRedisKey
     * @param findUserProfileRspVO
     */
    private void syncUserProfile2Redis(String userProfileRedisKey, FindUserProfileRspVO findUserProfileRspVO) {
        threadPoolTaskExecutor.submit(() -> {
            // 设置随机过期时间 (2小时以内)
            long expireTime = 60*60 + RandomUtil.randomInt(60 * 60);

            // 将 VO 转为 Json 字符串写入到 Redis 中
            redisTemplate.opsForValue().set(userProfileRedisKey, JsonUtils.toJsonString(findUserProfileRspVO), expireTime, TimeUnit.SECONDS);
        });
    }

    /**
     * 异步同步到本地缓存
     *
     * @param userId
     * @param findUserProfileRspVO
     */
    private void syncUserProfile2LocalCache(Long userId, FindUserProfileRspVO findUserProfileRspVO) {
        threadPoolTaskExecutor.submit(() -> {
            PROFILE_LOCAL_CACHE.put(userId, findUserProfileRspVO);
        });
    }

}

