# 非遗文化智能伴游系统

基于 AI 的智能旅游推荐系统，专注于非物质文化遗产的推广与体验。系统采用前后端分离架构，包含用户端、管理端、后端服务和智能推荐算法四个核心模块。

## 项目架构

```
├── Intelligent_travel_system/          # 用户端前端 (Vue3 + Vant + TailwindCSS)
├── Intelligent_travel_ManageSystem/    # 管理端前端 (Vue3 + Element Plus)
├── travel-houduan/                     # 后端服务 (Spring Boot 3.4 + Spring AI)
├── travel-suanfa/                      # 推荐算法服务 (FastAPI + Python)
└── Travel.sql                          # 数据库初始化脚本
```

## 运行环境

| 模块 | 环境要求 |
|------|----------|
| 用户端前端 | Node.js 18+, npm 9+ |
| 管理端前端 | Node.js 18+, npm 9+ |
| 后端服务 | JDK 17+, Maven 3.8+ |
| 推荐算法 | Python 3.10+ |
| 数据库 | MySQL 8.0+, Redis 7.0+, PostgreSQL 15+ (pgvector 扩展) |

## 依赖库及安装命令

### 1. 用户端前端

```bash
cd Intelligent_travel_system/Intelligent_travel_system
npm install
```

主要依赖：Vue 3.5、Vant 4、Pinia、Vue Router 4、高德地图 SDK、TailwindCSS

### 2. 管理端前端

```bash
cd Intelligent_travel_ManageSystem/intelligent_travel_ManageSystem
npm install
```

主要依赖：Vue 3.5、Element Plus、ECharts 6、Pinia、Vue Router 4、Three.js

### 3. 后端服务

```bash
cd travel-houduan/Travel
mvn clean install -DskipTests
```

主要依赖：Spring Boot 3.4、Spring AI 1.0、MyBatis-Plus、Sa-Token、Redis、阿里云 DashScope SDK

### 4. 推荐算法服务

```bash
cd travel-suanfa
pip install -r requirements.txt
```

主要依赖：FastAPI 0.104、SQLAlchemy 2.0、NumPy、SciPy、psycopg2 (pgvector)

## 详细运行步骤

### 第一步：数据库初始化

#### 1.1 MySQL 数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE Travel CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据
USE Travel;
SOURCE /path/to/Travel.sql;
```

#### 1.2 Redis 服务

确保 Redis 服务已启动，默认端口 6379。

```bash
# Windows
redis-server

# Linux/Mac
sudo systemctl start redis
```

#### 1.3 PostgreSQL + pgvector

> 注：向量数据库已使用云端服务（14.103.124.109:5433），无需本地安装。如需本地部署：

```bash
# 登录 PostgreSQL
psql -U postgres

# 创建数据库和扩展
CREATE DATABASE Travel;
\c Travel
CREATE EXTENSION IF NOT EXISTS vector;
```

### 第二步：配置环境变量

#### 2.1 后端配置

复制环境变量示例文件并填入实际配置：

```bash
cd travel-houduan/Travel
cp .env.example .env
# 编辑 .env 文件，填入实际配置
```

主要环境变量说明：

| 变量名 | 说明 | 示例 |
|--------|------|------|
| MYSQL_HOST | MySQL 主机地址 | localhost |
| MYSQL_PASSWORD | MySQL 密码 | your_password |
| REDIS_PASSWORD | Redis 密码 | your_password |
| DASHSCOPE_API_KEY | 阿里云 DashScope API Key | sk-xxx |
| MAIL_USERNAME | 邮箱账号 | your_email@163.com |
| MAIL_PASSWORD | 邮箱授权码 | your_auth_code |
| COS_SECRET_ID | 腾讯云 COS SecretId | your_secret_id |
| COS_SECRET_KEY | 腾讯云 COS SecretKey | your_secret_key |
| AMAP_KEY | 高德地图 API Key | your_amap_key |
| BAIDU_MAP_AK | 百度地图 API Key | your_baidu_ak |

> 注：PostgreSQL (pgvector) 向量数据库已内置配置，无需额外设置。

#### 2.2 算法服务配置

```bash
cd travel-suanfa
cp .env.example .env
# 编辑 .env 文件，填入数据库连接信息
```

#### 2.3 前端代理配置（可选）

如果后端不在 localhost:8080，需修改 `vite.config.ts` 中的 proxy target。

### 第三步：启动服务（按顺序）

#### 3.1 启动后端服务（端口 8080）

```bash
cd travel-houduan/Travel

# 方式一：Maven 启动
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package -DskipTests
java -jar target/Travel-0.0.1-SNAPSHOT.jar
```

#### 3.2 启动推荐算法服务（端口 8000）

```bash
cd travel-suanfa

# 方式一：直接运行
python run.py

# 方式二：使用 uvicorn
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

#### 3.3 启动用户端前端（端口 5173）

```bash
cd Intelligent_travel_system/Intelligent_travel_system
npm run dev
```

#### 3.4 启动管理端前端（端口 3000）

```bash
cd Intelligent_travel_ManageSystem/intelligent_travel_ManageSystem
npm run dev
```

### 第四步：访问系统

| 服务 | 访问地址 |
|------|----------|
| 用户端（移动端适配） | http://localhost:5173 |
| 管理端 | http://localhost:3000 |
| 后端 API 文档 | http://localhost:8080/api/doc.html |
| 算法服务 API 文档 | http://localhost:8000/docs |

## 技术栈

### 前端技术
- Vue 3.5 + TypeScript + Vite 7
- Vant 4（用户端移动端 UI 组件库）
- Element Plus（管理端 UI 组件库）
- Pinia 状态管理
- Vue Router 4 路由管理
- 高德地图 JavaScript API
- ECharts 6 数据可视化
- TailwindCSS 原子化 CSS
- Three.js + Vanta.js 3D 背景效果

### 后端技术
- Spring Boot 3.4.4
- Spring AI 1.0 + 阿里云百炼大模型（通义千问）
- MyBatis-Plus 3.5.8 ORM 框架
- Sa-Token 1.39 权限认证
- Spring Data Redis 缓存
- PostgreSQL + pgvector 向量数据库
- 腾讯云 COS 对象存储
- iText PDF 生成

### 算法服务技术
- FastAPI 0.104 异步 Web 框架
- SQLAlchemy 2.0 ORM
- NumPy / SciPy 科学计算
- pgvector 向量相似度搜索
- Pydantic 数据验证

## 核心功能

### 用户端
- 🗺️ 基于地理位置的非遗项目推荐
- 🤖 AI 智能对话与行程规划
- 📍 非遗项目地图展示与导航
- 📱 移动端适配的用户体验
- 📄 行程 PDF 导出

### 管理端
- 📊 数据统计与可视化大屏
- 🏪 商户与非遗项目管理
- 👥 用户管理与行为分析
- 📝 内容审核与发布

### 推荐算法
- 多因素权重融合推荐（距离 30%、兴趣 25%、历史 20%、天气 15%、评分 10%）
- 基于 pgvector 的向量相似度搜索
- 天气适配的智能推荐策略
- 用户行为实时分析

## 注意事项

1. 确保所有数据库服务（MySQL、Redis、PostgreSQL）已启动且可访问
2. 后端服务需要配置阿里云 DashScope API Key 才能使用 AI 对话功能
3. 高德地图功能需要配置有效的 API Key
4. PostgreSQL 需要安装 pgvector 扩展才能使用向量搜索功能
5. 生产环境部署时请修改所有默认密码和密钥

## 常见问题

### Q: 后端启动报数据库连接失败？
A: 检查 MySQL 服务是否启动，确认环境变量或 application.yml 中的数据库配置正确。

### Q: 前端页面空白或接口 404？
A: 确认后端服务已启动，检查 vite.config.ts 中的 proxy 配置是否指向正确的后端地址。

### Q: AI 对话功能无响应？
A: 需要配置有效的阿里云 DashScope API Key（环境变量 DASHSCOPE_API_KEY）。

### Q: 推荐结果为空？
A: 确认 PostgreSQL 和 pgvector 扩展已正确安装，且向量数据已初始化。

## 许可证

MIT License
