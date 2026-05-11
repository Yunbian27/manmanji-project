package com.yunbian27.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_versions")
public class ArticleVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;
    private String title;
    private String content;
    private Integer versionNumber;
    private String changeSummary;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
