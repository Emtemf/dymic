# COLLAPSE和GRID组件修复设计

**日期**: 2026-06-08  
**范围**: 修复COLLAPSE（折叠面板）和GRID（栅格布局）组件的保存、加载、渲染、拖拽功能

---

## 问题根因

### 问题1：COLLAPSE预览不出来

| 环节 | 问题 | 影响 |
|-----|------|------|
| **保存** | `extractLayoutNodes` 不保存 `panels` 配置 | 后端propsJson缺少面板数据 |
| **加载** | `buildComponentTree` 只映射基础属性，panels丢失 | 前端拿到空面板列表 |
| **渲染** | `renderCollapseComponent` 无点击交互，静态展示 | 用户无法展开/折叠 |
| **拖拽** | 无法拖拽子组件到COLLAPSE面板内 | 业务无法配置面板内容 |

### 问题2：GRID子组件问题

| 环节 | 问题 | 影响 |
|-----|------|------|
| **保存** | 子组件无 `gridColumn` 属性 | 无法记录网格位置 |
| **渲染** | 使用innerHTML + renderComponentPreview，破坏事件绑定 | 子组件无法点击选中 |
| **拖拽** | 无网格单元格定位逻辑 | 子组件随机分配，非可视化定位 |

---

## 设计方案

### 方案选择

**方案B：纯propsJson存储**

- 所有GRID/COLLAPSE配置存储在 `propsJson` 字段
- 使用openGauss JSONB查询语法（`@>` 包含查询）
- GIN索引命中验证

**理由**：
- 符合req设计，灵活扩展
- 展示openGauss JSONB特性
- 后端零改动，Entity已有 `propsJson` String字段

---

## 第1部分：前端保存逻辑

**文件**: `src/main/resources/static/config/js/config-api.js`  
**函数**: `extractLayoutNodes` (第60-96行)

**当前代码**：
```javascript
// 只保存这些
if (component.span !== undefined) propsObj.span = component.span;
if (component.offset !== undefined) propsObj.offset = component.offset;
if (component.gutter !== undefined) propsObj.gutter = component.gutter;
if (component.columns !== undefined) propsObj.columns = component.columns;
```

**修复代码**：
```javascript
// 基础布局属性
if (component.span !== undefined) propsObj.span = component.span;
if (component.offset !== undefined) propsObj.offset = component.offset;
if (component.gutter !== undefined) propsObj.gutter = component.gutter;

// 容器组件的特殊配置
if (component.panels !== undefined) propsObj.panels = component.panels;
if (component.tabs !== undefined) propsObj.tabs = component.tabs;

// GRID布局属性
if (component.type === 'GRID' && component.columns !== undefined) {
    propsObj.columns = component.columns;
}

// GRID子组件位置
if (component.gridColumn !== undefined) propsObj.gridColumn = component.gridColumn;
if (component.gridRow !== undefined) propsObj.gridRow = component.gridRow;

// DETAIL_TABLE列定义
if (component.type === 'DETAIL_TABLE' && component.columns !== undefined) {
    propsObj.columns = component.columns;
}
```

**数据结构示例**：

COLLAPSE的propsJson：
```json
{
  "panels": [
    {
      "name": "面板1",
      "code": "panel1",
      "children": ["comp_123", "comp_456"]
    },
    {
      "name": "面板2",
      "code": "panel2",
      "children": ["comp_789"]
    }
  ]
}
```

GRID子组件的propsJson：
```json
{
  "gridColumn": 0,
  "gridRow": 0,
  "placeholder": "请输入"
}
```

---

## 第2部分：前端加载逻辑

**文件**: `src/main/resources/static/config/js/config-api.js`  
**函数**: `buildComponentTree` (第219-243行)

**当前问题**：
- `parsePropsJson` 正确解析
- 但只映射span/offset/gutter到组件属性
- panels/tabs/gridColumn丢失

**修复代码**：
```javascript
function buildComponentTree(schemaData) {
    const layoutNodes = schemaData.layoutNodes || [];
    
    if (layoutNodes.length === 0) return null;
    
    function convertNode(node) {
        let props = parsePropsJson(node.propsJson);
        
        return {
            id: String(node.id),
            type: node.nodeType,
            name: node.nodeName,
            code: node.nodeCode,
            // 关键：展开所有props属性
            ...props,
            children: (node.children || []).map(c => convertNode(c))
        };
    }
    
    return convertNode(layoutNodes[0]);
}
```

**关键改动**：用 `...props` 展开运算符，所有propsJson属性映射到组件。

---

## 第3部分：前端渲染交互

### 3.1 COLLAPSE折叠面板

**文件**: `src/main/resources/static/config/js/preview-renderer.js`  
**函数**: `renderCollapseComponent` (第280-330行)

**修复要点**：

| 特性 | 实现 |
|-----|------|
| **独立模式** | 每个面板单独控制，可同时展开多个 |
| **默认展开** | 初始状态全部可见（用户可折叠） |
| **点击交互** | 面板标题绑定click事件 |
| **CSS动画** | transform + transition平滑展开/折叠 |
| **子组件渲染** | 通过ID查找并渲染到面板内容区 |

**核心代码**：
```javascript
function renderCollapseComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    // 渲染面板结构
    if (component.panels && component.panels.length > 0) {
        component.panels.forEach((panel, index) => {
            const isExpanded = panel.expanded !== false;
            
            collapseHTML += `
                <div class="collapse-panel" data-panel-index="${index}">
                    <div class="panel-header">
                        <i class="fas fa-chevron-${isExpanded ? 'down' : 'right'}"></i>
                        <span>${panel.name}</span>
                    </div>
                    <div class="panel-content" style="display: ${isExpanded ? 'block' : 'none'}">
                    </div>
                </div>
            `;
        });
    }

    element.innerHTML = collapseHTML;

    // 渲染子组件
    component.panels?.forEach((panel, index) => {
        if (panel.children?.length > 0) {
            const contentContainer = element.querySelectorAll('.panel-content')[index];
            panel.children.forEach(childId => {
                const child = findComponentById(childId);
                if (child) {
                    contentContainer.appendChild(renderComponent(child));
                }
            });
        }
    });

    // 绑定点击事件（独立模式）
    element.querySelectorAll('.panel-header').forEach(header => {
        header.addEventListener('click', (e) => {
            e.stopPropagation();
            const panel = header.parentElement;
            const content = panel.querySelector('.panel-content');
            const icon = header.querySelector('i');
            
            const isExpanded = content.style.display !== 'none';
            content.style.display = isExpanded ? 'none' : 'block';
            icon.className = `fas fa-chevron-${isExpanded ? 'right' : 'down'}`;
        });
    });

    return element;
}
```

---

### 3.2 GRID栅格布局

**文件**: `src/main/resources/static/config/js/preview-renderer.js`  
**函数**: `renderGridComponent` (第175-220行)

**当前问题**：
- 使用 `innerHTML` + `renderComponentPreview`
- 破坏子组件事件绑定，无法点击选中

**修复要点**：

| 特性 | 实现 |
|-----|------|
| **DOM操作** | createElement + appendChild，保留事件绑定 |
| **网格单元格** | 每个列创建独立div.grid-cell |
| **子组件按列分配** | 根据 `gridColumn` 属性分配到对应列 |
| **拖拽提示** | 空列显示"拖拽组件到第N列" |

**核心代码**：
```javascript
function renderGridComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    const columns = component.columns || 2;
    const gutter = component.gutter || 16;

    // 创建网格容器
    const gridContainer = document.createElement('div');
    gridContainer.style.cssText = `
        display: grid;
        grid-template-columns: repeat(${columns}, 1fr);
        gap: ${gutter}px;
    `;

    // 创建网格单元格
    for (let i = 0; i < columns; i++) {
        const cell = document.createElement('div');
        cell.className = 'grid-cell';
        cell.dataset.gridColumn = i;
        cell.style.cssText = `
            min-height: 80px;
            border: 1px dashed #d1d5da;
            background: white;
        `;
        
        // 渲染该列的子组件
        const columnChildren = component.children?.filter(c => c.gridColumn === i) || [];
        if (columnChildren.length > 0) {
            columnChildren.forEach(child => {
                cell.appendChild(renderComponent(child));
            });
        } else {
            cell.innerHTML = `<div style="padding:20px;text-align:center;color:#b4b4b4;">
                拖拽组件到第${i+1}列
            </div>`;
        }
        
        gridContainer.appendChild(cell);
    }

    element.appendChild(gridContainer);
    return element;
}
```

---

## 第4部分：拖拽定位逻辑

**文件**: `src/main/resources/static/config/js/drag-drop.js`

### 4.1 识别GRID网格单元格

**改动函数**: `getDropTarget` (第121-145行)

**新增逻辑**：
```javascript
function getDropTarget(event) {
    const elements = document.elementsFromPoint(event.clientX, event.clientY);

    for (const element of elements) {
        // 优先级1：grid-cell（GRID的网格单元格）
        const gridCell = element.closest('.grid-cell');
        if (gridCell) {
            const gridColumn = gridCell.dataset.gridColumn;
            const gridContainer = gridCell.closest('.component-preview');
            
            if (gridContainer) {
                const gridId = gridContainer.dataset.componentId;
                const gridComponent = findComponentById(gridId);
                
                return {
                    type: 'grid-cell',
                    component: gridComponent,
                    gridColumn: parseInt(gridColumn),
                    element: gridCell
                };
            }
        }

        // 优先级2：collapse-panel（COLLAPSE的面板）
        const collapsePanel = element.closest('.collapse-panel');
        if (collapsePanel) {
            const panelIndex = collapsePanel.dataset.panelIndex;
            const collapseContainer = collapsePanel.closest('.component-preview');
            
            if (collapseContainer) {
                const collapseId = collapseContainer.dataset.componentId;
                const collapseComponent = findComponentById(collapseId);
                
                return {
                    type: 'collapse-panel',
                    component: collapseComponent,
                    panelIndex: parseInt(panelIndex),
                    element: collapsePanel
                };
            }
        }

        // 优先级3：普通容器
        const componentPreview = element.closest('.component-preview');
        if (componentPreview) {
            const componentId = componentPreview.dataset.componentId;
            const component = findComponentById(componentId);

            if (component?.isContainer) {
                return { type: 'append', component: component };
            }
        }
    }

    return null;
}
```

---

### 4.2 处理GRID网格列拖拽

**改动函数**: `addComponentToTarget` (第150-164行)

**新增逻辑**：
```javascript
function addComponentToTarget(component, target) {
    if (target.type === 'grid-cell') {
        // GRID网格列拖拽
        component.gridColumn = target.gridColumn;
        component.gridRow = 0; // 单行GRID
        
        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);
        
        renderPreview();
        selectComponent(component.id);
        saveState();
        
    } else if (target.type === 'collapse-panel') {
        // COLLAPSE面板拖拽
        if (!target.component.panels[target.panelIndex].children) {
            target.component.panels[target.panelIndex].children = [];
        }
        target.component.panels[target.panelIndex].children.push(component);
        
        renderPreview();
        selectComponent(component.id);
        saveState();
        
    } else if (target.type === 'append') {
        // 普通容器拖拽
        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);
        
        renderPreview();
        selectComponent(component.id);
        saveState();
    }
}
```

---

### 4.3 拖拽视觉反馈

**改动函数**: `handleDragOver` (第49-63行)

**新增逻辑**：
```javascript
function handleDragOver(event) {
    event.preventDefault();
    
    // 清除之前的高亮
    document.querySelectorAll('.grid-cell-drop-target').forEach(el => {
        el.classList.remove('grid-cell-drop-target');
        el.style.background = '';
        el.style.border = '';
    });
    
    // 检查当前悬停的grid-cell
    const dropTarget = getDropTarget(event);
    if (dropTarget?.type === 'grid-cell') {
        dropTarget.element.classList.add('grid-cell-drop-target');
        dropTarget.element.style.background = 'rgba(102,126,234,0.1)';
        dropTarget.element.style.border = '2px dashed #667eea';
    }
    
    // ...原有代码...
}
```

---

## 第5部分：E2E验证方案

### 验证流程

| 步骤 | 操作 | 验证点 |
|-----|------|--------|
| **1** | 用户创建版本草稿 | 模板选择器 → 版本选择 → 进入设计器 |
| **2** | Claude拖拽COLLAPSE到画布 | Chrome DevTools: `mcp__chrome-devtools__drag` |
| **3** | Claude拖拽INPUT到COLLAPSE面板 | DevTools: 定位到第1个面板，拖拽子组件 |
| **4** | Claude点击保存按钮 | DevTools: `click` 保存按钮，等待API响应 |
| **5** | Claude刷新页面 | DevTools: `navigate_page` reload |
| **6** | Claude验证COLLAPSE渲染 | DevTools: `take_screenshot`，验证面板展开/折叠交互 |
| **7** | Claude查询数据库 | SQL: `SELECT propsJson FROM t_ui_layout_node WHERE nodeType='COLLAPSE'` |
| **8** | Claude验证openGauss JSONB特性 | SQL: `propsJson @> '{"panels":[{"name":"面板1"}]}'` + GIN索引命中 |

---

### SQL验证脚本

**查询COLLAPSE数据**：
```sql
-- 查询propsJson是否包含panels
SELECT 
    id,
    node_code,
    node_name,
    props_json
FROM t_ui_layout_node
WHERE node_type = 'COLLAPSE'
AND template_version_id = {versionId};
```

**openGauss JSONB特性验证**：
```sql
-- 1. JSONB包含查询
SELECT * FROM t_ui_layout_node 
WHERE props_json::jsonb @> '{"panels":[{"name":"面板1"}]}'::jsonb;

-- 2. JSONB路径查询
SELECT 
    id,
    props_json::jsonb->'panels'->0->>'name' as first_panel_name
FROM t_ui_layout_node 
WHERE node_type = 'COLLAPSE';

-- 3. GIN索引命中验证
EXPLAIN ANALYZE
SELECT * FROM t_ui_layout_node 
WHERE props_json::jsonb @> '{"panels":[]}'::jsonb;
-- 预期结果：Bitmap Index Scan on idx_layout_props_gin
```

---

### 验证GRID流程

| 步骤 | 操作 | 验证点 |
|-----|------|--------|
| **1** | 拖拽GRID（2列）到画布 | DevTools: drag GRID组件 |
| **2** | 拖拽INPUT到第1列 | DevTools: 定位grid-cell gridColumn=0 |
| **3** | 拖拽SELECT到第2列 | DevTools: 定位grid-cell gridColumn=1 |
| **4** | 保存 + 刷新 | 同COLLAPSE流程 |
| **5** | 验证子组件分配 | DevTools: screenshot验证左列INPUT右列SELECT |
| **6** | 查询数据库 | SQL验证propsJson包含gridColumn=0/1 |

---

## 后端兼容性分析

**结论**：已完全兼容 ✅

| 层级 | 字段 | 兼容性 |
|-----|------|--------|
| **Entity** | `String propsJson` | 可存任意JSON |
| **Domain** | `String propsJson` | 同上 |
| **Service** | `node.setPropsJson(dto.getPropsJson())` | 直接保存，不解析 |

**关键代码**（LayoutNodeService.java:55-57）：
```java
if (dto.getPropsJson() != null) {
    node.setPropsJson(dto.getPropsJson());
}
```

前端传什么，后端存什么到JSONB字段。

---

## 改动文件清单

| 文件 | 改动内容 |
|-----|---------|
| `config-api.js` | extractLayoutNodes 增加 panels/tabs/gridColumn 保存 |
| `config-api.js` | buildComponentTree 使用 ...props 展开 |
| `preview-renderer.js` | renderCollapseComponent 添加点击交互+子组件渲染 |
| `preview-renderer.js` | renderGridComponent 改用DOM操作+网格单元格 |
| `drag-drop.js` | getDropTarget 识别grid-cell和collapse-panel |
| `drag-drop.js` | addComponentToTarget 处理GRID和COLLAPSE拖拽 |
| `drag-drop.js` | handleDragOver 添加网格单元格高亮反馈 |

---

## 成功标准

| 标准 | 验证方式 |
|-----|---------|
| **E2E闭环** | Chrome DevTools完整操作：拖拽→保存→刷新→验证 |
| **数据库持久化** | SQL查询propsJson包含panels/gridColumn数据 |
| **openGauss特性** | JSONB `@>` 查询 + GIN索引命中（EXPLAIN ANALYZE） |
| **渲染交互** | COLLAPSE展开/折叠有动画，GRID子组件可点击选中 |
| **拖拽定位** | 业务拖拽到指定网格列，数据正确保存 |