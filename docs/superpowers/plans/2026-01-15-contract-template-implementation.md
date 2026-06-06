# 合同模板动态渲染系统 V1.0 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建合同模板配置、动态界面渲染、合同数据保存、外部数据比对的完整系统

**Architecture:** 模块化单体应用，后端采用 Spring Boot + MyBatis-Plus，前端使用原生 HTML/JS，数据库使用 H2（测试）和 openGauss（生产），通过 JSONB 存储动态配置和快照数据

**Tech Stack:** Java 21, Spring Boot 3.5.14, MyBatis-Plus 3.5.5, H2/openGauss, 原生 HTML5/JavaScript ES6+

---

## 文件结构总览

```
dymic/
├── src/main/java/com/contract/
│   ├── ContractTemplateApplication.java          # Spring Boot 主类
│   ├── common/                                     # 公共模块
│   │   ├── exception/                              # 异常定义
│   │   │   ├── BizException.java
│   │   │   ├── ConcurrentModificationException.java
│   │   │   ├── ContractLockedException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── result/                                 # 统一响应
│   │   │   ├── Result.java
│   │   │   └── PageResult.java
│   │   ├── enums/                                  # 枚举定义
│   │   │   ├── TemplateStatusEnum.java
│   │   │   ├── VersionStatusEnum.java
│   │   │   ├── ProviderTypeEnum.java
│   │   │   ├── NodeTypeEnum.java
│   │   │   └── ComponentTypeEnum.java
│   │   └── util/                                   # 工具类
│   │       ├── SnowflakeIdWorker.java
│   │       └── JsonbHelper.java
│   ├── adapter/                                    # 适配层
│   │   ├── controller/                             # REST API
│   │   │   ├── TemplateController.java
│   │   │   ├── TemplateVersionController.java
│   │   │   ├── LayoutNodeController.java
│   │   │   ├── FieldDefController.java
│   │   │   ├── FieldComponentController.java
│   │   │   ├── DataProviderController.java
│   │   │   ├── QueryConfigController.java
│   │   │   ├── DetailTableController.java
│   │   │   ├── ActionConfigController.java
│   │   │   ├── RenderController.java
│   │   │   ├── ContractController.java
│   │   │   └── ExternalDataController.java
│   │   └── persistence/                            # 数据持久化
│   │       ├── mapper/
│   │       │   ├── TemplateMapper.java
│   │       │   ├── TemplateVersionMapper.java
│   │       │   ├── LayoutNodeMapper.java
│   │       │   ├── FieldDefMapper.java
│   │       │   ├── FieldComponentMapper.java
│   │       │   ├── DataProviderMapper.java
│   │       │   ├── QueryConfigMapper.java
│   │       │   ├── QueryParamMapper.java
│   │       │   ├── QueryFillRuleMapper.java
│   │       │   ├── DetailTableMapper.java
│   │       │   ├── ActionConfigMapper.java
│   │       │   ├── ContractMapper.java
│   │       │   ├── ContractSnapshotMapper.java
│   │       │   ├── ContractFieldValueMapper.java
│   │       │   ├── ContractDetailRowMapper.java
│   │       │   ├── ContractDetailFieldValueMapper.java
│   │       │   ├── ContractSearchIndexMapper.java
│   │       │   ├── ContractAttachmentMapper.java
│   │       │   ├── ExtSystemMapper.java
│   │       │   ├── ExtMessageInboxMapper.java
│   │       │   ├── ExtDataMappingMapper.java
│   │       │   ├── ExtDataMappingFieldMapper.java
│   │       │   ├── ExtCompareRecordMapper.java
│   │       │   └── ExtCompareItemMapper.java
│   │       └── entity/
│   │           ├── TemplateEntity.java
│   │           ├── TemplateVersionEntity.java
│   │           ├── LayoutNodeEntity.java
│   │           ├── FieldDefEntity.java
│   │           ├── FieldComponentEntity.java
│   │           ├── DataProviderEntity.java
│   │           ├── QueryConfigEntity.java
│   │           ├── QueryParamEntity.java
│   │           ├── QueryFillRuleEntity.java
│   │           ├── DetailTableEntity.java
│   │           ├── ActionConfigEntity.java
│   │           ├── ContractEntity.java
│   │           ├── ContractSnapshotEntity.java
│   │           ├── ContractFieldValueEntity.java
│   │           ├── ContractDetailRowEntity.java
│   │           ├── ContractDetailFieldValueEntity.java
│   │           ├── ContractSearchIndexEntity.java
│   │           ├── ContractAttachmentEntity.java
│   │           ├── ExtSystemEntity.java
│   │           ├── ExtMessageInboxEntity.java
│   │           ├── ExtDataMappingEntity.java
│   │           ├── ExtDataMappingFieldEntity.java
│   │           ├── ExtCompareRecordEntity.java
│   │           └── ExtCompareItemEntity.java
│   ├── application/                                # 应用服务层
│   │   ├── template/                               # 模板配置应用服务
│   │   │   ├── TemplateService.java
│   │   │   ├── TemplateVersionService.java
│   │   │   ├── LayoutNodeService.java
│   │   │   ├── FieldDefService.java
│   │   │   ├── FieldComponentService.java
│   │   │   ├── QueryConfigService.java
│   │   │   ├── DetailTableService.java
│   │   │   └── ActionConfigService.java
│   │   ├── render/                                 # 渲染应用服务
│   │   │   ├── RenderService.java
│   │   │   └── TemplateSchemaAssembler.java
│   │   ├── contract/                               # 合同应用服务
│   │   │   ├── ContractService.java
│   │   │   ├── ContractValidator.java
│   │   │   └── CanonicalNormalizer.java
│   │   ├── query/                                  # 查询应用服务
│   │   │   ├── QueryExecutionService.java
│   │   │   └── ParamBinder.java
│   │   ├── provider/                               # 数据提供方应用服务
│   │   │   ├── DataProviderService.java
│   │   │   ├── DataProviderExecutorFactory.java
│   │   │   └── executor/
│   │   │       ├── DataProviderExecutor.java
│   │   │       ├── StaticDataProviderExecutor.java
│   │   │       ├── DictDataProviderExecutor.java
│   │   │       ├── HttpDataProviderExecutor.java
│   │   │       ├── PlatformDataProviderExecutor.java
│   │   │       └── InternalDataProviderExecutor.java
│   │   ├── integration/                             # 外部集成应用服务
│   │   │   ├── ExternalDataService.java
│   │   │   └── MappingEngine.java
│   │   └── compare/                                # 对比应用服务
│   │       ├── CompareService.java
│   │       └── DiffEngine.java
│   ├── domain/                                      # 领域模型层
│   │   ├── template/                                # 模板聚合
│   │   │   ├── Template.java
│   │   │   ├── TemplateVersion.java
│   │   │   ├── LayoutNode.java
│   │   │   ├── FieldDef.java
│   │   │   ├── FieldComponent.java
│   │   │   ├── QueryConfig.java
│   │   │   ├── QueryParam.java
│   │   │   ├── QueryFillRule.java
│   │   │   ├── DetailTableConfig.java
│   │   │   └── ActionConfig.java
│   │   ├── contract/                                # 合同聚合
│   │   │   ├── Contract.java
│   │   │   ├── ContractSnapshot.java
│   │   │   ├── ContractFieldValue.java
│   │   │   ├── ContractDetailRow.java
│   │   │   └── ContractDetailFieldValue.java
│   │   ├── provider/                                # 数据提供方聚合
│   │   │   └── DataProvider.java
│   │   ├── integration/                             # 外部集成聚合
│   │   │   ├── ExtSystem.java
│   │   │   ├── ExtMessageInbox.java
│   │   │   ├── ExtDataMapping.java
│   │   │   ├── ExtDataMappingField.java
│   │   │   ├── ExtCompareRecord.java
│   │   │   └── ExtCompareItem.java
│   │   └── render/                                   # 渲染模型
│   │       ├── TemplateSchema.java
│   │       └── RenderPageResult.java
│   └── infrastructure/                              # 基础设施层
│       ├── id/                                       # ID 生成
│       │   └── SnowflakeIdGenerator.java
│       ├── json/                                     # JSON 工具
│       │   └── JsonbService.java
│       ├── datasource/                               # 数据源适配器
│       │   ├── HttpAdapter.java
│       │   └── PlatformAdapter.java
│       └── transaction/                              # 事务封装
│           └── TransactionHelper.java
├── src/main/resources/
│   ├── application.yml                               # 主配置文件
│   ├── application-test.yml                          # 测试配置文件
│   ├── schema-h2.sql                                 # H2 初始化脚本
│   ├── schema-opengauss.sql                         # openGauss 初始化脚本
│   ├── test-data.sql                                 # 测试数据
│   └── mapper/                                       # MyBatis XML
│       ├── TemplateMapper.xml
│       ├── TemplateVersionMapper.xml
│       ├── LayoutNodeMapper.xml
│       ├── FieldDefMapper.xml
│       ├── FieldComponentMapper.xml
│       ├── DataProviderMapper.xml
│       ├── QueryConfigMapper.xml
│       ├── QueryParamMapper.xml
│       ├── QueryFillRuleMapper.xml
│       ├── DetailTableMapper.xml
│       ├── ActionConfigMapper.xml
│       ├── ContractMapper.xml
│       ├── ContractSnapshotMapper.xml
│       ├── ContractFieldValueMapper.xml
│       ├── ContractDetailRowMapper.xml
│       ├── ContractDetailFieldValueMapper.xml
│       ├── ContractSearchIndexMapper.xml
│       ├── ContractAttachmentMapper.xml
│       ├── ExtSystemMapper.xml
│       ├── ExtMessageInboxMapper.xml
│       ├── ExtDataMappingMapper.xml
│       ├── ExtDataMappingFieldMapper.xml
│       ├── ExtCompareRecordMapper.xml
│       └── ExtCompareItemMapper.xml
├── src/test/java/com/contract/
│   ├── template/                                      # 模板配置测试
│   │   ├── TemplateServiceTest.java
│   │   ├── TemplateVersionServiceTest.java
│   │   ├── LayoutNodeServiceTest.java
│   │   ├── FieldDefServiceTest.java
│   │   ├── FieldComponentServiceTest.java
│   │   ├── DataProviderServiceTest.java
│   │   ├── QueryConfigServiceTest.java
│   │   ├── DetailTableServiceTest.java
│   │   └── ActionConfigServiceTest.java
│   ├── render/                                        # 渲染测试
│   │   ├── RenderServiceTest.java
│   │   └── TemplateSchemaAssemblerTest.java
│   ├── contract/                                      # 合同测试
│   │   ├── ContractServiceTest.java
│   │   ├── ContractValidatorTest.java
│   │   └── CanonicalNormalizerTest.java
│   ├── query/                                         # 查询测试
│   │   ├── QueryExecutionServiceTest.java
│   │   └── ParamBinderTest.java
│   ├── provider/                                      # 数据提供方测试
│   │   ├── DataProviderServiceTest.java
│   │   ├── StaticDataProviderExecutorTest.java
│   │   ├── DictDataProviderExecutorTest.java
│   │   ├── HttpDataProviderExecutorTest.java
│   │   ├── PlatformDataProviderExecutorTest.java
│   │   └── InternalDataProviderExecutorTest.java
│   ├── integration/                                   # 外部集成测试
│   │   ├── ExternalDataServiceTest.java
│   │   └── MappingEngineTest.java
│   └── compare/                                       # 对比测试
│       ├── CompareServiceTest.java
│       └── DiffEngineTest.java
├── demo/                                              # 前端 Demo
│   ├── index.html                                     # 入口页面
│   ├── template-config.html                           # 模板配置界面
│   ├── contract-new.html                              # 合同新增界面
│   ├── contract-edit.html                             # 合同编辑界面
│   ├── contract-list.html                             # 合同列表界面
│   ├── css/
│   │   └── style.css                                  # 样式
│   ├── js/
│   │   ├── api.js                                     # HTTP 请求封装
│   │   ├── template-api.js                            # 模板配置 API
│   │   ├── contract-api.js                            # 合同 API
│   │   ├── render.js                                  # 动态渲染
│   │   ├── form.js                                    # 表单处理
│   │   ├── detail-table.js                            # 明细表
│   │   └── modal.js                                    # 弹窗
│   └── lib/
│       └── frontend-design.min.css                    # 样式库
└── pom.xml                                            # Maven 配置
```

---

## 第一阶段：核心数据结构和基础 API

**目标**：建立数据库表结构和基础 CRUD API，验证架构设计

---

### Task 1: 项目初始化和基础配置

**Files:**
- Create: `pom.xml`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-test.yml`
- Create: `src/main/java/com/contract/ContractTemplateApplication.java`

- [ ] **Step 1: 创建 Maven 项目配置文件**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.14</version>
        <relativePath/>
    </parent>

    <groupId>com.contract</groupId>
    <artifactId>contract-template</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>Contract Template System</name>
    <description>合同模板动态渲染系统</description>

    <properties>
        <java.version>21</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <h2.version>2.2.224</h2.version>
    </properties>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>${h2.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- JSON Processing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>

        <!-- Utilities -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.8.26</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建主配置文件**

```yaml
spring:
  application:
    name: contract-template

  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: ASSIGN_ID
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath:mapper/*.xml

server:
  port: 8080

logging:
  level:
    com.contract: DEBUG
```

- [ ] **Step 3: 创建测试配置文件**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: ASSIGN_ID
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

- [ ] **Step 4: 创建 Spring Boot 主类**

```java
package com.contract;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.contract.adapter.persistence.mapper")
public class ContractTemplateApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContractTemplateApplication.class, args);
    }
}
```

- [ ] **Step 5: 验证项目启动**

Run: `mvn clean spring-boot:run`
Expected: 应用启动成功，监听 8080 端口

- [ ] **Step 6: 提交代码**

```bash
git add pom.xml src/main/resources/application*.yml src/main/java/com/contract/ContractTemplateApplication.java
git commit -m "feat: initialize Spring Boot project with MyBatis-Plus and H2"
```

---

### Task 2: 公共模块开发

**Files:**
- Create: `src/main/java/com/contract/common/exception/BizException.java`
- Create: `src/main/java/com/contract/common/exception/ConcurrentModificationException.java`
- Create: `src/main/java/com/contract/common/exception/ContractLockedException.java`
- Create: `src/main/java/com/contract/common/exception/GlobalExceptionHandler.java`
- Create: `src/main/java/com/contract/common/result/Result.java`
- Create: `src/main/java/com/contract/common/result/PageResult.java`
- Create: `src/main/java/com/contract/common/enums/TemplateStatusEnum.java`
- Create: `src/main/java/com/contract/common/enums/VersionStatusEnum.java`
- Create: `src/main/java/com/contract/common/enums/ProviderTypeEnum.java`

- [ ] **Step 1: 创建业务异常类**

```java
package com.contract.common.exception;

public class BizException extends RuntimeException {
    private final String code;

    public BizException(String message) {
        super(message);
        this.code = "BIZ_ERROR";
    }

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
```

- [ ] **Step 2: 创建并发修改异常**

```java
package com.contract.common.exception;

public class ConcurrentModificationException extends BizException {
    public ConcurrentModificationException(String message) {
        super("CONCURRENT_MODIFICATION", message);
    }
}
```

- [ ] **Step 3: 创建合同锁定异常**

```java
package com.contract.common.exception;

public class ContractLockedException extends BizException {
    public ContractLockedException(String message) {
        super("CONTRACT_LOCKED", message);
    }
}
```

- [ ] **Step 4: 创建全局异常处理器**

```java
package com.contract.common.exception;

import com.contract.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBizException(BizException e) {
        log.warn("Business exception: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return Result.fail("SYSTEM_ERROR", "系统异常");
    }
}
```

- [ ] **Step 5: 创建统一响应类**

```java
package com.contract.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode("SUCCESS");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(String code, String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
```

- [ ] **Step 6: 创建分页响应类**

```java
package com.contract.common.result;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        return result;
    }
}
```

- [ ] **Step 7: 创建模板状态枚举**

```java
package com.contract.common.enums;

import lombok.Getter;

@Getter
public enum TemplateStatusEnum {
    ENABLED("ENABLED", "启用"),
    DISABLED("DISABLED", "停用");

    private final String code;
    private final String desc;

    TemplateStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
```

- [ ] **Step 8: 创建版本状态枚举**

```java
package com.contract.common.enums;

import lombok.Getter;

@Getter
public enum VersionStatusEnum {
    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    DISABLED("DISABLED", "停用");

    private final String code;
    private final String desc;

    VersionStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
```

- [ ] **Step 9: 创建数据提供方类型枚举**

```java
package com.contract.common.enums;

import lombok.Getter;

@Getter
public enum ProviderTypeEnum {
    STATIC("STATIC", "静态选项"),
    DICT("DICT", "字典"),
    HTTP("HTTP", "HTTP接口"),
    PLATFORM("PLATFORM", "平台集成"),
    INTERNAL("INTERNAL", "内部查询");

    private final String code;
    private final String desc;

    ProviderTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
```

- [ ] **Step 10: 提交代码**

```bash
git add src/main/java/com/contract/common/
git commit -m "feat: add common modules (exception, result, enums)"
```

---

### Task 3: 雪花 ID 生成器

**Files:**
- Create: `src/main/java/com/contract/infrastructure/id/SnowflakeIdGenerator.java`
- Test: `src/test/java/com/contract/infrastructure/id/SnowflakeIdGeneratorTest.java`

- [ ] **Step 1: 编写失败测试**

```java
package com.contract.infrastructure.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    void testGenerateId() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        long id = generator.nextId();
        assertTrue(id > 0);
    }

    @Test
    void testGenerateMultipleIds() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        long id1 = generator.nextId();
        long id2 = generator.nextId();
        assertNotEquals(id1, id2);
    }

    @Test
    void testGenerateIdShouldBeUnique() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        long id1 = generator.nextId();
        long id2 = generator.nextId();
        long id3 = generator.nextId();
        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=SnowflakeIdGeneratorTest`
Expected: 测试失败（类不存在）

- [ ] **Step 3: 实现雪花 ID 生成器**

```java
package com.contract.infrastructure.id;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SnowflakeIdGenerator {
    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    public SnowflakeIdGenerator() {
        this(1L, 1L);
    }

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("Worker ID must be between 0 and " + MAX_WORKER_ID);
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID must be between 0 and " + MAX_DATACENTER_ID);
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        log.info("Snowflake ID Generator initialized with workerId={}, datacenterId={}", workerId, datacenterId);
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp << TIMESTAMP_LEFT_SHIFT))
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `mvn test -Dtest=SnowflakeIdGeneratorTest`
Expected: 所有测试通过

- [ ] **Step 5: 提交代码**

```bash
git add src/main/java/com/contract/infrastructure/id/SnowflakeIdGenerator.java src/test/java/com/contract/infrastructure/id/SnowflakeIdGeneratorTest.java
git commit -m "feat: implement Snowflake ID generator with tests"
```

---

### Task 4: 数据库表结构初始化

**Files:**
- Create: `src/main/resources/schema-h2.sql`

- [ ] **Step 1: 创建 H2 数据库初始化脚本（模板配置侧）**

```sql
-- 模板主表
CREATE TABLE t_ui_template (
    id BIGINT PRIMARY KEY,
    template_code VARCHAR(100) NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    template_desc VARCHAR(1000),
    biz_type VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    current_version_id BIGINT,
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_name VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_template_code UNIQUE (template_code)
);

COMMENT ON TABLE t_ui_template IS '模板主表';

-- 模板版本表
CREATE TABLE t_ui_template_version (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    version_no INTEGER NOT NULL,
    version_name VARCHAR(200),
    version_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    publish_time TIMESTAMP,
    publish_by BIGINT,
    schema_hash VARCHAR(128),
    remark VARCHAR(1000),
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_name VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_template_version UNIQUE (template_id, version_no)
);

CREATE INDEX idx_ui_template_version_status ON t_ui_template_version (template_id, version_status);

COMMENT ON TABLE t_ui_template_version IS '模板版本表';

-- 布局节点树
CREATE TABLE t_ui_layout_node (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    parent_id BIGINT,
    node_code VARCHAR(100) NOT NULL,
    node_name VARCHAR(200),
    node_type VARCHAR(50) NOT NULL,
    sort_no INTEGER NOT NULL DEFAULT 0,
    level_no INTEGER NOT NULL DEFAULT 1,
    node_path VARCHAR(1000),
    grid_x INTEGER,
    grid_y INTEGER,
    grid_w INTEGER,
    grid_h INTEGER,
    row_no INTEGER,
    col_no INTEGER,
    col_span INTEGER,
    row_span INTEGER,
    bind_type VARCHAR(50),
    bind_ref_id BIGINT,
    visible_rule JSON,
    readonly_rule JSON,
    props_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_layout_node_code UNIQUE (template_version_id, node_code)
);

CREATE INDEX idx_ui_layout_parent ON t_ui_layout_node (template_version_id, parent_id, sort_no);
CREATE INDEX idx_ui_layout_bind ON t_ui_layout_node (bind_type, bind_ref_id);

COMMENT ON TABLE t_ui_layout_node IS '布局节点树';

-- 字段定义表
CREATE TABLE t_ui_field_def (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    detail_table_id BIGINT,
    field_code VARCHAR(100) NOT NULL,
    field_path VARCHAR(500) NOT NULL,
    field_name_cn VARCHAR(200) NOT NULL,
    field_name_en VARCHAR(200),
    data_type VARCHAR(50) NOT NULL,
    value_type VARCHAR(50) NOT NULL DEFAULT 'SINGLE',
    required_default SMALLINT NOT NULL DEFAULT 0,
    searchable SMALLINT NOT NULL DEFAULT 0,
    indexable SMALLINT NOT NULL DEFAULT 0,
    search_index_column VARCHAR(100),
    default_value VARCHAR(1000),
    validate_rule JSON,
    props_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_field_path UNIQUE (template_version_id, field_path)
);

CREATE INDEX idx_ui_field_version ON t_ui_field_def (template_version_id);
CREATE INDEX idx_ui_field_detail ON t_ui_field_def (detail_table_id);

COMMENT ON TABLE t_ui_field_def IS '字段定义表';

-- 字段组件绑定表
CREATE TABLE t_ui_field_component (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    layout_node_id BIGINT NOT NULL,
    field_def_id BIGINT NOT NULL,
    component_type VARCHAR(50) NOT NULL,
    label_name VARCHAR(200),
    placeholder VARCHAR(300),
    required_rule JSON,
    readonly_rule JSON,
    visible_rule JSON,
    component_props JSON,
    data_provider_id BIGINT,
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_field_component UNIQUE (template_version_id, layout_node_id, field_def_id)
);

CREATE INDEX idx_ui_field_component_node ON t_ui_field_component (layout_node_id);
CREATE INDEX idx_ui_field_component_field ON t_ui_field_component (field_def_id);

COMMENT ON TABLE t_ui_field_component IS '字段组件绑定表';

-- 数据提供方配置（简化版，合并了 t_ui_data_option）
CREATE TABLE t_ui_data_provider (
    id BIGINT PRIMARY KEY,
    provider_code VARCHAR(100) NOT NULL,
    provider_name VARCHAR(200) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    config_json JSON NOT NULL,
    cache_enabled SMALLINT NOT NULL DEFAULT 0,
    cache_ttl_seconds INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_name VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_data_provider_code UNIQUE (provider_code)
);

COMMENT ON TABLE t_ui_data_provider IS '数据提供方配置';
```

- [ ] **Step 2: 追加合同数据侧表结构**

```sql
-- 合同主表
CREATE TABLE t_contract (
    id BIGINT PRIMARY KEY,
    contract_no VARCHAR(100),
    contract_name VARCHAR(300),
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    current_snapshot_id BIGINT,
    contract_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    data_version INTEGER NOT NULL DEFAULT 0,
    source_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    source_system_code VARCHAR(100),
    source_biz_id VARCHAR(200),
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_name VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_contract_template ON t_contract (template_id, template_version_id);
CREATE INDEX idx_contract_no ON t_contract (contract_no);
CREATE INDEX idx_contract_source ON t_contract (source_system_code, source_biz_id);

COMMENT ON TABLE t_contract IS '合同主表';

-- 合同快照
CREATE TABLE t_contract_data_snapshot (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    snapshot_no INTEGER NOT NULL,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    canonical_data JSON NOT NULL,
    source_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    source_message_id BIGINT,
    save_reason VARCHAR(500),
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_contract_snapshot_no UNIQUE (contract_id, snapshot_no)
);

CREATE INDEX idx_contract_snapshot_contract ON t_contract_data_snapshot (contract_id, snapshot_no DESC);

COMMENT ON TABLE t_contract_data_snapshot IS '合同完整JSON快照';

-- 字段值索引
CREATE TABLE t_contract_field_value (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    field_def_id BIGINT NOT NULL,
    field_path VARCHAR(500) NOT NULL,
    value_text CLOB,
    value_number DECIMAL(24, 6),
    value_date DATE,
    value_datetime TIMESTAMP,
    value_bool SMALLINT,
    value_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_contract_field_text ON t_contract_field_value (field_path, value_text);
CREATE INDEX idx_contract_field_number ON t_contract_field_value (field_path, value_number);
CREATE INDEX idx_contract_field_date ON t_contract_field_value (field_path, value_date);
CREATE INDEX idx_contract_field_contract ON t_contract_field_value (contract_id, snapshot_id);

COMMENT ON TABLE t_contract_field_value IS '合同字段值索引表';

-- 明细行当前投影
CREATE TABLE t_contract_detail_row (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    detail_table_id BIGINT NOT NULL,
    detail_code VARCHAR(100) NOT NULL,
    row_uid VARCHAR(100) NOT NULL,
    row_no INTEGER NOT NULL,
    row_data JSON NOT NULL,
    row_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_contract_detail_row_uid UNIQUE (contract_id, detail_code, row_uid)
);

CREATE INDEX idx_contract_detail_row ON t_contract_detail_row (contract_id, detail_code, row_no);

COMMENT ON TABLE t_contract_detail_row IS '合同明细行当前投影';

-- 明细字段索引
CREATE TABLE t_contract_detail_field_value (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    detail_row_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    detail_code VARCHAR(100) NOT NULL,
    row_uid VARCHAR(100) NOT NULL,
    field_def_id BIGINT NOT NULL,
    field_path VARCHAR(500) NOT NULL,
    value_text CLOB,
    value_number DECIMAL(24, 6),
    value_date DATE,
    value_datetime TIMESTAMP,
    value_bool SMALLINT,
    value_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_detail_field_text ON t_contract_detail_field_value (detail_code, field_path, value_text);
CREATE INDEX idx_detail_field_number ON t_contract_detail_field_value (detail_code, field_path, value_number);
CREATE INDEX idx_detail_field_contract ON t_contract_detail_field_value (contract_id, detail_code);

COMMENT ON TABLE t_contract_detail_field_value IS '明细字段索引表';

-- 当前查询宽表
CREATE TABLE t_contract_search_index (
    contract_id BIGINT PRIMARY KEY,
    contract_no VARCHAR(100),
    contract_name VARCHAR(300),
    supplier_id VARCHAR(100),
    supplier_name VARCHAR(300),
    contract_type VARCHAR(100),
    total_amount DECIMAL(24, 6),
    currency VARCHAR(20),
    sign_date DATE,
    effective_date DATE,
    expire_date DATE,
    contract_status VARCHAR(50),
    source_system_code VARCHAR(100),
    current_snapshot_id BIGINT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contract_search_supplier ON t_contract_search_index (supplier_name);
CREATE INDEX idx_contract_search_status ON t_contract_search_index (contract_status);
CREATE INDEX idx_contract_search_date ON t_contract_search_index (sign_date);

COMMENT ON TABLE t_contract_search_index IS '当前合同查询宽表';

-- 附件表
CREATE TABLE t_contract_attachment (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    snapshot_id BIGINT,
    file_id VARCHAR(200) NOT NULL,
    file_name VARCHAR(300) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    biz_path VARCHAR(500),
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_contract_attachment ON t_contract_attachment (contract_id, biz_path);

COMMENT ON TABLE t_contract_attachment IS '合同附件表';
```

- [ ] **Step 3: 追加查询配置和外部集成表结构**

```sql
-- 查询配置
CREATE TABLE t_ui_query_config (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    query_code VARCHAR(100) NOT NULL,
    query_name VARCHAR(200) NOT NULL,
    query_type VARCHAR(50) NOT NULL,
    data_provider_id BIGINT NOT NULL,
    trigger_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    result_mode VARCHAR(50) NOT NULL DEFAULT 'SINGLE_SELECT',
    bind_node_id BIGINT,
    page_size INTEGER DEFAULT 20,
    props_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_query_code UNIQUE (template_version_id, query_code)
);

CREATE INDEX idx_ui_query_version ON t_ui_query_config (template_version_id);
CREATE INDEX idx_ui_query_provider ON t_ui_query_config (data_provider_id);

COMMENT ON TABLE t_ui_query_config IS '查询配置表';

-- 查询参数绑定
CREATE TABLE t_ui_query_param (
    id BIGINT PRIMARY KEY,
    query_config_id BIGINT NOT NULL,
    param_name VARCHAR(100) NOT NULL,
    param_label VARCHAR(200),
    bind_source VARCHAR(50) NOT NULL,
    bind_path VARCHAR(500),
    component_type VARCHAR(50),
    required SMALLINT NOT NULL DEFAULT 0,
    default_value VARCHAR(1000),
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_query_param UNIQUE (query_config_id, param_name)
);

COMMENT ON TABLE t_ui_query_param IS '查询参数绑定表';

-- 查询回填规则
CREATE TABLE t_ui_query_fill_rule (
    id BIGINT PRIMARY KEY,
    query_config_id BIGINT NOT NULL,
    source_field VARCHAR(300) NOT NULL,
    target_scope VARCHAR(50) NOT NULL DEFAULT 'FORM',
    target_path VARCHAR(500) NOT NULL,
    fill_mode VARCHAR(50) NOT NULL DEFAULT 'OVERWRITE',
    transform_json JSON,
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ui_query_fill_rule ON t_ui_query_fill_rule (query_config_id, sort_no);

COMMENT ON TABLE t_ui_query_fill_rule IS '查询结果回填规则配置';

-- 明细表配置
CREATE TABLE t_ui_detail_table (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    detail_code VARCHAR(100) NOT NULL,
    detail_name VARCHAR(200) NOT NULL,
    detail_path VARCHAR(500) NOT NULL,
    row_key_strategy VARCHAR(50) NOT NULL DEFAULT 'CLIENT_UUID',
    min_rows INTEGER DEFAULT 0,
    max_rows INTEGER,
    allow_add SMALLINT NOT NULL DEFAULT 1,
    allow_edit SMALLINT NOT NULL DEFAULT 1,
    allow_delete SMALLINT NOT NULL DEFAULT 1,
    delete_mode VARCHAR(50) NOT NULL DEFAULT 'MARK_IN_DRAFT',
    props_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_detail_code UNIQUE (template_version_id, detail_code)
);

COMMENT ON TABLE t_ui_detail_table IS '明细表配置';

-- 动作配置
CREATE TABLE t_ui_action_config (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    action_code VARCHAR(100) NOT NULL,
    action_name VARCHAR(200) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    bind_node_id BIGINT,
    bind_query_id BIGINT,
    confirm_required SMALLINT NOT NULL DEFAULT 0,
    confirm_text VARCHAR(500),
    before_rule JSON,
    after_rule JSON,
    props_json JSON,
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_action_code UNIQUE (template_version_id, action_code)
);

COMMENT ON TABLE t_ui_action_config IS '动作配置表';

-- 外部系统表
CREATE TABLE t_ext_system (
    id BIGINT PRIMARY KEY,
    system_code VARCHAR(100) NOT NULL,
    system_name VARCHAR(200) NOT NULL,
    system_type VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ext_system_code UNIQUE (system_code)
);

COMMENT ON TABLE t_ext_system IS '外部系统登记表';

-- 外部消息入库
CREATE TABLE t_ext_message_inbox (
    id BIGINT PRIMARY KEY,
    ext_system_id BIGINT NOT NULL,
    external_msg_id VARCHAR(200) NOT NULL,
    source_biz_id VARCHAR(200),
    message_type VARCHAR(100) NOT NULL,
    raw_payload JSON NOT NULL,
    mapped_data JSON,
    mapping_id BIGINT,
    process_status VARCHAR(50) NOT NULL DEFAULT 'RECEIVED',
    error_message CLOB,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ext_message UNIQUE (ext_system_id, external_msg_id)
);

CREATE INDEX idx_ext_message_status ON t_ext_message_inbox (process_status, received_at);

COMMENT ON TABLE t_ext_message_inbox IS '外部消息幂等入库表';

-- 外部映射主表
CREATE TABLE t_ext_data_mapping (
    id BIGINT PRIMARY KEY,
    ext_system_id BIGINT NOT NULL,
    mapping_code VARCHAR(100) NOT NULL,
    mapping_name VARCHAR(200) NOT NULL,
    message_type VARCHAR(100) NOT NULL,
    target_template_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    sample_payload JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ext_mapping_code UNIQUE (ext_system_id, mapping_code)
);

COMMENT ON TABLE t_ext_data_mapping IS '外部数据映射主表';

-- 外部字段映射
CREATE TABLE t_ext_data_mapping_field (
    id BIGINT PRIMARY KEY,
    mapping_id BIGINT NOT NULL,
    source_path VARCHAR(500) NOT NULL,
    target_path VARCHAR(500) NOT NULL,
    data_type VARCHAR(50),
    required SMALLINT NOT NULL DEFAULT 0,
    transform_rule JSON,
    default_value VARCHAR(1000),
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ext_mapping_field ON t_ext_data_mapping_field (mapping_id, sort_no);

COMMENT ON TABLE t_ext_data_mapping_field IS '外部字段映射表';

-- 外部对比记录
CREATE TABLE t_ext_compare_record (
    id BIGINT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    contract_id BIGINT,
    compare_type VARCHAR(50) NOT NULL DEFAULT 'EXTERNAL_TO_CURRENT',
    compare_status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    external_data JSON NOT NULL,
    current_data JSON,
    diff_summary JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_ext_compare_record IS '外部数据对比记录';

-- 外部对比明细
CREATE TABLE t_ext_compare_item (
    id BIGINT PRIMARY KEY,
    compare_record_id BIGINT NOT NULL,
    field_path VARCHAR(500) NOT NULL,
    field_name_cn VARCHAR(200),
    external_value CLOB,
    current_value CLOB,
    diff_type VARCHAR(50) NOT NULL,
    accept_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ext_compare_item ON t_ext_compare_item (compare_record_id, field_path);

COMMENT ON TABLE t_ext_compare_item IS '外部数据对比明细';
```

- [ ] **Step 4: 配置 Spring Boot 自动执行初始化脚本**

更新 `src/main/resources/application.yml`，添加：

```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema-h2.sql
```

- [ ] **Step 5: 启动应用验证表结构创建**

Run: `mvn clean spring-boot:run`
Expected: 应用启动成功，H2 控制台可访问 http://localhost:8080/h2-console

- [ ] **Step 6: 提交代码**

```bash
git add src/main/resources/schema-h2.sql src/main/resources/application.yml
git commit -m "feat: add database schema for H2 with all tables"
```

---

### Task 5: 实体类开发（模板配置侧）

**Files:**
- Create: `src/main/java/com/contract/adapter/persistence/entity/TemplateEntity.java`
- Create: `src/main/java/com/contract/adapter/persistence/entity/TemplateVersionEntity.java`
- Create: `src/main/java/com/contract/adapter/persistence/entity/LayoutNodeEntity.java`
- Create: `src/main/java/com/contract/adapter/persistence/entity/FieldDefEntity.java`
- Create: `src/main/java/com/contract/adapter/persistence/entity/FieldComponentEntity.java`
- Create: `src/main/java/com/contract/adapter/persistence/entity/DataProviderEntity.java`

- [ ] **Step 1: 创建模板实体类**

```java
package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_ui_template")
public class TemplateEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String bizType;
    private String status;
    private Long currentVersionId;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private String createdName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedName;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 2: 创建模板版本实体类**

```java
package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_ui_template_version")
public class TemplateVersionEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long templateId;
    private Integer versionNo;
    private String versionName;
    private String versionStatus;
    private LocalDateTime publishTime;
    private Long publishBy;
    private String schemaHash;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private String createdName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedName;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 3: 创建布局节点实体类**

```java
package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "t_ui_layout_node", autoResultMap = true)
public class LayoutNodeEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long templateId;
    private Long templateVersionId;
    private Long parentId;
    private String nodeCode;
    private String nodeName;
    private String nodeType;
    private Integer sortNo;
    private Integer levelNo;
    private String nodePath;

    private Integer gridX;
    private Integer gridY;
    private Integer gridW;
    private Integer gridH;
    private Integer rowNo;
    private Integer colNo;
    private Integer colSpan;
    private Integer rowSpan;

    private String bindType;
    private Long bindRefId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object visibleRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object readonlyRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object propsJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 4: 创建字段定义实体类**

```java
package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "t_ui_field_def", autoResultMap = true)
public class FieldDefEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long templateId;
    private Long templateVersionId;
    private Long detailTableId;
    private String fieldCode;
    private String fieldPath;
    private String fieldNameCn;
    private String fieldNameEn;
    private String dataType;
    private String valueType;
    private Integer requiredDefault;
    private Integer searchable;
    private Integer indexable;
    private String searchIndexColumn;
    private String defaultValue;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object validateRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object propsJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 5: 创建字段组件绑定实体类**

```java
package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "t_ui_field_component", autoResultMap = true)
public class FieldComponentEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long templateId;
    private Long templateVersionId;
    private Long layoutNodeId;
    private Long fieldDefId;
    private String componentType;
    private String labelName;
    private String placeholder;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object requiredRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object readonlyRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object visibleRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object componentProps;

    private Long dataProviderId;
    private Integer sortNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 6: 创建数据提供方实体类**

```java
package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "t_ui_data_provider", autoResultMap = true)
public class DataProviderEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String providerCode;
    private String providerName;
    private String providerType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object configJson;

    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private String createdName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedName;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **Step 7: 提交代码**

```bash
git add src/main/java/com/contract/adapter/persistence/entity/
git commit -m "feat: add template configuration entity classes"
```

---

### Task 6: Mapper 接口和 XML 开发

**Files:**
- Create: `src/main/java/com/contract/adapter/persistence/mapper/TemplateMapper.java`
- Create: `src/main/java/com/contract/adapter/persistence/mapper/TemplateVersionMapper.java`
- Create: `src/main/resources/mapper/TemplateMapper.xml`
- Create: `src/main/resources/mapper/TemplateVersionMapper.xml`

- [ ] **Step 1: 创建 TemplateMapper 接口**

```java
package com.contract.adapter.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.adapter.persistence.entity.TemplateEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TemplateMapper extends BaseMapper<TemplateEntity> {
}
```

- [ ] **Step 2: 创建 TemplateVersionMapper 接口**

```java
package com.contract.adapter.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.adapter.persistence.entity.TemplateVersionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TemplateVersionMapper extends BaseMapper<TemplateVersionEntity> {

    @Select("SELECT * FROM t_ui_template_version WHERE template_id = #{templateId} AND version_status = 'PUBLISHED' ORDER BY version_no DESC LIMIT 1")
    TemplateVersionEntity findCurrentVersion(@Param("templateId") Long templateId);
}
```

- [ ] **Step 3: 创建 TemplateMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.contract.adapter.persistence.mapper.TemplateMapper">
</mapper>
```

- [ ] **Step 4: 创建 TemplateVersionMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.contract.adapter.persistence.mapper.TemplateVersionMapper">
</mapper>
```

- [ ] **Step 5: 提交代码**

```bash
git add src/main/java/com/contract/adapter/persistence/mapper/ src/main/resources/mapper/
git commit -m "feat: add Mapper interfaces for template configuration"
```

---

### Task 7: 领域模型开发（模板配置侧）

**Files:**
- Create: `src/main/java/com/contract/domain/template/Template.java`
- Create: `src/main/java/com/contract/domain/template/TemplateVersion.java`

- [ ] **Step 1: 创建 Template 领域模型**

```java
package com.contract.domain.template;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Template {
    private Long id;
    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String bizType;
    private String status;
    private Long currentVersionId;
    private Long createdBy;
    private String createdName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private LocalDateTime updatedAt;

    public static Template create(String templateCode, String templateName, String templateDesc, String bizType) {
        Template template = new Template();
        template.setTemplateCode(templateCode);
        template.setTemplateName(templateName);
        template.setTemplateDesc(templateDesc);
        template.setBizType(bizType);
        template.setStatus("ENABLED");
        return template;
    }

    public void disable() {
        this.status = "DISABLED";
    }

    public void enable() {
        this.status = "ENABLED";
    }

    public void setCurrentVersion(Long versionId) {
        this.currentVersionId = versionId;
    }
}
```

- [ ] **Step 2: 创建 TemplateVersion 领域模型**

```java
package com.contract.domain.template;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TemplateVersion {
    private Long id;
    private Long templateId;
    private Integer versionNo;
    private String versionName;
    private String versionStatus;
    private LocalDateTime publishTime;
    private Long publishBy;
    private String schemaHash;
    private String remark;
    private Long createdBy;
    private String createdName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private LocalDateTime updatedAt;

    public static TemplateVersion createDraft(Long templateId, Integer versionNo, String versionName) {
        TemplateVersion version = new TemplateVersion();
        version.setTemplateId(templateId);
        version.setVersionNo(versionNo);
        version.setVersionName(versionName);
        version.setVersionStatus("DRAFT");
        return version;
    }

    public void publish(Long publishBy) {
        this.versionStatus = "PUBLISHED";
        this.publishTime = LocalDateTime.now();
        this.publishBy = publishBy;
    }

    public void disable() {
        this.versionStatus = "DISABLED";
    }

    public boolean isDraft() {
        return "DRAFT".equals(this.versionStatus);
    }

    public boolean isPublished() {
        return "PUBLISHED".equals(this.versionStatus);
    }
}
```

- [ ] **Step 3: 提交代码**

```bash
git add src/main/java/com/contract/domain/template/
git commit -m "feat: add domain models for template configuration"
```

---

### Task 8: 模板服务层开发

**Files:**
- Create: `src/main/java/com/contract/application/template/TemplateService.java`
- Test: `src/test/java/com/contract/application/template/TemplateServiceTest.java`

- [ ] **Step 1: 编写失败测试**

```java
package com.contract.application.template;

import com.contract.domain.template.Template;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TemplateServiceTest {

    @Autowired
    private TemplateService templateService;

    @Test
    void testCreateTemplate() {
        Template template = templateService.createTemplate(
            "PURCHASE_CONTRACT",
            "采购合同模板",
            "用于采购业务",
            "PURCHASE"
        );

        assertNotNull(template.getId());
        assertEquals("PURCHASE_CONTRACT", template.getTemplateCode());
        assertEquals("ENABLED", template.getStatus());
    }

    @Test
    void testGetTemplateByCode() {
        templateService.createTemplate("TEST_CONTRACT", "测试合同", "测试用", "TEST");

        Template template = templateService.getByCode("TEST_CONTRACT");
        assertNotNull(template);
        assertEquals("测试合同", template.getTemplateName());
    }

    @Test
    void testDisableTemplate() {
        Template template = templateService.createTemplate(
            "DISABLE_TEST",
            "停用测试",
            "测试停用",
            "TEST"
        );

        templateService.disable(template.getId());

        Template disabled = templateService.getById(template.getId());
        assertEquals("DISABLED", disabled.getStatus());
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=TemplateServiceTest`
Expected: 测试失败（服务类不存在）

- [ ] **Step 3: 实现 TemplateService**

```java
package com.contract.application.template;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.adapter.persistence.entity.TemplateEntity;
import com.contract.adapter.persistence.mapper.TemplateMapper;
import com.contract.common.exception.BizException;
import com.contract.domain.template.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateMapper templateMapper;

    @Transactional
    public Template createTemplate(String templateCode, String templateName, String templateDesc, String bizType) {
        // 检查编码是否已存在
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getTemplateCode, templateCode);
        if (templateMapper.selectCount(wrapper) > 0) {
            throw new BizException("模板编码已存在：" + templateCode);
        }

        Template template = Template.create(templateCode, templateName, templateDesc, bizType);

        TemplateEntity entity = new TemplateEntity();
        entity.setTemplateCode(template.getTemplateCode());
        entity.setTemplateName(template.getTemplateName());
        entity.setTemplateDesc(template.getTemplateDesc());
        entity.setBizType(template.getBizType());
        entity.setStatus(template.getStatus());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        templateMapper.insert(entity);
        template.setId(entity.getId());

        return template;
    }

    public Template getById(Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        if (entity == null) {
            throw new BizException("模板不存在：" + id);
        }
        return toDomain(entity);
    }

    public Template getByCode(String templateCode) {
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getTemplateCode, templateCode);
        TemplateEntity entity = templateMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BizException("模板不存在：" + templateCode);
        }
        return toDomain(entity);
    }

    @Transactional
    public void disable(Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        if (entity == null) {
            throw new BizException("模板不存在：" + id);
        }
        entity.setStatus("DISABLED");
        entity.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(entity);
    }

    @Transactional
    public void enable(Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        if (entity == null) {
            throw new BizException("模板不存在：" + id);
        }
        entity.setStatus("ENABLED");
        entity.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(entity);
    }

    private Template toDomain(TemplateEntity entity) {
        Template template = new Template();
        template.setId(entity.getId());
        template.setTemplateCode(entity.getTemplateCode());
        template.setTemplateName(entity.getTemplateName());
        template.setTemplateDesc(entity.getTemplateDesc());
        template.setBizType(entity.getBizType());
        template.setStatus(entity.getStatus());
        template.setCurrentVersionId(entity.getCurrentVersionId());
        template.setCreatedBy(entity.getCreatedBy());
        template.setCreatedName(entity.getCreatedName());
        template.setCreatedAt(entity.getCreatedAt());
        template.setUpdatedBy(entity.getUpdatedBy());
        template.setUpdatedName(entity.getUpdatedName());
        template.setUpdatedAt(entity.getUpdatedAt());
        return template;
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `mvn test -Dtest=TemplateServiceTest`
Expected: 所有测试通过

- [ ] **Step 5: 提交代码**

```bash
git add src/main/java/com/contract/application/template/TemplateService.java src/test/java/com/contract/application/template/TemplateServiceTest.java
git commit -m "feat: implement TemplateService with TDD approach"
```

---

### Task 9: 模板版本服务层开发

**Files:**
- Create: `src/main/java/com/contract/application/template/TemplateVersionService.java`
- Test: `src/test/java/com/contract/application/template/TemplateVersionServiceTest.java`

- [ ] **Step 1: 编写失败测试**

```java
package com.contract.application.template;

import com.contract.domain.template.TemplateVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TemplateVersionServiceTest {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateVersionService versionService;

    @Test
    void testCreateDraftVersion() {
        Long templateId = templateService.createTemplate(
            "VERSION_TEST",
            "版本测试",
            "测试版本",
            "TEST"
        ).getId();

        TemplateVersion version = versionService.createDraft(templateId, 1, "V1.0");

        assertNotNull(version.getId());
        assertEquals(templateId, version.getTemplateId());
        assertEquals(1, version.getVersionNo());
        assertEquals("DRAFT", version.getVersionStatus());
    }

    @Test
    void testPublishVersion() {
        Long templateId = templateService.createTemplate(
            "PUBLISH_TEST",
            "发布测试",
            "测试发布",
            "TEST"
        ).getId();

        Long versionId = versionService.createDraft(templateId, 1, "V1.0").getId();
        versionService.publish(versionId, 1001L);

        TemplateVersion published = versionService.getById(versionId);
        assertEquals("PUBLISHED", published.getVersionStatus());
        assertNotNull(published.getPublishTime());
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=TemplateVersionServiceTest`
Expected: 测试失败（服务类不存在）

- [ ] **Step 3: 实现 TemplateVersionService**

```java
package com.contract.application.template;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.adapter.persistence.entity.TemplateVersionEntity;
import com.contract.adapter.persistence.mapper.TemplateVersionMapper;
import com.contract.common.exception.BizException;
import com.contract.domain.template.TemplateVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TemplateVersionService {

    private final TemplateVersionMapper versionMapper;
    private final TemplateService templateService;

    @Transactional
    public TemplateVersion createDraft(Long templateId, Integer versionNo, String versionName) {
        // 检查模板是否存在
        templateService.getById(templateId);

        // 检查版本号是否已存在
        LambdaQueryWrapper<TemplateVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateVersionEntity::getTemplateId, templateId)
               .eq(TemplateVersionEntity::getVersionNo, versionNo);
        if (versionMapper.selectCount(wrapper) > 0) {
            throw new BizException("版本号已存在：" + versionNo);
        }

        TemplateVersion version = TemplateVersion.createDraft(templateId, versionNo, versionName);

        TemplateVersionEntity entity = new TemplateVersionEntity();
        entity.setTemplateId(version.getTemplateId());
        entity.setVersionNo(version.getVersionNo());
        entity.setVersionName(version.getVersionName());
        entity.setVersionStatus(version.getVersionStatus());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        versionMapper.insert(entity);
        version.setId(entity.getId());

        return version;
    }

    public TemplateVersion getById(Long id) {
        TemplateVersionEntity entity = versionMapper.selectById(id);
        if (entity == null) {
            throw new BizException("版本不存在：" + id);
        }
        return toDomain(entity);
    }

    @Transactional
    public void publish(Long versionId, Long publishBy) {
        TemplateVersionEntity entity = versionMapper.selectById(versionId);
        if (entity == null) {
            throw new BizException("版本不存在：" + versionId);
        }

        if (!"DRAFT".equals(entity.getVersionStatus())) {
            throw new BizException("只有草稿状态才能发布");
        }

        entity.setVersionStatus("PUBLISHED");
        entity.setPublishTime(LocalDateTime.now());
        entity.setPublishBy(publishBy);
        entity.setUpdatedAt(LocalDateTime.now());
        versionMapper.updateById(entity);

        // 更新模板的当前版本
        templateService.getById(entity.getTemplateId()).setCurrentVersion(versionId);
    }

    public TemplateVersion findCurrentVersion(Long templateId) {
        TemplateVersionEntity entity = versionMapper.findCurrentVersion(templateId);
        if (entity == null) {
            return null;
        }
        return toDomain(entity);
    }

    private TemplateVersion toDomain(TemplateVersionEntity entity) {
        TemplateVersion version = new TemplateVersion();
        version.setId(entity.getId());
        version.setTemplateId(entity.getTemplateId());
        version.setVersionNo(entity.getVersionNo());
        version.setVersionName(entity.getVersionName());
        version.setVersionStatus(entity.getVersionStatus());
        version.setPublishTime(entity.getPublishTime());
        version.setPublishBy(entity.getPublishBy());
        version.setSchemaHash(entity.getSchemaHash());
        version.setRemark(entity.getRemark());
        version.setCreatedBy(entity.getCreatedBy());
        version.setCreatedName(entity.getCreatedName());
        version.setCreatedAt(entity.getCreatedAt());
        version.setUpdatedBy(entity.getUpdatedBy());
        version.setUpdatedName(entity.getUpdatedName());
        version.setUpdatedAt(entity.getUpdatedAt());
        return version;
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `mvn test -Dtest=TemplateVersionServiceTest`
Expected: 所有测试通过

- [ ] **Step 5: 提交代码**

```bash
git add src/main/java/com/contract/application/template/TemplateVersionService.java src/test/java/com/contract/application/template/TemplateVersionServiceTest.java
git commit -m "feat: implement TemplateVersionService with TDD approach"
```

---

### Task 10: REST Controller 开发

**Files:**
- Create: `src/main/java/com/contract/adapter/controller/TemplateController.java`
- Create: `src/main/java/com/contract/adapter/controller/TemplateVersionController.java`
- Test: `src/test/java/com/contract/adapter/controller/TemplateControllerTest.java`

- [ ] **Step 1: 编写失败测试**

```java
package com.contract.adapter.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateTemplate() throws Exception {
        String requestBody = """
            {
              "templateCode": "API_TEST",
              "templateName": "API测试模板",
              "templateDesc": "测试用",
              "bizType": "TEST"
            }
            """;

        mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.templateCode").value("API_TEST"));
    }

    @Test
    void testGetTemplateByCode() throws Exception {
        // 先创建
        String requestBody = """
            {
              "templateCode": "GET_TEST",
              "templateName": "查询测试",
              "templateDesc": "测试查询",
              "bizType": "TEST"
            }
            """;

        mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isOk());

        // 再查询
        mockMvc.perform(get("/api/templates/code/GET_TEST"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.templateName").value("查询测试"));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `mvn test -Dtest=TemplateControllerTest`
Expected: 测试失败（Controller 不存在）

- [ ] **Step 3: 实现 TemplateController**

```java
package com.contract.adapter.controller;

import com.contract.application.template.TemplateService;
import com.contract.common.result.Result;
import com.contract.domain.template.Template;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public Result<Template> create(@RequestBody CreateTemplateRequest request) {
        Template template = templateService.createTemplate(
            request.getTemplateCode(),
            request.getTemplateName(),
            request.getTemplateDesc(),
            request.getBizType()
        );
        return Result.ok(template);
    }

    @GetMapping("/{id}")
    public Result<Template> getById(@PathVariable Long id) {
        Template template = templateService.getById(id);
        return Result.ok(template);
    }

    @GetMapping("/code/{code}")
    public Result<Template> getByCode(@PathVariable String code) {
        Template template = templateService.getByCode(code);
        return Result.ok(template);
    }

    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        templateService.disable(id);
        return Result.ok(null);
    }

    @PostMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        templateService.enable(id);
        return Result.ok(null);
    }

    @Data
    public static class CreateTemplateRequest {
        private String templateCode;
        private String templateName;
        private String templateDesc;
        private String bizType;
    }
}
```

- [ ] **Step 4: 实现 TemplateVersionController**

```java
package com.contract.adapter.controller;

import com.contract.application.template.TemplateVersionService;
import com.contract.common.result.Result;
import com.contract.domain.template.TemplateVersion;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateVersionController {

    private final TemplateVersionService versionService;

    @PostMapping("/{templateId}/versions")
    public Result<TemplateVersion> createDraft(
        @PathVariable Long templateId,
        @RequestBody CreateVersionRequest request
    ) {
        TemplateVersion version = versionService.createDraft(
            templateId,
            request.getVersionNo(),
            request.getVersionName()
        );
        return Result.ok(version);
    }

    @PostMapping("/versions/{versionId}/publish")
    public Result<Void> publish(@PathVariable Long versionId) {
        versionService.publish(versionId, 1001L); // TODO: 从上下文获取用户ID
        return Result.ok(null);
    }

    @GetMapping("/{templateId}/versions/current")
    public Result<TemplateVersion> getCurrentVersion(@PathVariable Long templateId) {
        TemplateVersion version = versionService.findCurrentVersion(templateId);
        return Result.ok(version);
    }

    @Data
    public static class CreateVersionRequest {
        private Integer versionNo;
        private String versionName;
    }
}
```

- [ ] **Step 5: 运行测试验证通过**

Run: `mvn test -Dtest=TemplateControllerTest`
Expected: 所有测试通过

- [ ] **Step 6: 提交代码**

```bash
git add src/main/java/com/contract/adapter/controller/ src/test/java/com/contract/adapter/controller/
git commit -m "feat: implement REST controllers for template management"
```

---

## 第一阶段完成检查点

至此，第一阶段的核心数据结构和基础 API 已经完成。可以验证：

1. ✅ 数据库表结构创建成功
2. ✅ 基础实体类和 Mapper 开发完成
3. ✅ 领域模型定义清晰
4. ✅ 服务层 CRUD 逻辑实现并通过测试
5. ✅ REST API 可以正常调用

**验证命令**：

```bash
# 运行所有测试
mvn test

# 启动应用
mvn spring-boot:run

# 测试 API
curl -X POST http://localhost:8080/api/templates \
  -H "Content-Type: application/json" \
  -d '{"templateCode":"TEST","templateName":"测试模板","templateDesc":"测试","bizType":"TEST"}'
```

---

## 后续阶段概要

由于完整实现计划篇幅过长，以下是后续阶段的任务概要。详细的任务步骤将在后续补充。

### 第二阶段：渲染和查询逻辑

**核心任务**：
- Task 11: TemplateSchemaAssembler 开发
- Task 12: 数据提供方执行器开发（Static/Dict/Http/Platform/Internal）
- Task 13: QueryExecutionService 开发
- Task 14: RenderService 开发
- Task 15: 前端 Demo（合同新增页面）

### 第三阶段：合同保存和回显

**核心任务**：
- Task 16: 合同实体类和 Mapper 开发
- Task 17: ContractValidator 开发
- Task 18: CanonicalNormalizer 开发
- Task 19: ContractService 保存逻辑开发
- Task 20: 明细表处理逻辑开发
- Task 21: 并发控制实现
- Task 22: 前端 Demo（合同编辑页面）

### 第四阶段：外部数据集成

**核心任务**：
- Task 23: 外部系统实体类和 Mapper 开发
- Task 24: MappingEngine 开发
- Task 25: DiffEngine 开发
- Task 26: ExternalDataService 开发
- Task 27: 外部数据自动应用逻辑

### 第五阶段：配置界面完善

**核心任务**：
- Task 28: 前端 Demo（模板配置界面）
- Task 29: 前端 Demo（布局节点配置界面）
- Task 30: 前端 Demo（字段和组件配置界面）
- Task 31: 前端 Demo（查询配置界面）
- Task 32: 前端 Demo（数据提供方配置界面）
- Task 33: 完整流程端到端测试

---

## 技术债务和优化点

在实现过程中需要注意以下技术债务：

1. **用户上下文获取**：当前硬编码用户ID，需要集成真实的用户上下文
2. **异常处理细化**：需要根据业务场景定义更细化的异常类型
3. **日志记录**：需要在关键操作处添加日志记录
4. **参数校验**：需要在 Controller 层添加参数校验注解
5. **性能优化**：批量插入、索引优化等
6. **安全控制**：权限校验、数据隔离等

---

**注意**：这是一个持续演进的计划，后续阶段的具体任务步骤将在实施过程中补充完善。建议采用迭代方式，每完成一个阶段后进行验收和调整。

---

由于篇幅限制，我将继续在下一个文件中完成实现计划的剩余部分。让我先保存当前的进度：

