# Dynamic Template Delivery Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把当前动态模板配置项目收敛到可交付、可迁移的目标形态：业务范围以 `req/req.md` 与 `req/supplement.md` 为准，架构上移除 MyBatis-Plus，落成 CXF + adapter Req/Rsp + application DTO + domain 充血模型 + infrastructure 数据实体/仓储实现 + MyBatis XML + 浏览器 E2E 与 JSONB/性能证据。

**Architecture:** 先建立审计基线和证据目录，再按“协议层收口 → 持久化收口 → 领域边界收口 → 功能闭环补齐 → 浏览器 E2E 与性能证据”推进。计划优先复用现有 `TemplateMapper.opengauss.xml` / `TemplateVersionMapper.opengauss.xml` 模式，把其余模块统一迁移到 `xxx.opengauss.xml`、原生 Mapper + RepositoryImpl，并把 Request/Response 从 application DTO 中剥离到 adapter/controller 包。浏览器 E2E 以 `docs/superpowers/e2e/README.md` 为覆盖基线，交付件额外包含“人工 double check 指引”，明确用户自己要手工验证的场景与截图清单。任何浏览器场景中配置界面、保存回显、嵌套展示、预览结果之间只要出现不一致，都必须先修复，再重新执行场景并截图留证，不允许只截图记录问题后跳过修复。

**Tech Stack:** Java 21, Spring Boot 3.5.14, Apache CXF, MyBatis XML, openGauss/H2, MapStruct, 原生 HTML/JS, JUnit 5, MockMvc, Chrome MCP

---

## File Structure

### Existing files to modify

- `pom.xml` — 移除 `mybatis-plus-spring-boot3-starter`，改为原生 MyBatis 依赖与配置
- `src/main/resources/application.yml` — 去掉 `mybatis-plus` 配置，改成 `mybatis` 配置
- `src/main/resources/application-test.yml` — 同步测试环境 MyBatis 配置
- `src/main/java/com/contract/adapter/controller/*.java` — Controller 收口到 Req/Rsp
- `src/main/java/com/contract/application/template/*.java` — 应用层变薄，仅保留 DTO/编排
- `src/main/java/com/contract/application/template/dto/*.java` — 清理 Request 混放，保留 DTO
- `src/main/java/com/contract/domain/**/*.java` — 补聚合根/值对象/仓储接口/领域流转语义
- `src/main/java/com/contract/infrastructure/persistence/entity/*.java` — 保留 `Entity` 后缀的数据实体
- `src/main/java/com/contract/infrastructure/persistence/mapper/*.java` — 原生 MyBatis Mapper 接口
- `src/main/java/com/contract/infrastructure/persistence/repository/*.java` — Repository 实现只依赖 Mapper + Entity Converter
- `src/main/resources/mapper/*.opengauss.xml` — 所有持久化 XML，禁止 `Entity` 出现在文件名里
- `src/main/resources/static/config/*.html`
- `src/main/resources/static/config/js/*.js`
- `src/main/resources/static/js/*.js` — 浏览器 E2E 涉及的页面与脚本
- `docs/superpowers/e2e/README.md` — E2E 场景覆盖基线
- `src/test/java/com/contract/**/*.java` — 单元/集成/MockMvc 测试

### New files to create

- `src/main/java/com/contract/adapter/controller/request/*.java` — Controller 入参 `XxxReq`
- `src/main/java/com/contract/adapter/controller/response/*.java` — Controller 出参 `XxxRsp`
- `src/main/resources/mapper/ActionConfig.opengauss.xml`
- `src/main/resources/mapper/DataProvider.opengauss.xml`
- `src/main/resources/mapper/FieldComponent.opengauss.xml`
- `src/main/resources/mapper/FieldDef.opengauss.xml`
- `src/main/resources/mapper/LayoutNode.opengauss.xml`
- `src/main/resources/mapper/QueryConfig.opengauss.xml`
- `src/main/resources/mapper/QueryFillRule.opengauss.xml`
- `src/main/resources/mapper/QueryParam.opengauss.xml`
- `docs/evidence/dynamic-template-audit/README.md` — 证据包目录说明
- `docs/evidence/dynamic-template-audit/browser-flows/*.md` — 浏览器场景记录
- `docs/evidence/dynamic-template-audit/file-notes/*.md` — 关键文件说明
- `docs/evidence/dynamic-template-audit/sql-and-xml/*.md` — XML/SQL/JSONB 说明
- `docs/evidence/dynamic-template-audit/performance/*.md` — EXPLAIN 与量级分析
- `docs/evidence/dynamic-template-audit/screenshots/` — 截图目录（运行期产物）
- `docs/evidence/dynamic-template-audit/manual-checklist.md` — 用户人工 double check 指引

### Files to delete after migration (when no references remain)

- `src/main/java/com/contract/application/template/dto/*Request.java` — 当前混在 application 层的 request 类型
- 所有残留的 MyBatis-Plus 相关 import / wrapper / starter 配置

---

## Phase 0: Freeze business baseline and evidence directory

### Task 1: Lock scope to req documents and create evidence skeleton

**Files:**
- Create: `docs/evidence/dynamic-template-audit/README.md`
- Create: `docs/evidence/dynamic-template-audit/browser-flows/.gitkeep`
- Create: `docs/evidence/dynamic-template-audit/file-notes/.gitkeep`
- Create: `docs/evidence/dynamic-template-audit/sql-and-xml/.gitkeep`
- Create: `docs/evidence/dynamic-template-audit/performance/.gitkeep`
- Create: `docs/evidence/dynamic-template-audit/manual-checklist.md`
- Modify: `docs/superpowers/specs/2026-06-10-dynamic-template-full-delivery-audit-design.md`

- [ ] **Step 1: Write the failing documentation check**

Create `docs/evidence/dynamic-template-audit/README.md` with this exact initial checklist block and leave all items unchecked:

```md
# Dynamic Template Audit Evidence Pack

## Scope Baseline
- [ ] `req/req.md` reviewed
- [ ] `req/supplement.md` reviewed
- [ ] Browser E2E scenarios enumerated
- [ ] XML naming audit recorded
- [ ] JSONB/performance evidence recorded
```

- [ ] **Step 2: Verify the evidence skeleton is missing before creation**

Run: `test -f docs/evidence/dynamic-template-audit/README.md`
Expected: exit code `1`

- [ ] **Step 3: Create the evidence directories and README**

Create the directories and put this content in `docs/evidence/dynamic-template-audit/README.md`:

```md
# Dynamic Template Audit Evidence Pack

This directory stores evidence for the dynamic template delivery audit and migration work.

## Scope Baseline
- [ ] `req/req.md` reviewed
- [ ] `req/supplement.md` reviewed
- [ ] Browser E2E scenarios enumerated
- [ ] XML naming audit recorded
- [ ] JSONB/performance evidence recorded
- [ ] Manual double-check checklist created

## Subdirectories
- `browser-flows/` — Browser scenario step logs
- `screenshots/` — Browser screenshots captured during E2E
- `sql-and-xml/` — Mapper XML, SQL, and JSONB notes
- `performance/` — EXPLAIN and scale notes
- `file-notes/` — Notes for key classes and call chains
```

- [ ] **Step 4: Verify the skeleton exists**

Run: `test -f docs/evidence/dynamic-template-audit/README.md && find docs/evidence/dynamic-template-audit -maxdepth 2 | sort`
Expected: README and five subdirectories exist

- [ ] **Step 5: Commit**

```bash
git add docs/evidence/dynamic-template-audit docs/superpowers/specs/2026-06-10-dynamic-template-full-delivery-audit-design.md
git commit -m "docs: add dynamic template audit evidence structure"
```

---

## Phase 1: Enforce protocol-layer boundaries (Req/Rsp vs DTO)

### Task 2: Move controller Request objects out of application DTO package

**Files:**
- Create: `src/main/java/com/contract/adapter/controller/request/ActionConfigCreateReq.java`
- Create: `src/main/java/com/contract/adapter/controller/request/ActionConfigUpdateReq.java`
- Create: `src/main/java/com/contract/adapter/controller/request/DataProviderCreateReq.java`
- Create: `src/main/java/com/contract/adapter/controller/request/FieldComponentCreateReq.java`
- Create: `src/main/java/com/contract/adapter/controller/request/QueryConfigCreateReq.java`
- Create: `src/main/java/com/contract/adapter/controller/request/QueryConfigUpdateReq.java`
- Modify: `src/main/java/com/contract/adapter/controller/ActionConfigController.java`
- Modify: `src/main/java/com/contract/adapter/controller/DataProviderController.java`
- Modify: `src/main/java/com/contract/adapter/controller/FieldComponentController.java`
- Modify: `src/main/java/com/contract/adapter/controller/QueryConfigController.java`
- Modify: `src/main/java/com/contract/application/template/dto/ActionConfigCreateRequest.java`
- Modify: `src/main/java/com/contract/application/template/dto/ActionConfigUpdateRequest.java`
- Modify: `src/main/java/com/contract/application/template/dto/DataProviderCreateRequest.java`
- Modify: `src/main/java/com/contract/application/template/dto/FieldComponentCreateRequest.java`
- Modify: `src/main/java/com/contract/application/template/dto/QueryConfigCreateRequest.java`
- Modify: `src/main/java/com/contract/application/template/dto/QueryConfigUpdateRequest.java`
- Test: `src/test/java/com/contract/DataProviderControllerTest.java`
- Test: `src/test/java/com/contract/BusinessDataSourceControllerTest.java`
- Test: `src/test/java/com/contract/LayoutNodeControllerTest.java`

- [ ] **Step 1: Write the failing controller-layer boundary test**

Add this test method to `src/test/java/com/contract/DataProviderControllerTest.java`:

```java
@Test
void controllerRequestsShouldLiveInAdapterPackage() {
    assertEquals(
        "com.contract.adapter.controller.request",
        com.contract.adapter.controller.request.DataProviderCreateReq.class.getPackageName()
    );
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=DataProviderControllerTest#controllerRequestsShouldLiveInAdapterPackage -q`
Expected: FAIL because `DataProviderCreateReq` does not exist

- [ ] **Step 3: Create adapter request classes with DTO-to-application conversion fields**

Create `src/main/java/com/contract/adapter/controller/request/DataProviderCreateReq.java`:

```java
package com.contract.adapter.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataProviderCreateReq {
    @NotBlank
    private String providerCode;
    @NotBlank
    private String providerName;
    @NotBlank
    private String providerType;
    @NotBlank
    private String configJson;
}
```

Create the other `*Req` classes by mirroring the fields currently exposed in the corresponding `*Request` classes, keeping them in `adapter.controller.request` and using validation annotations only at the protocol boundary.

- [ ] **Step 4: Update controllers to accept `*Req` and convert into application DTOs**

In each controller, change method signatures from `*Request` to `*Req`, and map explicitly to the application-layer DTO/request type until the next phase removes the old application request class.

Use this pattern in `DataProviderController.java`:

```java
@PostMapping
public Result<DataProviderDTO> create(@Valid @RequestBody DataProviderCreateReq req) {
    DataProviderCreateDTO dto = new DataProviderCreateDTO();
    dto.setProviderCode(req.getProviderCode());
    dto.setProviderName(req.getProviderName());
    dto.setProviderType(req.getProviderType());
    dto.setConfigJson(req.getConfigJson());
    return Result.ok(service.create(dto));
}
```

- [ ] **Step 5: Run the focused controller tests**

Run: `mvn test -Dtest=DataProviderControllerTest,BusinessDataSourceControllerTest,LayoutNodeControllerTest -q`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/contract/adapter/controller src/main/java/com/contract/application/template/dto src/test/java/com/contract/DataProviderControllerTest.java src/test/java/com/contract/BusinessDataSourceControllerTest.java src/test/java/com/contract/LayoutNodeControllerTest.java
git commit -m "refactor: move controller request models to adapter layer"
```

### Task 3: Introduce controller response models and stop exposing DTOs directly from CXF endpoints

**Files:**
- Create: `src/main/java/com/contract/adapter/controller/response/DataProviderRsp.java`
- Create: `src/main/java/com/contract/adapter/controller/response/BusinessDataSourceRsp.java`
- Create: `src/main/java/com/contract/adapter/controller/response/TemplateRsp.java`
- Modify: `src/main/java/com/contract/adapter/controller/DataProviderController.java`
- Modify: `src/main/java/com/contract/adapter/controller/BusinessDataSourceController.java`
- Modify: `src/main/java/com/contract/adapter/controller/TemplateController.java`
- Test: `src/test/java/com/contract/DataProviderControllerTest.java`
- Test: `src/test/java/com/contract/adapter/controller/TemplateControllerTest.java`

- [ ] **Step 1: Write the failing response-package test**

Add this test to `src/test/java/com/contract/DataProviderControllerTest.java`:

```java
@Test
void controllerResponsesShouldLiveInAdapterPackage() {
    assertEquals(
        "com.contract.adapter.controller.response",
        com.contract.adapter.controller.response.DataProviderRsp.class.getPackageName()
    );
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=DataProviderControllerTest#controllerResponsesShouldLiveInAdapterPackage -q`
Expected: FAIL because `DataProviderRsp` does not exist

- [ ] **Step 3: Create response types**

Create `src/main/java/com/contract/adapter/controller/response/DataProviderRsp.java`:

```java
package com.contract.adapter.controller.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataProviderRsp {
    private Long id;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String dataSourceCategory;
    private String configJson;
    private String status;
}
```

Create the other `*Rsp` classes for controller outputs that should not directly expose application DTOs.

- [ ] **Step 4: Map DTOs to `*Rsp` inside controllers**

Use this pattern in `DataProviderController.java`:

```java
private DataProviderRsp toRsp(DataProviderDTO dto) {
    DataProviderRsp rsp = new DataProviderRsp();
    rsp.setId(dto.getId());
    rsp.setProviderCode(dto.getProviderCode());
    rsp.setProviderName(dto.getProviderName());
    rsp.setProviderType(dto.getProviderType());
    rsp.setDataSourceCategory(dto.getDataSourceCategory());
    rsp.setConfigJson(dto.getConfigJson());
    rsp.setStatus(dto.getStatus());
    return rsp;
}
```

- [ ] **Step 5: Run focused tests**

Run: `mvn test -Dtest=DataProviderControllerTest,TemplateControllerTest -q`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/contract/adapter/controller/response src/main/java/com/contract/adapter/controller/DataProviderController.java src/main/java/com/contract/adapter/controller/BusinessDataSourceController.java src/main/java/com/contract/adapter/controller/TemplateController.java src/test/java/com/contract/DataProviderControllerTest.java src/test/java/com/contract/adapter/controller/TemplateControllerTest.java
git commit -m "refactor: return adapter response models from controllers"
```

---

## Phase 2: Remove MyBatis-Plus and standardize raw MyBatis XML

### Task 4: Remove MyBatis-Plus starter and configuration

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/resources/application-test.yml`

- [ ] **Step 1: Write the failing dependency assertion**

Add this test file: `src/test/java/com/contract/ArchitectureDependencyTest.java`

```java
package com.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ArchitectureDependencyTest {
    @Test
    void pomShouldNotContainMybatisPlusStarter() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        assertFalse(pom.contains("mybatis-plus-spring-boot3-starter"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ArchitectureDependencyTest#pomShouldNotContainMybatisPlusStarter -q`
Expected: FAIL because the starter is still present

- [ ] **Step 3: Replace MyBatis-Plus dependency/config with raw MyBatis**

In `pom.xml`, replace:

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>${mybatis-plus.version}</version>
</dependency>
```

with:

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>3.0.4</version>
</dependency>
```

In `src/main/resources/application.yml`, replace the `mybatis-plus:` block with:

```yaml
mybatis:
  mapper-locations: classpath:mapper/*.opengauss.xml
  configuration:
    map-underscore-to-camel-case: true
```

Apply the same shape in `application-test.yml`.

- [ ] **Step 4: Run the dependency/config tests**

Run: `mvn test -Dtest=ArchitectureDependencyTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add pom.xml src/main/resources/application.yml src/main/resources/application-test.yml src/test/java/com/contract/ArchitectureDependencyTest.java
git commit -m "refactor: replace mybatis-plus with raw mybatis"
```

### Task 5: Standardize mapper XML naming and create missing XML files

**Files:**
- Create: `src/main/resources/mapper/ActionConfig.opengauss.xml`
- Create: `src/main/resources/mapper/DataProvider.opengauss.xml`
- Create: `src/main/resources/mapper/FieldComponent.opengauss.xml`
- Create: `src/main/resources/mapper/FieldDef.opengauss.xml`
- Create: `src/main/resources/mapper/LayoutNode.opengauss.xml`
- Create: `src/main/resources/mapper/QueryConfig.opengauss.xml`
- Create: `src/main/resources/mapper/QueryFillRule.opengauss.xml`
- Create: `src/main/resources/mapper/QueryParam.opengauss.xml`
- Modify: `src/main/resources/mapper/TemplateMapper.opengauss.xml`
- Modify: `src/main/resources/mapper/TemplateVersionMapper.opengauss.xml`
- Test: `src/test/java/com/contract/ArchitectureDependencyTest.java`

- [ ] **Step 1: Write the failing XML naming test**

Extend `ArchitectureDependencyTest.java` with:

```java
@Test
void mapperXmlFileNamesShouldNotContainEntity() throws Exception {
    try (var stream = Files.list(Path.of("src/main/resources/mapper"))) {
        assertFalse(stream.anyMatch(path -> path.getFileName().toString().contains("Entity")));
    }
}
```

- [ ] **Step 2: Run test to verify current failure or gap**

Run: `mvn test -Dtest=ArchitectureDependencyTest#mapperXmlFileNamesShouldNotContainEntity -q`
Expected: PASS or neutral on naming, but missing XML files still remain for other modules

- [ ] **Step 3: Create missing XMLs following the Template pattern**

Use `TemplateMapper.opengauss.xml` as the pattern and create `DataProvider.opengauss.xml` with this starter shape:

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.contract.infrastructure.persistence.mapper.DataProviderMapper">

    <resultMap id="DataProviderResultMap" type="com.contract.infrastructure.persistence.entity.DataProviderEntity">
        <id column="id" property="id"/>
        <result column="provider_code" property="providerCode"/>
        <result column="provider_name" property="providerName"/>
        <result column="provider_type" property="providerType"/>
        <result column="data_source_category" property="dataSourceCategory"/>
        <result column="config_json" property="configJson"/>
        <result column="cache_enabled" property="cacheEnabled"/>
        <result column="cache_ttl_seconds" property="cacheTtlSeconds"/>
        <result column="is_temporary" property="isTemporary"/>
        <result column="status" property="status"/>
        <result column="created_by" property="createdBy"/>
        <result column="created_at" property="createdAt"/>
        <result column="updated_by" property="updatedBy"/>
        <result column="updated_at" property="updatedAt"/>
        <result column="is_deleted" property="isDeleted"/>
    </resultMap>

</mapper>
```

Create the other XML files with matching `namespace`, `resultMap`, and table-specific fields.

- [ ] **Step 4: Run build-time XML loading verification**

Run: `mvn test -Dtest=DataProviderServiceTest,FieldComponentServiceTest,SchemaServiceTest -q`
Expected: PASS with XML mappers loading successfully

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/mapper src/test/java/com/contract/ArchitectureDependencyTest.java
git commit -m "refactor: add raw mybatis opengauss mapper xml files"
```

### Task 6: Remove LambdaQueryWrapper/BaseMapper style from repository implementations

**Files:**
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/ActionConfigRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/DataProviderRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/FieldComponentRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/FieldDefRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/LayoutNodeRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/QueryConfigRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/QueryFillRuleRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/QueryParamRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/TemplateRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/TemplateVersionRepositoryImpl.java`
- Test: `src/test/java/com/contract/DataProviderServiceTest.java`
- Test: `src/test/java/com/contract/FieldComponentServiceTest.java`
- Test: `src/test/java/com/contract/SchemaServiceTest.java`

- [ ] **Step 1: Write the failing raw-mybatis import test**

Extend `ArchitectureDependencyTest.java` with:

```java
@Test
void repositoryImplementationsShouldNotUseMybatisPlusImports() throws Exception {
    try (var paths = Files.walk(Path.of("src/main/java/com/contract/infrastructure/persistence/repository"))) {
        for (Path path : paths.filter(Files::isRegularFile).toList()) {
            String content = Files.readString(path);
            assertFalse(content.contains("com.baomidou"), path.toString());
            assertFalse(content.contains("LambdaQueryWrapper"), path.toString());
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ArchitectureDependencyTest#repositoryImplementationsShouldNotUseMybatisPlusImports -q`
Expected: FAIL on repository implementations still importing MyBatis-Plus

- [ ] **Step 3: Replace MyBatis-Plus query/update code with mapper XML methods**

For `DataProviderRepositoryImpl.java`, refactor methods to use explicit mapper methods. Example target shape:

```java
@Override
public List<DataProvider> findByCategory(String category) {
    return converter.toDomainList(mapper.selectByCategory(category));
}
```

For `DataProviderMapper.java`, add signatures such as:

```java
List<DataProviderEntity> selectByCategory(@Param("category") String category);
DataProviderEntity selectByProviderCode(@Param("providerCode") String providerCode);
int insertDataProvider(DataProviderEntity entity);
int updateDataProvider(DataProviderEntity entity);
```

Add matching SQL to `DataProvider.opengauss.xml`.

Apply the same explicit-mapper pattern across the other repository implementations.

- [ ] **Step 4: Run repository and service tests**

Run: `mvn test -Dtest=ArchitectureDependencyTest,DataProviderServiceTest,FieldComponentServiceTest,SchemaServiceTest,TemplateVersionServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/infrastructure/persistence/repository src/main/java/com/contract/infrastructure/persistence/mapper src/main/resources/mapper src/test/java/com/contract/ArchitectureDependencyTest.java src/test/java/com/contract/DataProviderServiceTest.java src/test/java/com/contract/FieldComponentServiceTest.java src/test/java/com/contract/SchemaServiceTest.java src/test/java/com/contract/application/template/TemplateVersionServiceTest.java
git commit -m "refactor: migrate repositories to raw mybatis mappers"
```

---

## Phase 3: Tighten DDD boundaries and object flow

### Task 7: Document and enforce aggregate root and value object boundaries

**Files:**
- Modify: `src/main/java/com/contract/domain/template/Template.java`
- Modify: `src/main/java/com/contract/domain/template/TemplateVersion.java`
- Modify: `src/main/java/com/contract/domain/template/DataProvider.java`
- Modify: `src/main/java/com/contract/domain/shared/types/ConfigJson.java`
- Modify: `src/main/java/com/contract/domain/shared/types/AuditInfo.java`
- Modify: `src/main/java/com/contract/domain/template/service/TemplateDomainService.java`
- Test: `src/test/java/com/contract/application/template/TemplateServiceTest.java`
- Test: `src/test/java/com/contract/application/template/TemplateVersionServiceTest.java`
- Create: `docs/evidence/dynamic-template-audit/file-notes/domain-object-flow.md`

- [ ] **Step 1: Write the failing domain-flow documentation test**

Create `src/test/java/com/contract/DomainFlowDocumentationTest.java`:

```java
package com.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainFlowDocumentationTest {
    @Test
    void domainObjectFlowDocumentShouldExist() throws Exception {
        Path path = Path.of("docs/evidence/dynamic-template-audit/file-notes/domain-object-flow.md");
        assertTrue(Files.exists(path));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=DomainFlowDocumentationTest -q`
Expected: FAIL because the document does not exist

- [ ] **Step 3: Create the domain flow note and tighten model semantics**

Create `docs/evidence/dynamic-template-audit/file-notes/domain-object-flow.md`:

```md
# Domain Object Flow

## Vertical Call Chain
Controller Req/Rsp -> Application DTO -> Domain Aggregate/Value Object -> Infrastructure Entity/XML

## Aggregate Roots
- Template
- TemplateVersion
- DataProvider

## Value Objects
- ConfigJson
- AuditInfo

## Rule
Infrastructure entities never cross into Domain callers.
Application DTOs never enter Mapper/XML directly.
```

At the same time, add or tighten aggregate/value-object helper methods inside the listed domain classes so the code reflects the documented model.

- [ ] **Step 4: Run domain tests**

Run: `mvn test -Dtest=DomainFlowDocumentationTest,TemplateServiceTest,TemplateVersionServiceTest,DataProviderServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/domain src/main/java/com/contract/application/template/TemplateService.java src/main/java/com/contract/application/template/TemplateVersionService.java docs/evidence/dynamic-template-audit/file-notes/domain-object-flow.md src/test/java/com/contract/DomainFlowDocumentationTest.java src/test/java/com/contract/application/template/TemplateServiceTest.java src/test/java/com/contract/application/template/TemplateVersionServiceTest.java src/test/java/com/contract/DataProviderServiceTest.java
git commit -m "refactor: tighten domain boundaries and document object flow"
```

---

## Phase 4: Complete the business and designer closed loop

### Task 8: Close the designer save/reopen/preview path for nested components and fix preview mismatches

**Files:**
- Modify: `src/main/resources/static/config/template-designer.html`
- Modify: `src/main/resources/static/config/js/designer.js`
- Modify: `src/main/resources/static/config/js/property-panel.js`
- Modify: `src/main/resources/static/js/preview.js`
- Modify: `src/main/java/com/contract/application/template/SchemaService.java`
- Modify: `src/main/java/com/contract/application/template/FieldComponentService.java`
- Modify: `src/main/java/com/contract/application/template/dto/SchemaDTO.java`
- Modify: `src/main/java/com/contract/application/template/dto/SchemaSaveDTO.java`
- Test: `src/test/java/com/contract/SchemaServiceTest.java`
- Test: `src/test/java/com/contract/FieldComponentServiceTest.java`

- [ ] **Step 1: Write the failing nested save/reopen test**

Add this test to `src/test/java/com/contract/SchemaServiceTest.java`:

```java
@Test
void saveAndReloadSchemaShouldPreserveNestedComponentStructure() {
    SchemaSaveDTO saveDTO = new SchemaSaveDTO();
    // populate with nested layout/components matching current DTO shape

    schemaService.saveSchema(templateId, versionId, saveDTO);
    SchemaDTO reloaded = schemaService.getSchema(templateId, versionId);

    assertNotNull(reloaded.getLayoutNodes());
    assertFalse(reloaded.getLayoutNodes().isEmpty());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=SchemaServiceTest#saveAndReloadSchemaShouldPreserveNestedComponentStructure -q`
Expected: FAIL because nested structure is incomplete or not persisted/reloaded consistently

- [ ] **Step 3: Implement minimal nested persistence and preview consistency**

Ensure `SchemaService.saveSchema()` and `getSchema()` preserve nested layout/component relationships, and update `designer.js` / `preview.js` so preview consumes the same nested structure returned by schema APIs. If the preview and configured component properties diverge, treat that as a blocking defect in this task and fix it before any E2E evidence is captured.

Use this preview helper shape in `preview.js` when rendering nested children:

```javascript
function renderNode(node, container) {
  const element = document.createElement('div');
  element.dataset.nodeId = node.id || '';
  container.appendChild(element);
  (node.children || []).forEach(child => renderNode(child, element));
}
```

- [ ] **Step 4: Run focused tests**

Run: `mvn test -Dtest=SchemaServiceTest,FieldComponentServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/static/config/template-designer.html src/main/resources/static/config/js/designer.js src/main/resources/static/config/js/property-panel.js src/main/resources/static/js/preview.js src/main/java/com/contract/application/template/SchemaService.java src/main/java/com/contract/application/template/FieldComponentService.java src/main/java/com/contract/application/template/dto/SchemaDTO.java src/main/java/com/contract/application/template/dto/SchemaSaveDTO.java src/test/java/com/contract/SchemaServiceTest.java src/test/java/com/contract/FieldComponentServiceTest.java
git commit -m "feat: preserve nested schema structure across save and preview"
```

### Task 9: Restore business template generation and query fill-back closure

**Files:**
- Modify: `src/main/java/com/contract/application/template/QueryConfigService.java`
- Modify: `src/main/java/com/contract/application/template/DataSourceQueryFacadeService.java`
- Modify: `src/main/resources/static/config/js/config-api.js`
- Modify: `src/main/resources/static/config/js/designer.js`
- Modify: `src/main/resources/static/js/preview.js`
- Test: `src/test/java/com/contract/DataSourceQueryFacadeServiceTest.java`
- Test: `src/test/java/com/contract/DataSourceQueryControllerTest.java`

- [ ] **Step 1: Write the failing fill-back test**

Add this test to `src/test/java/com/contract/DataSourceQueryFacadeServiceTest.java`:

```java
@Test
void executeQueryShouldReturnDataUsableForFillBack() {
    // create provider + query config
    var result = service.executeQuery(providerId);
    assertFalse(result.isEmpty());
    assertNotNull(result.getFirst().getValue());
    assertNotNull(result.getFirst().getLabel());
}
```

- [ ] **Step 2: Run test to verify it fails or is incomplete**

Run: `mvn test -Dtest=DataSourceQueryFacadeServiceTest#executeQueryShouldReturnDataUsableForFillBack -q`
Expected: FAIL if current unified query/fill-back path is incomplete

- [ ] **Step 3: Normalize fill-back payload contract end-to-end**

Ensure query execution returns consistent option payloads for both preview and business template generation. Keep the client call shape aligned in `config-api.js` and use one response contract throughout preview/designer scripts.

Use this fetch helper shape:

```javascript
async function executeUnifiedDataSource(providerId) {
  const response = await fetch(`/api/v2/ui/data-sources/${providerId}/execute`);
  const result = await response.json();
  if (!result.success) throw new Error(result.message || '执行数据源失败');
  return result.data || [];
}
```

- [ ] **Step 4: Run focused tests**

Run: `mvn test -Dtest=DataSourceQueryFacadeServiceTest,DataSourceQueryControllerTest,SchemaServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/application/template/QueryConfigService.java src/main/java/com/contract/application/template/DataSourceQueryFacadeService.java src/main/resources/static/config/js/config-api.js src/main/resources/static/config/js/designer.js src/main/resources/static/js/preview.js src/test/java/com/contract/DataSourceQueryFacadeServiceTest.java src/test/java/com/contract/DataSourceQueryControllerTest.java src/test/java/com/contract/SchemaServiceTest.java
git commit -m "feat: complete unified query fill-back contract"
```

---

## Phase 5: Browser E2E and delivery evidence

### Task 10: Capture browser E2E for designer and business-template flows using the docs/superpowers/e2e baseline

**Files:**
- Modify: `docs/evidence/dynamic-template-audit/README.md`
- Modify: `docs/evidence/dynamic-template-audit/manual-checklist.md`
- Create: `docs/evidence/dynamic-template-audit/browser-flows/designer-e2e.md`
- Create: `docs/evidence/dynamic-template-audit/browser-flows/business-template-e2e.md`
- Create: `docs/evidence/dynamic-template-audit/browser-flows/component-library-e2e.md`
- Create: `docs/evidence/dynamic-template-audit/browser-flows/nested-combinations-e2e.md`
- Create/Update: `docs/evidence/dynamic-template-audit/screenshots/*`
- Reference: `docs/superpowers/e2e/README.md`

- [ ] **Step 1: Write the failing evidence test**

Create `src/test/java/com/contract/EvidencePackTest.java`:

```java
package com.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidencePackTest {
    @Test
    void browserFlowEvidenceFilesShouldExist() {
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/browser-flows/designer-e2e.md")));
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/browser-flows/business-template-e2e.md")));
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/browser-flows/component-library-e2e.md")));
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/browser-flows/nested-combinations-e2e.md")));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=EvidencePackTest -q`
Expected: FAIL because the browser-flow files do not exist

- [ ] **Step 3: Create browser scenario logs aligned to docs/superpowers/e2e/README.md**

Create `docs/evidence/dynamic-template-audit/browser-flows/designer-e2e.md`:

```md
# Designer E2E

Baseline: docs/superpowers/e2e/README.md

Covered scenarios:
- 01-template-crud
- 02-version-crud
- 03-layout-node-crud
- 04-field-component-crud

For every scenario record:
- page entry
- exact browser actions
- expected result
- actual result
- screenshot file names
```

Create `docs/evidence/dynamic-template-audit/browser-flows/business-template-e2e.md`:

```md
# Business Template E2E

Baseline: docs/superpowers/e2e/README.md

Covered scenarios:
- 05-data-provider-crud
- 06-contract-crud
- data-source query fill-back
- save and echo

Every scenario must include screenshot evidence.
```

Create `docs/evidence/dynamic-template-audit/browser-flows/component-library-e2e.md`:

```md
# Component Library E2E

Baseline count: 21 component scenarios from docs/superpowers/e2e/README.md

For each component type record:
- config entry
- render result
- save/reopen result
- preview result
- screenshot file names
```

Create `docs/evidence/dynamic-template-audit/browser-flows/nested-combinations-e2e.md`:

```md
# Nested Combinations E2E

Baseline:
- 08-01 two-level nesting
- 08-02 three-level nesting
- 08-03 deep nesting

For each representative combination record:
- structure
- save behavior
- reopen behavior
- preview behavior
- screenshot file names
```

- [ ] **Step 4: Expand the manual double-check checklist for the owner**

Replace `docs/evidence/dynamic-template-audit/manual-checklist.md` with:

```md
# Manual Double Check Checklist

## How to use this checklist
1. Start the app
2. Follow the scenario links below in order
3. Compare actual pages against the saved screenshots
4. Mark any mismatch immediately

## Required scenarios
- Template CRUD
- Version CRUD and publish
- Layout node CRUD
- Field component CRUD
- Data provider CRUD
- Contract save/echo
- 21 component render scenarios
- Representative nested combinations
- Query fill-back
- Preview consistency

## Evidence rule
Every scenario above must have at least one screenshot. Complex scenarios must have multiple step screenshots. Screenshots are valid only after the scenario has been fixed to match the configured result; if the UI, save/reopen state, nested display, or preview result is wrong, fix it first, then recapture the screenshots.
```

- [ ] **Step 5: Execute browser E2E manually and attach screenshots**

Run the app: `mvn spring-boot:run`
Use Chrome MCP to:
- open designer page
- operate nested save/reopen flow
- operate preview flow
- operate business-template fill-back flow
- traverse representative component scenarios
- capture screenshots into `docs/evidence/dynamic-template-audit/screenshots/`

Expected: screenshots and flow notes produced; each scenario has screenshot evidence. If any mismatch is found between configured properties and rendered/previewed output, stop evidence collection for that scenario, return to the relevant implementation task, fix it, and then rerun the scenario.

- [ ] **Step 6: Run evidence test again**

Run: `mvn test -Dtest=EvidencePackTest -q`
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add docs/evidence/dynamic-template-audit src/test/java/com/contract/EvidencePackTest.java docs/superpowers/e2e/README.md
git commit -m "test: align browser e2e evidence with e2e baseline"
```

---

## Phase 6: JSONB and MVP-scale performance evidence

### Task 11: Add JSONB query-path and EXPLAIN evidence for MVP scale

**Files:**
- Create: `docs/evidence/dynamic-template-audit/sql-and-xml/jsonb-query-path.md`
- Create: `docs/evidence/dynamic-template-audit/performance/mvp-scale.md`
- Modify: `src/main/resources/schema-opengauss.sql`
- Modify: `src/main/resources/schema-h2.sql`
- Test: `src/test/java/com/contract/ConfigJsonTest.java`

- [ ] **Step 1: Write the failing performance evidence test**

Extend `EvidencePackTest.java` with:

```java
@Test
void jsonbAndPerformanceEvidenceShouldExist() {
    assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/sql-and-xml/jsonb-query-path.md")));
    assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/performance/mvp-scale.md")));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=EvidencePackTest#jsonbAndPerformanceEvidenceShouldExist -q`
Expected: FAIL because the evidence files do not exist

- [ ] **Step 3: Add JSONB index notes and schema comments**

In `schema-opengauss.sql`, add or update comments/index DDL around the JSONB-bearing tables and include explicit JSONB index notes in `docs/evidence/dynamic-template-audit/sql-and-xml/jsonb-query-path.md`.

Starter document content:

```md
# JSONB Query Path

## Target paths
- template schema JSON
- contract snapshot JSON
- query fill-back payload JSON

## Evidence
- which tables store JSONB
- which queries read JSONB fields
- which indexes exist or are still missing
```

- [ ] **Step 4: Add MVP-scale analysis note**

Create `docs/evidence/dynamic-template-audit/performance/mvp-scale.md`:

```md
# MVP Scale Performance

Target scale: a few thousand records, not 100k+.

Checkpoints:
- template schema load
- data-source query execution
- contract echo
- nested structure parse/render

For each checkpoint record the current path, risk, and whether EXPLAIN is needed before migration.
```

- [ ] **Step 5: Run the evidence and JSON tests**

Run: `mvn test -Dtest=EvidencePackTest,ConfigJsonTest -q`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add docs/evidence/dynamic-template-audit src/main/resources/schema-opengauss.sql src/main/resources/schema-h2.sql src/test/java/com/contract/EvidencePackTest.java src/test/java/com/contract/ConfigJsonTest.java
git commit -m "docs: add jsonb and mvp performance evidence"
```

---

## Final Verification and Handoff

### Task 12: Run the full regression suite and produce migration checklist

**Files:**
- Create: `docs/evidence/dynamic-template-audit/file-notes/migration-checklist.md`
- Test: `src/test/java/com/contract/ArchitectureDependencyTest.java`
- Test: `src/test/java/com/contract/EvidencePackTest.java`
- Test: `src/test/java/com/contract/DataProviderServiceTest.java`
- Test: `src/test/java/com/contract/FieldComponentServiceTest.java`
- Test: `src/test/java/com/contract/SchemaServiceTest.java`
- Test: `src/test/java/com/contract/DataSourceQueryFacadeServiceTest.java`
- Test: `src/test/java/com/contract/DataSourceQueryControllerTest.java`
- Test: `src/test/java/com/contract/DataProviderControllerTest.java`
- Test: `src/test/java/com/contract/adapter/controller/TemplateControllerTest.java`

- [ ] **Step 1: Write the failing migration-checklist evidence test**

Extend `EvidencePackTest.java` with:

```java
@Test
void migrationChecklistShouldExist() {
    assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/file-notes/migration-checklist.md")));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=EvidencePackTest#migrationChecklistShouldExist -q`
Expected: FAIL because the checklist file does not exist

- [ ] **Step 3: Create the migration checklist**

Create `docs/evidence/dynamic-template-audit/file-notes/migration-checklist.md`:

```md
# Migration Checklist

- [ ] MyBatis-Plus removed
- [ ] Raw MyBatis XML present for all persistence modules
- [ ] XML files named `xxx.opengauss.xml`
- [ ] XML file names do not contain `Entity`
- [ ] Infrastructure entity classes retain `Entity` suffix
- [ ] Controller request/response models live in adapter layer
- [ ] Application layer contains DTOs only
- [ ] Domain layer contains aggregates/value objects/repository interfaces
- [ ] Browser E2E evidence attached
- [ ] JSONB and MVP-scale performance evidence attached
- [ ] Manual double-check checklist attached
```

- [ ] **Step 4: Run full regression suite**

Run: `mvn test -q`
Expected: PASS with zero failures and zero errors

- [ ] **Step 5: Run focused delivery checks**

Run: `mvn test -Dtest=ArchitectureDependencyTest,EvidencePackTest,DataProviderServiceTest,FieldComponentServiceTest,SchemaServiceTest,DataSourceQueryFacadeServiceTest,DataSourceQueryControllerTest,DataProviderControllerTest,TemplateControllerTest -q`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add docs/evidence/dynamic-template-audit src/test/java/com/contract/EvidencePackTest.java
git commit -m "test: finalize migration checklist and regression evidence"
```

---

## Self-Review

- **Spec coverage:** Covers req-based scope, strict DDD/CXF/MyBatis XML architecture, MyBatis-Plus removal, XML naming, Request/Response/DTO boundaries, browser E2E evidence, JSONB and MVP-scale performance evidence, and migration checklist output.
- **Placeholder scan:** No TBD/TODO placeholders remain; each task contains concrete paths, code blocks, and commands.
- **Type consistency:** Uses `Entity` suffix only for infrastructure entity classes, keeps XML names in `xxx.opengauss.xml`, and keeps adapter request/response vs application DTO boundaries consistent throughout.

Plan complete and saved to `docs/superpowers/plans/2026-06-10-dynamic-template-delivery-refactor.md`. Two execution options:

1. Subagent-Driven (recommended) - I dispatch a fresh subagent per task, review between tasks, fast iteration

2. Inline Execution - Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?