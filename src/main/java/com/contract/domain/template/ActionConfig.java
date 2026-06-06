package com.contract.domain.template;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 动作配置领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionConfig {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private String actionCode;
    private String actionName;
    private String actionType; // SAVE/QUERY/CANCEL/CUSTOM
    private Long bindNodeId;
    private Long bindQueryId;
    private Integer confirmRequired;
    private String confirmText;
    private Map<String, Object> beforeRule;
    private Map<String, Object> afterRule;
    private Map<String, Object> propsJson;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}