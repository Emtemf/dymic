package com.contract.application.template.dto;

import lombok.Data;
import java.util.List;

@Data
public class SchemaSaveDTO {
    private List<LayoutNodeSaveItem> layoutNodes;
    private List<FieldDefSaveItem> fieldDefs;
    private List<FieldComponentSaveItem> fieldComponents;
    private List<QueryConfigSaveItem> queryConfigs;
    private List<ActionConfigSaveItem> actionConfigs;

    @Data
    public static class LayoutNodeSaveItem {
        private Long id;
        private Long parentId;
        private String nodeCode;
        private String nodeName;
        private String nodeType;
        private Integer sortNo;
        private Integer levelNo;
        private Integer gridX;
        private Integer gridY;
        private Integer gridW;
        private Integer gridH;
        private Integer rowNo;
        private Integer colNo;
        private Integer colSpan;
        private Integer rowSpan;
        private String bindType;
        private Long bindRefId;
        private String visibleRule;
        private String readonlyRule;
        private String propsJson;
    }

    @Data
    public static class FieldDefSaveItem {
        private Long id;
        private String fieldCode;
        private String fieldPath;
        private String fieldNameCn;
        private String dataType;
        private String valueType;
        private Integer requiredDefault;
        private Integer searchable;
        private Integer indexable;
        private String defaultValue;
        private String validateRule;
        private String propsJson;
    }

    @Data
    public static class FieldComponentSaveItem {
        private Long id;
        private Long layoutNodeId;
        private Long fieldDefId;
        private String componentType;
        private String labelName;
        private String placeholder;
        private String requiredRule;
        private String readonlyRule;
        private String visibleRule;
        private String componentProps;
        private Long dataProviderId;
        private Integer sortNo;
    }

    @Data
    public static class QueryConfigSaveItem {
        private String queryCode;
        private String queryName;
        private String queryType;
        private Long dataProviderId;
    }

    @Data
    public static class ActionConfigSaveItem {
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
    }
}
