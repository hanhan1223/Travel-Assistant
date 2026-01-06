"""
推荐接口测试
"""
import pytest
from app.models.schemas import RecommendRequest
from app.services.recommend_service import recommend_service


def test_recommend_service():
    """测试推荐服务"""
    request = RecommendRequest(
        userId=10001,
        lat=30.6719,
        lng=104.0647,
        interestTags=["刺绣", "陶瓷"],
        weather="晴",
        outdoorSuitable=True,
        limit=10
    )
    
    # 执行推荐
    results = recommend_service.recommend(request)
    
    # 验证结果
    assert isinstance(results, list)
    print(f"推荐结果数量: {len(results)}")
    for item in results:
        print(f"- {item.name} (分数: {item.score:.2f}), 距离: {item.distance:.2f}km")
        print(f"  推荐原因: {', '.join(item.reasons)}")


if __name__ == "__main__":
    test_recommend_service()