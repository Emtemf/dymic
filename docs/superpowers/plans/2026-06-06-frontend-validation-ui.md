# 前端验证界面实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现业务人员模板配置界面（简化版）和预览验证界面，用于开发验证后端 API 功能正确性。界面采用从上到下堆积布局，实时自动保存。

**Architecture:** 两栏式布局（左侧组件库 + 右侧配置画布），简化版开发验证工具，不是交付给业务用户的正式系统。使用原生 HTML5/CSS3/JavaScript ES6+，无框架依赖。

**Tech Stack:** HTML5, CSS3 (frontend-design), JavaScript ES6+, Fetch API, chrome-devtools (验证工具)

---

## File Structure

**新增文件**：

```
前端验证界面文件结构：
├── src/main/resources/static/
│   ├── template-config.html            # 业务人员模板配置界面（简化版）
│   ├── template-preview.html           # 预览验证界面
│   ├── css/
│   │   ├── template-config.css         # 配置界面样式
│   │   └── template-preview.css        # 预览界面样式
│   └── js/
│   │   ├── component-library.js        # 组件库逻辑
│   │   ├── config-canvas.js            # 配置画布逻辑
│   │   ├── auto-save.js                # 自动保存机制
│   │   ├── template-config-api.js      # 模板配置 API 调用封装
│   │   ├── template-render.js          # 动态渲染逻辑
│   │   └── template-preview-api.js     # 预览 API 调用封装
```

---

## Task 1: 创建模板配置界面 HTML

**Files:**
- Create: `src/main/resources/static/template-config.html`

### Step 1: 创建基础 HTML 结构

- [ ] **创建 template-config.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>模板配置 - 开发验证界面</title>
    <link rel="stylesheet" href="/css/template-config.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/frontend-design@latest/dist/css/frontend-design.min.css">
</head>
<body>
    <div class="container">
        <!-- 顶部信息栏 -->
        <div class="header">
            <h1>模板配置（开发验证）</h1>
            <div class="template-info">
                模板：<span id="template-name">采购合同模板</span>
                版本：<span id="version-no">V1.0（草稿）</span>
            </div>
            <div class="header-actions">
                <button id="btn-preview" class="btn btn-secondary">预览</button>
            </div>
        </div>

        <!-- 主配置区域 -->
        <div class="main-area">
            <!-- 左侧组件库 -->
            <div class="component-library">
                <h3>组件库</h3>
                <div class="component-category">
                    <h4>基础组件</h4>
                    <button class="component-btn" data-type="INPUT">文本输入框</button>
                    <button class="component-btn" data-type="NUMBER">数字输入框</button>
                    <button class="component-btn" data-type="DATE">日期选择器</button>
                    <button class="component-btn" data-type="SELECT">下拉选择框</button>
                    <button class="component-btn" data-type="MONEY">金额输入框</button>
                </div>
                <div class="component-category">
                    <h4>布局组件</h4>
                    <button class="component-btn" data-type="CARD">卡片容器</button>
                </div>
                <div class="component-category">
                    <h4>按钮组件</h4>
                    <button class="component-btn" data-type="SAVE_BUTTON">保存按钮</button>
                </div>
            </div>

            <!-- 右侧配置画布 + 属性面板 -->
            <div class="config-area">
                <!-- 配置画布 -->
                <div class="config-canvas">
                    <div id="canvas-placeholder" class="placeholder">
                        点击左侧组件添加到画布
                    </div>
                    <div id="config-canvas-content"></div>
                </div>

                <!-- 属性面板（选中组件后显示） -->
                <div class="property-panel" id="property-panel" style="display: none;">
                    <h3>属性配置</h3>
                    <form id="property-form">
                        <div class="form-group">
                            <label for="display-name">显示名称</label>
                            <input type="text" id="display-name" name="displayName">
                        </div>
                        <div class="form-group">
                            <label for="required">是否必填</label>
                            <input type="checkbox" id="required" name="required">
                        </div>
                        <div class="form-group">
                            <label for="placeholder">提示文字</label>
                            <input type="text" id="placeholder" name="placeholder">
                        </div>
                        <div class="form-group" id="data-source-group" style="display: none;">
                            <label for="data-source-type">数据来源</label>
                            <select id="data-source-type" name="dataSourceType">
                                <option value="STATIC">静态选项</option>
                                <option value="PROVIDER">数据提供方</option>
                            </select>
                        </div>
                        <div class="form-group" id="static-options-group" style="display: none;">
                            <label>静态选项</label>
                            <textarea id="static-options" name="staticOptions" rows="5" placeholder="CNY:人民币\nUSD:美元"></textarea>
                        </div>
                        <div class="form-group" id="data-provider-group" style="display: none;">
                            <label for="data-provider-id">数据提供方</label>
                            <select id="data-provider-id" name="dataProviderId">
                                <!-- 动态填充 -->
                            </select>
                        </div>
                        <div class="form-actions">
                            <button id="btn-save-property" class="btn btn-primary">保存</button>
                            <button id="btn-cancel-property" class="btn btn-secondary">取消</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script src="/js/template-config-api.js"></script>
    <script src="/js/component-library.js"></script>
    <script src="/js/config-canvas.js"></script>
    <script src="/js/auto-save.js"></script>
</body>
</html>
```

---

## Task 2: 创建配置界面样式

**Files:**
- Create: `src/main/resources/static/css/template-config.css`

### Step 1: 创建基础样式

- [ ] **创建 template-config.css**

```css
/* 基础容器样式 */
.container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 10px;
}

/* 顶部信息栏 */
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding: 10px 15px;
    background: white;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.header h1 {
    font-size: 18px;
    color: #333;
}

.template-info {
    font-size: 14px;
    color: #666;
}

/* 主配置区域 */
.main-area {
    display: flex;
    gap: 15px;
    height: calc(100vh - 80px);
}

/* 左侧组件库 */
.component-library {
    width: 250px;
    background: white;
    padding: 15px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    overflow-y: auto;
}

.component-library h3 {
    font-size: 16px;
    margin-bottom: 15px;
    color: #333;
}

.component-category {
    margin-bottom: 20px;
}

.component-category h4 {
    font-size: 14px;
    color: #666;
    margin-bottom: 10px;
}

.component-btn {
    display: block;
    width: 100%;
    padding: 8px 10px;
    margin-bottom: 8px;
    border: 1px solid #e0e0e0;
    background: white;
    color: #333;
    font-size: 14px;
    cursor: pointer;
    border-radius: 4px;
    transition: background 0.2s;
}

.component-btn:hover {
    background: #f5f5f5;
}

.component-btn.active {
    background: #4CAF50;
    color: white;
    border-color: #4CAF50;
}

/* 右侧配置区域 */
.config-area {
    flex-grow: 1;
    display: flex;
    gap: 15px;
    background: white;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

/* 配置画布 */
.config-canvas {
    flex-grow: 1;
    padding: 15px;
    overflow-y: auto;
}

.placeholder {
    text-align: center;
    color: #999;
    padding: 50px 20px;
}

.canvas-card {
    background: #f9f9f9;
    border: 1px solid #e0e0e0;
    padding: 15px;
    margin-bottom: 15px;
    border-radius: 4px;
}

.canvas-card h4 {
    font-size: 16px;
    color: #333;
    margin-bottom: 10px;
}

.canvas-field {
    background: white;
    border: 1px solid #e0e0e0;
    padding: 10px;
    margin-bottom: 8px;
    border-radius: 4px;
    cursor: pointer;
}

.canvas-field:hover {
    border-color: #4CAF50;
}

.canvas-field.selected {
    border-color: #4CAF50;
    background: #f0f8f0;
}

.canvas-button {
    background: #4CAF50;
    color: white;
    padding: 8px 16px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    margin-top: 10px;
}

/* 属性面板 */
.property-panel {
    width: 300px;
    padding: 15px;
    border-left: 1px solid #e0e0e0;
    overflow-y: auto;
}

.property-panel h3 {
    font-size: 16px;
    color: #333;
    margin-bottom: 15px;
}

.form-group {
    margin-bottom: 15px;
}

.form-group label {
    display: block;
    margin-bottom: 5px;
    color: #666;
    font-size: 14px;
}

.form-group input[type="text"],
.form-group select,
.form-group textarea {
    width: 100%;
    padding: 8px 10px;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    font-size: 14px;
}

.form-actions {
    margin-top: 20px;
}

/* 按钮 */
.btn {
    padding: 8px 16px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
}

.btn-primary {
    background: #4CAF50;
    color: white;
}

.btn-secondary {
    background: #f0f0f0;
    color: #666;
}
```

---

## Task 3: 创建模板配置 API 封装

**Files:**
- Create: `src/main/resources/static/js/template-config-api.js`

### Step 1: 创建 API 封装

- [ ] **创建 template-config-api.js**

```javascript
// template-config-api.js
class TemplateConfigApi {
    constructor(templateId, versionId) {
        this.templateId = templateId || 100;
        this.versionId = versionId || 200;
        this.baseUrl = `/api/templates/${this.templateId}/versions/${this.versionId}`;
    }

    // 创建布局节点
    async createLayoutNode(data) {
        const response = await fetch(`${this.baseUrl}/layout-nodes`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        const result = await response.json();
        return result.data;
    }

    // 创建字段定义（同时创建字段组件绑定）
    async createFieldDef(data) {
        const response = await fetch(`${this.baseUrl}/field-defs`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        const result = await response.json();
        return result.data;
    }

    // 更新字段组件绑定
    async updateFieldComponent(id, data) {
        const response = await fetch(`${this.baseUrl}/field-components/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        const result = await response.json();
        return result.data;
    }

    // 创建动作配置
    async createAction(data) {
        const response = await fetch(`${this.baseUrl}/actions`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });
        const result = await response.json();
        return result.data;
    }

    // 获取布局节点列表
    async getLayoutNodes() {
        const response = await fetch(`${this.baseUrl}/layout-nodes`);
        const result = await response.json();
        return result.data;
    }

    // 获取数据提供方列表
    async getDataProviders() {
        const response = await fetch('/api/data-providers');
        const result = await response.json();
        return result.data;
    }
}

window.TemplateConfigApi = TemplateConfigApi;
```

---

## Task 4: 创建组件库逻辑

**Files:**
- Create: `src/main/resources/static/js/component-library.js`

### Step 1: 创建组件库逻辑

- [ ] **创建 component-library.js**

```javascript
// component-library.js
class ComponentLibrary {
    constructor(canvas) {
        this.canvas = canvas;
        this.api = new TemplateConfigApi();
        this.bindEvents();
    }

    bindEvents() {
        const buttons = document.querySelectorAll('.component-btn');
        buttons.forEach(btn => {
            btn.addEventListener('click', () => {
                const type = btn.dataset.type;
                this.onComponentClick(type);
            });
        });
    }

    async onComponentClick(type) {
        if (type === 'CARD') {
            const name = prompt('请输入卡片标题：');
            if (name) {
                await this.addCard(name);
            }
        } else if (type === 'SAVE_BUTTON') {
            await this.addSaveButton();
        } else {
            await this.addFieldComponent(type);
        }
    }

    async addCard(name) {
        const parentId = this.canvas.getCurrentParentId();
        const sortNo = this.canvas.getNextSortNo(parentId);
        
        const result = await this.api.createLayoutNode({
            nodeType: 'CARD',
            displayName: name,
            parentId: parentId,
            sortNo: sortNo
        });
        
        this.canvas.addCard(result);
    }

    async addFieldComponent(type) {
        const displayName = prompt('请输入显示名称：');
        if (!displayName) return;
        
        const parentId = this.canvas.getCurrentParentId();
        const sortNo = this.canvas.getNextSortNo(parentId);
        const required = confirm('是否必填？');
        const placeholder = prompt('请输入提示文字：');
        
        const data = {
            layoutNodeId: parentId,
            displayName: displayName,
            componentType: type,
            required: required,
            placeholder: placeholder || '',
            sortNo: sortNo,
            dataSourceType: 'STATIC'
        };
        
        if (type === 'SELECT') {
            const sourceType = prompt('数据来源：STATIC（静态选项）或 PROVIDER（数据提供方）？');
            data.dataSourceType = sourceType || 'STATIC';
            
            if (data.dataSourceType === 'STATIC') {
                const optionsText = prompt('请输入选项（格式：CNY:人民币，USD:美元）');
                if (optionsText) {
                    data.staticOptions = this.parseOptions(optionsText);
                }
            } else {
                const providers = await this.api.getDataProviders();
                if (providers.length > 0) {
                    const providerId = prompt(`请输入数据提供方ID：\n${providers.map(p => `${p.id}: ${p.providerName}`).join('\n')}`);
                    data.dataProviderId = parseInt(providerId);
                }
            }
        }
        
        const result = await this.api.createFieldDef(data);
        this.canvas.addField(result);
    }

    async addSaveButton() {
        const displayName = prompt('请输入按钮文字：') || '保存';
        const confirmText = prompt('请输入确认提示：') || '是否保存？';
        
        const result = await this.api.createAction({
            actionType: 'SAVE_CONTRACT',
            displayName: displayName,
            confirmRequired: true,
            confirmText: confirmText,
            sortNo: this.canvas.getNextSortNo(null)
        });
        
        this.canvas.addButton(result);
    }

    parseOptions(text) {
        const options = [];
        const lines = text.split(',');
        lines.forEach((line, index) => {
            const parts = line.split(':');
            if (parts.length === 2) {
                options.push({
                    value: parts[0].trim(),
                    label: parts[1].trim(),
                    sort: index + 1
                });
            }
        });
        return options;
    }
}

window.ComponentLibrary = ComponentLibrary;
```

---

## Task 5: 创建配置画布逻辑

**Files:**
- Create: `src/main/resources/static/js/config-canvas.js`

### Step 1: 创建配置画布逻辑

- [ ] **创建 config-canvas.js**

```javascript
// config-canvas.js
class ConfigCanvas {
    constructor() {
        this.content = document.getElementById('config-canvas-content');
        this.placeholder = document.getElementById('canvas-placeholder');
        this.components = [];
        this.currentParentId = null;
        this.selectedComponent = null;
    }

    getCurrentParentId() {
        return this.currentParentId;
    }

    getNextSortNo(parentId) {
        const children = this.components.filter(c => c.parentId === parentId);
        return children.length + 1;
    }

    addCard(data) {
        this.hidePlaceholder();
        
        const cardHtml = `
            <div class="canvas-card" data-id="${data.id}" data-parent-id="${data.parentId}">
                <h4>${data.nodeName}</h4>
                <div class="card-fields"></div>
            </div>
        `;
        
        this.content.insertAdjacentHTML('beforeend', cardHtml);
        this.components.push(data);
        this.currentParentId = data.id;
    }

    addField(data) {
        const fieldHtml = `
            <div class="canvas-field" data-id="${data.fieldDef.id}" data-component-id="${data.fieldComponent.id}" data-parent-id="${data.fieldDef.layoutNodeId}">
                <strong>${data.fieldDef.fieldNameCn}</strong> (${data.fieldComponent.componentType})
            </div>
        `;
        
        const cardElement = document.querySelector(`.canvas-card[data-id="${this.currentParentId}"]`);
        if (cardElement) {
            const fieldsDiv = cardElement.querySelector('.card-fields');
            fieldsDiv.insertAdjacentHTML('beforeend', fieldHtml);
        }
        
        this.components.push(data);
        this.bindFieldClick();
    }

    addButton(data) {
        const buttonHtml = `
            <div class="canvas-button" data-id="${data.id}">
                ${data.displayName}
            </div>
        `;
        
        this.content.insertAdjacentHTML('beforeend', buttonHtml);
        this.components.push(data);
    }

    bindFieldClick() {
        const fields = document.querySelectorAll('.canvas-field');
        fields.forEach(field => {
            field.addEventListener('click', () => {
                this.selectField(field);
            });
        });
    }

    selectField(element) {
        // 取消之前选中
        document.querySelectorAll('.canvas-field.selected').forEach(el => {
            el.classList.remove('selected');
        });
        
        // 选中当前
        element.classList.add('selected');
        this.selectedComponent = element;
        
        // 显示属性面板
        this.showPropertyPanel(element.dataset.id, element.dataset.componentId);
    }

    showPropertyPanel(fieldDefId, fieldComponentId) {
        const panel = document.getElementById('property-panel');
        panel.style.display = 'block';
        
        // 填充当前属性（从 components 数据）
        const data = this.components.find(c => c.fieldDef && c.fieldDef.id == fieldDefId);
        if (data) {
            document.getElementById('display-name').value = data.fieldDef.fieldNameCn;
            document.getElementById('required').checked = data.fieldDef.requiredDefault === 1;
            document.getElementById('placeholder').value = data.fieldComponent.placeholder || '';
        }
    }

    hidePlaceholder() {
        if (this.placeholder) {
            this.placeholder.style.display = 'none';
        }
    }
}

window.ConfigCanvas = ConfigCanvas;
```

---

## Task 6: 创建自动保存机制

**Files:**
- Create: `src/main/resources/static/js/auto-save.js`

### Step 1: 创建自动保存机制

- [ ] **创建 auto-save.js**

```javascript
// auto-save.js
class AutoSave {
    constructor(api) {
        this.api = api;
        this.saveQueue = [];
        this.saveTimer = null;
    }

    onPropertyChange(componentId, field, value) {
        this.saveQueue.push({
            type: 'UPDATE_FIELD_COMPONENT',
            componentId: componentId,
            field: field,
            value: value
        });
        
        clearTimeout(this.saveTimer);
        this.saveTimer = setTimeout(() => {
            this.saveChanges();
        }, 500);
    }

    async saveChanges() {
        const changes = [...this.saveQueue];
        this.saveQueue = [];
        
        for (const change of changes) {
            await this.executeSave(change);
        }
    }

    async executeSave(change) {
        try {
            if (change.type === 'UPDATE_FIELD_COMPONENT') {
                await this.api.updateFieldComponent(change.componentId, {
                    [change.field]: change.value
                });
                console.log('已自动保存：', change.field);
            }
        } catch (error) {
            console.error('自动保存失败：', error);
        }
    }
}

window.AutoSave = AutoSave;
```

---

## Task 7: 创建预览验证界面 HTML

**Files:**
- Create: `src/main/resources/static/template-preview.html`

### Step 1: 创建预览界面 HTML

- [ ] **创建 template-preview.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>模板预览 - 验证界面</title>
    <link rel="stylesheet" href="/css/template-preview.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/frontend-design@latest/dist/css/frontend-design.min.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>模板预览（验证界面）</h1>
            <button id="btn-back" class="btn btn-secondary">返回配置</button>
        </div>
        
        <div class="preview-area">
            <div id="preview-content"></div>
        </div>
    </div>

    <script src="/js/template-preview-api.js"></script>
    <script src="/js/template-render.js"></script>
</body>
</html>
```

---

## Task 8: 创建预览界面样式

**Files:**
- Create: `src/main/resources/static/css/template-preview.css`

### Step 1: 创建预览样式

- [ ] **创建 template-preview.css**

```css
/* 预览界面样式 */
.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.preview-area {
    background: white;
    padding: 20px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.preview-card {
    background: #f9f9f9;
    border: 1px solid #e0e0e0;
    padding: 15px;
    margin-bottom: 15px;
    border-radius: 4px;
}

.preview-card h3 {
    font-size: 18px;
    margin-bottom: 15px;
}

.preview-field {
    margin-bottom: 15px;
}

.preview-field label {
    display: block;
    margin-bottom: 5px;
    font-weight: 600;
}

.preview-field input,
.preview-field select {
    width: 100%;
    padding: 8px;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
}

.preview-button {
    background: #4CAF50;
    color: white;
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}
```

---

## Task 9: 创建预览渲染逻辑

**Files:**
- Create: `src/main/resources/static/js/template-render.js`

### Step 1: 创建预览渲染逻辑

- [ ] **创建 template-render.js**

```javascript
// template-render.js
class TemplateRender {
    constructor() {
        this.api = new TemplatePreviewApi();
        this.init();
    }

    async init() {
        const schema = await this.api.getSchema();
        this.render(schema);
    }

    render(schema) {
        const content = document.getElementById('preview-content');
        content.innerHTML = '';
        
        if (!schema.layoutNodes || schema.layoutNodes.length === 0) {
            content.innerHTML = '<p style="text-align: center; color: #999;">暂无配置</p>';
            return;
        }
        
        schema.layoutNodes.forEach(node => {
            if (node.nodeType === 'CARD') {
                this.renderCard(node, content);
            }
        });
        
        // 渲染按钮
        if (schema.actions && schema.actions.length > 0) {
            schema.actions.forEach(action => {
                const buttonHtml = `<button class="preview-button">${action.displayName}</button>`;
                content.insertAdjacentHTML('beforeend', buttonHtml);
            });
        }
    }

    renderCard(node, container) {
        const cardHtml = `
            <div class="preview-card">
                <h3>${node.nodeName}</h3>
                <div class="card-fields-preview"></div>
            </div>
        `;
        
        container.insertAdjacentHTML('beforeend', cardHtml);
        
        const cardElement = container.lastElementChild;
        const fieldsDiv = cardElement.querySelector('.card-fields-preview');
        
        if (node.children && node.children.length > 0) {
            node.children.forEach(child => {
                if (child.nodeType === 'FIELD' && child.fieldComponent) {
                    this.renderField(child, fieldsDiv);
                }
            });
        }
    }

    renderField(node, container) {
        const fieldHtml = `
            <div class="preview-field">
                <label>${node.fieldComponent.labelName}${node.fieldDef.requiredDefault === 1 ? ' *' : ''}</label>
                ${this.renderInput(node.fieldComponent)}
            </div>
        `;
        
        container.insertAdjacentHTML('beforeend', fieldHtml);
    }

    renderInput(component) {
        switch (component.componentType) {
            case 'INPUT':
                return `<input type="text" placeholder="${component.placeholder || ''}">`;
            case 'NUMBER':
                return `<input type="number" placeholder="${component.placeholder || ''}">`;
            case 'DATE':
                return `<input type="date">`;
            case 'MONEY':
                return `<input type="number" step="0.01" placeholder="${component.placeholder || ''}">`;
            case 'SELECT':
                if (component.dataSourceType === 'STATIC' && component.componentProps) {
                    const options = component.componentProps.options || [];
                    const optionsHtml = options.map(opt => 
                        `<option value="${opt.value}">${opt.label}</option>`
                    ).join('');
                    return `<select><option value="">请选择</option>${optionsHtml}</select>`;
                }
                return `<select><option value="">请选择</option></select>`;
            default:
                return `<input type="text">`;
        }
    }
}

window.TemplateRender = TemplateRender;

// 初始化
const templateRender = new TemplateRender();
```

---

## Task 10: 创建预览 API 封装

**Files:**
- Create: `src/main/resources/static/js/template-preview-api.js`

### Step 1: 创建预览 API 封装

- [ ] **创建 template-preview-api.js**

```javascript
// template-preview-api.js
class TemplatePreviewApi {
    constructor(templateId, versionId) {
        this.templateId = templateId || 100;
        this.versionId = versionId || 200;
    }

    async getSchema() {
        const response = await fetch(`/api/templates/${this.templateId}/versions/${this.versionId}/schema`);
        const result = await response.json();
        return result.data;
    }
}

window.TemplatePreviewApi = TemplatePreviewApi;
```

---

## Task 11: 使用 chrome-devtools 验证完整流程

**验证步骤**：

### Step 1: 启动 Spring Boot 应用

- [ ] **启动应用**

Run: `cd /home/wula/IdeaProjects/dymic && mvn spring-boot:run`

Expected: 应用启动成功

### Step 2: 打开配置界面

- [ ] **打开模板配置界面**

Run: 使用 chrome-devtools 导航到 `http://localhost:8888/template-config.html`

Expected: 界面正常显示，左侧组件库，右侧空白画布

### Step 3: 测试添加卡片

- [ ] **点击"卡片容器"按钮**

Expected: 弹出输入框

- [ ] **输入"基本信息"，点击确认**

Expected: 卡片添加到画布，后端保存成功

### Step 4: 测试添加字段

- [ ] **点击"文本输入框"按钮**

Expected: 弹出多个输入框

- [ ] **填写：显示名称="合同名称"，必填=是，提示文字="请输入合同名称"**

Expected: 字段添加到卡片内，后端保存成功

### Step 5: 测试添加下拉框（静态选项）

- [ ] **点击"下拉选择框"按钮**

- [ ] **填写：显示名称="币种"，数据来源="STATIC"，选项="CNY:人民币，USD:美元"**

Expected: 下拉框添加到卡片内，后端保存成功

### Step 6: 测试添加保存按钮

- [ ] **点击"保存按钮"按钮**

- [ ] **填写：按钮文字="保存"，确认提示="是否保存？"**

Expected: 保存按钮添加到画布底部

### Step 7: 打开预览界面验证

- [ ] **点击"预览"按钮或直接导航到 template-preview.html**

Expected: 预览界面显示动态渲染的表单，包含卡片、字段、下拉框、按钮

- [ ] **验证下拉框选项是否正确**

Expected: 币种下拉框显示"人民币"、"美元"选项

### Step 8: 截图记录

- [ ] **截图记录配置界面和预览界面**

Expected: 截图保存，用于验证记录

---

## Task 12: 提交代码

### Step 1: 提交前端验证界面文件

- [ ] **提交模板配置界面和预览验证界面**

Run: `cd /home/wula/IdeaProjects/dymic && git add src/main/resources/static/template-config.html src/main/resources/static/template-preview.html src/main/resources/static/css/template-config.css src/main/resources/static/css/template-preview.css src/main/resources/static/js/template-config-api.js src/main/resources/static/js/component-library.js src/main/resources/static/js/config-canvas.js src/main/resources/static/js/auto-save.js src/main/resources/static/js/template-preview-api.js src/main/resources/static/js/template-render.js`

Run: `cd /home/wula/IdeaProjects/dymic && git commit -m "feat: implement template configuration and preview validation UI for development testing"`

---

## Summary

This plan implements template configuration and preview validation UI for development testing:

**Implemented Features**:
- Template configuration UI (simplified version)
- Component library (basic components, layout components, buttons)
- Configuration canvas (from top to bottom stacking)
- Property panel (business attribute configuration)
- Auto-save mechanism (500ms delay)
- Preview validation UI (dynamic rendering)
- API integration (create layout node, create field definition, create action)

**Technology**:
- Native HTML5/CSS3/JavaScript ES6+
- frontend-design CSS library
- Fetch API for backend communication
- chrome-devtools for UI validation

**Testing**:
- Manual testing with chrome-devtools
- Validate component addition flow
- Verify auto-save functionality
- Verify preview rendering correctness

**Note**: This UI is for development validation only, not for delivery to business users. The interface is simplified and focuses on verifying backend API functionality.