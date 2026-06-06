---
name: naming-method
description: 方法命名规则
---

# 方法命名规则

## 核心原则

**Google Java Style**：方法名使用小驼峰命名法（lowerCamelCase），动词或动词短语开头。

---

## 命名规范

### 规则1：小驼峰命名
- ✅ `createTemplate`
- ✅ `findById`
- ✅ `updateStatus`
- ❌ `CreateTemplate`（错误：大驼峰）
- ❌ `create_template`（错误：下划线）

### 规则2：动词开头
- ✅ `create`、`update`、`delete`、`find`、`get`、`list`、`query`
- ❌ `template`（错误：名词开头）
- ❌ `templateCreate`（错误：名词开头）

---

## CRUD操作命名

### 创建操作
| 场景 | 命名 | 示例 |
|------|------|------|
| 创建单个 | `create{Entity}` | `createTemplate` |
| 批量创建 | `create{Entity}List` 或 `batchCreate{Entity}` | `createTemplateList` |
| 保存 | `save{Entity}` | `saveTemplate` |

### 查询操作
| 场景 | 命名 | 示例 |
|------|------|------|
| 按ID查询 | `findById` 或 `getById` | `findById` |
| 按编码查询 | `findByCode` 或 `getByCode` | `findByCode` |
| 查询列表 | `list{Entity}` 或 `findAll` | `listTemplates` |
| 条件查询 | `query{Entity}` | `queryTemplates` |
| 分页查询 | `page{Entity}` | `pageTemplates` |
| 统计数量 | `count{Entity}` | `countTemplates` |
| 检查存在 | `exists{Condition}` | `existsByCode` |

### 更新操作
| 场景 | 命名 | 示例 |
|------|------|------|
| 更新单个 | `update{Entity}` | `updateTemplate` |
| 批量更新 | `batchUpdate{Entity}` | `batchUpdateTemplate` |
| 更新状态 | `update{Entity}Status` | `updateTemplateStatus` |

### 删除操作
| 场景 | 命名 | 示例 |
|------|------|------|
| 删除单个 | `delete{Entity}` 或 `deleteById` | `deleteTemplate` |
| 批量删除 | `batchDelete{Entity}` | `batchDeleteTemplate` |
| 逻辑删除 | `remove{Entity}` | `removeTemplate` |

---

## 业务操作命名

### 状态变更
| 场景 | 命名 | 示例 |
|------|------|------|
| 启用 | `enable{Entity}` | `enableTemplate` |
| 停用 | `disable{Entity}` | `disableTemplate` |
| 发布 | `publish{Entity}` | `publishVersion` |
| 撤回 | `withdraw{Entity}` | `withdrawContract` |

### 复杂业务
| 场景 | 命名 | 示例 |
|------|------|------|
| 计算 | `calculate{Something}` | `calculateTotal` |
| 验证 | `validate{Something}` | `validateTemplate` |
| 转换 | `convert{From}To{To}` | `convertEntityToDTO` |
| 处理 | `process{Something}` | `processContract` |
| 执行 | `execute{Action}` | `executeRule` |

---

## 布尔返回方法命名

### 规则：使用 is/has/can 前缀
| 场景 | 命名 | 示例 |
|------|------|------|
| 判断状态 | `is{State}` | `isActive`、`isPublished` |
| 判断存在 | `has{Something}` | `hasPermission`、`hasChildren` |
| 判断能力 | `can{Action}` | `canDelete`、`canEdit` |

---

## 转换方法命名

### MapStruct转换方法
| 转换方向 | 命名 | 示例 |
|---------|------|------|
| DTO → Domain | `toDomain` | `toDomain(TemplateDTO dto)` |
| Domain → DTO | `toDTO` | `toDTO(Template domain)` |
| Request → DTO | `toDTO` | `toDTO(CreateTemplateRequest request)` |
| Domain → Entity | `toEntity` | `toEntity(Template domain)` |
| Entity → Domain | `toDomain` | `toDomain(TemplateEntity entity)` |
| List转换 | `toDTOList` | `toDTOList(List<Template> domains)` |

---

## Getter/Setter命名

### 标准命名
- Getter：`get{FieldName}`
- Setter：`set{FieldName}`
- 布尔类型Getter：`is{FieldName}`

### 示例
```java
public String getName() { return name; }
public void setName(String name) { this.name = name; }
public boolean isActive() { return active; }
```

---

## 禁止的命名方式

### 禁止大驼峰
```
❌ CreateTemplate
❌ FindById
❌ UpdateStatus
```

### 禁止下划线
```
❌ create_template
❌ find_by_id
❌ update_status
```

### 禁止无意义动词
```
❌ doCreate
❌ makeFind
❌ performUpdate
```

---

## 详细规则引用

- 类命名规则：查看 `08-naming-class.md`
- 字段命名规则：查看 `10-naming-field.md`
- 数据库字段命名：查看 `11-naming-database.md`