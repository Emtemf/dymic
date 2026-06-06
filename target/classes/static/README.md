# 合同模板动态渲染系统 - 前端 Demo

本 Demo 用于验证合同模板动态渲染系统的核心功能。

## 目录结构

```
demo/
├── index.html              # 入口页面 - 模板创建和版本管理
├── simple.html             # 简单界面 - 单页卡片表单
├── complex.html            # 复杂界面 - 栅格+Tab+明细表
├── css/
│   └── style.css           # 全局样式
└── js/
    ├── api.js              # HTTP 请求封装
    └── contract-api.js     # 合同相关 API
```

## 功能说明

### 1. 入口页面 (index.html)

- 创建合同模板
- 创建模板版本
- 发布模板版本
- 测试 API 连接

### 2. 简单界面 (simple.html)

单页卡片表单，包含：
- 合同基本信息
- 供应商选择弹窗
- 表单验证
- 合同保存和提交

### 3. 复杂界面 (complex.html)

复杂嵌套布局，包含：
- 左右栅格布局（16:8）
- 基本信息 + 商品明细表
- Tab 容器（附件、风险提示）
- 明细行弹窗编辑
- 供应商查询弹窗
- 文件上传功能

## 使用方法

### 1. 启动后端服务

确保后端 API 服务运行在 `http://localhost:8080`

### 2. 打开 Demo

在浏览器中打开 `index.html`：

```bash
# 方法 1: 使用 Python 简单服务器
cd /home/wula/IdeaProjects/dymic/demo
python3 -m http.server 8000

# 然后访问: http://localhost:8000

# 方法 2: 直接在浏览器打开文件
# file:///home/wula/IdeaProjects/dymic/demo/index.html
```

### 3. 验证流程

1. 打开 `index.html`
2. 点击"测试 API 连接"按钮验证后端连接
3. 创建模板（采购合同模板）
4. 创建版本（V1.0）
5. 发布版本
6. 点击"简单界面"测试单页表单
7. 点击"复杂界面"测试复杂布局

## API 配置

在 `index.html` 底部的 "API 配置" 区域可以修改 API 基础 URL。

默认配置：
```
http://localhost:8080/api
```

## 技术栈

- HTML5
- CSS3 (CSS Grid, Flexbox, CSS Variables)
- JavaScript ES6+
- Fetch API

## 代码风格

遵循 Google JavaScript Style Guide

## 注意事项

1. 本 Demo 使用模拟数据进行演示，实际使用时需要连接后端 API
2. 跨域问题：如果遇到 CORS 错误，请配置后端允许跨域请求
3. 文件上传：仅在前端模拟，实际需要后端支持

## 浏览器支持

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## 开发建议

### 调试技巧

1. 打开浏览器开发者工具（F12）
2. 查看 Console 面板中的日志信息
3. 查看 Network 面板中的 API 请求
4. 使用 Application 面板查看 localStorage

### 自定义样式

编辑 `css/style.css` 中的 CSS 变量：

```css
:root {
  --primary-color: #1a73e8;
  --success-color: #34a853;
  --warning-color: #fbbc04;
  --error-color: #ea4335;
}
```

### 扩展 API

在 `js/contract-api.js` 中添加新的 API 方法：

```javascript
ContractAPI.yourModule = {
  async yourMethod(params) {
    return await API.get('/your-endpoint', params);
  }
};
```

## 许可证

内部项目，仅供学习和测试使用
