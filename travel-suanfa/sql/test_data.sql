-- 推荐系统测试数据
-- 使用数据库：Travel

USE Travel;

-- ===========================
-- 1. 非遗项目测试数据
-- ===========================

-- 清空旧数据（可选）
-- TRUNCATE TABLE ich_project;

CREATE TABLE IF NOT EXISTS ich_project (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    city VARCHAR(100),
    lat DECIMAL(10, 6) NOT NULL,
    lng DECIMAL(10, 6) NOT NULL,
    is_indoor TINYINT DEFAULT 1 COMMENT '是否室内：1-室内，0-室外',
    open_status CHAR(1) DEFAULT '1' COMMENT '开放状态：1-开放，0-关闭',
    description TEXT,
    rating DECIMAL(3, 2) DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入成都附近的非遗项目（以你测试时的经纬度 30.6719, 104.0647 为中心）
INSERT INTO ich_project (name, category, city, lat, lng, is_indoor, open_status, description, rating) VALUES
('蜀绣体验中心', '传统美术', '成都', 30.6719, 104.0647, 1, '1', '四川著名传统刺绣，国家级非物质文化遗产', 4.8),
('成都皮影戏馆', '传统戏剧', '成都', 30.6789, 104.0712, 1, '1', '四川皮影戏传承基地，可观看表演和体验制作', 4.5),
('川剧变脸传习所', '传统戏剧', '成都', 30.6650, 104.0580, 1, '1', '川剧变脸艺术传承，提供表演和教学', 4.9),
('青城山茶艺馆', '传统技艺', '都江堰', 30.9000, 103.5670, 1, '1', '传统川茶制作技艺展示，室内茶文化体验', 4.6),
('都江堰竹编工坊', '传统技艺', '都江堰', 30.9980, 103.6470, 0, '1', '传统竹编技艺展示与体验，户外工坊', 4.4),
('陶瓷艺术工作室', '传统美术', '成都', 30.6800, 104.0700, 1, '1', '传统陶瓷制作技艺，可现场体验拉坯', 4.7),
('锦里古街', '传统技艺', '成都', 30.6500, 104.0450, 0, '1', '成都传统文化街区，集多种非遗技艺于一体', 4.8),
('漆器制作坊', '传统技艺', '成都', 30.6820, 104.0820, 1, '1', '成都漆器制作技艺传承基地', 4.3),
('剪纸艺术馆', '传统美术', '成都', 30.6600, 104.0600, 1, '1', '四川剪纸艺术展览与体验', 4.5),
('糖画传承馆', '传统技艺', '成都', 30.6700, 104.0650, 0, '1', '传统糖画技艺展示，户外展位', 4.2);

-- ===========================
-- 2. 商户测试数据
-- ===========================

-- 清空旧数据（可选）
-- TRUNCATE TABLE merchant;

CREATE TABLE IF NOT EXISTS merchant (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    project_id INT COMMENT '关联的非遗项目ID',
    lat DECIMAL(10, 6) NOT NULL,
    lng DECIMAL(10, 6) NOT NULL,
    rating DECIMAL(3, 2) DEFAULT 0.0,
    relevance_score DECIMAL(5, 4) DEFAULT 0.0 COMMENT '与非遗项目的相关度',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES ich_project(id) ON DELETE SET NULL
);

-- 插入商户数据（关联非遗项目）
INSERT INTO merchant (name, category, project_id, lat, lng, rating, relevance_score, description) VALUES
('锦绣坊刺绣店', '非遗体验店', 1, 30.6720, 104.0650, 4.7, 0.95, '专业蜀绣产品销售与体验'),
('川韵皮影工艺品店', '非遗体验店', 2, 30.6790, 104.0715, 4.4, 0.90, '皮影戏道具与纪念品'),
('变脸坊文创店', '非遗体验店', 3, 30.6655, 104.0585, 4.8, 0.92, '川剧变脸主题文创产品'),
('青城茶舍', '茶馆', 4, 30.9010, 103.5680, 4.6, 0.88, '传统川茶品鉴与销售'),
('竹编工艺店', '非遗体验店', 5, 30.9985, 103.6475, 4.3, 0.85, '手工竹编制品销售'),
('陶艺生活馆', '非遗体验店', 6, 30.6805, 104.0705, 4.5, 0.90, '陶瓷艺术品与生活用品'),
('锦里特色小吃街', '美食街', 7, 30.6505, 104.0455, 4.7, 0.80, '成都传统特色小吃集市'),
('漆器艺术店', '非遗体验店', 8, 30.6825, 104.0825, 4.2, 0.87, '成都漆器艺术品销售'),
('剪纸文创店', '非遗体验店', 9, 30.6605, 104.0605, 4.4, 0.88, '四川剪纸艺术作品与教学'),
('糖画小铺', '非遗体验店', 10, 30.6705, 104.0655, 4.1, 0.82, '现场制作传统糖画');

-- ===========================
-- 3. 用户行为历史表（可选，用于历史行为推荐）
-- ===========================

CREATE TABLE IF NOT EXISTS user_behavior (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    item_type ENUM('project', 'merchant') NOT NULL,
    behavior_type ENUM('view', 'click', 'favorite', 'visit') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_item (item_id, item_type)
);

-- 插入测试用户的历史行为（用户ID 10001）
INSERT INTO user_behavior (user_id, item_id, item_type, behavior_type) VALUES
(10001, 1, 'project', 'view'),
(10001, 1, 'project', 'click'),
(10001, 3, 'project', 'view'),
(10001, 6, 'project', 'favorite'),
(10001, 1, 'merchant', 'click'),
(10001, 3, 'merchant', 'view');

-- ===========================
-- 查询验证
-- ===========================

-- 查看非遗项目数据
SELECT id, name, category, city, lat, lng, is_indoor, open_status, rating 
FROM ich_project 
LIMIT 10;

-- 查看商户数据
SELECT id, name, category, project_id, lat, lng, rating, relevance_score 
FROM merchant 
LIMIT 10;

-- 查看用户行为数据
SELECT * FROM user_behavior WHERE user_id = 10001;

-- ===========================
-- 完成提示
-- ===========================
SELECT '测试数据插入完成！现在可以测试推荐接口了。' AS message;
