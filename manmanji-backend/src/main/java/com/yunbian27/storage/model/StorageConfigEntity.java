package com.yunbian27.storage.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("storage_config")
public class StorageConfigEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private Long userId;

    private String providerType;

    private String endpoint;

    private String bucketName;

    private String accessKeyIdCiphertext;

    private String accessKeyIdNonce;

    private String accessKeySecretCiphertext;

    private String accessKeySecretNonce;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
