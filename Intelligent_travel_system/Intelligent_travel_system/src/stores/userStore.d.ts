import type { UserInfo, LoginRequest, DocumentItem, UpdatePasswordRequest } from '../types/api';
export declare const useUserStore: import("pinia").StoreDefinition<"user", Pick<{
    userInfo: import("vue").Ref<{
        id: number;
        userName: string;
        userAvatar?: string | undefined;
        email: string;
    } | null, UserInfo | {
        id: number;
        userName: string;
        userAvatar?: string | undefined;
        email: string;
    } | null>;
    documentList: import("vue").Ref<{
        id: number;
        userId: number;
        projectId: number;
        title?: string | undefined;
        fileUrl: string;
        createdAt: string;
    }[], DocumentItem[] | {
        id: number;
        userId: number;
        projectId: number;
        title?: string | undefined;
        fileUrl: string;
        createdAt: string;
    }[]>;
    sendCode: (email: string) => Promise<boolean>;
    register: (payload: {
        email: string;
        code: string;
        password?: string;
    }) => Promise<boolean>;
    login: (payload: LoginRequest) => Promise<boolean>;
    fetchUserInfo: () => Promise<void>;
    logout: () => Promise<void>;
    uploadFile: (file: File) => Promise<string>;
    updateProfile: (userName: string, userAvatar: string) => Promise<boolean>;
    updatePassword: (payload: UpdatePasswordRequest) => Promise<boolean>;
    fetchDocuments: () => Promise<void>;
}, "userInfo" | "documentList">, Pick<{
    userInfo: import("vue").Ref<{
        id: number;
        userName: string;
        userAvatar?: string | undefined;
        email: string;
    } | null, UserInfo | {
        id: number;
        userName: string;
        userAvatar?: string | undefined;
        email: string;
    } | null>;
    documentList: import("vue").Ref<{
        id: number;
        userId: number;
        projectId: number;
        title?: string | undefined;
        fileUrl: string;
        createdAt: string;
    }[], DocumentItem[] | {
        id: number;
        userId: number;
        projectId: number;
        title?: string | undefined;
        fileUrl: string;
        createdAt: string;
    }[]>;
    sendCode: (email: string) => Promise<boolean>;
    register: (payload: {
        email: string;
        code: string;
        password?: string;
    }) => Promise<boolean>;
    login: (payload: LoginRequest) => Promise<boolean>;
    fetchUserInfo: () => Promise<void>;
    logout: () => Promise<void>;
    uploadFile: (file: File) => Promise<string>;
    updateProfile: (userName: string, userAvatar: string) => Promise<boolean>;
    updatePassword: (payload: UpdatePasswordRequest) => Promise<boolean>;
    fetchDocuments: () => Promise<void>;
}, never>, Pick<{
    userInfo: import("vue").Ref<{
        id: number;
        userName: string;
        userAvatar?: string | undefined;
        email: string;
    } | null, UserInfo | {
        id: number;
        userName: string;
        userAvatar?: string | undefined;
        email: string;
    } | null>;
    documentList: import("vue").Ref<{
        id: number;
        userId: number;
        projectId: number;
        title?: string | undefined;
        fileUrl: string;
        createdAt: string;
    }[], DocumentItem[] | {
        id: number;
        userId: number;
        projectId: number;
        title?: string | undefined;
        fileUrl: string;
        createdAt: string;
    }[]>;
    sendCode: (email: string) => Promise<boolean>;
    register: (payload: {
        email: string;
        code: string;
        password?: string;
    }) => Promise<boolean>;
    login: (payload: LoginRequest) => Promise<boolean>;
    fetchUserInfo: () => Promise<void>;
    logout: () => Promise<void>;
    uploadFile: (file: File) => Promise<string>;
    updateProfile: (userName: string, userAvatar: string) => Promise<boolean>;
    updatePassword: (payload: UpdatePasswordRequest) => Promise<boolean>;
    fetchDocuments: () => Promise<void>;
}, "sendCode" | "register" | "login" | "fetchUserInfo" | "logout" | "uploadFile" | "updateProfile" | "updatePassword" | "fetchDocuments">>;
