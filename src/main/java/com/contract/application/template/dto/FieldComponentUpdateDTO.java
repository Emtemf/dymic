package com.contract.application.template.dto;

import lombok.Data;
import java.util.Map;

/**
 * 字段组件绑定更新 DTO
 */
@Data
public class FieldComponentUpdateDTO {
    private String labelName;
    private String placeholder;
    private Map<String, Object> componentProps;
    private Map<String, Object> requiredRule;
}