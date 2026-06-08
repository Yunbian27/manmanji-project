package com.yunbian27.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunbian27.common.constant.CommonConstants;
import com.yunbian27.common.constant.SystemConstants;
import com.yunbian27.common.utils.JsonUtils;
import com.yunbian27.common.constant.RedisKeys;
import com.yunbian27.common.constant.RedisTTL;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import com.yunbian27.content.mapper.ArticleGroupMapper;
import com.yunbian27.content.mapper.GroupMapper;
import com.yunbian27.content.model.dto.ArticleSaveDTO;
import com.yunbian27.content.model.entity.Article;
import com.yunbian27.content.model.entity.ArticleGroup;
import com.yunbian27.content.model.entity.Group;
import com.yunbian27.content.model.vo.ArticleTitlesVO;
import com.yunbian27.content.model.vo.ArticleVO;
import com.yunbian27.content.model.vo.GroupVO;
import com.yunbian27.ai.mapper.LlmGlobalSettingMapper;
import com.yunbian27.ai.model.LlmGlobalSettingEntity;
import com.yunbian27.ai.registry.LlmProviderRegistry;
import com.yunbian27.content.model.dto.ArticlePublishDTO;
import com.yunbian27.content.mapper.ArticleMapper;
import com.yunbian27.content.mapper.ArticleTagMapper;
import com.yunbian27.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleGroupMapper articleGroupMapper;
    private final GroupMapper groupMapper;

    private final LlmProviderRegistry providerRegistry;
    private final LlmGlobalSettingMapper llmGlobalSettingMapper;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 发布文章
     * @param dto
     */
    @Transactional
    public Long publish(ArticlePublishDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long articleId = dto.getId() != null && dto.getId() > 0 ? dto.getId() : null;
        Article article;

        if (articleId != null) {
            article = articleMapper.selectById(articleId);
            if (article == null || !userId.equals(article.getUserId()))
                throw new BusinessException(ErrorCode.NOT_FOUND);
            BeanUtils.copyProperties(dto, article);
            article.setId(articleId);
            articleMapper.updateById(article);
        } else {
            article = new Article();
            BeanUtils.copyProperties(dto, article);
            if (article.getTitle() == null || article.getTitle().isBlank())
                article.setTitle(SystemConstants.DEFAULT_ARTICLE_TITLE);
            article.setUserId(userId);
            article.setSlug(generateSlug(article.getTitle()));
            articleMapper.insert(article);
            articleId = article.getId();
        }

        article.setStatus(CommonConstants.Article.PUBLISHED);
        article.setPublishedAt(LocalDateTime.now());

        List<String> groupNames = dto.getGroupNames();
        if (groupNames != null && !groupNames.isEmpty()) {
            List<Long> groupIds = resolveGroupIds(userId, groupNames);
            replaceArticleGroups(articleId, groupIds);
        }

        log.info("文章发布成功: id={}, title={}", articleId, article.getTitle());
        return articleId;
    }

    private List<Long> resolveGroupIds(Long userId, List<String> names) {
        // 1、查已有
        List<Group> existing = groupMapper.selectList(
                new LambdaQueryWrapper<Group>()
                        .eq(Group::getUserId, userId)
                        .in(Group::getName, names)
        );

        // 2、插新的
        Set<String> existingNames = existing.stream()
                .map(Group::getName).collect(toSet());
        List<String> newNames = names.stream()
                .filter(n -> n != null && !n.isBlank() && !existingNames.contains(n)).toList();
        if (!newNames.isEmpty()) {
            groupMapper.insert(
                    newNames.stream().map(n -> {
                        Group g = new Group();
                        g.setUserId(userId);
                        g.setName(n);
                        return g;
                    }).toList()
            );
        }

        // 3、返回ids
        return groupMapper.selectList(
                new LambdaQueryWrapper<Group>()
                        .eq(Group::getUserId, userId)
                        .in(Group::getName, names)
        ).stream().map(Group::getId).toList();
    }

    private void replaceArticleGroups(Long articleId, List<Long> groupIds) {
        articleGroupMapper.delete(
                new LambdaQueryWrapper<ArticleGroup>()
                        .eq(ArticleGroup::getArticleId, articleId)
        );
        articleGroupMapper.insert(
                groupIds.stream().map(gid -> {
                    ArticleGroup ag = new ArticleGroup();
                    ag.setArticleId(articleId);
                    ag.setGroupId(gid);
                    return ag;
                }).toList()
        );
    }

    /*public void publish(ArticlePublishDTO dto) {
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
                        .set(Article::getArticleType, dto.getArticleType())
                        .set(Article::getSourceUrl, dto.getSourceUrl())
                        .set(Article::getVisibility, dto.getVisibility())
                        .set(Article::getCreationStatement, dto.getCreationStatement())
                        .set(Article::getStatus, CommonConstants.ArticleStatus.PUBLISHED)
                        .set(Article::getPublishedAt, LocalDateTime.now())
                        .set(Article::getUpdatedAt, LocalDateTime.now())
                        .eq(Article::getId, dto.getId())
                        .eq(Article::getUserId, userId));

        log.info("文章更新成功: id={}, title={}", dto.getId(), dto.getTitle());
    }*/

    /**
     * 保持草稿
     * @param dto
     */
    /*public void save(ArticleSaveDTO dto) {
       if (dto.getTitle() == null)
           throw new BusinessException(ErrorCode.BAD_REQUEST);
       if (dto.getContent() == null)
           throw new BusinessException(ErrorCode.BAD_REQUEST);

        Article article = new Article();
        BeanUtils.copyProperties(dto, article);

        article.setStatus(CommonConstants.Article.DRAFT);
        article.setVisibility();

        articleMapper.insert(article);
        log.info("文章保存成功: id={}, title={}", dto.getId(), dto.getTitle());
    }*/



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
                new LambdaQueryWrapper<Article>().eq(com.yunbian27.content.model.entity.Article::getSlug, slug)) > 0) {
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
        // 私密文章仅作者本人可读
        if (CommonConstants.Article.DRAFT.equals(article.getVisibility())) {
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

    /*public FolderTreeVO rename(Long id, String title) {
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
    }*/

    /*@Transactional
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
    }*/

    /**
     * 显示用户的文章列表
     * @return
     */
    public List<ArticleTitlesVO> showArticleTitles() {
        // 1、获取用户id
        Long userId = SecurityUtils.getCurrentUserId();

        return articleMapper.selectArticleTitlestByUserId(userId);
    }

    /**
     * 显示分组
     * @return
     */
    public List<GroupVO> showGroup() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Group> groups = groupMapper.selectList(
            new LambdaQueryWrapper<Group>()
                .eq(Group::getUserId, userId)
                .select(Group::getId, Group::getName));
        return groups.stream().map(g -> {
            GroupVO vo = new GroupVO();
            BeanUtils.copyProperties(g, vo);
            return vo;
        }).toList();
    }

    /**
     * 新建分组
     * @param name 分组名
     */
    @Transactional
    public Long createGroup(String name) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<String> existing = groupMapper.selectGroupNamesByUserId(userId);
        if (existing.contains(name)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        Group g = new Group();
        g.setUserId(userId);
        g.setName(name);
        groupMapper.insert(g);
        return g.getId();
    }

    /**
     * 删除分组（同时清理关联的文章分组关系）
     * @param
     */
    @Transactional
    public void deleteGroup(Long groupId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Group group = groupMapper.selectOne(
            new LambdaQueryWrapper<Group>()
                .eq(Group::getUserId, userId)
                .eq(Group::getId, groupId));
        if (group == null) return;
        articleGroupMapper.delete(
            new LambdaQueryWrapper<ArticleGroup>()
                .eq(ArticleGroup::getGroupId, groupId));
        groupMapper.deleteById(groupId);
    }
}
