export declare class XFVoiceClient {
    private onTextChange;
    private onError;
    private socket;
    private audioContext;
    private workletNode;
    private mediaStream;
    private status;
    constructor(onTextChange: (text: string, isFinal: boolean) => void, onError: (err: string) => void);
    private getWebSocketUrl;
    start(): Promise<void>;
    private startRecording;
    stop(): void;
    private resampleTo16k;
    private floatTo16BitPCM;
    private floatTo16BitValue;
    private arrayBufferToBase64;
}
