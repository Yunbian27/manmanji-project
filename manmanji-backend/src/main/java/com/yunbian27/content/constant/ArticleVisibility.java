package com.yunbian27.content.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章可见性 — 1=PUBLIC 2=PRIVATE 3=FOLLOWER
 */
@Getter
@AllArgsConstructor
public enum ArticleVisibility {

    PRIVATE(0, "PRIVATE"),
    PUBLIC(1, "PUBLIC"),
    FOLLOWER(2, "FOLLOWER");

    @EnumValue
    private final Integer code;
    private final String value;
}
