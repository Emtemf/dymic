package com.contract.domain.dataprovider.types;

/**
 * 数据源状态枚举
 */
public enum ProviderStatus {
    ENABLED("启用"),
    DISABLED("停用");

    private final String displayName;

    ProviderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
