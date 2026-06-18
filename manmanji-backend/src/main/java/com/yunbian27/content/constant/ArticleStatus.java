package com.yunbian27.content.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章状态 — DRAFT / REVIEWING / PUBLISHED / REJECTED
 */
@Getter
@AllArgsConstructor
public enum ArticleStatus {

    DRAFT("DRAFT"),
    REVIEWING("REVIEWING"),
    PUBLISHED("PUBLISHED"),
    REJECTED("REJECTED"),
    PRIVATE("PRIVATE");

    @EnumValue
    private final String value;
}
