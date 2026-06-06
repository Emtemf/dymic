---
name: naming-special
description: 特殊后缀规则
---

# 特殊后缀规则

## 核心原则

**后缀命名规范**：根据类型使用特定后缀，便于识别对象用途。

---

## 后缀分类

### DTO后缀规则

| 类型 | 后缀 | 示例 | 说明 |
|------|------|------|------|
| 数据传输对象 | `DTO` | `TemplateDTO` | Service与Controller之间传递 |
| 创建请求 | `CreateRequest` | `CreateTemplateRequest` | Controller入参 |
| 更新请求 | `UpdateRequest` | `UpdateTemplateRequest` | Controller入参 |
| 查询请求 | `QueryRequest` | `QueryTemplateRequest` | Controller入参 |
| 查询条件 | `Condition` 或 `Query` | `TemplateCondition` | Service查询条件 |
| 响应对象 | `Response` | `TemplateResponse` | Controller出参 |

### Entity后缀规则

| 类型 | 后缀 | 示例 | 说明 |
|------|------|------|------|
| 数据库实体 | `Entity` | `TemplateEntity` | 与数据库表映射 |
| MyBatis映射 | 无后缀 | `Template` | 领域对象不带后缀 |

---

## 不使用后缀的场景

### 领域对象：无后缀
```
✅ Template          （领域对象）
✅ Contract          （领域对象）
✅ Datasource        （领域对象）
❌ TemplateDO        （错误：领域对象不应使用DO）
❌ TemplateVO        （错误：领域对象不应使用VO）
❌ TemplatePO        （错误：领域对象不应使用PO）
```

### 为什么领域对象不使用后缀？
- 领域对象是核心业务模型
- 不应暴露技术细节（如DO、PO、VO）
- 保持业务语义纯粹

---

## 各层对象后缀对照表

| 层级 | 对象类型 | 后缀 | 示例 | 位置 |
|------|---------|------|------|------|
| Controller | Request | `Request` | `CreateTemplateRequest` | application.dto |
| Controller | Response | `Response` | `TemplateResponse` | application.dto |
| Service | DTO | `DTO` | `TemplateDTO` | application.dto |
| Service | Condition | `Condition` | `TemplateCondition` | application.condition |
| Domain | Domain Object | 无后缀 | `Template` | domain.model |
| Infrastructure | Entity | `Entity` | `TemplateEntity` | infrastructure.entity |
| Infrastructure | Mapper | `Mapper` | `TemplateMapper` | infrastructure.mapper |

---

## 特殊场景后缀

### 异常类
| 类型 | 后缀 | 示例 |
|------|------|------|
| 业务异常 | `Exception` | `TemplateNotFoundException` |
| 系统异常 | `Exception` | `SystemException` |
| 参数异常 | `Exception` | `IllegalArgumentException` |

### 枚举类
| 类型 | 后缀 | 示例 |
|------|------|------|
| 状态枚举 | `Status` | `TemplateStatus` |
| 类型枚举 | `Type` | `DatasourceType` |
| 操作枚举 | `Type` 或 `Operation` | `AuditOperation` |

### 转换器
| 类型 | 后缀 | 示例 |
|------|------|------|
| MapStruct转换器 | `Converter` | `TemplateConverter` |
| 工具转换器 | `Converter` 或 `Convert` | `DateConverter` |

### 工具类
| 类型 | 后缀 | 示例 |
|------|------|------|
| 工具类 | `Util` 或 `Utils` | `StringUtil`、`DateUtils` |
| 帮助类 | `Helper` | `ValidationHelper` |

---

## 禁止使用的后缀

### 禁止在领域对象使用
```
❌ TemplateDO     （使用 Template）
❌ TemplatePO     （使用 Template）
❌ TemplateVO     （使用 Template）
❌ TemplateBO     （使用 Template）
❌ TemplateAO     （使用 Template）
```

### 禁止在Entity使用
```
❌ TemplateDO     （使用 TemplateEntity）
❌ TemplatePO     （使用 TemplateEntity）
❌ Template       （使用 TemplateEntity）
```

### 禁止在DTO使用
```
❌ TemplateDO     （使用 TemplateDTO）
❌ TemplatePO     （使用 TemplateDTO）
❌ Template       （使用 TemplateDTO）
```

---

## 后缀使用示例

### 正确示例
```java
// 领域层 - 无后缀
package com.contract.template.domain.model;
public class Template { ... }

// 基础设施层 - Entity后缀
package com.contract.template.infrastructure.entity;
public class TemplateEntity { ... }

// 应用层 - DTO后缀
package com.contract.template.application.dto;
public class TemplateDTO { ... }
public class CreateTemplateRequest { ... }

// 转换器 - Converter后缀
package com.contract.template.application.convert;
public interface TemplateConverter { ... }
```

### 错误示例
```java
// 错误：领域对象使用了后缀
package com.contract.template.domain.model;
public class TemplateDO { ... }  // 应该是 Template

// 错误：Entity没有使用后缀
package com.contract.template.infrastructure.entity;
public class Template { ... }  // 应该是 TemplateEntity

// 错误：DTO使用了错误后缀
package com.contract.template.application.dto;
public class TemplateVO { ... }  // 应该是 TemplateDTO
```

---

## 详细规则引用

- 类命名规则：查看 `08-naming-class.md`
- 字段命名规则：查看 `10-naming-field.md`
- 对象转换规则：查看 `06-conversion.md`