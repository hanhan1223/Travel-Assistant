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
// src/stores/userStore.ts
import { defineStore } from 'pinia';
import { ref } from 'vue';
import http from '../utils/request';
import { showToast } from 'vant';
export var useUserStore = defineStore('user', function () {
    var userInfo = ref(null);
    var documentList = ref([]);
    // 发送验证码
    var sendCode = function (email) { return __awaiter(void 0, void 0, void 0, function () {
        var error_1;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, , 3]);
                    return [4 /*yield*/, http.post('/user/register/send-code', { email: email })];
                case 1:
                    _a.sent();
                    showToast('验证码已发送');
                    return [2 /*return*/, true];
                case 2:
                    error_1 = _a.sent();
                    console.error('发送验证码失败:', error_1);
                    return [2 /*return*/, false];
                case 3: return [2 /*return*/];
            }
        });
    }); };
    // 注册
    var register = function (payload) { return __awaiter(void 0, void 0, void 0, function () {
        var requestBody, error_2;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, , 3]);
                    requestBody = {
                        email: payload.email,
                        code: payload.code,
                        userPassword: payload.password,
                        checkPassword: payload.password,
                        userName: "\u7528\u6237".concat(payload.email.split('@')[0]),
                        userAvatar: undefined
                    };
                    return [4 /*yield*/, http.post('/user/register/email', requestBody)];
                case 1:
                    _a.sent();
                    showToast('注册成功，请登录');
                    return [2 /*return*/, true];
                case 2:
                    error_2 = _a.sent();
                    console.error('注册失败:', error_2);
                    return [2 /*return*/, false];
                case 3: return [2 /*return*/];
            }
        });
    }); };
    // 登录
    var login = function (payload) { return __awaiter(void 0, void 0, void 0, function () {
        var res, error_3;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, , 3]);
                    return [4 /*yield*/, http.post('/user/login/email', payload)];
                case 1:
                    res = _a.sent();
                    userInfo.value = res;
                    showToast('登录成功');
                    return [2 /*return*/, true];
                case 2:
                    error_3 = _a.sent();
                    console.error('登录失败:', error_3);
                    return [2 /*return*/, false];
                case 3: return [2 /*return*/];
            }
        });
    }); };
    // 获取用户信息
    var fetchUserInfo = function () { return __awaiter(void 0, void 0, void 0, function () {
        var res, error_4;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, , 3]);
                    return [4 /*yield*/, http.get('/user/get/login')];
                case 1:
                    res = _a.sent();
                    // 现在的 res 被 TS 认为是 UserInfo 类型，拥有 id 属性
                    if (res && res.id) {
                        userInfo.value = res;
                    }
                    return [3 /*break*/, 3];
                case 2:
                    error_4 = _a.sent();
                    console.error('获取用户信息失败:', error_4);
                    return [3 /*break*/, 3];
                case 3: return [2 /*return*/];
            }
        });
    }); };
    // 退出登录
    var logout = function () { return __awaiter(void 0, void 0, void 0, function () {
        var e_1;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, 3, 4]);
                    return [4 /*yield*/, http.post('/user/logout')];
                case 1:
                    _a.sent();
                    return [3 /*break*/, 4];
                case 2:
                    e_1 = _a.sent();
                    console.error(e_1);
                    return [3 /*break*/, 4];
                case 3:
                    userInfo.value = null;
                    documentList.value = [];
                    showToast('已退出登录');
                    return [7 /*endfinally*/];
                case 4: return [2 /*return*/];
            }
        });
    }); };
    // 上传文件
    var uploadFile = function (file) { return __awaiter(void 0, void 0, void 0, function () {
        var formData, res, error_5;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, , 3]);
                    formData = new FormData();
                    formData.append('file', file);
                    return [4 /*yield*/, http.post('/file/test/upload', formData, {
                            headers: { 'Content-Type': 'multipart/form-data' }
                        })];
                case 1:
                    res = _a.sent();
                    return [2 /*return*/, res];
                case 2:
                    error_5 = _a.sent();
                    console.error('上传失败:', error_5);
                    return [2 /*return*/, ''];
                case 3: return [2 /*return*/];
            }
        });
    }); };
    // 更新个人信息
    var updateProfile = function (userName, userAvatar) { return __awaiter(void 0, void 0, void 0, function () {
        var error_6;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, , 3]);
                    return [4 /*yield*/, http.post('/user/update/my', { userName: userName, userAvatar: userAvatar })];
                case 1:
                    _a.sent();
                    showToast('更新成功');
                    // 更新本地状态
                    if (userInfo.value) {
                        userInfo.value.userName = userName;
                        if (userAvatar) {
                            userInfo.value.userAvatar = userAvatar;
                        }
                    }
                    return [2 /*return*/, true];
                case 2:
                    error_6 = _a.sent();
                    console.error('更新失败:', error_6);
                    showToast('更新失败，请重试');
                    return [2 /*return*/, false];
                case 3: return [2 /*return*/];
            }
        });
    }); };
    // 修改密码
    var updatePassword = function (payload) { return __awaiter(void 0, void 0, void 0, function () {
        var requestBody, error_7;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, , 3]);
                    requestBody = {
                        oldPassword: payload.oldPassword || '',
                        newPassword: payload.newPassword || '',
                        confirmPassword: payload.confirmPassword || ''
                    };
                    console.log('Sending update password request:', requestBody);
                    return [4 /*yield*/, http.post('/user/update/password', requestBody)];
                case 1:
                    _a.sent();
                    showToast('密码修改成功，请重新登录');
                    setTimeout(function () {
                        logout();
                    }, 1500);
                    return [2 /*return*/, true];
                case 2:
                    error_7 = _a.sent();
                    console.error('修改密码失败:', error_7);
                    return [2 /*return*/, false];
                case 3: return [2 /*return*/];
            }
        });
    }); };
    // 获取文档列表
    var fetchDocuments = function () { return __awaiter(void 0, void 0, void 0, function () {
        var res, error_8;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    _a.trys.push([0, 2, , 3]);
                    return [4 /*yield*/, http.post('/document/my', { current: 1, pageSize: 20 })];
                case 1:
                    res = _a.sent();
                    documentList.value = res.records || [];
                    return [3 /*break*/, 3];
                case 2:
                    error_8 = _a.sent();
                    console.error('获取文档列表失败:', error_8);
                    return [3 /*break*/, 3];
                case 3: return [2 /*return*/];
            }
        });
    }); };
    return {
        userInfo: userInfo,
        documentList: documentList,
        sendCode: sendCode,
        register: register,
        login: login,
        fetchUserInfo: fetchUserInfo,
        logout: logout,
        uploadFile: uploadFile,
        updateProfile: updateProfile,
        updatePassword: updatePassword,
        fetchDocuments: fetchDocuments
    };
});
