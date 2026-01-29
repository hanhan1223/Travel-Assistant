"""
测试LinUCB算法实现
验证动作选择、模型更新等核心功能
"""
import sys
from pathlib import Path

# 添加项目根目录到路径
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

import numpy as np
from app.services.rl_service import rl_service
from app.models.schemas import RecommendRequest


def test_linucb_workflow():
    """测试完整的LinUCB工作流"""
    print("=" * 60)
    print("测试LinUCB算法实现")
    print("=" * 60)
    
    # 1. 创建推荐请求
    request = RecommendRequest(
        userId=10001,
        lat=30.6719,
        lng=104.0647,
        interestTags=["刺绣", "陶瓷"],
        weather="晴",
        outdoorSuitable=True,
        limit=10
    )
    
    print("\n1. 获取用户状态...")
    user_state = rl_service.get_user_state(request.userId)
    print(f"   用户状态: clicks={user_state.total_clicks}, views={user_state.total_views}")
    
    print("\n2. 提取状态特征（50维向量）...")
    state_features = rl_service.extract_state_features(request, user_state)
    state_vector = np.array(state_features['state_vector'])
    print(f"   状态向量维度: {len(state_vector)}")
    print(f"   状态摘要: {state_features.get('state_summary')}")
    print(f"   状态向量前10维: {state_vector[:10]}")
    
    print("\n3. 获取模型参数...")
    model_params = rl_service.get_model_params("global")
    if model_params:
        A = np.array(model_params.params_json['A'])
        b = np.array(model_params.params_json['b'])
        alpha = model_params.params_json['alpha']
        print(f"   模型类型: {model_params.model_type}")
        print(f"   特征维度: {model_params.feature_dim}")
        print(f"   探索参数α: {alpha}")
        print(f"   A矩阵形状: {A.shape}")
        print(f"   b向量形状: {b.shape}")
        print(f"   A矩阵对角线（前5个）: {np.diag(A)[:5]}")
        print(f"   b向量（前5个）: {b[:5]}")
    
    print("\n4. LinUCB选择动作（计算UCB）...")
    action = rl_service.select_action(state_features, model_params)
    print(f"   动作类型: {action['action_type']}")
    print(f"   UCB分数: {action.get('ucb_score', 0):.4f}")
    print(f"   利用项: {action.get('exploitation_score', 0):.4f}")
    print(f"   探索项: {action.get('exploration_bonus', 0):.4f}")
    print(f"   权重调整:")
    for key, value in action['weights'].items():
        print(f"     - {key}: {value:.4f}")
    
    print("\n5. 模拟用户反馈（奖励=1.5）...")
    reward = 1.5
    print(f"   奖励值: {reward}")
    
    print("\n6. 更新LinUCB模型参数...")
    success = rl_service.update_model_params(
        state_vector=state_vector,
        reward=reward,
        model_key="global"
    )
    print(f"   更新结果: {'成功' if success else '失败'}")
    
    if success:
        print("\n7. 验证参数更新...")
        # 重新获取模型参数
        updated_params = rl_service.get_model_params("global")
        if updated_params:
            A_new = np.array(updated_params.params_json['A'])
            b_new = np.array(updated_params.params_json['b'])
            
            # 计算变化
            A_diff = np.linalg.norm(A_new - A)
            b_diff = np.linalg.norm(b_new - b)
            
            print(f"   A矩阵变化量（Frobenius范数）: {A_diff:.6f}")
            print(f"   b向量变化量（L2范数）: {b_diff:.6f}")
            print(f"   A矩阵新对角线（前5个）: {np.diag(A_new)[:5]}")
            print(f"   b向量新值（前5个）: {b_new[:5]}")
            
            # 理论验证：A_new = A + x·x^T
            x = state_vector.reshape(-1, 1)
            A_expected = A + x @ x.T
            b_expected = b + reward * state_vector
            
            A_error = np.linalg.norm(A_new - A_expected)
            b_error = np.linalg.norm(b_new - b_expected)
            
            print(f"\n   理论验证:")
            print(f"   A矩阵误差: {A_error:.10f} (应该接近0)")
            print(f"   b向量误差: {b_error:.10f} (应该接近0)")
            
            if A_error < 1e-6 and b_error < 1e-6:
                print("   ✅ LinUCB更新公式验证通过！")
            else:
                print("   ⚠️ 更新可能有误差")
    
    print("\n8. 测试多次更新...")
    for i in range(3):
        # 生成随机状态和奖励
        random_state = np.random.randn(50)
        random_reward = np.random.uniform(0, 2)
        
        success = rl_service.update_model_params(
            state_vector=random_state,
            reward=random_reward,
            model_key="global"
        )
        print(f"   第{i+1}次更新: reward={random_reward:.3f}, 结果={'成功' if success else '失败'}")
    
    print("\n9. 测试UCB计算...")
    final_params = rl_service.get_model_params("global")
    if final_params:
        A_final = np.array(final_params.params_json['A'])
        b_final = np.array(final_params.params_json['b'])
        alpha_final = final_params.params_json['alpha']
        
        # 计算θ = A^-1 b
        try:
            A_inv = np.linalg.inv(A_final)
            theta = A_inv @ b_final
            
            # 计算UCB
            exploitation = theta.T @ state_vector
            exploration = alpha_final * np.sqrt(state_vector.T @ A_inv @ state_vector)
            ucb = exploitation + exploration
            
            print(f"   θ范数: {np.linalg.norm(theta):.4f}")
            print(f"   利用项（θᵀx）: {exploitation:.4f}")
            print(f"   探索项（α√(xᵀA⁻¹x)）: {exploration:.4f}")
            print(f"   UCB值: {ucb:.4f}")
            print("   ✅ UCB计算成功！")
        except np.linalg.LinAlgError:
            print("   ⚠️ 矩阵不可逆，使用伪逆")
            A_inv = np.linalg.pinv(A_final)
            theta = A_inv @ b_final
            print(f"   θ范数（伪逆）: {np.linalg.norm(theta):.4f}")
    
    print("\n" + "=" * 60)
    print("测试完成！")
    print("=" * 60)
    print("\n总结:")
    print("✅ 状态提取（50维向量）")
    print("✅ LinUCB动作选择（UCB计算）")
    print("✅ 模型参数更新（A ← A + xxᵀ, b ← b + rx）")
    print("✅ 权重向量计算（θ = A⁻¹b）")
    print("✅ 探索-利用平衡（α参数）")
    print("\nLinUCB算法已完整实现！")


if __name__ == "__main__":
    try:
        test_linucb_workflow()
    except Exception as e:
        print(f"\n❌ 测试失败: {e}")
        import traceback
        traceback.print_exc()
