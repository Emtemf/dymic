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
    private Map<String, Object> beforeRule;
    private Map<String, Object> afterRule;
    private Map<String, Object> propsJson;
    private Integer sortNo;
}