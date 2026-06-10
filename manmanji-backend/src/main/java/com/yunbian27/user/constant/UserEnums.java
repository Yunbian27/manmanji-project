package com.yunbian27.user.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public final class UserEnums {

    private UserEnums() {}

    @Getter
    @AllArgsConstructor
    public enum Role {
        USER("USER"),
        ADMIN("ADMIN");

        private final String value;
    }

    @Getter
    @AllArgsConstructor
    public enum Status {
        ACTIVE("ACTIVE"),
        BANNED("BANNED");

        private final String value;
    }
}
