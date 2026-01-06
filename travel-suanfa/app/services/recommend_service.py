"""
推荐算法核心逻辑
"""
import math
from typing import List, Dict, Optional
import logging
from sqlalchemy import text

from config.settings import settings
from app.models.schemas import RecommendRequest, RecommendItem, IchProject, Merchant
from app.models.database import get_mysql_session
from app.services.embedding_service import embedding_service

logger = logging.getLogger(__name__)


class RecommendService:
    """推荐服务类"""
    
    def __init__(self):
        self.weights = {
            'distance': settings.WEIGHT_DISTANCE,
            'interest': settings.WEIGHT_INTEREST,
            'history': settings.WEIGHT_HISTORY,
            'weather': settings.WEIGHT_WEATHER,
            'rating': settings.WEIGHT_RATING,
        }
    
    def recommend(self, request: RecommendRequest) -> List[RecommendItem]:
        """
        执行推荐
        
        Args:
            request: 推荐请求
            
        Returns:
            推荐结果列表
        """
        try:
            # 1. 获取候选项目（非遗项目 + 商户）
            projects = self._get_projects(request)
            merchants = self._get_merchants(request, projects)
            
            # 2. 获取用户历史行为
            user_history = self._get_user_behavior_history(request.userId)
            
            # 3. 计算每个候选项目的推荐分数
            scored_items = []
            
            for project in projects:
                item = self._score_project(project, request, user_history)
                if item:
                    scored_items.append(('project', item))
            
            for merchant in merchants:
                item = self._score_merchant(merchant, request, user_history)
                if item:
                    scored_items.append(('merchant', item))
            
            # 4. 按分数排序
            scored_items.sort(key=lambda x: x[1].score, reverse=True)
            
            # 5. 返回前 N 个结果
            results = [item[1] for item in scored_items[:request.limit]]
            
            logger.info(f"为用户 {request.userId} 推荐了 {len(results)} 个项目")
            return results
            
        except Exception as e:
            logger.error(f"推荐失败: {e}", exc_info=True)
            raise
    
    def _get_projects(self, request: RecommendRequest) -> List[IchProject]:
        """获取候选非遗项目"""
        try:
            with get_mysql_session() as session:
                # 获取地理范围内的项目
                max_lat = request.lat + 0.5  # 约等于 50 公里范围
                min_lat = request.lat - 0.5
                max_lng = request.lng + 0.5
                min_lng = request.lng - 0.5
                
                query = text("""
                    SELECT id, name, category, city, lat, lng, is_indoor, open_status
                    FROM ich_project
                    WHERE lat BETWEEN :min_lat AND :max_lat
                    AND lng BETWEEN :min_lng AND :max_lng
                    AND open_status = '1'
                    LIMIT 100
                """)
                results = session.execute(query, {
                    'min_lat': min_lat,
                    'max_lat': max_lat,
                    'min_lng': min_lng,
                    'max_lng': max_lng
                }).fetchall()
                
                projects = []
                for row in results:
                    projects.append(IchProject(
                        id=row[0],
                        name=row[1],
                        category=row[2],
                        city=row[3],
                        lat=float(row[4]),
                        lng=float(row[5]),
                        is_indoor=row[6],
                        open_status=row[7]
                    ))
                
                return projects
                
        except Exception as e:
            logger.error(f"获取非遗项目失败: {e}")
            return []
    
    def _get_merchants(self, request: RecommendRequest, projects: List[IchProject]) -> List[Merchant]:
        """获取候选商户"""
        try:
            with get_mysql_session() as session:
                max_lat = request.lat + 0.5
                min_lat = request.lat - 0.5
                max_lng = request.lng + 0.5
                min_lng = request.lng - 0.5
                
                query = text("""
                    SELECT id, name, category, project_id, lat, lng, rating, relevance_score
                    FROM merchant
                    WHERE lat BETWEEN :min_lat AND :max_lat
                    AND lng BETWEEN :min_lng AND :max_lng
                    LIMIT 100
                """)
                results = session.execute(query, {
                    'min_lat': min_lat,
                    'max_lat': max_lat,
                    'min_lng': min_lng,
                    'max_lng': max_lng
                }).fetchall()
                
                merchants = []
                for row in results:
                    merchants.append(Merchant(
                        id=row[0],
                        name=row[1],
                        category=row[2],
                        project_id=row[3],
                        lat=float(row[4]),
                        lng=float(row[5]),
                        rating=float(row[6]) if row[6] else None,
                        relevance_score=float(row[7]) if row[7] else None
                    ))
                
                return merchants
                
        except Exception as e:
            logger.error(f"获取商户失败: {e}")
            return []
    
    def _get_user_behavior_history(self, user_id: int) -> Dict[str, float]:
        """
        获取用户行为历史
        
        Returns:
            用户关注的类别及其权重
        """
        try:
            with get_mysql_session() as session:
                query = text("""
                    SELECT target_type, target_id, COUNT(*) as count
                    FROM user_behavior_log
                    WHERE user_id = :user_id
                    GROUP BY target_type, target_id
                    ORDER BY count DESC
                    LIMIT 20
                """)
                results = session.execute(query, {'user_id': user_id}).fetchall()
                
                history = {}
                for row in results:
                    target_type = row[0]
                    target_id = row[1]
                    count = row[2]
                    key = f"{target_type}_{target_id}"
                    history[key] = min(count / 10.0, 1.0)  # 归一化到 0-1
                
                return history
                
        except Exception as e:
            logger.error(f"获取用户行为历史失败: {e}")
            return {}
    
    def _calculate_distance(self, lat1: float, lng1: float, lat2: float, lng2: float) -> float:
        """
        使用 Haversine 公式计算两点间的球面距离（公里）
        
        Args:
            lat1, lng1: 点1的经纬度
            lat2, lng2: 点2的经纬度
            
        Returns:
            距离（公里）
        """
        R = 6371  # 地球半径（公里）
        
        # 转换为弧度
        lat1_rad = math.radians(lat1)
        lng1_rad = math.radians(lng1)
        lat2_rad = math.radians(lat2)
        lng2_rad = math.radians(lng2)
        
        # Haversine 公式
        dlat = lat2_rad - lat1_rad
        dlng = lng2_rad - lng1_rad
        
        a = math.sin(dlat / 2) ** 2 + \
            math.cos(lat1_rad) * math.cos(lat2_rad) * math.sin(dlng / 2) ** 2
        c = 2 * math.asin(math.sqrt(a))
        
        distance = R * c
        return distance
    
    def _score_project(self, project: IchProject, request: RecommendRequest, 
                      user_history: Dict[str, float]) -> Optional[RecommendItem]:
        """计算非遗项目的推荐分数"""
        try:
            # 计算距离
            distance = self._calculate_distance(request.lat, request.lng, project.lat, project.lng)
            
            # 超出最大距离则跳过
            if distance > settings.MAX_DISTANCE_KM:
                return None
            
            reasons = []
            total_score = 0.0
            
            # 1. 距离分数 (30%)
            distance_score = max(0, 1 - distance / settings.MAX_DISTANCE_KM)
            total_score += distance_score * self.weights['distance']
            if distance < 5:
                reasons.append(f"距离您仅 {distance:.1f} 公里")
            
            # 2. 兴趣匹配分数 (25%)
            interest_score = 0.0
            if request.interestTags and project.category:
                # 简单的类别匹配
                for tag in request.interestTags:
                    if tag in project.category:
                        interest_score = 0.8
                        reasons.append(f"符合您的兴趣：{tag}")
                        break
            
            # 如果有向量，计算语义相似度
            if request.intentVector:
                project_vector = embedding_service.get_project_vector(project.id)
                if project_vector:
                    similarity = embedding_service.calculate_similarity(
                        request.intentVector, project_vector
                    )
                    interest_score = max(interest_score, similarity)
                    if similarity > 0.7:
                        reasons.append("根据您的偏好推荐")
            
            # 获取用户兴趣向量进行匹配
            user_interest_vector = embedding_service.get_user_interest_vector(request.userId)
            if user_interest_vector:
                project_vector = embedding_service.get_project_vector(project.id)
                if project_vector:
                    similarity = embedding_service.calculate_similarity(
                        user_interest_vector, project_vector
                    )
                    interest_score = max(interest_score, similarity)
                    if similarity > 0.6:
                        reasons.append("根据您的历史偏好推荐")
            
            total_score += interest_score * self.weights['interest']
            
            # 3. 历史行为分数 (20%)
            history_score = user_history.get(f"project_{project.id}", 0.0)
            total_score += history_score * self.weights['history']
            if history_score > 0.5:
                reasons.append("您之前浏览过类似项目")
            
            # 4. 天气适配分数 (15%)
            weather_score = self._calculate_weather_score(project.is_indoor, request)
            total_score += weather_score * self.weights['weather']
            if weather_score > 0.7:
                reasons.append("当前天气适宜")
            
            # 5. 热度分数 (10%) - 作为补充
            rating_score = 0.5  # 默认中等热度
            total_score += rating_score * self.weights['rating']
            
            return RecommendItem(
                id=project.id,
                type='project',
                name=project.name,
                category=project.category,
                lat=project.lat,
                lng=project.lng,
                distance=distance,
                rating=None,
                score=total_score,
                reasons=reasons
            )
            
        except Exception as e:
            logger.error(f"计算项目分数失败: {e}")
            return None
    
    def _score_merchant(self, merchant: Merchant, request: RecommendRequest,
                       user_history: Dict[str, float]) -> Optional[RecommendItem]:
        """计算商户的推荐分数"""
        try:
            distance = self._calculate_distance(request.lat, request.lng, merchant.lat, merchant.lng)
            
            if distance > settings.MAX_DISTANCE_KM:
                return None
            
            reasons = []
            total_score = 0.0
            
            # 1. 距离分数 (30%)
            distance_score = max(0, 1 - distance / settings.MAX_DISTANCE_KM)
            total_score += distance_score * self.weights['distance']
            if distance < 5:
                reasons.append(f"距离您仅 {distance:.1f} 公里")
            
            # 2. 兴趣匹配分数 (25%)
            interest_score = 0.0
            if request.interestTags and merchant.category:
                for tag in request.interestTags:
                    if tag in merchant.category:
                        interest_score = 0.8
                        reasons.append(f"符合您的兴趣：{tag}")
                        break
            
            total_score += interest_score * self.weights['interest']
            
            # 3. 历史行为分数 (20%)
            history_score = user_history.get(f"merchant_{merchant.id}", 0.0)
            total_score += history_score * self.weights['history']
            if history_score > 0.5:
                reasons.append("您之前浏览过该商户")
            
            # 4. 天气适配分数 (15%) - 商户默认可室内
            weather_score = self._calculate_weather_score(1, request)
            total_score += weather_score * self.weights['weather']
            if weather_score > 0.7:
                reasons.append("当前天气适宜")
            
            # 5. 评分分数 (10%)
            rating_score = (merchant.rating / 5.0) if merchant.rating else 0.5
            total_score += rating_score * self.weights['rating']
            if merchant.rating and merchant.rating >= 4.5:
                reasons.append(f"高评分商户 {merchant.rating}分")
            
            return RecommendItem(
                id=merchant.id,
                type='merchant',
                name=merchant.name,
                category=merchant.category,
                lat=merchant.lat,
                lng=merchant.lng,
                distance=distance,
                rating=merchant.rating,
                score=total_score,
                reasons=reasons
            )
            
        except Exception as e:
            logger.error(f"计算商户分数失败: {e}")
            return None
    
    def _calculate_weather_score(self, is_indoor: Optional[int], 
                                request: RecommendRequest) -> float:
        """
        计算天气适配分数
        
        Args:
            is_indoor: 是否为室内项目 (1=室内, 0=户外)
            request: 推荐请求
            
        Returns:
            天气适配分数 (0-1)
        """
        if not request.weather:
            return 0.5  # 没有天气信息，给中等分数
        
        weather = request.weather
        adjustments = settings.WEATHER_ADJUSTMENT
        
        # 获取天气对应的调整值
        weather_config = adjustments.get(weather, adjustments["阴"])
        outdoor_boost = weather_config.get("outdoor_boost", 0.0)
        indoor_boost = weather_config.get("indoor_boost", 0.0)
        
        # 如果不适合户外活动（大雨、暴雨室内项目优先）
        if not request.outdoorSuitable:
            if is_indoor == 1:
                return 1.0
            else:
                return 0.0
        
        # 根据天气和是否室内计算分数
        if is_indoor == 1:  # 室内项目
            score = 0.5 + indoor_boost
            return max(0.0, min(1.0, score))
        else:  # 户外项目
            score = 0.5 + outdoor_boost
            return max(0.0, min(1.0, score))


# 全局推荐服务实例
recommend_service = RecommendService()