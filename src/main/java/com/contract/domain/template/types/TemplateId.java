package com.contract.domain.template.types;

import java.util.Objects;

/**
 * 模板ID值对象
 */
public final class TemplateId {
    private final Long value;

    public TemplateId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("模板ID无效");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateId that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
