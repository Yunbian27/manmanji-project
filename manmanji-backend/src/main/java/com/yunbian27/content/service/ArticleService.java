package com.yunbian27.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import com.yunbian27.content.model.dto.MoveArticleDTO;
import com.yunbian27.content.model.vo.ArticleVO;
import com.yunbian27.ai.mapper.LlmGlobalSettingMapper;
import com.yunbian27.ai.model.LlmGlobalSettingEntity;
import com.yunbian27.ai.service.LlmProviderRegistry;
import com.yunbian27.content.model.dto.ArticleCreateDTO;
import com.yunbian27.content.model.entity.Article;
import com.yunbian27.content.model.entity.ArticleTag;
import com.yunbian27.content.mapper.ArticleMapper;
import com.yunbian27.content.mapper.ArticleTagMapper;
import com.yunbian27.common.Utils.SecurityUtils;
import com.yunbian27.content.model.vo.FolderTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final FolderService folderService;

    private final LlmProviderRegistry providerRegistry;
    private final LlmGlobalSettingMapper llmGlobalSettingMapper;

    private static final Long Temp_Folder = 10000L;

    @Transactional
    public Long createArticle(ArticleCreateDTO dto) {
        Long authorId = SecurityUtils.getCurrentUserId();

        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        article.setAuthorId(authorId);
        article.setSlug(generateSlug(dto.getTitle()));
        article.setSourceType("MANUAL");

        if ("PUBLISHED".equals(dto.getStatus())) {
            article.setPublishedAt(LocalDateTime.now());
        } else {
            article.setStatus("DRAFT");
        }

        // 如果文章folder_id为null,则固定放入未分类文件夹
        if (dto.getFolderId() == null) {
            article.setFolderId(Temp_Folder);
        }
        articleMapper.insert(article);

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            for (Long tagId : dto.getTagIds()) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(article.getId());
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }

        log.info("文章创建成功: id={}, title={}, status={}", article.getId(), article.getTitle(), article.getStatus());
        return article.getId();
    }

    /**
     * 生成文章 slug 用于用户导航 美化url地址
     * @param title
     * @return
     */
    private String generateSlug(String title) {
        String base = title.trim()
                .replaceAll("[^a-zA-Z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .toLowerCase();

        if (base.length() < 2) {
            base = "article";
        }

        if (base.length() > 60) {
            base = base.substring(0, 60);
        }

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String slug = base + "-" + suffix;

        while (articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getSlug, slug)) > 0) {
            suffix = UUID.randomUUID().toString().substring(0, 8);
            slug = base + "-" + suffix;
        }

        return slug;
    }

    /**
     * ai润色文章
     * @param article
     * @return
     */
    public String improve(String article) {
        ChatClient chatClient = providerRegistry.getChatClient(
                llmGlobalSettingMapper.selectById(LlmGlobalSettingEntity.SINGLETON_ID).getDefaultChatProviderId());
        return chatClient.prompt()
                .system("你是ai聊天助手")
                .user(article)
                .call()
                .content();
    }

    /**
     * 展示文章内容
     * @param articleId
     * @return
     */
    public ArticleVO showArticle(Long articleId) {
        ArticleVO articleVO = new ArticleVO();
        Article article = articleMapper.selectById(articleId);
        BeanUtils.copyProperties(article, articleVO);
        return articleVO;
    }

    public List<FolderTreeVO> move(MoveArticleDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (dto.getFolderId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (dto.getArticleId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        articleMapper.update(new LambdaUpdateWrapper<Article>()
                .set(Article::getFolderId, dto.getFolderId())
                .eq(Article::getId, dto.getArticleId()));
        return folderService.show();
    }
}
