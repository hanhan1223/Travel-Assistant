<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
// 引入所有需要的图标
import { Odometer, Reading, GoldMedal, Shop, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 页面加载时获取用户信息（保持登录状态）
onMounted(() => {
  if (!userStore.userInfo) {
    userStore.getUserInfo().catch(() => {
      // 如果获取失败（如未登录），可以在这里处理，request.ts 也会拦截
    })
  }
})

// 退出登录处理函数
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    // 1. 调用 Store 的登出方法 (包含 API 调用和状态清除)
    await userStore.logout()
    
    // 2. 提示成功
    ElMessage.success('已安全退出')
    
    // 3. 跳转回登录页
    router.replace('/login')
  }).catch(() => {
    // 取消退出，不做任何操作
  })
}
</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="200px" class="aside">
        <el-menu 
          router 
          :default-active="$route.path" 
          class="el-menu-vertical"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <div class="logo">
            <img src="/Tradition.svg" alt="" style="height:30px; vertical-align: middle; margin-right: 8px;" />
            非遗伴游后台
          </div>

          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>数据概览</span>
          </el-menu-item>

          <el-sub-menu index="/ich">
            <template #title>
              <el-icon><GoldMedal /></el-icon>
              <span>非遗项目</span>
            </template>
            <el-menu-item index="/ich/list">项目列表</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/merchant">
            <template #title>
              <el-icon><Shop /></el-icon>
              <span>商户管理</span>
            </template>
            <el-menu-item index="/merchant/list">商户列表</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/knowledge">
            <template #title>
              <el-icon><Reading /></el-icon>
              <span>知识库管理</span>
            </template>
            <el-menu-item index="/knowledge/list">文档列表</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/config">系统配置</el-menu-item>
          </el-sub-menu>

        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header class="header">
          <div class="breadcrumb">非遗文化智能伴游系统 - 管理后台</div>
          <div class="right-menu">
            <span class="username" v-if="userStore.userInfo">
              {{ userStore.userInfo.userName || userStore.userInfo.email }}
            </span>
            <el-button type="danger" link @click="handleLogout">退出登录</el-button>
          </div>
        </el-header>
        
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.common-layout, .el-container { height: 100vh; }
.aside { background-color: #545c64; overflow: hidden; } /* 隐藏侧边栏滚动条 */

.logo { 
  height: 60px; 
  line-height: 60px; 
  text-align: center; 
  font-weight: bold; 
  font-size: 18px; 
  color: #fff; 
  border-bottom: 1px solid #666; 
  background-color: #545c64;
}

.header { 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  border-bottom: 1px solid #eee; 
  background-color: #fff;
  height: 60px;
  padding: 0 20px;
}

.right-menu {
  display: flex;
  align-items: center;
  gap: 15px;
}
.username {
  font-size: 14px;
  color: #606266;
}

.el-menu-vertical {
  border-right: none;
}

:deep(.el-sub-menu__title) {
  color: #fff !important;
}
:deep(.el-sub-menu__title:hover) {
  background-color: #434a50 !important;
}
.el-menu-item:hover {
  background-color: #434a50 !important;
}
</style>