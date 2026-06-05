package com.yunbian27.content.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class FolderTree {
    private Long id;
    private String name;
    private List<FolderTree> children;
    private List<ArticleBasic> articles;

    @Data
    @Builder
    public static class ArticleBasic {
        private Long id;
        private String title;
        private String status;
    }
}
