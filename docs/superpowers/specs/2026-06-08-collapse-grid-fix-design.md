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

### 方案B：纯树形结构（req.md设计意图）

**核心设计**（来自req.md第28行、第1088行）：

```
布局节点树化：通过parent_id构建树形结构
```

**数据库设计**（req.md第1101-1131行）：
```sql
CREATE TABLE t_ui_layout_node (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT,      -- 父节点ID，构建树形结构
    ...
    props_json JSONB       -- 扩展属性（不存嵌套对象）
);
```

**关键原则**：
1. **树形构建**：每个节点通过 `parent_id` 关联父节点
2. **propsJson只存扩展属性**：span/offset/panels名称等配置，**不存嵌套组件对象**
3. **panels/tabs的children只存ID引用**：`["comp_123"]` 而非 `[完整组件对象]`
4. **后端API组装树**：通过 `parent_id` 查询，递归组装 `children` 数组返回

**优势**：
- 无数据冗余：每个组件只存一次，无嵌套对象重复
- 符合数据库设计规范：通过外键关系构建树
- openGauss JSONB查询高效：GIN索引命中
- 后端零改动：Entity已有 `propsJson` String字段

---

## 后端API组装树形结构（方案B核心）

**当前后端逻辑**（SchemaService.java）：

后端查询所有LayoutNode记录，通过 `parent_id` 字段在**前端**构建树形结构。

**改进方案**：后端组装树形结构返回

**Service层逻辑**：
```java
public SchemaDTO getSchema(Long templateId, Long versionId) {
    // 1. 查询所有布局节点（按versionId）
    List<LayoutNode> allNodes = layoutNodeRepository.findByVersionId(versionId);
    
    // 2. 构建节点Map（用于快速查找）
    Map<Long, LayoutNodeDTO> nodeMap = new HashMap<>();
    for (LayoutNode node : allNodes) {
        nodeMap.put(node.getId(), layoutNodeConverter.toDTO(node));
    }
    
    // 3. 构建树形结构（根据parent_id）
    LayoutNodeDTO root = null;
    for (LayoutNodeDTO node : nodeMap.values()) {
        Long parentId = node.getParentId();
        if (parentId == null) {
            root = node;  // 根节点
        } else {
            LayoutNodeDTO parent = nodeMap.get(parentId);
            if (parent != null) {
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(node);  // 加入父节点的children数组
            }
        }
    }
    
    // 4. 构建返回结果
    SchemaDTO schema = new SchemaDTO();
    schema.setLayoutNodes(Arrays.asList(root));  // 返回树形结构
    schema.setFieldDefs(fieldDefRepository.findByVersionId(versionId));
    schema.setFieldComponents(fieldComponentRepository.findByVersionId(versionId));
    
    return schema;
}
```

**API返回结构**：
```json
{
  "success": true,
  "data": {
    "layoutNodes": [{
      "id": "100",
      "parentId": null,
      "nodeType": "COLLAPSE",
      "propsJson": "{\"panels\":[{\"name\":\"面板1\",\"children\":[\"200\"]}]}",
      "children": [{                    // ← 后端通过parent_id组装的树形children
        "id": "200",
        "parentId": "100",
        "nodeType": "GRID",
        "propsJson": "{\"columns\":2}",
        "children": [
          {
            "id": "300",
            "parentId": "200",
            "nodeType": "INPUT",
            "propsJson": "{\"gridColumn\":0}"
          }
        ]
      }]
    }]
  }
}
```

**关键点**：
- `layoutNodes[0].children` 是后端组装的树形结构
- `propsJson` 中的 `panels[i].children` 只存ID引用 `["200"]`
- 前端需要合并这两个数据源（见第2部分加载逻辑）

---

## 数据结构对比

### 方案A（冗余）vs 方案B（纯树形）

| 数据位置 | 方案A | 方案B（req设计） |
|---------|-------|-----------------|
| **数据库存储** | propsJson嵌套完整组件对象 | propsJson只存ID引用 |
| **树形构建** | propsJson里的嵌套对象 | 通过parent_id查询组装 |
| **数据冗余** | 有冗余（组件存两遍） | 无冗余（每个组件唯一记录） |
| **API返回** | propsJson包含嵌套children | API组装树形children + propsJson存ID |

**方案B示例**：

COLLAPSE节点（id=100）：
```sql
props_json = '{"panels":[{"name":"面板1","children":["200"]}]}'
parent_id = NULL
```

GRID节点（id=200）：
```sql
props_json = '{"columns":2}'
parent_id = 100   -- 关联到COLLAPSE
```

INPUT节点（id=300）：
```sql
props_json = '{"gridColumn":0}'
parent_id = 200   -- 关联到GRID
```

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

COLLAPSE的propsJson（方案B：ID引用）：
```json
{
  "panels": [
    {
      "name": "面板1",
      "code": "panel1",
      "children": ["200", "201"]   // ← 只存ID引用，不存完整对象
    },
    {
      "name": "面板2",
      "code": "panel2",
      "children": ["300"]
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

TAB的propsJson（遗漏补充）：
```json
{
  "tabs": [
    {
      "name": "页签1",
      "code": "tab1",
      "children": ["400", "401"]   // ← 只存ID引用
    },
    {
      "name": "页签2",
      "code": "tab2",
      "children": ["500"]
    }
  ]
}
```

**关键原则**：`panels[i].children` 和 `tabs[i].children` 只存ID字符串数组，完整组件对象通过 `parent_id` 关联。

---

## 第2部分：前端加载逻辑（方案B：合并ID引用和树形children）

**文件**: `src/main/resources/static/config/js/config-api.js`  
**函数**: `buildComponentTree` (第219-243行)

**当前问题**：
- `parsePropsJson` 正确解析propsJson
- 但propsJson.panels[i].children是ID数组，API返回的children是对象数组
- 需要合并这两个数据源

**修复代码**：
```javascript
function buildComponentTree(schemaData) {
    const layoutNodes = schemaData.layoutNodes || [];
    
    if (layoutNodes.length === 0) return null;
    
    // 1. 构建组件Map（用于ID查找）
    const componentMap = {};
    
    function convertNode(node) {
        let props = parsePropsJson(node.propsJson);
        
        const component = {
            id: String(node.id),
            type: node.nodeType,
            name: node.nodeName,
            code: node.nodeCode,
            parentId: node.parentId,
            ...props,  // 展开props属性（panels/tabs/gridColumn等）
            // API返回的children是对象数组（后端通过parent_id组装）
            children: (node.children || []).map(c => convertNode(c))
        };
        
        componentMap[String(node.id)] = component;
        return component;
    }
    
    // 先遍历所有节点构建Map
    layoutNodes.forEach(node => convertNode(node));
    
    // 2. 合并ID引用和树形children
    Object.values(componentMap).forEach(component => {
        // 处理panels中的ID引用
        if (component.panels) {
            component.panels.forEach(panel => {
                if (panel.children && typeof panel.children[0] === 'string') {
                    // ID引用 → 从componentMap查找完整对象
                    panel.children = panel.children.map(id => componentMap[id]);
                }
            });
        }
        
        // 处理tabs中的ID引用
        if (component.tabs) {
            component.tabs.forEach(tab => {
                if (tab.children && typeof tab.children[0] === 'string') {
                    tab.children = tab.children.map(id => componentMap[id]);
                }
            });
        }
    });
    
    // 返回根节点
    return layoutNodes[0] ? componentMap[String(layoutNodes[0].id)] : null;
}
```

**关键改动**：
1. **构建componentMap**：所有节点转换为组件对象，存入Map供ID查找
2. **合并ID引用**：如果 `panels[i].children[0]` 是字符串（ID），从Map查找完整对象替换
3. **保留树形children**：API返回的 `children` 数组（后端通过parent_id组装）保留

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

### 4.2 处理GRID网格列拖拽（方案B：存ID引用）

**改动函数**: `addComponentToTarget` (第150-164行)

**方案B关键改动**：push ID而非完整对象

**新增逻辑**：
```javascript
function addComponentToTarget(component, target) {
    if (target.type === 'grid-cell') {
        // GRID网格列拖拽
        component.gridColumn = target.gridColumn;
        component.gridRow = 0;
        component.parentId = target.component.id;  // ← 设置parent_id
        
        // 加入target.component.children（用于renderPreview）
        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);  // 完整对象（渲染用）
        
        // 同时更新propsJson（用于保存）
        const propsObj = parsePropsJson(target.component.propsJson) || {};
        if (!propsObj.gridChildren) propsObj.gridChildren = [];
        propsObj.gridChildren.push(component.id);  // ← 只存ID引用
        target.component.propsJson = JSON.stringify(propsObj);
        
        renderPreview();
        selectComponent(component.id);
        saveState();
        
    } else if (target.type === 'collapse-panel') {
        // COLLAPSE面板拖拽（方案B：存ID引用）
        if (!target.component.panels[target.panelIndex].children) {
            target.component.panels[target.panelIndex].children = [];
        }
        // 只存ID引用（用于保存到propsJson）
        target.component.panels[target.panelIndex].children.push(component.id);
        
        component.parentId = target.component.id;  // ← 设置parent_id
        
        // 同时加入target.component.children（用于renderPreview）
        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);  // 完整对象（渲染用）
        
        renderPreview();
        selectComponent(component.id);
        saveState();
        
    } else if (target.type === 'tab-panel') {
        // TAB页签拖拽（方案B：存ID引用）
        if (!target.component.tabs[target.tabIndex].children) {
            target.component.tabs[target.tabIndex].children = [];
        }
        target.component.tabs[target.tabIndex].children.push(component.id);  // ← 只存ID
        
        component.parentId = target.component.id;
        
        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);
        
        renderPreview();
        selectComponent(component.id);
        saveState();
        
    } else if (target.type === 'append') {
        // 普通容器拖拽
        component.parentId = target.component.id;
        
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

**关键点**：
- `panels[i].children.push(component.id)`：只存ID字符串
- `component.parentId = target.component.id`：设置父节点ID（后端通过parent_id构建树）
- `target.component.children.push(component)`：同时加入完整对象数组（前端渲染用）

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

## 第5部分：E2E验证方案（完整闭环）

### 5.1 E2E验证流程（COLLAPSE场景）

**验证目标**：拖拽COLLAPSE → 拖拽子组件到面板 → 保存 → 刷新加载 → 验证渲染 → 数据库持久化 → openGauss JSONB特性

| 阶段 | 步骤 | 操作 | 验证点 | 工具 |
|-----|-----|------|--------|------|
| **准备** | 1 | 用户在界面创建版本草稿 | 进入设计器 | 用户操作 |
| **拖拽COLLAPSE** | 2 | Claude拖拽COLLAPSE到画布 | COLLAPSE渲染成功，显示1个面板 | Chrome DevTools: drag |
| **拖拽子组件** | 3 | Claude拖拽INPUT到面板1 | INPUT出现在面板内容区 | Chrome DevTools: drag + take_screenshot |
| **保存** | 4 | Claude点击保存按钮 | API调用成功，返回200 | Chrome DevTools: click + list_network_requests |
| **刷新加载** | 5 | Claude刷新页面 | 页面重新加载，COLLAPSE和INPUT都渲染 | Chrome DevTools: navigate_page reload |
| **渲染验证** | 6 | Claude点击面板标题 | 面板展开/折叠有动画 | Chrome DevTools: click + take_screenshot |
| **数据库验证** | 7 | Claude查询数据库 | propsJson包含panels，children是ID引用 | SQL查询 |
| **JSONB特性** | 8 | Claude执行JSONB查询 | GIN索引命中 | EXPLAIN ANALYZE |

---

### 5.2 Chrome DevTools操作脚本（COLLAPSE）

**Step 1-2: 拖拽COLLAPSE组件**
```javascript
// 导航到设计器
mcp__chrome-devtools__navigate_page({
    url: "http://localhost:8888/config/template-designer.html?templateId=xxx&versionId=xxx"
})

// 等待页面加载
mcp__chrome-devtools__wait_for({
    selector: "#previewContent",
    timeout: 5000
})

// 拖拽COLLAPSE组件
mcp__chrome-devtools__drag({
    fromSelector: "#component-library .component-item[data-type='COLLAPSE']",
    toSelector: "#previewContent"
})

// 截图验证
mcp__chrome-devtools__take_screenshot({
    format: "png"
})
// 预期：预览区显示COLLAPSE组件，有1个面板
```

**Step 3: 拖拽INPUT到面板**
```javascript
// 获取面板元素
mcp__chrome-devtools__take_snapshot()

// 找到面板内容区（panel-content）
// 拖拽INPUT到面板
mcp__chrome-devtools__drag({
    fromSelector: "#component-library .component-item[data-type='INPUT']",
    toSelector: ".collapse-panel:first-child .panel-content"
})

// 截图验证
mcp__chrome-devtools__take_screenshot()
// 预期：面板内有INPUT组件，显示"输入框"
```

**Step 4: 保存配置**
```javascript
// 点击保存按钮
mcp__chrome-devtools__click({
    uid: "save-button-uid"  // 从snapshot获取
})

// 等待API响应
mcp__chrome-devtools__wait_for({
    text: ["保存成功", "配置保存成功"],
    timeout: 5000
})

// 验证网络请求
mcp__chrome-devtools__list_network_requests({
    filter: {
        urlPattern: "/api/templates/*/versions/*/schema",
        method: "PUT"
    }
})

// 获取请求详情
mcp__chrome-devtools__get_network_request({
    requestId: "xxx"  // 从list获取
})
// 验证：请求体包含layoutNodes，其中COLLAPSE的propsJson包含panels配置
```

**Step 5-6: 刷新验证**
```javascript
// 刷新页面
mcp__chrome-devtools__navigate_page({
    type: "reload"
})

// 等待加载完成
mcp__chrome-devtools__wait_for({
    selector: ".collapse-panel",
    timeout: 5000
})

// 截图验证COLLAPSE渲染
mcp__chrome-devtools__take_screenshot()
// 预期：COLLAPSE面板和INPUT都正确渲染

// 点击面板标题，测试展开/折叠
mcp__chrome-devtools__click({
    uid: "panel-header-uid"
})

// 截图验证折叠状态
mcp__chrome-devtools__take_screenshot()
// 预期：面板内容消失，图标变成chevron-right
```

---

### 5.3 SQL数据库验证脚本

**Step 7: 查询数据库持久化**

```sql
-- 查询COLLAPSE节点（验证propsJson包含panels）
SELECT 
    id,
    parent_id,
    node_code,
    node_name,
    node_type,
    props_json
FROM t_ui_layout_node
WHERE node_type = 'COLLAPSE'
AND template_version_id = {versionId}
ORDER BY id;

-- 预期结果：
-- props_json = '{"panels":[{"name":"面板1","code":"panel1","children":["INPUT_ID"]}]}'
-- parent_id = NULL（根节点）
```

```sql
-- 查询INPUT节点（验证parent_id关联）
SELECT 
    id,
    parent_id,
    node_type,
    props_json
FROM t_ui_layout_node
WHERE node_type = 'INPUT'
AND template_version_id = {versionId}
ORDER BY id;

-- 预期结果：
-- parent_id = COLLAPSE_ID（关联到COLLAPSE）
-- props_json包含INPUT的配置（placeholder等）
```

```sql
-- 验证树形结构完整性
-- 查询所有节点，按parent_id排序
SELECT 
    id,
    parent_id,
    node_type,
    node_name
FROM t_ui_layout_node
WHERE template_version_id = {versionId}
ORDER BY parent_id NULLS FIRST, sort_no;

-- 预期结果：
-- COLLAPSE (parent_id=NULL) → root
-- INPUT (parent_id=COLLAPSE_ID) → child
-- 通过parent_id能重建完整的树形结构
```

---

**Step 8: openGauss JSONB特性验证**

```sql
-- 1. JSONB包含查询（验证panels数据）
SELECT 
    id,
    node_name,
    props_json::jsonb->'panels' as panels_data
FROM t_ui_layout_node
WHERE props_json::jsonb @> '{"panels":[{"name":"面板1"}]}'::jsonb
AND template_version_id = {versionId};

-- 预期：返回COLLAPSE节点，panels_data包含完整面板数组
```

```sql
-- 2. JSONB路径查询（提取children ID）
SELECT 
    id,
    node_name,
    props_json::jsonb->'panels'->0->'children'->0 as first_child_id
FROM t_ui_layout_node
WHERE node_type = 'COLLAPSE'
AND template_version_id = {versionId};

-- 预期：返回INPUT的ID（字符串）
```

```sql
-- 3. GIN索引命中验证（关键：证明openGauss JSONB特性生效）
EXPLAIN ANALYZE
SELECT * FROM t_ui_layout_node
WHERE props_json::jsonb @> '{"panels":[]}'::jsonb
AND template_version_id = {versionId};

-- 预期结果（关键）：
-- "Bitmap Index Scan on idx_layout_props_gin"
-- "Index Cond: (props_json::jsonb @> '{"panels":[]}'::jsonb)"
-- "Rows Removed by Index Filter: 0"
-- 
-- 如果看到上述结果，证明：
-- 1. GIN索引命中（不是全表扫描）
-- 2. openGauss JSONB @> 操作符生效
-- 3. propsJson作为JSONB类型存储，不是普通TEXT
```

```sql
-- 4. JSONB数组长度查询（验证children数量）
SELECT 
    id,
    node_name,
    jsonb_array_length(props_json::jsonb->'panels'->0->'children') as child_count
FROM t_ui_layout_node
WHERE node_type = 'COLLAPSE'
AND template_version_id = {versionId};

-- 预期：child_count = 1（面板内有1个子组件）
```

---

### 5.4 E2E验证流程（GRID场景）

| 阶段 | 步骤 | 操作 | 验证点 | 工具 |
|-----|-----|------|--------|------|
| **拖拽GRID** | 1 | 拖拽GRID（2列）到画布 | GRID渲染，显示2列网格 | Chrome DevTools: drag |
| **拖拽子组件** | 2 | 拖拽INPUT到第1列（grid-cell-0） | INPUT出现在左列 | Chrome DevTools: drag |
| **拖拽子组件** | 3 | 拖拽SELECT到第2列（grid-cell-1） | SELECT出现在右列 | Chrome DevTools: drag |
| **保存** | 4 | 点击保存 | API返回200 | Chrome DevTools: click |
| **刷新** | 5 | 刷新页面 | GRID和子组件都渲染 | Chrome DevTools: reload |
| **验证布局** | 6 | 截图验证 | INPUT在左列，SELECT在右列 | Chrome DevTools: screenshot |
| **数据库** | 7 | 查询GRID和子组件 | propsJson包含gridColumn | SQL查询 |
| **JSONB** | 8 | 验证gridColumn查询 | GIN索引命中 | EXPLAIN ANALYZE |

---

### 5.5 GRID数据库验证

```sql
-- 查询GRID节点
SELECT 
    id,
    parent_id,
    node_type,
    props_json
FROM t_ui_layout_node
WHERE node_type = 'GRID'
AND template_version_id = {versionId};

-- 预期：props_json = '{"columns":2}'
```

```sql
-- 查询GRID子组件（INPUT和SELECT）
SELECT 
    id,
    parent_id,
    node_type,
    props_json
FROM t_ui_layout_node
WHERE node_type IN ('INPUT', 'SELECT')
AND parent_id = {GRID_ID}
AND template_version_id = {versionId};

-- 预期：
-- INPUT: props_json包含 '{"gridColumn":0}'
-- SELECT: props_json包含 '{"gridColumn":1}'
-- parent_id都指向GRID_ID
```

```sql
-- JSONB查询gridColumn=0的子组件
SELECT 
    id,
    node_type,
    props_json::jsonb->>'gridColumn' as grid_column
FROM t_ui_layout_node
WHERE props_json::jsonb @> '{"gridColumn":0}'::jsonb
AND template_version_id = {versionId};

-- 预期：返回INPUT节点，grid_column='0'
```

```sql
-- GIN索引命中验证（gridColumn查询）
EXPLAIN ANALYZE
SELECT * FROM t_ui_layout_node
WHERE props_json::jsonb @> '{"gridColumn":0}'::jsonb
AND template_version_id = {versionId};

-- 预期：Bitmap Index Scan on idx_layout_props_gin
```

---

### 5.6 后端API验证（网络请求分析）

**验证保存API调用**：

```javascript
// 从Chrome DevTools获取网络请求
mcp__chrome-devtools__get_network_request({
    requestId: "xxx"
})

// 验证请求体结构
{
    "layoutNodes": [
        {
            "id": "COLLAPSE_ID",
            "nodeCode": "collapse_1",
            "nodeName": "折叠面板",
            "nodeType": "COLLAPSE",
            "parentId": null,
            "propsJson": "{\"panels\":[{\"name\":\"面板1\",\"children\":[\"INPUT_ID\"]}]}"
        },
        {
            "id": "INPUT_ID",
            "nodeCode": "input_1",
            "nodeName": "输入框",
            "nodeType": "INPUT",
            "parentId": "COLLAPSE_ID",
            "propsJson": "{\"placeholder\":\"请输入\"}"
        }
    ]
}

// 验证点：
// 1. layoutNodes包含所有节点
// 2. COLLAPSE.propsJson包含panels，children是ID引用
// 3. INPUT.parentId指向COLLAPSE_ID
// 4. INPUT.propsJson不包含panels，只有INPUT自己的属性
```

**验证加载API响应**：

```javascript
// GET /api/templates/{templateId}/versions/{versionId}/schema
// 验证响应体结构
{
    "success": true,
    "data": {
        "layoutNodes": [{
            "id": "COLLAPSE_ID",
            "nodeType": "COLLAPSE",
            "propsJson": "{\"panels\":[{\"name\":\"面板1\",\"children\":[\"INPUT_ID\"]}]}",
            "children": [{  // ← 后端通过parent_id组装的树形children
                "id": "INPUT_ID",
                "nodeType": "INPUT",
                "parentId": "COLLAPSE_ID",
                "propsJson": "{\"placeholder\":\"请输入\"}",
                "children": []
            }]
        }]
    }
}

// 验证点：
// 1. API返回树形结构（通过parent_id组装）
// 2. layoutNodes[0].children是完整对象数组（后端组装）
// 3. propsJson.panels[0].children是ID数组（["INPUT_ID"]）
// 4. 前端需要合并这两个children（见第2部分加载逻辑）
```

---

### 5.7 E2E成功标准

| 验证维度 | 成功标准 | 验证方式 |
|---------|---------|---------|
| **前端渲染** | COLLAPSE面板展开/折叠动画流畅，INPUT正确显示 | DevTools截图 |
| **拖拽定位** | 子组件拖拽到指定网格列（gridColumn=0/1） | DevTools截图验证位置 |
| **API调用** | 保存API返回200，请求体结构正确 | DevTools list_network_requests |
| **数据库持久化** | propsJson包含panels/tabs/gridColumn数据 | SQL SELECT查询 |
| **树形结构** | parent_id正确关联，能重建完整树 | SQL查询所有节点 |
| **ID引用** | panels[i].children只存ID字符串 | SQL查询propsJson |
| **openGauss JSONB** | GIN索引命中，@> 查询生效 | EXPLAIN ANALYZE显示Bitmap Index Scan |
| **刷新加载** | 页面刷新后组件正确渲染，交互正常 | DevTools reload + screenshot |

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