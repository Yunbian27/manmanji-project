package com.yunbian27.user.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    ACTIVE(0, "ACTIVE"),
    BANNED(1, "BANNED");

    @EnumValue
    private final Integer code;
    private final String value;
}
