package com.yunbian27.content.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章类型 — ORIGINAL / REPOST
 */
@Getter
@AllArgsConstructor
public enum ArticleType {

    ORIGINAL("ORIGINAL"),
    REPOST("REPOST");

    @EnumValue
    private final String value;
}
