package com.yunbian27.content.model.vo;

import com.yunbian27.content.constant.ArticleStatus;
import com.yunbian27.content.constant.ArticleVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleTitlesVO {
    private Long id;
    private String title;
    private ArticleStatus status;
    private ArticleVisibility visibility;
    private List<Long> groupIds;
    private LocalDateTime updateTime;
}
