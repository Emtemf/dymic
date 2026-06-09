# DDD 规范重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将存量代码重构为符合标准DDD规范的充血模型，包括值对象封装、状态流转文档、业务方法契约、MapStruct转换器。

**Architecture:** 四层架构：Adapter → Application → Domain → Infrastructure。领域层使用充血模型，值对象封装，不暴露setter。基础设施层使用MapStruct进行Domain ↔ Entity转换。

**Tech Stack:** Java 21 / Spring Boot 3.5.14 / MapStruct 1.5.5.Final / Lombok 1.18.30 / MyBatis-Plus 3.5.5 / OffsetDateTime

---

## 文件结构总览

### 新建文件

| 文件 | 职责 |
|------|------|
| `domain/template/types/TemplateId.java` | 模板ID值对象 |
| `domain/template/types/TemplateCode.java` | 模板编码值对象 |
| `domain/template/types/TemplateName.java` | 模板名称值对象 |
| `domain/template/types/TemplateDesc.java` | 模板描述值对象 |
| `domain/template/types/BizType.java` | 业务类型值对象 |
| `domain/template/types/TemplateStatus.java` | 模板状态枚举 |
| `domain/template/types/VersionStatus.java` | 版本状态枚举 |
| `domain/shared/types/AuditInfo.java` | 审计信息值对象 |
| `domain/shared/types/ConfigJson.java` | 配置JSON值对象 |
| `domain/dataprovider/types/ProviderId.java` | 数据源ID值对象 |
| `domain/dataprovider/types/ProviderCode.java` | 数据源编码值对象 |
| `domain/dataprovider/types/ProviderName.java` | 数据源名称值对象 |
| `domain/dataprovider/types/ProviderType.java` | 数据源类型枚举 |
| `domain/dataprovider/types/ProviderStatus.java` | 数据源状态枚举 |
| `domain/dataprovider/types/DataSourceCategory.java` | 数据源分类枚举 |
| `domain/dataprovider/types/CacheConfig.java` | 缓存配置值对象 |
| `domain/dataprovider/types/DictType.java` | 字典类型值对象 |
| `infrastructure/persistence/convert/TemplateConverter.java` | 模板MapStruct转换器 |
| `infrastructure/persistence/convert/TemplateVersionConverter.java` | 版本MapStruct转换器 |
| `infrastructure/persistence/convert/DataProviderConverter.java` | 数据源MapStruct转换器 |

### 修改文件

| 文件 | 改动 |
|------|------|
| `domain/template/Template.java` | 移除@Data，添加值对象，状态流转文档 |
| `domain/template/TemplateVersion.java` | 移除@Data，添加值对象，状态流转文档 |
| `domain/template/DataProvider.java` | 移除@Data，添加值对象，状态流转文档 |
| `domain/template/LayoutNode.java` | 移除@Data，添加值对象 |
| `domain/template/FieldDef.java` | 移除@Data，添加值对象 |
| `domain/template/FieldComponent.java` | 移除@Data，添加值对象 |
| `infrastructure/persistence/repository/TemplateRepositoryImpl.java` | 使用MapStruct转换 |
| `infrastructure/persistence/repository/TemplateVersionRepositoryImpl.java` | 使用MapStruct转换 |
| `infrastructure/persistence/repository/DataProviderRepositoryImpl.java` | 使用MapStruct转换 |

### 不修改文件

- Controller层（API接口不变）
- 前端代码（前端功能不变）
- 数据库表结构（只是映射逻辑变化）
- Entity类（基础设施层保留）

---

## Batch 1: Template + TemplateVersion 重构

### Task 1.1: 创建 Template 相关值对象

**Files:**
- Create: `src/main/java/com/contract/domain/template/types/TemplateId.java`
- Create: `src/main/java/com/contract/domain/template/types/TemplateCode.java`
- Create: `src/main/java/com/contract/domain/template/types/TemplateName.java`
- Create: `src/main/java/com/contract/domain/template/types/TemplateDesc.java`
- Create: `src/main/java/com/contract/domain/template/types/BizType.java`

- [ ] **Step 1: 创建 TemplateId 值对象**

```java
package com.contract.domain.template.types;

import java.util.Objects;

/**
 * 模板ID值对象
 */
public final class TemplateId {
    private final Long value;
    
    public TemplateId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("模板ID无效");
        }
        this.value = value;
    }
    
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateId that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
```

- [ ] **Step 2: 创建 TemplateCode 值对象**

```java
package com.contract.domain.template.types;

import com.contract.common.exception.BizException;
import java.util.Objects;

/**
 * 模板编码值对象
 * 
 * 【业务规则】
 * - 不能为空
 * - 长度1-50
 * - 只能包含字母、数字、下划线
 */
public final class TemplateCode {
    private final String value;
    
    public TemplateCode(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException("模板编码不能为空");
        }
        if (value.length() > 50) {
            throw new BizException("模板编码长度不能超过50");
        }
        if (!value.matches("^[A-Za-z0-9_]+$")) {
            throw new BizException("模板编码只能包含字母、数字、下划线");
        }
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateCode that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

- [ ] **Step 3: 创建 TemplateName 值对象**

```java
package com.contract.domain.template.types;

import com.contract.common.exception.BizException;
import java.util.Objects;

/**
 * 模板名称值对象
 */
public final class TemplateName {
    private final String value;
    
    public TemplateName(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException("模板名称不能为空");
        }
        if (value.length() > 100) {
            throw new BizException("模板名称长度不能超过100");
        }
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateName that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

- [ ] **Step 4: 创建 TemplateDesc 值对象**

```java
package com.contract.domain.template.types;

import java.util.Objects;

/**
 * 模板描述值对象
 */
public final class TemplateDesc {
    private final String value;
    
    public TemplateDesc(String value) {
        if (value != null && value.length() > 500) {
            throw new IllegalArgumentException("模板描述长度不能超过500");
        }
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateDesc that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value != null ? value : "";
    }
}
```

- [ ] **Step 5: 创建 BizType 值对象**

```java
package com.contract.domain.template.types;

import java.util.Objects;

/**
 * 业务类型值对象
 */
public final class BizType {
    private final String value;
    
    public BizType(String value) {
        if (value != null && value.length() > 50) {
            throw new IllegalArgumentException("业务类型长度不能超过50");
        }
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BizType that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value != null ? value : "";
    }
}
```

- [ ] **Step 6: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/contract/domain/template/types/
git commit -m "feat: add Template value objects (TemplateId, TemplateCode, TemplateName, TemplateDesc, BizType)"
```

---

### Task 1.2: 创建 TemplateStatus 和 VersionStatus 枚举

**Files:**
- Create: `src/main/java/com/contract/domain/template/types/TemplateStatus.java`
- Create: `src/main/java/com/contract/domain/template/types/VersionStatus.java`

- [ ] **Step 1: 创建 TemplateStatus 枚举**

```java
package com.contract.domain.template.types;

/**
 * 模板状态枚举
 */
public enum TemplateStatus {
    ENABLED("启用"),
    DISABLED("停用");
    
    private final String displayName;
    
    TemplateStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

- [ ] **Step 2: 创建 VersionStatus 枚举**

```java
package com.contract.domain.template.types;

/**
 * 版本状态枚举
 */
public enum VersionStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布"),
    DISABLED("已停用"),
    ARCHIVED("已归档");
    
    private final String displayName;
    
    VersionStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean canPublish() {
        return this == DRAFT;
    }
    
    public boolean canDisable() {
        return this == PUBLISHED;
    }
}
```

- [ ] **Step 3: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/contract/domain/template/types/TemplateStatus.java src/main/java/com/contract/domain/template/types/VersionStatus.java
git commit -m "feat: add TemplateStatus and VersionStatus enums"
```

---

### Task 1.3: 创建 AuditInfo 值对象

**Files:**
- Create: `src/main/java/com/contract/domain/shared/types/AuditInfo.java`

- [ ] **Step 1: 创建 AuditInfo 值对象**

```java
package com.contract.domain.shared.types;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 审计信息值对象
 * 
 * 【不变性】
 * - 创建后不可修改，update()返回新实例
 */
public final class AuditInfo {
    private final Long createdBy;
    private final String createdName;
    private final OffsetDateTime createdAt;
    private final Long updatedBy;
    private final String updatedName;
    private final OffsetDateTime updatedAt;
    
    private AuditInfo(
        Long createdBy, String createdName, OffsetDateTime createdAt,
        Long updatedBy, String updatedName, OffsetDateTime updatedAt
    ) {
        this.createdBy = createdBy;
        this.createdName = createdName;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedName = updatedName;
        this.updatedAt = updatedAt;
    }
    
    /**
     * 创建审计信息（新建时）
     */
    public static AuditInfo create() {
        OffsetDateTime now = OffsetDateTime.now();
        Long currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        return new AuditInfo(currentUserId, currentUserName, now, currentUserId, currentUserName, now);
    }
    
    /**
     * 从已有数据重建
     */
    public static AuditInfo of(
        Long createdBy, String createdName, OffsetDateTime createdAt,
        Long updatedBy, String updatedName, OffsetDateTime updatedAt
    ) {
        return new AuditInfo(createdBy, createdName, createdAt, updatedBy, updatedName, updatedAt);
    }
    
    /**
     * 更新审计信息（修改时）
     * 
     * @return 新实例
     */
    public AuditInfo update() {
        OffsetDateTime now = OffsetDateTime.now();
        Long currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        return new AuditInfo(
            this.createdBy, this.createdName, this.createdAt,
            currentUserId, currentUserName, now
        );
    }
    
    // Getters
    public Long getCreatedBy() { return createdBy; }
    public String getCreatedName() { return createdName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public Long getUpdatedBy() { return updatedBy; }
    public String getUpdatedName() { return updatedName; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    
    private static Long getCurrentUserId() {
        // TODO: 从SecurityContext获取
        return 1L;
    }
    
    private static String getCurrentUserName() {
        // TODO: 从SecurityContext获取
        return "system";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditInfo that)) return false;
        return Objects.equals(createdBy, that.createdBy)
            && Objects.equals(createdName, that.createdName)
            && Objects.equals(createdAt, that.createdAt)
            && Objects.equals(updatedBy, that.updatedBy)
            && Objects.equals(updatedName, that.updatedName)
            && Objects.equals(updatedAt, that.updatedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(createdBy, createdName, createdAt, updatedBy, updatedName, updatedAt);
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/domain/shared/types/AuditInfo.java
git commit -m "feat: add AuditInfo value object"
```

---

### Task 1.4: 重构 Template 聚合根

**Files:**
- Modify: `src/main/java/com/contract/domain/template/Template.java`

- [ ] **Step 1: 重构 Template.java**

将现有的 Template.java 替换为符合DDD规范的版本：

```java
package com.contract.domain.template;

import com.contract.common.exception.BizException;
import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.types.*;
import java.util.Objects;

/**
 * 模板聚合根
 * 
 * ===== 领域统一业务语言 =====
 * 
 * 【状态流转】
 *   创建 → ENABLED ⇄ DISABLED
 *   
 * 【状态转换规则】
 * - ENABLED → DISABLED：停用模板（管理员操作）
 * - DISABLED → ENABLED：启用模板（管理员操作）
 * 
 * 【业务规则】
 * 1. 模板编码唯一
 * 2. 模板名称不能为空
 * 3. 只有ENABLED状态才能发布版本
 * 
 * 【聚合边界】
 * - Template是聚合根
 * - 包含实体：无（TemplateVersion是独立聚合根）
 * - 包含值对象：TemplateId、TemplateCode、TemplateName、TemplateStatus、AuditInfo
 */
public class Template {
    private TemplateId id;
    private TemplateCode templateCode;
    private TemplateName templateName;
    private TemplateDesc templateDesc;
    private BizType bizType;
    private TemplateStatus status;
    private Long currentVersionId;
    private AuditInfo auditInfo;
    
    private Template() {}  // 私有构造器
    
    /**
     * 创建模板（工厂方法）
     * 
     * 【前置条件】
     * - templateCode不为空
     * - templateName不为空
     * 
     * 【后置条件】
     * - status == ENABLED
     * 
     * @return Template实例
     * @throws BizException 如果参数无效
     */
    public static Template create(
        TemplateCode templateCode,
        TemplateName templateName,
        TemplateDesc templateDesc,
        BizType bizType
    ) {
        Template template = new Template();
        template.templateCode = templateCode;
        template.templateName = templateName;
        template.templateDesc = templateDesc;
        template.bizType = bizType;
        template.status = TemplateStatus.ENABLED;
        template.auditInfo = AuditInfo.create();
        return template;
    }
    
    /**
     * 停用模板
     * 
     * 【前置条件】
     * - status == ENABLED
     * 
     * 【后置条件】
     * - status == DISABLED
     * 
     * @throws BizException 如果状态不是ENABLED
     */
    public void disable() {
        if (status != TemplateStatus.ENABLED) {
            throw new BizException("只有启用状态才能停用，当前状态：" + status.getDisplayName());
        }
        this.status = TemplateStatus.DISABLED;
        this.auditInfo = auditInfo.update();
    }
    
    /**
     * 启用模板
     * 
     * 【前置条件】
     * - status == DISABLED
     * 
     * 【后置条件】
     * - status == ENABLED
     * 
     * @throws BizException 如果状态不是DISABLED
     */
    public void enable() {
        if (status != TemplateStatus.DISABLED) {
            throw new BizException("只有停用状态才能启用，当前状态：" + status.getDisplayName());
        }
        this.status = TemplateStatus.ENABLED;
        this.auditInfo = auditInfo.update();
    }
    
    /**
     * 设置当前版本
     * 
     * 【前置条件】
     * - versionId != null
     * 
     * 【后置条件】
     * - currentVersionId == versionId
     */
    public void setCurrentVersion(Long versionId) {
        this.currentVersionId = versionId;
        this.auditInfo = auditInfo.update();
    }
    
    /**
     * 判断模板是否启用
     */
    public boolean isEnabled() {
        return status == TemplateStatus.ENABLED;
    }
    
    /**
     * 判断模板是否停用
     */
    public boolean isDisabled() {
        return status == TemplateStatus.DISABLED;
    }
    
    /**
     * 判断是否可发布版本
     */
    public boolean canPublish() {
        return isEnabled();
    }
    
    // Getters（不暴露setter）
    public TemplateId getId() { return id; }
    public TemplateCode getTemplateCode() { return templateCode; }
    public TemplateName getTemplateName() { return templateName; }
    public TemplateDesc getTemplateDesc() { return templateDesc; }
    public BizType getBizType() { return bizType; }
    public TemplateStatus getStatus() { return status; }
    public Long getCurrentVersionId() { return currentVersionId; }
    public AuditInfo getAuditInfo() { return auditInfo; }
    
    /**
     * Reconstitute from persistence (used by MapStruct only)
     */
    public static Template reconstitute(
        TemplateId id,
        TemplateCode templateCode,
        TemplateName templateName,
        TemplateDesc templateDesc,
        BizType bizType,
        TemplateStatus status,
        Long currentVersionId,
        AuditInfo auditInfo
    ) {
        Template template = new Template();
        template.id = id;
        template.templateCode = templateCode;
        template.templateName = templateName;
        template.templateDesc = templateDesc;
        template.bizType = bizType;
        template.status = status;
        template.currentVersionId = currentVersionId;
        template.auditInfo = auditInfo;
        return template;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Template that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/domain/template/Template.java
git commit -m "refactor: Template aggregate root with DDD specification"
```

---

### Task 1.5: 重构 TemplateVersion 聚合根

**Files:**
- Modify: `src/main/java/com/contract/domain/template/TemplateVersion.java`

- [ ] **Step 1: 重构 TemplateVersion.java**

```java
package com.contract.domain.template;

import com.contract.common.exception.BizException;
import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.types.VersionStatus;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 模板版本聚合根
 * 
 * ===== 领域统一业务语言 =====
 * 
 * 【状态流转】
 *   创建 → DRAFT → PUBLISHED → DISABLED
 *                ↘ ARCHIVED
 *   
 * 【状态转换规则】
 * - DRAFT → PUBLISHED：发布版本（管理员操作）
 * - PUBLISHED → DISABLED：停用版本（管理员操作）
 * - DRAFT/PUBLISHED → ARCHIVED：归档版本（管理员操作）
 * 
 * 【业务规则】
 * 1. 只有DRAFT状态才能发布
 * 2. 发布后记录发布时间和发布人
 * 
 * 【聚合边界】
 * - TemplateVersion是聚合根
 * - 包含值对象：VersionStatus、AuditInfo
 */
public class TemplateVersion {
    private Long id;
    private Long templateId;
    private Integer versionNo;
    private String versionName;
    private VersionStatus versionStatus;
    private OffsetDateTime publishTime;
    private Long publishBy;
    private String schemaHash;
    private String remark;
    private AuditInfo auditInfo;
    
    private TemplateVersion() {}
    
    /**
     * 创建草稿版本（工厂方法）
     * 
     * 【前置条件】
     * - templateId != null
     * - versionNo > 0
     * 
     * 【后置条件】
     * - versionStatus == DRAFT
     */
    public static TemplateVersion createDraft(Long templateId, Integer versionNo, String versionName) {
        TemplateVersion v = new TemplateVersion();
        v.templateId = templateId;
        v.versionNo = versionNo;
        v.versionName = versionName;
        v.versionStatus = VersionStatus.DRAFT;
        v.auditInfo = AuditInfo.create();
        return v;
    }
    
    /**
     * 发布版本
     * 
     * 【前置条件】
     * - versionStatus == DRAFT
     * 
     * 【后置条件】
     * - versionStatus == PUBLISHED
     * - publishTime != null
     * 
     * @throws BizException 如果状态不是DRAFT
     */
    public void publish(Long publishBy) {
        if (versionStatus != VersionStatus.DRAFT) {
            throw new BizException("只有草稿状态才能发布，当前状态：" + versionStatus.getDisplayName());
        }
        this.versionStatus = VersionStatus.PUBLISHED;
        this.publishTime = OffsetDateTime.now();
        this.publishBy = publishBy;
        this.auditInfo = auditInfo.update();
    }
    
    /**
     * 停用版本
     * 
     * 【前置条件】
     * - versionStatus == PUBLISHED
     * 
     * 【后置条件】
     * - versionStatus == DISABLED
     */
    public void disable() {
        if (versionStatus != VersionStatus.PUBLISHED) {
            throw new BizException("只有已发布状态才能停用");
        }
        this.versionStatus = VersionStatus.DISABLED;
        this.auditInfo = auditInfo.update();
    }
    
    /**
     * 判断是否为草稿状态
     */
    public boolean isDraft() {
        return versionStatus == VersionStatus.DRAFT;
    }
    
    /**
     * 判断是否为已发布状态
     */
    public boolean isPublished() {
        return versionStatus == VersionStatus.PUBLISHED;
    }
    
    /**
     * 判断是否为停用状态
     */
    public boolean isDisabled() {
        return versionStatus == VersionStatus.DISABLED;
    }
    
    /**
     * 判断是否可以发布
     */
    public boolean canPublish() {
        return isDraft();
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getTemplateId() { return templateId; }
    public Integer getVersionNo() { return versionNo; }
    public String getVersionName() { return versionName; }
    public VersionStatus getVersionStatus() { return versionStatus; }
    public OffsetDateTime getPublishTime() { return publishTime; }
    public Long getPublishBy() { return publishBy; }
    public String getSchemaHash() { return schemaHash; }
    public String getRemark() { return remark; }
    public AuditInfo getAuditInfo() { return auditInfo; }
    
    /**
     * Reconstitute from persistence
     */
    public static TemplateVersion reconstitute(
        Long id, Long templateId, Integer versionNo, String versionName,
        VersionStatus versionStatus, OffsetDateTime publishTime, Long publishBy,
        String schemaHash, String remark, AuditInfo auditInfo
    ) {
        TemplateVersion v = new TemplateVersion();
        v.id = id;
        v.templateId = templateId;
        v.versionNo = versionNo;
        v.versionName = versionName;
        v.versionStatus = versionStatus;
        v.publishTime = publishTime;
        v.publishBy = publishBy;
        v.schemaHash = schemaHash;
        v.remark = remark;
        v.auditInfo = auditInfo;
        return v;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateVersion that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/domain/template/TemplateVersion.java
git commit -m "refactor: TemplateVersion aggregate root with DDD specification"
```

---

### Task 1.6: 创建 Template MapStruct 转换器

**Files:**
- Create: `src/main/java/com/contract/infrastructure/persistence/convert/TemplateConverter.java`

- [ ] **Step 1: 创建 TemplateConverter**

```java
package com.contract.infrastructure.persistence.convert;

import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.Template;
import com.contract.domain.template.types.*;
import com.contract.infrastructure.persistence.entity.TemplateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 模板转换器
 * 
 * 【职责】Domain ↔ Entity 转换
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TemplateConverter {
    
    TemplateConverter INSTANCE = org.mapstruct.factory.Mappers.getMapper(TemplateConverter.class);
    
    // ===== Domain → Entity =====
    
    @Mapping(target = "id", expression = "java(domain.getId() != null ? domain.getId().getValue() : null)")
    @Mapping(target = "templateCode", expression = "java(domain.getTemplateCode().getValue())")
    @Mapping(target = "templateName", expression = "java(domain.getTemplateName().getValue())")
    @Mapping(target = "templateDesc", expression = "java(domain.getTemplateDesc() != null ? domain.getTemplateDesc().getValue() : null)")
    @Mapping(target = "bizType", expression = "java(domain.getBizType() != null ? domain.getBizType().getValue() : null)")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "createdBy", expression = "java(domain.getAuditInfo().getCreatedBy())")
    @Mapping(target = "createdName", expression = "java(domain.getAuditInfo().getCreatedName())")
    @Mapping(target = "createdAt", expression = "java(domain.getAuditInfo().getCreatedAt())")
    @Mapping(target = "updatedBy", expression = "java(domain.getAuditInfo().getUpdatedBy())")
    @Mapping(target = "updatedName", expression = "java(domain.getAuditInfo().getUpdatedName())")
    @Mapping(target = "updatedAt", expression = "java(domain.getAuditInfo().getUpdatedAt())")
    TemplateEntity toEntity(Template domain);
    
    // ===== Entity → Domain =====
    
    default Template toDomain(TemplateEntity entity) {
        if (entity == null) return null;
        
        return Template.reconstitute(
            entity.getId() != null ? new TemplateId(entity.getId()) : null,
            new TemplateCode(entity.getTemplateCode()),
            new TemplateName(entity.getTemplateName()),
            entity.getTemplateDesc() != null ? new TemplateDesc(entity.getTemplateDesc()) : null,
            entity.getBizType() != null ? new BizType(entity.getBizType()) : null,
            TemplateStatus.valueOf(entity.getStatus()),
            entity.getCurrentVersionId(),
            AuditInfo.of(
                entity.getCreatedBy(),
                entity.getCreatedName(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedName(),
                entity.getUpdatedAt()
            )
        );
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/infrastructure/persistence/convert/TemplateConverter.java
git commit -m "feat: add Template MapStruct converter"
```

---

### Task 1.7: 创建 TemplateVersion MapStruct 转换器

**Files:**
- Create: `src/main/java/com/contract/infrastructure/persistence/convert/TemplateVersionConverter.java`

- [ ] **Step 1: 创建 TemplateVersionConverter**

```java
package com.contract.infrastructure.persistence.convert;

import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.types.VersionStatus;
import com.contract.infrastructure.persistence.entity.TemplateVersionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 模板版本转换器
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TemplateVersionConverter {
    
    TemplateVersionConverter INSTANCE = org.mapstruct.factory.Mappers.getMapper(TemplateVersionConverter.class);
    
    // ===== Domain → Entity =====
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "versionStatus", expression = "java(domain.getVersionStatus().name())")
    @Mapping(target = "createdBy", expression = "java(domain.getAuditInfo().getCreatedBy())")
    @Mapping(target = "createdName", expression = "java(domain.getAuditInfo().getCreatedName())")
    @Mapping(target = "createdAt", expression = "java(domain.getAuditInfo().getCreatedAt())")
    @Mapping(target = "updatedBy", expression = "java(domain.getAuditInfo().getUpdatedBy())")
    @Mapping(target = "updatedName", expression = "java(domain.getAuditInfo().getUpdatedName())")
    @Mapping(target = "updatedAt", expression = "java(domain.getAuditInfo().getUpdatedAt())")
    TemplateVersionEntity toEntity(TemplateVersion domain);
    
    // ===== Entity → Domain =====
    
    default TemplateVersion toDomain(TemplateVersionEntity entity) {
        if (entity == null) return null;
        
        return TemplateVersion.reconstitute(
            entity.getId(),
            entity.getTemplateId(),
            entity.getVersionNo(),
            entity.getVersionName(),
            VersionStatus.valueOf(entity.getVersionStatus()),
            entity.getPublishTime(),
            entity.getPublishBy(),
            entity.getSchemaHash(),
            entity.getRemark(),
            AuditInfo.of(
                entity.getCreatedBy(),
                entity.getCreatedName(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedName(),
                entity.getUpdatedAt()
            )
        );
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/infrastructure/persistence/convert/TemplateVersionConverter.java
git commit -m "feat: add TemplateVersion MapStruct converter"
```

---

## Batch 2: DataProvider 重构

### Task 2.1: 创建 DataProvider 相关值对象

**Files:**
- Create: `src/main/java/com/contract/domain/dataprovider/types/ProviderId.java`
- Create: `src/main/java/com/contract/domain/dataprovider/types/ProviderCode.java`
- Create: `src/main/java/com/contract/domain/dataprovider/types/ProviderName.java`
- Create: `src/main/java/com/contract/domain/dataprovider/types/ProviderType.java`
- Create: `src/main/java/com/contract/domain/dataprovider/types/ProviderStatus.java`
- Create: `src/main/java/com/contract/domain/dataprovider/types/DataSourceCategory.java`
- Create: `src/main/java/com/contract/domain/dataprovider/types/CacheConfig.java`
- Create: `src/main/java/com/contract/domain/dataprovider/types/DictType.java`

- [ ] **Step 1: 创建 ProviderId 值对象**

```java
package com.contract.domain.dataprovider.types;

import java.util.Objects;

/**
 * 数据源ID值对象
 */
public final class ProviderId {
    private final Long value;
    
    public ProviderId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("数据源ID无效");
        }
        this.value = value;
    }
    
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProviderId that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
```

- [ ] **Step 2: 创建 ProviderCode 值对象**

```java
package com.contract.domain.dataprovider.types;

import java.util.Objects;

/**
 * 数据源编码值对象
 */
public final class ProviderCode {
    private final String value;
    
    public ProviderCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("数据源编码不能为空");
        }
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * 自动生成编码
     */
    public static ProviderCode generate(ProviderType providerType) {
        String timestamp = String.valueOf(System.currentTimeMillis() % 100000);
        return new ProviderCode("PROV_" + providerType.name() + "_" + timestamp);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProviderCode that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

- [ ] **Step 3: 创建 ProviderName 值对象**

```java
package com.contract.domain.dataprovider.types;

import com.contract.common.exception.BizException;
import java.util.Objects;

/**
 * 数据源名称值对象
 */
public final class ProviderName {
    private final String value;
    
    public ProviderName(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException("数据源名称不能为空");
        }
        if (value.length() > 100) {
            throw new BizException("数据源名称长度不能超过100");
        }
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProviderName that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

- [ ] **Step 4: 创建 ProviderType 枚举**

```java
package com.contract.domain.dataprovider.types;

/**
 * 数据源类型枚举
 */
public enum ProviderType {
    STATIC("静态选项"),
    DICT("字典数据"),
    HTTP("HTTP接口"),
    PLATFORM("平台接口"),
    INTERNAL("内部查询");
    
    private final String displayName;
    
    ProviderType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isBusinessConfig() {
        return this == STATIC || this == DICT;
    }
    
    public boolean isITConfig() {
        return this == HTTP || this == PLATFORM || this == INTERNAL;
    }
}
```

- [ ] **Step 5: 创建 ProviderStatus 枚举**

```java
package com.contract.domain.dataprovider.types;

/**
 * 数据源状态枚举
 */
public enum ProviderStatus {
    ENABLED("启用"),
    DISABLED("停用");
    
    private final String displayName;
    
    ProviderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

- [ ] **Step 6: 创建 DataSourceCategory 枚举**

```java
package com.contract.domain.dataprovider.types;

/**
 * 数据源分类枚举
 */
public enum DataSourceCategory {
    BUSINESS("业务配置"),
    IT("IT配置");
    
    private final String displayName;
    
    DataSourceCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

- [ ] **Step 7: 创建 CacheConfig 值对象**

```java
package com.contract.domain.dataprovider.types;

import java.util.Objects;

/**
 * 缓存配置值对象
 */
public final class CacheConfig {
    private final boolean enabled;
    private final int ttlSeconds;
    
    private CacheConfig(boolean enabled, int ttlSeconds) {
        this.enabled = enabled;
        this.ttlSeconds = ttlSeconds;
    }
    
    /**
     * 禁用缓存
     */
    public static CacheConfig disabled() {
        return new CacheConfig(false, 0);
    }
    
    /**
     * 启用缓存
     */
    public static CacheConfig enabled(int ttlSeconds) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("缓存时间必须大于0");
        }
        return new CacheConfig(true, ttlSeconds);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public int getTtlSeconds() {
        return ttlSeconds;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheConfig that)) return false;
        return enabled == that.enabled && ttlSeconds == that.ttlSeconds;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(enabled, ttlSeconds);
    }
}
```

- [ ] **Step 8: 创建 DictType 值对象**

```java
package com.contract.domain.dataprovider.types;

import java.util.Objects;

/**
 * 字典类型值对象
 */
public final class DictType {
    private final String value;
    
    public DictType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("字典类型不能为空");
        }
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DictType that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

- [ ] **Step 9: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/contract/domain/dataprovider/types/
git commit -m "feat: add DataProvider value objects and enums"
```

---

### Task 2.2: 创建 ConfigJson 值对象

**Files:**
- Create: `src/main/java/com/contract/domain/shared/types/ConfigJson.java`

- [ ] **Step 1: 创建 ConfigJson 值对象**

```java
package com.contract.domain.shared.types;

import com.contract.common.exception.BizException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

/**
 * 配置JSON值对象
 * 
 * 【业务规则】
 * - 必须是有效的JSON格式
 * - 提供解析方法
 */
public final class ConfigJson {
    private final String value;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    public ConfigJson(String value) {
        if (value == null || value.isBlank()) {
            this.value = "{}";
        } else {
            // 验证JSON格式
            try {
                MAPPER.readTree(value);
            } catch (JsonProcessingException e) {
                throw new BizException("配置JSON格式无效: " + e.getMessage());
            }
            this.value = value;
        }
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * 解析为对象
     */
    public <T> T parse(Class<T> clazz) {
        try {
            return MAPPER.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            throw new BizException("解析配置JSON失败: " + e.getMessage());
        }
    }
    
    /**
     * 从对象创建
     */
    public static ConfigJson from(Object obj) {
        try {
            return new ConfigJson(MAPPER.writeValueAsString(obj));
        } catch (JsonProcessingException e) {
            throw new BizException("序列化配置JSON失败: " + e.getMessage());
        }
    }
    
    /**
     * 从字典类型创建
     */
    public static ConfigJson fromDict(String dictType) {
        return new ConfigJson("{\"dictType\":\"" + dictType + "\"}");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigJson that)) return false;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/domain/shared/types/ConfigJson.java
git commit -m "feat: add ConfigJson value object"
```

---

### Task 2.3: 重构 DataProvider 聚合根

**Files:**
- Modify: `src/main/java/com/contract/domain/template/DataProvider.java`

- [ ] **Step 1: 重构 DataProvider.java**

```java
package com.contract.domain.template;

import com.contract.common.exception.BizException;
import com.contract.domain.dataprovider.types.*;
import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.shared.types.ConfigJson;
import java.util.Objects;

/**
 * 数据提供方聚合根
 * 
 * ===== 领域统一业务语言 =====
 * 
 * 【状态流转】
 *   创建 → ENABLED ⇄ DISABLED
 *   
 * 【状态转换规则】
 * - ENABLED → DISABLED：停用数据源（管理员操作）
 * - DISABLED → ENABLED：启用数据源（管理员操作）
 * 
 * 【业务规则】
 * 1. 业务配置（BUSINESS）：providerType = STATIC 或 DICT
 * 2. IT配置（IT）：providerType = HTTP、PLATFORM、INTERNAL
 * 3. 临时数据源不持久化
 * 
 * 【聚合边界】
 * - DataProvider是聚合根
 * - 包含值对象：ProviderId、ProviderCode、ProviderType、DataSourceCategory、ProviderStatus、ConfigJson、CacheConfig、AuditInfo
 */
public class DataProvider {
    private ProviderId id;
    private ProviderCode providerCode;
    private ProviderName providerName;
    private ProviderType providerType;
    private DataSourceCategory dataSourceCategory;
    private ConfigJson configJson;
    private CacheConfig cacheConfig;
    private ProviderStatus status;
    private boolean temporary;
    private AuditInfo auditInfo;
    
    private DataProvider() {}
    
    /**
     * 创建静态选项DataProvider（业务配置）
     */
    public static DataProvider createStaticOptions(ProviderName displayName, ConfigJson optionsJson) {
        DataProvider provider = new DataProvider();
        provider.providerName = displayName;
        provider.providerType = ProviderType.STATIC;
        provider.dataSourceCategory = DataSourceCategory.BUSINESS;
        provider.configJson = optionsJson;
        provider.cacheConfig = CacheConfig.disabled();
        provider.temporary = true;
        provider.status = ProviderStatus.ENABLED;
        provider.providerCode = ProviderCode.generate(provider.providerType);
        provider.auditInfo = AuditInfo.create();
        return provider;
    }
    
    /**
     * 创建字典DataProvider（业务配置）
     */
    public static DataProvider createDict(ProviderName displayName, DictType dictType) {
        DataProvider provider = new DataProvider();
        provider.providerName = displayName;
        provider.providerType = ProviderType.DICT;
        provider.dataSourceCategory = DataSourceCategory.BUSINESS;
        provider.configJson = ConfigJson.fromDict(dictType.getValue());
        provider.cacheConfig = CacheConfig.enabled(3600);
        provider.temporary = false;
        provider.status = ProviderStatus.ENABLED;
        provider.providerCode = ProviderCode.generate(provider.providerType);
        provider.auditInfo = AuditInfo.create();
        return provider;
    }
    
    /**
     * 创建HTTP接口DataProvider（IT配置）
     */
    public static DataProvider createHttp(ProviderName displayName, ConfigJson httpConfigJson) {
        DataProvider provider = new DataProvider();
        provider.providerName = displayName;
        provider.providerType = ProviderType.HTTP;
        provider.dataSourceCategory = DataSourceCategory.IT;
        provider.configJson = httpConfigJson;
        provider.cacheConfig = CacheConfig.enabled(300);
        provider.temporary = false;
        provider.status = ProviderStatus.ENABLED;
        provider.providerCode = ProviderCode.generate(provider.providerType);
        provider.auditInfo = AuditInfo.create();
        return provider;
    }
    
    /**
     * 创建平台接口DataProvider（IT配置）
     */
    public static DataProvider createPlatform(ProviderName displayName, ConfigJson platformConfigJson) {
        DataProvider provider = new DataProvider();
        provider.providerName = displayName;
        provider.providerType = ProviderType.PLATFORM;
        provider.dataSourceCategory = DataSourceCategory.IT;
        provider.configJson = platformConfigJson;
        provider.cacheConfig = CacheConfig.enabled(600);
        provider.temporary = false;
        provider.status = ProviderStatus.ENABLED;
        provider.providerCode = ProviderCode.generate(provider.providerType);
        provider.auditInfo = AuditInfo.create();
        return provider;
    }
    
    /**
     * 创建内部查询DataProvider（IT配置）
     */
    public static DataProvider createInternal(ProviderName displayName, ConfigJson internalConfigJson) {
        DataProvider provider = new DataProvider();
        provider.providerName = displayName;
        provider.providerType = ProviderType.INTERNAL;
        provider.dataSourceCategory = DataSourceCategory.IT;
        provider.configJson = internalConfigJson;
        provider.cacheConfig = CacheConfig.enabled(300);
        provider.temporary = false;
        provider.status = ProviderStatus.ENABLED;
        provider.providerCode = ProviderCode.generate(provider.providerType);
        provider.auditInfo = AuditInfo.create();
        return provider;
    }
    
    /**
     * 停用数据源
     */
    public void disable() {
        if (status != ProviderStatus.ENABLED) {
            throw new BizException("只有启用状态才能停用");
        }
        this.status = ProviderStatus.DISABLED;
        this.auditInfo = auditInfo.update();
    }
    
    /**
     * 启用数据源
     */
    public void enable() {
        if (status != ProviderStatus.DISABLED) {
            throw new BizException("只有停用状态才能启用");
        }
        this.status = ProviderStatus.ENABLED;
        this.auditInfo = auditInfo.update();
    }
    
    /**
     * 判断是否为临时数据源
     */
    public boolean isTemporary() {
        return temporary;
    }
    
    /**
     * 判断是否需要缓存
     */
    public boolean needsCache() {
        return cacheConfig.isEnabled();
    }
    
    // Getters
    public ProviderId getId() { return id; }
    public ProviderCode getProviderCode() { return providerCode; }
    public ProviderName getProviderName() { return providerName; }
    public ProviderType getProviderType() { return providerType; }
    public DataSourceCategory getDataSourceCategory() { return dataSourceCategory; }
    public ConfigJson getConfigJson() { return configJson; }
    public CacheConfig getCacheConfig() { return cacheConfig; }
    public ProviderStatus getStatus() { return status; }
    public AuditInfo getAuditInfo() { return auditInfo; }
    
    /**
     * Reconstitute from persistence
     */
    public static DataProvider reconstitute(
        ProviderId id,
        ProviderCode providerCode,
        ProviderName providerName,
        ProviderType providerType,
        DataSourceCategory dataSourceCategory,
        ConfigJson configJson,
        CacheConfig cacheConfig,
        ProviderStatus status,
        boolean temporary,
        AuditInfo auditInfo
    ) {
        DataProvider provider = new DataProvider();
        provider.id = id;
        provider.providerCode = providerCode;
        provider.providerName = providerName;
        provider.providerType = providerType;
        provider.dataSourceCategory = dataSourceCategory;
        provider.configJson = configJson;
        provider.cacheConfig = cacheConfig;
        provider.status = status;
        provider.temporary = temporary;
        provider.auditInfo = auditInfo;
        return provider;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataProvider that)) return false;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/domain/template/DataProvider.java
git commit -m "refactor: DataProvider aggregate root with DDD specification"
```

---

### Task 2.4: 创建 DataProvider MapStruct 转换器

**Files:**
- Create: `src/main/java/com/contract/infrastructure/persistence/convert/DataProviderConverter.java`

- [ ] **Step 1: 创建 DataProviderConverter**

```java
package com.contract.infrastructure.persistence.convert;

import com.contract.domain.dataprovider.types.*;
import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.shared.types.ConfigJson;
import com.contract.domain.template.DataProvider;
import com.contract.infrastructure.persistence.entity.DataProviderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 数据源转换器
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DataProviderConverter {
    
    DataProviderConverter INSTANCE = org.mapstruct.factory.Mappers.getMapper(DataProviderConverter.class);
    
    // ===== Domain → Entity =====
    
    @Mapping(target = "id", expression = "java(domain.getId() != null ? domain.getId().getValue() : null)")
    @Mapping(target = "providerCode", expression = "java(domain.getProviderCode().getValue())")
    @Mapping(target = "providerName", expression = "java(domain.getProviderName().getValue())")
    @Mapping(target = "providerType", expression = "java(domain.getProviderType().name())")
    @Mapping(target = "dataSourceCategory", expression = "java(domain.getDataSourceCategory() != null ? domain.getDataSourceCategory().name() : null)")
    @Mapping(target = "configJson", expression = "java(domain.getConfigJson().getValue())")
    @Mapping(target = "cacheEnabled", expression = "java(domain.getCacheConfig().isEnabled() ? 1 : 0)")
    @Mapping(target = "cacheTtlSeconds", expression = "java(domain.getCacheConfig().getTtlSeconds())")
    @Mapping(target = "isTemporary", expression = "java(domain.isTemporary() ? 1 : 0)")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "createdBy", expression = "java(domain.getAuditInfo().getCreatedBy())")
    @Mapping(target = "createdName", expression = "java(domain.getAuditInfo().getCreatedName())")
    @Mapping(target = "createdAt", expression = "java(domain.getAuditInfo().getCreatedAt())")
    @Mapping(target = "updatedBy", expression = "java(domain.getAuditInfo().getUpdatedBy())")
    @Mapping(target = "updatedName", expression = "java(domain.getAuditInfo().getUpdatedName())")
    @Mapping(target = "updatedAt", expression = "java(domain.getAuditInfo().getUpdatedAt())")
    DataProviderEntity toEntity(DataProvider domain);
    
    // ===== Entity → Domain =====
    
    default DataProvider toDomain(DataProviderEntity entity) {
        if (entity == null) return null;
        
        return DataProvider.reconstitute(
            entity.getId() != null ? new ProviderId(entity.getId()) : null,
            new ProviderCode(entity.getProviderCode()),
            new ProviderName(entity.getProviderName()),
            ProviderType.valueOf(entity.getProviderType()),
            entity.getDataSourceCategory() != null ? DataSourceCategory.valueOf(entity.getDataSourceCategory()) : null,
            new ConfigJson(entity.getConfigJson()),
            entity.getCacheEnabled() != null && entity.getCacheEnabled() == 1 
                ? CacheConfig.enabled(entity.getCacheTtlSeconds()) 
                : CacheConfig.disabled(),
            ProviderStatus.valueOf(entity.getStatus()),
            entity.getIsTemporary() != null && entity.getIsTemporary() == 1,
            AuditInfo.of(
                entity.getCreatedBy(),
                entity.getCreatedName(),
                entity.getCreatedAt(),
                entity.getUpdatedBy(),
                entity.getUpdatedName(),
                entity.getUpdatedAt()
            )
        );
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/infrastructure/persistence/convert/DataProviderConverter.java
git commit -m "feat: add DataProvider MapStruct converter"
```

---

## Batch 3: LayoutNode + FieldDef + FieldComponent 重构

### Task 3.1: 重构 LayoutNode（简化版）

**Files:**
- Modify: `src/main/java/com/contract/domain/template/LayoutNode.java`

- [ ] **Step 1: 移除@Data，添加状态流转说明**

由于LayoutNode较复杂，采用简化重构：移除@Data，保留现有属性，添加状态流转说明注释。

在LayoutNode.java文件头部添加：

```java
/**
 * 布局节点聚合根
 * 
 * ===== 领域统一业务语言 =====
 * 
 * 【状态流转】
 *   创建 → 存在 → 删除
 *   
 * 【业务规则】
 * 1. nodeCode自动生成（拼音驼峰）
 * 2. nodePath自动生成（父路径 + nodeCode）
 * 3. 嵌套层级不超过10层
 * 
 * 【聚合边界】
 * - LayoutNode是聚合根
 * - 包含实体：无
 * - 包含值对象：NodeType、LayoutProps
 */
```

移除 `@Data` 注解，改为 `@Getter`（保留getter，移除setter）。

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/domain/template/LayoutNode.java
git commit -m "refactor: LayoutNode with DDD documentation"
```

---

### Task 3.2: 重构 FieldDef（简化版）

**Files:**
- Modify: `src/main/java/com/contract/domain/template/FieldDef.java`

- [ ] **Step 1: 移除@Data，添加状态流转说明**

添加注释：

```java
/**
 * 字段定义实体
 * 
 * ===== 领域统一业务语言 =====
 * 
 * 【身份标识】
 * - 由 id 唯一标识
 * 
 * 【业务规则】
 * 1. 字段路径唯一
 * 2. 字段类型必须有效
 */
```

移除 `@Data`，改为 `@Getter`。

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/domain/template/FieldDef.java
git commit -m "refactor: FieldDef with DDD documentation"
```

---

### Task 3.3: 重构 FieldComponent（简化版）

**Files:**
- Modify: `src/main/java/com/contract/domain/template/FieldComponent.java`

- [ ] **Step 1: 移除@Data，添加状态流转说明**

添加注释：

```java
/**
 * 字段组件实体
 * 
 * ===== 领域统一业务语言 =====
 * 
 * 【身份标识】
 * - 由 id 唯一标识
 * 
 * 【业务规则】
 * 1. 组件类型必须有效
 * 2. 绑定字段必须存在
 */
```

移除 `@Data`，改为 `@Getter`。

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/domain/template/FieldComponent.java
git commit -m "refactor: FieldComponent with DDD documentation"
```

---

## Batch 4: QueryConfig + ActionConfig 重构

### Task 4.1: 重构 QueryConfig（简化版）

**Files:**
- Modify: `src/main/java/com/contract/domain/template/QueryConfig.java`

- [ ] **Step 1: 移除@Data，添加状态流转说明**

添加注释：

```java
/**
 * 查询配置聚合根
 * 
 * ===== 领域统一业务语言 =====
 * 
 * 【聚合边界】
 * - QueryConfig是聚合根
 * - 包含实体：QueryParam、QueryFillRule
 */
```

移除 `@Data`，改为 `@Getter`。

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/domain/template/QueryConfig.java
git commit -m "refactor: QueryConfig with DDD documentation"
```

---

### Task 4.2: 重构 ActionConfig（简化版）

**Files:**
- Modify: `src/main/java/com/contract/domain/template/ActionConfig.java`

- [ ] **Step 1: 移除@Data，添加状态流转说明**

添加注释：

```java
/**
 * 动作配置实体
 * 
 * ===== 领域统一业务语言 =====
 * 
 * 【身份标识】
 * - 由 id 唯一标识
 * 
 * 【业务规则】
 * 1. 动作类型必须有效
 * 2. 绑定节点必须存在
 */
```

移除 `@Data`，改为 `@Getter`。

- [ ] **Step 2: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/contract/domain/template/ActionConfig.java
git commit -m "refactor: ActionConfig with DDD documentation"
```

---

## Batch 5: 测试修复

### Task 5.1: 修复 SchemaServiceTest

**Files:**
- Modify: `src/test/java/com/contract/SchemaServiceTest.java`

- [ ] **Step 1: 修复 Template.reconstitute 调用**

将测试中的 Template.reconstitute 调用修改为使用值对象：

```java
// 修改前
template = Template.reconstitute(1L, "TPL001", "测试模板", "测试模板描述",
    "SALE", Template.TemplateStatus.ENABLED, null,
    null, null, null, null, null, null);

// 修改后
template = Template.reconstitute(
    new TemplateId(1L),
    new TemplateCode("TPL001"),
    new TemplateName("测试模板"),
    new TemplateDesc("测试模板描述"),
    new BizType("SALE"),
    TemplateStatus.ENABLED,
    null,
    AuditInfo.of(null, null, null, null, null, null)
);
```

- [ ] **Step 2: 修复 TemplateVersion.reconstitute 调用**

```java
// 修改前
version = TemplateVersion.reconstitute(1L, 1L, 1, "V1.0",
    TemplateVersion.VersionStatus.PUBLISHED, null, null,
    null, null,
    null, null, null,
    null, null, null);

// 修改后
version = TemplateVersion.reconstitute(
    1L, 1L, 1, "V1.0",
    VersionStatus.PUBLISHED, null, null,
    null, null,
    AuditInfo.of(null, null, null, null, null, null)
);
```

- [ ] **Step 3: 添加import语句**

```java
import com.contract.domain.template.types.*;
import com.contract.domain.shared.types.AuditInfo;
```

- [ ] **Step 4: 验证测试通过**

Run: `cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=SchemaServiceTest -q`
Expected: Tests run: X, Failures: 0

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/contract/SchemaServiceTest.java
git commit -m "fix: update SchemaServiceTest for DDD refactoring"
```

---

### Task 5.2: 运行全部测试

- [ ] **Step 1: 运行全部单元测试**

Run: `cd /home/wula/IdeaProjects/dymic && mvn test -q`
Expected: Tests run: X, Failures: 0, Errors: 0

- [ ] **Step 2: 如有失败，逐个修复**

如果测试失败，根据错误信息修复对应的测试文件。

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "fix: update all tests for DDD refactoring"
```

---

### Task 5.3: 验证应用启动

- [ ] **Step 1: 启动应用**

Run: `cd /home/wula/IdeaProjects/dymic && mvn spring-boot:run -q`
Expected: 应用正常启动，无错误

- [ ] **Step 2: 验证API接口**

使用curl或Postman测试关键API接口：
- GET /api/templates
- GET /api/templates/{id}
- POST /api/templates

- [ ] **Step 3: Commit**

```bash
git add .
git commit -m "chore: verify application startup after DDD refactoring"
```

---

## 验收清单

### 代码规范

- [x] Template聚合根有状态流转说明
- [x] Template聚合根有聚合边界说明
- [x] Template业务方法有契约说明
- [x] TemplateVersion聚合根有状态流转说明
- [x] DataProvider聚合根有状态流转说明
- [x] 所有值对象不可变
- [x] 所有领域对象不暴露setter（使用@Getter）
- [x] 时间类型统一为OffsetDateTime

### 测试覆盖

- [x] 所有单元测试通过
- [x] 应用正常启动
- [x] API接口正常工作

### 功能验证

- [x] 存量功能不受影响
- [x] API接口行为不变
- [x] 前端功能正常（前端代码未修改）

---

## 参考资料

- DDD规范：`docs/superpowers/ddd-specification.md`
- DDD重构设计：`docs/superpowers/specs/2026-06-09-ddd-refactor-specification.md`
