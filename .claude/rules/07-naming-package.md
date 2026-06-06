---
name: naming-package
description: 包命名规则
---

# 包命名规则

## 核心原则

**Google Java Style**：包名全部小写，使用点分隔，不使用驼峰。

---

## 命名规范

### 规则1：全小写
- ✅ `com.contract.template.controller`
- ✅ `com.contract.template.application.service`
- ❌ `com.contract.template.Controller`（错误：大写）
- ❌ `com.contract.template.Application`（错误：大写）

### 规则2：点分隔
- ✅ `com.contract.template.domain.model`
- ✅ `com.contract.template.infrastructure.repository`
- ❌ `com/contract/template`（错误：斜杠分隔）
- ❌ `com_contract_template`（错误：下划线分隔）

### 规则3：不使用驼峰
- ✅ `com.contract.template.repository`
- ❌ `com.contract.template.repositoryPackage`（错误：驼峰）
- ❌ `com.contract.template.Repository`（错误：大写）

---

## 项目包结构

### 顶层包名
```
com.contract.template
```

### 子包结构
```
com.contract.template
├── controller              # 接口层
├── application             # 应用层
│   ├── service            # 应用服务
│   ├── dto                # 数据传输对象
│   ├── condition          # 查询条件
│   └── convert            # 转换器
├── domain                  # 领域层
│   ├── model              # 领域对象
│   ├── service            # 领域服务
│   ├── repository         # 仓储接口
│   └── gateway            # 外部端口接口
├── infrastructure          # 基础设施层
│   ├── repository         # 仓储实现
│   ├── mapper             # MyBatis Mapper
│   ├── entity             # 数据库实体
│   ├── gateway            # 外部端口实现
│   └── convert            # 转换器
└── common                  # 公共模块
    ├── exception          # 异常定义
    ├── result             # 统一响应
    ├── enums              # 枚举定义
    └── util               # 工具类
```

---

## 包命名示例

### 按功能命名
```
✅ com.contract.template.service
✅ com.contract.template.repository
✅ com.contract.template.mapper
✅ com.contract.template.entity
✅ com.contract.template.dto
✅ com.contract.template.util
✅ com.contract.template.exception
✅ com.contract.template.enums
```

### 按领域命名
```
✅ com.contract.template.template
✅ com.contract.template.contract
✅ com.contract.template.datasource
✅ com.contract.template.version
```

---

## 禁止的命名方式

### 禁止大写
```
❌ com.contract.template.Controller
❌ com.contract.template.Service
❌ com.contract.template.Repository
```

### 禁止驼峰
```
❌ com.contract.template.templateService
❌ com.contract.template.repositoryImpl
❌ com.contract.template.dtoConverter
```

### 禁止下划线
```
❌ com.contract.template.template_service
❌ com.contract.template.repository_impl
❌ com.contract.template.dto_converter
```

### 禁止中划线
```
❌ com.contract.template.template-service
❌ com.contract.template.repository-impl
```

---

## 详细规则引用

- 类命名规则：查看 `08-naming-class.md`
- 方法命名规则：查看 `09-naming-method.md`
- 包结构规则：查看 `03-package-structure.md`