package com.yunbian27.content.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 创作声明 — 0=NONE 1=AI_ASSISTED 2=NETWORK_SOURCED 3=PERSONAL_OPINION
 */
@Getter
@AllArgsConstructor
public enum CreationStatement {

    NONE(0, "NONE"),
    AI_ASSISTED(1, "AI_ASSISTED"),
    NETWORK_SOURCED(2, "NETWORK_SOURCED"),
    PERSONAL_OPINION(3, "PERSONAL_OPINION");

    @EnumValue
    private final Integer code;
    private final String value;
}
