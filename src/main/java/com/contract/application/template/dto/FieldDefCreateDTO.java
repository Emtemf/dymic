package com.contract.application.template.dto;

import lombok.Data;
import java.util.List;

/**
 * 创建字段定义 DTO
 */
@Data
public class FieldDefCreateDTO {
    private Long layoutNodeId;
    private String displayName;         // 业务名称: 合同名称
    private String componentType;       // INPUT/SELECT/DATE/MONEY/NUMBER
    private Boolean required;           // 是否必填
    private String placeholder;         // 输入提示
    private Integer sortNo;             // 排序号

    // 组件配置(用于 SELECT 类型)
    private String dataSourceType;      // STATIC/PROVIDER
    private List<StaticOption> staticOptions;  // 静态选项
    private Long dataProviderId;        // 数据提供方 ID

    @Data
    public static class StaticOption {
        private String value;
        private String label;
        private Integer sortNo;

        public StaticOption() {}

        public StaticOption(String value, String label, Integer sortNo) {
            this.value = value;
            this.label = label;
            this.sortNo = sortNo;
        }
    }
}