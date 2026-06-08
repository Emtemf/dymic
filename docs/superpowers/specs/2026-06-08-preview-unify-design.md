# 预览按钮统一渲染设计

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让右上角预览按钮展示和左侧预览区一致的组件渲染效果（干净表单形式）

**Architecture:** 统一渲染路径——`renderComponent()` 加 mode 参数区分设计器模式和预览模式，去掉独立的 `renderComponentHTML()` 函数和 iframe，预览弹窗改为 div 直接渲染。

**Tech Stack:** 原生 JavaScript，无新增依赖

---

## 问题

`renderComponentHTML()`（config-api.js:573-689）是一个独立的 HTML 字符串生成函数，只实现了部分组件类型：

| 组件 | 左侧预览区 | 预览按钮 |
|------|-----------|---------|
| GRID（栅格） | 多列布局 | 平铺无分列 |
| COLLAPSE（折叠面板） | 折叠/展开 | 空白（未实现） |
| TAB（页签） | 页签切换 | 空白（未实现） |
| SUPPLIER_SELECT | 选择器 | 空白（未实现） |
| ATTACHMENT | 上传区域 | 空白（未实现） |
| IMAGE | 图片上传 | 空白（未实现） |
| QUERY_DIALOG | 查询按钮 | 空白（未实现） |

## 方案

### 核心改动

1. **`renderComponent(component, mode='designer')`** 加第二个参数 `mode`
   - `'designer'`：当前行为（组件边框、拖拽提示、空状态文字）
   - `'preview'`：干净表单（无边框、无拖拽提示、真实表单元素）

2. **各 render 函数**根据 mode 切换样式：
   - designer：`border: 1px dashed`、组件名标签、拖拽提示
   - preview：无边框、label + 表单控件的标准表单样式

3. **预览弹窗**改为 div 渲染（去掉 iframe）
   - `previewTemplate()` 调用 `renderComponent(root, 'preview')` 直接插入弹窗 div
   - COLLAPSE/TAB 的交互事件自动生效

4. **删除 `renderComponentHTML()` 和 `generatePreviewHTML()`**

### 文件改动

| 文件 | 改动 |
|------|------|
| `preview-renderer.js` | `renderComponent()` 及各 render 函数加 mode 参数 |
| `config-api.js` | `previewTemplate()` 改为 div 渲染，删掉 `renderComponentHTML()` 和 `generatePreviewHTML()` |
| `template-designer.html` | 预览弹窗 `iframe` 改为 `div` |

### 不改动

- 左侧预览区（designer mode 默认行为不变）
- 后端逻辑（纯前端改动）
- 拖拽逻辑（drag-drop.js）
- 组件库定义（component-library.js）
