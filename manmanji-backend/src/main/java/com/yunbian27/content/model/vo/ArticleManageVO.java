package com.yunbian27.content.model.vo;
import com.yunbian27.content.constant.ArticleStatus;
import com.yunbian27.content.constant.ArticleVisibility;
import com.yunbian27.content.constant.ArticleType;
import com.yunbian27.content.constant.CreationStatement;
import com.yunbian27.content.constant.SourceType;

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
    private ArticleStatus status;
    private ArticleVisibility visibility;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer bookmarkCount;
    private String reviewReason;
    private LocalDateTime updateTime;
}
