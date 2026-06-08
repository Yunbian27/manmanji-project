package com.yunbian27.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunbian27.content.model.entity.Group;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GroupMapper extends BaseMapper<Group> {

    @Select("SELECT name FROM groups WHERE user_id = #{userId}")
    List<String> selectGroupNamesByUserId(Long userId);
}
