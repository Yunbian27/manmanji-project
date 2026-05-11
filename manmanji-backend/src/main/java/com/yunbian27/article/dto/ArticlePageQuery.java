package com.yunbian27.article.dto;

import lombok.Data;

@Data
public class ArticlePageQuery {

    /** 当前页码(默认1) */
    private Integer page = 1;
    /** 每页条数(默认10) */
    private Integer size = 10;
    /** 分类ID筛选 */
    private Long categoryId;
    /** 标签ID筛选 */
    private Long tagId;
    /** 关键词搜索 */
    private String keyword;
    /** 状态筛选 */
    private String status;
    /** 来源类型筛选 */
    private String sourceType;
    /** 排序方式 */
    private String sort;
}
