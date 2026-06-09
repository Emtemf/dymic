package com.contract.domain.dataprovider.types;

import com.contract.common.exception.BizException;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

/**
 * 数据源ID值对象
 */
public final class ProviderId {
    private final Long value;

    public ProviderId(Long value) {
        if (value == null || value <= 0) {
            throw new BizException("数据源ID无效");
        }
        this.value = value;
    }

    @JsonValue
    public Long getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProviderId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return String.valueOf(value); }
}
