package com.haishi.LittleRedBook.user.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.haishi.LittleRedBook.user.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.haishi.LittleRedBook")
@ComponentScan({"com.haishi.LittleRedBook.user.biz", "com.haishi.LittleRedBook.count"}) //  多模块扫描
public class LittleRedBookUserBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(LittleRedBookUserBizApplication.class, args);
    }
}
