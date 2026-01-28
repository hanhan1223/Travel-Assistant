// src/utils/sse-client.ts
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
var SSEClient = /** @class */ (function () {
    function SSEClient(url) {
        this.controller = null;
        this.url = url;
    }
    SSEClient.prototype.connect = function (body, onMessage) {
        return __awaiter(this, void 0, void 0, function () {
            var headers, requestBody, response, reader, decoder, buffer, _a, done, value, parts, _i, parts_1, part, lines, eventType, dataStr, _b, lines_1, line, parsedData, error_1;
            return __generator(this, function (_c) {
                switch (_c.label) {
                    case 0:
                        this.controller = new AbortController();
                        _c.label = 1;
                    case 1:
                        _c.trys.push([1, 6, 7, 8]);
                        headers = {};
                        requestBody = body;
                        // ✅ 关键修改：如果是 FormData，不要设置 Content-Type，让浏览器自动处理 Boundary
                        // 否则保持 application/json
                        if (!(body instanceof FormData)) {
                            headers['Content-Type'] = 'application/json';
                            requestBody = JSON.stringify(body);
                        }
                        return [4 /*yield*/, fetch(this.url, {
                                method: 'POST',
                                headers: headers,
                                body: requestBody,
                                signal: this.controller.signal,
                                credentials: 'include',
                            })];
                    case 2:
                        response = _c.sent();
                        if (!response.ok) {
                            throw new Error("HTTP error! status: ".concat(response.status));
                        }
                        if (!response.body) {
                            throw new Error('Response body is empty');
                        }
                        reader = response.body.getReader();
                        decoder = new TextDecoder();
                        buffer = '';
                        _c.label = 3;
                    case 3:
                        if (!true) return [3 /*break*/, 5];
                        return [4 /*yield*/, reader.read()];
                    case 4:
                        _a = _c.sent(), done = _a.done, value = _a.value;
                        if (done)
                            return [3 /*break*/, 5];
                        buffer += decoder.decode(value, { stream: true });
                        parts = buffer.split('\n\n');
                        buffer = parts.pop() || '';
                        for (_i = 0, parts_1 = parts; _i < parts_1.length; _i++) {
                            part = parts_1[_i];
                            if (!part.trim())
                                continue;
                            lines = part.split('\n');
                            eventType = 'message';
                            dataStr = '';
                            for (_b = 0, lines_1 = lines; _b < lines_1.length; _b++) {
                                line = lines_1[_b];
                                if (line.startsWith('event:')) {
                                    eventType = line.slice(6).trim();
                                }
                                else if (line.startsWith('data:')) {
                                    dataStr = line.slice(5).trim();
                                }
                            }
                            if (dataStr) {
                                if (dataStr === '[DONE]') {
                                    onMessage({ event: 'done', data: null });
                                    return [2 /*return*/];
                                }
                                parsedData = dataStr;
                                try {
                                    parsedData = JSON.parse(dataStr);
                                }
                                catch (e) {
                                    // ignore
                                }
                                onMessage({ event: eventType, data: parsedData });
                            }
                        }
                        return [3 /*break*/, 3];
                    case 5:
                        onMessage({ event: 'done', data: null });
                        return [3 /*break*/, 8];
                    case 6:
                        error_1 = _c.sent();
                        if (error_1.name !== 'AbortError') {
                            console.error('SSE Error:', error_1);
                            onMessage({ event: 'error', data: error_1.message });
                        }
                        return [3 /*break*/, 8];
                    case 7:
                        this.controller = null;
                        return [7 /*endfinally*/];
                    case 8: return [2 /*return*/];
                }
            });
        });
    };
    SSEClient.prototype.abort = function () {
        if (this.controller) {
            this.controller.abort();
            this.controller = null;
        }
    };
    return SSEClient;
}());
export { SSEClient };
