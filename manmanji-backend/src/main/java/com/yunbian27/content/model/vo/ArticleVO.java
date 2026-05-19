package com.yunbian27.content.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleVO {

    /** 主键ID */
    private Long id;
    /** 标题 */
    private String title;
    /** URL标识 */
    private String slug;
    /** Markdown正文 */
    private String content;
    /** AI摘要 */
    private String summary;
    /** 封面图 */
    private String coverUrl;
    /** 作者用户ID */
    private Long userId;
    /** 个人文件夹ID */
    private Long folderId;
    /** 分类ID */
    private Long categoryId;
    /** 状态(UNPUBLISHED/PUBLISHED) */
    private String status;
    /** 来源(MANUAL/AI_GENERATED) */
    private String sourceType;
    /** AI生成原始输入 */
    private String sourcePrompt;
    /** 阅读量(冗余计数) */
    private Integer viewCount;
    /** 点赞数(冗余计数) */
    private Integer likeCount;
    /** 评论数(冗余计数) */
    private Integer commentCount;
    /** 收藏数(冗余计数) */
    private Integer bookmarkCount;
    /** 是否原创 */
    private Boolean isOriginal;
    /** 转载来源链接 */
    private String sourceUrl;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 发布时间 */
    private LocalDateTime publishedAt;
}
