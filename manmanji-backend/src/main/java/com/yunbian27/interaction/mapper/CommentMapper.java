package com.yunbian27.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunbian27.interaction.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
