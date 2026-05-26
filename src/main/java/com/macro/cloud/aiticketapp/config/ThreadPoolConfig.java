package com.macro.cloud.aiticketapp.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @program: AiTicketApp
 * @description: 异步线程池
 * @author: feiwei
 * @create: 2026-05-26 09:04
 **/

/**
 * AI任务专用线程池
 * 生产级配置 => 面试加分
 */
@Configuration
public class ThreadPoolConfig {

    @Bean("aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // 核心线程
        executor.setMaxPoolSize(10);      // 最大线程
        executor.setQueueCapacity(100);  // 缓冲队列
        executor.setKeepAliveSeconds(60); // 空闲时间
        executor.setThreadNamePrefix("ai-async-task-"); // 线程名（日志好排查）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
