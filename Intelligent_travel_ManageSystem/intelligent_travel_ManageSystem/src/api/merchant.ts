import request from '@/utils/request'

// === 商户查询 ===

/**
 * 分页查询商户列表 (默认排序)
 * @param data { current, pageSize, name?, category?, projectId? }
 */
export const getMerchantListAPI = (data: any) => {
  return request({
    url: '/merchant/list',
    method: 'POST',
    data
  })
}

/**
 * 根据ID获取商户详情
 */
export const getMerchantByIdAPI = (id: number) => {
  return request({
    url: `/merchant/${id}`,
    method: 'GET'
  })
}

/**
 * 根据非遗项目ID查询关联商户
 */
export const getMerchantsByProjectAPI = (projectId: number) => {
  return request({
    url: `/merchant/project/${projectId}`,
    method: 'GET'
  })
}

/**
 * 根据类别查询商户
 */
export const getMerchantsByCategoryAPI = (category: string) => {
  return request({
    url: `/merchant/category/${category}`,
    method: 'GET'
  })
}

/**
 * 获取高评分商户列表 (用于评分排序)
 * @param limit 获取数量，默认 100 (为了前端分页，我们获取多一点)
 */
export const getTopMerchantsAPI = (limit: number = 100) => {
  return request({
    url: '/merchant/top',
    method: 'GET',
    params: { limit }
  })
}

// === 商户管理 (管理员) ===

export const addMerchantAPI = (data: any) => {
  return request({
    url: '/merchant/add',
    method: 'POST',
    data
  })
}

export const updateMerchantAPI = (id: number, data: any) => {
  return request({
    url: `/merchant/update/${id}`,
    method: 'PUT',
    data
  })
}

export const deleteMerchantAPI = (id: number) => {
  return request({
    url: `/merchant/${id}`,
    method: 'DELETE'
  })
}