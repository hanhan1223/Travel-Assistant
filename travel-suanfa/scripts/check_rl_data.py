"""
检查RL模块数据库中的数据
"""
import sys
import os
from datetime import datetime

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from sqlalchemy import text
from app.models.database import get_mysql_session
from config.settings import settings


def check_interaction_records():
    """检查交互记录"""
    print("=" * 60)
    print("检查推荐交互记录")
    print("=" * 60)
    
    try:
        with get_mysql_session() as session:
            # 检查总数
            count_query = text("SELECT COUNT(*) FROM rl_recommend_interaction")
            total = session.execute(count_query).fetchone()[0]
            print(f"总记录数: {total}")
            
            if total > 0:
                # 最近5条记录
                recent_query = text("""
                    SELECT id, user_id, session_id, action_type, recommended_count,
                           total_reward, created_at
                    FROM rl_recommend_interaction
                    ORDER BY created_at DESC
                    LIMIT 5
                """)
                records = session.execute(recent_query).fetchall()
                
                print("\n最近5条记录:")
                for record in records:
                    print(f"  ID: {record[0]}, 用户: {record[1]}, "
                          f"会话: {record[2][:20]}..., "
                          f"动作: {record[3]}, "
                          f"推荐数: {record[4]}, "
                          f"奖励: {record[5]:.4f}, "
                          f"时间: {record[6]}")
            else:
                print("  暂无记录")
            
            return True
    except Exception as e:
        print(f"✗ 查询失败: {e}")
        return False


def check_feedback_records():
    """检查反馈记录"""
    print("\n" + "=" * 60)
    print("检查用户反馈记录")
    print("=" * 60)
    
    try:
        with get_mysql_session() as session:
            # 检查总数
            count_query = text("SELECT COUNT(*) FROM rl_user_feedback")
            total = session.execute(count_query).fetchone()[0]
            print(f"总记录数: {total}")
            
            if total > 0:
                # 按反馈类型统计
                type_query = text("""
                    SELECT feedback_type, COUNT(*) as count
                    FROM rl_user_feedback
                    GROUP BY feedback_type
                    ORDER BY count DESC
                """)
                types = session.execute(type_query).fetchall()
                
                print("\n按反馈类型统计:")
                for row in types:
                    print(f"  {row[0]}: {row[1]}次")
                
                # 最近5条记录
                recent_query = text("""
                    SELECT id, session_id, user_id, item_id, item_type,
                           feedback_type, feedback_value, feedback_time
                    FROM rl_user_feedback
                    ORDER BY feedback_time DESC
                    LIMIT 5
                """)
                records = session.execute(recent_query).fetchall()
                
                print("\n最近5条记录:")
                for record in records:
                    print(f"  ID: {record[0]}, 会话: {record[1][:20]}..., "
                          f"用户: {record[2]}, "
                          f"物品: {record[3]}({record[4]}), "
                          f"类型: {record[5]}, "
                          f"值: {record[6]}, "
                          f"时间: {record[7]}")
            else:
                print("  暂无记录")
            
            return True
    except Exception as e:
        print(f"✗ 查询失败: {e}")
        return False


def check_user_states():
    """检查用户状态"""
    print("\n" + "=" * 60)
    print("检查用户状态")
    print("=" * 60)
    
    try:
        with get_mysql_session() as session:
            # 检查总数
            count_query = text("SELECT COUNT(*) FROM rl_user_state")
            total = session.execute(count_query).fetchone()[0]
            print(f"总用户数: {total}")
            
            if total > 0:
                # 统计信息
                stats_query = text("""
                    SELECT 
                        COUNT(*) as total_users,
                        SUM(total_clicks) as total_clicks,
                        SUM(total_views) as total_views,
                        SUM(total_conversions) as total_conversions,
                        AVG(avg_reward) as avg_reward
                    FROM rl_user_state
                """)
                stats = session.execute(stats_query).fetchone()
                
                print("\n统计信息:")
                print(f"  总用户数: {stats[0]}")
                print(f"  总点击: {stats[1]}")
                print(f"  总浏览: {stats[2]}")
                print(f"  总转化: {stats[3]}")
                print(f"  平均奖励: {stats[4]:.4f}")
                
                # 活跃用户（最近有推荐的）
                active_query = text("""
                    SELECT user_id, total_clicks, total_views, total_conversions,
                           avg_reward, last_recommend_time
                    FROM rl_user_state
                    WHERE last_recommend_time IS NOT NULL
                    ORDER BY last_recommend_time DESC
                    LIMIT 5
                """)
                active_users = session.execute(active_query).fetchall()
                
                print("\n最近活跃的5个用户:")
                for user in active_users:
                    print(f"  用户ID: {user[0]}, "
                          f"点击: {user[1]}, 浏览: {user[2]}, 转化: {user[3]}, "
                          f"奖励: {user[4]:.4f}, "
                          f"最后推荐: {user[5]}")
            else:
                print("  暂无用户状态")
            
            return True
    except Exception as e:
        print(f"✗ 查询失败: {e}")
        return False


def check_model_params():
    """检查模型参数"""
    print("\n" + "=" * 60)
    print("检查模型参数")
    print("=" * 60)
    
    try:
        with get_mysql_session() as session:
            # 检查总数
            count_query = text("""
                SELECT COUNT(*) 
                FROM rl_model_params 
                WHERE is_active = 1
            """)
            total = session.execute(count_query).fetchone()[0]
            print(f"激活的模型数: {total}")
            
            if total > 0:
                # 列出所有激活的模型
                models_query = text("""
                    SELECT id, model_type, model_key, feature_dim, version,
                           created_at, updated_at
                    FROM rl_model_params
                    WHERE is_active = 1
                    ORDER BY updated_at DESC
                """)
                models = session.execute(models_query).fetchall()
                
                print("\n激活的模型:")
                for model in models:
                    print(f"  ID: {model[0]}, "
                          f"类型: {model[1]}, "
                          f"标识: {model[2]}, "
                          f"特征维度: {model[3]}, "
                          f"版本: {model[4]}, "
                          f"更新时间: {model[6]}")
            else:
                print("  暂无激活的模型")
                print("  提示: 首次推荐时会自动创建默认模型")
            
            return True
    except Exception as e:
        print(f"✗ 查询失败: {e}")
        return False


def main():
    """主函数"""
    print("\n" + "=" * 60)
    print("RL模块数据检查")
    print("=" * 60 + "\n")
    
    results = []
    
    results.append(("交互记录", check_interaction_records()))
    results.append(("反馈记录", check_feedback_records()))
    results.append(("用户状态", check_user_states()))
    results.append(("模型参数", check_model_params()))
    
    print("\n" + "=" * 60)
    print("检查完成")
    print("=" * 60)
    
    for name, success in results:
        status = "✓" if success else "✗"
        print(f"{status} {name}")


if __name__ == "__main__":
    main()


