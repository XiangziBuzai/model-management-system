package com.model.management;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.model.management.mapper")
@EnableScheduling // 启用定时任务支持
public class ModelManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModelManagementApplication.class, args);
    }
}
