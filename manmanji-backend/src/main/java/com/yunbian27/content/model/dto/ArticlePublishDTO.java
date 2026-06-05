package com.yunbian27.content.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ArticlePublishDTO {

    /** 文章ID */
    @NotNull(message = "文章ID不能为空")
    private Long id;

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
