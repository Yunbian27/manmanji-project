package com.yunbian27.admin.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewAction {
    APPROVE("APPROVE"),
    REJECT("REJECT");

    @EnumValue
    private final String value;
}
