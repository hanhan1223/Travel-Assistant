"""
推荐结果缓存服务
"""
import hashlib
import json
import logging
from typing import Optional, List
from datetime import datetime, timedelta

logger = logging.getLogger(__name__)


class RecommendCache:
    """推荐结果缓存（内存缓存，可替换为Redis）"""
    
    def __init__(self, ttl_minutes: int = 10):
        self._cache = {}  # {cache_key: (result, expire_time)}
        self.ttl_minutes = ttl_minutes
    
    def _generate_key(self, user_id: int, lat: float, lng: float, 
                     interest_tags: Optional[List[str]], weather: Optional[str]) -> str:
        """生成缓存键"""
        # 位置精度降低到小数点后2位（约1km精度）
        lat_rounded = round(lat, 2)
        lng_rounded = round(lng, 2)
        
        # 兴趣标签排序后拼接
        tags_str = ",".join(sorted(interest_tags)) if interest_tags else ""
        
        # 组合键
        key_parts = [
            str(user_id),
            f"{lat_rounded},{lng_rounded}",
            tags_str,
            weather or ""
        ]
        key_string = "|".join(key_parts)
        
        # MD5哈希
        return hashlib.md5(key_string.encode()).hexdigest()
    
    def get(self, user_id: int, lat: float, lng: float,
            interest_tags: Optional[List[str]], weather: Optional[str]) -> Optional[List]:
        """获取缓存结果"""
        key = self._generate_key(user_id, lat, lng, interest_tags, weather)
        
        if key in self._cache:
            result, expire_time = self._cache[key]
            
            # 检查是否过期
            if datetime.now() < expire_time:
                logger.info(f"缓存命中: {key}")
                return result
            else:
                # 删除过期缓存
                del self._cache[key]
                logger.info(f"缓存过期: {key}")
        
        return None
    
    def set(self, user_id: int, lat: float, lng: float,
            interest_tags: Optional[List[str]], weather: Optional[str],
            result: List):
        """设置缓存"""
        key = self._generate_key(user_id, lat, lng, interest_tags, weather)
        expire_time = datetime.now() + timedelta(minutes=self.ttl_minutes)
        
        self._cache[key] = (result, expire_time)
        logger.info(f"缓存设置: {key}, 过期时间: {expire_time}")
        
        # 清理过期缓存（简单实现）
        self._cleanup_expired()
    
    def _cleanup_expired(self):
        """清理过期缓存"""
        now = datetime.now()
        expired_keys = [
            key for key, (_, expire_time) in self._cache.items()
            if now >= expire_time
        ]
        
        for key in expired_keys:
            del self._cache[key]
        
        if expired_keys:
            logger.info(f"清理了 {len(expired_keys)} 个过期缓存")
    
    def clear(self):
        """清空所有缓存"""
        self._cache.clear()
        logger.info("缓存已清空")


# 全局缓存实例
recommend_cache = RecommendCache(ttl_minutes=10)
