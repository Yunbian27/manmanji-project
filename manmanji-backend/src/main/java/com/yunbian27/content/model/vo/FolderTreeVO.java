package com.yunbian27.content.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class FolderTreeVO {
    private List<FolderTree> folders;
    private List<FolderTree.ArticleBasic> rootArticles;
}
