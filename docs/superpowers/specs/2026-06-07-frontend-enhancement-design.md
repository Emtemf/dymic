# 前端功能增强设计规范

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 增强模板设计器前端功能，实现完整的表单布局配置、保存和查询验证流程

**Architecture:** 基于现有组件库扩展，增加 GRID/ROW/COL 表格布局组件，完善保存和查询 API 集成

**Tech Stack:** 原生 HTML/CSS/JS + Bootstrap 5.3 + 后端 REST API

---

## 1. 问题分析

### 1.1 当前问题

| 问题 | 描述 | 影响 |
|------|------|------|
| 缺少表格布局组件 | 现有 GRID 组件不支持 ROW/COL 结构，无法实现左右两栏布局 | 用户无法配置左右分栏表单 |
| 属性配置交互问题 | 用户反馈属性配置面板点击无反应 | 配置流程中断 |
| 保存/查询流程不完整 | 缺少完整的端到端验证 | 无法确认功能正常 |

### 1.2 需求分析

根据 `req/req.md`，布局节点类型应包括：

```
PAGE、GRID、ROW、COL、CARD、TABS、TAB_PANE、MODAL、FIELD、DETAIL_TABLE、QUERY、ACTION
```

当前 `component-library.js` 已有：
- `PAGE`、`CARD`、`GRID`、`TAB`、`COLLAPSE` - 布局组件
- 缺少 `ROW`、`COL` 组件用于表格布局

### 1.3 表格布局设计

目标效果：
```
┌─────────────────────────────────────┐
│ GRID (2列)                          │
│ ┌─────────────┐ ┌─────────────┐    │
│ │ COL (第1列) │ │ COL (第2列) │    │
│ │ 字段A       │ │ 字段B       │    │
│ │ 字段C       │ │ 字段D       │    │
│ └─────────────┘ └─────────────┘    │
└─────────────────────────────────────┘
```

---

## 2. 解决方案

### 2.1 组件库增强

**新增组件类型：**

```javascript
// 在 component-library.js 中添加
ROW: {
    name: '行',
    icon: 'fa-grip-lines',
    category: 'layout',
    isContainer: true,
    defaultConfig: {
        name: '行',
        code: 'row_1',
        children: [],
        gutter: 16  // 列间距
    }
},
COL: {
    name: '列',
    icon: 'fa-grip-lines-vertical',
    category: 'layout',
    isContainer: true,
    defaultConfig: {
        name: '列',
        code: 'col_1',
        span: 12,  // 栅格列数 (24栅格系统)
        offset: 0,
        children: []
    }
}
```

**修改 GRID 组件：**

```javascript
GRID: {
    name: '栅格',
    icon: 'fa-th',
    category: 'layout',
    isContainer: true,
    defaultConfig: {
        name: '栅格布局',
        code: 'grid_1',
        description: '',
        columns: 2,  // 列数（用于显示）
        gutter: 16,  // 间距
        children: [] // 存放 ROW 组件
    }
}
```

### 2.2 保存功能完善

**保存流程：**

```
用户点击保存 -> 收集 DesignerState.templateConfig -> 
调用 POST /api/templates/{templateId}/versions/{versionId}/schema ->
后端保存到数据库 -> 返回成功
```

**API 数据格式：**

```json
{
  "layoutNodes": [...],
  "fieldDefs": [...],
  "fieldComponents": [...],
  "queryConfigs": [...],
  "actionConfigs": [...]
}
```

### 2.3 查询功能验证

**查询流程：**

```
用户点击查询按钮 -> 打开查询弹窗 ->
调用 POST /api/ui/query/{queryId}/execute ->
后端根据 query_config 查询数据 -> 返回结果列表 ->
用户选择 -> 按回填规则填充表单
```

---

## 3. 实现计划

### 任务分解

**Task 1: 增强 component-library.js**
- 添加 ROW 组件定义
- 添加 COL 组件定义
- 更新 GRID 组件配置

**Task 2: 增强 preview-renderer.js**
- 添加 renderRowComponent 函数
- 添加 renderColComponent 函数
- 更新 renderGridComponent 函数

**Task 3: 增强 property-panel.js**
- 添加 renderRowSpecificConfig 函数
- 添加 renderColSpecificConfig 函数
- 更新栅格配置面板

**Task 4: 更新 template-designer.html**
- 在组件库 UI 中添加 ROW、COL 组件项

**Task 5: 完善 config-api.js**
- 实现 saveConfig 函数调用后端 API
- 实现 loadConfig 函数加载模板配置

**Task 6: E2E 验证**
- 使用 Chrome DevTools 验证组件拖拽
- 验证属性配置面板交互
- 验证保存和加载流程

---

## 4. 验收标准

### 4.1 功能验收

- [ ] 可以从组件库拖拽 ROW 和 COL 组件到预览区
- [ ] ROW 和 COL 组件显示正确的属性配置面板
- [ ] 可以配置 COL 的 span 属性（控制列宽）
- [ ] 保存配置后可以重新加载
- [ ] 查询功能可以正常执行

### 4.2 UI 验收

- [ ] 组件库中显示 ROW 和 COL 组件图标
- [ ] 预览区正确渲染表格布局
- [ ] 属性面板交互流畅，无卡顿

### 4.3 数据验收

- [ ] 保存的 JSON 格式符合后端 API 规范
- [ ] 加载配置后组件树结构正确

---

## 5. 风险和依赖

### 5.1 风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 后端 API 不完整 | 保存/加载失败 | 先实现前端，使用 localStorage 模拟 |
| 组件嵌套复杂 | 渲染性能问题 | 限制嵌套层级 |

### 5.2 依赖

- 后端 API: `/api/templates/{id}/versions/{vid}/schema`
- 后端 API: `/api/ui/query/{queryId}/execute`
- Bootstrap 5.3 CSS 框架
- Font Awesome 图标库
