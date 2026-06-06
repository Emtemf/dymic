# 合同模板配置界面设计规范

**日期**: 2026-06-07  
**版本**: V1.0  
**状态**: 已批准  

---

## 1. 系统定位

### 1.1 配置界面是什么

**核心定位**: 开发验证工具，用于验证后端API功能是否正确

- ✅ 验证后端API功能是否正确的工具
- ✅ 开发人员自己验证配置流程
- ✅ 验证数据入库是否正确
- ✅ 验证动态渲染是否正确
- ✅ 相对简单，重点是功能完整

### 1.2 配置界面不是什么

**不在范围内的功能**:

- ❌ 不是低代码平台（不提供拖拽设计器给用户）
- ❌ 不是审批系统（审批在其他系统）
- ❌ 不是权限系统（权限在其他系统）
- ❌ 不是用户管理（用户中心负责）
- ❌ 不交付给业务用户作为正式配置工具

### 1.3 核心验证闭环

**V1.0必须验证的完整闭环**:

```
步骤1: config.html 配置 → 添加卡片、字段、按钮（实时保存）
步骤2: 后端API保存 → POST layout-nodes、field-defs、action-configs
步骤3: data-viewer.html 验证 → 查看数据库JSON，确认自动生成正确
步骤4: preview.html 预览 → GET schema API，动态渲染表单界面
```

**核心验证目标**: 配置数据正确入库 → 配置树正确读取 → 动态渲染正确显示

---

## 2. 三个界面定位

### 2.1 config.html — 配置主界面

**用途**: 业务人员配置模板结构（开发验证）

**布局**: 三栏式布局
- **左侧**: 组件库（可添加的组件列表）
- **中间**: 配置画布（卡片容器 + 字段列表）
- **右侧**: 属性面板（当前选中组件的属性配置）

**保存策略**: 实时保存（500ms延迟）+ 内存预览优化
- 每次操作立即调用API保存（500ms防抖延迟）
- 同时更新内存配置树 + 调用API验证
- 配置不丢失 + 预览无延迟 + API实时验证

**用户**: 业务配置人员（开发验证）

### 2.2 preview.html — 独立预览界面

**用途**: 验证已保存配置的渲染效果

**流程**:
1. GET /api/templates/{id}/versions/{vid}/schema → 获取完整配置树
2. 前端动态渲染：
   - 遍历 layoutNodes → 渲染卡片容器
   - 遍历 children → 渲染字段组件
   - 渲染 actionConfigs → 动作按钮

**用户**: 开发验证用

### 2.3 data-viewer.html — 数据验证界面

**用途**: 验证配置是否正确入库

**布局**: 三列展示
- **列1**: 布局节点（t_ui_layout_node）
- **列2**: 字段定义（t_ui_field_def）
- **列3**: 字段组件绑定（t_ui_field_component）

**验证内容**:
- nodeCode/nodePath 自动生成（拼音转换）
- fieldCode/fieldPath 自动生成（拼音 + 父节点路径）
- JSONB字段存储正确
- 完整配置树JSON结构正确

**用户**: 开发验证用

---

## 3. config.html 详细设计

### 3.1 三栏布局

#### 3.1.1 左侧组件库

**分类展示**:

**基础组件**:
- 文本框（TEXT_INPUT）
- 数字框（NUMBER_INPUT）
- 金额框（MONEY_INPUT）
- 日期选择（DATE_PICKER）
- 下拉选择框（SELECT）

**布局组件**:
- 卡片容器（CARD_CONTAINER）
- 分隔线（SEPARATOR）

**按钮组件**:
- 保存按钮（SAVE_BUTTON）
- 查询按钮（QUERY_BUTTON）
- 自定义按钮（CUSTOM_BUTTON）

**交互**: 点击组件 → 添加到配置画布（卡片内部）

#### 3.1.2 中间配置画布

**卡片容器显示**:

```
┌─────────────────────────────────────┐
│ 卡片1：基本信息                       │
├─────────────────────────────────────┤
│ 字段1：合同名称 [文本框]              │
│ 字段2：合同金额 [金额框]              │
│ 字段3：签订日期 [日期选择]            │
│ 字段4：币种      [下拉框]             │
│                                     │
│ [保存按钮]  [查询按钮]                │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ 卡片2：合同条款                       │
├─────────────────────────────────────┤
│ 字段5：合同条款 [文本框]              │
│                                     │
│ [保存按钮]                           │
└─────────────────────────────────────┘
```

**交互**:
- 点击卡片 → 选中卡片，右侧显示卡片属性
- 点击字段 → 选中字段，右侧显示字段属性
- 点击按钮 → 选中按钮，右侧显示按钮属性

#### 3.1.3 右侧属性面板

**动态显示**: 根据当前选中组件类型显示不同属性配置

### 3.2 字段属性面板（通用）

**基本信息**:
- 字段名称（labelName）
- 是否必填（requiredDefault: true/false）
- 提示文字（placeholder）

**数据类型**:
- 文本（dataType: TEXT）
- 数字（dataType: NUMBER）
- 金额（dataType: MONEY）
- 日期（dataType: DATE）

**字段路径**（自动生成，不可编辑）:
- fieldCode: 拼音转换（合同名称 → contractName）
- fieldPath: 父节点路径拼接（card1.contractName）

### 3.3 下拉框特有配置（业务友好设计）

**业务人员看到的界面**:

```
选择数据源：
┌─────────────────────────────────────┐
│ 币种列表                             │ ← IT配置的数据源
│ 合同类型                             │ ← IT配置的数据源
│ 供应商列表                           │ ← IT配置的数据源
│ 自定义选项（自己输入）               │ ← 前端添加的特殊选项
└─────────────────────────────────────┘
```

**选择"自定义选项"后**:

```
输入选项列表（每行一个）：
┌─────────────────────────────────────┐
│ CNY - 人民币                         │
│ USD - 美元                           │
│ EUR - 欧元                           │
└─────────────────────────────────────┘
```

**后台实现逻辑**:

1. **数据源列表API**:
   ```
   GET /api/data-providers
   返回：
   [
     { "id": 1, "name": "币种列表", "code": "CURRENCY" },
     { "id": 2, "name": "合同类型", "code": "CONTRACT_TYPE" },
     { "id": 3, "name": "供应商列表", "code": "SUPPLIER" }
   ]
   ```

2. **前端动态添加**:
   - 在数据源列表末尾添加"自定义选项（自己输入）"
   - 选择该选项后，显示textarea输入框

3. **自定义选项保存逻辑**:
   ```
   后端自动创建临时DataProvider：
   • providerCode: field_{fieldId}_static
   • providerName: {字段名称}_静态选项
   • providerType: STATIC
   • configJson: {"options": ["CNY - 人民币", "USD - 美元", "EUR - 欧元"]}
   ```

### 3.4 查询功能预留

**字段查询预留**（每个字段可配置）:

```
是否需要查询：
┌─────────────────────────────────────┐
│ 否                                   │
│ 是（字段旁边显示查询按钮）           │
└─────────────────────────────────────┘
```

**表格查询预留**（后续阶段实现）:
- 配置位置：卡片容器属性面板
- 功能：查询按钮触发弹窗，显示表格查询结果
- 数据源：绑定DataProvider（查询型）

### 3.5 保存策略

**实时保存机制**:

1. **自动保存**: 500ms延迟，避免频繁请求
   ```
   用户操作 → 500ms延迟 → POST API → 更新数据库
   ```

2. **乐观更新**: 先更新前端UI，API失败后再回滚
   ```
   用户操作 → 更新UI → POST API → 成功/失败
   成功：保持UI状态
   失败：回滚UI状态 + 显示错误提示
   ```

3. **实时预览**: 每次保存成功后，刷新右侧预览
   ```
   POST API成功 → GET schema API → 更新预览渲染
   ```

**API调用**:
- 添加卡片: POST /api/layout-nodes
- 添加字段: POST /api/field-defs + POST /api/field-components
- 添加按钮: POST /api/action-configs
- 更新属性: PUT /api/field-defs/{id} + PUT /api/field-components/{id}

---

## 4. preview.html 详细设计

### 4.1 渲染流程

**步骤1**: 获取配置树
```
GET /api/templates/{id}/versions/{vid}/schema
返回：
{
  "layoutNodes": [
    {
      "id": 1,
      "nodeCode": "card1",
      "nodeName": "基本信息",
      "nodeType": "CARD",
      "children": [
        {
          "id": 2,
          "fieldCode": "contractName",
          "fieldName": "合同名称",
          "dataType": "TEXT",
          "requiredDefault": true,
          "component": {
            "componentType": "TEXT_INPUT",
            "placeholder": "请输入合同名称"
          }
        },
        ...
      ]
    }
  ],
  "actionConfigs": [...]
}
```

**步骤2**: 前端动态渲染

**卡片容器渲染**:
```
遍历 layoutNodes → 渲染卡片容器（卡片标题 + 字段列表）
```

**字段组件渲染**:
```
遍历 children → 根据 componentType 渲染对应组件
- TEXT_INPUT → 文本框
- MONEY_INPUT → 金额框（带货币符号）
- DATE_PICKER → 日期选择器
- SELECT → 下拉框（绑定DataProvider）
```

**动作按钮渲染**:
```
遍历 actionConfigs → 渲染按钮
- SAVE_BUTTON → 保存按钮
- QUERY_BUTTON → 查询按钮
```

### 4.2 渲染验证点

**卡片容器验证**:
- 卡片标题正确显示（基本信息、合同条款）
- 卡片内字段列表正确显示

**字段组件验证**:
- 字段标签正确显示（合同名称、合同金额、签订日期、币种）
- 必填标识正确显示（合同名称、币种显示红色*号）
- 组件类型正确（文本框、金额框、日期、下拉框）
- placeholder正确显示

**数据源绑定验证**:
- 币种下拉框绑定"币种列表"DataProvider
- 下拉选项正确显示（CNY、USD、EUR）

**动作按钮验证**:
- 保存按钮正确显示
- 查询按钮正确显示（如果配置了查询功能）

---

## 5. data-viewer.html 详细设计

### 5.1 三列布局

#### 5.1.1 列1：布局节点（t_ui_layout_node）

**显示内容**:
- nodeCode（自动生成，拼音转换）
- nodePath（自动生成，父节点路径拼接）
- nodeName（卡片名称）
- nodeType（CARD）

**验证点**:
- nodeCode正确生成（基本信息 →基本信息）
- nodePath正确生成（root.basicInfo）
- nodeType正确（CARD）

#### 5.1.2 列2：字段定义（t_ui_field_def）

**显示内容**:
- fieldCode（自动生成，拼音转换）
- fieldPath（自动生成，父节点路径拼接）
- fieldName（字段名称）
- dataType（TEXT、NUMBER、MONEY、DATE）
- requiredDefault（true/false）

**验证点**:
- fieldCode正确生成（合同名称 → contractName）
- fieldPath正确生成（root.basicInfo.contractName）
- dataType正确
- requiredDefault正确

#### 5.1.3 列3：字段组件绑定（t_ui_field_component）

**显示内容**:
- componentType（TEXT_INPUT、SELECT等）
- labelName（字段标签）
- placeholder（提示文字）
- dataProviderId（数据源绑定）
- configJson（JSONB存储，下拉框特有）

**验证点**:
- componentType正确
- labelName正确
- placeholder正确
- dataProviderId正确（币种下拉框绑定DataProvider ID）
- configJson正确存储（JSONB格式）

### 5.2 完整配置树JSON

**显示位置**: 页面底部

**API**: GET /api/templates/{id}/versions/{vid}/schema

**验证内容**:
- layoutNodes嵌套结构正确
- children包含字段列表
- actionConfigs包含动作按钮
- JSON格式正确（无语法错误）

---

## 6. 第一阶段边界

### 6.1 必须包含

- ✅ 所有基础组件（文本、数字、日期、下拉、金额）
- ✅ 卡片容器布局
- ✅ 保存按钮、查询按钮
- ✅ 从上到下堆积布局
- ✅ 实时保存 + 内存预览优化
- ✅ 查询功能预留（字段查询、表格查询）
- ✅ 数据入库验证界面

### 6.2 后续阶段

- ⏳ 明细表组件（行列操作复杂）
- ⏳ 拖拽排序功能
- ⏳ 规则配置（显隐、必填、只读）
- ⏳ 查询弹窗配置
- ⏳ 外部数据集成
- ⏳ 合同录入功能

### 6.3 不在范围内

- ❌ 工作流审批（审批系统负责）
- ❌ 多人协作草稿（协作系统负责）
- ❌ 多租户隔离（平台层负责）
- ❌ 权限管理（权限系统负责）
- ❌ 用户管理（用户中心负责）
- ❌ Redis缓存、MQ消息队列

---

## 7. 技术实现要点

### 7.1 自动生成字段路径

**nodeCode生成规则**:
- 卡片名称 → 拼音转换（基本信息 →基本信息）
- 全小写，无空格

**nodePath生成规则**:
- 父节点路径拼接（root.basicInfo）
- 层级结构：root → 卡片 → 字段

**fieldCode生成规则**:
- 字段名称 → 拼音转换（合同名称 → contractName）
- 全小写，无空格

**fieldPath生成规则**:
- 父节点路径拼接（root.basicInfo.contractName）
- 层级结构：root → 卡片 → 字段

### 7.2 JSONB存储

**字段类型**: String（Java）

**存储示例**:
```json
{
  "componentType": "SELECT",
  "dataProviderId": 1,
  "options": null
}
```

**自定义选项存储**:
```json
{
  "componentType": "SELECT",
  "dataProviderId": null,
  "options": ["CNY - 人民币", "USD - 美元", "EUR - 欧元"]
}
```

### 7.3 DataProvider处理

**IT配置的数据源**:
- DataProvider表中已存在记录
- 前端选择后，保存dataProviderId

**自定义选项**:
- 后端自动创建临时DataProvider
- providerCode: field_{fieldId}_static
- providerType: STATIC
- configJson存储选项列表

---

## 8. 验证清单

### 8.1 config.html验证

- [ ] 组件库显示正确（基础、布局、按钮）
- [ ] 点击组件添加到配置画布
- [ ] 卡片容器正确显示
- [ ] 字段列表正确显示
- [ ] 属性面板动态切换
- [ ] 字段属性保存正确
- [ ] 下拉框数据源选择正确
- [ ] 自定义选项输入正确
- [ ] 实时保存生效（500ms延迟）
- [ ] 查询功能预留显示

### 8.2 preview.html验证

- [ ] GET schema API成功
- [ ] 卡片容器渲染正确
- [ ] 字段组件渲染正确
- [ ] 必填标识显示正确
- [ ] 数据源绑定正确
- [ ] 下拉选项显示正确
- [ ] 动作按钮渲染正确

### 8.3 data-viewer.html验证

- [ ] 布局节点显示正确
- [ ] 字段定义显示正确
- [ ] 字段组件绑定显示正确
- [ ] nodeCode/nodePath自动生成正确
- [ ] fieldCode/fieldPath自动生成正确
- [ ] JSONB存储正确
- [ ] 完整配置树JSON正确

---

## 9. 附录：API接口清单

### 9.1 配置保存API

**布局节点**:
- POST /api/layout-nodes
- PUT /api/layout-nodes/{id}
- DELETE /api/layout-nodes/{id}

**字段定义**:
- POST /api/field-defs
- PUT /api/field-defs/{id}
- DELETE /api/field-defs/{id}

**字段组件绑定**:
- POST /api/field-components
- PUT /api/field-components/{id}
- DELETE /api/field-components/{id}

**动作配置**:
- POST /api/action-configs
- PUT /api/action-configs/{id}
- DELETE /api/action-configs/{id}

### 9.2 数据源API

**数据源列表**:
- GET /api/data-providers

**自定义数据源创建**:
- POST /api/data-providers（后端自动创建临时DataProvider）

### 9.3 配置树API

**获取完整配置树**:
- GET /api/templates/{id}/versions/{vid}/schema

**返回结构**:
```json
{
  "layoutNodes": [
    {
      "id": 1,
      "nodeCode": "basicInfo",
      "nodeName": "基本信息",
      "nodeType": "CARD",
      "nodePath": "root.basicInfo",
      "children": [
        {
          "id": 2,
          "fieldCode": "contractName",
          "fieldName": "合同名称",
          "fieldPath": "root.basicInfo.contractName",
          "dataType": "TEXT",
          "requiredDefault": true,
          "component": {
            "componentType": "TEXT_INPUT",
            "labelName": "合同名称",
            "placeholder": "请输入合同名称",
            "dataProviderId": null,
            "configJson": null
          }
        }
      ]
    }
  ],
  "actionConfigs": [
    {
      "id": 10,
      "actionType": "SAVE",
      "actionName": "保存合同",
      "layoutNodeId": 1
    }
  ]
}
```

---

## 10. 设计决策记录

### 10.1 为什么选择实时保存？

**决策**: 实时保存（500ms延迟）+ 内存预览优化

**原因**:
- 配置不丢失（随时保存到数据库）
- 实时验证API正确性（每次操作都调用API）
- 预览无延迟（内存配置树 + API验证）
- 支持撤销/重做（后续阶段）
- 支持多设备协同（后续阶段）

### 10.2 为什么选择业务友好的数据源设计？

**决策**: 业务人员只看到"选择数据源"下拉框

**原因**:
- 业务人员不懂技术概念（数据来源、静态选项、数据提供方）
- 降低理解成本（直接选择就行）
- 后端自动处理技术细节（创建临时DataProvider）
- 符合业务直觉（IT配置 + 自己输入）

### 10.3 为什么预留查询功能？

**决策**: 每个字段可配置"是否需要查询"

**原因**:
- V1.0不实现查询功能（复杂度较高）
- 预留配置字段（queryEnabled）
- 后续阶段实现查询弹窗
- 数据源绑定DataProvider（查询型）

---

## 11. 扩展字段设计（预留复杂业务）

### 11.1 设计原则

- **预留扩展性**：保留扩展字段用于复杂业务场景
- **渐进式使用**：V1.0不使用，后续版本按需启用
- **JSON灵活性**：规则和属性使用JSON格式，支持灵活扩展
- **schema优先**：扩展字段保留在schema中，实现时可暂不使用

### 11.2 LayoutNode扩展字段

| 字段 | 类型 | 说明 | V1.0使用 | 后续阶段 |
|-----|------|------|---------|---------|
| grid_x | INTEGER | Grid布局X坐标 | ❌ 不使用 | V1.1+ Grid布局 |
| grid_y | INTEGER | Grid布局Y坐标 | ❌ 不使用 | V1.1+ Grid布局 |
| grid_w | INTEGER | Grid布局宽度 | ❌ 不使用 | V1.1+ Grid布局 |
| grid_h | INTEGER | Grid布局高度 | ❌ 不使用 | V1.1+ Grid布局 |
| row_no | INTEGER | 表格行号 | ❌ 不使用 | V1.2+ 明细表 |
| col_no | INTEGER | 表格列号 | ❌ 不使用 | V1.2+ 明细表 |
| col_span | INTEGER | 跨列数 | ❌ 不使用 | V1.1+ Grid布局 |
| row_span | INTEGER | 跨行数 | ❌ 不使用 | V1.1+ Grid布局 |
| bind_type | VARCHAR(50) | 绑定类型（DATASOURCE/QUERY/FIELD） | ❌ 不使用 | V1.2+ 动态绑定 |
| bind_ref_id | BIGINT | 绑定对象ID | ❌ 不使用 | V1.2+ 动态绑定 |
| visible_rule | JSON | 显隐规则配置 | ❌ 不使用 | V1.2+ 规则引擎 |
| readonly_rule | JSON | 只读规则配置 | ❌ 不使用 | V1.2+ 规则引擎 |
| props_json | JSON | 扩展属性 | ❌ 不使用 | V1.1+ 自定义属性 |

**Grid布局字段用途**：
- `grid_x/y/w/h`：CSS Grid定位，用于复杂表单布局
- `row_no/col_no`：表格布局定位，用于明细表场景
- `col_span/row_span`：跨列/跨行合并，用于特殊布局需求

**绑定字段用途**：
- `bind_type`：节点可绑定数据源/查询/字段，实现动态数据注入
- `bind_ref_id`：关联DataProvider或QueryConfig的ID

**规则字段用途**：
- `visible_rule`：动态显隐规则，如"当字段A=X时显示此卡片"
- `readonly_rule`：动态只读规则，如"当状态=PUBLISHED时卡片只读"

### 11.3 FieldDef扩展字段

| 字段 | 类型 | 说明 | V1.0使用 | 后续阶段 |
|-----|------|------|---------|---------|
| detail_table_id | BIGINT | 明细表关联ID | ❌ 不使用 | V1.2+ 明细表 |
| searchable | SMALLINT | 是否可搜索（0/1） | ❌ 不使用 | V1.3+ 搜索功能 |
| indexable | SMALLINT | 是否建索引（0/1） | ❌ 不使用 | V1.3+ 搜索功能 |
| search_index_column | VARCHAR(100) | 搜索索引列名 | ❌ 不使用 | V1.3+ 搜索功能 |
| validate_rule | JSON | 校验规则配置 | ❌ 不使用 | V1.2+ 校验引擎 |
| props_json | JSON | 扩展属性 | ❌ 不使用 | V1.1+ 自定义属性 |

**明细表关联用途**：
- `detail_table_id`：字段属于某个明细表时，关联明细表配置

**搜索预留用途**：
- `searchable`：标记字段是否需要搜索
- `indexable`：标记字段是否需要索引
- `search_index_column`：指定索引列名（如`value_text`、`value_number`）

**校验规则用途**：
- `validate_rule`：复杂校验配置，如正则表达式、范围校验、自定义校验

```json
// validate_rule 示例
{
  "type": "REGEX",
  "pattern": "^[A-Z]{2}\\d{6}$",
  "message": "请输入正确的编码格式"
}
```

### 11.4 FieldComponent扩展字段

| 字段 | 类型 | 说明 | V1.0使用 | 后续阶段 |
|-----|------|------|---------|---------|
| required_rule | JSON | 必填规则配置 | ❌ 不使用 | V1.2+ 规则引擎 |
| readonly_rule | JSON | 只读规则配置 | ❌ 不使用 | V1.2+ 规则引擎 |
| visible_rule | JSON | 显隐规则配置 | ❌ 不使用 | V1.2+ 规则引擎 |
| component_props | JSON | 组件自定义属性 | ❌ 不使用 | V1.1+ 组件扩展 |
| data_provider_id | BIGINT | 数据源绑定ID | ✅ 使用 | V1.0 下拉框 |

**规则字段用途**：
- `required_rule`：动态必填规则，如"当合同类型=A时必填"
- `readonly_rule`：动态只读规则，如"当审批状态≠DRAFT时只读"
- `visible_rule`：动态显隐规则，如"当金额>10000时显示"

```json
// required_rule 示例
{
  "condition": {
    "fieldPath": "contractType",
    "operator": "EQ",
    "value": "A"
  },
  "message": "合同类型为A时必填"
}
```

**组件属性用途**：
- `component_props`：组件特定属性，如最大长度、最小值、格式化等

```json
// component_props 示例
{
  "maxLength": 100,
  "minValue": 0,
  "format": "YYYY-MM-DD",
  "precision": 2
}
```

**数据源绑定用途**：
- `data_provider_id`：下拉框等组件绑定DataProvider，获取选项列表

### 11.5 ActionConfig扩展字段

| 字段 | 类型 | 说明 | V1.0使用 | 后续阶段 |
|-----|------|------|---------|---------|
| bind_query_id | BIGINT | 查询配置绑定ID | ❌ 不使用 | V1.2+ 查询按钮 |
| confirm_required | SMALLINT | 是否需要确认（0/1） | ❌ 不使用 | V1.1+ 确认弹窗 |
| confirm_text | VARCHAR(500) | 确认提示文案 | ❌ 不使用 | V1.1+ 确认弹窗 |
| before_rule | JSON | 执行前规则 | ❌ 不使用 | V1.2+ 规则引擎 |
| after_rule | JSON | 执行后规则 | ❌ 不使用 | V1.2+ 规则引擎 |
| props_json | JSON | 扩展属性 | ❌ 不使用 | V1.1+ 自定义属性 |

**查询绑定用途**：
- `bind_query_id`：查询按钮绑定QueryConfig，点击触发查询弹窗

**确认弹窗用途**：
- `confirm_required`：是否需要用户确认才执行
- `confirm_text`：确认弹窗显示的提示文案

**前后规则用途**：
- `before_rule`：动作执行前的校验规则（如校验必填）
- `after_rule`：动作执行后的处理规则（如刷新数据）

```json
// before_rule 示例
{
  "validations": [
    { "fieldPath": "contractName", "check": "REQUIRED" },
    { "fieldPath": "amount", "check": "MIN_VALUE", "value": 0 }
  ],
  "onFail": "BLOCK_WITH_MESSAGE"
}

// after_rule 示例
{
  "actions": [
    { "type": "REFRESH_DATA" },
    { "type": "SHOW_MESSAGE", "text": "保存成功" }
  ]
}
```

### 11.6 JSON字段存储规范

**通用存储格式**：
- Java类型：String
- 数据库类型：JSON（H2）/ JSONB（生产环境）
- 序列化：Jackson ObjectMapper
- 空值处理：null存储为null，不存储空对象

**字段存储示例**：

```sql
-- visible_rule 存储示例
INSERT INTO t_ui_layout_node (
  visible_rule
) VALUES (
  '{"condition": {"fieldPath": "contractType", "operator": "EQ", "value": "A"}}'
);

-- component_props 存储示例
INSERT INTO t_ui_field_component (
  component_props
) VALUES (
  '{"maxLength": 100, "precision": 2}'
);

-- validate_rule 存储示例
INSERT INTO t_ui_field_def (
  validate_rule
) VALUES (
  '{"type": "REGEX", "pattern": "^[A-Z]{2}\\d{6}$"}'
);
```

### 11.7 扩展字段使用策略

**V1.0阶段**：
- schema保留扩展字段（已存在）
- Entity类定义扩展字段（但暂不使用）
- DTO/Request类暂不包含扩展字段
- API入参/出参暂不包含扩展字段
- 前端配置界面暂不显示扩展字段

**V1.1阶段启用部分**：
- Grid布局字段：实现复杂表单布局
- component_props：组件自定义属性
- confirm_required/confirm_text：确认弹窗

**V1.2阶段启用全部**：
- 规则字段：实现动态规则引擎
- bind字段：实现动态绑定
- 明细表字段：实现明细表功能
- 查询绑定：实现查询按钮

### 11.8 扩展字段决策记录

**决策**：保留扩展字段在schema中

**原因**：
1. **扩展性优先**：后续版本可直接启用字段，无需修改schema
2. **向后兼容**：新增字段不影响V1.0数据，平滑升级
3. **开发友好**：字段定义完整，后续实现有准确依据
4. **零风险**：V1.0不使用，字段值为null，不影响现有功能

**权衡**：
- 优点：扩展性强，后续版本升级简单
- 缺点：schema略显冗余，字段定义比V1.0需求多
- 结论：扩展性优先，保留扩展字段

---

**设计文档结束**