package com.nutri.guard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.nutri.guard.mapper") // 扫描 Mapper 接口
public class NutriGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(NutriGuardApplication.class, args);
    }
}