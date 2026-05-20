package com.yunbian27.storage.registry;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunbian27.common.service.ApiKeyEncryption;
import com.yunbian27.storage.config.AliOssProperties;
import com.yunbian27.storage.mapper.StorageConfigMapper;
import com.yunbian27.storage.model.StorageConfigDTO;
import com.yunbian27.storage.model.StorageConfigEntity;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 存储提供商注册表
 * 解析策略：数据库 → YAML 降级
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorageProviderRegistry {

    private final StorageConfigMapper storageConfigMapper;
    private final AliOssProperties aliOssProperties;
    private final ApiKeyEncryption encryption;

    /**
     * 加载存储配置：先查 DB，不存在则降级到 YAML
     */
    public StorageConfigDTO loadConfig(Long userId) {
        StorageConfigEntity entity = storageConfigMapper.selectOne(
                new LambdaQueryWrapper<StorageConfigEntity>()
                        .eq(StorageConfigEntity::getUserId, userId)
                        .eq(StorageConfigEntity::getEnabled, true)
        );

        if (entity == null) {
            log.info("未找到用户 {} 的存储配置，使用 YAML 默认配置", userId);
            return buildFromProperties();
        }


        // 解密
        String accessKeyId = encryption.decrypt(entity.getAccessKeyIdNonce(), entity.getAccessKeyIdCiphertext());
        String accessKeySecret = encryption.decrypt(entity.getAccessKeySecretNonce(),entity.getAccessKeySecretCiphertext());

        return StorageConfigDTO.builder()
                .endpoint(entity.getEndpoint())
                .bucketName(entity.getBucketName())
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .build();
    }

    private StorageConfigDTO buildFromProperties() {
        return StorageConfigDTO.builder()
                .endpoint(aliOssProperties.getEndpoint())
                .bucketName(aliOssProperties.getBucketName())
                .accessKeyId(aliOssProperties.getAccessKeyId())
                .accessKeySecret(aliOssProperties.getAccessKeySecret())
                .build();
    }
}
