# DDD 重构设计规格书

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将现有 Spring MVC 后端重构为 Apache CXF JAX-RS + DDD 四层架构，建立充血领域模型、严格垂直调用链、MapStruct 转换链和 XML Mapper。

**Architecture:** CXF JAX-RS（/api/v2/*）与现有 Spring MVC（/api/*）共存。四层架构：Adapter → Application → Domain → Infrastructure。应用层薄编排，领域层厚逻辑。应用层禁止直接调用 Repository，必须通过 DomainService。

**Tech Stack:** Apache CXF 4.x JAX-RS / Spring Boot 3.5.14 / MapStruct 1.6.3 / MyBatis-Plus 3.5.5 / Jackson / openGauss

---

## 1. 包结构与分层规则

### 1.1 目标包结构

```
com.contract
├── adapter                          # 接口层（Apache CXF JAX-RS）
│   ├── jaxrs                        # JAX-RS 资源类
│   │   ├── TemplateResource.java
│   │   ├── SchemaResource.java
│   │   └── ...
│   └── convert                      # ReqConverter（Req → DTO）
│       └── TemplateReqConverter.java
│
├── application                      # 应用层（薄编排）
│   ├── service                      # 应用服务（编排，不含业务逻辑）
│   │   ├── TemplateService.java
│   │   └── ...
│   ├── dto                          # DTO 对象
│   │   ├── TemplateDTO.java
│   │   └── ...
│   └── convert                      # DomainConverter（DTO ↔ Domain）
│       └── TemplateDomainConverter.java
│
├── domain                           # 领域层（充血模型）
│   ├── template                     # Template 聚合
│   │   ├── Template.java            # 聚合根
│   │   ├── TemplateVersion.java     # 实体
│   │   ├── LayoutNode.java          # 实体
│   │   ├── FieldDef.java            # 实体
│   │   ├── FieldComponent.java      # 实体
│   │   ├── DataProvider.java        # 实体
│   │   ├── QueryConfig.java         # 实体
│   │   ├── QueryParam.java          # 值对象
│   │   ├── QueryFillRule.java       # 值对象
│   │   ├── ActionConfig.java        # 值对象
│   │   ├── service                  # 领域服务
│   │   │   ├── TemplateDomainService.java
│   │   │   └── SchemaDomainService.java
│   │   └── repository              # 仓储接口（只有接口）
│   │       ├── TemplateRepository.java
│   │       └── ...
│   ├── contract                     # Contract 聚合（Sub-project 3）
│   │   ├── Contract.java
│   │   ├── service
│   │   └── repository
│   └── shared                       # 领域共享
│       └── types                    # 值对象类型
│           ├── LayoutProps.java
│           ├── ComponentProps.java
│           └── ProviderConfig.java
│
├── infrastructure                   # 基础设施层
│   ├── persistence
│   │   ├── entity                   # 数据库实体（Entity 后缀）
│   │   │   ├── TemplateEntity.java
│   │   │   └── ...
│   │   ├── mapper                   # MyBatis Mapper 接口
│   │   │   ├── TemplateMapper.java
│   │   │   └── ...
│   │   ├── repository              # Repository 实现
│   │   │   ├── TemplateRepositoryImpl.java
│   │   │   └── ...
│   │   └── convert                  # EntityConverter（Entity ↔ Domain）
│   │       └── TemplateEntityConverter.java
│   ├── cxf                          # CXF 配置
│   │   └── CxfConfig.java
│   ├── json                         # JSON 工具
│   │   └── JsonbHelper.java
│   └── id                           # ID 生成
│       └── SnowflakeIdGenerator.java
│
└── common                           # 公共模块
    ├── exception
    ├── result
    ├── enums
    └── config
```

### 1.2 严格垂直调用链

```
Adapter（Resource）
  │  TemplateReqConverter: TemplateCreateReq → TemplateDTO
  ▼
Application（Service）
  │  TemplateDomainConverter: TemplateDTO → Template
  │  ⚠ 禁止直接调用 Repository，必须通过 DomainService
  ▼
Domain（DomainService）
  │  承载业务逻辑、校验、状态流转
  │  调用 Repository 接口
  ▼
Infrastructure（RepositoryImpl）
  │  TemplateEntityConverter: Template ↔ TemplateEntity
  ▼
Database（openGauss）
```

**绝对禁止的调用：**
- Application → Repository（必须经过 DomainService）
- Domain → Entity/Mapper（依赖倒置，Domain 只知 Repository 接口）
- Adapter → Domain（必须经过 Application Service）

### 1.3 各层职责

| 层 | 职责 | 包含 | 禁止 |
|---|---|---|---|
| Adapter | HTTP 入口、参数校验、Req/Rsp 转换 | Resource、Req、Rsp、ReqConverter | 业务逻辑、直接调用 Domain/Repository |
| Application | 薄编排、DTO 转换、事务边界 | Service、DTO、DomainConverter | 业务规则、直接调用 Repository |
| Domain | 业务规则、充血模型、状态流转 | 领域对象、DomainService、Repository 接口 | 依赖基础设施、使用 @Repository/@Mapper |
| Infrastructure | 持久化、外部集成、技术实现 | Entity、Mapper、RepositoryImpl、EntityConverter | 业务逻辑 |

---

## 2. CXF JAX-RS 集成

### 2.1 共存模式

- **CXF JAX-RS:** `/api/v2/*` — 新代码
- **Spring MVC:** `/api/*` — 现有代码（逐步废弃）
- 两套 API 同时可用，前端逐步切换

### 2.2 Resource 类规范

```java
@Path("/v2/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TemplateResource {

    private final TemplateService templateService;

    public TemplateResource(TemplateService templateService) {
        this.templateService = templateService;
    }

    @POST
    public TemplateCreateRsp create(TemplateCreateReq req) {
        TemplateDTO dto = templateService.create(req);
        return TemplateReqConverter.toCreateRsp(dto);
    }

    @GET
    @Path("/{id}")
    public TemplateGetRsp getById(@PathParam("id") Long id) {
        TemplateDTO dto = templateService.getById(id);
        return TemplateReqConverter.toGetRsp(dto);
    }
}
```

**关键规则：**
- 入参：具体的 `XxxReq` 类（不用 `@RequestBody`，CXF 自动绑定）
- 出参：具体的 `XxxRsp` 类（不用 `Result<T>` 泛型包装）
- 每个 API 端点有自己独立的 Req/Rsp 类型，不用共享泛型
- 使用 `@Path`、`@GET`、`@POST`、`@PUT`、`@DELETE`（CXF JAX-RS 注解）

### 2.3 Req/Rsp 命名规范

| 操作 | 入参 | 出参 |
|------|------|------|
| 创建 | `TemplateCreateReq` | `TemplateCreateRsp` |
| 查询单个 | `@PathParam("id")` | `TemplateGetRsp` |
| 更新 | `TemplateUpdateReq` | `TemplateUpdateRsp` |
| 删除 | `@PathParam("id")` | `void`（204） |
| 列表查询 | `TemplateListReq`（QueryParam） | `TemplateListRsp` |

### 2.4 CXF 配置

```java
@Configuration
public class CxfConfig {

    @Bean
    public CXFServlet cxfServlet() {
        return new CXFServlet();
    }

    @Bean
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public Server jaxRsServer() {
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setBus(springBus());
        factory.setAddress("/");
        factory.setServiceBeans(/* all Resource beans */);
        factory.setProviders(List.of(
            new JacksonJsonProvider(),
            new ValidationExceptionMapper()
        ));
        return factory.create();
    }
}
```

CXF Servlet 映射到 `/api/*`，Resource 用 `@Path("/v2/...")` 区分版本前缀。

---

## 3. MapStruct 转换链

### 3.1 三层转换器

```
请求流向：
TemplateCreateReq → [ReqConverter] → TemplateDTO → [DomainConverter] → Template → [EntityConverter] → TemplateEntity

响应流向：
TemplateEntity → [EntityConverter] → Template → [DomainConverter] → TemplateDTO → [ReqConverter] → TemplateCreateRsp
```

| 转换器 | 位置 | 职责 |
|--------|------|------|
| `TemplateReqConverter` | `adapter.convert` | Req ↔ DTO、DTO → Rsp |
| `TemplateDomainConverter` | `application.convert` | DTO ↔ Domain |
| `TemplateEntityConverter` | `infrastructure.persistence.convert` | Domain ↔ Entity |

### 3.2 MapStruct 接口定义

```java
// adapter.convert — 无状态，可用 default 方法或 @Mapper
@Mapper(componentModel = "spring")
public interface TemplateReqConverter {
    TemplateDTO toDTO(TemplateCreateReq req);
    TemplateCreateRsp toCreateRsp(TemplateDTO dto);
    TemplateGetRsp toGetRsp(TemplateDTO dto);
}

// application.convert
@Mapper(componentModel = "spring")
public interface TemplateDomainConverter {
    Template toDomain(TemplateDTO dto);
    TemplateDTO toDTO(Template domain);
    List<TemplateDTO> toDTOList(List<Template> domains);
}

// infrastructure.persistence.convert
@Mapper(componentModel = "spring")
public interface TemplateEntityConverter {
    Template toDomain(TemplateEntity entity);
    TemplateEntity toEntity(Template domain);
}
```

---

## 4. 充血领域模型

### 4.1 聚合根：Template

```java
public class Template {
    private Long id;
    private String templateCode;
    private String templateName;
    private String description;
    private TemplateStatus status;      // 枚举，非 String
    private OffsetDateTime createTime;  // 非 LocalDateTime
    private OffsetDateTime updateTime;

    // 状态流转
    public void activate() { /* DRAFT → ACTIVE */ }
    public void deactivate() { /* ACTIVE → INACTIVE */ }

    // 工厂方法
    public static Template create(String code, String name, String desc) {
        // 校验 + 创建
    }

    // 业务规则
    public boolean canPublish() { return status == TemplateStatus.ACTIVE; }
}
```

**关键规则：**
- 领域对象无 Lombok `@Data`（防止意外 setter 暴露）
- 使用 getter + 私有 setter 或 builder
- 状态变更通过方法（`activate()`、`deactivate()`），不暴露 `setStatus()`
- 时间类型用 `OffsetDateTime`（对应 openGauss `TIMESTAMP WITH TIME ZONE`）
- 枚举替代 String 状态字段

### 4.2 状态流转文档

每个聚合根需要状态流转文档，存放于 `docs/domain/`：

```
Template:
  DRAFT ──activate()──→ ACTIVE ──deactivate()──→ INACTIVE
  DRAFT ──(删除)──→ [终态]
  ACTIVE ──(删除)──→ [终态]

TemplateVersion:
  DRAFT ──publish()──→ PUBLISHED
  DRAFT ──(废弃)──→ ARCHIVED
  PUBLISHED ──(基于它创建新草稿)──→ [不变]
```

### 4.3 JSONB 强类型

领域层用 Java 类型对象，基础设施层用 String 存储：

```
Domain:  LayoutProps（Java 类）   ↔ JSONB String ↔ Entity: String
Domain:  ComponentProps           ↔ JSONB String ↔ Entity: String
Domain:  ProviderConfig           ↔ JSONB String ↔ Entity: String
```

转换在 EntityConverter 中通过 Jackson 完成：

```java
// EntityConverter 内
@Mapper(componentModel = "spring")
public interface LayoutNodeEntityConverter {
    @Mapping(target = "props", expression = "java(toProps(entity.getPropsJson()))")
    LayoutNode toDomain(LayoutNodeEntity entity);

    @Mapping(target = "propsJson", expression = "java(toJson(domain.getProps()))")
    LayoutNodeEntity toEntity(LayoutNode domain);
}
```

---

## 5. XML Mapper 与数据库

### 5.1 XML Mapper 规范

- 文件位置：`src/main/resources/mapper/`
- 命名：`{Entity}Mapper.opengauss.xml`（如 `TemplateMapper.opengauss.xml`）
- XML 中省略 Entity 后缀（resultType/id 不含 Entity）
- 复杂 SQL（JOIN、子查询、JSONB 操作）写在 XML 中
- 简单 CRUD 可保留 MyBatis-Plus BaseMapper 继承

```xml
<!-- TemplateMapper.opengauss.xml -->
<mapper namespace="com.contract.infrastructure.persistence.mapper.TemplateMapper">
    <resultMap id="templateResult" type="com.contract.infrastructure.persistence.entity.TemplateEntity">
        <id property="id" column="id"/>
        <result property="templateCode" column="template_code"/>
        <result property="templateName" column="template_name"/>
        <result property="status" column="status"/>
        <result property="createTime" column="create_time"/>
    </resultMap>

    <select id="findByCodeWithVersion" resultMap="templateResult">
        SELECT t.*, v.version_number, v.version_status
        FROM t_template t
        LEFT JOIN t_template_version v ON t.id = v.template_id AND v.deleted = 0
        WHERE t.template_code = #{code} AND t.deleted = 0
    </select>
</mapper>
```

### 5.2 现有 Mapper 迁移计划

| 现有文件 | 迁移目标 |
|---------|---------|
| `TemplateMapper.xml` | → `TemplateMapper.opengauss.xml` |
| `TemplateVersionMapper.xml` | → `TemplateVersionMapper.opengauss.xml` |
| 其余 8 个 Mapper（仅 Java 接口） | 新增对应的 `.opengauss.xml` |

### 5.3 时间字段

- openGauss 列类型：`TIMESTAMP WITH TIME ZONE`
- Java 类型：`OffsetDateTime`（非 LocalDateTime）
- 所有审计字段统一：`create_time`、`update_time` 类型为 `OffsetDateTime`

---

## 6. 模块迁移计划

### Phase 0: 基础设施准备
- [ ] 添加 CXF 依赖到 pom.xml
- [ ] 创建 `CxfConfig.java`
- [ ] 配置 CXF Servlet 映射 + Jackson Provider
- [ ] 验证 `/api/v2/` 端点可用（hello world）
- [ ] 确保现有 `/api/*` Spring MVC 不受影响

### Phase 1: Domain 层改造（Template 聚合）
- [ ] 重构 `Template.java`：充血模型 + 枚举状态 + OffsetDateTime + 工厂方法
- [ ] 重构 `TemplateVersion.java`：状态流转方法
- [ ] 创建值对象类型：`LayoutProps.java`、`ComponentProps.java`、`ProviderConfig.java`
- [ ] 创建 `TemplateDomainService.java`：从 Application Service 迁移业务逻辑
- [ ] 编写状态流转文档 `docs/domain/template-state-flow.md`

### Phase 2: Infrastructure 层改造
- [ ] 创建三层 MapStruct 转换器（ReqConverter、DomainConverter、EntityConverter）
- [ ] 迁移 XML Mapper：`*.xml` → `*.opengauss.xml`
- [ ] 为其余 8 个 Mapper 创建 XML 文件
- [ ] EntityConverter 中处理 JSONB 强类型转换

### Phase 3: Application 层改造
- [ ] Application Service 瘦身：只保留编排逻辑
- [ ] 业务逻辑迁移到 DomainService
- [ ] 确保应用层不直接调用 Repository

### Phase 4: Adapter 层改造（CXF Resource）
- [ ] 创建 `TemplateResource.java`（CXF JAX-RS）
- [ ] 创建 `SchemaResource.java`
- [ ] 创建所有 Req/Rsp 类型
- [ ] 实现三层转换链

### Phase 5: 测试与验证
- [ ] DomainService 单元测试（Mockito）
- [ ] RepositoryImpl 集成测试（H2）
- [ ] Resource 接口测试
- [ ] 前端切换到 `/api/v2/*`
- [ ] 移除旧 Spring MVC Controller（可选，待稳定后）

### Phase 6: EXPLAIN 验证（JSONB 索引）
- [ ] 伪造测试数据（足够量级以体现索引效果）
- [ ] 执行 EXPLAIN ANALYZE 查询
- [ ] 记录结果到 `docs/explain/` 目录
- [ ] 验证 JSONB 索引被正确使用

---

## 7. 关键决策记录

| 决策 | 选择 | 理由 |
|------|------|------|
| REST 框架 | Apache CXF JAX-RS | 用户指定，非 SOAP |
| 迁移策略 | 增量共存（/api/v2/） | 零停机，可回滚 |
| 入参命名 | `XxxReq`（缩写） | 简洁 |
| 出参命名 | `XxxRsp`（具体类型，非泛型） | 避免泛型代码坏味道 |
| 响应包装 | 无 `Result<T>` | 具体类型更清晰 |
| Entity 后缀 | Java 用 Entity 后缀，XML 省略 | Java 区分领域对象，XML 简洁 |
| 时间类型 | `OffsetDateTime` | 对应 openGauss `TIMESTAMPTZ` |
| JSON 库 | Jackson（禁止 FastJSON） | 安全性、性能、生态 |
| 调用链 | App → DomainService → Repo（禁止 App → Repo） | DDD 依赖规则 |
| 领域模型 | 充血模型 | 业务逻辑内聚 |
| MapStruct | 三层转换器 | 各层对象隔离 |
| SQL 管理 | XML（`.opengauss.xml`） | 复杂 SQL 可维护 |
| EXPLAIN 验证 | 伪造数据 | 体现索引效果 |

---

## 8. 子项目依赖关系

```
Sub-project 1: DDD 重构 ← 当前
    ↓
Sub-project 2: 数据源配置统一（DataProvider 入口收束 + 业务/IT 视图区分）
    ↓
Sub-project 3: 合同创建界面（Contract 聚合 + 动态渲染）
    ↓
Sub-project 4: JSONB EXPLAIN 验证（伪造数据 + 索引证明）
```

每个子项目有独立的 spec → plan → 实施周期。
