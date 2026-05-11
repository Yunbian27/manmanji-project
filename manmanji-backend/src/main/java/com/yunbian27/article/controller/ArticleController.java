package com.yunbian27.article.controller;

import com.yunbian27.article.dto.ArticleCreateReq;
import com.yunbian27.article.service.ArticleService;
import com.yunbian27.common.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 编辑文章
     * @return
     */
    @PostMapping("/create")
    public R<Long> create(@Valid @RequestBody ArticleCreateReq req) {
        Long articleId = articleService.createArticle(req);
        return R.ok(articleId);
    }
}
