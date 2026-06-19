package com.yunbian27.content.model.vo;
import com.yunbian27.content.constant.ArticleStatus;
import com.yunbian27.content.constant.ArticleVisibility;
import com.yunbian27.content.constant.ArticleType;
import com.yunbian27.content.constant.CreationStatement;
import com.yunbian27.content.constant.SourceType;

import lombok.Data;

import java.util.List;

@Data
public class ArticleEditorVO {

    private Long id;
    private String title;
    private String content;
    private ArticleStatus status;
    private String summary;
    private String coverUrl;
    private ArticleType articleType;
    private String sourceUrl;
    private ArticleVisibility visibility;
    private CreationStatement creationStatement;
    private List<Long> tagIds;
    private List<String> groupNames;
}
