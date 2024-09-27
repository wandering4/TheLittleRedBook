package com.haishi.littleredbookauth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Stack;

@SpringBootApplication
@MapperScan("com.haishi.littleredbookauth.domain.mapper")
public class LittleRedBookAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(LittleRedBookAuthApplication.class, args);
    }

}
