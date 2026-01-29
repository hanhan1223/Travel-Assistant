"""
非遗文化智能伴游系统 - 推荐引擎服务
主启动文件

使用方式：
    python main.py              # 开发模式（自动重载）
    python main.py --prod       # 生产模式
    python main.py --port 8080  # 指定端口
"""
import sys
import argparse
import uvicorn
from pathlib import Path

# 添加项目根目录到 Python 路径
project_root = Path(__file__).parent
sys.path.insert(0, str(project_root))

from config.settings import settings


def parse_args():
    """解析命令行参数"""
    parser = argparse.ArgumentParser(description='启动推荐引擎服务')
    parser.add_argument(
        '--host',
        type=str,
        default=settings.HOST,
        help=f'服务监听地址 (默认: {settings.HOST})'
    )
    parser.add_argument(
        '--port',
        type=int,
        default=settings.PORT,
        help=f'服务监听端口 (默认: {settings.PORT})'
    )
    parser.add_argument(
        '--prod',
        action='store_true',
        help='生产模式（关闭自动重载和调试）'
    )
    parser.add_argument(
        '--workers',
        type=int,
        default=1,
        help='工作进程数（仅生产模式，默认: 1）'
    )
    return parser.parse_args()


def main():
    """主函数"""
    args = parse_args()
    
    # 确定是否为开发模式
    is_dev = not args.prod and settings.DEBUG
    
    print("=" * 60)
    print(f"🚀 {settings.APP_NAME} v{settings.APP_VERSION}")
    print("=" * 60)
    print(f"📍 服务地址: http://{args.host}:{args.port}")
    print(f"📖 API 文档: http://{args.host}:{args.port}/docs")
    print(f"📚 ReDoc 文档: http://{args.host}:{args.port}/redoc")
    print(f"🔧 运行模式: {'开发模式 (自动重载)' if is_dev else '生产模式'}")
    if not is_dev and args.workers > 1:
        print(f"👷 工作进程: {args.workers}")
    print("=" * 60)
    print("按 Ctrl+C 停止服务")
    print()
    
    # 启动配置
    uvicorn_config = {
        "app": "app.main:app",
        "host": args.host,
        "port": args.port,
        "reload": is_dev,
        "log_level": "debug" if is_dev else "info",
        "access_log": True,
    }
    
    # 生产模式下支持多进程
    if not is_dev and args.workers > 1:
        uvicorn_config["workers"] = args.workers
    
    # 启动服务
    try:
        uvicorn.run(**uvicorn_config)
    except KeyboardInterrupt:
        print("\n\n👋 服务已停止")
    except Exception as e:
        print(f"\n❌ 启动失败: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
