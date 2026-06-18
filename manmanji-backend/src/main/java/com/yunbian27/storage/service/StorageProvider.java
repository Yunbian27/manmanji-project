package com.yunbian27.storage.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageProvider {

    /** 上传文件到指定目录，返回访问路径 */
    String upload(MultipartFile file, String dir);

    /** 删除文件 */
    void delete(String path);
}
