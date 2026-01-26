export interface SSECallback {
    event: string;
    data: any;
}
export declare class SSEClient {
    private url;
    private controller;
    constructor(url: string);
    /**
     * 连接 SSE 流
     * @param body 请求体参数
     * @param onMessage 消息回调
     */
    connect(body: any, onMessage: (payload: SSECallback) => void): Promise<void>;
    /**
     * 中断连接
     */
    abort(): void;
}
