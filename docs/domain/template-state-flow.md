# Template 聚合状态流转

## Template 状态

```
ENABLED ──disable()──→ DISABLED
DISABLED ──enable()──→ ENABLED
```

| 状态 | 说明 | 允许的操作 |
|------|------|-----------|
| ENABLED | 启用 | disable(), 删除 |
| DISABLED | 停用 | enable(), 删除 |

### 工厂方法
- `Template.create(code, name, desc, bizType)` → 创建模板，初始状态 ENABLED

### 业务规则
1. 创建模板时自动创建 DRAFT 版本（由 DomainService 编排）
2. 模板编码创建后不可修改
3. 停用模板不影响已发布版本

---

## TemplateVersion 状态

```
DRAFT ──publish()──→ PUBLISHED ──disable()──→ DISABLED
DRAFT ──(废弃)──→ ARCHIVED
```

| 状态 | 说明 | 允许的操作 |
|------|------|-----------|
| DRAFT | 草稿 | publish(), 废弃 |
| PUBLISHED | 已发布 | disable() |
| DISABLED | 停用 | - |
| ARCHIVED | 已归档 | - |

### 工厂方法
- `TemplateVersion.createDraft(templateId, versionNo, versionName)` → 创建草稿版本

### 业务规则
1. 只有 DRAFT 状态才能发布
2. 发布时记录 publishTime 和 publishBy
3. PUBLISHED 状态不可回退为 DRAFT
4. 基于已发布版本创建新草稿不影响原版本
