package com.contract.domain.template.types;

import com.contract.common.exception.BizException;
import java.util.Objects;

/**
 * 模板编码值对象
 *
 * 【业务规则】
 * - 不能为空
 * - 长度1-50
 * - 只能包含字母、数字、下划线
 */
public final class TemplateCode {
    private final String value;

    public TemplateCode(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException("模板编码不能为空");
        }
        if (value.length() > 50) {
            throw new BizException("模板编码长度不能超过50");
        }
        if (!value.matches("^[A-Za-z0-9_]+$")) {
            throw new BizException("模板编码只能包含字母、数字、下划线");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateCode that)) return false;
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
