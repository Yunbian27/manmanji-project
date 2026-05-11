package com.yunbian27.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ArticleCreateReq {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多 200 字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 500, message = "摘要最多 500 字")
    private String summary;

    private String coverUrl;
    private Long categoryId;
    private List<Long> tagIds;
    private Boolean isOriginal;
    private String sourceUrl;

    // 直接发布还是保存草稿
    private String status;
}
