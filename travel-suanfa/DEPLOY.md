# 服务器部署说明

## 启动方式

### 方式一：使用 Gunicorn（推荐生产环境）

Gunicorn 是生产环境推荐的方式，提供更好的性能和稳定性。

```bash
# 使用配置文件启动
gunicorn -c gunicorn_config.py asgi:app

# 或者直接指定参数启动
gunicorn asgi:app \
    --workers 4 \
    --worker-class uvicorn.workers.UvicornWorker \
    --bind 0.0.0.0:8000 \
    --timeout 120 \
    --access-logfile - \
    --error-logfile -
```

### 方式二：使用 Uvicorn（简单直接）

```bash
# 开发环境（带自动重载）
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload

# 生产环境（多进程）
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

### 方式三：使用 run.py（简单启动）

```bash
python run.py
```

## 环境变量配置

创建 `.env` 文件配置环境变量（可选，如果不配置则使用默认值）：

```env
# 应用配置
HOST=0.0.0.0
PORT=8000
DEBUG=False

# 数据库配置
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

# Gunicorn 日志配置（可选）
GUNICORN_ACCESS_LOG=/var/log/app/access.log
GUNICORN_ERROR_LOG=/var/log/app/error.log
GUNICORN_PIDFILE=/var/run/app.pid
```

## 使用 Systemd 管理服务（Linux）

创建服务文件 `/etc/systemd/system/travel-recommend.service`：

```ini
[Unit]
Description=Travel Recommend Service
After=network.target

[Service]
Type=notify
User=www-data
Group=www-data
WorkingDirectory=/path/to/travel-suanfa
Environment="PATH=/path/to/venv/bin"
ExecStart=/path/to/venv/bin/gunicorn -c gunicorn_config.py asgi:app
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable travel-recommend
sudo systemctl start travel-recommend
sudo systemctl status travel-recommend
```

## 使用 Supervisor 管理服务

创建配置文件 `/etc/supervisor/conf.d/travel-recommend.conf`：

```ini
[program:travel-recommend]
command=/path/to/venv/bin/gunicorn -c gunicorn_config.py asgi:app
directory=/path/to/travel-suanfa
user=www-data
autostart=true
autorestart=true
redirect_stderr=true
stdout_logfile=/var/log/travel-recommend.log
```

启动服务：

```bash
sudo supervisorctl reread
sudo supervisorctl update
sudo supervisorctl start travel-recommend
```

## 性能优化建议

1. **工作进程数**：通常设置为 `CPU核心数 * 2 + 1`
2. **超时时间**：根据实际业务调整，默认 120 秒
3. **日志管理**：生产环境建议将日志输出到文件，便于排查问题
4. **反向代理**：建议使用 Nginx 作为反向代理，处理静态文件和负载均衡

## Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

