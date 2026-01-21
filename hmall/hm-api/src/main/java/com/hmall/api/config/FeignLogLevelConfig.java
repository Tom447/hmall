package com.hmall.api.config;

import com.hmall.api.interceptors.UserInfoInterceptpor;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class FeignLogLevelConfig {
    @Bean
    public Logger.Level feignLogLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoInterceptor() {
        return new UserInfoInterceptpor();
    }
}
