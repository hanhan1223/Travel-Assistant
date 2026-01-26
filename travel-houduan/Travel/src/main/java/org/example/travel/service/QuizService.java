package org.example.travel.service;

import org.example.travel.model.dto.quiz.StartGameRequest;
import org.example.travel.model.dto.quiz.SubmitAnswerRequest;
import org.example.travel.model.entity.QuizGameRecord;
import org.example.travel.model.entity.QuizQuestion;

import java.util.List;
import java.util.Map;

/**
 * 题目游戏服务接口
 */
public interface QuizService {
    
    /**
     * 开始游戏
     * 
     * @param request 游戏请求
     * @param userId 用户ID
     * @return 游戏记录ID和题目列表
     */
    Map<String, Object> startGame(StartGameRequest request, Long userId);
    
    /**
     * 提交答案
     */
    Map<String, Object> submitAnswer(SubmitAnswerRequest request, Long userId);
    
    /**
     * 完成游戏
     */
    QuizGameRecord completeGame(Long gameRecordId, Long userId);
    
    /**
     * 获取游戏记录
     */
    QuizGameRecord getGameRecord(Long gameRecordId, Long userId);
    
    /**
     * 获取历史记录
     */
    List<QuizGameRecord> getGameHistory(Long userId, int page, int pageSize);
    
    /**
     * 生成题目并保存到数据库
     */
    List<QuizQuestion> generateAndSaveQuestions(String projectName, int difficulty, int count);
}
