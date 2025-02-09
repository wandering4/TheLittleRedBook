package com.haishi.LittleRedBook.comment.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry // 启用 Spring Retry
@SpringBootApplication
@MapperScan("com.haishi.LittleRedBook.comment.biz.domain.mapper")
public class LittleRedBookCommentBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(LittleRedBookCommentBizApplication.class, args);
    }
}
