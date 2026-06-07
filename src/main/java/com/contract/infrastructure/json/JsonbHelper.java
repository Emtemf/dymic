package com.contract.infrastructure.json;

import org.springframework.stereotype.Component;

/**
 * openGauss JSONB 查询工具类
 * 封装 JSONB 操作符和函数，用于 MyBatis XML 中的 SQL 片段
 */
@Component
public class JsonbHelper {

    public String extractText(String column, String path) {
        return column + "->>'" + path + "'";
    }

    public String extractInt(String column, String path) {
        return "(" + column + "->>'" + path + "')::int";
    }

    public String pathExists(String column, String path) {
        return column + " ? '" + path + "'";
    }

    public String contains(String column, String json) {
        return column + " @> '" + json + "'::jsonb";
    }

    public String pathQuery(String column, String path) {
        return "jsonb_path_query(" + column + ", '$." + path + "')";
    }

    public String extractTextDeep(String column, String... paths) {
        return column + "#>>'{" + String.join(",", paths) + "}'";
    }
}
