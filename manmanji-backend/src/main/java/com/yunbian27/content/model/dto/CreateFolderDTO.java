package com.yunbian27.content.model.dto;

import lombok.Data;

@Data
public class CreateFolderDTO {
    private String folderName;
    private Long parentId;
}
