package org.example.travel.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.cos.model.COSObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.constants.CacheConstants;
import org.example.travel.exception.BusinessException;
import org.example.travel.exception.ErrorCode;
import org.example.travel.manager.CosManager;
import org.example.travel.mapper.KnowledgeDocumentMapper;
import org.example.travel.model.dto.knowledge.KnowledgeQueryRequest;
import org.example.travel.model.dto.knowledge.KnowledgeUploadRequest;
import org.example.travel.model.entity.KnowledgeDocument;
import org.example.travel.rag.CustomPgVectorStore;
import org.example.travel.service.CacheService;
import org.example.travel.service.KnowledgeService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 知识库服务实现
 */
@Service
@Slf4j
public class KnowledgeServiceImpl extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument>
        implements KnowledgeService {

    @Resource
    private CosManager cosManager;

    @Resource(name = "customVectorStore")
    private CustomPgVectorStore vectorStore;
    
    @Resource
    private CacheService cacheService;

    // 支持的文件类型
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "txt", "pdf", "doc", "docx", "md", "html"
    );

    @Override
    public Long uploadDocument(MultipartFile file, KnowledgeUploadRequest request, Long userId) {
        // 1. 校验文件
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }
        String extension = FileUtil.extName(originalFilename).toLowerCase();
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                    "不支持的文件类型，支持：" + String.join(",", SUPPORTED_EXTENSIONS));
        }

        // 2. 上传文件到COS
        String filePath;
        try {
            filePath = cosManager.putObject(file, "knowledge/" + UUID.randomUUID() + "." + extension);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }

        // 3. 保存文档记录
        KnowledgeDocument document = new KnowledgeDocument();
        document.setTitle(StrUtil.isNotBlank(request.getTitle()) ? request.getTitle() : originalFilename);
        document.setDocType(request.getDocType());
        document.setFileName(originalFilename);
        document.setFilePath(filePath);
        document.setFileSize(file.getSize());
        document.setProjectId(request.getProjectId());
        document.setMerchantId(request.getMerchantId());
        document.setTags(request.getTags());
        document.setVectorStatus(0); // 待处理
        document.setCreateUserId(userId);
        document.setCreatedAt(new Date());
        this.save(document);

        // 4. 异步进行文档切分和向量化
        final Long documentId = document.getId();
        final Long projectId = request.getProjectId();
        final Long merchantId = request.getMerchantId();
        final String fileName = originalFilename; // 使用已有的变量
        
        // 在异步处理之前，先读取文件内容到内存（避免临时文件被清理）
        final byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            log.error("读取文件内容失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件内容失败");
        }
        
        CompletableFuture.runAsync(() -> {
            processDocument(documentId, fileBytes, fileName, projectId, merchantId);
        });

        return document.getId();
    }

    /**
     * 处理文档：解析、切分、向量化
     */
    private void processDocument(Long documentId, byte[] fileBytes, String originalFilename, Long projectId, Long merchantId) {
        // 更新状态为处理中
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(documentId);
        doc.setVectorStatus(1);
        this.updateById(doc);

        File tempFile = null;
        try {
            // 创建临时文件并写入内容
            tempFile = File.createTempFile("knowledge_", "_" + originalFilename);
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(fileBytes);
            }

            // 处理文件
            int chunkCount = processFile(tempFile, documentId, projectId, merchantId);

            // 更新状态为完成
            doc.setVectorStatus(2);
            doc.setChunkCount(chunkCount);
            doc.setUpdatedAt(new Date());
            this.updateById(doc);

            log.info("文档向量化完成，documentId={}, chunkCount={}", documentId, chunkCount);
        } catch (Exception e) {
            log.error("文档向量化失败，documentId={}", documentId, e);
            doc.setVectorStatus(3); // 失败
            this.updateById(doc);
        } finally {
            // 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * 处理文件：解析、切分、向量化
     */
    private int processFile(File file, Long documentId, Long projectId, Long merchantId) {
        // 使用Tika读取文档
        TikaDocumentReader reader = new TikaDocumentReader(file.toURI().toString());
        List<Document> documents = reader.get();

        // 文本切分（每个切片约500个token，重叠100个token）
        TokenTextSplitter splitter = new TokenTextSplitter(500, 100, 5, 1000, true);
        List<Document> chunks = splitter.apply(documents);

        // 为每个切片添加元数据
        for (Document chunk : chunks) {
            chunk.getMetadata().put("documentId", documentId.toString());
            if (projectId != null) {
                chunk.getMetadata().put("projectId", projectId.toString());
            }
            if (merchantId != null) {
                chunk.getMetadata().put("merchantId", merchantId.toString());
            }
            chunk.getMetadata().put("source", "doc_" + documentId);
        }

        // 存储到向量数据库
        vectorStore.add(chunks);

        return chunks.size();
    }

    @Override
    public Page<KnowledgeDocument> listDocuments(KnowledgeQueryRequest request) {
        Page<KnowledgeDocument> page = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(request.getTitle())) {
            wrapper.like(KnowledgeDocument::getTitle, request.getTitle());
        }
        if (StrUtil.isNotBlank(request.getDocType())) {
            wrapper.eq(KnowledgeDocument::getDocType, request.getDocType());
        }
        if (request.getProjectId() != null) {
            wrapper.eq(KnowledgeDocument::getProjectId, request.getProjectId());
        }
        wrapper.orderByDesc(KnowledgeDocument::getCreatedAt);
        
        return this.page(page, wrapper);
    }

    @Override
    public boolean deleteDocument(Long documentId) {
        KnowledgeDocument document = this.getById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文档不存在");
        }

        // 删除向量数据
        try {
            vectorStore.delete(List.of("documentId == '" + documentId + "'"));
        } catch (Exception e) {
            log.warn("删除向量数据失败，documentId={}", documentId, e);
        }

        // 删除COS文件
        try {
            cosManager.deleteObject(document.getFilePath());
        } catch (Exception e) {
            log.warn("删除COS文件失败，filePath={}", document.getFilePath(), e);
        }

        // 逻辑删除文档记录
        return this.removeById(documentId);
    }

    @Override
    public boolean reVectorize(Long documentId) {
        KnowledgeDocument document = this.getById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文档不存在");
        }

        // 异步重新向量化
        CompletableFuture.runAsync(() -> {
            // 更新状态为处理中
            KnowledgeDocument doc = new KnowledgeDocument();
            doc.setId(documentId);
            doc.setVectorStatus(1);
            this.updateById(doc);

            File tempFile = null;
            try {
                // 先删除旧的向量数据
                vectorStore.delete(List.of("documentId == '" + documentId + "'"));

                // 从COS下载文件
                COSObject cosObject = cosManager.getObject(document.getFilePath());
                tempFile = File.createTempFile("revectorize_", "_" + document.getFileName());
                
                try (InputStream inputStream = cosObject.getObjectContent();
                     FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                // 重新处理文件
                int chunkCount = processFile(tempFile, documentId, document.getProjectId(), document.getMerchantId());

                // 更新状态为完成
                doc.setVectorStatus(2);
                doc.setChunkCount(chunkCount);
                doc.setUpdatedAt(new Date());
                this.updateById(doc);

                log.info("文档重新向量化完成，documentId={}, chunkCount={}", documentId, chunkCount);
            } catch (Exception e) {
                log.error("文档重新向量化失败，documentId={}", documentId, e);
                doc.setVectorStatus(3);
                this.updateById(doc);
            } finally {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        });

        return true;
    }

    @Override
    public Long updateDocument(Long documentId, MultipartFile file, KnowledgeUploadRequest request, Long userId) {
        KnowledgeDocument document = this.getById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文档不存在");
        }

        // 1. 校验文件
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }
        String extension = FileUtil.extName(originalFilename).toLowerCase();
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, 
                    "不支持的文件类型，支持：" + String.join(",", SUPPORTED_EXTENSIONS));
        }

        // 2. 删除旧的COS文件
        try {
            cosManager.deleteObject(document.getFilePath());
        } catch (Exception e) {
            log.warn("删除旧COS文件失败，filePath={}", document.getFilePath(), e);
        }

        // 3. 上传新文件到COS
        String newFilePath;
        try {
            newFilePath = cosManager.putObject(file, "knowledge/" + UUID.randomUUID() + "." + extension);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }

        // 4. 删除旧的向量数据
        try {
            vectorStore.delete(List.of("documentId == '" + documentId + "'"));
        } catch (Exception e) {
            log.warn("删除旧向量数据失败，documentId={}", documentId, e);
        }

        // 5. 更新文档记录
        document.setTitle(StrUtil.isNotBlank(request.getTitle()) ? request.getTitle() : originalFilename);
        document.setDocType(request.getDocType());
        document.setFileName(originalFilename);
        document.setFilePath(newFilePath);
        document.setFileSize(file.getSize());
        document.setProjectId(request.getProjectId());
        document.setMerchantId(request.getMerchantId());
        document.setTags(request.getTags());
        document.setVectorStatus(0); // 待处理
        document.setChunkCount(0);
        document.setUpdatedAt(new Date());
        this.updateById(document);

        // 6. 异步重新向量化
        final Long projectId = request.getProjectId();
        final Long merchantId = request.getMerchantId();
        
        // 在异步处理之前，先读取文件内容到内存
        final byte[] fileBytes;
        final String fileName = originalFilename;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            log.error("读取文件内容失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件内容失败");
        }
        
        CompletableFuture.runAsync(() -> {
            processDocument(documentId, fileBytes, fileName, projectId, merchantId);
        });

        return documentId;
    }

    @Override
    public String searchRelevantContent(String query, int topK) {
        if (StrUtil.isBlank(query)) {
            return "";
        }

        // 使用缓存
        String cacheKey = CacheConstants.buildKey(
            CacheConstants.KNOWLEDGE_SEARCH_PREFIX,
            query,
            topK
        );
        
        return cacheService.get(
            cacheKey,
            () -> {
                try {
                    SearchRequest searchRequest = SearchRequest.builder()
                            .query(query)
                            .topK(topK)
                            .build();
                    
                    List<Document> results = vectorStore.similaritySearch(searchRequest);
                    
                    if (results.isEmpty()) {
                        return "";
                    }

                    return results.stream()
                            .map(Document::getText)
                            .collect(Collectors.joining("\n\n---\n\n"));
                } catch (Exception e) {
                    log.error("向量检索失败", e);
                    return "";
                }
            },
            CacheConstants.KNOWLEDGE_SEARCH_TIMEOUT,
            CacheConstants.KNOWLEDGE_SEARCH_UNIT,
            String.class
        );
    }
}
