package com.yunbian27.content.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.yunbian27.content.constant.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("articles")
public class Article {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
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
    /** 状态 */
    private ArticleStatus status;
    /** 可见范围 */
    private ArticleVisibility visibility;
    /** 文章类型 */
    private ArticleType articleType;
    /** 创作声明 */
    private CreationStatement creationStatement;
    /** 来源类型 */
    private SourceType sourceType;
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 审核意见/驳回理由 */
    private String reviewReason;
}
