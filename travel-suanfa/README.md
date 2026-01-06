<<<<<<< HEAD
# 非遗文化智能伴游系统 - Python 算法推荐服务

基于 FastAPI 框架构建的推荐引擎，为用户提供个性化的非遗项目和商户推荐。

## 功能特性

- 基于地理位置的推荐
- 多因素权重融合（距离、兴趣、历史、天气、评分）
- 向量相似度搜索（基于 pgvector）
- 天气适配策略
- 用户行为分析
- RESTful API 接口

## 系统架构

```
python_tuijian/
├── app/                    # 应用主目录
│   ├── models/            # 数据模型
│   │   ├── database.py    # 数据库连接
│   │   └── schemas.py     # Pydantic 模型
│   ├── services/          # 业务逻辑
│   │   ├── embedding_service.py   # 向量化服务
│   │   └── recommend_service.py    # 推荐算法
│   ├── routers/           # API 路由
│   │   └── recommend.py    # 推荐接口
│   ├── utils/             # 工具函数
│   └── main.py            # 应用入口
├── config/                # 配置文件
│   └── settings.py        # 配置定义
├── tests/                 # 测试代码
├── requirements.txt        # 依赖包
└── README.md             # 项目说明
```

## 推荐算法

### 推荐因素权重

| 因素 | 权重 | 说明 |
|------|------|------|
| 距离 | 30% | 越近分数越高，使用 Haversine 公式计算 |
| 兴趣匹配 | 25% | 用户兴趣标签与项目类别匹配 |
| 历史行为 | 20% | 基于用户点击、浏览历史 |
| 天气适配 | 15% | 雨天推荐室内项目 |
| 热度/评分 | 10% | 商户评分、项目热度 |

### 天气适配策略

| 天气 | 策略 |
|------|------|
| 晴/多云 | 户外项目权重 +10% |
| 阴 | 正常推荐 |
| 小雨 | 室内项目权重 +20%，户外项目权重 -20% |
| 大雨/暴雨 | 仅推荐室内项目 |

## API 接口

### 1. 推荐接口

**请求：** `POST /api/recommend`

**请求参数：**

```json
{
  "userId": 10001,
  "lat": 30.6719,
  "lng": 104.0647,
  "interestTags": ["刺绣", "陶瓷"],
  "weather": "晴",
  "outdoorSuitable": true,
  "intentVector": [0.1, 0.2, ...],  // 可选，1024维向量
  "limit": 10
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "type": "project",
      "name": "蜀绣体验中心",
      "category": "刺绣",
      "lat": 30.6725,
      "lng": 104.0650,
      "distance": 0.8,
      "rating": null,
      "score": 0.85,
      "reasons": ["距离您仅 0.8 公里", "符合您的兴趣：刺绣"]
    }
  ],
  "total": 1
}
```

### 2. 文本向量化接口

**请求：** `POST /api/embedding`

**请求参数：**

```json
{
  "text": "我想体验传统刺绣文化活动",
  "model": "default"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "vector": [0.1, 0.2, ...],  // 1024维向量
    "dimension": 1024,
    "model": "default"
  }
}
```

## 数据库配置

### MySQL 数据库

```python
MYSQL_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "database": "Travel",
    "user": "root",
    "password": "your_password"
}
```

**主要表：**
- `ich_project` - 非遗项目信息
- `merchant` - 商户信息
- `user_behavior_log` - 用户行为日志

### PostgreSQL (pgvector) 向量数据库

```python
PG_CONFIG = {
    "host": "localhost",
    "port": 5433,
    "database": "Travel",
    "user": "admin",
    "password": "your_password"
}
```

**主要表：**
- `ich_project_vector` - 非遗项目向量表（1024维）
- `knowledge_chunk_vector` - 知识片段向量表
- `user_interest_vector` - 用户兴趣向量表

## 快速开始

### 1. 安装依赖

```bash
pip install -r requirements.txt
```

### 2. 配置环境变量（可选）

创建 `.env` 文件：

```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=Travel
MYSQL_USER=root
MYSQL_PASSWORD=your_password

PG_HOST=localhost
PG_PORT=5433
PG_DATABASE=Travel
PG_USER=admin
PG_PASSWORD=your_password
```

### 3. 启动服务

**Windows:**

```cmd
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

**Linux/Mac:**

```bash
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 4. 访问 API 文档

服务启动后，访问：http://localhost:8000/docs

## 测试

### 测试推荐接口

```bash
curl -X POST "http://localhost:8000/api/recommend" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 10001,
    "lat": 30.6719,
    "lng": 104.0647,
    "interestTags": ["刺绣", "陶瓷"],
    "weather": "晴",
    "limit": 10
  }'
```

### 测试向量化接口

```bash
curl -X POST "http://localhost:8000/api/embedding" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "我想体验传统刺绣文化活动"
  }'
```

## 技术栈

- **Web 框架**: FastAPI 0.104.1
- **数据库**: MySQL, PostgreSQL (pgvector)
- **ORM**: SQLAlchemy 2.0.23
- **向量计算**: NumPy, SciPy
- **HTTP 客户端**: httpx
- **ASGI 服务器**: Uvicorn
- **向量化服务**: 后端 Spring Boot 服务提供

## 向量化服务说明

### 外部服务调用
系统通过 HTTP POST 请求调用后端向量化服务生成文本向量：

**向量化接口**: `http://localhost:8080/api/embeding`

**请求格式**:
```json
{
  "text": "我想了解广绣的历史"
}
```

**响应格式**:
```json
{
  "embedding": [0.123, -0.456, 0.789, ...]  // 1024维向量
}
```

### 容错机制
- **超时处理**: 30秒超时自动失败
- **备用方案**: 当外部服务不可用时，自动使用本地哈希算法生成向量
- **错误日志**: 记录所有外部服务调用失败的情况

## 注意事项

1. **向量化服务**: 当前使用简化实现生成向量，生产环境需要替换为真实的嵌入模型（如 sentence-transformers、OpenAI Embeddings）

2. **数据库连接**: 确保 MySQL 和 PostgreSQL 服务可访问，且已创建相应的表结构

3. **pgvector 扩展**: PostgreSQL 数据库需要安装 pgvector 扩展

4. **性能优化**: 可根据实际需求调整数据库连接池、缓存等参数

## 许可证

MIT License
