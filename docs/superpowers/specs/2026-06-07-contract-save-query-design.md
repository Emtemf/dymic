# 合同表单保存与查询功能设计

## 1. 概述

### 1.1 背景
模板配置界面(config.html)已支持拖拽"保存按钮"和"查询按钮"组件，但存在以下问题：
1. 按钮拖入后无法配置属性（如绑定查询、校验规则等）
2. 查询配置功能缺失
3. 明细表操作配置缺失
4. 合同保存和查询执行的后端API缺失

### 1.2 两阶段功能

**阶段一：模板配置（业务人员操作）**
- 在config.html中配置保存按钮属性
- 配置查询按钮属性（绑定数据源、查询参数、回填规则）
- 配置明细表操作规则（增删改）
- 配置显隐/必填/只读规则

**阶段二：合同录入（员工操作）**
- 员工根据模板填写合同
- 点击保存按钮执行校验和保存
- 点击查询按钮执行查询和回填
- 操作明细表

### 1.3 目标
本设计覆盖两个阶段的完整功能。

### 1.4 范围
- 按钮属性配置（保存按钮、查询按钮）
- 查询配置CRUD
- 明细表配置CRUD
- 规则配置CRUD
- 合同CRUD API
- 查询执行API

---

## 2. 数据模型

### 2.1 已存在的表

**模板配置侧表**：
```
t_ui_action_config      - 动作配置（保存按钮、查询按钮）
t_ui_query_config       - 查询配置
t_ui_query_param        - 查询参数
t_ui_query_fill_rule    - 查询回填规则
t_ui_detail_table       - 明细表配置
```

**合同数据侧表**：
```
t_contract                    - 合同主表
t_contract_data_snapshot      - 合同数据快照
t_contract_field_value        - 合同字段值索引
t_contract_detail_row         - 合同明细行索引
t_contract_detail_field_value - 合同明细字段值索引
t_contract_search_index       - 合同搜索索引
```

### 2.2 表关系

```
Contract (合同)
  ├── template_id          -> Template
  ├── template_version_id  -> TemplateVersion
  └── current_snapshot_id  -> ContractDataSnapshot

ActionConfig (动作配置)
  ├── template_version_id  -> TemplateVersion
  ├── bind_node_id         -> LayoutNode
  └── bind_query_id        -> QueryConfig (查询按钮)

QueryConfig (查询配置)
  ├── template_version_id  -> TemplateVersion
  ├── data_provider_id     -> DataProvider
  └── fill_rules           -> QueryFillRule[]

DetailTable (明细表配置)
  └── template_version_id  -> TemplateVersion
```

---

## 3. 按钮属性配置（阶段一）

### 3.1 保存按钮属性配置

**数据库字段**（t_ui_action_config）：
```
action_type = 'SAVE_BUTTON'
action_name     - 按钮显示名称
bind_node_id    - 绑定的布局节点（保存该节点下的数据）
confirm_required - 是否需要确认弹窗
confirm_text    - 确认弹窗文本
before_rule     - 执行前规则（JSON）
after_rule      - 执行后规则（JSON）
```

**配置界面需要支持**：
- 按钮名称配置
- 绑定节点选择（保存哪个卡片/区域的数据）
- 是否需要确认弹窗
- 确认弹窗文本
- 执行前校验规则（如：检查必填字段）
- 执行后跳转规则（如：保存后跳转到列表页）

**API**：
```
POST   /api/templates/{templateId}/versions/{versionId}/action-configs
GET    /api/action-configs/{id}
PUT    /api/action-configs/{id}
DELETE /api/action-configs/{id}
```

### 3.2 查询按钮属性配置

**数据库字段**（t_ui_action_config）：
```
action_type = 'QUERY_BUTTON'
action_name     - 按钮显示名称
bind_query_id   - 绑定的查询配置ID
bind_node_id    - 绑定的布局节点（回填到该节点下的字段）
```

**配置界面需要支持**：
- 按钮名称配置
- 查询配置选择（选择已配置的查询）
- 回填目标节点选择

---

## 4. 查询配置（阶段一）

### 4.1 查询配置CRUD

**API**：
```
POST   /api/templates/{templateId}/versions/{versionId}/query-configs
GET    /api/templates/{templateId}/versions/{versionId}/query-configs
GET    /api/query-configs/{id}
PUT    /api/query-configs/{id}
DELETE /api/query-configs/{id}
```

**入参**：
```java
public class QueryConfigCreateDTO {
    private String queryCode;         // 查询编码
    private String queryName;         // 查询名称
    private Long dataProviderId;      // 数据源ID
    private String queryMode;         // 查询模式（POPUP/INLINE）
    private String resultTitle;       // 结果弹窗标题
    private List<QueryParamDTO> params;      // 查询参数
    private List<QueryFillRuleDTO> fillRules; // 回填规则
}
```

### 4.2 查询参数配置

```java
public class QueryParamDTO {
    private String paramCode;      // 参数编码
    private String paramName;      // 参数名称
    private String paramType;      // 参数类型（TEXT/SELECT/DATE）
    private String bindFieldPath;  // 绑定的表单字段路径
    private String defaultValue;   // 默认值
    private Boolean required;      // 是否必填
}
```

### 4.3 回填规则配置

```java
public class QueryFillRuleDTO {
    private String sourceField;  // 源字段（查询结果中的字段）
    private String targetPath;   // 目标路径（表单字段路径）
}
```

---

## 5. 明细表配置（阶段一）

### 5.1 明细表配置CRUD

**API**：
```
POST   /api/templates/{templateId}/versions/{versionId}/detail-tables
GET    /api/templates/{templateId}/versions/{versionId}/detail-tables
GET    /api/detail-tables/{id}
PUT    /api/detail-tables/{id}
DELETE /api/detail-tables/{id}
```

**入参**：
```java
public class DetailTableCreateDTO {
    private String detailCode;      // 明细表编码
    private String detailName;      // 明细表名称
    private String detailPath;      // 数据路径
    private Integer minRows;        // 最小行数
    private Integer maxRows;        // 最大行数
    private Boolean allowAdd;       // 是否允许增行
    private Boolean allowEdit;      // 是否允许编辑
    private Boolean allowDelete;    // 是否允许删行
    private String deleteMode;      // 删除模式（MARK_IN_DRAFT/PHYSICAL）
}
```

---

## 6. 规则配置（阶段一）

### 6.1 字段级规则配置

**已存在于 t_ui_field_component 表**：
```
required_rule    - 必填规则（JSON表达式）
readonly_rule    - 只读规则（JSON表达式）
visible_rule     - 显隐规则（JSON表达式）
```

**规则表达式示例**：
```json
{
  "type": "CONDITION",
  "condition": {
    "field": "basic.contractType",
    "operator": "EQ",
    "value": "采购"
  }
}
```

**配置界面需要支持**：
- 规则类型选择（必填/只读/显隐）
- 条件字段选择
- 条件操作符选择（等于/不等于/包含/大于等）
- 条件值配置

---

## 7. 合同CRUD API（阶段二）

### 7.1 创建/保存合同

**接口**：`POST /api/contracts/save`

**入参**：
```java
public class SaveContractCommand {
    private Long contractId;           // 可选，有则为编辑
    private Long templateId;           // 模板ID
    private Long templateVersionId;    // 模板版本ID
    private String draftData;          // 前端草稿数据(JSON)
}
```

**出参**：
```java
public class SaveContractResult {
    private Long contractId;
    private Long snapshotId;
    private Integer dataVersion;
}
```

**保存链路**（参考req.md 18.2节）：
1. 校验 template_version_id 是否仍可用
2. 根据字段定义校验必填、类型、格式
3. 归一化 detail rows（过滤 `_row_op = DELETE` 的行）
4. 生成 canonical_data
5. 写 t_contract
6. 写 t_contract_data_snapshot
7. 重建 t_contract_field_value（删除旧记录，插入新记录）
8. 重建 t_contract_detail_row
9. 重建 t_contract_detail_field_value
10. 刷新 t_contract_search_index
11. 更新 t_contract.current_snapshot_id 和 data_version

### 7.2 查询合同详情

**接口**：`GET /api/contracts/{id}`

**出参**：
```java
public class ContractDTO {
    private Long id;
    private String contractNo;
    private String contractName;
    private String contractStatus;
    
    // 模板信息
    private Long templateId;
    private String templateCode;
    private String templateName;
    private Long templateVersionId;
    private Integer versionNo;
    
    // 数据信息
    private Long currentSnapshotId;
    private String jsonData;
    
    // 审计信息
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 7.3 查询合同列表

**接口**：`GET /api/templates/{templateId}/versions/{versionId}/contracts`

**参数**：
- page: 页码
- size: 每页大小
- contractNo: 合同编号（模糊查询）
- contractStatus: 合同状态

### 7.4 删除合同

**接口**：`DELETE /api/contracts/{id}`

---

## 8. 表单校验API（阶段二）

### 8.1 校验接口

**接口**：`POST /api/templates/{templateId}/versions/{versionId}/contracts/validate`

**入参**：
```java
public class ContractValidateDTO {
    private String jsonData;  // 表单数据JSON
}
```

**出参**：
```java
public class ValidationResult {
    private boolean valid;
    private List<FieldError> errors;
    
    public static class FieldError {
        private String fieldPath;    // 字段路径
        private String fieldName;    // 字段名称
        private String errorCode;    // 错误码
        private String errorMessage; // 错误信息
    }
}
```

### 8.2 校验规则来源

从 `t_ui_field_def` 和 `t_ui_field_component` 获取：
- `requiredDefault` - 是否必填
- `dataType` - 数据类型校验（TEXT、NUMBER、MONEY、DATE）
- `requiredRule` - 动态必填规则（JSON表达式）
- `validateRule` - 自定义校验规则

### 8.3 校验逻辑

1. 解析模板版本的Schema
2. 遍历所有字段定义
3. 检查必填校验
4. 检查数据类型校验
5. 检查动态规则校验

---

## 9. 查询执行API（阶段二）

### 9.1 执行查询

**接口**：`POST /api/queries/{queryId}/execute`

**入参**：
```java
public class QueryExecuteDTO {
    private Map<String, Object> params;  // 查询参数
}
```

**出参**：
```java
public class QueryExecuteResult {
    private boolean success;
    private List<Map<String, Object>> data;  // 查询结果列表
    private String message;
}
```

### 9.2 执行流程

1. 获取查询配置
2. 获取数据源配置
3. 组装查询参数
4. 调用数据源（HTTP/字典/静态等）
5. 解析返回结果
6. 返回统一格式数据

---

## 10. 字段回填API（阶段二）

### 10.1 执行回填

**接口**：`POST /api/contracts/{contractId}/fill-fields`

**入参**：
```java
public class FillFieldsDTO {
    private Long queryId;
    private Map<String, Object> selectedData;  // 用户选择的数据
}
```

**出参**：
```java
public class FillFieldsResult {
    private boolean success;
    private List<FilledField> filledFields;
    
    public static class FilledField {
        private String path;   // 字段路径
        private Object value;  // 填充值
    }
}
```

### 10.2 回填流程

1. 获取查询配置的回填规则
2. 遍历回填规则
3. 从 selectedData 中提取源字段值
4. 映射到目标字段路径
5. 返回回填结果（前端负责更新表单）

---

## 11. 需要创建的文件

### 11.1 领域层

```
domain/contract/
├── model/
│   ├── Contract.java
│   ├── ContractSnapshot.java
│   ├── ContractFieldValue.java
│   └── ContractDetailRow.java
└── repository/
    ├── ContractRepository.java
    ├── ContractSnapshotRepository.java
    └── ContractFieldValueRepository.java

domain/query/
├── model/
│   ├── QueryConfig.java
│   ├── QueryParam.java
│   └── QueryFillRule.java
└── repository/
    └── QueryConfigRepository.java

domain/detail/
├── model/
│   └── DetailTable.java
└── repository/
    └── DetailTableRepository.java
```

### 11.2 基础设施层

```
infrastructure/persistence/
├── entity/
│   ├── ContractEntity.java
│   ├── ContractSnapshotEntity.java
│   ├── ContractFieldValueEntity.java
│   ├── ContractDetailRowEntity.java
│   ├── QueryConfigEntity.java
│   ├── QueryParamEntity.java
│   ├── QueryFillRuleEntity.java
│   └── DetailTableEntity.java
├── mapper/
│   ├── ContractMapper.java
│   ├── ContractSnapshotMapper.java
│   ├── ContractFieldValueMapper.java
│   ├── ContractDetailRowMapper.java
│   ├── QueryConfigMapper.java
│   ├── QueryParamMapper.java
│   ├── QueryFillRuleMapper.java
│   └── DetailTableMapper.java
└── repository/
    ├── ContractRepositoryImpl.java
    ├── ContractSnapshotRepositoryImpl.java
    ├── ContractFieldValueRepositoryImpl.java
    ├── ContractDetailRowRepositoryImpl.java
    ├── QueryConfigRepositoryImpl.java
    └── DetailTableRepositoryImpl.java
```

### 11.3 应用层

```
application/contract/
├── ContractService.java
├── ContractValidator.java
├── ContractIndexBuilder.java
└── dto/
    ├── SaveContractCommand.java
    ├── SaveContractResult.java
    ├── ContractDTO.java
    ├── ContractValidateDTO.java
    └── ValidationResult.java

application/query/
├── QueryConfigService.java
├── QueryExecuteService.java
└── dto/
    ├── QueryConfigCreateDTO.java
    ├── QueryConfigDTO.java
    ├── QueryParamDTO.java
    ├── QueryFillRuleDTO.java
    ├── QueryExecuteDTO.java
    ├── QueryExecuteResult.java
    ├── FillFieldsDTO.java
    └── FillFieldsResult.java

application/detail/
├── DetailTableService.java
└── dto/
    ├── DetailTableCreateDTO.java
    └── DetailTableDTO.java
```

### 11.4 接口层

```
adapter/controller/
├── ContractController.java
├── QueryConfigController.java
├── QueryExecuteController.java
└── DetailTableController.java
```

---

## 12. 实现顺序

### 阶段1：按钮和查询配置（优先）
1. 实现 ActionConfigService CRUD
2. 实现 QueryConfigService CRUD
3. 实现 QueryParamService
4. 实现 QueryFillRuleService
5. 更新 config.html 支持按钮属性编辑

### 阶段2：明细表配置
1. 实现 DetailTableService CRUD
2. 更新 config.html 支持明细表配置

### 阶段3：规则配置
1. 实现规则编辑器组件
2. 更新 config.html 支持规则配置

### 阶段4：合同CRUD
1. 创建 Contract Entity/Mapper/Repository
2. 创建 ContractSnapshot Entity/Mapper/Repository
3. 实现 ContractService.save() 基础保存
4. 实现 ContractService.getById()
5. 实现 ContractService.list()
6. 实现 ContractService.delete()

### 阶段5：表单校验
1. 实现 ContractValidator.validate()
2. 集成到 ContractService.save()

### 阶段6：索引构建
1. 实现 ContractIndexBuilder（重建字段值索引）
2. 实现 DetailRow 索引构建
3. 集成到 ContractService.save()

### 阶段7：查询执行
1. 实现 QueryExecuteService
2. 实现数据源调用（HTTP/字典/静态）
3. 实现字段回填逻辑

---

## 13. 参考文档

- req/req.md 第18节：保存和回显规则
- req/req.md 第21节：代码示例
- req/req.md 第22节：接口建议
