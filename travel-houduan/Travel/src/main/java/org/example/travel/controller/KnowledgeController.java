package org.example.travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.example.travel.exception.ErrorCode;
import org.example.travel.exception.ThrowUtils;
import org.example.travel.model.dto.knowledge.KnowledgeQueryRequest;
import org.example.travel.model.dto.knowledge.KnowledgeUploadRequest;
import org.example.travel.model.entity.KnowledgeDocument;
import org.example.travel.model.entity.User;
import org.example.travel.service.KnowledgeService;
import org.example.travel.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库管理控制器
 */
@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    @Resource
    private KnowledgeService knowledgeService;

    @Resource
    private UserService userService;

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public BaseResponse<Long> uploadDocument(
            @RequestParam("file") MultipartFile file,
            KnowledgeUploadRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(httpRequest);
        Long documentId = knowledgeService.uploadDocument(file, request, loginUser.getId());
        return Result.success(documentId);
    }

    /**
     * 分页查询文档列表
     */
    @PostMapping("/list")
    public BaseResponse<Page<KnowledgeDocument>> listDocuments(
            @RequestBody KnowledgeQueryRequest request,
            HttpServletRequest httpRequest) {
        userService.getLoginUser(httpRequest);
        Page<KnowledgeDocument> page = knowledgeService.listDocuments(request);
        return Result.success(page);
    }

    /**
     * 获取文档详情
     */
    @GetMapping("/{id}")
    public BaseResponse<KnowledgeDocument> getDocument(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        userService.getLoginUser(httpRequest);
        KnowledgeDocument document = knowledgeService.getById(id);
        ThrowUtils.throwIf(document == null, ErrorCode.NOT_FOUND_ERROR, "文档不存在");
        return Result.success(document);
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteDocument(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        userService.getLoginUser(httpRequest);
        boolean result = knowledgeService.deleteDocument(id);
        return Result.success(result);
    }

    /**
     * 更新文档（替换文件并重新向量化）
     */
    @PutMapping("/update/{id}")
    public BaseResponse<Long> updateDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            KnowledgeUploadRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        User loginUser = userService.getLoginUser(httpRequest);
        Long documentId = knowledgeService.updateDocument(id, file, request, loginUser.getId());
        return Result.success(documentId);
    }

    /**
     * 重新向量化文档
     */
    @PostMapping("/revectorize/{id}")
    public BaseResponse<Boolean> reVectorize(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        userService.getLoginUser(httpRequest);
        boolean result = knowledgeService.reVectorize(id);
        return Result.success(result);
    }
}
