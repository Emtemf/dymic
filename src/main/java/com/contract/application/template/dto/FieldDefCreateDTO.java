package com.contract.application.template.dto;

import lombok.Data;
import java.util.List;

@Data
public class FieldDefCreateDTO {
    private Long layoutNodeId;
    private String fieldNameCn;
    private String dataType;
    private Boolean required;
    private String placeholder;
    private Integer sortNo;

    private String dataSourceType;
    private List<StaticOption> staticOptions;
    private Long dataProviderId;

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
