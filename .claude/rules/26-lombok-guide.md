---
name: lombok-guide
description: Lombok使用指南
---

# Lombok使用指南

## 核心原则

**Lombok**：通过注解简化Java代码，减少样板代码，提高开发效率。

---

## Lombok特点

### 优势
- 减少样板代码（Getter/Setter/构造器等）
- 代码更简洁，可读性更高
- IDE插件支持，开发体验好

### 注意事项
- 编译期处理，IDE需要安装插件
- 与MapStruct需要配置绑定
- 部分注解慎用（如@Data）

---

## Maven配置

### pom.xml配置
```xml
<properties>
    <lombok.version>1.18.36</lombok.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
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
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
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

## 常用注解

### @Getter / @Setter
```java
@Getter
@Setter
public class Template {
    private Long id;
    private String templateCode;
    private String templateName;
}

// 生成的代码：
// public Long getId() { return this.id; }
// public void setId(Long id) { this.id = id; }
// ... 其他Getter/Setter
```

### @ToString
```java
@Getter
@Setter
@ToString
public class Template {
    private Long id;
    private String templateCode;
    private String templateName;
}

// 生成的代码：
// public String toString() {
//     return "Template(id=" + this.id + ", templateCode=" + this.templateCode + ", templateName=" + this.templateName + ")";
// }

// 排除字段：
@ToString(exclude = {"createTime", "updateTime"})
public class Template { ... }
```

### @EqualsAndHashCode
```java
@Getter
@Setter
@EqualsAndHashCode
public class Template {
    private Long id;
    private String templateCode;
}

// 生成的代码：
// public boolean equals(Object o) { ... }
// public int hashCode() { ... }

// 指定包含字段：
@EqualsAndHashCode(of = {"id"})
public class Template { ... }
```

### @NoArgsConstructor / @AllArgsConstructor / @RequiredArgsConstructor
```java
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Template {
    private Long id;
    private final String templateCode;  // RequiredArgsConstructor包含final字段
    private String templateName;
}

// NoArgsConstructor：无参构造器
// AllArgsConstructor：全参构造器
// RequiredArgsConstructor：final字段构造器
```

### @Data
```java
@Data
public class Template {
    private Long id;
    private String templateCode;
}

// @Data包含：
// @Getter（所有字段）
// @Setter（所有非final字段）
// @ToString
// @EqualsAndHashCode
// @RequiredArgsConstructor

// 注意：慎用@Data，建议分开使用各注解
```

### @Builder
```java
@Getter
@Setter
@Builder
public class Template {
    private Long id;
    private String templateCode;
    private String templateName;
}

// 使用方式：
Template template = Template.builder()
    .templateCode("TPL001")
    .templateName("测试模板")
    .build();
```

### @Slf4j
```java
@Slf4j
@Service
public class TemplateServiceImpl {
    
    public void createTemplate() {
        log.info("创建模板");
        log.debug("模板详情：{}", template);
    }
}

// 生成的代码：
// private static final Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);
```

---

## 推荐使用方式

### Entity类
```java
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateEntity {
    private Long id;
    private String templateCode;
    private String templateName;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### DTO类
```java
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateDTO {
    private Long id;
    private String templateCode;
    private String templateName;
    private String status;
}
```

### 领域对象
```java
@Getter
@Setter
@ToString(exclude = {"createTime", "updateTime"})
@NoArgsConstructor
@AllArgsConstructor
public class Template {
    private Long id;
    private String templateCode;
    private String templateName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### Service类
```java
@Slf4j
@Service
public class TemplateServiceImpl implements TemplateService {
    
    private final TemplateRepository templateRepository;
    
    public TemplateServiceImpl(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }
    
    public TemplateDTO create(CreateTemplateRequest request) {
        log.info("创建模板：{}", request.getTemplateCode());
        // ...
    }
}
```

---

## 不推荐使用方式

### 不推荐：@Data（过度使用）
```java
// ❌ 不推荐
@Data
public class Template {
    private Long id;
    private String templateCode;
}

// 问题：
// 1. @EqualsAndHashCode可能包含所有字段，不符合业务语义
// 2. @ToString可能暴露敏感信息
// 3. 不够灵活，无法细粒度控制

// ✅ 推荐：分开使用
@Getter
@Setter
@ToString(exclude = {"createTime"})
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
public class Template {
    private Long id;
    private String templateCode;
    private LocalDateTime createTime;
}
```

### 不推荐：@Value（不可变类）
```java
// ❌ 不推荐（不适用于Entity/DTO）
@Value
public class Template {
    Long id;
    String templateCode;
}

// 问题：
// 1. 所有字段变为final，无法修改
// 2. 不适用于需要修改的场景

// ✅ 推荐：使用@Getter + @Setter
@Getter
@Setter
public class TemplateDTO {
    private Long id;
    private String templateCode;
}
```

---

## 与MapStruct配合

### 配置绑定
```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok-mapstruct-binding</artifactId>
        <version>0.2.0</version>
    </path>
</annotationProcessorPaths>
```

### 使用示例
```java
@Getter
@Setter
@Builder
public class Template {
    private Long id;
    private String templateCode;
}

@Getter
@Setter
@Builder
public class TemplateDTO {
    private Long id;
    private String templateCode;
}

@Mapper(componentModel = "spring")
public interface TemplateConverter {
    
    TemplateDTO toDTO(Template template);
    
    // Lombok的Builder会被MapStruct识别并使用
}
```

---

## 验证清单

### Lombok配置必须验证
- [ ] Maven配置正确
- [ ] IDE插件安装
- [ ] 注解使用正确
- [ ] 与MapStruct配合正确
- [ ] 编译生成代码正确

---

## 详细规则引用

- 对象转换规则：查看 `06-conversion.md`
- MapStruct使用指南：查看 `25-mapstruct-guide.md`
- 依赖管理规范：查看 `24-dependency-management.md`