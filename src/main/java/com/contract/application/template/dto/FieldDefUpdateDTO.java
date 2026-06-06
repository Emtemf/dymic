package com.contract.application.template.dto;

import lombok.Data;

/**
 * 更新字段定义 DTO
 */
@Data
public class FieldDefUpdateDTO {
    private String fieldNameCn;
    private Integer requiredDefault;
}