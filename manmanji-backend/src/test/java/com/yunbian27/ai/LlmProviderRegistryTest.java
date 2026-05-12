package com.yunbian27.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunbian27.ai.config.LlmProviderProperties;
import com.yunbian27.ai.entity.LlmProviderEntity;
import com.yunbian27.ai.mapper.LlmProviderMapper;
import com.yunbian27.ai.utils.ApiKeyEncryption;
import com.yunbian27.ai.utils.ApiPathResolver;
import com.yunbian27.ai.utils.LlmProviderRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmProviderRegistryTest {

    @Mock
    private LlmProviderMapper providerMapper;
    @Mock
    private LlmProviderProperties properties;
    @Mock
    private ApiKeyEncryption encryption;
    @Mock
    private ToolCallingManager toolCallingManager;
    @Mock
    private ObservationRegistry observationRegistry;
    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ChatClient chatClient;
    @Mock
    private OpenAiApi openAiApi;

    private LlmProviderRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new LlmProviderRegistry(properties, providerMapper, encryption,
                toolCallingManager, observationRegistry);
    }

    @Test
    void shouldCreateChatClientFromDatabase() {
        try (MockedStatic<ChatClient> chatClientMock = mockStatic(ChatClient.class);
             MockedStatic<ApiPathResolver> apiResolverMock = mockStatic(ApiPathResolver.class)) {

            LlmProviderEntity entity = createEntity("dashscope",
                    "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen3.5-flash");
            when(providerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(entity);
            when(encryption.decrypt(null, null)).thenReturn("decrypted-api-key");
            apiResolverMock.when(() -> ApiPathResolver.buildOpenAiApi(anyString(), anyString()))
                    .thenReturn(openAiApi);
            chatClientMock.when(() -> ChatClient.builder(any(OpenAiChatModel.class)))
                    .thenReturn(chatClientBuilder);
            when(chatClientBuilder.build()).thenReturn(chatClient);

            ChatClient result = registry.getChatClient("dashscope");

            assertThat(result).isSameAs(chatClient);
            verify(providerMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(encryption).decrypt(null, null);
        }
    }

    @Test
    void shouldReturnCachedClientOnSecondCall() {
        try (MockedStatic<ChatClient> chatClientMock = mockStatic(ChatClient.class);
             MockedStatic<ApiPathResolver> apiResolverMock = mockStatic(ApiPathResolver.class)) {

            LlmProviderEntity entity = createEntity("dashscope",
                    "https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen3.5-flash");
            when(providerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(entity);
            when(encryption.decrypt(null, null)).thenReturn("decrypted-api-key");
            apiResolverMock.when(() -> ApiPathResolver.buildOpenAiApi(anyString(), anyString()))
                    .thenReturn(openAiApi);
            chatClientMock.when(() -> ChatClient.builder(any(OpenAiChatModel.class)))
                    .thenReturn(chatClientBuilder);
            when(chatClientBuilder.build()).thenReturn(chatClient);

            registry.getChatClient("dashscope");
            ChatClient result = registry.getChatClient("dashscope");

            assertThat(result).isSameAs(chatClient);
            verify(providerMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    void shouldFallbackToYamlWhenDatabaseReturnsNull() {
        try (MockedStatic<ChatClient> chatClientMock = mockStatic(ChatClient.class);
             MockedStatic<ApiPathResolver> apiResolverMock = mockStatic(ApiPathResolver.class)) {

            when(providerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            LlmProviderProperties.ProviderConfig config = new LlmProviderProperties.ProviderConfig();
            config.setBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1");
            config.setApiKey("yaml-api-key");
            config.setModel("qwen3.5-flash");
            when(properties.getProviders()).thenReturn(Map.of("dashscope", config));

            apiResolverMock.when(() -> ApiPathResolver.buildOpenAiApi(anyString(), anyString()))
                    .thenReturn(openAiApi);
            chatClientMock.when(() -> ChatClient.builder(any(OpenAiChatModel.class)))
                    .thenReturn(chatClientBuilder);
            when(chatClientBuilder.build()).thenReturn(chatClient);

            ChatClient result = registry.getChatClient("dashscope");

            assertThat(result).isSameAs(chatClient);
            verify(providerMapper).selectOne(any(LambdaQueryWrapper.class));
            verify(properties).getProviders();
            verify(encryption, never()).decrypt(anyString(), anyString());
        }
    }

    @Test
    void shouldThrowExceptionWhenProviderNotFound() {
        when(providerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(properties.getProviders()).thenReturn(Map.of());

        assertThatThrownBy(() -> registry.getChatClient("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown LLM provider");
    }

    private LlmProviderEntity createEntity(String id, String baseUrl, String model) {
        LlmProviderEntity entity = new LlmProviderEntity();
        entity.setId(id);
        entity.setBaseUrl(baseUrl);
        entity.setModel(model);
        entity.setEnabled(true);
        entity.setSupportsEmbedding(false);
        return entity;
    }
}
