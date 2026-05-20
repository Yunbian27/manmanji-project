package com.yunbian27.storage.model;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class StorageConfigDTO {

    private String endpoint;

    private String bucketName;

    private String accessKeyId;

    private String accessKeySecret;
}
