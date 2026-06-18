package com.yunbian27.content.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ArticleSaveDTO {

    /** 文章ID（新建时为 null） */
    private Long id;

    /** 文章标题(最长100) */
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多 100 字")
    private String title;

    /** Markdown正文 */
    @NotBlank(message = "内容不能为空")
    private String content;
}

