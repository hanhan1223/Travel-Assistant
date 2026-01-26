import type { ChatMessage } from '../types/api';
interface ExtendedMessage extends ChatMessage {
    isLoading?: boolean;
    isThinking?: boolean;
    tempContent?: string;
}
export declare const useChatStore: import("pinia").StoreDefinition<"chat", Pick<{
    messages: import("vue").Ref<{
        isLoading?: boolean | undefined;
        isThinking?: boolean | undefined;
        tempContent?: string | undefined;
        id: string | number;
        role: "user" | "assistant";
        content: string;
        type: import("../types/api").MessageType;
        location?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        } | undefined;
        locations?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        }[] | undefined;
        products?: {
            name: string;
            shop: string;
            price?: string | undefined;
            imageUrl?: string | undefined;
            shopLat?: number | undefined;
            shopLng?: number | undefined;
        }[] | undefined;
        createdAt: string | number;
    }[], ExtendedMessage[] | {
        isLoading?: boolean | undefined;
        isThinking?: boolean | undefined;
        tempContent?: string | undefined;
        id: string | number;
        role: "user" | "assistant";
        content: string;
        type: import("../types/api").MessageType;
        location?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        } | undefined;
        locations?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        }[] | undefined;
        products?: {
            name: string;
            shop: string;
            price?: string | undefined;
            imageUrl?: string | undefined;
            shopLat?: number | undefined;
            shopLng?: number | undefined;
        }[] | undefined;
        createdAt: string | number;
    }[]>;
    historyList: import("vue").Ref<{
        id: number;
        userId: number;
        title: string;
        createdAt: string;
        updatedAt: string;
        lastMessage?: string | undefined;
    }[], import("../types/api").ConversationItem[] | {
        id: number;
        userId: number;
        title: string;
        createdAt: string;
        updatedAt: string;
        lastMessage?: string | undefined;
    }[]>;
    currentConversationId: import("vue").Ref<number | null, number | null>;
    isStreaming: import("vue").Ref<boolean, boolean>;
    envContext: import("vue").Ref<{
        weather: string;
        city: string;
        district: string;
    }, {
        weather: string;
        city: string;
        district: string;
    } | {
        weather: string;
        city: string;
        district: string;
    }>;
    userLocation: import("vue").Ref<{
        lat: number;
        lng: number;
    }, {
        lat: number;
        lng: number;
    } | {
        lat: number;
        lng: number;
    }>;
    initChat: () => Promise<void>;
    resetChat: () => void;
    sendMessage: (content: string) => Promise<void>;
    fetchHistory: () => Promise<void>;
    loadHistory: (id: string | number) => Promise<void>;
    deleteConversation: (id: number) => Promise<boolean>;
    updateConversationTitle: (id: number, newTitle: string, silent?: boolean) => Promise<boolean>;
    initLocation: () => Promise<{
        lat: number;
        lng: number;
    }>;
}, "messages" | "historyList" | "currentConversationId" | "isStreaming" | "envContext" | "userLocation">, Pick<{
    messages: import("vue").Ref<{
        isLoading?: boolean | undefined;
        isThinking?: boolean | undefined;
        tempContent?: string | undefined;
        id: string | number;
        role: "user" | "assistant";
        content: string;
        type: import("../types/api").MessageType;
        location?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        } | undefined;
        locations?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        }[] | undefined;
        products?: {
            name: string;
            shop: string;
            price?: string | undefined;
            imageUrl?: string | undefined;
            shopLat?: number | undefined;
            shopLng?: number | undefined;
        }[] | undefined;
        createdAt: string | number;
    }[], ExtendedMessage[] | {
        isLoading?: boolean | undefined;
        isThinking?: boolean | undefined;
        tempContent?: string | undefined;
        id: string | number;
        role: "user" | "assistant";
        content: string;
        type: import("../types/api").MessageType;
        location?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        } | undefined;
        locations?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        }[] | undefined;
        products?: {
            name: string;
            shop: string;
            price?: string | undefined;
            imageUrl?: string | undefined;
            shopLat?: number | undefined;
            shopLng?: number | undefined;
        }[] | undefined;
        createdAt: string | number;
    }[]>;
    historyList: import("vue").Ref<{
        id: number;
        userId: number;
        title: string;
        createdAt: string;
        updatedAt: string;
        lastMessage?: string | undefined;
    }[], import("../types/api").ConversationItem[] | {
        id: number;
        userId: number;
        title: string;
        createdAt: string;
        updatedAt: string;
        lastMessage?: string | undefined;
    }[]>;
    currentConversationId: import("vue").Ref<number | null, number | null>;
    isStreaming: import("vue").Ref<boolean, boolean>;
    envContext: import("vue").Ref<{
        weather: string;
        city: string;
        district: string;
    }, {
        weather: string;
        city: string;
        district: string;
    } | {
        weather: string;
        city: string;
        district: string;
    }>;
    userLocation: import("vue").Ref<{
        lat: number;
        lng: number;
    }, {
        lat: number;
        lng: number;
    } | {
        lat: number;
        lng: number;
    }>;
    initChat: () => Promise<void>;
    resetChat: () => void;
    sendMessage: (content: string) => Promise<void>;
    fetchHistory: () => Promise<void>;
    loadHistory: (id: string | number) => Promise<void>;
    deleteConversation: (id: number) => Promise<boolean>;
    updateConversationTitle: (id: number, newTitle: string, silent?: boolean) => Promise<boolean>;
    initLocation: () => Promise<{
        lat: number;
        lng: number;
    }>;
}, never>, Pick<{
    messages: import("vue").Ref<{
        isLoading?: boolean | undefined;
        isThinking?: boolean | undefined;
        tempContent?: string | undefined;
        id: string | number;
        role: "user" | "assistant";
        content: string;
        type: import("../types/api").MessageType;
        location?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        } | undefined;
        locations?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        }[] | undefined;
        products?: {
            name: string;
            shop: string;
            price?: string | undefined;
            imageUrl?: string | undefined;
            shopLat?: number | undefined;
            shopLng?: number | undefined;
        }[] | undefined;
        createdAt: string | number;
    }[], ExtendedMessage[] | {
        isLoading?: boolean | undefined;
        isThinking?: boolean | undefined;
        tempContent?: string | undefined;
        id: string | number;
        role: "user" | "assistant";
        content: string;
        type: import("../types/api").MessageType;
        location?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        } | undefined;
        locations?: {
            name: string;
            address: string;
            lat: number;
            lng: number;
            phone?: string | undefined;
            rating?: number | undefined;
            distance?: string | undefined;
            images?: string[] | undefined;
            mapImageUrl?: string | undefined;
        }[] | undefined;
        products?: {
            name: string;
            shop: string;
            price?: string | undefined;
            imageUrl?: string | undefined;
            shopLat?: number | undefined;
            shopLng?: number | undefined;
        }[] | undefined;
        createdAt: string | number;
    }[]>;
    historyList: import("vue").Ref<{
        id: number;
        userId: number;
        title: string;
        createdAt: string;
        updatedAt: string;
        lastMessage?: string | undefined;
    }[], import("../types/api").ConversationItem[] | {
        id: number;
        userId: number;
        title: string;
        createdAt: string;
        updatedAt: string;
        lastMessage?: string | undefined;
    }[]>;
    currentConversationId: import("vue").Ref<number | null, number | null>;
    isStreaming: import("vue").Ref<boolean, boolean>;
    envContext: import("vue").Ref<{
        weather: string;
        city: string;
        district: string;
    }, {
        weather: string;
        city: string;
        district: string;
    } | {
        weather: string;
        city: string;
        district: string;
    }>;
    userLocation: import("vue").Ref<{
        lat: number;
        lng: number;
    }, {
        lat: number;
        lng: number;
    } | {
        lat: number;
        lng: number;
    }>;
    initChat: () => Promise<void>;
    resetChat: () => void;
    sendMessage: (content: string) => Promise<void>;
    fetchHistory: () => Promise<void>;
    loadHistory: (id: string | number) => Promise<void>;
    deleteConversation: (id: number) => Promise<boolean>;
    updateConversationTitle: (id: number, newTitle: string, silent?: boolean) => Promise<boolean>;
    initLocation: () => Promise<{
        lat: number;
        lng: number;
    }>;
}, "initChat" | "resetChat" | "sendMessage" | "fetchHistory" | "loadHistory" | "deleteConversation" | "updateConversationTitle" | "initLocation">>;
export {};
