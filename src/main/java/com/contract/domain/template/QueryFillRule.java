package com.contract.domain.template;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 查询回填规则领域对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryFillRule {
    private Long id;
    private Long queryConfigId;
    private String sourceField;
    private String targetScope;
    private String targetPath;
    private String fillMode;
    private String transformJson;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
