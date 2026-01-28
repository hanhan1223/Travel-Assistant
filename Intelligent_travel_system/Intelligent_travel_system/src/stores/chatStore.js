var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g = Object.create((typeof Iterator === "function" ? Iterator : Object).prototype);
    return g.next = verb(0), g["throw"] = verb(1), g["return"] = verb(2), typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
// src/stores/chatStore.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import http from '../utils/request';
import { showToast } from 'vant';
import { SSEClient } from '../utils/sse-client';
import { getStaticMapUrl } from '../utils/amap';
// ✅ 配置打字机效果参数
var TYPING_SPEED = 50; // 打字间隔 (毫秒)
var CHUNK_SIZE = 1; // 每次渲染多少个字符
export var useChatStore = defineStore('chat', function () {
    // ==================== 状态定义 ====================
    var messages = ref([]);
    var historyList = ref([]);
    var currentConversationId = ref(null);
    var isStreaming = ref(false);
    // 缓存环境信息
    var envContext = ref({
        weather: '晴',
        city: '广州市',
        district: '天河区'
    });
    // 位置缓存
    var userLocation = ref({ lat: 23.1291, lng: 113.2644 });
    var isLocationInit = ref(false);
    var API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
    // ==================== 打字机核心逻辑 ====================
    var textBuffer = ''; // 待渲染的文本队列
    var typingTimer = null;
    var isTyping = false;
    // 启动打字机循环
    var startTypingLoop = function (targetMsg) {
        if (isTyping)
            return;
        isTyping = true;
        var loop = function () {
            if (textBuffer.length > 0) {
                var chunk = textBuffer.slice(0, CHUNK_SIZE);
                textBuffer = textBuffer.slice(CHUNK_SIZE);
                targetMsg.content += chunk;
                typingTimer = setTimeout(loop, TYPING_SPEED);
            }
            else {
                if (!isStreaming.value) {
                    isTyping = false;
                    clearTimeout(typingTimer);
                    targetMsg.isLoading = false;
                }
                else {
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
    var initLocation = function () { return __awaiter(void 0, void 0, void 0, function () {
        return __generator(this, function (_a) {
            if (isLocationInit.value)
                return [2 /*return*/, userLocation.value];
            return [2 /*return*/, new Promise(function (resolve) {
                    if (!navigator.geolocation) {
                        isLocationInit.value = true;
                        resolve(userLocation.value);
                        return;
                    }
                    navigator.geolocation.getCurrentPosition(function (pos) {
                        userLocation.value = { lat: pos.coords.latitude, lng: pos.coords.longitude };
                        isLocationInit.value = true;
                        resolve(userLocation.value);
                    }, function (err) {
                        console.warn('定位失败，使用默认坐标', err);
                        isLocationInit.value = true;
                        resolve(userLocation.value);
                    }, { timeout: 5000, enableHighAccuracy: true });
                })];
        });
    }); };
    var generateLocalWelcome = function () {
        return "\u60A8\u597D\uFF01\u6211\u662F\u60A8\u7684\u975E\u9057\u6587\u5316\u667A\u80FD\u4F34\u6E38\u52A9\u624B\u3002\u68C0\u6D4B\u5230\u60A8\u5F53\u524D\u4F4D\u4E8E".concat(envContext.value.city, "\u3002\u4ECA\u5929\u5929\u6C14").concat(envContext.value.weather, "\uFF0C\u975E\u5E38\u9002\u5408\u63A2\u7D22\u5468\u8FB9\u7684\u975E\u9057\u6587\u5316\uFF01");
    };
    var ensureHistoryItem = function (id, title) {
        var existingItem = historyList.value.find(function (item) { return item.id == id; });
        if (existingItem) {
            existingItem.title = title;
        }
        else {
            historyList.value.unshift({
                id: id,
                userId: 0,
                title: title,
                createdAt: new Date().toISOString(),
                updatedAt: new Date().toISOString()
            });
        }
    };
    // ==================== 核心功能 Action ====================
    var initChat = function () { return __awaiter(void 0, void 0, void 0, function () {
        var _a, lat, lng, res, data, error_1;
        return __generator(this, function (_b) {
            switch (_b.label) {
                case 0:
                    _b.trys.push([0, 3, , 4]);
                    return [4 /*yield*/, initLocation()];
                case 1:
                    _b.sent();
                    _a = userLocation.value, lat = _a.lat, lng = _a.lng;
                    return [4 /*yield*/, http.post('/chat/init', { lat: lat, lng: lng })];
                case 2:
                    res = _b.sent();
                    data = res.data || res;
                    if (data) {
                        if (data.conversationId)
                            currentConversationId.value = Number(data.conversationId);
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
                    return [3 /*break*/, 4];
                case 3:
                    error_1 = _b.sent();
                    if (messages.value.length === 0) {
                        messages.value = [{
                                id: Date.now().toString(),
                                role: 'assistant',
                                content: generateLocalWelcome(),
                                createdAt: new Date().toISOString(),
                                type: 'text'
                            }];
                    }
                    return [3 /*break*/, 4];
                case 4: return [2 /*return*/];
            }
        });
    }); };
    var resetChat = function () {
        currentConversationId.value = null;
        messages.value = [{
                id: Date.now().toString(),
                role: 'assistant',
                content: generateLocalWelcome(),
                createdAt: new Date().toISOString(),
                type: 'text'
            }];
    };
    var updateConversationTitle = function (id_1, newTitle_1) {
        var args_1 = [];
        for (var _i = 2; _i < arguments.length; _i++) {
            args_1[_i - 2] = arguments[_i];
        }
        return __awaiter(void 0, __spreadArray([id_1, newTitle_1], args_1, true), void 0, function (id, newTitle, silent) {
            var error_2;
            if (silent === void 0) { silent = false; }
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        ensureHistoryItem(id, newTitle);
                        _a.label = 1;
                    case 1:
                        _a.trys.push([1, 3, , 4]);
                        return [4 /*yield*/, http.put("/chat/conversation/".concat(id, "/title"), { title: newTitle })];
                    case 2:
                        _a.sent();
                        if (!silent)
                            showToast('修改成功');
                        return [2 /*return*/, true];
                    case 3:
                        error_2 = _a.sent();
                        return [2 /*return*/, false];
                    case 4: return [2 /*return*/];
                }
            });
        });
    };
    var fetchHistory = function () { return __awaiter(void 0, void 0, void 0, function () {
        var res, remoteRecords, localItem, remoteItem, e_1;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, , 3]);
                    return [4 /*yield*/, http.post('/chat/conversations', { current: 1, pageSize: 20 })];
                case 1:
                    res = _a.sent();
                    remoteRecords = res.records || [];
                    if (currentConversationId.value) {
                        localItem = historyList.value.find(function (i) { return i.id == currentConversationId.value; });
                        remoteItem = remoteRecords.find(function (i) { return i.id == currentConversationId.value; });
                        if (localItem && remoteItem && localItem.title && localItem.title !== '新会话') {
                            if (!remoteItem.title || remoteItem.title === '新会话') {
                                remoteItem.title = localItem.title;
                            }
                        }
                    }
                    historyList.value = remoteRecords;
                    return [3 /*break*/, 3];
                case 2:
                    e_1 = _a.sent();
                    console.error(e_1);
                    return [3 /*break*/, 3];
                case 3: return [2 /*return*/];
            }
        });
    }); };
    var loadHistory = function (id) { return __awaiter(void 0, void 0, void 0, function () {
        var res, e_2;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, , 3]);
                    return [4 /*yield*/, http.get("/chat/history/".concat(id))];
                case 1:
                    res = _a.sent();
                    currentConversationId.value = Number(id);
                    messages.value = (res || []).map(function (msg) {
                        // 处理位置数据
                        if (msg.locations && msg.locations.length > 0) {
                            msg.locations = msg.locations.map(function (loc) { return (__assign(__assign({}, loc), { mapImageUrl: getStaticMapUrl(loc.lat, loc.lng), images: loc.images || [] })); });
                        }
                        // 处理用户上传的图片（从 toolCall 中提取）
                        var tempContent = undefined;
                        if (msg.role === 'user' && msg.toolCall) {
                            try {
                                var toolData = JSON.parse(msg.toolCall);
                                if (toolData.type === 'image' && toolData.url) {
                                    tempContent = toolData.url;
                                }
                            }
                            catch (e) {
                                console.warn('解析 toolCall 失败:', e);
                            }
                        }
                        return __assign(__assign({}, msg), { type: (msg.locations && msg.locations.length > 0) ? 'location' : 'text', tempContent: tempContent // 用户上传的图片 URL
                         });
                    });
                    return [3 /*break*/, 3];
                case 2:
                    e_2 = _a.sent();
                    console.error(e_2);
                    return [3 /*break*/, 3];
                case 3: return [2 /*return*/];
            }
        });
    }); };
    var sendMessage = function (content) { return __awaiter(void 0, void 0, void 0, function () {
        function handleStreamEnd() {
            isStreaming.value = false;
        }
        var assistantMsg, isFirstUserMessage, hasUpdatedTitle, cleanContent, autoTitle, _a, lat, lng, sse, err_1;
        return __generator(this, function (_b) {
            switch (_b.label) {
                case 0:
                    messages.value.push({
                        id: Date.now().toString(),
                        role: 'user',
                        content: content,
                        createdAt: new Date().toISOString(),
                        type: 'text'
                    });
                    isStreaming.value = true;
                    textBuffer = '';
                    isTyping = false;
                    if (typingTimer)
                        clearTimeout(typingTimer);
                    assistantMsg = ref({
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
                    isFirstUserMessage = messages.value.filter(function (m) { return m.role === 'user'; }).length === 1;
                    hasUpdatedTitle = false;
                    if (isFirstUserMessage && currentConversationId.value) {
                        cleanContent = content.trim();
                        autoTitle = cleanContent.length > 15 ? cleanContent.slice(0, 15) + '...' : cleanContent;
                        updateConversationTitle(currentConversationId.value, autoTitle, true);
                        hasUpdatedTitle = true;
                    }
                    _a = userLocation.value, lat = _a.lat, lng = _a.lng;
                    sse = new SSEClient("".concat(API_BASE_URL, "/chat/send"));
                    _b.label = 1;
                case 1:
                    _b.trys.push([1, 3, , 4]);
                    return [4 /*yield*/, sse.connect({
                            message: content,
                            conversationId: currentConversationId.value,
                            lat: lat,
                            lng: lng
                        }, function (event) {
                            if (event.event === 'conversationId') {
                                var newId_1 = Number(event.data);
                                currentConversationId.value = newId_1;
                                if (isFirstUserMessage && !hasUpdatedTitle) {
                                    var cleanContent = content.trim();
                                    var autoTitle = cleanContent.length > 15 ? cleanContent.slice(0, 15) + '...' : cleanContent;
                                    updateConversationTitle(newId_1, autoTitle, true);
                                    hasUpdatedTitle = true;
                                }
                                else {
                                    var exists = historyList.value.some(function (i) { return i.id == newId_1; });
                                    if (!exists)
                                        ensureHistoryItem(newId_1, '新会话');
                                }
                            }
                            else if (event.event === 'status') {
                                if (event.data === 'thinking') {
                                    assistantMsg.value.isThinking = true;
                                }
                                else if (event.data === 'answering') {
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
                                var rawData = event.data;
                                // ✨✨✨ 关键修复：拦截 start 消息 ✨✨✨
                                // 如果是 "start" 类型，说明会话刚建立，还没有具体内容
                                // 此时必须【保持 isThinking = true】，不要进入打字机逻辑
                                if (typeof rawData === 'object' && (rawData === null || rawData === void 0 ? void 0 : rawData.type) === 'start') {
                                    if (rawData.conversationId) {
                                        var newId_2 = Number(rawData.conversationId);
                                        currentConversationId.value = newId_2;
                                        // 补全标题逻辑（双保险）
                                        if (isFirstUserMessage && !hasUpdatedTitle) {
                                            var cleanContent = content.trim();
                                            var autoTitle = cleanContent.length > 15 ? cleanContent.slice(0, 15) + '...' : cleanContent;
                                            updateConversationTitle(newId_2, autoTitle, true);
                                            hasUpdatedTitle = true;
                                        }
                                        else {
                                            var exists = historyList.value.some(function (i) { return i.id == newId_2; });
                                            if (!exists)
                                                ensureHistoryItem(newId_2, '新会话');
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
                                        var text = rawData.content || '';
                                        textBuffer += text;
                                    }
                                    else if (rawData.type === 'location') {
                                        var backendLocations = (rawData.locations || []).map(function (item) { return ({
                                            name: item.name,
                                            address: item.address,
                                            lat: item.lat,
                                            lng: item.lng,
                                            mapImageUrl: getStaticMapUrl(item.lat, item.lng),
                                            images: item.images || []
                                        }); });
                                        assistantMsg.value.locations = __spreadArray(__spreadArray([], (assistantMsg.value.locations || []), true), backendLocations, true);
                                        assistantMsg.value.type = 'location';
                                    }
                                }
                                else {
                                    var text = String(rawData).replace(/^"|"$/g, '').replace(/\\n/g, '\n');
                                    if (text)
                                        textBuffer += text;
                                }
                            }
                            else if (event.event === 'done') {
                                handleStreamEnd();
                            }
                        })];
                case 2:
                    _b.sent();
                    return [3 /*break*/, 4];
                case 3:
                    err_1 = _b.sent();
                    console.error(err_1);
                    textBuffer += '\n[网络连接异常]';
                    handleStreamEnd();
                    return [3 /*break*/, 4];
                case 4: return [2 /*return*/];
            }
        });
    }); };
    var deleteConversation = function (id) { return __awaiter(void 0, void 0, void 0, function () {
        var error_3;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, , 3]);
                    return [4 /*yield*/, http.delete("/chat/conversation/".concat(id))];
                case 1:
                    _a.sent();
                    showToast('删除成功');
                    historyList.value = historyList.value.filter(function (item) { return item.id !== id; });
                    if (currentConversationId.value === id)
                        resetChat();
                    return [2 /*return*/, true];
                case 2:
                    error_3 = _a.sent();
                    return [2 /*return*/, false];
                case 3: return [2 /*return*/];
            }
        });
    }); };
    var sendImageMessage = function (file, caption) { return __awaiter(void 0, void 0, void 0, function () {
        var tempUrl, assistantMsg, _a, lat, lng, formData, sse, err_2;
        return __generator(this, function (_b) {
            switch (_b.label) {
                case 0:
                    tempUrl = URL.createObjectURL(file);
                    messages.value.push({
                        id: Date.now().toString(),
                        role: 'user',
                        content: caption || '【发送了图片】',
                        type: 'image',
                        tempContent: tempUrl
                    });
                    isStreaming.value = true;
                    textBuffer = '';
                    assistantMsg = ref({
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
                    _a = userLocation.value, lat = _a.lat, lng = _a.lng;
                    formData = new FormData();
                    formData.append('file', file);
                    if (currentConversationId.value) {
                        formData.append('conversationId', currentConversationId.value.toString());
                    }
                    if (caption)
                        formData.append('message', caption);
                    formData.append('lat', lat.toString());
                    formData.append('lng', lng.toString());
                    sse = new SSEClient("".concat(API_BASE_URL, "/chat/send/image"));
                    _b.label = 1;
                case 1:
                    _b.trys.push([1, 3, , 4]);
                    return [4 /*yield*/, sse.connect(formData, function (event) {
                            if (event.event === 'conversationId') {
                                currentConversationId.value = Number(event.data);
                            }
                            else if (event.event === 'status') {
                                if (event.data === 'thinking')
                                    assistantMsg.value.isThinking = true;
                                else if (event.data === 'answering') {
                                    assistantMsg.value.isThinking = false;
                                    startTypingLoop(assistantMsg.value);
                                }
                            }
                            else if (event.event === 'message') {
                                var rawData = event.data;
                                // ✨✨✨ 修复点：同样拦截 start ✨✨✨
                                if (typeof rawData === 'object' && (rawData === null || rawData === void 0 ? void 0 : rawData.type) === 'start') {
                                    if (rawData.conversationId)
                                        currentConversationId.value = Number(rawData.conversationId);
                                    return;
                                }
                                assistantMsg.value.isThinking = false;
                                startTypingLoop(assistantMsg.value);
                                if (typeof rawData === 'object' && (rawData === null || rawData === void 0 ? void 0 : rawData.type) === 'text') {
                                    textBuffer += rawData.content;
                                }
                                else if ((rawData === null || rawData === void 0 ? void 0 : rawData.type) === 'location') {
                                    var backendLocations = (rawData.locations || []).map(function (item) { return ({
                                        name: item.name, address: item.address, lat: item.lat, lng: item.lng,
                                        mapImageUrl: getStaticMapUrl(item.lat, item.lng), images: item.images || []
                                    }); });
                                    assistantMsg.value.locations = __spreadArray(__spreadArray([], (assistantMsg.value.locations || []), true), backendLocations, true);
                                    assistantMsg.value.type = 'location';
                                }
                            }
                            else if (event.event === 'done') {
                                isStreaming.value = false;
                            }
                        })];
                case 2:
                    _b.sent();
                    return [3 /*break*/, 4];
                case 3:
                    err_2 = _b.sent();
                    console.error(err_2);
                    textBuffer += '\n[图片分析失败]';
                    isStreaming.value = false;
                    return [3 /*break*/, 4];
                case 4: return [2 /*return*/];
            }
        });
    }); };
    return {
        messages: messages,
        historyList: historyList,
        currentConversationId: currentConversationId,
        isStreaming: isStreaming,
        envContext: envContext,
        userLocation: userLocation,
        initChat: initChat,
        resetChat: resetChat,
        sendMessage: sendMessage,
        fetchHistory: fetchHistory,
        loadHistory: loadHistory,
        deleteConversation: deleteConversation,
        updateConversationTitle: updateConversationTitle,
        initLocation: initLocation,
        sendImageMessage: sendImageMessage
    };
});
