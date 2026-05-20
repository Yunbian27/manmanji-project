package com.yunbian27.storage.service;

import com.yunbian27.common.constant.SystemConstants;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import com.yunbian27.common.service.ApiKeyEncryption;
import com.yunbian27.common.utils.SecurityUtils;
import com.yunbian27.storage.mapper.StorageConfigMapper;
import com.yunbian27.storage.model.StorageConfigDTO;
import com.yunbian27.storage.model.StorageConfigEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final AliOssStorageProvider aliOssStorageProvider;
    private final StorageConfigMapper storageConfigMapper;
    private final ApiKeyEncryption apiKeyEncryption;

    public void create(StorageConfigDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        Long userId = SecurityUtils.getCurrentUserId();
        ApiKeyEncryption.EncryptedValue encryptId = apiKeyEncryption.encrypt(dto.getAccessKeyId());
        ApiKeyEncryption.EncryptedValue encryptSecret = apiKeyEncryption.encrypt(dto.getAccessKeySecret());

        // 存入数据库
        storageConfigMapper.insert(
                StorageConfigEntity.builder()
                        .userId(userId)
                        .providerType("aliyun-oss")
                        .endpoint(dto.getEndpoint())
                        .bucketName(dto.getBucketName())
                        .accessKeyIdCiphertext(encryptId.ciphertext())
                        .accessKeyIdNonce(encryptId.nonce())
                        .accessKeySecretCiphertext(encryptSecret.ciphertext())
                        .accessKeySecretNonce(encryptSecret.nonce())
                        .enabled(Boolean.TRUE)
                        .build()
        );
        log.info("alioss创建成功: userId={}", userId);
    }

    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // TODO 校验图片大小

        String imageUrl = aliOssStorageProvider.upload(file, SystemConstants.DEFAULT_IMAGES_URL);

        return imageUrl;
    }
}
