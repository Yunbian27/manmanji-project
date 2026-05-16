package com.yunbian27.common.constant;

/**
 * Redis Key 命名规范
 */
public final class RedisKeys {

    private RedisKeys() {}

    public static final String PROJECT = "manmanji:";

    // ===== Token =====
    public static final String REFRESH_TOKEN = "auth:refresh_token:";       // userId
    public static final String ACCESS_TOKEN_BLACKLIST = "auth:access_blacklist:"; // tokenId

    // 文章缓存
    public static final String ARTICLE_CACHE_PREFIX = PROJECT + "article:";

    // ===== 签到 =====
    public static final String CHECK_IN_BITMAP = "checkin:bitmap:%d:%d";      // year:month
    public static final String CHECK_IN_LOCK = "checkin:lock:%s";             // userId

    // ===== 互动计数 =====
    public static final String LIKE_SET = "like:article:%d";                  // articleId
    public static final String VIEW_COUNT = "view:article:%d";                // articleId
    public static final String USER_READ_SET = "read:user:%s:%s";             // userId, date
    public static final String USER_LIKE_COUNT = "like:count:user:%s:%s";     // userId, date
    public static final String USER_COMMENT_COUNT = "comment:count:user:%s:%s"; // userId, date

    // ===== 积分 =====
    public static final String POINTS_LOCK = "points:lock:%s";                // userId

    // ===== 热榜 =====
    public static final String HOT_ARTICLES = "hot:articles";

    // ===== 秒杀 =====
    public static final String FLASH_SALE_STOCK = "flashsale:stock:%d";       // itemId
    public static final String FLASH_SALE_USER_SET = "flashsale:user:%d:%d";  // itemId, userId
    public static final String FLASH_SALE_LOCK = "flashsale:lock:%d:%s";      // itemId, userId

    // ===== 优惠券 =====
    public static final String COUPON_STOCK = "coupon:stock:%d";              // couponId
    public static final String COUPON_LOCK = "coupon:lock:%d:%s";             // couponId, userId
}
