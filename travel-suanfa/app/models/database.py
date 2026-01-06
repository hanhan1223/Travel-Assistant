"""
数据库连接模块
"""
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker, Session
from sqlalchemy.pool import QueuePool
import psycopg2
from psycopg2.extras import RealDictCursor
from contextlib import contextmanager
from typing import Optional
import logging

from config.settings import settings

logger = logging.getLogger(__name__)


# MySQL 数据库连接引擎
mysql_engine = create_engine(
    f"mysql+pymysql://{settings.MYSQL_USER}:{settings.MYSQL_PASSWORD}@"
    f"{settings.MYSQL_HOST}:{settings.MYSQL_PORT}/{settings.MYSQL_DATABASE}",
    poolclass=QueuePool,
    pool_size=5,
    max_overflow=10,
    pool_pre_ping=True,
    pool_recycle=3600,
    echo=settings.DEBUG
)

# MySQL 会话工厂
MySQLSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=mysql_engine)


@contextmanager
def get_mysql_session() -> Session:
    """获取 MySQL 数据库会话"""
    session = MySQLSessionLocal()
    try:
        yield session
        session.commit()
    except Exception as e:
        session.rollback()
        logger.error(f"MySQL 数据库操作失败: {e}")
        raise
    finally:
        session.close()


class PostgreSQLVectorDB:
    """PostgreSQL (pgvector) 向量数据库连接类"""
    
    def __init__(self):
        self.connection_params = {
            "host": settings.PG_HOST,
            "port": settings.PG_PORT,
            "database": settings.PG_DATABASE,
            "user": settings.PG_USER,
            "password": settings.PG_PASSWORD,
            "cursor_factory": RealDictCursor
        }
    
    @contextmanager
    def get_connection(self):
        """获取 PostgreSQL 连接"""
        conn = None
        try:
            conn = psycopg2.connect(**self.connection_params)
            yield conn
        except Exception as e:
            logger.error(f"PostgreSQL 连接失败: {e}")
            raise
        finally:
            if conn:
                conn.close()
    
    def execute_query(self, query: str, params: Optional[tuple] = None):
        """执行查询"""
        with self.get_connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(query, params or ())
                results = cursor.fetchall()
                return results
    
    def fetch_one(self, query: str, params: Optional[tuple] = None):
        """获取单条记录"""
        with self.get_connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(query, params or ())
                return cursor.fetchone()
    
    def vector_similarity_search(
        self,
        table_name: str,
        vector_column: str,
        query_vector: list,
        limit: int = 10,
        where_conditions: Optional[str] = None
    ) -> list:
        """
        向量相似度搜索
        
        Args:
            table_name: 表名
            vector_column: 向量列名
            query_vector: 查询向量
            limit: 返回结果数量
            where_conditions: 额外的 WHERE 条件
            
        Returns:
            相似度搜索结果列表
        """
        vector_str = "[" + ",".join(map(str, query_vector)) + "]"
        
        query = f"""
            SELECT *, 
                   1 - ({vector_column} <=> %s) as similarity
            FROM {table_name}
            WHERE 1=1
        """
        
        params = [vector_str]
        
        if where_conditions:
            query += f" AND {where_conditions}"
        
        query += f" ORDER BY {vector_column} <=> %s LIMIT {limit}"
        params.append(vector_str)
        
        return self.execute_query(query, params)


# 向量数据库实例
vector_db = PostgreSQLVectorDB()