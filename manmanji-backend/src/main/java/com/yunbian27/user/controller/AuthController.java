package com.yunbian27.user.controller;

import com.yunbian27.user.model.dto.LoginDTO;
import com.yunbian27.user.model.vo.LoginVO;
import com.yunbian27.user.model.dto.RefreshDTO;
import com.yunbian27.user.model.dto.RegisterDTO;
import com.yunbian27.user.service.AuthService;
import com.yunbian27.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证模块", description = "基于 SpringSecurity + JWT 实现了用户的登录拦截校验")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO req) {
        return Result.success(authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO req) {
        return Result.success(authService.login(req));
    }

    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@Valid @RequestBody RefreshDTO req) {
        return Result.success(authService.refreshAccessToken(req.getRefreshToken()));
    }
}
