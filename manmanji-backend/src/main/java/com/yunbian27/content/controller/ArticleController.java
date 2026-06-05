package com.yunbian27.content.controller;

import com.yunbian27.content.model.dto.ArticlePublishDTO;
import com.yunbian27.content.model.dto.ArticleRenameDTO;
import com.yunbian27.content.model.dto.ArticleSaveDTO;
import com.yunbian27.content.model.vo.ArticleTitlesVO;
import com.yunbian27.content.model.vo.ArticleVO;
import com.yunbian27.content.model.vo.FolderTreeVO;
import com.yunbian27.content.service.ArticleService;
import com.yunbian27.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "文章模块", description = "管理文章编辑发布相关操作")
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("/create")
    public Result<Long> create() {
        return Result.success(articleService.create());
    }

    @PutMapping("/save")
    public Result<Void> save(@Valid @RequestBody ArticleSaveDTO dto) {
        articleService.save(dto);
        return Result.success();
    }

    @PutMapping("/publish")
    public Result<Void> publish(@Valid @RequestBody ArticlePublishDTO dto) {
        articleService.publish(dto);
        return Result.success();
    }

    @PostMapping("/improve")
    public Result<String> improve(@RequestBody String article) {
        return Result.success(articleService.improve(article));
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> article(@PathVariable Long id) {
        return Result.success(articleService.showArticle(id));
    }

    @PutMapping("/{id}")
    public Result<FolderTreeVO> rename(@PathVariable Long id, @RequestBody ArticleRenameDTO dto) {
        return Result.success(articleService.rename(id, dto.getTitle()));
    }

    @DeleteMapping("/{id}")
    public Result<FolderTreeVO> delete(@PathVariable Long id) {
        return Result.success(articleService.delete(id));
    }

    /**
     * 显示用户的文章列表
     * @return
     */
    @GetMapping("/titles")
    public Result<ArticleTitlesVO> showArticleTitles() {
        return Result.success(articleService.showArticleTitles());
    }

}
