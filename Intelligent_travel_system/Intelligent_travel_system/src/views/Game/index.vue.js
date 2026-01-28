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
var _a, _b, _c, _d, _e, _f, _g, _h;
import { ref, onMounted, watch, computed } from 'vue';
import { useGameStore } from '../../stores/gameStore';
import { useRouter } from 'vue-router';
import { showLoadingToast } from 'vant';
var gameStore = useGameStore();
var router = useRouter();
// 内部状态
var selectedAnswer = ref('');
var showResultModal = ref(false); // 单题结果弹窗
var currentResult = ref(null); // 单题结果数据
// 游戏设置
var gameMode = ref('normal');
var projectName = ref('');
var rankingTab = ref('weekly');
// 计时器
var timeSpent = ref(0);
var timer = null;
onMounted(function () {
    gameStore.fetchRankings('weekly');
});
watch(rankingTab, function (newVal) {
    gameStore.fetchRankings(newVal);
});
var handleStartGame = function () { return __awaiter(void 0, void 0, void 0, function () {
    var toast, success;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                toast = showLoadingToast({
                    message: '准备题库中...',
                    forbidClick: true,
                    duration: 0 // 持续展示
                });
                return [4 /*yield*/, gameStore.startGame(gameMode.value, 1, projectName.value)];
            case 1:
                success = _a.sent();
                toast.close();
                if (success) {
                    startTimer();
                }
                return [2 /*return*/];
        }
    });
}); };
var handleBack = function () {
    router.back();
};
var startTimer = function () {
    timeSpent.value = 0;
    clearInterval(timer);
    timer = setInterval(function () {
        timeSpent.value++;
    }, 1000);
};
var handleOptionSelect = function (opt) {
    selectedAnswer.value = opt;
};
var submit = function () { return __awaiter(void 0, void 0, void 0, function () {
    var toast, result;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (!selectedAnswer.value)
                    return [2 /*return*/];
                clearInterval(timer);
                toast = showLoadingToast({
                    message: '提交答案中...',
                    forbidClick: true,
                });
                return [4 /*yield*/, gameStore.submitAnswer(selectedAnswer.value, timeSpent.value)];
            case 1:
                result = _a.sent();
                toast.close();
                if (result) {
                    currentResult.value = result;
                    showResultModal.value = true;
                }
                return [2 /*return*/];
        }
    });
}); };
var next = function () {
    showResultModal.value = false;
    selectedAnswer.value = '';
    currentResult.value = null;
    var hasNext = gameStore.nextQuestion();
    if (hasNext) {
        startTimer();
    }
    else {
        gameStore.completeGame();
    }
};
// 工具：数字转字母 (0->A, 1->B)
var indexToChar = function (i) { return String.fromCharCode(65 + i); };
// 🛠️ 工具：格式化正确率显示
var formatAccuracy = function (val) {
    if (!val)
        return '0%';
    if (val > 1)
        return val.toFixed(0) + '%';
    return (val * 100).toFixed(0) + '%';
};
var modeOptions = [
    { text: '普通模式', value: 'normal', icon: '🌟', desc: '经典玩法，轻松挑战' },
    { text: '挑战模式', value: 'challenge', icon: '🔥', desc: '限时答题，更高难度' },
    { text: '每日一练', value: 'daily', icon: '📅', desc: '每天更新，保持手感' },
];
var currentModeDesc = computed(function () {
    var _a;
    return ((_a = modeOptions.find(function (m) { return m.value === gameMode.value; })) === null || _a === void 0 ? void 0 : _a.desc) || '';
});
var __VLS_ctx = __assign(__assign({}, {}), {});
var __VLS_components;
var __VLS_intrinsics;
var __VLS_directives;
/** @type {__VLS_StyleScopedClasses['forced-white-nav']} */ ;
/** @type {__VLS_StyleScopedClasses['forced-white-nav']} */ ;
/** @type {__VLS_StyleScopedClasses['forced-white-nav']} */ ;
/** @type {__VLS_StyleScopedClasses['custom-scrollbar']} */ ;
/** @type {__VLS_StyleScopedClasses['custom-scrollbar']} */ ;
/** @type {__VLS_StyleScopedClasses['ranking-tabs']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "h-screen flex flex-col bg-slate-50 font-sans relative overflow-hidden" }));
/** @type {__VLS_StyleScopedClasses['h-screen']} */ ;
/** @type {__VLS_StyleScopedClasses['flex']} */ ;
/** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-slate-50']} */ ;
/** @type {__VLS_StyleScopedClasses['font-sans']} */ ;
/** @type {__VLS_StyleScopedClasses['relative']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute inset-0 pointer-events-none overflow-hidden z-0" }));
/** @type {__VLS_StyleScopedClasses['absolute']} */ ;
/** @type {__VLS_StyleScopedClasses['inset-0']} */ ;
/** @type {__VLS_StyleScopedClasses['pointer-events-none']} */ ;
/** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
/** @type {__VLS_StyleScopedClasses['z-0']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute -top-1/4 -left-1/4 w-1/2 h-1/2 bg-blue-200/30 rounded-full blur-3xl" }));
/** @type {__VLS_StyleScopedClasses['absolute']} */ ;
/** @type {__VLS_StyleScopedClasses['-top-1/4']} */ ;
/** @type {__VLS_StyleScopedClasses['-left-1/4']} */ ;
/** @type {__VLS_StyleScopedClasses['w-1/2']} */ ;
/** @type {__VLS_StyleScopedClasses['h-1/2']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-blue-200/30']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['blur-3xl']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute top-1/4 -right-1/4 w-2/3 h-2/3 bg-indigo-200/30 rounded-full blur-3xl" }));
/** @type {__VLS_StyleScopedClasses['absolute']} */ ;
/** @type {__VLS_StyleScopedClasses['top-1/4']} */ ;
/** @type {__VLS_StyleScopedClasses['-right-1/4']} */ ;
/** @type {__VLS_StyleScopedClasses['w-2/3']} */ ;
/** @type {__VLS_StyleScopedClasses['h-2/3']} */ ;
/** @type {__VLS_StyleScopedClasses['bg-indigo-200/30']} */ ;
/** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
/** @type {__VLS_StyleScopedClasses['blur-3xl']} */ ;
if (!__VLS_ctx.gameStore.isPlaying && !__VLS_ctx.gameStore.lastResult) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex flex-col h-full z-10 relative" }));
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
    /** @type {__VLS_StyleScopedClasses['h-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['z-10']} */ ;
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-gradient-to-br from-blue-600 to-indigo-700 pb-8 pt-safe-top rounded-b-[3rem] shadow-xl relative overflow-hidden" }));
    /** @type {__VLS_StyleScopedClasses['bg-gradient-to-br']} */ ;
    /** @type {__VLS_StyleScopedClasses['from-blue-600']} */ ;
    /** @type {__VLS_StyleScopedClasses['to-indigo-700']} */ ;
    /** @type {__VLS_StyleScopedClasses['pb-8']} */ ;
    /** @type {__VLS_StyleScopedClasses['pt-safe-top']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-b-[3rem]']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute inset-0 opacity-10 bg-[url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgdmlld0JveD0iMCAwIDQwIDQwIj48ZyBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPjxjaXJYbGUgY3g9IjIwIiBjeT0iMjAiIHI9IjIiIGZpbGw9IiNGRkZGRkYiLz48L2c+PC9zdmc+')]" }));
    /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
    /** @type {__VLS_StyleScopedClasses['inset-0']} */ ;
    /** @type {__VLS_StyleScopedClasses['opacity-10']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-[url(\'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgdmlld0JveD0iMCAwIDQwIDQwIj48ZyBmaWxsPSJub25lIiBmaWxsLXJ1bGU9ImV2ZW5vZGQiPjxjaXJYbGUgY3g9IjIwIiBjeT0iMjAiIHI9IjIiIGZpbGw9IiNGRkZGRkYiLz48L2c+PC9zdmc+\')]']} */ ;
    var __VLS_0 = void 0;
    /** @ts-ignore @type {typeof __VLS_components.vanNavBar | typeof __VLS_components.VanNavBar} */
    vanNavBar;
    // @ts-ignore
    var __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0(__assign(__assign(__assign({ 'onClickLeft': {} }, { title: "非遗知识大闯关", leftText: "返回", leftArrow: true }), { class: "bg-transparent text-white border-none forced-white-nav" }), { border: (false) })));
    var __VLS_2 = __VLS_1.apply(void 0, __spreadArray([__assign(__assign(__assign({ 'onClickLeft': {} }, { title: "非遗知识大闯关", leftText: "返回", leftArrow: true }), { class: "bg-transparent text-white border-none forced-white-nav" }), { border: (false) })], __VLS_functionalComponentArgsRest(__VLS_1), false));
    var __VLS_5 = void 0;
    var __VLS_6 = ({ clickLeft: {} },
        { onClickLeft: (__VLS_ctx.handleBack) });
    /** @type {__VLS_StyleScopedClasses['bg-transparent']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-none']} */ ;
    /** @type {__VLS_StyleScopedClasses['forced-white-nav']} */ ;
    var __VLS_3;
    var __VLS_4;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "px-6 mt-2 text-white" }));
    /** @type {__VLS_StyleScopedClasses['px-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['mt-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-white']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.h1, __VLS_intrinsics.h1)(__assign({ class: "text-2xl font-bold mb-1" }));
    /** @type {__VLS_StyleScopedClasses['text-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-1']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)(__assign({ class: "text-blue-100 text-sm opacity-90" }));
    /** @type {__VLS_StyleScopedClasses['text-blue-100']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['opacity-90']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex-1 overflow-hidden flex flex-col px-4 -mt-6" }));
    /** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
    /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
    /** @type {__VLS_StyleScopedClasses['px-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['-mt-6']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-white/90 backdrop-blur-md rounded-2xl p-5 shadow-lg mb-4 relative z-10 border border-white/50" }));
    /** @type {__VLS_StyleScopedClasses['bg-white/90']} */ ;
    /** @type {__VLS_StyleScopedClasses['backdrop-blur-md']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['p-5']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-lg']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    /** @type {__VLS_StyleScopedClasses['z-10']} */ ;
    /** @type {__VLS_StyleScopedClasses['border']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-white/50']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "mb-5" }));
    /** @type {__VLS_StyleScopedClasses['mb-5']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "text-sm text-gray-600 mb-3 font-bold flex items-center" }));
    /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-600']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "mr-1" }));
    /** @type {__VLS_StyleScopedClasses['mr-1']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "grid grid-cols-3 gap-3" }));
    /** @type {__VLS_StyleScopedClasses['grid']} */ ;
    /** @type {__VLS_StyleScopedClasses['grid-cols-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['gap-3']} */ ;
    var _loop_1 = function (mode) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign(__assign({ onClick: function () {
                var _a = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    _a[_i] = arguments[_i];
                }
                var $event = _a[0];
                if (!(!__VLS_ctx.gameStore.isPlaying && !__VLS_ctx.gameStore.lastResult))
                    return;
                __VLS_ctx.gameMode = mode.value;
                // @ts-ignore
                [gameStore, gameStore, handleBack, modeOptions, gameMode,];
            } }, { key: (mode.value) }), { class: ([
                'flex flex-col items-center justify-center p-3 rounded-xl transition-all cursor-pointer border-2',
                __VLS_ctx.gameMode === mode.value
                    ? 'bg-blue-50 border-blue-500 text-blue-700 shadow-md scale-[1.02]'
                    : 'bg-gray-50 border-transparent text-gray-500 hover:bg-gray-100 hover:border-gray-200'
            ]) }));
        /** @type {__VLS_StyleScopedClasses['flex']} */ ;
        /** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
        /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['p-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
        /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
        /** @type {__VLS_StyleScopedClasses['cursor-pointer']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-2']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-2xl mb-1" }));
        /** @type {__VLS_StyleScopedClasses['text-2xl']} */ ;
        /** @type {__VLS_StyleScopedClasses['mb-1']} */ ;
        (mode.icon);
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-xs font-bold" }));
        /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
        (mode.text);
        // @ts-ignore
        [gameMode,];
    };
    for (var _i = 0, _j = __VLS_vFor((__VLS_ctx.modeOptions)); _i < _j.length; _i++) {
        var mode = _j[_i][0];
        _loop_1(mode);
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)(__assign({ class: "text-xs text-gray-400 mt-2 text-center" }));
    /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
    /** @type {__VLS_StyleScopedClasses['mt-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-center']} */ ;
    (__VLS_ctx.currentModeDesc);
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "mb-5" }));
    /** @type {__VLS_StyleScopedClasses['mb-5']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "text-sm text-gray-600 mb-3 font-bold flex items-center" }));
    /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-600']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "mr-1" }));
    /** @type {__VLS_StyleScopedClasses['mr-1']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex items-center bg-gray-50/80 rounded-xl px-4 py-2 border border-gray-200 focus-within:border-blue-400 transition-colors" }));
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-gray-50/80']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['px-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['border']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-gray-200']} */ ;
    /** @type {__VLS_StyleScopedClasses['focus-within:border-blue-400']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-lg mr-3 text-gray-400" }));
    /** @type {__VLS_StyleScopedClasses['text-lg']} */ ;
    /** @type {__VLS_StyleScopedClasses['mr-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)(__assign({ value: (__VLS_ctx.projectName), type: "text", placeholder: "输入非遗名称，如：剪纸" }, { class: "w-full bg-transparent border-none outline-none text-sm py-1 text-gray-700 placeholder-gray-400" }));
    /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-transparent']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-none']} */ ;
    /** @type {__VLS_StyleScopedClasses['outline-none']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-1']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-700']} */ ;
    /** @type {__VLS_StyleScopedClasses['placeholder-gray-400']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign({ onClick: (__VLS_ctx.handleStartGame) }, { class: "w-full bg-gradient-to-r from-yellow-400 to-orange-500 hover:from-yellow-500 hover:to-orange-600 text-white py-3.5 rounded-xl font-bold shadow-lg shadow-orange-200/50 active:scale-[0.98] transition-all flex items-center justify-center gap-2 text-lg" }));
    /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-gradient-to-r']} */ ;
    /** @type {__VLS_StyleScopedClasses['from-yellow-400']} */ ;
    /** @type {__VLS_StyleScopedClasses['to-orange-500']} */ ;
    /** @type {__VLS_StyleScopedClasses['hover:from-yellow-500']} */ ;
    /** @type {__VLS_StyleScopedClasses['hover:to-orange-600']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-3.5']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-lg']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-orange-200/50']} */ ;
    /** @type {__VLS_StyleScopedClasses['active:scale-[0.98]']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['gap-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-lg']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-white/90 backdrop-blur-md flex-1 rounded-2xl shadow-lg flex flex-col overflow-hidden border border-white/50 mb-safe-bottom" }));
    /** @type {__VLS_StyleScopedClasses['bg-white/90']} */ ;
    /** @type {__VLS_StyleScopedClasses['backdrop-blur-md']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-lg']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
    /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
    /** @type {__VLS_StyleScopedClasses['border']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-white/50']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-safe-bottom']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "border-b border-gray-100" }));
    /** @type {__VLS_StyleScopedClasses['border-b']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-gray-100']} */ ;
    var __VLS_7 = void 0;
    /** @ts-ignore @type {typeof __VLS_components.vanTabs | typeof __VLS_components.VanTabs | typeof __VLS_components.vanTabs | typeof __VLS_components.VanTabs} */
    vanTabs;
    // @ts-ignore
    var __VLS_8 = __VLS_asFunctionalComponent1(__VLS_7, new __VLS_7(__assign({ active: (__VLS_ctx.rankingTab), shrink: true, animated: true, color: "#2563eb", titleActiveColor: "#2563eb", lineWidth: "40px" }, { class: "ranking-tabs" })));
    var __VLS_9 = __VLS_8.apply(void 0, __spreadArray([__assign({ active: (__VLS_ctx.rankingTab), shrink: true, animated: true, color: "#2563eb", titleActiveColor: "#2563eb", lineWidth: "40px" }, { class: "ranking-tabs" })], __VLS_functionalComponentArgsRest(__VLS_8), false));
    /** @type {__VLS_StyleScopedClasses['ranking-tabs']} */ ;
    var __VLS_12 = __VLS_10.slots.default;
    var __VLS_13 = void 0;
    /** @ts-ignore @type {typeof __VLS_components.vanTab | typeof __VLS_components.VanTab | typeof __VLS_components.vanTab | typeof __VLS_components.VanTab} */
    vanTab;
    // @ts-ignore
    var __VLS_14 = __VLS_asFunctionalComponent1(__VLS_13, new __VLS_13({
        title: "🏆 本周榜单",
        name: "weekly",
    }));
    var __VLS_15 = __VLS_14.apply(void 0, __spreadArray([{
            title: "🏆 本周榜单",
            name: "weekly",
        }], __VLS_functionalComponentArgsRest(__VLS_14), false));
    var __VLS_18 = void 0;
    /** @ts-ignore @type {typeof __VLS_components.vanTab | typeof __VLS_components.VanTab | typeof __VLS_components.vanTab | typeof __VLS_components.VanTab} */
    vanTab;
    // @ts-ignore
    var __VLS_19 = __VLS_asFunctionalComponent1(__VLS_18, new __VLS_18({
        title: "📅 月度榜单",
        name: "monthly",
    }));
    var __VLS_20 = __VLS_19.apply(void 0, __spreadArray([{
            title: "📅 月度榜单",
            name: "monthly",
        }], __VLS_functionalComponentArgsRest(__VLS_19), false));
    // @ts-ignore
    [currentModeDesc, projectName, handleStartGame, rankingTab,];
    var __VLS_10;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-blue-50/50 px-4 py-2 flex justify-between items-center text-xs text-gray-500 border-b border-blue-100/50" }));
    /** @type {__VLS_StyleScopedClasses['bg-blue-50/50']} */ ;
    /** @type {__VLS_StyleScopedClasses['px-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['justify-between']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-500']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-b']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-blue-100/50']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)(__assign({ class: "text-blue-600 text-sm" }));
    /** @type {__VLS_StyleScopedClasses['text-blue-600']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
    (__VLS_ctx.gameStore.myRank ? "\u7B2C ".concat(__VLS_ctx.gameStore.myRank, " \u540D") : '未上榜');
    if (__VLS_ctx.gameStore.myRank) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-orange-500" }));
        /** @type {__VLS_StyleScopedClasses['text-orange-500']} */ ;
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex-1 overflow-y-auto custom-scrollbar" }));
    /** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
    /** @type {__VLS_StyleScopedClasses['overflow-y-auto']} */ ;
    /** @type {__VLS_StyleScopedClasses['custom-scrollbar']} */ ;
    if (__VLS_ctx.gameStore.rankingList.length === 0) {
        var __VLS_23 = void 0;
        /** @ts-ignore @type {typeof __VLS_components.vanEmpty | typeof __VLS_components.VanEmpty} */
        vanEmpty;
        // @ts-ignore
        var __VLS_24 = __VLS_asFunctionalComponent1(__VLS_23, new __VLS_23({
            description: "暂无排名数据",
            image: "https://gw.alipayobjects.com/zos/antfincdn/ZHrcdLPrvN/empty.svg",
        }));
        var __VLS_25 = __VLS_24.apply(void 0, __spreadArray([{
                description: "暂无排名数据",
                image: "https://gw.alipayobjects.com/zos/antfincdn/ZHrcdLPrvN/empty.svg",
            }], __VLS_functionalComponentArgsRest(__VLS_24), false));
    }
    else {
        for (var _k = 0, _l = __VLS_vFor((__VLS_ctx.gameStore.rankingList)); _k < _l.length; _k++) {
            var _m = _l[_k], item = _m[0], idx = _m[1];
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ key: (item.userId) }, { class: "flex items-center py-3 px-4 border-b border-gray-50 last:border-0 hover:bg-gray-50/80 transition-colors relative" }));
            /** @type {__VLS_StyleScopedClasses['flex']} */ ;
            /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
            /** @type {__VLS_StyleScopedClasses['py-3']} */ ;
            /** @type {__VLS_StyleScopedClasses['px-4']} */ ;
            /** @type {__VLS_StyleScopedClasses['border-b']} */ ;
            /** @type {__VLS_StyleScopedClasses['border-gray-50']} */ ;
            /** @type {__VLS_StyleScopedClasses['last:border-0']} */ ;
            /** @type {__VLS_StyleScopedClasses['hover:bg-gray-50/80']} */ ;
            /** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
            /** @type {__VLS_StyleScopedClasses['relative']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "w-8 flex justify-center" }));
            /** @type {__VLS_StyleScopedClasses['w-8']} */ ;
            /** @type {__VLS_StyleScopedClasses['flex']} */ ;
            /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
            if (item.rank === 1) {
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-2xl" }));
                /** @type {__VLS_StyleScopedClasses['text-2xl']} */ ;
            }
            else if (item.rank === 2) {
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-2xl" }));
                /** @type {__VLS_StyleScopedClasses['text-2xl']} */ ;
            }
            else if (item.rank === 3) {
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-2xl" }));
                /** @type {__VLS_StyleScopedClasses['text-2xl']} */ ;
            }
            else {
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-gray-400 font-bold text-sm italic" }));
                /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
                /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
                /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
                /** @type {__VLS_StyleScopedClasses['italic']} */ ;
                (item.rank);
            }
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "relative mx-3" }));
            /** @type {__VLS_StyleScopedClasses['relative']} */ ;
            /** @type {__VLS_StyleScopedClasses['mx-3']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.img)(__assign({ src: (item.avatar || 'https://via.placeholder.com/40') }, { class: "w-11 h-11 rounded-full bg-gray-200 object-cover border-2 border-white shadow-sm" }));
            /** @type {__VLS_StyleScopedClasses['w-11']} */ ;
            /** @type {__VLS_StyleScopedClasses['h-11']} */ ;
            /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
            /** @type {__VLS_StyleScopedClasses['bg-gray-200']} */ ;
            /** @type {__VLS_StyleScopedClasses['object-cover']} */ ;
            /** @type {__VLS_StyleScopedClasses['border-2']} */ ;
            /** @type {__VLS_StyleScopedClasses['border-white']} */ ;
            /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
            if (item.rank <= 3) {
                __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute -bottom-1 -right-1 w-5 h-5 bg-yellow-400 rounded-full flex items-center justify-center border-2 border-white shadow-sm" }));
                /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
                /** @type {__VLS_StyleScopedClasses['-bottom-1']} */ ;
                /** @type {__VLS_StyleScopedClasses['-right-1']} */ ;
                /** @type {__VLS_StyleScopedClasses['w-5']} */ ;
                /** @type {__VLS_StyleScopedClasses['h-5']} */ ;
                /** @type {__VLS_StyleScopedClasses['bg-yellow-400']} */ ;
                /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
                /** @type {__VLS_StyleScopedClasses['flex']} */ ;
                /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
                /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
                /** @type {__VLS_StyleScopedClasses['border-2']} */ ;
                /** @type {__VLS_StyleScopedClasses['border-white']} */ ;
                /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
                __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-[10px]" }));
                /** @type {__VLS_StyleScopedClasses['text-[10px]']} */ ;
            }
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex-1" }));
            /** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "font-bold text-gray-800 text-[15px] line-clamp-1" }));
            /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-gray-800']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-[15px]']} */ ;
            /** @type {__VLS_StyleScopedClasses['line-clamp-1']} */ ;
            (item.username);
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "text-xs text-gray-400 mt-0.5 flex items-center gap-2" }));
            /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
            /** @type {__VLS_StyleScopedClasses['mt-0.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['flex']} */ ;
            /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
            /** @type {__VLS_StyleScopedClasses['gap-2']} */ ;
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "bg-blue-100 text-blue-600 px-1.5 py-0.5 rounded-md font-medium" }));
            /** @type {__VLS_StyleScopedClasses['bg-blue-100']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-blue-600']} */ ;
            /** @type {__VLS_StyleScopedClasses['px-1.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['py-0.5']} */ ;
            /** @type {__VLS_StyleScopedClasses['rounded-md']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
            (item.level);
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
            ((item.bestAccuracy * 100).toFixed(0));
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "font-black text-blue-600 text-base" }));
            /** @type {__VLS_StyleScopedClasses['font-black']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-blue-600']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-base']} */ ;
            (item.points);
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-xs font-normal text-gray-500" }));
            /** @type {__VLS_StyleScopedClasses['text-xs']} */ ;
            /** @type {__VLS_StyleScopedClasses['font-normal']} */ ;
            /** @type {__VLS_StyleScopedClasses['text-gray-500']} */ ;
            // @ts-ignore
            [gameStore, gameStore, gameStore, gameStore, gameStore,];
        }
    }
}
else if (__VLS_ctx.gameStore.isPlaying && __VLS_ctx.gameStore.currentQuestion) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex flex-col h-full bg-slate-100 z-10 relative" }));
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
    /** @type {__VLS_StyleScopedClasses['h-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-slate-100']} */ ;
    /** @type {__VLS_StyleScopedClasses['z-10']} */ ;
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    var __VLS_28 = void 0;
    /** @ts-ignore @type {typeof __VLS_components.vanNavBar | typeof __VLS_components.VanNavBar | typeof __VLS_components.vanNavBar | typeof __VLS_components.VanNavBar} */
    vanNavBar;
    // @ts-ignore
    var __VLS_29 = __VLS_asFunctionalComponent1(__VLS_28, new __VLS_28(__assign(__assign(__assign({ 'onClickLeft': {} }, { title: ("\u7B2C ".concat(__VLS_ctx.gameStore.currentQuestionIndex + 1, " \u9898")), leftArrow: true }), { class: "bg-white text-gray-800" }), { fixed: true, placeholder: true })));
    var __VLS_30 = __VLS_29.apply(void 0, __spreadArray([__assign(__assign(__assign({ 'onClickLeft': {} }, { title: ("\u7B2C ".concat(__VLS_ctx.gameStore.currentQuestionIndex + 1, " \u9898")), leftArrow: true }), { class: "bg-white text-gray-800" }), { fixed: true, placeholder: true })], __VLS_functionalComponentArgsRest(__VLS_29), false));
    var __VLS_33 = void 0;
    var __VLS_34 = ({ clickLeft: {} },
        { onClickLeft: (__VLS_ctx.handleBack) });
    /** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-800']} */ ;
    var __VLS_35 = __VLS_31.slots.default;
    {
        var __VLS_36 = __VLS_31.slots.right;
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-blue-50 border border-blue-100 rounded-full px-3 py-1 text-sm text-blue-600 flex items-center gap-1 font-mono font-bold" }));
        /** @type {__VLS_StyleScopedClasses['bg-blue-50']} */ ;
        /** @type {__VLS_StyleScopedClasses['border']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-blue-100']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['px-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['py-1']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-blue-600']} */ ;
        /** @type {__VLS_StyleScopedClasses['flex']} */ ;
        /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['gap-1']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-mono']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
        (__VLS_ctx.timeSpent);
        // @ts-ignore
        [gameStore, gameStore, gameStore, handleBack, timeSpent,];
    }
    // @ts-ignore
    [];
    var __VLS_31;
    var __VLS_32;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex-1 p-5 flex flex-col justify-center max-w-lg mx-auto w-full" }));
    /** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
    /** @type {__VLS_StyleScopedClasses['p-5']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
    /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['max-w-lg']} */ ;
    /** @type {__VLS_StyleScopedClasses['mx-auto']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-white p-6 rounded-2xl shadow-lg border border-white relative overflow-hidden" }));
    /** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['p-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-lg']} */ ;
    /** @type {__VLS_StyleScopedClasses['border']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute top-0 left-0 h-1 bg-blue-500 transition-all duration-300" }, { style: ({ width: "".concat(((__VLS_ctx.gameStore.currentQuestionIndex + 1) / (((_a = __VLS_ctx.gameStore.currentSession) === null || _a === void 0 ? void 0 : _a.totalQuestions) || 5)) * 100, "%") }) }));
    /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
    /** @type {__VLS_StyleScopedClasses['top-0']} */ ;
    /** @type {__VLS_StyleScopedClasses['left-0']} */ ;
    /** @type {__VLS_StyleScopedClasses['h-1']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-blue-500']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
    /** @type {__VLS_StyleScopedClasses['duration-300']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)(__assign({ class: "text-lg font-bold text-gray-800 mb-8 leading-relaxed pt-4" }));
    /** @type {__VLS_StyleScopedClasses['text-lg']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-800']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-8']} */ ;
    /** @type {__VLS_StyleScopedClasses['leading-relaxed']} */ ;
    /** @type {__VLS_StyleScopedClasses['pt-4']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-blue-500 mr-2" }));
    /** @type {__VLS_StyleScopedClasses['text-blue-500']} */ ;
    /** @type {__VLS_StyleScopedClasses['mr-2']} */ ;
    (__VLS_ctx.gameStore.currentQuestion.questionText);
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "space-y-3" }));
    /** @type {__VLS_StyleScopedClasses['space-y-3']} */ ;
    var _loop_2 = function (opt, idx) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign(__assign({ onClick: function () {
                var _a = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    _a[_i] = arguments[_i];
                }
                var $event = _a[0];
                if (!!(!__VLS_ctx.gameStore.isPlaying && !__VLS_ctx.gameStore.lastResult))
                    return;
                if (!(__VLS_ctx.gameStore.isPlaying && __VLS_ctx.gameStore.currentQuestion))
                    return;
                __VLS_ctx.handleOptionSelect(__VLS_ctx.indexToChar(idx));
                // @ts-ignore
                [gameStore, gameStore, gameStore, gameStore, handleOptionSelect, indexToChar,];
            } }, { key: (idx) }), { class: ([
                'w-full text-left p-4 rounded-xl border-2 transition-all active:scale-[0.98] flex items-center',
                __VLS_ctx.selectedAnswer === __VLS_ctx.indexToChar(idx)
                    ? 'border-blue-500 bg-blue-50/80 text-blue-700 font-bold shadow-sm'
                    : 'border-gray-100 bg-gray-50 text-gray-600 hover:bg-gray-100 hover:border-gray-300'
            ]) }));
        /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-left']} */ ;
        /** @type {__VLS_StyleScopedClasses['p-4']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
        /** @type {__VLS_StyleScopedClasses['border-2']} */ ;
        /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
        /** @type {__VLS_StyleScopedClasses['active:scale-[0.98]']} */ ;
        /** @type {__VLS_StyleScopedClasses['flex']} */ ;
        /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: ([
                'inline-flex items-center justify-center w-8 h-8 rounded-full mr-3 font-bold text-sm transition-colors',
                __VLS_ctx.selectedAnswer === __VLS_ctx.indexToChar(idx) ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-500'
            ]) }));
        /** @type {__VLS_StyleScopedClasses['inline-flex']} */ ;
        /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['w-8']} */ ;
        /** @type {__VLS_StyleScopedClasses['h-8']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['mr-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
        /** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
        (__VLS_ctx.indexToChar(idx));
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "flex-1" }));
        /** @type {__VLS_StyleScopedClasses['flex-1']} */ ;
        (opt.replace(/^[A-Z]\./, ''));
        // @ts-ignore
        [indexToChar, indexToChar, indexToChar, selectedAnswer, selectedAnswer,];
    };
    for (var _o = 0, _p = __VLS_vFor((__VLS_ctx.gameStore.currentQuestion.options)); _o < _p.length; _o++) {
        var _q = _p[_o], opt = _q[0], idx = _q[1];
        _loop_2(opt, idx);
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign(__assign({ onClick: (__VLS_ctx.submit) }, { disabled: (!__VLS_ctx.selectedAnswer) }), { class: "mt-8 w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white py-4 rounded-xl font-bold shadow-lg shadow-blue-200/50 disabled:from-gray-300 disabled:to-gray-400 disabled:shadow-none disabled:cursor-not-allowed transition-all active:scale-[0.98] text-lg" }));
    /** @type {__VLS_StyleScopedClasses['mt-8']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-gradient-to-r']} */ ;
    /** @type {__VLS_StyleScopedClasses['from-blue-600']} */ ;
    /** @type {__VLS_StyleScopedClasses['to-indigo-600']} */ ;
    /** @type {__VLS_StyleScopedClasses['hover:from-blue-700']} */ ;
    /** @type {__VLS_StyleScopedClasses['hover:to-indigo-700']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-lg']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-blue-200/50']} */ ;
    /** @type {__VLS_StyleScopedClasses['disabled:from-gray-300']} */ ;
    /** @type {__VLS_StyleScopedClasses['disabled:to-gray-400']} */ ;
    /** @type {__VLS_StyleScopedClasses['disabled:shadow-none']} */ ;
    /** @type {__VLS_StyleScopedClasses['disabled:cursor-not-allowed']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
    /** @type {__VLS_StyleScopedClasses['active:scale-[0.98]']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-lg']} */ ;
}
else if (__VLS_ctx.gameStore.lastResult) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "h-full flex flex-col items-center justify-center p-6 bg-white z-10 relative overflow-hidden" }));
    /** @type {__VLS_StyleScopedClasses['h-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['p-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['z-10']} */ ;
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute inset-0 pointer-events-none" }));
    /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
    /** @type {__VLS_StyleScopedClasses['inset-0']} */ ;
    /** @type {__VLS_StyleScopedClasses['pointer-events-none']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute top-1/3 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-yellow-200/30 rounded-full blur-3xl animate-pulse" }));
    /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
    /** @type {__VLS_StyleScopedClasses['top-1/3']} */ ;
    /** @type {__VLS_StyleScopedClasses['left-1/2']} */ ;
    /** @type {__VLS_StyleScopedClasses['-translate-x-1/2']} */ ;
    /** @type {__VLS_StyleScopedClasses['-translate-y-1/2']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-96']} */ ;
    /** @type {__VLS_StyleScopedClasses['h-96']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-yellow-200/30']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['blur-3xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['animate-pulse']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "text-8xl mb-4 animate-bounce drop-shadow-lg" }));
    /** @type {__VLS_StyleScopedClasses['text-8xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['animate-bounce']} */ ;
    /** @type {__VLS_StyleScopedClasses['drop-shadow-lg']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)(__assign({ class: "text-3xl font-black text-gray-800 mb-2" }));
    /** @type {__VLS_StyleScopedClasses['text-3xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-black']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-800']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)(__assign({ class: "text-gray-500 mb-10" }));
    /** @type {__VLS_StyleScopedClasses['text-gray-500']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-10']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "relative mb-12" }));
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-12']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "text-[8rem] font-black text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600 leading-none tracking-tighter drop-shadow-sm" }));
    /** @type {__VLS_StyleScopedClasses['text-[8rem]']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-black']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-transparent']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-clip-text']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-gradient-to-r']} */ ;
    /** @type {__VLS_StyleScopedClasses['from-blue-600']} */ ;
    /** @type {__VLS_StyleScopedClasses['to-indigo-600']} */ ;
    /** @type {__VLS_StyleScopedClasses['leading-none']} */ ;
    /** @type {__VLS_StyleScopedClasses['tracking-tighter']} */ ;
    /** @type {__VLS_StyleScopedClasses['drop-shadow-sm']} */ ;
    (__VLS_ctx.gameStore.lastResult.totalScore);
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "absolute -right-8 top-4 text-2xl font-bold text-gray-400" }));
    /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
    /** @type {__VLS_StyleScopedClasses['-right-8']} */ ;
    /** @type {__VLS_StyleScopedClasses['top-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "grid grid-cols-2 gap-5 w-full mb-12 max-w-sm" }));
    /** @type {__VLS_StyleScopedClasses['grid']} */ ;
    /** @type {__VLS_StyleScopedClasses['grid-cols-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['gap-5']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-12']} */ ;
    /** @type {__VLS_StyleScopedClasses['max-w-sm']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-gray-50 p-5 rounded-2xl text-center border border-gray-100 shadow-sm" }));
    /** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
    /** @type {__VLS_StyleScopedClasses['p-5']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['border']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-gray-100']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "text-gray-400 text-sm mb-2 font-medium" }));
    /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "font-black text-2xl text-blue-600" }));
    /** @type {__VLS_StyleScopedClasses['font-black']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-blue-600']} */ ;
    (__VLS_ctx.formatAccuracy(__VLS_ctx.gameStore.lastResult.accuracy));
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-gray-50 p-5 rounded-2xl text-center border border-gray-100 shadow-sm" }));
    /** @type {__VLS_StyleScopedClasses['bg-gray-50']} */ ;
    /** @type {__VLS_StyleScopedClasses['p-5']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['border']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-gray-100']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "text-gray-400 text-sm mb-2 font-medium" }));
    /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "font-black text-2xl text-orange-500" }));
    /** @type {__VLS_StyleScopedClasses['font-black']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-orange-500']} */ ;
    (__VLS_ctx.gameStore.lastResult.timeSpent);
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "flex flex-col w-full max-w-sm gap-3" }));
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex-col']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['max-w-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['gap-3']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign({ onClick: (__VLS_ctx.handleStartGame) }, { class: "w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-4 rounded-xl shadow-md transition-colors text-lg" }));
    /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-blue-600']} */ ;
    /** @type {__VLS_StyleScopedClasses['hover:bg-blue-700']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-md']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-lg']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign({ onClick: function () {
            var _a = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                _a[_i] = arguments[_i];
            }
            var $event = _a[0];
            if (!!(!__VLS_ctx.gameStore.isPlaying && !__VLS_ctx.gameStore.lastResult))
                return;
            if (!!(__VLS_ctx.gameStore.isPlaying && __VLS_ctx.gameStore.currentQuestion))
                return;
            if (!(__VLS_ctx.gameStore.lastResult))
                return;
            __VLS_ctx.gameStore.lastResult = null;
            // @ts-ignore
            [gameStore, gameStore, gameStore, gameStore, gameStore, handleStartGame, selectedAnswer, submit, formatAccuracy,];
        } }, { class: "w-full bg-gray-100 hover:bg-gray-200 text-gray-700 font-bold py-4 rounded-xl transition-colors" }));
    /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-gray-100']} */ ;
    /** @type {__VLS_StyleScopedClasses['hover:bg-gray-200']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-700']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-4']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-colors']} */ ;
}
if (__VLS_ctx.showResultModal) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center p-6 z-50 transition-opacity" }));
    /** @type {__VLS_StyleScopedClasses['fixed']} */ ;
    /** @type {__VLS_StyleScopedClasses['inset-0']} */ ;
    /** @type {__VLS_StyleScopedClasses['bg-black/60']} */ ;
    /** @type {__VLS_StyleScopedClasses['backdrop-blur-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['flex']} */ ;
    /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['p-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['z-50']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-opacity']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-white w-full max-w-sm rounded-[2rem] p-6 animate-bounce-in shadow-2xl relative overflow-hidden" }));
    /** @type {__VLS_StyleScopedClasses['bg-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['max-w-sm']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-[2rem]']} */ ;
    /** @type {__VLS_StyleScopedClasses['p-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['animate-bounce-in']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    /** @type {__VLS_StyleScopedClasses['overflow-hidden']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: (['absolute top-0 left-0 right-0 h-2', ((_b = __VLS_ctx.currentResult) === null || _b === void 0 ? void 0 : _b.correct) ? 'bg-green-500' : 'bg-red-500']) }));
    /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
    /** @type {__VLS_StyleScopedClasses['top-0']} */ ;
    /** @type {__VLS_StyleScopedClasses['left-0']} */ ;
    /** @type {__VLS_StyleScopedClasses['right-0']} */ ;
    /** @type {__VLS_StyleScopedClasses['h-2']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "text-center mb-5 mt-2" }));
    /** @type {__VLS_StyleScopedClasses['text-center']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-5']} */ ;
    /** @type {__VLS_StyleScopedClasses['mt-2']} */ ;
    if ((_c = __VLS_ctx.currentResult) === null || _c === void 0 ? void 0 : _c.correct) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "inline-flex items-center justify-center w-20 h-20 rounded-full bg-green-100 text-5xl mb-3 shadow-sm animate-scale-in" }));
        /** @type {__VLS_StyleScopedClasses['inline-flex']} */ ;
        /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['w-20']} */ ;
        /** @type {__VLS_StyleScopedClasses['h-20']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['bg-green-100']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-5xl']} */ ;
        /** @type {__VLS_StyleScopedClasses['mb-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
        /** @type {__VLS_StyleScopedClasses['animate-scale-in']} */ ;
    }
    else {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "inline-flex items-center justify-center w-20 h-20 rounded-full bg-red-100 text-5xl mb-3 shadow-sm animate-scale-in" }));
        /** @type {__VLS_StyleScopedClasses['inline-flex']} */ ;
        /** @type {__VLS_StyleScopedClasses['items-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['justify-center']} */ ;
        /** @type {__VLS_StyleScopedClasses['w-20']} */ ;
        /** @type {__VLS_StyleScopedClasses['h-20']} */ ;
        /** @type {__VLS_StyleScopedClasses['rounded-full']} */ ;
        /** @type {__VLS_StyleScopedClasses['bg-red-100']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-5xl']} */ ;
        /** @type {__VLS_StyleScopedClasses['mb-3']} */ ;
        /** @type {__VLS_StyleScopedClasses['shadow-sm']} */ ;
        /** @type {__VLS_StyleScopedClasses['animate-scale-in']} */ ;
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.h3, __VLS_intrinsics.h3)(__assign({ class: "text-2xl font-black text-gray-800" }));
    /** @type {__VLS_StyleScopedClasses['text-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-black']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-gray-800']} */ ;
    (((_d = __VLS_ctx.currentResult) === null || _d === void 0 ? void 0 : _d.correct) ? '回答正确!' : '回答错误');
    if ((_e = __VLS_ctx.currentResult) === null || _e === void 0 ? void 0 : _e.correct) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)(__assign({ class: "text-green-600 font-bold text-lg mt-1" }));
        /** @type {__VLS_StyleScopedClasses['text-green-600']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
        /** @type {__VLS_StyleScopedClasses['text-lg']} */ ;
        /** @type {__VLS_StyleScopedClasses['mt-1']} */ ;
        (__VLS_ctx.currentResult.points);
    }
    else {
        __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)(__assign({ class: "text-gray-400 font-medium mt-1" }));
        /** @type {__VLS_StyleScopedClasses['text-gray-400']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-medium']} */ ;
        /** @type {__VLS_StyleScopedClasses['mt-1']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "text-blue-600 font-bold" }));
        /** @type {__VLS_StyleScopedClasses['text-blue-600']} */ ;
        /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
        (__VLS_ctx.currentResult.correctAnswer);
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "bg-yellow-50/80 p-5 rounded-2xl text-[15px] text-yellow-900 mb-6 leading-relaxed border border-yellow-100/50 relative" }));
    /** @type {__VLS_StyleScopedClasses['bg-yellow-50/80']} */ ;
    /** @type {__VLS_StyleScopedClasses['p-5']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-[15px]']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-yellow-900']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-6']} */ ;
    /** @type {__VLS_StyleScopedClasses['leading-relaxed']} */ ;
    /** @type {__VLS_StyleScopedClasses['border']} */ ;
    /** @type {__VLS_StyleScopedClasses['border-yellow-100/50']} */ ;
    /** @type {__VLS_StyleScopedClasses['relative']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)(__assign({ class: "absolute top-3 left-3 text-2xl opacity-20" }));
    /** @type {__VLS_StyleScopedClasses['absolute']} */ ;
    /** @type {__VLS_StyleScopedClasses['top-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['left-3']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-2xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['opacity-20']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "font-bold mb-2 pl-8" }));
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    /** @type {__VLS_StyleScopedClasses['mb-2']} */ ;
    /** @type {__VLS_StyleScopedClasses['pl-8']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)(__assign({ class: "pl-8 opacity-90" }));
    /** @type {__VLS_StyleScopedClasses['pl-8']} */ ;
    /** @type {__VLS_StyleScopedClasses['opacity-90']} */ ;
    ((_f = __VLS_ctx.currentResult) === null || _f === void 0 ? void 0 : _f.explanation);
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)(__assign({ onClick: (__VLS_ctx.next) }, { class: (['w-full py-3.5 rounded-xl font-bold shadow-lg text-white transition-all active:scale-[0.98] text-lg', ((_g = __VLS_ctx.currentResult) === null || _g === void 0 ? void 0 : _g.correct) ? 'bg-green-500 hover:bg-green-600 shadow-green-200/50' : 'bg-blue-600 hover:bg-blue-700 shadow-blue-200/50']) }));
    /** @type {__VLS_StyleScopedClasses['w-full']} */ ;
    /** @type {__VLS_StyleScopedClasses['py-3.5']} */ ;
    /** @type {__VLS_StyleScopedClasses['rounded-xl']} */ ;
    /** @type {__VLS_StyleScopedClasses['font-bold']} */ ;
    /** @type {__VLS_StyleScopedClasses['shadow-lg']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-white']} */ ;
    /** @type {__VLS_StyleScopedClasses['transition-all']} */ ;
    /** @type {__VLS_StyleScopedClasses['active:scale-[0.98]']} */ ;
    /** @type {__VLS_StyleScopedClasses['text-lg']} */ ;
    (__VLS_ctx.gameStore.currentQuestionIndex >= (((_h = __VLS_ctx.gameStore.currentSession) === null || _h === void 0 ? void 0 : _h.totalQuestions) || 5) - 1 ? '查看总成绩' : '下一题');
}
// @ts-ignore
[gameStore, gameStore, showResultModal, currentResult, currentResult, currentResult, currentResult, currentResult, currentResult, currentResult, currentResult, next,];
var __VLS_export = (await import('vue')).defineComponent({});
export default {};
