package com.yunbian27.llmprovider.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProviderDTO {

    @NotBlank(message = "Provider ID 不能为空")
    private String id;

    @NotBlank(message = "Base URL 不能为空")
    private String baseUrl;

    @NotBlank(message = "API Key 不能为空")
    private String apiKey;

    @NotBlank(message = "模型名不能为空")
    private String model;

    private String embeddingModel;
    private Integer embeddingDimensions;
    private Boolean supportsEmbedding;
    private Double temperature;
}
