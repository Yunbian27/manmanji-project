package com.yunbian27.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunbian27.article.mapper.ArticleMapper;
import com.yunbian27.article.mapper.FolderMapper;
import com.yunbian27.article.model.entity.Article;
import com.yunbian27.article.model.entity.Folder;
import com.yunbian27.common.Utils.SecurityUtils;
import com.yunbian27.user.model.vo.FolderTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final FolderMapper folderMapper;
    private final ArticleMapper articleMapper;

    public List<FolderTreeVO> showFolders() {
        // 从security中获取userId
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("准备获取用户文件夹，当前用户: {}", userId);

        // 获取所有文件夹和文章
        List<Folder> folders = folderMapper.selectList(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getUserId, userId));
        List<Article> articles = articleMapper.selectList(null);

        //构建文件树
        return buildFolderTree(folders, articles, null);
    }

    private List<FolderTreeVO> buildFolderTree(
            List<Folder> folders, List<Article> articles, Long parentId) {
        List<FolderTreeVO> result = new ArrayList<>();
        for (Folder folder: folders) {
            // 跳过不属于当前层级
            boolean match = (parentId == null && folder.getParentId() == null)
                    || (parentId != null && parentId.equals(folder.getParentId()));
            if (!match) continue;

            FolderTreeVO vo = new FolderTreeVO();
            vo.setId(folder.getId());
            vo.setName(folder.getName());

            // 递归，当前id就是子文件夹的父节点
            vo.setChildren(buildFolderTree(folders, articles, folder.getId()));

            // 插入文章
            List<FolderTreeVO.ArticleItem> itemList = articles.stream()
                    // 1. 过滤指定文件夹的文章
                    .filter(article -> folder.getId().equals(article.getFolderId()))
                    // 2. 转换：Article → ArticleItem
                    .map(article ->
                        FolderTreeVO.ArticleItem.builder()
                                .id(article.getId())
                                .title(article.getTitle())
                                .status(article.getStatus())
                                .build()
                    )
                    // 3. 收集成 List
                    .collect(Collectors.toList());
            vo.setArticles(itemList);
            result.add(vo);
        }
        return result;
    }
}
