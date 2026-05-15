package com.yunbian27.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;

    private final LlmProviderRegistry providerRegistry;
    private final LlmGlobalSettingMapper llmGlobalSettingMapper;

    @Transactional
    public Long createArticle(ArticleCreateDTO req) {
        Long authorId = SecurityUtils.getCurrentUserId();

        Article article = new Article();
        BeanUtils.copyProperties(req, article);
        article.setAuthorId(authorId);
        article.setSlug(generateSlug(req.getTitle()));
        article.setSourceType("MANUAL");

        if ("PUBLISHED".equals(req.getStatus())) {
            article.setPublishedAt(LocalDateTime.now());
        } else {
            article.setStatus("DRAFT");
        }

        articleMapper.insert(article);

        if (req.getTagIds() != null && !req.getTagIds().isEmpty()) {
            for (Long tagId : req.getTagIds()) {
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
}
