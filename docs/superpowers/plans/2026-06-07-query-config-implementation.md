# 查询配置功能实现计划（阶段一）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现模板配置界面的查询配置CRUD功能，支持业务人员配置查询按钮的数据源绑定、参数映射和回填规则。

**Architecture:** 采用四层架构：Controller → Service → Repository → Mapper。领域对象定义在domain层，Entity定义在infrastructure层，使用MapStruct进行转换。查询配置与参数、回填规则是一对多关系。

**Tech Stack:** Spring Boot 3.5, MyBatis-Plus 3.5, MapStruct 1.6, Lombok, H2 (测试), openGauss (生产)

---

## 文件结构

### 领域层
- `domain/template/QueryConfig.java` - 查询配置领域对象
- `domain/template/QueryParam.java` - 查询参数领域对象
- `domain/template/QueryFillRule.java` - 回填规则领域对象
- `domain/template/repository/QueryConfigRepository.java` - 查询配置仓储接口
- `domain/template/repository/QueryParamRepository.java` - 查询参数仓储接口
- `domain/template/repository/QueryFillRuleRepository.java` - 回填规则仓储接口

### 基础设施层
- `infrastructure/persistence/entity/QueryConfigEntity.java` - 查询配置实体
- `infrastructure/persistence/entity/QueryParamEntity.java` - 查询参数实体
- `infrastructure/persistence/entity/QueryFillRuleEntity.java` - 回填规则实体
- `infrastructure/persistence/mapper/QueryConfigMapper.java` - 查询配置Mapper
- `infrastructure/persistence/mapper/QueryParamMapper.java` - 查询参数Mapper
- `infrastructure/persistence/mapper/QueryFillRuleMapper.java` - 回填规则Mapper
- `infrastructure/persistence/repository/QueryConfigRepositoryImpl.java` - 查询配置仓储实现
- `infrastructure/persistence/repository/QueryParamRepositoryImpl.java` - 查询参数仓储实现
- `infrastructure/persistence/repository/QueryFillRuleRepositoryImpl.java` - 回填规则仓储实现
- `infrastructure/persistence/convert/EntityQueryConfigConverter.java` - Entity转换器
- `infrastructure/persistence/convert/EntityQueryParamConverter.java` - 参数转换器
- `infrastructure/persistence/convert/EntityQueryFillRuleConverter.java` - 回填规则转换器

### 应用层
- `application/template/QueryConfigService.java` - 查询配置应用服务
- `application/template/dto/QueryConfigDTO.java` - 查询配置DTO
- `application/template/dto/QueryConfigCreateRequest.java` - 创建请求
- `application/template/dto/QueryConfigUpdateRequest.java` - 更新请求
- `application/template/dto/QueryParamDTO.java` - 查询参数DTO
- `application/template/dto/QueryFillRuleDTO.java` - 回填规则DTO
- `application/template/convert/QueryConfigConverter.java` - DTO转换器

### 接口层
- `adapter/controller/QueryConfigController.java` - 查询配置控制器

### 测试层
- `test/java/com/contract/QueryConfigServiceTest.java` - 服务层测试
- `test/java/com/contract/QueryConfigControllerTest.java` - 控制器层测试

---

## Task 1: 领域对象定义

**Files:**
- Create: `src/main/java/com/contract/domain/template/QueryConfig.java`
- Create: `src/main/java/com/contract/domain/template/QueryParam.java`
- Create: `src/main/java/com/contract/domain/template/QueryFillRule.java`

- [ ] **Step 1: 创建 QueryConfig 领域对象**

```java
package com.contract.domain.template;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 查询配置领域对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryConfig {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private String queryCode;
    private String queryName;
    private String queryType;
    private Long dataProviderId;
    private String triggerType;
    private String resultMode;
    private Long bindNodeId;
    private Integer pageSize;
    private String propsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 创建 QueryParam 领域对象**

```java
package com.contract.domain.template;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 查询参数领域对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParam {
    private Long id;
    private Long queryConfigId;
    private String paramName;
    private String paramLabel;
    private String bindSource;
    private String bindPath;
    private String componentType;
    private Integer required;
    private String defaultValue;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 3: 创建 QueryFillRule 领域对象**

```java
package com.contract.domain.template;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 查询回填规则领域对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryFillRule {
    private Long id;
    private Long queryConfigId;
    private String sourceField;
    private String targetScope;
    private String targetPath;
    private String fillMode;
    private String transformJson;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 4: 提交领域对象**

```bash
git add src/main/java/com/contract/domain/template/QueryConfig.java
git add src/main/java/com/contract/domain/template/QueryParam.java
git add src/main/java/com/contract/domain/template/QueryFillRule.java
git commit -m "feat: add QueryConfig, QueryParam, QueryFillRule domain objects"
```

---

## Task 2: Repository接口定义

**Files:**
- Create: `src/main/java/com/contract/domain/template/repository/QueryConfigRepository.java`
- Create: `src/main/java/com/contract/domain/template/repository/QueryParamRepository.java`
- Create: `src/main/java/com/contract/domain/template/repository/QueryFillRuleRepository.java`

- [ ] **Step 1: 创建 QueryConfigRepository 接口**

```java
package com.contract.domain.template.repository;

import com.contract.domain.template.QueryConfig;
import java.util.List;

/**
 * 查询配置仓储接口
 */
public interface QueryConfigRepository {
    QueryConfig save(QueryConfig config);
    QueryConfig findById(Long id);
    List<QueryConfig> findByTemplateVersionId(Long versionId);
    void update(QueryConfig config);
    void deleteById(Long id);
}
```

- [ ] **Step 2: 创建 QueryParamRepository 接口**

```java
package com.contract.domain.template.repository;

import com.contract.domain.template.QueryParam;
import java.util.List;

/**
 * 查询参数仓储接口
 */
public interface QueryParamRepository {
    QueryParam save(QueryParam param);
    List<QueryParam> findByQueryConfigId(Long queryConfigId);
    void deleteByQueryConfigId(Long queryConfigId);
}
```

- [ ] **Step 3: 创建 QueryFillRuleRepository 接口**

```java
package com.contract.domain.template.repository;

import com.contract.domain.template.QueryFillRule;
import java.util.List;

/**
 * 查询回填规则仓储接口
 */
public interface QueryFillRuleRepository {
    QueryFillRule save(QueryFillRule rule);
    List<QueryFillRule> findByQueryConfigId(Long queryConfigId);
    void deleteByQueryConfigId(Long queryConfigId);
}
```

- [ ] **Step 4: 提交Repository接口**

```bash
git add src/main/java/com/contract/domain/template/repository/QueryConfigRepository.java
git add src/main/java/com/contract/domain/template/repository/QueryParamRepository.java
git add src/main/java/com/contract/domain/template/repository/QueryFillRuleRepository.java
git commit -m "feat: add QueryConfig, QueryParam, QueryFillRule repository interfaces"
```

---

## Task 3: Entity和Mapper定义

**Files:**
- Create: `src/main/java/com/contract/infrastructure/persistence/entity/QueryConfigEntity.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/entity/QueryParamEntity.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/entity/QueryFillRuleEntity.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/mapper/QueryConfigMapper.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/mapper/QueryParamMapper.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/mapper/QueryFillRuleMapper.java`

- [ ] **Step 1: 创建 QueryConfigEntity**

```java
package com.contract.infrastructure.persistence.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 查询配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryConfigEntity {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private String queryCode;
    private String queryName;
    private String queryType;
    private Long dataProviderId;
    private String triggerType;
    private String resultMode;
    private Long bindNodeId;
    private Integer pageSize;
    private String propsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
```

- [ ] **Step 2: 创建 QueryParamEntity**

```java
package com.contract.infrastructure.persistence.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 查询参数实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParamEntity {
    private Long id;
    private Long queryConfigId;
    private String paramName;
    private String paramLabel;
    private String bindSource;
    private String bindPath;
    private String componentType;
    private Integer required;
    private String defaultValue;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
```

- [ ] **Step 3: 创建 QueryFillRuleEntity**

```java
package com.contract.infrastructure.persistence.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 查询回填规则实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryFillRuleEntity {
    private Long id;
    private Long queryConfigId;
    private String sourceField;
    private String targetScope;
    private String targetPath;
    private String fillMode;
    private String transformJson;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
```

- [ ] **Step 4: 创建 QueryConfigMapper**

```java
package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.QueryConfigEntity;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 查询配置Mapper
 */
@Mapper
public interface QueryConfigMapper {
    @Insert("INSERT INTO t_ui_query_config (id, template_id, template_version_id, query_code, query_name, " +
            "query_type, data_provider_id, trigger_type, result_mode, bind_node_id, page_size, props_json, " +
            "created_at, updated_at, is_deleted) " +
            "VALUES (#{id}, #{templateId}, #{templateVersionId}, #{queryCode}, #{queryName}, " +
            "#{queryType}, #{dataProviderId}, #{triggerType}, #{resultMode}, #{bindNodeId}, #{pageSize}, #{propsJson}, " +
            "#{createdAt}, #{updatedAt}, 0)")
    int insert(QueryConfigEntity entity);

    @Select("SELECT * FROM t_ui_query_config WHERE id = #{id} AND is_deleted = 0")
    QueryConfigEntity findById(@Param("id") Long id);

    @Select("SELECT * FROM t_ui_query_config WHERE template_version_id = #{versionId} AND is_deleted = 0 ORDER BY created_at")
    List<QueryConfigEntity> findByTemplateVersionId(@Param("versionId") Long versionId);

    @Update("UPDATE t_ui_query_config SET query_name = #{queryName}, query_type = #{queryType}, " +
            "data_provider_id = #{dataProviderId}, trigger_type = #{triggerType}, result_mode = #{resultMode}, " +
            "bind_node_id = #{bindNodeId}, page_size = #{pageSize}, props_json = #{propsJson}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(QueryConfigEntity entity);

    @Update("UPDATE t_ui_query_config SET is_deleted = 1, updated_at = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
```

- [ ] **Step 5: 创建 QueryParamMapper**

```java
package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.QueryParamEntity;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 查询参数Mapper
 */
@Mapper
public interface QueryParamMapper {
    @Insert("INSERT INTO t_ui_query_param (id, query_config_id, param_name, param_label, bind_source, " +
            "bind_path, component_type, required, default_value, sort_no, created_at, updated_at, is_deleted) " +
            "VALUES (#{id}, #{queryConfigId}, #{paramName}, #{paramLabel}, #{bindSource}, " +
            "#{bindPath}, #{componentType}, #{required}, #{defaultValue}, #{sortNo}, #{createdAt}, #{updatedAt}, 0)")
    int insert(QueryParamEntity entity);

    @Select("SELECT * FROM t_ui_query_param WHERE query_config_id = #{queryConfigId} AND is_deleted = 0 ORDER BY sort_no")
    List<QueryParamEntity> findByQueryConfigId(@Param("queryConfigId") Long queryConfigId);

    @Update("UPDATE t_ui_query_param SET is_deleted = 1, updated_at = NOW() WHERE query_config_id = #{queryConfigId}")
    int deleteByQueryConfigId(@Param("queryConfigId") Long queryConfigId);
}
```

- [ ] **Step 6: 创建 QueryFillRuleMapper**

```java
package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.QueryFillRuleEntity;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 查询回填规则Mapper
 */
@Mapper
public interface QueryFillRuleMapper {
    @Insert("INSERT INTO t_ui_query_fill_rule (id, query_config_id, source_field, target_scope, target_path, " +
            "fill_mode, transform_json, sort_no, created_at, updated_at, is_deleted) " +
            "VALUES (#{id}, #{queryConfigId}, #{sourceField}, #{targetScope}, #{targetPath}, " +
            "#{fillMode}, #{transformJson}, #{sortNo}, #{createdAt}, #{updatedAt}, 0)")
    int insert(QueryFillRuleEntity entity);

    @Select("SELECT * FROM t_ui_query_fill_rule WHERE query_config_id = #{queryConfigId} AND is_deleted = 0 ORDER BY sort_no")
    List<QueryFillRuleEntity> findByQueryConfigId(@Param("queryConfigId") Long queryConfigId);

    @Update("UPDATE t_ui_query_fill_rule SET is_deleted = 1, updated_at = NOW() WHERE query_config_id = #{queryConfigId}")
    int deleteByQueryConfigId(@Param("queryConfigId") Long queryConfigId);
}
```

- [ ] **Step 7: 提交Entity和Mapper**

```bash
git add src/main/java/com/contract/infrastructure/persistence/entity/QueryConfigEntity.java
git add src/main/java/com/contract/infrastructure/persistence/entity/QueryParamEntity.java
git add src/main/java/com/contract/infrastructure/persistence/entity/QueryFillRuleEntity.java
git add src/main/java/com/contract/infrastructure/persistence/mapper/QueryConfigMapper.java
git add src/main/java/com/contract/infrastructure/persistence/mapper/QueryParamMapper.java
git add src/main/java/com/contract/infrastructure/persistence/mapper/QueryFillRuleMapper.java
git commit -m "feat: add QueryConfig, QueryParam, QueryFillRule entities and mappers"
```

---

## Task 4: MapStruct转换器

**Files:**
- Create: `src/main/java/com/contract/infrastructure/persistence/convert/EntityQueryConfigConverter.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/convert/EntityQueryParamConverter.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/convert/EntityQueryFillRuleConverter.java`

- [ ] **Step 1: 创建 EntityQueryConfigConverter**

```java
package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryConfig;
import com.contract.infrastructure.persistence.entity.QueryConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityQueryConfigConverter {
    QueryConfig toDomain(QueryConfigEntity entity);
    QueryConfigEntity toEntity(QueryConfig domain);
    List<QueryConfig> toDomainList(List<QueryConfigEntity> entities);
}
```

- [ ] **Step 2: 创建 EntityQueryParamConverter**

```java
package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryParam;
import com.contract.infrastructure.persistence.entity.QueryParamEntity;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityQueryParamConverter {
    QueryParam toDomain(QueryParamEntity entity);
    QueryParamEntity toEntity(QueryParam domain);
    List<QueryParam> toDomainList(List<QueryParamEntity> entities);
}
```

- [ ] **Step 3: 创建 EntityQueryFillRuleConverter**

```java
package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryFillRule;
import com.contract.infrastructure.persistence.entity.QueryFillRuleEntity;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityQueryFillRuleConverter {
    QueryFillRule toDomain(QueryFillRuleEntity entity);
    QueryFillRuleEntity toEntity(QueryFillRule domain);
    List<QueryFillRule> toDomainList(List<QueryFillRuleEntity> entities);
}
```

- [ ] **Step 4: 提交转换器**

```bash
git add src/main/java/com/contract/infrastructure/persistence/convert/EntityQueryConfigConverter.java
git add src/main/java/com/contract/infrastructure/persistence/convert/EntityQueryParamConverter.java
git add src/main/java/com/contract/infrastructure/persistence/convert/EntityQueryFillRuleConverter.java
git commit -m "feat: add QueryConfig, QueryParam, QueryFillRule entity converters"
```

---

## Task 5: Repository实现

**Files:**
- Create: `src/main/java/com/contract/infrastructure/persistence/repository/QueryConfigRepositoryImpl.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/repository/QueryParamRepositoryImpl.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/repository/QueryFillRuleRepositoryImpl.java`

- [ ] **Step 1: 创建 QueryConfigRepositoryImpl**

```java
package com.contract.infrastructure.persistence.repository;

import com.contract.domain.template.QueryConfig;
import com.contract.domain.template.repository.QueryConfigRepository;
import com.contract.infrastructure.persistence.convert.EntityQueryConfigConverter;
import com.contract.infrastructure.persistence.entity.QueryConfigEntity;
import com.contract.infrastructure.persistence.mapper.QueryConfigMapper;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QueryConfigRepositoryImpl implements QueryConfigRepository {
    private final QueryConfigMapper mapper;
    private final EntityQueryConfigConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public QueryConfig save(QueryConfig config) {
        QueryConfigEntity entity = converter.toEntity(config);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        mapper.insert(entity);
        return converter.toDomain(entity);
    }

    @Override
    public QueryConfig findById(Long id) {
        QueryConfigEntity entity = mapper.findById(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<QueryConfig> findByTemplateVersionId(Long versionId) {
        return converter.toDomainList(mapper.findByTemplateVersionId(versionId));
    }

    @Override
    public void update(QueryConfig config) {
        mapper.update(converter.toEntity(config));
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
```

- [ ] **Step 2: 创建 QueryParamRepositoryImpl**

```java
package com.contract.infrastructure.persistence.repository;

import com.contract.domain.template.QueryParam;
import com.contract.domain.template.repository.QueryParamRepository;
import com.contract.infrastructure.persistence.convert.EntityQueryParamConverter;
import com.contract.infrastructure.persistence.entity.QueryParamEntity;
import com.contract.infrastructure.persistence.mapper.QueryParamMapper;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QueryParamRepositoryImpl implements QueryParamRepository {
    private final QueryParamMapper mapper;
    private final EntityQueryParamConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public QueryParam save(QueryParam param) {
        QueryParamEntity entity = converter.toEntity(param);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        mapper.insert(entity);
        return converter.toDomain(entity);
    }

    @Override
    public List<QueryParam> findByQueryConfigId(Long queryConfigId) {
        return converter.toDomainList(mapper.findByQueryConfigId(queryConfigId));
    }

    @Override
    public void deleteByQueryConfigId(Long queryConfigId) {
        mapper.deleteByQueryConfigId(queryConfigId);
    }
}
```

- [ ] **Step 3: 创建 QueryFillRuleRepositoryImpl**

```java
package com.contract.infrastructure.persistence.repository;

import com.contract.domain.template.QueryFillRule;
import com.contract.domain.template.repository.QueryFillRuleRepository;
import com.contract.infrastructure.persistence.convert.EntityQueryFillRuleConverter;
import com.contract.infrastructure.persistence.entity.QueryFillRuleEntity;
import com.contract.infrastructure.persistence.mapper.QueryFillRuleMapper;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QueryFillRuleRepositoryImpl implements QueryFillRuleRepository {
    private final QueryFillRuleMapper mapper;
    private final EntityQueryFillRuleConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public QueryFillRule save(QueryFillRule rule) {
        QueryFillRuleEntity entity = converter.toEntity(rule);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        mapper.insert(entity);
        return converter.toDomain(entity);
    }

    @Override
    public List<QueryFillRule> findByQueryConfigId(Long queryConfigId) {
        return converter.toDomainList(mapper.findByQueryConfigId(queryConfigId));
    }

    @Override
    public void deleteByQueryConfigId(Long queryConfigId) {
        mapper.deleteByQueryConfigId(queryConfigId);
    }
}
```

- [ ] **Step 4: 提交Repository实现**

```bash
git add src/main/java/com/contract/infrastructure/persistence/repository/QueryConfigRepositoryImpl.java
git add src/main/java/com/contract/infrastructure/persistence/repository/QueryParamRepositoryImpl.java
git add src/main/java/com/contract/infrastructure/persistence/repository/QueryFillRuleRepositoryImpl.java
git commit -m "feat: add QueryConfig, QueryParam, QueryFillRule repository implementations"
```

---

## Task 6: DTO定义

**Files:**
- Create: `src/main/java/com/contract/application/template/dto/QueryParamDTO.java`
- Create: `src/main/java/com/contract/application/template/dto/QueryFillRuleDTO.java`
- Create: `src/main/java/com/contract/application/template/dto/QueryConfigDTO.java`
- Create: `src/main/java/com/contract/application/template/dto/QueryConfigCreateRequest.java`
- Create: `src/main/java/com/contract/application/template/dto/QueryConfigUpdateRequest.java`

- [ ] **Step 1: 创建 QueryParamDTO**

```java
package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParamDTO {
    private Long id;
    private Long queryConfigId;
    private String paramName;
    private String paramLabel;
    private String bindSource;
    private String bindPath;
    private String componentType;
    private Integer required;
    private String defaultValue;
    private Integer sortNo;
}
```

- [ ] **Step 2: 创建 QueryFillRuleDTO**

```java
package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryFillRuleDTO {
    private Long id;
    private Long queryConfigId;
    private String sourceField;
    private String targetScope;
    private String targetPath;
    private String fillMode;
    private String transformJson;
    private Integer sortNo;
}
```

- [ ] **Step 3: 创建 QueryConfigDTO**

```java
package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryConfigDTO {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private String queryCode;
    private String queryName;
    private String queryType;
    private Long dataProviderId;
    private String triggerType;
    private String resultMode;
    private Long bindNodeId;
    private Integer pageSize;
    private String propsJson;
    private List<QueryParamDTO> params;
    private List<QueryFillRuleDTO> fillRules;
}
```

- [ ] **Step 4: 创建 QueryConfigCreateRequest**

```java
package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryConfigCreateRequest {
    private String queryCode;
    private String queryName;
    private String queryType;
    private Long dataProviderId;
    private String triggerType;
    private String resultMode;
    private Long bindNodeId;
    private Integer pageSize;
    private String propsJson;
    private List<QueryParamDTO> params;
    private List<QueryFillRuleDTO> fillRules;
}
```

- [ ] **Step 5: 创建 QueryConfigUpdateRequest**

```java
package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryConfigUpdateRequest {
    private String queryName;
    private String queryType;
    private Long dataProviderId;
    private String triggerType;
    private String resultMode;
    private Long bindNodeId;
    private Integer pageSize;
    private String propsJson;
    private List<QueryParamDTO> params;
    private List<QueryFillRuleDTO> fillRules;
}
```

- [ ] **Step 6: 提交DTO**

```bash
git add src/main/java/com/contract/application/template/dto/QueryParamDTO.java
git add src/main/java/com/contract/application/template/dto/QueryFillRuleDTO.java
git add src/main/java/com/contract/application/template/dto/QueryConfigDTO.java
git add src/main/java/com/contract/application/template/dto/QueryConfigCreateRequest.java
git add src/main/java/com/contract/application/template/dto/QueryConfigUpdateRequest.java
git commit -m "feat: add QueryConfig, QueryParam, QueryFillRule DTOs"
```

---

## Task 7: DTO转换器

**Files:**
- Create: `src/main/java/com/contract/application/template/convert/QueryConfigConverter.java`
- Create: `src/main/java/com/contract/application/template/convert/QueryParamConverter.java`
- Create: `src/main/java/com/contract/application/template/convert/QueryFillRuleConverter.java`

- [ ] **Step 1: 创建 QueryParamConverter**

```java
package com.contract.application.template.convert;

import com.contract.application.template.dto.QueryParamDTO;
import com.contract.domain.template.QueryParam;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface QueryParamConverter {
    QueryParamDTO toDTO(QueryParam domain);
    QueryParam toDomain(QueryParamDTO dto);
    List<QueryParamDTO> toDTOList(List<QueryParam> domains);
}
```

- [ ] **Step 2: 创建 QueryFillRuleConverter**

```java
package com.contract.application.template.convert;

import com.contract.application.template.dto.QueryFillRuleDTO;
import com.contract.domain.template.QueryFillRule;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface QueryFillRuleConverter {
    QueryFillRuleDTO toDTO(QueryFillRule domain);
    QueryFillRule toDomain(QueryFillRuleDTO dto);
    List<QueryFillRuleDTO> toDTOList(List<QueryFillRule> domains);
}
```

- [ ] **Step 3: 创建 QueryConfigConverter**

```java
package com.contract.application.template.convert;

import com.contract.application.template.dto.QueryConfigDTO;
import com.contract.application.template.dto.QueryConfigCreateRequest;
import com.contract.application.template.dto.QueryConfigUpdateRequest;
import com.contract.domain.template.QueryConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(componentModel = "spring", uses = {QueryParamConverter.class, QueryFillRuleConverter.class})
public interface QueryConfigConverter {
    QueryConfigDTO toDTO(QueryConfig domain);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "queryCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    QueryConfig toDomain(QueryConfigCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "queryCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromDTO(QueryConfigUpdateRequest request, @MappingTarget QueryConfig domain);
    
    List<QueryConfigDTO> toDTOList(List<QueryConfig> domains);
}
```

- [ ] **Step 4: 提交转换器**

```bash
git add src/main/java/com/contract/application/template/convert/QueryParamConverter.java
git add src/main/java/com/contract/application/template/convert/QueryFillRuleConverter.java
git add src/main/java/com/contract/application/template/convert/QueryConfigConverter.java
git commit -m "feat: add QueryConfig, QueryParam, QueryFillRule DTO converters"
```

---

## Task 8: QueryConfigService实现

**Files:**
- Create: `src/main/java/com/contract/application/template/QueryConfigService.java`

- [ ] **Step 1: 创建 QueryConfigService**

```java
package com.contract.application.template;

import com.contract.application.template.convert.QueryConfigConverter;
import com.contract.application.template.convert.QueryParamConverter;
import com.contract.application.template.convert.QueryFillRuleConverter;
import com.contract.application.template.dto.*;
import com.contract.common.exception.BizException;
import com.contract.domain.template.QueryConfig;
import com.contract.domain.template.QueryParam;
import com.contract.domain.template.QueryFillRule;
import com.contract.domain.template.repository.QueryConfigRepository;
import com.contract.domain.template.repository.QueryParamRepository;
import com.contract.domain.template.repository.QueryFillRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryConfigService {
    private final QueryConfigRepository repository;
    private final QueryParamRepository paramRepository;
    private final QueryFillRuleRepository fillRuleRepository;
    private final QueryConfigConverter converter;
    private final QueryParamConverter paramConverter;
    private final QueryFillRuleConverter fillRuleConverter;

    @Transactional
    public QueryConfigDTO create(Long templateId, Long versionId, QueryConfigCreateRequest request) {
        // 创建查询配置
        QueryConfig config = converter.toDomain(request);
        config.setTemplateId(templateId);
        config.setTemplateVersionId(versionId);
        
        // 生成queryCode（如果没有提供）
        if (config.getQueryCode() == null || config.getQueryCode().isBlank()) {
            config.setQueryCode("query_" + System.currentTimeMillis());
        }
        
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        
        QueryConfig saved = repository.save(config);
        
        // 保存查询参数
        if (request.getParams() != null && !request.getParams().isEmpty()) {
            for (QueryParamDTO paramDTO : request.getParams()) {
                QueryParam param = paramConverter.toDomain(paramDTO);
                param.setQueryConfigId(saved.getId());
                param.setCreatedAt(LocalDateTime.now());
                param.setUpdatedAt(LocalDateTime.now());
                paramRepository.save(param);
            }
        }
        
        // 保存回填规则
        if (request.getFillRules() != null && !request.getFillRules().isEmpty()) {
            for (QueryFillRuleDTO ruleDTO : request.getFillRules()) {
                QueryFillRule rule = fillRuleConverter.toDomain(ruleDTO);
                rule.setQueryConfigId(saved.getId());
                rule.setCreatedAt(LocalDateTime.now());
                rule.setUpdatedAt(LocalDateTime.now());
                fillRuleRepository.save(rule);
            }
        }
        
        log.info("创建查询配置成功: id={}, templateId={}, versionId={}", saved.getId(), templateId, versionId);
        
        return getById(saved.getId());
    }

    public QueryConfigDTO getById(Long id) {
        QueryConfig config = repository.findById(id);
        if (config == null) {
            throw new BizException("查询配置不存在：" + id);
        }
        
        QueryConfigDTO dto = converter.toDTO(config);
        
        // 加载查询参数
        List<QueryParam> params = paramRepository.findByQueryConfigId(id);
        dto.setParams(paramConverter.toDTOList(params));
        
        // 加载回填规则
        List<QueryFillRule> rules = fillRuleRepository.findByQueryConfigId(id);
        dto.setFillRules(fillRuleConverter.toDTOList(rules));
        
        return dto;
    }

    public List<QueryConfigDTO> listByVersionId(Long versionId) {
        List<QueryConfig> configs = repository.findByTemplateVersionId(versionId);
        return converter.toDTOList(configs);
    }

    @Transactional
    public QueryConfigDTO update(Long id, QueryConfigUpdateRequest request) {
        QueryConfig config = repository.findById(id);
        if (config == null) {
            throw new BizException("查询配置不存在：" + id);
        }
        
        converter.updateFromDTO(request, config);
        config.setUpdatedAt(LocalDateTime.now());
        repository.update(config);
        
        // 更新查询参数（先删除后新增）
        if (request.getParams() != null) {
            paramRepository.deleteByQueryConfigId(id);
            for (QueryParamDTO paramDTO : request.getParams()) {
                QueryParam param = paramConverter.toDomain(paramDTO);
                param.setQueryConfigId(id);
                param.setCreatedAt(LocalDateTime.now());
                param.setUpdatedAt(LocalDateTime.now());
                paramRepository.save(param);
            }
        }
        
        // 更新回填规则（先删除后新增）
        if (request.getFillRules() != null) {
            fillRuleRepository.deleteByQueryConfigId(id);
            for (QueryFillRuleDTO ruleDTO : request.getFillRules()) {
                QueryFillRule rule = fillRuleConverter.toDomain(ruleDTO);
                rule.setQueryConfigId(id);
                rule.setCreatedAt(LocalDateTime.now());
                rule.setUpdatedAt(LocalDateTime.now());
                fillRuleRepository.save(rule);
            }
        }
        
        log.info("更新查询配置成功: id={}", id);
        
        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        QueryConfig config = repository.findById(id);
        if (config == null) {
            throw new BizException("查询配置不存在：" + id);
        }
        
        // 删除关联的参数和规则
        paramRepository.deleteByQueryConfigId(id);
        fillRuleRepository.deleteByQueryConfigId(id);
        
        repository.deleteById(id);
        log.info("删除查询配置成功: id={}", id);
    }
}
```

- [ ] **Step 2: 提交Service**

```bash
git add src/main/java/com/contract/application/template/QueryConfigService.java
git commit -m "feat: add QueryConfigService with CRUD operations"
```

---

## Task 9: Controller实现

**Files:**
- Create: `src/main/java/com/contract/adapter/controller/QueryConfigController.java`

- [ ] **Step 1: 创建 QueryConfigController**

```java
package com.contract.adapter.controller;

import com.contract.application.template.QueryConfigService;
import com.contract.application.template.dto.*;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QueryConfigController {
    private final QueryConfigService service;

    @PostMapping("/templates/{templateId}/versions/{versionId}/query-configs")
    public Result<QueryConfigDTO> create(
            @PathVariable Long templateId,
            @PathVariable Long versionId,
            @RequestBody QueryConfigCreateRequest request) {
        return Result.ok(service.create(templateId, versionId, request));
    }

    @GetMapping("/query-configs/{id}")
    public Result<QueryConfigDTO> getById(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @GetMapping("/templates/{templateId}/versions/{versionId}/query-configs")
    public Result<List<QueryConfigDTO>> listByVersionId(
            @PathVariable Long templateId,
            @PathVariable Long versionId) {
        return Result.ok(service.listByVersionId(versionId));
    }

    @PutMapping("/query-configs/{id}")
    public Result<QueryConfigDTO> update(
            @PathVariable Long id,
            @RequestBody QueryConfigUpdateRequest request) {
        return Result.ok(service.update(id, request));
    }

    @DeleteMapping("/query-configs/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok(null);
    }
}
```

- [ ] **Step 2: 提交Controller**

```bash
git add src/main/java/com/contract/adapter/controller/QueryConfigController.java
git commit -m "feat: add QueryConfigController with CRUD endpoints"
```

---

## Task 10: 编译验证

**Files:**
- 无

- [ ] **Step 1: 编译项目**

```bash
mvn clean compile -DskipTests
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 运行测试**

```bash
mvn test
```

Expected: Tests run successfully

- [ ] **Step 3: 提交最终状态**

```bash
git add -A
git commit -m "feat: complete QueryConfig CRUD implementation"
```

---

## 自我审查清单

**1. Spec覆盖检查:**
- [x] 查询配置CRUD API - Task 9
- [x] 查询参数配置 - Task 1, 2, 3, 5
- [x] 回填规则配置 - Task 1, 2, 3, 5
- [x] 领域对象定义 - Task 1
- [x] Repository接口 - Task 2
- [x] Entity和Mapper - Task 3
- [x] DTO转换 - Task 4, 7
- [x] Service实现 - Task 8
- [x] Controller实现 - Task 9

**2. Placeholder扫描:**
- 无 TBD/TODO
- 无"implement later"
- 无"add validation"
- 无"similar to Task N"
- 所有代码步骤都有完整代码块

**3. 类型一致性检查:**
- QueryConfig.queryCode 类型: String - 全部一致
- QueryParam.queryConfigId 类型: Long - 全部一致
- QueryFillRule.queryConfigId 类型: Long - 全部一致
- 方法签名在所有任务中一致

---

## 后续任务（不在本计划范围）

以下功能将在后续计划中实现：
- 前端config.html查询配置编辑界面
- 明细表配置CRUD
- 规则配置编辑器
- 合同CRUD API（阶段二）
- 查询执行API（阶段二）
