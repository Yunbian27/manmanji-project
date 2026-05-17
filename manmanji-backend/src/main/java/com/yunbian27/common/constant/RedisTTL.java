package com.yunbian27.common.constant;

import java.time.Duration;

public final class RedisTTL {

    // 禁止外部实例化这个类
    private RedisTTL(){}

    // 文章缓存：30分钟
    public static final Duration ARTICLE = Duration.ofMinutes(30);

}
