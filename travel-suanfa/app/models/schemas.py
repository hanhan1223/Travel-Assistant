"""
Pydantic 模型定义
"""
from pydantic import BaseModel, Field
from typing import List, Optional, Any
from datetime import datetime


# ==================== 请求模型 ====================

class RecommendRequest(BaseModel):
    """推荐请求模型"""
    userId: int = Field(..., description="用户ID，用于获取历史行为")
    lat: float = Field(..., description="用户当前纬度")
    lng: float = Field(..., description="用户当前经度")
    interestTags: Optional[List[str]] = Field(None, description="用户兴趣标签")
    weather: Optional[str] = Field(None, description="当前天气（晴/阴/雨等）")
    outdoorSuitable: Optional[bool] = Field(True, description="是否适合户外活动")
    intentVector: Optional[List[float]] = Field(None, description="用户意图向量")
    limit: Optional[int] = Field(10, description="返回数量，默认10")


class EmbeddingRequest(BaseModel):
    """文本向量化请求模型"""
    text: str = Field(..., description="待向量化的文本")
    model: Optional[str] = Field("default", description="使用的模型名称")


# ==================== 响应模型 ====================

class RecommendItem(BaseModel):
    """推荐项模型"""
    id: int
    type: str  # 'project' 或 'merchant'
    name: str
    category: Optional[str] = None
    lat: float
    lng: float
    distance: float  # 距离（公里）
    rating: Optional[float] = None
    score: float  # 最终推荐分数
    reasons: List[str]  # 推荐原因


class RecommendResponse(BaseModel):
    """推荐响应模型"""
    code: int = 200
    message: str = "success"
    data: List[RecommendItem]
    total: int


class EmbeddingResponse(BaseModel):
    """向量化响应模型"""
    code: int = 200
    message: str = "success"
    data: dict


# ==================== 数据库模型 ====================

class IchProject(BaseModel):
    """非遗项目模型"""
    id: int
    name: str
    category: Optional[str]
    city: Optional[str]
    lat: float
    lng: float
    is_indoor: Optional[int]
    open_status: Optional[str]


class Merchant(BaseModel):
    """商户模型"""
    id: int
    name: str
    category: Optional[str]
    project_id: Optional[int]
    lat: float
    lng: float
    rating: Optional[float]
    relevance_score: Optional[float]


class UserBehaviorLog(BaseModel):
    """用户行为日志模型"""
    id: int
    user_id: int
    action: str
    target_type: str
    target_id: int
    created_at: datetime