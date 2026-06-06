---
name: naming-field
description: 字段命名规则
---

# 字段命名规则

## 核心原则

**Google Java Style**：字段名使用小驼峰命名法（lowerCamelCase），名词或名词短语。

---

## 命名规范

### 规则1：小驼峰命名
- ✅ `templateName`
- ✅ `createTime`
- ✅ `status`
- ❌ `TemplateName`（错误：大驼峰）
- ❌ `template_name`（错误：下划线）
- ❌ `TEMPLATENAME`（错误：全大写）

### 规则2：名词或名词短语
- ✅ `name`、`status`、`createTime`
- ✅ `templateCode`、`datasourceType`
- ❌ `create`（错误：动词）
- ❌ `isPublished`（错误：布尔类型应使用is前缀）

---

## 按数据类型命名

### 字符串类型
| 场景 | 命名 | 示例 |
|------|------|------|
| 名称 | `{Entity}Name` | `templateName`、`contractName` |
| 编码 | `{Entity}Code` | `templateCode`、`contractCode` |
| 描述 | `{Entity}Desc` 或 `description` | `templateDesc` |
| 类型 | `{Entity}Type` | `datasourceType` |
| 内容 | `content` 或 `{Entity}Content` | `content` |

### 数值类型
| 场景 | 命名 | 示例 |
|------|------|------|
| 数量 | `{Entity}Count` 或 `quantity` | `itemCount` |
| 金额 | `{Type}Amount` 或 `{Type}Fee` | `totalAmount` |
| 百分比 | `{Type}Rate` 或 `{Type}Percent` | `taxRate` |
| 排序 | `sort` 或 `orderNum` | `sort` |
| 版本 | `version` | `version` |

### 布尔类型
| 场景 | 命名 | 示例 |
|------|------|------|
| 状态标志 | `is{State}` | `isActive`、`isPublished` |
| 存在标志 | `has{Something}` | `hasChildren` |
| 能力标志 | `can{Action}` | `canEdit`、`canDelete` |
| 功能标志 | `{Feature}Flag` 或 `enable{Feature}` | `enableAudit` |

### 日期时间类型
| 场景 | 命名 | 示例 |
|------|------|------|
| 创建时间 | `createTime` | `createTime` |
| 更新时间 | `updateTime` | `updateTime` |
| 发布时间 | `publishTime` | `publishTime` |
| 开始时间 | `startTime` 或 `beginTime` | `startTime` |
| 结束时间 | `endTime` | `endTime` |
| 有效期 | `validTime` 或 `expireTime` | `expireTime` |

---

## 常用字段命名

### 主键和外键
| 场景 | 命名 | 示例 |
|------|------|------|
| 主键 | `id` | `id` |
| 外键 | `{Entity}Id` | `templateId`、`contractId` |

### 审计字段
| 场景 | 命名 | 示例 |
|------|------|------|
| 创建人 | `createBy` 或 `creator` | `createBy` |
| 创建时间 | `createTime` | `createTime` |
| 更新人 | `updateBy` 或 `modifier` | `updateBy` |
| 更新时间 | `updateTime` | `updateTime` |

### 状态字段
| 场景 | 命名 | 示例 |
|------|------|------|
| 状态 | `status` | `status` |
| 状态编码 | `statusCode` | `statusCode` |

### 配置字段
| 场景 | 命名 | 示例 |
|------|------|------|
| 配置 | `config` 或 `{Type}Config` | `datasourceConfig` |
| 配置JSON | `configJson` | `configJson` |

---

## 集合类型命名

### List类型
- 命名格式：`{Entity}List` 或 `{Entity}s`
- 示例：
  - `templateList`
  - `items`、`children`

### Map类型
- 命名格式：`{KeyType}To{ValueType}Map` 或 `{Purpose}Map`
- 示例：
  - `codeToNameMap`
  - `fieldValueMap`

### Set类型
- 命名格式：`{Entity}Set`
- 示例：
  - `idSet`
  - `codeSet`

---

## 禁止的命名方式

### 禁止大驼峰
```
❌ TemplateName
❌ CreateTime
❌ StatusCode
```

### 禁止下划线
```
❌ template_name
❌ create_time
❌ status_code
```

### 禁止匈牙利命名法
```
❌ strName
❌ intCount
❌ boolActive
❌ lstTemplates
```

### 禁止缩写（除非通用）
```
❌ tmpName    （应使用 templateName）
❌ dtTime     （应使用 dateTime）
❌ cnt        （应使用 count）
```

---

## 详细规则引用

- 类命名规则：查看 `08-naming-class.md`
- 方法命名规则：查看 `09-naming-method.md`
- 数据库字段命名：查看 `11-naming-database.md`