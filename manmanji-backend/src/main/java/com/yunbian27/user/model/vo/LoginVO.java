package com.yunbian27.user.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginVO {

    /** 访问令牌(15分钟) */
    private String accessToken;
    /** 刷新令牌(7天) */
    private String refreshToken;
    /** 过期秒数 */
    private long expiresIn;
    /** 用户信息 */
    private UserInfo user;

    @Data
    @Builder
    @AllArgsConstructor
    public static class UserInfo {
        /** 用户ID */
        private Long id;
        /** 用户名 */
        private String username;
        /** 邮箱 */
        private String email;
        /** 昵称 */
        private String nickname;
        /** 头像链接 */
        private String avatarUrl;
        /** 角色(USER/ADMIN) */
        private String role;
    }
}
