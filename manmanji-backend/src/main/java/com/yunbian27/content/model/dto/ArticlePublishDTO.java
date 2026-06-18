package com.yunbian27.content.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ArticlePublishDTO {

    /** 文章标题(5-100字) */
    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 100, message = "标题 5-100 字")
    private String title;

    /** Markdown正文 */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** AI摘要(可选) */
    @Size(max = 150, message = "摘要最多 150 字")
    private String summary;

    /** 封面图链接 */
    private String coverUrl;
    /** 标签ID列表 */
    private List<Long> tagIds;
    /** 分组名称列表 */
    private List<String> groupNames;
    /** 文章类型(ORIGINAL/REPOST) */
    private String articleType;
    /** 转载来源链接 */
    private String sourceUrl;
    /** 可见范围(PUBLIC/PRIVATE) */
    private String visibility;
    /** 创作声明(NONE/AI_ASSISTED/NETWORK_SOURCED/PERSONAL_OPINION) */
    private String creationStatement;
}
