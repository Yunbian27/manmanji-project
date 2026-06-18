package com.yunbian27.content.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章来源类型 — MANUAL / AI_GENERATED
 */
@Getter
@AllArgsConstructor
public enum SourceType {

    MANUAL("MANUAL"),
    AI_GENERATED("AI_GENERATED");

    @EnumValue
    private final String value;
}
