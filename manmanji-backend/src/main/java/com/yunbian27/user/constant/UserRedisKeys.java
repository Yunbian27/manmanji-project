package com.yunbian27.user.constant;

/**
 * 用户模块 Redis Key
 */
public final class UserRedisKeys {

    private UserRedisKeys() {}

    // Token
    public static final String REFRESH_TOKEN_PREFIX = "manmanji:auth:refresh_token:";
    public static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "manmanji:auth:access_blacklist:";

    // 验证码
    public static final String REGISTER_CODE_PREFIX = "manmanji:auth:register_code:";
    public static final String REGISTER_CODE_RATE_PREFIX = "manmanji:auth:code_rate:";

    // 签到
    public static final String CHECK_IN_BITMAP = "manmanji:checkin:bitmap:%d:%d";
    public static final String CHECK_IN_LOCK = "manmanji:checkin:lock:%s";

    // 积分
    public static final String POINTS_LOCK = "points:lock:%s";
}
