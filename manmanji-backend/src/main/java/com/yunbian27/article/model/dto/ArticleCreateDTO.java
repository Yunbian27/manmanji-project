package com.yunbian27.article.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ArticleCreateDTO {

    /** 文章标题(最长200) */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多 200 字")
    private String title;

    /** Markdown正文 */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** AI摘要(可选) */
    @Size(max = 500, message = "摘要最多 500 字")
    private String summary;

    /** 封面图链接 */
    private String coverUrl;
    /** 分类ID */
    private Long categoryId;
    /** 标签ID列表 */
    private List<Long> tagIds;
    /** 是否原创 */
    private Boolean isOriginal;
    /** 转载来源链接 */
    private String sourceUrl;

    /** 状态(DRAFT/PUBLISHED) */
    private String status;
}

