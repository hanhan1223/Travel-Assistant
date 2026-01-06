package org.example.travel.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * 向量存储配置
 * 
 * 使用自定义 PgVector 实现，适配 knowledge_chunk_vector 表结构
 * 使用 DashScope 的 text-embedding-v3 模型进行文本向量化（1024维）
 * 
 * 向量表结构：
 * - ich_project_vector: 非遗项目向量表
 * - knowledge_chunk_vector: 知识片段向量表（RAG检索）
 * - user_interest_vector: 用户兴趣向量表
 */
@Configuration
public class TravelVectorStoreConfig {

    @Value("${pgvector.datasource.url}")
    private String pgUrl;

    @Value("${pgvector.datasource.username}")
    private String pgUsername;

    @Value("${pgvector.datasource.password}")
    private String pgPassword;

    /**
     * PostgreSQL 数据源（用于 PgVector）
     */
    @Bean(name = "pgVectorDataSource")
    public DataSource pgVectorDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(pgUrl);
        dataSource.setUsername(pgUsername);
        dataSource.setPassword(pgPassword);
        return dataSource;
    }

    /**
     * PgVector JdbcTemplate
     */
    @Bean(name = "pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate() {
        return new JdbcTemplate(pgVectorDataSource());
    }

    /**
     * 自定义 PgVector 向量存储（用于知识库RAG检索）
     * 适配 knowledge_chunk_vector 表结构
     */
    @Bean(name = "simpleVectorStore")
    @Primary
    public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return new CustomPgVectorStore(pgVectorJdbcTemplate(), embeddingModel);
    }

    /**
     * 自定义向量存储（支持按项目/商户检索）
     */
    @Bean(name = "customVectorStore")
    public CustomPgVectorStore customVectorStore(EmbeddingModel embeddingModel) {
        return new CustomPgVectorStore(pgVectorJdbcTemplate(), embeddingModel);
    }
}
