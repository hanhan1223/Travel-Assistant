"""
pytest 配置文件
用于设置测试环境和路径
"""
import sys
from pathlib import Path

# 获取项目根目录
project_root = Path(__file__).parent.parent

# 将项目根目录添加到 Python 路径
if str(project_root) not in sys.path:
    sys.path.insert(0, str(project_root))
