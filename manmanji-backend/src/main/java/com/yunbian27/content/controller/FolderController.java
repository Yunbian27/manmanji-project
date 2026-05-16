package com.yunbian27.content.controller;

import com.yunbian27.common.Result;
import com.yunbian27.content.model.dto.CreateFolderDTO;
import com.yunbian27.content.model.dto.MoveFolderDTO;
import com.yunbian27.content.service.FolderService;
import com.yunbian27.content.model.vo.FolderTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    /**
     * 获取文件夹树
     * @return
     */
    @GetMapping
    public Result<List<FolderTreeVO>> show() {
        return Result.success(folderService.show());
    }


    /**
     * 根据id更新文件夹树
     * @return
     */
    @PutMapping("/move")
    public Result<List<FolderTreeVO>> move(@RequestBody MoveFolderDTO dto) {
        return Result.success(folderService.move(dto));
    }

    @PostMapping
    public Result<List<FolderTreeVO>> create(@RequestBody CreateFolderDTO dto) {
        return Result.success(folderService.create(dto));
    }

}
