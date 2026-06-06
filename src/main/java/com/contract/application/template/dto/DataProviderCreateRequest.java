package com.contract.application.template.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 数据提供方创建请求（业务友好）
 *
 * 业务人员选择数据源类型时的请求结构：
 * - STATIC: 业务自定义选项（前端传递选项列表）
 * - DICT: 字典数据（前端传递字典类型）
 * - HTTP/PLATFORM/INTERNAL: IT已配置的数据源（前端传递dataProviderId）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataProviderCreateRequest {

    /**
     * 数据源类型：STATIC/DICT/HTTP/PLATFORM/INTERNAL
     */
    @NotBlank(message = "数据源类型不能为空")
    private String dataSourceType;

    /**
     * 显示名称（如"城市选择"、"状态字典"）
     */
    @NotBlank(message = "显示名称不能为空")
    private String displayName;

    // ============ STATIC类型字段 ============

    /**
     * 静态选项JSON（仅STATIC类型使用）
     * 格式：[{"value":"北京","label":"北京"},{"value":"上海","label":"上海"}]
     */
    private String staticOptionsJson;

    // ============ DICT类型字段 ============

    /**
     * 字典类型编码（仅DICT类型使用）
     * 如：CITY、STATUS、GENDER
     */
    private String dictType;

    // ============ HTTP/PLATFORM/INTERNAL类型字段（IT配置） ============

    /**
     * 已配置的数据提供方ID（HTTP/PLATFORM/INTERNAL类型使用）
     */
    private Long dataProviderId;

    // ============ 其他配置 ============

    /**
     * 是否启用缓存（可选，默认根据类型自动设置）
     */
    private Integer cacheEnabled;

    /**
     * 缓存时长（秒，可选）
     */
    private Integer cacheTtlSeconds;
}