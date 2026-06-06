package com.contract.common.enums;

import lombok.Getter;

@Getter
public enum VersionStatusEnum {
    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    DISABLED("DISABLED", "停用");

    private final String code;
    private final String desc;

    VersionStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}