import request from '@/utils/request'

// 登录
export const loginAPI = (data: { email: string; code?: string; password?: string }) => {
  return request({
    url: '/user/login/email',
    method: 'post',
    data
  })
}

// 获取用户信息
export const getUserInfoAPI = () => {
  return request({
    url: '/user/get/login',
    method: 'get'
  })
}

// 退出登录
export const logoutAPI = () => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}