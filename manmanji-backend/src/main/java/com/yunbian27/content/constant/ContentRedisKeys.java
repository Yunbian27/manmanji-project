package com.yunbian27.content.constant;

/**
 * 内容模块 Redis Key
 */
public final class ContentRedisKeys {

    private ContentRedisKeys() {}

    public static final String ARTICLE_CACHE_PREFIX = "manmanji:article:";

    // 互动计数
    public static final String LIKE_SET = "like:article:%d";
    public static final String VIEW_COUNT = "view:article:%d";
    public static final String USER_READ_SET = "read:user:%s:%s";
    public static final String USER_LIKE_COUNT = "like:count:user:%s:%s";
    public static final String USER_COMMENT_COUNT = "comment:count:user:%s:%s";

    // 热榜
    public static final String HOT_ARTICLES = "hot:articles";

    // 秒杀
    public static final String FLASH_SALE_STOCK = "flashsale:stock:%d";
    public static final String FLASH_SALE_USER_SET = "flashsale:user:%d:%d";
    public static final String FLASH_SALE_LOCK = "flashsale:lock:%d:%s";

    // 优惠券
    public static final String COUPON_STOCK = "coupon:stock:%d";
    public static final String COUPON_LOCK = "coupon:lock:%d:%s";
}
