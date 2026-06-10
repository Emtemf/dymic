package com.contract.domain.shared.types;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * 审计信息值对象
 *
 * 【不变性】
 * - 创建后不可修改，update()返回新实例
 */
public final class AuditInfo {
    private final Long createdBy;
    private final String createdName;
    private final OffsetDateTime createdAt;
    private final Long updatedBy;
    private final String updatedName;
    private final OffsetDateTime updatedAt;

    private AuditInfo(
        Long createdBy, String createdName, OffsetDateTime createdAt,
        Long updatedBy, String updatedName, OffsetDateTime updatedAt
    ) {
        this.createdBy = createdBy;
        this.createdName = createdName;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedName = updatedName;
        this.updatedAt = updatedAt;
    }

    /**
     * 创建审计信息（新建时）
     */
    public static AuditInfo create() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Long currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        return new AuditInfo(currentUserId, currentUserName, now, currentUserId, currentUserName, now);
    }

    /**
     * 从已有数据重建
     */
    public static AuditInfo of(
        Long createdBy, String createdName, OffsetDateTime createdAt,
        Long updatedBy, String updatedName, OffsetDateTime updatedAt
    ) {
        return new AuditInfo(createdBy, createdName, createdAt, updatedBy, updatedName, updatedAt);
    }

    /**
     * 更新审计信息（修改时）
     *
     * @return 新实例
     */
    public AuditInfo update() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Long currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        return new AuditInfo(
            this.createdBy, this.createdName, this.createdAt,
            currentUserId, currentUserName, now
        );
    }

    // Getters
    public Long getCreatedBy() { return createdBy; }
    public String getCreatedName() { return createdName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public Long getUpdatedBy() { return updatedBy; }
    public String getUpdatedName() { return updatedName; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    private static Long getCurrentUserId() {
        // TODO: 从SecurityContext获取
        return 1L;
    }

    private static String getCurrentUserName() {
        // TODO: 从SecurityContext获取
        return "system";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditInfo that)) return false;
        return Objects.equals(createdBy, that.createdBy)
            && Objects.equals(createdName, that.createdName)
            && Objects.equals(createdAt, that.createdAt)
            && Objects.equals(updatedBy, that.updatedBy)
            && Objects.equals(updatedName, that.updatedName)
            && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdBy, createdName, createdAt, updatedBy, updatedName, updatedAt);
    }
}
