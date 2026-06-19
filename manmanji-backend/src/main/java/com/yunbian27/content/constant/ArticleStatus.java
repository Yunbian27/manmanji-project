package com.yunbian27.content.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章状态 — 1=DRAFT 2=REVIEWING 3=PUBLISHED 4=REJECTED
 */
@Getter
@AllArgsConstructor
public enum ArticleStatus {

    DRAFT(0, "DRAFT"),
    REVIEWING(1, "REVIEWING"),
    PUBLISHED(2, "PUBLISHED"),
    REJECTED(3, "REJECTED");

    @EnumValue
    private final Integer code;
    private final String value;
}
