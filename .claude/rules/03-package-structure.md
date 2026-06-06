---
name: package-structure
description: 包结构规则
---

# 包结构规则

## 包命名规则

**原则**:全小写,点分隔,不使用驼峰

示例:
- ✅ `com.contract.template.controller`
- ✅ `com.contract.template.application.service`
- ❌ `com.contract.template.Controller` (错误:大写)
- ❌ `com.contract.template.controllerPackage` (错误:驼峰)

## 包结构层次

```
com.contract.template
├── controller          # 接口层
├── application         # 应用层
│   ├── service        # 应用服务
│   ├── dto            # 数据传输对象
│   ├── condition      # 查询条件
│   └─ convert         # 转换器(MapStruct)
├── domain              # 领域层
│   ├── model          # 领域对象
│   ├── service        # 领域服务
│   ├── repository     # 仓储接口
│   └─ gateway         # 外部端口接口
├── infrastructure      # 基础设施层
│   ├── repository     # 仓储实现
│   ├── mapper         # MyBatis Mapper
│   ├── entity         # 数据库实体
│   ├── gateway        # 外部端口实现
│   └─ convert         # 转换器(MapStruct)
└── common              # 公共模块
    ├── exception      # 异常定义
    ├── result         # 统一响应
    ├── enums          # 枚举定义
    └─ util            # 工具类
```

## 包职责说明

### controller 包
- 所有Controller类
- REST API入口

### application 包
- 业务编排服务
- DTO/Condition定义
- 应用层转换器

### domain 包
- 领域对象(不带Entity后缀)
- 领域服务(业务规则)
- Repository接口定义
- Gateway接口定义

### infrastructure 包
- Repository接口实现
- Entity(带Entity后缀)
- Mapper接口
- Gateway接口实现
- 基础设施层转换器

### common 包
- 异常类
- 响应包装类
- 枚举类
- 工具类

## 详细规则引用

- 类命名规则:查看 `08-naming-class.md`
- 各层职责规则:查看 `04-layer-responsibility.md`