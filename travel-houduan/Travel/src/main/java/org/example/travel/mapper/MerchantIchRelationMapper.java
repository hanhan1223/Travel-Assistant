package org.example.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.travel.model.entity.MerchantIchRelation;

/**
 * 商户-非遗关联Mapper
 */
@Mapper
public interface MerchantIchRelationMapper extends BaseMapper<MerchantIchRelation> {
}
