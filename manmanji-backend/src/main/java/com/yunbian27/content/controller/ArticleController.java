package com.yunbian27.content.controller;

import com.yunbian27.content.model.dto.ArticlePublishDTO;
import com.yunbian27.content.model.dto.ArticleSaveDTO;
import com.yunbian27.content.model.vo.ArticleTitlesVO;
import com.yunbian27.content.model.vo.ArticleVO;
import com.yunbian27.content.model.vo.GroupVO;
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
     * 新建并发布文章
     * @param dto
     * @return
     */
    @PostMapping("/publish")
    public Result<Long> publish(@Valid @RequestBody ArticlePublishDTO dto) {
        return Result.success(articleService.publish(dto));
    }

    /**
     * 更新已有文章并发布
     * @param id
     * @param dto
     * @return
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticlePublishDTO dto) {
        articleService.update(id, dto);
        return Result.success();
    }

    /*@PutMapping("/save")
    public Result<Void> save(@Valid @RequestBody ArticleSaveDTO dto) {
        articleService.save(dto);
        return Result.success();
    }*/

    @GetMapping("/groups")
    public Result<List<GroupVO>> group() {
        return Result.success(articleService.showGroup());
    }

    @PostMapping("/groups")
    public Result<Long> createGroup(@RequestBody Map<String, String> body) {
        return Result.success(articleService.createGroup(body.get("name")));
    }

    @DeleteMapping("/groups/{id}")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        articleService.deleteGroup(id);
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
