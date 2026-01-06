"""
ASGI 应用入口文件
用于生产环境部署（Gunicorn + Uvicorn Workers）

使用方式：
1. 作为 Gunicorn 入口: gunicorn -c gunicorn_config.py asgi:app
2. 直接运行: python asgi.py
"""
import uvicorn
from app.main import app
from config.settings import settings

# 导出 app 实例供 Gunicorn 使用
__all__ = ["app"]

# 如果直接运行此文件，则启动服务
if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.DEBUG,
        log_level="info" if not settings.DEBUG else "debug",
        access_log=True,
    )

