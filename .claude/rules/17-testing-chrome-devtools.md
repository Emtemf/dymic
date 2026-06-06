---
name: testing-chrome-devtools
description: chrome-devtools使用规则
---

# chrome-devtools使用规则

## 核心原则

**前端测试工具**：使用Chrome DevTools MCP进行E2E测试和页面验证。

---

## 工具概述

Chrome DevTools MCP提供以下功能：
- 页面导航和管理
- 元素操作（点击、填写、悬停）
- 截图和快照
- 网络请求监控
- 控制台消息监控
- 性能分析

---

## 工具列表

### 页面管理工具

| 工具名称 | 功能 | 使用场景 |
|---------|------|---------|
| `mcp__chrome-devtools__navigate_page` | 导航到URL | 打开测试页面 |
| `mcp__chrome-devtools__new_page` | 创建新页面 | 打开新窗口测试 |
| `mcp__chrome-devtools__list_pages` | 列出所有页面 | 管理多页面测试 |
| `mcp__chrome-devtools__select_page` | 选择当前页面 | 切换测试页面 |
| `mcp__chrome-devtools__close_page` | 关闭页面 | 清理测试环境 |

### 元素操作工具

| 工具名称 | 功能 | 使用场景 |
|---------|------|---------|
| `mcp__chrome-devtools__click` | 点击元素 | 点击按钮、链接 |
| `mcp__chrome-devtools__fill` | 填写字段 | 填写表单字段 |
| `mcp__chrome-devtools__fill_form` | 批量填写表单 | 快速填写多个字段 |
| `mcp__chrome-devtools__type_text` | 输入文本 | 模拟键盘输入 |
| `mcp__chrome-devtools__hover` | 悬停元素 | 触发悬停效果 |
| `mcp__chrome-devtools__press_key` | 按键 | 模拟键盘操作 |
| `mcp__chrome-devtools__drag` | 拖拽元素 | 拖拽排序等 |
| `mcp__chrome-devtools__upload_file` | 上传文件 | 文件上传测试 |

### 验证工具

| 工具名称 | 功能 | 使用场景 |
|---------|------|---------|
| `mcp__chrome-devtools__take_screenshot` | 截取页面截图 | 视觉验证 |
| `mcp__chrome-devtools__take_snapshot` | 获取DOM快照 | 结构验证 |
| `mcp__chrome-devtools__wait_for` | 等待条件 | 同步等待 |

### 监控工具

| 工具名称 | 功能 | 使用场景 |
|---------|------|---------|
| `mcp__chrome-devtools__list_network_requests` | 列出网络请求 | API调用验证 |
| `mcp__chrome-devtools__get_network_request` | 获取请求详情 | 请求参数验证 |
| `mcp__chrome-devtools__list_console_messages` | 列出控制台消息 | 错误检查 |
| `mcp__chrome-devtools__get_console_message` | 获取消息详情 | 错误详情分析 |

### 性能工具

| 工具名称 | 功能 | 使用场景 |
|---------|------|---------|
| `mcp__chrome-devtools__lighthouse_audit` | 性能审计 | 页面性能分析 |
| `mcp__chrome-devtools__performance_start_trace` | 开始性能追踪 | 性能问题定位 |
| `mcp__chrome-devtools__performance_stop_trace` | 停止性能追踪 | 获取性能数据 |
| `mcp__chrome-devtools__performance_analyze_insight` | 分析性能洞察 | 性能优化建议 |
| `mcp__chrome-devtools__take_heapsnapshot` | 堆快照 | 内存泄漏分析 |

---

## 使用示例

### 1. 基本页面测试流程

```markdown
步骤1: 打开页面
使用 navigate_page 打开目标URL

步骤2: 等待页面加载
使用 wait_for 等待关键元素出现

步骤3: 填写表单
使用 fill 填写各个字段

步骤4: 提交表单
使用 click 点击提交按钮

步骤5: 验证结果
使用 take_screenshot 截图验证
使用 list_network_requests 验证API调用
```

### 2. 表单验证测试

```markdown
步骤1: 打开表单页面
使用 navigate_page 打开表单URL

步骤2: 获取初始快照
使用 take_snapshot 记录初始状态

步骤3: 触发校验
使用 fill 填写无效数据
使用 click 触发校验

步骤4: 验证错误提示
使用 take_screenshot 截图
使用 take_snapshot 检查错误信息显示

步骤5: 填写有效数据
使用 fill_form 批量填写正确数据

步骤6: 提交验证
使用 click 提交表单
使用 list_network_requests 验证提交请求
```

### 3. API调用验证

```markdown
步骤1: 打开页面
使用 navigate_page 打开目标页面

步骤2: 触发API调用
使用 click 触发按钮

步骤3: 检查网络请求
使用 list_network_requests 获取请求列表
使用 get_network_request 获取请求详情

步骤4: 验证请求参数
检查请求URL、方法、参数是否正确

步骤5: 验证响应结果
检查响应状态码、响应数据是否正确
```

### 4. 错误检查

```markdown
步骤1: 执行操作
使用 navigate_page 和 fill 执行操作

步骤2: 检查控制台
使用 list_console_messages 获取控制台消息

步骤3: 分析错误
使用 get_console_message 获取错误详情

步骤4: 验证无错误
确保没有JavaScript错误
```

---

## 最佳实践

### 1. 等待策略
```
优先使用 wait_for 等待特定条件：
- 等待元素出现
- 等待网络请求完成
- 等待URL变化
```

### 2. 截图命名
```
截图时使用有意义的名称标识：
- 页面加载完成
- 表单填写后
- 提交成功后
- 错误提示出现
```

### 3. 网络请求过滤
```
使用URL过滤特定API请求：
- 过滤 /api/templates 只查看模板API
- 过滤 POST 只查看提交请求
```

### 4. 控制台消息过滤
```
使用类型过滤消息：
- error: 只查看错误
- warning: 查看警告和错误
- log: 查看所有日志
```

---

## 常见问题解决

### 问题1：元素定位失败
```
解决方案：
1. 使用 take_snapshot 获取当前DOM结构
2. 检查选择器是否正确
3. 使用 wait_for 等待元素出现
```

### 问题2：网络请求未捕获
```
解决方案：
1. 确保在操作前开启网络监控
2. 使用 list_network_requests 获取完整列表
3. 使用 get_network_request 获取详情
```

### 问题3：页面加载超时
```
解决方案：
1. 增加等待时间
2. 使用 wait_for 等待关键元素
3. 检查网络状态
```

---

## 详细规则引用

- 前端界面验证：查看 `16-testing-frontend.md`
- 优先级1验证步骤：查看 `20-validation-priority1.md`
- 优先级2验证步骤：查看 `21-validation-priority2.md`