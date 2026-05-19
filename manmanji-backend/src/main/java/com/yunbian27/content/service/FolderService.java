package com.yunbian27.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yunbian27.common.utils.SecurityUtils;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.content.mapper.ArticleMapper;
import com.yunbian27.content.mapper.FolderMapper;
import com.yunbian27.content.model.dto.FolderCreateDTO;
import com.yunbian27.content.model.dto.FolderMoveDTO;
import com.yunbian27.content.model.entity.Article;
import com.yunbian27.content.model.entity.Folder;
import com.yunbian27.content.model.vo.FolderTree;
import com.yunbian27.content.model.vo.FolderTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderService {

    private final ArticleMapper articleMapper;
    private final FolderMapper folderMapper;

    /**
     * 展示文件夹树
     */
    public FolderTreeVO show() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("准备获取用户文件夹，当前用户: {}", userId);

        List<Folder> folders = folderMapper.selectList(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getUserId, userId));
        List<FolderTree.ArticleBasic> articleBasics = articleMapper.getArticleBasics(userId);

        List<FolderTree> folderTree = buildFolderTree(folders, articleBasics, null);

        List<FolderTree.ArticleBasic> rootArticles = articleBasics.stream()
                .filter(a -> a.getFolderId() == null)
                .collect(Collectors.toList());

        FolderTreeVO result = new FolderTreeVO();
        result.setFolders(folderTree);
        result.setRootArticles(rootArticles);
        return result;
    }

    private List<FolderTree> buildFolderTree(
            List<Folder> folders, List<FolderTree.ArticleBasic> articleBasics, Long parentId) {
        List<FolderTree> result = new ArrayList<>();

        for (Folder folder : folders) {
            boolean match = (parentId == null && folder.getParentId() == null)
                    || (parentId != null && parentId.equals(folder.getParentId()));
            if (!match) continue;

            FolderTree vo = new FolderTree();
            vo.setId(folder.getId());
            vo.setName(folder.getName());

            vo.setChildren(buildFolderTree(folders, articleBasics, folder.getId()));

            List<FolderTree.ArticleBasic> itemList = articleBasics.stream()
                    .filter(a -> folder.getId().equals(a.getFolderId()))
                    .collect(Collectors.toList());
            vo.setArticles(itemList);
            result.add(vo);
        }
        return result;
    }

    public FolderTreeVO move(FolderMoveDTO dto) {
        if (dto == null || dto.getFolderId() == null) {
            throw new BusinessException("缺少文件夹ID");
        }
        if (dto.getFolderId().equals(dto.getParentId())) {
            throw new BusinessException("无法移动至自身");
        }
        if (dto.getParentId() != null) {
            Long userId = SecurityUtils.getCurrentUserId();
            List<Folder> folders = folderMapper.selectList(new LambdaQueryWrapper<Folder>()
                    .eq(Folder::getUserId, userId));
            if (isDescendantOf(dto.getParentId(), dto.getFolderId(), folders)) {
                throw new BusinessException("无法将文件夹移动至其子目录下");
            }
        }
        folderMapper.update(new LambdaUpdateWrapper<Folder>()
                .set(Folder::getParentId, dto.getParentId())
                .eq(Folder::getId, dto.getFolderId()));
        return show();
    }

    private boolean isDescendantOf(Long targetId, Long folderId, List<Folder> folders) {
        for (Folder f : folders) {
            if (folderId.equals(f.getParentId())) {
                if (f.getId().equals(targetId)) return true;
                if (isDescendantOf(targetId, f.getId(), folders)) return true;
            }
        }
        return false;
    }

    public FolderTreeVO create(FolderCreateDTO dto) {
        if (dto == null) {
            throw new BusinessException("参数不能为空");
        }
        if (dto.getFolderName() == null || dto.getFolderName().trim().isEmpty()) {
            throw new BusinessException("文件夹名称不能为空");
        }
        folderMapper.insert(Folder.builder()
                .userId(SecurityUtils.getCurrentUserId())
                .parentId(dto.getParentId())
                .name(dto.getFolderName())
                .build());
        return show();
    }

    public FolderTreeVO rename(Long id, String name) {
        if (id == null) {
            throw new BusinessException("缺少文件夹ID");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException("文件夹名称不能为空");
        }
        Long userId = SecurityUtils.getCurrentUserId();
        Folder folder = folderMapper.selectById(id);
        if (folder == null || !userId.equals(folder.getUserId())) {
            throw new BusinessException("文件夹不存在");
        }
        folderMapper.update(new LambdaUpdateWrapper<Folder>()
                .set(Folder::getName, name.trim())
                .eq(Folder::getId, id));
        return show();
    }

    @Transactional
    public FolderTreeVO delete(Long id) {
        if (id == null) {
            throw new BusinessException("缺少文件夹ID");
        }
        if (id == 10000L) {
            throw new BusinessException("无法删除未分类文件夹");
        }
        Long userId = SecurityUtils.getCurrentUserId();
        Folder folder = folderMapper.selectById(id);
        if (folder == null || !userId.equals(folder.getUserId())) {
            throw new BusinessException("文件夹不存在");
        }

        List<Folder> allFolders = folderMapper.selectList(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getUserId, userId));
        List<Long> idsToDelete = new ArrayList<>();
        idsToDelete.add(id);
        collectDescendantIds(id, allFolders, idsToDelete);

        articleMapper.delete(new LambdaQueryWrapper<Article>()
                .in(Article::getFolderId, idsToDelete));

        folderMapper.deleteBatchIds(idsToDelete);
        return show();
    }

    private void collectDescendantIds(Long parentId, List<Folder> allFolders, List<Long> result) {
        for (Folder f : allFolders) {
            if (parentId.equals(f.getParentId())) {
                result.add(f.getId());
                collectDescendantIds(f.getId(), allFolders, result);
            }
        }
    }
}
