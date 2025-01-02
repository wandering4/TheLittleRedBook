package com.haishi.LittleRedBook.user.biz.service;

import com.haishi.LittleRedBook.user.biz.model.vo.UpdateUserInfoReqVO;
import com.haishi.framework.commons.response.Response;

/**
 * @description: 用户业务
 */
public interface UserService {

    /**
     * 更新用户信息
     *
     * @param updateUserInfoReqVO
     * @return
     */
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);
}