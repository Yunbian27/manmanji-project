package com.yunbian27.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${jwt.access-token-expire}")
    private long accessTokenExpire;

    @Value("${jwt.refresh-token-expire}")
    private long refreshTokenExpire;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            String privateKeyContent = StreamUtils.copyToString(
                    privateKeyResource.getInputStream(), StandardCharsets.UTF_8);
            String publicKeyContent = StreamUtils.copyToString(
                    publicKeyResource.getInputStream(), StandardCharsets.UTF_8);

            privateKeyContent = stripPemHeaders(privateKeyContent);
            publicKeyContent = stripPemHeaders(publicKeyContent);

            byte[] privateBytes = Base64.getDecoder().decode(privateKeyContent);
            byte[] publicBytes = Base64.getDecoder().decode(publicKeyContent);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicBytes));

            log.info("RSA 密钥加载成功");
        } catch (Exception e) {
            log.error("RSA 密钥加载失败", e);
            throw new RuntimeException("JWT 密钥加载失败", e);
        }
    }

    private String stripPemHeaders(String pem) {
        return pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
    }

    public String generateAccessToken(Long userId, String role) {
        String tokenId = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpire * 1000);

        return Jwts.builder()
                .id(tokenId)
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        String tokenId = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpire * 1000);

        return Jwts.builder()
                .id(tokenId)
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith((java.security.interfaces.RSAPublicKey) publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public long getAccessTokenExpire() {
        return accessTokenExpire;
    }
}
