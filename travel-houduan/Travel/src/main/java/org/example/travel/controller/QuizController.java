package org.example.travel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.example.travel.model.dto.quiz.RankingDTO;
import org.example.travel.model.dto.quiz.StartGameRequest;
import org.example.travel.model.dto.quiz.SubmitAnswerRequest;
import org.example.travel.model.entity.QuizGameRecord;
import org.example.travel.model.entity.User;
import org.example.travel.service.QuizService;
import org.example.travel.service.RankingService;
import org.example.travel.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识闯关游戏控制器
 */
@RestController
@RequestMapping("/quiz")
@Tag(name = "知识闯关游戏接口")
@Slf4j
public class QuizController {
    
    @Resource
    private QuizService quizService;
    
    @Resource
    private RankingService rankingService;
    
    @Resource
    private UserService userService;
    
    /**
     * 开始游戏
     */
    @PostMapping("/start")
    @Operation(summary = "开始游戏")
    public BaseResponse<Map<String, Object>> startGame(@RequestBody StartGameRequest request, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        
        Map<String, Object> result = quizService.startGame(request, loginUser.getId());
        return Result.success(result);
    }
    
    /**
     * 提交答案
     */
    @PostMapping("/answer")
    @Operation(summary = "提交答案")
    public BaseResponse<Map<String, Object>> submitAnswer(@RequestBody SubmitAnswerRequest request, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        
        Map<String, Object> result = quizService.submitAnswer(request, loginUser.getId());
        return Result.success(result);
    }
    
    /**
     * 完成游戏
     */
    @PostMapping("/complete/{gameRecordId}")
    @Operation(summary = "完成游戏")
    public BaseResponse<QuizGameRecord> completeGame(@PathVariable Long gameRecordId, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        
        QuizGameRecord record = quizService.completeGame(gameRecordId, loginUser.getId());
        return Result.success(record);
    }
    
    /**
     * 获取游戏记录
     */
    @GetMapping("/record/{gameRecordId}")
    @Operation(summary = "获取游戏记录")
    public BaseResponse<QuizGameRecord> getGameRecord(@PathVariable Long gameRecordId, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        
        QuizGameRecord record = quizService.getGameRecord(gameRecordId, loginUser.getId());
        return Result.success(record);
    }
    
    /**
     * 获取历史记录
     */
    @GetMapping("/history")
    @Operation(summary = "获取历史记录")
    public BaseResponse<List<QuizGameRecord>> getGameHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        
        List<QuizGameRecord> records = quizService.getGameHistory(loginUser.getId(), page, pageSize);
        return Result.success(records);
    }
    
    /**
     * 获取积分排行榜
     */
    @GetMapping("/ranking/points")
    @Operation(summary = "积分排行榜")
    public BaseResponse<List<RankingDTO>> getPointsRanking(
            @RequestParam(defaultValue = "100") int topN) {
        List<RankingDTO> rankings = rankingService.getPointsRanking(topN);
        return Result.success(rankings);
    }
    
    /**
     * 获取周排行榜
     */
    @GetMapping("/ranking/weekly")
    @Operation(summary = "周排行榜")
    public BaseResponse<List<RankingDTO>> getWeeklyRanking(
            @RequestParam(defaultValue = "100") int topN) {
        List<RankingDTO> rankings = rankingService.getWeeklyRanking(topN);
        return Result.success(rankings);
    }
    
    /**
     * 获取月排行榜
     */
    @GetMapping("/ranking/monthly")
    @Operation(summary = "月排行榜")
    public BaseResponse<List<RankingDTO>> getMonthlyRanking(
            @RequestParam(defaultValue = "100") int topN) {
        List<RankingDTO> rankings = rankingService.getMonthlyRanking(topN);
        return Result.success(rankings);
    }
    
    /**
     * 获取我的排名
     */
    @GetMapping("/ranking/my")
    @Operation(summary = "我的排名")
    public BaseResponse<Long> getMyRank(HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        
        Long rank = rankingService.getUserRank(loginUser.getId());
        return Result.success(rank);
    }
}
