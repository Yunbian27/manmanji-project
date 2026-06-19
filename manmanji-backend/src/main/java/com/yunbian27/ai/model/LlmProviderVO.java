package com.yunbian27.ai.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LlmProviderVO {
    private String id;
    private String baseUrl;
    private String model;
    private String embeddingModel;
    private Integer embeddingDimensions;
    private Boolean supportsEmbedding;
    private Double temperature;
    private Boolean enabled;
    private Boolean builtin;
    private boolean hasApiKey;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
