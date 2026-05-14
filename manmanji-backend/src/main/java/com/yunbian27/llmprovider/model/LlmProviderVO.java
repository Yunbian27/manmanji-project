package com.yunbian27.llmprovider.model;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
