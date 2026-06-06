package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;

/**
 * 创建字段定义结果(一体化:返回 FieldDef 和 FieldComponent)
 */
@Data
@Builder
public class FieldDefCreateResult {
    private FieldDefDTO fieldDef;
    private FieldComponentDTO fieldComponent;
}