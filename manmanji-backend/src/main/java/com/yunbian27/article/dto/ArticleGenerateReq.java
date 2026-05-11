package com.yunbian27.article.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ArticleGenerateReq {

    @NotBlank(message = "想法不能为空")
    private String idea;

    // 体裁 / 风格
    private String genre;
    // 目标读者
    private String audience;
    // 期望篇幅
    private String length;
    // 参考分类
    private Long categoryId;
    // 参考标签
    private List<Long> tagIds;
}
