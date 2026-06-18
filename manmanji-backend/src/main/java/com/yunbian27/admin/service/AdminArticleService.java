package com.yunbian27.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunbian27.admin.constant.ReviewAction;
import com.yunbian27.admin.model.dto.ReviewDTO;
import com.yunbian27.admin.model.vo.ReviewListVO;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.result.PageDTO;
import com.yunbian27.common.exception.ErrorCode;
import com.yunbian27.content.mapper.ArticleMapper;
import com.yunbian27.content.constant.ArticleStatus;
import com.yunbian27.content.model.entity.Article;
import com.yunbian27.user.mapper.UserMapper;
import com.yunbian27.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminArticleService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    /**
     * 分页查询审核列表
     */
    public PageDTO<ReviewListVO> list(String status, int page, int size) {
        LambdaQueryWrapper<Article> qw = new LambdaQueryWrapper<Article>()
                .select(Article::getId, Article::getTitle, Article::getSummary,
                        Article::getCoverUrl, Article::getUserId,
                        Article::getStatus, Article::getReviewReason,
                        Article::getUpdatedAt)
                .in(Article::getStatus, ArticleStatus.REVIEWING, ArticleStatus.PUBLISHED, ArticleStatus.REJECTED)
                .orderByDesc(Article::getUpdatedAt);

        if (status != null && !status.isEmpty()) {
            qw.eq(Article::getStatus, ArticleStatus.valueOf(status.toUpperCase()));
        }

        IPage<Article> result = articleMapper.selectPage(new Page<>(page, size), qw);
        Set<Long> userIds = result.getRecords().stream()
                .map(Article::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> userNames = userIds.isEmpty()
                ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));

        List<ReviewListVO> records = result.getRecords().stream().map(a -> ReviewListVO.builder()
                .id(a.getId())
                .title(a.getTitle())
                .summary(a.getSummary())
                .coverUrl(a.getCoverUrl())
                .author(userNames.getOrDefault(a.getUserId(), "未知用户"))
                .reviewStatus(a.getStatus().getValue())
                .reviewReason(a.getReviewReason())
                .submittedAt(a.getUpdatedAt())
                .build()).toList();
        return PageDTO.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    /**
     * 审核文章
     */
    @Transactional
    public void review(Long articleId, ReviewDTO dto) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        ReviewAction action = ReviewAction.valueOf(dto.getAction().toUpperCase());

        if (action == ReviewAction.APPROVE) {
            articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                    .eq(Article::getId, articleId)
                    .set(Article::getStatus, ArticleStatus.PUBLISHED)
                    .set(Article::getReviewReason, Objects.requireNonNullElse(dto.getReason(), "审核通过")));
            log.info("审核通过: articleId={}", articleId);
        } else {
            articleMapper.update(null, new LambdaUpdateWrapper<Article>()
                    .eq(Article::getId, articleId)
                    .set(Article::getStatus, ArticleStatus.REJECTED)
                    .set(Article::getReviewReason, Objects.requireNonNullElse(dto.getReason(), "未提供理由")));
            log.info("审核驳回: articleId={}, reason={}", articleId, dto.getReason());
        }
    }
}
