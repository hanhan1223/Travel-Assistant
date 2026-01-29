"""
RL模块验证脚本
用于检查RL模块是否正确实现和配置
"""
import sys
import os

# 添加项目根目录到路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from sqlalchemy import text
from app.models.database import get_mysql_session
from config.settings import settings
import importlib


def check_files():
    """检查必要的文件是否存在"""
    print("=" * 60)
    print("1. 检查文件是否存在")
    print("=" * 60)
    
    required_files = [
        "sql/rl_tables.sql",
        "app/models/rl_dao.py",
        "app/services/rl_service.py",
        "app/utils/cache_manager.py",
        "docs/RL_MODULE_README.md"
    ]
    
    all_exist = True
    for file_path in required_files:
        exists = os.path.exists(file_path)
        status = "✓" if exists else "✗"
        print(f"{status} {file_path}")
        if not exists:
            all_exist = False
    
    return all_exist


def check_imports():
    """检查模块是否可以正常导入"""
    print("\n" + "=" * 60)
    print("2. 检查模块导入")
    print("=" * 60)
    
    modules_to_check = [
        ("app.models.rl_dao", "RLUserStateDAO"),
        ("app.models.rl_dao", "RLModelParamsDAO"),
        ("app.models.rl_dao", "RLInteractionDAO"),
        ("app.models.rl_dao", "RLFeedbackDAO"),
        ("app.services.rl_service", "rl_service"),
        ("app.utils.cache_manager", "cache_manager"),
    ]
    
    all_ok = True
    for module_name, class_name in modules_to_check:
        try:
            module = importlib.import_module(module_name)
            obj = getattr(module, class_name)
            print(f"✓ {module_name}.{class_name}")
        except Exception as e:
            print(f"✗ {module_name}.{class_name} - 错误: {e}")
            all_ok = False
    
    return all_ok


def check_config():
    """检查配置"""
    print("\n" + "=" * 60)
    print("3. 检查配置")
    print("=" * 60)
    
    configs = {
        "RL_ENABLED": settings.RL_ENABLED,
        "RL_MODEL_TYPE": settings.RL_MODEL_TYPE,
        "RL_FEATURE_DIM": settings.RL_FEATURE_DIM,
        "RL_CACHE_TTL_USER_STATE": settings.RL_CACHE_TTL_USER_STATE,
        "RL_CACHE_TTL_MODEL_PARAMS": settings.RL_CACHE_TTL_MODEL_PARAMS,
    }
    
    for key, value in configs.items():
        print(f"✓ {key} = {value}")
    
    return True


def check_database_tables():
    """检查数据库表是否存在"""
    print("\n" + "=" * 60)
    print("4. 检查数据库表")
    print("=" * 60)
    
    required_tables = [
        "rl_recommend_interaction",
        "rl_user_feedback",
        "rl_user_state",
        "rl_model_params",
        "rl_model_training_history"
    ]
    
    all_exist = True
    try:
        with get_mysql_session() as session:
            for table_name in required_tables:
                query = text("""
                    SELECT COUNT(*) 
                    FROM information_schema.tables 
                    WHERE table_schema = :db_name 
                    AND table_name = :table_name
                """)
                result = session.execute(query, {
                    'db_name': settings.MYSQL_DATABASE,
                    'table_name': table_name
                }).fetchone()
                
                exists = result[0] > 0 if result else False
                status = "✓" if exists else "✗"
                print(f"{status} {table_name}")
                if not exists:
                    all_exist = False
    except Exception as e:
        print(f"✗ 数据库连接失败: {e}")
        print("  请确保MySQL数据库可访问")
        return False
    
    return all_exist


def check_api_routes():
    """检查API路由"""
    print("\n" + "=" * 60)
    print("5. 检查API路由")
    print("=" * 60)
    
    try:
        from app.routers.recommend import router
        routes = [route.path for route in router.routes]
        
        expected_routes = [
            "/api/recommend",
            "/api/embedding",
            "/api/feedback"
        ]
        
        all_exist = True
        for route in expected_routes:
            exists = route in routes
            status = "✓" if exists else "✗"
            print(f"{status} {route}")
            if not exists:
                all_exist = False
        
        return all_exist
    except Exception as e:
        print(f"✗ 检查路由失败: {e}")
        return False


def check_rl_service_functionality():
    """检查RL服务功能"""
    print("\n" + "=" * 60)
    print("6. 检查RL服务功能")
    print("=" * 60)
    
    try:
        from app.services.rl_service import rl_service
        
        # 检查方法是否存在
        methods = [
            'extract_state_features',
            'get_user_state',
            'get_model_params',
            'select_action',
            'calculate_reward',
            'save_interaction',
            'save_feedback',
            'update_reward',
            'generate_session_id'
        ]
        
        all_exist = True
        for method_name in methods:
            exists = hasattr(rl_service, method_name)
            status = "✓" if exists else "✗"
            print(f"{status} rl_service.{method_name}()")
            if not exists:
                all_exist = False
        
        # 测试生成会话ID
        try:
            session_id = rl_service.generate_session_id()
            print(f"✓ 测试生成会话ID: {session_id[:20]}...")
        except Exception as e:
            print(f"✗ 生成会话ID失败: {e}")
            all_exist = False
        
        return all_exist
    except Exception as e:
        print(f"✗ 检查RL服务失败: {e}")
        return False


def main():
    """主函数"""
    print("\n" + "=" * 60)
    print("RL模块验证检查")
    print("=" * 60 + "\n")
    
    results = []
    
    # 1. 检查文件
    results.append(("文件检查", check_files()))
    
    # 2. 检查导入
    results.append(("模块导入", check_imports()))
    
    # 3. 检查配置
    results.append(("配置检查", check_config()))
    
    # 4. 检查数据库表
    results.append(("数据库表", check_database_tables()))
    
    # 5. 检查API路由
    results.append(("API路由", check_api_routes()))
    
    # 6. 检查RL服务功能
    results.append(("RL服务功能", check_rl_service_functionality()))
    
    # 汇总结果
    print("\n" + "=" * 60)
    print("检查结果汇总")
    print("=" * 60)
    
    all_passed = True
    for name, result in results:
        status = "✓ 通过" if result else "✗ 失败"
        print(f"{status} - {name}")
        if not result:
            all_passed = False
    
    print("\n" + "=" * 60)
    if all_passed:
        print("✓ 所有检查通过！RL模块已正确实现。")
    else:
        print("✗ 部分检查失败，请查看上面的详细信息。")
    print("=" * 60 + "\n")
    
    return all_passed


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)


