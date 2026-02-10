package com.hmall.search;

import com.hmall.api.config.FeignLogLevelConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;


@MapperScan("com.hmall.search.mapper")
@EnableFeignClients(basePackages = "com.hmall.api.client", defaultConfiguration = FeignLogLevelConfig.class)
@SpringBootApplication
public class SearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }
}