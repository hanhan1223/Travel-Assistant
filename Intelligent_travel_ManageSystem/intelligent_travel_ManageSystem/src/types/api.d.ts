// API 响应基础类型
export interface BaseResponse<T = any> {
  code: number
  data: T
  message: string
}

// 系统配置相关类型
export interface ConfigItem {
  configKey: string
  configValue: string
  configGroup: string
  description: string
  encrypted: boolean
  updatedAt: string
}

export interface ConfigUpdateRequest {
  configKey: string
  configValue: string
  description?: string
}

export interface ConfigTestResult {
  success: boolean
  message: string
  details?: any
}

// 分页相关类型
export interface PageRequest {
  current: number
  pageSize: number
}

export interface PageResponse<T> {
  records: T[]
  total: number
  current: number
  size: number
}
