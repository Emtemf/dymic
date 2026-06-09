package com.contract.domain.dataprovider.types;

import com.contract.common.exception.BizException;
import java.util.Objects;

/**
 * 字典类型值对象
 */
public final class DictType {
    private final String value;

    public DictType(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException("字典类型不能为空");
        }
        this.value = value;
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DictType that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
