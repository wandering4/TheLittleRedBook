package com.haishi.littleredbookauth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Stack;

@SpringBootApplication
@MapperScan("com.haishi.littleredbookauth.domain.mapper")
@ComponentScan({"com.haishi.littleredbookauth", "com.haishi.framework.common"})
public class LittleRedBookAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(LittleRedBookAuthApplication.class, args);
    }

}
