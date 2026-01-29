"""
强化学习服务模块
负责状态提取、奖励计算等核心功能
"""
import uuid
import logging
from typing import Dict, Any, List, Optional
from datetime import datetime
import numpy as np

from app.models.rl_dao import (
    RLUserStateDAO, RLModelParamsDAO, RLInteractionDAO, RLFeedbackDAO
)
from app.models.schemas import (
    RLUserState, RLModelParams, RLInteractionRecord, RecommendRequest, RecommendItem
)
from app.utils.cache_manager import cache_manager
from app.services.recommend_service import recommend_service

logger = logging.getLogger(__name__)


class RLService:
    """强化学习服务类"""
    
    def __init__(self):
        self.default_weights = {
            'distance': 0.30,
            'interest': 0.25,
            'history': 0.20,
            'weather': 0.15,
            'rating': 0.10
        }
        self.model_type = "linucb"
        self.feature_dim = 50  # 状态特征维度
    
    def extract_state_features(self, request: RecommendRequest, 
                              user_state: Optional[RLUserState] = None) -> Dict[str, Any]:
        """
        提取状态特征
        
        Args:
            request: 推荐请求
            user_state: 用户状态（可选）
            
        Returns:
            状态特征字典
        """
        # 用户特征
        user_features = {}
        if user_state and user_state.user_features:
            user_features = user_state.user_features
        else:
            # 默认用户特征
            user_features = {
                'avg_view_duration': 0.0,
                'click_rate': 0.0,
                'conversion_rate': 0.0,
                'preferred_categories': [],
                'avg_distance': 0.0
            }
        
        # 上下文特征
        context_features = {
            'lat': request.lat,
            'lng': request.lng,
            'weather': request.weather or 'unknown',
            'outdoor_suitable': request.outdoorSuitable or True,
            'hour': datetime.now().hour,
            'day_of_week': datetime.now().weekday(),
            'is_weekend': datetime.now().weekday() >= 5
        }
        
        # 兴趣标签
        interest_tags = request.interestTags or []
        
        # 状态向量（用于模型输入）
        state_vector = self._build_state_vector(user_features, context_features, interest_tags)
        
        return {
            'user_features': user_features,
            'context_features': context_features,
            'interest_tags': interest_tags,
            'state_vector': state_vector.tolist() if isinstance(state_vector, np.ndarray) else state_vector,
            'state_summary': self._build_state_summary(user_features, context_features)
        }
    
    def _build_state_vector(self, user_features: Dict[str, Any],
                           context_features: Dict[str, Any],
                           interest_tags: List[str]) -> np.ndarray:
        """
        构建状态向量（用于模型输入）
        
        Args:
            user_features: 用户特征
            context_features: 上下文特征
            interest_tags: 兴趣标签
            
        Returns:
            状态向量（numpy数组）
        """
        vector = []
        
        # 用户特征（10维）
        vector.extend([
            user_features.get('avg_view_duration', 0.0) / 300.0,  # 归一化到0-1
            user_features.get('click_rate', 0.0),
            user_features.get('conversion_rate', 0.0),
            user_features.get('avg_distance', 0.0) / 50.0,  # 归一化到0-1
            len(user_features.get('preferred_categories', [])) / 10.0,  # 归一化
        ])
        # 补充到10维
        while len(vector) < 10:
            vector.append(0.0)
        
        # 上下文特征（15维）
        vector.extend([
            context_features.get('lat', 0.0) / 90.0,  # 归一化
            context_features.get('lng', 0.0) / 180.0,  # 归一化
            self._encode_weather(context_features.get('weather', 'unknown')),
            float(context_features.get('outdoor_suitable', True)),
            context_features.get('hour', 12) / 24.0,  # 归一化
            context_features.get('day_of_week', 0) / 7.0,  # 归一化
            float(context_features.get('is_weekend', False)),
        ])
        # 补充到15维
        while len(vector) < 25:
            vector.append(0.0)
        
        # 兴趣标签（10维，one-hot编码主要类别）
        category_vector = [0.0] * 10
        categories = ['刺绣', '陶瓷', '剪纸', '木雕', '漆器', '织锦', '竹编', '泥塑', '皮影', '其他']
        for i, cat in enumerate(categories[:10]):
            if cat in interest_tags:
                category_vector[i] = 1.0
        vector.extend(category_vector)
        
        # 补充到指定维度
        while len(vector) < self.feature_dim:
            vector.append(0.0)
        
        return np.array(vector[:self.feature_dim])
    
    def _encode_weather(self, weather: str) -> float:
        """将天气编码为数值"""
        weather_map = {
            '晴': 1.0,
            '多云': 0.8,
            '阴': 0.5,
            '小雨': 0.3,
            '中雨': 0.2,
            '大雨': 0.1,
            '暴雨': 0.0
        }
        return weather_map.get(weather, 0.5)
    
    def _build_state_summary(self, user_features: Dict[str, Any],
                            context_features: Dict[str, Any]) -> str:
        """构建状态摘要（便于查看）"""
        summary_parts = []
        
        if user_features.get('click_rate', 0) > 0:
            summary_parts.append(f"点击率{user_features['click_rate']:.2%}")
        
        if context_features.get('weather'):
            summary_parts.append(f"天气{context_features['weather']}")
        
        if context_features.get('is_weekend'):
            summary_parts.append("周末")
        
        return ", ".join(summary_parts) if summary_parts else "默认状态"
    
    def get_user_state(self, user_id: int) -> Optional[RLUserState]:
        """
        获取用户状态（带缓存）
        
        Args:
            user_id: 用户ID
            
        Returns:
            用户状态对象
        """
        # 先查缓存
        cached_state = cache_manager.get_user_state(user_id)
        if cached_state:
            return cached_state
        
        # 查数据库
        state = RLUserStateDAO.get_user_state(user_id)
        
        # 如果不存在，创建默认状态
        if state is None:
            state = RLUserState(
                user_id=user_id,
                user_features={},
                recent_context={},
                total_clicks=0,
                total_views=0,
                total_conversions=0,
                avg_reward=0.0
            )
            # 保存到数据库
            RLUserStateDAO.save_user_state(state)
        
        # 存入缓存
        if state:
            cache_manager.set_user_state(user_id, state)
        
        return state
    
    def get_model_params(self, model_key: str = "global") -> Optional[RLModelParams]:
        """
        获取模型参数（带缓存）
        
        Args:
            model_key: 模型标识，默认"global"
            
        Returns:
            模型参数对象
        """
        full_key = f"{self.model_type}:{model_key}"
        
        # 先查缓存
        cached_params = cache_manager.get_model_params(full_key)
        if cached_params:
            return cached_params
        
        # 查数据库
        params = RLModelParamsDAO.get_active_model_params(self.model_type, model_key)
        
        # 如果不存在，初始化默认参数
        if params is None:
            default_params = self._init_default_params()
            RLModelParamsDAO.init_default_model(
                self.model_type, model_key, self.feature_dim, default_params
            )
            params = RLModelParamsDAO.get_active_model_params(self.model_type, model_key)
        
        # 存入缓存
        if params:
            cache_manager.set_model_params(full_key, params)
        
        return params
    
    def _init_default_params(self) -> Dict[str, Any]:
        """初始化默认模型参数（LinUCB）"""
        # LinUCB 默认参数
        # A: d×d 单位矩阵
        # b: d维零向量
        # alpha: 探索参数
        d = self.feature_dim
        return {
            'A': np.eye(d).tolist(),  # 单位矩阵
            'b': np.zeros(d).tolist(),  # 零向量
            'alpha': 0.1  # 探索参数
        }
    
    def select_action(self, state_features: Dict[str, Any],
                     model_params: Optional[RLModelParams] = None) -> Dict[str, Any]:
        """
        根据状态选择动作（权重组合）- LinUCB算法
        
        Args:
            state_features: 状态特征
            model_params: 模型参数（可选）
            
        Returns:
            动作字典（包含权重组合）
        """
        # 如果模型参数不存在，使用默认权重
        if model_params is None:
            return {
                'action_type': 'weight_adjustment',
                'weights': self.default_weights.copy(),
                'ucb_score': 0.0,
                'exploration_bonus': 0.0
            }
        
        try:
            # 提取状态向量
            state_vector = np.array(state_features.get('state_vector', []))
            if len(state_vector) == 0 or len(state_vector) != self.feature_dim:
                logger.warning(f"状态向量维度不匹配: {len(state_vector)} != {self.feature_dim}")
                return {
                    'action_type': 'weight_adjustment',
                    'weights': self.default_weights.copy(),
                    'ucb_score': 0.0,
                    'exploration_bonus': 0.0
                }
            
            # 获取模型参数
            params_json = model_params.params_json
            A = np.array(params_json['A'])  # d×d 协方差矩阵
            b = np.array(params_json['b'])  # d维 累积奖励向量
            alpha = params_json.get('alpha', 0.1)  # 探索参数
            
            # 计算权重向量 θ = A⁻¹b
            try:
                A_inv = np.linalg.inv(A)
                theta = A_inv @ b  # θ = A⁻¹b
            except np.linalg.LinAlgError:
                logger.warning("矩阵A不可逆，使用伪逆")
                A_inv = np.linalg.pinv(A)
                theta = A_inv @ b
            
            # 计算UCB上界
            # UCB = θᵀx + α√(xᵀA⁻¹x)
            exploitation_score = theta.T @ state_vector  # 利用项
            exploration_bonus = alpha * np.sqrt(state_vector.T @ A_inv @ state_vector)  # 探索项
            ucb_score = exploitation_score + exploration_bonus
            
            # 根据UCB分数调整权重（简化实现：基于exploitation_score调整）
            # 这里可以设计多个动作候选，选择UCB最大的
            # 简化版本：根据当前学到的θ微调默认权重
            adjusted_weights = self._adjust_weights_by_theta(theta, state_vector)
            
            logger.info(f"LinUCB选择动作: UCB={ucb_score:.4f}, "
                       f"利用={exploitation_score:.4f}, 探索={exploration_bonus:.4f}")
            
            return {
                'action_type': 'weight_adjustment',
                'weights': adjusted_weights,
                'ucb_score': float(ucb_score),
                'exploitation_score': float(exploitation_score),
                'exploration_bonus': float(exploration_bonus),
                'theta_norm': float(np.linalg.norm(theta))
            }
            
        except Exception as e:
            logger.error(f"LinUCB动作选择失败: {e}", exc_info=True)
            return {
                'action_type': 'weight_adjustment',
                'weights': self.default_weights.copy(),
                'ucb_score': 0.0,
                'exploration_bonus': 0.0
            }
    
    def _adjust_weights_by_theta(self, theta: np.ndarray, state_vector: np.ndarray) -> Dict[str, float]:
        """
        根据学习到的θ调整权重（简化实现）
        
        Args:
            theta: 权重向量
            state_vector: 状态向量
            
        Returns:
            调整后的权重字典
        """
        # 简化版本：使用默认权重作为基础，根据θ的前5维微调
        # 实际可以设计更复杂的映射关系
        weights = self.default_weights.copy()
        
        # 提取θ中与权重相关的维度（假设前5维对应5个权重因子）
        if len(theta) >= 5:
            adjustments = theta[:5]
            # 归一化调整量到 [-0.1, 0.1] 范围
            adjustments = np.clip(adjustments, -0.1, 0.1)
            
            weight_keys = ['distance', 'interest', 'history', 'weather', 'rating']
            for i, key in enumerate(weight_keys):
                weights[key] = max(0.05, min(0.50, weights[key] + adjustments[i]))
            
            # 重新归一化到和为1
            total = sum(weights.values())
            weights = {k: v / total for k, v in weights.items()}
        
        return weights
    
    def calculate_reward(self, session_id: str) -> Dict[str, float]:
        """
        计算奖励（基于用户反馈）
        
        Args:
            session_id: 会话ID
            
        Returns:
            奖励字典
        """
        # 获取该会话的所有反馈
        feedbacks = RLFeedbackDAO.get_feedbacks_by_session(session_id)
        
        immediate_reward = 0.0
        delayed_reward = 0.0
        
        reward_details = {
            'click_reward': 0.0,
            'view_duration_reward': 0.0,
            'interaction_reward': 0.0,
            'visit_reward': 0.0,
            'purchase_reward': 0.0
        }
        
        for feedback in feedbacks:
            feedback_type = feedback['feedback_type']
            feedback_value = feedback['feedback_value']
            
            if feedback_type == 'click':
                immediate_reward += 0.3
                reward_details['click_reward'] += 0.3
            elif feedback_type == 'view':
                # 浏览时长奖励（假设feedback_value是秒数）
                duration_reward = min(feedback_value / 60.0, 1.0) * 0.2
                immediate_reward += duration_reward
                reward_details['view_duration_reward'] += duration_reward
            elif feedback_type == 'favorite' or feedback_type == 'share':
                immediate_reward += 0.5
                reward_details['interaction_reward'] += 0.5
            elif feedback_type == 'visit':
                delayed_reward += 1.0
                reward_details['visit_reward'] += 1.0
            elif feedback_type == 'purchase':
                delayed_reward += 2.0
                reward_details['purchase_reward'] += 2.0
            elif feedback_type == 'dismiss':
                immediate_reward -= 0.2
        
        total_reward = immediate_reward + 0.5 * delayed_reward  # 延迟奖励衰减
        
        return {
            'immediate_reward': immediate_reward,
            'delayed_reward': delayed_reward,
            'total_reward': total_reward,
            'reward_details': reward_details
        }
    
    def save_interaction(self, user_id: int, session_id: str,
                        state_features: Dict[str, Any], action: Dict[str, Any],
                        recommended_items: List[RecommendItem],
                        request: RecommendRequest) -> Optional[int]:
        """
        保存推荐交互记录
        
        Args:
            user_id: 用户ID
            session_id: 会话ID
            state_features: 状态特征
            action: 动作
            recommended_items: 推荐结果
            request: 推荐请求
            
        Returns:
            记录ID
        """
        # 构建推荐物品列表
        items_list = []
        for item in recommended_items:
            items_list.append({
                'id': item.id,
                'type': item.type,
                'name': item.name,
                'score': item.score
            })
        
        # 构建请求上下文
        request_context = {
            'lat': request.lat,
            'lng': request.lng,
            'weather': request.weather,
            'outdoor_suitable': request.outdoorSuitable,
            'interest_tags': request.interestTags,
            'limit': request.limit
        }
        
        record = RLInteractionRecord(
            user_id=user_id,
            session_id=session_id,
            state_features=state_features,
            state_summary=state_features.get('state_summary'),
            action_type=action.get('action_type', 'weight_adjustment'),
            action_params=action.get('weights'),
            recommended_items=items_list,
            recommended_count=len(recommended_items),
            request_context=request_context,
            model_version=f"{self.model_type}_v1"
        )
        
        return RLInteractionDAO.save_interaction(record)
    
    def update_reward(self, session_id: str) -> bool:
        """
        更新交互记录的奖励信息，并触发模型参数更新
        
        Args:
            session_id: 会话ID
            
        Returns:
            是否成功
        """
        reward_info = self.calculate_reward(session_id)
        
        # 更新交互记录的奖励
        success = RLInteractionDAO.update_reward(
            session_id=session_id,
            immediate_reward=reward_info['immediate_reward'],
            delayed_reward=reward_info['delayed_reward'],
            total_reward=reward_info['total_reward'],
            reward_details=reward_info['reward_details']
        )
        
        # 如果奖励更新成功，触发模型参数更新
        if success:
            try:
                # 获取该会话的交互记录
                interaction = RLInteractionDAO.get_interaction_by_session(session_id)
                if interaction and interaction.get('state_features'):
                    state_vector = np.array(interaction['state_features'].get('state_vector', []))
                    total_reward = reward_info['total_reward']
                    
                    if len(state_vector) == self.feature_dim:
                        # 更新模型参数（LinUCB在线学习）
                        self.update_model_params(
                            state_vector=state_vector,
                            reward=total_reward,
                            model_key="global"  # 使用全局模型
                        )
                        logger.info(f"模型参数已更新: session_id={session_id}, reward={total_reward:.4f}")
            except Exception as e:
                logger.error(f"模型参数更新失败: {e}", exc_info=True)
        
        return success
    
    def update_model_params(self, state_vector: np.ndarray, reward: float, 
                           model_key: str = "global") -> bool:
        """
        更新LinUCB模型参数（在线学习）
        
        Args:
            state_vector: 状态向量 x (d维)
            reward: 奖励值 r
            model_key: 模型标识
            
        Returns:
            是否成功
        """
        try:
            full_key = f"{self.model_type}:{model_key}"
            
            # 获取当前模型参数
            model_params = self.get_model_params(model_key)
            if model_params is None:
                logger.error(f"模型参数不存在: {model_key}")
                return False
            
            # 提取当前的A和b
            params_json = model_params.params_json
            A = np.array(params_json['A'])  # d×d 矩阵
            b = np.array(params_json['b'])  # d维向量
            alpha = params_json.get('alpha', 0.1)
            
            # LinUCB参数更新公式
            # A ← A + x·xᵀ
            # b ← b + r·x
            x = state_vector.reshape(-1, 1)  # 转为列向量 (d, 1)
            A_new = A + x @ x.T  # (d, d) + (d, 1) @ (1, d) = (d, d)
            b_new = b + reward * state_vector  # (d,) + scalar * (d,) = (d,)
            
            # 更新参数到数据库
            new_params_json = {
                'A': A_new.tolist(),
                'b': b_new.tolist(),
                'alpha': alpha
            }
            
            success = RLModelParamsDAO.update_model_params(
                model_type=self.model_type,
                model_key=model_key,
                params_json=new_params_json
            )
            
            if success:
                # 更新缓存
                model_params.params_json = new_params_json
                cache_manager.set_model_params(full_key, model_params)
                logger.info(f"LinUCB参数更新成功: model_key={model_key}, reward={reward:.4f}")
            
            return success
            
        except Exception as e:
            logger.error(f"更新模型参数失败: {e}", exc_info=True)
            return False
    
    def save_feedback(self, session_id: str, user_id: int, item_id: int,
                     item_type: str, feedback_type: str, feedback_value: float = 0.0,
                     extra_data: Optional[Dict[str, Any]] = None) -> Optional[int]:
        """
        保存用户反馈
        
        Args:
            session_id: 会话ID
            user_id: 用户ID
            item_id: 物品ID
            item_type: 物品类型
            feedback_type: 反馈类型
            feedback_value: 反馈值
            extra_data: 额外数据
            
        Returns:
            反馈记录ID
        """
        return RLFeedbackDAO.save_feedback(
            session_id=session_id,
            user_id=user_id,
            item_id=item_id,
            item_type=item_type,
            feedback_type=feedback_type,
            feedback_value=feedback_value,
            extra_data=extra_data
        )
    
    def generate_session_id(self) -> str:
        """生成会话ID"""
        return str(uuid.uuid4())


# 全局RL服务实例
rl_service = RLService()

