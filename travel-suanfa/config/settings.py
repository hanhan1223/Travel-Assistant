"""
配置文件模块
"""
from pydantic_settings import BaseSettings
from typing import Optional, Dict, Any


class Settings(BaseSettings):
    """应用配置类"""
    
    # 应用基础配置
    APP_NAME: str = "Python算法推荐服务"
    APP_VERSION: str = "1.0.0"
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    DEBUG: bool = True
    
    # MySQL 数据库配置
    MYSQL_HOST: str = "localhost"
    MYSQL_PORT: int = 3306
    MYSQL_DATABASE: str = "Travel"
    MYSQL_USER: str = "root"
    MYSQL_PASSWORD: str = ""
    
    # PostgreSQL (pgvector) 配置
    PG_HOST: str = "localhost"
    PG_PORT: int = 5433
    PG_DATABASE: str = "Travel"
    PG_USER: str = "admin"
    PG_PASSWORD: str = ""
    
    # 向量维度配置
    VECTOR_DIMENSION: int = 1024
    
    # 外部向量化服务配置
    EMBEDDING_SERVICE_URL: str = "http://localhost:8080/api/embeding"
    EMBEDDING_TIMEOUT: int = 30  # 超时时间（秒）
    
    # 推荐算法权重配置
    WEIGHT_DISTANCE: float = 0.30  # 距离权重 30%
    WEIGHT_INTEREST: float = 0.25  # 兴趣匹配权重 25%
    WEIGHT_HISTORY: float = 0.20  # 历史行为权重 20%
    WEIGHT_WEATHER: float = 0.15  # 天气适配权重 15%
    WEIGHT_RATING: float = 0.10  # 热度评分权重 10%
    
    # 距离计算配置
    MAX_DISTANCE_KM: float = 50.0  # 最大推荐距离(公里)
    
    # 天气适配配置
    WEATHER_ADJUSTMENT: Dict[str, Dict[str, float]] = {
        "晴": {"outdoor_boost": 0.10, "indoor_boost": 0.0},
        "多云": {"outdoor_boost": 0.10, "indoor_boost": 0.0},
        "阴": {"outdoor_boost": 0.0, "indoor_boost": 0.0},
        "小雨": {"outdoor_boost": -0.20, "indoor_boost": 0.20},
        "中雨": {"outdoor_boost": -0.30, "indoor_boost": 0.30},
        "大雨": {"outdoor_boost": -0.5, "indoor_boost": 0.5},
        "暴雨": {"outdoor_boost": -1.0, "indoor_boost": 1.0},  # 只推荐室内
    }
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


# 全局配置实例
settings = Settings()