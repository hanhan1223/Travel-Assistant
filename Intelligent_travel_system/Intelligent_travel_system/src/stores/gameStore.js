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
// src/stores/gameStore.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import http from '../utils/request';
import { showToast } from 'vant';
export var useGameStore = defineStore('game', function () {
    var isPlaying = ref(false);
    var currentSession = ref(null);
    var currentQuestionIndex = ref(0);
    var currentQuestion = ref(null);
    var lastResult = ref(null);
    // 排行榜数据
    var rankingList = ref([]);
    var myRank = ref(null);
    var currentRankingType = ref('weekly');
    // 开始游戏
    var startGame = function () {
        var args_1 = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            args_1[_i] = arguments[_i];
        }
        return __awaiter(void 0, __spreadArray([], args_1, true), void 0, function (mode, difficulty, projectName) {
            var payload, data, e_1;
            if (mode === void 0) { mode = 'normal'; }
            if (difficulty === void 0) { difficulty = 1; }
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        _a.trys.push([0, 2, , 3]);
                        payload = {
                            gameMode: mode,
                            difficulty: difficulty,
                            questionCount: 5,
                            projectName: projectName ? projectName.trim() : ""
                        };
                        return [4 /*yield*/, http.post('/quiz/start', payload)];
                    case 1:
                        data = _a.sent();
                        // 🛡️🛡️🛡️【数据防御与清洗】🛡️🛡️🛡️
                        if (data && data.questions) {
                            data.questions.forEach(function (q, index) {
                                // 情况 1: 如果 options 是 null 或空数组
                                if (!q.options || q.options.length === 0) {
                                    console.warn("\u26A0\uFE0F [GameStore] \u7B2C ".concat(index + 1, " \u9898 (ID: ").concat(q.id, ") \u7F3A\u5C11\u9009\u9879\uFF01\u5DF2\u89E6\u53D1\u81EA\u52A8\u4FEE\u590D\u3002"));
                                    q.options = [
                                        '选项A (数据缺失)',
                                        '选项B (数据缺失)',
                                        '选项C (数据缺失)',
                                        '选项D (数据缺失)'
                                    ];
                                }
                                else {
                                    q.options = q.options.map(function (opt) { return opt.replace(/^[A-Z]\.\s*/, ''); });
                                }
                                if (!q.questionType) {
                                    q.questionType = 'single';
                                }
                            });
                        }
                        else {
                            console.error('❌ [GameStore] 返回数据中缺少 questions 字段！');
                        }
                        currentSession.value = data;
                        currentQuestionIndex.value = 0;
                        if (data && data.questions && data.questions.length > 0) {
                            currentQuestion.value = data.questions[0];
                            isPlaying.value = true;
                            return [2 /*return*/, true];
                        }
                        else {
                            showToast('题库暂无题目');
                            return [2 /*return*/, false];
                        }
                        return [3 /*break*/, 3];
                    case 2:
                        e_1 = _a.sent();
                        console.error('❌ [GameStore] startGame 发生异常:', e_1);
                        showToast('开始游戏失败');
                        return [2 /*return*/, false];
                    case 3: return [2 /*return*/];
                }
            });
        });
    };
    // 提交答案
    var submitAnswer = function (answer, timeSpent) { return __awaiter(void 0, void 0, void 0, function () {
        var rawResult, result, e_2;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    if (!currentSession.value || !currentQuestion.value)
                        return [2 /*return*/, null];
                    _a.label = 1;
                case 1:
                    _a.trys.push([1, 3, , 4]);
                    console.log("\uD83D\uDE80 [GameStore] \u63D0\u4EA4\u7B54\u6848: \u9898\u76EEID=".concat(currentQuestion.value.id, ", \u7B54\u6848=").concat(answer, ", \u7528\u65F6=").concat(timeSpent));
                    return [4 /*yield*/, http.post('/quiz/answer', {
                            gameRecordId: currentSession.value.gameRecordId,
                            questionId: currentQuestion.value.id,
                            userAnswer: answer,
                            timeSpent: timeSpent
                        })];
                case 2:
                    rawResult = _a.sent();
                    result = {
                        correct: rawResult.isCorrect !== undefined ? rawResult.isCorrect : rawResult.correct,
                        points: rawResult.score !== undefined ? rawResult.score : rawResult.points,
                        correctAnswer: rawResult.correctAnswer,
                        explanation: rawResult.explanation,
                        totalScore: rawResult.totalScore,
                        correctCount: 0,
                        answeredCount: 0
                    };
                    return [2 /*return*/, result];
                case 3:
                    e_2 = _a.sent();
                    console.error('❌ [GameStore] 提交失败:', e_2);
                    showToast('提交失败');
                    return [2 /*return*/, null];
                case 4: return [2 /*return*/];
            }
        });
    }); };
    // 下一题
    var nextQuestion = function () {
        if (!currentSession.value)
            return false;
        if (currentQuestionIndex.value < currentSession.value.questions.length - 1) {
            currentQuestionIndex.value++;
            currentQuestion.value = currentSession.value.questions[currentQuestionIndex.value];
            return true;
        }
        else {
            return false;
        }
    };
    // 结算游戏
    var completeGame = function () { return __awaiter(void 0, void 0, void 0, function () {
        var rawRes, finalTimeSpent, start, end, res, e_3;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    if (!currentSession.value)
                        return [2 /*return*/];
                    _a.label = 1;
                case 1:
                    _a.trys.push([1, 3, , 4]);
                    return [4 /*yield*/, http.post("/quiz/complete/".concat(currentSession.value.gameRecordId))];
                case 2:
                    rawRes = _a.sent();
                    finalTimeSpent = rawRes.timeSpent;
                    if (finalTimeSpent === null || finalTimeSpent === undefined) {
                        if (rawRes.startedAt && rawRes.completedAt) {
                            start = new Date(rawRes.startedAt).getTime();
                            end = new Date(rawRes.completedAt).getTime();
                            finalTimeSpent = Math.floor((end - start) / 1000);
                        }
                        else {
                            finalTimeSpent = 0;
                        }
                    }
                    res = __assign(__assign({}, rawRes), { timeSpent: finalTimeSpent });
                    lastResult.value = res;
                    isPlaying.value = false;
                    currentSession.value = null;
                    return [3 /*break*/, 4];
                case 3:
                    e_3 = _a.sent();
                    console.error(e_3);
                    return [3 /*break*/, 4];
                case 4: return [2 /*return*/];
            }
        });
    }); };
    // 获取排行榜
    var fetchRankings = function () {
        var args_1 = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            args_1[_i] = arguments[_i];
        }
        return __awaiter(void 0, __spreadArray([], args_1, true), void 0, function (type) {
            var list, rank, e_4;
            if (type === void 0) { type = 'weekly'; }
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        _a.trys.push([0, 3, , 4]);
                        currentRankingType.value = type;
                        return [4 /*yield*/, http.get("/quiz/ranking/".concat(type, "?topN=20"))];
                    case 1:
                        list = _a.sent();
                        rankingList.value = Array.isArray(list) ? list : [];
                        return [4 /*yield*/, http.get('/quiz/ranking/my')];
                    case 2:
                        rank = _a.sent();
                        myRank.value = rank;
                        return [3 /*break*/, 4];
                    case 3:
                        e_4 = _a.sent();
                        console.error(e_4);
                        rankingList.value = [];
                        myRank.value = null;
                        return [3 /*break*/, 4];
                    case 4: return [2 /*return*/];
                }
            });
        });
    };
    return {
        isPlaying: isPlaying,
        currentSession: currentSession,
        currentQuestion: currentQuestion,
        currentQuestionIndex: currentQuestionIndex,
        lastResult: lastResult,
        rankingList: rankingList,
        myRank: myRank,
        currentRankingType: currentRankingType,
        startGame: startGame,
        submitAnswer: submitAnswer,
        nextQuestion: nextQuestion,
        completeGame: completeGame,
        fetchRankings: fetchRankings
    };
});
