package com.contract.domain.template.types;

import com.contract.common.exception.BizException;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

/**
 * 模板名称值对象
 *
 * 【业务规则】
 * - 不能为空
 * - 长度1-100
 */
public final class TemplateName {
    private final String value;

    public TemplateName(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException("模板名称不能为空");
        }
        if (value.length() > 100) {
            throw new BizException("模板名称长度不能超过100");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateName that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
