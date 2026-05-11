package com.yunbian27.ai.controller;

import com.yunbian27.ai.service.ArticlePlusService;
import com.yunbian27.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章润色
 */
@RestController
@RequiredArgsConstructor
public class ArticlePlusController {

    private final ArticlePlusService articlePlusService;

    @GetMapping("/api/article-plus")
    public R<String> improve(@RequestBody String article) {
        return R.ok(articlePlusService.improve(article));
    }
}
