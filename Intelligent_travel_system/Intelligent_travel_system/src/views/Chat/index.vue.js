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
var _a, _b, _c;
import { ref, onMounted, nextTick, watch, computed, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useChatStore } from '../../stores/chatStore';
import { useUserStore } from '../../stores/userStore';
import { showConfirmDialog, showToast } from 'vant';
import LocationCard from './LocationCard.vue';
import ProductCard from './ProductCard.vue';
import MarkdownIt from 'markdown-it';
import { XFVoiceClient } from '../../utils/xf-voice';
var route = useRoute();
var router = useRouter();
var chatStore = useChatStore();
var userStore = useUserStore();
var inputContent = ref('');
var chatContainer = ref(null);
var showHistory = ref(false);
var currentConversationId = computed(function () { return chatStore.currentConversationId; });
// 🌟 核心状态：判断用户是否正在向上翻阅历史记录
var isUserScrolling = ref(false);
var isRecording = ref(false);
var showVoicePanel = ref(false);
var fileInput = ref(null);
var selectedImage = ref(null); // 存储选中的图片
var imagePreviewUrl = ref(null); // 图片预览URL
var voiceClient = null;
var title = computed(function () {
    if (route.query.id) {
        var id_1 = Number(route.query.id);
        var item = chatStore.historyList.find(function (i) { return i.id === id_1; });
        return (item === null || item === void 0 ? void 0 : item.title) || '历史回顾';
    }
    if (currentConversationId.value) {
        var item = chatStore.historyList.find(function (i) { return i.id === currentConversationId.value; });
        return (item === null || item === void 0 ? void 0 : item.title) || '非遗伴游';
    }
    return '非遗伴游';
});
var md = new MarkdownIt({
    html: true,
    linkify: true,
    breaks: true
});
var quickActions = [
    '📍 附近推荐',
    '🎨 非遗介绍',
    '🛍️ 文创产品',
    '🗺️ 游览路线',
    '🏺 历史渊源'
];
var w = computed(function () { return (chatStore.envContext.weather || '').toLowerCase(); });
var isRainy = computed(function () { return /雨|rain|shower|drizzle|storm/i.test(w.value); });
var isSunny = computed(function () { return /晴|sunny|clear/i.test(w.value); });
var isCloudy = computed(function () { return /云|阴|cloud|overcast/i.test(w.value); });
var isSnowy = computed(function () { return /雪|snow|blizzard/i.test(w.value); });
var isFoggy = computed(function () { return /雾|fog|mist|haze/i.test(w.value); });
// 图片URL缓存，避免重复计算
var imageUrlCache = new Map();
// 从消息中提取图片URL（带缓存）
var getImageUrl = function (msg) {
    var msgId = msg.id || JSON.stringify(msg);
    // 检查缓存
    if (imageUrlCache.has(msgId)) {
        return imageUrlCache.get(msgId);
    }
    var url = null;
    // 1. 优先从 tempContent 获取（实时上传的图片）
    if (msg.tempContent) {
        url = msg.tempContent;
    }
    // 2. 从 toolCall 字段解析（历史记录）
    else if (msg.toolCall) {
        try {
            var toolData = JSON.parse(msg.toolCall);
            if (toolData.type === 'image' && toolData.url) {
                url = toolData.url;
            }
        }
        catch (e) {
            // 解析失败，继续尝试其他方法
        }
    }
    // 3. 从消息内容中提取图片URL（兼容旧格式）
    else if (msg.content && typeof msg.content === 'string') {
        // 匹配 "图片: https://..." 格式
        var match = msg.content.match(/图片[：:]\s*(https?:\/\/[^\s]+)/);
        if (match && match[1]) {
            url = match[1];
        }
    }
    // 缓存结果
    imageUrlCache.set(msgId, url);
    return url;
};
var renderMessage = function (content, role) {
    if (!content)
        return '';
    // 先处理转义的换行符
    var processedContent = content.replace(/\\n/g, '\n');
    // 移除图片URL行（如果存在），因为图片会单独渲染
    processedContent = processedContent.replace(/图片[：:]\s*https?:\/\/[^\s]+\n?/g, '');
    // 移除 [图片识别] 标签
    processedContent = processedContent.replace(/\[图片识别\]\s*/g, '');
    // 如果处理后内容为空，返回空字符串
    if (!processedContent.trim()) {
        return '';
    }
    // 渲染 Markdown
    var html = md.render(processedContent);
    // 增强图片渲染：添加样式和点击预览功能
    html = html.replace(/<img src="(.*?)" alt="(.*?)"(.*?)>/g, '<img src="$1" alt="$2" class="chat-image rounded-xl my-3 max-w-full h-auto shadow-md border border-gray-200 cursor-pointer hover:shadow-lg transition-all" loading="lazy" onclick="window.previewImage(\'$1\')" />');
    // 处理链接：在新标签页打开
    html = html.replace(/<a href="(.*?)">/g, '<a href="$1" target="_blank" rel="noopener noreferrer">');
    return html;
};
// 图片预览功能
var previewImage = function (url) {
    // 使用 Vant 的 ImagePreview
    import('vant').then(function (_a) {
        var showImagePreview = _a.showImagePreview;
        showImagePreview({
            images: [url],
            closeable: true,
        });
    });
};
// 将预览函数挂载到 window 对象，供 HTML 中的 onclick 调用
if (typeof window !== 'undefined') {
    window.previewImage = previewImage;
}
var formatTime = function (time) {
    var date = new Date(time);
    var isToday = new Date().toDateString() === date.toDateString();
    return isNaN(date.getTime())
        ? ''
        : isToday
            ? date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
            : "".concat(date.getMonth() + 1, "/").concat(date.getDate(), " ").concat(date.getHours(), ":").concat(date.getMinutes().toString().padStart(2, '0'));
};
// 🌟 滚动处理函数：判断用户是否偏离底部
var handleScroll = function () {
    if (!chatContainer.value)
        return;
    var _a = chatContainer.value, scrollTop = _a.scrollTop, scrollHeight = _a.scrollHeight, clientHeight = _a.clientHeight;
    // 如果距离底部超过 100px，则认为用户正在浏览历史
    isUserScrolling.value = scrollHeight - scrollTop - clientHeight > 100;
};
// 防抖定时器
var scrollTimer = null;
// 🌟 智能滚动函数（带防抖）
var scrollToBottom = function (force) {
    if (force === void 0) { force = false; }
    // 清除之前的定时器
    if (scrollTimer) {
        clearTimeout(scrollTimer);
    }
    // 使用防抖，避免频繁滚动导致抖动
    scrollTimer = setTimeout(function () {
        nextTick(function () {
            if (chatContainer.value) {
                // 只有在强制滚动，或者用户当前就在底部附近时，才执行滚动
                if (force || !isUserScrolling.value) {
                    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
                    if (force)
                        isUserScrolling.value = false; // 强制滚动后，重置状态
                }
            }
        });
    }, force ? 0 : 100); // 强制滚动立即执行，否则延迟100ms
};
// 🌟 监听：新消息增加 -> 强制滚动
watch(function () { return chatStore.messages.length; }, function () {
    scrollToBottom(true);
});
// 🌟 监听：消息内容变化（打字机效果）-> 智能滚动（仅在流式传输时）
watch(function () { return chatStore.messages[chatStore.messages.length - 1]; }, function () {
    // 只有在流式传输时才自动滚动，否则用户可能在查看历史消息
    if (chatStore.isStreaming) {
        scrollToBottom(false);
    }
}, { deep: true });
watch(showHistory, function (newVal) {
    if (newVal)
        chatStore.fetchHistory();
});
var initOrLoad = function () { return __awaiter(void 0, void 0, void 0, function () {
    var historyId;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                historyId = route.query.id;
                if (!historyId) return [3 /*break*/, 2];
                return [4 /*yield*/, chatStore.loadHistory(historyId)];
            case 1:
                _a.sent();
                return [3 /*break*/, 4];
            case 2:
                if (!(chatStore.messages.length === 0)) return [3 /*break*/, 4];
                return [4 /*yield*/, chatStore.initChat()];
            case 3:
                _a.sent();
                _a.label = 4;
            case 4:
                scrollToBottom(true); // 初始化强制到底部
                return [2 /*return*/];
        }
    });
}); };
watch(function () { return route.query.id; }, function () { initOrLoad(); });
onMounted(function () { initOrLoad(); });
onUnmounted(function () {
    if (voiceClient)
        voiceClient.stop();
    if (scrollTimer)
        clearTimeout(scrollTimer);
    if (imagePreviewUrl.value)
        URL.revokeObjectURL(imagePreviewUrl.value);
    imageUrlCache.clear(); // 清理缓存
});
var handleBack = function () {
    if (route.query.id)
        router.back();
    else
        showHistory.value = true;
};
var handleQuickAction = function (text) {
    if (chatStore.isStreaming)
        return;
    chatStore.sendMessage(text);
};
var handleSend = function () {
    // 如果有选中的图片，发送图片消息
    if (selectedImage.value) {
        chatStore.sendImageMessage(selectedImage.value, inputContent.value);
        // 清理图片相关状态
        selectedImage.value = null;
        if (imagePreviewUrl.value) {
            URL.revokeObjectURL(imagePreviewUrl.value);
            imagePreviewUrl.value = null;
        }
        inputContent.value = '';
        return;
    }
    // 否则发送普通文本消息
    if (!inputContent.value.trim() || chatStore.isStreaming)
        return;
    chatStore.sendMessage(inputContent.value);
    inputContent.value = '';
};
var triggerImageUpload = function () {
    var _a;
    (_a = fileInput.value) === null || _a === void 0 ? void 0 : _a.click();
};
var cancelImageSelection = function () {
    selectedImage.value = null;
    if (imagePreviewUrl.value) {
        URL.revokeObjectURL(imagePreviewUrl.value);
        imagePreviewUrl.value = null;
    }
    showToast('已取消图片选择');
};
var handleFileChange = function (event) {
    var target = event.target;
    if (target.files && target.files[0]) {
        var file = target.files[0];
        if (file.size > 10 * 1024 * 1024) {
            showToast('图片不能超过 10MB');
            return;
        }
        // 保存选中的图片
        selectedImage.value = file;
        // 创建预览URL
        imagePreviewUrl.value = URL.createObjectURL(file);
        // 提示用户可以输入问题
        showToast('图片已选择，可以输入问题或直接发送');
    }
    if (target.value)
        target.value = '';
};
var toggleVoicePanel = function () {
    showVoicePanel.value = !showVoicePanel.value;
};
var startRecording = function () { return __awaiter(void 0, void 0, void 0, function () {
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                isRecording.value = true;
                if (!voiceClient) {
                    voiceClient = new XFVoiceClient(function (text, isFinal) {
                        inputContent.value += text;
                    }, function (err) {
                        showToast(err);
                        isRecording.value = false;
                    });
                }
                return [4 /*yield*/, voiceClient.start()];
            case 1:
                _a.sent();
                return [2 /*return*/];
        }
    });
}); };
var stopRecording = function () {
    if (voiceClient) {
        voiceClient.stop();
    }
    isRecording.value = false;
};
var touchStart = ref({ x: 0, y: 0 });
var minSwipeDistance = 50;
var handleTouchStart = function (e) {
    touchStart.value = { x: e.touches[0].clientX, y: e.touches[0].clientY };
};
var handleTouchEnd = function (e) {
    var touchEnd = { x: e.changedTouches[0].clientX, y: e.changedTouches[0].clientY };
    var deltaX = touchEnd.x - touchStart.value.x;
    var deltaY = touchEnd.y - touchStart.value.y;
    if (Math.abs(deltaX) > minSwipeDistance && Math.abs(deltaY) < 50) {
        if (deltaX < 0)
            showHistory.value = true;
    }
};
var switchConversation = function (id) { return __awaiter(void 0, void 0, void 0, function () {
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (currentConversationId.value === id) {
                    showHistory.value = false;
                    return [2 /*return*/];
                }
                return [4 /*yield*/, chatStore.loadHistory(id)];
            case 1:
                _a.sent();
                showHistory.value = false;
                if (route.query.id)
                    router.replace({ query: __assign(__assign({}, route.query), { id: id }) });
                return [2 /*return*/];
        }
    });
}); };
var startNewChat = function () {
    chatStore.resetChat();
    showHistory.value = false;
    if (route.query.id)
        router.push('/chat');
};
var confirmDelete = function (id) {
    showConfirmDialog({
        title: '删除会话',
        message: '确定要删除这条会话记录吗？',
    })
        .then(function () {
        chatStore.deleteConversation(id);
    })
        .catch(function () { });
};
var __VLS_ctx = __assign(__assign({}, {}), {});
var __VLS_components;
var __VLS_intrinsics;
var __VLS_directives;
/** @type {__VLS_StyleScopedClasses['no-scrollbar']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant-message']} */ ;
/** @type {__VLS_StyleScopedClasses['user-message']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['user-message']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant-message']} */ ;
/** @type {__VLS_StyleScopedClasses['user-message']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['message-content']} */ ;
/** @type {__VLS_StyleScopedClasses['cloud']} */ ;
/** @type {__VLS_StyleScopedClasses['cloud']} */ ;
/** @type {__VLS_StyleScopedClasses['cloud']} */ ;
/** @type {__VLS_StyleScopedClasses['cloud']} */ ;
/** @type {__VLS_StyleScopedClasses['layer-1']} */ ;
/** @type {__VLS_StyleScopedClasses['layer-2']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign(__assign({ onTouchstart: (__VLS_ctx.handleTouchStart) }, { onTouchend: (__VLS_ctx.handleTouchEnd) }), { class: "flex flex-col h-screen bg-gray-50 relative overflow-hidden font-sans" }));
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
/** @type {__VLS_StyleScopedClasses['h-screen']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
/** @type {__VLS_StyleScopedClasses['relative']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
/** @type {__VLS_StyleScopedClasses['font-sans']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute inset-0 pointer-events-none z-0 overflow-hidden" }));
/** @type {__VLS_StyleScopedClasses['absolute']} */ ;
/** @type {__VLS_StyleScopedClasses['inset-0']} */ ;
/** @type {__VLS_StyleScopedClasses['pointer-events-none']} */ ;
/** @type {__VLS_StyleScopedClasses['z-0']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
if (__VLS_ctx.isRainy) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "weather-layer rain-container" }));
    /** @type {__VLS_StyleScopedClasses['weather-layer']} */ ;
    /** @type {__VLS_StyleScopedClasses['rain-container']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "rain-layer layer-1" }));
    /** @type {__VLS_StyleScopedClasses['rain-layer']} */ ;
    /** @type {__VLS_StyleScopedClasses['layer-1']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "rain-layer layer-2" }));
    /** @type {__VLS_StyleScopedClasses['rain-layer']} */ ;
    /** @type {__VLS_StyleScopedClasses['layer-2']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "rain-overlay" }));
    /** @type {__VLS_StyleScopedClasses['rain-overlay']} */ ;
}
if (__VLS_ctx.isSunny) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "weather-layer sun-container" }));
    /** @type {__VLS_StyleScopedClasses['weather-layer']} */ ;
    /** @type {__VLS_StyleScopedClasses['sun-container']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "sun-beams" }));
    /** @type {__VLS_StyleScopedClasses['sun-beams']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "sun-glow" }));
    /** @type {__VLS_StyleScopedClasses['sun-glow']} */ ;
}
if (__VLS_ctx.isCloudy) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "weather-layer cloud-container" }));
    /** @type {__VLS_StyleScopedClasses['weather-layer']} */ ;
    /** @type {__VLS_StyleScopedClasses['cloud-container']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "cloud x1" }));
    /** @type {__VLS_StyleScopedClasses['cloud']} */ ;
    /** @type {__VLS_StyleScopedClasses['x1']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "cloud x2" }));
    /** @type {__VLS_StyleScopedClasses['cloud']} */ ;
    /** @type {__VLS_StyleScopedClasses['x2']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "cloud x3" }));
    /** @type {__VLS_StyleScopedClasses['cloud']} */ ;
    /** @type {__VLS_StyleScopedClasses['x3']} */ ;
}
if (__VLS_ctx.isSnowy) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "weather-layer snow-container" }));
    /** @type {__VLS_StyleScopedClasses['weather-layer']} */ ;
    /** @type {__VLS_StyleScopedClasses['snow-container']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "snow layer-1" }));
    /** @type {__VLS_StyleScopedClasses['snow']} */ ;
    /** @type {__VLS_StyleScopedClasses['layer-1']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "snow layer-2" }));
    /** @type {__VLS_StyleScopedClasses['snow']} */ ;
    /** @type {__VLS_StyleScopedClasses['layer-2']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "snow layer-3" }));
    /** @type {__VLS_StyleScopedClasses['snow']} */ ;
    /** @type {__VLS_StyleScopedClasses['layer-3']} */ ;
}
if (__VLS_ctx.isFoggy) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "weather-layer fog-container" }));
    /** @type {__VLS_StyleScopedClasses['weather-layer']} */ ;
    /** @type {__VLS_StyleScopedClasses['fog-container']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "fog-img fog-img-first" }));
    /** @type {__VLS_StyleScopedClasses['fog-img']} */ ;
    /** @type {__VLS_StyleScopedClasses['fog-img-first']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "fog-img fog-img-second" }));
    /** @type {__VLS_StyleScopedClasses['fog-img']} */ ;
    /** @type {__VLS_StyleScopedClasses['fog-img-second']} */ ;
}
var __VLS_0;
/** @ts-ignore @type {typeof __VLS_components.vanNavBar | typeof __VLS_components.VanNavBar | typeof __VLS_components.vanNavBar | typeof __VLS_components.VanNavBar} */
vanNavBar;
// @ts-ignore
var __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0(__assign(__assign({ 'onClickLeft': {} }, { title: (__VLS_ctx.title), leftArrow: (!!__VLS_ctx.route.query.id), fixed: true, placeholder: true, zIndex: "50", border: (false) }), { class: "custom-nav relative z-50" })));
var __VLS_2 = __VLS_1.apply(void 0, __spreadArray([__assign(__assign({ 'onClickLeft': {} }, { title: (__VLS_ctx.title), leftArrow: (!!__VLS_ctx.route.query.id), fixed: true, placeholder: true, zIndex: "50", border: (false) }), { class: "custom-nav relative z-50" })], __VLS_functionalComponentArgsRest(__VLS_1), false));
var __VLS_5;
var __VLS_6 = ({ clickLeft: {} },
    { onClickLeft: (__VLS_ctx.handleBack) });
/** @type {__VLS_StyleScopedClasses['custom-nav']} */ ;
/** @type {__VLS_StyleScopedClasses['relative']} */ ;
/** @type {__VLS_StyleScopedClasses['z-50']} */ ;
var __VLS_7 = __VLS_3.slots.default;
if (!__VLS_ctx.route.query.id) {
    {
        var __VLS_8 = __VLS_3.slots.left;
        var __VLS_9 = void 0;
        /** @ts-ignore @type {typeof __VLS_components.vanIcon | typeof __VLS_components.VanIcon} */
        vanIcon;
        // @ts-ignore
        var __VLS_10 = __VLS_asFunctionalComponent1(__VLS_9, new __VLS_9(__assign(__assign({ 'onClick': {} }, { name: "wap-nav", size: "24" }), { class: "text-gray-700" })));
        var __VLS_11 = __VLS_10.apply(void 0, __spreadArray([__assign(__assign({ 'onClick': {} }, { name: "wap-nav", size: "24" }), { class: "text-gray-700" })], __VLS_functionalComponentArgsRest(__VLS_10), false));
        var __VLS_14 = void 0;
        var __VLS_15 = ({ click: {} },
            { onClick: function () {
                    var _a = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        _a[_i] = arguments[_i];
                    }
                    var $event = _a[0];
                    if (!(!__VLS_ctx.route.query.id))
                        return;
                    __VLS_ctx.showHistory = true;
                    // @ts-ignore
                    [handleTouchStart, handleTouchEnd, isRainy, isSunny, isCloudy, isSnowy, isFoggy, title, route, route, handleBack, showHistory,];
                } });
        /** @type {__VLS_StyleScopedClasses['text-gray-700']} */ ;
        var __VLS_12;
        var __VLS_13;
        // @ts-ignore
        [];
    }
}
{
    var __VLS_16 = __VLS_3.slots.right;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ onClick: function () {
            var _a = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                _a[_i] = arguments[_i];
            }
            var $event = _a[0];
            __VLS_ctx.router.push('/user');
            // @ts-ignore
            [router,];
        } }, { class: "flex items-center justify-center w-9 h-9 bg-white/50 backdrop-blur-md rounded-full cursor-pointer hover:bg-white/80 transition-all shadow-sm active:scale-95 overflow-hidden" }));
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-9']} */ ;
    /** @type {__VLS_StyleScopedClasses['h-9']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-white/50']} */ ;
    /** @type {__VLS_StyleScopedClasses['backdrop-blur-md']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['cursor-pointer']} */ ;
    /** @type {__VLS_StyleScopedClasses['hover:bg-white/80']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['active:scale-95']} */ ;
    /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
    if ((_a = __VLS_ctx.userStore.userInfo) === null || _a === void 0 ? void 0 : _a.userAvatar) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.img)(__assign(__assign({ src: (__VLS_ctx.userStore.userInfo.userAvatar) }, { class: "w-full h-full object-cover" }), { alt: "用户" }));
        /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['h-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['object-cover']} */ ;
    }
    else {
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-base" }));
        /** @type {__VLS_StyleScopedClasses['text-base']} */ ;
    }
    // @ts-ignore
    [userStore, userStore,];
}
// @ts-ignore
[];
var __VLS_3;
var __VLS_4;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign(__assign({ onScroll: (__VLS_ctx.handleScroll) }, { class: "flex-1 overflow-y-auto p-4 space-y-6 relative z-10" }), { ref: "chatContainer" }));
/** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-y-auto']} */ ;
/** @type {__VLS_StyleScopedClasses['p-4']} */ ;
/** @type {__VLS_StyleScopedClasses['space-y-6']} */ ;
/** @type {__VLS_StyleScopedClasses['relative']} */ ;
/** @type {__VLS_StyleScopedClasses['z-10']} */ ;
var _loop_1 = function (msg) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ key: (msg.id) }, { class: "flex flex-col" }));
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "text-center text-xs text-gray-400/80 mb-3 scale-90" }));
    /** @type {__VLS_StyleScopedClasses['text-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-400/80']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['scale-90']} */ ;
    (__VLS_ctx.formatTime(msg.createdAt));
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: (['flex', msg.role === 'user' ? 'justify-end' : 'justify-start']) }));
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    if (msg.role === 'assistant') {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center mr-3 flex-shrink-0 border-2 border-white shadow-sm overflow-hidden" }));
        /** @type {__VLS_StyleScopedClasses['w-10']} */ ;
        /** @type {__VLS_StyleScopedClasses['h-10']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['bg-indigo-100']} */ ;
        /** @type {__VLS_StyleScopedClasses['flex']} */ ;
        /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['mr-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['flex-shrink-0']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-2']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-white']} */ ;
        /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
        /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-xl" }));
        /** @type {__VLS_StyleScopedClasses['text-xl']} */ ;
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex flex-col max-w-[85%]" }));
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
    /** @type {__VLS_StyleScopedClasses['max-w-[85%]']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: ([
            'px-4 py-3 rounded-2xl text-[15px] leading-relaxed shadow-sm break-words transition-all',
            msg.role === 'user'
                ? 'bg-gradient-to-br from-indigo-500 to-indigo-600 text-white rounded-tr-sm shadow-indigo-100'
                : 'bg-white/90 backdrop-blur-sm text-gray-800 rounded-tl-sm border border-gray-100 shadow-gray-100'
        ]) }));
    /** @type {__VLS_StyleScopedClasses['px-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-[15px]']} */ ;
    /** @type {__VLS_StyleScopedClasses['leading-relaxed']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['break-words']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
    if (__VLS_ctx.getImageUrl(msg)) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.img)(__assign(__assign(__assign({ onClick: function () {
                var _a = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    _a[_i] = arguments[_i];
                }
                var $event = _a[0];
                if (!(__VLS_ctx.getImageUrl(msg)))
                    return;
                __VLS_ctx.previewImage(__VLS_ctx.getImageUrl(msg));
                // @ts-ignore
                [handleScroll, chatStore, formatTime, getImageUrl, getImageUrl, previewImage,];
            } }, { src: (__VLS_ctx.getImageUrl(msg)) }), { class: "rounded-lg mb-2 max-w-full border border-white/20 cursor-pointer hover:opacity-90 transition-opacity" }), { alt: "发送的图片" }));
        /** @type {__VLS_StyleScopedClasses['rounded-lg']} */ ;
        /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
        /** @type {__VLS_StyleScopedClasses['max-w-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['border']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-white/20']} */ ;
        /** @type {__VLS_StyleScopedClasses['cursor-pointer']} */ ;
        /** @type {__VLS_StyleScopedClasses['hover:opacity-90']} */ ;
        /** @type {__VLS_StyleScopedClasses['transition-opacity']} */ ;
    }
    if (msg.isThinking && !msg.content) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex items-center space-x-1 py-1 h-6" }));
        /** @type {__VLS_StyleScopedClasses['flex']} */ ;
        /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['space-x-1']} */ ;
        /** @type {__VLS_StyleScopedClasses['py-1']} */ ;
        /** @type {__VLS_StyleScopedClasses['h-6']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "typing-dot" }));
        /** @type {__VLS_StyleScopedClasses['typing-dot']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "typing-dot animation-delay-200" }));
        /** @type {__VLS_StyleScopedClasses['typing-dot']} */ ;
        /** @type {__VLS_StyleScopedClasses['animation-delay-200']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "typing-dot animation-delay-400" }));
        /** @type {__VLS_StyleScopedClasses['typing-dot']} */ ;
        /** @type {__VLS_StyleScopedClasses['animation-delay-400']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "ml-2 text-xs text-gray-400" }));
        /** @type {__VLS_StyleScopedClasses['ml-2']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
    }
    else if (msg.content) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: ([
                'message-content markdown-body',
                msg.role === 'user' ? 'user-message' : 'assistant-message'
            ]) }));
        __VLS_asFunctionalDirective(__VLS_directives.vHtml, {})(null, __assign(__assign({}, __VLS_directiveBindingRestFields), { value: (__VLS_ctx.renderMessage(msg.content, msg.role)) }), null, null);
        /** @type {__VLS_StyleScopedClasses['message-content']} */ ;
        /** @type {__VLS_StyleScopedClasses['markdown-body']} */ ;
    }
    if (msg.locations && msg.locations.length > 0) {
        for (var _j = 0, _k = __VLS_vFor((msg.locations)); _j < _k.length; _j++) {
            var _l = _k[_j], loc = _l[0], idx = _l[1];
            var __VLS_17 = LocationCard;
            // @ts-ignore
            var __VLS_18 = __VLS_asFunctionalComponent1(__VLS_17, new __VLS_17(__assign({ key: (idx), data: (loc) }, { class: "mt-3 shadow-md" })));
            var __VLS_19 = __VLS_18.apply(void 0, __spreadArray([__assign({ key: (idx), data: (loc) }, { class: "mt-3 shadow-md" })], __VLS_functionalComponentArgsRest(__VLS_18), false));
            /** @type {__VLS_StyleScopedClasses['mt-3']} */ ;
            /** @type {__VLS_StyleScopedClasses['shadow-md']} */ ;
            // @ts-ignore
            [getImageUrl, renderMessage,];
        }
    }
    else if (msg.type === 'location' && msg.location) {
        var __VLS_22 = LocationCard;
        // @ts-ignore
        var __VLS_23 = __VLS_asFunctionalComponent1(__VLS_22, new __VLS_22(__assign({ data: (msg.location) }, { class: "mt-3 shadow-md" })));
        var __VLS_24 = __VLS_23.apply(void 0, __spreadArray([__assign({ data: (msg.location) }, { class: "mt-3 shadow-md" })], __VLS_functionalComponentArgsRest(__VLS_23), false));
        /** @type {__VLS_StyleScopedClasses['mt-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['shadow-md']} */ ;
    }
    if (msg.type === 'product' && msg.products) {
        for (var _m = 0, _o = __VLS_vFor((msg.products)); _m < _o.length; _m++) {
            var _p = _o[_m], prod = _p[0], idx = _p[1];
            var __VLS_27 = ProductCard;
            // @ts-ignore
            var __VLS_28 = __VLS_asFunctionalComponent1(__VLS_27, new __VLS_27(__assign({ key: (idx), data: (prod) }, { class: "mt-3 shadow-md" })));
            var __VLS_29 = __VLS_28.apply(void 0, __spreadArray([__assign({ key: (idx), data: (prod) }, { class: "mt-3 shadow-md" })], __VLS_functionalComponentArgsRest(__VLS_28), false));
            /** @type {__VLS_StyleScopedClasses['mt-3']} */ ;
            /** @type {__VLS_StyleScopedClasses['shadow-md']} */ ;
            // @ts-ignore
            [];
        }
    }
    if (msg.role === 'user') {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center ml-3 flex-shrink-0 border-2 border-white shadow-sm overflow-hidden" }));
        /** @type {__VLS_StyleScopedClasses['w-10']} */ ;
        /** @type {__VLS_StyleScopedClasses['h-10']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['bg-gray-200']} */ ;
        /** @type {__VLS_StyleScopedClasses['flex']} */ ;
        /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['ml-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['flex-shrink-0']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-2']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-white']} */ ;
        /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
        /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
        if ((_b = __VLS_ctx.userStore.userInfo) === null || _b === void 0 ? void 0 : _b.userAvatar) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.img)(__assign(__assign({ src: (__VLS_ctx.userStore.userInfo.userAvatar) }, { class: "w-full h-full object-cover" }), { alt: "User" }));
            /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
            /** @type {__VLS_StyleScopedClasses['h-full']} */ ;
            /** @type {__VLS_StyleScopedClasses['object-cover']} */ ;
        }
        else {
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-xl" }));
            /** @type {__VLS_StyleScopedClasses['text-xl']} */ ;
        }
    }
    // @ts-ignore
    [userStore, userStore,];
};
for (var _i = 0, _d = __VLS_vFor((__VLS_ctx.chatStore.messages)); _i < _d.length; _i++) {
    var msg = _d[_i][0];
    _loop_1(msg);
}
var __VLS_32;
/** @ts-ignore @type {typeof __VLS_components.vanPopup | typeof __VLS_components.VanPopup | typeof __VLS_components.vanPopup | typeof __VLS_components.VanPopup} */
vanPopup;
// @ts-ignore
var __VLS_33 = __VLS_asFunctionalComponent1(__VLS_32, new __VLS_32(__assign(__assign({ show: (__VLS_ctx.showHistory), position: "right" }, { style: ({ width: '75%', height: '100%' }) }), { class: "bg-gray-50" })));
var __VLS_34 = __VLS_33.apply(void 0, __spreadArray([__assign(__assign({ show: (__VLS_ctx.showHistory), position: "right" }, { style: ({ width: '75%', height: '100%' }) }), { class: "bg-gray-50" })], __VLS_functionalComponentArgsRest(__VLS_33), false));
/** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
var __VLS_37 = __VLS_35.slots.default;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex flex-col h-full" }));
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
/** @type {__VLS_StyleScopedClasses['h-full']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "p-4 bg-white shadow-sm border-b flex justify-between items-center" }));
/** @type {__VLS_StyleScopedClasses['p-4']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
/** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
/** @type {__VLS_StyleScopedClasses['border-b']} */ ;
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['justify-between']} */ ;
/** @type {__VLS_StyleScopedClasses['items-center']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)(__assign({ class: "text-lg font-bold text-gray-800" }));
/** @type {__VLS_StyleScopedClasses['text-lg']} */ ;
/** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
/** @type {__VLS_StyleScopedClasses['text-gray-800']} */ ;
var __VLS_38;
/** @ts-ignore @type {typeof __VLS_components.vanIcon | typeof __VLS_components.VanIcon} */
vanIcon;
// @ts-ignore
var __VLS_39 = __VLS_asFunctionalComponent1(__VLS_38, new __VLS_38(__assign(__assign({ 'onClick': {} }, { name: "cross" }), { class: "text-gray-500" })));
var __VLS_40 = __VLS_39.apply(void 0, __spreadArray([__assign(__assign({ 'onClick': {} }, { name: "cross" }), { class: "text-gray-500" })], __VLS_functionalComponentArgsRest(__VLS_39), false));
var __VLS_43;
var __VLS_44 = ({ click: {} },
    { onClick: function () {
            var _a = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                _a[_i] = arguments[_i];
            }
            var $event = _a[0];
            __VLS_ctx.showHistory = false;
            // @ts-ignore
            [showHistory, showHistory,];
        } });
/** @type {__VLS_StyleScopedClasses['text-gray-500']} */ ;
var __VLS_41;
var __VLS_42;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex-1 overflow-y-auto p-2" }));
/** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-y-auto']} */ ;
/** @type {__VLS_StyleScopedClasses['p-2']} */ ;
if (!((_c = __VLS_ctx.chatStore.historyList) === null || _c === void 0 ? void 0 : _c.length)) {
    var __VLS_45 = void 0;
    /** @ts-ignore @type {typeof __VLS_components.vanEmpty | typeof __VLS_components.VanEmpty} */
    vanEmpty;
    // @ts-ignore
    var __VLS_46 = __VLS_asFunctionalComponent1(__VLS_45, new __VLS_45({
        description: "暂无历史记录",
    }));
    var __VLS_47 = __VLS_46.apply(void 0, __spreadArray([{
            description: "暂无历史记录",
        }], __VLS_functionalComponentArgsRest(__VLS_46), false));
}
var _loop_2 = function (item) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign(__assign({ onClick: function () {
            var _a = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                _a[_i] = arguments[_i];
            }
            var $event = _a[0];
            __VLS_ctx.switchConversation(item.id);
            // @ts-ignore
            [chatStore, chatStore, switchConversation,];
        } }, { key: (item.id) }), { class: ([
            'p-3 mb-3 rounded-xl border transition-all cursor-pointer active:scale-95 group relative',
            __VLS_ctx.currentConversationId === item.id
                ? 'bg-indigo-50 border-indigo-200 shadow-inner'
                : 'bg-white border-gray-100 shadow-sm hover:shadow-md'
        ]) }));
    /** @type {__VLS_StyleScopedClasses['p-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['border']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
    /** @type {__VLS_StyleScopedClasses['cursor-pointer']} */ ;
    /** @type {__VLS_StyleScopedClasses['active:scale-95']} */ ;
    /** @type {__VLS_StyleScopedClasses['group']} */ ;
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "font-medium text-gray-800 line-clamp-1 mb-1 pr-6" }));
    /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-800']} */ ;
    /** @type {__VLS_StyleScopedClasses['line-clamp-1']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-1']} */ ;
    /** @type {__VLS_StyleScopedClasses['pr-6']} */ ;
    (item.title || '新会话');
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "text-xs text-gray-400 flex justify-between" }));
    /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['justify-between']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    (__VLS_ctx.formatTime(item.updatedAt || item.createdAt));
    if (__VLS_ctx.currentConversationId === item.id) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-indigo-500" }));
        /** @type {__VLS_StyleScopedClasses['text-indigo-500']} */ ;
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ onClick: function () {
            var _a = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                _a[_i] = arguments[_i];
            }
            var $event = _a[0];
            __VLS_ctx.confirmDelete(item.id);
            // @ts-ignore
            [formatTime, currentConversationId, currentConversationId, confirmDelete,];
        } }, { class: "absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition-opacity" }));
    /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
    /** @type {__VLS_StyleScopedClasses['top-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['right-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['opacity-0']} */ ;
    /** @type {__VLS_StyleScopedClasses['group-hover:opacity-100']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-opacity']} */ ;
    var __VLS_50 = void 0;
    /** @ts-ignore @type {typeof __VLS_components.vanIcon | typeof __VLS_components.VanIcon} */
    vanIcon;
    // @ts-ignore
    var __VLS_51 = __VLS_asFunctionalComponent1(__VLS_50, new __VLS_50(__assign({ name: "delete-o" }, { class: "text-red-400 hover:text-red-600" })));
    var __VLS_52 = __VLS_51.apply(void 0, __spreadArray([__assign({ name: "delete-o" }, { class: "text-red-400 hover:text-red-600" })], __VLS_functionalComponentArgsRest(__VLS_51), false));
    /** @type {__VLS_StyleScopedClasses['text-red-400']} */ ;
    /** @type {__VLS_StyleScopedClasses['hover:text-red-600']} */ ;
    // @ts-ignore
    [];
};
for (var _e = 0, _f = __VLS_vFor((__VLS_ctx.chatStore.historyList)); _e < _f.length; _e++) {
    var item = _f[_e][0];
    _loop_2(item);
}
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "p-4 border-t bg-white" }));
/** @type {__VLS_StyleScopedClasses['p-4']} */ ;
/** @type {__VLS_StyleScopedClasses['border-t']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
var __VLS_55;
/** @ts-ignore @type {typeof __VLS_components.vanButton | typeof __VLS_components.VanButton | typeof __VLS_components.vanButton | typeof __VLS_components.VanButton} */
vanButton;
// @ts-ignore
var __VLS_56 = __VLS_asFunctionalComponent1(__VLS_55, new __VLS_55(__assign({ 'onClick': {} }, { block: true, type: "primary", plain: true, size: "small" })));
var __VLS_57 = __VLS_56.apply(void 0, __spreadArray([__assign({ 'onClick': {} }, { block: true, type: "primary", plain: true, size: "small" })], __VLS_functionalComponentArgsRest(__VLS_56), false));
var __VLS_60;
var __VLS_61 = ({ click: {} },
    { onClick: (__VLS_ctx.startNewChat) });
var __VLS_62 = __VLS_58.slots.default;
{
    var __VLS_63 = __VLS_58.slots.icon;
    var __VLS_64 = void 0;
    /** @ts-ignore @type {typeof __VLS_components.vanIcon | typeof __VLS_components.VanIcon} */
    vanIcon;
    // @ts-ignore
    var __VLS_65 = __VLS_asFunctionalComponent1(__VLS_64, new __VLS_64({
        name: "plus",
    }));
    var __VLS_66 = __VLS_65.apply(void 0, __spreadArray([{
            name: "plus",
        }], __VLS_functionalComponentArgsRest(__VLS_65), false));
    // @ts-ignore
    [startNewChat,];
}
// @ts-ignore
[];
var __VLS_58;
var __VLS_59;
// @ts-ignore
[];
var __VLS_35;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-white/80 backdrop-blur-xl border-t border-gray-100/50 safe-area-bottom relative z-50 shadow-[0_-4px_20px_rgba(0,0,0,0.02)] flex flex-col" }));
/** @type {__VLS_StyleScopedClasses['bg-white/80']} */ ;
/** @type {__VLS_StyleScopedClasses['backdrop-blur-xl']} */ ;
/** @type {__VLS_StyleScopedClasses['border-t']} */ ;
/** @type {__VLS_StyleScopedClasses['border-gray-100/50']} */ ;
/** @type {__VLS_StyleScopedClasses['safe-area-bottom']} */ ;
/** @type {__VLS_StyleScopedClasses['relative']} */ ;
/** @type {__VLS_StyleScopedClasses['z-50']} */ ;
/** @type {__VLS_StyleScopedClasses['shadow-[0_-4px_20px_rgba(0,0,0,0.02)]']} */ ;
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
if (__VLS_ctx.imagePreviewUrl) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "px-4 pt-3 pb-2 border-b border-gray-100" }));
    /** @type {__VLS_StyleScopedClasses['px-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['pt-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['pb-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-b']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-gray-100']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "relative inline-block" }));
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    /** @type {__VLS_StyleScopedClasses['inline-block']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.img)(__assign(__assign({ src: (__VLS_ctx.imagePreviewUrl) }, { class: "h-20 w-20 object-cover rounded-lg border-2 border-indigo-200" }), { alt: "预览" }));
    /** @type {__VLS_StyleScopedClasses['h-20']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-20']} */ ;
    /** @type {__VLS_StyleScopedClasses['object-cover']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-lg']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-indigo-200']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign({ onClick: (__VLS_ctx.cancelImageSelection) }, { class: "absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 transition-colors shadow-md" }));
    /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
    /** @type {__VLS_StyleScopedClasses['-top-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['-right-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['h-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-red-500']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['hover:bg-red-600']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-md']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.svg, __VLS_intrinsics.svg)(__assign(__assign({ xmlns: "http://www.w3.org/2000/svg" }, { class: "h-4 w-4" }), { fill: "none", viewBox: "0 0 24 24", stroke: "currentColor" }));
    /** @type {__VLS_StyleScopedClasses['h-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-4']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.path)({
        'stroke-linecap': "round",
        'stroke-linejoin': "round",
        'stroke-width': "2",
        d: "M6 18L18 6M6 6l12 12",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)(__assign({ class: "text-xs text-gray-500 mt-1" }));
    /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-500']} */ ;
    /** @type {__VLS_StyleScopedClasses['mt-1']} */ ;
}
if (!__VLS_ctx.showVoicePanel) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex gap-2 px-4 pt-3 pb-1 overflow-x-auto no-scrollbar w-full" }));
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['gap-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['px-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['pt-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['pb-1']} */ ;
    /** @type {__VLS_StyleScopedClasses['overflow-x-auto']} */ ;
    /** @type {__VLS_StyleScopedClasses['no-scrollbar']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
    var _loop_3 = function (item) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign(__assign({ onClick: function () {
                var _a = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    _a[_i] = arguments[_i];
                }
                var $event = _a[0];
                if (!(!__VLS_ctx.showVoicePanel))
                    return;
                __VLS_ctx.handleQuickAction(item);
                // @ts-ignore
                [imagePreviewUrl, imagePreviewUrl, cancelImageSelection, showVoicePanel, quickActions, handleQuickAction,];
            } }, { key: (item), disabled: (__VLS_ctx.chatStore.isStreaming) }), { class: "flex-shrink-0 px-3 py-1.5 bg-indigo-50 text-indigo-600 text-xs font-medium rounded-full border border-indigo-100 active:bg-indigo-100 active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed whitespace-nowrap" }));
        /** @type {__VLS_StyleScopedClasses['flex-shrink-0']} */ ;
        /** @type {__VLS_StyleScopedClasses['px-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['py-1.5']} */ ;
        /** @type {__VLS_StyleScopedClasses['bg-indigo-50']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-indigo-600']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['border']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-indigo-100']} */ ;
        /** @type {__VLS_StyleScopedClasses['active:bg-indigo-100']} */ ;
        /** @type {__VLS_StyleScopedClasses['active:scale-95']} */ ;
        /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
        /** @type {__VLS_StyleScopedClasses['disabled:opacity-50']} */ ;
        /** @type {__VLS_StyleScopedClasses['disabled:cursor-not-allowed']} */ ;
        /** @type {__VLS_StyleScopedClasses['whitespace-nowrap']} */ ;
        (item);
        // @ts-ignore
        [chatStore,];
    };
    for (var _g = 0, _h = __VLS_vFor((__VLS_ctx.quickActions)); _g < _h.length; _g++) {
        var item = _h[_g][0];
        _loop_3(item);
    }
}
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex items-center gap-3 px-4 py-3" }));
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['items-center']} */ ;
/** @type {__VLS_StyleScopedClasses['gap-3']} */ ;
/** @type {__VLS_StyleScopedClasses['px-4']} */ ;
/** @type {__VLS_StyleScopedClasses['py-3']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign(__assign({ onClick: function () {
        var _a = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            _a[_i] = arguments[_i];
        }
        var $event = _a[0];
        __VLS_ctx.router.push('/game');
        // @ts-ignore
        [router,];
    } }, { class: "p-2 rounded-full text-yellow-500 bg-yellow-50 hover:bg-yellow-100 hover:text-yellow-600 border border-yellow-200 shadow-sm transition-all active:scale-95 flex-shrink-0" }), { title: "知识闯关" }));
/** @type {__VLS_StyleScopedClasses['p-2']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['text-yellow-500']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-yellow-50']} */ ;
/** @type {__VLS_StyleScopedClasses['hover:bg-yellow-100']} */ ;
/** @type {__VLS_StyleScopedClasses['hover:text-yellow-600']} */ ;
/** @type {__VLS_StyleScopedClasses['border']} */ ;
/** @type {__VLS_StyleScopedClasses['border-yellow-200']} */ ;
/** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
/** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
/** @type {__VLS_StyleScopedClasses['active:scale-95']} */ ;
/** @type {__VLS_StyleScopedClasses['flex-shrink-0']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.svg, __VLS_intrinsics.svg)(__assign(__assign({ xmlns: "http://www.w3.org/2000/svg" }, { class: "h-6 w-6" }), { fill: "none", viewBox: "0 0 24 24", stroke: "currentColor" }));
/** @type {__VLS_StyleScopedClasses['h-6']} */ ;
/** @type {__VLS_StyleScopedClasses['w-6']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.path)({
    'stroke-linecap': "round",
    'stroke-linejoin': "round",
    'stroke-width': "2",
    d: "M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10",
});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign({ onClick: (__VLS_ctx.toggleVoicePanel) }, { class: "p-2 rounded-full text-gray-500 hover:text-blue-600 hover:bg-gray-100 transition-colors" }));
/** @type {__VLS_StyleScopedClasses['p-2']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['text-gray-500']} */ ;
/** @type {__VLS_StyleScopedClasses['hover:text-blue-600']} */ ;
/** @type {__VLS_StyleScopedClasses['hover:bg-gray-100']} */ ;
/** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
if (__VLS_ctx.showVoicePanel) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.svg, __VLS_intrinsics.svg)(__assign(__assign({ xmlns: "http://www.w3.org/2000/svg" }, { class: "h-6 w-6" }), { fill: "none", viewBox: "0 0 24 24", stroke: "currentColor" }));
    /** @type {__VLS_StyleScopedClasses['h-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-6']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.path)({
        'stroke-linecap': "round",
        'stroke-linejoin': "round",
        'stroke-width': "2",
        d: "M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z",
    });
}
else {
    __VLS_asFunctionalElement1(__VLS_intrinsics.svg, __VLS_intrinsics.svg)(__assign(__assign({ xmlns: "http://www.w3.org/2000/svg" }, { class: "h-6 w-6" }), { fill: "none", viewBox: "0 0 24 24", stroke: "currentColor" }));
    /** @type {__VLS_StyleScopedClasses['h-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-6']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.path)({
        'stroke-linecap': "round",
        'stroke-linejoin': "round",
        'stroke-width': "2",
        d: "M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z",
    });
}
__VLS_asFunctionalElement1(__VLS_intrinsics.input)(__assign(__assign(__assign({ onKeyup: (__VLS_ctx.handleSend) }, { value: (__VLS_ctx.inputContent), type: "text" }), { class: "flex-1 bg-gray-100/80 rounded-full px-5 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/50 focus:bg-white transition-all placeholder-gray-400" }), { placeholder: (__VLS_ctx.showVoicePanel ? '按住下方按钮说话...' : '问问附近的非遗体验...'), disabled: (__VLS_ctx.chatStore.isStreaming || __VLS_ctx.showVoicePanel) }));
/** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-gray-100/80']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['px-5']} */ ;
/** @type {__VLS_StyleScopedClasses['py-3']} */ ;
/** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
/** @type {__VLS_StyleScopedClasses['focus:outline-none']} */ ;
/** @type {__VLS_StyleScopedClasses['focus:ring-2']} */ ;
/** @type {__VLS_StyleScopedClasses['focus:ring-indigo-500/50']} */ ;
/** @type {__VLS_StyleScopedClasses['focus:bg-white']} */ ;
/** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
/** @type {__VLS_StyleScopedClasses['placeholder-gray-400']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign({ onClick: (__VLS_ctx.triggerImageUpload) }, { class: "p-2 rounded-full text-gray-500 hover:text-blue-600 hover:bg-gray-100 transition-colors" }));
/** @type {__VLS_StyleScopedClasses['p-2']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['text-gray-500']} */ ;
/** @type {__VLS_StyleScopedClasses['hover:text-blue-600']} */ ;
/** @type {__VLS_StyleScopedClasses['hover:bg-gray-100']} */ ;
/** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.svg, __VLS_intrinsics.svg)(__assign(__assign({ xmlns: "http://www.w3.org/2000/svg" }, { class: "h-6 w-6" }), { fill: "none", viewBox: "0 0 24 24", stroke: "currentColor" }));
/** @type {__VLS_StyleScopedClasses['h-6']} */ ;
/** @type {__VLS_StyleScopedClasses['w-6']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.path)({
    'stroke-linecap': "round",
    'stroke-linejoin': "round",
    'stroke-width': "2",
    d: "M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z",
});
__VLS_asFunctionalElement1(__VLS_intrinsics.input)(__assign(__assign({ onChange: (__VLS_ctx.handleFileChange) }, { type: "file", ref: "fileInput", accept: "image/*" }), { class: "hidden" }));
/** @type {__VLS_StyleScopedClasses['hidden']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign(__assign({ onClick: (__VLS_ctx.handleSend) }, { disabled: ((!__VLS_ctx.inputContent.trim() && !__VLS_ctx.selectedImage) || __VLS_ctx.chatStore.isStreaming) }), { class: ([
        'rounded-full p-3 transition-all duration-300 flex items-center justify-center',
        (__VLS_ctx.inputContent.trim() || __VLS_ctx.selectedImage) && !__VLS_ctx.chatStore.isStreaming
            ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-200 scale-100 hover:bg-indigo-700'
            : 'bg-gray-100 text-gray-300 scale-95'
    ]) }));
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['p-3']} */ ;
/** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
/** @type {__VLS_StyleScopedClasses['duration-300']} */ ;
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['items-center']} */ ;
/** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.svg, __VLS_intrinsics.svg)(__assign(__assign({ xmlns: "http://www.w3.org/2000/svg" }, { class: "h-5 w-5 transform rotate-90" }), { viewBox: "0 0 20 20", fill: "currentColor" }));
/** @type {__VLS_StyleScopedClasses['h-5']} */ ;
/** @type {__VLS_StyleScopedClasses['w-5']} */ ;
/** @type {__VLS_StyleScopedClasses['transform']} */ ;
/** @type {__VLS_StyleScopedClasses['rotate-90']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.path)({
    d: "M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z",
});
if (__VLS_ctx.showVoicePanel) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-gray-50 border-t border-gray-100 p-8 flex justify-center items-center h-48 transition-all animate-slide-up" }));
    /** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-t']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-gray-100']} */ ;
    /** @type {__VLS_StyleScopedClasses['p-8']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['h-48']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
    /** @type {__VLS_StyleScopedClasses['animate-slide-up']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "relative" }));
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign(__assign(__assign(__assign({ onMousedown: (__VLS_ctx.startRecording) }, { onMouseup: (__VLS_ctx.stopRecording) }), { onTouchstart: (__VLS_ctx.startRecording) }), { onTouchend: (__VLS_ctx.stopRecording) }), { class: ([
            'w-24 h-24 rounded-full flex items-center justify-center text-4xl shadow-xl select-none transition-all duration-200',
            __VLS_ctx.isRecording ? 'bg-indigo-500 text-white scale-110 ring-8 ring-indigo-200' : 'bg-white text-indigo-500 hover:shadow-2xl'
        ]) }));
    /** @type {__VLS_StyleScopedClasses['w-24']} */ ;
    /** @type {__VLS_StyleScopedClasses['h-24']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-4xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['select-none']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
    /** @type {__VLS_StyleScopedClasses['duration-200']} */ ;
    if (__VLS_ctx.isRecording) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute inset-0 rounded-full animate-ping bg-indigo-400 opacity-20 z-0" }));
        /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
        /** @type {__VLS_StyleScopedClasses['inset-0']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['animate-ping']} */ ;
        /** @type {__VLS_StyleScopedClasses['bg-indigo-400']} */ ;
        /** @type {__VLS_StyleScopedClasses['opacity-20']} */ ;
        /** @type {__VLS_StyleScopedClasses['z-0']} */ ;
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)(__assign({ class: "absolute bottom-6 text-gray-400 text-sm font-medium" }));
    /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
    /** @type {__VLS_StyleScopedClasses['bottom-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
    (__VLS_ctx.isRecording ? '松开结束' : '按住说话');
}
// @ts-ignore
[chatStore, chatStore, chatStore, showVoicePanel, showVoicePanel, showVoicePanel, showVoicePanel, toggleVoicePanel, handleSend, handleSend, inputContent, inputContent, inputContent, triggerImageUpload, handleFileChange, selectedImage, selectedImage, startRecording, startRecording, stopRecording, stopRecording, isRecording, isRecording, isRecording,];
var __VLS_export = (await import('vue')).defineComponent({});
export default {};
