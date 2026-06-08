# 后端测试修复 + 前端流程改造 + E2E验证

## Context

当前系统 116 个测试中有 23 个失败（13 Failure + 10 Error），前端设计器缺少模板/版本选择流程。需要先修复后端确保逻辑正确，再改造前端入口流程，最后用 Chrome DevTools 做完整的 E2E 验证。

验证的核心是 req.md 中第一阶段的配置闭环：**模板配置 → 版本发布 → 可视化设计器配置布局/字段/组件/数据源 → 保存配置 → 重新加载验证**，覆盖组件拖拽、属性设置、组件组合、持久化、JSONB 查询。

---

## 第一阶段：修复测试（目标：116/116 通过）

### 1.1 FieldDefServiceTest — 3 处断言与实现不符

**文件**: `src/test/java/com/contract/FieldDefServiceTest.java`

**问题**: `FieldDefService.create()` 直接存储用户传入的 `dataType`（`INPUT`/`MONEY`/`SELECT`），不再做类型转换。测试断言期望的是旧的转换后值。

**修复**:
- `testCreateFieldDef` (L65): `TEXT` → `INPUT`
- `testCreateMoneyField` (L120): `NUMBER` → `MONEY`
- `testCreateSelectFieldWithStaticOptions` (L142): `TEXT` → `SELECT`

### 1.2 LayoutNodeControllerTest — 6 处 500 错误（DTO 字段名不匹配）

**文件**: `src/test/java/com/contract/LayoutNodeControllerTest.java`

**问题**: 测试 JSON 用 `displayName` 字段，但 `LayoutNodeCreateDTO` 实际字段是 `nodeName`（`@NotBlank` 校验失败 → 500）。`visibleRule`/`readonlyRule` 传 JSON 对象但 DTO 字段是 `String` 类型导致 Jackson 反序列化失败。

**修复**:
- 所有测试 JSON 中 `displayName` → `nodeName`
- `visibleRule`/`readonlyRule` 改为 JSON 字符串（`"{"condition":"status == 'ACTIVE'"}"`）
- `testUpdate` 中硬编码 `id=1` → 先创建节点从响应提取真实 ID

### 1.3 TemplateVersionServiceTest / TemplateServiceTest — 10 处 unique 冲突

**文件**:
- `src/test/java/com/contract/application/template/TemplateVersionServiceTest.java`
- `src/test/java/com/contract/application/template/TemplateServiceTest.java`

**问题**: `@SpringBootTest` + `@Transactional` 但 H2 数据未在测试间隔离。前一个测试创建的数据影响后续测试的唯一约束。

**修复**: 给每个测试的模板编码加时间戳后缀（`"VERSION_TEST_" + System.nanoTime()`），确保不冲突。

### 1.4 DataProviderControllerTest — 2 处 500（configJson NOT NULL）

**文件**: `src/test/java/com/contract/DataProviderControllerTest.java`

**问题**: `t_data_provider.config_json` 列定义为 NOT NULL，测试创建时没传。

**修复**: 在测试 JSON 中添加 `configJson` 字段。

---

## 第二阶段：改造前端入口流程

按已确认的低保真原型（http://localhost:8889/index.html）改造 `template-designer.html`：

**步骤1 — 模板列表**:
- 调用 `GET /api/templates` 加载列表
- 点击模板 → 进入步骤2
- 支持新建模板

**步骤2 — 版本列表**:
- 调用 `GET /api/templates/{id}/versions` 加载版本列表
- 显示版本状态（DRAFT/PUBLISHED）
- 草稿版本 → "编辑配置" 进入设计器
- 已发布版本 → "查看配置" 进入设计器（只读）
- 支持新建草稿版本、发布版本

**步骤3 — 设计器**:
- URL 参数携带 `?templateId=X&versionId=Y`
- 顶部显示当前模板名 + 版本号 + 状态
- 草稿版本可编辑保存，已发布版本只读
- `DesignerState` 从 URL 参数初始化，不再硬编码

---

## 第三阶段：E2E 验证

使用 Chrome DevTools 逐场景验证。以下场景按业务闭环排列，覆盖 req.md 第一阶段全部能力。

### 场景 A：模板管理 API 闭环

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| A1 | `POST /api/templates` 创建模板（编码: E2E_TEST） | 返回 success=true，有 id，状态 ENABLED |
| A2 | `GET /api/templates` 列表查询 | 列表包含 E2E_TEST |
| A3 | `GET /api/templates/code/E2E_TEST` 按编码查询 | 返回正确模板详情 |
| A4 | `POST /api/templates/{id}/disable` 停用 | 状态变为 DISABLED |
| A5 | `POST /api/templates/{id}/enable` 启用 | 状态恢复 ENABLED |

### 场景 B：版本管理 API 闭环

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| B1 | `POST /api/templates/{id}/versions` 创建草稿版本 | 状态 DRAFT，有 versionNo |
| B2 | `GET /api/templates/{id}/versions` 版本列表 | 包含刚创建的版本 |
| B3 | `POST /api/templates/versions/{id}/publish` 发布 | 状态变为 PUBLISHED，有 publishTime |
| B4 | `GET /api/templates/{id}/versions/current` 查询当前版本 | 返回刚发布的版本 |
| B5 | 再创建一个草稿版本 V2 | 状态 DRAFT |
| B6 | 发布 V2 | 当前版本切换到 V2 |

### 场景 C：前端入口流程（模板选择 → 版本选择 → 设计器）

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| C1 | 打开 `config/template-designer.html` | 显示模板列表，不是空白设计器 |
| C2 | 点击模板 E2E_TEST | 右侧显示模板详情 + 版本历史 |
| C3 | 点击"选择版本" | 左侧切换为版本列表 |
| C4 | 点击草稿版本 | 右侧显示版本详情 + "编辑配置"按钮 |
| C5 | 点击"编辑配置" | 进入设计器，URL 带 templateId + versionId |
| C6 | 检查顶部信息条 | 显示模板名 + 版本号 + 草稿状态 |
| C7 | 点击已发布版本的"查看配置" | 进入设计器，显示只读模式提示 |

### 场景 D：组件拖拽和预览

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| D1 | 从组件库拖拽 PAGE 到预览区 | 预览区显示 PAGE 组件 |
| D2 | 拖拽 CARD 到 PAGE 内 | CARD 作为子组件显示在 PAGE 下 |
| D3 | 拖拽 GRID 到 CARD 内 | GRID 正确嵌套 |
| D4 | 拖拽 INPUT 到 GRID 内 | INPUT 组件渲染为输入框 |
| D5 | 拖拽 SELECT 到 GRID 内 | SELECT 组件渲染为下拉框 |
| D6 | 拖拽 DATE 到 GRID 内 | DATE 组件渲染为日期选择器 |
| D7 | 拖拽 MONEY 到 GRID 内 | MONEY 组件渲染为金额输入框 |
| D8 | 拖拽 TEXTAREA 到 GRID 内 | TEXTAREA 渲染为多行文本 |
| D9 | 拖拽 NUMBER 到 GRID 内 | NUMBER 渲染为数字输入框 |
| D10 | 拖拽 BUTTON 到 CARD 内 | BUTTON 渲染为按钮，颜色正确 |
| D11 | 拖拽 DETAIL_TABLE 到 PAGE 内 | DETAIL_TABLE 渲染为表格 |
| D12 | 点击预览按钮 | 弹窗显示完整表单预览 |

### 场景 E：组件属性设置

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| E1 | 点击 INPUT 组件 | 右侧属性面板显示 INPUT 配置项 |
| E2 | 修改名称为"合同名称" | 预览区更新显示 |
| E3 | 设置编码为"contractName" | 属性保存 |
| E4 | 设置字段路径为"basic.contractName" | 数据绑定配置 |
| E5 | 设置数据类型为 string | 类型配置保存 |
| E6 | 点击 SELECT 组件 | 属性面板显示 SELECT 配置项 |
| E7 | 配置 SELECT 的 placeholder | 预览区更新 |
| E8 | 点击 MONEY 组件 | 属性面板显示金额配置 |
| E9 | 设置币种为 CNY | 预览区显示货币符号 |
| E10 | 点击 DETAIL_TABLE | 属性面板显示明细表列配置、行数限制 |
| E11 | 配置明细表列 | 列配置保存 |
| E12 | 点击 CARD 组件 | 属性面板显示容器配置 |
| E13 | 修改 CARD 名称为"基本信息" | 预览区标题更新 |

### 场景 F：复杂布局组合

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| F1 | 创建 PAGE > CARD"基本信息" > GRID(2列) > [INPUT, SELECT] 组合 | 正确渲染两列布局 |
| F2 | 创建 CARD"金额信息" > GRID(2列) > [MONEY, DATE] 组合 | 正确渲染金额+日期 |
| F3 | 创建 CARD"明细" > DETAIL_TABLE 组合 | 正确渲染明细表 |
| F4 | 创建 CARD"操作" > BUTTON 组合 | 正确渲染按钮区 |
| F5 | 点击预览按钮 | 完整采购合同表单展示，所有组件正确 |
| F6 | 在预览中输入数据 | INPUT 可输入，SELECT 可选择，DATE 可选择 |

### 场景 G：配置保存和持久化

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| G1 | 搭建场景 F 的完整布局 | 预览区显示完整表单 |
| G2 | 点击保存按钮 | 请求 PUT /api/templates/{id}/versions/{vid}/schema |
| G3 | 检查响应 | 返回 200，success=true |
| G4 | 检查网络请求体 | layoutNodes、fieldDefs、fieldComponents 都有数据 |
| G5 | 刷新页面，重新选择模板和版本 | 重新进入设计器 |
| G6 | 等待配置加载 | 配置自动从后端加载，预览区恢复之前的布局 |
| G7 | 比对加载后的组件 | PAGE/CARD/GRID/INPUT/SELECT/MONEY/DATE/DETAIL_TABLE/BUTTON 都在，层级关系正确 |
| G8 | 比对属性 | 组件名称、编码、字段路径、placeholder 等属性正确回显 |

### 场景 H：Schema API 验证

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| H1 | `GET /api/templates/{id}/versions/{vid}/schema` | 返回完整配置树 |
| H2 | 检查 layoutNodes | 包含所有节点，parentId 关系正确，树结构完整 |
| H3 | 检查 fieldDefs | 包含所有字段定义，fieldCode、fieldPath 正确 |
| H4 | 检查 fieldComponents | 包含所有组件绑定，componentType 正确 |
| H5 | 检查 actionConfigs | 包含按钮等动作配置 |
| H6 | `PUT /api/templates/{id}/versions/{vid}/schema` 替换保存 | 返回 200 |
| H7 | 再次 GET | 数据与 PUT 时一致 |

### 场景 I：JSONB 特性验证

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| I1 | 检查布局节点 visible_rule JSONB 存储 | 数据库中 visible_rule 正确存储为 JSONB |
| I2 | 检查布局节点 readonly_rule JSONB 存储 | 数据库中 readonly_rule 正确存储为 JSONB |
| I3 | 检查布局节点 props_json JSONB 存储 | props_json 包含 span、offset 等布局属性 |
| I4 | 检查字段组件 component_props JSONB | 包含 placeholder、required 等组件属性 |
| I5 | 设置 visible_rule 条件（如 `status == 'ACTIVE'`） | 保存后 JSONB 字段正确 |
| I6 | 重新加载配置 | JSONB 规则正确回显到规则配置面板 |
| I7 | 验证 JSONB 索引存在 | 检查 GIN 索引创建成功 |
| I8 | 执行 JSONB 查询测试 | 通过 props_json 查询节点，索引生效 |

### 场景 J：数据源配置

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| J1 | 在设计器切换到"数据源" Tab | 显示数据源配置面板 |
| J2 | 点击"添加数据源" | 弹出数据源类型选择 |
| J3 | 选择"静态数据" | 显示 JSON 编辑器 |
| J4 | 配置币种选项（CNY/USD/EUR） | 保存成功 |
| J5 | 选择"字典"类型 | 显示字典类型输入框 |
| J6 | 配置字典类型 CONTRACT_TYPE | 保存成功 |
| J7 | 保存配置 | 数据源配置随 Schema 一起保存 |
| J8 | 重新加载 | 数据源列表正确回显 |

### 场景 K：版本发布和只读验证

| 步骤 | 操作 | 验证点 |
|------|------|--------|
| K1 | 在版本列表页点击"发布"按钮 | 版本状态变为 PUBLISHED |
| K2 | 再次进入设计器（已发布版本） | 顶部显示"已发布 - 只读"，保存按钮隐藏 |
| K3 | 尝试拖拽组件 | 确认只读模式下不能修改 |
| K4 | 创建新的草稿版本 | 状态 DRAFT |
| K5 | 在草稿版本中修改配置 | 可以正常编辑和保存 |
| K6 | 发布新版本 | 旧版本仍可查看，新版本成为当前版本 |

---

## 验证标准

- [ ] `mvn test` 116/116 通过，0 Failure，0 Error
- [ ] 前端流程：模板选择 → 版本选择 → 设计器，步骤 C1-C7 全通过
- [ ] E2E 场景 A-K 全部通过 Chrome DevTools 验证
- [ ] 后端 API 返回格式统一（`Result<T>`）
- [ ] JSONB 字段正确存储和查询
- [ ] 配置保存后重新加载，数据完整一致
