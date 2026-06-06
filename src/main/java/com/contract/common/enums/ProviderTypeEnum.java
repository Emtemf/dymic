package com.contract.common.enums;

import lombok.Getter;

@Getter
public enum ProviderTypeEnum {
    STATIC("STATIC", "静态选项"),
    DICT("DICT", "字典"),
    HTTP("HTTP", "HTTP接口"),
    PLATFORM("PLATFORM", "平台集成"),
    INTERNAL("INTERNAL", "内部查询");

    private final String code;
    private final String desc;

    ProviderTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}