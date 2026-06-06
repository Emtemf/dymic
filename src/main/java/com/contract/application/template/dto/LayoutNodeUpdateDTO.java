package com.contract.application.template.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 布局节点更新 DTO
 */
@Data
public class LayoutNodeUpdateDTO {
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;
    private Integer sortNo;

    // Grid 布局属性
    private Integer gridX;
    private Integer gridY;
    private Integer gridW;
    private Integer gridH;
    private Integer rowNo;
    private Integer colNo;
    private Integer colSpan;
    private Integer rowSpan;

    // 规则配置
    private String visibleRule;
    private String readonlyRule;
    private String propsJson;
}