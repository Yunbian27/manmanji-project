package com.yunbian27.content.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class FolderTreeVO {
    private Long id;
    private String name;
    private List<FolderTreeVO> children;
    private List<ArticleBasic> articles;

    @Data
    @Builder
    public static class ArticleBasic {
        private Long id;
        private String title;
        private String status; //未发布或已发布
        private Long folderId;
    }
}


