package com.contract.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JSONB 工具类
 * 用于处理 JSONB 字段的序列化和反序列化
 */
public class JsonbUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonbUtils.class);

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * 对象转 JSON 字符串
     *
     * @param obj 对象
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON", e);
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * JSON 字符串转对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @return 对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to object: {}", json, e);
            throw new RuntimeException("Failed to parse JSON to object", e);
        }
    }

    /**
     * JSON 字符串转 List
     *
     * @param json  JSON 字符串
     * @param clazz 元素类型
     * @return List
     */
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to list: {}", json, e);
            throw new RuntimeException("Failed to parse JSON to list", e);
        }
    }

    /**
     * JSON 字符串转 Map
     *
     * @param json JSON 字符串
     * @return Map
     */
    public static Map<String, Object> fromJsonMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to map: {}", json, e);
            throw new RuntimeException("Failed to parse JSON to map", e);
        }
    }

    /**
     * 获取 ObjectMapper 实例
     *
     * @return ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}