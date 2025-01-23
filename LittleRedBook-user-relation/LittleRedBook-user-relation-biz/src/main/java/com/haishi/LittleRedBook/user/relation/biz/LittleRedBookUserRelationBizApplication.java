package com.haishi.LittleRedBook.user.relation.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@MapperScan("com.haishi.LittleRedBook.user.relation.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.haishi.LittleRedBook")
public class LittleRedBookUserRelationBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(LittleRedBookUserRelationBizApplication.class, args);
    }
}