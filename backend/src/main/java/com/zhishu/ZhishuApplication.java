package com.zhishu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zhishu.mapper")
public class ZhishuApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhishuApplication.class, args);
    }
}