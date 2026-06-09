package com.contract.domain.dataprovider.types;

/**
 * 数据源分类枚举
 */
public enum DataSourceCategory {
    BUSINESS("业务配置"),
    IT("IT配置");

    private final String displayName;

    DataSourceCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static DataSourceCategory fromProviderType(ProviderType providerType) {
        return providerType.isBusinessConfig() ? BUSINESS : IT;
    }
}
