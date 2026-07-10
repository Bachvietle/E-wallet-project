package com.BachLe.ewallet.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailListenerTaskExecutor")
    public Executor emailListenerTaskExecutor(){

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // corePoolSize
        executor.setCorePoolSize(20);

        // maximumPoolSize
        executor.setMaxPoolSize(100);

        // queue
        executor.setQueueCapacity(100);

        // tiền tố của Thread để dễ debug
        executor.setThreadNamePrefix("Email-Thread-");

        // khởi tạo pool
        executor.initialize();

        return executor;
    }

    @Bean(name = "notificationListenerTaskExecutor")
    public Executor notificationListenerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("RabbitWorker-");
        executor.initialize();
        return executor;
    }
}
