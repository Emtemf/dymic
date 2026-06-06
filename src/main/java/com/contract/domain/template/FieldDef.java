package com.contract.domain.template;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字段定义领域模型
 */
@Data
public class FieldDef {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long layoutNodeId;
    private String fieldCode;         // 自动生成: contractName
    private String fieldPath;         // 自动生成: basicInfo.contractName
    private String fieldNameCn;       // 业务名称: 合同名称
    private String dataType;          // TEXT/NUMBER/DATE
    private Integer requiredDefault;  // 0=可选, 1=必填
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FieldDef create(String displayName, String dataType) {
        FieldDef fieldDef = new FieldDef();
        fieldDef.setFieldNameCn(displayName);
        fieldDef.setDataType(dataType);
        return fieldDef;
    }
}