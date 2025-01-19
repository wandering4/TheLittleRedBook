package com.haishi.LittleRedBook.note.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Hello world!
 *
 */
@EnableFeignClients(basePackages = "com.haishi.LittleRedBook")
@SpringBootApplication
@MapperScan("com.haishi.LittleRedBook.note.biz.domain.mapper")
public class LittleRedBookNoteBizApplication {
    public static void main( String[] args ) {
        SpringApplication.run(LittleRedBookNoteBizApplication.class,args);
    }
}
