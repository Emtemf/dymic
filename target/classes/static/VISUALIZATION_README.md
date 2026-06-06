# 可视化配置系统

一个完整的可视化配置界面系统，支持通过拖拽方式设计合同模板，并动态渲染展示界面。

## 系统组成

系统分为三个独立界面：

### 1. 模板配置界面（可视化设计器）

**文件位置**：`config/template-designer.html`

**功能**：
- 左侧预览区（40%）- 实时渲染配置效果
- 右侧配置面板（60%）- 包含组件库、属性配置、规则配置、数据源配置
- 拖拽组件到预览区进行布局设计
- 点击预览区组件，右侧显示详细配置
- 实时预览配置效果
- 保存配置到数据库
- 导入/导出配置（JSON格式）
- 发布模板版本

**组件库包含**：
- **布局组件**：页面、卡片、栅格、Tab页签、折叠面板
- **基础组件**：输入框、下拉框、日期、数字、金额、多行文本
- **业务组件**：供应商选择、明细表、附件上传、图片上传
- **动作组件**：按钮、查询弹窗、保存按钮、提交按钮

**配置功能**：
- **基本属性**：名称、编码、描述
- **样式配置**：宽度、高度、栅格列数、偏移量
- **数据绑定**：字段路径、数据类型
- **规则配置**：可见性规则、必填规则、只读规则
- **组件特有配置**：下拉框数据源、明细表列配置、附件上传限制等

**使用方法**：
```bash
# 打开模板设计器
http://localhost:8080/config/template-designer.html
```

### 2. 数据源配置界面

**文件位置**：`config/data-source.html`

**功能**：
- 创建和管理数据提供方
- 支持5种数据源类型：
  - **静态数据**：手动配置JSON数据
  - **字典数据**：从系统字典加载
  - **HTTP接口**：调用外部API获取数据
  - **平台服务**：调用内部平台服务
  - **内部服务**：调用业务服务
- 测试数据源连接
- 启用/禁用数据源
- 删除数据源

**使用方法**：
```bash
# 打开数据源配置界面
http://localhost:8080/config/data-source.html
```

### 3. 合同展示界面（动态渲染）

**文件位置**：`display/dynamic-display.html`

**功能**：
- 根据模板配置动态渲染界面
- 执行规则引擎（显隐、必填、只读）
- 双向数据绑定
- 数据录入和保存
- 提交审批流程

**核心引擎**：
- `dynamic-render.js` - 动态渲染引擎
- `rule-engine.js` - 规则引擎
- `data-binding.js` - 数据绑定系统

**使用方法**：
```bash
# 打开动态展示界面
http://localhost:8080/display/dynamic-display.html
```

## 核心技术

### 1. 动态渲染引擎

**文件**：`display/js/dynamic-render.js`

**功能**：
- 加载模板配置
- 递归渲染组件树
- 处理布局嵌套（Page/Card/Grid/Tab/Collapse）
- 渲染字段组件（Input/Select/Date/Number/Money/Textarea）
- 渲染业务组件（SupplierSelect/DetailTable/Attachment/Image）
- 渲染动作组件（Button/SaveButton/SubmitButton）

**示例**：
```javascript
// 创建渲染器实例
const renderer = new DynamicRenderer('template_001', 'latest');

// 渲染到容器
await renderer.render('containerId');
```

### 2. 规则引擎

**文件**：`display/js/rule-engine.js`

**功能**：
- 执行动态显隐规则
- 执行动态必填规则
- 执行动态只读规则
- 安全的表达式执行环境
- 规则验证和测试

**支持的规则类型**：
- `visibility` - 可见性规则
- `required` - 必填规则
- `readonly` - 只读规则
- `validation` - 验证规则

**示例**：
```javascript
// 创建规则引擎实例
const ruleEngine = new RuleEngine(rules);

// 设置上下文数据
ruleEngine.setContext(formData);

// 执行所有规则
ruleEngine.executeAll();

// 执行单个规则
ruleEngine.executeComponentRule('component_001', 'visibility');
```

### 3. 数据绑定系统

**文件**：`display/js/data-binding.js`

**功能**：
- 双向数据绑定
- 字段监听和通知
- 嵌套字段访问（如：`supplier.name`）
- 计算字段
- 字段验证

**示例**：
```javascript
// 创建数据绑定实例
const dataBinding = new DataBinding();

// 初始化数据
dataBinding.initData({
    contractNumber: 'PO-2024-001',
    supplier: {
        name: '北京科技有限公司'
    }
});

// 获取字段值
const value = dataBinding.getField('supplier.name');

// 设置字段值
dataBinding.setField('supplier.name', '上海贸易公司');

// 监听字段变化
dataBinding.watch('supplier.name', (newValue, oldValue) => {
    console.log(`字段值从 ${oldValue} 变为 ${newValue}`);
});

// 双向绑定组件
dataBinding.bindTwoWay('input_supplier_name', 'supplier.name');
```

### 4. 组件库定义

**文件**：`config/js/component-library.js`

**功能**：
- 定义所有可用组件类型
- 组件默认配置
- 组件分类管理
- 组件实例创建
- 组件配置验证

**示例**：
```javascript
// 获取组件定义
const def = ComponentLibrary.getComponentDef('INPUT');

// 创建组件实例
const inputComponent = ComponentLibrary.createComponent('INPUT');

// 按分类获取组件列表
const layoutComponents = ComponentLibrary.getComponentsByCategory('layout');

// 验证组件配置
const validation = ComponentLibrary.validateComponent(component);
```

### 5. 拖拽系统

**文件**：`config/js/drag-drop.js`

**功能**：
- 处理组件拖拽添加
- 处理组件排序
- 处理组件嵌套
- 组件移动（上移/下移）
- 组件复制
- 组件删除

**示例**：
```javascript
// 开始拖拽组件
handleDragStart(event);

// 处理拖拽经过
handleDragOver(event);

// 处理拖拽离开
handleDragLeave(event);

// 处理放下组件
handleDrop(event);

// 删除组件
deleteComponent(componentId);

// 复制组件
duplicateComponent(componentId);

// 移动组件
moveComponent(componentId, 'up');
```

### 6. 属性配置面板

**文件**：`config/js/property-panel.js`

**功能**：
- 显示组件属性配置表单
- 处理属性修改
- 实时更新预览
- 组件特有配置渲染

**示例**：
```javascript
// 选中组件并显示配置
selectComponent(componentId);

// 更新组件属性
updateProperty('name', '新的组件名称');

// 清空属性面板
clearPropertyPanel();
```

### 7. 配置API

**文件**：`config/js/config-api.js`

**功能**：
- 保存配置到后端
- 加载配置
- 导入配置（JSON文件）
- 导出配置（JSON文件）
- 验证配置
- 生成预览HTML
- 发布模板版本

**示例**：
```javascript
// 保存配置
await saveConfig();

// 加载配置
await loadConfig(templateId);

// 导出配置
exportConfig();

// 导入配置
importConfig();

// 预览模板
previewTemplate();

// 发布模板
await publishTemplate();
```

### 8. 设计器核心逻辑

**文件**：`config/js/designer.js`

**功能**：
- 管理设计器状态
- 处理事件绑定
- Tab切换管理
- 规则配置管理
- 数据源配置管理
- 缩放功能

**示例**：
```javascript
// 初始化设计器
initDesigner();

// 切换Tab
switchTab('properties');

// 删除选中组件
deleteSelectedComponent();

// 测试规则表达式
testExpression('visibility');

// 添加数据源
addDataSource();
```

## 文件结构

```
src/main/resources/static/
├── config/                          # 配置界面
│   ├── template-designer.html        # 模板可视化设计器
│   ├── data-source.html              # 数据源配置界面
│   ├── css/
│   │   └── designer.css              # 设计器样式
│   └── js/
│       ├── designer.js               # 设计器核心逻辑
│       ├── component-library.js      # 组件库定义
│       ├── drag-drop.js              # 拖拽功能
│       ├── property-panel.js         # 属性配置面板
│       ├── preview-renderer.js       # 预览渲染器
│       └── config-api.js             # 配置相关API
│
├── display/                         # 展示界面
│   ├── dynamic-display.html          # 动态渲染展示界面
│   ├── simple.html                   # 简单界面（固定展示）
│   ├── complex.html                  # 复杂界面（固定展示）
│   └── js/
│       ├── dynamic-render.js         # 动态渲染引擎
│       ├── rule-engine.js            # 规则引擎
│       └── data-binding.js           # 数据绑定
│
├── index.html                       # 首页
├── css/
│   └── style.css                    # 全局样式
└── js/
    ├── api.js                       # API封装
    └── contract-api.js               # 合同API
```

## 使用流程

### 1. 设计模板

1. 打开模板设计器：`config/template-designer.html`
2. 从右侧组件库拖拽组件到左侧预览区
3. 点击预览区的组件，右侧显示配置面板
4. 配置组件属性、数据绑定、规则等
5. 点击"保存"按钮保存配置
6. 点击"预览"按钮查看最终效果
7. 点击"发布"按钮发布模板版本

### 2. 配置数据源

1. 打开数据源配置界面：`config/data-source.html`
2. 点击"新建数据源"按钮
3. 选择数据源类型（静态、字典、HTTP等）
4. 配置数据源参数
5. 测试数据源连接
6. 保存数据源

### 3. 使用模板

1. 打开动态展示界面：`display/dynamic-display.html`
2. 选择已发布的模板
3. 系统自动根据配置渲染界面
4. 填写表单数据
5. 规则自动执行（显隐、必填、只读）
6. 点击保存或提交按钮

## 配置示例

### 组件配置示例

```json
{
  "id": "input_001",
  "type": "INPUT",
  "name": "合同编号",
  "code": "contract_number",
  "description": "合同编号输入框",
  "width": "100%",
  "span": 24,
  "offset": 0,
  "fieldPath": "contractNumber",
  "dataType": "string",
  "placeholder": "PO-2024-001",
  "required": true,
  "readonly": false,
  "maxLength": 200
}
```

### 规则配置示例

```json
{
  "id": "rule_001",
  "componentId": "textarea_001",
  "type": "visibility",
  "expression": "amount > 10000",
  "enabled": true,
  "description": "当金额大于10000时显示备注字段"
}
```

### 数据源配置示例

```json
{
  "id": "supplier_list",
  "name": "供应商列表",
  "type": "http",
  "config": {
    "url": "/api/suppliers/list",
    "method": "POST",
    "mapping": {
      "valueField": "id",
      "labelField": "name"
    }
  }
}
```

## 后端API接口

系统需要以下后端API接口：

### 1. 模板配置API

```
POST   /api/template/config          # 保存模板配置
GET    /api/template/config/{id}     # 加载模板配置
POST   /api/template/publish         # 发布模板版本
GET    /api/template/list            # 模板列表
DELETE /api/template/{id}            # 删除模板
```

### 2. 数据源API

```
POST   /api/datasource               # 创建数据源
GET    /api/datasource/{id}          # 获取数据源
GET    /api/datasource/list          # 数据源列表
PUT    /api/datasource/{id}          # 更新数据源
DELETE /api/datasource/{id}          # 删除数据源
POST   /api/datasource/test/{id}     # 测试数据源
```

### 3. 合同业务API

```
POST   /api/contract/save            # 保存合同
POST   /api/contract/submit          # 提交合同
GET    /api/contract/{id}            # 获取合同
GET    /api/contract/list            # 合同列表
DELETE /api/contract/{id}            # 删除合同
```

### 4. 供应商API

```
GET    /api/suppliers/search         # 搜索供应商
GET    /api/suppliers/{id}           # 获取供应商详情
```

## 扩展功能

### 1. 添加新组件

在 `component-library.js` 中添加新的组件定义：

```javascript
types: {
    // 新增自定义组件
    CUSTOM_COMPONENT: {
        name: '自定义组件',
        icon: 'fa-star',
        category: 'basic',
        isContainer: false,
        defaultConfig: {
            name: '自定义组件',
            code: 'custom_001',
            // ... 其他配置
        }
    }
}
```

在 `preview-renderer.js` 中添加渲染方法：

```javascript
function renderCustomComponent(component) {
    // 渲染逻辑
}
```

### 2. 添加新规则类型

在 `rule-engine.js` 中扩展规则类型：

```javascript
getRuleTypes() {
    return {
        visibility: '可见性规则',
        required: '必填规则',
        readonly: '只读规则',
        validation: '验证规则',
        // 新增规则类型
        custom_rule: '自定义规则'
    };
}

applyCustomRule(component, result) {
    // 应用自定义规则逻辑
}
```

### 3. 添加新数据源类型

在 `data-source.html` 中添加新的数据源类型选项。

## 技术栈

- **前端框架**：原生JavaScript（无框架依赖）
- **样式**：CSS3 + Flexbox + Grid
- **图标**：Font Awesome 6.4
- **数据格式**：JSON
- **渲染引擎**：递归组件树渲染
- **规则引擎**：安全的表达式执行
- **数据绑定**：观察者模式

## 浏览器支持

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## 性能优化

- 使用事件委托减少事件监听器数量
- 使用CSS transform进行缩放
- 使用requestAnimationFrame进行动画
- 使用IntersectionObserver进行懒加载
- 使用WeakMap存储组件引用

## 安全性

- 规则表达式在沙箱环境中执行
- 禁止访问全局对象（window、document等）
- 输入验证和过滤
- XSS防护
- CSRF防护（需要后端配合）

## 开发建议

1. **模块化开发**：每个文件负责独立功能
2. **状态管理**：使用单一状态树
3. **事件驱动**：使用事件进行模块通信
4. **配置驱动**：所有功能通过配置实现
5. **可扩展性**：预留扩展接口

## 注意事项

1. 确保组件编码唯一
2. 字段路径使用点号分隔（如：`supplier.name`）
3. 规则表达式使用JavaScript语法
4. 数据源测试需要真实后端支持
5. 配置导出前建议先验证

## 后续计划

- [ ] 添加撤销/重做功能
- [ ] 添加组件版本管理
- [ ] 添加模板权限控制
- [ ] 添加多语言支持
- [ ] 添加移动端适配
- [ ] 添加性能监控
- [ ] 添加单元测试
- [ ] 添加E2E测试

## 联系方式

如有问题或建议，请提交Issue或Pull Request。

---

**版本**：1.0.0
**最后更新**：2024-01-15