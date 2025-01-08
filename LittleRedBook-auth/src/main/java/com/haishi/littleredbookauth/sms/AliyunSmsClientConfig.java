package com.haishi.littleredbookauth.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliyunSmsProperties.class)
@ConditionalOnProperty(name = "aliyun.sms.endpoint")
@Slf4j
public class AliyunSmsClientConfig {

    @Resource
    private AliyunSmsProperties aliyunSmsProperties;

    @Bean
    public Client smsClient() {
        try {
            Config config = new Config()
                    // 必填
                    .setAccessKeyId(aliyunSmsProperties.getAccessKeyId())
                    // 必填
                    .setAccessKeySecret(aliyunSmsProperties.getAccessKeySecret())
                    //必填
                    .setEndpoint(aliyunSmsProperties.getEndpoint());
            return new Client(config);
        } catch (Exception e) {
            log.error("初始化阿里云短信发送客户端错误: ", e);
            return null;
        }
    }
}
