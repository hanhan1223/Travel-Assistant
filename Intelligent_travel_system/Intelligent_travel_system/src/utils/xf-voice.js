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
// src/utils/xf-voice.ts
import CryptoJS from 'crypto-js';
// ⚠️ 请在此处填入您的科大讯飞 Key，或者从 import.meta.env 读取
var APPID = 'ab7bf99e';
var API_SECRET = 'YmM1MzFjMmEyNjc2MmMyMTkxODU4YWQ1';
var API_KEY = '520f545f0d554dac923ae572af96b380';
// ==========================================
// 1. AudioWorklet 处理器 (独立线程采集音频)
// ==========================================
var WORKLET_CODE = "\nclass XFVoiceProcessor extends AudioWorkletProcessor {\n  constructor() {\n    super();\n    // \u51CF\u5C0F\u7F13\u51B2\u5927\u5C0F\u5230 2048 (\u7EA6 40-50ms)\uFF0C\u964D\u4F4E\u5EF6\u8FDF\uFF0C\u63D0\u9AD8\u4F20\u8F93\u7A33\u5B9A\u6027\n    this.bufferSize = 2048;\n    this.buffer = new Float32Array(this.bufferSize);\n    this.bufferIndex = 0;\n  }\n\n  process(inputs, outputs, parameters) {\n    const input = inputs[0];\n    if (!input || !input.length) return true;\n    \n    const channel0 = input[0];\n    \n    for (let i = 0; i < channel0.length; i++) {\n      this.buffer[this.bufferIndex++] = channel0[i];\n      \n      if (this.bufferIndex >= this.bufferSize) {\n        this.port.postMessage(this.buffer.slice());\n        this.bufferIndex = 0;\n      }\n    }\n    return true;\n  }\n}\nregisterProcessor('xf-voice-processor', XFVoiceProcessor);\n";
var XFVoiceClient = /** @class */ (function () {
    function XFVoiceClient(onTextChange, onError) {
        this.onTextChange = onTextChange;
        this.onError = onError;
        this.socket = null;
        this.audioContext = null;
        this.workletNode = null;
        this.mediaStream = null;
        this.status = 'init';
    }
    XFVoiceClient.prototype.getWebSocketUrl = function () {
        var url = 'wss://iat-api.xfyun.cn/v2/iat';
        var host = 'iat-api.xfyun.cn';
        var date = new Date().toUTCString();
        var algorithm = 'hmac-sha256';
        var headers = "host date request-line";
        var signatureOrigin = "host: ".concat(host, "\ndate: ").concat(date, "\nGET /v2/iat HTTP/1.1");
        var signatureSha = CryptoJS.HmacSHA256(signatureOrigin, API_SECRET);
        var signature = CryptoJS.enc.Base64.stringify(signatureSha);
        var authorizationOrigin = "api_key=\"".concat(API_KEY, "\", algorithm=\"").concat(algorithm, "\", headers=\"").concat(headers, "\", signature=\"").concat(signature, "\"");
        var authorization = btoa(authorizationOrigin);
        return "".concat(url, "?authorization=").concat(authorization, "&date=").concat(date, "&host=").concat(host);
    };
    XFVoiceClient.prototype.start = function () {
        return __awaiter(this, void 0, void 0, function () {
            var url;
            var _this = this;
            return __generator(this, function (_a) {
                if (this.status === 'recording')
                    return [2 /*return*/];
                try {
                    this.status = 'init';
                    url = this.getWebSocketUrl();
                    this.socket = new WebSocket(url);
                    this.socket.onopen = function () {
                        _this.status = 'recording';
                        _this.startRecording();
                    };
                    this.socket.onmessage = function (e) {
                        var _a;
                        var result = JSON.parse(e.data);
                        if (result.code !== 0) {
                            console.error('XF Error:', result);
                            (_a = _this.socket) === null || _a === void 0 ? void 0 : _a.close();
                            // 忽略 10165 错误，如果它是偶发的，通常是 socket 关闭时序问题
                            if (result.code !== 10165) {
                                _this.onError("\u8BC6\u522B\u9519\u8BEF: ".concat(result.code));
                            }
                            return;
                        }
                        if (result.data && result.data.result) {
                            var ws = result.data.result.ws;
                            var str_1 = '';
                            ws.forEach(function (w) {
                                w.cw.forEach(function (c) {
                                    str_1 += c.w;
                                });
                            });
                            _this.onTextChange(str_1, result.data.status === 2);
                        }
                    };
                    this.socket.onerror = function (e) {
                        console.error("WebSocket error:", e);
                        _this.onError('网络连接失败，请检查网络');
                        _this.stop();
                    };
                    this.socket.onclose = function () {
                        _this.stop();
                    };
                }
                catch (error) {
                    this.onError('无法启动语音识别');
                }
                return [2 /*return*/];
            });
        });
    };
    XFVoiceClient.prototype.startRecording = function () {
        return __awaiter(this, void 0, void 0, function () {
            var AudioContext_1, blob, workletUrl, _a, source, e_1;
            var _this = this;
            var _b;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0:
                        _c.trys.push([0, 3, , 4]);
                        AudioContext_1 = window.AudioContext || window.webkitAudioContext;
                        this.audioContext = new AudioContext_1();
                        blob = new Blob([WORKLET_CODE], { type: 'application/javascript' });
                        workletUrl = URL.createObjectURL(blob);
                        return [4 /*yield*/, this.audioContext.audioWorklet.addModule(workletUrl)];
                    case 1:
                        _c.sent();
                        _a = this;
                        return [4 /*yield*/, navigator.mediaDevices.getUserMedia({ audio: true })];
                    case 2:
                        _a.mediaStream = _c.sent();
                        source = this.audioContext.createMediaStreamSource(this.mediaStream);
                        this.workletNode = new AudioWorkletNode(this.audioContext, 'xf-voice-processor');
                        this.workletNode.port.onmessage = function (event) {
                            if (_this.status !== 'recording' || !_this.socket || _this.socket.readyState !== WebSocket.OPEN)
                                return;
                            // 1. 获取原始音频数据 (Float32, 采样率可能是 48000Hz 或 44100Hz)
                            var inputBuffer = event.data;
                            // 2. 关键步骤：降采样到 16000Hz 并转为 16位 PCM
                            var buffer = _this.resampleTo16k(inputBuffer, _this.audioContext.sampleRate);
                            // 3. 发送数据
                            _this.socket.send(JSON.stringify({
                                data: {
                                    status: 1,
                                    format: "audio/L16;rate=16000",
                                    encoding: "raw",
                                    audio: _this.arrayBufferToBase64(buffer.buffer)
                                }
                            }));
                        };
                        source.connect(this.workletNode);
                        this.workletNode.connect(this.audioContext.destination);
                        // 发送第一帧（握手）
                        (_b = this.socket) === null || _b === void 0 ? void 0 : _b.send(JSON.stringify({
                            common: { app_id: APPID },
                            business: { language: "zh_cn", domain: "iat", accent: "mandarin", vad_eos: 5000 },
                            data: { status: 0, format: "audio/L16;rate=16000", encoding: "raw" }
                        }));
                        return [3 /*break*/, 4];
                    case 3:
                        e_1 = _c.sent();
                        console.error(e_1);
                        this.onError('麦克风访问失败');
                        this.stop();
                        return [3 /*break*/, 4];
                    case 4: return [2 /*return*/];
                }
            });
        });
    };
    XFVoiceClient.prototype.stop = function () {
        var _this = this;
        var _a;
        if (this.status === 'recording' && ((_a = this.socket) === null || _a === void 0 ? void 0 : _a.readyState) === WebSocket.OPEN) {
            this.socket.send(JSON.stringify({
                data: { status: 2, format: "audio/L16;rate=16000", encoding: "raw", audio: "" }
            }));
        }
        this.status = 'init';
        if (this.mediaStream) {
            this.mediaStream.getTracks().forEach(function (track) { return track.stop(); });
            this.mediaStream = null;
        }
        if (this.workletNode) {
            this.workletNode.disconnect();
            this.workletNode = null;
        }
        if (this.audioContext && this.audioContext.state !== 'closed') {
            this.audioContext.close().catch(console.error);
            this.audioContext = null;
        }
        setTimeout(function () {
            if (_this.socket && _this.socket.readyState === WebSocket.OPEN) {
                _this.socket.close();
            }
            _this.socket = null;
        }, 1000);
    };
    // ✅ 核心修复：降采样算法 (任意采样率 -> 16000Hz Int16)
    XFVoiceClient.prototype.resampleTo16k = function (audioData, sampleRate) {
        var targetSampleRate = 16000;
        // 如果已经是 16k，直接转换
        if (sampleRate === targetSampleRate) {
            return this.floatTo16BitPCM(audioData);
        }
        // 计算压缩比例
        var compression = sampleRate / targetSampleRate;
        var length = Math.floor(audioData.length / compression);
        var result = new Int16Array(length);
        var index = 0, j = 0;
        // 线性插值降采样
        while (index < length) {
            var offset = Math.floor(j);
            var nextOffset = Math.ceil(j);
            var weight = j - offset;
            var s0 = audioData[offset] || 0;
            var s1 = audioData[nextOffset] || 0;
            // 插值计算
            var interpolatedValue = s0 * (1 - weight) + s1 * weight;
            result[index] = this.floatTo16BitValue(interpolatedValue);
            j += compression;
            index++;
        }
        return result;
    };
    XFVoiceClient.prototype.floatTo16BitPCM = function (input) {
        var output = new Int16Array(input.length);
        for (var i = 0; i < input.length; i++) {
            output[i] = this.floatTo16BitValue(input[i]);
        }
        return output;
    };
    XFVoiceClient.prototype.floatTo16BitValue = function (input) {
        var s = Math.max(-1, Math.min(1, input));
        return s < 0 ? s * 0x8000 : s * 0x7FFF;
    };
    XFVoiceClient.prototype.arrayBufferToBase64 = function (buffer) {
        var binary = '';
        var bytes = new Uint8Array(buffer);
        for (var i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return window.btoa(binary);
    };
    return XFVoiceClient;
}());
export { XFVoiceClient };
