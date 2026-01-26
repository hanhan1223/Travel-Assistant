// src/utils/request.ts
import axios from 'axios';
import { showToast } from 'vant';
// ✅ 核心修复：如果环境变量未定义，默认使用 '/api' 触发 Vite 代理
var baseURL = import.meta.env.VITE_API_BASE_URL || '/api';
var service = axios.create({
    baseURL: baseURL,
    timeout: 10000, // 请求超时时间
    withCredentials: true, // ✅ 必须开启，后端使用 Session Cookie 认证
    headers: {
        'Content-Type': 'application/json',
    },
});
// 请求拦截器
service.interceptors.request.use(function (config) {
    // 可以在这里添加 token 逻辑，目前使用 Cookie 认证，无需手动添加 Header
    return config;
}, function (error) {
    return Promise.reject(error);
});
// 响应拦截器
service.interceptors.response.use(function (response) {
    var res = response.data;
    // 根据 API 文档，code === 0 代表成功
    if (res.code === 0) {
        return res.data; // 直接返回 data 部分
    }
    else {
        // 业务错误处理
        showToast(res.message || '请求失败');
        return Promise.reject(new Error(res.message || 'Error'));
    }
}, function (error) {
    var _a, _b;
    console.error('Request Error:', error);
    var msg = ((_b = (_a = error.response) === null || _a === void 0 ? void 0 : _a.data) === null || _b === void 0 ? void 0 : _b.message) || '网络请求异常，请稍后重试';
    showToast(msg);
    return Promise.reject(error);
});
export default service;
