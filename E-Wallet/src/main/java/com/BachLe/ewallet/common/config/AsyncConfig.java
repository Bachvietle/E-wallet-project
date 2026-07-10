package com.BachLe.ewallet.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailListenerTaskExecutor")
    public Executor emailListenerTaskExecutor(){
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(name = "notificationListenerTaskExecutor")
    public Executor notificationListenerTaskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
