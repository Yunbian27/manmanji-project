package com.yunbian27.article.dto;

import lombok.Data;

@Data
public class ArticlePageQuery {

    private Integer page = 1;
    private Integer size = 10;
    private Long categoryId;
    private Long tagId;
    private String keyword;
    private String status;
    private String sourceType;
    private String sort;
}
