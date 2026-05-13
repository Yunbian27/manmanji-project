package com.yunbian27.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunbian27.ai.config.LlmProviderProperties;
import com.yunbian27.ai.model.CreateProviderDTO;
import com.yunbian27.ai.model.LlmProviderVO;
import com.yunbian27.ai.model.LlmProviderEntity;
import com.yunbian27.ai.mapper.LlmProviderMapper;
import com.yunbian27.ai.utils.ApiKeyEncryption;
import com.yunbian27.ai.utils.LlmProviderRegistry;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final LlmProviderMapper llmProviderMapper;
    private final LlmProviderProperties providerProperties;
    private final ApiKeyEncryption encryption;
    private final LlmProviderRegistry registry;

    private static final Map<String, String> RECOMMENDED_EMBEDDING_MODELS = Map.of(
            "dashscope", "text-embedding-v3",
            "glm", "embedding-3",
            "zhipu", "embedding-3",
            "baidu", "Embedding-V1",
            "minimax", "embo-01"
    );


    public List<LlmProviderVO> listProviders() {
        List<LlmProviderEntity> entities = llmProviderMapper.selectList(
                new LambdaQueryWrapper<LlmProviderEntity>()
                        .orderByAsc(LlmProviderEntity::getId)
        );
        return entities.stream().map(this::toVO).toList();
    }

    public LlmProviderVO getProvider(String id) {
        LlmProviderEntity entity = llmProviderMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.PROVIDER_NOT_FOUND);
        }
        return toVO(entity);
    }

    private LlmProviderVO toVO(LlmProviderEntity entity) {
        LlmProviderVO vo = new LlmProviderVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setHasApiKey(entity.getApiKeyCiphertext() != null
                && !entity.getApiKeyCiphertext().isBlank());
        return vo;
    }

    public void createProvider(CreateProviderDTO dto) {
        //1、获取大模型名称
        String providerId = trimOrNull(dto.getId());
        //2、检测数据库中是否已存在
        LlmProviderEntity llmProviderEntity = llmProviderMapper.selectById(providerId);
        if (llmProviderEntity != null) {
            throw new BusinessException(ErrorCode.PROVIDER_ALREADY_EXISTS,
                    "Provider '" + dto.getId() + "' 已存在");
        }
        //去除空格，确保数据正常
        String baseUrl = requireNonBlank(dto.getBaseUrl(), "baseUrl");
        String model = requireNonBlank(dto.getModel(), "model");
        String apiKey = requireNonBlank(dto.getApiKey(), "apiKey");
        String embeddingModel = trimOrNull(dto.getEmbeddingModel());
        //如果dto中不合法，则从yml中获取
        Integer embeddingDimensions = resolveEmbeddingDimensions(dto.getEmbeddingDimensions());
        boolean supportsEmbedding = dto.getSupportsEmbedding() != null
                ? dto.getSupportsEmbedding()
                : embeddingModel != null;
        validateEmbeddingConfig(providerId, supportsEmbedding, embeddingModel, embeddingDimensions);
        //apiKey加密
        ApiKeyEncryption.EncryptedValue encrypted = encryption.encrypt(apiKey);
        // 存入数据库
        llmProviderMapper.insert(LlmProviderEntity.builder()
                .id(providerId)
                .baseUrl(baseUrl)
                .apiKeyNonce(encrypted.nonce())
                .apiKeyCiphertext(encrypted.ciphertext())
                .model(model)
                .embeddingModel(embeddingModel)
                .embeddingDimensions(embeddingDimensions)
                .supportsEmbedding(supportsEmbedding)
                .temperature(dto.getTemperature())
                .enabled(true)
                .builtin(false)
                .build());
        registry.reload();
        log.info("Created provider: id={}, baseUrl={}, model={}", providerId, baseUrl, model);
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String requireNonBlank(String value, String fieldName) {
        String normalized = trimOrNull(value);
        if (normalized == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 不能为空");
        }
        return normalized;
    }

    private Integer resolveEmbeddingDimensions(Integer configuredDimensions) {
        //如果dto中不合法，则从yml中获取
        if (configuredDimensions != null && configuredDimensions > 0) {
            return configuredDimensions;
        }
        return providerProperties.getEmbeddingDimensions();
    }

    private void validateEmbeddingConfig(
            String providerId,
            boolean supportsEmbedding,
            String embeddingModel,
            Integer embeddingDimensions) {
        String normalizedModel = trimOrNull(embeddingModel);
        if (!supportsEmbedding) {
            return;
        }
        if (normalizedModel == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "支持 Embedding 的 Provider 必须填写 embeddingModel");
        }
        if (looksLikeChatModel(normalizedModel)) {
            String recommendation = RECOMMENDED_EMBEDDING_MODELS.get(providerId.toLowerCase());
            String suffix = recommendation != null
                    ? "，推荐填写 " + recommendation
                    : "，请填写该厂商真实的 Embedding 模型名";
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "Embedding Model 不能填写聊天模型 '" + normalizedModel + "'" + suffix);
        }
        if (embeddingDimensions == null || embeddingDimensions <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "向量维度必须为正整数");
        }
    }

    private boolean looksLikeChatModel(String model) {
        String lower = model.toLowerCase();
        return lower.startsWith("glm-")
                || lower.startsWith("deepseek")
                || lower.startsWith("kimi")
                || lower.startsWith("moonshot")
                || lower.startsWith("qwen")
                || lower.startsWith("ernie");
    }
}
