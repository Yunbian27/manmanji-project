package com.yunbian27.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunbian27.auth.dto.LoginReq;
import com.yunbian27.auth.dto.LoginResp;
import com.yunbian27.auth.dto.RegisterReq;
import com.yunbian27.auth.entity.User;
import com.yunbian27.auth.mapper.UserMapper;
import com.yunbian27.common.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResp register(RegisterReq req) {
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (exists) {
            throw new BizException("用户名已存在");
        }

        exists = userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, req.getEmail()));
        if (exists) {
            throw new BizException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        user.setRole("USER");
        user.setStatus("ACTIVE");
        user.setPointsBalance(0);
        user.setAiQuota(10);
        userMapper.insert(user);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return LoginResp.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpire())
                .user(toUserInfo(user))
                .build();
    }

    public LoginResp login(LoginReq req) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            org.springframework.security.core.userdetails.User principal =
                    (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, principal.getUsername()));

            String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
            String refreshToken = jwtService.generateRefreshToken(user.getId());

            return LoginResp.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtService.getAccessTokenExpire())
                    .user(toUserInfo(user))
                    .build();

        } catch (BadCredentialsException e) {
            throw e;
        }
    }

    public LoginResp refreshAccessToken(String refreshTokenStr) {
        Long userId = jwtService.getUserIdFromToken(refreshTokenStr);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if ("BANNED".equals(user.getStatus())) {
            throw new BizException("用户已被禁用");
        }

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        return LoginResp.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtService.getAccessTokenExpire())
                .user(toUserInfo(user))
                .build();
    }

    private LoginResp.UserInfo toUserInfo(User user) {
        return LoginResp.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }
}
