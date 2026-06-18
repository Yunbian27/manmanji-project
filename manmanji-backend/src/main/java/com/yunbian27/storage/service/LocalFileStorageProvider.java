package com.yunbian27.storage.service;

import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageProvider implements StorageProvider {

    private final String rootPath;
    private final String urlPrefix;

    public LocalFileStorageProvider(
            @Value("${storage.local.path:/data/images}") String rootPath,
            @Value("${storage.local.url-prefix:/images}") String urlPrefix) {
        this.rootPath = rootPath.endsWith("/") ? rootPath.substring(0, rootPath.length() - 1) : rootPath;
        this.urlPrefix = urlPrefix;
    }

    @Override
    public String upload(MultipartFile file, String dir) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString().replace("-", "") + suffix;

        File targetDir = new File(rootPath, dir);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new BusinessException(ErrorCode.PROVIDER_CONFIG_WRITE_FAILED);
        }

        try {
            file.transferTo(new File(targetDir, filename));
        } catch (Exception e) {
            log.error("本地文件写入失败", e);
            throw new BusinessException(ErrorCode.PROVIDER_CONFIG_WRITE_FAILED);
        }

        String path = urlPrefix + "/" + filename;
        log.info("本地文件上传成功: {}", path);
        return path;
    }

    @Override
    public void delete(String path) {
        String relativePath = path.startsWith(urlPrefix) ? path.substring(urlPrefix.length()) : path;
        File file = new File(rootPath, relativePath);
        if (file.exists()) {
            file.delete();
            log.info("本地文件删除成功: {}", path);
        }
    }
}
