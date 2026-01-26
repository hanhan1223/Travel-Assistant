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
var _a, _b;
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useChatStore } from '../../stores/chatStore';
import { showDialog, showToast } from 'vant';
var router = useRouter();
var chatStore = useChatStore();
var loading = ref(false);
var showRename = ref(false);
var renameValue = ref('');
var currentEditId = ref(null);
var onClickLeft = function () {
    router.push('/user');
};
var formatTime = function (timeStr) {
    if (!timeStr)
        return '';
    var date = new Date(timeStr);
    return "".concat(date.getMonth() + 1, "\u6708").concat(date.getDate(), "\u65E5 ").concat(date.getHours(), ":").concat(date.getMinutes().toString().padStart(2, '0'));
};
var toChat = function (id) {
    router.push({ path: '/chat', query: { id: id } });
};
// 打开弹窗
var openRenameDialog = function (item) {
    console.log('点击编辑，当前项:', item);
    if (!item || !item.id) {
        showToast('数据异常：无法获取会话ID');
        return;
    }
    currentEditId.value = item.id;
    renameValue.value = item.title;
    showRename.value = true;
};
// 确认修改（核心逻辑）
var onRenameConfirm = function (action) { return __awaiter(void 0, void 0, void 0, function () {
    var success, error_1;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                console.log('触发 onRenameConfirm, action:', action);
                if (!(action === 'confirm')) return [3 /*break*/, 4];
                // 1. 校验非空
                if (!renameValue.value.trim()) {
                    showToast('标题不能为空');
                    return [2 /*return*/, false]; // 阻止关闭，停止转圈（Vant机制）
                }
                // 2. 校验ID
                if (currentEditId.value === null) {
                    console.error('错误：currentEditId 为 null');
                    showToast('系统错误：ID丢失');
                    return [2 /*return*/, true]; // 关闭弹窗
                }
                console.log("\u51C6\u5907\u53D1\u8D77\u8BF7\u6C42: ID=".concat(currentEditId.value, ", \u65B0\u6807\u9898=").concat(renameValue.value));
                _a.label = 1;
            case 1:
                _a.trys.push([1, 3, , 4]);
                return [4 /*yield*/, chatStore.updateConversationTitle(currentEditId.value, renameValue.value)];
            case 2:
                success = _a.sent();
                console.log('接口调用结果:', success);
                if (success) {
                    return [2 /*return*/, true]; // 成功，关闭弹窗
                }
                else {
                    return [2 /*return*/, false]; // 失败，保持弹窗（通常 store 内部已经报了错）
                }
                return [3 /*break*/, 4];
            case 3:
                error_1 = _a.sent();
                // 4. 捕获所有异常
                console.error('发生未知错误:', error_1);
                showToast('请求发生异常');
                return [2 /*return*/, false]; // 停止转圈，保持弹窗
            case 4: return [2 /*return*/, true]; // 取消操作，直接关闭
        }
    });
}); };
var handleDelete = function (id) {
    showDialog({
        title: '确认删除',
        message: '删除后无法恢复，确定要删除该会话吗？',
        showCancelButton: true,
    }).then(function () { return __awaiter(void 0, void 0, void 0, function () {
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4 /*yield*/, chatStore.deleteConversation(id)];
                case 1:
                    _a.sent();
                    return [2 /*return*/];
            }
        });
    }); }).catch(function () { });
};
onMounted(function () { return __awaiter(void 0, void 0, void 0, function () {
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                loading.value = true;
                return [4 /*yield*/, chatStore.fetchHistory()];
            case 1:
                _a.sent();
                loading.value = false;
                return [2 /*return*/];
        }
    });
}); });
var __VLS_ctx = __assign(__assign({}, {}), {});
var __VLS_components;
var __VLS_intrinsics;
var __VLS_directives;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "min-h-screen bg-gray-50 pb-safe" }));
/** @type {__VLS_StyleScopedClasses['min-h-screen']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
/** @type {__VLS_StyleScopedClasses['pb-safe']} */ ;
var __VLS_0;
/** @ts-ignore @type {typeof __VLS_components.vanNavBar | typeof __VLS_components.VanNavBar} */
vanNavBar;
// @ts-ignore
var __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0(__assign({ 'onClickLeft': {} }, { title: "历史会话", leftText: "返回", leftArrow: true, fixed: true, placeholder: true })));
var __VLS_2 = __VLS_1.apply(void 0, __spreadArray([__assign({ 'onClickLeft': {} }, { title: "历史会话", leftText: "返回", leftArrow: true, fixed: true, placeholder: true })], __VLS_functionalComponentArgsRest(__VLS_1), false));
var __VLS_5;
var __VLS_6 = ({ clickLeft: {} },
    { onClickLeft: (__VLS_ctx.onClickLeft) });
var __VLS_3;
var __VLS_4;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "pt-2" }));
/** @type {__VLS_StyleScopedClasses['pt-2']} */ ;
if (__VLS_ctx.loading && ((_a = __VLS_ctx.chatStore.historyList) === null || _a === void 0 ? void 0 : _a.length) === 0) {
    var __VLS_7 = void 0;
    /** @ts-ignore @type {typeof __VLS_components.vanEmpty | typeof __VLS_components.VanEmpty} */
    vanEmpty;
    // @ts-ignore
    var __VLS_8 = __VLS_asFunctionalComponent1(__VLS_7, new __VLS_7({
        description: "加载中...",
    }));
    var __VLS_9 = __VLS_8.apply(void 0, __spreadArray([{
            description: "加载中...",
        }], __VLS_functionalComponentArgsRest(__VLS_8), false));
}
else if (!__VLS_ctx.loading && ((_b = __VLS_ctx.chatStore.historyList) === null || _b === void 0 ? void 0 : _b.length) === 0) {
    var __VLS_12 = void 0;
    /** @ts-ignore @type {typeof __VLS_components.vanEmpty | typeof __VLS_components.VanEmpty} */
    vanEmpty;
    // @ts-ignore
    var __VLS_13 = __VLS_asFunctionalComponent1(__VLS_12, new __VLS_12({
        description: "暂无历史会话",
    }));
    var __VLS_14 = __VLS_13.apply(void 0, __spreadArray([{
            description: "暂无历史会话",
        }], __VLS_functionalComponentArgsRest(__VLS_13), false));
}
else {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "px-3 space-y-3" }));
    /** @type {__VLS_StyleScopedClasses['px-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['space-y-3']} */ ;
    var _loop_1 = function (item) {
        var __VLS_17 = void 0;
        /** @ts-ignore @type {typeof __VLS_components.vanSwipeCell | typeof __VLS_components.VanSwipeCell | typeof __VLS_components.vanSwipeCell | typeof __VLS_components.VanSwipeCell} */
        vanSwipeCell;
        // @ts-ignore
        var __VLS_18 = __VLS_asFunctionalComponent1(__VLS_17, new __VLS_17(__assign({ key: (item.id) }, { class: "bg-white rounded-xl overflow-hidden shadow-sm" })));
        var __VLS_19 = __VLS_18.apply(void 0, __spreadArray([__assign({ key: (item.id) }, { class: "bg-white rounded-xl overflow-hidden shadow-sm" })], __VLS_functionalComponentArgsRest(__VLS_18), false));
        /** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
        /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
        /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
        var __VLS_22 = __VLS_20.slots.default;
        var __VLS_23 = void 0;
        /** @ts-ignore @type {typeof __VLS_components.vanCell | typeof __VLS_components.VanCell | typeof __VLS_components.vanCell | typeof __VLS_components.VanCell} */
        vanCell;
        // @ts-ignore
        var __VLS_24 = __VLS_asFunctionalComponent1(__VLS_23, new __VLS_23(__assign(__assign({ 'onClick': {} }, { label: (__VLS_ctx.formatTime(item.updatedAt || item.createdAt)), isLink: true, center: true }), { class: "py-4" })));
        var __VLS_25 = __VLS_24.apply(void 0, __spreadArray([__assign(__assign({ 'onClick': {} }, { label: (__VLS_ctx.formatTime(item.updatedAt || item.createdAt)), isLink: true, center: true }), { class: "py-4" })], __VLS_functionalComponentArgsRest(__VLS_24), false));
        var __VLS_28 = void 0;
        var __VLS_29 = ({ click: {} },
            { onClick: function () {
                    var _a, _b;
                    var _c = [];
                    for (var _i = 0; _i < arguments.length; _i++) {
                        _c[_i] = arguments[_i];
                    }
                    var $event = _c[0];
                    if (!!(__VLS_ctx.loading && ((_a = __VLS_ctx.chatStore.historyList) === null || _a === void 0 ? void 0 : _a.length) === 0))
                        return;
                    if (!!(!__VLS_ctx.loading && ((_b = __VLS_ctx.chatStore.historyList) === null || _b === void 0 ? void 0 : _b.length) === 0))
                        return;
                    __VLS_ctx.toChat(item.id);
                    // @ts-ignore
                    [onClickLeft, loading, loading, chatStore, chatStore, chatStore, formatTime, toChat,];
                } });
        /** @type {__VLS_StyleScopedClasses['py-4']} */ ;
        var __VLS_30 = __VLS_26.slots.default;
        {
            var __VLS_31 = __VLS_26.slots.title;
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex items-center space-x-2 pr-2" }));
            /** @type {__VLS_StyleScopedClasses['flex']} */ ;
            /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
            /** @type {__VLS_StyleScopedClasses['space-x-2']} */ ;
            /** @type {__VLS_StyleScopedClasses['pr-2']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "font-medium text-gray-800 truncate text-base" }));
            /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-gray-800']} */ ;
            /** @type {__VLS_StyleScopedClasses['truncate']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-base']} */ ;
            (item.title || '新会话');
            var __VLS_32 = void 0;
            /** @ts-ignore @type {typeof __VLS_components.vanIcon | typeof __VLS_components.VanIcon} */
            vanIcon;
            // @ts-ignore
            var __VLS_33 = __VLS_asFunctionalComponent1(__VLS_32, new __VLS_32(__assign(__assign({ 'onClick': {} }, { name: "edit" }), { class: "text-gray-400 p-1 cursor-pointer hover:text-indigo-600" })));
            var __VLS_34 = __VLS_33.apply(void 0, __spreadArray([__assign(__assign({ 'onClick': {} }, { name: "edit" }), { class: "text-gray-400 p-1 cursor-pointer hover:text-indigo-600" })], __VLS_functionalComponentArgsRest(__VLS_33), false));
            var __VLS_37 = void 0;
            var __VLS_38 = ({ click: {} },
                { onClick: function () {
                        var _a, _b;
                        var _c = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            _c[_i] = arguments[_i];
                        }
                        var $event = _c[0];
                        if (!!(__VLS_ctx.loading && ((_a = __VLS_ctx.chatStore.historyList) === null || _a === void 0 ? void 0 : _a.length) === 0))
                            return;
                        if (!!(!__VLS_ctx.loading && ((_b = __VLS_ctx.chatStore.historyList) === null || _b === void 0 ? void 0 : _b.length) === 0))
                            return;
                        __VLS_ctx.openRenameDialog(item);
                        // @ts-ignore
                        [openRenameDialog,];
                    } });
            /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
            /** @type {__VLS_StyleScopedClasses['p-1']} */ ;
            /** @type {__VLS_StyleScopedClasses['cursor-pointer']} */ ;
            /** @type {__VLS_StyleScopedClasses['hover:text-indigo-600']} */ ;
            // @ts-ignore
            [];
        }
        {
            var __VLS_39 = __VLS_26.slots.icon;
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "mr-3 w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center text-indigo-600" }));
            /** @type {__VLS_StyleScopedClasses['mr-3']} */ ;
            /** @type {__VLS_StyleScopedClasses['w-10']} */ ;
            /** @type {__VLS_StyleScopedClasses['h-10']} */ ;
            /** @type {__VLS_StyleScopedClasses['bg-indigo-100']} */ ;
            /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
            /** @type {__VLS_StyleScopedClasses['flex']} */ ;
            /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
            /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-indigo-600']} */ ;
            var __VLS_40 = void 0;
            /** @ts-ignore @type {typeof __VLS_components.vanIcon | typeof __VLS_components.VanIcon} */
            vanIcon;
            // @ts-ignore
            var __VLS_41 = __VLS_asFunctionalComponent1(__VLS_40, new __VLS_40({
                name: "chat-o",
                size: "20",
            }));
            var __VLS_42 = __VLS_41.apply(void 0, __spreadArray([{
                    name: "chat-o",
                    size: "20",
                }], __VLS_functionalComponentArgsRest(__VLS_41), false));
            // @ts-ignore
            [];
        }
        // @ts-ignore
        [];
        {
            var __VLS_45 = __VLS_20.slots.right;
            var __VLS_46 = void 0;
            /** @ts-ignore @type {typeof __VLS_components.vanButton | typeof __VLS_components.VanButton} */
            vanButton;
            // @ts-ignore
            var __VLS_47 = __VLS_asFunctionalComponent1(__VLS_46, new __VLS_46(__assign(__assign({ 'onClick': {} }, { square: true, text: "删除", type: "danger" }), { class: "h-full" })));
            var __VLS_48 = __VLS_47.apply(void 0, __spreadArray([__assign(__assign({ 'onClick': {} }, { square: true, text: "删除", type: "danger" }), { class: "h-full" })], __VLS_functionalComponentArgsRest(__VLS_47), false));
            var __VLS_51 = void 0;
            var __VLS_52 = ({ click: {} },
                { onClick: function () {
                        var _a, _b;
                        var _c = [];
                        for (var _i = 0; _i < arguments.length; _i++) {
                            _c[_i] = arguments[_i];
                        }
                        var $event = _c[0];
                        if (!!(__VLS_ctx.loading && ((_a = __VLS_ctx.chatStore.historyList) === null || _a === void 0 ? void 0 : _a.length) === 0))
                            return;
                        if (!!(!__VLS_ctx.loading && ((_b = __VLS_ctx.chatStore.historyList) === null || _b === void 0 ? void 0 : _b.length) === 0))
                            return;
                        __VLS_ctx.handleDelete(item.id);
                        // @ts-ignore
                        [handleDelete,];
                    } });
            /** @type {__VLS_StyleScopedClasses['h-full']} */ ;
            // @ts-ignore
            [];
        }
        // @ts-ignore
        [];
        // @ts-ignore
        [];
    };
    var __VLS_35, __VLS_36, __VLS_26, __VLS_27, __VLS_49, __VLS_50, __VLS_20;
    for (var _i = 0, _c = __VLS_vFor((__VLS_ctx.chatStore.historyList)); _i < _c.length; _i++) {
        var item = _c[_i][0];
        _loop_1(item);
    }
}
var __VLS_53;
/** @ts-ignore @type {typeof __VLS_components.vanDialog | typeof __VLS_components.VanDialog | typeof __VLS_components.vanDialog | typeof __VLS_components.VanDialog} */
vanDialog;
// @ts-ignore
var __VLS_54 = __VLS_asFunctionalComponent1(__VLS_53, new __VLS_53({
    show: (__VLS_ctx.showRename),
    title: "修改标题",
    showCancelButton: true,
    beforeClose: (__VLS_ctx.onRenameConfirm),
}));
var __VLS_55 = __VLS_54.apply(void 0, __spreadArray([{
        show: (__VLS_ctx.showRename),
        title: "修改标题",
        showCancelButton: true,
        beforeClose: (__VLS_ctx.onRenameConfirm),
    }], __VLS_functionalComponentArgsRest(__VLS_54), false));
var __VLS_58 = __VLS_56.slots.default;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "p-4" }));
/** @type {__VLS_StyleScopedClasses['p-4']} */ ;
var __VLS_59;
/** @ts-ignore @type {typeof __VLS_components.vanField | typeof __VLS_components.VanField} */
vanField;
// @ts-ignore
var __VLS_60 = __VLS_asFunctionalComponent1(__VLS_59, new __VLS_59(__assign({ modelValue: (__VLS_ctx.renameValue), placeholder: "请输入新的会话标题", border: true }, { class: "bg-gray-50 rounded-md" })));
var __VLS_61 = __VLS_60.apply(void 0, __spreadArray([__assign({ modelValue: (__VLS_ctx.renameValue), placeholder: "请输入新的会话标题", border: true }, { class: "bg-gray-50 rounded-md" })], __VLS_functionalComponentArgsRest(__VLS_60), false));
/** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-md']} */ ;
// @ts-ignore
[showRename, onRenameConfirm, renameValue,];
var __VLS_56;
// @ts-ignore
[];
var __VLS_export = (await import('vue')).defineComponent({});
export default {};
