package com.yunbian27.content.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章可见性 — PUBLIC / PRIVATE / FOLLOWER
 */
@Getter
@AllArgsConstructor
public enum ArticleVisibility {

    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE"),
    FOLLOWER("FOLLOWER");

    @EnumValue
    private final String value;
}
