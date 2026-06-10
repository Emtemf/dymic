package com.contract.infrastructure.persistence.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.OffsetDateTime;

/**
 * 查询配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryConfigEntity {
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
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer isDeleted;
}
