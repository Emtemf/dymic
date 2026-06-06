---
name: layer-responsibility
description: 各层职责规则
---

# 各层职责规则

## Controller层职责

### 允许的职责
- HTTP请求入口
- 参数校验（使用@Valid注解）
- 调用应用服务
- 返回统一响应格式
- 异常捕获和转换

### 不允许的职责
- ❌ 包含业务逻辑
- ❌ 直接访问数据库
- ❌ 调用Repository
- ❌ 数据转换（应由Service处理）

### 示例
```java
@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    private final TemplateService templateService;
    
    @PostMapping
    public Result<TemplateDTO> create(@Valid @RequestBody CreateTemplateRequest request) {
        return Result.success(templateService.create(request));
    }
}
```

---

## Service层职责

### 允许的职责
- 业务编排（调用多个领域服务）
- DTO与领域对象转换
- 事务边界定义（@Transactional）
- 参数组装和校验
- 跨聚合的业务协调

### 不允许的职责
- ❌ 直接访问数据库
- ❌ SQL拼接
- ❌ 调用Mapper

### 示例
```java
@Service
public class TemplateServiceImpl implements TemplateService {
    private final TemplateRepository templateRepository;
    private final VersionRepository versionRepository;
    
    @Transactional
    public TemplateDTO create(CreateTemplateRequest request) {
        Template template = TemplateConverter.toDomain(request);
        template = templateRepository.save(template);
        return TemplateConverter.toDTO(template);
    }
}
```

---

## DomainService层职责

### 允许的职责
- 核心业务规则实现
- 领域对象验证
- 业务逻辑计算
- Repository接口定义
- Gateway接口定义

### 不允许的职责
- ❌ 依赖任何基础设施
- ❌ 使用@Repository、@Mapper等注解
- ❌ 直接访问数据库

### 示例
```java
public interface TemplateRepository {
    Template save(Template template);
    Optional<Template> findById(Long id);
    List<Template> findAll();
}
```

---

## Infrastructure层职责

### 允许的职责
- Repository接口实现
- Entity与领域对象转换
- 数据库访问（Mapper调用）
- 外部系统调用（Gateway实现）
- 技术细节处理

### 必须包含的子包
- repository: Repository接口实现
- mapper: MyBatis Mapper接口
- entity: 数据库实体类
- gateway: Gateway接口实现
- convert: Entity与领域对象转换器

### 示例
```java
@Repository
public class TemplateRepositoryImpl implements TemplateRepository {
    private final TemplateMapper templateMapper;
    
    @Override
    public Template save(Template template) {
        TemplateEntity entity = TemplateConverter.toEntity(template);
        templateMapper.insert(entity);
        return TemplateConverter.toDomain(entity);
    }
}
```

---

## 详细规则引用

- 架构规则：查看 `02-architecture.md`
- 依赖倒置规则：查看 `05-dependency-inversion.md`
- 对象转换规则：查看 `06-conversion.md`