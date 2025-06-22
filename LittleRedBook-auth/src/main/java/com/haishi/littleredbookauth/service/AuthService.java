package com.haishi.littleredbookauth.service;


import com.haishi.framework.commons.response.Response;
import com.haishi.littleredbookauth.model.vo.user.UpdatePasswordReqVO;
import com.haishi.littleredbookauth.model.vo.user.UserLoginRequest;

public interface AuthService {
    /**
     * 登录与注册
     * @param userLoginRequest
     * @return
     */
    Response<String> loginAndRegister(UserLoginRequest userLoginRequest);


    /**
     * 退出登录
     * @return
     */
    Response<?> logout();


    /**
     * 修改密码
     * @param updatePasswordReqVO
     * @return
     */
    Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO);

}
