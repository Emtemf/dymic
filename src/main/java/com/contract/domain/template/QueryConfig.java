package com.contract.domain.template;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 查询配置聚合根
 *
 * ===== 领域统一业务语言 =====
 *
 * 【聚合边界】
 * - QueryConfig是聚合根
 * - 包含实体：QueryParam、QueryFillRule
 *
 * 【业务规则】
 * 1. 查询类型必须有效
 * 2. 数据提供方必须存在
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryConfig {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private String queryCode;
    private String queryName;
    private String queryType;
    private Long dataProviderId;
    private String triggerType;
    private String resultMode;
    private Long bindNodeId;
    private Integer pageSize;
    private String propsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
