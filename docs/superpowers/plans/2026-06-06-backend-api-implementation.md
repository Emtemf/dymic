# 后端 API 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现模板配置系统的后端 API，包括数据提供方、布局节点、字段定义、字段组件绑定、动作配置、配置读取等核心功能。

**Architecture:** 采用 DDD 分层架构，Repository 接口在领域层定义，实现基础设施层。使用 MapStruct 进行 Entity ↔ Domain ↔ DTO 转换。业务友好 API，后端自动生成技术字段（字段编码、字段路径等）。

**Tech Stack:** Java 21, Spring Boot 3.5.14, MyBatis-Plus 3.5.5, MapStruct 1.5.5, H2 2.2.224, Snowflake ID Generator

---

## File Structure

**新增文件**：

```
后端 API 文件结构：
├── adapter/controller/
│   ├── DataProviderController.java           # 数据提供方配置 API
│   ├── LayoutNodeController.java             # 布局节点配置 API
│   ├── FieldDefController.java               # 字段定义配置 API
│   ├── FieldComponentController.java         # 字段组件绑定配置 API
│   ├── ActionConfigController.java           # 动作配置 API
│   └── TemplateSchemaController.java         # 配置读取 API
│
├── application/template/
│   ├── DataProviderService.java              # 数据提供方应用服务
│   ├── LayoutNodeService.java                # 布局节点应用服务
│   ├── FieldDefService.java                  # 字段定义应用服务
│   ├── FieldComponentService.java            # 字段组件绑定应用服务
│   ├── ActionConfigService.java              # 动作配置应用服务
│   └ TemplateSchemaService.java              # 配置聚合应用服务
│   └ convert/
│   │   ├── DataProviderConverter.java        # DTO ↔ Domain 转换
│   │   ├── LayoutNodeConverter.java          # DTO ↔ Domain 转换
│   │   ├── FieldDefConverter.java            # DTO ↔ Domain 转换
│   │   ├── FieldComponentConverter.java      # DTO ↔ Domain 转换
│   │   ├── ActionConfigConverter.java        # DTO ↔ Domain 转换
│   │   └ TemplateSchemaConverter.java        # DTO ↔ Domain 转换
│   └ dto/
│   │   ├── DataProviderDTO.java              # 数据提供方 DTO
│   │   ├── LayoutNodeDTO.java                # 布局节点 DTO
│   │   ├── FieldDefDTO.java                  # 字段定义 DTO
│   │   ├── FieldComponentDTO.java            # 字段组件绑定 DTO
│   │   ├── ActionConfigDTO.java              # 动作配置 DTO
│   │   ├── TemplateSchemaDTO.java            # 配置树 DTO
│   │   ├── LayoutNodeTreeDTO.java            # 布局节点树 DTO
│
├── domain/template/
│   ├── DataProvider.java                     # 数据提供方领域模型
│   ├── LayoutNode.java                       # 布局节点领域模型
│   ├── FieldDef.java                         # 字段定义领域模型
│   ├── FieldComponent.java                   # 字段组件绑定领域模型
│   ├── ActionConfig.java                     # 动作配置领域模型
│   └ repository/
│   │   ├── DataProviderRepository.java       # 数据提供方仓储接口
│   │   ├── LayoutNodeRepository.java         # 布局节点仓储接口
│   │   ├── FieldDefRepository.java           # 字段定义仓储接口
│   │   ├── FieldComponentRepository.java     # 字段组件绑定仓储接口
│   │   ├── ActionConfigRepository.java       # 动作配置仓储接口
│
├── infrastructure/persistence/
│   ├── entity/
│   │   ├── DataProviderEntity.java           # 数据提供方实体（已存在）
│   │   ├── LayoutNodeEntity.java             # 布局节点实体（已存在）
│   │   ├── FieldDefEntity.java               # 字段定义实体（已存在）
│   │   ├── FieldComponentEntity.java         # 字段组件绑定实体（已存在）
│   │   ├── ActionConfigEntity.java           # 动作配置实体（已存在）
│   ├── mapper/
│   │   ├── DataProviderMapper.java           # 数据提供方 Mapper（新增）
│   │   ├── LayoutNodeMapper.java             # 布局节点 Mapper（新增）
│   │   ├── FieldDefMapper.java               # 字段定义 Mapper（新增）
│   │   ├── FieldComponentMapper.java         # 字段组件绑定 Mapper（新增）
│   │   ├── ActionConfigMapper.java           # 动作配置 Mapper（新增）
│   ├── repository/
│   │   ├── DataProviderRepositoryImpl.java   # 数据提供方仓储实现
│   │   ├── LayoutNodeRepositoryImpl.java     # 布局节点仓储实现
│   │   ├── FieldDefRepositoryImpl.java       # 字段定义仓储实现
│   │   ├── FieldComponentRepositoryImpl.java # 字段组件绑定仓储实现
│   │   ├── ActionConfigRepositoryImpl.java   # 动作配置仓储实现
│   └ convert/
│   │   ├── EntityDataProviderConverter.java  # Entity ↔ Domain 转换
│   │   ├── EntityLayoutNodeConverter.java    # Entity ↔ Domain 转换
│   │   ├── EntityFieldDefConverter.java      # Entity ↔ Domain 转换
│   │   ├── EntityFieldComponentConverter.java # Entity ↔ Domain 转换
│   │   ├── EntityActionConfigConverter.java  # Entity ↔ Domain 转换
│
├── common/util/
│   ├── ChineseToPinyin.java                  # 中文转拼音工具（新增）
│   └ JsonbUtils.java                         # JSONB 工具（已存在）
│
└── test/java/com/contract/
    ├── DataProviderServiceTest.java          # 数据提供方服务测试
    ├── LayoutNodeServiceTest.java            # 布局节点服务测试
    ├── FieldDefServiceTest.java              # 字段定义服务测试
    ├── FieldComponentServiceTest.java        # 字段组件绑定服务测试
    ├── ActionConfigServiceTest.java          # 动作配置服务测试
    ├── TemplateSchemaServiceTest.java        # 配置聚合服务测试
    ├── DataProviderControllerTest.java       # 数据提供方 Controller 测试
    ├── LayoutNodeControllerTest.java         # 布局节点 Controller 测试
    ├── FieldDefControllerTest.java           # 字段定义 Controller 测试
    ├── FieldComponentControllerTest.java     # 字段组件绑定 Controller 测试
    ├── ActionConfigControllerTest.java       # 动作配置 Controller 测试
    ├── TemplateSchemaControllerTest.java     # 配置读取 Controller 测试
```

---

## Task 1: 数据提供方配置 API

**Files:**
- Create: `src/main/java/com/contract/domain/template/DataProvider.java`
- Create: `src/main/java/com/contract/domain/template/repository/DataProviderRepository.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/mapper/DataProviderMapper.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/repository/DataProviderRepositoryImpl.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/convert/EntityDataProviderConverter.java`
- Create: `src/main/java/com/contract/application/template/DataProviderService.java`
- Create: `src/main/java/com/contract/application/template/dto/DataProviderDTO.java`
- Create: `src/main/java/com/contract/application/template/convert/DataProviderConverter.java`
- Create: `src/main/java/com/contract/adapter/controller/DataProviderController.java`
- Test: `src/test/java/com/contract/DataProviderServiceTest.java`
- Test: `src/test/java/com/contract/DataProviderControllerTest.java`

### Step 1: 创建数据提供方领域模型

- [ ] **创建 DataProvider.java**

```java
package com.contract.domain.template;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DataProvider {
    private Long id;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String configJson;
    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
    private String status;
    private Long createdBy;
    private String createdName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private LocalDateTime updatedAt;
    
    public static DataProvider create(String providerCode, String providerName, String providerType) {
        DataProvider provider = new DataProvider();
        provider.setProviderCode(providerCode);
        provider.setProviderName(providerName);
        provider.setProviderType(providerType);
        provider.setStatus("ENABLED");
        return provider;
    }
}
```

### Step 2: 创建数据提供方仓储接口

- [ ] **创建 DataProviderRepository.java**

```java
package com.contract.domain.template.repository;

import com.contract.domain.template.DataProvider;
import java.util.List;

public interface DataProviderRepository {
    DataProvider save(DataProvider provider);
    DataProvider findById(Long id);
    DataProvider findByProviderCode(String providerCode);
    boolean existsByProviderCode(String providerCode);
    void update(DataProvider provider);
    List<DataProvider> findAll();
    List<DataProvider> findByIds(List<Long> ids);
}
```

### Step 3: 创建数据提供方 Mapper

- [ ] **创建 DataProviderMapper.java**

```java
package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.adapter.persistence.entity.DataProviderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataProviderMapper extends BaseMapper<DataProviderEntity> {
}
```

### Step 4: 创建 Entity ↔ Domain 转换器（MapStruct）

- [ ] **创建 EntityDataProviderConverter.java**

```java
package com.contract.infrastructure.persistence.convert;

import com.contract.adapter.persistence.entity.DataProviderEntity;
import com.contract.domain.template.DataProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityDataProviderConverter {
    DataProvider toDomain(DataProviderEntity entity);
    DataProviderEntity toEntity(DataProvider domain);
    List<DataProvider> toDomainList(List<DataProviderEntity> entities);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @NullValuePropertyMappingStrategy(NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDomain(DataProvider domain, @MappingTarget DataProviderEntity entity);
}
```

### Step 5: 创建数据提供方仓储实现

- [ ] **创建 DataProviderRepositoryImpl.java**

```java
package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.adapter.persistence.entity.DataProviderEntity;
import com.contract.adapter.persistence.mapper.DataProviderMapper;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.repository.DataProviderRepository;
import com.contract.infrastructure.persistence.convert.EntityDataProviderConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DataProviderRepositoryImpl implements DataProviderRepository {
    private final DataProviderMapper mapper;
    private final EntityDataProviderConverter converter;
    
    @Override
    public DataProvider save(DataProvider provider) {
        DataProviderEntity entity = converter.toEntity(provider);
        mapper.insert(entity);
        provider.setId(entity.getId());
        return provider;
    }
    
    @Override
    public DataProvider findById(Long id) {
        DataProviderEntity entity = mapper.selectById(id);
        return entity != null ? converter.toDomain(entity) : null;
    }
    
    @Override
    public DataProvider findByProviderCode(String providerCode) {
        LambdaQueryWrapper<DataProviderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataProviderEntity::getProviderCode, providerCode);
        DataProviderEntity entity = mapper.selectOne(wrapper);
        return entity != null ? converter.toDomain(entity) : null;
    }
    
    @Override
    public boolean existsByProviderCode(String providerCode) {
        LambdaQueryWrapper<DataProviderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataProviderEntity::getProviderCode, providerCode);
        return mapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public void update(DataProvider provider) {
        DataProviderEntity entity = converter.toEntity(provider);
        mapper.updateById(entity);
    }
    
    @Override
    public List<DataProvider> findAll() {
        List<DataProviderEntity> entities = mapper.selectList(null);
        return converter.toDomainList(entities);
    }
    
    @Override
    public List<DataProvider> findByIds(List<Long> ids) {
        List<DataProviderEntity> entities = mapper.selectBatchIds(ids);
        return converter.toDomainList(entities);
    }
}
```

### Step 6: 创建数据提供方 DTO

- [ ] **创建 DataProviderDTO.java**

```java
package com.contract.application.template.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DataProviderDTO {
    private Long id;
    private String providerCode;
    private String providerName;
    private String providerType;
    private Map<String, Object> configJson;
    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
    private String status;
}

@Data
public class DataProviderCreateDTO {
    private String providerCode;
    private String providerName;
    private String providerType;
    private Map<String, Object> configJson;
}

@Data
public class DataProviderUpdateDTO {
    private String providerName;
    private Map<String, Object> configJson;
}
```

### Step 7: 创建 DTO ↔ Domain 转换器（MapStruct）

- [ ] **创建 DataProviderConverter.java**

```java
package com.contract.application.template.convert;

import com.contract.application.template.dto.DataProviderDTO;
import com.contract.domain.template.DataProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DataProviderConverter {
    DataProviderDTO toDTO(DataProvider domain);
    DataProvider toDomain(DataProviderCreateDTO dto);
    
    @NullValuePropertyMappingStrategy(NullValuePropertyMappingStrategy.IGNORE)
    void updateDomainFromDTO(DataProviderUpdateDTO dto, @MappingTarget DataProvider domain);
    
    List<DataProviderDTO> toDTOList(List<DataProvider> domains);
}
```

### Step 8: 创建数据提供方应用服务

- [ ] **创建 DataProviderService.java**

```java
package com.contract.application.template;

import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.application.template.convert.DataProviderConverter;
import com.contract.common.exception.BizException;
import com.contract.common.util.JsonbUtils;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.repository.DataProviderRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataProviderService {
    private final DataProviderRepository repository;
    private final DataProviderConverter converter;
    private final SnowflakeIdGenerator idGenerator;
    
    @Transactional
    public DataProviderDTO create(DataProviderCreateDTO dto) {
        if (repository.existsByProviderCode(dto.getProviderCode())) {
            throw new BizException("数据提供方编码已存在：" + dto.getProviderCode());
        }
        
        DataProvider provider = converter.toDomain(dto);
        provider.setId(idGenerator.nextId());
        provider.setStatus("ENABLED");
        provider.setCreatedAt(LocalDateTime.now());
        provider.setUpdatedAt(LocalDateTime.now());
        
        if (dto.getConfigJson() != null) {
            provider.setConfigJson(JsonbUtils.toJson(dto.getConfigJson()));
        }
        
        repository.save(provider);
        return converter.toDTO(provider);
    }
    
    public DataProviderDTO getById(Long id) {
        DataProvider provider = repository.findById(id);
        if (provider == null) {
            throw new BizException("数据提供方不存在：" + id);
        }
        return converter.toDTO(provider);
    }
    
    public List<DataProviderDTO> list() {
        List<DataProvider> providers = repository.findAll();
        return converter.toDTOList(providers);
    }
    
    @Transactional
    public DataProviderDTO update(Long id, DataProviderUpdateDTO dto) {
        DataProvider provider = repository.findById(id);
        if (provider == null) {
            throw new BizException("数据提供方不存在：" + id);
        }
        
        converter.updateDomainFromDTO(dto, provider);
        provider.setUpdatedAt(LocalDateTime.now());
        
        if (dto.getConfigJson() != null) {
            provider.setConfigJson(JsonbUtils.toJson(dto.getConfigJson()));
        }
        
        repository.update(provider);
        return converter.toDTO(provider);
    }
}
```

### Step 9: 创建数据提供方 Controller

- [ ] **创建 DataProviderController.java**

```java
package com.contract.adapter.controller;

import com.contract.application.template.DataProviderService;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/data-providers")
@RequiredArgsConstructor
public class DataProviderController {
    private final DataProviderService service;
    
    @PostMapping
    public Result<DataProviderDTO> create(@RequestBody DataProviderCreateDTO dto) {
        DataProviderDTO result = service.create(dto);
        return Result.ok(result);
    }
    
    @GetMapping("/{id}")
    public Result<DataProviderDTO> getById(@PathVariable Long id) {
        DataProviderDTO result = service.getById(id);
        return Result.ok(result);
    }
    
    @GetMapping
    public Result<List<DataProviderDTO>> list() {
        List<DataProviderDTO> result = service.list();
        return Result.ok(result);
    }
    
    @PutMapping("/{id}")
    public Result<DataProviderDTO> update(@PathVariable Long id, @RequestBody DataProviderUpdateDTO dto) {
        DataProviderDTO result = service.update(id, dto);
        return Result.ok(result);
    }
}
```

### Step 10: 创建 Service 测试（TDD）

- [ ] **创建 DataProviderServiceTest.java**

```java
package com.contract;

import com.contract.application.template.DataProviderService;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DataProviderServiceTest {
    @Autowired
    private DataProviderService service;
    
    @Test
    void testCreateDataProvider() {
        DataProviderCreateDTO dto = new DataProviderCreateDTO();
        dto.setProviderCode("SUPPLIER_LIST");
        dto.setProviderName("供应商列表");
        dto.setProviderType("HTTP");
        
        Map<String, Object> config = new HashMap<>();
        config.put("url", "http://api.example.com/suppliers");
        dto.setConfigJson(config);
        
        DataProviderDTO result = service.create(dto);
        
        assertNotNull(result.getId());
        assertEquals("SUPPLIER_LIST", result.getProviderCode());
        assertEquals("ENABLED", result.getStatus());
    }
    
    @Test
    void testCreateDuplicateCode() {
        DataProviderCreateDTO dto1 = new DataProviderCreateDTO();
        dto1.setProviderCode("DUPLICATE_TEST");
        dto1.setProviderName("测试1");
        dto1.setProviderType("STATIC");
        service.create(dto1);
        
        DataProviderCreateDTO dto2 = new DataProviderCreateDTO();
        dto2.setProviderCode("DUPLICATE_TEST");
        dto2.setProviderName("测试2");
        dto2.setProviderType("STATIC");
        
        assertThrows(BizException.class, () -> service.create(dto2));
    }
    
    @Test
    void testGetById() {
        DataProviderCreateDTO dto = new DataProviderCreateDTO();
        dto.setProviderCode("GET_TEST");
        dto.setProviderName("查询测试");
        dto.setProviderType("STATIC");
        
        DataProviderDTO created = service.create(dto);
        DataProviderDTO result = service.getById(created.getId());
        
        assertEquals("GET_TEST", result.getProviderCode());
    }
    
    @Test
    void testUpdate() {
        DataProviderCreateDTO dto = new DataProviderCreateDTO();
        dto.setProviderCode("UPDATE_TEST");
        dto.setProviderName("更新测试");
        dto.setProviderType("STATIC");
        
        DataProviderDTO created = service.create(dto);
        
        DataProviderUpdateDTO updateDto = new DataProviderUpdateDTO();
        updateDto.setProviderName("更新后名称");
        
        DataProviderDTO result = service.update(created.getId(), updateDto);
        assertEquals("更新后名称", result.getProviderName());
    }
    
    @Test
    void testList() {
        DataProviderCreateDTO dto = new DataProviderCreateDTO();
        dto.setProviderCode("LIST_TEST");
        dto.setProviderName("列表测试");
        dto.setProviderType("STATIC");
        service.create(dto);
        
        List<DataProviderDTO> result = service.list();
        assertTrue(result.size() > 0);
    }
}
```

### Step 11: 创建 Controller 测试（MockMvc）

- [ ] **创建 DataProviderControllerTest.java**

```java
package com.contract;

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
class DataProviderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateDataProvider() throws Exception {
        String body = """
            {
              "providerCode": "API_TEST",
              "providerName": "API测试",
              "providerType": "HTTP",
              "configJson": {
                "url": "http://api.example.com/suppliers"
              }
            }
            """;
        
        mockMvc.perform(post("/api/data-providers")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.providerCode").value("API_TEST"));
    }
    
    @Test
    void testGetById() throws Exception {
        String body = """
            {
              "providerCode": "GET_API_TEST",
              "providerName": "查询API测试",
              "providerType": "STATIC"
            }
            """;
        
        mockMvc.perform(post("/api/data-providers")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/data-providers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

### Step 12: 运行测试验证

- [ ] **运行数据提供方测试**

Run: `cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=DataProviderServiceTest,DataProviderControllerTest`

Expected: 所有测试通过

### Step 13: 提交代码

- [ ] **提交数据提供方配置 API**

Run: `cd /home/wula/IdeaProjects/dymic && git add src/main/java/com/contract/domain/template/DataProvider.java src/main/java/com/contract/domain/template/repository/DataProviderRepository.java src/main/java/com/contract/infrastructure/persistence/mapper/DataProviderMapper.java src/main/java/com/contract/infrastructure/persistence/repository/DataProviderRepositoryImpl.java src/main/java/com/contract/infrastructure/persistence/convert/EntityDataProviderConverter.java src/main/java/com/contract/application/template/DataProviderService.java src/main/java/com/contract/application/template/dto/DataProviderDTO.java src/main/java/com/contract/application/template/convert/DataProviderConverter.java src/main/java/com/contract/adapter/controller/DataProviderController.java src/test/java/com/contract/DataProviderServiceTest.java src/test/java/com/contract/DataProviderControllerTest.java`

Run: `cd /home/wula/IdeaProjects/dymic && git commit -m "$(cat <<'EOF'
feat: implement data provider configuration API with TDD

Implemented complete data provider configuration API including:
- DataProvider domain model and repository interface
- DataProviderRepository implementation with MyBatis-Plus
- MapStruct converters (Entity ↔ Domain, Domain ↔ DTO)
- DataProviderService with business logic
- DataProviderController REST API
- Unit tests (Service layer)
- Integration tests (Controller layer with MockMvc)

Features:
- CRUD operations for data providers
- Duplicate code validation
- JSONB configuration storage
- Auto-generated ID with snowflake algorithm
EOF
)"`

---

## Task 2: 布局节点配置 API

**Files:**
- Create: `src/main/java/com/contract/domain/template/LayoutNode.java`
- Create: `src/main/java/com/contract/domain/template/repository/LayoutNodeRepository.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/mapper/LayoutNodeMapper.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/repository/LayoutNodeRepositoryImpl.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/convert/EntityLayoutNodeConverter.java`
- Create: `src/main/java/com/contract/application/template/LayoutNodeService.java`
- Create: `src/main/java/com/contract/application/template/dto/LayoutNodeDTO.java`
- Create: `src/main/java/com/contract/application/template/convert/LayoutNodeConverter.java`
- Create: `src/main/java/com/contract/adapter/controller/LayoutNodeController.java`
- Test: `src/test/java/com/contract/LayoutNodeServiceTest.java`
- Test: `src/test/java/com/contract/LayoutNodeControllerTest.java`

**说明：Mapper、DTO、Converter 等文件创建方式与 Task 1 类似，此处略去详细代码。**

### Step 1: 创建布局节点领域模型

- [ ] **创建 LayoutNode.java**

```java
package com.contract.domain.template;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LayoutNode {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long parentId;
    private String nodeCode;
    private String nodeName;
    private String nodeType;
    private Integer sortNo;
    private String nodePath;
    
    public static LayoutNode create(String nodeType, String displayName) {
        LayoutNode node = new LayoutNode();
        node.setNodeType(nodeType);
        node.setNodeName(displayName);
        return node;
    }
}
```

### Step 2: 创建布局节点仓储接口

- [ ] **创建 LayoutNodeRepository.java**

```java
package com.contract.domain.template.repository;

import com.contract.domain.template.LayoutNode;
import java.util.List;

public interface LayoutNodeRepository {
    LayoutNode save(LayoutNode node);
    LayoutNode findById(Long id);
    List<LayoutNode> findByVersionId(Long versionId);
    List<LayoutNode> findByParentId(Long parentId);
    void update(LayoutNode node);
}
```

### Step 3: 创建布局节点应用服务（关键：自动生成 nodeCode 和 nodePath）

- [ ] **创建 LayoutNodeService.java**

```java
package com.contract.application.template;

import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.LayoutNodeCreateDTO;
import com.contract.application.template.dto.LayoutNodeUpdateDTO;
import com.contract.application.template.convert.LayoutNodeConverter;
import com.contract.common.exception.BizException;
import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.repository.LayoutNodeRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LayoutNodeService {
    private final LayoutNodeRepository repository;
    private final LayoutNodeConverter converter;
    private final SnowflakeIdGenerator idGenerator;
    
    @Transactional
    public LayoutNodeDTO create(Long templateId, Long versionId, LayoutNodeCreateDTO dto) {
        LayoutNode node = converter.toDomain(dto);
        node.setId(idGenerator.nextId());
        node.setTemplateId(templateId);
        node.setTemplateVersionId(versionId);
        
        // 自动生成 nodeCode 和 nodePath
        node.setNodeCode(generateNodeCode(dto.getNodeType(), dto.getDisplayName()));
        node.setNodePath(generateNodePath(dto.getParentId(), dto.getDisplayName()));
        
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        
        repository.save(node);
        return converter.toDTO(node);
    }
    
    public LayoutNodeDTO getById(Long id) {
        LayoutNode node = repository.findById(id);
        if (node == null) {
            throw new BizException("布局节点不存在：" + id);
        }
        return converter.toDTO(node);
    }
    
    public List<LayoutNodeDTO> listByVersionId(Long versionId) {
        List<LayoutNode> nodes = repository.findByVersionId(versionId);
        return converter.toDTOList(nodes);
    }
    
    @Transactional
    public LayoutNodeDTO update(Long id, LayoutNodeUpdateDTO dto) {
        LayoutNode node = repository.findById(id);
        if (node == null) {
            throw new BizException("布局节点不存在：" + id);
        }
        
        converter.updateDomainFromDTO(dto, node);
        node.setUpdatedAt(LocalDateTime.now());
        
        repository.update(node);
        return converter.toDTO(node);
    }
    
    // 自动生成节点编码：card_basicInfo
    private String generateNodeCode(String nodeType, String displayName) {
        String pinyin = ChineseToPinyin.toPinyin(displayName);
        String typePrefix = nodeType.toLowerCase();
        return typePrefix + "_" + pinyin.toLowerCase();
    }
    
    // 自动生成节点路径：basicInfo 或 parentPath.fieldName
    private String generateNodePath(Long parentId, String displayName) {
        if (parentId == null) {
            return ChineseToPinyin.toPinyin(displayName).toLowerCase();
        } else {
            LayoutNode parent = repository.findById(parentId);
            return parent.getNodePath() + "." + ChineseToPinyin.toPinyin(displayName).toLowerCase();
        }
    }
}
```

### Step 4: 创建布局节点 Controller

- [ ] **创建 LayoutNodeController.java**

```java
package com.contract.adapter.controller;

import com.contract.application.template.LayoutNodeService;
import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.LayoutNodeCreateDTO;
import com.contract.application.template.dto.LayoutNodeUpdateDTO;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}/layout-nodes")
@RequiredArgsConstructor
public class LayoutNodeController {
    private final LayoutNodeService service;
    
    @PostMapping
    public Result<LayoutNodeDTO> create(
        @PathVariable Long templateId,
        @PathVariable Long versionId,
        @RequestBody LayoutNodeCreateDTO dto
    ) {
        LayoutNodeDTO result = service.create(templateId, versionId, dto);
        return Result.ok(result);
    }
    
    @GetMapping("/{id}")
    public Result<LayoutNodeDTO> getById(@PathVariable Long id) {
        LayoutNodeDTO result = service.getById(id);
        return Result.ok(result);
    }
    
    @GetMapping
    public Result<List<LayoutNodeDTO>> list(@PathVariable Long versionId) {
        List<LayoutNodeDTO> result = service.listByVersionId(versionId);
        return Result.ok(result);
    }
    
    @PutMapping("/{id}")
    public Result<LayoutNodeDTO> update(@PathVariable Long id, @RequestBody LayoutNodeUpdateDTO dto) {
        LayoutNodeDTO result = service.update(id, dto);
        return Result.ok(result);
    }
}
```

### Step 5: 创建测试

- [ ] **创建 LayoutNodeServiceTest.java**

```java
package com.contract;

import com.contract.application.template.LayoutNodeService;
import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.LayoutNodeCreateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LayoutNodeServiceTest {
    @Autowired
    private LayoutNodeService service;
    
    @Test
    void testCreateLayoutNode() {
        LayoutNodeCreateDTO dto = new LayoutNodeCreateDTO();
        dto.setNodeType("CARD");
        dto.setDisplayName("基本信息");
        dto.setParentId(null);
        dto.setSortNo(1);
        
        LayoutNodeDTO result = service.create(100L, 200L, dto);
        
        assertNotNull(result.getId());
        assertEquals("card_basicInfo", result.getNodeCode());
        assertEquals("basicInfo", result.getNodePath());
    }
}
```

### Step 6: 运行测试验证

- [ ] **运行布局节点测试**

Run: `cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=LayoutNodeServiceTest`

Expected: 测试通过

### Step 7: 提交代码

- [ ] **提交布局节点配置 API**

Run: `cd /home/wula/IdeaProjects/dymic && git add src/main/java/com/contract/domain/template/LayoutNode.java src/main/java/com/contract/domain/template/repository/LayoutNodeRepository.java src/main/java/com/contract/infrastructure/persistence/mapper/LayoutNodeMapper.java src/main/java/com/contract/infrastructure/persistence/repository/LayoutNodeRepositoryImpl.java src/main/java/com/contract/infrastructure/persistence/convert/EntityLayoutNodeConverter.java src/main/java/com/contract/application/template/LayoutNodeService.java src/main/java/com/contract/application/template/dto/LayoutNodeDTO.java src/main/java/com/contract/application/template/convert/LayoutNodeConverter.java src/main/java/com/contract/adapter/controller/LayoutNodeController.java src/test/java/com/contract/LayoutNodeServiceTest.java`

Run: `cd /home/wula/IdeaProjects/dymic && git commit -m "feat: implement layout node configuration API with auto-generated nodeCode and nodePath"`

---

## Task 3: 字段定义配置 API

**Files:**
- Create: `src/main/java/com/contract/domain/template/FieldDef.java`
- Create: `src/main/java/com/contract/domain/template/repository/FieldDefRepository.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/mapper/FieldDefMapper.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/repository/FieldDefRepositoryImpl.java`
- Create: `src/main/java/com/contract/infrastructure/persistence/convert/EntityFieldDefConverter.java`
- Create: `src/main/java/com/contract/application/template/FieldDefService.java`
- Create: `src/main/java/com/contract/application/template/dto/FieldDefDTO.java`
- Create: `src/main/java/com/contract/application/template/convert/FieldDefConverter.java`
- Create: `src/main/java/com/contract/adapter/controller/FieldDefController.java`
- Test: `src/test/java/com/contract/FieldDefServiceTest.java`

**关键特性：创建字段定义时，同时创建字段组件绑定（一体化）。**

### Step 1: 创建字段定义领域模型

- [ ] **创建 FieldDef.java**

```java
package com.contract.domain.template;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FieldDef {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long layoutNodeId;
    private String fieldCode;
    private String fieldPath;
    private String fieldNameCn;
    private String dataType;
    private Integer requiredDefault;
    
    public static FieldDef create(String displayName, String dataType) {
        FieldDef fieldDef = new FieldDef();
        fieldDef.setFieldNameCn(displayName);
        fieldDef.setDataType(dataType);
        return fieldDef;
    }
}
```

### Step 2: 创建字段定义应用服务（关键：自动生成 fieldCode 和 fieldPath，同时创建字段组件绑定）

- [ ] **创建 FieldDefService.java**

```java
package com.contract.application.template;

import com.contract.application.template.dto.FieldDefDTO;
import com.contract.application.template.dto.FieldDefCreateDTO;
import com.contract.application.template.dto.FieldDefUpdateDTO;
import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.convert.FieldDefConverter;
import com.contract.application.template.FieldComponentService;
import com.contract.common.exception.BizException;
import com.contract.domain.template.FieldDef;
import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.repository.FieldDefRepository;
import com.contract.domain.template.repository.LayoutNodeRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldDefService {
    private final FieldDefRepository repository;
    private final LayoutNodeRepository layoutNodeRepository;
    private final FieldComponentService fieldComponentService;
    private final FieldDefConverter converter;
    private final SnowflakeIdGenerator idGenerator;
    
    @Transactional
    public FieldDefCreateResult create(Long templateId, Long versionId, FieldDefCreateDTO dto) {
        // 1. 创建字段定义
        FieldDef fieldDef = converter.toDomain(dto);
        fieldDef.setId(idGenerator.nextId());
        fieldDef.setTemplateId(templateId);
        fieldDef.setTemplateVersionId(versionId);
        
        // 自动生成 fieldCode 和 fieldPath
        LayoutNode layoutNode = layoutNodeRepository.findById(dto.getLayoutNodeId());
        fieldDef.setFieldCode(generateFieldCode(dto.getDisplayName()));
        fieldDef.setFieldPath(generateFieldPath(layoutNode.getNodePath(), dto.getDisplayName()));
        fieldDef.setDataType(detectDataType(dto.getComponentType()));
        fieldDef.setFieldNameCn(dto.getDisplayName());
        fieldDef.setRequiredDefault(dto.getRequired() ? 1 : 0);
        
        fieldDef.setCreatedAt(LocalDateTime.now());
        repository.save(fieldDef);
        
        // 2. 同时创建字段组件绑定
        FieldComponentDTO fieldComponent = fieldComponentService.create(
            templateId, versionId, fieldDef.getId(), dto.getLayoutNodeId(), dto
        );
        
        // 3. 返回结果
        FieldDefDTO fieldDefDTO = converter.toDTO(fieldDef);
        return FieldDefCreateResult.builder()
            .fieldDef(fieldDefDTO)
            .fieldComponent(fieldComponent)
            .build();
    }
    
    // 自动生成字段编码：contractName
    private String generateFieldCode(String displayName) {
        return ChineseToPinyin.toPinyin(displayName).toLowerCase();
    }
    
    // 自动生成字段路径：basicInfo.contractName
    private String generateFieldPath(String nodePath, String displayName) {
        return nodePath + "." + generateFieldCode(displayName);
    }
    
    // 自动检测数据类型：INPUT → TEXT, NUMBER → NUMBER
    private String detectDataType(String componentType) {
        switch (componentType) {
            case "INPUT": return "TEXT";
            case "NUMBER": return "NUMBER";
            case "DATE": return "DATE";
            case "MONEY": return "NUMBER";
            case "SELECT": return "TEXT";
            default: return "TEXT";
        }
    }
}

@Data
@Builder
public class FieldDefCreateResult {
    private FieldDefDTO fieldDef;
    private FieldComponentDTO fieldComponent;
}
```

### Step 2: 创建字段定义 Controller

- [ ] **创建 FieldDefController.java**

```java
package com.contract.adapter.controller;

import com.contract.application.template.FieldDefService;
import com.contract.application.template.dto.FieldDefCreateDTO;
import com.contract.application.template.dto.FieldDefCreateResult;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}/field-defs")
@RequiredArgsConstructor
public class FieldDefController {
    private final FieldDefService service;
    
    @PostMapping
    public Result<FieldDefCreateResult> create(
        @PathVariable Long templateId,
        @PathVariable Long versionId,
        @RequestBody FieldDefCreateDTO dto
    ) {
        FieldDefCreateResult result = service.create(templateId, versionId, dto);
        return Result.ok(result);
    }
}
```

### Step 3: 创建测试

- [ ] **创建 FieldDefServiceTest.java**

```java
package com.contract;

import com.contract.application.template.FieldDefService;
import com.contract.application.template.dto.FieldDefCreateDTO;
import com.contract.application.template.dto.FieldDefCreateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class FieldDefServiceTest {
    @Autowired
    private FieldDefService service;
    
    @Test
    void testCreateFieldDef() {
        // 先创建布局节点
        // ...
        
        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setLayoutNodeId(12345L);
        dto.setDisplayName("合同名称");
        dto.setComponentType("INPUT");
        dto.setRequired(true);
        dto.setPlaceholder("请输入合同名称");
        dto.setSortNo(1);
        
        FieldDefCreateResult result = service.create(100L, 200L, dto);
        
        assertNotNull(result.getFieldDef().getId());
        assertEquals("contractName", result.getFieldDef().getFieldCode());
        assertEquals("basicInfo.contractName", result.getFieldDef().getFieldPath());
        assertEquals("TEXT", result.getFieldDef().getDataType());
        
        // 验证字段组件绑定同时创建
        assertNotNull(result.getFieldComponent().getId());
    }
}
```

### Step 4: 运行测试验证

- [ ] **运行字段定义测试**

Run: `cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=FieldDefServiceTest`

Expected: 测试通过

### Step 5: 提交代码

- [ ] **提交字段定义配置 API**

Run: `cd /home/wula/IdeaProjects/dymic && git add src/main/java/com/contract/domain/template/FieldDef.java src/main/java/com/contract/domain/template/repository/FieldDefRepository.java src/main/java/com/contract/application/template/FieldDefService.java src/main/java/com/contract/adapter/controller/FieldDefController.java src/test/java/com/contract/FieldDefServiceTest.java`

Run: `cd /home/wula/IdeaProjects/dymic && git commit -m "feat: implement field definition API with auto-generated fieldCode and fieldPath"`

---

## Task 4: 字段组件绑定配置 API

**Files:**
- Create: `src/main/java/com/contract/domain/template/FieldComponent.java`
- Create: `src/main/java/com/contract/domain/template/repository/FieldComponentRepository.java`
- Create: `src/main/java/com/contract/application/template/FieldComponentService.java`
- Create: `src/main/java/com/contract/adapter/controller/FieldComponentController.java`
- Test: `src/test/java/com/contract/FieldComponentServiceTest.java`

**关键特性：支持两种数据来源（静态选项或数据提供方）。**

### Step 1: 创建字段组件绑定应用服务（关键：处理数据来源）

- [ ] **创建 FieldComponentService.java**

```java
package com.contract.application.template;

import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.FieldDefCreateDTO;
import com.contract.application.template.convert.FieldComponentConverter;
import com.contract.common.util.JsonbUtils;
import com.contract.domain.template.FieldComponent;
import com.contract.domain.template.repository.FieldComponentRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FieldComponentService {
    private final FieldComponentRepository repository;
    private final FieldComponentConverter converter;
    private final SnowflakeIdGenerator idGenerator;
    
    public FieldComponentDTO create(
        Long templateId,
        Long versionId,
        Long fieldDefId,
        Long layoutNodeId,
        FieldDefCreateDTO dto
    ) {
        FieldComponent component = new FieldComponent();
        component.setId(idGenerator.nextId());
        component.setTemplateId(templateId);
        component.setTemplateVersionId(versionId);
        component.setFieldDefId(fieldDefId);
        component.setLayoutNodeId(layoutNodeId);
        component.setComponentType(dto.getComponentType());
        component.setLabelName(dto.getDisplayName());
        component.setPlaceholder(dto.getPlaceholder());
        component.setSortNo(dto.getSortNo());
        
        // 处理数据来源配置
        if ("SELECT".equals(dto.getComponentType())) {
            Map<String, Object> props = new HashMap<>();
            
            if ("STATIC".equals(dto.getDataSourceType())) {
                // 静态选项
                props.put("options", dto.getStaticOptions());
                component.setComponentProps(JsonbUtils.toJson(props));
            } else if ("PROVIDER".equals(dto.getDataSourceType())) {
                // 数据提供方
                component.setDataProviderId(dto.getDataProviderId());
                props.put("dataProviderId", dto.getDataProviderId());
                component.setComponentProps(JsonbUtils.toJson(props));
            }
        }
        
        // 处理必填规则
        Map<String, Object> requiredRule = new HashMap<>();
        requiredRule.put("required", dto.getRequired());
        component.setRequiredRule(JsonbUtils.toJson(requiredRule));
        
        repository.save(component);
        return converter.toDTO(component);
    }
}
```

### Step 2: 创建字段组件绑定 Controller

- [ ] **创建 FieldComponentController.java**

```java
package com.contract.adapter.controller;

import com.contract.application.template.FieldComponentService;
import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.FieldComponentUpdateDTO;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}/field-components")
@RequiredArgsConstructor
public class FieldComponentController {
    private final FieldComponentService service;
    
    @PutMapping("/{id}")
    public Result<FieldComponentDTO> update(@PathVariable Long id, @RequestBody FieldComponentUpdateDTO dto) {
        FieldComponentDTO result = service.update(id, dto);
        return Result.ok(result);
    }
}
```

### Step 3: 创建测试（关键：验证静态选项和数据提供方）

- [ ] **创建 FieldComponentServiceTest.java**

```java
package com.contract;

import com.contract.application.template.FieldComponentService;
import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.FieldDefCreateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class FieldComponentServiceTest {
    @Autowired
    private FieldComponentService service;
    
    @Test
    void testCreateWithStaticOptions() {
        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setDisplayName("币种");
        dto.setComponentType("SELECT");
        dto.setDataSourceType("STATIC");
        dto.setStaticOptions(Arrays.asList(
            new FieldDefCreateDTO.StaticOption("CNY", "人民币", 1),
            new FieldDefCreateDTO.StaticOption("USD", "美元", 2)
        ));
        
        FieldComponentDTO result = service.create(100L, 200L, 111L, 12345L, dto);
        
        assertNotNull(result.getId());
        assertNotNull(result.getComponentProps());
        // 验证静态选项已保存到 componentProps
    }
    
    @Test
    void testCreateWithDataProvider() {
        // 先创建数据提供方
        // ...
        
        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setDisplayName("供应商");
        dto.setComponentType("SELECT");
        dto.setDataSourceType("PROVIDER");
        dto.setDataProviderId(33333L);
        
        FieldComponentDTO result = service.create(100L, 200L, 112L, 12345L, dto);
        
        assertNotNull(result.getId());
        assertEquals(33333L, result.getDataProviderId());
        // 验证数据提供方 ID 已关联
    }
}
```

### Step 4: 运行测试验证

- [ ] **运行字段组件绑定测试**

Run: `cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=FieldComponentServiceTest`

Expected: 测试通过

### Step 5: 提交代码

- [ ] **提交字段组件绑定配置 API**

Run: `cd /home/wula/IdeaProjects/dymic && git commit -m "feat: implement field component binding API with static options and data provider support"`

---

## Task 5: 动作配置 API

**Files:**
- Create: `src/main/java/com/contract/domain/template/ActionConfig.java`
- Create: `src/main/java/com/contract/domain/template/repository/ActionConfigRepository.java`
- Create: `src/main/java/com/contract/application/template/ActionConfigService.java`
- Create: `src/main/java/com/contract/adapter/controller/ActionConfigController.java`
- Test: `src/test/java/com/contract/ActionConfigServiceTest.java`

**关键特性：预定义动作类型列表。**

### Step 1: 创建动作配置应用服务

- [ ] **创建 ActionConfigService.java**

```java
package com.contract.application.template;

import com.contract.application.template.dto.ActionConfigDTO;
import com.contract.application.template.dto.ActionConfigCreateDTO;
import com.contract.application.template.convert.ActionConfigConverter;
import com.contract.common.util.JsonbUtils;
import com.contract.domain.template.ActionConfig;
import com.contract.domain.template.repository.ActionConfigRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActionConfigService {
    private final ActionConfigRepository repository;
    private final ActionConfigConverter converter;
    private final SnowflakeIdGenerator idGenerator;
    
    public ActionConfigDTO create(Long templateId, Long versionId, ActionConfigCreateDTO dto) {
        ActionConfig action = converter.toDomain(dto);
        action.setId(idGenerator.nextId());
        action.setTemplateId(templateId);
        action.setTemplateVersionId(versionId);
        
        // 自动生成 actionCode
        action.setActionCode(generateActionCode(dto.getDisplayName()));
        
        // 处理规则配置
        if (dto.getBeforeRule() != null) {
            action.setBeforeRule(JsonbUtils.toJson(dto.getBeforeRule()));
        }
        if (dto.getAfterRule() != null) {
            action.setAfterRule(JsonbUtils.toJson(dto.getAfterRule()));
        }
        
        action.setCreatedAt(LocalDateTime.now());
        repository.save(action);
        return converter.toDTO(action);
    }
    
    private String generateActionCode(String displayName) {
        return "action_" + ChineseToPinyin.toPinyin(displayName).toLowerCase();
    }
}
```

### Step 2: 创建动作配置 Controller

- [ ] **创建 ActionConfigController.java**

```java
package com.contract.adapter.controller;

import com.contract.application.template.ActionConfigService;
import com.contract.application.template.dto.ActionConfigDTO;
import com.contract.application.template.dto.ActionConfigCreateDTO;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}/actions")
@RequiredArgsConstructor
public class ActionConfigController {
    private final ActionConfigService service;
    
    @PostMapping
    public Result<ActionConfigDTO> create(
        @PathVariable Long templateId,
        @PathVariable Long versionId,
        @RequestBody ActionConfigCreateDTO dto
    ) {
        ActionConfigDTO result = service.create(templateId, versionId, dto);
        return Result.ok(result);
    }
    
    @GetMapping
    public Result<List<ActionConfigDTO>> list(@PathVariable Long versionId) {
        List<ActionConfigDTO> result = service.listByVersionId(versionId);
        return Result.ok(result);
    }
}
```

### Step 3: 创建测试

- [ ] **创建 ActionConfigServiceTest.java**

```java
package com.contract;

import com.contract.application.template.ActionConfigService;
import com.contract.application.template.dto.ActionConfigDTO;
import com.contract.application.template.dto.ActionConfigCreateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ActionConfigServiceTest {
    @Autowired
    private ActionConfigService service;
    
    @Test
    void testCreateSaveAction() {
        ActionConfigCreateDTO dto = new ActionConfigCreateDTO();
        dto.setActionType("SAVE_CONTRACT");
        dto.setDisplayName("保存合同");
        dto.setConfirmRequired(true);
        dto.setConfirmText("是否保存合同？");
        
        ActionConfigDTO result = service.create(100L, 200L, dto);
        
        assertNotNull(result.getId());
        assertEquals("action_saveContract", result.getActionCode());
    }
}
```

### Step 4: 运行测试验证

- [ ] **运行动作配置测试**

Run: `cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=ActionConfigServiceTest`

Expected: 测试通过

### Step 5: 提交代码

- [ ] **提交动作配置 API**

Run: `cd /home/wula/IdeaProjects/dymic && git commit -m "feat: implement action configuration API"`

---

## Task 6: 配置读取 API

**Files:**
- Create: `src/main/java/com/contract/application/template/TemplateSchemaService.java`
- Create: `src/main/java/com/contract/application/template/dto/TemplateSchemaDTO.java`
- Create: `src/main/java/com/contract/application/template/dto/LayoutNodeTreeDTO.java`
- Create: `src/main/java/com/contract/adapter/controller/TemplateSchemaController.java`
- Test: `src/test/java/com/contract/TemplateSchemaServiceTest.java`

**关键特性：聚合所有配置，构建完整的嵌套树结构。**

### Step 1: 创建配置聚合应用服务

- [ ] **创建 TemplateSchemaService.java**

```java
package com.contract.application.template;

import com.contract.application.template.dto.TemplateSchemaDTO;
import com.contract.application.template.dto.LayoutNodeTreeDTO;
import com.contract.application.template.convert.TemplateSchemaConverter;
import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.FieldDef;
import com.contract.domain.template.FieldComponent;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.ActionConfig;
import com.contract.domain.template.repository.LayoutNodeRepository;
import com.contract.domain.template.repository.FieldDefRepository;
import com.contract.domain.template.repository.FieldComponentRepository;
import com.contract.domain.template.repository.DataProviderRepository;
import com.contract.domain.template.repository.ActionConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateSchemaService {
    private final LayoutNodeRepository layoutNodeRepository;
    private final FieldDefRepository fieldDefRepository;
    private final FieldComponentRepository fieldComponentRepository;
    private final DataProviderRepository dataProviderRepository;
    private final ActionConfigRepository actionConfigRepository;
    
    public TemplateSchemaDTO getSchema(Long templateId, Long versionId) {
        // 1. 查询所有布局节点
        List<LayoutNode> layoutNodes = layoutNodeRepository.findByVersionId(versionId);
        
        // 2. 查询所有字段定义
        List<FieldDef> fieldDefs = fieldDefRepository.findByVersionId(versionId);
        
        // 3. 查询所有字段组件绑定
        List<FieldComponent> fieldComponents = fieldComponentRepository.findByVersionId(versionId);
        
        // 4. 查询引用的数据提供方
        Set<Long> providerIds = fieldComponents.stream()
            .filter(fc -> fc.getDataProviderId() != null)
            .map(FieldComponent::getDataProviderId)
            .collect(Collectors.toSet());
        List<DataProvider> dataProviders = dataProviderRepository.findByIds(new ArrayList<>(providerIds));
        
        // 5. 查询所有动作配置
        List<ActionConfig> actions = actionConfigRepository.findByVersionId(versionId);
        
        // 6. 构建完整配置树
        List<LayoutNodeTreeDTO> layoutNodeTree = buildLayoutNodeTree(layoutNodes, fieldDefs, fieldComponents);
        
        // 7. 返回结果
        return TemplateSchemaDTO.builder()
            .templateId(templateId)
            .versionId(versionId)
            .layoutNodes(layoutNodeTree)
            .dataProviders(dataProviders)
            .actions(actions)
            .build();
    }
    
    private List<LayoutNodeTreeDTO> buildLayoutNodeTree(
        List<LayoutNode> layoutNodes,
        List<FieldDef> fieldDefs,
        List<FieldComponent> fieldComponents
    ) {
        // 构建父子关系 Map
        Map<Long, List<LayoutNode>> nodeMap = layoutNodes.stream()
            .collect(Collectors.groupingBy(LayoutNode::getParentId));
        
        // 递归构建树结构
        return buildNodeTree(null, nodeMap, fieldDefs, fieldComponents);
    }
    
    private List<LayoutNodeTreeDTO> buildNodeTree(
        Long parentId,
        Map<Long, List<LayoutNode>> nodeMap,
        List<FieldDef> fieldDefs,
        List<FieldComponent> fieldComponents
    ) {
        List<LayoutNode> children = nodeMap.get(parentId);
        if (children == null) {
            return Collections.emptyList();
        }
        
        return children.stream()
            .sorted(Comparator.comparing(LayoutNode::getSortNo))
            .map(node -> {
                LayoutNodeTreeDTO dto = convertToTreeDTO(node);
                
                // 如果是字段节点，关联字段定义和组件绑定
                if ("FIELD".equals(node.getNodeType())) {
                    FieldDef fieldDef = findFieldDef(fieldDefs, node);
                    FieldComponent fieldComponent = findFieldComponent(fieldComponents, fieldDef);
                    dto.setFieldDef(fieldDef);
                    dto.setFieldComponent(fieldComponent);
                }
                
                // 递归处理子节点
                dto.setChildren(buildNodeTree(node.getId(), nodeMap, fieldDefs, fieldComponents));
                
                return dto;
            })
            .collect(Collectors.toList());
    }
}
```

### Step 2: 创建配置读取 Controller

- [ ] **创建 TemplateSchemaController.java**

```java
package com.contract.adapter.controller;

import com.contract.application.template.TemplateSchemaService;
import com.contract.application.template.dto.TemplateSchemaDTO;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}")
@RequiredArgsConstructor
public class TemplateSchemaController {
    private final TemplateSchemaService service;
    
    @GetMapping("/schema")
    public Result<TemplateSchemaDTO> getSchema(
        @PathVariable Long templateId,
        @PathVariable Long versionId
    ) {
        TemplateSchemaDTO result = service.getSchema(templateId, versionId);
        return Result.ok(result);
    }
}
```

### Step 3: 创建测试

- [ ] **创建 TemplateSchemaServiceTest.java**

```java
package com.contract;

import com.contract.application.template.TemplateSchemaService;
import com.contract.application.template.dto.TemplateSchemaDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TemplateSchemaServiceTest {
    @Autowired
    private TemplateSchemaService service;
    
    @Test
    void testGetSchema() {
        // 先创建完整的配置（布局节点、字段定义、字段组件绑定、动作配置）
        // ...
        
        TemplateSchemaDTO result = service.getSchema(100L, 200L);
        
        assertNotNull(result);
        assertTrue(result.getLayoutNodes().size() > 0);
        // 验证嵌套结构正确
        // 验证数据提供方引用正确
    }
}
```

### Step 4: 运行测试验证

- [ ] **运行配置读取测试**

Run: `cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=TemplateSchemaServiceTest`

Expected: 测试通过

### Step 5: 提交代码

- [ ] **提交配置读取 API**

Run: `cd /home/wula/IdeaProjects/dymic && git commit -m "feat: implement template schema aggregation API for preview rendering"`

---

## Summary

This plan implements complete backend API for template configuration system:

**Implemented APIs**:
1. Data Provider Configuration API - CRUD for HTTP/Platform/Internal data sources
2. Layout Node Configuration API - Tree structure with auto-generated nodeCode/nodePath
3. Field Definition Configuration API - Auto-generated fieldCode/fieldPath, creates FieldComponent together
4. Field Component Binding Configuration API - Static options or data provider support
5. Action Configuration API - Predefined action types with auto-generated actionCode
6. Template Schema Aggregation API - Complete nested tree structure for preview rendering

**Key Features**:
- Business-friendly APIs (business language, auto-generated technical fields)
- DDD layered architecture (Repository interface in domain layer)
- MapStruct converters (Entity ↔ Domain ↔ DTO)
- TDD approach (write tests first)
- Auto-generated IDs with snowflake algorithm
- JSONB configuration storage

**Testing Coverage**:
- Service layer tests (Mock Repository)
- Controller layer tests (MockMvc)
- Integration tests (H2 memory database)