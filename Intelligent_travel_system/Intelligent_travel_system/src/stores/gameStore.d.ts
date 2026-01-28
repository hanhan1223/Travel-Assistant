import type { GameSession, QuizQuestion, AnswerResult, GameResult, RankingItem } from '../types/api';
export declare const useGameStore: import("pinia").StoreDefinition<"game", Pick<{
    isPlaying: import("vue").Ref<boolean, boolean>;
    currentSession: import("vue").Ref<{
        gameRecordId: number;
        questions: {
            id: number;
            questionText: string;
            questionType: "single" | "multiple" | "boolean";
            options: string[];
            points: number;
            difficulty: number;
        }[];
        totalQuestions: number;
        startedAt: string;
    } | null, GameSession | {
        gameRecordId: number;
        questions: {
            id: number;
            questionText: string;
            questionType: "single" | "multiple" | "boolean";
            options: string[];
            points: number;
            difficulty: number;
        }[];
        totalQuestions: number;
        startedAt: string;
    } | null>;
    currentQuestion: import("vue").Ref<{
        id: number;
        questionText: string;
        questionType: "single" | "multiple" | "boolean";
        options: string[];
        points: number;
        difficulty: number;
    } | null, QuizQuestion | {
        id: number;
        questionText: string;
        questionType: "single" | "multiple" | "boolean";
        options: string[];
        points: number;
        difficulty: number;
    } | null>;
    currentQuestionIndex: import("vue").Ref<number, number>;
    lastResult: import("vue").Ref<{
        id: number;
        userId: number;
        gameMode: string;
        totalQuestions: number;
        correctCount: number;
        totalScore: number;
        accuracy: number;
        timeSpent: number;
        status: string;
        startedAt: string;
        completedAt: string;
    } | null, GameResult | {
        id: number;
        userId: number;
        gameMode: string;
        totalQuestions: number;
        correctCount: number;
        totalScore: number;
        accuracy: number;
        timeSpent: number;
        status: string;
        startedAt: string;
        completedAt: string;
    } | null>;
    rankingList: import("vue").Ref<{
        rank: number;
        userId: number;
        username: string;
        avatar: string;
        points: number;
        level: number;
        totalGames: number;
        bestAccuracy: number;
    }[], RankingItem[] | {
        rank: number;
        userId: number;
        username: string;
        avatar: string;
        points: number;
        level: number;
        totalGames: number;
        bestAccuracy: number;
    }[]>;
    myRank: import("vue").Ref<number | null, number | null>;
    currentRankingType: import("vue").Ref<"weekly" | "monthly", "weekly" | "monthly">;
    startGame: (mode?: string, difficulty?: number, projectName?: string) => Promise<boolean>;
    submitAnswer: (answer: string, timeSpent: number) => Promise<AnswerResult | null>;
    nextQuestion: () => boolean;
    completeGame: () => Promise<void>;
    fetchRankings: (type?: "weekly" | "monthly") => Promise<void>;
}, "isPlaying" | "currentSession" | "currentQuestion" | "currentQuestionIndex" | "lastResult" | "rankingList" | "myRank" | "currentRankingType">, Pick<{
    isPlaying: import("vue").Ref<boolean, boolean>;
    currentSession: import("vue").Ref<{
        gameRecordId: number;
        questions: {
            id: number;
            questionText: string;
            questionType: "single" | "multiple" | "boolean";
            options: string[];
            points: number;
            difficulty: number;
        }[];
        totalQuestions: number;
        startedAt: string;
    } | null, GameSession | {
        gameRecordId: number;
        questions: {
            id: number;
            questionText: string;
            questionType: "single" | "multiple" | "boolean";
            options: string[];
            points: number;
            difficulty: number;
        }[];
        totalQuestions: number;
        startedAt: string;
    } | null>;
    currentQuestion: import("vue").Ref<{
        id: number;
        questionText: string;
        questionType: "single" | "multiple" | "boolean";
        options: string[];
        points: number;
        difficulty: number;
    } | null, QuizQuestion | {
        id: number;
        questionText: string;
        questionType: "single" | "multiple" | "boolean";
        options: string[];
        points: number;
        difficulty: number;
    } | null>;
    currentQuestionIndex: import("vue").Ref<number, number>;
    lastResult: import("vue").Ref<{
        id: number;
        userId: number;
        gameMode: string;
        totalQuestions: number;
        correctCount: number;
        totalScore: number;
        accuracy: number;
        timeSpent: number;
        status: string;
        startedAt: string;
        completedAt: string;
    } | null, GameResult | {
        id: number;
        userId: number;
        gameMode: string;
        totalQuestions: number;
        correctCount: number;
        totalScore: number;
        accuracy: number;
        timeSpent: number;
        status: string;
        startedAt: string;
        completedAt: string;
    } | null>;
    rankingList: import("vue").Ref<{
        rank: number;
        userId: number;
        username: string;
        avatar: string;
        points: number;
        level: number;
        totalGames: number;
        bestAccuracy: number;
    }[], RankingItem[] | {
        rank: number;
        userId: number;
        username: string;
        avatar: string;
        points: number;
        level: number;
        totalGames: number;
        bestAccuracy: number;
    }[]>;
    myRank: import("vue").Ref<number | null, number | null>;
    currentRankingType: import("vue").Ref<"weekly" | "monthly", "weekly" | "monthly">;
    startGame: (mode?: string, difficulty?: number, projectName?: string) => Promise<boolean>;
    submitAnswer: (answer: string, timeSpent: number) => Promise<AnswerResult | null>;
    nextQuestion: () => boolean;
    completeGame: () => Promise<void>;
    fetchRankings: (type?: "weekly" | "monthly") => Promise<void>;
}, never>, Pick<{
    isPlaying: import("vue").Ref<boolean, boolean>;
    currentSession: import("vue").Ref<{
        gameRecordId: number;
        questions: {
            id: number;
            questionText: string;
            questionType: "single" | "multiple" | "boolean";
            options: string[];
            points: number;
            difficulty: number;
        }[];
        totalQuestions: number;
        startedAt: string;
    } | null, GameSession | {
        gameRecordId: number;
        questions: {
            id: number;
            questionText: string;
            questionType: "single" | "multiple" | "boolean";
            options: string[];
            points: number;
            difficulty: number;
        }[];
        totalQuestions: number;
        startedAt: string;
    } | null>;
    currentQuestion: import("vue").Ref<{
        id: number;
        questionText: string;
        questionType: "single" | "multiple" | "boolean";
        options: string[];
        points: number;
        difficulty: number;
    } | null, QuizQuestion | {
        id: number;
        questionText: string;
        questionType: "single" | "multiple" | "boolean";
        options: string[];
        points: number;
        difficulty: number;
    } | null>;
    currentQuestionIndex: import("vue").Ref<number, number>;
    lastResult: import("vue").Ref<{
        id: number;
        userId: number;
        gameMode: string;
        totalQuestions: number;
        correctCount: number;
        totalScore: number;
        accuracy: number;
        timeSpent: number;
        status: string;
        startedAt: string;
        completedAt: string;
    } | null, GameResult | {
        id: number;
        userId: number;
        gameMode: string;
        totalQuestions: number;
        correctCount: number;
        totalScore: number;
        accuracy: number;
        timeSpent: number;
        status: string;
        startedAt: string;
        completedAt: string;
    } | null>;
    rankingList: import("vue").Ref<{
        rank: number;
        userId: number;
        username: string;
        avatar: string;
        points: number;
        level: number;
        totalGames: number;
        bestAccuracy: number;
    }[], RankingItem[] | {
        rank: number;
        userId: number;
        username: string;
        avatar: string;
        points: number;
        level: number;
        totalGames: number;
        bestAccuracy: number;
    }[]>;
    myRank: import("vue").Ref<number | null, number | null>;
    currentRankingType: import("vue").Ref<"weekly" | "monthly", "weekly" | "monthly">;
    startGame: (mode?: string, difficulty?: number, projectName?: string) => Promise<boolean>;
    submitAnswer: (answer: string, timeSpent: number) => Promise<AnswerResult | null>;
    nextQuestion: () => boolean;
    completeGame: () => Promise<void>;
    fetchRankings: (type?: "weekly" | "monthly") => Promise<void>;
}, "startGame" | "submitAnswer" | "nextQuestion" | "completeGame" | "fetchRankings">>;
