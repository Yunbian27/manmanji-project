package com.yunbian27.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunbian27.common.constant.RedisTTL;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import com.yunbian27.common.result.PageDTO;
import com.yunbian27.common.utils.JsonUtils;
import com.yunbian27.common.utils.SecurityUtils;
import com.yunbian27.content.constant.ArticleStatus;
import com.yunbian27.content.constant.ArticleVisibility;
import com.yunbian27.content.constant.ContentConstants;
import com.yunbian27.content.constant.ContentRedisKeys;
import com.yunbian27.content.mapper.ArticleGroupMapper;
import com.yunbian27.content.mapper.GroupMapper;
import com.yunbian27.content.model.dto.ArticlePublishDTO;
import com.yunbian27.content.model.dto.ArticleSaveDTO;
import com.yunbian27.content.model.entity.Article;
import com.yunbian27.content.model.entity.ArticleGroup;
import com.yunbian27.content.model.entity.ArticleTag;
import com.yunbian27.content.model.entity.Group;
import com.yunbian27.content.model.entity.Tag;
import com.yunbian27.content.model.vo.ArticleEditorVO;
import com.yunbian27.content.model.vo.TagVO;
import com.yunbian27.content.model.vo.ArticleManageVO;
import com.yunbian27.content.model.vo.ArticleTitlesVO;
import com.yunbian27.content.model.vo.ArticleVO;
import com.yunbian27.content.model.vo.GroupVO;
import com.yunbian27.ai.mapper.LlmGlobalSettingMapper;
import com.yunbian27.ai.model.LlmGlobalSettingEntity;
import com.yunbian27.ai.registry.LlmProviderRegistry;
import com.yunbian27.content.mapper.ArticleMapper;
import com.yunbian27.content.mapper.ArticleTagMapper;
import com.yunbian27.content.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleGroupMapper articleGroupMapper;
    private final GroupMapper groupMapper;
    private final TagMapper tagMapper;

    private final LlmProviderRegistry providerRegistry;
    private final LlmGlobalSettingMapper llmGlobalSettingMapper;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 发布文章（新建或更新已有并提交审核）
     */
    @Transactional
    public Long publish(ArticlePublishDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        Article article;

        if (dto.getId() != null) {
            // 更新已有文章
            article = articleMapper.selectById(dto.getId());
            if (article == null || !userId.equals(article.getUserId())) {
                throw new BusinessException(ErrorCode.NOT_FOUND);
            }
            BeanUtils.copyProperties(dto, article);
            article.setId(dto.getId());
        } else {
            // 新建文章
            article = new Article();
            BeanUtils.copyProperties(dto, article);
            article.setUserId(userId);
            article.setSlug(generateSlug(article.getTitle()));
        }

        if (article.getTitle() == null || article.getTitle().isBlank())
            article.setTitle(ContentConstants.DEFAULT_ARTICLE_TITLE);
        article.setStatus(ArticleStatus.REVIEWING);
        article.setPublishTime(LocalDateTime.now());

        if (dto.getId() != null) {
            articleMapper.updateById(article);
        } else {
            articleMapper.insert(article);
        }

        Long articleId = article.getId();
        handleGroups(userId, articleId, dto.getGroupNames());
        handleTags(articleId, dto.getTagIds());

        log.info("文章发布成功: id={}, title={}", articleId, article.getTitle());
        // 删除缓存，确保下次读取获取最新数据
        stringRedisTemplate.delete(ContentRedisKeys.ARTICLE_CACHE_PREFIX + articleId);
        return articleId;
    }

    private void handleGroups(Long userId, Long articleId, List<String> groupNames) {
        if (groupNames != null && !groupNames.isEmpty()) {
            List<Long> groupIds = resolveGroupIds(userId, groupNames);
            replaceArticleGroups(articleId, groupIds);
        }
    }

    private List<Long> resolveGroupIds(Long userId, List<String> names) {
        // 1、查已有
        List<Group> existing = groupMapper.selectList(
                new LambdaQueryWrapper<Group>()
                        .eq(Group::getUserId, userId)
                        .in(Group::getName, names)
        );

        // 2、插新的（逐条插入，立即执行并回填 ID）
        Set<String> existingNames = existing.stream()
                .map(Group::getName).collect(toSet());
        List<Long> groupIds = new ArrayList<>(existing.stream().map(Group::getId).toList());
        for (String n : names) {
            if (n == null || n.isBlank() || existingNames.contains(n)) continue;
            if (n.length() > 50) throw new BusinessException(ErrorCode.BAD_REQUEST, "分组名最多50字");
            Group g = new Group();
            g.setUserId(userId);
            g.setName(n);
            groupMapper.insert(g);
            groupIds.add(g.getId());
        }

        return groupIds;
    }

    private void replaceArticleGroups(Long articleId, List<Long> groupIds) {
        articleGroupMapper.delete(
                new LambdaQueryWrapper<ArticleGroup>()
                        .eq(ArticleGroup::getArticleId, articleId)
        );
        if (!groupIds.isEmpty()) {
            articleGroupMapper.insert(
                    groupIds.stream().map(gid -> {
                        ArticleGroup ag = new ArticleGroup();
                        ag.setArticleId(articleId);
                        ag.setGroupId(gid);
                        return ag;
                    }).toList()
            );
        }
    }

    private void handleTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        articleTagMapper.delete(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId));
        articleTagMapper.insert(
                tagIds.stream().map(tid -> {
                    ArticleTag at = new ArticleTag();
                    at.setArticleId(articleId);
                    at.setTagId(tid);
                    return at;
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
                        .set(Article::getStatus, ArticleStatus.PUBLISHED)
                        .set(Article::getPublishedTime, LocalDateTime.now())
                        .set(Article::getUpdateTime, LocalDateTime.now())
                        .eq(Article::getId, dto.getId())
                        .eq(Article::getUserId, userId));

        log.info("文章更新成功: id={}, title={}", dto.getId(), dto.getTitle());
    }*/

    /**
     * 保存草稿（新建或更新已有草稿）
     */
    public Long save(ArticleSaveDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        Article article;

        if (dto.getId() != null) {
            article = articleMapper.selectById(dto.getId());
            if (article == null || !userId.equals(article.getUserId())) {
                throw new BusinessException(ErrorCode.NOT_FOUND);
            }
            if (!ArticleStatus.DRAFT.equals(article.getStatus())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "仅草稿状态可保存");
            }
            article.setTitle(dto.getTitle());
            article.setContent(dto.getContent());
            article.setSlug(generateSlug(dto.getTitle()));
            articleMapper.updateById(article);
        } else {
            article = new Article();
            BeanUtils.copyProperties(dto, article);
            article.setUserId(userId);
            article.setSlug(generateSlug(dto.getTitle()));
            article.setStatus(ArticleStatus.DRAFT);
            article.setVisibility(ArticleVisibility.PRIVATE);
            articleMapper.insert(article);
        }

        log.info("文章保存成功: id={}, title={}", article.getId(), dto.getTitle());
        // 删除缓存，确保下次读取获取最新数据
        stringRedisTemplate.delete(ContentRedisKeys.ARTICLE_CACHE_PREFIX + article.getId());
        return article.getId();
    }

    /**
     * 加载文章数据供编辑器回显（含标签和分组）
     */
    public ArticleEditorVO loadForEditor(Long articleId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Article article = articleMapper.selectById(articleId);
        if (article == null || !userId.equals(article.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        ArticleEditorVO vo = new ArticleEditorVO();
        BeanUtils.copyProperties(article, vo);

        // 标签 ID 列表
        List<ArticleTag> articleTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId));
        vo.setTagIds(articleTags.stream().map(ArticleTag::getTagId).toList());

        // 分组名称列表
        List<ArticleGroup> articleGroups = articleGroupMapper.selectList(
                new LambdaQueryWrapper<ArticleGroup>().eq(ArticleGroup::getArticleId, articleId));
        if (!articleGroups.isEmpty()) {
            List<Long> groupIds = articleGroups.stream().map(ArticleGroup::getGroupId).toList();
            List<Group> groups = groupMapper.selectBatchIds(groupIds);
            vo.setGroupNames(groups.stream().map(Group::getName).toList());
        } else {
            vo.setGroupNames(List.of());
        }

        return vo;
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
        String key = ContentRedisKeys.ARTICLE_CACHE_PREFIX + articleId;
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
        if (ArticleVisibility.PRIVATE.equals(article.getVisibility())) {
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
        stringRedisTemplate.delete(ContentRedisKeys.ARTICLE_CACHE_PREFIX + id);
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
        stringRedisTemplate.delete(ContentRedisKeys.ARTICLE_CACHE_PREFIX + id);
        return folderService.show();
    }*/

    /**
     * 显示用户的文章列表
     * @return
     */
    public List<ArticleTitlesVO> showArticleTitles() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Article> articles = articleMapper.selectList(
            new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getTitle, Article::getStatus,
                        Article::getVisibility, Article::getUpdateTime)
                .eq(Article::getUserId, userId)
                .orderByDesc(Article::getUpdateTime)
        );
        // 批量查分组ID
        List<Long> articleIds = articles.stream().map(Article::getId).toList();
        Map<Long, List<Long>> groupIdMap = articleIds.isEmpty() ? Map.of()
            : articleGroupMapper.selectList(
                new LambdaQueryWrapper<ArticleGroup>().in(ArticleGroup::getArticleId, articleIds))
                .stream()
                .collect(Collectors.groupingBy(
                    ArticleGroup::getArticleId,
                    Collectors.mapping(ArticleGroup::getGroupId, Collectors.toList())
                ));

        return articles.stream().map(a -> {
            ArticleTitlesVO vo = new ArticleTitlesVO();
            vo.setId(a.getId());
            vo.setTitle(a.getTitle());
            vo.setStatus(a.getStatus());
            vo.setVisibility(a.getVisibility());
            vo.setGroupIds(groupIdMap.getOrDefault(a.getId(), List.of()));
            vo.setUpdateTime(a.getUpdateTime());
            return vo;
        }).toList();
    }

    /**
     * 内容管理列表（含封面、摘要、阅读/点赞/评论/收藏统计，分页）
     */
    public PageDTO<ArticleManageVO> listManageArticles(int page, int size, String status) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Article> qw = new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getTitle, Article::getSummary,
                        Article::getCoverUrl, Article::getStatus, Article::getVisibility,
                        Article::getViewCount, Article::getLikeCount, Article::getCommentCount,
                        Article::getBookmarkCount, Article::getReviewReason, Article::getUpdateTime)
                .eq(Article::getUserId, userId)
                .orderByDesc(Article::getUpdateTime);
        if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            qw.eq(Article::getStatus, ArticleStatus.valueOf(status.toUpperCase()));
        }
        IPage<Article> result = articleMapper.selectPage(new Page<>(page, size), qw);
        List<ArticleManageVO> records = result.getRecords().stream().map(a -> ArticleManageVO.builder()
                .id(a.getId()).title(a.getTitle()).summary(a.getSummary())
                .coverUrl(a.getCoverUrl()).status(a.getStatus())
                .visibility(a.getVisibility())
                .viewCount(a.getViewCount()).likeCount(a.getLikeCount())
                .commentCount(a.getCommentCount()).bookmarkCount(a.getBookmarkCount())
                .reviewReason(a.getReviewReason()).updateTime(a.getUpdateTime())
                .build()).toList();
        return PageDTO.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    /**
     * 获取所有标签
     */
    public List<TagVO> listTags() {
        List<Tag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<Tag>().select(Tag::getId, Tag::getName));
        return tags.stream().map(t -> {
            TagVO vo = new TagVO();
            vo.setId(t.getId());
            vo.setName(t.getName());
            return vo;
        }).toList();
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
