import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

// === 核心修复：参数清洗函数 ===
// 递归移除对象中的 undefined, null, 和空字符串
const cleanParams = (data: any): any => {
  if (data === null || typeof data !== 'object') {
    return data
  }
  // 如果是 FormData，不做处理，直接返回
  if (data instanceof FormData) {
    return data
  }
  
  if (Array.isArray(data)) {
    return data.map(item => cleanParams(item))
  }

  const newData: any = {}
  for (const key in data) {
    const value = data[key]
    // 过滤掉 undefined, null, 和空字符串
    if (value !== undefined && value !== null && value !== '') {
      newData[key] = cleanParams(value)
    }
  }
  return newData
}

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/api', 
  timeout: 10000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // === 核心修复 1：FormData 自动移除 JSON Header ===
    // 必须让浏览器自动生成带 boundary 的 Content-Type
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }

    // === 核心修复 2：自动清洗参数 ===
    // 无论是 params (GET) 还是 data (POST)，都进行清洗
    if (config.params && typeof config.params === 'object') {
      config.params = cleanParams(config.params)
    }
    if (config.data && typeof config.data === 'object') {
      config.data = cleanParams(config.data)
    }
    return config
  },
  (error: any) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    
    // 二进制流直接返回
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return res
    }

    // 兼容后端可能返回 code 为 0 或 200 的情况（根据 API 文档，成功是 code: 0）
    if (res.code === 0 || res.code === 200) {
      return res
    } else {
      // 401: 未登录
      if (res.code === 40100 || res.code === 401) {
        ElMessage.error('登录已过期，请重新登录')
        // 建议添加 redirect 参数以便登录后跳回
        // window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`
        window.location.href = '/login'
        return Promise.reject(new Error('Unauthorized'))
      }

      // 403: 无权限
      if (res.code === 40300 || res.code === 403) {
        ElMessage.error('您没有权限执行此操作')
        return Promise.reject(new Error('Forbidden'))
      }
      
      ElMessage.error(res.message || '系统错误')
      return Promise.reject(new Error(res.message || 'Error'))
    }
  },
  (error: any) => {
    console.error('Request Err:', error)
    let msg = '请求超时或服务器异常'
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        window.location.href = '/login'
        return Promise.reject(error)
      }
      if (status === 403) msg = '无权访问'
      if (status === 404) msg = '请求资源不存在'
      if (status === 500) msg = '服务器内部错误'
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service