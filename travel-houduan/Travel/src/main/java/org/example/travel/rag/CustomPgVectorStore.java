package org.example.travel.rag;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import org.springframework.ai.vectorstore.filter.Filter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义 PgVector 存储实现
 * 适配 knowledge_chunk_vector 表结构
 */
@Slf4j
public class CustomPgVectorStore implements VectorStore {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;

    private static final String INSERT_SQL = """
            INSERT INTO knowledge_chunk_vector (content, content_vector, project_id, merchant_id, source, created_at)
            VALUES (?, ?::vector, ?, ?, ?, CURRENT_TIMESTAMP)
            """;

    private static final String SEARCH_SQL = """
            SELECT id, content, project_id, merchant_id, source, created_at,
                   1 - (content_vector <=> ?::vector) as similarity
            FROM knowledge_chunk_vector
            ORDER BY content_vector <=> ?::vector
            LIMIT ?
            """;

    private static final String DELETE_BY_IDS_SQL = """
            DELETE FROM knowledge_chunk_vector WHERE id IN (%s)
            """;

    private static final String DELETE_BY_FILTER_SQL = """
            DELETE FROM knowledge_chunk_vector WHERE %s
            """;

    public CustomPgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void add(List<Document> documents) {
        for (Document document : documents) {
            // 生成向量
            float[] embedding = embeddingModel.embed(document.getText());
            String vectorStr = toVectorString(embedding);

            // 从元数据获取关联信息
            Map<String, Object> metadata = document.getMetadata();
            Long projectId = null;
            Long merchantId = null;
            String source = "knowledge_base";
            
            if (metadata.get("projectId") != null) {
                try {
                    projectId = Long.parseLong(metadata.get("projectId").toString());
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            if (metadata.get("merchantId") != null) {
                try {
                    merchantId = Long.parseLong(metadata.get("merchantId").toString());
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            if (metadata.get("source") != null) {
                source = metadata.get("source").toString();
            }

            jdbcTemplate.update(INSERT_SQL, 
                    document.getText(), 
                    vectorStr, 
                    projectId, 
                    merchantId, 
                    source);
        }
        log.info("Added {} documents to vector store", documents.size());
    }

    @Override
    public void delete(List<String> idList) {
        if (idList == null || idList.isEmpty()) {
            return;
        }

        try {
            // 检查是否是过滤条件（如 "documentId == '123'"）
            if (idList.size() == 1 && idList.get(0).contains("==")) {
                String filter = idList.get(0);
                // 解析简单的过滤条件
                if (filter.contains("documentId")) {
                    String docId = filter.replaceAll(".*'([^']*)'.*", "$1");
                    // 通过 source 字段删除（source 格式为 "doc_123"）
                    int deleted = jdbcTemplate.update(
                            "DELETE FROM knowledge_chunk_vector WHERE source = ?", 
                            "doc_" + docId);
                    log.info("Deleted {} chunks for documentId={}", deleted, docId);
                    return;
                }
            }

            // 按ID删除（数字ID）
            for (String idStr : idList) {
                try {
                    Long id = Long.parseLong(idStr);
                    jdbcTemplate.update("DELETE FROM knowledge_chunk_vector WHERE id = ?", id);
                } catch (NumberFormatException e) {
                    log.warn("Invalid id format: {}", idStr);
                }
            }
        } catch (Exception e) {
            log.error("Failed to delete documents", e);
        }
    }

    @Override
    public void delete(Filter.Expression filterExpression) {
        // 简单实现：暂不支持复杂过滤表达式删除
        log.warn("Filter expression delete not fully supported, expression: {}", filterExpression);
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        // 生成查询向量
        float[] queryEmbedding = embeddingModel.embed(request.getQuery());
        String vectorStr = toVectorString(queryEmbedding);

        int topK = request.getTopK() > 0 ? request.getTopK() : 5;

        return jdbcTemplate.query(SEARCH_SQL, new DocumentRowMapper(), vectorStr, vectorStr, topK);
    }

    /**
     * 根据非遗项目ID检索相关知识
     */
    public List<Document> searchByProjectId(String query, Long projectId, int topK) {
        float[] queryEmbedding = embeddingModel.embed(query);
        String vectorStr = toVectorString(queryEmbedding);

        String sql = """
                SELECT id, content, project_id, merchant_id, source, created_at,
                       1 - (content_vector <=> ?::vector) as similarity
                FROM knowledge_chunk_vector
                WHERE project_id = ?
                ORDER BY content_vector <=> ?::vector
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, new DocumentRowMapper(), vectorStr, projectId, vectorStr, topK);
    }

    /**
     * 根据商户ID检索相关知识
     */
    public List<Document> searchByMerchantId(String query, Long merchantId, int topK) {
        float[] queryEmbedding = embeddingModel.embed(query);
        String vectorStr = toVectorString(queryEmbedding);

        String sql = """
                SELECT id, content, project_id, merchant_id, source, created_at,
                       1 - (content_vector <=> ?::vector) as similarity
                FROM knowledge_chunk_vector
                WHERE merchant_id = ?
                ORDER BY content_vector <=> ?::vector
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, new DocumentRowMapper(), vectorStr, merchantId, vectorStr, topK);
    }

    /**
     * 将float数组转换为PostgreSQL向量字符串
     */
    private String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Document行映射器
     */
    private static class DocumentRowMapper implements RowMapper<Document> {
        @Override
        public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
            String content = rs.getString("content");
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("id", rs.getLong("id"));
            
            // 避免 null 值
            Object projectId = rs.getObject("project_id");
            if (projectId != null) {
                metadata.put("projectId", projectId);
            }
            
            Object merchantId = rs.getObject("merchant_id");
            if (merchantId != null) {
                metadata.put("merchantId", merchantId);
            }
            
            String source = rs.getString("source");
            if (source != null) {
                metadata.put("source", source);
            }
            
            metadata.put("similarity", rs.getDouble("similarity"));
            
            return new Document(String.valueOf(rs.getLong("id")), content, metadata);
        }
    }

    // ==================== ich_project_vector 表操作 ====================

    private static final String INSERT_PROJECT_VECTOR_SQL = """
            INSERT INTO ich_project_vector (project_id, content_vector, model, updated_at)
            VALUES (?, ?::vector, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (project_id) DO UPDATE SET
                content_vector = EXCLUDED.content_vector,
                model = EXCLUDED.model,
                updated_at = CURRENT_TIMESTAMP
            """;

    private static final String DELETE_PROJECT_VECTOR_SQL = """
            DELETE FROM ich_project_vector WHERE project_id = ?
            """;

    private static final String SEARCH_PROJECT_VECTOR_SQL = """
            SELECT project_id, 1 - (content_vector <=> ?::vector) as similarity
            FROM ich_project_vector
            ORDER BY content_vector <=> ?::vector
            LIMIT ?
            """;

    /**
     * 保存或更新非遗项目向量
     * @param projectId 项目ID
     * @param content 用于生成向量的文本内容（名称+类别+描述+城市）
     */
    public void saveProjectVector(Long projectId, String content) {
        if (projectId == null || content == null || content.trim().isEmpty()) {
            log.warn("Invalid params for saveProjectVector: projectId={}, content={}", projectId, content);
            return;
        }
        
        try {
            float[] embedding = embeddingModel.embed(content);
            String vectorStr = toVectorString(embedding);
            String modelName = embeddingModel.getClass().getSimpleName();
            
            jdbcTemplate.update(INSERT_PROJECT_VECTOR_SQL, projectId, vectorStr, modelName);
            log.info("Saved project vector for projectId={}", projectId);
        } catch (Exception e) {
            log.error("Failed to save project vector for projectId={}", projectId, e);
        }
    }

    /**
     * 删除非遗项目向量
     */
    public void deleteProjectVector(Long projectId) {
        if (projectId == null) {
            return;
        }
        try {
            int deleted = jdbcTemplate.update(DELETE_PROJECT_VECTOR_SQL, projectId);
            log.info("Deleted project vector for projectId={}, count={}", projectId, deleted);
        } catch (Exception e) {
            log.error("Failed to delete project vector for projectId={}", projectId, e);
        }
    }

    /**
     * 根据查询文本搜索相似的非遗项目
     * @return 项目ID列表（按相似度排序）
     */
    public List<Long> searchSimilarProjects(String query, int topK) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            float[] queryEmbedding = embeddingModel.embed(query);
            String vectorStr = toVectorString(queryEmbedding);
            
            return jdbcTemplate.query(SEARCH_PROJECT_VECTOR_SQL, (rs, rowNum) -> rs.getLong("project_id"), 
                    vectorStr, vectorStr, topK);
        } catch (Exception e) {
            log.error("Failed to search similar projects", e);
            return List.of();
        }
    }

    // ==================== user_interest_vector 表操作 ====================

    private static final String UPSERT_USER_INTEREST_SQL = """
            INSERT INTO user_interest_vector (user_id, interest_vector, updated_at)
            VALUES (?, ?::vector, CURRENT_TIMESTAMP)
            ON CONFLICT (user_id) DO UPDATE SET
                interest_vector = EXCLUDED.interest_vector,
                updated_at = CURRENT_TIMESTAMP
            """;

    /**
     * 保存或更新用户兴趣向量
     */
    public void saveUserInterestVector(Long userId, String interestText) {
        if (userId == null || interestText == null || interestText.trim().isEmpty()) {
            return;
        }
        
        try {
            float[] embedding = embeddingModel.embed(interestText);
            String vectorStr = toVectorString(embedding);
            
            jdbcTemplate.update(UPSERT_USER_INTEREST_SQL, userId, vectorStr);
            log.info("Saved user interest vector for userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to save user interest vector for userId={}", userId, e);
        }
    }
}
