---
name: mapstruct-guide
description: MapStruct使用指南
---

# MapStruct使用指南

## 核心原则

**MapStruct**：类型安全的对象转换工具，编译期生成转换代码，零反射，高性能。

---

## MapStruct特点

### 优势
- 编译期生成代码，零反射
- 类型安全，编译期检查错误
- 高性能，无运行时开销
- 代码清晰，易于调试

### 与其他方案对比
| 方案 | 性能 | 类型安全 | 调试性 | 推荐度 |
|-----|------|---------|--------|-------|
| MapStruct | 高 | 高 | 高 | ✅ 推荐 |
| 手写转换 | 高 | 低 | 高 | 可选 |
| BeanUtils（反射） | 低 | 低 | 低 | ❌ 不推荐 |

---

## 基本配置

### Maven配置
```xml
<properties>
    <mapstruct.version>1.6.3</mapstruct.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>
    
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<!-- Compiler Plugin配置 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
            </path>
            <!-- Lombok绑定 -->
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>0.2.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

---

## 基本使用

### 定义转换器
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    // 单对象转换
    TemplateDTO toDTO(Template template);
    
    Template toDomain(TemplateDTO dto);
    
    Template toDomain(CreateTemplateRequest request);
    
    // List转换
    List<TemplateDTO> toDTOList(List<Template> templates);
}
```

### 使用转换器
```java
@Service
public class TemplateServiceImpl implements TemplateService {
    
    private final TemplateConverter templateConverter;
    
    public TemplateServiceImpl(TemplateConverter templateConverter) {
        this.templateConverter = templateConverter;
    }
    
    public TemplateDTO create(CreateTemplateRequest request) {
        Template template = templateConverter.toDomain(request);
        // ...
        return templateConverter.toDTO(template);
    }
}
```

---

## 字段映射

### 同名字段自动映射
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    // 同名字段自动映射
    TemplateDTO toDTO(Template template);
}

// 自动映射规则：
// template.id → dto.id
// template.templateCode → dto.templateCode
// template.templateName → dto.templateName
```

### 不同名字字段映射
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    @Mapping(source = "code", target = "templateCode")
    @Mapping(source = "name", target = "templateName")
    TemplateDTO toDTO(Template template);
}
```

### 忽略字段
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    Template toDomain(CreateTemplateRequest request);
}
```

### 常量映射
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "version", constant = "1")
    Template toDomain(CreateTemplateRequest request);
}
```

### 表达式映射
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    @Mapping(target = "createTime", expression = "java(LocalDateTime.now())")
    Template toDomain(CreateTemplateRequest request);
}
```

---

## 复杂映射

### 嵌套对象映射
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    // 嵌套对象自动映射
    TemplateDTO toDTO(Template template);
    
    // 手动指定嵌套映射
    @Mapping(source = "config.layoutNodes", target = "layoutNodes")
    TemplateDTO toDTO(Template template);
}
```

### 自定义映射方法
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    TemplateDTO toDTO(Template template);
    
    // 自定义字段转换方法
    default String mapStatus(Status status) {
        return status != null ? status.getCode() : null;
    }
    
    default Status mapStatus(String status) {
        return status != null ? Status.fromCode(status) : null;
    }
}
```

### 使用外部转换器
```java
@Mapper(componentModel = "spring", uses = {DateConverter.class, StatusConverter.class})
public interface TemplateConverter {
    
    TemplateDTO toDTO(Template template);
}

@Component
public class DateConverter {
    
    public String map(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
    
    public LocalDateTime map(String dateTime) {
        return dateTime != null ? LocalDateTime.parse(dateTime) : null;
    }
}
```

---

## Collection映射

### List映射
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    List<TemplateDTO> toDTOList(List<Template> templates);
    
    // 自动使用单对象转换方法
    TemplateDTO toDTO(Template template);
}
```

### Map映射
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    @MapMapping(valueQualifiedByName = "toDTO")
    Map<String, TemplateDTO> toDTOMap(Map<String, Template> templateMap);
}
```

---

## 枚举映射

### 枚举值映射
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    @ValueMapping(source = "DRAFT", target = "DRAFT")
    @ValueMapping(source = "PUBLISHED", target = "PUBLISHED")
    @ValueMapping(source = "ARCHIVED", target = MappingConstants.NULL)
    TemplateStatusDTO map(TemplateStatus status);
}
```

---

## 多源映射

### 多对象合并
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    @Mapping(source = "template.templateCode", target = "code")
    @Mapping(source = "version.versionNumber", target = "version")
    @Mapping(source = "config.configJson", target = "config")
    TemplateVersionDTO toDTO(Template template, TemplateVersion version, TemplateConfig config);
}
```

---

## 最佳实践

### 实践1：转换器分层
```
应用层转换器：
- DTO ↔ Request/Response
- DTO ↔ Domain

基础设施层转换器：
- Domain ↔ Entity
```

### 实践2：命名约定
```java
// 转换方法命名约定
TemplateDTO toDTO(Template template);     // Domain → DTO
Template toDomain(TemplateDTO dto);       // DTO → Domain
Template toDomain(CreateTemplateRequest request); // Request → Domain

// List转换命名约定
List<TemplateDTO> toDTOList(List<Template> templates);
```

### 实践3：使用INSTANCE
```java
@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    // 非Spring环境可用
    TemplateConverter INSTANCE = Mappers.getMapper(TemplateConverter.class);
    
    TemplateDTO toDTO(Template template);
}

// 使用方式
TemplateDTO dto = TemplateConverter.INSTANCE.toDTO(template);
```

### 实践4：避免循环依赖
```java
// 如果AConverter需要BConverter，使用uses属性
@Mapper(componentModel = "spring", uses = {VersionConverter.class})
public interface TemplateConverter {
    TemplateDTO toDTO(Template template);
}
```

---

## 验证清单

### MapStruct配置必须验证
- [ ] Maven配置正确
- [ ] 编译生成实现类
- [ ] 字段映射正确
- [ ] 类型转换正确
- [ ] List映射正确
- [ ] 自定义方法正确

---

## 详细规则引用

- 对象转换规则：查看 `06-conversion.md`
- Lombok使用指南：查看 `26-lombok-guide.md`
- 依赖管理规范：查看 `24-dependency-management.md`