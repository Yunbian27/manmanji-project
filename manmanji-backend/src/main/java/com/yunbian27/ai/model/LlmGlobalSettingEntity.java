package com.yunbian27.ai.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("llm_global_setting")
public class LlmGlobalSettingEntity {

    public static final Long SINGLETON_ID = 1L;

    @TableId(type = IdType.INPUT)
    private Long id;

    private String defaultChatProviderId;
    private String defaultEmbeddingProviderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
