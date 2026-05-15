package com.yunbian27.user.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    private String username;
    /** 邮箱 */
    private String email;
    /** BCrypt密码哈希 */
    private String passwordHash;
    /** 昵称 */
    private String nickname;
    /** 头像链接 */
    private String avatarUrl;
    /** 个人简介 */
    private String bio;
    /** 角色(USER/ADMIN) */
    private String role;
    /** 积分余额 */
    private Integer pointsBalance;
    /** AI对话剩余次数 */
    private Integer aiQuota;
    /** 用户自带API Key(加密存储) */
    private String apiKeyEncrypted;
    /** API提供商(如deepseek/openai) */
    private String apiKeyProvider;
    /** 账号状态(ACTIVE/BANNED) */
    private String status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
