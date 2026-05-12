package com.yunbian27.article.controller;

import com.yunbian27.article.dto.ArticleCreateReq;
import com.yunbian27.article.service.ArticleService;
import com.yunbian27.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 编辑文章
     * @return
     */
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody ArticleCreateReq req) {
        Long articleId = articleService.createArticle(req);
        return Result.success(articleId);
    }

    /**
     * 文章润色
     */
    @GetMapping("improve")
    public Result<String> improve(@RequestBody String article) {
        return Result.success(articleService.improve(article));
    }
}
