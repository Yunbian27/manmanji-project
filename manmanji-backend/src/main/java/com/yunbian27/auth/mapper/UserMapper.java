package com.yunbian27.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunbian27.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
