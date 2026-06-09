package com.contract.domain.dataprovider.types;

/**
 * 数据源类型枚举
 */
public enum ProviderType {
    STATIC("静态选项"),
    DICT("字典数据"),
    HTTP("HTTP接口"),
    PLATFORM("平台接口"),
    INTERNAL("内部查询");

    private final String displayName;

    ProviderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public boolean isBusinessConfig() {
        return this == STATIC || this == DICT;
    }

    public boolean isITConfig() {
        return this == HTTP || this == PLATFORM || this == INTERNAL;
    }
}
