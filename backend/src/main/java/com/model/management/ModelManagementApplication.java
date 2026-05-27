package com.model.management;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.model.management.mapper")
public class ModelManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModelManagementApplication.class, args);
    }
}
