import { createApp } from 'vue';
import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import 'vant/lib/index.css';
import App from './App.vue';
import router from './router';
// 1. 引入 Vant 核心库
import Vant from 'vant';
import './style.css';
import 'vant/lib/index.css';
var app = createApp(App);
// 创建 Pinia 实例并添加持久化插件
var pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
app.use(pinia);
app.use(router);
// 3. 注册 Vant 组件库
app.use(Vant);
app.mount('#app');
