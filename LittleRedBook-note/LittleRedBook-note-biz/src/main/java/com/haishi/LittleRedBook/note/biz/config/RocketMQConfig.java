package com.haishi.LittleRedBook.note.biz.config;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/*兼容性问题，手动导入一下*/
@Configuration
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
}
