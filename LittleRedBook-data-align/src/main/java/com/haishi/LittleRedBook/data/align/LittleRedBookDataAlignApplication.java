package com.haishi.LittleRedBook.data.align;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients(basePackages = "com.haishi.LittleRedBook")
@MapperScan("com.haishi.LittleRedBook.data.align.domain.mapper")
public class LittleRedBookDataAlignApplication {

    public static void main(String[] args) {
        SpringApplication.run(LittleRedBookDataAlignApplication.class, args);
    }

}
