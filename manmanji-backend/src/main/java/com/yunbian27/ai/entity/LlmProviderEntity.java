package com.yunbian27.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("llm_provider_config")
public class LlmProviderEntity {

    /** 主键，如dashscope/openai/deepseek等 */
    @TableId
    private String id;

    /** API基础地址 */
    @TableField("base_url")
    private String baseUrl;

    /** API密钥密文(加密存储) */
    @TableField("api_key_ciphertext")
    private String apiKeyCiphertext;

    /** API密钥加密用Nonce */
    @TableField("api_key_nonce")
    private String apiKeyNonce;

    /** 对话模型名称，如deepseek-chat */
    private String model;

    /** Embedding模型名称 */
    @TableField("embedding_model")
    private String embeddingModel;

    /** Embedding输出维度 */
    @TableField("embedding_dimensions")
    private Integer embeddingDimensions;

    /** 是否支持Embedding能力 */
    @TableField("supports_embedding")
    private Boolean supportsEmbedding;

    /** 模型温度(0.0~2.0)，控制输出随机性 */
    private Double temperature;

    /** 是否启用 */
    private Boolean enabled;

    /** 是否内置(内置提供商不可删除) */
    private Boolean builtin;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
