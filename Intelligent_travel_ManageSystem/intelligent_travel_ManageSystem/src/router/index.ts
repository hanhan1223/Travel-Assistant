import { createRouter, createWebHistory } from 'vue-router'
// 关键点：加上 type 关键字
import type { RouteRecordRaw } from 'vue-router' 
import Layout from '@/layout/index.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据概览', icon: 'Odometer' }
      }
    ]
  },
  {
    path: '/ich',
    component: Layout,
    redirect: '/ich/list',
    meta: { title: '非遗项目', icon: 'GoldMedal' },
    children: [
      {
        path: 'list',
        name: 'ICHList',
        component: () => import('@/views/ich/index.vue'),
        meta: { title: '项目列表' }
      }
    ]
  },
  {
    path: '/merchant',
    component: Layout,
    redirect: '/merchant/list',
    meta: { title: '商户管理', icon: 'Shop' },
    children: [
      {
        path: 'list',
        name: 'MerchantList',
        component: () => import('@/views/merchant/index.vue'),
        meta: { title: '商户列表' }
      }
    ]
  },
  {
    path: '/knowledge',
    component: Layout,
    redirect: '/knowledge/list',
    meta: { title: '知识库管理', icon: 'Reading' },
    children: [
      {
        path: 'list',
        name: 'KnowledgeList',
        component: () => import('@/views/knowledge/index.vue'),
        meta: { title: '文档列表' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router