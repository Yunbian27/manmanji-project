package com.yunbian27.user.controller;

import com.yunbian27.user.model.dto.LoginDTO;
import com.yunbian27.user.model.vo.LoginVO;
import com.yunbian27.user.model.dto.RefreshDTO;
import com.yunbian27.user.model.dto.RegisterDTO;
import com.yunbian27.user.model.dto.SendCodeDTO;
import com.yunbian27.user.service.AuthService;
import com.yunbian27.common.Result;
import com.yunbian27.common.utils.SecurityUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        authService.sendCode(dto.getEmail());
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@Valid @RequestBody RefreshDTO dto) {
        return Result.success(authService.refreshAccessToken(dto.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Claims claims = (Claims) auth.getDetails();
        authService.logout(SecurityUtils.getCurrentUserId(), claims.getId(), claims.getExpiration());
        return Result.success();
    }
}
