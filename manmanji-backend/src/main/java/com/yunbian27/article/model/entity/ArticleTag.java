package com.yunbian27.article.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("article_tags")
public class ArticleTag {

    /** 文章ID */
    private Long articleId;
    /** 标签ID */
    private Long tagId;
}
