-- ============================================
-- 数据库性能优化 SQL
-- ============================================

-- 1. 为地理位置查询添加空间索引（MySQL）
-- ============================================
USE Travel;

-- 检查是否已有索引
SHOW INDEX FROM ich_project WHERE Key_name = 'idx_location';
SHOW INDEX FROM merchant WHERE Key_name = 'idx_location';

-- 为非遗项目表添加地理位置索引
ALTER TABLE ich_project 
ADD INDEX idx_location (lat, lng);

-- 为商户表添加地理位置索引
ALTER TABLE merchant 
ADD INDEX idx_location (lat, lng);

-- 为开放状态添加索引
ALTER TABLE ich_project 
ADD INDEX idx_open_status (open_status);

-- 2. 为用户行为日志添加索引
-- ============================================
ALTER TABLE user_behavior_log 
ADD INDEX idx_user_target (user_id, target_type, target_id);

ALTER TABLE user_behavior_log 
ADD INDEX idx_user_created (user_id, created_at);

-- 3. 为聊天消息添加索引
-- ============================================
ALTER TABLE chat_message 
ADD INDEX idx_conversation_created (conversation_id, created_at);

-- 4. PostgreSQL 向量数据库优化
-- ============================================
-- 连接到 PostgreSQL
\c Travel

-- 为向量表添加 HNSW 索引（加速相似度搜索）
CREATE INDEX IF NOT EXISTS idx_project_vector_hnsw 
ON ich_project_vector 
USING hnsw (content_vector vector_cosine_ops);

CREATE INDEX IF NOT EXISTS idx_knowledge_vector_hnsw 
ON knowledge_chunk_vector 
USING hnsw (content_vector vector_cosine_ops);

CREATE INDEX IF NOT EXISTS idx_user_interest_vector_hnsw 
ON user_interest_vector 
USING hnsw (interest_vector vector_cosine_ops);

-- 为关联字段添加索引
CREATE INDEX IF NOT EXISTS idx_project_vector_project_id 
ON ich_project_vector (project_id);

CREATE INDEX IF NOT EXISTS idx_knowledge_vector_project_id 
ON knowledge_chunk_vector (project_id);

CREATE INDEX IF NOT EXISTS idx_knowledge_vector_merchant_id 
ON knowledge_chunk_vector (merchant_id);

-- 5. 查询性能分析（执行后查看慢查询）
-- ============================================
-- MySQL 慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1; -- 记录超过1秒的查询

-- 查看当前慢查询
SHOW VARIABLES LIKE 'slow_query%';

-- 6. 连接池优化建议
-- ============================================
-- 在 application.yml 中添加以下配置：
/*
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
*/

-- 7. 查询优化示例
-- ============================================
-- 优化前（全表扫描）
-- SELECT * FROM ich_project WHERE lat BETWEEN 23.0 AND 23.5 AND lng BETWEEN 113.0 AND 113.5;

-- 优化后（使用索引 + 限制返回字段）
-- SELECT id, name, category, city, lat, lng, is_indoor, open_status 
-- FROM ich_project 
-- WHERE lat BETWEEN 23.0 AND 23.5 
--   AND lng BETWEEN 113.0 AND 113.5 
--   AND open_status = '1'
-- LIMIT 100;

-- 8. 定期维护
-- ============================================
-- 分析表（更新统计信息）
ANALYZE TABLE ich_project;
ANALYZE TABLE merchant;
ANALYZE TABLE user_behavior_log;

-- 优化表（整理碎片）
OPTIMIZE TABLE ich_project;
OPTIMIZE TABLE merchant;

-- PostgreSQL 维护
VACUUM ANALYZE ich_project_vector;
VACUUM ANALYZE knowledge_chunk_vector;
VACUUM ANALYZE user_interest_vector;
