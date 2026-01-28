export interface SSECallback {
    event: string;
    data: any;
}
export declare class SSEClient {
    private url;
    private controller;
    constructor(url: string);
    connect(body: any, onMessage: (payload: SSECallback) => void): Promise<void>;
    abort(): void;
}
