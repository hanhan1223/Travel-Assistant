-- =============================================
-- 步骤1：启用 pgvector 扩展（向量存储必备）
-- =============================================
-- 检查并启用 pgvector 扩展，若已存在则不报错
CREATE EXTENSION IF NOT EXISTS vector;

-- =============================================
-- 步骤2：非遗项目向量表（存储非遗项目语义向量）
-- =============================================
CREATE TABLE ich_project_vector (
    project_id BIGINT PRIMARY KEY,  -- 非遗项目ID（对应 MySQL）
    content_vector VECTOR(1024),    -- 非遗描述向量
    model VARCHAR(100),             -- 向量模型名称
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间
);

-- 为 ich_project_vector 表和列添加注释（PostgreSQL 标准语法）
COMMENT ON TABLE ich_project_vector IS '非遗项目向量表：存储非遗项目的语义向量';
COMMENT ON COLUMN ich_project_vector.project_id IS '非遗项目ID（对应 MySQL）';
COMMENT ON COLUMN ich_project_vector.content_vector IS '非遗描述向量';
COMMENT ON COLUMN ich_project_vector.model IS '向量模型名称';
COMMENT ON COLUMN ich_project_vector.updated_at IS '更新时间';

-- =============================================
-- 步骤3：知识片段向量表（用于 RAG 检索）
-- 可存储非遗文档、商户商品说明等切分文本
-- =============================================
CREATE TABLE knowledge_chunk_vector (
    id BIGSERIAL PRIMARY KEY,       -- 知识片段ID
    project_id BIGINT,              -- 关联非遗项目ID
    merchant_id BIGINT,             -- 关联商户ID（可为空）
    content TEXT,                   -- 切分后的文本内容
    content_vector VECTOR(1024),    -- 文本向量
    source VARCHAR(100),            -- 来源（ich_doc / merchant_doc）
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 创建时间
);

-- 为 knowledge_chunk_vector 表和列添加注释
COMMENT ON TABLE knowledge_chunk_vector IS '知识片段向量表：用于 RAG 检索';
COMMENT ON COLUMN knowledge_chunk_vector.id IS '知识片段ID';
COMMENT ON COLUMN knowledge_chunk_vector.project_id IS '关联非遗项目ID';
COMMENT ON COLUMN knowledge_chunk_vector.merchant_id IS '关联商户ID（可为空）';
COMMENT ON COLUMN knowledge_chunk_vector.content IS '切分后的文本内容';
COMMENT ON COLUMN knowledge_chunk_vector.content_vector IS '文本向量';
COMMENT ON COLUMN knowledge_chunk_vector.source IS '来源（ich_doc / merchant_doc）';
COMMENT ON COLUMN knowledge_chunk_vector.created_at IS '创建时间';

-- =============================================
-- 步骤4：用户兴趣向量表（存储用户画像向量，可选）
-- =============================================
CREATE TABLE user_interest_vector (
    user_id BIGINT PRIMARY KEY,     -- 用户ID（对应 MySQL）
    interest_vector VECTOR(1024),   -- 用户兴趣向量
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间
);

-- 为 user_interest_vector 表和列添加注释
COMMENT ON TABLE user_interest_vector IS '用户兴趣向量表：存储用户画像向量';
COMMENT ON COLUMN user_interest_vector.user_id IS '用户ID（对应 MySQL）';
COMMENT ON COLUMN user_interest_vector.interest_vector IS '用户兴趣向量';
COMMENT ON COLUMN user_interest_vector.updated_at IS '更新时间';

-- =============================================
-- 步骤5：向量索引（性能关键，基于 ivfflat 算法）
-- 注：ivfflat 适合中小规模向量数据，大规模可考虑 hnsw 索引
-- =============================================
-- 非遗项目向量索引（余弦相似度匹配）
CREATE INDEX idx_ich_project_vector ON ich_project_vector
USING ivfflat (content_vector vector_cosine_ops);

-- 知识片段向量索引（余弦相似度匹配）
CREATE INDEX idx_knowledge_chunk_vector ON knowledge_chunk_vector
USING ivfflat (content_vector vector_cosine_ops);

-- 可选：为用户兴趣向量添加索引（若有检索需求）
CREATE INDEX idx_user_interest_vector ON user_interest_vector
USING ivfflat (interest_vector vector_cosine_ops);