package com.yunbian27.content.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleManageVO {

    private Long id;
    private String title;
    private String summary;
    private String coverUrl;
    private String status;
    private String visibility;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer bookmarkCount;
    private String reviewReason;
    private LocalDateTime updatedAt;
}
