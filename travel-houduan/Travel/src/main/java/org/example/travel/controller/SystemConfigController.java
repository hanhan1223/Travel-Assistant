package org.example.travel.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.example.travel.model.dto.config.ConfigUpdateRequest;
import org.example.travel.model.dto.config.ConfigVO;
import org.example.travel.service.SystemConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置管理接口
 */
@RestController
@RequestMapping("/system/config")
@Tag(name = "系统配置管理")
@Slf4j
public class SystemConfigController {

    @Resource
    private SystemConfigService configService;

    @GetMapping("/list")
    @Operation(summary = "获取所有配置（按分组）")
    @SaCheckRole("admin")
    public BaseResponse<Map<String, List<ConfigVO>>> getAllConfigs() {
        Map<String, List<ConfigVO>> configs = configService.getAllConfigs();
        return Result.success(configs);
    }

    @GetMapping("/group/{group}")
    @Operation(summary = "获取指定分组的配置")
    @SaCheckRole("admin")
    public BaseResponse<List<ConfigVO>> getConfigsByGroup(@PathVariable String group) {
        List<ConfigVO> configs = configService.getConfigsByGroup(group);
        return Result.success(configs);
    }

    @GetMapping("/value/{key}")
    @Operation(summary = "获取配置值（明文）")
    @SaCheckRole("admin")
    public BaseResponse<String> getConfigValue(@PathVariable String key) {
        String value = configService.getConfigValue(key);
        return Result.success(value);
    }

    @PostMapping("/update")
    @Operation(summary = "更新配置")
    @SaCheckRole("admin")
    public BaseResponse<Boolean> updateConfig(@RequestBody ConfigUpdateRequest request) {
        boolean result = configService.updateConfig(request);
        return Result.success(result);
    }

    @PostMapping("/batch-update")
    @Operation(summary = "批量更新配置")
    @SaCheckRole("admin")
    public BaseResponse<Boolean> batchUpdateConfigs(@RequestBody List<ConfigUpdateRequest> requests) {
        boolean result = configService.batchUpdateConfigs(requests);
        return Result.success(result);
    }

    @PostMapping("/reload")
    @Operation(summary = "重新加载配置")
    @SaCheckRole("admin")
    public BaseResponse<String> reloadConfigs() {
        configService.reloadConfigs();
        return Result.success("配置重新加载成功");
    }

    @PostMapping("/test/{key}")
    @Operation(summary = "测试配置连接")
    @SaCheckRole("admin")
    public BaseResponse<Map<String, Object>> testConnection(@PathVariable String key) {
        Map<String, Object> result = configService.testConnection(key);
        return Result.success(result);
    }
}
