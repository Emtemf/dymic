# 数据源配置统一设计文档

> **版本**：V1.0
> **日期**：2026-06-09
> **状态**：设计中

---

## 一、需求概述

### 1.1 背景

现有数据源配置存在以下问题：
1. **入口分散**：业务配置和IT配置没有统一入口
2. **视图混淆**：业务人员和IT人员看到相同的配置界面，业务看不懂技术配置
3. **树形数据不支持**：静态选项和字典引用不支持树形结构

### 1.2 目标

1. **入口收束**：Backend API层统一入口
2. **视图区分**：Backend返回全量数据，Frontend按角色过滤
3. **树形支持**：STATIC静态选项树形 + DICT字典层级结构
4. **职责分离**：业务人员配置STATIC/DICT，IT人员配置HTTP/PLATFORM/INTERNAL

### 1.3 成功标准

- 业务人员能独立配置静态选项（含树形），无需IT介入
- IT配置外部数据源后，业务能直接使用，无需理解技术细节
- 前端能统一渲染所有数据源类型（扁平+树形），无需特殊处理

---

## 二、架构设计

### 2.1 分层结构

```
Controller层：
  /api/v2/ui/data-sources/query          → DataSourceQueryController（统一查询入口）
  /api/v2/config/business-data-sources   → BusinessDataSourceController（业务配置）
  /api/v2/it/data-providers              → DataProviderController（IT配置）

应用层：
  DataSourceQueryFacadeService           → 统一查询，聚合业务+IT配置
  DataSourceConfigService                → 处理 STATIC/DICT 业务配置
  DataProviderService                    → 处理 HTTP/PLATFORM/INTERNAL IT配置

领域层：
  DataProvider                           → 聚合根
  DataProviderExecutor                   → 执行器接口
  值对象：ProviderType、DataSourceCategory、ProviderStatus、ConfigJson

基础设施层：
  DataProviderExecutorImpl               → 执行器实现（策略模式）
  DataProviderRepositoryImpl             → 仓储实现
  DataProviderConverter                  → MapStruct转换器
```

### 2.2 路由逻辑

```java
DataSourceQueryFacadeService.queryAll() {
    // 1. 查询业务配置
    List<DataProvider> businessConfigs = dataSourceConfigService.listBusinessConfigs();
    //    → providerType = STATIC 且 dataSourceCategory = BUSINESS
    //    → providerType = DICT 且 dataSourceCategory = BUSINESS
    
    // 2. 查询IT配置
    List<DataProvider> itConfigs = dataProviderService.listITConfigs();
    //    → providerType = HTTP/PLATFORM/INTERNAL
    //    → dataSourceCategory = IT
    
    // 3. 合并返回，标记 configSource
    return mergeWithConfigSource(businessConfigs, itConfigs);
}
```

---

## 三、DDD规范设计

### 3.1 聚合定义

**DataProvider聚合**：
- **聚合根**：DataProvider
- **值对象**：ProviderType、DataSourceCategory、ProviderStatus、ConfigJson
- **聚合边界**：DataProvider的配置数据是一个完整的聚合，外部只能通过聚合根引用
- **事务边界**：事务不跨越聚合边界

### 3.2 聚合根设计

```java
/**
 * 数据提供方聚合根
 * 
 * ===== 领域统一业务语言 =====
 * 
 * 【状态流转】
 * 
 *   创建 → ENABLED ⇄ DISABLED
 *   
 *   - 创建：通过工厂方法创建，初始状态为ENABLED
 *   - 停用：ENABLED → DISABLED（业务方法：disable）
 *   - 启用：DISABLED → ENABLED（业务方法：enable）
 *   
 * 【业务规则】
 * 
 * 1. 业务配置（BUSINESS）：
 *    - providerType = STATIC 或 DICT
 *    - 由业务人员配置
 *    - STATIC不缓存，DICT缓存1小时
 *    
 * 2. IT配置（IT）：
 *    - providerType = HTTP、PLATFORM_API、INTERNAL_QUERY
 *    - 由IT人员配置
 *    - 默认缓存5-10分钟
 *    
 * 【聚合边界】
 * 
 * - DataProvider是聚合根
 * - 包含值对象：ProviderType、DataSourceCategory、ProviderStatus、ConfigJson
 * - 外部只能通过聚合根引用，不能直接访问值对象
 * - 事务不跨越聚合边界
 * 
 * 【业务方法契约】
 * 
 * - disable(): 
 *   前置条件：status = ENABLED
 *   后置条件：status = DISABLED
 *   异常：status = DISABLED时抛出IllegalStateException
 *   
 * - enable():
 *   前置条件：status = DISABLED
 *   后置条件：status = ENABLED
 *   异常：status = ENABLED时抛出IllegalStateException
 * 
 */
public class DataProvider {
    // 标识
    private Long id;
    private String providerCode;
    private String providerName;
    
    // 值对象
    private ProviderType providerType;
    private DataSourceCategory dataSourceCategory;
    private ConfigJson configJson;
    private ProviderStatus status;
    
    // 配置属性
    private Boolean cacheEnabled;
    private Integer cacheTtlSeconds;
    
    // 审计字段
    private Long createdBy;
    private String createdName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private LocalDateTime updatedAt;
    
    // 私有构造器
    private DataProvider() {}
    
    // 工厂方法
    public static DataProvider createStaticOptions(String displayName, ConfigJson configJson);
    public static DataProvider createDict(String displayName, String dictType);
    public static DataProvider createHttp(String displayName, ConfigJson configJson);
    // ...
    
    // 业务方法
    public void disable();
    public void enable();
    public boolean isBusinessConfig();
    public boolean isITConfig();
    
    // Getters（有限暴露）
    // Setters（仅Repository使用）
}
```

### 3.3 值对象设计

#### ProviderType（数据源类型）

```java
/**
 * 数据源类型值对象
 * 
 * ===== 业务含义 =====
 * 
 * - STATIC：静态选项（业务配置固定选项，如城市、是否）
 * - DICT：字典引用（业务引用系统字典，如合同类型）
 * - HTTP：HTTP接口（IT配置外部接口）
 * - PLATFORM_API：平台集成（IT配置平台能力，如组织、人员）
 * - INTERNAL_QUERY：内部查询（IT配置本系统查询）
 */
public enum ProviderType {
    STATIC,
    DICT,
    HTTP,
    PLATFORM_API,
    INTERNAL_QUERY;
    
    public boolean isBusinessType() {
        return this == STATIC || this == DICT;
    }
    
    public boolean isITType() {
        return this == HTTP || this == PLATFORM_API || this == INTERNAL_QUERY;
    }
}
```

#### DataSourceCategory（数据源分类）

```java
/**
 * 数据源分类值对象
 * 
 * ===== 业务含义 =====
 * 
 * - BUSINESS：业务配置（业务人员配置STATIC/DICT）
 * - IT：IT配置（IT人员配置HTTP/PLATFORM/INTERNAL）
 */
public enum DataSourceCategory {
    BUSINESS,
    IT;
    
    public boolean isBusiness() {
        return this == BUSINESS;
    }
    
    public boolean isIT() {
        return this == IT;
    }
}
```

#### ProviderStatus（数据源状态）

```java
/**
 * 数据源状态值对象
 * 
 * ===== 状态流转 =====
 * 
 *   ENABLED ⇄ DISABLED
 *   
 * - ENABLED：启用（可使用）
 * - DISABLED：停用（不可使用）
 */
public enum ProviderStatus {
    ENABLED,
    DISABLED;
    
    public boolean isEnabled() {
        return this == ENABLED;
    }
    
    public boolean isDisabled() {
        return this == DISABLED;
    }
}
```

#### ConfigJson（配置JSON）

```java
/**
 * 配置JSON值对象
 * 
 * ===== 业务含义 =====
 * 
 * 封装JSON配置，提供解析方法：
 * - 扁平列表：[{"value":"BJ","label":"北京"}]
 * - 树形结构：{"structure":"tree","children":[...]}
 * - 字典引用：{"dictType":"INDUSTRY_TYPE"}
 * - HTTP配置：{"url":"...","method":"GET",...}
 */
public class ConfigJson {
    private final String value;
    
    public ConfigJson(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ConfigJson不能为空");
        }
        this.value = value;
    }
    
    public static ConfigJson fromDictType(String dictType);
    public String getValue();
    public boolean isTreeStructure();
    public List<OptionData> parseOptions();
    public String extractDictType();
    
    // equals, hashCode, toString
}
```

---

## 四、数据模型设计

### 4.1 数据库设计

**核心原则**：
- **生产环境（核心）**：高斯数据库，JSONB类型
- **测试环境（辅助）**：H2数据库，JSON类型
- **H2仅用于单元测试，核心是高斯数据库**

#### 高斯建表脚本

```sql
CREATE TABLE t_ui_data_provider (
    id BIGSERIAL PRIMARY KEY,
    provider_code VARCHAR(50) NOT NULL UNIQUE,
    provider_name VARCHAR(100) NOT NULL,
    provider_type VARCHAR(20) NOT NULL,
    data_source_category VARCHAR(20) NOT NULL,
    config_json JSONB,
    cache_enabled INTEGER DEFAULT 0,
    cache_ttl_seconds INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ENABLED',
    created_by BIGINT,
    created_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_name VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INTEGER DEFAULT 0
);

-- 索引
CREATE INDEX idx_provider_type ON t_ui_data_provider(provider_type);
CREATE INDEX idx_provider_category ON t_ui_data_provider(data_source_category);
CREATE INDEX idx_provider_config_dict_type 
ON t_ui_data_provider ((config_json->>'dictType'))
WHERE provider_type = 'DICT';

-- 注释
COMMENT ON TABLE t_ui_data_provider IS '数据提供方配置';
COMMENT ON COLUMN t_ui_data_provider.data_source_category IS '数据源分类：BUSINESS-业务配置，IT-IT配置';
COMMENT ON COLUMN t_ui_data_provider.config_json IS '配置JSON，支持扁平/树形结构';
```

#### H2建表脚本

```sql
CREATE TABLE t_ui_data_provider (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_code VARCHAR(50) NOT NULL UNIQUE,
    provider_name VARCHAR(100) NOT NULL,
    provider_type VARCHAR(20) NOT NULL,
    data_source_category VARCHAR(20) NOT NULL,
    config_json JSON,
    cache_enabled INTEGER DEFAULT 0,
    cache_ttl_seconds INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ENABLED',
    created_by BIGINT,
    created_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_name VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INTEGER DEFAULT 0
);
```

### 4.2 字段关系

| providerType | dataSourceCategory | 谁配置 | 说明 |
|---|---|---|---|
| STATIC | BUSINESS | 业务人员 | 静态选项（城市、是否等） |
| DICT | BUSINESS | 业务人员 | 字典引用（合同类型等） |
| HTTP | IT | IT人员 | 外部HTTP接口 |
| PLATFORM_API | IT | IT人员 | 平台集成能力 |
| INTERNAL_QUERY | IT | IT人员 | 本系统内部查询 |

### 4.3 configJson格式

#### STATIC扁平格式

```json
[
  {"value": "BJ", "label": "北京"},
  {"value": "SH", "label": "上海"}
]
```

#### STATIC树形格式

```json
{
  "structure": "tree",
  "children": [
    {
      "value": "BJ",
      "label": "北京",
      "children": [
        {"value": "BJ-HD", "label": "海淀区"},
        {"value": "BJ-DC", "label": "东城区"}
      ]
    },
    {
      "value": "SH",
      "label": "上海",
      "children": [
        {"value": "SH-PD", "label": "浦东新区"}
      ]
    }
  ]
}
```

#### DICT格式

```json
{"dictType": "INDUSTRY_TYPE"}
```

#### HTTP格式

```json
{
  "url": "/api/suppliers",
  "method": "GET",
  "valueField": "id",
  "labelField": "name"
}
```

---

## 五、服务层设计

### 5.1 DataSourceConfigService（业务配置服务）

```java
/**
 * 业务配置服务
 * 
 * 职责：处理 STATIC/DICT 的业务配置，由业务人员操作
 */
public class DataSourceConfigService {
    
    /**
     * 创建静态选项（业务配置）
     */
    public DataProviderDTO createStaticOptions(BusinessDataSourceRequest request);
    
    /**
     * 创建字典引用（业务配置）
     */
    public DataProviderDTO createDict(BusinessDataSourceRequest request);
    
    /**
     * 更新业务配置
     */
    public DataProviderDTO update(Long id, BusinessDataSourceRequest request);
    
    /**
     * 删除业务配置（仅限 BUSINESS 类）
     */
    public void delete(Long id);
    
    /**
     * 查询业务配置列表
     */
    public List<DataProviderDTO> listBusinessConfigs();
    
    /**
     * 验证 configJson 格式（扁平或树形）
     */
    private void validateStaticConfigJson(String configJson);
}
```

### 5.2 DataProviderService（IT配置服务）

```java
/**
 * IT配置服务
 * 
 * 职责：处理 HTTP/PLATFORM/INTERNAL 的 IT 配置
 */
public class DataProviderService {
    
    /**
     * 创建HTTP数据源
     */
    public DataProviderDTO createHttp(DataProviderCreateDTO dto);
    
    /**
     * 创建平台数据源
     */
    public DataProviderDTO createPlatform(DataProviderCreateDTO dto);
    
    /**
     * 创建内部查询数据源
     */
    public DataProviderDTO createInternal(DataProviderCreateDTO dto);
    
    /**
     * 查询IT配置列表
     */
    public List<DataProviderDTO> listITConfigs();
    
    // 其他CRUD方法...
}
```

### 5.3 DataSourceQueryFacadeService（统一查询门面）

```java
/**
 * 统一查询门面服务
 * 
 * 职责：聚合业务+IT配置，返回全量数据供前端过滤
 */
public class DataSourceQueryFacadeService {
    
    /**
     * 统一查询所有数据源（返回全量，前端按角色过滤）
     */
    public List<DataSourceQueryDTO> queryAll();
    
    /**
     * 按类型查询
     */
    public List<DataSourceQueryDTO> queryByType(String providerType);
    
    /**
     * 执行数据源查询（调用真实数据源，返回选项数据）
     */
    public List<OptionDataDTO> executeQuery(Long providerId);
}
```

### 5.4 DataSourceQueryDTO

```java
public class DataSourceQueryDTO {
    private Long id;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String dataSourceCategory;
    private String configSource;       // BUSINESS/IT（前端过滤用）
    private String configJson;
    private Boolean isTree;            // 是否树形结构
}
```

---

## 六、执行层设计（策略模式）

### 6.1 领域层接口定义

```java
/**
 * 数据源执行器接口（领域层）
 */
public interface DataProviderExecutor {
    /**
     * 判断是否支持该类型
     */
    boolean supports(String providerType);
    
    /**
     * 执行数据源查询
     */
    List<OptionDataDTO> execute(DataProvider provider);
}
```

### 6.2 基础设施层实现

#### StaticDataProviderExecutor（本次实现）

```java
/**
 * 静态数据源执行器
 */
@Repository
public class StaticDataProviderExecutor implements DataProviderExecutor {
    
    @Override
    public boolean supports(String providerType) {
        return "STATIC".equals(providerType);
    }
    
    @Override
    public List<OptionDataDTO> execute(DataProvider provider) {
        ConfigJson configJson = provider.getConfigJson();
        return configJson.parseOptions();
    }
}
```

#### DictDataProviderExecutor（预留）

```java
/**
 * 字典数据源执行器（预留）
 */
@Repository
public class DictDataProviderExecutor implements DataProviderExecutor {
    
    @Override
    public boolean supports(String providerType) {
        return "DICT".equals(providerType);
    }
    
    @Override
    public List<OptionDataDTO> execute(DataProvider provider) {
        // TODO: 后续实现字典服务
        return Collections.emptyList();
    }
}
```

#### HttpDataProviderExecutor（预留）

```java
/**
 * HTTP数据源执行器（预留）
 */
@Repository
public class HttpDataProviderExecutor implements DataProviderExecutor {
    
    @Override
    public boolean supports(String providerType) {
        return "HTTP".equals(providerType);
    }
    
    @Override
    public List<OptionDataDTO> execute(DataProvider provider) {
        // TODO: 后续实现HTTP调用
        return Collections.emptyList();
    }
}
```

### 6.3 策略工厂

```java
/**
 * 执行器工厂
 */
@Repository
public class DataProviderExecutorFactory {
    private final List<DataProviderExecutor> executors;
    
    public DataProviderExecutorFactory(List<DataProviderExecutor> executors) {
        this.executors = executors;
    }
    
    public DataProviderExecutor getExecutor(String providerType) {
        return executors.stream()
            .filter(e -> e.supports(providerType))
            .findFirst()
            .orElseThrow(() -> new UnsupportedProviderTypeException(providerType));
    }
}
```

### 6.4 应用层调用

```java
@Service
public class DataSourceQueryFacadeService {
    private final DataProviderRepository repository;
    private final DataProviderExecutorFactory executorFactory;
    
    public List<OptionDataDTO> executeQuery(Long providerId) {
        DataProvider provider = repository.findById(providerId);
        if (provider == null) {
            throw new ProviderNotFoundException(providerId);
        }
        
        DataProviderExecutor executor = executorFactory.getExecutor(provider.getProviderType().name());
        return executor.execute(provider);
    }
}
```

---

## 七、接口层设计

### 7.1 统一查询接口

```java
@Path("/api/v2/ui/data-sources")
public class DataSourceQueryController {
    
    /**
     * 统一查询所有数据源（前端按角色过滤）
     * GET /api/v2/ui/data-sources/query
     */
    @GET
    @Path("/query")
    public Result<List<DataSourceQueryDTO>> queryAll();
    
    /**
     * 按类型查询
     * GET /api/v2/ui/data-sources/query?type=STATIC
     */
    @GET
    @Path("/query")
    public Result<List<DataSourceQueryDTO>> queryByType(@QueryParam("type") String type);
    
    /**
     * 执行数据源查询（返回选项数据）
     * GET /api/v2/ui/data-sources/{providerId}/execute
     */
    @GET
    @Path("/{providerId}/execute")
    public Result<List<OptionDataDTO>> executeQuery(@PathParam("providerId") Long providerId);
}
```

### 7.2 业务配置接口

```java
@Path("/api/v2/config/business-data-sources")
public class BusinessDataSourceController {
    
    @POST
    public Result<DataProviderDTO> create(BusinessDataSourceRequest request);
    
    @PUT
    @Path("/{id}")
    public Result<DataProviderDTO> update(@PathParam("id") Long id, BusinessDataSourceRequest request);
    
    @DELETE
    @Path("/{id}")
    public Result<Void> delete(@PathParam("id") Long id);
    
    @GET
    public Result<List<DataProviderDTO>> list();
}
```

### 7.3 IT配置接口

```java
@Path("/api/v2/it/data-providers")
public class DataProviderController {
    
    @POST
    public Result<DataProviderDTO> create(DataProviderCreateDTO dto);
    
    @GET
    @Path("/{id}")
    public Result<DataProviderDTO> getById(@PathParam("id") Long id);
    
    @GET
    public Result<List<DataProviderDTO>> list();
    
    @PUT
    @Path("/{id}")
    public Result<DataProviderDTO> update(@PathParam("id") Long id, DataProviderUpdateDTO dto);
    
    @DELETE
    @Path("/{id}")
    public Result<Void> delete(@PathParam("id") Long id);
}
```

---

## 八、前端设计

### 8.1 配置界面（支持树形）

```javascript
function loadDataSourceOptions() {
    const type = document.getElementById('dataSourceType').value;
    const container = document.getElementById('dataSourceConfig');
    
    if (type === 'static') {
        container.innerHTML = `
            <div class="form-group">
                <label>数据源名称</label>
                <input type="text" id="dsName" class="form-control">
            </div>
            <div class="form-group">
                <label>数据结构</label>
                <select id="dsStructure" class="form-select" onchange="toggleStaticStructure()">
                    <option value="flat">扁平列表</option>
                    <option value="tree">树形结构</option>
                </select>
            </div>
            <div id="flatConfig" class="form-group">
                <label>选项数据（JSON格式）</label>
                <textarea id="dsFlatData" class="form-control" rows="4">
[{"value":"BJ","label":"北京"},{"value":"SH","label":"上海"}]
                </textarea>
            </div>
            <div id="treeConfig" class="form-group" style="display:none;">
                <label>树形数据（JSON格式）</label>
                <textarea id="dsTreeData" class="form-control" rows="8">
{
  "structure": "tree",
  "children": [
    {"value": "BJ", "label": "北京", "children": [...]}
  ]
}
                </textarea>
            </div>
        `;
    }
}
```

### 8.2 角色过滤视图

```javascript
function showDataSourceForm(component) {
    fetch('/api/v2/ui/data-sources/query')
        .then(response => response.json())
        .then(data => {
            const allProviders = data.data;
            const role = getCurrentRole(); // BUSINESS 或 IT
            const filteredProviders = allProviders.filter(p => p.configSource === role);
            
            // 渲染列表...
        });
}
```

### 8.3 统一渲染方法

```javascript
function renderSelectOptions(component, container) {
    fetch(`/api/v2/ui/data-sources/${providerId}/execute`)
        .then(response => response.json())
        .then(data => {
            if (isTreeData(data.data)) {
                renderTreeSelect(component, data.data, container);
            } else {
                renderFlatSelect(component, data.data, container);
            }
        });
}
```

---

## 九、MapStruct转换器

### 9.1 Entity ↔ Domain转换

```java
/**
 * DataProvider Entity ↔ Domain 转换器
 */
@Mapper(componentModel = "spring")
public interface DataProviderConverter {
    
    /**
     * Entity → Domain
     */
    @Mapping(target = "providerType", expression = "java(ProviderType.valueOf(entity.getProviderType()))")
    @Mapping(target = "dataSourceCategory", expression = "java(DataSourceCategory.valueOf(entity.getDataSourceCategory()))")
    @Mapping(target = "status", expression = "java(ProviderStatus.valueOf(entity.getStatus()))")
    @Mapping(target = "configJson", expression = "java(new ConfigJson(entity.getConfigJson()))")
    DataProvider toDomain(DataProviderEntity entity);
    
    /**
     * Domain → Entity
     */
    @Mapping(target = "providerType", expression = "java(domain.getProviderType().name())")
    @Mapping(target = "dataSourceCategory", expression = "java(domain.getDataSourceCategory().name())")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "configJson", expression = "java(domain.getConfigJson().getValue())")
    DataProviderEntity toEntity(DataProvider domain);
    
    /**
     * List转换
     */
    List<DataProvider> toDomainList(List<DataProviderEntity> entities);
}
```

---

## 十、错误处理设计

### 10.1 异常分类

```java
// 业务配置相关异常
public class DataSourceConfigException extends BizException {
    public static final String INVALID_JSON_FORMAT = "INVALID_JSON_FORMAT";
    public static final String INVALID_TREE_STRUCTURE = "INVALID_TREE_STRUCTURE";
    public static final String DICT_TYPE_NOT_FOUND = "DICT_TYPE_NOT_FOUND";
}

// 数据源执行相关异常
public class DataSourceExecutionException extends BizException {
    public static final String PROVIDER_NOT_FOUND = "PROVIDER_NOT_FOUND";
    public static final String HTTP_REQUEST_FAILED = "HTTP_REQUEST_FAILED";
    public static final String PLATFORM_SERVICE_UNAVAILABLE = "PLATFORM_SERVICE_UNAVAILABLE";
}
```

### 10.2 错误场景处理

| 场景 | 异常 | 处理方式 |
|-----|------|---------|
| 数据源不存在 | ProviderNotFoundException | 前端显示"数据源不存在" |
| JSON格式错误 | InvalidConfigException | 前端显示"配置错误" |
| 树形层级过深 | InvalidConfigException | 限制最大3级 |
| 必填字段缺失 | InvalidConfigException | 提示补充字段 |

---

## 十一、本次迭代范围

### 11.1 实现内容

**后端**：
- DataProvider领域对象（DDD规范）
- DataSourceConfigService（业务配置）
- DataProviderService（IT配置）
- DataSourceQueryFacadeService（统一查询）
- StaticDataProviderExecutor（静态数据源执行）
- 接口层：统一查询、业务配置、IT配置

**前端**：
- 配置界面支持树形编辑
- 角色过滤视图
- 统一渲染方法

### 11.2 预留接口

- **DICT**：预留，后续实现字典服务
- **HTTP**：预留，后续实现HTTP调用
- **PLATFORM_API**：预留，后续实现平台服务调用
- **INTERNAL_QUERY**：预留，后续实现内部查询

---

## 十二、E2E验证设计

详见：`docs/superpowers/e2e/README.md`

### 12.1 测试套件结构

```
docs/superpowers/e2e/
├── README.md
├── scenarios/           # 场景设计文档
├── records/             # 执行记录
├── explains/            # EXPLAIN结果
└── test-data/           # 测试数据
```

### 12.2 主要验证场景

1. **组件库遍历验证**（21个组件全覆盖）
2. **嵌套组合场景验证**（两层嵌套147种组合、三层嵌套典型场景）
3. **JSONB查询特性验证**（布局节点、字段组件、数据源、合同快照）
4. **完整CRUD验证**（模板、版本、布局节点、字段组件、数据源、合同）
5. **规则执行验证**（显隐规则、必填规则、只读规则）
6. **数据绑定验证**（字段路径绑定、数据源绑定）
7. **性能验证**（大数据量、深层嵌套）

---

## 十三、数据库核心原则

### 13.1 明确原则

- **生产环境（核心）**：高斯数据库，JSONB类型，支持JSON查询、索引
- **测试环境（辅助）**：H2数据库，JSON类型，仅用于单元测试

### 13.2 JSONB设计

- configJson字段使用JSONB类型
- 所有JSON解析在Java层完成
- 使用JSONB查询优化性能
- 必要时创建表达式索引

---

## 十四、后续迭代计划

### 14.1 V1.1（字典服务）

- 实现 DictQueryService
- 实现 DictDataProviderExecutor
- 字典表设计和数据初始化

### 14.2 V1.2（外部数据源）

- 实现 HttpDataProviderExecutor
- 实现 PlatformDataProviderExecutor
- 实现 InternalDataProviderExecutor

### 14.3 V1.3（缓存机制）

- Redis缓存集成
- 缓存失效策略
- 缓存监控

---

## 十五、参考资料

- Martin Fowler: DDD Aggregate - https://martinfowler.com/bliki/DDD_Aggregate.html
- Martin Fowler: Value Object - https://martinfowler.com/bliki/ValueObject.html
- 项目需求文档：`req/req.md`
