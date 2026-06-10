package com.yunbian27.common.constant;

/**
 * Redis Key 命名规范
 * @deprecated 请使用各模块下的 RedisKeys（如 ContentRedisKeys、UserRedisKeys）
 */
@Deprecated
public final class RedisKeys {

    private RedisKeys() {}

    public static final String PROJECT = "manmanji:";

    // ===== Token =====
    public static final String REFRESH_TOKEN_PREFIX = PROJECT + "auth:refresh_token:";
    public static final String ACCESS_TOKEN_BLACKLIST_PREFIX = PROJECT + "auth:access_blacklist:";

    // ===== 验证码 =====
    public static final String REGISTER_CODE_PREFIX = PROJECT + "auth:register_code:";
    public static final String REGISTER_CODE_RATE_PREFIX = PROJECT + "auth:code_rate:";

    // 文章缓存
    public static final String ARTICLE_CACHE_PREFIX = PROJECT + "article:";

    // ===== 签到 =====
    public static final String CHECK_IN_BITMAP = PROJECT + "checkin:bitmap:%d:%d";
    public static final String CHECK_IN_LOCK = PROJECT + "checkin:lock:%s";

    // ===== 互动计数 =====
    public static final String LIKE_SET = "like:article:%d";
    public static final String VIEW_COUNT = "view:article:%d";
    public static final String USER_READ_SET = "read:user:%s:%s";
    public static final String USER_LIKE_COUNT = "like:count:user:%s:%s";
    public static final String USER_COMMENT_COUNT = "comment:count:user:%s:%s";

    // ===== 积分 =====
    public static final String POINTS_LOCK = "points:lock:%s";

    // ===== 热榜 =====
    public static final String HOT_ARTICLES = "hot:articles";

    // ===== 秒杀 =====
    public static final String FLASH_SALE_STOCK = "flashsale:stock:%d";
    public static final String FLASH_SALE_USER_SET = "flashsale:user:%d:%d";
    public static final String FLASH_SALE_LOCK = "flashsale:lock:%d:%s";

    // ===== 优惠券 =====
    public static final String COUPON_STOCK = "coupon:stock:%d";
    public static final String COUPON_LOCK = "coupon:lock:%d:%s";
}
