package com.yunbian27.interaction.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_likes")
public class ArticleLike {

    @TableId(type = IdType.AUTO)
    /** 主键 */
    private Long id;

    /** 文章ID */
    private Long articleId;
    /** 用户ID */
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    /** 点赞时间(article_id+user_id唯一索引防止重复点赞) */
    private LocalDateTime createdAt;
}
