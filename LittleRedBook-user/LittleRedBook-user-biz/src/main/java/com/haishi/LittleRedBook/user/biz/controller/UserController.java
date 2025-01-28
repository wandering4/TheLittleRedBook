package com.haishi.LittleRedBook.user.biz.controller;

import com.haishi.LittleRedBook.user.biz.model.vo.request.UpdateUserInfoRequest;
import com.haishi.LittleRedBook.user.biz.service.UserService;
import com.haishi.LittleRedBook.user.dto.req.*;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByIdResponse;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByPhoneRspDTO;
import com.haishi.framework.biz.operationlog.aspect.ApiOperationLog;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户信息修改
     *
     * @param updateUserInfoRequest
     * @return
     */
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> updateUserInfo(@Validated UpdateUserInfoRequest updateUserInfoRequest) {
        return userService.updateUserInfo(updateUserInfoRequest);
    }

    // ===================================== 对其他服务提供的接口 =====================================
    @PostMapping("/register")
    @ApiOperationLog(description = "用户注册")
    public Response<Long> register(@Validated @RequestBody RegisterUserRequest registerUserRequest) {
        return userService.register(registerUserRequest);
    }

    @PostMapping("/findByPhone")
    @ApiOperationLog(description = "手机号查询用户信息")
    public Response<FindUserByPhoneRspDTO> findByPhone(@Validated @RequestBody FindUserByPhoneRequest findUserByPhoneRequest) {
        return userService.findByPhone(findUserByPhoneRequest);
    }

    @PostMapping("/password/update")
    @ApiOperationLog(description = "密码更新")
    public Response<?> updatePassword(@Validated @RequestBody UpdateUserPasswordRequest updateUserPasswordRequest) {
        return userService.updatePassword(updateUserPasswordRequest);
    }

    @PostMapping("/findById")
    @ApiOperationLog(description = "查询用户信息")
    public Response<FindUserByIdResponse> findById(@Validated @RequestBody FindUserByIdRequest findUserByIdRequest) {
        return userService.findById(findUserByIdRequest);
    }

    @PostMapping("/findByIds")
    @ApiOperationLog(description = "批量查询用户信息")
    public Response<List<FindUserByIdResponse>> findByIds(@Validated @RequestBody FindUsersByIdsReqDTO findUsersByIdsReqDTO) {
        return userService.findByIds(findUsersByIdsReqDTO);
    }

}
