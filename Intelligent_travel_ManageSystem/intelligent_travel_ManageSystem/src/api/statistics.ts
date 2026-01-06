import request from '@/utils/request'

// 获取首页概览数据
export const getOverviewAPI = () => {
  return request({
    url: '/admin/statistics/overview',
    method: 'GET'
  })
}

// 获取流量趋势
export const getTrafficTrendAPI = () => {
  return request({
    url: '/admin/statistics/traffic-trend',
    method: 'GET'
  })
}

// 获取热门项目
export const getHotProjectsAPI = () => {
  return request({
    url: '/admin/statistics/hot-projects',
    method: 'GET'
  })
}

// 获取兴趣分布
export const getInterestDistributionAPI = () => {
  return request({
    url: '/admin/statistics/interest-distribution',
    method: 'GET'
  })
}