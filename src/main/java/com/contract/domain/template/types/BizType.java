package com.contract.domain.template.types;

import com.contract.common.exception.BizException;
import java.util.Objects;

/**
 * 业务类型值对象
 *
 * 【业务规则】
 * - 可为空
 * - 长度不超过50
 */
public final class BizType {
    private final String value;

    public BizType(String value) {
        if (value != null && value.length() > 50) {
            throw new BizException("业务类型长度不能超过50");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BizType that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value != null ? value : "";
    }
}
