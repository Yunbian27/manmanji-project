package com.yunbian27.content.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章来源类型 — 1=MANUAL 2=AI_GENERATED
 */
@Getter
@AllArgsConstructor
public enum SourceType {

    MANUAL(0, "MANUAL"),
    AI_GENERATED(1, "AI_GENERATED");

    @EnumValue
    private final Integer code;
    private final String value;
}
