package com.cheolhyeon.diary.app.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignRetryConfig {

    @Bean
    public Retryer feignRetryer() {
        // 초기지연 100ms, 최대지연 2000ms, 최대시도 5회(최초 1 + 재시도 =4
        return new Retryer.Default(100, 2000, 5);
    }
}
