package com.contract.domain.shared.types;

import com.contract.application.template.dto.OptionDataDTO;
import com.contract.common.exception.BizException;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
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
            return;
        }
        try {
            JsonNode root = MAPPER.readTree(value);
            if (root.isTextual()) {
                String nested = root.asText();
                JsonNode nestedRoot = MAPPER.readTree(nested);
                this.value = MAPPER.writeValueAsString(nestedRoot);
            } else {
                this.value = MAPPER.writeValueAsString(root);
            }
        } catch (JsonProcessingException e) {
            throw new BizException("配置JSON格式无效: " + e.getMessage());
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

    public boolean isTreeStructure() {
        try {
            JsonNode root = MAPPER.readTree(value);
            return root.isObject() && "tree".equals(root.path("structure").asText());
        } catch (JsonProcessingException e) {
            throw new BizException("解析配置JSON失败: " + e.getMessage());
        }
    }

    public List<OptionDataDTO> parseOptions() {
        try {
            JsonNode root = MAPPER.readTree(value);
            if (root.isArray()) {
                return parseOptionArray(root);
            }
            if (root.isObject() && root.has("children")) {
                return parseOptionArray(root.get("children"));
            }
            if (root.isObject() && root.has("options")) {
                return parseOptionArray(root.get("options"));
            }
            return List.of();
        } catch (JsonProcessingException e) {
            throw new BizException("解析配置JSON失败: " + e.getMessage());
        }
    }

    private List<OptionDataDTO> parseOptionArray(JsonNode arrayNode) {
        List<OptionDataDTO> result = new ArrayList<>();
        for (JsonNode item : arrayNode) {
            result.add(OptionDataDTO.builder()
                .value(item.path("value").asText())
                .label(item.path("label").asText())
                .children(item.has("children") ? parseOptionArray(item.get("children")) : List.of())
                .build());
        }
        return result;
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
