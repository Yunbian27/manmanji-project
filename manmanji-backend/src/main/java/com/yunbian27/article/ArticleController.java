package com.yunbian27.article;

import com.yunbian27.article.model.ArticleCreateDTO;
import com.yunbian27.article.service.ArticleService;
import com.yunbian27.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
@Tag(name = "文章模块", description = "管理文章编辑发布相关操作")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 编辑文章
     * @return
     */
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody ArticleCreateDTO req) {
        Long articleId = articleService.createArticle(req);
        return Result.success(articleId);
    }

    /**
     * 文章润色
     */
    @PostMapping("improve")
    public Result<String> improve(@RequestBody String article) {
        return Result.success(articleService.improve(article));
    }
}
