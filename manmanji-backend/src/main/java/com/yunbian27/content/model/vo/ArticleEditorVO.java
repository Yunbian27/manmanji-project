package com.yunbian27.content.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ArticleEditorVO {

    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverUrl;
    private String articleType;
    private String sourceUrl;
    private String visibility;
    private String creationStatement;
    private List<Long> tagIds;
    private List<String> groupNames;
}
