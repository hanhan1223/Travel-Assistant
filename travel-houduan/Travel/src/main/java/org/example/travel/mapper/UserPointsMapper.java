package org.example.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.travel.model.entity.UserPoints;

/**
 * 用户积分 Mapper
 */
@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {
}
