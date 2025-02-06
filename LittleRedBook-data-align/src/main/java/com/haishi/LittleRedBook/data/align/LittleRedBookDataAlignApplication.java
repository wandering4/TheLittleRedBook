package com.haishi.LittleRedBook.data.align;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.haishi.LittleRedBook.data.align.domain.mapper")
public class LittleRedBookDataAlignApplication {

    public static void main(String[] args) {
        SpringApplication.run(LittleRedBookDataAlignApplication.class, args);
    }

}
