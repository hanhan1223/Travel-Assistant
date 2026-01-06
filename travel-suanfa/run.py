"""
生产环境启动文件
可以直接运行此文件启动服务，或使用 Gunicorn 启动
"""
import uvicorn
from config.settings import settings

if __name__ == "__main__":
    """
    启动方式：
    1. 直接运行: python run.py
    2. 使用 Gunicorn: gunicorn -c gunicorn_config.py asgi:app
    3. 使用 Uvicorn: uvicorn app.main:app --host 0.0.0.0 --port 8000
    """
    uvicorn.run(
        "app.main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=settings.DEBUG,  # 开发环境自动重载，生产环境应设为 False
        log_level="info" if not settings.DEBUG else "debug",
        access_log=True,
        workers=1 if settings.DEBUG else None,  # 开发环境单进程，生产环境使用 Gunicorn
    )

