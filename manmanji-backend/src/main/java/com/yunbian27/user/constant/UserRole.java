package com.yunbian27.user.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
    USER(0, "USER"),
    ADMIN(1, "ADMIN");

    @EnumValue
    private final Integer code;
    private final String value;
}
