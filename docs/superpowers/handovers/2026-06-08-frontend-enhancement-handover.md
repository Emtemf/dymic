# 前端功能增强交接文档

> **交接时间**: 2026-06-08
> **任务状态**: 前端功能已完成，后端API待修复
> **项目路径**: `/home/wula/IdeaProjects/dymic`

---

## 1. 背景和决策过程

### 1.1 需求背景

根据 `req/req.md` 要求，模板设计器需要支持 ROW/COL 表格布局组件，实现左右两栏表单布局。原组件库只有 PAGE、CARD、GRID、TAB、COLLAPSE 五种布局组件，缺少行(Row)和列(Col)组件来构建表格布局。

### 1.2 技术决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 栅格系统 | 24栅格 | 主流框架标准，Ant Design、Element UI 均采用 |
| ROW 组件 | Flexbox 布局 | 现代CSS方案，支持响应式 |
| COL 组件 | span + offset 属性 | 与栅格系统配合，灵活控制列宽 |
| 数据结构 | 嵌套 children | 支持无限层级嵌套 |

### 1.3 设计规范

完整设计文档：`docs/superpowers/specs/2026-06-07-frontend-enhancement-design.md`

目标布局效果：
```
┌─────────────────────────────────────┐
│ PAGE                                │
│ ┌─────────────────────────────────┐ │
│ │ ROW                             │ │
│ │ ┌─────────────┐ ┌─────────────┐ │ │
│ │ │ COL (span=12)│ │ COL (span=12)│ │ │
│ │ │ 字段A       │ │ 字段B       │ │ │
│ │ └─────────────┘ └─────────────┘ │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

---

## 2. 完整改动内容

### 2.1 文件清单

| 文件 | 路径 | 改动类型 |
|------|------|---------|
| component-library.js | `src/main/resources/static/config/js/` | 修改 |
| preview-renderer.js | `src/main/resources/static/config/js/` | 修改 |
| property-panel.js | `src/main/resources/static/config/js/` | 修改 |
| config-api.js | `src/main/resources/static/config/js/` | 修改 |
| template-designer.html | `src/main/resources/static/config/` | 修改 |

### 2.2 详细代码改动

#### 文件1: `component-library.js`

**位置**: 第53-79行

**新增 ROW 组件定义**:
```javascript
ROW: {
    name: '行',
    icon: 'fa-grip-lines',
    category: 'layout',
    isContainer: true,
    defaultConfig: {
        name: '行',
        code: 'row_1',
        description: '',
        gutter: 16,  // 列间距
        children: []
    }
}
```

**新增 COL 组件定义**:
```javascript
COL: {
    name: '列',
    icon: 'fa-grip-lines-vertical',
    category: 'layout',
    isContainer: true,
    defaultConfig: {
        name: '列',
        code: 'col_1',
        description: '',
        span: 12,    // 栅格列数 (24栅格系统，12=半行)
        offset: 0,   // 偏移量
        children: []
    }
}
```

#### 文件2: `preview-renderer.js`

**新增 renderRowComponent 函数**:
```javascript
/**
 * 渲染行组件
 * @param {Object} component - 组件配置
 * @returns {string} HTML字符串
 */
function renderRowComponent(component) {
    const gutter = component.gutter || 16;
    const children = component.children || [];
    
    let childrenHtml = '';
    if (children.length > 0) {
        childrenHtml = children.map(child => renderComponent(child)).join('');
    } else {
        childrenHtml = '<div class="empty-placeholder" style="color:#999;padding:20px;text-align:center;">拖拽列组件到这里</div>';
    }
    
    return `
        <div class="preview-row" 
             data-id="${component.id}" 
             data-type="ROW"
             style="display:flex;flex-wrap:wrap;margin:0 -${gutter/2}px;min-height:60px;border:1px dashed #ddd;padding:10px;background:#fafafa;">
            ${childrenHtml}
        </div>
    `;
}
```

**新增 renderColComponent 函数**:
```javascript
/**
 * 渲染列组件
 * @param {Object} component - 组件配置
 * @returns {string} HTML字符串
 */
function renderColComponent(component) {
    const span = component.span || 12;
    const offset = component.offset || 0;
    const children = component.children || [];
    
    // 24栅格系统计算宽度
    const widthPercent = (span / 24) * 100;
    const offsetPercent = (offset / 24) * 100;
    
    let childrenHtml = '';
    if (children.length > 0) {
        childrenHtml = children.map(child => renderComponent(child)).join('');
    } else {
        childrenHtml = '<div class="empty-placeholder" style="color:#999;padding:20px;text-align:center;">拖拽组件到这里</div>';
    }
    
    return `
        <div class="preview-col" 
             data-id="${component.id}" 
             data-type="COL"
             style="width:${widthPercent}%;margin-left:${offsetPercent}%;padding:10px;min-height:50px;border:1px dashed #ccc;background:#fff;box-sizing:border-box;">
            <div style="font-size:12px;color:#666;margin-bottom:5px;">列 (span=${span})</div>
            ${childrenHtml}
        </div>
    `;
}
```

**更新 renderComponent 函数** (在 switch 语句中添加):
```javascript
case 'ROW':
    return renderRowComponent(component);
case 'COL':
    return renderColComponent(component);
```

#### 文件3: `property-panel.js`

**新增 renderRowSpecificConfig 函数**:
```javascript
/**
 * 渲染行组件特有配置
 * @param {Object} component - 组件配置
 * @returns {string} HTML字符串
 */
function renderRowSpecificConfig(component) {
    return `
        <div class="config-section">
            <label class="form-label">列间距</label>
            <select class="form-select" id="prop-gutter" onchange="updateComponentProperty('gutter', this.value)">
                <option value="8" ${component.gutter === 8 ? 'selected' : ''}>8px</option>
                <option value="16" ${component.gutter === 16 ? 'selected' : ''}>16px</option>
                <option value="24" ${component.gutter === 24 ? 'selected' : ''}>24px</option>
                <option value="32" ${component.gutter === 32 ? 'selected' : ''}>32px</option>
            </select>
        </div>
    `;
}
```

**新增 renderColSpecificConfig 函数**:
```javascript
/**
 * 渲染列组件特有配置
 * @param {Object} component - 组件配置
 * @returns {string} HTML字符串
 */
function renderColSpecificConfig(component) {
    // 生成 span 选项 (1-24)
    const spanOptions = Array.from({length: 24}, (_, i) => i + 1)
        .map(n => `<option value="${n}" ${component.span === n ? 'selected' : ''}>${n}/24 (${((n/24)*100).toFixed(1)}%)</option>`)
        .join('');
    
    // 生成 offset 选项 (0-23)
    const offsetOptions = Array.from({length: 24}, (_, i) => i)
        .map(n => `<option value="${n}" ${component.offset === n ? 'selected' : ''}>${n}/24</option>`)
        .join('');
    
    return `
        <div class="config-section">
            <label class="form-label">列宽 (span)</label>
            <select class="form-select" id="prop-span" onchange="updateComponentProperty('span', parseInt(this.value))">
                ${spanOptions}
            </select>
            <small class="text-muted">24栅格系统，12=50%宽度</small>
        </div>
        <div class="config-section mt-2">
            <label class="form-label">偏移量 (offset)</label>
            <select class="form-select" id="prop-offset" onchange="updateComponentProperty('offset', parseInt(this.value))">
                ${offsetOptions}
            </select>
        </div>
    `;
}
```

**更新 showPropertyForm 函数** (在 switch 语句中添加):
```javascript
case 'ROW':
    specificConfig = renderRowSpecificConfig(component);
    break;
case 'COL':
    specificConfig = renderColSpecificConfig(component);
    break;
```

#### 文件4: `config-api.js`

**saveConfig 函数** (确保正确实现):
```javascript
async function saveConfig() {
    if (!DesignerState.templateConfig) {
        alert('没有可保存的配置');
        return;
    }
    
    const config = DesignerState.templateConfig;
    
    // 构建请求数据
    const requestData = {
        layoutNodes: extractLayoutNodes(config.rootComponent),
        fieldDefs: extractFieldDefs(config.rootComponent),
        fieldComponents: extractFieldComponents(config.rootComponent),
        queryConfigs: [],
        actionConfigs: []
    };
    
    try {
        const response = await fetch(`/api/templates/${DesignerState.templateId}/versions/${DesignerState.versionId}/schema`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });
        
        if (!response.ok) {
            throw new Error(`保存失败: ${response.status}`);
        }
        
        alert('保存成功');
    } catch (error) {
        console.error('保存失败:', error);
        alert('保存失败: ' + error.message);
    }
}
```

#### 文件5: `template-designer.html`

**在组件库布局组件区域添加** (约第120-150行):
```html
<!-- 布局组件区域 -->
<div class="component-category">
    <h6><i class="fa fa-th-large"></i> 布局组件</h6>
    <div class="component-list">
        <!-- 已有组件: PAGE, CARD, GRID -->
        
        <!-- 新增 ROW 组件 -->
        <div class="component-item" 
             draggable="true" 
             data-type="ROW"
             title="行组件，用于水平排列列组件">
            <i class="fa fa-grip-lines"></i>
            <span>行</span>
        </div>
        
        <!-- 新增 COL 组件 -->
        <div class="component-item" 
             draggable="true" 
             data-type="COL"
             title="列组件，使用span控制宽度">
            <i class="fa fa-grip-lines-vertical"></i>
            <span>列</span>
        </div>
        
        <!-- 已有组件: TAB, COLLAPSE -->
    </div>
</div>
```

---

## 3. 实施顺序和依赖关系

### 3.1 依赖关系图

```
component-library.js  (组件定义)
        ↓
preview-renderer.js   (渲染逻辑，依赖组件定义)
        ↓
property-panel.js     (属性配置，依赖渲染)
        ↓
template-designer.html (UI展示，依赖以上)
        ↓
config-api.js         (保存加载，独立)
```

### 3.2 实施顺序

| 步骤 | 文件 | 说明 |
|------|------|------|
| 1 | component-library.js | 添加 ROW/COL 类型定义，无依赖 |
| 2 | preview-renderer.js | 添加渲染函数，依赖步骤1 |
| 3 | property-panel.js | 添加配置面板，依赖步骤1 |
| 4 | template-designer.html | 更新UI，依赖步骤1-3 |
| 5 | config-api.js | 完善保存逻辑，独立 |
| 6 | 同步到target目录 | 关键步骤！见下文 |

### 3.3 重要提醒：Spring Boot 静态资源

**核心问题**: Spring Boot 从 `target/classes/static/` 提供静态文件，不是 `src/main/resources/static/`！

**必须执行的同步命令**:
```bash
# 每次修改前端文件后，必须执行：
cp src/main/resources/static/config/js/component-library.js \
   target/classes/static/config/js/

cp src/main/resources/static/config/js/preview-renderer.js \
   target/classes/static/config/js/

cp src/main/resources/static/config/js/property-panel.js \
   target/classes/static/config/js/

cp src/main/resources/static/config/js/config-api.js \
   target/classes/static/config/js/

cp src/main/resources/static/config/template-designer.html \
   target/classes/static/config/
```

或者一键同步：
```bash
cp -r src/main/resources/static/config/* target/classes/static/config/
```

---

## 4. 验证方法

### 4.1 快速验证脚本

在浏览器开发者工具中执行：
```javascript
// 1. 验证组件库加载
console.log('组件库加载:', typeof ComponentLibrary !== 'undefined');
console.log('ROW组件:', ComponentLibrary.types.ROW);
console.log('COL组件:', ComponentLibrary.types.COL);

// 2. 验证渲染函数
console.log('渲染函数:', typeof renderRowComponent, typeof renderColComponent);

// 3. 添加组件并验证
const page = ComponentLibrary.createComponent('PAGE');
const row = ComponentLibrary.createComponent('ROW');
const col = ComponentLibrary.createComponent('COL');
row.children.push(col);
page.children.push(row);
DesignerState.templateConfig.rootComponent = page;
renderPreview();

// 4. 验证预览区
const preview = document.getElementById('preview-area');
console.log('预览区内容:', preview.innerHTML.includes('preview-row'), preview.innerHTML.includes('preview-col'));

// 5. 验证属性面板
const colElement = document.querySelector('[data-type="COL"]');
if (colElement) colElement.click();
console.log('属性面板:', document.getElementById('property-panel').innerHTML.includes('span'));
```

### 4.2 Chrome DevTools E2E 验证

```bash
# 1. 启动应用
mvn spring-boot:run

# 2. 打开设计器
# 使用 mcp__chrome-devtools__navigate_page 打开
# http://localhost:8888/config/template-designer.html

# 3. 验证组件库
# 使用 mcp__chrome-devtools__take_snapshot 检查组件库
# 应看到 "行" 和 "列" 组件项

# 4. 测试拖拽
# 使用 mcp__chrome-devtools__drag 从组件库拖拽到预览区

# 5. 测试属性配置
# 使用 mcp__chrome-devtools__click 点击组件
# 使用 mcp__chrome-devtools__take_screenshot 检查属性面板

# 6. 测试保存
# 使用 mcp__chrome-devtools__click 点击保存按钮
# 使用 mcp__chrome-devtools__list_network_requests 检查 API 请求
```

### 4.3 预期验证结果

```json
{
    "libraryLoaded": true,
    "layoutTypesAvailable": ["PAGE", "CARD", "GRID", "ROW", "COL", "TAB", "COLLAPSE"],
    "hasRowComponent": true,
    "hasColComponent": true,
    "renderedComponentsCount": 3,
    "hasPageInPreview": true,
    "hasRowInPreview": true,
    "hasColInPreview": true,
    "propertyPanelWorking": true,
    "spanConfigExists": true,
    "offsetConfigExists": true,
    "saveRequestSent": true
}
```

---

## 5. 风险点

### 5.1 已知问题

| 问题 | 状态 | 说明 |
|------|------|------|
| Spring Boot 静态资源缓存 | ✅ 已解决 | 需手动同步到 target 目录 |
| 后端保存 API 返回 500 | ⚠️ 待修复 | 前端请求格式正确，后端需排查 |
| DesignerState 数据结构 | ✅ 已解决 | 使用 rootComponent 而非 children |

### 5.2 潜在风险

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 浏览器缓存 | 修改不生效 | 使用 Ctrl+Shift+R 强制刷新 |
| 组件嵌套过深 | 渲染性能下降 | 限制嵌套层级 <= 5 |
| span 值超过24 | 布局错乱 | 添加校验 clamp(1, 24) |
| 循环引用 | 无限递归 | 添加 visited Set 检测 |

### 5.3 后端待修复

**API端点**: `PUT /api/templates/{templateId}/versions/{versionId}/schema`

**当前状态**: 返回 HTTP 500

**请求数据格式** (前端发送正确):
```json
{
    "layoutNodes": [
        {
            "nodeType": "PAGE",
            "nodeCode": "page_1",
            "nodeName": "新建页面",
            "parentNodeCode": null,
            "sortOrder": 0
        },
        {
            "nodeType": "ROW",
            "nodeCode": "row_1",
            "nodeName": "行",
            "parentNodeCode": "page_1",
            "sortOrder": 0
        }
    ],
    "fieldDefs": [...],
    "fieldComponents": [...]
}
```

**后端排查方向**:
1. 检查 Controller 是否正确接收 `@RequestBody`
2. 检查 Service 层事务处理
3. 检查 Repository 层数据映射
4. 检查数据库约束（外键、唯一键等）

---

## 6. 其他

### 6.1 相关文件路径

```
项目根目录: /home/wula/IdeaProjects/dymic

设计文档: docs/superpowers/specs/2026-06-07-frontend-enhancement-design.md
实现计划: docs/superpowers/plans/2026-06-07-frontend-enhancement.md

前端源码: src/main/resources/static/config/
├── template-designer.html
└── js/
    ├── component-library.js
    ├── preview-renderer.js
    ├── property-panel.js
    └── config-api.js

运行时目录: target/classes/static/config/
(注意: Spring Boot 从这里加载静态资源!)

后端API:
├── controller/TemplateController.java
├── controller/TemplateVersionController.java
└── application/service/TemplateSchemaService.java
```

### 6.2 依赖版本

```
Spring Boot: 3.5.14
Bootstrap: 5.3
Font Awesome: 6.x
Java: 21
```

### 6.3 常用命令

```bash
# 启动开发服务器
mvn spring-boot:run

# 同步静态资源
cp -r src/main/resources/static/config/* target/classes/static/config/

# 查看日志
tail -f /tmp/app.log

# 检查端口
lsof -i :8888
```

### 6.4 参考文档

- 项目规范: `CLAUDE.md`
- 需求文档: `req/req.md`
- 补充需求: `req/supplement.md`
- 技术栈: `.claude/rules/23-tech-stack.md`
- 前端技术: `.claude/rules/27-frontend-tech.md`

---

## 7. 新会话启动提示

**复制以下内容到新会话开始**:

```
请阅读交接文档 docs/superpowers/handovers/2026-06-08-frontend-enhancement-handover.md 继续工作。

当前状态：前端 ROW/COL 组件功能已实现并通过验证，后端保存 API 返回 500 错误待修复。

优先任务：
1. 排查后端 PUT /api/templates/{templateId}/versions/{versionId}/schema 返回 500 的原因
2. 修复后端 API 使保存功能正常工作
3. 验证完整的端到端流程

关键提醒：修改前端文件后必须同步到 target/classes/static/config/ 目录！
```