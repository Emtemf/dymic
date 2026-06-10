# 合同模板动态渲染系统

## 项目定位

### 系统是什么
**合同模板动态渲染系统**：配置驱动、动态渲染、数据比对的一体化平台

**核心价值**：
1. 业务人员配置模板结构（表单填写，不是拖拽）
2. 系统动态渲染合同录入界面（根据配置）
3. IT配置数据源实现字段自动填充
4. 外部数据自动映射并对比差异

### 系统不是什么
- ❌ 不是低代码平台（不提供拖拽设计器给用户）
- ❌ 不是审批系统（审批在其他系统）
- ❌ 不是权限系统（权限在其他系统）

### 前端界面定位
**可视化配置界面**：仅用于开发验证，不是产品功能
- 用途：验证后端API功能是否正确
- 不交付：不作为业务人员的配置工具


## 默认运行约定（当前）

### 默认数据库
- 默认运行数据库：**openGauss**
- 单元测试数据库：**H2**
- `application.yml`：默认连接 openGauss
- `application-test.yml`：仅测试使用 H2

### openGauss Docker 启动方式
当前项目使用自己的 Docker 容器启动高斯。

```bash
bash scripts/start-opengauss.sh
bash scripts/init-opengauss.sh
mvn spring-boot:run
```

当前约定：
- 容器名：`opengauss-contract`
- 镜像：`enmotech/opengauss:5.0.0`
- 数据目录：`/home/wula/.local/opengauss-data`
- 端口：`5432`
- 用户：`gaussdb`
- 密码：`OpenGauss@123`
- 默认应用端口：`8888`

默认 JDBC：
```text
jdbc:postgresql://localhost:5432/contract_template?options=-c%20TimeZone=UTC
```

### 前端界面与真实入口
当前前端界面：
- 首页：`/index.html`
- 设计器：`/config/template-designer.html`
- 数据源配置：`/config/data-source.html`
- 动态展示：`/display/dynamic-display.html`
- 简单验证界面：`/simple.html`
- 复杂验证界面：`/complex.html`

真实验证路径必须从首页开始：
1. 打开 `/index.html`
2. 创建模板
3. 创建版本
4. 发布版本
5. 进入设计器或展示页继续验证

说明：
- `template-designer.html` 不是完整业务主入口
- 可视化设计器仅用于开发验证，不是业务交付界面

---


### ✅ V1.0 必须包含

**核心闭环**：
```
优先级1（最小闭环）：
1. 模板管理：创建模板、发布版本
2. 合同录入：新增合同、保存合同、回显合同
3. 数据源配置：5种数据源类型
4. 配置管理：布局、字段、组件、查询、明细表、规则
5. E2E验证工具：可视化配置界面（验证用）

优先级2（增强功能）：
1. 查询执行：执行查询、回填字段
2. 明细表：增行、删行、编辑行
3. 规则执行：显隐、必填、只读规则

优先级3（高级功能）：
1. 外部数据映射：外部→统一JSON
2. 外部数据对比：逐字段差异
3. 对比结果应用：自动/手动确认
```

### ❌ V1.0 不包含

**完全不在范围内**：
- ❌ 工作流审批（审批系统负责）
- ❌ 多人协作草稿（协作系统负责）
- ❌ 多租户隔离（平台层负责）
- ❌ 权限管理（权限系统负责）
- ❌ 用户管理（用户中心负责）

**推迟到V1.1/V1.2**：
- ❌ Redis缓存
- ❌ MQ消息队列
- ❌ 真实物理分表
- ❌ 多实例部署

---

## 开发流程

### 开始新功能的标准流程

**Step 1：理解需求**
```bash
# 读取需求文档
cat req/req.md
cat req/supplement.md

# 确认边界：是否在V1.0范围内？
```

**Step 2：设计架构**
```bash
# 确定分层：
# - 接口层：Controller
# - 应用层：Service
# - 领域层：DomainService + Repository接口
# - 基础设施层：RepositoryImpl + Mapper + Entity
```

**Step 3：编写测试（TDD）**
```bash
# 先写测试，再写实现
# Repository层：H2单元测试
# Service层：Mock Repository
# Controller层：MockMvc测试
```

**Step 4：实现代码**
```bash
# 按照简洁架构四层实现
# 遵循命名规范（见下文）
```

**Step 5：验证功能**
```bash
# 运行单元测试
mvn test

# 使用chrome-devtools验证前端
# （详见验证策略部分）
```

---

## 架构规则

### 包结构

```
com.contract.template
├── controller              # 接口层
│   ├── TemplateController.java
│   ├── ContractController.java
│   └── DataProviderController.java
│
├── application             # 应用层
│   ├── service            # 应用服务（业务编排）
│   │   ├── TemplateService.java
│   │   ├── ContractService.java
│   │   ├── RenderService.java
│   │   └── QueryExecutionService.java
│   ├── dto                 # DTO（入参/出参）
│   ├── condition           # 查询条件
│   └─ convert              # MapStruct转换器
│
├── domain                  # 领域层
│   ├── model               # 领域对象（不带Entity后缀）
│   │   ├── Template.java
│   │   ├── Contract.java
│   ├── service             # 领域服务（业务规则）
│   ├── repository          # Repository接口
│   │   ├── TemplateRepository.java
│   │   ├── ContractRepository.java
│   └─ gateway              # Gateway接口（外部端口）
│       ├── DataProviderGateway.java
│
├── infrastructure          # 基础设施层
│   ├── repository          # Repository实现
│   │   ├── TemplateRepositoryImpl.java
│   │   ├── ContractRepositoryImpl.java
│   ├── mapper              # MyBatis Mapper
│   │   ├── TemplateMapper.java
│   │   ├── ContractMapper.java
│   ├── entity              # Entity（带Entity后缀）
│   │   ├── TemplateEntity.java
│   │   ├── ContractEntity.java
│   ├── gateway             # Gateway实现
│   └─ convert              # MapStruct转换器
│
└── common                  # 公共模块
    ├── exception
    ├── result
    ├── enums
    └─ util
```

### 四层职责

**接口层（Controller）**：
- HTTP入口，参数校验
- 调用应用服务
- 返回统一响应 `Result<T>`
- **不允许**：包含业务逻辑

**应用层（Service）**：
- 业务编排，调用多个领域服务
- DTO ↔ Model 转换
- 定义事务边界 `@Transactional`
- **不允许**：直接访问数据库

**领域层（DomainService + Repository接口）**：
- 核心业务规则
- Repository接口定义
- Gateway接口定义
- **不允许**：依赖任何基础设施

**基础设施层（RepositoryImpl + Mapper + Entity）**：
- 实现领域层接口
- Entity ↔ Model 转换
- 外部系统调用
- **包含**：Mapper、Entity、RepositoryImpl、GatewayImpl

---

## 命名规范（Google风格）

### 包命名
- ✅ 全小写，点分隔：`com.contract.template.controller`
- ❌ 不使用驼峰：`com.contract.template.ControllerPackage`（错误）

### 类命名
- ✅ 大驼峰：`TemplateController`, `ContractService`
- ❌ 不使用下划线：`Template_Controller`（错误）

**特殊后缀规则**：
- **Entity类**：必须带 `Entity` 后缀 → `TemplateEntity.java`
- **领域对象**：不带后缀 → `Template.java`
- **Repository接口**：不带 `Impl` → `TemplateRepository.java`
- **Repository实现**：带 `Impl` → `TemplateRepositoryImpl.java`
- **DTO**：统一带 `DTO` 后缀 → `TemplateDTO.java`

### 方法命名
- ✅ 小驼峰，动词开头：`findById`, `createTemplate`, `saveContract`
- ❌ 不使用大写开头：`FindById`（错误）

**入参规则**：
- ≤3字段：直接拆开传 → `getTemplate(Long templateId)`
- >3字段：用DTO包装 → `createTemplate(CreateTemplateDTO dto)`

### 数据库字段命名
- ✅ 下划线命名：`created_by`, `created_at`, `updated_by`, `updated_at`
- ✅ MyBatis自动映射到驼峰：`createdBy`, `createdAt`

---

## 验证策略

### Repository层验证（H2内存数据库）

**测试内容**：
- CRUD操作正确性
- 查询条件准确性
- JSONB字段存储
- 索引有效性

**测试代码示例**：
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TemplateRepositoryTest {
    @Autowired
    private TemplateRepository repository;
    
    @Test
    void testSaveAndFind() {
        Template template = Template.builder()
            .templateCode("TEST")
            .templateName("测试模板")
            .build();
        
        repository.save(template);
        Template found = repository.findById(template.getId());
        assertEquals("TEST", found.getTemplateCode());
    }
}
```

**运行命令**：
```bash
mvn test -Dtest=*RepositoryTest
```

### Service层验证（Mock Repository）

**测试内容**：
- 业务流程正确性
- 业务规则生效性
- 异常处理正确性

**测试代码示例**：
```java
@SpringBootTest
class TemplateServiceTest {
    @Autowired
    private TemplateService service;
    
    @MockBean
    private TemplateRepository repository;
    
    @Test
    void testCreateTemplate() {
        when(repository.existsByTemplateCode("TEST")).thenReturn(false);
        
        TemplateDTO dto = new TemplateDTO();
        dto.setTemplateCode("TEST");
        
        Long id = service.createTemplate(dto);
        assertNotNull(id);
    }
}
```

**运行命令**：
```bash
mvn test -Dtest=*ServiceTest
```

### Controller层验证（MockMvc）

**测试内容**：
- HTTP方法正确性
- 参数绑定正确性
- 响应格式正确性

**测试代码示例**：
```java
@SpringBootTest
@AutoConfigureMockMvc
class TemplateControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCreateTemplate() throws Exception {
        String body = """
            {
              "templateCode": "TEST",
              "templateName": "测试模板"
            }
            """;
        
        mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.templateCode").value("TEST"));
    }
}
```

**运行命令**：
```bash
mvn test -Dtest=*ControllerTest
```

### 前端界面验证（chrome-devtools）

**验证内容**：
- 动态渲染正确性
- 规则执行正确性
- 数据绑定正确性

**验证步骤**：
```bash
# 1. 启动Spring Boot应用
mvn spring-boot:run

# 2. 使用chrome-devtools打开配置界面
mcp__chrome-devtools.navigate_page("http://localhost:8888/config/template-designer.html")

# 3. 自动拖拽组件验证
mcp__chrome-devtools.drag(...)  # 拖拽组件

# 4. 验证左侧预览渲染
mcp__chrome-devtools.take_screenshot()  # 截图验证

# 5. 保存配置并验证数据库
# 检查数据库是否有新的配置记录
```

---

## 技术栈清单

### 后端技术栈
```
Java: 21
Spring Boot: 3.5.14
MyBatis-Plus: 3.5.5
MapStruct: 1.5.5.Final（对象转换）
Lombok: 1.18.30（减少样板代码）
H2: 2.2.224（测试数据库）
openGauss: （生产数据库）
```

### 前端技术栈
```
HTML: 原生 HTML5
CSS: 原生 CSS3 + frontend-design样式库
JavaScript: 原生 ES6+
HTTP: Fetch API
构建: 无构建工具（直接静态文件）
```

### 开发工具
```
Maven: 3.9.6
IntelliJ IDEA: （推荐IDE）
chrome-devtools: MCP工具（前端验证）
```

---

## 数据库核心原则

### 明确原则

**生产环境（核心）**：
- 高斯数据库（openGauss）
- JSONB类型：原生JSON存储，支持JSON查询、索引
- 查询性能：JSONB字段支持GIN索引，查询性能优秀

**测试环境（辅助）**：
- H2数据库（内存数据库）
- JSON类型：H2兼容模式，仅用于单元测试
- 不追求生产级性能，仅验证业务逻辑

**设计原则**：
1. 数据库设计以高斯为核心，H2为辅助测试
2. JSONB字段用于存储配置JSON，支持复杂查询
3. 不使用TEXT存储JSON，必须使用JSONB/JSON类型
4. JSONB查询需要EXPLAIN验证性能

---

## 代码规范

### 使用Lombok
```java
// ✅ 允许使用
@Data
@Builder
@RequiredArgsConstructor

// 示例
@Data
@Builder
public class TemplateDTO {
    private Long id;
    private String templateCode;
    private String templateName;
}
```

### 使用MapStruct
```java
// ✅ 应用层转换：Model → DTO
@Mapper
public interface TemplateConverter {
    TemplateDTO toDTO(Template template);
    List<TemplateDTO> toDTOList(List<Template> templates);
}

// ✅ 基础设施层转换：Entity ↔ Model
@Mapper
public interface EntityConverter {
    Template toModel(TemplateEntity entity);
    TemplateEntity toEntity(Template template);
}
```

### 统一响应格式
```java
// ✅ 所有Controller返回Result<T>
@GetMapping("/{id}")
public Result<TemplateDTO> getTemplate(@PathVariable Long id) {
    TemplateDTO dto = service.findById(id);
    return Result.ok(dto);
}

// Result类定义
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
}
```

### 异常处理
```java
// ✅ 业务异常使用BizException
public class BizException extends RuntimeException {
    private final String code;
    
    public BizException(String message) {
        super(message);
        this.code = "BIZ_ERROR";
    }
}

// ✅ 全局异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }
}
```

---

## 快速参考

### 创建新功能的模板
```bash
# 1. 创建Controller
controller/XxxController.java

# 2. 创建Service
application/service/XxxService.java

# 3. 创建Domain对象
domain/model/Xxx.java

# 4. 创建Repository接口
domain/repository/XxxRepository.java

# 5. 创建Entity
infrastructure/entity/XxxEntity.java

# 6. 创建Mapper
infrastructure/mapper/XxxMapper.java

# 7. 创建RepositoryImpl
infrastructure/repository/XxxRepositoryImpl.java

# 8. 创建测试
test/XxxRepositoryTest.java
test/XxxServiceTest.java
test/XxxControllerTest.java
```

### 常用Git命令
```bash
# 提交代码
git add .
git commit -m "feat: add template management feature"

# 查看提交历史
git log --oneline -10

# 查看当前状态
git status
```

---

## 详细规则文件（如需要）

查看 `.claude/rules/` 目录：
- `architecture-details.md` - 架构详细说明
- `testing-guide.md` - 验证详细指南
- `tech-stack-details.md` - 技术栈详细配置

**渐进式披露**：大多数规则已在本文档中，只有真正需要详细说明的才放在规则文件。