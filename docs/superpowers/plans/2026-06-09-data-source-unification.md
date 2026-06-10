# Data Source Unification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为数据源配置提供统一后端入口、区分业务/IT视图，并支持静态/字典树形数据结构，同时保持现有设计器与 Schema 流程可用。

**Architecture:** 后端新增统一查询门面与业务配置服务，继续复用 `DataProvider` 作为聚合根承载所有数据源配置；前端继续通过现有设计器与 Schema 页面消费统一 DTO。统一入口只做聚合与执行，不把角色逻辑塞进领域层，角色过滤保留在前端或门面 DTO 级别。

**Tech Stack:** Java 21, Spring Boot 3.5.14, MyBatis-Plus, MapStruct, H2/openGauss-compatible SQL, HTML5, 原生 JavaScript, Fetch/Axios, JUnit 5, MockMvc

---

## File Structure

### New files

- `src/main/java/com/contract/adapter/controller/DataSourceQueryController.java` — 统一数据源查询/执行入口
- `src/main/java/com/contract/adapter/controller/BusinessDataSourceController.java` — 业务配置 STATIC/DICT 入口
- `src/main/java/com/contract/application/template/DataSourceQueryFacadeService.java` — 聚合业务+IT配置并执行查询
- `src/main/java/com/contract/application/template/DataSourceConfigService.java` — 业务配置服务（STATIC/DICT）
- `src/main/java/com/contract/application/template/dto/BusinessDataSourceRequest.java` — 业务配置入参
- `src/main/java/com/contract/application/template/dto/DataSourceQueryDTO.java` — 统一查询结果 DTO
- `src/main/java/com/contract/application/template/dto/OptionDataDTO.java` — 执行查询后统一选项 DTO
- `src/main/java/com/contract/domain/template/gateway/DataProviderExecutor.java` — 数据源执行器接口
- `src/main/java/com/contract/infrastructure/gateway/StaticDataProviderExecutor.java` — STATIC 执行器
- `src/main/java/com/contract/infrastructure/gateway/DictDataProviderExecutor.java` — DICT 执行器（先返回空/占位）
- `src/main/java/com/contract/infrastructure/gateway/HttpDataProviderExecutor.java` — HTTP 执行器占位实现
- `src/main/java/com/contract/infrastructure/gateway/DataProviderExecutorFactory.java` — 执行器工厂
- `src/test/java/com/contract/DataSourceConfigServiceTest.java` — 业务配置服务测试
- `src/test/java/com/contract/DataSourceQueryFacadeServiceTest.java` — 统一查询门面测试
- `src/test/java/com/contract/adapter/controller/DataSourceQueryControllerTest.java` — 统一查询接口测试
- `src/test/java/com/contract/adapter/controller/BusinessDataSourceControllerTest.java` — 业务配置接口测试

### Modified files

- `src/main/java/com/contract/infrastructure/persistence/entity/DataProviderEntity.java` — 增加 `dataSourceCategory`
- `src/main/java/com/contract/domain/template/DataProvider.java` — 增加 `dataSourceCategory`、树形/分类辅助方法、执行兼容能力
- `src/main/java/com/contract/domain/dataprovider/types/ProviderType.java` — 对齐 `PLATFORM_API` / `INTERNAL_QUERY` 命名及业务/IT判断
- `src/main/java/com/contract/domain/dataprovider/types/DataSourceCategory.java` — 增加 `fromProviderType()`、布尔判断方法
- `src/main/java/com/contract/domain/shared/types/ConfigJson.java` — 增加树形结构判断与选项解析方法
- `src/main/java/com/contract/application/template/DataProviderService.java` — 收敛为 IT 配置服务，补充 `listITConfigs()` 与专用 create 方法
- `src/main/java/com/contract/application/template/convert/DataProviderConverter.java` — 适配统一 DTO 读写
- `src/main/java/com/contract/infrastructure/persistence/convert/EntityDataProviderConverter.java` — 映射 `dataSourceCategory`
- `src/main/java/com/contract/infrastructure/persistence/repository/DataProviderRepositoryImpl.java` — 增加按分类查询方法
- `src/main/java/com/contract/domain/template/repository/DataProviderRepository.java` — 暴露分类查询能力
- `src/main/java/com/contract/application/template/dto/DataProviderCreateRequest.java` — 补充树形结构字段说明，统一类型常量
- `src/main/java/com/contract/application/template/dto/FieldComponentCreateRequest.java` — 统一业务侧数据源请求形状
- `src/main/java/com/contract/application/template/FieldComponentService.java` — 通过统一业务配置服务绑定数据源
- `src/main/java/com/contract/application/template/SchemaService.java` — 保存/读取 queryConfigs 与 data source 查询 DTO 关联信息
- `src/main/java/com/contract/application/template/dto/SchemaDTO.java` — 输出统一数据源查询结果或最少 provider 元数据
- `src/main/java/com/contract/application/template/dto/SchemaSaveDTO.java` — 扩展前端保存时的数据源配置块
- `src/main/resources/static/config/js/property-panel.js` — 区分业务/IT视图并支持树形数据源编辑
- `src/main/resources/static/config/js/designer.js` — 统一查询入口接线
- `src/main/resources/static/config/js/config-api.js` — 新增统一数据源查询与执行 API 调用
- `src/main/resources/static/js/preview.js` — 调用统一执行接口并根据树形/扁平结果渲染
- `src/main/resources/static/config/data-source.html` — IT 管理页使用 `/api/v2/it/data-providers`
- `src/test/java/com/contract/DataProviderServiceTest.java` — 对齐 IT 视图行为
- `src/test/java/com/contract/DataProviderControllerTest.java` — 对齐 v2 IT 路径或原路径兼容策略
- `src/test/java/com/contract/SchemaServiceTest.java` — 补充 queryConfigs / provider 元数据聚合断言
- `src/test/java/com/contract/FieldComponentServiceTest.java` — 对齐统一业务配置流程

---

### Task 1: Align DataProvider persistence and domain category model

**Files:**
- Modify: `src/main/java/com/contract/infrastructure/persistence/entity/DataProviderEntity.java`
- Modify: `src/main/java/com/contract/domain/template/DataProvider.java`
- Modify: `src/main/java/com/contract/domain/dataprovider/types/ProviderType.java`
- Modify: `src/main/java/com/contract/domain/dataprovider/types/DataSourceCategory.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/convert/EntityDataProviderConverter.java`
- Modify: `src/main/java/com/contract/domain/template/repository/DataProviderRepository.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/DataProviderRepositoryImpl.java`
- Test: `src/test/java/com/contract/DataProviderServiceTest.java`

- [ ] **Step 1: Write the failing test for category persistence and lookup**

```java
@Test
void testListBusinessAndItConfigsSeparately() {
    DataProvider staticProvider = DataProvider.createStaticOptions(
        "城市选择",
        "[{\"value\":\"BJ\",\"label\":\"北京\"}]"
    );
    DataProvider httpProvider = DataProvider.createHttp(
        "供应商接口",
        "{\"url\":\"/api/suppliers\",\"method\":\"GET\"}"
    );

    repository.save(staticProvider);
    repository.save(httpProvider);

    List<DataProvider> business = repository.findByCategory("BUSINESS");
    List<DataProvider> it = repository.findByCategory("IT");

    assertFalse(business.isEmpty());
    assertFalse(it.isEmpty());
    assertTrue(business.stream().allMatch(p -> "BUSINESS".equals(p.getDataSourceCategory())));
    assertTrue(it.stream().allMatch(p -> "IT".equals(p.getDataSourceCategory())));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=DataProviderServiceTest -q`
Expected: FAIL with missing `dataSourceCategory` field or repository method.

- [ ] **Step 3: Implement minimal category model and repository support**

```java
// DataProviderEntity.java
private String dataSourceCategory;

// DataProviderRepository.java
List<DataProvider> findByCategory(String category);

// DataProviderRepositoryImpl.java
@Override
public List<DataProvider> findByCategory(String category) {
    LambdaQueryWrapper<DataProviderEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(DataProviderEntity::getDataSourceCategory, category)
           .orderByDesc(DataProviderEntity::getCreatedAt);
    return converter.toDomainList(mapper.selectList(wrapper));
}
```

```java
// DataSourceCategory.java
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

    public boolean isBusiness() {
        return this == BUSINESS;
    }

    public boolean isIT() {
        return this == IT;
    }

    public static DataSourceCategory fromProviderType(ProviderType providerType) {
        return providerType.isBusinessConfig() ? BUSINESS : IT;
    }
}
```

```java
// ProviderType.java
public enum ProviderType {
    STATIC("静态选项"),
    DICT("字典数据"),
    HTTP("HTTP接口"),
    PLATFORM_API("平台接口"),
    INTERNAL_QUERY("内部查询");

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
        return this == HTTP || this == PLATFORM_API || this == INTERNAL_QUERY;
    }
}
```

```java
// DataProvider.java (relevant parts)
private final DataSourceCategory dataSourceCategory;

public DataSourceCategory getDataSourceCategoryValue() {
    return dataSourceCategory;
}

public String getDataSourceCategory() {
    return dataSourceCategory != null ? dataSourceCategory.name() : null;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=DataProviderServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/infrastructure/persistence/entity/DataProviderEntity.java \
  src/main/java/com/contract/domain/template/DataProvider.java \
  src/main/java/com/contract/domain/dataprovider/types/ProviderType.java \
  src/main/java/com/contract/domain/dataprovider/types/DataSourceCategory.java \
  src/main/java/com/contract/infrastructure/persistence/convert/EntityDataProviderConverter.java \
  src/main/java/com/contract/domain/template/repository/DataProviderRepository.java \
  src/main/java/com/contract/infrastructure/persistence/repository/DataProviderRepositoryImpl.java \
  src/test/java/com/contract/DataProviderServiceTest.java
git commit -m "feat: persist data source category for providers"
```

---

### Task 2: Add ConfigJson tree parsing support

**Files:**
- Modify: `src/main/java/com/contract/domain/shared/types/ConfigJson.java`
- Create: `src/main/java/com/contract/application/template/dto/OptionDataDTO.java`
- Test: `src/test/java/com/contract/ConfigJsonTest.java`

- [ ] **Step 1: Write the failing test for flat/tree parsing**

```java
@Test
void parseTreeOptionsShouldReturnTreeNodes() {
    ConfigJson configJson = new ConfigJson("""
        {
          \"structure\": \"tree\",
          \"children\": [
            {\"value\":\"BJ\",\"label\":\"北京\",\"children\":[{\"value\":\"HD\",\"label\":\"海淀\"}]}
          ]
        }
        """);

    assertTrue(configJson.isTreeStructure());
    List<OptionDataDTO> options = configJson.parseOptions();
    assertEquals(1, options.size());
    assertEquals("BJ", options.getFirst().getValue());
    assertEquals(1, options.getFirst().getChildren().size());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ConfigJsonTest -q`
Expected: FAIL because `isTreeStructure()` / `parseOptions()` do not exist.

- [ ] **Step 3: Implement minimal tree parsing**

```java
// OptionDataDTO.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionDataDTO {
    private String value;
    private String label;
    private List<OptionDataDTO> children;
}
```

```java
// ConfigJson.java
public boolean isTreeStructure() {
    try {
        JsonNode root = MAPPER.readTree(value);
        return root.isObject() && "tree".equals(root.path("structure").asText());
    } catch (JsonProcessingException e) {
        throw new BizException("解析配置JSON失败: " + e.getMessage());
    }
}

public List<OptionDataDTO> parseOptions() {
    try {
        JsonNode root = MAPPER.readTree(value);
        if (root.isArray()) {
            return parseOptionArray(root);
        }
        if (root.isObject() && root.has("children")) {
            return parseOptionArray(root.get("children"));
        }
        return List.of();
    } catch (JsonProcessingException e) {
        throw new BizException("解析配置JSON失败: " + e.getMessage());
    }
}

private List<OptionDataDTO> parseOptionArray(JsonNode arrayNode) {
    List<OptionDataDTO> result = new ArrayList<>();
    for (JsonNode item : arrayNode) {
        result.add(OptionDataDTO.builder()
            .value(item.path("value").asText())
            .label(item.path("label").asText())
            .children(item.has("children") ? parseOptionArray(item.get("children")) : List.of())
            .build());
    }
    return result;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ConfigJsonTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/domain/shared/types/ConfigJson.java \
  src/main/java/com/contract/application/template/dto/OptionDataDTO.java \
  src/test/java/com/contract/ConfigJsonTest.java
git commit -m "feat: support tree and flat option parsing in config json"
```

---

### Task 3: Split business config service from IT provider service

**Files:**
- Create: `src/main/java/com/contract/application/template/DataSourceConfigService.java`
- Create: `src/main/java/com/contract/application/template/dto/BusinessDataSourceRequest.java`
- Modify: `src/main/java/com/contract/application/template/DataProviderService.java`
- Test: `src/test/java/com/contract/DataSourceConfigServiceTest.java`

- [ ] **Step 1: Write the failing test for business config creation**

```java
@Test
void createStaticBusinessConfigShouldPersistBusinessCategory() {
    BusinessDataSourceRequest request = BusinessDataSourceRequest.builder()
        .providerName("城市选择")
        .providerType("STATIC")
        .configJson("[{\"value\":\"BJ\",\"label\":\"北京\"}]")
        .build();

    DataProviderDTO result = service.create(request);

    assertNotNull(result.getId());
    assertEquals("STATIC", result.getProviderType());
    assertEquals("BUSINESS", result.getDataSourceCategory());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=DataSourceConfigServiceTest -q`
Expected: FAIL because service and request type do not exist.

- [ ] **Step 3: Implement business config service**

```java
// BusinessDataSourceRequest.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDataSourceRequest {
    @NotBlank
    private String providerName;
    @NotBlank
    private String providerType;
    @NotBlank
    private String configJson;
    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
}
```

```java
// DataSourceConfigService.java
@Service
@RequiredArgsConstructor
public class DataSourceConfigService {
    private final DataProviderRepository repository;
    private final com.contract.application.template.convert.DataProviderConverter converter;

    @Transactional
    public DataProviderDTO create(BusinessDataSourceRequest request) {
        DataProvider provider;
        if ("STATIC".equals(request.getProviderType())) {
            provider = DataProvider.createStaticOptions(request.getProviderName(), request.getConfigJson());
        } else if ("DICT".equals(request.getProviderType())) {
            provider = DataProvider.createDict(request.getConfigJson(), request.getProviderName());
        } else {
            throw new BizException("业务配置仅支持 STATIC/DICT");
        }
        return converter.toDTO(repository.save(provider));
    }

    public List<DataProviderDTO> listBusinessConfigs() {
        return converter.toDTOList(repository.findByCategory("BUSINESS"));
    }
}
```

```java
// DataProviderService.java
public List<DataProviderDTO> listITConfigs() {
    return converter.toDTOList(repository.findByCategory("IT"));
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=DataSourceConfigServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/application/template/DataSourceConfigService.java \
  src/main/java/com/contract/application/template/dto/BusinessDataSourceRequest.java \
  src/main/java/com/contract/application/template/DataProviderService.java \
  src/test/java/com/contract/DataSourceConfigServiceTest.java
git commit -m "feat: split business data source config from IT provider service"
```

---

### Task 4: Add unified query facade and executor strategy

**Files:**
- Create: `src/main/java/com/contract/domain/template/gateway/DataProviderExecutor.java`
- Create: `src/main/java/com/contract/infrastructure/gateway/StaticDataProviderExecutor.java`
- Create: `src/main/java/com/contract/infrastructure/gateway/DictDataProviderExecutor.java`
- Create: `src/main/java/com/contract/infrastructure/gateway/HttpDataProviderExecutor.java`
- Create: `src/main/java/com/contract/infrastructure/gateway/DataProviderExecutorFactory.java`
- Create: `src/main/java/com/contract/application/template/DataSourceQueryFacadeService.java`
- Create: `src/main/java/com/contract/application/template/dto/DataSourceQueryDTO.java`
- Test: `src/test/java/com/contract/DataSourceQueryFacadeServiceTest.java`

- [ ] **Step 1: Write the failing test for unified query**

```java
@Test
void queryAllShouldMergeBusinessAndItConfigs() {
    List<DataSourceQueryDTO> result = facadeService.queryAll();
    assertNotNull(result);
    assertTrue(result.stream().anyMatch(item -> "BUSINESS".equals(item.getConfigSource())));
    assertTrue(result.stream().anyMatch(item -> "IT".equals(item.getConfigSource())));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=DataSourceQueryFacadeServiceTest -q`
Expected: FAIL because facade and DTO do not exist.

- [ ] **Step 3: Implement facade and static executor**

```java
// DataProviderExecutor.java
public interface DataProviderExecutor {
    boolean supports(String providerType);
    List<OptionDataDTO> execute(DataProvider provider);
}
```

```java
// StaticDataProviderExecutor.java
@Repository
public class StaticDataProviderExecutor implements DataProviderExecutor {
    @Override
    public boolean supports(String providerType) {
        return "STATIC".equals(providerType);
    }

    @Override
    public List<OptionDataDTO> execute(DataProvider provider) {
        return new ConfigJson(provider.getConfigJson()).parseOptions();
    }
}
```

```java
// DataSourceQueryDTO.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceQueryDTO {
    private Long id;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String dataSourceCategory;
    private String configSource;
    private String configJson;
    private Boolean isTree;
}
```

```java
// DataSourceQueryFacadeService.java
@Service
@RequiredArgsConstructor
public class DataSourceQueryFacadeService {
    private final DataSourceConfigService dataSourceConfigService;
    private final DataProviderService dataProviderService;
    private final DataProviderRepository repository;
    private final DataProviderExecutorFactory executorFactory;

    public List<DataSourceQueryDTO> queryAll() {
        List<DataSourceQueryDTO> result = new ArrayList<>();
        result.addAll(dataSourceConfigService.listBusinessConfigs().stream().map(this::toQueryDTO).toList());
        result.addAll(dataProviderService.listITConfigs().stream().map(this::toQueryDTO).toList());
        return result;
    }

    public List<OptionDataDTO> executeQuery(Long providerId) {
        DataProvider provider = repository.findById(providerId);
        if (provider == null) {
            throw new BizException("数据源不存在: " + providerId);
        }
        return executorFactory.getExecutor(provider.getProviderType()).execute(provider);
    }

    private DataSourceQueryDTO toQueryDTO(DataProviderDTO dto) {
        return DataSourceQueryDTO.builder()
            .id(dto.getId())
            .providerCode(dto.getProviderCode())
            .providerName(dto.getProviderName())
            .providerType(dto.getProviderType())
            .dataSourceCategory(dto.getDataSourceCategory())
            .configSource(dto.getDataSourceCategory())
            .configJson(dto.getConfigJson())
            .isTree(dto.getConfigJson() != null && dto.getConfigJson().contains("\"structure\": \"tree\""))
            .build();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=DataSourceQueryFacadeServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/domain/template/gateway/DataProviderExecutor.java \
  src/main/java/com/contract/infrastructure/gateway/StaticDataProviderExecutor.java \
  src/main/java/com/contract/infrastructure/gateway/DictDataProviderExecutor.java \
  src/main/java/com/contract/infrastructure/gateway/HttpDataProviderExecutor.java \
  src/main/java/com/contract/infrastructure/gateway/DataProviderExecutorFactory.java \
  src/main/java/com/contract/application/template/DataSourceQueryFacadeService.java \
  src/main/java/com/contract/application/template/dto/DataSourceQueryDTO.java \
  src/test/java/com/contract/DataSourceQueryFacadeServiceTest.java
git commit -m "feat: add unified data source query facade and static executor"
```

---

### Task 5: Expose v2 backend APIs for UI, business, and IT views

**Files:**
- Create: `src/main/java/com/contract/adapter/controller/DataSourceQueryController.java`
- Create: `src/main/java/com/contract/adapter/controller/BusinessDataSourceController.java`
- Modify: `src/main/java/com/contract/adapter/controller/DataProviderController.java`
- Test: `src/test/java/com/contract/adapter/controller/DataSourceQueryControllerTest.java`
- Test: `src/test/java/com/contract/adapter/controller/BusinessDataSourceControllerTest.java`
- Test: `src/test/java/com/contract/DataProviderControllerTest.java`

- [ ] **Step 1: Write the failing MockMvc tests**

```java
@Test
void queryAllShouldReturnUnifiedDataSources() throws Exception {
    mockMvc.perform(get("/api/v2/ui/data-sources/query"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray());
}

@Test
void createBusinessStaticShouldReturnCreatedProvider() throws Exception {
    String body = """
        {
          "providerName": "城市选择",
          "providerType": "STATIC",
          "configJson": "[{\"value\":\"BJ\",\"label\":\"北京\"}]"
        }
        """;

    mockMvc.perform(post("/api/v2/config/business-data-sources")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.providerType").value("STATIC"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=DataSourceQueryControllerTest,BusinessDataSourceControllerTest,DataProviderControllerTest -q`
Expected: FAIL because new controllers/routes do not exist.

- [ ] **Step 3: Implement controllers**

```java
@RestController
@RequestMapping("/api/v2/ui/data-sources")
@RequiredArgsConstructor
public class DataSourceQueryController {
    private final DataSourceQueryFacadeService service;

    @GetMapping("/query")
    public Result<List<DataSourceQueryDTO>> queryAll(@RequestParam(required = false) String type) {
        return Result.ok(type == null ? service.queryAll() : service.queryByType(type));
    }

    @GetMapping("/{providerId}/execute")
    public Result<List<OptionDataDTO>> execute(@PathVariable Long providerId) {
        return Result.ok(service.executeQuery(providerId));
    }
}
```

```java
@RestController
@RequestMapping("/api/v2/config/business-data-sources")
@RequiredArgsConstructor
public class BusinessDataSourceController {
    private final DataSourceConfigService service;

    @PostMapping
    public Result<DataProviderDTO> create(@RequestBody @Valid BusinessDataSourceRequest request) {
        return Result.ok(service.create(request));
    }

    @GetMapping
    public Result<List<DataProviderDTO>> list() {
        return Result.ok(service.listBusinessConfigs());
    }
}
```

```java
@RestController
@RequestMapping({"/api/data-providers", "/api/v2/it/data-providers"})
@RequiredArgsConstructor
public class DataProviderController {
    // keep existing CRUD methods
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=DataSourceQueryControllerTest,BusinessDataSourceControllerTest,DataProviderControllerTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/adapter/controller/DataSourceQueryController.java \
  src/main/java/com/contract/adapter/controller/BusinessDataSourceController.java \
  src/main/java/com/contract/adapter/controller/DataProviderController.java \
  src/test/java/com/contract/adapter/controller/DataSourceQueryControllerTest.java \
  src/test/java/com/contract/adapter/controller/BusinessDataSourceControllerTest.java \
  src/test/java/com/contract/DataProviderControllerTest.java
git commit -m "feat: expose unified business and IT data source APIs"
```

---

### Task 6: Route component business datasource binding through unified backend flow

**Files:**
- Modify: `src/main/java/com/contract/application/template/dto/DataProviderCreateRequest.java`
- Modify: `src/main/java/com/contract/application/template/dto/FieldComponentCreateRequest.java`
- Modify: `src/main/java/com/contract/application/template/FieldComponentService.java`
- Test: `src/test/java/com/contract/FieldComponentServiceTest.java`

- [ ] **Step 1: Write the failing test for tree/static binding**

```java
@Test
void createSelectComponentWithStaticTreeShouldBindCreatedProvider() {
    FieldComponentCreateRequest request = FieldComponentCreateRequest.builder()
        .layoutNodeId(layoutNodeId)
        .fieldDefId(fieldDefId)
        .componentType("SELECT")
        .labelName("区域")
        .dataSourceType("STATIC")
        .staticOptionsJson("{\"structure\":\"tree\",\"children\":[{\"value\":\"BJ\",\"label\":\"北京\"}]}")
        .build();

    FieldComponentDTO result = service.create(templateId, versionId, request);
    assertNotNull(result.getDataProviderId());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=FieldComponentServiceTest -q`
Expected: FAIL because service still uses legacy assumptions.

- [ ] **Step 3: Implement unified binding flow**

```java
// DataProviderCreateRequest.java
/**
 * staticOptionsJson 支持两种格式：
 * 1. 扁平数组: [{"value":"BJ","label":"北京"}]
 * 2. 树形对象: {"structure":"tree","children":[...]}
 */
```

```java
// FieldComponentService.java (relevant section)
private final DataSourceConfigService dataSourceConfigService;

private Long handleDataProvider(FieldComponentCreateRequest request) {
    String type = request.getDataSourceType();
    if (type == null || type.isBlank()) {
        return null;
    }

    if ("STATIC".equals(type) || "DICT".equals(type)) {
        BusinessDataSourceRequest businessRequest = BusinessDataSourceRequest.builder()
            .providerName(request.getLabelName() + "-数据源")
            .providerType(type)
            .configJson("STATIC".equals(type) ? request.getStaticOptionsJson() : "{\"dictType\":\"" + request.getDictType() + "\"}")
            .build();
        return dataSourceConfigService.create(businessRequest).getId();
    }

    DataProviderCreateRequest dpRequest = DataProviderCreateRequest.builder()
        .dataSourceType(type)
        .displayName(request.getLabelName() + "-数据源")
        .dataProviderId(request.getDataProviderId())
        .build();
    return dataProviderService.createFromBusinessRequest(dpRequest).getId();
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=FieldComponentServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/application/template/dto/DataProviderCreateRequest.java \
  src/main/java/com/contract/application/template/dto/FieldComponentCreateRequest.java \
  src/main/java/com/contract/application/template/FieldComponentService.java \
  src/test/java/com/contract/FieldComponentServiceTest.java
git commit -m "feat: unify component data source binding for business and IT flows"
```

---

### Task 7: Surface unified datasource metadata through schema and frontend API client

**Files:**
- Modify: `src/main/java/com/contract/application/template/dto/SchemaDTO.java`
- Modify: `src/main/java/com/contract/application/template/dto/SchemaSaveDTO.java`
- Modify: `src/main/java/com/contract/application/template/SchemaService.java`
- Modify: `src/main/resources/static/config/js/config-api.js`
- Test: `src/test/java/com/contract/SchemaServiceTest.java`

- [ ] **Step 1: Write the failing schema aggregation test**

```java
@Test
void getSchemaShouldIncludeQueryConfigsAndDataSourceMetadata() {
    SchemaDTO schema = schemaService.getSchema(templateId, versionId);
    assertNotNull(schema.getFieldComponents());
    assertNotNull(schema.getQueryConfigs());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=SchemaServiceTest -q`
Expected: FAIL because query configs are not returned.

- [ ] **Step 3: Implement schema exposure**

```java
// SchemaDTO.java
private List<QueryConfigDTO> queryConfigs;
private List<DataSourceQueryDTO> dataSources;
```

```java
// SchemaService.java (constructor fields)
private final QueryConfigService queryConfigService;
private final DataSourceQueryFacadeService dataSourceQueryFacadeService;

// getSchema()
List<QueryConfigDTO> queryConfigs = queryConfigService.listByVersionId(versionId);
List<DataSourceQueryDTO> dataSources = dataSourceQueryFacadeService.queryAll();

SchemaDTO schema = SchemaDTO.builder()
    // existing fields
    .queryConfigs(queryConfigs)
    .dataSources(dataSources)
    .build();
```

```javascript
// config-api.js
async function loadUnifiedDataSources() {
  const response = await fetch('/api/v2/ui/data-sources/query');
  const result = await response.json();
  if (!result.success) throw new Error(result.message || '加载统一数据源失败');
  return result.data || [];
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=SchemaServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/contract/application/template/dto/SchemaDTO.java \
  src/main/java/com/contract/application/template/dto/SchemaSaveDTO.java \
  src/main/java/com/contract/application/template/SchemaService.java \
  src/main/resources/static/config/js/config-api.js \
  src/test/java/com/contract/SchemaServiceTest.java
git commit -m "feat: expose unified data source metadata through schema"
```

---

### Task 8: Update frontend business/IT views and tree option rendering

**Files:**
- Modify: `src/main/resources/static/config/js/property-panel.js`
- Modify: `src/main/resources/static/config/js/designer.js`
- Modify: `src/main/resources/static/js/preview.js`
- Modify: `src/main/resources/static/config/data-source.html`

- [ ] **Step 1: Add failing browser/manual check scenario**

```text
Scenario:
1. 打开 template-designer.html
2. 选择 SELECT 组件
3. 业务视图下只能看到 STATIC/DICT 业务配置项
4. 选择 STATIC 树形结构后能录入 tree JSON
5. 预览页通过统一 execute 接口渲染选项
```

- [ ] **Step 2: Run the scenario to verify current behavior fails**

Run: `mvn spring-boot:run`
Expected: 前端仍使用旧 `/api/data-providers` 或旧 dataSourceId 逻辑，无法区分业务/IT视图或树形渲染。

- [ ] **Step 3: Implement minimal frontend wiring**

```javascript
// property-panel.js (relevant select config)
<select id="configDataSourceType" onchange="updateProperty('dataSourceType', this.value)">
  <option value="STATIC">静态数据</option>
  <option value="DICT">字典数据</option>
  <option value="HTTP">HTTP接口</option>
  <option value="PLATFORM_API">平台接口</option>
  <option value="INTERNAL_QUERY">内部查询</option>
</select>
```

```javascript
// designer.js
async function loadUnifiedProviders(role) {
  const response = await fetch('/api/v2/ui/data-sources/query');
  const result = await response.json();
  const providers = result.data || [];
  return providers.filter(item => role === 'IT' ? item.configSource === 'IT' : item.configSource === 'BUSINESS');
}
```

```javascript
// preview.js
async function renderSelectOptions(providerId, container) {
  const response = await axios.get(`/api/v2/ui/data-sources/${providerId}/execute`);
  const options = response.data.data || [];
  if (options.some(item => item.children && item.children.length > 0)) {
    return renderTreeSelect(container, options);
  }
  return renderFlatSelect(container, options);
}
```

```javascript
// config/data-source.html
// 使用 /api/v2/it/data-providers 替换旧 /api/data-providers 页面调用
```

- [ ] **Step 4: Run the scenario to verify it passes**

Run: `mvn spring-boot:run`
Then validate in browser: `/config/template-designer.html` and `/config/data-source.html`
Expected: 业务/IT视图分流正常，树形/扁平选项都能渲染。

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/static/config/js/property-panel.js \
  src/main/resources/static/config/js/designer.js \
  src/main/resources/static/js/preview.js \
  src/main/resources/static/config/data-source.html
git commit -m "feat: unify frontend business and IT data source views"
```

---

### Task 9: Run regression tests and browser verification

**Files:**
- Test: `src/test/java/com/contract/DataProviderServiceTest.java`
- Test: `src/test/java/com/contract/FieldComponentServiceTest.java`
- Test: `src/test/java/com/contract/SchemaServiceTest.java`
- Test: `src/test/java/com/contract/adapter/controller/TemplateControllerTest.java`

- [ ] **Step 1: Run focused backend tests**

```bash
mvn test -Dtest=DataProviderServiceTest,DataSourceConfigServiceTest,DataSourceQueryFacadeServiceTest,FieldComponentServiceTest,SchemaServiceTest,DataProviderControllerTest,DataSourceQueryControllerTest,BusinessDataSourceControllerTest -q
```

Expected: PASS

- [ ] **Step 2: Run full test suite**

```bash
mvn test -q
```

Expected: PASS with `Failures: 0, Errors: 0`

- [ ] **Step 3: Run browser verification**

```bash
mvn spring-boot:run
```

Then validate:
- `/config/template-designer.html`
- `/config/data-source.html`
- `GET /api/v2/ui/data-sources/query`
- `GET /api/v2/ui/data-sources/{id}/execute`

Expected:
- STATIC flat executes to flat options
- STATIC tree executes to tree options
- BUSINESS/IT filtering behaves as designed

- [ ] **Step 4: Commit**

```bash
git add .
git commit -m "test: verify unified data source flow end to end"
```

---

## Self-review

- Spec coverage: covered unified query entry, business/IT split, tree support, DataProvider/DataSourceConfig split, executor strategy, backend APIs, frontend adapters, schema integration, and regression verification.
- Placeholder scan: removed TBD-style placeholders from the plan; where functionality is intentionally stubbed (DICT/HTTP executors), the exact file and minimal behavior are specified.
- Type consistency: provider type names are normalized to `STATIC/DICT/HTTP/PLATFORM_API/INTERNAL_QUERY` in the plan; this is intentionally included as part of the implementation to remove the current `PLATFORM/INTERNAL` mismatch.
