package org.example.travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.example.travel.exception.ErrorCode;
import org.example.travel.exception.ThrowUtils;
import org.example.travel.model.entity.GeneratedDocument;
import org.example.travel.model.entity.User;
import org.example.travel.service.GeneratedDocumentService;
import org.example.travel.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 生成文档控制器（PDF游览报告等）
 */
@RestController
@RequestMapping("/document")
public class GeneratedDocumentController {

    @Resource
    private GeneratedDocumentService generatedDocumentService;

    @Resource
    private UserService userService;

    /**
     * 根据ID获取文档
     */
    @GetMapping("/{id}")
    public BaseResponse<GeneratedDocument> getById(@PathVariable Long id, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        GeneratedDocument document = generatedDocumentService.getById(id);
        ThrowUtils.throwIf(document == null || !document.getUserId().equals(loginUser.getId()),
                ErrorCode.NOT_FOUND_ERROR, "文档不存在");
        return Result.success(document);
    }

    /**
     * 获取当前用户的文档列表
     */
    @PostMapping("/my")
    public BaseResponse<Page<GeneratedDocument>> getMyDocuments(
            @RequestBody DocumentQueryRequest request,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Page<GeneratedDocument> page = new Page<>(request.getCurrent(), request.getPageSize());
        generatedDocumentService.lambdaQuery()
                .eq(GeneratedDocument::getUserId, loginUser.getId())
                .eq(request.getProjectId() != null, GeneratedDocument::getProjectId, request.getProjectId())
                .orderByDesc(GeneratedDocument::getCreatedAt)
                .page(page);
        return Result.success(page);
    }

    /**
     * 保存生成的文档记录
     */
    @PostMapping("/save")
    public BaseResponse<Long> saveDocument(
            @RequestBody GeneratedDocument document,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(document.getFileUrl() == null, ErrorCode.PARAMS_ERROR, "文件URL不能为空");
        document.setUserId(loginUser.getId());
        document.setCreatedAt(new Date());
        generatedDocumentService.save(document);
        return Result.success(document.getId());
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteDocument(@PathVariable Long id, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        GeneratedDocument document = generatedDocumentService.getById(id);
        ThrowUtils.throwIf(document == null || !document.getUserId().equals(loginUser.getId()),
                ErrorCode.NOT_FOUND_ERROR, "文档不存在");
        boolean result = generatedDocumentService.removeById(id);
        return Result.success(result);
    }

    @lombok.Data
    public static class DocumentQueryRequest {
        private Integer current = 1;
        private Integer pageSize = 10;
        private Long projectId;
    }
}
