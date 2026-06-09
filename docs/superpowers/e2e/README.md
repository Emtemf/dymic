# E2E测试套件

> **版本**：V1.0
> **日期**：2026-06-09

---

## 一、测试套件概述

### 1.1 目的

验证数据源配置统一功能的完整性和正确性，包括：
- 存量功能不受影响
- 新增功能正常工作
- DDD规范符合要求
- JSONB查询性能达标

### 1.2 测试范围

| 类别 | 测试场景数 | 说明 |
|-----|-----------|------|
| CRUD完整流程 | 6类×4操作 | 模板、版本、布局节点、字段组件、数据源、合同 |
| 组件库遍历 | 21个 | 布局7+基础6+业务4+动作4 |
| 嵌套组合场景 | 147+ | 两层嵌套所有组合 + 三层嵌套典型场景 |
| JSONB查询 | 4类×5查询 | 布局节点、字段组件、数据源、合同快照 |
| 规则执行 | 3类 | 显隐规则、必填规则、只读规则 |
| 数据绑定 | 3类 | 字段路径、数据源绑定、嵌套路径 |

---

## 二、目录结构

```
docs/superpowers/e2e/
├── README.md                           # 测试套件说明（本文档）
├── scenarios/                          # 场景设计文档
│   ├── 01-template-crud.md             # 模板CRUD验证
│   ├── 02-version-crud.md             # 版本CRUD验证
│   ├── 03-layout-node-crud.md         # 布局节点CRUD验证
│   ├── 04-field-component-crud.md     # 字段组件CRUD验证
│   ├── 05-data-provider-crud.md       # 数据源CRUD验证
│   ├── 06-contract-crud.md            # 合同CRUD验证
│   ├── 07-component-render/           # 组件渲染验证
│   │   ├── 07-01-INPUT.md
│   │   ├── 07-02-SELECT.md
│   │   ├── 07-03-DATE.md
│   │   ├── 07-04-NUMBER.md
│   │   ├── 07-05-MONEY.md
│   │   ├── 07-06-TEXTAREA.md
│   │   ├── 07-07-CARD.md
│   │   ├── 07-08-TAB.md
│   │   ├── 07-09-COLLAPSE.md
│   │   ├── 07-10-GRID.md
│   │   ├── 07-11-DETAIL_TABLE.md
│   │   ├── 07-12-ATTACHMENT.md
│   │   ├── 07-13-IMAGE.md
│   │   ├── 07-14-BUTTON.md
│   │   ├── 07-15-QUERY_DIALOG.md
│   │   ├── 07-16-SAVE_BUTTON.md
│   │   └── 07-17-SUBMIT_BUTTON.md
│   ├── 08-nested-combinations/        # 嵌套组合验证
│   │   ├── 08-01-two-level-nesting.md
│   │   ├── 08-02-three-level-nesting.md
│   │   └── 08-03-deep-nesting.md
│   ├── 09-rules-execution/            # 规则执行验证
│   │   ├── 09-01-visible-rule.md
│   │   ├── 09-02-required-rule.md
│   │   └── 09-03-readonly-rule.md
│   └── 10-jsonb-queries/              # JSONB查询验证
│       ├── 10-01-layout-node-queries.md
│       ├── 10-02-field-component-queries.md
│       ├── 10-03-data-provider-queries.md
│       └── 10-04-contract-snapshot-queries.md
├── records/                            # 执行记录
│   └── 2026-06-09/                     # 按日期组织
│       ├── 01-template-crud-record.md
│       ├── 02-version-crud-record.md
│       └── ...
├── explains/                           # EXPLAIN结果
│   └── 2026-06-09/                     # 按日期组织
│       ├── 10-01-layout-node-explain.md
│       ├── 10-02-field-component-explain.md
│       └── ...
└── test-data/                          # 测试数据
    ├── components/                     # 组件配置数据
    ├── templates/                      # 模板配置数据
    └── contracts/                      # 合同数据
```

---

## 三、测试流程

### 3.1 场景设计流程

```
1. 编写场景设计文档（scenarios/）
   - 定义测试数据（至少3组）
   - 定义测试步骤（详细操作）
   - 定义预期结果
   - 定义验证清单

2. 执行测试（chrome-devtools + 数据库验证）
   - 按步骤执行操作
   - 截图保存
   - 执行SQL验证JSONB存储

3. 记录执行结果（records/）
   - 记录每个步骤的执行状态
   - 记录问题（如有）
   - 保存截图路径

4. 记录EXPLAIN结果（explains/）
   - 执行EXPLAIN ANALYZE
   - 记录性能数据
   - 记录优化建议
```

### 3.2 文档模板

#### 场景设计模板

```markdown
# 场景ID：XX-YY-场景名称

## 场景描述
简要描述验证内容。

## 前置条件
- 条件1
- 条件2

## 测试数据

### 数据1：场景描述
```json
{
  "type": "INPUT",
  "name": "合同名称",
  ...
}
```

### 数据2：场景描述
```json
{
  ...
}
```

### 数据3：场景描述
```json
{
  ...
}
```

## 测试步骤

### 步骤1：操作描述
- 操作：具体操作
- 预期：预期结果

### 步骤2：...

## 验证清单
- [ ] 验证项1
- [ ] 验证项2
- [ ] ...
```

#### 执行记录模板

```markdown
# 执行记录：XX-YY-场景名称

## 执行信息
- 执行日期：YYYY-MM-DD
- 执行人：xxx
- 环境：本地/开发/测试

## 执行结果

### 步骤1：操作描述
- 状态：✅ 通过 / ❌ 失败
- 截图：records/YYYY-MM-DD/images/XX-YY-step1.png
- 备注：...

### 步骤2：...

## 问题记录
- 问题1：描述
  - 原因：
  - 解决方案：

## 总结
总体评价。
```

#### EXPLAIN结果模板

```markdown
# EXPLAIN结果：XX-YY-查询名称

## 查询语句
```sql
SELECT ...
```

## EXPLAIN ANALYZE结果
```
QUERY PLAN
----------------------------------------------------------
...
```

## 性能分析
- 执行时间：XXms
- 扫描行数：XX行
- 返回行数：XX行
- 性能：良好/需优化

## 优化建议
- 建议1
- 建议2
```

---

## 四、测试场景清单

### 4.1 CRUD验证（24个场景）

| 场景ID | 场景名称 | 测试操作 |
|-------|---------|---------|
| 01-template-crud | 模板CRUD | Create/Read/Update/Delete |
| 02-version-crud | 版本CRUD | Create/Read/Update/Publish/Delete |
| 03-layout-node-crud | 布局节点CRUD | Create/Read/Update/Delete/嵌套 |
| 04-field-component-crud | 字段组件CRUD | Create/Read/Update/Delete |
| 05-data-provider-crud | 数据源CRUD | Create/Read/Update/Delete/类型分类 |
| 06-contract-crud | 合同CRUD | Create/Read/Update/Delete/快照验证 |

### 4.2 组件渲染验证（21个场景）

| 场景ID | 组件类型 | 验证内容 |
|-------|---------|---------|
| 07-01-INPUT | 输入框 | 基础属性、只读、maxLength、宽度 |
| 07-02-SELECT | 下拉框 | 静态选项、数据源绑定、多选 |
| 07-03-DATE | 日期选择 | 格式、时间选择、范围限制 |
| 07-04-NUMBER | 数字输入 | 范围、精度、步进 |
| 07-05-MONEY | 金额输入 | 货币、精度、范围 |
| 07-06-TEXTAREA | 多行文本 | 行数、maxLength |
| 07-07-CARD | 卡片 | 标题、嵌套子组件 |
| 07-08-TAB | Tab页签 | 页签配置、切换、嵌套 |
| 07-09-COLLAPSE | 折叠面板 | 面板配置、展开收起 |
| 07-10-GRID | 栅格 | 列数、间距、响应式 |
| 07-11-DETAIL_TABLE | 明细表 | 列配置、增删行、汇总 |
| 07-12-ATTACHMENT | 附件上传 | 数量限制、大小限制、格式限制 |
| 07-13-IMAGE | 图片上传 | 数量限制、大小限制、格式限制 |
| 07-14-BUTTON | 按钮 | 文本、类型、图标 |
| 07-15-QUERY_DIALOG | 查询弹窗 | 配置、触发 |
| 07-16-SAVE_BUTTON | 保存按钮 | 文本、确认提示 |
| 07-17-SUBMIT_BUTTON | 提交按钮 | 文本、确认提示 |

### 4.3 嵌套组合验证（147+场景）

| 场景ID | 组合类型 | 场景数 |
|-------|---------|-------|
| 08-01-two-level-nesting | 两层嵌套 | 7×21=147种 |
| 08-02-three-level-nesting | 三层嵌套 | 5种典型场景 |
| 08-03-deep-nesting | 深层嵌套 | 4-5层嵌套 |

### 4.4 JSONB查询验证（20个场景）

| 场景ID | 查询类型 | 场景数 |
|-------|---------|-------|
| 10-01-layout-node-queries | 布局节点JSONB查询 | 5个查询 |
| 10-02-field-component-queries | 字段组件JSONB查询 | 5个查询 |
| 10-03-data-provider-queries | 数据源JSONB查询 | 5个查询 |
| 10-04-contract-snapshot-queries | 合同快照JSONB查询 | 5个查询 |

### 4.5 规则执行验证（3个场景）

| 场景ID | 规则类型 | 验证内容 |
|-------|---------|---------|
| 09-01-visible-rule | 显隐规则 | 单条件、多条件AND、多条件OR |
| 09-02-required-rule | 必填规则 | 动态必填、条件必填 |
| 09-03-readonly-rule | 只读规则 | 动态只读、条件只读 |

---

## 五、执行工具

### 5.1 Chrome DevTools MCP

使用chrome-devtools MCP工具进行前端操作验证：
- 页面导航：`mcp__chrome-devtools__navigate_page`
- 元素点击：`mcp__chrome-devtools__click`
- 表单填写：`mcp__chrome-devtools__fill`
- 截图：`mcp__chrome-devtools__take_screenshot`
- DOM快照：`mcp__chrome-devtools__take_snapshot`
- 网络请求：`mcp__chrome-devtools__list_network_requests`
- 控制台日志：`mcp__chrome-devtools__list_console_messages`

### 5.2 数据库验证

使用高斯数据库执行SQL验证：
- JSONB存储验证
- JSONB查询验证
- EXPLAIN ANALYZE性能分析

---

## 六、验收标准

### 6.1 功能验收标准

- 所有组件属性设置生效
- 所有组件预览效果准确显示
- 所有嵌套组合渲染正确
- 所有JSONB查询返回正确结果
- 所有JSONB存储格式正确

### 6.2 性能验收标准

| 场景 | 性能要求 |
|-----|---------|
| 单组件渲染 | <500ms |
| 50个组件渲染 | <3s |
| 10层嵌套渲染 | <3s |
| JSONB查询（<10000行） | <100ms |
| JSONB查询（>10000行） | <500ms（有索引） |

### 6.3 DDD规范验收标准

- 所有领域对象符合DDD规范
- 所有聚合根有状态流转说明
- 所有值对象有业务含义说明
- 所有业务方法有契约说明

---

## 七、测试报告

测试完成后，汇总执行记录和EXPLAIN结果，生成测试报告：

```markdown
# 测试报告

## 一、测试概况
- 测试日期：YYYY-MM-DD
- 测试环境：xxx
- 测试人员：xxx
- 测试场景数：XX个
- 通过场景数：XX个
- 失败场景数：XX个

## 二、执行结果汇总
（表格形式）

## 三、问题汇总
（问题列表）

## 四、性能汇总
（EXPLAIN结果汇总）

## 五、结论
总体评价。
```

---

## 八、参考资料

- 设计文档：`docs/superpowers/specs/2026-06-09-data-source-unified-design.md`
- 需求文档：`req/req.md`
- DDD规范：`CLAUDE.md`
