package com.contract.domain.shared.types;

import com.contract.common.exception.BizException;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;

/**
 * 配置JSON值对象
 */
public final class ConfigJson {
    private final String value;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ConfigJson(String value) {
        if (value == null || value.isBlank()) {
            this.value = "{}";
        } else {
            try {
                MAPPER.readTree(value);
            } catch (JsonProcessingException e) {
                throw new BizException("配置JSON格式无效: " + e.getMessage());
            }
            this.value = value;
        }
    }

    @JsonValue
    public String getValue() { return value; }

    public <T> T parse(Class<T> clazz) {
        try {
            return MAPPER.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            throw new BizException("解析配置JSON失败: " + e.getMessage());
        }
    }

    public static ConfigJson from(Object obj) {
        try {
            return new ConfigJson(MAPPER.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            throw new BizException("序列化配置JSON失败: " + e.getMessage());
        }
    }

    public static ConfigJson fromDict(String dictType) {
        return new ConfigJson("{\"dictType\":\"" + dictType + "\"}");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigJson that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
