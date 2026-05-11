package com.yunbian27.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunbian27.interaction.entity.ArticleLike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LikeMapper extends BaseMapper<ArticleLike> {
}
