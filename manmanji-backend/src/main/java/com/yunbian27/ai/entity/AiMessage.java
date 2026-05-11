package com.yunbian27.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_messages")
public class AiMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;
    private String role;
    private String content;
    private String referencesJson;
    private Integer tokenCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
