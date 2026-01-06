"""
Gunicorn 配置文件
用于生产环境部署
"""
import multiprocessing
import os
from config.settings import settings

# 服务器配置
bind = f"{settings.HOST}:{settings.PORT}"
workers = multiprocessing.cpu_count() * 2 + 1  # 推荐的工作进程数
worker_class = "uvicorn.workers.UvicornWorker"  # 使用 Uvicorn Worker
worker_connections = 1000
max_requests = 1000  # 每个 worker 处理的最大请求数，防止内存泄漏
max_requests_jitter = 50  # 随机抖动，避免所有 worker 同时重启
timeout = 120  # 超时时间（秒）
keepalive = 5  # Keep-alive 连接时间

# 日志配置
accesslog = os.getenv("GUNICORN_ACCESS_LOG", "-")  # 访问日志，"-" 表示输出到 stdout
errorlog = os.getenv("GUNICORN_ERROR_LOG", "-")  # 错误日志，"-" 表示输出到 stderr
loglevel = "info" if not settings.DEBUG else "debug"
access_log_format = '%(h)s %(l)s %(u)s %(t)s "%(r)s" %(s)s %(b)s "%(f)s" "%(a)s" %(D)s'

# 进程配置
daemon = False  # 不以后台进程运行（建议使用 systemd/supervisor 管理）
pidfile = os.getenv("GUNICORN_PIDFILE", None)  # PID 文件路径（可选）
user = os.getenv("GUNICORN_USER", None)  # 运行用户（可选）
group = os.getenv("GUNICORN_GROUP", None)  # 运行组（可选）
tmp_upload_dir = None  # 临时上传目录

# 性能优化
preload_app = True  # 预加载应用，节省内存
# worker_tmp_dir = "/dev/shm"  # Linux 内存文件系统（如果可用）提高性能，Windows 不需要

# 安全配置
limit_request_line = 4094  # 请求行最大长度
limit_request_fields = 100  # 请求头最大数量
limit_request_field_size = 8190  # 单个请求头最大大小

