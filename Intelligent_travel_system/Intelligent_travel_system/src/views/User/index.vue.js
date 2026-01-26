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
import { onMounted, ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/userStore';
import { showToast, showDialog } from 'vant';
var router = useRouter();
var userStore = useUserStore();
var defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
var showEditDialog = ref(false);
var editForm = reactive({
    userName: '',
    userAvatar: ''
});
var onClickLeft = function () {
    router.push('/');
};
onMounted(function () {
    if (!userStore.userInfo) {
        userStore.fetchUserInfo();
    }
});
var openEditDialog = function () {
    if (userStore.userInfo) {
        editForm.userName = userStore.userInfo.userName;
        editForm.userAvatar = userStore.userInfo.userAvatar || '';
        showEditDialog.value = true;
    }
};
var onAvatarUpload = function (file) { return __awaiter(void 0, void 0, void 0, function () {
    var url;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                showToast({ message: '上传中...', type: 'loading' });
                return [4 /*yield*/, userStore.uploadFile(file.file)];
            case 1:
                url = _a.sent();
                if (url) {
                    editForm.userAvatar = url;
                    showToast('上传成功');
                }
                return [2 /*return*/];
        }
    });
}); };
var onBeforeClose = function (action) { return __awaiter(void 0, void 0, void 0, function () {
    var success;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (!(action === 'confirm')) return [3 /*break*/, 2];
                if (!editForm.userName.trim()) {
                    showToast('昵称不能为空');
                    return [2 /*return*/, false];
                }
                return [4 /*yield*/, userStore.updateProfile(editForm.userName, editForm.userAvatar)];
            case 1:
                success = _a.sent();
                if (success) {
                    return [2 /*return*/, true];
                }
                else {
                    return [2 /*return*/, false];
                }
                _a.label = 2;
            case 2: return [2 /*return*/, true];
        }
    });
}); };
var handleLogout = function () {
    showDialog({
        title: '提示',
        message: '确定要退出登录吗？',
        showCancelButton: true,
    }).then(function () {
        userStore.logout();
    }).catch(function () { });
};
var __VLS_ctx = __assign(__assign({}, {}), {});
var __VLS_components;
var __VLS_intrinsics;
var __VLS_directives;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "min-h-screen bg-gray-50 pb-20" }));
/** @type {__VLS_StyleScopedClasses['min-h-screen']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
/** @type {__VLS_StyleScopedClasses['pb-20']} */ ;
var __VLS_0;
/** @ts-ignore @type {typeof __VLS_components.vanNavBar | typeof __VLS_components.VanNavBar} */
vanNavBar;
// @ts-ignore
var __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0(__assign({ 'onClickLeft': {} }, { title: "个人中心", leftText: "返回", leftArrow: true, fixed: true, placeholder: true })));
var __VLS_2 = __VLS_1.apply(void 0, __spreadArray([__assign({ 'onClickLeft': {} }, { title: "个人中心", leftText: "返回", leftArrow: true, fixed: true, placeholder: true })], __VLS_functionalComponentArgsRest(__VLS_1), false));
var __VLS_5;
var __VLS_6 = ({ clickLeft: {} },
    { onClickLeft: (__VLS_ctx.onClickLeft) });
var __VLS_3;
var __VLS_4;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-indigo-600 text-white pt-8 pb-12 px-6 rounded-b-[2rem] shadow-lg relative overflow-hidden" }));
/** @type {__VLS_StyleScopedClasses['bg-indigo-600']} */ ;
/** @type {__VLS_StyleScopedClasses['text-white']} */ ;
/** @type {__VLS_StyleScopedClasses['pt-8']} */ ;
/** @type {__VLS_StyleScopedClasses['pb-12']} */ ;
/** @type {__VLS_StyleScopedClasses['px-6']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-b-[2rem]']} */ ;
/** @type {__VLS_StyleScopedClasses['shadow-lg']} */ ;
/** @type {__VLS_StyleScopedClasses['relative']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute top-0 right-0 w-32 h-32 bg-white opacity-10 rounded-full transform translate-x-10 -translate-y-10" }));
/** @type {__VLS_StyleScopedClasses['absolute']} */ ;
/** @type {__VLS_StyleScopedClasses['top-0']} */ ;
/** @type {__VLS_StyleScopedClasses['right-0']} */ ;
/** @type {__VLS_StyleScopedClasses['w-32']} */ ;
/** @type {__VLS_StyleScopedClasses['h-32']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
/** @type {__VLS_StyleScopedClasses['opacity-10']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['transform']} */ ;
/** @type {__VLS_StyleScopedClasses['translate-x-10']} */ ;
/** @type {__VLS_StyleScopedClasses['-translate-y-10']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "relative z-10 flex items-center space-x-4" }));
/** @type {__VLS_StyleScopedClasses['relative']} */ ;
/** @type {__VLS_StyleScopedClasses['z-10']} */ ;
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['items-center']} */ ;
/** @type {__VLS_StyleScopedClasses['space-x-4']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "w-20 h-20 rounded-full border-4 border-white/30 overflow-hidden bg-white/20 backdrop-blur-sm shadow-inner" }));
/** @type {__VLS_StyleScopedClasses['w-20']} */ ;
/** @type {__VLS_StyleScopedClasses['h-20']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['border-4']} */ ;
/** @type {__VLS_StyleScopedClasses['border-white/30']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-white/20']} */ ;
/** @type {__VLS_StyleScopedClasses['backdrop-blur-sm']} */ ;
/** @type {__VLS_StyleScopedClasses['shadow-inner']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.img)(__assign(__assign({ src: (((_a = __VLS_ctx.userStore.userInfo) === null || _a === void 0 ? void 0 : _a.userAvatar) || __VLS_ctx.defaultAvatar) }, { class: "w-full h-full object-cover" }), { alt: "User Avatar" }));
/** @type {__VLS_StyleScopedClasses['w-full']} */ ;
/** @type {__VLS_StyleScopedClasses['h-full']} */ ;
/** @type {__VLS_StyleScopedClasses['object-cover']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex-1" }));
/** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
if (__VLS_ctx.userStore.userInfo) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)(__assign({ class: "text-2xl font-bold" }));
    /** @type {__VLS_StyleScopedClasses['text-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    (__VLS_ctx.userStore.userInfo.userName);
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)(__assign({ class: "text-indigo-100 text-sm mt-1 opacity-90" }));
    /** @type {__VLS_StyleScopedClasses['text-indigo-100']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['mt-1']} */ ;
    /** @type {__VLS_StyleScopedClasses['opacity-90']} */ ;
    (__VLS_ctx.userStore.userInfo.email);
}
else {
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)(__assign({ onClick: function () {
            var _a = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                _a[_i] = arguments[_i];
            }
            var $event = _a[0];
            if (!!(__VLS_ctx.userStore.userInfo))
                return;
            __VLS_ctx.router.push('/login');
            // @ts-ignore
            [onClickLeft, userStore, userStore, userStore, userStore, defaultAvatar, router,];
        } }, { class: "text-xl font-bold" }));
    /** @type {__VLS_StyleScopedClasses['text-xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)(__assign({ class: "text-indigo-200 text-sm mt-1" }));
    /** @type {__VLS_StyleScopedClasses['text-indigo-200']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['mt-1']} */ ;
}
if (__VLS_ctx.userStore.userInfo) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign({ onClick: (__VLS_ctx.openEditDialog) }, { class: "bg-white/20 hover:bg-white/30 p-2 rounded-full transition-colors backdrop-blur-md" }));
    /** @type {__VLS_StyleScopedClasses['bg-white/20']} */ ;
    /** @type {__VLS_StyleScopedClasses['hover:bg-white/30']} */ ;
    /** @type {__VLS_StyleScopedClasses['p-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
    /** @type {__VLS_StyleScopedClasses['backdrop-blur-md']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.svg, __VLS_intrinsics.svg)(__assign(__assign({ xmlns: "http://www.w3.org/2000/svg" }, { class: "h-6 w-6 text-white" }), { fill: "none", viewBox: "0 0 24 24", stroke: "currentColor" }));
    /** @type {__VLS_StyleScopedClasses['h-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-white']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.path)({
        'stroke-linecap': "round",
        'stroke-linejoin': "round",
        'stroke-width': "2",
        d: "M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z",
    });
}
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "px-4 -mt-6 relative z-20" }));
/** @type {__VLS_StyleScopedClasses['px-4']} */ ;
/** @type {__VLS_StyleScopedClasses['-mt-6']} */ ;
/** @type {__VLS_StyleScopedClasses['relative']} */ ;
/** @type {__VLS_StyleScopedClasses['z-20']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-white rounded-xl shadow-sm overflow-hidden mb-4" }));
/** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
/** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
/** @type {__VLS_StyleScopedClasses['mb-4']} */ ;
var __VLS_7;
/** @ts-ignore @type {typeof __VLS_components.vanCell | typeof __VLS_components.VanCell} */
vanCell;
// @ts-ignore
var __VLS_8 = __VLS_asFunctionalComponent1(__VLS_7, new __VLS_7({
    title: "我的游览报告",
    isLink: true,
    to: "/user/documents",
    icon: "orders-o",
    size: "large",
}));
var __VLS_9 = __VLS_8.apply(void 0, __spreadArray([{
        title: "我的游览报告",
        isLink: true,
        to: "/user/documents",
        icon: "orders-o",
        size: "large",
    }], __VLS_functionalComponentArgsRest(__VLS_8), false));
var __VLS_12;
/** @ts-ignore @type {typeof __VLS_components.vanCell | typeof __VLS_components.VanCell} */
vanCell;
// @ts-ignore
var __VLS_13 = __VLS_asFunctionalComponent1(__VLS_12, new __VLS_12({
    title: "历史会话",
    isLink: true,
    to: "/chat/history",
    icon: "chat-o",
    size: "large",
}));
var __VLS_14 = __VLS_13.apply(void 0, __spreadArray([{
        title: "历史会话",
        isLink: true,
        to: "/chat/history",
        icon: "chat-o",
        size: "large",
    }], __VLS_functionalComponentArgsRest(__VLS_13), false));
var __VLS_17;
/** @ts-ignore @type {typeof __VLS_components.vanCell | typeof __VLS_components.VanCell} */
vanCell;
// @ts-ignore
var __VLS_18 = __VLS_asFunctionalComponent1(__VLS_17, new __VLS_17({
    title: "修改密码",
    isLink: true,
    to: "/user/password",
    icon: "lock",
    size: "large",
}));
var __VLS_19 = __VLS_18.apply(void 0, __spreadArray([{
        title: "修改密码",
        isLink: true,
        to: "/user/password",
        icon: "lock",
        size: "large",
    }], __VLS_functionalComponentArgsRest(__VLS_18), false));
if (__VLS_ctx.userStore.userInfo) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-white rounded-xl shadow-sm overflow-hidden" }));
    /** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
    var __VLS_22 = void 0;
    /** @ts-ignore @type {typeof __VLS_components.vanCell | typeof __VLS_components.VanCell} */
    vanCell;
    // @ts-ignore
    var __VLS_23 = __VLS_asFunctionalComponent1(__VLS_22, new __VLS_22(__assign(__assign(__assign({ 'onClick': {} }, { title: "退出登录", icon: "revoke" }), { class: "text-red-500" }), { size: "large", center: true })));
    var __VLS_24 = __VLS_23.apply(void 0, __spreadArray([__assign(__assign(__assign({ 'onClick': {} }, { title: "退出登录", icon: "revoke" }), { class: "text-red-500" }), { size: "large", center: true })], __VLS_functionalComponentArgsRest(__VLS_23), false));
    var __VLS_27 = void 0;
    var __VLS_28 = ({ click: {} },
        { onClick: (__VLS_ctx.handleLogout) });
    /** @type {__VLS_StyleScopedClasses['text-red-500']} */ ;
    var __VLS_25;
    var __VLS_26;
}
var __VLS_29;
/** @ts-ignore @type {typeof __VLS_components.vanDialog | typeof __VLS_components.VanDialog | typeof __VLS_components.vanDialog | typeof __VLS_components.VanDialog} */
vanDialog;
// @ts-ignore
var __VLS_30 = __VLS_asFunctionalComponent1(__VLS_29, new __VLS_29({
    show: (__VLS_ctx.showEditDialog),
    title: "编辑资料",
    showCancelButton: true,
    beforeClose: (__VLS_ctx.onBeforeClose),
}));
var __VLS_31 = __VLS_30.apply(void 0, __spreadArray([{
        show: (__VLS_ctx.showEditDialog),
        title: "编辑资料",
        showCancelButton: true,
        beforeClose: (__VLS_ctx.onBeforeClose),
    }], __VLS_functionalComponentArgsRest(__VLS_30), false));
var __VLS_34 = __VLS_32.slots.default;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "p-6 flex flex-col items-center space-y-6" }));
/** @type {__VLS_StyleScopedClasses['p-6']} */ ;
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
/** @type {__VLS_StyleScopedClasses['items-center']} */ ;
/** @type {__VLS_StyleScopedClasses['space-y-6']} */ ;
var __VLS_35;
/** @ts-ignore @type {typeof __VLS_components.vanUploader | typeof __VLS_components.VanUploader | typeof __VLS_components.vanUploader | typeof __VLS_components.VanUploader} */
vanUploader;
// @ts-ignore
var __VLS_36 = __VLS_asFunctionalComponent1(__VLS_35, new __VLS_35({
    afterRead: (__VLS_ctx.onAvatarUpload),
    maxCount: "1",
    showUpload: (false),
}));
var __VLS_37 = __VLS_36.apply(void 0, __spreadArray([{
        afterRead: (__VLS_ctx.onAvatarUpload),
        maxCount: "1",
        showUpload: (false),
    }], __VLS_functionalComponentArgsRest(__VLS_36), false));
var __VLS_40 = __VLS_38.slots.default;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "relative group cursor-pointer" }));
/** @type {__VLS_StyleScopedClasses['relative']} */ ;
/** @type {__VLS_StyleScopedClasses['group']} */ ;
/** @type {__VLS_StyleScopedClasses['cursor-pointer']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "w-24 h-24 rounded-full overflow-hidden border-2 border-gray-200" }));
/** @type {__VLS_StyleScopedClasses['w-24']} */ ;
/** @type {__VLS_StyleScopedClasses['h-24']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
/** @type {__VLS_StyleScopedClasses['border-2']} */ ;
/** @type {__VLS_StyleScopedClasses['border-gray-200']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.img)(__assign({ src: (__VLS_ctx.editForm.userAvatar || ((_b = __VLS_ctx.userStore.userInfo) === null || _b === void 0 ? void 0 : _b.userAvatar) || __VLS_ctx.defaultAvatar) }, { class: "w-full h-full object-cover" }));
/** @type {__VLS_StyleScopedClasses['w-full']} */ ;
/** @type {__VLS_StyleScopedClasses['h-full']} */ ;
/** @type {__VLS_StyleScopedClasses['object-cover']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute inset-0 bg-black/40 flex items-center justify-center rounded-full opacity-0 group-hover:opacity-100 transition-opacity" }));
/** @type {__VLS_StyleScopedClasses['absolute']} */ ;
/** @type {__VLS_StyleScopedClasses['inset-0']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-black/40']} */ ;
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['items-center']} */ ;
/** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['opacity-0']} */ ;
/** @type {__VLS_StyleScopedClasses['group-hover:opacity-100']} */ ;
/** @type {__VLS_StyleScopedClasses['transition-opacity']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-white text-xs" }));
/** @type {__VLS_StyleScopedClasses['text-white']} */ ;
/** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
// @ts-ignore
[userStore, userStore, userStore, defaultAvatar, openEditDialog, handleLogout, showEditDialog, onBeforeClose, onAvatarUpload, editForm,];
var __VLS_38;
var __VLS_41;
/** @ts-ignore @type {typeof __VLS_components.vanField | typeof __VLS_components.VanField} */
vanField;
// @ts-ignore
var __VLS_42 = __VLS_asFunctionalComponent1(__VLS_41, new __VLS_41({
    modelValue: (__VLS_ctx.editForm.userName),
    label: "昵称",
    placeholder: "请输入新昵称",
    inputAlign: "right",
    border: true,
}));
var __VLS_43 = __VLS_42.apply(void 0, __spreadArray([{
        modelValue: (__VLS_ctx.editForm.userName),
        label: "昵称",
        placeholder: "请输入新昵称",
        inputAlign: "right",
        border: true,
    }], __VLS_functionalComponentArgsRest(__VLS_42), false));
// @ts-ignore
[editForm,];
var __VLS_32;
// @ts-ignore
[];
var __VLS_export = (await import('vue')).defineComponent({});
export default {};
