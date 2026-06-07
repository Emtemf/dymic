# 统一配置界面 + DETAIL_TABLE + JSONB 优化设计

> 日期: 2026-06-08
> 状态: 已确认

---

## 背景

项目存在两个配置界面：`config.html`（简单版）和 `template-designer.html`（完整版）。两者功能重叠，需要统一。后端缺少 PUT schema 聚合保存端点导致前端保存失败。DETAIL_TABLE 组件定义存在但前端交互未实现。JSONB 查询尚未做索引优化。

## 决策

- 统一到 `template-designer.html`，`config.html` 重定向
- 后端新增 PUT schema 聚合端点
- DETAIL_TABLE 实现完整功能（增行、删除、弹窗编辑、行状态管理）
- JSONB 查询优化只在 openGauss 上做，H2 仅用于基础 CRUD 单元测试

---

## 阶段 1：统一界面 + 修复后端保存 API

### 1a. 统一界面

- `/config.html` 改为 302 重定向到 `/config/template-designer.html`
- 保留旧文件不删除，避免引用断裂

### 1b. 后端新增 PUT schema 聚合端点

**端点**: `PUT /api/templates/{templateId}/versions/{versionId}/schema`

**请求体 DTO** (`SchemaSaveDTO`):
```json
{
  "layoutNodes": [...],
  "fieldDefs": [...],
  "fieldComponents": [...],
  "queryConfigs": [],
  "actionConfigs": []
}
```

**保存逻辑** (`SchemaService.saveSchema()`):
1. `@Transactional`
2. 删除旧数据（按外键依赖反序）：actionConfigs → fieldComponents → fieldDefs → layoutNodes
3. 插入新数据（按外键依赖顺序）：layoutNodes → fieldDefs → fieldComponents → actionConfigs
4. 返回保存结果

**修改文件**:
- `SchemaController.java` — 加 `@PutMapping`
- `SchemaService.java` — 加 `saveSchema()` 方法
- 新增 `SchemaSaveDTO.java`
- 前端 `config-api.js` 确认请求格式匹配

---

## 阶段 2：DETAIL_TABLE 完整功能

### 前端渲染 (preview-renderer.js)

- 渲染表格框架：列头从组件配置的 `columns` 读取
- 渲染数据行：从 draft 明细数组读取
- 操作列：新增、编辑（弹窗）、删除、撤销删除
- 空状态提示

### 前端交互 (新增 detail-table.js)

| 函数 | 功能 |
|------|------|
| `addRow()` | 生成 `_row_uid`(UUID), `_row_op=ADD`，追加到 draft 数组 |
| `editRow(uid)` | 打开弹窗，复制行数据到弹窗 draft |
| `deleteRow(uid)` | 标记 `_row_op=DELETE`，前端隐藏该行 |
| `restoreRow(uid)` | 恢复 `_row_op` 为原值 |
| `saveModalDraft()` | 弹窗保存，校验后合并回父 draft |
| 行数限制 | `min_rows` / `max_rows` 校验 |

### 弹窗编辑

- 弹窗 HTML 动态生成，字段从明细表列配置读取
- "保存"只合并到父页面 draft，不调后端
- "取消"丢弃弹窗修改

### 属性面板 (property-panel.js)

- 明细表名称、编码、路径（detail_path）
- 列配置：添加/删除列，每列有字段编码、名称、类型
- 行数限制：min_rows、max_rows
- 操作开关：allow_add、allow_edit、allow_delete

### 后端

- 确认 `t_ui_detail_table` 的 CRUD API 存在且完整
- 如缺失则补充 Controller + Service

---

## 阶段 3：Chrome DevTools E2E 验证

47 个场景，8 个模块：

### 模块 1：模板管理全流程（5 个场景）

| # | 场景 | 验证点 |
|---|------|--------|
| 1.1 | 创建模板 | API 调用成功，返回模板 ID |
| 1.2 | 创建版本 | 状态 DRAFT，版本号正确 |
| 1.3 | 发布版本 | 状态变 PUBLISHED，发布时间记录 |
| 1.4 | 加载已有模板 | schema 正确加载，画布正确渲染 |
| 1.5 | 切换模板版本 | 数据正确切换，无残留 |

### 模块 2：布局组件拖拽与嵌套（10 个场景）

| # | 场景 | 验证点 |
|---|------|--------|
| 2.1 | 添加 PAGE | 画布出现 PAGE 容器 |
| 2.2 | PAGE 内嵌 CARD | CARD 正确嵌套在 PAGE 下 |
| 2.3 | ROW/COL 左右布局 | 两个 COL 各占 50% 并排 |
| 2.4 | 调整 COL span | 改 span=8 后宽度变 33% |
| 2.5 | ROW 内 3 个 COL | 三等分布局 |
| 2.6 | GRID 栅格 | columns=3 时三列渲染 |
| 2.7 | TAB 页签 | 两 Tab 渲染，点击可切换 |
| 2.8 | COLLAPSE 折叠 | 折叠面板可展开/折叠 |
| 2.9 | 三层嵌套 | PAGE→ROW→COL→CARD 无错乱 |
| 2.10 | 删除布局节点 | 节点及子节点全部移除 |

### 模块 3：基础组件配置（11 个场景）

| # | 场景 | 验证点 |
|---|------|--------|
| 3.1 | 拖入 INPUT | 输入框正确渲染 |
| 3.2 | 配置 fieldPath | 属性保存到组件配置 |
| 3.3 | 配置必填 | 预览区显示必填标记 |
| 3.4 | 配置只读 | 预览区输入框禁用 |
| 3.5 | 拖入 SELECT | 下拉框渲染 |
| 3.6 | 配置数据源 | 静态选项保存在组件配置中 |
| 3.7 | 拖入 DATE | 日期选择器渲染 |
| 3.8 | 拖入 NUMBER | 数字输入框，支持 min/max/step |
| 3.9 | 拖入 AMOUNT | 金额输入框，千分位格式 |
| 3.10 | 拖入 TEXTAREA | 多行文本框渲染 |
| 3.11 | COL 内多组件 | 组件垂直排列，宽度正确 |

### 模块 4：DETAIL_TABLE 完整操作（10 个场景）

| # | 场景 | 验证点 |
|---|------|--------|
| 4.1 | 添加明细表 | 空表格渲染，带列头和操作按钮 |
| 4.2 | 配置列 | 表头显示配置的列名 |
| 4.3 | 新增行 | 出现空行，`_row_op=ADD` |
| 4.4 | 编辑行（内联） | 值正确绑定到 draft |
| 4.5 | 编辑行（弹窗） | 弹窗打开，回显当前行数据 |
| 4.6 | 弹窗修改保存 | 弹窗关闭，父页面数据已更新 |
| 4.7 | 删除行 | 标记 DELETE，前端隐藏/置灰 |
| 4.8 | 恢复已删除行 | 行恢复显示 |
| 4.9 | 多行混合操作 | 增 3 行删 1 行编辑 1 行，状态各自正确 |
| 4.10 | 行数限制 | max_rows=5 时第 6 行提示 |

### 模块 5：属性面板验证（8 个场景）

| # | 场景 | 验证点 |
|---|------|--------|
| 5.1 | 选中显示属性 | 属性面板显示全部配置项 |
| 5.2 | 修改即时生效 | 预览区立即更新 |
| 5.3 | 取消选中 | 属性面板回到空状态 |
| 5.4 | 切换选中组件 | 属性面板正确切换 |
| 5.5 | ROW 属性 | 显示 gutter 配置 |
| 5.6 | COL 属性 | 显示 span 和 offset |
| 5.7 | DETAIL_TABLE 属性 | 显示列配置、行数限制、增删改开关 |
| 5.8 | 非法值校验 | span 输入 0/25 时提示或修正 |

### 模块 6：保存与加载闭环（7 个场景）

| # | 场景 | 验证点 |
|---|------|--------|
| 6.1 | 保存空配置 | 不报错 |
| 6.2 | 保存布局配置 | 请求包含正确数据 |
| 6.3 | 保存含明细表 | detailTables 不为空 |
| 6.4 | 刷新后恢复 | 所有配置与保存前一致 |
| 6.5 | 修改再保存 | 新组件追加，已有配置不丢 |
| 6.6 | 网络错误处理 | 显示错误提示，不丢失 draft |
| 6.7 | 保存请求格式 | JSON 结构符合后端 DTO |

### 模块 7：预览功能（4 个场景）

| # | 场景 | 验证点 |
|---|------|--------|
| 7.1 | 预览空白配置 | 空页面或提示 |
| 7.2 | 预览简单表单 | 表单可填写 |
| 7.3 | 预览左右布局 | 两栏布局正确 |
| 7.4 | 预览含明细表 | 表格可交互 |

### 模块 8：错误与边界（5 个场景）

| # | 场景 | 验证点 |
|---|------|--------|
| 8.1 | 控制台无 JS 错误 | Console 无红色错误 |
| 8.2 | 快速反复拖拽 | 无无限递归或栈溢出 |
| 8.3 | 深层嵌套 6 层 | 渲染不崩溃 |
| 8.4 | 一次 20+ 组件 | 渲染 <2s |
| 8.5 | 强制刷新 | 新代码生效，无缓存问题 |

---

## 阶段 4：JSONB 查询优化

### 核心原则

openGauss 是唯一持久化数据库。H2 仅用于单元测试，测试 CRUD 存取（JSONB 当普通字符串）。JSONB 查询优化全部在 openGauss 上做。

### 4a. 索引策略 (openGauss)

| 表 | 字段 | 索引 | 理由 |
|---|---|---|---|
| `t_contract_data_snapshot` | `canonical_data` | GIN (jsonb_path_ops) | 按字段路径查询合同数据 |
| `t_contract_detail_row` | `row_data` | GIN (jsonb_path_ops) | 按明细字段查询 |
| `t_ext_message_inbox` | `raw_payload` | GIN | 外部消息内容查询 |
| `t_ui_data_provider` | `config_json` | GIN | 数据源配置查询 |

规则类 JSONB（visible_rule、readonly_rule 等）不建索引——只在前端渲染时读取，不做查询条件。

索引脚本放在 openGauss 建表脚本中，不放 `schema-h2.sql`。

### 4b. JsonbHelper 工具类

封装在 `infrastructure/json/JsonbHelper.java`：

| 方法 | openGauss 语法 |
|------|---------------|
| `extractText(column, path)` | `column->>'path'` |
| `extractInt(column, path)` | `(column->>'path')::int` |
| `pathExists(column, path)` | `column ? 'path'` |
| `contains(column, json)` | `column @> 'json'` |
| `pathQuery(column, path)` | `jsonb_path_query(column, '$.path')` |

H2 单元测试不测 JSONB 查询。需要在测试中验证的业务逻辑，用 Jackson 在 Java 层面解析 JSON。

### 4c. 测试分层

| 层级 | 数据库 | 测试内容 |
|------|--------|---------|
| Repository 层（单元测试） | H2 | 基础 CRUD、JSONB 字段当字符串存取 |
| Service 层（单元测试） | Mock | 业务逻辑 |
| JSONB 查询验证 | openGauss | 真实数据验证索引和查询性能 |

### 4d. 验证 (openGauss)

- 用 `scripts/start-opengauss.sh` 启动 openGauss
- 插入 1000+ 条 `t_contract_data_snapshot` 测试数据
- 执行常用查询，`EXPLAIN ANALYZE` 验证 GIN 索引命中
- 对比有/无索引的查询耗时

---

## 文件改动预估

### 阶段 1

| 文件 | 改动 |
|------|------|
| `src/main/resources/static/config.html` | 改为重定向 |
| `SchemaController.java` | 加 @PutMapping |
| `SchemaService.java` | 加 saveSchema() |
| 新增 `SchemaSaveDTO.java` | 请求体 DTO |
| `config-api.js` | 确认请求格式匹配 |

### 阶段 2

| 文件 | 改动 |
|------|------|
| `preview-renderer.js` | 加 DETAIL_TABLE 渲染 |
| 新增 `detail-table.js` | 明细表交互逻辑 |
| `property-panel.js` | 加 DETAIL_TABLE 属性配置 |
| `template-designer.html` | 加明细表弹窗模板 |
| `component-library.js` | 确认 DETAIL_TABLE 定义完整 |

### 阶段 3

无代码改动，纯验证。

### 阶段 4

| 文件 | 改动 |
|------|------|
| 新增 `JsonbHelper.java` | JSONB 查询工具类 |
| openGauss 建表脚本 | 加 GIN 索引 |
| 新增 openGauss 查询验证脚本 | 性能测试数据+查询 |
