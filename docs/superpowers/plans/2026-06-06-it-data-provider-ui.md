# IT 数据提供方配置界面实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现IT数据提供方配置界面，用于IT配置人员配置数据提供方（HTTP接口、平台集成、内部查询等），验证数据提供方 API 功能正确性。

**Architecture:** 简单表单式配置界面，前端使用原生 HTML5/CSS3/JavaScript ES6+，无框架依赖。界面用于开发验证，不是交付给业务用户的正式系统。

**Tech Stack:** HTML5, CSS3 (frontend-design), JavaScript ES6+, Fetch API, chrome-devtools (验证工具)

---

## File Structure

**新增文件**：

```
前端界面文件结构：
├── src/main/resources/static/
│   ├── data-provider.html              # IT 数据提供方配置界面
│   ├── css/
│   │   └ data-provider.css              # 数据提供方配置界面样式
│   └── js/
│   │   ├── data-provider-api.js         # 数据提供方 API 调用封装
│   │   └ data-provider-form.js          # 表单逻辑
│   │   └ data-provider-list.js          # 列表展示逻辑
│   │   └ data-provider-dialog.js        # 对话框逻辑
```

---

## Task 1: 创建数据提供方配置界面 HTML

**Files:**
- Create: `src/main/resources/static/data-provider.html`

### Step 1: 创建基础 HTML 结构

- [ ] **创建 data-provider.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>数据提供方配置 - IT 管理界面</title>
    <link rel="stylesheet" href="/css/data-provider.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/frontend-design@latest/dist/css/frontend-design.min.css">
</head>
<body>
    <div class="container">
        <!-- 顶部标题栏 -->
        <div class="header">
            <h1>数据提供方配置</h1>
            <div class="header-actions">
                <button id="btn-add" class="btn btn-primary">新增数据提供方</button>
            </div>
        </div>

        <!-- 数据提供方列表 -->
        <div class="list-section">
            <table id="data-provider-table" class="table">
                <thead>
                    <tr>
                        <th width="150">编码</th>
                        <th width="200">名称</th>
                        <th width="100">类型</th>
                        <th width="80">状态</th>
                        <th width="150">操作</th>
                    </tr>
                </thead>
                <tbody id="data-provider-list">
                    <!-- 动态填充 -->
                </tbody>
            </table>
        </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <div id="dialog-overlay" class="dialog-overlay" style="display: none;">
        <div class="dialog">
            <div class="dialog-header">
                <h3 id="dialog-title">新增数据提供方</h3>
                <button id="btn-close-dialog" class="btn-close">×</button>
            </div>
            <div class="dialog-body">
                <form id="data-provider-form">
                    <div class="form-group">
                        <label for="provider-code">编码 *</label>
                        <input type="text" id="provider-code" name="providerCode" required>
                    </div>
                    <div class="form-group">
                        <label for="provider-name">名称 *</label>
                        <input type="text" id="provider-name" name="providerName" required>
                    </div>
                    <div class="form-group">
                        <label for="provider-type">类型 *</label>
                        <select id="provider-type" name="providerType" required>
                            <option value="STATIC">静态选项</option>
                            <option value="DICT">字典</option>
                            <option value="HTTP">HTTP接口</option>
                            <option value="PLATFORM">平台集成</option>
                            <option value="INTERNAL">内部查询</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="config-json">配置JSON</label>
                        <textarea id="config-json" name="configJson" rows="10" placeholder="请输入 JSON 配置"></textarea>
                    </div>
                </form>
            </div>
            <div class="dialog-footer">
                <button id="btn-save" class="btn btn-primary">保存</button>
                <button id="btn-cancel" class="btn btn-secondary">取消</button>
            </div>
        </div>
    </div>

    <script src="/js/data-provider-api.js"></script>
    <script src="/js/data-provider-form.js"></script>
    <script src="/js/data-provider-list.js"></script>
    <script src="/js/data-provider-dialog.js"></script>
</body>
</html>
```

---

## Task 2: 创建样式文件

**Files:**
- Create: `src/main/resources/static/css/data-provider.css`

### Step 1: 创建基础样式

- [ ] **创建 data-provider.css**

```css
/* 基础容器样式 */
.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

/* 顶部标题栏 */
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #e0e0e0;
}

.header h1 {
    font-size: 24px;
    color: #333;
}

.header-actions {
    display: flex;
    gap: 10px;
}

/* 列表区域 */
.list-section {
    margin-top: 20px;
}

.table {
    width: 100%;
    border-collapse: collapse;
    background: white;
    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.table th, .table td {
    padding: 12px 15px;
    text-align: left;
    border-bottom: 1px solid #e0e0e0;
}

.table th {
    background: #f5f5f5;
    font-weight: 600;
    color: #666;
}

.table td {
    color: #333;
}

.table tbody tr:hover {
    background: #f9f9f9;
}

/* 按钮 */
.btn {
    padding: 8px 16px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    transition: background 0.2s;
}

.btn-primary {
    background: #4CAF50;
    color: white;
}

.btn-primary:hover {
    background: #45a049;
}

.btn-secondary {
    background: #f0f0f0;
    color: #666;
}

.btn-secondary:hover {
    background: #e0e0e0;
}

.btn-danger {
    background: #f44336;
    color: white;
}

.btn-danger:hover {
    background: #da190b;
}

.btn-close {
    background: transparent;
    border: none;
    font-size: 20px;
    color: #999;
    cursor: pointer;
    padding: 0;
    width: 30px;
    height: 30px;
}

/* 对话框 */
.dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.dialog {
    background: white;
    width: 600px;
    max-height: 80vh;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    overflow: auto;
}

.dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #e0e0e0;
}

.dialog-header h3 {
    font-size: 18px;
    color: #333;
}

.dialog-body {
    padding: 20px;
}

.dialog-footer {
    padding: 15px 20px;
    border-top: 1px solid #e0e0e0;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

/* 表单 */
.form-group {
    margin-bottom: 15px;
}

.form-group label {
    display: block;
    margin-bottom: 5px;
    color: #666;
    font-size: 14px;
}

.form-group input,
.form-group select,
.form-group textarea {
    width: 100%;
    padding: 8px 10px;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    font-size: 14px;
}

.form-group textarea {
    resize: vertical;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
    border-color: #4CAF50;
    outline: none;
}
```

---

## Task 3: 创建 API 调用封装

**Files:**
- Create: `src/main/resources/static/js/data-provider-api.js`

### Step 1: 创建 API 调用封装

- [ ] **创建 data-provider-api.js**

```javascript
// data-provider-api.js
class DataProviderApi {
    constructor() {
        this.baseUrl = '/api/data-providers';
    }

    // 创建数据提供方
    async create(data) {
        const response = await fetch(this.baseUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) {
            throw new Error('创建失败');
        }
        
        const result = await response.json();
        if (!result.success) {
            throw new Error(result.message || '创建失败');
        }
        
        return result.data;
    }

    // 获取列表
    async list() {
        const response = await fetch(this.baseUrl, {
            method: 'GET'
        });
        
        if (!response.ok) {
            throw new Error('获取列表失败');
        }
        
        const result = await response.json();
        return result.data || [];
    }

    // 获取单个
    async getById(id) {
        const response = await fetch(`${this.baseUrl}/${id}`, {
            method: 'GET'
        });
        
        if (!response.ok) {
            throw new Error('获取详情失败');
        }
        
        const result = await response.json();
        return result.data;
    }

    // 更新
    async update(id, data) {
        const response = await fetch(`${this.baseUrl}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        if (!response.ok) {
            throw new Error('更新失败');
        }
        
        const result = await response.json();
        if (!result.success) {
            throw new Error(result.message || '更新失败');
        }
        
        return result.data;
    }

    // 删除（暂时不支持，后续阶段）
    async delete(id) {
        console.warn('删除功能暂未实现');
    }
}

// 导出
window.DataProviderApi = DataProviderApi;
```

---

## Task 4: 创建列表展示逻辑

**Files:**
- Create: `src/main/resources/static/js/data-provider-list.js`

### Step 1: 创建列表展示逻辑

- [ ] **创建 data-provider-list.js**

```javascript
// data-provider-list.js
class DataProviderList {
    constructor() {
        this.api = new DataProviderApi();
        this.data = [];
    }

    // 初始化列表
    async init() {
        await this.loadList();
        this.render();
    }

    // 加载列表数据
    async loadList() {
        try {
            this.data = await this.api.list();
        } catch (error) {
            console.error('加载列表失败:', error);
            alert('加载列表失败：' + error.message);
        }
    }

    // 渲染列表
    render() {
        const tbody = document.getElementById('data-provider-list');
        tbody.innerHTML = '';

        if (this.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: #999;">暂无数据</td></tr>';
            return;
        }

        this.data.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${item.providerCode}</td>
                <td>${item.providerName}</td>
                <td>${this.getTypeLabel(item.providerType)}</td>
                <td>${item.status === 'ENABLED' ? '启用' : '停用'}</td>
                <td>
                    <button class="btn btn-secondary btn-sm" onclick="dataProviderDialog.edit(${item.id})">编辑</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    // 获取类型标签
    getTypeLabel(type) {
        const labels = {
            'STATIC': '静态选项',
            'DICT': '字典',
            'HTTP': 'HTTP接口',
            'PLATFORM': '平台集成',
            'INTERNAL': '内部查询'
        };
        return labels[type] || type;
    }

    // 刷新列表
    async refresh() {
        await this.loadList();
        this.render();
    }
}

// 导出
window.DataProviderList = DataProviderList;

// 初始化
const dataProviderList = new DataProviderList();
dataProviderList.init();
```

---

## Task 5: 创建对话框逻辑

**Files:**
- Create: `src/main/resources/static/js/data-provider-dialog.js`

### Step 1: 创建对话框逻辑

- [ ] **创建 data-provider-dialog.js**

```javascript
// data-provider-dialog.js
class DataProviderDialog {
    constructor() {
        this.api = new DataProviderApi();
        this.currentId = null;
        this.overlay = document.getElementById('dialog-overlay');
        this.form = document.getElementById('data-provider-form');
        
        this.bindEvents();
    }

    // 绑定事件
    bindEvents() {
        // 新增按钮
        document.getElementById('btn-add').addEventListener('click', () => {
            this.show();
        });

        // 关闭按钮
        document.getElementById('btn-close-dialog').addEventListener('click', () => {
            this.hide();
        });

        // 取消按钮
        document.getElementById('btn-cancel').addEventListener('click', () => {
            this.hide();
        });

        // 保存按钮
        document.getElementById('btn-save').addEventListener('click', () => {
            this.save();
        });

        // 点击遮罩层关闭
        this.overlay.addEventListener('click', (e) => {
            if (e.target === this.overlay) {
                this.hide();
            }
        });
    }

    // 显示对话框（新增）
    show() {
        this.currentId = null;
        document.getElementById('dialog-title').textContent = '新增数据提供方';
        this.form.reset();
        this.overlay.style.display = 'flex';
    }

    // 显示对话框（编辑）
    async edit(id) {
        this.currentId = id;
        document.getElementById('dialog-title').textContent = '编辑数据提供方';
        
        try {
            const data = await this.api.getById(id);
            document.getElementById('provider-code').value = data.providerCode;
            document.getElementById('provider-name').value = data.providerName;
            document.getElementById('provider-type').value = data.providerType;
            
            if (data.configJson) {
                document.getElementById('config-json').value = JSON.stringify(data.configJson, null, 2);
            }
            
            this.overlay.style.display = 'flex';
        } catch (error) {
            console.error('获取详情失败:', error);
            alert('获取详情失败：' + error.message);
        }
    }

    // 隐藏对话框
    hide() {
        this.overlay.style.display = 'none';
        this.form.reset();
    }

    // 保存
    async save() {
        const formData = {
            providerCode: document.getElementById('provider-code').value.trim(),
            providerName: document.getElementById('provider-name').value.trim(),
            providerType: document.getElementById('provider-type').value
        };

        // 解析 JSON 配置
        const configJsonText = document.getElementById('config-json').value.trim();
        if (configJsonText) {
            try {
                formData.configJson = JSON.parse(configJsonText);
            } catch (error) {
                alert('JSON 格式错误，请检查配置');
                return;
            }
        }

        try {
            if (this.currentId) {
                // 编辑
                await this.api.update(this.currentId, formData);
                alert('更新成功');
            } else {
                // 新增
                await this.api.create(formData);
                alert('创建成功');
            }
            
            this.hide();
            dataProviderList.refresh();
        } catch (error) {
            console.error('保存失败:', error);
            alert('保存失败：' + error.message);
        }
    }
}

// 导出
window.DataProviderDialog = DataProviderDialog;

// 初始化
const dataProviderDialog = new DataProviderDialog();
```

---

## Task 6: 创建表单逻辑（可选，已在对话框中实现）

**说明：表单逻辑已在 Task 5 的 data-provider-dialog.js 中实现，此任务可跳过。**

---

## Task 7: 使用 chrome-devtools 验证界面

**验证步骤**：

### Step 1: 启动 Spring Boot 应用

- [ ] **启动应用**

Run: `cd /home/wula/IdeaProjects/dymic && mvn spring-boot:run`

Expected: 应用启动成功，监听 8888 端口

### Step 2: 使用 chrome-devtools 打开界面

- [ ] **打开数据提供方配置界面**

Run: 使用 chrome-devtools 工具导航到 `http://localhost:8888/data-provider.html`

Expected: 界面正常显示，列表为空或显示已有数据提供方

### Step 3: 测试新增功能

- [ ] **点击"新增数据提供方"按钮**

Run: 使用 chrome-devtools 点击按钮

Expected: 对话框弹出，显示表单

- [ ] **填写表单数据**

Run: 使用 chrome-devtools 填充表单字段：
- 编码：SUPPLIER_LIST
- 名称：供应商列表
- 类型：HTTP
- 配置JSON：{"url": "http://api.example.com/suppliers"}

- [ ] **点击"保存"按钮**

Run: 使用 chrome-devtools 点击保存按钮

Expected: 提示"创建成功"，对话框关闭，列表刷新显示新数据

### Step 4: 测试编辑功能

- [ ] **点击列表中的"编辑"按钮**

Run: 使用 chrome-devtools 点击编辑按钮

Expected: 对话框弹出，表单填充已有数据

- [ ] **修改名称并保存**

Run: 使用 chrome-devtools 修改名称字段，点击保存

Expected: 提示"更新成功"，列表刷新显示更新后的名称

### Step 5: 截图验证

- [ ] **截图记录验证结果**

Run: 使用 chrome-devtools 截图

Expected: 截图保存，用于后续验证记录

---

## Task 8: 提交代码

### Step 1: 提交前端界面文件

- [ ] **提交 IT 数据提供方配置界面**

Run: `cd /home/wula/IdeaProjects/dymic && git add src/main/resources/static/data-provider.html src/main/resources/static/css/data-provider.css src/main/resources/static/js/data-provider-api.js src/main/resources/static/js/data-provider-list.js src/main/resources/static/js/data-provider-dialog.js`

Run: `cd /home/wula/IdeaProjects/dymic && git commit -m "feat: implement IT data provider configuration UI for development validation"`

---

## Summary

This plan implements IT data provider configuration UI for development validation:

**Implemented Features**:
- Data provider list display
- Add/Edit data provider dialog
- API integration (create, list, update)
- Form validation
- JSON configuration editor

**Technology**:
- Native HTML5/CSS3/JavaScript ES6+
- frontend-design CSS library
- Fetch API for backend communication
- chrome-devtools for UI validation

**Testing**:
- Manual testing with chrome-devtools
- Validate create/update operations
- Verify UI rendering and interaction

**Note**: This UI is for development validation only, not for delivery to business users.