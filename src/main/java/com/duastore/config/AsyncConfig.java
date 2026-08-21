package com.duastore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Cau hinh bat dong bo (@Async / Queue nhe).
 * Using: gui email / thong bao khong duoc chet nghe request chinh (checkout, cap nhat don).
 * Viec gui email duoc day qua hang doi nen nho (thread pool) — khong can Kafka/RabbitMQ.
 */
@Configuration
@EnableAsync
/**
 * Lớp cấu hình Spring liên quan tới async config.
 */
public class AsyncConfig {

    @Bean(name = "duastoreMailExecutor")
    public Executor duastoreMailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("mail-async-");
        executor.initialize();
        return executor;
    }
}