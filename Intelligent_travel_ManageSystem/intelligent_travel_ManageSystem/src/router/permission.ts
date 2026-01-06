import router from './index'
import { useUserStore } from '@/store/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login']

router.beforeEach(async (to, _from, next) => {
  NProgress.start()
  const userStore = useUserStore()
  
  // 简单的鉴权逻辑：检查用户信息是否存在
  // 注意：实际项目中通常判断 Token (Cookies)，这里假设 userInfo 为空即未登录
  // 如果是刷新页面，userInfo 会丢失，所以通常会尝试调用 getUserInfo 恢复
  
  if (userStore.userInfo) {
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else {
      next()
    }
  } else {
    // 没有用户信息，尝试获取（用于刷新页面保持登录）
    // 注意：这里需要配合 request.ts 的 401 拦截
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      try {
        // 尝试拉取用户信息
        await userStore.getUserInfo()
        next() // 获取成功，继续
      } catch (error) {
        // 获取失败（Cookie过期或未登录），去登录页
        next(`/login?redirect=${to.path}`)
        NProgress.done()
      }
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})