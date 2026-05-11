package com.yunbian27.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("articles")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String slug;
    private String content;
    private String summary;
    private String coverUrl;
    private Long authorId;
    private Long categoryId;
    private String status;
    private String sourceType;
    private String sourcePrompt;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer bookmarkCount;
    private Boolean isOriginal;
    private String sourceUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;
}
