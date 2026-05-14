package com.yunbian27.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunbian27.article.model.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
