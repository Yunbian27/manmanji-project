package com.yunbian27.ai.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunbian27.ai.config.LlmProviderProperties;
import com.yunbian27.ai.model.LlmProviderEntity;
import com.yunbian27.ai.mapper.LlmProviderMapper;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态路由选择大语言模型
 * 采用数据库 + YML  配置文件进行动态配置
 */
@Slf4j
@Component
public class LlmProviderRegistry {

    // 线程安全的缓存Map：key是唯一标识，value是ChatClient客户端
    private final Map<String, ChatClient> clientCache = new ConcurrentHashMap<>();
    private final Map<String, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();

    private final LlmProviderProperties properties;
    private final ApiKeyEncryption encryption;
    private final LlmProviderMapper providerMapper;

    private final ToolCallingManager toolCallingManager;
    private final ObservationRegistry observationRegistry;

    @Autowired
    public LlmProviderRegistry(
            LlmProviderProperties properties,
            LlmProviderMapper providerMapper,
            ApiKeyEncryption encryption,
            @Autowired(required = false) ToolCallingManager toolCallingManager,
            @Autowired(required = false) ObservationRegistry observationRegistry) {
        this.properties = properties;
        this.providerMapper = providerMapper;
        this.encryption = encryption;
        this.toolCallingManager = toolCallingManager;
        this.observationRegistry = observationRegistry;
    }

    public ChatClient getChatClient(String providerId) {
        return clientCache.computeIfAbsent(providerId, id -> {
            log.info("[LlmProviderRegistry] 正在为指定的模型服务商创建新的客户端: {}", id);
            return createChatClient(id);
        });
    }

    public void reload() {
        int size = clientCache.size() + embeddingModelCache.size();
        log.info("[LlmProviderRegistry] 正在重新加载模型服务商配置，当前缓存大小为: {}", size);
        clientCache.clear();
        embeddingModelCache.clear();
    }

    /**
     * 创建 ChatClient
     * @param providerId
     * @return
     */
    private ChatClient createChatClient(String providerId) {
        //根据 OpenAiChatModel 创建 ChatClient
        OpenAiChatModel chatModel = buildChatModel(providerId);
        ChatClient.Builder builder = ChatClient.builder(chatModel);
        //最终要返回ChatClient
        return builder.build();
    }

    /**
     * 创建 OpenAiChatModel
     * @param providerId
     * @return
     */
    private OpenAiChatModel buildChatModel(String providerId) {
        //1、通过 loadProviderOrThrow 方法获取指定的模型服务商
        ProviderSnapshot config = loadProviderOrThrow(providerId);
        log.info("[LlmProviderRegistry] 创建 ChatModel - Provider: {}, BaseUrl: {}, Model: {}",
                providerId, config.baseUrl(), config.model());

        OpenAiApi openAiApi = ApiPathResolver.buildOpenAiApi(config.baseUrl(), config.apiKey());

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(config.model())
                .temperature(config.temperature() != null ? config.temperature() : 0.2)
                .build();

        return new OpenAiChatModel(
                openAiApi,
                options,
                toolCallingManager,
                RetryUtils.DEFAULT_RETRY_TEMPLATE,
                observationRegistry != null ? observationRegistry : ObservationRegistry.NOOP
        );
    }

    private ProviderSnapshot loadProviderOrThrow(String providerId) {
        //1、先查询数据库，若数据库为空则
        LlmProviderEntity entity = providerMapper.selectOne(
                new LambdaQueryWrapper<LlmProviderEntity>()
                        .eq(LlmProviderEntity::getId, providerId)
                        .eq(LlmProviderEntity::getEnabled, true)
        );

        if (entity == null) {
            //2、采用降级策略，从yml中获取配置
            return loadProviderFromPropertiesOrThrow(providerId);
        }

        //3、封装ProviderSnapshot返回，为chatmodel的配置
        return new ProviderSnapshot(
                entity.getId(),
                entity.getBaseUrl(),
                encryption.decrypt(entity.getApiKeyNonce(), entity.getApiKeyCiphertext()),
                entity.getModel(),
                entity.getEmbeddingModel(),
                entity.getEmbeddingDimensions(),
                entity.getSupportsEmbedding(),
                entity.getTemperature()
        );
    }

    private ProviderSnapshot loadProviderFromPropertiesOrThrow(String providerId) {
        LlmProviderProperties.ProviderConfig config = properties.getProviders().get(providerId);
        if (config == null) {
            log.error("[LlmProviderRegistry] Provider config not found: {}", providerId);
            throw new IllegalArgumentException("Unknown LLM provider: " + providerId);
        }
        boolean supportsEmbedding = Boolean.TRUE.equals(config.getSupportsEmbedding())
                || !isBlank(config.getEmbeddingModel());
        return new ProviderSnapshot(
                providerId,
                config.getBaseUrl(),
                config.getApiKey(),
                config.getModel(),
                config.getEmbeddingModel(),
                config.getEmbeddingDimensions(),
                supportsEmbedding,
                config.getTemperature()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // java16 写法 等同于创建了一个内部类 并带有get和set方法
    private record ProviderSnapshot(
            String id,
            String baseUrl,
            String apiKey,
            String model,
            String embeddingModel,
            Integer embeddingDimensions,
            boolean supportsEmbedding,
            Double temperature
    ) {
    }

}
