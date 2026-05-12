package com.yunbian27.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /** JWT RSA 私钥路径 */
    private Resource privateKeyPath;
    /** JWT RSA 公钥路径 */
    private Resource publicKeyPath;
    /** AccessToken 过期时间（秒），默认 15 分钟 */
    private long accessTokenExpire = 900;
    /** RefreshToken 过期时间（秒），默认 7 天 */
    private long refreshTokenExpire = 604800;
}
