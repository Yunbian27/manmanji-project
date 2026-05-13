package com.yunbian27.ai;

import com.yunbian27.ai.config.LlmProviderProperties;
import com.yunbian27.ai.mapper.LlmProviderMapper;
import com.yunbian27.ai.model.CreateProviderDTO;
import com.yunbian27.ai.model.LlmProviderEntity;
import com.yunbian27.ai.service.AiService;
import com.yunbian27.ai.utils.ApiKeyEncryption;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private LlmProviderMapper llmProviderMapper;
    @Mock
    private LlmProviderProperties providerProperties;
    @Mock
    private ApiKeyEncryption encryption;

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService(llmProviderMapper, providerProperties, encryption);
    }

    @Test
    void shouldCreateProviderSuccessfully() {
        CreateProviderDTO dto = buildDTO("dashscope", "https://dashscope.aliyuncs.com/compatible-mode/v1",
                "sk-test-key", "qwen3.5-flash");
        when(llmProviderMapper.selectById("dashscope")).thenReturn(null);
        when(providerProperties.getEmbeddingDimensions()).thenReturn(1024);
        when(encryption.encrypt("sk-test-key"))
                .thenReturn(new ApiKeyEncryption.EncryptedValue("nonce-abc", "cipher-xyz"));

        aiService.createProvider(dto);

        ArgumentCaptor<LlmProviderEntity> captor = ArgumentCaptor.forClass(LlmProviderEntity.class);
        verify(llmProviderMapper).insert(captor.capture());
        LlmProviderEntity entity = captor.getValue();
        assertThat(entity.getId()).isEqualTo("dashscope");
        assertThat(entity.getBaseUrl()).isEqualTo("https://dashscope.aliyuncs.com/compatible-mode/v1");
        assertThat(entity.getModel()).isEqualTo("qwen3.5-flash");
        assertThat(entity.getApiKeyNonce()).isEqualTo("nonce-abc");
        assertThat(entity.getApiKeyCiphertext()).isEqualTo("cipher-xyz");
        assertThat(entity.getEnabled()).isTrue();
        assertThat(entity.getBuiltin()).isFalse();
    }

    @Test
    void shouldCreateProviderWithEmbeddingConfig() {
        CreateProviderDTO dto = buildDTO("glm", "https://open.bigmodel.cn/api/coding/paas/v4",
                "sk-test-key", "glm-5");
        dto.setSupportsEmbedding(true);
        dto.setEmbeddingModel("embedding-3");
        dto.setEmbeddingDimensions(1024);
        when(llmProviderMapper.selectById("glm")).thenReturn(null);
        when(encryption.encrypt(anyString()))
                .thenReturn(new ApiKeyEncryption.EncryptedValue("n", "c"));

        aiService.createProvider(dto);

        verify(llmProviderMapper).insert(any(LlmProviderEntity.class));
    }

    @Test
    void shouldThrowWhenProviderAlreadyExists() {
        CreateProviderDTO dto = buildDTO("dashscope", "https://example.com/v1",
                "sk-key", "qwen3.5-flash");
        when(llmProviderMapper.selectById("dashscope")).thenReturn(LlmProviderEntity.builder().build());

        assertThatThrownBy(() -> aiService.createProvider(dto))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.PROVIDER_ALREADY_EXISTS.getCode());
    }

    @Test
    void shouldThrowWhenBaseUrlIsBlank() {
        CreateProviderDTO dto = buildDTO("dashscope", "   ", "sk-key", "qwen3.5-flash");
        when(llmProviderMapper.selectById("dashscope")).thenReturn(null);

        assertThatThrownBy(() -> aiService.createProvider(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("baseUrl 不能为空");
    }

    @Test
    void shouldThrowWhenBaseUrlIsNull() {
        CreateProviderDTO dto = buildDTO("dashscope", null, "sk-key", "qwen3.5-flash");
        when(llmProviderMapper.selectById("dashscope")).thenReturn(null);

        assertThatThrownBy(() -> aiService.createProvider(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("baseUrl 不能为空");
    }

    @Test
    void shouldThrowWhenModelIsBlank() {
        CreateProviderDTO dto = buildDTO("dashscope",
                "https://dashscope.aliyuncs.com/compatible-mode/v1", "sk-key", "");
        when(llmProviderMapper.selectById("dashscope")).thenReturn(null);

        assertThatThrownBy(() -> aiService.createProvider(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("model 不能为空");
    }

    @Test
    void shouldThrowWhenApiKeyIsBlank() {
        CreateProviderDTO dto = buildDTO("dashscope",
                "https://dashscope.aliyuncs.com/compatible-mode/v1", null, "qwen3.5-flash");
        when(llmProviderMapper.selectById("dashscope")).thenReturn(null);

        assertThatThrownBy(() -> aiService.createProvider(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("apiKey 不能为空");
    }

    @Test
    void shouldThrowWhenSupportsEmbeddingWithoutEmbeddingModel() {
        CreateProviderDTO dto = buildDTO("dashscope",
                "https://dashscope.aliyuncs.com/compatible-mode/v1", "sk-key", "qwen3.5-flash");
        dto.setSupportsEmbedding(true);
        dto.setEmbeddingDimensions(1024);
        when(llmProviderMapper.selectById("dashscope")).thenReturn(null);

        assertThatThrownBy(() -> aiService.createProvider(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("支持 Embedding 的 Provider 必须填写 embeddingModel");
    }

    @Test
    void shouldThrowWhenEmbeddingModelLooksLikeChatModel() {
        CreateProviderDTO dto = buildDTO("dashscope",
                "https://dashscope.aliyuncs.com/compatible-mode/v1", "sk-key", "qwen3.5-flash");
        dto.setSupportsEmbedding(true);
        dto.setEmbeddingModel("qwen3.5-flash");
        dto.setEmbeddingDimensions(1024);
        when(llmProviderMapper.selectById("dashscope")).thenReturn(null);

        assertThatThrownBy(() -> aiService.createProvider(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Embedding Model 不能填写聊天模型");
    }

    @Test
    void shouldThrowWhenEmbeddingDimensionsIsInvalid() {
        CreateProviderDTO dto = buildDTO("dashscope",
                "https://dashscope.aliyuncs.com/compatible-mode/v1", "sk-key", "qwen3.5-flash");
        dto.setSupportsEmbedding(true);
        dto.setEmbeddingModel("text-embedding-v3");
        dto.setEmbeddingDimensions(0);
        when(llmProviderMapper.selectById("dashscope")).thenReturn(null);

        assertThatThrownBy(() -> aiService.createProvider(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("向量维度必须为正整数");
    }

    @Test
    void shouldDefaultEmbeddingDimensionsFromPropertiesWhenNotProvided() {
        CreateProviderDTO dto = buildDTO("dashscope",
                "https://dashscope.aliyuncs.com/compatible-mode/v1", "sk-key", "qwen3.5-flash");
        when(llmProviderMapper.selectById("dashscope")).thenReturn(null);
        when(providerProperties.getEmbeddingDimensions()).thenReturn(2048);
        when(encryption.encrypt(anyString()))
                .thenReturn(new ApiKeyEncryption.EncryptedValue("n", "c"));

        aiService.createProvider(dto);

        verify(providerProperties).getEmbeddingDimensions();
        verify(llmProviderMapper).insert(any(LlmProviderEntity.class));
    }

    private CreateProviderDTO buildDTO(String id, String baseUrl, String apiKey, String model) {
        CreateProviderDTO dto = new CreateProviderDTO();
        dto.setId(id);
        dto.setBaseUrl(baseUrl);
        dto.setApiKey(apiKey);
        dto.setModel(model);
        return dto;
    }
}
