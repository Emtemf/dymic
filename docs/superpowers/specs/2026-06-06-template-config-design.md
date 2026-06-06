# 模板配置界面设计文档 V1.0

> 本文档描述合同模板动态配置界面的架构设计，用于第一阶段实现：业务人员可以动态配置模板，后端如实保存配置。

---

## 1. 系统定位

### 1.1 核心目标

**第一阶段目标**：实现动态配置界面的核心闭环

- 业务人员可以动态配置模板结构（布局节点、字段定义、字段组件绑定、动作配置）
- 后端如实保存配置数据
- 实时预览验证配置正确性

### 1.2 界面定位

**前端界面定位**：开发验证工具，不是交付给业务用户的正式系统

- 用途：验证后端 API 功能是否正确
- 用户：开发人员自己验证
- 界面复杂度：相对简单，重点是功能完整

---

## 2. 核心功能范围

### 2.1 两个独立配置界面

```
界面1：业务人员模板配置界面（主界面，必须）
  - 业务配置人员使用
  - 配置：布局节点、字段定义、字段组件绑定、动作配置
  - 数据来源：
    ✅ 静态选项（业务人员直接输入，例如：是/否、币种等）
    ✅ 引用已配置的数据提供方（下拉选择，可选）
  - 从上到下堆积布局
  - 实时自动保存（500ms 延迟）
  - 实时预览验证

界面2：IT 数据提供方配置界面（可选，独立界面）
  - IT 配置人员使用
  - 配置：HTTP接口、平台集成、内部查询（复杂数据源）
  - 业务人员需要时再配置
  - 不是必须步骤
```

### 2.2 配置预览验证界面

```
界面3：配置预览验证界面（开发验证用）
  - 读取完整配置树（GET /api/templates/{templateId}/versions/{versionId}/schema）
  - 动态渲染表单
  - 验证配置正确性
```

---

## 3. 角色分工

### 3.1 IT 配置人员（独立界面）

**配置内容**：
- 数据提供方配置（HTTP接口、平台集成、内部查询）
- 外部系统配置（后续阶段）
- 外部数据映射配置（后续阶段）

**配置界面**：独立界面，不与业务人员混用

### 3.2 业务配置人员（主配置界面）

**配置内容**：
- 模板创建和版本管理
- 布局节点配置（卡片、表格）
- 字段定义配置（显示名称、是否必填）
- 字段组件绑定配置（选择组件类型）
- 数据来源选择（静态选项或引用数据提供方）
- 动作配置（保存、查询等按钮）

**配置方式**：
- 点击组件库选择组件
- 填写简单业务属性（显示名称、是否必填、提示文字）
- 从上到下堆积布局
- **不暴露技术细节**（字段路径、JSONB等技术概念）

---

## 4. 架构概览

### 4.1 前端架构

**技术栈**：
- HTML：原生 HTML5
- CSS：frontend-design 样式库
- JavaScript：原生 JavaScript ES6+
- 构建：无构建工具，直接静态文件

**界面布局**：两栏式

```
┌─────────────────────────────────────────────────┐
│ 顶部：模板信息                                     │
│ 模板名称：采购合同模板  版本：V1.0（草稿）           │
└─────────────────────────────────────────────────┘
┌──────────────┬──────────────────────────────────┐
│ 左侧：组件库      │ 右侧：配置画布 + 属性面板           │
│ (250px)      │ (flex-grow)                     │
│              │                                 │
│ 📦 基础组件    │ 配置画布（从上到下堆积）            │
│  ├─ 文本输入框  │ 属性面板（选中组件后显示）          │
│  ├─ 数字输入框  │                                 │
│  ├─ 日期选择器  │                                 │
│  ├─ 下拉选择框  │                                 │
│  └─ 金额输入框  │                                 │
│              │                                 │
│ 📦 布局组件    │                                 │
│  ├─ 卡片容器    │                                 │
│  └─ 表格组件    │                                 │
│              │                                 │
│ 📦 按钮组件    │                                 │
│  ├─ 保存按钮    │                                 │
│  └─ 查询按钮    │                                 │
└──────────────┴──────────────────────────────────┘
```

**核心组件**：
- **组件库**：分类展示可选组件（基础组件、布局组件、按钮组件）
- **配置画布**：从上到下堆积已配置的组件
- **属性面板**：选中组件后显示业务属性配置表单
- **自动保存机制**：500ms 延迟保存，避免频繁请求

### 4.2 后端架构

**技术栈**：
- Java 21
- Spring Boot 3.5.14
- MyBatis-Plus 3.5.5
- MapStruct 1.5.5（对象转换）
- H2 2.2.224（测试数据库）
- openGauss（生产数据库）

**分层架构**：

```
com.contract/
├── adapter/
│   ├── controller/           # REST API
│   │   ├── LayoutNodeController.java
│   │   ├── FieldDefController.java
│   │   ├── FieldComponentController.java
│   │   ├── ActionConfigController.java
│   │   ├── DataProviderController.java
│   │   └── TemplateSchemaController.java
│   └── persistence/
│       ├── mapper/           # MyBatis Mapper
│       └── entity/           # Entity（带Entity后缀）
│
├── application/
│   └── template/             # 应用服务（业务编排）
│       ├── LayoutNodeService.java
│       ├── FieldDefService.java
│       ├── FieldComponentService.java
│       ├── ActionConfigService.java
│       ├── DataProviderService.java
│       └ TemplateSchemaService.java
│   └ convert/                # MapStruct转换器（DTO ↔ Domain）
│
├── domain/
│   └ template/               # 领域模型（不带Entity后缀）
│   │   ├── LayoutNode.java
│   │   ├── FieldDef.java
│   │   ├── FieldComponent.java
│   │   ├── ActionConfig.java
│   │   └── DataProvider.java
│   └ repository/             # Repository接口（领域层定义）
│
├── infrastructure/
│   └ persistence/
│   │   └ repository/         # Repository实现（基础设施层实现）
│   │   └ convert/            # MapStruct转换器（Entity ↔ Domain）
│   └ id/                     # ID生成
│   └ json/                   # JSON工具
│
└── common/
    ├── exception/            # 异常定义
    ├── result/               # 统一响应
    └ enums/                  # 枚举定义
```

---

## 5. API 模块划分

### 5.1 模块列表

```
模块1：模板和版本管理 API（已完成）
  - TemplateController
  - TemplateVersionController

模块2：数据提供方配置 API（可选，独立界面）
  - DataProviderController
  - CRUD API：创建、查询、更新数据提供方
  - IT 配置人员使用

模块3：布局节点配置 API（主配置界面）
  - LayoutNodeController
  - CRUD API：创建、查询、更新布局节点（卡片、表格）

模块4：字段定义配置 API（主配置界面）
  - FieldDefController
  - CRUD API：创建、查询、更新字段定义
  - 同时创建字段组件绑定（一体化）

模块5：字段组件绑定配置 API（主配置界面）
  - FieldComponentController
  - CRUD API：创建、查询、更新字段组件绑定
  - 支持静态选项输入或引用数据提供方

模块6：动作配置 API（主配置界面）
  - ActionConfigController
  - CRUD API：创建、查询、更新动作配置

模块7：配置读取 API（预览验证用）
  - TemplateSchemaController
  - GET API：读取完整配置树（用于渲染预览）
```

### 5.2 设计原则

**业务友好**：
- API 参数使用业务语言（显示名称、是否必填、提示文字）
- 不暴露技术细节（字段路径、JSONB、grid_x/y/w/h）

**技术字段自动生成**：
- 字段编码：后端自动生成（拼音转换）
- 字段路径：后端自动生成（根据父节点路径）
- 数据类型：后端自动检测（根据组件类型）
- 布局位置：相对位置（从上到下，sortNo），不是绝对坐标

**使用 MapStruct**：
- 所有 Entity ↔ DTO 转换使用 MapStruct（符合规范）
- 不手写转换逻辑

**实时保存**：
- 支持单组件保存，无需批量提交
- 500ms 延迟保存，避免频繁请求

**配置阶段不删除**：
- 只有添加和修改，不删除（模板草稿阶段）
- 删除操作在合同录入阶段（业务操作）

---

## 6. 关键设计决策

### 6.1 数据来源策略（可选）

**决策**：数据提供方是可选的，不是必须步骤

**原因**：
- 业务人员可以完全自定义模板，不需要外部数据源
- 简单场景：业务人员直接输入静态选项（不依赖IT）
- 复杂场景：IT先配置数据提供方，业务人员再引用

**实现**：
- 字段组件绑定支持两种数据来源：
  - STATIC（静态选项）：业务人员直接输入选项列表
  - PROVIDER（数据提供方）：业务人员下拉选择已配置的数据提供方

### 6.2 从上到下堆积布局

**决策**：第一阶段先支持从上到下堆积，后续版本再支持拖拽

**原因**：
- 简化实现复杂度
- 符合业务人员的直觉（按顺序配置）
- 后续版本可以增加拖拽排序功能

**实现**：
- 使用 sortNo 字段表示相对位置（第1个、第2个、第3个...）
- 不使用 grid_x/grid_y/grid_w/grid_h（绝对坐标）

### 6.3 屏蔽技术细节

**决策**：配置界面不暴露技术细节，业务人员只填写业务属性

**原因**：
- 业务人员不懂IT技术（字段路径、JSONB等技术概念）
- 降低配置难度，提高易用性

**实现**：
- 业务人员填写：显示名称、是否必填、提示文字、数据来源
- 后端自动生成：字段编码、字段路径、数据类型、JSONB配置

### 6.4 使用 MapStruct（符合规范）

**决策**：所有对象转换使用 MapStruct，不手写转换逻辑

**原因**：
- 项目规范要求
- 减少样板代码
- 类型安全

**实现**：
- Entity ↔ Domain：在基础设施层使用 MapStruct
- Domain ↔ DTO：在应用层使用 MapStruct

---

## 7. 核心数据流

### 7.1 配置流程

```
步骤1：IT 配置数据提供方（可选）
  → POST /api/data-providers
  → 保存到 t_ui_data_provider

步骤2：业务人员创建模板和版本
  → POST /api/templates
  → POST /api/templates/{templateId}/versions
  → 保存到 t_ui_template、t_ui_template_version

步骤3：业务人员配置布局节点
  → POST /api/templates/{templateId}/versions/{versionId}/layout-nodes
  → 保存到 t_ui_layout_node
  → 自动生成：nodeCode、nodePath

步骤4：业务人员配置字段定义
  → POST /api/templates/{templateId}/versions/{versionId}/field-defs
  → 同时创建字段组件绑定（一体化）
  → 保存到 t_ui_field_def、t_ui_field_component
  → 自动生成：fieldCode、fieldPath、dataType

步骤5：业务人员选择数据来源
  → 如果是静态选项：直接输入选项列表
  → 如果是数据提供方：下拉选择已配置的数据提供方ID
  → 保存到 field_component.componentProps（JSONB）

步骤6：业务人员配置动作按钮
  → POST /api/templates/{templateId}/versions/{versionId}/actions
  → 保存到 t_ui_action_config
  → 自动生成：actionCode

步骤7：实时预览验证
  → GET /api/templates/{templateId}/versions/{versionId}/schema
  → 读取完整配置树
  → 动态渲染表单
  → 验证配置正确性

步骤8：发布版本
  → POST /api/templates/versions/{versionId}/publish
  → 更新版本状态为 PUBLISHED
  → 更新模板的当前版本ID
```

### 7.2 配置读取流程（预览渲染）

```
请求：GET /api/templates/{templateId}/versions/{versionId}/schema

后端处理：
  1. 查询所有布局节点（t_ui_layout_node）
  2. 查询所有字段定义（t_ui_field_def）
  3. 查询所有字段组件绑定（t_ui_field_component）
  4. 查询引用的数据提供方（t_ui_data_provider）
  5. 查询所有动作配置（t_ui_action_config）
  6. 构建完整配置树（嵌套结构）
  7. 使用 MapStruct 转换 Entity → DTO
  8. 返回 TemplateSchemaDTO

前端渲染：
  1. 接收 TemplateSchemaDTO
  2. 遍历布局节点树，渲染卡片、表格等容器
  3. 在容器内渲染字段组件（文本框、下拉框等）
  4. 渲染动作按钮（保存、查询等）
  5. 绑定数据提供方到下拉框组件
```

---

## 8. 测试策略

### 8.1 后端测试

**Repository 层测试（H2 内存数据库）**：
- 测试 CRUD 操作正确性
- 测试查询条件准确性
- 测试 JSONB 字段存储

**Service 层测试（Mock Repository）**：
- 测试业务流程正确性
- 测试自动生成逻辑（字段编码、字段路径）
- 测试 MapStruct 转换正确性

**Controller 层测试（MockMvc）**：
- 测试 HTTP 方法正确性
- 测试参数绑定正确性
- 测试响应格式正确性

### 8.2 前端测试（开发验证）

**配置流程验证**：
- 添加卡片容器 → 验证后端保存
- 添加文本输入框 → 验证字段定义和组件绑定创建
- 添加下拉框（静态选项）→ 验证选项保存
- 添加下拉框（数据提供方）→ 验证引用关系
- 添加保存按钮 → 验证动作配置创建

**预览渲染验证**：
- 读取配置树 → 验证嵌套结构正确
- 动态渲染表单 → 验证组件显示正确
- 验证数据绑定正确性

---

## 9. 后续阶段范围

**不在第一阶段范围**：

```
查询配置（后续阶段）：
  - QueryConfigController
  - 查询参数绑定、回填规则
  - 查询弹窗配置

明细表配置（后续阶段）：
  - DetailTableController
  - 明细表的行列操作规则
  - 增删行逻辑

合同录入（后续阶段）：
  - ContractController
  - 合同创建、保存、回显
  - 字段值保存、快照生成
  - 明细行操作（增删改）

外部数据集成（后续阶段）：
  - ExternalDataController
  - 外部数据映射、对比
```

---

## 10. 技术债务和优化点

**需要注意的技术债务**：

```
1. 用户上下文获取：
   - 当前硬编码用户ID
   - 需要集成真实的用户上下文

2. 并发编辑控制：
   - 第一阶段不考虑并发场景
   - 后续版本可以使用 Redis + for update

3. 字段路径冲突检测：
   - 后端自动生成字段路径
   - 需要检测是否与已有字段冲突

4. 配置验证规则：
   - 后续版本可以增加配置完整性校验
   - 例如：字段定义必须关联字段组件绑定

5. 性能优化：
   - 配置树的查询可以优化（批量查询替代多次查询）
   - 后续版本可以增加缓存
```

---

**文档版本**：v1.0
**创建时间**：2026-06-06
**状态**：待审核