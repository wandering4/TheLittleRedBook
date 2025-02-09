package com.haishi.LittleRedBook.comment.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @version: v1.0.0
 * @description: Retry 配置读取
 **/
@ConfigurationProperties(prefix = RetryProperties.PREFIX)
@Component
@Data
public class RetryProperties {

    public static final String PREFIX = "retry";

    /**
     * 最大重试次数
     */
    private Integer maxAttempts = 3;

    /**
     * 初始间隔时间，单位 ms
     */
    private Integer initInterval = 1000;

    /**
     * 乘积（每次乘以 2）
     */
    private Double multiplier = 2.0;

}
