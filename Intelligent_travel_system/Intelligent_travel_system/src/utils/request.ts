/**
 * Axios 请求封装 - 性能优化版
 */
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { showToast } from 'vant'

// ✅ 创建请求实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000, // 30秒超时
  headers: {
    'Content-Type': 'application/json',
  },
})

// ✅ 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // ✅ 添加请求时间戳（用于性能监控）
    config.metadata = { startTime: Date.now() }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// ✅ 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    // ✅ 计算请求耗时
    const duration = Date.now() - (response.config.metadata?.startTime || 0)
    if (duration > 3000) {
      console.warn(`慢请求: ${response.config.url} 耗时 ${duration}ms`)
    }
    
    return response.data
  },
  (error) => {
    // 错误处理
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          showToast('请先登录')
          // 跳转到登录页
          window.location.href = '/login'
          break
        case 403:
          showToast('没有权限')
          break
        case 404:
          showToast('请求的资源不存在')
          break
        case 500:
          showToast('服务器错误')
          break
        default:
          showToast(data?.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      showToast('请求超时，请检查网络')
    } else {
      showToast('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

// ✅ 请求防抖/节流工具
const pendingRequests = new Map<string, AbortController>()

/**
 * 取消重复请求
 */
export function cancelDuplicateRequest(config: AxiosRequestConfig) {
  const requestKey = `${config.method}_${config.url}_${JSON.stringify(config.params || {})}`
  
  // 如果有相同的请求正在进行，取消它
  if (pendingRequests.has(requestKey)) {
    const controller = pendingRequests.get(requestKey)
    controller?.abort()
    pendingRequests.delete(requestKey)
  }
  
  // 创建新的 AbortController
  const controller = new AbortController()
  config.signal = controller.signal
  pendingRequests.set(requestKey, controller)
  
  return () => {
    pendingRequests.delete(requestKey)
  }
}

// ✅ 请求缓存（用于GET请求）
const requestCache = new Map<string, { data: any; timestamp: number }>()
const CACHE_TTL = 5 * 60 * 1000 // 5分钟缓存

/**
 * 带缓存的GET请求
 */
export async function cachedGet<T = any>(
  url: string,
  params?: any,
  ttl: number = CACHE_TTL
): Promise<T> {
  const cacheKey = `${url}_${JSON.stringify(params || {})}`
  
  // 检查缓存
  const cached = requestCache.get(cacheKey)
  if (cached && Date.now() - cached.timestamp < ttl) {
    console.log(`使用缓存: ${url}`)
    return cached.data
  }
  
  // 发起请求
  const data = await request.get<any, T>(url, { params })
  
  // 存入缓存
  requestCache.set(cacheKey, {
    data,
    timestamp: Date.now(),
  })
  
  return data
}

/**
 * 清除缓存
 */
export function clearCache(url?: string) {
  if (url) {
    // 清除特定URL的缓存
    for (const key of requestCache.keys()) {
      if (key.startsWith(url)) {
        requestCache.delete(key)
      }
    }
  } else {
    // 清除所有缓存
    requestCache.clear()
  }
}

export default request
