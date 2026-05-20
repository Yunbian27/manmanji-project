package com.yunbian27.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yunbian27.common.constant.CommonConstants;
import com.yunbian27.common.constant.SystemConstants;
import com.yunbian27.common.utils.JsonUtils;
import com.yunbian27.common.constant.RedisKeys;
import com.yunbian27.common.constant.RedisTTL;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import com.yunbian27.content.model.dto.ArticleMoveDTO;
import com.yunbian27.content.model.dto.ArticleSaveDTO;
import com.yunbian27.content.model.vo.ArticleVO;
import com.yunbian27.ai.mapper.LlmGlobalSettingMapper;
import com.yunbian27.ai.model.LlmGlobalSettingEntity;
import com.yunbian27.ai.registry.LlmProviderRegistry;
import com.yunbian27.content.model.dto.ArticlePublishDTO;
import com.yunbian27.content.model.entity.Article;
import com.yunbian27.content.model.entity.ArticleTag;
import com.yunbian27.content.mapper.ArticleMapper;
import com.yunbian27.content.mapper.ArticleTagMapper;
import com.yunbian27.common.utils.SecurityUtils;
import com.yunbian27.content.model.vo.FolderTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final FolderService folderService;

    private final LlmProviderRegistry providerRegistry;
    private final LlmGlobalSettingMapper llmGlobalSettingMapper;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * @param id 文件夹id，可为null
     * @return 新文章id
     */
    public Long create(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(SystemConstants.DEFAULT_ARTICLE_TITLE);
        article.setSlug(generateSlug(SystemConstants.DEFAULT_ARTICLE_TITLE));
        article.setStatus(CommonConstants.ArticleStatus.UNPUBLISHED);
        article.setFolderId(id);
        articleMapper.insert(article);
        log.info("文章创建成功: id={}, title={}, status={}", article.getId(), article.getTitle(), article.getStatus());
        return article.getId();
    }

    /**
     * 文章保存
     * @param dto
     */
    public void save(ArticleSaveDTO dto) {
        // 删除redis中缓存的文章
        stringRedisTemplate.delete(RedisKeys.ARTICLE_CACHE_PREFIX + dto.getId());

        Long userId = SecurityUtils.getCurrentUserId();
        if (dto == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        articleMapper.update(null,
            new LambdaUpdateWrapper<Article>()
                .set(Article::getTitle, dto.getTitle())
                .set(Article::getContent, dto.getContent())
                .set(Article::getSlug, generateSlug(dto.getTitle()))
                .set(Article::getUpdatedAt, LocalDateTime.now())
                .eq(Article::getId, dto.getId())
                .eq(Article::getUserId, userId));

        log.info("文章保存成功: id={}, title={}", dto.getId(), dto.getTitle());
    }

    public void publish(ArticlePublishDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (dto == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        articleMapper.update(null,
            new LambdaUpdateWrapper<Article>()
                .set(Article::getTitle, dto.getTitle())
                .set(Article::getContent, dto.getContent())
                .set(Article::getSlug, generateSlug(dto.getTitle()))
                .set(Article::getSummary, dto.getSummary())
                .set(Article::getCoverUrl, dto.getCoverUrl())
                .set(Article::getCategoryId, dto.getCategoryId())
                .set(Article::getIsOriginal, dto.getIsOriginal())
                .set(Article::getSourceUrl, dto.getSourceUrl())
                .set(Article::getStatus, CommonConstants.ArticleStatus.PUBLISHED)
                .set(Article::getPublishedAt, LocalDateTime.now())
                .set(Article::getUpdatedAt, LocalDateTime.now())
                .eq(Article::getId, dto.getId())
                .eq(Article::getUserId, userId));

        log.info("文章发布成功: id={}, title={}", dto.getId(), dto.getTitle());
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
        // 1、查缓存
        String key = RedisKeys.ARTICLE_CACHE_PREFIX + articleId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            return JsonUtils.toBean(json, ArticleVO.class);
        }
        // 2、查库
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        // 未发布文章仅作者本人可读
        if (CommonConstants.ArticleStatus.UNPUBLISHED.equals(article.getStatus())) {
            Long userId = SecurityUtils.getCurrentUserId();
            if (!userId.equals(article.getUserId())) {
                throw new BusinessException(ErrorCode.NOT_FOUND);
            }
        }
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);
        // 3、写缓存
        stringRedisTemplate.opsForValue().set(key, JsonUtils.toJson(articleVO), RedisTTL.ARTICLE);
        return articleVO;
    }

    public FolderTreeVO move(ArticleMoveDTO dto) {
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

    public FolderTreeVO rename(Long id, String title) {
        if (id == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        Long userId = SecurityUtils.getCurrentUserId();
        Article article = articleMapper.selectById(id);
        if (article == null || !userId.equals(article.getUserId())) {
            throw new BusinessException("文章不存在");
        }
        String newSlug = generateSlug(title.trim());
        articleMapper.update(new LambdaUpdateWrapper<Article>()
                .set(Article::getTitle, title.trim())
                .set(Article::getSlug, newSlug)
                .eq(Article::getId, id));
        stringRedisTemplate.delete(RedisKeys.ARTICLE_CACHE_PREFIX + id);
        return folderService.show();
    }

    @Transactional
    public FolderTreeVO delete(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        Long userId = SecurityUtils.getCurrentUserId();
        Article article = articleMapper.selectById(id);
        if (article == null || !userId.equals(article.getUserId())) {
            throw new BusinessException("文章不存在");
        }
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, id));
        articleMapper.deleteById(id);
        stringRedisTemplate.delete(RedisKeys.ARTICLE_CACHE_PREFIX + id);
        return folderService.show();
    }


}
