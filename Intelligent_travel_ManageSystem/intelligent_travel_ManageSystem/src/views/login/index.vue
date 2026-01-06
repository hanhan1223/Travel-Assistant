<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import * as THREE from 'three'
// 由于 vanta 没有官方的 TS 类型定义，这里忽略类型检查
// @ts-ignore
import CLOUDS from 'vanta/dist/vanta.clouds.min'

const userStore = useUserStore()
const router = useRouter()
const form = ref({ email: '', password: '' })
const loading = ref(false)

// Vanta.js 相关引用
const vantaRef = ref<HTMLElement | null>(null)
let vantaEffect: any = null

// 登录逻辑
const handleLogin = async () => {
  if (!form.value.email || !form.value.password) {
    return ElMessage.warning('请输入邮箱和密码')
  }
  loading.value = true
  try {
    await userStore.login(form.value)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 生命周期：挂载时启动背景特效
onMounted(() => {
  vantaEffect = CLOUDS({
    el: vantaRef.value,
    THREE: THREE, // 必需：将 three.js 实例传递给 vanta
    mouseControls: true,
    touchControls: true,
    gyroControls: false,
    minHeight: 200.00,
    minWidth: 200.00,
    skyColor: 0x68b8d7,  // 天空颜色，你可以自己修改 hex 值
    cloudColor: 0xadc1de, // 云朵颜色
    cloudShadowColor: 0x183550, // 云影颜色
    sunColor: 0xff9919,   // 太阳颜色
    sunGlareColor: 0xff6633,
    sunlightColor: 0xff9933,
    speed: 1.0 // 动画速度
  })
})

// 生命周期：卸载前销毁特效，防止内存泄漏
onBeforeUnmount(() => {
  if (vantaEffect) {
    vantaEffect.destroy()
  }
})
</script>

<template>
  <div class="login-container" ref="vantaRef">
    <el-card class="login-card">
      <h2 class="title">非遗伴游管理系统</h2>
      <el-form :model="form" size="large">
        <el-form-item>
          <el-input v-model="form.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item>
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码" 
            show-password 
            @keyup.enter="handleLogin" 
          />
        </el-form-item>
        <el-button type="primary" class="w-full" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container { 
  height: 100vh; 
  width: 100%;
  display: flex; 
  justify-content: center; 
  align-items: center; 
  overflow: hidden; 
}

.login-card { 
  width: 400px; 
  /* 核心修改：背景颜色透明度调低 (0.2) */
  background: rgba(255, 255, 255, 0.2); 
  
  /* 毛玻璃模糊效果，数字越大越模糊 */
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px); /* 兼容 Safari */
  
  border-radius: 16px; /* 圆角稍微大一点更柔和 */
  
  /* 阴影调淡，更轻盈 */
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
  
  /* 边框增加半透明白线，增强立体感 */
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.title { 
  text-align: center; 
  margin-bottom: 30px; 
  /* 标题颜色稍微深一点，防止背景太亮看不清 */
  color: #2c3e50;
  font-weight: 600;
  font-size: 24px;
}

.w-full { 
  width: 100%; 
}

/* 额外优化：让输入框也稍微半透明（可选，配合整体风格） */
:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.6) !important;
  box-shadow: none !important;
}
:deep(.el-input__wrapper.is-focus) {
  background-color: rgba(255, 255, 255, 0.9) !important;
  box-shadow: 0 0 0 1px #409eff !important;
}
</style>