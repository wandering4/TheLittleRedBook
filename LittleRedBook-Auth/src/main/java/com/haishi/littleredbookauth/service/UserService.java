package com.haishi.littleredbookauth.service;

import com.haishi.framework.common.response.Response;
import com.haishi.littleredbookauth.model.vo.user.UserLoginReqVO;

public interface UserService {
    /**
     * 登录与注册
     * @param userLoginReqVO
     * @return
     */
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);
}
