package com.nutri.guard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nutri.guard.entity.DietLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 饮食日记 Mapper 接口
 */
@Mapper
public interface DietLogMapper extends BaseMapper<DietLog> {
}
