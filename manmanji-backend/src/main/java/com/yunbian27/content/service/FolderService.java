package com.yunbian27.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yunbian27.common.Utils.SecurityUtils;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import com.yunbian27.content.mapper.ArticleMapper;
import com.yunbian27.content.mapper.FolderMapper;
import com.yunbian27.content.model.dto.CreateFolderDTO;
import com.yunbian27.content.model.dto.MoveFolderDTO;
import com.yunbian27.content.model.entity.Article;
import com.yunbian27.content.model.entity.Folder;
import com.yunbian27.content.model.vo.FolderTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
     * @return
     */
    public List<FolderTreeVO> show() {
        //TODO 需要做redis缓存

        // 从security中获取userId
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("准备获取用户文件夹，当前用户: {}", userId);

        // 获取所有文件夹和文章
        List<Folder> folders = folderMapper.selectList(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getUserId, userId));
        List<FolderTreeVO.ArticleBasic> articleBasics = articleMapper.getArticleBasics(userId);

        //构建文件树
        return buildFolderTree(folders, articleBasics, null);
    }

    private List<FolderTreeVO> buildFolderTree(
            List<Folder> folders, List<FolderTreeVO.ArticleBasic> articleBasics, Long parentId) {
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
            vo.setChildren(buildFolderTree(folders, articleBasics, folder.getId()));

            // 插入文章
            List<FolderTreeVO.ArticleBasic> itemList = articleBasics.stream()
                    // 1. 过滤指定文件夹的文章
                    .filter(articleBasic -> folder.getId().equals(articleBasic.getFolderId()))
                    // 3. 收集成 List
                    .collect(Collectors.toList());
            vo.setArticles(itemList);
            result.add(vo);
        }
        return result;
    }

    public List<FolderTreeVO> move(MoveFolderDTO dto) {
        if (dto == null || dto.getFolderId() == null) {
            throw new BusinessException("缺少文件夹ID");
        }
        if (dto.getFolderId().equals(dto.getParentId())) {
            throw new BusinessException("无法移动至自身");
        }
        // 循环引用校验：检查目标文件夹是否为自己的子孙
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

    public List<FolderTreeVO> create(CreateFolderDTO dto) {
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
}
