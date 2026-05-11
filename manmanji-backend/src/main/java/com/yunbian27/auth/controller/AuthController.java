package com.yunbian27.auth.controller;

import com.yunbian27.auth.dto.LoginReq;
import com.yunbian27.auth.dto.LoginResp;
import com.yunbian27.auth.dto.RefreshReq;
import com.yunbian27.auth.dto.RegisterReq;
import com.yunbian27.auth.service.AuthService;
import com.yunbian27.common.R;
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
    public R<LoginResp> register(@Valid @RequestBody RegisterReq req) {
        return R.ok(authService.register(req));
    }

    @PostMapping("/login")
    public R<LoginResp> login(@Valid @RequestBody LoginReq req) {
        return R.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public R<LoginResp> refresh(@Valid @RequestBody RefreshReq req) {
        return R.ok(authService.refreshAccessToken(req.getRefreshToken()));
    }
}
