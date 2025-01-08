package com.haishi.littleredbookauth;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.haishi.LittleRedBook")
public class LittleRedBookAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(LittleRedBookAuthApplication.class, args);
    }

}
