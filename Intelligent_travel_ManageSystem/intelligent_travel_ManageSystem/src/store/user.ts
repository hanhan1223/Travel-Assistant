import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginAPI, getUserInfoAPI, logoutAPI } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<any>(null)

  // 登录动作
  const login = async (loginForm: any) => {
    try {
      const res = await loginAPI(loginForm)
      // 登录成功后，立即获取一次用户信息填入状态
      userInfo.value = res.data
      return res
    } catch (error) {
      throw error
    }
  }

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const res = await getUserInfoAPI()
      userInfo.value = res.data
      return res.data
    } catch (error) {
      // 获取失败（如未登录），清空状态
      userInfo.value = null
      throw error
    }
  }

  // 登出
  const logout = async () => {
    try {
      // 1. 调用后端接口注销 Session
      await logoutAPI()
    } catch (error) {
      console.warn('Logout API failed:', error)
    } finally {
      // 2. 无论后端是否成功，前端都必须清除状态
      userInfo.value = null
      // 清除可能存在的本地存储辅助字段
      localStorage.removeItem('userInfo')
    }
  }

  // 强制重置状态（用于 401 拦截器）
  const resetState = () => {
    userInfo.value = null
    localStorage.removeItem('userInfo')
  }

  return { 
    userInfo, 
    login, 
    getUserInfo, 
    logout,
    resetState
  }
})