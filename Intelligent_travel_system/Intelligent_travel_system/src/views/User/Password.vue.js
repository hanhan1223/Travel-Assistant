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
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../../stores/userStore';
import { showToast } from 'vant';
var router = useRouter();
var userStore = useUserStore();
var loading = ref(false);
var form = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
});
var onClickLeft = function () {
    router.back();
};
// 校验密码长度 (8-20位)
var validatorPassword = function (val) {
    return val.length >= 8 && val.length <= 20;
};
// 校验两次密码是否一致
var validatorSame = function (val) {
    return val === form.newPassword;
};
var onSubmit = function () { return __awaiter(void 0, void 0, void 0, function () {
    var success, error_1;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                // 二次校验，防止空值提交（虽然 van-form rules 已经挡住了）
                if (!form.oldPassword || !form.newPassword || !form.confirmPassword) {
                    showToast('请填写完整信息');
                    return [2 /*return*/];
                }
                loading.value = true;
                _a.label = 1;
            case 1:
                _a.trys.push([1, 3, , 4]);
                return [4 /*yield*/, userStore.updatePassword({
                        oldPassword: form.oldPassword,
                        newPassword: form.newPassword,
                        confirmPassword: form.confirmPassword
                    })];
            case 2:
                success = _a.sent();
                // 如果 Store 返回 true，说明修改成功并正在跳转/登出，不需要取消 loading
                if (!success) {
                    loading.value = false;
                }
                return [3 /*break*/, 4];
            case 3:
                error_1 = _a.sent();
                loading.value = false;
                showToast('网络请求异常');
                return [3 /*break*/, 4];
            case 4: return [2 /*return*/];
        }
    });
}); };
var __VLS_ctx = __assign(__assign({}, {}), {});
var __VLS_components;
var __VLS_intrinsics;
var __VLS_directives;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "min-h-screen bg-gray-50" }));
/** @type {__VLS_StyleScopedClasses['min-h-screen']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
var __VLS_0;
/** @ts-ignore @type {typeof __VLS_components.vanNavBar | typeof __VLS_components.VanNavBar} */
vanNavBar;
// @ts-ignore
var __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0(__assign({ 'onClickLeft': {} }, { title: "修改密码", leftText: "返回", leftArrow: true, fixed: true, placeholder: true })));
var __VLS_2 = __VLS_1.apply(void 0, __spreadArray([__assign({ 'onClickLeft': {} }, { title: "修改密码", leftText: "返回", leftArrow: true, fixed: true, placeholder: true })], __VLS_functionalComponentArgsRest(__VLS_1), false));
var __VLS_5;
var __VLS_6 = ({ clickLeft: {} },
    { onClickLeft: (__VLS_ctx.onClickLeft) });
var __VLS_3;
var __VLS_4;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "mt-4 px-2" }));
/** @type {__VLS_StyleScopedClasses['mt-4']} */ ;
/** @type {__VLS_StyleScopedClasses['px-2']} */ ;
var __VLS_7;
/** @ts-ignore @type {typeof __VLS_components.vanForm | typeof __VLS_components.VanForm | typeof __VLS_components.vanForm | typeof __VLS_components.VanForm} */
vanForm;
// @ts-ignore
var __VLS_8 = __VLS_asFunctionalComponent1(__VLS_7, new __VLS_7(__assign({ 'onSubmit': {} })));
var __VLS_9 = __VLS_8.apply(void 0, __spreadArray([__assign({ 'onSubmit': {} })], __VLS_functionalComponentArgsRest(__VLS_8), false));
var __VLS_12;
var __VLS_13 = ({ submit: {} },
    { onSubmit: (__VLS_ctx.onSubmit) });
var __VLS_14 = __VLS_10.slots.default;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-white rounded-xl shadow-sm overflow-hidden mb-6" }));
/** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
/** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
/** @type {__VLS_StyleScopedClasses['mb-6']} */ ;
var __VLS_15;
/** @ts-ignore @type {typeof __VLS_components.vanField | typeof __VLS_components.VanField} */
vanField;
// @ts-ignore
var __VLS_16 = __VLS_asFunctionalComponent1(__VLS_15, new __VLS_15({
    modelValue: (__VLS_ctx.form.oldPassword),
    type: "password",
    name: "oldPassword",
    label: "原密码",
    placeholder: "请输入原密码",
    rules: ([{ required: true, message: '请填写原密码' }]),
}));
var __VLS_17 = __VLS_16.apply(void 0, __spreadArray([{
        modelValue: (__VLS_ctx.form.oldPassword),
        type: "password",
        name: "oldPassword",
        label: "原密码",
        placeholder: "请输入原密码",
        rules: ([{ required: true, message: '请填写原密码' }]),
    }], __VLS_functionalComponentArgsRest(__VLS_16), false));
var __VLS_20;
/** @ts-ignore @type {typeof __VLS_components.vanField | typeof __VLS_components.VanField} */
vanField;
// @ts-ignore
var __VLS_21 = __VLS_asFunctionalComponent1(__VLS_20, new __VLS_20({
    modelValue: (__VLS_ctx.form.newPassword),
    type: "password",
    name: "newPassword",
    label: "新密码",
    placeholder: "8-20位，建议包含字母和数字",
    rules: ([
        { required: true, message: '请填写新密码' },
        { validator: __VLS_ctx.validatorPassword, message: '密码长度需为8-20位' }
    ]),
}));
var __VLS_22 = __VLS_21.apply(void 0, __spreadArray([{
        modelValue: (__VLS_ctx.form.newPassword),
        type: "password",
        name: "newPassword",
        label: "新密码",
        placeholder: "8-20位，建议包含字母和数字",
        rules: ([
            { required: true, message: '请填写新密码' },
            { validator: __VLS_ctx.validatorPassword, message: '密码长度需为8-20位' }
        ]),
    }], __VLS_functionalComponentArgsRest(__VLS_21), false));
var __VLS_25;
/** @ts-ignore @type {typeof __VLS_components.vanField | typeof __VLS_components.VanField} */
vanField;
// @ts-ignore
var __VLS_26 = __VLS_asFunctionalComponent1(__VLS_25, new __VLS_25({
    modelValue: (__VLS_ctx.form.confirmPassword),
    type: "password",
    name: "confirmPassword",
    label: "确认密码",
    placeholder: "请再次输入新密码",
    rules: ([
        { required: true, message: '请确认新密码' },
        { validator: __VLS_ctx.validatorSame, message: '两次输入的密码不一致' }
    ]),
}));
var __VLS_27 = __VLS_26.apply(void 0, __spreadArray([{
        modelValue: (__VLS_ctx.form.confirmPassword),
        type: "password",
        name: "confirmPassword",
        label: "确认密码",
        placeholder: "请再次输入新密码",
        rules: ([
            { required: true, message: '请确认新密码' },
            { validator: __VLS_ctx.validatorSame, message: '两次输入的密码不一致' }
        ]),
    }], __VLS_functionalComponentArgsRest(__VLS_26), false));
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "px-4" }));
/** @type {__VLS_StyleScopedClasses['px-4']} */ ;
var __VLS_30;
/** @ts-ignore @type {typeof __VLS_components.vanButton | typeof __VLS_components.VanButton | typeof __VLS_components.vanButton | typeof __VLS_components.VanButton} */
vanButton;
// @ts-ignore
var __VLS_31 = __VLS_asFunctionalComponent1(__VLS_30, new __VLS_30({
    round: true,
    block: true,
    type: "primary",
    nativeType: "submit",
    color: "#4f46e5",
    loading: (__VLS_ctx.loading),
    loadingText: "提交中...",
}));
var __VLS_32 = __VLS_31.apply(void 0, __spreadArray([{
        round: true,
        block: true,
        type: "primary",
        nativeType: "submit",
        color: "#4f46e5",
        loading: (__VLS_ctx.loading),
        loadingText: "提交中...",
    }], __VLS_functionalComponentArgsRest(__VLS_31), false));
var __VLS_35 = __VLS_33.slots.default;
// @ts-ignore
[onClickLeft, onSubmit, form, form, form, validatorPassword, validatorSame, loading,];
var __VLS_33;
// @ts-ignore
[];
var __VLS_10;
var __VLS_11;
__VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)(__assign({ class: "text-center text-gray-400 text-xs mt-6" }));
/** @type {__VLS_StyleScopedClasses['text-center']} */ ;
/** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
/** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
/** @type {__VLS_StyleScopedClasses['mt-6']} */ ;
// @ts-ignore
[];
var __VLS_export = (await import('vue')).defineComponent({});
export default {};
