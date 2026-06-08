package com.yunbian27.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunbian27.content.model.entity.Article;
import com.yunbian27.content.model.vo.ArticleTitlesVO;
import com.yunbian27.content.model.vo.FolderTree;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Select("select id, title, status from articles where user_id = #{userId}")
    List<FolderTree.ArticleBasic> getArticleBasics(Long userId);

    @Select("select id, title, status ,visibility ,updated_at from articles where user_id = #{userId}")
    List<ArticleTitlesVO> selectArticleTitlestByUserId(Long userId);
}
