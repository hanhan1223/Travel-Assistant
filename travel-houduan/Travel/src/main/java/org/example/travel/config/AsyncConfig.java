package org.example.travel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置
 * 用于优化工具调用和异步任务处理性能
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {
    
    /**
     * 工具调用专用线程池
     * 用于并行执行Agent的工具调用，提升响应速度
     * 
     * 线程数计算策略：
     * - IO密集型任务（网络请求、数据库查询）：CPU核心数 * 2
     * - 最小核心线程数：4（保证基本并发能力）
     * - 最大线程数：核心线程数 * 2.5（应对突发流量）
     */
    @Bean("toolExecutor")
    public Executor toolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 获取CPU核心数
        int cpuCores = Runtime.getRuntime().availableProcessors();
        log.info("检测到CPU核心数: {}", cpuCores);
        
        // 核心线程数：IO密集型任务，设置为 CPU核心数 * 2，最小为4
        int corePoolSize = Math.max(4, cpuCores * 2);
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程数：核心线程数 * 2.5，用于应对突发流量
        int maxPoolSize = (int) (corePoolSize * 2.5);
        executor.setMaxPoolSize(maxPoolSize);
        
        // 队列容量：根据核心线程数动态调整
        // 队列容量 = 核心线程数 * 10（保证有足够的缓冲）
        int queueCapacity = corePoolSize * 10;
        executor.setQueueCapacity(queueCapacity);
        
        // 线程名称前缀（便于日志追踪）
        executor.setThreadNamePrefix("tool-exec-");
        
        // 拒绝策略：队列满时，由调用线程执行（保证任务不丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 线程空闲时间：60秒后回收
        executor.setKeepAliveSeconds(60);
        
        // 允许核心线程超时（节省资源）
        executor.setAllowCoreThreadTimeOut(true);
        
        // 等待所有任务完成后关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("工具调用线程池初始化完成 - CPU核心数: {}, 核心线程数: {}, 最大线程数: {}, 队列容量: {}", 
            cpuCores, corePoolSize, maxPoolSize, queueCapacity);
        
        return executor;
    }
    
    /**
     * 通用异步任务线程池
     * 用于处理聊天、推荐等异步任务
     * 
     * 线程数计算策略：
     * - 混合型任务（CPU + IO）：CPU核心数 * 1.5
     * - 最小核心线程数：2（保证基本能力）
     * - 最大线程数：核心线程数 * 2（应对突发流量）
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 获取CPU核心数
        int cpuCores = Runtime.getRuntime().availableProcessors();
        
        // 核心线程数：混合型任务，设置为 CPU核心数 * 1.5，最小为2
        int corePoolSize = Math.max(2, (int) (cpuCores * 1.5));
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程数：核心线程数 * 2
        int maxPoolSize = corePoolSize * 2;
        executor.setMaxPoolSize(maxPoolSize);
        
        // 队列容量：根据核心线程数动态调整
        int queueCapacity = corePoolSize * 10;
        executor.setQueueCapacity(queueCapacity);
        
        executor.setThreadNamePrefix("async-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("异步任务线程池初始化完成 - CPU核心数: {}, 核心线程数: {}, 最大线程数: {}, 队列容量: {}", 
            cpuCores, corePoolSize, maxPoolSize, queueCapacity);
        
        return executor;
    }
}
