package com.yunbian27.content.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 创作声明 — NONE / AI_ASSISTED / NETWORK_SOURCED / PERSONAL_OPINION
 */
@Getter
@AllArgsConstructor
public enum CreationStatement {

    NONE("NONE"),
    AI_ASSISTED("AI_ASSISTED"),
    NETWORK_SOURCED("NETWORK_SOURCED"),
    PERSONAL_OPINION("PERSONAL_OPINION");

    @EnumValue
    private final String value;
}
