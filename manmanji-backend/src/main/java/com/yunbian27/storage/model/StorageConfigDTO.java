package com.yunbian27.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageConfigDTO {

    private String endpoint;

    private String bucketName;

    private String accessKeyId;

    private String accessKeySecret;
}
