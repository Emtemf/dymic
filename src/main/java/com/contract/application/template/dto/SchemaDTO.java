package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 完整配置树DTO（用于前端渲染）
 * Schema API返回的完整配置聚合对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaDTO {
    private Long templateId;
    private Long templateVersionId;
    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String bizType;
    private Integer versionNo;
    private String versionName;
    private String versionStatus;

    // 完整配置树（嵌套树结构）
    private List<LayoutNodeTreeDTO> layoutNodes;

    // 平铺列表（便于快速访问）
    private List<FieldDefDTO> fieldDefs;
    private List<FieldComponentDTO> fieldComponents;
    private List<QueryConfigDTO> queryConfigs;
    private List<ActionConfigDTO> actionConfigs;
    private List<DataSourceQueryDTO> dataSources;
}
