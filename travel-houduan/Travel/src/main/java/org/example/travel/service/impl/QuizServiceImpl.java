package org.example.travel.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.agent.QuizAgent;
import org.example.travel.constants.CacheConstants;
import org.example.travel.exception.BusinessException;
import org.example.travel.exception.ErrorCode;
import org.example.travel.mapper.QuizGameRecordMapper;
import org.example.travel.mapper.QuizQuestionMapper;
import org.example.travel.model.dto.quiz.StartGameRequest;
import org.example.travel.model.dto.quiz.SubmitAnswerRequest;
import org.example.travel.model.entity.QuizGameRecord;
import org.example.travel.model.entity.QuizQuestion;
import org.example.travel.service.CacheService;
import org.example.travel.service.QuizService;
import org.example.travel.service.RankingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 题目游戏服务实现
 */
@Slf4j
@Service
public class QuizServiceImpl implements QuizService {
    
    @Resource
    private QuizAgent quizAgent;
    
    @Resource
    private QuizQuestionMapper questionMapper;
    
    @Resource
    private QuizGameRecordMapper gameRecordMapper;
    
    @Resource
    private RankingService rankingService;
    
    @Resource
    private CacheService cacheService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startGame(StartGameRequest request, Long userId) {
        log.info("开始游戏: userId={}, request={}", userId, request);
        
        // 1. 生成或获取题目
        List<QuizQuestion> questions;
        if (request.getProjectName() != null) {
            // 指定项目：先从缓存获取，缓存不足则生成
            questions = getQuestionsFromCacheOrGenerate(
                    request.getProjectName(),
                    request.getDifficulty(),
                    request.getQuestionCount()
            );
        } else {
            // 随机获取题目
            questions = questionMapper.selectRandomQuestions(
                    request.getDifficulty(),
                    request.getQuestionCount()
            );
            
            // 如果数据库题目不足，生成新题目
            if (questions.size() < request.getQuestionCount()) {
                List<String> projects = Arrays.asList("苏绣", "景泰蓝", "剪纸", "泥塑");
                String randomProject = projects.get(new Random().nextInt(projects.size()));
                questions = getQuestionsFromCacheOrGenerate(
                        randomProject,
                        request.getDifficulty(),
                        request.getQuestionCount()
                );
            }
        }
        
        // 2. 创建游戏记录
        QuizGameRecord gameRecord = new QuizGameRecord();
        gameRecord.setUserId(userId);
        gameRecord.setGameMode(request.getGameMode());
        gameRecord.setTotalQuestions(questions.size());
        gameRecord.setCorrectCount(0);
        gameRecord.setTotalScore(0);
        gameRecord.setStatus("playing");
        gameRecord.setStartedAt(new Date());
        
        gameRecordMapper.insert(gameRecord);
        
        // 3. 返回游戏信息（不包含正确答案）
        List<Map<String, Object>> questionList = new ArrayList<>();
        for (QuizQuestion q : questions) {
            Map<String, Object> questionMap = new HashMap<>();
            questionMap.put("id", q.getId());
            questionMap.put("questionText", q.getQuestionText());
            questionMap.put("options", q.getOptions());
            questionMap.put("points", q.getPoints());
            questionMap.put("difficulty", q.getDifficulty());
            questionList.add(questionMap);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("gameRecordId", gameRecord.getId());
        result.put("questions", questionList);
        result.put("totalQuestions", questions.size());
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitAnswer(SubmitAnswerRequest request, Long userId) {
        // 1. 验证游戏记录
        QuizGameRecord gameRecord = gameRecordMapper.selectById(request.getGameRecordId());
        if (gameRecord == null || !gameRecord.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "游戏记录不存在");
        }
        
        if (!"playing".equals(gameRecord.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "游戏已结束");
        }
        
        // 2. 获取题目（使用缓存）
        QuizQuestion question = getQuestionById(request.getQuestionId());
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        
        // 3. 判断答案是否正确
        boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(request.getUserAnswer());
        int score = isCorrect ? question.getPoints() : 0;
        
        // 4. 更新游戏记录
        if (isCorrect) {
            gameRecord.setCorrectCount(gameRecord.getCorrectCount() + 1);
        }
        gameRecord.setTotalScore(gameRecord.getTotalScore() + score);
        gameRecordMapper.updateById(gameRecord);
        
        // 5. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("isCorrect", isCorrect);
        result.put("correctAnswer", question.getCorrectAnswer());
        result.put("explanation", question.getExplanation());
        result.put("score", score);
        result.put("totalScore", gameRecord.getTotalScore());
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuizGameRecord completeGame(Long gameRecordId, Long userId) {
        // 1. 获取游戏记录
        QuizGameRecord gameRecord = gameRecordMapper.selectById(gameRecordId);
        if (gameRecord == null || !gameRecord.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "游戏记录不存在");
        }
        
        // 2. 计算正确率
        BigDecimal accuracy = BigDecimal.ZERO;
        if (gameRecord.getTotalQuestions() > 0) {
            accuracy = BigDecimal.valueOf(gameRecord.getCorrectCount())
                    .divide(BigDecimal.valueOf(gameRecord.getTotalQuestions()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        // 3. 更新游戏记录
        gameRecord.setAccuracy(accuracy);
        gameRecord.setStatus("completed");
        gameRecord.setCompletedAt(new Date());
        gameRecordMapper.updateById(gameRecord);
        
        // 4. 更新排行榜
        rankingService.updateUserScore(userId, gameRecord.getTotalScore());
        
        log.info("游戏完成: userId={}, gameRecordId={}, score={}, accuracy={}",
                userId, gameRecordId, gameRecord.getTotalScore(), accuracy);
        
        return gameRecord;
    }
    
    @Override
    public QuizGameRecord getGameRecord(Long gameRecordId, Long userId) {
        QuizGameRecord gameRecord = gameRecordMapper.selectById(gameRecordId);
        if (gameRecord == null || !gameRecord.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "游戏记录不存在");
        }
        return gameRecord;
    }
    
    @Override
    public List<QuizGameRecord> getGameHistory(Long userId, int page, int pageSize) {
        Page<QuizGameRecord> pageObj = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<QuizGameRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizGameRecord::getUserId, userId)
               .eq(QuizGameRecord::getStatus, "completed")
               .orderByDesc(QuizGameRecord::getCompletedAt);
        
        return gameRecordMapper.selectPage(pageObj, wrapper).getRecords();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<QuizQuestion> generateAndSaveQuestions(String projectName, int difficulty, int count) {
        log.info("生成并保存题目: project={}, difficulty={}, count={}", projectName, difficulty, count);
        
        try {
            // 1. 使用 AI 生成题目
            String jsonContent = quizAgent.generateQuestions(projectName, difficulty, count);
            
            // 2. 解析 JSON
            JSONArray jsonArray = JSONUtil.parseArray(jsonContent);
            List<QuizQuestion> questions = new ArrayList<>();
            List<String> validationErrors = new ArrayList<>();
            
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                
                // 验证题目完整性
                String validationError = validateQuestion(jsonObj, i + 1);
                if (validationError != null) {
                    validationErrors.add(validationError);
                    continue; // 跳过不完整的题目
                }
                
                QuizQuestion question = new QuizQuestion();
                question.setCategory("非遗知识");
                question.setDifficulty(difficulty);
                question.setQuestionText(jsonObj.getStr("questionText"));
                question.setOptions(jsonObj.getJSONArray("options").toList(String.class));
                question.setCorrectAnswer(jsonObj.getStr("correctAnswer"));
                question.setExplanation(jsonObj.getStr("explanation"));
                question.setPoints(jsonObj.getInt("points", 10));
                question.setCreatedBy("AI");
                question.setCreatedAt(new Date());
                
                questions.add(question);
            }
            
            // 如果有验证错误，记录日志
            if (!validationErrors.isEmpty()) {
                log.warn("题目验证失败，跳过不完整的题目: {}", String.join("; ", validationErrors));
            }
            
            // 如果没有任何有效题目，抛出异常
            if (questions.isEmpty()) {
                String errorMsg = "生成的题目都不完整，无法入库。错误详情: " + String.join("; ", validationErrors);
                log.error(errorMsg);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目生成失败：所有题目都不完整");
            }
            
            // 如果有效题目数量不足，记录警告
            if (questions.size() < count) {
                log.warn("生成的有效题目数量({})少于请求数量({})", questions.size(), count);
            }
            
            // 3. 保存到数据库
            for (QuizQuestion question : questions) {
                questionMapper.insert(question);
            }
            
            log.info("题目保存成功，共 {} 道（请求 {} 道）", questions.size(), count);
            
            // 4. 更新缓存
            String cacheKey = CacheConstants.buildKey(
                CacheConstants.QUIZ_QUESTIONS_PREFIX,
                projectName,
                difficulty
            );
            cacheService.set(
                cacheKey,
                questions,
                CacheConstants.QUIZ_QUESTIONS_TIMEOUT,
                CacheConstants.QUIZ_QUESTIONS_UNIT
            );
            
            return questions;
            
        } catch (BusinessException e) {
            throw e; // 重新抛出业务异常
        } catch (Exception e) {
            log.error("生成并保存题目失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证题目完整性
     * @param jsonObj 题目 JSON 对象
     * @param index 题目序号（用于错误提示）
     * @return 如果验证失败返回错误信息，否则返回 null
     */
    private String validateQuestion(JSONObject jsonObj, int index) {
        // 检查必需字段
        if (jsonObj.getStr("questionText") == null || jsonObj.getStr("questionText").trim().isEmpty()) {
            return String.format("第%d题缺少题目文本", index);
        }
        
        if (jsonObj.getJSONArray("options") == null || jsonObj.getJSONArray("options").isEmpty()) {
            return String.format("第%d题缺少选项", index);
        }
        
        List<String> options = jsonObj.getJSONArray("options").toList(String.class);
        if (options.size() < 2) {
            return String.format("第%d题选项数量不足（至少需要2个）", index);
        }
        
        // 检查选项是否为空
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i) == null || options.get(i).trim().isEmpty()) {
                return String.format("第%d题的选项%d为空", index, i + 1);
            }
        }
        
        if (jsonObj.getStr("correctAnswer") == null || jsonObj.getStr("correctAnswer").trim().isEmpty()) {
            return String.format("第%d题缺少正确答案", index);
        }
        
        if (jsonObj.getStr("explanation") == null || jsonObj.getStr("explanation").trim().isEmpty()) {
            return String.format("第%d题缺少答案解析", index);
        }
        
        if (jsonObj.getInt("points") == null || jsonObj.getInt("points") <= 0) {
            return String.format("第%d题缺少分值或分值无效", index);
        }
        
        return null; // 验证通过
    }
    
    /**
     * 从缓存获取题目，缓存不足则生成
     */
    private List<QuizQuestion> getQuestionsFromCacheOrGenerate(String projectName, int difficulty, int count) {
        String cacheKey = CacheConstants.buildKey(
            CacheConstants.QUIZ_QUESTIONS_PREFIX,
            projectName,
            difficulty
        );
        
        // 尝试从缓存获取
        List<QuizQuestion> cachedQuestions = cacheService.getList(
            cacheKey,
            () -> null, // 不自动加载
            CacheConstants.QUIZ_QUESTIONS_TIMEOUT,
            CacheConstants.QUIZ_QUESTIONS_UNIT,
            QuizQuestion.class
        );
        
        // 如果缓存中有足够的题目，随机返回
        if (cachedQuestions != null && cachedQuestions.size() >= count) {
            log.info("从缓存获取题目: project={}, difficulty={}, 缓存数量={}", 
                projectName, difficulty, cachedQuestions.size());
            
            // 随机选择题目
            List<QuizQuestion> selected = new ArrayList<>();
            List<QuizQuestion> temp = new ArrayList<>(cachedQuestions);
            Random random = new Random();
            for (int i = 0; i < count && !temp.isEmpty(); i++) {
                int index = random.nextInt(temp.size());
                selected.add(temp.remove(index));
            }
            return selected;
        }
        
        // 缓存不足，生成新题目
        log.info("缓存题目不足，生成新题目: project={}, difficulty={}", projectName, difficulty);
        return generateAndSaveQuestions(projectName, difficulty, count);
    }
    
    /**
     * 获取单个题目（使用缓存）
     */
    private QuizQuestion getQuestionById(Long id) {
        String cacheKey = CacheConstants.buildKey(
            CacheConstants.QUIZ_QUESTION_PREFIX,
            id
        );
        
        return cacheService.get(
            cacheKey,
            () -> questionMapper.selectById(id),
            CacheConstants.QUIZ_QUESTION_TIMEOUT,
            CacheConstants.QUIZ_QUESTION_UNIT,
            QuizQuestion.class
        );
    }
}
