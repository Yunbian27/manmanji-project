package com.yunbian27.auth.controller;

import com.yunbian27.auth.dto.LoginReq;
import com.yunbian27.auth.dto.LoginResp;
import com.yunbian27.auth.dto.RefreshReq;
import com.yunbian27.auth.dto.RegisterReq;
import com.yunbian27.auth.service.AuthService;
import com.yunbian27.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<LoginResp> register(@Valid @RequestBody RegisterReq req) {
        return Result.success(authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return Result.success(authService.login(req));
    }

    @PostMapping("/refresh")
    public Result<LoginResp> refresh(@Valid @RequestBody RefreshReq req) {
        return Result.success(authService.refreshAccessToken(req.getRefreshToken()));
    }
}
