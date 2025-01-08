package com.haishi.littleredbookauth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.haishi.framework.commons.constant.RedisKeyConstants;
import com.haishi.framework.commons.exception.BizException;
import com.haishi.framework.commons.response.Response;

import com.haishi.littleredbookauth.enums.ResponseCodeEnum;
import com.haishi.littleredbookauth.service.VerificationCodeService;
import com.haishi.littleredbookauth.model.vo.veriticationcode.SendVerificationCodeReqVO;
import com.haishi.littleredbookauth.sms.AliyunSmsHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private AliyunSmsHelper aliyunSmsHelper;

    /**
     * 发送短信验证码
     *
     * @param sendVerificationCodeReqVO
     * @return
     */
    @Override
    public Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        String phone = sendVerificationCodeReqVO.getPhone();
        String key= RedisKeyConstants.buildVerificationCodeKey(phone);

        boolean exist = redisTemplate.hasKey(key);
        if(exist){
            //若之前的验证码未过期，则提示发送频繁
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }


        //生成6位随机验证码
        String verificationCode= RandomUtil.randomNumbers(6);

        log.info("-->手机号：{},已生成验证码：[{}]", phone, verificationCode);

        //调用第三方短信发送服务
        threadPoolTaskExecutor.submit(() -> {
            String signName = "阿里云短信测试";
            String templateCode = "SMS_154950909";
            String templateParam = String.format("{\"code\":\"%s\"}", verificationCode);
            aliyunSmsHelper.sendMessage(signName, templateCode, phone, templateParam);
        });


        log.info("--> 手机号：{},已发送验证码：[{}]", phone,verificationCode);


        //验证码存储到redis,方便后面校验
        redisTemplate.opsForValue().set(key,verificationCode,3, TimeUnit.MINUTES);

        return Response.success();
    }
}
