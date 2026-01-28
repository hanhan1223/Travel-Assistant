// src/stores/chatStore.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import http from '../utils/request';
import { showToast } from 'vant';
import { SSEClient, type SSECallback } from '../utils/sse-client';
import { getStaticMapUrl } from '../utils/amap'; 
import type { ChatMessage, ChatHistoryItem, LocationData } from '../types/api';

interface ExtendedMessage extends ChatMessage {
  isLoading?: boolean;
  isThinking?: boolean;
  tempContent?: string;
}

// ✅ 配置打字机效果参数
const TYPING_SPEED = 50; // 打字间隔 (毫秒)
const CHUNK_SIZE = 1;    // 每次渲染多少个字符

export const useChatStore = defineStore('chat', () => {
  // ==================== 状态定义 ====================
  const messages = ref<ExtendedMessage[]>([]);
  const historyList = ref<ChatHistoryItem[]>([]);
  const currentConversationId = ref<number | null>(null);
  const isStreaming = ref(false);
  
  // 缓存环境信息
  const envContext = ref({
    weather: '晴',
    city: '广州市',
    district: '天河区'
  });

  // 位置缓存
  const userLocation = ref<{ lat: number; lng: number }>({ lat: 23.1291, lng: 113.2644 });
  const isLocationInit = ref(false);

  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

  // ==================== 打字机核心逻辑 ====================
  let textBuffer = ''; // 待渲染的文本队列
  let typingTimer: any = null;
  let isTyping = false;

  // 启动打字机循环
  const startTypingLoop = (targetMsg: ExtendedMessage) => {
    if (isTyping) return;
    isTyping = true;

    const loop = () => {
      if (textBuffer.length > 0) {
        const chunk = textBuffer.slice(0, CHUNK_SIZE);
        textBuffer = textBuffer.slice(CHUNK_SIZE);
        targetMsg.content += chunk;
        typingTimer = setTimeout(loop, TYPING_SPEED);
      } else {
        if (!isStreaming.value) {
          isTyping = false;
          clearTimeout(typingTimer);
          targetMsg.isLoading = false; 
        } else {
          typingTimer = setTimeout(loop, 100); 
        }
      }
    };
    loop();
  };

  // ==================== 辅助函数 ====================
  // (extractLocationsFromText, initLocation, generateLocalWelcome, ensureHistoryItem 保持不变)
  // ... 为了篇幅，这里复用之前的辅助函数逻辑 ...
  
  // ⚠️ 这里简单补全一下辅助函数，确保代码完整性
  const initLocation = async () => {
    if (isLocationInit.value) return userLocation.value;
    return new Promise<{lat: number, lng: number}>((resolve) => {
      if (!navigator.geolocation) {
        isLocationInit.value = true;
        resolve(userLocation.value);
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          userLocation.value = { lat: pos.coords.latitude, lng: pos.coords.longitude };
          isLocationInit.value = true;
          resolve(userLocation.value);
        },
        (err) => {
          console.warn('定位失败，使用默认坐标', err);
          isLocationInit.value = true;
          resolve(userLocation.value);
        },
        { timeout: 5000, enableHighAccuracy: true }
      );
    });
  };

  const generateLocalWelcome = () => {
    return `您好！我是您的非遗文化智能伴游助手。检测到您当前位于${envContext.value.city}。今天天气${envContext.value.weather}，非常适合探索周边的非遗文化！`;
  };

  const ensureHistoryItem = (id: number, title: string) => {
    const existingItem = historyList.value.find(item => item.id == id);
    if (existingItem) {
      existingItem.title = title;
    } else {
      historyList.value.unshift({
        id,
        userId: 0, 
        title: title,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      });
    }
  };

  // ==================== 核心功能 Action ====================

  const initChat = async () => {
    try {
      await initLocation();
      const { lat, lng } = userLocation.value;
      const res = await http.post<any>('/chat/init', { lat, lng });
      const data = (res as any).data || res;
      if (data) {
        if (data.conversationId) currentConversationId.value = Number(data.conversationId);
        if (data.envContext) {
          envContext.value = {
            weather: data.envContext.weather || '',
            city: data.envContext.city || '',
            district: data.envContext.district || ''
          };
        }
        if (messages.value.length === 0) {
          messages.value = [{
            id: Date.now().toString(),
            role: 'assistant',
            content: data.welcomeMessage || generateLocalWelcome(),
            createdAt: new Date().toISOString(),
            type: 'text'
          }];
        }
      }
    } catch (error) {
      if (messages.value.length === 0) {
        messages.value = [{
          id: Date.now().toString(),
          role: 'assistant',
          content: generateLocalWelcome(),
          createdAt: new Date().toISOString(),
          type: 'text'
        }];
      }
    }
  };

  const resetChat = () => {
    currentConversationId.value = null;
    messages.value = [{
      id: Date.now().toString(),
      role: 'assistant',
      content: generateLocalWelcome(),
      createdAt: new Date().toISOString(),
      type: 'text'
    }];
  };

  const updateConversationTitle = async (id: number, newTitle: string, silent = false) => {
    ensureHistoryItem(id, newTitle);
    try {
      await http.put(`/chat/conversation/${id}/title`, { title: newTitle });
      if (!silent) showToast('修改成功');
      return true;
    } catch (error) {
      return false;
    }
  };

  const fetchHistory = async () => {
    try {
      const res: any = await http.post('/chat/conversations', { current: 1, pageSize: 20 });
      const remoteRecords = res.records || [];
      if (currentConversationId.value) {
        const localItem = historyList.value.find(i => i.id == currentConversationId.value);
        const remoteItem = remoteRecords.find((i: any) => i.id == currentConversationId.value);
        if (localItem && remoteItem && localItem.title && localItem.title !== '新会话') {
           if (!remoteItem.title || remoteItem.title === '新会话') {
              remoteItem.title = localItem.title; 
           }
        }
      }
      historyList.value = remoteRecords;
    } catch (e) { console.error(e); }
  };

  const loadHistory = async (id: string | number) => {
    try {
      const res: any = await http.get(`/chat/history/${id}`);
      currentConversationId.value = Number(id);
      messages.value = (res || []).map((msg: any) => {
        // 处理位置数据
        if (msg.locations && msg.locations.length > 0) {
           msg.locations = msg.locations.map((loc: any) => ({
             ...loc,
             mapImageUrl: getStaticMapUrl(loc.lat, loc.lng), 
             images: loc.images || []
           }));
        }
        
        // 处理用户上传的图片（从 toolCall 中提取）
        let tempContent = undefined;
        if (msg.role === 'user' && msg.toolCall) {
          try {
            const toolData = JSON.parse(msg.toolCall);
            if (toolData.type === 'image' && toolData.url) {
              tempContent = toolData.url;
            }
          } catch (e) {
            console.warn('解析 toolCall 失败:', e);
          }
        }
        
        return {
          ...msg,
          type: (msg.locations && msg.locations.length > 0) ? 'location' : 'text',
          tempContent // 用户上传的图片 URL
        };
      });
    } catch (e) { console.error(e); }
  };

  const sendMessage = async (content: string) => {
    messages.value.push({
      id: Date.now().toString(),
      role: 'user',
      content,
      createdAt: new Date().toISOString(),
      type: 'text'
    });
    
    isStreaming.value = true;
    textBuffer = ''; 
    isTyping = false;
    if (typingTimer) clearTimeout(typingTimer);

    const assistantMsg = ref<ExtendedMessage>({
      id: (Date.now() + 1).toString(),
      role: 'assistant',
      content: '', 
      createdAt: new Date().toISOString(),
      type: 'text',
      isLoading: true,
      isThinking: true, // 🟡 初始为思考中
      locations: []
    });
    messages.value.push(assistantMsg.value);

    const isFirstUserMessage = messages.value.filter(m => m.role === 'user').length === 1;
    let hasUpdatedTitle = false;
    if (isFirstUserMessage && currentConversationId.value) {
      const cleanContent = content.trim();
      const autoTitle = cleanContent.length > 15 ? cleanContent.slice(0, 15) + '...' : cleanContent;
      updateConversationTitle(currentConversationId.value, autoTitle, true);
      hasUpdatedTitle = true;
    }

    const { lat, lng } = userLocation.value;
    const sse = new SSEClient(`${API_BASE_URL}/chat/send`);
    
    try {
      await sse.connect({
        message: content,
        conversationId: currentConversationId.value,
        lat,
        lng
      }, (event: SSECallback) => {
        
        if (event.event === 'conversationId') {
          const newId = Number(event.data);
          currentConversationId.value = newId;
          if (isFirstUserMessage && !hasUpdatedTitle) {
             const cleanContent = content.trim();
             const autoTitle = cleanContent.length > 15 ? cleanContent.slice(0, 15) + '...' : cleanContent;
             updateConversationTitle(newId, autoTitle, true);
             hasUpdatedTitle = true;
          } else {
             const exists = historyList.value.some(i => i.id == newId);
             if (!exists) ensureHistoryItem(newId, '新会话');
          }
        }

        else if (event.event === 'status') {
          if (event.data === 'thinking') {
            assistantMsg.value.isThinking = true;
          } else if (event.data === 'answering') {
            // 注意：这里不要急着关 isThinking，等真正的数据来了再关会更平滑，
            // 或者保留此处逻辑也没问题，因为 answering 通常紧接着就是 message
            assistantMsg.value.isThinking = false;
            startTypingLoop(assistantMsg.value);
          }
        } 
        
        else if (event.event === 'error') {
          textBuffer += '\n[抱歉，遇到了一些问题，请稍后再试]';
          handleStreamEnd();
        } 
        
        // --- 核心消息处理 ---
        else if (event.event === 'message') {
          const rawData = event.data;
          
          // ✨✨✨ 关键修复：拦截 start 消息 ✨✨✨
          // 如果是 "start" 类型，说明会话刚建立，还没有具体内容
          // 此时必须【保持 isThinking = true】，不要进入打字机逻辑
          if (typeof rawData === 'object' && rawData?.type === 'start') {
             if (rawData.conversationId) {
                const newId = Number(rawData.conversationId);
                currentConversationId.value = newId;
                
                // 补全标题逻辑（双保险）
                if (isFirstUserMessage && !hasUpdatedTitle) {
                   const cleanContent = content.trim();
                   const autoTitle = cleanContent.length > 15 ? cleanContent.slice(0, 15) + '...' : cleanContent;
                   updateConversationTitle(newId, autoTitle, true);
                   hasUpdatedTitle = true;
                } else {
                   const exists = historyList.value.some(i => i.id == newId);
                   if (!exists) ensureHistoryItem(newId, '新会话');
                }
             }
             // ⚡️ 核心：直接返回，不做任何状态变更，保持思考动画
             return;
          }

          // 走到这里说明是真正的文本或内容了，关闭思考，开始打字
          assistantMsg.value.isThinking = false;
          startTypingLoop(assistantMsg.value);

          if (typeof rawData === 'object' && rawData !== null) {
            if (rawData.type === 'text') {
              const text = rawData.content || '';
              textBuffer += text;
            } 
            else if (rawData.type === 'location') {
              const backendLocations = (rawData.locations || []).map((item: any) => ({
                 name: item.name,
                 address: item.address,
                 lat: item.lat,
                 lng: item.lng,
                 mapImageUrl: getStaticMapUrl(item.lat, item.lng),
                 images: item.images || []
              }));
              assistantMsg.value.locations = [...(assistantMsg.value.locations || []), ...backendLocations];
              assistantMsg.value.type = 'location';
            }
          } else {
             const text = String(rawData).replace(/^"|"$/g, '').replace(/\\n/g, '\n');
             if (text) textBuffer += text;
          }
        } 
        
        else if (event.event === 'done') {
          handleStreamEnd();
        }
      });
    } catch (err) {
      console.error(err);
      textBuffer += '\n[网络连接异常]';
      handleStreamEnd();
    }

    function handleStreamEnd() {
      isStreaming.value = false;
    }
  };

  const deleteConversation = async (id: number) => {
    try {
      await http.delete(`/chat/conversation/${id}`);
      showToast('删除成功');
      historyList.value = historyList.value.filter(item => item.id !== id);
      if (currentConversationId.value === id) resetChat();
      return true;
    } catch (error) { return false; }
  };

  const sendImageMessage = async (file: File, caption?: string) => {
    const tempUrl = URL.createObjectURL(file);
    messages.value.push({
      id: Date.now().toString(),
      role: 'user',
      content: caption || '【发送了图片】',
      type: 'image', 
      tempContent: tempUrl
    } as any);

    isStreaming.value = true;
    textBuffer = ''; 
    
    const assistantMsg = ref<ExtendedMessage>({
      id: (Date.now() + 1).toString(),
      role: 'assistant',
      content: '',
      createdAt: new Date().toISOString(),
      type: 'text',
      isLoading: true,
      isThinking: true,
      locations: []
    });
    messages.value.push(assistantMsg.value);

    const { lat, lng } = userLocation.value;
    const formData = new FormData();
    formData.append('file', file);
    if (currentConversationId.value) {
      formData.append('conversationId', currentConversationId.value.toString());
    }
    if (caption) formData.append('message', caption);
    formData.append('lat', lat.toString());
    formData.append('lng', lng.toString());

    const sse = new SSEClient(`${API_BASE_URL}/chat/send/image`);
    
    try {
      await sse.connect(formData, (event: SSECallback) => {
        if (event.event === 'conversationId') {
          currentConversationId.value = Number(event.data);
        }
        else if (event.event === 'status') {
           if (event.data === 'thinking') assistantMsg.value.isThinking = true;
           else if (event.data === 'answering') {
             assistantMsg.value.isThinking = false;
             startTypingLoop(assistantMsg.value);
           }
        }
        else if (event.event === 'message') {
           const rawData = event.data;
           
           // ✨✨✨ 修复点：同样拦截 start ✨✨✨
           if (typeof rawData === 'object' && rawData?.type === 'start') {
              if (rawData.conversationId) currentConversationId.value = Number(rawData.conversationId);
              return;
           }

           assistantMsg.value.isThinking = false;
           startTypingLoop(assistantMsg.value);
           
           if (typeof rawData === 'object' && rawData?.type === 'text') {
             textBuffer += rawData.content;
           } else if (rawData?.type === 'location') {
             const backendLocations = (rawData.locations || []).map((item: any) => ({
                 name: item.name, address: item.address, lat: item.lat, lng: item.lng,
                 mapImageUrl: getStaticMapUrl(item.lat, item.lng), images: item.images || []
              }));
              assistantMsg.value.locations = [...(assistantMsg.value.locations || []), ...backendLocations];
              assistantMsg.value.type = 'location';
           }
        }
        else if (event.event === 'done') {
          isStreaming.value = false;
        }
      });
    } catch (err) {
      console.error(err);
      textBuffer += '\n[图片分析失败]';
      isStreaming.value = false;
    }
  };

  return {
    messages, historyList, currentConversationId, isStreaming, envContext, userLocation,
    initChat, resetChat, sendMessage, fetchHistory, loadHistory, deleteConversation, updateConversationTitle, initLocation, sendImageMessage
  };
});