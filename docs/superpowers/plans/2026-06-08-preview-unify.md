# 预览按钮统一渲染 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 统一渲染路径，让右上角预览按钮展示干净的正式表单

**Architecture:** `renderComponent()` 加 `mode` 参数，预览弹窗去掉 iframe 改为 div 直接渲染，删掉旧的 `renderComponentHTML()`

**Tech Stack:** 原生 JavaScript

---

## File Structure

| File | Action | Responsibility |
|------|--------|---------------|
| `src/main/resources/static/config/js/preview-renderer.js` | Modify | renderComponent + 所有 render 函数加 mode 参数 |
| `src/main/resources/static/config/js/config-api.js` | Modify | previewTemplate 改 div 渲染，删 renderComponentHTML + generatePreviewHTML |
| `src/main/resources/static/config/template-designer.html` | Modify | iframe 改 div |

---

### Task 1: preview-renderer.js — renderComponent 加 mode 参数透传

**Files:**
- Modify: `src/main/resources/static/config/js/preview-renderer.js:30-80`

- [ ] **Step 1: 修改 renderComponent 函数签名**

`renderComponent(component)` 改为 `renderComponent(component, mode)`，默认 `mode = 'designer'`。把 mode 传给每个 render 函数：

```javascript
function renderComponent(component, mode = 'designer') {
    const def = ComponentLibrary.getComponentDef(component.type);
    if (!def) return null;

    switch (component.type) {
        case 'PAGE':
            return renderPageComponent(component, mode);
        case 'CARD':
            return renderCardComponent(component, mode);
        case 'GRID':
            return renderGridComponent(component, mode);
        case 'ROW':
            return renderRowComponent(component, mode);
        case 'COL':
            return renderColComponent(component, mode);
        case 'TAB':
            return renderTabComponent(component, mode);
        case 'COLLAPSE':
            return renderCollapseComponent(component, mode);
        case 'INPUT':
            return renderInputComponent(component, mode);
        case 'SELECT':
            return renderSelectComponent(component, mode);
        case 'DATE':
            return renderDateComponent(component, mode);
        case 'NUMBER':
            return renderNumberComponent(component, mode);
        case 'MONEY':
            return renderMoneyComponent(component, mode);
        case 'TEXTAREA':
            return renderTextareaComponent(component, mode);
        case 'SUPPLIER_SELECT':
            return renderSupplierSelectComponent(component, mode);
        case 'DETAIL_TABLE':
            return renderDetailTableComponent(component, mode);
        case 'ATTACHMENT':
            return renderAttachmentComponent(component, mode);
        case 'IMAGE':
            return renderImageComponent(component, mode);
        case 'BUTTON':
            return renderButtonComponent(component, mode);
        case 'QUERY_DIALOG':
            return renderQueryDialogComponent(component, mode);
        case 'SAVE_BUTTON':
            return renderSaveButtonComponent(component, mode);
        case 'SUBMIT_BUTTON':
            return renderSubmitButtonComponent(component, mode);
        default:
            return renderDefaultComponent(component, mode);
    }
}
```

- [ ] **Step 2: 提取公共 preview 样式工具函数**

在文件顶部（renderPreview 函数之前）加两个工具函数：

```javascript
function isPreview(mode) {
    return mode === 'preview';
}

function formGroupHTML(label, contentHTML) {
    return `<div style="margin-bottom:15px;">
        <label style="display:block;margin-bottom:5px;font-weight:600;color:#24292e;">${label}</label>
        ${contentHTML}
    </div>`;
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/js/preview-renderer.js
git commit -m "refactor: renderComponent adds mode parameter for unified rendering"
```

---

### Task 2: preview-renderer.js — 布局组件支持 preview mode

**Files:**
- Modify: `src/main/resources/static/config/js/preview-renderer.js`

改 4 个布局组件：PAGE、CARD、GRID、ROW+COL。每个函数加 `mode` 参数，preview 模式去掉边框/拖拽提示/组件名标签。

- [ ] **Step 1: 修改 renderPageComponent**

```javascript
function renderPageComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = 'padding: 0;';
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;
        element.style.cssText = `
            min-height: 200px;
            border: 2px dashed #d1d5da;
            background: #fafbfc;
        `;
        element.innerHTML = `
            <div class="component-preview-label">
                <i class="fas fa-file"></i> ${component.name}
            </div>
            <div class="component-preview-content">
                ${component.description || '页面容器'}
            </div>
        `;
    }

    const childrenContainer = document.createElement('div');
    if (mode === 'preview') {
        // preview: no extra wrapper class
    } else {
        element.querySelector('.component-children') || childrenContainer;
    }

    if (component.children && component.children.length > 0) {
        const container = mode === 'preview' ? element : (element.querySelector('.component-children') || element);
        component.children.forEach(child => {
            const childElement = renderComponent(child, mode);
            if (childElement) {
                container.appendChild(childElement);
            }
        });
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}
```

- [ ] **Step 2: 修改 renderCardComponent**

```javascript
function renderCardComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = `
            border: 1px solid #e1e4e8;
            border-radius: 8px;
            margin-bottom: 20px;
            background: white;
        `;
        element.innerHTML = `
            <div style="padding:15px 20px;background:#f6f8fa;border-bottom:1px solid #e1e4e8;font-weight:600;">
                ${component.title || component.name}
            </div>
            <div style="padding:20px;"></div>
        `;
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;
        element.style.cssText = `
            border: 1px solid #e1e4e8;
            background: white;
            margin-bottom: 10px;
        `;
        element.innerHTML = `
            <div class="card-header" style="padding:10px 15px;background:#f6f8fa;border-bottom:1px solid #e1e4e8;font-weight:600;">
                <i class="fas fa-square"></i> ${component.title || component.name}
            </div>
            <div class="card-body" style="padding:15px;">
                <div class="component-children"></div>
            </div>
        `;
    }

    const container = mode === 'preview'
        ? element.querySelector('div[style*="padding:20px"]')
        : element.querySelector('.component-children');

    if (component.children && component.children.length > 0 && container) {
        component.children.forEach(child => {
            const childElement = renderComponent(child, mode);
            if (childElement) {
                container.appendChild(childElement);
            }
        });
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}
```

- [ ] **Step 3: 修改 renderGridComponent**

```javascript
function renderGridComponent(component, mode = 'designer') {
    const element = document.createElement('div');
    const columns = component.columns || 2;
    const gutter = component.gutter || 16;

    if (mode === 'preview') {
        element.style.cssText = `margin-bottom:20px;`;
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;
        element.style.cssText = `background:#f6f8fa;padding:10px;margin-bottom:10px;`;

        const labelDiv = document.createElement('div');
        labelDiv.className = 'component-preview-label';
        labelDiv.style.marginBottom = '10px';
        labelDiv.innerHTML = `<i class="fas fa-th"></i> ${component.name} (${columns}列)`;
        element.appendChild(labelDiv);
    }

    const gridContainer = document.createElement('div');
    gridContainer.style.cssText = `
        display: grid;
        grid-template-columns: repeat(${columns}, 1fr);
        gap: ${gutter}px;
    `;

    for (let i = 0; i < columns; i++) {
        const cell = document.createElement('div');

        if (mode === 'preview') {
            cell.style.cssText = `min-height:1px;`;
        } else {
            cell.className = 'grid-cell';
            cell.dataset.gridColumn = i;
            cell.style.cssText = `
                min-height: 80px;
                border: 1px dashed #d1d5da;
                background: white;
                padding: 10px;
            `;
        }

        const columnChildren = (component.children || []).filter(c => c.gridColumn === i);
        if (columnChildren.length > 0) {
            columnChildren.forEach(child => {
                const childElement = renderComponent(child, mode);
                if (childElement) {
                    cell.appendChild(childElement);
                }
            });
        } else if (mode !== 'preview') {
            cell.innerHTML = `<div style="padding:20px;text-align:center;color:#b4b4b4;">
                拖拽组件到第${i + 1}列
            </div>`;
        }

        gridContainer.appendChild(cell);
    }

    element.appendChild(gridContainer);

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}
```

- [ ] **Step 4: 修改 renderRowComponent 和 renderColComponent**

```javascript
function renderRowComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = `
            display: flex;
            flex-wrap: wrap;
            margin-bottom: 15px;
        `;
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;
        element.style.cssText = `
            display: flex;
            flex-wrap: wrap;
            margin-bottom: 10px;
            margin-left: -${(component.gutter || 16) / 2}px;
            margin-right: -${(component.gutter || 16) / 2}px;
        `;
    }

    if (component.children && component.children.length > 0) {
        component.children.forEach(child => {
            const childElement = renderComponent(child, mode);
            if (childElement) {
                element.appendChild(childElement);
            }
        });
    } else if (mode !== 'preview') {
        element.innerHTML = `
            <div class="component-preview-label" style="width:100%;padding:10px;text-align:center;color:#b4b4b4;">
                <i class="fas fa-grip-lines"></i> ${component.name} - 拖拽列组件到此处
            </div>
        `;
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}

function renderColComponent(component, mode = 'designer') {
    const element = document.createElement('div');
    const span = component.span || 12;
    const offset = component.offset || 0;
    const widthPercent = (span / 24) * 100;
    const offsetPercent = (offset / 24) * 100;

    if (mode === 'preview') {
        element.style.cssText = `
            flex: 0 0 ${widthPercent}%;
            margin-left: ${offsetPercent}%;
            padding-left: 8px;
            padding-right: 8px;
            box-sizing: border-box;
        `;
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;
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
    }

    if (component.children && component.children.length > 0) {
        const childrenContainer = document.createElement('div');
        childrenContainer.className = 'component-children';
        component.children.forEach(child => {
            const childElement = renderComponent(child, mode);
            if (childElement) {
                childrenContainer.appendChild(childElement);
            }
        });
        element.appendChild(childrenContainer);
    } else if (mode !== 'preview') {
        element.innerHTML = `
            <div class="component-preview-label" style="padding:10px;text-align:center;color:#b4b4b4;">
                <i class="fas fa-grip-lines-vertical"></i> ${component.name} (${span}/24列)
            </div>
            <div class="component-children"></div>
        `;
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}
```

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/static/config/js/preview-renderer.js
git commit -m "feat: layout components support preview mode"
```

---

### Task 3: preview-renderer.js — 容器组件支持 preview mode（TAB + COLLAPSE）

**Files:**
- Modify: `src/main/resources/static/config/js/preview-renderer.js`

- [ ] **Step 1: 修改 renderTabComponent**

```javascript
function renderTabComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = 'margin-bottom:20px;';
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;
    }

    // Tab header
    const headerDiv = document.createElement('div');
    headerDiv.style.cssText = 'display:flex;border-bottom:2px solid #e1e4e8;margin-bottom:10px;';

    if (!mode || mode === 'designer') {
        const labelDiv = document.createElement('div');
        labelDiv.className = 'component-preview-label';
        labelDiv.style.marginBottom = '10px';
        labelDiv.innerHTML = `<i class="fas fa-folder"></i> ${component.name}`;
        element.appendChild(labelDiv);
    }

    // Tab items + content
    if (component.tabs && component.tabs.length > 0) {
        component.tabs.forEach((tab, index) => {
            const tabItem = document.createElement('div');
            tabItem.style.cssText = `
                padding: 8px 16px;
                cursor: pointer;
                border-bottom: ${index === 0 ? '2px solid #667eea' : 'none'};
                margin-bottom: -2px;
                color: ${index === 0 ? '#667eea' : '#586069'};
                font-weight: ${index === 0 ? '600' : 'normal'};
            `;
            tabItem.textContent = tab.name;
            tabItem.dataset.tabIndex = index;
            headerDiv.appendChild(tabItem);
        });

        element.appendChild(headerDiv);

        // Content containers for each tab
        component.tabs.forEach((tab, index) => {
            const contentDiv = document.createElement('div');
            contentDiv.className = 'tab-content';
            contentDiv.dataset.tabContent = index;
            contentDiv.style.cssText = `min-height:100px;display:${index === 0 ? 'block' : 'none'};`;

            if (tab.children && tab.children.length > 0) {
                tab.children.forEach(child => {
                    const childComponent = typeof child === 'string' ? findComponentById(child) : child;
                    if (childComponent) {
                        const childElement = renderComponent(childComponent, mode);
                        if (childElement) {
                            contentDiv.appendChild(childElement);
                        }
                    }
                });
            }

            element.appendChild(contentDiv);
        });

        // Tab click handler
        headerDiv.querySelectorAll('[data-tab-index]').forEach(tabItem => {
            tabItem.addEventListener('click', (e) => {
                e.stopPropagation();
                const tabIndex = tabItem.dataset.tabIndex;

                // Update tab styles
                headerDiv.querySelectorAll('[data-tab-index]').forEach((t, i) => {
                    t.style.borderBottom = i == tabIndex ? '2px solid #667eea' : 'none';
                    t.style.color = i == tabIndex ? '#667eea' : '#586069';
                    t.style.fontWeight = i == tabIndex ? '600' : 'normal';
                });

                // Show/hide content
                element.querySelectorAll('[data-tab-content]').forEach((c, i) => {
                    c.style.display = i == tabIndex ? 'block' : 'none';
                });
            });
        });
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}
```

- [ ] **Step 2: 修改 renderCollapseComponent**

```javascript
function renderCollapseComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = 'margin-bottom:20px;';
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;
        element.style.cssText = 'background:#f6f8fa;padding:10px;margin-bottom:10px;';
    }

    if (!mode || mode === 'designer') {
        const labelDiv = document.createElement('div');
        labelDiv.className = 'component-preview-label';
        labelDiv.style.marginBottom = '10px';
        labelDiv.innerHTML = `<i class="fas fa-chevron-down"></i> ${component.name}`;
        element.appendChild(labelDiv);
    }

    if (component.panels && component.panels.length > 0) {
        component.panels.forEach((panel, index) => {
            const isExpanded = panel.expanded !== false;

            const panelDiv = document.createElement('div');
            panelDiv.style.cssText = mode === 'preview'
                ? 'border:1px solid #e1e4e8;border-radius:4px;margin-bottom:8px;overflow:hidden;'
                : 'border:1px solid #e1e4e8;margin-bottom:-1px;';

            const headerDiv = document.createElement('div');
            headerDiv.style.cssText = `
                padding:10px 15px;
                background:#f6f8fa;
                cursor:pointer;
                display:flex;
                align-items:center;
                gap:8px;
                font-weight:600;
            `;
            headerDiv.innerHTML = `
                <i class="fas fa-chevron-${isExpanded ? 'down' : 'right'}"></i>
                <span>${panel.name}</span>
            `;

            const contentDiv = document.createElement('div');
            contentDiv.style.cssText = `padding:15px;display:${isExpanded ? 'block' : 'none'};`;

            if (panel.children && panel.children.length > 0) {
                panel.children.forEach(child => {
                    const childComponent = typeof child === 'string' ? findComponentById(child) : child;
                    if (childComponent) {
                        const childElement = renderComponent(childComponent, mode);
                        if (childElement) {
                            contentDiv.appendChild(childElement);
                        }
                    }
                });
            }

            // Toggle click
            headerDiv.addEventListener('click', (e) => {
                e.stopPropagation();
                const isVisible = contentDiv.style.display !== 'none';
                contentDiv.style.display = isVisible ? 'none' : 'block';
                const icon = headerDiv.querySelector('i');
                icon.className = `fas fa-chevron-${isVisible ? 'right' : 'down'}`;
            });

            panelDiv.appendChild(headerDiv);
            panelDiv.appendChild(contentDiv);
            element.appendChild(panelDiv);
        });
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/js/preview-renderer.js
git commit -m "feat: TAB and COLLAPSE support preview mode"
```

---

### Task 4: preview-renderer.js — 基础组件支持 preview mode

**Files:**
- Modify: `src/main/resources/static/config/js/preview-renderer.js`

基础表单组件（INPUT、SELECT、DATE、NUMBER、MONEY、TEXTAREA）在 preview 模式下用 `formGroupHTML` 输出标准表单样式，去掉组件边框和标签。

改动模式统一：每个函数加 `mode = 'designer'` 参数，preview 模式下用 `formGroupHTML(component.name, ...)` 包裹表单控件，designer 模式保持不变。

涉及函数：
- `renderInputComponent`
- `renderSelectComponent`
- `renderDateComponent`
- `renderNumberComponent`
- `renderMoneyComponent`
- `renderTextareaComponent`

每个函数的 preview 模式模板：

```javascript
// INPUT
if (mode === 'preview') {
    element.innerHTML = formGroupHTML(component.name,
        `<input type="text" placeholder="${component.placeholder || '请输入'}"
               style="width:100%;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px;font-size:14px;"
               ${component.readonly ? 'readonly' : ''}>`);
    return element;
}

// SELECT
if (mode === 'preview') {
    element.innerHTML = formGroupHTML(component.name,
        `<select style="width:100%;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px;font-size:14px;"
                 ${component.readonly ? 'disabled' : ''}>
            <option>${component.placeholder || '请选择'}</option>
        </select>`);
    return element;
}

// DATE
if (mode === 'preview') {
    element.innerHTML = formGroupHTML(component.name,
        `<input type="date" style="width:100%;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px;font-size:14px;"
               ${component.readonly ? 'readonly' : ''}>`);
    return element;
}

// NUMBER
if (mode === 'preview') {
    element.innerHTML = formGroupHTML(component.name,
        `<input type="number" placeholder="${component.placeholder || '请输入数字'}"
               min="${component.min || 0}" max="${component.max || 999999}"
               style="width:100%;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px;font-size:14px;"
               ${component.readonly ? 'readonly' : ''}>`);
    return element;
}

// MONEY
if (mode === 'preview') {
    element.innerHTML = formGroupHTML(component.name,
        `<div style="display:flex;align-items:center;">
            <span style="padding:8px 12px;background:#f6f8fa;border:1px solid #d1d5da;border-right:none;border-radius:4px 0 0 4px;">
                ${component.currency || 'CNY'}
            </span>
            <input type="number" placeholder="${component.placeholder || '请输入金额'}"
                   style="flex:1;padding:8px 12px;border:1px solid #d1d5da;border-radius:0 4px 4px 0;font-size:14px;"
                   ${component.readonly ? 'readonly' : ''}>
        </div>`);
    return element;
}

// TEXTAREA
if (mode === 'preview') {
    element.innerHTML = formGroupHTML(component.name,
        `<textarea rows="${component.rows || 4}" placeholder="${component.placeholder || '请输入内容'}"
                  style="width:100%;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px;resize:vertical;font-size:14px;"
                  ${component.readonly ? 'readonly' : ''}></textarea>`);
    return element;
}
```

- [ ] **Step 1: 给所有 6 个基础组件函数加 mode 参数和 preview 分支**

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/static/config/js/preview-renderer.js
git commit -m "feat: form components support preview mode"
```

---

### Task 5: preview-renderer.js — 业务/动作组件支持 preview mode

**Files:**
- Modify: `src/main/resources/static/config/js/preview-renderer.js`

业务组件（SUPPLIER_SELECT、DETAIL_TABLE、ATTACHMENT、IMAGE）和动作组件（BUTTON、QUERY_DIALOG、SAVE_BUTTON、SUBMIT_BUTTON）的 preview 模式。

改动模式与 Task 4 一致：加 `mode` 参数，preview 分支用 `formGroupHTML`。

```javascript
// SUPPLIER_SELECT
if (mode === 'preview') {
    element.innerHTML = formGroupHTML(component.name,
        `<div style="display:flex;align-items:center;">
            <input type="text" placeholder="${component.placeholder || '请选择供应商'}"
                   style="flex:1;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px 0 0 4px;"
                   ${component.readonly ? 'readonly' : ''}>
            <button style="padding:8px 12px;border:1px solid #667eea;background:#667eea;color:white;border-radius:0 4px 4px 0;cursor:pointer;">
                <i class="fas fa-search"></i>
            </button>
        </div>`);
    return element;
}

// DETAIL_TABLE — preview 模式渲染表格，无编辑功能
// ATTACHMENT — preview 模式显示上传区域
// IMAGE — preview 模式显示图片上传区域
// BUTTON / QUERY_DIALOG / SAVE_BUTTON / SUBMIT_BUTTON — preview 模式只显示按钮
```

动作组件 preview 模式简单处理：只输出按钮，不加 formGroupHTML 包裹。

- [ ] **Step 1: 给所有 7 个函数加 mode 参数和 preview 分支**

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/static/config/js/preview-renderer.js
git commit -m "feat: business and action components support preview mode"
```

---

### Task 6: template-designer.html — iframe 改 div

**Files:**
- Modify: `src/main/resources/static/config/template-designer.html:503-504`

- [ ] **Step 1: 替换 iframe 为 div**

把：
```html
<iframe id="previewFrame" style="width:100%; height:70vh; border:none;"></iframe>
```

改为：
```html
<div id="previewContent" style="width:100%;max-height:70vh;overflow-y:auto;padding:20px;background:#f5f7fa;"></div>
```

注意：这个 div 的 id 不能和左侧预览区的 `previewContent` 冲突。改用 `previewModalContent`：

```html
<div id="previewModalContent" style="width:100%;max-height:70vh;overflow-y:auto;padding:20px;background:#f5f7fa;border-radius:4px;"></div>
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/static/config/template-designer.html
git commit -m "refactor: preview modal replaces iframe with div"
```

---

### Task 7: config-api.js — 重写 previewTemplate，删 renderComponentHTML

**Files:**
- Modify: `src/main/resources/static/config/js/config-api.js:504-689`

- [ ] **Step 1: 重写 previewTemplate 函数**

把 `previewTemplate()`（504-524行）改为：

```javascript
function previewTemplate() {
    const config = DesignerState.templateConfig;

    const validation = validateConfig(config);
    if (!validation.valid) {
        showNotification(validation.message, 'error');
        return;
    }

    const modal = document.getElementById('previewModal');
    const content = document.getElementById('previewModalContent');
    content.innerHTML = '';

    if (config.rootComponent) {
        const rootElement = renderComponent(config.rootComponent, 'preview');
        if (rootElement) {
            content.appendChild(rootElement);
        }
    }

    modal.classList.add('active');
}
```

- [ ] **Step 2: 删除 generatePreviewHTML 和 renderComponentHTML 函数**

删除 config-api.js 里的：
- `generatePreviewHTML()` 函数（528-568行）
- `renderComponentHTML()` 函数（573-689行）

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/js/config-api.js
git commit -m "refactor: previewTemplate uses renderComponent, delete renderComponentHTML"
```

---

### Task 8: E2E 验证

**Files:** 无代码改动

- [ ] **Step 1: 启动应用**

```bash
mvn spring-boot:run
```

- [ ] **Step 2: 用浏览器打开模板设计器页面**

打开 `http://localhost:8888/config/template-designer.html?templateId=2063944529064701953&versionId=2063945060336218113`

- [ ] **Step 3: 验证左侧预览区不受影响**

确认左侧预览区的组件边框、拖拽提示、组件名标签都正常显示。

- [ ] **Step 4: 点击右上角预览按钮**

确认弹窗展示干净表单：
- GRID 组件保持多列布局
- COLLAPSE 面板可以折叠/展开
- TAB 页签可以切换
- 所有表单组件正常显示（无边框、无拖拽提示）
- 按钮组件正常显示
