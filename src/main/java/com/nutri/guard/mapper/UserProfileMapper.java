package com.nutri.guard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nutri.guard.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}