---
name: conversion
description: 对象转换规则
---

# 对象转换规则

## 核心原则

**职责分离**：每层使用自己的数据对象，层与层之间通过转换器转换。

```
Controller层：Request/Response/DTO
      ↓ (Converter)
Service层：DTO/Condition
      ↓ (Converter)
Domain层：领域对象（Domain Object）
      ↓ (Converter)
Infrastructure层：Entity
```

---

## 对象类型定义

### 1. DTO（Data Transfer Object）
- 位置：`application.dto` 包
- 用途：Service层与Controller层之间传递数据
- 命名：`{Entity}DTO`（如 `TemplateDTO`）

### 2. 领域对象（Domain Object）
- 位置：`domain.model` 包
- 用途：承载核心业务逻辑
- 命名：`{Entity}`（如 `Template`，不带后缀）

### 3. Entity
- 位置：`infrastructure.entity` 包
- 用途：数据库映射
- 命名：`{Entity}Entity`（如 `TemplateEntity`）

### 4. Request/Response
- 位置：`application.dto` 包
- 用途：Controller层入参和出参
- 命名：`{Action}{Entity}Request/Response`

---

## 转换器规则

### 规则1：MapStruct转换器位置
- 应用层转换器：`application.convert` 包
- 基础设施层转换器：`infrastructure.convert` 包

### 规则2：转换器命名规则
- DTO转换器：`{Entity}Converter`（如 `TemplateConverter`）
- Entity转换器：`{Entity}EntityConverter`（如 `TemplateEntityConverter`）

### 规则3：转换方法命名
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    // Request → DTO
    TemplateDTO toDTO(CreateTemplateRequest request);
    
    // DTO → 领域对象
    Template toDomain(TemplateDTO dto);
    
    // 领域对象 → DTO
    TemplateDTO toDTO(Template domain);
    
    // List转换
    List<TemplateDTO> toDTOList(List<Template> domains);
}
```

---

## 各层转换职责

### Controller层
- **输入**：Request对象
- **输出**：Response对象或DTO
- **转换**：调用Service层方法，Service返回DTO后包装为Response

### Service层
- **输入**：DTO或Request对象
- **输出**：DTO对象
- **转换**：DTO ↔ 领域对象

### Domain层
- **输入**：领域对象
- **输出**：领域对象
- **转换**：不涉及（纯业务逻辑）

### Infrastructure层
- **输入**：领域对象
- **输出**：领域对象
- **转换**：领域对象 ↔ Entity

---

## MapStruct使用规范

### 基本配置
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    TemplateConverter INSTANCE = Mappers.getMapper(TemplateConverter.class);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", expression = "java(LocalDateTime.now())")
    Template toDomain(CreateTemplateRequest request);
    
    @Mapping(target = "status", constant = "ACTIVE")
    TemplateDTO toDTO(Template domain);
}
```

### 复杂映射
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    @Mappings({
        @Mapping(source = "templateCode", target = "code"),
        @Mapping(source = "templateName", target = "name"),
        @Mapping(target = "version", constant = "1")
    })
    Template toDomain(CreateTemplateRequest request);
}
```

---

## 转换时机

| 层级 | 入站转换 | 出站转换 |
|------|---------|---------|
| Controller | Request → DTO | DTO → Response |
| Service | DTO → 领域对象 | 领域对象 → DTO |
| Infrastructure | 领域对象 → Entity | Entity → 领域对象 |

---

## 详细规则引用

- 架构规则：查看 `02-architecture.md`
- 各层职责规则：查看 `04-layer-responsibility.md`
- MapStruct使用指南：查看 `25-mapstruct-guide.md`