package com.haishi.LittleRedBook.count.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.haishi.LittleRedBook.count.biz.domain.mapper")
public class LittleRedBookCountBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(LittleRedBookCountBizApplication.class, args);
    }

}
