package org.example.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.travel.model.dto.knowledge.KnowledgeQueryRequest;
import org.example.travel.model.dto.knowledge.KnowledgeUploadRequest;
import org.example.travel.model.entity.KnowledgeDocument;
import org.springframework.web.multipart.MultipartFile;

/**
 * 知识库服务接口
 */
public interface KnowledgeService extends IService<KnowledgeDocument> {
    
    /**
     * 上传文档并进行向量化处理
     * @param file 上传的文件
     * @param request 上传请求参数
     * @param userId 用户ID
     * @return 文档ID
     */
    Long uploadDocument(MultipartFile file, KnowledgeUploadRequest request, Long userId);
    
    /**
     * 分页查询知识库文档
     * @param request 查询请求
     * @return 分页结果
     */
    Page<KnowledgeDocument> listDocuments(KnowledgeQueryRequest request);
    
    /**
     * 删除文档（同时删除向量数据）
     * @param documentId 文档ID
     * @return 是否成功
     */
    boolean deleteDocument(Long documentId);
    
    /**
     * 更新文档（替换文件并重新向量化）
     * @param documentId 文档ID
     * @param file 新文件
     * @param request 更新请求
     * @param userId 用户ID
     * @return 是否成功
     */
    Long updateDocument(Long documentId, MultipartFile file, KnowledgeUploadRequest request, Long userId);
    
    /**
     * 重新向量化文档
     * @param documentId 文档ID
     * @return 是否成功
     */
    boolean reVectorize(Long documentId);
    
    /**
     * 根据查询文本检索相关文档片段
     * @param query 查询文本
     * @param topK 返回数量
     * @return 相关文档内容
     */
    String searchRelevantContent(String query, int topK);
}
