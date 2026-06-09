package com.contract.infrastructure.persistence.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动作配置实体
 * Corresponds to table: t_ui_action_config
 */
@Data
public class ActionConfigEntity {

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
