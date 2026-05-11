package com.yunbian27.article.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ArticleDetailResp {

    private Long id;
    private String title;
    private String slug;
    private String content;
    private String summary;
    private String coverUrl;
    private AuthorInfo author;
    private CategoryInfo category;
    private List<TagInfo> tags;
    private String status;
    private String sourceType;
    private String sourcePrompt;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer bookmarkCount;
    private Boolean isOriginal;
    private String sourceUrl;
    private Boolean liked;
    private Boolean bookmarked;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatarUrl;
    }

    @Data
    @Builder
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String slug;
    }

    @Data
    @Builder
    public static class TagInfo {
        private Long id;
        private String name;
        private String slug;
        private String color;
    }
}
