"""
推荐相关 API 路由
"""
from fastapi import APIRouter
import logging

from app.models.schemas import RecommendRequest, RecommendResponse
from app.services.recommend_service import recommend_service
from app.services.embedding_service import embedding_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api", tags=["推荐"])


@router.post("/recommend", response_model=RecommendResponse)
async def recommend(request: RecommendRequest):
    """
    推荐接口
    
    根据用户位置、兴趣、天气等因素，推荐非遗项目和商户。
    
    推荐因素权重：
    - 距离: 30%
    - 兴趣匹配: 25%
    - 历史行为: 20%
    - 天气适配: 15%
    - 热度/评分: 10%
    """
    try:
        logger.info(f"收到推荐请求：用户ID={request.userId}, 位置=({request.lat}, {request.lng})")
        
        # 执行推荐
        results = recommend_service.recommend(request)
        
        return RecommendResponse(
            code=200,
            message="success",
            data=results,
            total=len(results)
        )
    except Exception as e:
        logger.error(f"推荐接口错误: {e}", exc_info=True)
        return RecommendResponse(
            code=500,
            message=f"推荐失败: {str(e)}",
            data=[],
            total=0
        )


@router.post("/embedding")
async def embedding(text: str, model: str = "default"):
    """
    文本向量化接口
    
    将文本转换为向量，用于语义相似度计算。
    向量维度为 1024 维。
    """
    try:
        logger.info(f"收到向量化请求：文本长度={len(text)}, 模型={model}")
        
        # 生成向量
        vector = embedding_service.generate_embedding(text, model)
        
        return {
            "code": 200,
            "message": "success",
            "data": {
                "vector": vector,
                "dimension": len(vector),
                "model": model
            }
        }
    except Exception as e:
        logger.error(f"向量化接口错误: {e}", exc_info=True)
        return {
            "code": 500,
            "message": f"向量化失败: {str(e)}",
            "data": None
        }