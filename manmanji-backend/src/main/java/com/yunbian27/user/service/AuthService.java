package com.yunbian27.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunbian27.common.constant.RedisKeys;
import com.yunbian27.common.constant.RedisTTL;
import com.yunbian27.common.constant.SystemConstants;
import com.yunbian27.user.config.JwtProperties;
import com.yunbian27.user.model.dto.LoginDTO;
import com.yunbian27.user.model.vo.LoginVO;
import com.yunbian27.user.model.dto.RegisterDTO;
import com.yunbian27.user.model.entity.User;
import com.yunbian27.user.mapper.UserMapper;
import com.yunbian27.common.exception.BusinessException;
import com.yunbian27.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;
    private final MailService mailService;

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration CODE_RATE_TTL = Duration.ofSeconds(60);

    public void sendCode(String email) {
        // 限流：同一邮箱 60 秒内不能重复发送
        String rateKey = RedisKeys.REGISTER_CODE_RATE_PREFIX + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(rateKey))) {
            throw new BusinessException(ErrorCode.CODE_SEND_TOO_FREQUENT);
        }

        // 生成 6 位数字验证码
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        // 存入 Redis（5 分钟过期）
        stringRedisTemplate.opsForValue().set(
                RedisKeys.REGISTER_CODE_PREFIX + email, code, CODE_TTL);
        // 限流标记（60 秒）
        stringRedisTemplate.opsForValue().set(rateKey, "1", CODE_RATE_TTL);

        // 异步发邮件
        mailService.sendCode(email, code);
    }

    public LoginVO register(RegisterDTO req) {
        boolean exists = userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (exists) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        exists = userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, req.getEmail()));
        if (exists) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        // 校验邮箱验证码
        String codeKey = RedisKeys.REGISTER_CODE_PREFIX + req.getEmail();
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (storedCode == null) {
            throw new BusinessException(ErrorCode.CODE_EXPIRED);
        }
        if (!storedCode.equals(req.getCode())) {
            throw new BusinessException(ErrorCode.CODE_INCORRECT);
        }
        stringRedisTemplate.delete(codeKey); // 验证通过后删除，一次性使用

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        user.setAvatarUrl(SystemConstants.DEFAULT_AVATAR);
        user.setRole("USER");
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        // 用户注册时自动分配一个待整理文件夹


        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpire())
                .user(toUserInfo(user))
                .build();
    }

    public LoginVO login(LoginDTO dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getAccount(), dto.getPassword())
            );

            org.springframework.security.core.userdetails.User principal =
                    (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, principal.getUsername()));

            String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
            String refreshToken = jwtService.generateRefreshToken(user.getId());

            return LoginVO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtService.getAccessTokenExpire())
                    .user(toUserInfo(user))
                    .build();

        } catch (BadCredentialsException e) {
            throw e;
        }
    }

    public LoginVO refreshAccessToken(String refreshTokenStr) {
        Long userId = jwtService.getUserIdFromToken(refreshTokenStr);
        // 获取轮回id
        String incomingJti = jwtService.parseToken(refreshTokenStr).getId();

        // 查询reids中的刷新token的id是否匹配
        String key = RedisKeys.REFRESH_TOKEN_PREFIX + userId;
        String storedJti = stringRedisTemplate.opsForValue().get(key);

        if (storedJti == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!storedJti.equals(incomingJti)) {
            // 重用检测：有人拿着已失效的 Token 来刷新
            stringRedisTemplate.delete(key); // 全部作废
            log.warn("检测到 Refresh Token 被重用！userId={}", userId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED); // 强制重新登录
        }

        // 查库，校验当前用户状态
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if ("BANNED".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        // 构建新的Jti
        String newRefreshTokenJti = jwtService.parseToken(newRefreshToken).getId();
        stringRedisTemplate.opsForValue().set(key, newRefreshTokenJti, Duration.ofSeconds(jwtProperties.getRefreshTokenExpire()));

        return LoginVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtService.getAccessTokenExpire())
                .user(toUserInfo(user))
                .build();
    }

    public void logout(Long userId, String accessTokenJti, Date accessTokenExp) {
        // 删除 Refresh Token 存根
        stringRedisTemplate.delete(RedisKeys.REFRESH_TOKEN_PREFIX + userId);
        // Access Token 加入黑名单，TTL = 剩余有效时间
        long ttl = (accessTokenExp.getTime() - System.currentTimeMillis()) / 1000;
        if (ttl > 0) {
            stringRedisTemplate.opsForValue().set(
                    RedisKeys.ACCESS_TOKEN_BLACKLIST_PREFIX + accessTokenJti,
                    "1", Duration.ofSeconds(ttl));
        }
    }

    private LoginVO.UserInfo toUserInfo(User user) {
        return LoginVO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }
}
