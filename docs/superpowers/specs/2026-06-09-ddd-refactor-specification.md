# DDD 规范重构设计

> **版本**：V1.0
> **日期**：2026-06-09
> **目标**：将存量代码重构为符合标准DDD规范的充血模型

---

## 一、现状分析

### 1.1 存量代码问题

| 问题 | 说明 | 影响 |
|------|------|------|
| **缺少值对象** | 所有属性都是基本类型（String、Long等），没有封装 | 业务规则分散，无法复用 |
| **缺少状态流转说明** | 领域对象没有状态流转文档 | 业务规则不清晰 |
| **缺少业务方法契约** | 业务方法没有前置/后置条件说明 | 边界条件不明确 |
| **暴露setter** | 使用@Data注解，暴露所有setter | 破坏封装性，违反充血模型 |
| **缺少聚合边界说明** | 没有说明聚合根包含哪些实体/值对象 | 聚合边界不清晰 |
| **时间类型不一致** | 部分用LocalDateTime，部分用OffsetDateTime | 时区处理混乱 |

### 1.2 示例：DataProvider 问题

```java
// ❌ 当前代码：不符合DDD规范
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataProvider {
    private Long id;
    private String providerCode;
    private String providerName;
    private String providerType;     // 应该是值对象
    private String configJson;       // 应该是值对象
    private Integer cacheEnabled;    // 应该是值对象
    private Integer cacheTtlSeconds;
    private Integer isTemporary;
    private String status;           // 应该是值对象
    // ...
    
    // 问题：
    // 1. 所有属性都可set，破坏封装
    // 2. 无状态流转说明
    // 3. 无业务方法契约
    // 4. providerType、status等应该是值对象
}
```

---

## 二、重构目标

### 2.1 符合DDD规范

| 规范项 | 要求 |
|--------|------|
| **聚合根** | 私有构造器 + 工厂方法 + 业务方法 + 状态流转说明 + 聚合边界说明 |
| **实体** | 唯一标识 + equals/hashCode由ID决定 + 业务方法在实体内部 |
| **值对象** | 不可变 + 无setter + 运算返回新实例 + equals/hashCode由属性值决定 |
| **领域服务** | 无状态 + 方法名表达业务含义 + 操作多个聚合 |
| **Repository** | 接口在领域层 + 实现在基础设施层 + 按聚合根操作 |
| **MapStruct** | Domain ↔ Entity 转换，避免手写reconstitute |

### 2.2 时间类型统一

- **统一使用**：`OffsetDateTime`（带时区）
- **原因**：业务系统跨时区，需要明确的时区信息

### 2.3 禁止使用@Data

- **原因**：@Data暴露setter，破坏充血模型封装性
- **替代**：
  - 聚合根/实体：手写getter，不暴露setter
  - DTO：@Getter @Setter @Builder
  - 值对象：final属性 + getter

---

## 三、重构范围

### 3.1 领域对象清单

| 领域对象 | 类型 | 重构内容 |
|---------|------|---------|
| Template | 聚合根 | 添加状态流转说明、业务方法契约、值对象封装 |
| TemplateVersion | 聚合根 | 添加状态流转说明、业务方法契约 |
| LayoutNode | 聚合根 | 添加聚合边界说明、值对象封装 |
| FieldDef | 实体 | 添加业务方法、equals/hashCode |
| FieldComponent | 实体 | 添加业务方法、equals/hashCode |
| DataProvider | 聚合根 | 添加状态流转说明、值对象封装 |
| QueryConfig | 聚合根 | 添加聚合边界说明 |
| ActionConfig | 实体 | 添加业务方法 |

### 3.2 值对象清单

| 值对象 | 封装内容 | 位置 |
|--------|---------|------|
| TemplateId | Long id | domain.template.types |
| TemplateCode | String code（带校验） | domain.template.types |
| TemplateStatus | 枚举：ENABLED/DISABLED | domain.template.types |
| VersionStatus | 枚举：DRAFT/PUBLISHED/DISABLED/ARCHIVED | domain.template.types |
| ProviderType | 枚举：STATIC/DICT/HTTP/PLATFORM/INTERNAL | domain.dataprovider.types |
| ProviderStatus | 枚举：ENABLED/DISABLED | domain.dataprovider.types |
| DataSourceCategory | 枚举：BUSINESS/IT | domain.dataprovider.types |
| ConfigJson | String json（带解析方法） | domain.shared.types |
| NodeType | 枚举：CARD/TAB/COLLAPSE/GRID/FIELD | domain.template.types |

---

## 四、重构设计

### 4.1 Template 聚合根重构

**重构前**：
```java
public class Template {
    private Long id;
    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String bizType;
    private TemplateStatus status;  // 枚举，但无状态流转说明
    // ...
}
```

**重构后**：
```java
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
 * - 包含值对象：TemplateId、TemplateCode、TemplateStatus
 */
public class Template {
    private TemplateId id;                    // 值对象
    private TemplateCode templateCode;        // 值对象
    private TemplateName templateName;        // 值对象
    private TemplateDesc templateDesc;        // 值对象
    private BizType bizType;                  // 值对象
    private TemplateStatus status;            // 值对象（枚举）
    private VersionId currentVersionId;       // 值对象
    private AuditInfo auditInfo;              // 值对象（审计信息）
    
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
     * - id != null
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
        // 校验逻辑在值对象构造器中
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
    public void setCurrentVersion(VersionId versionId) {
        this.currentVersionId = versionId;
        this.auditInfo = auditInfo.update();
    }
    
    // Getters（不暴露setter）
    public TemplateId getId() { return id; }
    public TemplateCode getTemplateCode() { return templateCode; }
    public TemplateName getTemplateName() { return templateName; }
    public TemplateDesc getTemplateDesc() { return templateDesc; }
    public BizType getBizType() { return bizType; }
    public TemplateStatus getStatus() { return status; }
    public VersionId getCurrentVersionId() { return currentVersionId; }
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
        VersionId currentVersionId,
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

### 4.2 DataProvider 聚合根重构

**重构前**：
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataProvider {
    private Long id;
    private String providerCode;
    private String providerType;     // String，应该是枚举
    private String configJson;       // String，应该是值对象
    private String status;           // String，应该是枚举
    // ...
}
```

**重构后**：
```java
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
 * - 包含值对象：ProviderType、DataSourceCategory、ProviderStatus、ConfigJson
 */
public class DataProvider {
    private ProviderId id;                       // 值对象
    private ProviderCode providerCode;           // 值对象
    private ProviderName providerName;           // 值对象
    private ProviderType providerType;           // 值对象（枚举）
    private DataSourceCategory dataSourceCategory; // 值对象（枚举）
    private ConfigJson configJson;               // 值对象
    private CacheConfig cacheConfig;             // 值对象
    private ProviderStatus status;               // 值对象（枚举）
    private boolean temporary;                   // 临时数据源标志
    private AuditInfo auditInfo;                 // 值对象
    
    private DataProvider() {}  // 私有构造器
    
    /**
     * 创建静态选项DataProvider（业务配置）
     * 
     * 【前置条件】
     * - displayName不为空
     * - optionsJson格式正确
     * 
     * 【后置条件】
     * - providerType == STATIC
     * - dataSourceCategory == BUSINESS
     * - status == ENABLED
     * 
     * @return DataProvider实例
     */
    public static DataProvider createStaticOptions(
        ProviderName displayName,
        ConfigJson optionsJson
    ) {
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
    public static DataProvider createDict(
        ProviderName displayName,
        DictType dictType
    ) {
        DataProvider provider = new DataProvider();
        provider.providerName = displayName;
        provider.providerType = ProviderType.DICT;
        provider.dataSourceCategory = DataSourceCategory.BUSINESS;
        provider.configJson = ConfigJson.fromDict(dictType);
        provider.cacheConfig = CacheConfig.enabled(3600);  // 缓存1小时
        provider.temporary = false;
        provider.status = ProviderStatus.ENABLED;
        provider.providerCode = ProviderCode.generate(provider.providerType);
        provider.auditInfo = AuditInfo.create();
        return provider;
    }
    
    /**
     * 创建HTTP接口DataProvider（IT配置）
     */
    public static DataProvider createHttp(
        ProviderName displayName,
        ConfigJson httpConfigJson
    ) {
        DataProvider provider = new DataProvider();
        provider.providerName = displayName;
        provider.providerType = ProviderType.HTTP;
        provider.dataSourceCategory = DataSourceCategory.IT;
        provider.configJson = httpConfigJson;
        provider.cacheConfig = CacheConfig.enabled(300);  // 缓存5分钟
        provider.temporary = false;
        provider.status = ProviderStatus.ENABLED;
        provider.providerCode = ProviderCode.generate(provider.providerType);
        provider.auditInfo = AuditInfo.create();
        return provider;
    }
    
    /**
     * 停用数据源
     * 
     * 【前置条件】
     * - status == ENABLED
     * 
     * 【后置条件】
     * - status == DISABLED
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
    
    // Getters
    public ProviderId getId() { return id; }
    public ProviderCode getProviderCode() { return providerCode; }
    public ProviderName getProviderName() { return providerName; }
    public ProviderType getProviderType() { return providerType; }
    public DataSourceCategory getDataSourceCategory() { return dataSourceCategory; }
    public ConfigJson getConfigJson() { return configJson; }
    public CacheConfig getCacheConfig() { return cacheConfig; }
    public ProviderStatus getStatus() { return status; }
    public boolean isTemporary() { return temporary; }
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

### 4.3 值对象设计

#### 4.3.1 TemplateCode 值对象

```java
/**
 * 模板编码值对象
 * 
 * 【业务规则】
 * - 不能为空
 * - 长度1-50
 * - 只能包含字母、数字、下划线
 * - 唯一性由Repository保证
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

#### 4.3.2 ConfigJson 值对象

```java
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
    public static ConfigJson fromDict(DictType dictType) {
        return new ConfigJson("{\"dictType\":\"" + dictType.getValue() + "\"}");
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

#### 4.3.3 AuditInfo 值对象

```java
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
        // 实际应用中从上下文获取当前用户
        Long currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        return new AuditInfo(currentUserId, currentUserName, now, currentUserId, currentUserName, now);
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
    
    // 从上下文获取当前用户（示例）
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

### 4.4 MapStruct 转换器

```java
/**
 * 模板转换器（基础设施层）
 * 
 * 【职责】Domain ↔ Entity 转换
 * 【位置】infrastructure.convert包
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TemplateConverter {
    
    TemplateConverter INSTANCE = Mappers.getMapper(TemplateConverter.class);
    
    // ===== Domain → Entity =====
    
    @Mapping(target = "id", expression = "java(domain.getId() != null ? domain.getId().getValue() : null)")
    @Mapping(target = "templateCode", expression = "java(domain.getTemplateCode().getValue())")
    @Mapping(target = "templateName", expression = "java(domain.getTemplateName().getValue())")
    @Mapping(target = "templateDesc", expression = "java(domain.getTemplateDesc() != null ? domain.getTemplateDesc().getValue() : null)")
    @Mapping(target = "bizType", expression = "java(domain.getBizType() != null ? domain.getBizType().getValue() : null)")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "currentVersionId", expression = "java(domain.getCurrentVersionId() != null ? domain.getCurrentVersionId().getValue() : null)")
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
            new TemplateId(entity.getId()),
            new TemplateCode(entity.getTemplateCode()),
            new TemplateName(entity.getTemplateName()),
            entity.getTemplateDesc() != null ? new TemplateDesc(entity.getTemplateDesc()) : null,
            entity.getBizType() != null ? new BizType(entity.getBizType()) : null,
            TemplateStatus.valueOf(entity.getStatus()),
            entity.getCurrentVersionId() != null ? new VersionId(entity.getCurrentVersionId()) : null,
            new AuditInfo(
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

---

## 五、实施策略

### 5.1 分批重构

| 批次 | 范围 | 预计工作量 |
|------|------|-----------|
| **Batch 1** | Template + TemplateVersion | 2天 |
| **Batch 2** | DataProvider | 1天 |
| **Batch 3** | LayoutNode + FieldDef + FieldComponent | 2天 |
| **Batch 4** | QueryConfig + ActionConfig | 1天 |
| **Batch 5** | 测试修复 + 文档完善 | 1天 |

### 5.2 重构步骤（每个领域对象）

1. **创建值对象**：将基本类型封装为值对象
2. **重构领域对象**：添加状态流转说明、业务方法契约
3. **修改MapStruct**：更新转换器映射逻辑
4. **修改RepositoryImpl**：使用MapStruct转换
5. **修改Service**：调整调用方式
6. **修复测试**：更新测试用例

### 5.3 不修改的部分

- **Controller层**：不改，参数仍然是DTO
- **前端**：不改，API接口不变
- **数据库表结构**：不改，只是字段映射逻辑变化

---

## 六、验收标准

### 6.1 代码规范

- [ ] 所有聚合根有状态流转说明
- [ ] 所有聚合根有聚合边界说明
- [ ] 所有业务方法有契约说明
- [ ] 所有值对象不可变
- [ ] 所有领域对象不暴露setter
- [ ] 时间类型统一为OffsetDateTime

### 6.2 测试覆盖

- [ ] 所有领域对象有单元测试
- [ ] 所有业务方法有测试用例
- [ ] 所有值对象有测试用例
- [ ] MapStruct转换器有测试用例

### 6.3 功能验证

- [ ] 存量功能不受影响
- [ ] API接口行为不变
- [ ] 前端功能正常

---

## 七、参考资料

- DDD规范：`docs/superpowers/ddd-specification.md`
- 数据源配置设计：`docs/superpowers/specs/2026-06-09-data-source-unified-design.md`
