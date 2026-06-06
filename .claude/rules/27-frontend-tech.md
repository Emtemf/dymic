---
name: frontend-tech
description: 前端技术选择
---

# 前端技术选择

## 核心原则

**前端技术选择**：优先原生技术，轻量框架可选，零构建复杂度，最大化灵活性。

---

## 技术选择总览

| 层级 | 技术选择 | 说明 |
|-----|---------|------|
| 基础层 | HTML/CSS/JS | 原生技术，零依赖 |
| UI层 | Bootstrap 5.3 | 无需构建，直接使用 |
| 交互层 | Alpine.js（可选） | 轕量级，学习成本低 |
| HTTP层 | Axios | 主流HTTP客户端 |
| 工具层 | Lodash/Day.js | 工具库（可选） |

---

## 基础技术

### HTML/CSS/JS
```
优势：
- 零学习成本，团队熟悉
- 零构建复杂度，直接使用
- 最大灵活性，完全控制
- 无依赖风险，长期稳定

适用场景：
- 页面结构定义
- 样式控制
- 基本交互
- 动态渲染
```

### 代码示例
```html
<!-- 原生HTML -->
<!DOCTYPE html>
<html>
<head>
    <title>模板配置</title>
    <link href="/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container">
        <form id="templateForm">
            <div class="mb-3">
                <label for="templateCode">模板编码</label>
                <input type="text" id="templateCode" class="form-control" required>
            </div>
            <div class="mb-3">
                <label for="templateName">模板名称</label>
                <input type="text" id="templateName" class="form-control" required>
            </div>
            <button type="submit" class="btn btn-primary">保存</button>
        </form>
    </div>
    
    <script src="/js/axios.min.js"></script>
    <script src="/js/app.js"></script>
</body>
</html>

<!-- 原生JS -->
<script>
// app.js
document.getElementById('templateForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const templateCode = document.getElementById('templateCode').value;
    const templateName = document.getElementById('templateName').value;
    
    try {
        const response = await axios.post('/api/templates', {
            templateCode: templateCode,
            templateName: templateName
        });
        
        if (response.data.success) {
            alert('保存成功');
        }
    } catch (error) {
        alert('保存失败：' + error.message);
    }
});
</script>
```

---

## UI框架

### Bootstrap 5.3
```
优势：
- 无需构建，直接引入CDN或本地文件
- 响应式设计，适配各种设备
- 组件丰富，快速开发
- 样式统一，美观大方

引入方式：
<!-- CDN引入 -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- 本地引入（推荐生产环境） -->
<link href="/css/bootstrap.min.css" rel="stylesheet">
<script src="/js/bootstrap.bundle.min.js"></script>
```

### 常用组件
```html
<!-- 表单组件 -->
<div class="mb-3">
    <label for="field" class="form-label">字段名称</label>
    <input type="text" id="field" class="form-control">
</div>

<!-- 下拉框组件 -->
<div class="mb-3">
    <label for="select" class="form-label">选择类型</label>
    <select id="select" class="form-select">
        <option value="text">文本</option>
        <option value="number">数字</option>
        <option value="date">日期</option>
    </select>
</div>

<!-- 按钮组件 -->
<button type="button" class="btn btn-primary">保存</button>
<button type="button" class="btn btn-secondary">取消</button>
<button type="button" class="btn btn-danger">删除</button>

<!-- 表格组件 -->
<table class="table table-striped table-hover">
    <thead>
        <tr>
            <th>编码</th>
            <th>名称</th>
            <th>状态</th>
            <th>操作</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>TPL001</td>
            <td>销售合同</td>
            <td>已发布</td>
            <td>
                <button class="btn btn-sm btn-primary">编辑</button>
                <button class="btn btn-sm btn-danger">删除</button>
            </td>
        </tr>
    </tbody>
</table>
```

---

## 交互框架（可选）

### Alpine.js 3.x
```
优势：
- 轻量级（约15KB），无构建需求
- 类似Vue语法，学习成本低
- 直接在HTML中使用，无需组件化
- 适合简单交互场景

引入方式：
<!-- CDN引入 -->
<script defer src="https://cdn.jsdelivr.net/npm/alpinejs@3.x.x/dist/cdn.min.js"></script>

<!-- 本地引入 -->
<script defer src="/js/alpine.min.js"></script>
```

### Alpine.js示例
```html
<!-- 模板配置页面 -->
<div x-data="templateConfig()">
    <form @submit.prevent="saveTemplate">
        <div class="mb-3">
            <label class="form-label">模板编码</label>
            <input type="text" x-model="template.templateCode" class="form-control" required>
        </div>
        
        <div class="mb-3">
            <label class="form-label">模板名称</label>
            <input type="text" x-model="template.templateName" class="form-control" required>
        </div>
        
        <!-- 动态渲染配置 -->
        <div class="mb-3">
            <label class="form-label">字段配置</label>
            <template x-for="field in template.fields" :key="field.path">
                <div class="row mb-2">
                    <input type="text" x-model="field.path" class="col-4 form-control">
                    <select x-model="field.type" class="col-4 form-select">
                        <option value="text">文本</option>
                        <option value="number">数字</option>
                    </select>
                    <button @click="removeField(field)" class="col-2 btn btn-danger">删除</button>
                </div>
            </template>
            <button @click="addField()" class="btn btn-success">添加字段</button>
        </div>
        
        <button type="submit" class="btn btn-primary">保存</button>
    </form>
    
    <!-- 提示消息 -->
    <div x-show="message.show" x-transition class="alert" :class="message.type">
        <span x-text="message.text"></span>
    </div>
</div>

<script>
function templateConfig() {
    return {
        template: {
            templateCode: '',
            templateName: '',
            fields: []
        },
        message: {
            show: false,
            text: '',
            type: 'alert-success'
        },
        
        addField() {
            this.template.fields.push({ path: '', type: 'text' });
        },
        
        removeField(field) {
            this.template.fields = this.template.fields.filter(f => f !== field);
        },
        
        async saveTemplate() {
            try {
                const response = await axios.post('/api/templates', this.template);
                if (response.data.success) {
                    this.showMessage('保存成功', 'alert-success');
                }
            } catch (error) {
                this.showMessage('保存失败：' + error.message, 'alert-danger');
            }
        },
        
        showMessage(text, type) {
            this.message = { show: true, text: text, type: type };
            setTimeout(() => this.message.show = false, 3000);
        }
    };
}
</script>
```

---

## HTTP客户端

### Axios
```
优势：
- 主流HTTP客户端，文档完善
- 支持拦截器，统一处理请求/响应
- 支持请求取消
- Promise支持，易于异步处理

引入方式：
<!-- CDN引入 -->
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>

<!-- 本地引入 -->
<script src="/js/axios.min.js"></script>
```

### Axios配置
```javascript
// axios配置
axios.defaults.baseURL = '/api';
axios.defaults.timeout = 10000;

// 请求拦截器
axios.interceptors.request.use(config => {
    // 添加认证token
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = 'Bearer ' + token;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

// 响应拦截器
axios.interceptors.response.use(response => {
    // 统一处理响应
    if (response.data.success) {
        return response.data;
    } else {
        // 业务错误
        alert(response.data.message);
        return Promise.reject(new Error(response.data.message));
    }
}, error => {
    // HTTP错误
    if (error.response) {
        switch (error.response.status) {
            case 401:
                alert('未授权，请登录');
                break;
            case 403:
                alert('禁止访问');
                break;
            case 404:
                alert('资源不存在');
                break;
            case 500:
                alert('服务器错误');
                break;
        }
    }
    return Promise.reject(error);
});
```

---

## 工具库（可选）

### Lodash
```
用途：
- 数组/对象操作
- 函数工具
- 字符串处理

引入方式：
<script src="https://cdn.jsdelivr.net/npm/lodash@4.x/lodash.min.js"></script>

使用示例：
// 深拷贝
const clone = _.cloneDeep(template);

// 数组去重
const unique = _.uniqBy(fields, 'path');

// 对象合并
const merged = _.merge(baseConfig, customConfig);
```

### Day.js
```
用途：
- 日期解析
- 日期格式化
- 日期计算

引入方式：
<script src="https://cdn.jsdelivr.net/npm/dayjs@1.x/dayjs.min.js"></script>

使用示例：
// 格式化日期
const formatted = dayjs(createTime).format('YYYY-MM-DD HH:mm:ss');

// 日期计算
const tomorrow = dayjs().add(1, 'day');

// 日期比较
const isBefore = dayjs(date1).isBefore(date2);
```

---

## 技术选择决策

### V1.0推荐选择
```
基础：原生HTML/CSS/JS（必须）
UI：Bootstrap 5.3（必须）
交互：Alpine.js（可选，简单交互使用）
HTTP：Axios（必须）
工具：Lodash/Day.js（可选）
```

### 选择原则
```
原则1：原生优先
- 优先使用原生技术
- 减少依赖和学习成本

原则2：零构建优先
- 不使用webpack/vite等构建工具
- 直接引入CDN或本地文件

原则3：轻量优先
- 选择轻量级框架
- Alpine.js而非Vue/React

原则4：渐进增强
- 从简单开始，逐步引入
- 需要时才添加复杂功能
```

---

## 验证清单

### 前端技术选择必须验证
- [ ] 基础技术可用
- [ ] UI框架引入正确
- [ ] 交互框架（可选）正确
- [ ] HTTP客户端配置正确
- [ ] 无构建复杂度
- [ ] 页面渲染正确

---

## 详细规则引用

- 技术栈规范：查看 `23-tech-stack.md`
- 前端界面验证：查看 `16-testing-frontend.md`
- chrome-devtools使用：查看 `17-testing-chrome-devtools.md`