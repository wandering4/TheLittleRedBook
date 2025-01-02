package com.haishi.LittleRedBook.user.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.haishi.LittleRedBook.user.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.haishi.LittleRedBook")
public class LittleRedBookUserBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(LittleRedBookUserBizApplication.class, args);
    }
}
