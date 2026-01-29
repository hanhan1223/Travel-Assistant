"""
应用层缓存管理器
用于缓存用户状态和模型参数，减少数据库查询
"""
import time
import logging
from typing import Optional, Dict, Any, Tuple
from threading import Lock

logger = logging.getLogger(__name__)


class CacheManager:
    """缓存管理器（线程安全）"""
    
    def __init__(self):
        self._user_state_cache: Dict[int, Tuple[Any, float]] = {}  # {user_id: (state, timestamp)}
        self._model_params_cache: Dict[str, Tuple[Any, float]] = {}  # {model_key: (params, timestamp)}
        self._lock = Lock()
        
        # 缓存过期时间（秒）
        self.cache_ttl = {
            'user_state': 600,  # 10分钟
            'model_params': 1800  # 30分钟
        }
    
    def get_user_state(self, user_id: int) -> Optional[Any]:
        """
        从缓存获取用户状态
        
        Args:
            user_id: 用户ID
            
        Returns:
            用户状态对象，如果缓存未命中或已过期返回None
        """
        with self._lock:
            if user_id not in self._user_state_cache:
                return None
            
            state, timestamp = self._user_state_cache[user_id]
            if time.time() - timestamp > self.cache_ttl['user_state']:
                # 缓存已过期，删除
                del self._user_state_cache[user_id]
                return None
            
            return state
    
    def set_user_state(self, user_id: int, state: Any) -> None:
        """
        缓存用户状态
        
        Args:
            user_id: 用户ID
            state: 用户状态对象
        """
        with self._lock:
            self._user_state_cache[user_id] = (state, time.time())
    
    def invalidate_user_state(self, user_id: int) -> None:
        """使指定用户的缓存失效"""
        with self._lock:
            if user_id in self._user_state_cache:
                del self._user_state_cache[user_id]
    
    def get_model_params(self, model_key: str) -> Optional[Any]:
        """
        从缓存获取模型参数
        
        Args:
            model_key: 模型标识
            
        Returns:
            模型参数对象，如果缓存未命中或已过期返回None
        """
        with self._lock:
            if model_key not in self._model_params_cache:
                return None
            
            params, timestamp = self._model_params_cache[model_key]
            if time.time() - timestamp > self.cache_ttl['model_params']:
                # 缓存已过期，删除
                del self._model_params_cache[model_key]
                return None
            
            return params
    
    def set_model_params(self, model_key: str, params: Any) -> None:
        """
        缓存模型参数
        
        Args:
            model_key: 模型标识
            params: 模型参数对象
        """
        with self._lock:
            self._model_params_cache[model_key] = (params, time.time())
    
    def invalidate_model_params(self, model_key: str) -> None:
        """使指定模型的缓存失效"""
        with self._lock:
            if model_key in self._model_params_cache:
                del self._model_params_cache[model_key]
    
    def clear_all(self) -> None:
        """清空所有缓存"""
        with self._lock:
            self._user_state_cache.clear()
            self._model_params_cache.clear()
    
    def get_cache_stats(self) -> Dict[str, Any]:
        """获取缓存统计信息"""
        with self._lock:
            return {
                'user_state_count': len(self._user_state_cache),
                'model_params_count': len(self._model_params_cache),
                'user_state_ttl': self.cache_ttl['user_state'],
                'model_params_ttl': self.cache_ttl['model_params']
            }


# 全局缓存管理器实例
cache_manager = CacheManager()


