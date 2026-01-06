"""
向量化服务模块
"""
from typing import List, Optional
import numpy as np
from scipy.spatial.distance import cosine
import logging
import httpx
from asyncio import to_thread

from config.settings import settings
from app.models.database import vector_db

logger = logging.getLogger(__name__)


class EmbeddingService:
    """向量化服务类"""
    
    def __init__(self):
        self.vector_dimension = settings.VECTOR_DIMENSION
    
    def generate_embedding(self, text: str, model: str = "default") -> List[float]:
        """
        生成文本向量（调用外部向量化服务）
        
        Args:
            text: 待向量化的文本
            model: 使用的模型名称（保留参数兼容性，实际不使用）
            
        Returns:
            文本的向量表示
        """
        try:
            logger.info(f"调用外部向量化服务，文本长度：{len(text)}")
            
            # 调用外部向量化接口
            with httpx.Client(timeout=settings.EMBEDDING_TIMEOUT) as client:
                response = client.post(
                    settings.EMBEDDING_SERVICE_URL,
                    json={"text": text}
                )
                response.raise_for_status()
                
                result = response.json()
                
                # 解析响应获取向量（新格式：{"embedding": [...]}）
                vector = result.get("embedding")
                
                if vector and len(vector) == self.vector_dimension:
                    logger.info(f"成功获取 {len(vector)} 维向量")
                    return vector
                elif vector:
                    logger.error(f"向量维度不匹配，期望 {self.vector_dimension}，实际 {len(vector)}")
                    return self._generate_fallback_embedding(text)
                else:
                    logger.error(f"外部服务返回格式错误：{result}")
                    return self._generate_fallback_embedding(text)
                    
        except httpx.TimeoutException:
            logger.error("调用外部向量化服务超时，使用备用方案")
            return self._generate_fallback_embedding(text)
        except httpx.HTTPError as e:
            logger.error(f"调用外部向量化服务失败：{e}")
            return self._generate_fallback_embedding(text)
        except Exception as e:
            logger.error(f"生成向量时发生错误：{e}")
            return self._generate_fallback_embedding(text)
    
    def _generate_fallback_embedding(self, text: str) -> List[float]:
        """
        备用向量生成方法（当外部服务不可用时使用）
        
        Args:
            text: 待向量化的文本
            
        Returns:
            文本的向量表示
        """
        logger.warning("使用备用向量生成方法")
        
        # 基于文本的哈希生成伪随机向量
        vector = []
        for i in range(self.vector_dimension):
            char_code = sum(ord(c) for c in text) if text else 0
            value = (char_code * (i + 1) * 9301 + 49297) % 233280
            vector.append((value / 233280 - 0.5) * 2)
        
        return self._normalize_vector(vector)
    
    def _normalize_vector(self, vector: List[float]) -> List[float]:
        """向量归一化"""
        vec = np.array(vector)
        norm = np.linalg.norm(vec)
        if norm == 0:
            return vector
        return (vec / norm).tolist()
    
    def calculate_similarity(self, vector1: List[float], vector2: List[float]) -> float:
        """
        计算两个向量的余弦相似度
        
        Args:
            vector1: 向量1
            vector2: 向量2
            
        Returns:
            余弦相似度 (0-1)
        """
        try:
            # 1 - 余弦距离 = 余弦相似度
            return 1 - cosine(vector1, vector2)
        except Exception as e:
            logger.error(f"计算向量相似度失败: {e}")
            return 0.0
    
    def search_similar_projects(
        self,
        query_vector: List[float],
        limit: int = 10
    ) -> List[dict]:
        """
        在向量数据库中搜索相似的非遗项目
        
        Args:
            query_vector: 查询向量
            limit: 返回结果数量
            
        Returns:
            相似项目列表
        """
        try:
            results = vector_db.vector_similarity_search(
                table_name="ich_project_vector",
                vector_column="content_vector",
                query_vector=query_vector,
                limit=limit
            )
            return [dict(row) for row in results]
        except Exception as e:
            logger.error(f"搜索相似项目失败: {e}")
            return []
    
    def search_similar_knowledge(
        self,
        query_vector: List[float],
        project_id: Optional[int] = None,
        merchant_id: Optional[int] = None,
        limit: int = 10
    ) -> List[dict]:
        """
        在向量数据库中搜索相似的知识片段（用于 RAG 检索）
        
        Args:
            query_vector: 查询向量
            project_id: 关联的项目ID（可选）
            merchant_id: 关联的商户ID（可选）
            limit: 返回结果数量
            
        Returns:
            相似知识片段列表
        """
        try:
            where_conditions = []
            params = {}
            
            if project_id:
                where_conditions.append("project_id = %(project_id)s")
                params["project_id"] = project_id
            
            if merchant_id:
                where_conditions.append("merchant_id = %(merchant_id)s")
                params["merchant_id"] = merchant_id
            
            where_clause = " AND ".join(where_conditions) if where_conditions else None
            
            results = vector_db.vector_similarity_search(
                table_name="knowledge_chunk_vector",
                vector_column="content_vector",
                query_vector=query_vector,
                limit=limit,
                where_conditions=where_clause
            )
            return [dict(row) for row in results]
        except Exception as e:
            logger.error(f"搜索相似知识失败: {e}")
            return []
    
    def get_user_interest_vector(self, user_id: int) -> Optional[List[float]]:
        """
        获取用户兴趣向量
        
        Args:
            user_id: 用户ID
            
        Returns:
            用户兴趣向量，如果不存在则返回 None
        """
        try:
            query = """
                SELECT user_id, interest_vector 
                FROM user_interest_vector 
                WHERE user_id = %s
            """
            result = vector_db.fetch_one(query, (user_id,))
            
            if result and result.get('interest_vector'):
                # PostgreSQL 的向量类型需要特殊处理
                vector_str = result['interest_vector']
                # 去除方括号并分割
                vector = [float(x) for x in vector_str.strip('[]').split(',')]
                return vector
            
            return None
        except Exception as e:
            logger.error(f"获取用户兴趣向量失败: {e}")
            return None
    
    def update_user_interest_vector(self, user_id: int, interest_vector: List[float]) -> bool:
        """
        更新用户兴趣向量
        
        Args:
            user_id: 用户ID
            interest_vector: 兴趣向量
            
        Returns:
            是否更新成功
        """
        try:
            vector_str = "[" + ",".join(map(str, interest_vector)) + "]"
            
            query = """
                INSERT INTO user_interest_vector (user_id, interest_vector)
                VALUES (%s, %s)
                ON CONFLICT (user_id) 
                DO UPDATE SET 
                    interest_vector = EXCLUDED.interest_vector,
                    updated_at = CURRENT_TIMESTAMP
            """
            
            with vector_db.get_connection() as conn:
                with conn.cursor() as cursor:
                    cursor.execute(query, (user_id, vector_str))
                    conn.commit()
            
            logger.info(f"更新用户 {user_id} 的兴趣向量成功")
            return True
        except Exception as e:
            logger.error(f"更新用户兴趣向量失败: {e}")
            return False
    
    def get_project_vector(self, project_id: int) -> Optional[List[float]]:
        """
        获取项目向量
        
        Args:
            project_id: 项目ID
            
        Returns:
            项目向量，如果不存在则返回 None
        """
        try:
            query = """
                SELECT project_id, content_vector 
                FROM ich_project_vector 
                WHERE project_id = %s
            """
            result = vector_db.fetch_one(query, (project_id,))
            
            if result and result.get('content_vector'):
                vector_str = result['content_vector']
                vector = [float(x) for x in vector_str.strip('[]').split(',')]
                return vector
            
            return None
        except Exception as e:
            logger.error(f"获取项目向量失败: {e}")
            return None


# 全局向量化服务实例
embedding_service = EmbeddingService()