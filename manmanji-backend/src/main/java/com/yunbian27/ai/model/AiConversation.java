package com.yunbian27.ai.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_conversations")
public class AiConversation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;
    /** 对话标题(AI自动生成) */
    private String title;
    /** 使用的模型名(如deepseek-chat) */
    private String modelName;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
