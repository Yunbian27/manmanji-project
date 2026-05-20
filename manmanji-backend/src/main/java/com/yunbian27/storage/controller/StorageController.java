package com.yunbian27.storage.controller;

import com.yunbian27.storage.model.StorageConfigDTO;
import com.yunbian27.storage.model.StorageConfigEntity;
import com.yunbian27.common.result.Result;
import com.yunbian27.storage.service.StorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Tag(name = "存储配置模块")
public class StorageController {

    private final StorageService storageService;

    /**
     * 创建/更新存储配置
     */
    @PostMapping("/create")
    public Result<Void> create(@RequestBody StorageConfigDTO dto) {
        storageService.create(dto);
        return Result.success();
    }

    /**
     * 图片上传
     */
    @PostMapping("/image")
    public Result<String> image(@RequestParam("file") MultipartFile file) {
        return Result.success(storageService.uploadImage(file));
    }
}
