import request from '@/utils/request'

// === 系统配置管理 ===

// 获取所有配置（按分组）
export const getAllConfigsAPI = () => {
  return request({
    url: '/system/config/list',
    method: 'GET'
  })
}

// 获取指定分组的配置
export const getConfigsByGroupAPI = (group: string) => {
  return request({
    url: `/system/config/group/${group}`,
    method: 'GET'
  })
}

// 获取配置值（明文）
export const getConfigValueAPI = (key: string) => {
  return request({
    url: `/system/config/value/${key}`,
    method: 'GET'
  })
}

// 更新配置
export const updateConfigAPI = (data: {
  configKey: string
  configValue: string
  description?: string
}) => {
  return request({
    url: '/system/config/update',
    method: 'POST',
    data
  })
}

// 批量更新配置
export const batchUpdateConfigsAPI = (data: Array<{
  configKey: string
  configValue: string
  description?: string
}>) => {
  return request({
    url: '/system/config/batch-update',
    method: 'POST',
    data
  })
}

// 重新加载配置
export const reloadConfigsAPI = () => {
  return request({
    url: '/system/config/reload',
    method: 'POST'
  })
}

// 测试配置连接
export const testConnectionAPI = (key: string) => {
  return request({
    url: `/system/config/test/${key}`,
    method: 'POST'
  })
}
