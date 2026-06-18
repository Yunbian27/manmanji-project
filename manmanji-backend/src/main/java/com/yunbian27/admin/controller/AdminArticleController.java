package com.yunbian27.admin.controller;

import com.yunbian27.admin.model.dto.ReviewDTO;
import com.yunbian27.admin.model.vo.ReviewListVO;
import com.yunbian27.admin.service.AdminArticleService;
import com.yunbian27.common.result.PageDTO;
import com.yunbian27.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
@Tag(name = "管理员 - 文章审核")
public class AdminArticleController {

    private final AdminArticleService adminArticleService;

    @GetMapping
    @Operation(summary = "审核文章列表")
    public Result<PageDTO<ReviewListVO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminArticleService.list(status, page, size));
    }

    @PutMapping("/{id}/review")
    @Operation(summary = "审核文章（通过/驳回）")
    public Result<Void> review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDTO dto) {
        adminArticleService.review(id, dto);
        return Result.success();
    }
}
