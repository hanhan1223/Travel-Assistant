// src/stores/userStore.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import http from '../utils/request';
import type { UserInfo, LoginRequest, DocumentItem, UpdatePasswordRequest } from '../types/api';
import { showToast } from 'vant';

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null);
  const documentList = ref<DocumentItem[]>([]);

  // 发送验证码
  const sendCode = async (email: string) => {
    try {
      await http.post('/user/register/send-code', { email });
      showToast('验证码已发送');
      return true;
    } catch (error) {
      console.error('发送验证码失败:', error);
      return false;
    }
  };

  // 注册
  const register = async (payload: { email: string; code: string; password?: string }) => {
    try {
      const requestBody = {
        email: payload.email,
        code: payload.code,
        userPassword: payload.password,
        checkPassword: payload.password,
        userName: `用户${payload.email.split('@')[0]}`,
        userAvatar: undefined 
      };
      await http.post('/user/register/email', requestBody);
      showToast('注册成功，请登录');
      return true;
    } catch (error) {
      console.error('注册失败:', error);
      return false;
    }
  };

  // 登录
  const login = async (payload: LoginRequest) => {
    try {
      const res = await http.post<UserInfo>('/user/login/email', payload) as unknown as UserInfo;
      userInfo.value = res; 
      showToast('登录成功');
      return true;
    } catch (error) {
      console.error('登录失败:', error);
      return false;
    }
  };

  // 获取用户信息
  const fetchUserInfo = async () => {
    try {
      const res = await http.get<UserInfo>('/user/get/login') as unknown as UserInfo;
      if (res && res.id) {
        userInfo.value = res;
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
      // 如果获取失败，清空本地存储的用户信息
      userInfo.value = null;
    }
  };

  // 退出登录
  const logout = async () => {
    try {
      await http.post('/user/logout');
    } catch (e) {
      console.error(e);
    } finally {
      userInfo.value = null;
      documentList.value = [];
      showToast('已退出登录');
    }
  };

  // 上传文件
  const uploadFile = async (file: File) => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await http.post<string>('/file/test/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      }) as unknown as string;
      
      return res;
    } catch (error) {
      console.error('上传失败:', error);
      return '';
    }
  };

  // 更新个人信息
  const updateProfile = async (userName: string, userAvatar: string) => {
    try {
      await http.post('/user/update/my', { userName, userAvatar });
      showToast('更新成功');
      // 更新本地状态
      if (userInfo.value) {
        userInfo.value.userName = userName;
        if (userAvatar) {
          userInfo.value.userAvatar = userAvatar;
        }
      }
      return true;
    } catch (error) {
      console.error('更新失败:', error);
      showToast('更新失败，请重试');
      return false;
    }
  };

  // 修改密码
  const updatePassword = async (payload: UpdatePasswordRequest) => {
    try {
      const requestBody = {
        oldPassword: payload.oldPassword || '',
        newPassword: payload.newPassword || '',
        confirmPassword: payload.confirmPassword || ''
      };

      console.log('Sending update password request:', requestBody); 

      await http.post('/user/update/password', requestBody);

      showToast('密码修改成功，请重新登录');
      
      setTimeout(() => {
        logout(); 
      }, 1500);
      
      return true;
    } catch (error) {
      console.error('修改密码失败:', error);
      return false;
    }
  };

  // 获取文档列表
  const fetchDocuments = async () => {
    try {
      const res = await http.post('/document/my', { current: 1, pageSize: 20 }) as any;
      documentList.value = res.records || [];
    } catch (error) {
      console.error('获取文档列表失败:', error);
    }
  };

  return {
    userInfo,
    documentList,
    sendCode,
    register,
    login,
    fetchUserInfo,
    logout,
    uploadFile,
    updateProfile,
    updatePassword,
    fetchDocuments
  };
}, {
  // 持久化配置
  persist: {
    key: 'travel-user',
    storage: localStorage,
  }
});