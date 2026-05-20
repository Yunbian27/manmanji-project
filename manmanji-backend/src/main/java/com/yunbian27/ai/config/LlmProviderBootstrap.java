package com.yunbian27.ai.config;

import com.yunbian27.ai.mapper.LlmGlobalSettingMapper;
import com.yunbian27.ai.mapper.LlmProviderMapper;
import com.yunbian27.ai.model.LlmGlobalSettingEntity;
import com.yunbian27.ai.model.LlmProviderEntity;
import com.yunbian27.common.service.ApiKeyEncryption;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmProviderBootstrap {

    private final LlmProviderMapper providerMapper;
    private final LlmGlobalSettingMapper globalSettingMapper;
    private final LlmProviderProperties properties;
    private final ApiKeyEncryption encryption;

    /**
     * 项目启动时，通过 yml 往表中插入 LLM 配置，并确保 LLM 的全局设置
     */
    @PostConstruct
    @Transactional
    public void seedProvidersIfNecessary() {
        List<LlmProviderEntity> providers = providerMapper.selectList(null);
        log.info("已存在的模型数量: {}", providers.size());
        if (providers == null || providers.isEmpty()) {
            seedProviders();
        }
        ensureLlmGlobalSetting();
    }

    /**
     * 将yml中配置的大模型数据批量插入到数据库中
     */
    private void seedProviders() {
        // 从yml配置中获取大模型数据
        Map<String, LlmProviderProperties.ProviderConfig> providers = properties.getProviders();
        if (providers.isEmpty() || providers.isEmpty()) {
            log.warn("yml中的大模型配置数据未找到");
            return;
        }

        // 将数据批量插入到数据库中
        providers.forEach((id, config) -> {
            //跳过不合法的
            if (isBlank(id) || config == null || isBlank(config.getBaseUrl()) || isBlank(config.getModel())) {
                log.warn("当前模型 {} 配置无效", id);
                return; //等价与continue
            }

            //加密apiKey
            ApiKeyEncryption.EncryptedValue encryptedValue = encryption.encrypt(config.getApiKey());
            log.info("seed provider: id={}, apiKeyLength={}, nonce={}, ciphertext={}",
                    id,
                    config.getApiKey() != null ? config.getApiKey().length() : 0,
                    encryptedValue.nonce(),
                    encryptedValue.ciphertext());

            //判断是否支持Embedding
            boolean isSupportsEmbedding = Boolean.TRUE.equals(config.getSupportsEmbedding()
            || !isBlank(config.getEmbeddingModel()));

            //插入数据库
            providerMapper.insert(LlmProviderEntity.builder()
                    .id(id)
                    .baseUrl(config.getBaseUrl())
                    .apiKeyCiphertext(encryptedValue.ciphertext())
                    .apiKeyNonce(encryptedValue.nonce())
                    .model(config.getModel())
                    .embeddingModel(config.getEmbeddingModel())
                    .embeddingDimensions(resolveEmbeddingDimensions(config.getEmbeddingDimensions()))
                    .supportsEmbedding(isSupportsEmbedding)
                    .temperature(config.getTemperature())
                    .enabled(true)
                    .builtin(true)
                    .build());
        });
    }

    /**
     * 确保大模型全局设置表中的数据正确性
     * 注意：全局设置表中两个字段实际上存储的是厂商id
     */
    private void ensureLlmGlobalSetting() {
        // 从表中获取数据，如果存在则直接返回
        LlmGlobalSettingEntity entity = globalSettingMapper.selectById(LlmGlobalSettingEntity.SINGLETON_ID);
        if (entity != null) {
            return;
        }
        // 获取默认的chat
        String defaultChatProvider = resolveExistingProvider(properties.getDefaultProvider(), "dashscope");
        // 获取默认的Embedding
        String defaultEmbeddingProvider = resolveExistingEmbeddingProvider(properties.getDefaultEmbeddingProvider()
        , defaultChatProvider);

        // 插入数据库
        globalSettingMapper.insert(LlmGlobalSettingEntity.builder()
                .id(LlmGlobalSettingEntity.SINGLETON_ID)
                .defaultChatProviderId(defaultChatProvider)
                .defaultEmbeddingProviderId(defaultEmbeddingProvider)
                .build());
        log.info("初始化 LLM 全局设置: chatProvider={}, embeddingProvider={}",
                defaultChatProvider, defaultEmbeddingProvider);
    }


    private String resolveExistingProvider(String preferredProvider, String fallbackProvider) {
        LlmProviderEntity llmProviderEntity = providerMapper.selectById(preferredProvider);
        if (preferredProvider != null && llmProviderEntity != null) {
            return preferredProvider;
        }
        return fallbackProvider;
    }

    private String resolveExistingEmbeddingProvider(String preferredEmbeddingProvider, String defaultChatProvider) {
        if (isBlank(preferredEmbeddingProvider)) {
            return defaultChatProvider;
        }

        // 先查首选
        LlmProviderEntity preferred = providerMapper.selectById(preferredEmbeddingProvider);
        if (preferred != null && canProvideEmbedding(preferred)) {
            return preferred.getId();
        }

        // 首选不行，遍历全表随便挑一个支持 Embedding 的
        List<LlmProviderEntity> all = providerMapper.selectList(null);
        for (LlmProviderEntity e : all) {
            if (canProvideEmbedding(e)) {
                return e.getId();
            }
        }

        // 实在没有，退回聊天的默认值（总比空着强）
        return defaultChatProvider;
    }

    private boolean canProvideEmbedding(LlmProviderEntity provider) {
        return provider.getEnabled() && provider.getSupportsEmbedding()
                || !isBlank(provider.getEmbeddingModel());
    }

    private Integer resolveEmbeddingDimensions(Integer configuredDimensions) {
        if (configuredDimensions != null && configuredDimensions > 0) {
            return configuredDimensions;
        }
        return properties.getEmbeddingDimensions();
    }

    private Boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
