package com.yunbian27.storage.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import com.yunbian27.common.utils.SecurityUtils;
import com.yunbian27.storage.config.AliOssProperties;
import com.yunbian27.storage.model.StorageConfigDTO;
import com.yunbian27.storage.registry.StorageProviderRegistry;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 存储实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AliOssStorageProvider {

    private final StorageProviderRegistry registry;

    /**
     * 上传文件到 OSS
     *
     * @param file 上传的文件
     * @param dir  存储目录（如 "images"、"avatar"）
     * @return 文件访问 URL
     */
    public String upload(MultipartFile file, String dir) {
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String cleanDir = dir != null && dir.endsWith("/") ? dir.substring(0, dir.length() - 1) : dir;
        String objectName = cleanDir + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;

        StorageConfigDTO config = registry.loadConfig(SecurityUtils.getCurrentUserId());

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    config.getEndpoint(),
                    config.getAccessKeyId(),
                    config.getAccessKeySecret()
            );

            InputStream inputStream = file.getInputStream();
            ossClient.putObject(config.getBucketName(), objectName, inputStream);

            String url = "https://" + config.getBucketName() + "."
                    + config.getEndpoint().replace("https://", "")
                    + "/" + objectName;

            log.info("文件上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.PROVIDER_CONFIG_READ_FAILED);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 上传文件到 OSS（默认存储到 images 目录）
     */
    public String upload(MultipartFile file) {
        return upload(file, "images");
    }

    /**
     * 删除 OSS 文件
     */
    public void delete(String objectName) {
        StorageConfigDTO config = registry.loadConfig(SecurityUtils.getCurrentUserId());
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    config.getEndpoint(),
                    config.getAccessKeyId(),
                    config.getAccessKeySecret()
            );
            ossClient.deleteObject(config.getBucketName(), objectName);
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
