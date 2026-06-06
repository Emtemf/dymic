---
name: dependency-inversion
description: 依赖倒置规则
---

# 依赖倒置规则

## 核心原则

**依赖倒置原则（DIP）**：高层模块不应该依赖低层模块，两者都应该依赖其抽象。

```
Controller → Service → DomainService → Repository接口
                                            ↓
                                      RepositoryImpl
                                            ↓
                                          Mapper
                                            ↓
                                        Database
```

---

## 接口定义规则

### 规则1：接口定义位置
- ✅ Repository接口定义在 `domain.repository` 包
- ✅ Gateway接口定义在 `domain.gateway` 包
- ❌ 接口不应定义在 `infrastructure` 包

### 规则2：接口命名规则
- Repository接口：`{Entity}Repository`
- Gateway接口：`{ExternalSystem}Gateway`
- 示例：
  - `TemplateRepository` (领域层接口)
  - `TemplateRepositoryImpl` (基础设施层实现)

---

## 实现类规则

### 规则3：实现类位置
- ✅ Repository实现类放在 `infrastructure.repository` 包
- ✅ Gateway实现类放在 `infrastructure.gateway` 包
- ❌ 实现类不应放在 `domain` 包

### 规则4：实现类命名规则
- Repository实现：`{Entity}RepositoryImpl`
- Gateway实现：`{ExternalSystem}GatewayImpl`
- 使用 `@Repository` 注解

---

## 依赖注入规则

### 规则5：构造器注入优先
```java
// 推荐：构造器注入
@Service
public class TemplateServiceImpl implements TemplateService {
    private final TemplateRepository templateRepository;
    
    public TemplateServiceImpl(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }
}

// 不推荐：字段注入
@Service
public class TemplateServiceImpl implements TemplateService {
    @Autowired
    private TemplateRepository templateRepository;
}
```

### 规则6：接口类型声明
```java
// 推荐：使用接口类型
private final TemplateRepository templateRepository;

// 不推荐：使用实现类类型
private final TemplateRepositoryImpl templateRepository;
```

---

## 包依赖关系

### 允许的依赖方向
```
Controller → Service
Service → DomainService
Service → Repository接口
DomainService → Repository接口
RepositoryImpl → Repository接口
RepositoryImpl → Mapper
RepositoryImpl → Entity
```

### 禁止的依赖方向
```
❌ Domain → Infrastructure
❌ Domain → Mapper
❌ Domain → Entity
❌ Repository接口 → Mapper
```

---

## 代码示例

### 正确的依赖关系
```java
// 领域层：接口定义
package com.contract.template.domain.repository;

public interface TemplateRepository {
    Template save(Template template);
}

// 基础设施层：接口实现
package com.contract.template.infrastructure.repository;

@Repository
public class TemplateRepositoryImpl implements TemplateRepository {
    private final TemplateMapper templateMapper;
    
    public TemplateRepositoryImpl(TemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }
}

// 应用层：依赖接口
package com.contract.template.application.service;

@Service
public class TemplateServiceImpl implements TemplateService {
    private final TemplateRepository templateRepository; // 依赖接口
    
    public TemplateServiceImpl(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }
}
```

---

## 详细规则引用

- 架构规则：查看 `02-architecture.md`
- 各层职责规则：查看 `04-layer-responsibility.md`
- 包结构规则：查看 `03-package-structure.md`