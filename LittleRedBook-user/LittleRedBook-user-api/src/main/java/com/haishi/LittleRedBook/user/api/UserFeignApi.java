package com.haishi.LittleRedBook.user.api;

import com.haishi.LittleRedBook.user.constant.ApiConstants;
import com.haishi.LittleRedBook.user.dto.req.*;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByIdResponse;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByPhoneRspDTO;
import com.haishi.framework.commons.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface UserFeignApi {

    String PREFIX = "/user";

    /**
     * 用户注册
     *
     * @param registerUserRequest
     * @return
     */
    @PostMapping(value = PREFIX + "/register")
    Response<Long> registerUser(@RequestBody RegisterUserRequest registerUserRequest);

    /**
     * 根据手机号查询用户信息
     *
     * @param findUserByPhoneRequest
     * @return
     */
    @PostMapping(value = PREFIX + "/findByPhone")
    Response<FindUserByPhoneRspDTO> findByPhone(@RequestBody FindUserByPhoneRequest findUserByPhoneRequest);

    /**
     * 更新密码
     *
     * @param updateUserPasswordRequest
     * @return
     */
    @PostMapping(value = PREFIX + "/password/update")
    Response<?> updatePassword(@RequestBody UpdateUserPasswordRequest updateUserPasswordRequest);

    /**
     * 根据用户 ID 查询用户信息
     *
     * @param findUserByIdRequest
     * @return
     */
    @PostMapping(value = PREFIX + "/findById")
    Response<FindUserByIdResponse> findById(@RequestBody FindUserByIdRequest findUserByIdRequest);

    /**
     * 批量查询用户信息
     *
     * @param findUsersByIdsReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/findByIds")
    Response<List<FindUserByIdResponse>> findByIds(@RequestBody FindUsersByIdsReqDTO findUsersByIdsReqDTO);

}
