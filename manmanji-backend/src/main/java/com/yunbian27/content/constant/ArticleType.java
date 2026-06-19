package com.yunbian27.content.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章类型 — 1=ORIGINAL 2=REPOST
 */
@Getter
@AllArgsConstructor
public enum ArticleType {

    ORIGINAL(0, "ORIGINAL"),
    REPOST(1, "REPOST");

    @EnumValue
    private final Integer code;
    private final String value;
}
