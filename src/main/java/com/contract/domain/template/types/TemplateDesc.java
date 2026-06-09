package com.contract.domain.template.types;

import java.util.Objects;

/**
 * 模板描述值对象
 */
public final class TemplateDesc {
    private final String value;

    public TemplateDesc(String value) {
        if (value != null && value.length() > 500) {
            throw new IllegalArgumentException("模板描述长度不能超过500");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateDesc that)) return false;
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
