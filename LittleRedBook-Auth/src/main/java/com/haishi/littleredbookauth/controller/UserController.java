package com.haishi.littleredbookauth.controller;

import com.haishi.framework.biz.operationlog.aspect.ApiOperationLog;
import com.haishi.framework.common.response.Response;
import com.haishi.littleredbookauth.model.vo.user.UserLoginReqVO;
import com.haishi.littleredbookauth.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    @ApiOperationLog(description = "用户登录/注册")
    public Response<String> loginAndRegister(@Validated @RequestBody UserLoginReqVO userLoginReqVO) {
        return userService.loginAndRegister(userLoginReqVO);
    }

    @PostMapping("/logout")
    @ApiOperationLog(description = "账号登出")
    public Response<?> logout(@RequestHeader("userId") String userId) {

        // todo 账号退出登录逻辑待实现

        log.info("==> 网关透传过来的用户 ID: {}", userId);

        return Response.success();
    }

}
