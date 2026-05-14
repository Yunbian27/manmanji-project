package com.yunbian27.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunbian27.user.model.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FollowMapper extends BaseMapper<UserFollow> {
}
