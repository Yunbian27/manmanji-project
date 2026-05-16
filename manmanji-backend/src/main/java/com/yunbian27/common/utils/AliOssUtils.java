package com.yunbian27.common.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yunbian27.common.config.AliOssProperties;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 工具类
 */
@Data
@Slf4j
@Component
@RequiredArgsConstructor
public class AliOssUtils {

    private final AliOssProperties aliOssProperties;

    /**
     * 上传文件到 OSS
     *
     * @param file 上传的文件
     * @param dir  存储目录（如 "images"、"avatar"）
     * @return 文件访问 URL
     */
    public String upload(MultipartFile file, String dir) {
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成唯一文件名
        // 确保目录路径不以 / 结尾，避免双斜杠
        String cleanDir = dir != null && dir.endsWith("/") ? dir.substring(0, dir.length() - 1) : dir;
        String objectName = cleanDir + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    aliOssProperties.getEndpoint(),
                    aliOssProperties.getAccessKeyId(),
                    aliOssProperties.getAccessKeySecret()
            );

            InputStream inputStream = file.getInputStream();
            ossClient.putObject(aliOssProperties.getBucketName(), objectName, inputStream);

            // 拼接访问 URL
            String url = "https://" + aliOssProperties.getBucketName() + "."
                    + aliOssProperties.getEndpoint().replace("https://", "")
                    + "/" + objectName;

            log.info("文件上传成功: {}", url);
            return url;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 上传文件到 OSS（默认存储到 images 目录）
     *
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    public String upload(MultipartFile file) {
        return upload(file, "images");
    }

    /**
     * 删除 OSS 文件
     *
     * @param objectName 文件路径（如 "images/xxx.jpg"）
     */
    public void delete(String objectName) {
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    aliOssProperties.getEndpoint(),
                    aliOssProperties.getAccessKeyId(),
                    aliOssProperties.getAccessKeySecret()
            );
            ossClient.deleteObject(aliOssProperties.getBucketName(), objectName);
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