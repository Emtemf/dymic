package com.contract.domain.dataprovider.types;

import com.contract.common.exception.BizException;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

/**
 * 数据源名称值对象
 */
public final class ProviderName {
    private final String value;

    public ProviderName(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException("数据源名称不能为空");
        }
        if (value.length() > 100) {
            throw new BizException("数据源名称长度不能超过100");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProviderName that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
