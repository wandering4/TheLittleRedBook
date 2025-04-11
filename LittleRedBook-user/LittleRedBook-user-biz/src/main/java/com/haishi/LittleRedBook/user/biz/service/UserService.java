package com.haishi.LittleRedBook.user.biz.service;

import com.haishi.LittleRedBook.user.biz.model.vo.request.FindUserProfileReqVO;
import com.haishi.LittleRedBook.user.biz.model.vo.response.FindUserProfileRspVO;
import com.haishi.LittleRedBook.user.dto.req.*;
import com.haishi.LittleRedBook.user.biz.model.vo.request.UpdateUserInfoRequest;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByIdResponse;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByPhoneRspDTO;
import com.haishi.framework.commons.response.Response;

import java.util.List;

/**
 * @description: 用户业务
 */
public interface UserService {

    /**
     * 更新用户信息
     *
     * @param updateUserInfoRequest
     * @return
     */
    Response<?> updateUserInfo(UpdateUserInfoRequest updateUserInfoRequest);

    /**
     * 用户注册
     *
     * @param registerUserRequest
     * @return
     */
    Response<Long> register(RegisterUserRequest registerUserRequest);

    /**
     * 根据手机号查询用户信息
     *
     * @param findUserByPhoneRequest
     * @return
     */
    Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneRequest findUserByPhoneRequest);

    /**
     * 更新密码
     *
     * @param updateUserPasswordRequest
     * @return
     */
    Response<?> updatePassword(UpdateUserPasswordRequest updateUserPasswordRequest);

    /**
     * 根据用户 ID 查询用户信息
     *
     * @param findUserByIdRequest
     * @return
     */
    Response<FindUserByIdResponse> findById(FindUserByIdRequest findUserByIdRequest);

    /**
     * 批量根据用户 ID 查询用户信息
     *
     * @param findUsersByIdsReqDTO
     * @return
     */
    Response<List<FindUserByIdResponse>> findByIds(FindUsersByIdsReqDTO findUsersByIdsReqDTO);


    /**
     * 获取用户主页信息
     *
     * @return
     */
    Response<FindUserProfileRspVO> findUserProfile(FindUserProfileReqVO findUserProfileReqVO);


}