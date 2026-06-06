# CLAUDE.md 文件设计规范

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为合同模板动态渲染系统创建清晰的CLAUDE.md项目规则文档

**设计原则:**
- 核心规则摘要（CLAUDE.md中仅5条）
- 详细规则渐进式披露（拆分到17个rule文件）
- 统一在本项目内，不引用外部文档

---

## 1. 项目愿景和定位

### 1.1 系统定位

**项目愿景**：
```
合同模板动态渲染系统 - 配置驱动、动态渲染、数据比对的一体化平台

核心价值：
- 业务人员通过简单配置界面定义模板结构
- 系统根据配置动态渲染合同录入界面
- IT人员配置数据源实现字段自动填充
- 外部数据自动映射并对比差异
```

**定位澄清**：
```
这不是低代码平台，不是拖拽式设计器，而是：

✅ 配置驱动的模板管理系统
   - 业务人员：通过表单填写配置模板
   - IT人员：配置数据源和映射规则

✅ 动态渲染的合同录入系统
   - 根据模板配置动态生成录入界面
   - 执行配置规则（显隐、必填、只读）

✅ E2E验证工具（开发辅助）
   - 可视化配置界面：用于验证后端API
   - 拖拽组件验证配置功能是否正确
   - 必须包含在V1.0中用于完整验证
```

---

## 2. V1.0边界定义

### 2.1 V1.0核心闭环（必须包含）

**优先级1：模板配置 + 合同录入 + E2E验证工具**

```
模板管理：
- 创建模板（编码、名称、描述）
- 停用/启用模板
- 当前版本管理

版本管理：
- 创建草稿版本
- 发布版本（只有草稿能发布）
- 版本状态管理（DRAFT/PUBLISHED）

配置管理：
- 布局节点配置（树形结构）
- 字段定义配置（路径、类型）
- 组件绑定配置（组件类型、属性）
- 查询配置（数据源、参数、回填规则）
- 明细表配置（增删改配置）
- 规则配置（显隐、必填、只读）

数据源配置：
- 静态数据源（手动JSON）
- 字典数据源（系统字典）
- HTTP数据源（外部API）
- 平台数据源（内部服务）
- 内部数据源（业务查询）

合同录入：
- 新增合同（根据配置动态渲染）
- 编辑合同（从快照回显）
- 保存合同（生成快照、建立索引）
- 明细表操作（增行、删行、编辑行）
- 查询选择（执行查询、回填字段）

外部数据接入：
- 外部消息接收（幂等入库）
- 数据映射转换（外部→统一JSON）
- 数据对比（逐字段差异）
- 对比结果应用（自动/手动确认）

E2E验证工具：
- 可视化配置界面（左侧预览+右侧配置面板）
- 拖拽组件验证（验证组件配置API）
- 动态渲染验证（验证渲染引擎）
- 规则执行验证（验证规则引擎）
- 数据绑定验证（验证数据流转）
```

### 2.2 V1.0不做（明确边界）

**完全不在范围内的功能**：
```
❌ 工作流审批（审批系统负责）
❌ 多人协作草稿（协作系统负责）
❌ 多租户隔离（平台层负责）
❌ 权限管理（权限系统负责）
❌ 用户管理（用户中心负责）
❌ 组织架构（组织系统负责）
```

**性能优化（推迟到V1.1/V1.2）**：
```
❌ Redis缓存
❌ MQ消息队列
❌ 真实物理分表
❌ 多实例部署
```

---

## 3. 简洁架构规则

### 3.1 架构核心规则（CLAUDE.md摘要）

```
【架构核心规则】
1. 四层架构：Controller → Service → DomainService → Repository
2. 依赖倒置：接口在domain层，实现在infrastructure层
```

### 3.2 详细架构规则拆分

拆分到以下规则文件（渐进式披露）：

```
.claude/rules/
├─ 01-project-vision.md        # 项目愿景和边界
├─ 02-architecture.md          # 四层架构规则
├─ 03-package-structure.md     # 包结构规则
├─ 04-layer-responsibility.md  # 各层职责规则
├─ 05-dependency-inversion.md  # 依赖倒置规则
└─ 06-conversion.md            # 对象转换规则
```

**详细内容在各规则文件中定义**

---

## 4. 命名规范规则

### 4.1 命名核心规则（CLAUDE.md摘要）

```
【命名核心规则】
Google风格：包名小写点分隔，类名大驼峰，方法名小驼峰动词开头
```

### 4.2 详细命名规则拆分

拆分到以下规则文件（渐进式披露）：

```
.claude/rules/
├─ 07-naming-package.md        # 包命名规则
├─ 08-naming-class.md          # 类命名规则
├─ 09-naming-method.md         # 方法命名规则
├─ 10-naming-field.md          # 字段命名规则
├─ 11-naming-database.md       # 数据库字段命名
└─ 12-naming-special.md        # 特殊后缀规则
```

**详细内容在各规则文件中定义**

---

## 5. 验证策略规则

### 5.1 验证核心规则（CLAUDE.md摘要）

```
【验证核心规则】
分层验证：Repository(H2) → Service(Mock) → Controller(MockMvc) → 前端

验证优先级：
1. 模板配置 + 合同录入（最小闭环）
2. 查询配置 + 数据源（增强功能）
3. 明细表 + 外部数据（高级功能）
```

### 5.2 详细验证规则拆分

拆分到以下规则文件（渐进式披露）：

```
.claude/rules/
├─ 13-testing-repository.md    # Repository层验证规则
├─ 14-testing-service.md       # Service层验证规则
├─ 15-testing-controller.md    # Controller层验证规则
├─ 16-testing-frontend.md      # 前端界面验证规则
├─ 17-testing-chrome-devtools.md # chrome-devtools使用规则
├─ 18-testing-h2-database.md   # H2数据库配置规则
├─ 19-testing-mock-strategy.md # Mock策略规则
├─ 20-validation-priority1.md  # 优先级1验证步骤
├─ 21-validation-priority2.md  # 优先级2验证步骤
└─ 22-validation-priority3.md  # 优先级3验证步骤
```

**详细内容在各规则文件中定义**

---

## 6. 技术栈规则

### 6.1 技术栈核心规则（CLAUDE.md摘要）

```
【技术栈核心规则】
Java 21 + Spring Boot 3.5.14 + MyBatis-Plus 3.5.5
合理使用工具：MapStruct、Lombok
前端原生或可选框架：Alpine.js/Vue
```

### 6.2 详细技术栈规则拆分

拆分到以下规则文件（渐进式披露）：

```
.claude/rules/
├─ 23-tech-stack.md            # 技术栈规范
├─ 24-dependency-management.md # 依赖管理规范
├─ 25-mapstruct-guide.md       # MapStruct使用指南
├─ 26-lombok-guide.md          # Lombok使用指南
└─ 27-frontend-tech.md         # 前端技术选择
```

**详细内容在各规则文件中定义**

---

## 7. CLAUDE.md文件结构设计

### 7.1 CLAUDE.md核心内容（仅5条规则）

```markdown
# 合同模板动态渲染系统

## 项目愿景
合同模板动态渲染系统：配置驱动、动态渲染、数据比对

## 架构核心规则
1. 四层架构：Controller → Service → DomainService → Repository
2. 依赖倒置：接口在domain层，实现在infrastructure层

## 命名核心规则
Google风格：包名小写点分隔，类名大驼峰，方法名小驼峰动词开头

## 验证核心规则
分层验证：Repository(H2) → Service(Mock) → Controller(MockMvc) → 前端
验证优先级：模板配置→查询配置→明细表

## 技术栈核心规则
Java 21 + Spring Boot 3.5.14 + MyBatis-Plus 3.5.5 + MapStruct + Lombok

## 详细规则
查看 .claude/rules/ 目录下的规则文件（渐进式披露）
```

---

## 8. 规则文件总数

共22个规则文件（细粒度拆分）：

```
项目愿景和边界：01-project-vision.md
架构规则：02-06（5个文件）
命名规则：07-12（6个文件）
验证规则：13-22（10个文件）
技术栈规则：23-27（5个文件）

总计：22个详细规则文件 + 1个CLAUDE.md核心摘要
```

---

## 9. 实施计划

### Task 1: 创建CLAUDE.md核心文件
- 文件：`/home/wula/IdeaProjects/dymic/CLAUDE.md`
- 内容：仅包含5条核心规则摘要

### Task 2: 创建22个详细规则文件
- 目录：`.claude/rules/`
- 每个文件只关注一个主题
- 采用渐进式披露设计

### Task 3: 提交到git
- 提交CLAUDE.md和所有规则文件
- 提交信息：`docs: add CLAUDE.md project rules with progressive disclosure`

---

## 10. 自检清单

- ✅ 项目愿景明确
- ✅ V1.0边界清晰（要做什么、不做什么）
- ✅ 架构规则核心摘要（仅2条）
- ✅ 命名规则核心摘要（仅1条）
- ✅ 验证规则核心摘要（仅1条）
- ✅ 技术栈规则核心摘要（仅1条）
- ✅ 详细规则渐进式披露（22个文件）
- ✅ 统一在本项目内（不引用外部文档）
- ✅ 每个规则文件只关注一个主题

---

**设计完成，等待用户审核**