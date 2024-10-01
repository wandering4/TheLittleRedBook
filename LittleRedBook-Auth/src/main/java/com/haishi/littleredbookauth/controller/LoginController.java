package com.haishi.littleredbookauth.controller;

import com.haishi.framework.common.constant.RedisKeyConstants;
import com.haishi.framework.common.validator.PhoneNumber;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/user/verification/login")
    public String verificationLogin(@Validated @PhoneNumber String phoneNumber,@Validated @NotEmpty String verificationCode) {
        String code=stringRedisTemplate.opsForValue().get(RedisKeyConstants.buildVerificationCodeKey(phoneNumber));
        if(verificationCode.equals(code)){

        }else{

        }
    }
}
