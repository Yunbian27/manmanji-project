package com.yunbian27.interaction.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comments")
public class Comment {

    @TableId(type = IdType.AUTO)
    /** 主键 */
    private Long id;

    /** 文章ID */
    private Long articleId;
    /** 评论用户ID */
    private Long userId;
    /** 父评论ID(支持两层嵌套回复) */
    private Long parentId;
    /** 评论内容 */
    private String content;
    /** 是否已删除(软删除) */
    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    /** 创建时间 */
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
