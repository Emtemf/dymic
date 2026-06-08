# COLLAPSE 和 GRID 组件修复 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 COLLAPSE（折叠面板）和 GRID（栅格布局）组件的保存、加载、渲染、拖拽全流程，使其完整可用。

**Architecture:** 方案B（纯树形结构）— 每个组件一条数据库记录，通过 `parent_id` 构建树形关系；`propsJson` 只存扩展属性（panels 名称、gridColumn 等），`panels[i].children` 和 `tabs[i].children` 只存 ID 引用字符串数组。后端零改动，前端负责 ID 引用与树形 children 的合并。

**Tech Stack:** 原生 JavaScript（ES6+），无框架，Chrome DevTools MCP 用于 E2E 验证。

**设计文档:** `docs/superpowers/specs/2026-06-08-collapse-grid-fix-design.md`
**交接文档:** `docs/superpowers/handover/2026-06-08-collapse-grid-fix-handover.md`

---

## 文件结构

| 文件 | 改动 | 职责 |
|-----|------|------|
| `src/main/resources/static/config/js/config-api.js` | 修改 | extractLayoutNodes 保存 panels/tabs/gridColumn；buildComponentTree 合并 ID 引用 |
| `src/main/resources/static/config/js/preview-renderer.js` | 修改 | renderCollapseComponent 点击交互+全面板渲染；renderGridComponent DOM 操作+网格单元格 |
| `src/main/resources/static/config/js/drag-drop.js` | 修改 | getDropTarget 识别 grid-cell/collapse-panel；addComponentToTarget 处理特殊容器；handleDragOver 高亮反馈 |

无新建文件。后端零改动。

---

## Task 1: extractLayoutNodes — 增加 panels/tabs/gridColumn 保存

**Files:**
- Modify: `src/main/resources/static/config/js/config-api.js:72-76`

- [ ] **Step 1: 修改 extractLayoutNodes 的 propsObj 构建逻辑**

将第 72-76 行的 propsObj 构建代码：

```javascript
const propsObj = {};
if (component.span !== undefined) propsObj.span = component.span;
if (component.offset !== undefined) propsObj.offset = component.offset;
if (component.gutter !== undefined) propsObj.gutter = component.gutter;
if (component.columns !== undefined && component.columns !== null) propsObj.columns = component.columns;
```

替换为：

```javascript
const propsObj = {};
if (component.span !== undefined) propsObj.span = component.span;
if (component.offset !== undefined) propsObj.offset = component.offset;
if (component.gutter !== undefined) propsObj.gutter = component.gutter;
if (component.type === 'GRID' && component.columns !== undefined) {
    propsObj.columns = component.columns;
}
if (component.type === 'DETAIL_TABLE' && component.columns !== undefined) {
    propsObj.columns = component.columns;
}
if (component.panels !== undefined) propsObj.panels = component.panels;
if (component.tabs !== undefined) propsObj.tabs = component.tabs;
if (component.gridColumn !== undefined) propsObj.gridColumn = component.gridColumn;
if (component.gridRow !== undefined) propsObj.gridRow = component.gridRow;
```

- [ ] **Step 2: 在 extractLayoutNodes 中增加 panels/tabs 子组件递归**

在第 90-94 行的子组件递归之后（`component.children` 处理），增加 panels 和 tabs 的递归。找到：

```javascript
    // 递归处理子组件
    if (component.children && component.children.length > 0) {
        component.children.forEach(child => {
            extractLayoutNodes(child, component.id, result, usedCodes);
        });
    }
```

替换为：

```javascript
    // 递归处理子组件
    if (component.children && component.children.length > 0) {
        component.children.forEach(child => {
            extractLayoutNodes(child, component.id, result, usedCodes);
        });
    }

    // 递归处理 panels 子组件（COLLAPSE）
    if (component.panels && component.panels.length > 0) {
        component.panels.forEach(panel => {
            if (panel.children && panel.children.length > 0) {
                panel.children.forEach(child => {
                    const c = typeof child === 'string' ? null : child;
                    if (c) extractLayoutNodes(c, component.id, result, usedCodes);
                });
            }
        });
    }

    // 递归处理 tabs 子组件（TAB）
    if (component.tabs && component.tabs && component.tabs.length > 0) {
        component.tabs.forEach(tab => {
            if (tab.children && tab.children.length > 0) {
                tab.children.forEach(child => {
                    const c = typeof child === 'string' ? null : child;
                    if (c) extractLayoutNodes(c, component.id, result, usedCodes);
                });
            }
        });
    }
```

- [ ] **Step 3: 在浏览器中验证保存逻辑**

Run: 启动应用 `mvn spring-boot:run`，打开设计器，拖入 COLLAPSE 组件，添加 INPUT 到面板，点击保存，打开浏览器 DevTools Console 执行：

```javascript
console.log(JSON.stringify(extractLayoutNodes(DesignerState.templateConfig.rootComponent), null, 2))
```

Expected: 输出的 layoutNodes 数组中，COLLAPSE 节点的 `propsJson` 包含 `panels` 配置，INPUT 节点的 `parentId` 指向 COLLAPSE。

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/static/config/js/config-api.js
git commit -m "fix: extractLayoutNodes saves panels/tabs/gridColumn props"
```

---

## Task 2: buildComponentTree — 合并 ID 引用与树形 children

**Files:**
- Modify: `src/main/resources/static/config/js/config-api.js:219-243`

- [ ] **Step 1: 替换 buildComponentTree 函数**

将第 219-243 行的 `buildComponentTree` 函数整体替换为：

```javascript
function buildComponentTree(schemaData) {
    const layoutNodes = schemaData.layoutNodes || [];

    if (layoutNodes.length === 0) return null;

    const componentMap = {};

    function convertNode(node) {
        let props = parsePropsJson(node.propsJson);

        const component = {
            id: String(node.id),
            type: node.nodeType,
            name: node.nodeName,
            code: node.nodeCode,
            parentId: node.parentId,
            ...props,
            children: (node.children || []).map(c => convertNode(c))
        };

        componentMap[String(node.id)] = component;
        return component;
    }

    layoutNodes.forEach(node => convertNode(node));

    // 合并 ID 引用：panels[i].children 从 ID 字符串替换为完整对象
    Object.values(componentMap).forEach(component => {
        if (component.panels) {
            component.panels.forEach(panel => {
                if (panel.children && panel.children.length > 0 && typeof panel.children[0] === 'string') {
                    panel.children = panel.children.map(id => componentMap[id]).filter(Boolean);
                }
            });
        }
        if (component.tabs) {
            component.tabs.forEach(tab => {
                if (tab.children && tab.children.length > 0 && typeof tab.children[0] === 'string') {
                    tab.children = tab.children.map(id => componentMap[id]).filter(Boolean);
                }
            });
        }
    });

    return layoutNodes[0] ? componentMap[String(layoutNodes[0].id)] : null;
}
```

- [ ] **Step 2: 在浏览器中验证加载逻辑**

Run: 在浏览器中刷新设计器页面，确认已保存的 COLLAPSE+INPUT 配置能正确加载。在 Console 中执行：

```javascript
console.log(JSON.stringify(DesignerState.templateConfig.rootComponent, null, 2))
```

Expected: COLLAPSE 组件的 `panels[0].children` 是完整 INPUT 对象（非 ID 字符串），`children` 数组也包含 INPUT。

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/js/config-api.js
git commit -m "fix: buildComponentTree merges ID refs with tree children"
```

---

## Task 3: renderCollapseComponent — 点击交互 + 全面板子组件渲染

**Files:**
- Modify: `src/main/resources/static/config/js/preview-renderer.js:280-331`

- [ ] **Step 1: 替换 renderCollapseComponent 函数**

将第 280-331 行的 `renderCollapseComponent` 函数整体替换为：

```javascript
function renderCollapseComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;
    element.style.cssText = `
        background: #f6f8fa;
        padding: 10px;
        margin-bottom: 10px;
    `;

    let collapseHTML = `
        <div class="component-preview-label" style="margin-bottom: 10px;">
            <i class="fas fa-chevron-down"></i> ${component.name}
        </div>
    `;

    if (component.panels && component.panels.length > 0) {
        component.panels.forEach((panel, index) => {
            const isExpanded = panel.expanded !== false;
            collapseHTML += `
                <div class="collapse-panel" data-panel-index="${index}" style="
                    border: 1px solid #e1e4e8;
                    margin-bottom: -1px;
                ">
                    <div class="panel-header" style="
                        padding: 10px 15px;
                        background: #f6f8fa;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        gap: 8px;
                    ">
                        <i class="fas fa-chevron-${isExpanded ? 'down' : 'right'}"></i>
                        <span>${panel.name}</span>
                    </div>
                    <div class="panel-content" style="
                        padding: 15px;
                        display: ${isExpanded ? 'block' : 'none'};
                    "></div>
                </div>
            `;
        });
    }

    element.innerHTML = collapseHTML;

    // 渲染每个面板的子组件
    if (component.panels && component.panels.length > 0) {
        component.panels.forEach((panel, index) => {
            if (panel.children && panel.children.length > 0) {
                const contentContainer = element.querySelectorAll('.panel-content')[index];
                panel.children.forEach(child => {
                    const childComponent = typeof child === 'string'
                        ? findComponentById(child)
                        : child;
                    if (childComponent) {
                        const childElement = renderComponent(childComponent);
                        if (childElement) {
                            contentContainer.appendChild(childElement);
                        }
                    }
                });
            }
        });
    }

    // 绑定点击事件（独立模式：每个面板单独控制）
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

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}
```

- [ ] **Step 2: 在浏览器中验证 COLLAPSE 渲染**

Run: 刷新设计器，验证：
1. COLLAPSE 渲染出面板结构（带标题栏）
2. 面板内的 INPUT 子组件正确显示
3. 点击面板标题，内容区展开/折叠，图标切换 chevron-down ↔ chevron-right
4. 点击子组件能选中（高亮显示）

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/js/preview-renderer.js
git commit -m "fix: renderCollapseComponent adds click toggle and all-panel rendering"
```

---

## Task 4: renderGridComponent — DOM 操作 + 网格单元格

**Files:**
- Modify: `src/main/resources/static/config/js/preview-renderer.js:175-220`

- [ ] **Step 1: 替换 renderGridComponent 函数**

将第 175-220 行的 `renderGridComponent` 函数整体替换为：

```javascript
function renderGridComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;
    element.style.cssText = `
        background: #f6f8fa;
        padding: 10px;
        margin-bottom: 10px;
    `;

    const columns = component.columns || 2;
    const gutter = component.gutter || 16;

    const labelDiv = document.createElement('div');
    labelDiv.className = 'component-preview-label';
    labelDiv.style.marginBottom = '10px';
    labelDiv.innerHTML = `<i class="fas fa-th"></i> ${component.name} (${columns}列)`;
    element.appendChild(labelDiv);

    const gridContainer = document.createElement('div');
    gridContainer.style.cssText = `
        display: grid;
        grid-template-columns: repeat(${columns}, 1fr);
        gap: ${gutter}px;
    `;

    for (let i = 0; i < columns; i++) {
        const cell = document.createElement('div');
        cell.className = 'grid-cell';
        cell.dataset.gridColumn = i;
        cell.style.cssText = `
            min-height: 80px;
            border: 1px dashed #d1d5da;
            background: white;
            padding: 10px;
        `;

        const columnChildren = (component.children || []).filter(c => c.gridColumn === i);
        if (columnChildren.length > 0) {
            columnChildren.forEach(child => {
                const childElement = renderComponent(child);
                if (childElement) {
                    cell.appendChild(childElement);
                }
            });
        } else {
            cell.innerHTML = `<div style="padding:20px;text-align:center;color:#b4b4b4;">
                拖拽组件到第${i + 1}列
            </div>`;
        }

        gridContainer.appendChild(cell);
    }

    element.appendChild(gridContainer);

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}
```

- [ ] **Step 2: 在浏览器中验证 GRID 渲染**

Run: 刷新设计器，拖入 GRID 组件，验证：
1. 显示 N 列网格，每列有虚线边框和 `grid-cell` class
2. 每列有 `data-grid-column` 属性（0, 1, ...）
3. 空列显示"拖拽组件到第N列"提示
4. 如果已有子组件，根据 `gridColumn` 属性分配到对应列

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/js/preview-renderer.js
git commit -m "fix: renderGridComponent uses DOM ops with grid-cell structure"
```

---

## Task 5: getDropTarget — 识别 grid-cell 和 collapse-panel

**Files:**
- Modify: `src/main/resources/static/config/js/drag-drop.js:121-145`

- [ ] **Step 1: 替换 getDropTarget 函数**

将第 121-145 行的 `getDropTarget` 函数整体替换为：

```javascript
function getDropTarget(event) {
    const previewContent = document.getElementById('previewContent');
    const elements = document.elementsFromPoint(event.clientX, event.clientY);

    for (const element of elements) {
        if (element === previewContent) continue;

        // 优先级1：grid-cell（GRID 的网格单元格）
        const gridCell = element.closest('.grid-cell');
        if (gridCell) {
            const gridColumn = gridCell.dataset.gridColumn;
            const gridContainer = gridCell.closest('.component-preview');

            if (gridContainer) {
                const gridId = gridContainer.dataset.componentId;
                const gridComponent = findComponentById(gridId);

                if (gridComponent) {
                    return {
                        type: 'grid-cell',
                        component: gridComponent,
                        gridColumn: parseInt(gridColumn),
                        element: gridCell
                    };
                }
            }
        }

        // 优先级2：collapse-panel（COLLAPSE 的面板）
        const collapsePanel = element.closest('.collapse-panel');
        if (collapsePanel) {
            const panelIndex = collapsePanel.dataset.panelIndex;
            const collapseContainer = collapsePanel.closest('.component-preview');

            if (collapseContainer) {
                const collapseId = collapseContainer.dataset.componentId;
                const collapseComponent = findComponentById(collapseId);

                if (collapseComponent) {
                    return {
                        type: 'collapse-panel',
                        component: collapseComponent,
                        panelIndex: parseInt(panelIndex),
                        element: collapsePanel
                    };
                }
            }
        }

        // 优先级3：tab-content（TAB 的页签）
        const tabContent = element.closest('.tab-content');
        if (tabContent) {
            const tabContainer = tabContent.closest('.component-preview');

            if (tabContainer) {
                const tabId = tabContainer.dataset.componentId;
                const tabComponent = findComponentById(tabId);

                if (tabComponent) {
                    const activeTab = tabContainer.querySelector('.tab-item.active');
                    const tabIndex = activeTab ? parseInt(activeTab.dataset.tabIndex || 0) : 0;

                    return {
                        type: 'tab-panel',
                        component: tabComponent,
                        tabIndex: tabIndex,
                        element: tabContent
                    };
                }
            }
        }

        // 优先级4：普通容器
        const componentPreview = element.closest('.component-preview');
        if (componentPreview) {
            const componentId = componentPreview.dataset.componentId;
            const component = findComponentById(componentId);

            if (component && ComponentLibrary.types[component.type]?.isContainer) {
                return {
                    type: 'append',
                    component: component,
                    element: componentPreview
                };
            }
        }
    }

    return null;
}
```

- [ ] **Step 2: 在浏览器中验证拖拽目标识别**

Run: 刷新设计器，拖入 GRID 和 COLLAPSE，从组件库拖拽 INPUT 悬停在 grid-cell 上，在 Console 观察 `draggedComponent` 和拖拽目标识别是否正确。

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/js/drag-drop.js
git commit -m "fix: getDropTarget identifies grid-cell and collapse-panel"
```

---

## Task 6: addComponentToTarget — 处理 GRID 和 COLLAPSE 拖拽

**Files:**
- Modify: `src/main/resources/static/config/js/drag-drop.js:150-164`

- [ ] **Step 1: 替换 addComponentToTarget 函数**

将第 150-164 行的 `addComponentToTarget` 函数整体替换为：

```javascript
function addComponentToTarget(component, target) {
    if (target.type === 'grid-cell') {
        component.gridColumn = target.gridColumn;
        component.gridRow = 0;
        component.parentId = target.component.id;

        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);

        renderPreview();
        selectComponent(component.id);
        saveState();

    } else if (target.type === 'collapse-panel') {
        if (!target.component.panels[target.panelIndex].children) {
            target.component.panels[target.panelIndex].children = [];
        }
        target.component.panels[target.panelIndex].children.push(component.id);

        component.parentId = target.component.id;

        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);

        renderPreview();
        selectComponent(component.id);
        saveState();

    } else if (target.type === 'tab-panel') {
        if (!target.component.tabs[target.tabIndex].children) {
            target.component.tabs[target.tabIndex].children = [];
        }
        target.component.tabs[target.tabIndex].children.push(component.id);

        component.parentId = target.component.id;

        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);

        renderPreview();
        selectComponent(component.id);
        saveState();

    } else if (target.type === 'append') {
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

- [ ] **Step 2: 在浏览器中验证拖拽添加**

Run: 刷新设计器，测试以下场景：
1. 拖入 GRID → 拖入 INPUT 到左列 → INPUT 出现在左列
2. 拖入 COLLAPSE → 拖入 INPUT 到面板 → INPUT 出现在面板内
3. 在 Console 检查 `DesignerState.templateConfig.rootComponent` 中 panels[i].children 是 ID 字符串，children 数组包含完整对象

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/js/drag-drop.js
git commit -m "fix: addComponentToTarget handles grid-cell and collapse-panel drop"
```

---

## Task 7: handleDragOver — 网格单元格高亮反馈

**Files:**
- Modify: `src/main/resources/static/config/js/drag-drop.js:49-63`

- [ ] **Step 1: 增强 handleDragOver 函数**

将第 49-63 行的 `handleDragOver` 函数替换为：

```javascript
function handleDragOver(event) {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'copy';

    const previewCanvas = document.getElementById('previewCanvas');
    if (!previewCanvas.classList.contains('drag-over')) {
        previewCanvas.classList.add('drag-over');
    }

    // 隐藏提示
    const dropHint = document.getElementById('dropHint');
    if (dropHint) {
        dropHint.classList.add('hidden');
    }

    // 清除之前的 grid-cell 高亮
    document.querySelectorAll('.grid-cell').forEach(el => {
        el.style.background = 'white';
        el.style.border = '1px dashed #d1d5da';
    });

    // 高亮当前悬停的 grid-cell
    const dropTarget = getDropTarget(event);
    if (dropTarget && dropTarget.type === 'grid-cell') {
        dropTarget.element.style.background = 'rgba(102,126,234,0.1)';
        dropTarget.element.style.border = '2px dashed #667eea';
    }
}
```

- [ ] **Step 2: 在浏览器中验证拖拽高亮**

Run: 刷新设计器，拖入 GRID 组件，从组件库拖拽 INPUT 悬停在 GRID 的不同列上，验证：
1. 悬停列出现蓝色高亮背景和蓝色虚线边框
2. 移到其他列时，前一列恢复原样，新列高亮
3. 离开 GRID 区域后所有列恢复

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/js/drag-drop.js
git commit -m "fix: handleDragOver adds grid-cell drop highlight feedback"
```

---

## Task 8: E2E 验证 — COLLAPSE 完整闭环

**Files:**
- No code changes — verification only

- [ ] **Step 1: 启动应用并打开设计器**

Run: `mvn spring-boot:run`

使用 Chrome DevTools MCP 导航到设计器：

```
mcp__chrome-devtools__navigate_page("http://localhost:8888/config/template-designer.html")
```

- [ ] **Step 2: 拖拽 COLLAPSE 到画布**

```
mcp__chrome-devtools__drag(
    fromSelector: "#component-library .component-item[data-type='COLLAPSE']",
    toSelector: "#previewContent"
)
mcp__chrome-devtools__take_screenshot()
```

Expected: 预览区显示 COLLAPSE 组件，有一个面板"面板1"，默认展开，图标 chevron-down。

- [ ] **Step 3: 拖拽 INPUT 到面板内容区**

```
mcp__chrome-devtools__drag(
    fromSelector: "#component-library .component-item[data-type='INPUT']",
    toSelector: ".panel-content"
)
mcp__chrome-devtools__take_screenshot()
```

Expected: 面板内出现 INPUT 组件。

- [ ] **Step 4: 保存配置**

```
mcp__chrome-devtools__click(selector: "#saveBtn")
```

检查网络请求：

```
mcp__chrome-devtools__list_network_requests({ filter: { urlPattern: "/api/templates", method: "PUT" } })
```

Expected: PUT 请求返回 200，请求体中 COLLAPSE 的 propsJson 包含 `panels`，INPUT 的 `parentId` 指向 COLLAPSE。

- [ ] **Step 5: 刷新页面验证加载**

```
mcp__chrome-devtools__navigate_page({ type: "reload" })
mcp__chrome-devtools__wait_for({ selector: ".collapse-panel", timeout: 5000 })
mcp__chrome-devtools__take_screenshot()
```

Expected: COLLAPSE 和 INPUT 都正确渲染。

- [ ] **Step 6: 验证点击交互**

```
mcp__chrome-devtools__click(selector: ".panel-header")
mcp__chrome-devtools__take_screenshot()
```

Expected: 面板折叠，图标变成 chevron-right，内容隐藏。再次点击恢复展开。

---

## Task 9: E2E 验证 — GRID 完整闭环

**Files:**
- No code changes — verification only

- [ ] **Step 1: 拖拽 GRID 到画布**

```
mcp__chrome-devtools__drag(
    fromSelector: "#component-library .component-item[data-type='GRID']",
    toSelector: "#previewContent"
)
mcp__chrome-devtools__take_screenshot()
```

Expected: 显示 2 列网格，每列有虚线边框和"拖拽组件到第N列"提示。

- [ ] **Step 2: 拖拽 INPUT 到第 1 列，SELECT 到第 2 列**

```
mcp__chrome-devtools__drag(
    fromSelector: "#component-library .component-item[data-type='INPUT']",
    toSelector: ".grid-cell[data-grid-column='0']"
)
mcp__chrome-devtools__drag(
    fromSelector: "#component-library .component-item[data-type='SELECT']",
    toSelector: ".grid-cell[data-grid-column='1']"
)
mcp__chrome-devtools__take_screenshot()
```

Expected: INPUT 在左列，SELECT 在右列。

- [ ] **Step 3: 保存并刷新验证**

```
mcp__chrome-devtools__click(selector: "#saveBtn")
```

验证网络请求体中 GRID 子组件的 `propsJson` 包含 `"gridColumn":0` 和 `"gridColumn":1`。

```
mcp__chrome-devtools__navigate_page({ type: "reload" })
mcp__chrome-devtools__wait_for({ selector: ".grid-cell", timeout: 5000 })
mcp__chrome-devtools__take_screenshot()
```

Expected: INPUT 在左列，SELECT 在右列，位置与保存前一致。

- [ ] **Step 4: Commit（如有修复）**

如果 E2E 验证中发现问题并修复，提交修复。

```bash
git add -A
git commit -m "fix: E2E verification fixes for COLLAPSE and GRID"
```

---

## 自查清单

**1. Spec 覆盖率：**

| 需求 | 任务 |
|-----|------|
| extractLayoutNodes 保存 panels/tabs/gridColumn | Task 1 |
| buildComponentTree 合并 ID 引用 | Task 2 |
| COLLAPSE 点击交互+全面板渲染 | Task 3 |
| GRID DOM 操作+网格单元格 | Task 4 |
| getDropTarget 识别 grid-cell/collapse-panel | Task 5 |
| addComponentToTarget 处理特殊容器 | Task 6 |
| handleDragOver 高亮反馈 | Task 7 |
| E2E COLLAPSE 验证 | Task 8 |
| E2E GRID 验证 | Task 9 |

**2. 占位符扫描：** 无 TBD/TODO/占位符。

**3. 类型一致性：**
- `findComponentById` 已存在于 `drag-drop.js:196`，返回组件对象或 null
- `renderPreview` 已存在于 `preview-renderer.js:9`
- `selectComponent` 在 `designer.js` 中通过 property-panel.js 定义
- `saveState` 已存在于 `config-api.js:694`
- `parsePropsJson` 已存在于 `config-api.js:248`
- `ComponentLibrary.types[type].isContainer` 在 `drag-drop.js` 中使用与 `component-library.js` 中定义一致
- `data-grid-column` 在 renderGridComponent 中通过 `cell.dataset.gridColumn = i` 设置，在 getDropTarget 中通过 `gridCell.dataset.gridColumn` 读取 — 一致
- `data-panel-index` 在 renderCollapseComponent 的 HTML 模板中通过 `data-panel-index="${index}"` 设置，在 getDropTarget 中通过 `collapsePanel.dataset.panelIndex` 读取 — 一致（HTML 属性 `data-panel-index` 对应 JS 的 `dataset.panelIndex`）
