package com.haishi.littleredbookauth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;

import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;

import com.haishi.LittleRedBook.user.dto.resp.FindUserByPhoneRspDTO;
import com.haishi.framework.biz.context.holder.LoginUserContextHolder;
import com.haishi.framework.commons.constant.RedisKeyConstants;
import com.haishi.framework.commons.exception.BizException;
import com.haishi.framework.commons.response.Response;
import com.haishi.littleredbookauth.enums.LoginTypeEnum;
import com.haishi.littleredbookauth.enums.ResponseCodeEnum;
import com.haishi.littleredbookauth.model.vo.user.UpdatePasswordReqVO;
import com.haishi.littleredbookauth.model.vo.user.UserLoginRequest;
import com.haishi.littleredbookauth.rpc.UserRpcService;
import com.haishi.littleredbookauth.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserRpcService userRpcService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 登录与注册
     *
     * @param userLoginRequest
     * @return
     */
    @Override
    public Response<String> loginAndRegister(UserLoginRequest userLoginRequest) {
        String phone = userLoginRequest.getPhone();
        Integer type = userLoginRequest.getType();

        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);

        // 登录类型错误
        if (Objects.isNull(loginTypeEnum)) {
            throw new BizException(ResponseCodeEnum.LOGIN_TYPE_ERROR);
        }

        Long userId = null;

        //判断登录类型
        switch (loginTypeEnum) {
            case VERIFICATION_CODE:
                //验证码登录
                String verificationCode = userLoginRequest.getCode();

                Preconditions.checkArgument(StringUtils.isNotBlank(verificationCode), "验证码不能为空");

                // 构建验证码 Redis Key
                String key = RedisKeyConstants.buildVerificationCodeKey(phone);
                String code = (String) redisTemplate.opsForValue().get(key);

                if (!StringUtils.equals(verificationCode, code)) {
                    throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
                }

                // RPC: 调用用户服务，注册用户
                Long userIdTmp = userRpcService.registerUser(phone);

                // 若调用用户服务，返回的用户 ID 为空，则提示登录失败
                if (Objects.isNull(userIdTmp)) {
                    throw new BizException(ResponseCodeEnum.LOGIN_FAIL);
                }

                userId = userIdTmp;

                break;

            case PASSWORD:
                String password = userLoginRequest.getPassword();

                FindUserByPhoneRspDTO findUserByPhoneRspDTO  = userRpcService.findUserByPhone(phone);

                //是否注册
                if (Objects.isNull(findUserByPhoneRspDTO )) {
                    throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
                }

                String encodedPassword = findUserByPhoneRspDTO .getPassword();

                boolean matches = passwordEncoder.matches(password, encodedPassword);

                //如果不匹配，则抛出业务异常
                if (!matches) {
                    throw new BizException(ResponseCodeEnum.PHONE_OR_PASSWORD_ERROR);
                }
                userId = findUserByPhoneRspDTO .getId();

                break;
            default:
                break;
        }
        if (userId == null || userId.toString().isEmpty()) {
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



    @Override
    public Response<?> logout() {

        Long userId = LoginUserContextHolder.getUserId();

        log.info("==> 用户退出登录, userId: {}", userId);

        if (ObjectUtils.isEmpty(userId)) {
            return Response.fail(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 退出登录 (指定用户 ID)
        StpUtil.logout(userId);

        return Response.success();
    }

    @Override
    public Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO) {
        // 新密码
        String newPassword = updatePasswordReqVO.getNewPassword();
        // 密码加密
        String encodePassword = passwordEncoder.encode(newPassword);

        //RPC:调用用户服务更新密码
        userRpcService.updatePassword(encodePassword);

        return Response.success();
    }
}
