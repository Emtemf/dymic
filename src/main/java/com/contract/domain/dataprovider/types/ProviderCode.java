package com.contract.domain.dataprovider.types;

import com.contract.common.exception.BizException;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;

/**
 * 数据源编码值对象
 */
public final class ProviderCode {
    private final String value;

    public ProviderCode(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException("数据源编码不能为空");
        }
        if (value.length() > 50) {
            throw new BizException("数据源编码长度不能超过50");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() { return value; }

    public static ProviderCode generate(ProviderType providerType) {
        String timestamp = String.valueOf(System.currentTimeMillis() % 100000);
        return new ProviderCode("PROV_" + providerType.name() + "_" + timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProviderCode that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
