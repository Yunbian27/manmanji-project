package com.yunbian27.content.controller;

import com.yunbian27.content.model.dto.ArticlePublishDTO;
import com.yunbian27.content.model.dto.ArticleSaveDTO;
import com.yunbian27.content.model.vo.ArticleTitlesVO;
import com.yunbian27.content.model.vo.ArticleVO;
import com.yunbian27.content.service.ArticleService;
import com.yunbian27.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "文章模块", description = "管理文章编辑发布相关操作")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 发布文章
     * @param dto
     * @return
     */
    @PutMapping("/publish")
    public Result<Void> publish(@Valid @RequestBody ArticlePublishDTO dto) {
        articleService.publish(dto);
        return Result.success();
    }

    @PutMapping("/save")
    public Result<Void> save(@Valid @RequestBody ArticleSaveDTO dto) {
        articleService.save(dto);
        return Result.success();
    }

    @GetMapping("/groups")
    public Result<List<String>> group() {
        return Result.success(articleService.showGroup());
    }

    @PostMapping("/groups")
    public Result<Void> createGroup(@RequestBody Map<String, String> body) {
        articleService.createGroup(body.get("name"));
        return Result.success();
    }

    @DeleteMapping("/groups/{name}")
    public Result<Void> deleteGroup(@PathVariable String name) {
        articleService.deleteGroup(name);
        return Result.success();
    }

    @PostMapping("/improve")
    public Result<String> improve(@RequestBody String article) {
        return Result.success(articleService.improve(article));
    }

    /**
     * 显示文章内容
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<ArticleVO> article(@PathVariable Long id) {
        return Result.success(articleService.showArticle(id));
    }

    /**
     * 显示用户的文章列表
     * @return
     */
    @GetMapping("/titles")
    public Result<List<ArticleTitlesVO>> showArticleTitles() {
        return Result.success(articleService.showArticleTitles());
    }

}
