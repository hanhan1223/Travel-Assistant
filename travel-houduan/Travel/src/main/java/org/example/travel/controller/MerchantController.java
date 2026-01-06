package org.example.travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.example.travel.exception.ErrorCode;
import org.example.travel.exception.ThrowUtils;
import org.example.travel.model.dto.merchant.MerchantAddRequest;
import org.example.travel.model.entity.Merchant;
import org.example.travel.model.entity.User;
import org.example.travel.service.MerchantService;
import org.example.travel.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 商户控制器
 */
@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Resource
    private MerchantService merchantService;

    @Resource
    private UserService userService;

    /**
     * 根据ID获取商户详情
     */
    @GetMapping("/{id}")
    public BaseResponse<Merchant> getById(@PathVariable Long id) {
        Merchant merchant = merchantService.getById(id);
        ThrowUtils.throwIf(merchant == null, ErrorCode.NOT_FOUND_ERROR, "商户不存在");
        return Result.success(merchant);
    }

    /**
     * 根据类别查询商户
     */
    @GetMapping("/category/{category}")
    public BaseResponse<List<Merchant>> getByCategory(@PathVariable String category) {
        List<Merchant> merchants = merchantService.lambdaQuery()
                .like(Merchant::getCategory, category)
                .list();
        return Result.success(merchants);
    }

    /**
     * 根据非遗项目ID查询关联商户
     */
    @GetMapping("/project/{projectId}")
    public BaseResponse<List<Merchant>> getByProject(@PathVariable Long projectId) {
        List<Merchant> merchants = merchantService.lambdaQuery()
                .eq(Merchant::getProjectId, projectId)
                .orderByDesc(Merchant::getRelevanceScore)
                .list();
        return Result.success(merchants);
    }

    /**
     * 分页查询商户
     */
    @PostMapping("/list")
    public BaseResponse<Page<Merchant>> listMerchants(@RequestBody MerchantQueryRequest request) {
        Page<Merchant> page = new Page<>(request.getCurrent(), request.getPageSize());
        merchantService.lambdaQuery()
                .like(request.getName() != null, Merchant::getName, request.getName())
                .like(request.getCategory() != null, Merchant::getCategory, request.getCategory())
                .eq(request.getProjectId() != null, Merchant::getProjectId, request.getProjectId())
                .page(page);
        return Result.success(page);
    }

    /**
     * 获取高评分商户
     */
    @GetMapping("/top")
    public BaseResponse<List<Merchant>> getTopRated(@RequestParam(defaultValue = "10") Integer limit) {
        List<Merchant> merchants = merchantService.lambdaQuery()
                .orderByDesc(Merchant::getRating)
                .last("LIMIT " + limit)
                .list();
        return Result.success(merchants);
    }

    // ==================== 管理后台接口 ====================

    /**
     * 新增商户（管理员）
     * 注：商户与非遗项目的关联由非遗项目端管理
     */
    @PostMapping("/add")
    public BaseResponse<Long> addMerchant(@RequestBody MerchantAddRequest request, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(!"admin".equals(loginUser.getUserrole()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        
        ThrowUtils.throwIf(request.getName() == null || request.getName().isEmpty(), 
                ErrorCode.PARAMS_ERROR, "商户名称不能为空");
        
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(request, merchant);
        merchant.setCreatedAt(new Date());
        merchantService.save(merchant);
        return Result.success(merchant.getId());
    }

    /**
     * 更新商户（管理员）
     * 注：不能通过此接口修改projectId，商户关联由非遗项目端管理
     */
    @PutMapping("/update/{id}")
    public BaseResponse<Boolean> updateMerchant(@PathVariable Long id,
                                                 @RequestBody MerchantAddRequest request,
                                                 HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(!"admin".equals(loginUser.getUserrole()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        
        Merchant existing = merchantService.getById(id);
        ThrowUtils.throwIf(existing == null, ErrorCode.NOT_FOUND_ERROR, "商户不存在");
        
        // 检查是否有字段需要更新
        boolean hasFieldToUpdate = request.getName() != null || request.getCategory() != null
                || request.getLat() != null || request.getLng() != null
                || request.getRating() != null || request.getThirdPartyUrl() != null;
        
        if (!hasFieldToUpdate) {
            return Result.success(true); // 没有字段需要更新，直接返回成功
        }
        
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(request, merchant);
        merchant.setId(id);
        // 保留原有的projectId关联
        merchant.setProjectId(existing.getProjectId());
        boolean result = merchantService.updateById(merchant);
        return Result.success(result);
    }

    /**
     * 删除商户（管理员）
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteMerchant(@PathVariable Long id, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(!"admin".equals(loginUser.getUserrole()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        
        Merchant existing = merchantService.getById(id);
        ThrowUtils.throwIf(existing == null, ErrorCode.NOT_FOUND_ERROR, "商户不存在");
        
        boolean result = merchantService.removeById(id);
        return Result.success(result);
    }

    /**
     * 查询请求
     */
    @lombok.Data
    public static class MerchantQueryRequest {
        private Integer current = 1;
        private Integer pageSize = 10;
        private String name;
        private String category;
        private Long projectId;
    }
}
