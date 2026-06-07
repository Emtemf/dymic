package com.contract.application.template.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 布局节点创建 DTO
 */
@Data
public class LayoutNodeCreateDTO {
    private Long parentId;

    @NotBlank(message = "节点类型不能为空")
    private String nodeType;

    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    private Integer sortNo;        // 默认 0
    private Integer levelNo;       // 默认 1

    // Grid 布局属性
    private Integer gridX;
    private Integer gridY;
    private Integer gridW;
    private Integer gridH;
    private Integer rowNo;
    private Integer colNo;
    private Integer colSpan;
    private Integer rowSpan;

    // 绑定属性
    private String bindType;
    private Long bindRefId;

    // 规则配置
    private String visibleRule;
    private String readonlyRule;
    private String propsJson;
}