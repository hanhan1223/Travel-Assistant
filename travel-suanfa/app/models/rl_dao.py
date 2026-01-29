"""
强化学习数据访问层（DAO）
用于操作MySQL中的RL相关表
"""
import json
import uuid
import logging
from typing import Optional, Dict, Any, List
from datetime import datetime, date
from sqlalchemy import text

from app.models.database import get_mysql_session
from app.models.schemas import RLUserState, RLModelParams, RLInteractionRecord

logger = logging.getLogger(__name__)


class RLUserStateDAO:
    """用户状态数据访问层"""
    
    @staticmethod
    def get_user_state(user_id: int) -> Optional[RLUserState]:
        """
        获取用户状态
        
        Args:
            user_id: 用户ID
            
        Returns:
            用户状态对象，如果不存在返回None
        """
        try:
            with get_mysql_session() as session:
                query = text("""
                    SELECT user_id, user_features, recent_context,
                           total_clicks, total_views, total_conversions, avg_reward,
                           last_recommend_time, last_update_time
                    FROM rl_user_state
                    WHERE user_id = :user_id
                """)
                result = session.execute(query, {'user_id': user_id}).fetchone()
                
                if result is None:
                    return None
                
                return RLUserState(
                    user_id=result[0],
                    user_features=json.loads(result[1]) if result[1] else None,
                    recent_context=json.loads(result[2]) if result[2] else None,
                    total_clicks=result[3] or 0,
                    total_views=result[4] or 0,
                    total_conversions=result[5] or 0,
                    avg_reward=float(result[6]) if result[6] else 0.0,
                    last_recommend_time=result[7],
                    last_update_time=result[8]
                )
        except Exception as e:
            logger.error(f"获取用户状态失败: {e}", exc_info=True)
            return None
    
    @staticmethod
    def save_user_state(state: RLUserState) -> bool:
        """
        保存或更新用户状态
        
        Args:
            state: 用户状态对象
            
        Returns:
            是否成功
        """
        try:
            with get_mysql_session() as session:
                query = text("""
                    INSERT INTO rl_user_state 
                    (user_id, user_features, recent_context, total_clicks, total_views,
                     total_conversions, avg_reward, last_recommend_time, last_update_time)
                    VALUES (:user_id, :user_features, :recent_context, :total_clicks, :total_views,
                            :total_conversions, :avg_reward, :last_recommend_time, NOW())
                    ON DUPLICATE KEY UPDATE
                        user_features = VALUES(user_features),
                        recent_context = VALUES(recent_context),
                        total_clicks = VALUES(total_clicks),
                        total_views = VALUES(total_views),
                        total_conversions = VALUES(total_conversions),
                        avg_reward = VALUES(avg_reward),
                        last_recommend_time = VALUES(last_recommend_time),
                        last_update_time = NOW()
                """)
                
                session.execute(query, {
                    'user_id': state.user_id,
                    'user_features': json.dumps(state.user_features, ensure_ascii=False) if state.user_features else None,
                    'recent_context': json.dumps(state.recent_context, ensure_ascii=False) if state.recent_context else None,
                    'total_clicks': state.total_clicks,
                    'total_views': state.total_views,
                    'total_conversions': state.total_conversions,
                    'avg_reward': state.avg_reward,
                    'last_recommend_time': state.last_recommend_time or datetime.now()
                })
                return True
        except Exception as e:
            logger.error(f"保存用户状态失败: {e}", exc_info=True)
            return False
    
    @staticmethod
    def update_user_stats(user_id: int, clicks: int = 0, views: int = 0, 
                         conversions: int = 0, reward: float = 0.0) -> bool:
        """
        更新用户统计信息（原子操作）
        
        Args:
            user_id: 用户ID
            clicks: 增加的点击次数
            views: 增加的浏览次数
            conversions: 增加的转化次数
            reward: 增加的奖励值
            
        Returns:
            是否成功
        """
        try:
            with get_mysql_session() as session:
                query = text("""
                    UPDATE rl_user_state
                    SET total_clicks = total_clicks + :clicks,
                        total_views = total_views + :views,
                        total_conversions = total_conversions + :conversions,
                        avg_reward = (avg_reward * total_clicks + :reward) / (total_clicks + :clicks + 1),
                        last_update_time = NOW()
                    WHERE user_id = :user_id
                """)
                session.execute(query, {
                    'user_id': user_id,
                    'clicks': clicks,
                    'views': views,
                    'conversions': conversions,
                    'reward': reward
                })
                return True
        except Exception as e:
            logger.error(f"更新用户统计失败: {e}", exc_info=True)
            return False


class RLModelParamsDAO:
    """模型参数数据访问层"""
    
    @staticmethod
    def get_active_model_params(model_type: str, model_key: str) -> Optional[RLModelParams]:
        """
        获取激活的模型参数
        
        Args:
            model_type: 模型类型（linucb/dqn等）
            model_key: 模型标识（user:10001 或 global）
            
        Returns:
            模型参数对象，如果不存在返回None
        """
        try:
            with get_mysql_session() as session:
                query = text("""
                    SELECT id, model_type, model_key, params_json, feature_dim,
                           version, is_active, created_at, updated_at
                    FROM rl_model_params
                    WHERE model_type = :model_type 
                      AND model_key = :model_key
                      AND is_active = 1
                    ORDER BY version DESC
                    LIMIT 1
                """)
                result = session.execute(query, {
                    'model_type': model_type,
                    'model_key': model_key
                }).fetchone()
                
                if result is None:
                    return None
                
                return RLModelParams(
                    id=result[0],
                    model_type=result[1],
                    model_key=result[2],
                    params_json=json.loads(result[3]) if result[3] else {},
                    feature_dim=result[4],
                    version=result[5],
                    is_active=bool(result[6]),
                    created_at=result[7],
                    updated_at=result[8]
                )
        except Exception as e:
            logger.error(f"获取模型参数失败: {e}", exc_info=True)
            return None
    
    @staticmethod
    def save_model_params(params: RLModelParams) -> bool:
        """
        保存模型参数（插入新版本，旧版本标记为非激活）
        
        Args:
            params: 模型参数对象
            
        Returns:
            是否成功
        """
        try:
            with get_mysql_session() as session:
                # 先将旧版本标记为非激活
                deactivate_query = text("""
                    UPDATE rl_model_params
                    SET is_active = 0
                    WHERE model_type = :model_type 
                      AND model_key = :model_key
                      AND is_active = 1
                """)
                session.execute(deactivate_query, {
                    'model_type': params.model_type,
                    'model_key': params.model_key
                })
                
                # 获取新版本号
                version_query = text("""
                    SELECT COALESCE(MAX(version), 0) + 1
                    FROM rl_model_params
                    WHERE model_type = :model_type 
                      AND model_key = :model_key
                """)
                version_result = session.execute(version_query, {
                    'model_type': params.model_type,
                    'model_key': params.model_key
                }).fetchone()
                new_version = version_result[0] if version_result else 1
                
                # 插入新版本
                insert_query = text("""
                    INSERT INTO rl_model_params
                    (model_type, model_key, params_json, feature_dim, version, is_active)
                    VALUES (:model_type, :model_key, :params_json, :feature_dim, :version, 1)
                """)
                session.execute(insert_query, {
                    'model_type': params.model_type,
                    'model_key': params.model_key,
                    'params_json': json.dumps(params.params_json, ensure_ascii=False),
                    'feature_dim': params.feature_dim,
                    'version': new_version
                })
                return True
        except Exception as e:
            logger.error(f"保存模型参数失败: {e}", exc_info=True)
            return False
    
    @staticmethod
    def init_default_model(model_type: str, model_key: str, feature_dim: int, 
                          default_params: Dict[str, Any]) -> bool:
        """
        初始化默认模型参数
        
        Args:
            model_type: 模型类型
            model_key: 模型标识
            feature_dim: 特征维度
            default_params: 默认参数字典
            
        Returns:
            是否成功
        """
        params = RLModelParams(
            model_type=model_type,
            model_key=model_key,
            params_json=default_params,
            feature_dim=feature_dim,
            version=1,
            is_active=True
        )
        return RLModelParamsDAO.save_model_params(params)
    
    @staticmethod
    def update_model_params(model_type: str, model_key: str, 
                           params_json: Dict[str, Any]) -> bool:
        """
        更新模型参数（在线学习时更新A和b矩阵）
        
        Args:
            model_type: 模型类型
            model_key: 模型标识
            params_json: 新的参数JSON
            
        Returns:
            是否成功
        """
        try:
            with get_mysql_session() as session:
                query = text("""
                    UPDATE rl_model_params
                    SET params_json = :params_json,
                        updated_at = NOW()
                    WHERE model_type = :model_type 
                      AND model_key = :model_key
                      AND is_active = 1
                """)
                result = session.execute(query, {
                    'model_type': model_type,
                    'model_key': model_key,
                    'params_json': json.dumps(params_json, ensure_ascii=False)
                })
                return result.rowcount > 0
        except Exception as e:
            logger.error(f"更新模型参数失败: {e}", exc_info=True)
            return False


class RLInteractionDAO:
    """推荐交互记录数据访问层"""
    
    @staticmethod
    def save_interaction(record: RLInteractionRecord) -> Optional[int]:
        """
        保存推荐交互记录
        
        Args:
            record: 交互记录对象
            
        Returns:
            插入的记录ID，失败返回None
        """
        try:
            with get_mysql_session() as session:
                query = text("""
                    INSERT INTO rl_recommend_interaction
                    (user_id, session_id, state_features, state_summary, action_type,
                     action_params, recommended_items, recommended_count,
                     immediate_reward, delayed_reward, total_reward, reward_details,
                     request_context, model_version)
                    VALUES (:user_id, :session_id, :state_features, :state_summary, :action_type,
                            :action_params, :recommended_items, :recommended_count,
                            :immediate_reward, :delayed_reward, :total_reward, :reward_details,
                            :request_context, :model_version)
                """)
                
                result = session.execute(query, {
                    'user_id': record.user_id,
                    'session_id': record.session_id,
                    'state_features': json.dumps(record.state_features, ensure_ascii=False) if record.state_features else None,
                    'state_summary': record.state_summary,
                    'action_type': record.action_type,
                    'action_params': json.dumps(record.action_params, ensure_ascii=False) if record.action_params else None,
                    'recommended_items': json.dumps(record.recommended_items, ensure_ascii=False) if record.recommended_items else None,
                    'recommended_count': record.recommended_count,
                    'immediate_reward': record.immediate_reward,
                    'delayed_reward': record.delayed_reward,
                    'total_reward': record.total_reward,
                    'reward_details': json.dumps(record.reward_details, ensure_ascii=False) if record.reward_details else None,
                    'request_context': json.dumps(record.request_context, ensure_ascii=False) if record.request_context else None,
                    'model_version': record.model_version
                })
                
                # 获取插入的ID
                id_query = text("SELECT LAST_INSERT_ID()")
                id_result = session.execute(id_query).fetchone()
                return id_result[0] if id_result else None
        except Exception as e:
            logger.error(f"保存交互记录失败: {e}", exc_info=True)
            return None
    
    @staticmethod
    def update_reward(session_id: str, immediate_reward: float = 0.0,
                     delayed_reward: float = 0.0, total_reward: float = 0.0,
                     reward_details: Optional[Dict[str, Any]] = None) -> bool:
        """
        更新交互记录的奖励信息
        
        Args:
            session_id: 会话ID
            immediate_reward: 即时奖励
            delayed_reward: 延迟奖励
            total_reward: 总奖励
            reward_details: 奖励详情
            
        Returns:
            是否成功
        """
        try:
            with get_mysql_session() as session:
                query = text("""
                    UPDATE rl_recommend_interaction
                    SET immediate_reward = :immediate_reward,
                        delayed_reward = :delayed_reward,
                        total_reward = :total_reward,
                        reward_details = :reward_details,
                        updated_at = NOW()
                    WHERE session_id = :session_id
                """)
                session.execute(query, {
                    'session_id': session_id,
                    'immediate_reward': immediate_reward,
                    'delayed_reward': delayed_reward,
                    'total_reward': total_reward,
                    'reward_details': json.dumps(reward_details, ensure_ascii=False) if reward_details else None
                })
                return True
        except Exception as e:
            logger.error(f"更新奖励信息失败: {e}", exc_info=True)
            return False
    
    @staticmethod
    def get_interaction_by_session(session_id: str) -> Optional[Dict[str, Any]]:
        """
        根据session_id获取交互记录
        
        Args:
            session_id: 会话ID
            
        Returns:
            交互记录字典，如果不存在返回None
        """
        try:
            with get_mysql_session() as session:
                query = text("""
                    SELECT id, user_id, session_id, state_features, state_summary,
                           action_type, action_params, recommended_items, recommended_count,
                           immediate_reward, delayed_reward, total_reward, reward_details,
                           request_context, model_version, created_at, updated_at
                    FROM rl_recommend_interaction
                    WHERE session_id = :session_id
                """)
                result = session.execute(query, {'session_id': session_id}).fetchone()
                
                if result is None:
                    return None
                
                return {
                    'id': result[0],
                    'user_id': result[1],
                    'session_id': result[2],
                    'state_features': json.loads(result[3]) if result[3] else None,
                    'state_summary': result[4],
                    'action_type': result[5],
                    'action_params': json.loads(result[6]) if result[6] else None,
                    'recommended_items': json.loads(result[7]) if result[7] else None,
                    'recommended_count': result[8],
                    'immediate_reward': float(result[9]) if result[9] else 0.0,
                    'delayed_reward': float(result[10]) if result[10] else 0.0,
                    'total_reward': float(result[11]) if result[11] else 0.0,
                    'reward_details': json.loads(result[12]) if result[12] else None,
                    'request_context': json.loads(result[13]) if result[13] else None,
                    'model_version': result[14],
                    'created_at': result[15],
                    'updated_at': result[16]
                }
        except Exception as e:
            logger.error(f"获取交互记录失败: {e}", exc_info=True)
            return None
    
    @staticmethod
    def get_interactions_by_user(user_id: int, limit: int = 100) -> List[Dict[str, Any]]:
        """
        获取用户的交互记录（用于训练）
        
        Args:
            user_id: 用户ID
            limit: 返回数量限制
            
        Returns:
            交互记录列表
        """
        try:
            with get_mysql_session() as session:
                query = text("""
                    SELECT id, user_id, session_id, state_features, action_type,
                           action_params, total_reward, created_at
                    FROM rl_recommend_interaction
                    WHERE user_id = :user_id
                    ORDER BY created_at DESC
                    LIMIT :limit
                """)
                results = session.execute(query, {
                    'user_id': user_id,
                    'limit': limit
                }).fetchall()
                
                interactions = []
                for row in results:
                    interactions.append({
                        'id': row[0],
                        'user_id': row[1],
                        'session_id': row[2],
                        'state_features': json.loads(row[3]) if row[3] else None,
                        'action_type': row[4],
                        'action_params': json.loads(row[5]) if row[5] else None,
                        'total_reward': float(row[6]) if row[6] else 0.0,
                        'created_at': row[7]
                    })
                return interactions
        except Exception as e:
            logger.error(f"获取用户交互记录失败: {e}", exc_info=True)
            return []


class RLFeedbackDAO:
    """用户反馈数据访问层"""
    
    @staticmethod
    def save_feedback(session_id: str, user_id: int, item_id: int, item_type: str,
                     feedback_type: str, feedback_value: float = 0.0,
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
            插入的记录ID，失败返回None
        """
        try:
            with get_mysql_session() as session:
                # 计算距离推荐的时间
                time_query = text("""
                    SELECT TIMESTAMPDIFF(SECOND, created_at, NOW()) as time_diff
                    FROM rl_recommend_interaction
                    WHERE session_id = :session_id
                """)
                time_result = session.execute(time_query, {'session_id': session_id}).fetchone()
                time_since_recommend = time_result[0] if time_result else None
                
                query = text("""
                    INSERT INTO rl_user_feedback
                    (session_id, user_id, item_id, item_type, feedback_type,
                     feedback_value, time_since_recommend, extra_data)
                    VALUES (:session_id, :user_id, :item_id, :item_type, :feedback_type,
                            :feedback_value, :time_since_recommend, :extra_data)
                """)
                
                result = session.execute(query, {
                    'session_id': session_id,
                    'user_id': user_id,
                    'item_id': item_id,
                    'item_type': item_type,
                    'feedback_type': feedback_type,
                    'feedback_value': feedback_value,
                    'time_since_recommend': time_since_recommend,
                    'extra_data': json.dumps(extra_data, ensure_ascii=False) if extra_data else None
                })
                
                # 获取插入的ID
                id_query = text("SELECT LAST_INSERT_ID()")
                id_result = session.execute(id_query).fetchone()
                return id_result[0] if id_result else None
        except Exception as e:
            logger.error(f"保存用户反馈失败: {e}", exc_info=True)
            return None
    
    @staticmethod
    def get_feedbacks_by_session(session_id: str) -> List[Dict[str, Any]]:
        """
        获取会话的所有反馈
        
        Args:
            session_id: 会话ID
            
        Returns:
            反馈记录列表
        """
        try:
            with get_mysql_session() as session:
                query = text("""
                    SELECT id, item_id, item_type, feedback_type, feedback_value,
                           feedback_time, time_since_recommend, extra_data
                    FROM rl_user_feedback
                    WHERE session_id = :session_id
                    ORDER BY feedback_time ASC
                """)
                results = session.execute(query, {'session_id': session_id}).fetchall()
                
                feedbacks = []
                for row in results:
                    feedbacks.append({
                        'id': row[0],
                        'item_id': row[1],
                        'item_type': row[2],
                        'feedback_type': row[3],
                        'feedback_value': float(row[4]) if row[4] else 0.0,
                        'feedback_time': row[5],
                        'time_since_recommend': row[6],
                        'extra_data': json.loads(row[7]) if row[7] else None
                    })
                return feedbacks
        except Exception as e:
            logger.error(f"获取会话反馈失败: {e}", exc_info=True)
            return []


