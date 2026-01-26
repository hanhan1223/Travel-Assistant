# 安全配置说明

## 环境变量配置

为了保护敏感信息，本项目使用环境变量来管理密钥和密码。

### 配置方式

#### 方式 1：使用 .env 文件（推荐用于开发环境）

1. 复制 `.env.example` 文件为 `.env`
2. 填写实际的配置值
3. 确保 `.env` 文件已添加到 `.gitignore`

#### 方式 2：使用系统配置管理（推荐用于生产环境）

通过管理后台的"系统管理" -> "系统配置"界面进行配置：
- 所有敏感配置会自动加密存储
- 支持在线测试连接
- 修改后立即生效

#### 方式 3：使用系统环境变量

在服务器上设置环境变量：

```bash
export MYSQL_PASSWORD=your-mysql-password
export DASHSCOPE_API_KEY=your-dashscope-api-key
export REDIS_PASSWORD=your-redis-password
# ... 其他配置
```

### 必需的环境变量

| 变量名 | 说明 | 示例 |
|--------|------|------|
| MYSQL_PASSWORD | MySQL 数据库密码 | `your-password` |
| DASHSCOPE_API_KEY | 阿里云通义千问 API Key | `sk-xxxxx` |
| PGVECTOR_PASSWORD | PGVector 数据库密码 | `your-password` |
| REDIS_PASSWORD | Redis 密码 | `your-password` |
| MAIL_USERNAME | 邮件服务用户名 | `your-email@163.com` |
| MAIL_PASSWORD | 邮件服务密码 | `your-password` |
| COS_SECRET_ID | 腾讯云 COS Secret ID | `AKIDxxxxx` |
| COS_SECRET_KEY | 腾讯云 COS Secret Key | `xxxxx` |
| AMAP_KEY | 高德地图 API Key | `xxxxx` |
| BAIDU_MAP_AK | 百度地图 API Key | `xxxxx` |

### 注意事项

1. **永远不要**将包含真实密钥的配置文件提交到 Git
2. 生产环境建议使用系统配置管理功能
3. 定期更换密钥和密码
4. 使用强密码策略

### 配置优先级

1. 系统配置管理（数据库）- 最高优先级
2. 环境变量
3. application.yml 中的默认值 - 最低优先级

## Git 提交前检查

在提交代码前，请确保：
- [ ] 没有硬编码的密钥和密码
- [ ] `.env` 文件已添加到 `.gitignore`
- [ ] 敏感信息已使用环境变量占位符
