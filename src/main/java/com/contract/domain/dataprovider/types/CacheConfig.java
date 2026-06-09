package com.contract.domain.dataprovider.types;

import java.util.Objects;

/**
 * 缓存配置值对象
 */
public final class CacheConfig {
    private final boolean enabled;
    private final int ttlSeconds;

    private CacheConfig(boolean enabled, int ttlSeconds) {
        this.enabled = enabled;
        this.ttlSeconds = ttlSeconds;
    }

    public static CacheConfig disabled() {
        return new CacheConfig(false, 0);
    }

    public static CacheConfig enabled(int ttlSeconds) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("缓存时间必须大于0");
        }
        return new CacheConfig(true, ttlSeconds);
    }

    public boolean isEnabled() { return enabled; }
    public int getTtlSeconds() { return ttlSeconds; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheConfig that)) return false;
        return enabled == that.enabled && ttlSeconds == that.ttlSeconds;
    }

    @Override
    public int hashCode() { return Objects.hash(enabled, ttlSeconds); }
}
