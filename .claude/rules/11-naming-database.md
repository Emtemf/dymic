---
name: naming-database
description: 数据库字段命名规则
---

# 数据库字段命名规则

## 核心原则

**数据库字段命名**：使用蛇形命名法（snake_case），全小写，下划线分隔。

---

## 命名规范

### 规则1：蛇形命名
- ✅ `template_name`
- ✅ `create_time`
- ✅ `datasource_type`
- ❌ `templateName`（错误：驼峰）
- ❌ `TemplateName`（错误：大驼峰）
- ❌ `TEMPLATENAME`（错误：全大写）

### 规则2：全小写
- ✅ `status_code`
- ✅ `template_code`
- ❌ `STATUS_CODE`（错误：全大写，除非是常量）
- ❌ `Status_Code`（错误：混合大小写）

### 规则3：下划线分隔
- ✅ `create_by`
- ✅ `update_time`
- ❌ `createBy`（错误：驼峰）
- ❌ `createby`（错误：无分隔）

---

## 主键和外键命名

### 主键
| 场景 | 命名 | 示例 |
|------|------|------|
| 单字段主键 | `id` | `id` |
| 自增主键 | `id` | `id` |
| UUID主键 | `id` | `id` |

### 外键
| 场景 | 命名 | 示例 |
|------|------|------|
| 外键 | `{table}_id` | `template_id`、`contract_id` |
| 关联表外键 | `{related_table}_id` | `datasource_id` |

---

## 常用字段命名

### 审计字段
| 场景 | 命名 | 示例 |
|------|------|------|
| 创建人 | `create_by` | `create_by` |
| 创建时间 | `create_time` | `create_time` |
| 更新人 | `update_by` | `update_by` |
| 更新时间 | `update_time` | `update_time` |
| 逻辑删除标志 | `deleted` 或 `is_deleted` | `deleted` |

### 状态字段
| 场景 | 命名 | 示例 |
|------|------|------|
| 状态 | `status` | `status` |
| 状态编码 | `status_code` | `status_code` |
| 版本号 | `version` | `version` |

### 名称和编码字段
| 场景 | 命名 | 示例 |
|------|------|------|
| 名称 | `{entity}_name` | `template_name` |
| 编码 | `{entity}_code` | `template_code` |
| 描述 | `{entity}_desc` | `template_desc` |

---

## Java字段与数据库字段映射

### 映射规则
使用MyBatis-Plus自动映射：
- Java字段：小驼峰（`templateName`）
- 数据库字段：蛇形（`template_name`）
- MyBatis-Plus自动转换

### 示例
```java
// Entity类
@Data
@TableName("t_template")
public class TemplateEntity {
    @TableId
    private Long id;
    
    private String templateName;    // 映射到 template_name
    private String templateCode;    // 映射到 template_code
    private LocalDateTime createTime; // 映射到 create_time
}
```

```sql
-- 数据库表
CREATE TABLE t_template (
    id BIGINT PRIMARY KEY,
    template_name VARCHAR(100),
    template_code VARCHAR(50),
    create_time DATETIME
);
```

---

## 表命名规则

### 规则：蛇形命名 + 表前缀
| 场景 | 命名 | 示例 |
|------|------|------|
| 业务表 | `t_{entity}` | `t_template`、`t_contract` |
| 关联表 | `t_{entity1}_{entity2}` | `t_template_contract` |
| 系统表 | `sys_{entity}` | `sys_config`、`sys_user` |

### 示例
```
✅ t_template
✅ t_contract
✅ t_datasource
✅ t_template_version
✅ t_contract_field
❌ template         （错误：无前缀）
❌ Template         （错误：大写）
❌ tTemplate        （错误：驼峰）
```

---

## 索引命名规则

### 规则：`idx_{table}_{columns}`
| 场景 | 命名 | 示例 |
|------|------|------|
| 单列索引 | `idx_{table}_{column}` | `idx_template_code` |
| 多列索引 | `idx_{table}_{col1}_{col2}` | `idx_template_status_create_time` |
| 唯一索引 | `uk_{table}_{column}` | `uk_template_code` |

---

## 禁止的命名方式

### 禁止驼峰
```
❌ templateName
❌ createTime
❌ statusCode
```

### 禁止全大写（非常量表）
```
❌ TEMPLATE_NAME
❌ CREATE_TIME
❌ STATUS_CODE
```

### 禁止无分隔
```
❌ templatename
❌ createtime
❌ statuscode
```

### 禁止驼峰+下划线混用
```
❌ template_Name
❌ create_Time
❌ status_Code
```

---

## 详细规则引用

- Java字段命名：查看 `10-naming-field.md`
- 特殊后缀规则：查看 `12-naming-special.md`
- 包结构规则：查看 `03-package-structure.md`