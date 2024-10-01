package com.haishi.littleredbookauth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.haishi.framework.biz.operationlog.aspect.ApiOperationLog;
import com.haishi.framework.common.response.Response;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

@RestController
public class TestController {

    @GetMapping("/test")
    @ApiOperationLog(description = "测试接口")
    public Response<String> test() {
        return Response.success("Hello, 犬小哈专栏");
    }
    @GetMapping("/test2")
    @ApiOperationLog(description = "测试接口2")
    public Response<User> test2() {
        return Response.success(User.builder()
                .nickName("犬小哈")
                .createTime(LocalDateTime.now())
                .build());
    }

    @PostMapping("/test2")
    @ApiOperationLog(description = "测试接口2")
    public Response<User> test2(@RequestBody @Validated User user) {
        int i = 1 / 0;
        return Response.success(user);
    }


    @RequestMapping("/user/pwd/login")
    public String login(String username, String password) {
        if("admin".equals(username) && "123456".equals(password)) {
            StpUtil.login(100);
            return "success";
        }
        return "fail";
    }

    @RequestMapping("/user/isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

}
