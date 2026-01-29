"""
推荐相关 API 路由
"""
from fastapi import APIRouter, BackgroundTasks
import logging

from app.models.schemas import (
    RecommendRequest, RecommendResponse, UserFeedbackRequest, UserFeedbackResponse
)
from app.services.recommend_service import recommend_service
from app.services.embedding_service import embedding_service
from app.services.rl_service import rl_service
from config.settings import settings

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api", tags=["推荐"])


@router.post("/recommend", response_model=RecommendResponse)
async def recommend(request: RecommendRequest, background_tasks: BackgroundTasks):
    """
    推荐接口（集成强化学习）
    
    根据用户位置、兴趣、天气等因素，推荐非遗项目和商户。
    如果启用RL，会根据用户状态动态调整权重。
    
    推荐因素权重：
    - 距离: 30%
    - 兴趣匹配: 25%
    - 历史行为: 20%
    - 天气适配: 15%
    - 热度/评分: 10%
    """
    try:
        logger.info(f"收到推荐请求：用户ID={request.userId}, 位置=({request.lat}, {request.lng})")
        
        session_id = None
        state_features = None
        action = None
        original_weights = None
        
        # 如果启用RL，进行状态提取和动作选择
        if settings.RL_ENABLED:
            try:
                # 生成会话ID
                session_id = rl_service.generate_session_id()
                
                # 获取用户状态
                user_state = rl_service.get_user_state(request.userId)
                
                # 提取状态特征
                state_features = rl_service.extract_state_features(request, user_state)
                
                # 获取模型参数（用户模型或全局模型）
                model_key = f"user:{request.userId}"
                model_params = rl_service.get_model_params(model_key)
                if model_params is None:
                    model_params = rl_service.get_model_params("global")
                
                # 选择动作（权重组合）
                action = rl_service.select_action(state_features, model_params)
                
                # 如果动作包含权重调整，临时修改推荐服务的权重
                if action and 'weights' in action:
                    original_weights = recommend_service.weights.copy()
                    recommend_service.weights = action['weights']
                    logger.info(f"RL调整权重: {action['weights']}")
            except Exception as e:
                logger.warning(f"RL处理失败，使用默认权重: {e}")
                # RL失败时使用默认权重，不影响推荐
        
        # 执行推荐（原有逻辑不变）
        results = recommend_service.recommend(request)
        
        # 恢复原始权重（如果被修改）
        if original_weights is not None:
            recommend_service.weights = original_weights
        
        # 异步保存交互记录（如果启用RL）
        if settings.RL_ENABLED and session_id and state_features and action:
            background_tasks.add_task(
                rl_service.save_interaction,
                request.userId,
                session_id,
                state_features,
                action,
                results,
                request
            )
        
        # 返回结果（添加session_id用于后续反馈）
        response = RecommendResponse(
            code=200,
            message="success",
            data=results,
            total=len(results)
        )
        
        # 在响应中添加session_id（如果需要，可以通过扩展响应模型实现）
        # 这里先记录到日志
        if session_id:
            logger.info(f"推荐完成，session_id={session_id}, user_id={request.userId}")
        
        return response
        
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


@router.post("/feedback", response_model=UserFeedbackResponse)
async def feedback(feedback_request: UserFeedbackRequest, background_tasks: BackgroundTasks):
    """
    用户反馈接口
    
    记录用户对推荐结果的反馈行为（点击、浏览、访问等），用于强化学习训练。
    
    反馈类型：
    - click: 点击
    - view: 浏览（需要提供feedbackValue作为浏览时长，单位：秒）
    - dismiss: 忽略/关闭
    - favorite: 收藏
    - share: 分享
    - visit: 实际访问
    - purchase: 购买/消费
    """
    try:
        logger.info(f"收到用户反馈：session_id={feedback_request.sessionId}, "
                   f"user_id={feedback_request.userId}, "
                   f"item_id={feedback_request.itemId}, "
                   f"feedback_type={feedback_request.feedbackType}")
        
        # 保存反馈记录
        feedback_id = rl_service.save_feedback(
            session_id=feedback_request.sessionId,
            user_id=feedback_request.userId,
            item_id=feedback_request.itemId,
            item_type=feedback_request.itemType,
            feedback_type=feedback_request.feedbackType,
            feedback_value=feedback_request.feedbackValue or 0.0,
            extra_data=feedback_request.extraData
        )
        
        if feedback_id is None:
            return UserFeedbackResponse(
                code=500,
                message="保存反馈失败",
                data=None
            )
        
        # 异步更新奖励和用户统计
        if settings.RL_ENABLED:
            background_tasks.add_task(
                rl_service.update_reward,
                feedback_request.sessionId
            )
            
            # 更新用户统计
            from app.models.rl_dao import RLUserStateDAO
            clicks = 1 if feedback_request.feedbackType == 'click' else 0
            views = 1 if feedback_request.feedbackType == 'view' else 0
            conversions = 1 if feedback_request.feedbackType in ['visit', 'purchase'] else 0
            
            background_tasks.add_task(
                RLUserStateDAO.update_user_stats,
                feedback_request.userId,
                clicks=clicks,
                views=views,
                conversions=conversions,
                reward=0.0  # 奖励会在update_reward中计算
            )
        
        return UserFeedbackResponse(
            code=200,
            message="success",
            data={"feedback_id": feedback_id}
        )
        
    except Exception as e:
        logger.error(f"反馈接口错误: {e}", exc_info=True)
        return UserFeedbackResponse(
            code=500,
            message=f"反馈失败: {str(e)}",
            data=None
        )