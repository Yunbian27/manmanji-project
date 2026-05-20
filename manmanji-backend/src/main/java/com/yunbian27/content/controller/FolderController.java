package com.yunbian27.content.controller;

import com.yunbian27.common.result.Result;
import com.yunbian27.content.model.dto.FolderCreateDTO;
import com.yunbian27.content.model.dto.FolderMoveDTO;
import com.yunbian27.content.model.dto.FolderRenameDTO;
import com.yunbian27.content.service.FolderService;
import com.yunbian27.content.model.vo.FolderTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping
    public Result<FolderTreeVO> show() {
        return Result.success(folderService.show());
    }

    @PutMapping("/move")
    public Result<FolderTreeVO> move(@RequestBody FolderMoveDTO dto) {
        return Result.success(folderService.move(dto));
    }

    @PostMapping
    public Result<FolderTreeVO> create(@RequestBody FolderCreateDTO dto) {
        return Result.success(folderService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<FolderTreeVO> rename(@PathVariable Long id, @RequestBody FolderRenameDTO dto) {
        return Result.success(folderService.rename(id, dto.getName()));
    }

    @DeleteMapping("/{id}")
    public Result<FolderTreeVO> delete(@PathVariable Long id) {
        return Result.success(folderService.delete(id));
    }

}
