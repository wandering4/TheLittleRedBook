package com.haishi.littleredbookauth.config;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.redisson.config.Config;

@Configuration
@ConditionalOnProperty(name = "spring.data.redis.redisson.enabled", havingValue = "true")
public class RedissonConfig {

    @Value("${spring.data.redis.password}")
    private String password;

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private Integer port;

    @Value("${spring.data.redis.redisson.mode}")
    private String mode;

    @Value("${spring.data.redis.database: 0}")
    private Integer database;



    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        switch (mode.toLowerCase()) {
            case "single":
                // 单机模式配置
                SingleServerConfig serverConfig = config.useSingleServer()
                        .setAddress("redis://" + host + ":" + port)  // Redis 地址
                        .setPassword(password)  // Redis 密码
                        .setDatabase(database);  // Redis 数据库
                break;
            default:
                throw new IllegalArgumentException("Invalid Redis mode: " + mode);
        }

        return Redisson.create(config);
    }


}
