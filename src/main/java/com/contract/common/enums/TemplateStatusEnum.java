package com.contract.common.enums;

import lombok.Getter;

@Getter
public enum TemplateStatusEnum {
    ENABLED("ENABLED", "启用"),
    DISABLED("DISABLED", "停用");

    private final String code;
    private final String desc;

    TemplateStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}