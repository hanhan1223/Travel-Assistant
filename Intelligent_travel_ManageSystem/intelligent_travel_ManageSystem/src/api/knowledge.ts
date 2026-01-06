// src/api/knowledge.ts
import request from '@/utils/request'

// 上传知识库文档
export const uploadKnowledgeAPI = (data: FormData) => {
  return request({
    url: '/knowledge/upload',
    method: 'POST',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取知识库列表
export const getKnowledgeListAPI = (data: any) => {
  return request({
    url: '/knowledge/list',
    method: 'POST',
    data
  })
}

// 删除文档
export const deleteKnowledgeAPI = (id: number) => {
  return request({
    url: `/knowledge/${id}`,
    method: 'DELETE'
  })
}

// === 新增部分 ===

// 重新向量化
export const revectorizeKnowledgeAPI = (id: number) => {
  return request({
    url: `/knowledge/revectorize/${id}`,
    method: 'POST'
  })
}

// 更新文档
export const updateKnowledgeAPI = (id: number, data: FormData) => {
  return request({
    url: `/knowledge/update/${id}`,
    method: 'PUT',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}