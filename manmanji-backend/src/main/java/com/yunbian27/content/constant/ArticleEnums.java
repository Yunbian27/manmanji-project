package com.yunbian27.content.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public final class ArticleEnums {

    private ArticleEnums() {}

    @Getter
    @AllArgsConstructor
    public enum Status {
        UNPUBLISHED("UNPUBLISHED"),
        PUBLISHED("PUBLISHED");

        private final String value;
    }

    @Getter
    @AllArgsConstructor
    public enum Visibility {
        PUBLIC("PUBLIC"),
        PRIVATE("PRIVATE");

        private final String value;
    }

    @Getter
    @AllArgsConstructor
    public enum Type {
        ORIGINAL("ORIGINAL"),
        REPOST("REPOST");

        private final String value;
    }

    @Getter
    @AllArgsConstructor
    public enum CreationStatement {
        NONE("NONE"),
        AI_ASSISTED("AI_ASSISTED"),
        NETWORK_SOURCED("NETWORK_SOURCED"),
        PERSONAL_OPINION("PERSONAL_OPINION");

        private final String value;
    }

    @Getter
    @AllArgsConstructor
    public enum SourceType {
        MANUAL("MANUAL"),
        AI_GENERATED("AI_GENERATED");

        private final String value;
    }
}
