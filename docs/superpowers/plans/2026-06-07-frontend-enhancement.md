# 前端功能增强实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 增强模板设计器前端功能，实现 ROW/COL 表格布局组件和完整的保存/查询验证流程

**Architecture:** 扩展现有组件库，添加 ROW/COL 组件，完善渲染器和属性面板，集成后端 API

**Tech Stack:** 原生 HTML/CSS/JS + Bootstrap 5.3 + Font Awesome + REST API

---

## 文件结构

```
src/main/resources/static/config/
├── js/
│   ├── component-library.js   # 修改: 添加 ROW/COL 组件定义
│   ├── preview-renderer.js    # 修改: 添加 ROW/COL 渲染函数
│   ├── property-panel.js      # 修改: 添加 ROW/COL 属性配置
│   ├── config-api.js          # 修改: 完善保存/加载 API
│   ├── designer.js            # 不变
│   └── drag-drop.js           # 不变
├── css/
│   └── designer.css           # 不变
└── template-designer.html     # 修改: 添加 ROW/COL UI 组件
```

---

### Task 1: 添加 ROW/COL 组件定义

**Files:**
- Modify: `src/main/resources/static/config/js/component-library.js:38-52` (GRID 组件后)

- [ ] **Step 1: 在 GRID 组件后添加 ROW 组件定义**

在 `component-library.js` 第 52 行 `GRID` 组件定义结束后，添加以下代码：

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
        },
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
        },
```

- [ ] **Step 2: 验证代码语法正确**

打开浏览器控制台，访问 `http://localhost:8888/config/template-designer.html`，确认无 JavaScript 错误。

运行: 在浏览器开发者工具 Console 中输入 `ComponentLibrary.types.ROW`
预期结果: 返回 ROW 组件定义对象

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/js/component-library.js
git commit -m "feat: add ROW and COL layout component definitions"
```

---

### Task 2: 添加 ROW/COL 渲染函数

**Files:**
- Modify: `src/main/resources/static/config/js/preview-renderer.js:34-75` (renderComponent 函数)

- [ ] **Step 1: 在 renderComponent 函数的 switch 中添加 ROW 和 COL case**

在 `preview-renderer.js` 的 `renderComponent` 函数中，找到 `case 'GRID':` 行（约第 40 行），在其后添加：

```javascript
        case 'ROW':
            return renderRowComponent(component);
        case 'COL':
            return renderColComponent(component);
```

- [ ] **Step 2: 在文件末尾添加 renderRowComponent 函数**

在 `preview-renderer.js` 文件末尾（约第 800 行后）添加：

```javascript
/**
 * 渲染行组件
 */
function renderRowComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;
    element.style.cssText = `
        display: flex;
        flex-wrap: wrap;
        margin-bottom: 10px;
        margin-left: -${(component.gutter || 16) / 2}px;
        margin-right: -${(component.gutter || 16) / 2}px;
    `;

    // 渲染子组件（COL）
    if (component.children && component.children.length > 0) {
        component.children.forEach(child => {
            const childElement = renderComponent(child);
            if (childElement) {
                element.appendChild(childElement);
            }
        });
    } else {
        element.innerHTML = `
            <div class="component-preview-label" style="width: 100%; padding: 10px; text-align: center; color: #b4b4b4;">
                <i class="fas fa-grip-lines"></i> ${component.name} - 拖拽列组件到此处
            </div>
        `;
    }

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染列组件
 */
function renderColComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;
    
    const span = component.span || 12;
    const offset = component.offset || 0;
    const widthPercent = (span / 24) * 100;
    const offsetPercent = (offset / 24) * 100;
    
    element.style.cssText = `
        flex: 0 0 ${widthPercent}%;
        margin-left: ${offsetPercent}%;
        padding-left: 8px;
        padding-right: 8px;
        min-height: 60px;
        border: 1px dashed #d1d5da;
        background: #fafbfc;
        box-sizing: border-box;
    `;

    // 渲染子组件
    if (component.children && component.children.length > 0) {
        const childrenContainer = document.createElement('div');
        childrenContainer.className = 'component-children';
        component.children.forEach(child => {
            const childElement = renderComponent(child);
            if (childElement) {
                childrenContainer.appendChild(childElement);
            }
        });
        element.appendChild(childrenContainer);
    } else {
        element.innerHTML = `
            <div class="component-preview-label" style="padding: 10px; text-align: center; color: #b4b4b4;">
                <i class="fas fa-grip-lines-vertical"></i> ${component.name} (${span}/24列)
            </div>
            <div class="component-children"></div>
        `;
    }

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}
```

- [ ] **Step 3: 验证渲染函数**

在浏览器控制台中测试：

```javascript
// 创建 ROW 组件
const row = ComponentLibrary.createComponent('ROW');
// 创建两个 COL 组件
const col1 = ComponentLibrary.createComponent('COL');
const col2 = ComponentLibrary.createComponent('COL');
col2.span = 12;
// 添加到 ROW
row.children = [col1, col2];
// 添加到设计器
addComponentToRoot(row);
```

预期结果: 预览区显示一行两列的布局

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/static/config/js/preview-renderer.js
git commit -m "feat: add renderRowComponent and renderColComponent functions"
```

---

### Task 3: 添加 ROW/COL 属性配置面板

**Files:**
- Modify: `src/main/resources/static/config/js/property-panel.js:91-135` (renderComponentSpecificConfig 函数)

- [ ] **Step 1: 在 renderComponentSpecificConfig 函数的 switch 中添加 ROW 和 COL case**

在 `property-panel.js` 的 `renderComponentSpecificConfig` 函数中，找到 `case 'COLLAPSE':` 行（约第 131 行），在其后添加：

```javascript
        case 'ROW':
            renderRowSpecificConfig(container, component);
            break;
        case 'COL':
            renderColSpecificConfig(container, component);
            break;
```

- [ ] **Step 2: 在文件末尾添加 ROW 和 COL 属性配置函数**

在 `property-panel.js` 文件末尾添加：

```javascript
/**
 * 行特有配置
 */
function renderRowSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>列间距(px)</label>
            <input type="number" id="configGutter" value="${component.gutter || 16}"
                   min="0" max="100"
                   onchange="updateProperty('gutter', parseInt(this.value))">
        </div>
        <div class="form-group">
            <label style="color: #586069; font-size: 12px;">
                <i class="fas fa-info-circle"></i> 提示：行组件用于包含列组件，请拖拽"列"组件到此处
            </label>
        </div>
    `;
}

/**
 * 列特有配置
 */
function renderColSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>栅格列数 (共24列)</label>
            <select id="configSpan" onchange="updateProperty('span', parseInt(this.value))">
                <option value="24" ${component.span === 24 ? 'selected' : ''}>24列 (整行)</option>
                <option value="12" ${component.span === 12 ? 'selected' : ''}>12列 (半行)</option>
                <option value="8" ${component.span === 8 ? 'selected' : ''}>8列 (1/3行)</option>
                <option value="6" ${component.span === 6 ? 'selected' : ''}>6列 (1/4行)</option>
                <option value="4" ${component.span === 4 ? 'selected' : ''}>4列 (1/6行)</option>
            </select>
        </div>
        <div class="form-group">
            <label>偏移列数</label>
            <select id="configOffset" onchange="updateProperty('offset', parseInt(this.value))">
                <option value="0" ${component.offset === 0 ? 'selected' : ''}>无偏移</option>
                <option value="6" ${component.offset === 6 ? 'selected' : ''}>偏移6列</option>
                <option value="12" ${component.offset === 12 ? 'selected' : ''}>偏移12列</option>
            </select>
        </div>
        <div class="form-group">
            <label style="color: #586069; font-size: 12px;">
                <i class="fas fa-info-circle"></i> 当前: ${component.span || 12}/24 列 (${((component.span || 12) / 24 * 100).toFixed(0)}% 宽度)
            </label>
        </div>
    `;
}
```

- [ ] **Step 3: 验证属性配置面板**

1. 在设计器中添加一个 ROW 组件
2. 点击选中 ROW 组件
3. 切换到"属性配置" Tab
4. 确认看到"列间距"配置项
5. 修改列间距值，确认预览区更新

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/static/config/js/property-panel.js
git commit -m "feat: add ROW and COL property configuration panels"
```

---

### Task 4: 更新组件库 UI

**Files:**
- Modify: `src/main/resources/static/config/template-designer.html:93-123` (布局组件区域)

- [ ] **Step 1: 在布局组件区域添加 ROW 和 COL 组件**

在 `template-designer.html` 中找到布局组件区域（约第 93-123 行），在 `折叠面板` 组件项后添加：

```html
                            <div class="component-item" draggable="true"
                                 ondragstart="handleDragStart(event)"
                                 data-type="ROW">
                                <i class="fas fa-grip-lines"></i>
                                <span>行</span>
                            </div>
                            <div class="component-item" draggable="true"
                                 ondragstart="handleDragStart(event)"
                                 data-type="COL">
                                <i class="fas fa-grip-lines-vertical"></i>
                                <span>列</span>
                            </div>
```

- [ ] **Step 2: 验证组件库显示**

1. 刷新页面 `http://localhost:8888/config/template-designer.html`
2. 确认组件库布局组件区域显示"行"和"列"组件
3. 确认图标正确显示

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/template-designer.html
git commit -m "feat: add ROW and COL components to UI component library"
```

---

### Task 5: 完善保存和加载 API 集成

**Files:**
- Modify: `src/main/resources/static/config/js/config-api.js:9-45` (saveConfig 函数)
- Modify: `src/main/resources/static/config/js/config-api.js:49-74` (loadConfig 函数)
- Modify: `src/main/resources/static/config/js/config-api.js:291-376` (renderComponentHTML 函数)

- [ ] **Step 1: 更新 saveConfig 函数使用正确的后端 API**

将 `config-api.js` 中的 `saveConfig` 函数替换为：

```javascript
/**
 * 保存配置
 */
async function saveConfig() {
    try {
        const config = DesignerState.templateConfig;

        // 验证配置
        const validation = validateConfig(config);
        if (!validation.valid) {
            showNotification(validation.message, 'error');
            return;
        }

        // 获取模板ID和版本ID（从URL参数或状态中）
        const urlParams = new URLSearchParams(window.location.search);
        const templateId = urlParams.get('templateId') || DesignerState.templateId || '1001';
        const versionId = urlParams.get('versionId') || DesignerState.versionId || '2001';

        // 构建保存数据
        const saveData = {
            layoutNodes: extractLayoutNodes(config.rootComponent),
            fieldDefs: extractFieldDefs(config),
            fieldComponents: extractFieldComponents(config),
            queryConfigs: config.queryConfigs || [],
            actionConfigs: config.actionConfigs || []
        };

        // 调用后端API保存
        const response = await fetch(`/api/templates/${templateId}/versions/${versionId}/schema`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(saveData)
        });

        if (response.ok) {
            const result = await response.json();
            showNotification('配置保存成功', 'success');
            console.log('保存结果:', result);
        } else {
            const errorText = await response.text();
            throw new Error(`保存失败: ${response.status} ${errorText}`);
        }
    } catch (error) {
        console.error('保存配置失败:', error);
        showNotification('保存失败: ' + error.message, 'error');
    }
}

/**
 * 从组件树提取布局节点
 */
function extractLayoutNodes(component, parentId = null, result = []) {
    if (!component) return result;
    
    const node = {
        id: component.id,
        nodeCode: component.code,
        nodeName: component.name,
        nodeType: component.type,
        parentId: parentId,
        sortNo: result.length,
        propsJson: JSON.stringify({
            span: component.span,
            offset: component.offset,
            gutter: component.gutter,
            columns: component.columns
        })
    };
    result.push(node);
    
    // 递归处理子组件
    if (component.children && component.children.length > 0) {
        component.children.forEach(child => {
            extractLayoutNodes(child, component.id, result);
        });
    }
    
    return result;
}

/**
 * 提取字段定义
 */
function extractFieldDefs(config) {
    const fieldDefs = [];
    const processedPaths = new Set();
    
    function collectFields(component) {
        if (!component) return;
        
        if (component.fieldPath && !processedPaths.has(component.fieldPath)) {
            processedPaths.add(component.fieldPath);
            fieldDefs.push({
                id: component.id + '_field',
                fieldCode: component.code,
                fieldPath: component.fieldPath,
                fieldNameCn: component.name,
                dataType: component.dataType || 'string'
            });
        }
        
        if (component.children) {
            component.children.forEach(collectFields);
        }
    }
    
    collectFields(config.rootComponent);
    return fieldDefs;
}

/**
 * 提取字段组件绑定
 */
function extractFieldComponents(config) {
    const components = [];
    
    function collectComponents(component) {
        if (!component) return;
        
        if (component.fieldPath) {
            components.push({
                id: component.id + '_comp',
                fieldDefId: component.id + '_field',
                layoutNodeId: component.id,
                componentType: component.type,
                propsJson: JSON.stringify({
                    placeholder: component.placeholder,
                    required: component.required,
                    readonly: component.readonly
                })
            });
        }
        
        if (component.children) {
            component.children.forEach(collectComponents);
        }
    }
    
    collectComponents(config.rootComponent);
    return components;
}
```

- [ ] **Step 2: 更新 loadConfig 函数**

将 `loadConfig` 函数替换为：

```javascript
/**
 * 加载配置
 */
async function loadConfig(templateId, versionId) {
    try {
        // 如果没有传入参数，从 URL 获取
        if (!templateId || !versionId) {
            const urlParams = new URLSearchParams(window.location.search);
            templateId = templateId || urlParams.get('templateId');
            versionId = versionId || urlParams.get('versionId');
        }

        if (!templateId || !versionId) {
            console.log('缺少 templateId 或 versionId，使用本地存储状态');
            restoreState();
            return;
        }

        const response = await fetch(`/api/templates/${templateId}/versions/${versionId}/schema`);

        if (response.ok) {
            const result = await response.json();
            
            // 保存到状态
            DesignerState.templateId = templateId;
            DesignerState.versionId = versionId;
            
            // 从后端数据重建组件树
            DesignerState.templateConfig = {
                rootComponent: buildComponentTree(result.data || result),
                fieldDefs: result.data?.fieldDefs || [],
                queryConfigs: result.data?.queryConfigs || [],
                actionConfigs: result.data?.actionConfigs || []
            };

            // 更新模板名称显示
            document.getElementById('templateName').textContent = result.data?.templateName || '已加载模板';

            // 渲染预览
            renderPreview();
            saveState();

            showNotification('配置加载成功', 'success');
        } else {
            throw new Error(`加载失败: ${response.status}`);
        }
    } catch (error) {
        console.error('加载配置失败:', error);
        showNotification('加载失败: ' + error.message, 'error');
        // 失败时尝试恢复本地状态
        restoreState();
    }
}

/**
 * 从布局节点构建组件树
 */
function buildComponentTree(schemaData) {
    const layoutNodes = schemaData.layoutNodes || [];
    const fieldComponents = schemaData.fieldComponents || [];
    
    if (layoutNodes.length === 0) return null;
    
    // 创建节点映射
    const nodeMap = {};
    layoutNodes.forEach(node => {
        const props = node.propsJson ? JSON.parse(node.propsJson) : {};
        nodeMap[node.id] = {
            id: node.id,
            type: node.nodeType,
            name: node.nodeName,
            code: node.nodeCode,
            ...props,
            children: []
        };
    });
    
    // 关联字段组件信息
    fieldComponents.forEach(fc => {
        const node = nodeMap[fc.layoutNodeId];
        if (node) {
            const props = fc.propsJson ? JSON.parse(fc.propsJson) : {};
            Object.assign(node, {
                fieldPath: node.fieldPath || fc.fieldPath,
                dataType: node.dataType || fc.dataType,
                ...props
            });
        }
    });
    
    // 构建树结构
    let root = null;
    layoutNodes.forEach(node => {
        const component = nodeMap[node.id];
        if (node.parentId && nodeMap[node.parentId]) {
            nodeMap[node.parentId].children.push(component);
        } else {
            root = component;
        }
    });
    
    return root;
}
```

- [ ] **Step 3: 在 renderComponentHTML 函数中添加 ROW 和 COL 渲染**

在 `renderComponentHTML` 函数的 switch 中添加：

```javascript
        case 'ROW':
            return `
                <div style="display: flex; flex-wrap: wrap; margin-bottom: 15px;">
                    ${component.children ? component.children.map(c => renderComponentHTML(c)).join('') : ''}
                </div>
            `;
        case 'COL':
            const colSpan = component.span || 12;
            const colWidth = (colSpan / 24) * 100;
            return `
                <div style="flex: 0 0 ${colWidth}%; padding: 0 8px;">
                    ${component.children ? component.children.map(c => renderComponentHTML(c)).join('') : ''}
                </div>
            `;
```

- [ ] **Step 4: 测试保存功能**

1. 在设计器中创建一个包含 ROW 和 COL 的布局
2. 添加一些字段组件
3. 点击"保存"按钮
4. 检查浏览器网络面板，确认 API 调用
5. 检查控制台日志

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/static/config/js/config-api.js
git commit -m "feat: enhance save and load API integration with schema format"
```

---

### Task 6: E2E 验证 - 完整流程测试

**Files:**
- Test: Chrome DevTools MCP 验证

- [ ] **Step 1: 验证组件拖拽功能**

使用 Chrome DevTools 执行以下验证：

1. 打开页面: `http://localhost:8888/config/template-designer.html`
2. 从组件库拖拽 ROW 组件到预览区
3. 从组件库拖拽两个 COL 组件到 ROW 内部
4. 设置第一个 COL 的 span 为 12
5. 设置第二个 COL 的 span 为 12
6. 在每个 COL 中添加一个 INPUT 组件
7. 截图验证布局效果

预期结果:
- ROW 正确显示为一行
- 两个 COL 各占 50% 宽度
- INPUT 组件在各自的 COL 内显示

- [ ] **Step 2: 验证属性配置交互**

1. 点击选中 ROW 组件
2. 确认属性面板显示 ROW 配置
3. 修改 gutter 属性，确认预览区实时更新
4. 点击选中 COL 组件
5. 确认属性面板显示 COL 配置
6. 修改 span 属性，确认列宽变化

预期结果:
- 属性面板正确显示对应组件的配置
- 修改属性后预览区实时更新

- [ ] **Step 3: 验证保存和加载流程**

1. 创建完整的表单布局（至少包含 ROW、COL、INPUT）
2. 点击"保存"按钮
3. 刷新页面
4. 确认配置自动恢复
5. 检查 localStorage 中的数据

预期结果:
- 保存成功，显示成功提示
- 刷新后配置正确恢复

- [ ] **Step 4: 验证预览功能**

1. 点击"预览"按钮
2. 确认弹窗正确显示渲染结果
3. 确认 ROW/COL 布局正确渲染
4. 关闭预览弹窗

预期结果:
- 预览弹窗正确显示
- 表单布局与设计器一致

- [ ] **Step 5: 最终截图验证**

使用 `mcp__chrome-devtools__take_screenshot` 记录最终状态：
- 设计器整体界面
- 组件库中的 ROW/COL 组件
- 预览区的表格布局效果
- 属性配置面板

---

## 自我审查

### 规范覆盖检查

| 规范要求 | 计划任务 | 状态 |
|----------|----------|------|
| 添加 ROW 组件定义 | Task 1 | ✅ |
| 添加 COL 组件定义 | Task 1 | ✅ |
| ROW/COL 渲染函数 | Task 2 | ✅ |
| ROW/COL 属性配置 | Task 3 | ✅ |
| 更新 UI 组件库 | Task 4 | ✅ |
| 保存 API 集成 | Task 5 | ✅ |
| 加载 API 集成 | Task 5 | ✅ |
| E2E 验证 | Task 6 | ✅ |

### 占位符扫描

- ✅ 无 "TBD" 或 "TODO"
- ✅ 无 "implement later"
- ✅ 所有代码步骤都有完整代码
- ✅ 所有验证步骤都有预期结果

### 类型一致性

- ✅ ROW 组件属性: `name`, `code`, `gutter`, `children`
- ✅ COL 组件属性: `name`, `code`, `span`, `offset`, `children`
- ✅ 渲染函数使用相同属性名
- ✅ 属性配置面板使用相同属性名

---

## 执行选项

**计划已完成并保存到 `docs/superpowers/plans/2026-06-07-frontend-enhancement.md`**

**两种执行方式：**

**1. Subagent-Driven (推荐)** - 每个任务派遣独立 subagent，任务间有代码审查，快速迭代

**2. Inline Execution** - 在当前会话中执行，批量执行带检查点

**请选择执行方式？**
