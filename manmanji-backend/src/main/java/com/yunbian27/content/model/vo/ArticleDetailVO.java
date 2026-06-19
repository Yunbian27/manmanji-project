package com.yunbian27.content.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ArticleDetailVO {

    /** 文章ID */
    private Long id;
    /** 标题 */
    private String title;
    /** URL标识 */
    private String slug;
    /** 正文 */
    private String content;
    /** 摘要 */
    private String summary;
    /** 封面图 */
    private String coverUrl;
    /** 作者信息 */
    private AuthorInfo author;
    /** 标签列表 */
    private List<TagInfo> tags;
    /** 分组名称列表 */
    private List<String> groupNames;
    /** 状态 */
    private String status;
    /** 可见范围(PUBLIC/PRIVATE) */
    private String visibility;
    /** 文章类型(ORIGINAL/REPOST) */
    private String articleType;
    /** 创作声明(NONE/AI_ASSISTED/NETWORK_SOURCED/PERSONAL_OPINION) */
    private String creationStatement;
    /** 来源类型(MANUAL/AI_GENERATED) */
    private String sourceType;
    /** 转载来源链接 */
    private String sourceUrl;
    /** 阅读量 */
    private Integer viewCount;
    /** 点赞数 */
    private Integer likeCount;
    /** 评论数 */
    private Integer commentCount;
    /** 收藏数 */
    private Integer bookmarkCount;
    /** 当前用户是否已点赞 */
    private Boolean liked;
    /** 当前用户是否已收藏 */
    private Boolean bookmarked;
    /** 发布时间 */
    private LocalDateTime publishedTime;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

    @Data
    @Builder
    public static class AuthorInfo {
        /** 作者ID */
        private Long id;
        /** 作者用户名 */
        private String username;
        /** 作者昵称 */
        private String nickname;
        /** 作者头像URL */
        private String avatarUrl;
    }

    @Data
    @Builder
    public static class TagInfo {
        /** 标签ID */
        private Long id;
        /** 标签名称 */
        private String name;
        /** 标签URL标识 */
        private String slug;
        /** 标签颜色 */
        private String color;
    }
}
