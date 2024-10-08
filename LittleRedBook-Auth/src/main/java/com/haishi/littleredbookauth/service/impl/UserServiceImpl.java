package com.haishi.littleredbookauth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.haishi.framework.common.constant.RedisKeyConstants;
import com.haishi.framework.common.enums.DeletedEnum;
import com.haishi.framework.common.enums.StatusEnum;
import com.haishi.framework.common.exception.BizException;
import com.haishi.framework.common.response.Response;
import com.haishi.framework.common.util.JsonUtil;
import com.haishi.littleredbookauth.constant.RoleConstants;
import com.haishi.littleredbookauth.domain.DO.UserDO;
import com.haishi.littleredbookauth.domain.DO.UserRoleDO;
import com.haishi.littleredbookauth.domain.mapper.UserDOMapper;
import com.haishi.littleredbookauth.domain.mapper.UserRoleDOMapper;
import com.haishi.littleredbookauth.enums.LoginTypeEnum;
import com.haishi.littleredbookauth.enums.ResponseCodeEnum;
import com.haishi.littleredbookauth.model.vo.user.UserLoginReqVO;
import com.haishi.littleredbookauth.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserDOMapper userDOMapper;
    @Resource
    private UserRoleDOMapper userRoleDOMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 登录与注册
     *
     * @param userLoginReqVO
     * @return
     */
    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {
        String phone = userLoginReqVO.getPhone();
        Integer type = userLoginReqVO.getType();

        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);

        Long userId = null;

        //判断登录类型
        switch (loginTypeEnum) {
            case VERIFICATION_CODE :
                //验证码登录
                String verificationCode = userLoginReqVO.getCode();

                Preconditions.checkArgument(StringUtils.isNotBlank(verificationCode), "验证码不能为空");

                // 构建验证码 Redis Key
                String key = RedisKeyConstants.buildVerificationCodeKey(phone);
                String code=(String) redisTemplate.opsForValue().get(key);

                if(!StringUtils.equals(verificationCode, code)) {
                    throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
                }

                // 通过手机号查询记录
                UserDO userDO = userDOMapper.selectByPhone(phone);

                log.info("==> 用户是否注册, phone: {}, userDO: {}", phone, JsonUtil.toJsonString(userDO));

                // 判断是否注册
                if (Objects.isNull(userDO)) {
                    // 若此用户还没有注册，系统自动注册该用户
                    //因为出现自调用，无法使用@Transactional回滚
                    userId = registerUser(phone);
                } else {
                    // 已注册，则获取其用户 ID
                    userId = userDO.getId();
                }
                break;

            case PASSWORD:
                String password = userLoginReqVO.getPassword();
                //TODO
                break;
            default:
                break;
        }
        if(userId==null|| userId.toString().isEmpty()) {
            return Response.fail(ResponseCodeEnum.LOGIN_FAILURE);
        }
        // SaToken 登录用户，并返回 token 令牌
        // SaToken 登录用户, 入参为用户 ID
        StpUtil.login(userId);

        // 获取 Token 令牌
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 返回 Token 令牌
        return Response.success(tokenInfo.tokenValue);
    }

    /**
     * 系统自动注册用户
     * @param phone
     * @return
     */
    private Long registerUser(String phone) {
        return transactionTemplate.execute(status -> {
            try {
                // 获取全局自增的账号 ID
                Long bookId = redisTemplate.opsForValue().increment(RedisKeyConstants.BOOK_ID_GENERATOR_KEY);

                UserDO userDO = UserDO.builder()
                        .phone(phone)
                        .accountId(String.valueOf(bookId)) // 自动生成小红书号 ID
                        .nickname("小红书" + bookId) // 自动生成昵称, 如：小红书10000
                        .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                        .build();

                // 添加入库
                userDOMapper.insert(userDO);
                // 获取刚刚添加入库的用户 ID
                Long userId = userDO.getId();

                // 给该用户分配一个默认角色
                UserRoleDO userRoleDO = UserRoleDO.builder()
                        .userId(userId)
                        .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(DeletedEnum.NO.getValue())
                        .build();
                userRoleDOMapper.insert(userRoleDO);

                // 将该用户的角色 ID 存入 Redis 中
                List<Long> roles = Lists.newArrayList();
                roles.add(RoleConstants.COMMON_USER_ROLE_ID);
                String userRolesKey = RedisKeyConstants.buildUserRoleKey(phone);
                redisTemplate.opsForValue().set(userRolesKey, JsonUtil.toJsonString(roles));

                return userId;
            }catch (Exception e) {
                //回滚
                status.setRollbackOnly();
                log.error(e.getMessage(), e);
                return null;
            }
        });
    }
}
