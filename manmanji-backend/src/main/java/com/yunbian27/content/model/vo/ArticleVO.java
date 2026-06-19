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
    /** 状态(UNPUBLISHED/PUBLISHED) */
    private String status;
    /** 可见范围(PUBLIC/PRIVATE) */
    private String visibility;
    /** 文章类型(ORIGINAL/REPOST) */
    private String articleType;
    /** 创作声明(NONE/AI_ASSISTED/NETWORK_SOURCED/PERSONAL_OPINION) */
    private String creationStatement;
    /** 来源(MANUAL/AI_GENERATED) */
    private String sourceType;
    /** 转载来源链接 */
    private String sourceUrl;
    /** 阅读量(冗余计数) */
    private Integer viewCount;
    /** 点赞数(冗余计数) */
    private Integer likeCount;
    /** 评论数(冗余计数) */
    private Integer commentCount;
    /** 收藏数(冗余计数) */
    private Integer bookmarkCount;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 发布时间 */
    private LocalDateTime publishedTime;
}
