package com.yunbian27.content.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("article_groups")
public class ArticleGroup {

    /** 文章ID */
    private Long articleId;
    /** 分组ID */
    private Long groupId;
}
