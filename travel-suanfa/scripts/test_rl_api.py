"""
RL模块API测试脚本
用于测试RL相关的API接口
"""
import requests
import json
import time
from typing import Dict, Any


class RLAPITester:
    """RL API测试类"""
    
    def __init__(self, base_url: str = "http://localhost:8000"):
        self.base_url = base_url
        self.session_id = None
    
    def test_recommend_api(self, user_id: int = 10001) -> Dict[str, Any]:
        """测试推荐接口"""
        print("=" * 60)
        print("测试推荐接口（集成RL）")
        print("=" * 60)
        
        url = f"{self.base_url}/api/recommend"
        payload = {
            "userId": user_id,
            "lat": 30.6719,
            "lng": 104.0647,
            "interestTags": ["刺绣", "陶瓷"],
            "weather": "晴",
            "outdoorSuitable": True,
            "limit": 10
        }
        
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            
            data = response.json()
            
            print(f"✓ 请求成功")
            print(f"  状态码: {response.status_code}")
            print(f"  返回消息: {data.get('message')}")
            print(f"  推荐数量: {data.get('total', 0)}")
            
            # 检查是否有推荐结果
            if data.get('data') and len(data['data']) > 0:
                print(f"  第一个推荐项: {data['data'][0].get('name')}")
                print(f"  推荐分数: {data['data'][0].get('score')}")
            
            # 注意：session_id目前没有在响应中返回
            # 如果需要，可以修改响应模型添加session_id字段
            print(f"\n  提示: 查看服务器日志可以看到session_id")
            
            return {
                "success": True,
                "data": data
            }
        except requests.exceptions.RequestException as e:
            print(f"✗ 请求失败: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    def test_feedback_api(self, session_id: str, user_id: int = 10001, 
                         item_id: int = 1, item_type: str = "project",
                         feedback_type: str = "click") -> Dict[str, Any]:
        """测试反馈接口"""
        print("\n" + "=" * 60)
        print("测试反馈接口")
        print("=" * 60)
        
        url = f"{self.base_url}/api/feedback"
        payload = {
            "sessionId": session_id,
            "userId": user_id,
            "itemId": item_id,
            "itemType": item_type,
            "feedbackType": feedback_type,
            "feedbackValue": 0.0
        }
        
        try:
            response = requests.post(url, json=payload, timeout=10)
            response.raise_for_status()
            
            data = response.json()
            
            print(f"✓ 请求成功")
            print(f"  状态码: {response.status_code}")
            print(f"  返回消息: {data.get('message')}")
            
            if data.get('data'):
                print(f"  反馈ID: {data['data'].get('feedback_id')}")
            
            return {
                "success": True,
                "data": data
            }
        except requests.exceptions.RequestException as e:
            print(f"✗ 请求失败: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    def test_view_feedback(self, session_id: str, user_id: int = 10001,
                          item_id: int = 1, duration: float = 30.0) -> Dict[str, Any]:
        """测试浏览反馈（带时长）"""
        return self.test_feedback_api(
            session_id=session_id,
            user_id=user_id,
            item_id=item_id,
            item_type="project",
            feedback_type="view",
            feedback_value=duration
        )
    
    def run_full_test(self):
        """运行完整测试流程"""
        print("\n" + "=" * 60)
        print("RL模块API完整测试")
        print("=" * 60 + "\n")
        
        # 1. 测试推荐接口
        recommend_result = self.test_recommend_api()
        
        if not recommend_result["success"]:
            print("\n✗ 推荐接口测试失败，无法继续")
            return
        
        # 2. 模拟session_id（实际应该从推荐接口返回）
        # 这里使用一个测试用的session_id
        test_session_id = "test-session-" + str(int(time.time()))
        print(f"\n使用测试session_id: {test_session_id}")
        
        # 3. 测试点击反馈
        click_result = self.test_feedback_api(
            session_id=test_session_id,
            feedback_type="click"
        )
        
        # 4. 测试浏览反馈
        view_result = self.test_view_feedback(
            session_id=test_session_id,
            duration=45.5
        )
        
        # 5. 测试收藏反馈
        favorite_result = self.test_feedback_api(
            session_id=test_session_id,
            feedback_type="favorite"
        )
        
        # 汇总结果
        print("\n" + "=" * 60)
        print("测试结果汇总")
        print("=" * 60)
        
        results = [
            ("推荐接口", recommend_result["success"]),
            ("点击反馈", click_result["success"]),
            ("浏览反馈", view_result["success"]),
            ("收藏反馈", favorite_result["success"])
        ]
        
        all_passed = True
        for name, success in results:
            status = "✓ 通过" if success else "✗ 失败"
            print(f"{status} - {name}")
            if not success:
                all_passed = False
        
        print("\n" + "=" * 60)
        if all_passed:
            print("✓ 所有API测试通过！")
        else:
            print("✗ 部分API测试失败")
        print("=" * 60 + "\n")
        
        return all_passed


def main():
    """主函数"""
    import sys
    
    # 可以从命令行参数获取base_url
    base_url = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8000"
    
    tester = RLAPITester(base_url=base_url)
    success = tester.run_full_test()
    
    return 0 if success else 1


if __name__ == "__main__":
    import sys
    sys.exit(main())


