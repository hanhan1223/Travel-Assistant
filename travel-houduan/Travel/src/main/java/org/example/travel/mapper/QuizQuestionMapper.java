package org.example.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.travel.model.entity.QuizQuestion;

import java.util.List;

/**
 * 题目 Mapper
 */
@Mapper
public interface QuizQuestionMapper extends BaseMapper<QuizQuestion> {
    
    /**
     * 随机获取题目
     */
    @Select("SELECT * FROM quiz_question WHERE difficulty = #{difficulty} AND is_deleted = 0 ORDER BY RAND() LIMIT #{count}")
    List<QuizQuestion> selectRandomQuestions(int difficulty, int count);
}
