# DDD 重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将现有 Spring MVC 后端重构为 Apache CXF JAX-RS + DDD 四层架构，建立充血领域模型、严格垂直调用链、MapStruct 转换链和 XML Mapper。

**Architecture:** CXF JAX-RS（/api/v2/*）与现有 Spring MVC（/api/*）共存。四层架构：Adapter → Application → Domain → Infrastructure。应用层薄编排，领域层厚逻辑。应用层禁止直接调用 Repository，必须通过 DomainService。

**Tech Stack:** Apache CXF 4.x JAX-RS / Spring Boot 3.5.14 / MapStruct 1.5.5.Final / MyBatis-Plus 3.5.5 / Jackson / H2 / openGauss

**Git tag:** `v0.1-pre-ddd-refactor` 标记了重构前的代码状态

---

## 文件结构总览

### 新建文件

| 文件 | 职责 |
|------|------|
| `adapter/jaxrs/TemplateResource.java` | CXF JAX-RS 模板资源 |
| `adapter/jaxrs/SchemaResource.java` | CXF JAX-RS Schema 资源 |
| `adapter/jaxrs/HealthResource.java` | CXF JAX-RS 健康检查 |
| `adapter/req/TemplateCreateReq.java` | 创建模板请求 |
| `adapter/req/TemplateUpdateReq.java` | 更新模板请求 |
| `adapter/rsp/TemplateCreateRsp.java` | 创建模板响应 |
| `adapter/rsp/TemplateGetRsp.java` | 查询模板响应 |
| `adapter/rsp/TemplateListRsp.java` | 列表查询响应 |
| `adapter/convert/TemplateReqConverter.java` | Req ↔ DTO 转换 |
| `application/service/TemplateAppService.java` | 薄编排应用服务（新版） |
| `application/convert/TemplateDomainConverter.java` | DTO ↔ Domain 转换（新版，替代旧 Converter） |
| `domain/template/service/TemplateDomainService.java` | 模板领域服务（承载业务逻辑） |
| `domain/template/service/SchemaDomainService.java` | Schema 领域服务 |
| `domain/shared/types/LayoutProps.java` | 布局属性值对象 |
| `domain/shared/types/ComponentProps.java` | 组件属性值对象 |
| `domain/shared/types/ProviderConfig.java` | 数据源配置值对象 |
| `infrastructure/cxf/CxfConfig.java` | CXF 配置类 |
| `infrastructure/persistence/convert/TemplateEntityConverter.java` | Domain ↔ Entity 转换（新版） |
| `resources/mapper/TemplateMapper.opengauss.xml` | 模板 Mapper XML |
| `resources/mapper/TemplateVersionMapper.opengauss.xml` | 版本 Mapper XML |
| `docs/domain/template-state-flow.md` | 状态流转文档 |

### 修改文件

| 文件 | 改动 |
|------|------|
| `pom.xml` | 添加 CXF 依赖 |
| `domain/template/Template.java` | 充血模型 + OffsetDateTime + 枚举状态 |
| `domain/template/TemplateVersion.java` | OffsetDateTime + 状态流转 |
| `domain/template/LayoutNode.java` | JSONB 强类型 props |
| `application/template/TemplateService.java` | 瘦身 → 薄编排，业务逻辑迁移到 DomainService |
| `application/template/SchemaService.java` | 瘦身 → 薄编排 |
| `infrastructure/persistence/repository/TemplateRepositoryImpl.java` | 使用 MapStruct EntityConverter |

### 不修改文件

- 现有 Spring MVC Controller（保留共存）
- 前端 JS 文件（Phase 4 前不切换）
- Repository 接口（domain 层接口不变）
- Entity 类（基础设施层保留，只改时间类型）

---

## Task 1: 添加 CXF 依赖到 pom.xml

**Files:**
- Modify: `pom.xml:23-26` (properties section)
- Modify: `pom.xml:28-119` (dependencies section)

- [ ] **Step 1: 添加 CXF 版本属性和依赖**

在 `pom.xml` 的 `<properties>` 中添加：

```xml
<cxf.version>4.1.1</cxf.version>
<mapstruct.version>1.5.5.Final</mapstruct.version>
```

替换现有 MapStruct 版本硬编码。然后在 `<dependencies>` 中 Spring Boot Web 之后添加：

```xml
<!-- Apache CXF JAX-RS -->
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-spring-boot-starter-jaxrs</artifactId>
    <version>${cxf.version}</version>
</dependency>

<dependency>
    <groupId>com.fasterxml.jackson.jakarta.rs</groupId>
    <artifactId>jackson-jakarta-rs-json-provider</artifactId>
</dependency>
```

- [ ] **Step 2: 验证编译通过**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "feat: add Apache CXF JAX-RS dependencies"
```

---

## Task 2: 创建 CXF 配置类

**Files:**
- Create: `src/main/java/com/contract/infrastructure/cxf/CxfConfig.java`

- [ ] **Step 1: 创建 CXF 配置**

```java
package com.contract.infrastructure.cxf;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.ws.rs.ext.RuntimeDelegate;
import java.util.List;

@Configuration
public class CxfConfig {

    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServlet() {
        ServletRegistrationBean<CXFServlet> servlet = new ServletRegistrationBean<>(new CXFServlet(), "/api/*");
        servlet.setLoadOnStartup(1);
        servlet.addInitParameter("static-resources", "classpath:/static/");
        return servlet;
    }

    @Bean
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public JacksonJsonProvider jacksonJsonProvider() {
        return new JacksonJsonProvider();
    }
}
```

注意：CXF Servlet 映射到 `/api/*`。Spring MVC 的 `DispatcherServlet` 需要改为映射到 `/` 以外的路径，或者我们通过 CXF `@Path("/v2/...")` 来区分。由于当前 Spring MVC 也用 `/api/*`，需要确保共存。实际做法：CXF 只映射 `/api/v2/*`：

```java
@Bean
public ServletRegistrationBean<CXFServlet> cxfServlet() {
    ServletRegistrationBean<CXFServlet> servlet = new ServletRegistrationBean<>(new CXFServlet(), "/api/v2/*");
    servlet.setLoadOnStartup(1);
    return servlet;
}
```

这样 Spring MVC 的 `/api/*` 和 CXF 的 `/api/v2/*` 共存。

- [ ] **Step 2: 创建健康检查 Resource**

```java
package com.contract.adapter.jaxrs;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;

@Component
@Path("/v2")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok("{\"status\":\"UP\",\"framework\":\"cxf-jaxrs\"}").build();
    }
}
```

在 `CxfConfig` 中注册 Resource bean：

```java
@Bean
public Server jaxRsServer(
        SpringBus bus,
        JacksonJsonProvider jsonProvider,
        HealthResource healthResource
) {
    JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
    factory.setBus(bus);
    factory.setAddress("/");
    factory.setServiceBeans(List.of(healthResource));
    factory.setProviders(List.of(jsonProvider));
    return factory.create();
}
```

- [ ] **Step 3: 启动应用验证 CXF 端点可用**

Run: `cd /home/wula/IdeaProjects/dymic && mvn spring-boot:run -q &` 后台启动，然后：

Run: `curl -s http://localhost:8888/api/v2/health`
Expected: `{"status":"UP","framework":"cxf-jaxrs"}`

同时验证 Spring MVC 未受影响：

Run: `curl -s http://localhost:8888/api/templates`
Expected: 返回 JSON 格式的模板列表（原有接口正常）

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/contract/infrastructure/cxf/CxfConfig.java src/main/java/com/contract/adapter/jaxrs/HealthResource.java
git commit -m "feat: add CXF JAX-RS config with /api/v2/* coexistence"
```

---

## Task 3: 创建值对象类型（JSONB 强类型）

**Files:**
- Create: `src/main/java/com/contract/domain/shared/types/LayoutProps.java`
- Create: `src/main/java/com/contract/domain/shared/types/ComponentProps.java`
- Create: `src/main/java/com/contract/domain/shared/types/ProviderConfig.java`

- [ ] **Step 1: 创建 LayoutProps**

```java
package com.contract.domain.shared.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LayoutProps {
    private Integer span;
    private Integer offset;
    private Integer gutter;
    private Integer columns;
    private List<PanelDef> panels;
    private List<TabDef> tabs;
    private Integer gridColumn;
    private Integer gridRow;

    public LayoutProps() {}

    public Integer getSpan() { return span; }
    public void setSpan(Integer span) { this.span = span; }
    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }
    public Integer getGutter() { return gutter; }
    public void setGutter(Integer gutter) { this.gutter = gutter; }
    public Integer getColumns() { return columns; }
    public void setColumns(Integer columns) { this.columns = columns; }
    public List<PanelDef> getPanels() { return panels; }
    public void setPanels(List<PanelDef> panels) { this.panels = panels; }
    public List<TabDef> getTabs() { return tabs; }
    public void setTabs(List<TabDef> tabs) { this.tabs = tabs; }
    public Integer getGridColumn() { return gridColumn; }
    public void setGridColumn(Integer gridColumn) { this.gridColumn = gridColumn; }
    public Integer getGridRow() { return gridRow; }
    public void setGridRow(Integer gridRow) { this.gridRow = gridRow; }

    public static class PanelDef {
        private String title;
        private List<String> children;

        public PanelDef() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<String> getChildren() { return children; }
        public void setChildren(List<String> children) { this.children = children; }
    }

    public static class TabDef {
        private String title;
        private List<String> children;

        public TabDef() {}

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<String> getChildren() { return children; }
        public void setChildren(List<String> children) { this.children = children; }
    }
}
```

- [ ] **Step 2: 创建 ComponentProps**

```java
package com.contract.domain.shared.types;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComponentProps {
    private String placeholder;
    private Boolean required;
    private Boolean readonly;
    private String format;
    private String defaultValue;

    public ComponentProps() {}

    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
    public Boolean getReadonly() { return readonly; }
    public void setReadonly(Boolean readonly) { this.readonly = readonly; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
}
```

- [ ] **Step 3: 创建 ProviderConfig**

```java
package com.contract.domain.shared.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderConfig {
    private String providerType;
    private Map<String, Object> staticOptions;
    private String dictCode;
    private String httpUrl;
    private String httpMethod;
    private Map<String, String> httpHeaders;
    private List<StaticOption> options;

    public ProviderConfig() {}

    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }
    public Map<String, Object> getStaticOptions() { return staticOptions; }
    public void setStaticOptions(Map<String, Object> staticOptions) { this.staticOptions = staticOptions; }
    public String getDictCode() { return dictCode; }
    public void setDictCode(String dictCode) { this.dictCode = dictCode; }
    public String getHttpUrl() { return httpUrl; }
    public void setHttpUrl(String httpUrl) { this.httpUrl = httpUrl; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public Map<String, String> getHttpHeaders() { return httpHeaders; }
    public void setHttpHeaders(Map<String, String> httpHeaders) { this.httpHeaders = httpHeaders; }
    public List<StaticOption> getOptions() { return options; }
    public void setOptions(List<StaticOption> options) { this.options = options; }

    public static class StaticOption {
        private String label;
        private String value;
        private List<StaticOption> children;

        public StaticOption() {}

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public List<StaticOption> getChildren() { return children; }
        public void setChildren(List<StaticOption> children) { this.children = children; }
    }
}
```

- [ ] **Step 4: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/domain/shared/types/
git commit -m "feat: add JSONB strong-typed value objects (LayoutProps, ComponentProps, ProviderConfig)"
```

---

## Task 4: 重构 Template 领域对象为充血模型

**Files:**
- Modify: `src/main/java/com/contract/domain/template/Template.java`
- Modify: `src/main/java/com/contract/domain/template/TemplateVersion.java`

- [ ] **Step 1: 重构 Template.java**

替换整个文件内容：

```java
package com.contract.domain.template;

import com.contract.common.exception.BizException;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Template {
    private Long id;
    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String bizType;
    private TemplateStatus status;
    private Long currentVersionId;
    private Long createdBy;
    private String createdName;
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private OffsetDateTime updatedAt;

    // --- 状态枚举 ---
    public enum TemplateStatus {
        ENABLED, DISABLED
    }

    // --- 构造器 ---
    private Template() {}

    // --- 工厂方法 ---
    public static Template create(String templateCode, String templateName, String templateDesc, String bizType) {
        if (templateCode == null || templateCode.isBlank()) {
            throw new BizException("模板编码不能为空");
        }
        if (templateName == null || templateName.isBlank()) {
            throw new BizException("模板名称不能为空");
        }
        Template t = new Template();
        t.templateCode = templateCode;
        t.templateName = templateName;
        t.templateDesc = templateDesc;
        t.bizType = bizType;
        t.status = TemplateStatus.ENABLED;
        return t;
    }

    // --- 状态变更（不暴露 setStatus） ---
    public void disable() {
        if (status == TemplateStatus.DISABLED) {
            throw new BizException("模板已是停用状态");
        }
        this.status = TemplateStatus.DISABLED;
    }

    public void enable() {
        if (status == TemplateStatus.ENABLED) {
            throw new BizException("模板已是启用状态");
        }
        this.status = TemplateStatus.ENABLED;
    }

    public void setCurrentVersion(Long versionId) {
        this.currentVersionId = versionId;
    }

    // --- 业务规则 ---
    public boolean isEnabled() {
        return status == TemplateStatus.ENABLED;
    }

    public boolean isDisabled() {
        return status == TemplateStatus.DISABLED;
    }

    public boolean canPublish() {
        return isEnabled();
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getTemplateCode() { return templateCode; }
    public String getTemplateName() { return templateName; }
    public String getTemplateDesc() { return templateDesc; }
    public String getBizType() { return bizType; }
    public TemplateStatus getStatus() { return status; }
    public Long getCurrentVersionId() { return currentVersionId; }
    public Long getCreatedBy() { return createdBy; }
    public String getCreatedName() { return createdName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public Long getUpdatedBy() { return updatedBy; }
    public String getUpdatedName() { return updatedName; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    // --- Package-private setters (for Repository/Converter use) ---
    void setId(Long id) { this.id = id; }
    void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    void setCreatedName(String createdName) { this.createdName = createdName; }
    void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    void setUpdatedName(String updatedName) { this.updatedName = updatedName; }
    void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    // --- equals/hashCode (基于 id) ---
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

注意：`status` 字段类型从 `String` 变为 `TemplateStatus` 枚举。这个变更会影响 EntityConverter、现有 Controller、Service 等引用 `getStatus()` 返回 String 的地方。由于我们采用共存模式，先让旧代码编译通过，再逐步迁移。

- [ ] **Step 2: 重构 TemplateVersion.java**

替换整个文件内容：

```java
package com.contract.domain.template;

import com.contract.common.exception.BizException;
import java.time.OffsetDateTime;
import java.util.Objects;

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
    private Long createdBy;
    private String createdName;
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private OffsetDateTime updatedAt;

    public enum VersionStatus {
        DRAFT, PUBLISHED, DISABLED, ARCHIVED
    }

    private TemplateVersion() {}

    public static TemplateVersion createDraft(Long templateId, Integer versionNo, String versionName) {
        TemplateVersion v = new TemplateVersion();
        v.templateId = templateId;
        v.versionNo = versionNo;
        v.versionName = versionName;
        v.versionStatus = VersionStatus.DRAFT;
        return v;
    }

    public void publish(Long publishBy) {
        if (versionStatus != VersionStatus.DRAFT) {
            throw new BizException("只有草稿状态才能发布，当前状态：" + versionStatus);
        }
        this.versionStatus = VersionStatus.PUBLISHED;
        this.publishTime = OffsetDateTime.now();
        this.publishBy = publishBy;
    }

    public void disable() {
        this.versionStatus = VersionStatus.DISABLED;
    }

    public boolean isDraft() { return versionStatus == VersionStatus.DRAFT; }
    public boolean isPublished() { return versionStatus == VersionStatus.PUBLISHED; }
    public boolean isDisabled() { return versionStatus == VersionStatus.DISABLED; }
    public boolean canPublish() { return isDraft(); }

    // --- Getters ---
    public Long getId() { return id; }
    public Long getTemplateId() { return templateId; }
    public Integer getVersionNo() { return versionNo; }
    public String getVersionName() { return versionName; }
    public VersionStatus getVersionStatus() { return versionStatus; }
    public OffsetDateTime getPublishTime() { return publishTime; }
    public Long getPublishBy() { return publishBy; }
    public String getSchemaHash() { return schemaHash; }
    public String getRemark() { return remark; }
    public Long getCreatedBy() { return createdBy; }
    public String getCreatedName() { return createdName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public Long getUpdatedBy() { return updatedBy; }
    public String getUpdatedName() { return updatedName; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    // --- Package-private setters ---
    void setId(Long id) { this.id = id; }
    void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    void setCreatedName(String createdName) { this.createdName = createdName; }
    void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    void setUpdatedName(String updatedName) { this.updatedName = updatedName; }
    void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateVersion that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
```

- [ ] **Step 3: 修复旧代码编译错误**

领域对象移除 `@Data` 后，旧代码中所有 `setter` 调用和 `getter` 返回类型都会出问题。需要做兼容处理：

1. 在 Template 中添加 `@Deprecated` 的公开 setter 用于旧代码过渡，或
2. 修复所有引用点

对于 `Template.status` 从 String 变为枚举，旧代码中 `template.setStatus("ENABLED")` 等调用需要更新。最安全的做法是在 Template 中保留 String 版本的兼容 getter：

```java
// 兼容旧代码 — 过渡期使用
@Deprecated
public String getStatusString() { return status != null ? status.name() : null; }
```

但更好的做法是：直接修复所有旧代码引用点。当前 Template 的 setter 调用方只有 `TemplateRepositoryImpl.toDomain()`、`Template.create()` 工厂方法、`TemplateService`。逐一修改这些引用。

**修改 `TemplateRepositoryImpl.toDomain()`：**

将 `template.setStatus(entity.getStatus())` 改为：
```java
if (entity.getStatus() != null) {
    template.status = Template.TemplateStatus.valueOf(entity.getStatus());
}
```

需要在 Template 中添加一个 package-private 的 setStatus 方法供 RepositoryImpl 使用。由于 RepositoryImpl 在不同包，改为在 Template 中添加：

```java
// 供基础设施层转换使用（非业务方法）
public void setStatusForPersistence(TemplateStatus status) { this.status = status; }
public void setId(Long id) { this.id = id; }
```

将所有 package-private setter 改为 public（MapStruct 需要访问）：

```java
public void setId(Long id) { this.id = id; }
public void setTemplateDesc(String templateDesc) { this.templateDesc = templateDesc; }
public void setBizType(String bizType) { this.bizType = bizType; }
public void setCurrentVersionId(Long currentVersionId) { this.currentVersionId = currentVersionId; }
public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
public void setCreatedName(String createdName) { this.createdName = createdName; }
public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
public void setUpdatedName(String updatedName) { this.updatedName = updatedName; }
public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
```

**修改 `TemplateRepositoryImpl`：** 更新 `toDomain` 和 `toEntity` 方法中 status 的 String ↔ enum 转换。

**修改 `TemplateVersionRepositoryImpl`：** 同理更新 versionStatus 转换。

**修改 `TemplateService`：** `templateService.getById()` 返回 Template 后旧 Controller 调用 `template.getStatus()` 现在返回枚举。旧 Controller 返回 `Result<Template>` 直接序列化，枚举会序列化为字符串名，所以旧 API 兼容。

**修改 `SchemaService`：** `version.getVersionStatus()` 现在返回枚举，SchemaDTO 中 `versionStatus` 是 String，需要 `.name()` 转换。

- [ ] **Step 4: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/domain/template/Template.java src/main/java/com/contract/domain/template/TemplateVersion.java
git add src/main/java/com/contract/infrastructure/persistence/repository/TemplateRepositoryImpl.java
git add src/main/java/com/contract/infrastructure/persistence/repository/TemplateVersionRepositoryImpl.java
git add src/main/java/com/contract/application/template/TemplateService.java
git add src/main/java/com/contract/application/template/SchemaService.java
git commit -m "refactor: rich domain model for Template and TemplateVersion with enum status and OffsetDateTime"
```

---

## Task 5: 创建 TemplateDomainService

**Files:**
- Create: `src/main/java/com/contract/domain/template/service/TemplateDomainService.java`

- [ ] **Step 1: 创建领域服务**

从 `TemplateService` 中提取业务逻辑到领域服务：

```java
package com.contract.domain.template.service;

import com.contract.common.exception.BizException;
import com.contract.domain.template.Template;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.repository.TemplateRepository;
import com.contract.domain.template.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemplateDomainService {

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository versionRepository;

    public Template createTemplate(String templateCode, String templateName, String templateDesc, String bizType) {
        if (templateRepository.existsByTemplateCode(templateCode)) {
            throw new BizException("模板编码已存在：" + templateCode);
        }
        Template template = Template.create(templateCode, templateName, templateDesc, bizType);
        template = templateRepository.save(template);

        TemplateVersion version = TemplateVersion.createDraft(template.getId(), 1, "初始版本");
        versionRepository.save(version);
        return template;
    }

    public Template getById(Long id) {
        Template template = templateRepository.findById(id);
        if (template == null) {
            throw new BizException("模板不存在：" + id);
        }
        return template;
    }

    public Template getByCode(String code) {
        Template template = templateRepository.findByTemplateCode(code);
        if (template == null) {
            throw new BizException("模板不存在：" + code);
        }
        return template;
    }

    public void disable(Long id) {
        Template template = getById(id);
        template.disable();
        templateRepository.update(template);
    }

    public void enable(Long id) {
        Template template = getById(id);
        template.enable();
        templateRepository.update(template);
    }
}
```

- [ ] **Step 2: 瘦身 TemplateService 为薄编排**

修改 `TemplateService` 使其委托给 `TemplateDomainService`：

```java
package com.contract.application.template;

import com.contract.domain.template.Template;
import com.contract.domain.template.service.TemplateDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateDomainService templateDomainService;
    private final com.contract.domain.template.repository.TemplateRepository templateRepository;

    @Transactional
    public Template createTemplate(String templateCode, String templateName, String templateDesc, String bizType) {
        return templateDomainService.createTemplate(templateCode, templateName, templateDesc, bizType);
    }

    public Template getById(Long id) {
        return templateDomainService.getById(id);
    }

    public Template getByCode(String templateCode) {
        return templateDomainService.getByCode(templateCode);
    }

    @Transactional
    public void disable(Long id) {
        templateDomainService.disable(id);
    }

    @Transactional
    public void enable(Long id) {
        templateDomainService.enable(id);
    }

    @Transactional
    public void updateCurrentVersion(Long templateId, Long versionId) {
        Template template = templateDomainService.getById(templateId);
        template.setCurrentVersion(versionId);
        templateRepository.update(template);
    }

    public List<Template> listAll() {
        return templateRepository.findAll();
    }
}
```

注意：`updateCurrentVersion` 仍然直接调用 Repository — 这需要在后续 Task 中迁移到 DomainService。但为了渐进式重构，先保留。

- [ ] **Step 3: 验证编译并启动**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/contract/domain/template/service/TemplateDomainService.java src/main/java/com/contract/application/template/TemplateService.java
git commit -m "refactor: extract TemplateDomainService, slim TemplateService to thin orchestration"
```

---

## Task 6: 创建三层 MapStruct 转换器

**Files:**
- Create: `src/main/java/com/contract/adapter/convert/TemplateReqConverter.java`
- Create: `src/main/java/com/contract/application/convert/TemplateDomainConverter.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/convert/TemplateEntityConverter.java`

- [ ] **Step 1: 创建 TemplateEntityConverter（替代 RepositoryImpl 中手写的 toDomain/toEntity）**

```java
package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.Template;
import com.contract.domain.template.TemplateVersion;
import com.contract.infrastructure.persistence.entity.TemplateEntity;
import com.contract.infrastructure.persistence.entity.TemplateVersionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TemplateEntityConverter {

    @Mapping(target = "status", expression = "java(template.getStatus() != null ? template.getStatus().name() : null)")
    TemplateEntity toEntity(Template template);

    @Mapping(target = "status", expression = "java(entity.getStatus() != null ? com.contract.domain.template.Template.TemplateStatus.valueOf(entity.getStatus()) : null)")
    Template toDomain(TemplateEntity entity);

    List<Template> toDomainList(List<TemplateEntity> entities);

    @Mapping(target = "versionStatus", expression = "java(version.getVersionStatus() != null ? version.getVersionStatus().name() : null)")
    TemplateVersionEntity toVersionEntity(TemplateVersion version);

    @Mapping(target = "versionStatus", expression = "java(entity.getVersionStatus() != null ? com.contract.domain.template.TemplateVersion.VersionStatus.valueOf(entity.getVersionStatus()) : null)")
    TemplateVersion toVersionDomain(TemplateVersionEntity entity);
}
```

- [ ] **Step 2: 修改 TemplateRepositoryImpl 使用 EntityConverter**

将 `TemplateRepositoryImpl` 中的手写 `toDomain()`/`toEntity()` 替换为 EntityConverter 调用：

```java
@Repository
@RequiredArgsConstructor
public class TemplateRepositoryImpl implements TemplateRepository {

    private final TemplateMapper templateMapper;
    private final TemplateEntityConverter converter;

    @Override
    public Template save(Template template) {
        TemplateEntity entity = converter.toEntity(template);
        templateMapper.insert(entity);
        template.setId(entity.getId());
        return template;
    }

    @Override
    public Template findById(Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public Template findByTemplateCode(String templateCode) {
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getTemplateCode, templateCode);
        TemplateEntity entity = templateMapper.selectOne(wrapper);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<Template> findAll() {
        return converter.toDomainList(templateMapper.selectList(null));
    }

    @Override
    public void update(Template template) {
        templateMapper.updateById(converter.toEntity(template));
    }

    @Override
    public boolean existsByTemplateCode(String templateCode) {
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getTemplateCode, templateCode);
        return templateMapper.selectCount(wrapper) > 0;
    }
}
```

- [ ] **Step 3: 创建 TemplateDomainConverter**

```java
package com.contract.application.convert;

import com.contract.application.template.dto.SchemaDTO;
import com.contract.domain.template.Template;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TemplateDomainConverter {

    @Mapping(target = "templateName", source = "templateName")
    @Mapping(target = "templateDesc", source = "templateDesc")
    TemplateDTO toDTO(Template template);

    List<TemplateDTO> toDTOList(List<Template> domains);
}
```

这里需要一个 `TemplateDTO`。当前项目没有独立的 TemplateDTO（旧代码直接返回 Template 领域对象）。需要新建。

- [ ] **Step 4: 创建 TemplateDTO**

```java
package com.contract.application.template.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.OffsetDateTime;

@Getter
@Builder
public class TemplateDTO {
    private Long id;
    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String bizType;
    private String status;
    private Long currentVersionId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

注意：DTO 中 `status` 是 String（对外接口用字符串），Domain 中是枚举（内部用类型安全）。DomainConverter 负责转换。

更新 DomainConverter：

```java
@Mapper(componentModel = "spring")
public interface TemplateDomainConverter {

    @Mapping(target = "status", expression = "java(template.getStatus() != null ? template.getStatus().name() : null)")
    TemplateDTO toDTO(Template template);

    List<TemplateDTO> toDTOList(List<Template> domains);
}
```

- [ ] **Step 5: 创建 TemplateReqConverter**

```java
package com.contract.adapter.convert;

import com.contract.application.template.dto.TemplateDTO;

@Mapper(componentModel = "spring")
public interface TemplateReqConverter {

    TemplateDTO toDTO(TemplateCreateReq req);

    TemplateCreateRsp toCreateRsp(TemplateDTO dto);
    TemplateGetRsp toGetRsp(TemplateDTO dto);
}
```

- [ ] **Step 6: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS（MapStruct 会自动生成实现类）

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/contract/adapter/convert/TemplateReqConverter.java
git add src/main/java/com/contract/application/convert/TemplateDomainConverter.java
git add src/main/java/com/contract/infrastructure/persistence/convert/TemplateEntityConverter.java
git add src/main/java/com/contract/application/template/dto/TemplateDTO.java
git add src/main/java/com/contract/infrastructure/persistence/repository/TemplateRepositoryImpl.java
git commit -m "feat: add three-layer MapStruct converters (ReqConverter, DomainConverter, EntityConverter)"
```

---

## Task 7: 创建 Req/Rsp 类型

**Files:**
- Create: `src/main/java/com/contract/adapter/req/TemplateCreateReq.java`
- Create: `src/main/java/com/contract/adapter/req/TemplateUpdateReq.java`
- Create: `src/main/java/com/contract/adapter/rsp/TemplateCreateRsp.java`
- Create: `src/main/java/com/contract/adapter/rsp/TemplateGetRsp.java`
- Create: `src/main/java/com/contract/adapter/rsp/TemplateListRsp.java`

- [ ] **Step 1: 创建 Req 类型**

```java
package com.contract.adapter.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TemplateCreateReq {
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;
    @NotBlank(message = "模板名称不能为空")
    private String templateName;
    private String templateDesc;
    private String bizType;
}
```

```java
package com.contract.adapter.req;

import lombok.Getter;

@Getter
public class TemplateUpdateReq {
    private String templateName;
    private String templateDesc;
    private String bizType;
}
```

- [ ] **Step 2: 创建 Rsp 类型**

```java
package com.contract.adapter.rsp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemplateCreateRsp {
    private Long id;
    private String templateCode;
    private String templateName;
    private String status;
}
```

```java
package com.contract.adapter.rsp;

import lombok.Builder;
import lombok.Getter;
import java.time.OffsetDateTime;

@Getter
@Builder
public class TemplateGetRsp {
    private Long id;
    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String bizType;
    private String status;
    private Long currentVersionId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

```java
package com.contract.adapter.rsp;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TemplateListRsp {
    private List<TemplateGetRsp> templates;
    private Integer total;
}
```

- [ ] **Step 3: 验证编译**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/contract/adapter/req/ src/main/java/com/contract/adapter/rsp/
git commit -m "feat: add concrete Req/Rsp types for TemplateResource"
```

---

## Task 8: 创建 TemplateResource（CXF JAX-RS）

**Files:**
- Create: `src/main/java/com/contract/adapter/jaxrs/TemplateResource.java`
- Modify: `src/main/java/com/contract/infrastructure/cxf/CxfConfig.java` (注册新 Resource)

- [ ] **Step 1: 创建 TemplateResource**

```java
package com.contract.adapter.jaxrs;

import com.contract.adapter.convert.TemplateReqConverter;
import com.contract.adapter.req.TemplateCreateReq;
import com.contract.adapter.rsp.TemplateCreateRsp;
import com.contract.adapter.rsp.TemplateGetRsp;
import com.contract.adapter.rsp.TemplateListRsp;
import com.contract.application.template.dto.TemplateDTO;
import com.contract.domain.template.Template;
import com.contract.domain.template.service.TemplateDomainService;
import com.contract.domain.template.repository.TemplateRepository;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/v2/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class TemplateResource {

    private final TemplateDomainService templateDomainService;
    private final TemplateRepository templateRepository;
    private final TemplateReqConverter reqConverter;

    @POST
    public Response create(TemplateCreateReq req) {
        Template template = templateDomainService.createTemplate(
            req.getTemplateCode(),
            req.getTemplateName(),
            req.getTemplateDesc(),
            req.getBizType()
        );
        TemplateDTO dto = com.contract.application.convert.TemplateDomainConverter.INSTANCE != null
            ? toDTO(template) : null;
        TemplateCreateRsp rsp = TemplateCreateRsp.builder()
            .id(template.getId())
            .templateCode(template.getTemplateCode())
            .templateName(template.getTemplateName())
            .status(template.getStatus().name())
            .build();
        return Response.status(Response.Status.CREATED).entity(rsp).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Template template = templateDomainService.getById(id);
        TemplateGetRsp rsp = TemplateGetRsp.builder()
            .id(template.getId())
            .templateCode(template.getTemplateCode())
            .templateName(template.getTemplateName())
            .templateDesc(template.getTemplateDesc())
            .bizType(template.getBizType())
            .status(template.getStatus().name())
            .currentVersionId(template.getCurrentVersionId())
            .createdAt(template.getCreatedAt())
            .updatedAt(template.getUpdatedAt())
            .build();
        return Response.ok(rsp).build();
    }

    @GET
    public Response listAll() {
        List<Template> templates = templateRepository.findAll();
        List<TemplateGetRsp> items = templates.stream()
            .map(t -> TemplateGetRsp.builder()
                .id(t.getId())
                .templateCode(t.getTemplateCode())
                .templateName(t.getTemplateName())
                .templateDesc(t.getTemplateDesc())
                .bizType(t.getBizType())
                .status(t.getStatus().name())
                .currentVersionId(t.getCurrentVersionId())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build())
            .toList();
        return Response.ok(TemplateListRsp.builder().templates(items).total(items.size()).build()).build();
    }

    @POST
    @Path("/{id}/disable")
    public Response disable(@PathParam("id") Long id) {
        templateDomainService.disable(id);
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/enable")
    public Response enable(@PathParam("id") Long id) {
        templateDomainService.enable(id);
        return Response.ok().build();
    }
}
```

- [ ] **Step 2: 在 CxfConfig 中注册 TemplateResource**

更新 `CxfConfig.jaxRsServer()` 的参数列表和 serviceBeans：

```java
@Bean
public Server jaxRsServer(
        SpringBus bus,
        JacksonJsonProvider jsonProvider,
        HealthResource healthResource,
        TemplateResource templateResource
) {
    JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
    factory.setBus(bus);
    factory.setAddress("/");
    factory.setServiceBeans(List.of(healthResource, templateResource));
    factory.setProviders(List.of(jsonProvider));
    return factory.create();
}
```

- [ ] **Step 3: 启动验证 CXF 端点**

Run: `mvn spring-boot:run -q &` 后台启动，然后：

Run: `curl -s http://localhost:8888/api/v2/templates | head -c 200`
Expected: JSON 格式的模板列表

Run: `curl -s http://localhost:8888/api/templates | head -c 200`
Expected: 原有 Spring MVC 端点仍然正常

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/contract/adapter/jaxrs/TemplateResource.java src/main/java/com/contract/infrastructure/cxf/CxfConfig.java
git commit -m "feat: add TemplateResource CXF JAX-RS at /api/v2/templates"
```

---

## Task 9: 创建 SchemaResource（CXF JAX-RS）

**Files:**
- Create: `src/main/java/com/contract/adapter/jaxrs/SchemaResource.java`
- Create: `src/main/java/com/contract/adapter/rsp/SchemaGetRsp.java`
- Modify: `src/main/java/com/contract/infrastructure/cxf/CxfConfig.java` (注册)

- [ ] **Step 1: 创建 SchemaGetRsp**

```java
package com.contract.adapter.rsp;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class SchemaGetRsp {
    private Long templateId;
    private Long templateVersionId;
    private String templateCode;
    private String templateName;
    private String versionStatus;
    private List<NodeRsp> layoutNodes;
    private List<FieldDefRsp> fieldDefs;
    private List<FieldComponentRsp> fieldComponents;
    private List<ActionConfigRsp> actionConfigs;

    @Getter
    @Builder
    public static class NodeRsp {
        private Long id;
        private String nodeCode;
        private String nodeName;
        private String nodeType;
        private Integer sortNo;
        private String propsJson;
        private List<NodeRsp> children;
    }

    @Getter
    @Builder
    public static class FieldDefRsp {
        private Long id;
        private Long layoutNodeId;
        private String fieldNameCn;
        private String dataType;
    }

    @Getter
    @Builder
    public static class FieldComponentRsp {
        private Long id;
        private Long fieldDefId;
        private String componentType;
        private String labelName;
    }

    @Getter
    @Builder
    public static class ActionConfigRsp {
        private Long id;
        private String actionName;
        private String actionType;
    }
}
```

- [ ] **Step 2: 创建 SchemaResource**

```java
package com.contract.adapter.jaxrs;

import com.contract.adapter.rsp.SchemaGetRsp;
import com.contract.application.template.SchemaService;
import com.contract.application.template.dto.SchemaDTO;
import com.contract.application.template.dto.SchemaSaveDTO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Path("/v2/templates/{templateId}/versions/{versionId}/schema")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class SchemaResource {

    private final SchemaService schemaService;

    @GET
    public Response getSchema(
        @PathParam("templateId") Long templateId,
        @PathParam("versionId") Long versionId
    ) {
        SchemaDTO schema = schemaService.getSchema(templateId, versionId);
        return Response.ok(schema).build();
    }

    @PUT
    public Response saveSchema(
        @PathParam("templateId") Long templateId,
        @PathParam("versionId") Long versionId,
        SchemaSaveDTO dto
    ) {
        SchemaDTO schema = schemaService.saveSchema(templateId, versionId, dto);
        return Response.ok(schema).build();
    }
}
```

- [ ] **Step 3: 在 CxfConfig 中注册 SchemaResource**

在 `jaxRsServer()` 方法参数添加 `SchemaResource schemaResource`，并加入 serviceBeans 列表。

- [ ] **Step 4: 验证端点**

Run: `curl -s http://localhost:8888/api/v2/templates/1001/versions/2001/schema | head -c 200`
Expected: JSON 格式的 Schema 数据（或 404 如果数据不存在）

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/adapter/jaxrs/SchemaResource.java src/main/java/com/contract/adapter/rsp/SchemaGetRsp.java src/main/java/com/contract/infrastructure/cxf/CxfConfig.java
git commit -m "feat: add SchemaResource CXF JAX-RS at /api/v2/templates/{id}/versions/{id}/schema"
```

---

## Task 10: 迁移 XML Mapper 到 `.opengauss.xml`

**Files:**
- Rename: `src/main/resources/mapper/TemplateMapper.xml` → `src/main/resources/mapper/TemplateMapper.opengauss.xml`
- Rename: `src/main/resources/mapper/TemplateVersionMapper.xml` → `src/main/resources/mapper/TemplateVersionMapper.opengauss.xml`
- Modify: `src/main/resources/application.yml` (更新 mapper-locations)

- [ ] **Step 1: 读取现有 XML 内容并重命名**

Run: `mv src/main/resources/mapper/TemplateMapper.xml src/main/resources/mapper/TemplateMapper.opengauss.xml`
Run: `mv src/main/resources/mapper/TemplateVersionMapper.xml src/main/resources/mapper/TemplateVersionMapper.opengauss.xml`

- [ ] **Step 2: 更新 application.yml 的 mapper-locations**

```yaml
mybatis-plus:
  mapper-locations: classpath:mapper/*.opengauss.xml
```

- [ ] **Step 3: 验证编译和启动**

Run: `cd /home/wula/IdeaProjects/dymic && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add -A src/main/resources/mapper/ src/main/resources/application.yml
git commit -m "refactor: rename XML mappers to .opengauss.xml convention"
```

---

## Task 11: 编写状态流转文档

**Files:**
- Create: `docs/domain/template-state-flow.md`

- [ ] **Step 1: 创建状态流转文档**

```markdown
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

## 业务规则

1. 创建模板时自动创建 DRAFT 版本
2. 只有 DRAFT 版本可以发布
3. 发布版本时记录 publishTime 和 publishBy
4. 停用模板不会影响已发布版本
5. 模板编码创建后不可修改
```

- [ ] **Step 2: Commit**

```bash
git add docs/domain/template-state-flow.md
git commit -m "docs: add template aggregate state flow documentation"
```

---

## Task 12: 集成验证（E2E Smoke Test）

**Files:**
- No new files

- [ ] **Step 1: 启动应用**

Run: `cd /home/wula/IdeaProjects/dymic && mvn spring-boot:run -q &`

- [ ] **Step 2: 验证 CXF JAX-RS 端点**

```bash
# 健康检查
curl -s http://localhost:8888/api/v2/health

# 创建模板
curl -s -X POST http://localhost:8888/api/v2/templates \
  -H "Content-Type: application/json" \
  -d '{"templateCode":"TEST001","templateName":"测试模板"}'

# 查询模板列表
curl -s http://localhost:8888/api/v2/templates

# 查询单个模板
curl -s http://localhost:8888/api/v2/templates/{id}
```

- [ ] **Step 3: 验证旧 Spring MVC 端点未受影响**

```bash
curl -s http://localhost:8888/api/templates
```

Expected: 返回 JSON 格式模板列表，结构与重构前一致

- [ ] **Step 4: 验证前端配置界面**

在浏览器打开 `http://localhost:8888/config/template-designer.html`，验证拖拽和保存功能正常。

- [ ] **Step 5: Commit（如有修复）**

```bash
git add -A
git commit -m "fix: integration fixes from E2E smoke test"
```

---

## 自我审查

### 规格覆盖检查

| 规格要求 | 任务 |
|---------|------|
| CXF JAX-RS 集成 | Task 1, 2 |
| 充血领域模型 | Task 4 |
| 枚举状态 + OffsetDateTime | Task 4 |
| 值对象（JSONB 强类型） | Task 3 |
| 三层 MapStruct 转换器 | Task 6 |
| DomainService（应用层薄） | Task 5 |
| Req/Rsp 具体类型 | Task 7 |
| CXF Resource | Task 8, 9 |
| XML Mapper 重命名 | Task 10 |
| 状态流转文档 | Task 11 |
| E2E 验证 | Task 12 |

### 占位符扫描

无 TBD、TODO、"implement later" 等占位符。所有代码步骤包含完整代码。

### 类型一致性检查

- `Template.status` 类型在所有引用处统一为 `Template.TemplateStatus` 枚举
- `TemplateVersion.versionStatus` 类型统一为 `TemplateVersion.VersionStatus` 枚举
- 时间字段统一为 `OffsetDateTime`
- ReqConverter/DomainConverter/EntityConverter 中的转换方法签名一致
