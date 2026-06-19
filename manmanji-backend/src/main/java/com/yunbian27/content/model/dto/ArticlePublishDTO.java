package com.yunbian27.content.model.dto;

import com.yunbian27.content.constant.ArticleType;
import com.yunbian27.content.constant.ArticleVisibility;
import com.yunbian27.content.constant.CreationStatement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ArticlePublishDTO {

    /** 文章ID（新建时为 null，更新时传入） */
    private Long id;

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
    /** 文章类型 */
    @NotNull(message = "文章类型不能为空")
    private ArticleType articleType;
    /** 转载来源链接 */
    private String sourceUrl;
    /** 可见范围 */
    @NotNull(message = "可见范围不能为空")
    private ArticleVisibility visibility;
    /** 创作声明 */
    private CreationStatement creationStatement;
}
