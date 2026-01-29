-- ============================================
-- 强化学习推荐模块 - MySQL 表结构
-- 数据库: Travel
-- ============================================

-- 1. 推荐交互记录表
-- 用途：记录每次推荐的完整信息（状态、动作、奖励）
CREATE TABLE IF NOT EXISTS rl_recommend_interaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID（一次推荐请求的唯一标识）',
    
    -- 状态信息（JSON格式存储）
    state_features JSON COMMENT '状态特征向量（用户特征、上下文等）',
    state_summary TEXT COMMENT '状态摘要（便于查看）',
    
    -- 动作信息
    action_type VARCHAR(32) NOT NULL COMMENT '动作类型（weight_adjustment/strategy_select等）',
    action_params JSON COMMENT '动作参数（权重组合或策略ID）',
    
    -- 推荐结果
    recommended_items JSON COMMENT '推荐的物品列表（项目ID、商户ID等）',
    recommended_count INT DEFAULT 0 COMMENT '推荐数量',
    
    -- 奖励信息
    immediate_reward DECIMAL(10, 4) DEFAULT 0 COMMENT '即时奖励（点击、浏览等）',
    delayed_reward DECIMAL(10, 4) DEFAULT 0 COMMENT '延迟奖励（访问、转化等）',
    total_reward DECIMAL(10, 4) DEFAULT 0 COMMENT '总奖励',
    reward_details JSON COMMENT '奖励详情（各维度奖励值）',
    
    -- 元数据
    request_context JSON COMMENT '请求上下文（位置、天气、时间等）',
    model_version VARCHAR(32) COMMENT '使用的模型版本',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at),
    INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='强化学习推荐交互记录表';

-- 2. 用户反馈记录表
-- 用途：记录用户对推荐结果的反馈行为
CREATE TABLE IF NOT EXISTS rl_user_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    session_id VARCHAR(64) NOT NULL COMMENT '关联的推荐会话ID',
    user_id INT NOT NULL COMMENT '用户ID',
    
    -- 反馈信息
    item_id INT NOT NULL COMMENT '物品ID（项目或商户）',
    item_type VARCHAR(16) NOT NULL COMMENT '物品类型（project/merchant）',
    feedback_type VARCHAR(32) NOT NULL COMMENT '反馈类型（click/view/dismiss/favorite/share/visit/purchase）',
    feedback_value DECIMAL(10, 4) DEFAULT 0 COMMENT '反馈值（如浏览时长、评分等）',
    
    -- 时间信息
    feedback_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '反馈时间',
    time_since_recommend INT COMMENT '距离推荐的时间（秒）',
    
    -- 额外信息
    extra_data JSON COMMENT '额外数据（如停留页面、跳转路径等）',
    
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_item (item_id, item_type),
    INDEX idx_feedback_time (feedback_time),
    INDEX idx_feedback_type (feedback_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈记录表';

-- 3. 用户状态表
-- 用途：存储用户当前状态（替代Redis的用户状态）
CREATE TABLE IF NOT EXISTS rl_user_state (
    user_id INT PRIMARY KEY COMMENT '用户ID',
    
    -- 用户特征（JSON格式）
    user_features JSON COMMENT '用户特征（平均浏览时长、点击率、转化率等）',
    
    -- 最近上下文（JSON格式）
    recent_context JSON COMMENT '最近一次请求的上下文（位置、天气、时间等）',
    
    -- 统计信息
    total_clicks INT DEFAULT 0 COMMENT '总点击次数',
    total_views INT DEFAULT 0 COMMENT '总浏览次数',
    total_conversions INT DEFAULT 0 COMMENT '总转化次数',
    avg_reward DECIMAL(10, 4) DEFAULT 0 COMMENT '平均奖励',
    
    -- 时间戳
    last_recommend_time DATETIME COMMENT '最后一次推荐时间',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    
    INDEX idx_last_update (last_update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户状态表';

-- 4. 模型参数表
-- 用途：存储模型参数（替代Redis的模型参数）
CREATE TABLE IF NOT EXISTS rl_model_params (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    model_type VARCHAR(32) NOT NULL COMMENT '模型类型（linucb/dqn等）',
    model_key VARCHAR(128) NOT NULL COMMENT '模型标识（user:10001 或 global）',
    
    -- 模型参数（JSON格式）
    params_json JSON NOT NULL COMMENT '模型参数（A矩阵、b向量、alpha等）',
    
    -- 元数据
    feature_dim INT COMMENT '特征维度',
    version INT DEFAULT 1 COMMENT '版本号（每次更新+1）',
    
    -- 状态
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否激活（1=是，0=否）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_model_key_active (model_key, is_active),
    INDEX idx_model_type (model_type),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型参数表';

-- 5. 模型训练历史表
-- 用途：记录模型训练的历史记录
CREATE TABLE IF NOT EXISTS rl_model_training_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    model_type VARCHAR(32) NOT NULL COMMENT '模型类型（linucb/dqn等）',
    model_key VARCHAR(128) NOT NULL COMMENT '模型标识（user:10001 或 global）',
    
    -- 训练信息
    training_date DATE NOT NULL COMMENT '训练日期',
    training_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '训练时间',
    training_samples INT DEFAULT 0 COMMENT '训练样本数',
    
    -- 性能指标
    performance_metrics JSON COMMENT '性能指标（CTR、CVR、累计奖励等）',
    model_params_summary TEXT COMMENT '模型参数摘要',
    
    -- 状态
    status VARCHAR(16) DEFAULT 'success' COMMENT '状态（success/failed/running）',
    error_message TEXT COMMENT '错误信息',
    
    INDEX idx_model_key (model_key),
    INDEX idx_training_date (training_date),
    INDEX idx_model_type (model_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型训练历史表';


