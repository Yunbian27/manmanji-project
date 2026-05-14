package com.yunbian27.llmprovider.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_messages")
public class AiMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 对话ID */
    private Long conversationId;
    /** 角色(user/assistant/system) */
    private String role;
    /** 消息内容(Markdown格式) */
    private String content;
    /** 引用的文章数据(JSONB格式) */
    private String referencesJson;
    /** 消耗token数 */
    private Integer tokenCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
