package com.yunbian27.interaction.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_likes")
public class ArticleLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
