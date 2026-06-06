package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import java.util.Map;

/**
 * 动作配置 DTO
 */
@Data
@Builder
public class ActionConfigDTO {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private String actionCode;
    private String actionName;
    private String actionType;
    private Long bindNodeId;
    private Long bindQueryId;
    private Integer confirmRequired;
    private String confirmText;
    private String beforeRule;
    private String afterRule;
    private String propsJson;
    private Integer sortNo;
}