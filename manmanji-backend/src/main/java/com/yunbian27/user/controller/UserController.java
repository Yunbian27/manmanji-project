package com.yunbian27.user.controller;

import com.yunbian27.common.Result;
import com.yunbian27.user.model.vo.FolderTreeVO;
import com.yunbian27.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户模块", description = "用户相关操作")
public class UserController {

    private final UserService userService;

    /**
     * 获取文件夹树
     * @return
     */
    @GetMapping("/folders")
    public Result<List<FolderTreeVO>> folders() {
        return Result.success(userService.showFolders());
    }

}
