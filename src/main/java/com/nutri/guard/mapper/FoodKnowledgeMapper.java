package com.nutri.guard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nutri.guard.entity.FoodKnowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 营养知识库 Mapper 接口
 */
@Mapper
public interface FoodKnowledgeMapper extends BaseMapper<FoodKnowledge> {
}
