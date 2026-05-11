package com.yunbian27.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_conversations")
public class AiConversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String modelName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
