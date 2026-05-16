package com.yunbian27.content.controller;

import com.yunbian27.content.model.dto.ArticleCreateDTO;
import com.yunbian27.content.model.dto.MoveArticleDTO;
import com.yunbian27.content.model.dto.RenameArticleDTO;
import com.yunbian27.content.model.vo.ArticleVO;
import com.yunbian27.content.model.vo.FolderTreeVO;
import com.yunbian27.content.service.ArticleService;
import com.yunbian27.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "文章模块", description = "管理文章编辑发布相关操作")
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody ArticleCreateDTO dto) {
        Long articleId = articleService.createArticle(dto);
        return Result.success(articleId);
    }

    @PostMapping("improve")
    public Result<String> improve(@RequestBody String article) {
        return Result.success(articleService.improve(article));
    }

    @GetMapping("/{articleId}")
    public Result<ArticleVO> article(@PathVariable Long articleId) {
        return Result.success(articleService.showArticle(articleId));
    }

    @PutMapping("/move")
    public Result<List<FolderTreeVO>> move(@RequestBody MoveArticleDTO dto) {
        return Result.success(articleService.move(dto));
    }

    @PutMapping("/{id}")
    public Result<List<FolderTreeVO>> rename(@PathVariable Long id, @RequestBody RenameArticleDTO dto) {
        return Result.success(articleService.rename(id, dto.getTitle()));
    }

    @DeleteMapping("/{id}")
    public Result<List<FolderTreeVO>> delete(@PathVariable Long id) {
        return Result.success(articleService.delete(id));
    }

}
