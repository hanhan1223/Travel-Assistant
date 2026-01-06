// src/api/file.ts
import request from '@/utils/request'

// 通用文件上传 (对应文档 POST /file/test/upload)
export const uploadFileAPI = (data: FormData) => {
  return request({
    url: '/file/test/upload',
    method: 'POST',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}