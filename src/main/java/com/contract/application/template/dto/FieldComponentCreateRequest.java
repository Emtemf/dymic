package com.contract.application.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 字段组件绑定创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldComponentCreateRequest {

    @NotNull(message = "布局节点ID不能为空")
    private Long layoutNodeId;

    @NotNull(message = "字段定义ID不能为空")
    private Long fieldDefId;

    @NotBlank(message = "组件类型不能为空")
    private String componentType;   // INPUT/SELECT/DATE/MONEY/NUMBER/RADIO/CHECKBOX等

    private String labelName;       // 可选，默认使用fieldNameCn

    private String placeholder;     // 可选

    // 扩展字段（可选）
    private String requiredRule;    // JSON格式动态必填规则

    private String readonlyRule;    // JSON格式动态只读规则

    private String visibleRule;     // JSON格式动态显隐规则

    private String componentProps;  // JSON格式组件自定义属性

    // ============ 数据源绑定字段（业务友好） ============

    /**
     * 数据源类型（业务选择）：STATIC/DICT/HTTP/PLATFORM/INTERNAL
     * 业务人员看到的选项：
     * - 静态选项 → STATIC
     * - 字典数据 → DICT
     * - HTTP接口 → HTTP（IT配置）
     * - 平台接口 → PLATFORM（IT配置）
     * - 内部查询 → INTERNAL（IT配置）
     */
    private String dataSourceType;

    /**
     * 静态选项JSON（仅STATIC类型使用）
     * 格式：[{"value":"北京","label":"北京"},{"value":"上海","label":"上海"}]
     */
    private String staticOptionsJson;

    /**
     * 字典类型编码（仅DICT类型使用）
     * 如：CITY、STATUS、GENDER
     */
    private String dictType;

    /**
     * 已配置的数据提供方ID（HTTP/PLATFORM/INTERNAL类型使用，IT配置）
     */
    private Long dataProviderId;

    private Integer sortNo;         // 可选，默认0
}