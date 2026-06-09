package com.contract.domain.template.types;

/**
 * 版本状态枚举
 */
public enum VersionStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布"),
    DISABLED("已停用"),
    ARCHIVED("已归档");

    private final String displayName;

    VersionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canPublish() {
        return this == DRAFT;
    }

    public boolean canDisable() {
        return this == PUBLISHED;
    }
}
