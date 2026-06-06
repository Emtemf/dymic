---
name: naming-class
description: 类命名规则
---

# 类命名规则

## 核心原则

**Google Java Style**：类名使用大驼峰命名法（UpperCamelCase），名词或名词短语。

---

## 命名规范

### 规则1：大驼峰命名
- ✅ `TemplateController`
- ✅ `TemplateService`
- ✅ `TemplateRepository`
- ❌ `templateController`（错误：小驼峰）
- ❌ `Template_Controller`（错误：下划线）

### 规则2：名词或名词短语
- ✅ `User`、`Template`、`Contract`
- ✅ `TemplateService`、`ContractRepository`
- ❌ `Create`、`Delete`（错误：动词）
- ❌ `DoSomething`（错误：动词短语）

---

## 按类型命名规则

### Controller类
- 命名格式：`{Entity}Controller`
- 示例：
  - `TemplateController`
  - `ContractController`
  - `DatasourceController`

### Service类
- 接口命名：`{Entity}Service`
- 实现类命名：`{Entity}ServiceImpl`
- 示例：
  - `TemplateService` / `TemplateServiceImpl`
  - `ContractService` / `ContractServiceImpl`

### Repository类
- 接口命名：`{Entity}Repository`（领域层）
- 实现类命名：`{Entity}RepositoryImpl`（基础设施层）
- 示例：
  - `TemplateRepository` / `TemplateRepositoryImpl`
  - `ContractRepository` / `ContractRepositoryImpl`

### Mapper类
- 命名格式：`{Entity}Mapper`
- 示例：
  - `TemplateMapper`
  - `ContractMapper`

### Entity类
- 命名格式：`{Entity}Entity`
- 示例：
  - `TemplateEntity`
  - `ContractEntity`

### DTO类
- 命名格式：`{Entity}DTO`
- 示例：
  - `TemplateDTO`
  - `ContractDTO`

### Request类
- 命名格式：`{Action}{Entity}Request`
- 示例：
  - `CreateTemplateRequest`
  - `UpdateContractRequest`
  - `QueryTemplateRequest`

### Response类
- 命名格式：`{Entity}Response` 或 `{Action}{Entity}Response`
- 示例：
  - `TemplateResponse`
  - `CreateTemplateResponse`

---

## 领域对象命名

### 规则：不带后缀
- ✅ `Template`（领域对象）
- ✅ `Contract`（领域对象）
- ❌ `TemplateDO`（错误：使用后缀）
- ❌ `TemplateVO`（错误：使用后缀）
- ❌ `TemplatePO`（错误：使用后缀）

### 与Entity区分
| 类型 | 位置 | 命名 | 示例 |
|------|------|------|------|
| 领域对象 | domain.model | 无后缀 | `Template` |
| Entity | infrastructure.entity | Entity后缀 | `TemplateEntity` |
| DTO | application.dto | DTO后缀 | `TemplateDTO` |

---

## Converter/Converter类命名

### 命名格式
- `{Entity}Converter` 或 `{Entity}Convert`
- 示例：
  - `TemplateConverter`
  - `ContractConverter`

---

## 异常类命名

### 命名格式
- `{Entity}Exception` 或 `{Type}Exception`
- 示例：
  - `TemplateNotFoundException`
  - `ContractValidationException`
  - `BusinessException`
  - `SystemException`

---

## 枚举类命名

### 命名格式
- `{Entity}{Type}` 或 `{Type}`
- 示例：
  - `TemplateStatus`
  - `ContractType`
  - `DatasourceType`

---

## 详细规则引用

- 包命名规则：查看 `07-naming-package.md`
- 方法命名规则：查看 `09-naming-method.md`
- 字段命名规则：查看 `10-naming-field.md`