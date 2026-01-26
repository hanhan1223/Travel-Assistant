package org.example.travel.controller;

import jakarta.annotation.Resource;
import lombok.Builder;
import lombok.Data;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 系统监控接口
 * 用于查看线程池状态、性能指标等
 */
@RestController
@RequestMapping("/monitor")
public class MonitorController {
    
    @Resource
    @Qualifier("toolExecutor")
    private Executor toolExecutor;
    
    @Resource
    @Qualifier("asyncExecutor")
    private Executor asyncExecutor;
    
    @Resource
    private ToolCallback[] allTools;
    
    /**
     * 获取所有已注册的工具列表
     */
    @GetMapping("/tools")
    public BaseResponse<List<ToolInfo>> getRegisteredTools() {
        List<ToolInfo> toolInfos = Arrays.stream(allTools)
            .map(tool -> ToolInfo.builder()
                .name(tool.getToolDefinition().name())
                .description(tool.getToolDefinition().description())
                .className(tool.getClass().getSimpleName())
                .build())
            .collect(Collectors.toList());
        
        return Result.success(toolInfos);
    }
    
    /**
     * 获取工具调用线程池统计信息
     */
    @GetMapping("/threadpool/tool")
    public BaseResponse<ThreadPoolStats> getToolThreadPoolStats() {
        if (toolExecutor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) toolExecutor;
            ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();
            
            ThreadPoolStats stats = ThreadPoolStats.builder()
                .poolName("工具调用线程池")
                .corePoolSize(threadPoolExecutor.getCorePoolSize())
                .maxPoolSize(threadPoolExecutor.getMaximumPoolSize())
                .currentPoolSize(threadPoolExecutor.getPoolSize())
                .activeThreads(threadPoolExecutor.getActiveCount())
                .queueSize(threadPoolExecutor.getQueue().size())
                .queueCapacity(executor.getQueueCapacity())
                .completedTasks(threadPoolExecutor.getCompletedTaskCount())
                .totalTasks(threadPoolExecutor.getTaskCount())
                .build();
            
            return Result.success(stats);
        }
        
        return (BaseResponse<ThreadPoolStats>) Result.error(500, "线程池类型不支持");
    }
    
    /**
     * 获取异步任务线程池统计信息
     */
    @GetMapping("/threadpool/async")
    public BaseResponse<ThreadPoolStats> getAsyncThreadPoolStats() {
        if (asyncExecutor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncExecutor;
            ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();
            
            ThreadPoolStats stats = ThreadPoolStats.builder()
                .poolName("异步任务线程池")
                .corePoolSize(threadPoolExecutor.getCorePoolSize())
                .maxPoolSize(threadPoolExecutor.getMaximumPoolSize())
                .currentPoolSize(threadPoolExecutor.getPoolSize())
                .activeThreads(threadPoolExecutor.getActiveCount())
                .queueSize(threadPoolExecutor.getQueue().size())
                .queueCapacity(executor.getQueueCapacity())
                .completedTasks(threadPoolExecutor.getCompletedTaskCount())
                .totalTasks(threadPoolExecutor.getTaskCount())
                .build();
            
            return Result.success(stats);
        }
        
        return (BaseResponse<ThreadPoolStats>) Result.error(500, "线程池类型不支持");
    }
    
    /**
     * 工具信息
     */
    @Data
    @Builder
    public static class ToolInfo {
        private String name;
        private String description;
        private String className;
    }
    
    /**
     * 线程池统计信息
     */
    @Data
    @Builder
    public static class ThreadPoolStats {
        private String poolName;
        private int corePoolSize;
        private int maxPoolSize;
        private int currentPoolSize;
        private int activeThreads;
        private int queueSize;
        private int queueCapacity;
        private long completedTasks;
        private long totalTasks;
        
        public double getUtilization() {
            return currentPoolSize > 0 ? (double) activeThreads / currentPoolSize * 100 : 0;
        }
        
        public double getQueueUtilization() {
            return queueCapacity > 0 ? (double) queueSize / queueCapacity * 100 : 0;
        }
    }
}
