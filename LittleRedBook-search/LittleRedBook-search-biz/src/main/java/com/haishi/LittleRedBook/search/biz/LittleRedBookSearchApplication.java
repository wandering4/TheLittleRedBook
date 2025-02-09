package com.haishi.LittleRedBook.search.biz;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("com.haishi.LittleRedBook.search.biz.domain.mapper")
public class LittleRedBookSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(LittleRedBookSearchApplication.class, args);
    }
}
