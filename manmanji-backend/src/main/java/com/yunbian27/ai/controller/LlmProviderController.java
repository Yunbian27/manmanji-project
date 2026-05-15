package com.yunbian27.ai.controller;

import com.yunbian27.ai.model.CreateProviderDTO;
import com.yunbian27.ai.model.LlmProviderVO;
import com.yunbian27.ai.service.LlmProviderService;
import com.yunbian27.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI模块", description = "AI Provider 管理与对话")
public class LlmProviderController {

    private final LlmProviderService aiService;

    @GetMapping("/list")
    @Operation(summary = "查询所有 LLM Provider")
    public Result<List<LlmProviderVO>> listProviders() {
        return Result.success(aiService.listProviders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询单个 LLM Provider 详情")
    public Result<LlmProviderVO> getProvider(@PathVariable String id) {
        return Result.success(aiService.getProvider(id));
    }

    /**
     * 创建 LLM Provider
     */
    @PostMapping
    public Result<Void> createProvider(@RequestBody CreateProviderDTO dto) {
        aiService.createProvider(dto);
        return Result.success();
    }
}
