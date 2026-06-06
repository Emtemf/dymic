---
name: dependency-management
description: 依赖管理规范
---

# 依赖管理规范

## 核心原则

**依赖管理**：统一管理项目依赖版本，确保版本兼容性和一致性。

---

## Maven依赖管理

### pom.xml结构
```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.contract</groupId>
    <artifactId>template</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.14</version>
    </parent>
    
    <properties>
        <java.version>21</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <mapstruct.version>1.6.3</mapstruct.version>
        <lombok.version>1.18.36</lombok.version>
        <hutool.version>6.0.0</hutool.version>
        <h2.version>2.3.232</h2.version>
    </properties>
    
    <dependencies>
        <!-- 核心依赖 -->
    </dependencies>
    
    <dependencyManagement>
        <!-- 依赖版本管理 -->
    </dependencyManagement>
    
    <build>
        <plugins>
            <!-- 构建插件 -->
        </plugins>
    </build>
</project>
```

---

## 核心依赖配置

### Spring Boot核心
```xml
<!-- Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Boot Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Spring Boot Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

### MyBatis-Plus
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>${mybatis-plus.version}</version>
</dependency>
```

### MapStruct
```xml
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
```

### Lombok
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>${lombok.version}</version>
    <scope>provided</scope>
</dependency>
```

### Hutool
```xml
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>${hutool.version}</version>
</dependency>
```

### H2 Database（测试）
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>${h2.version}</version>
    <scope>test</scope>
</dependency>
```

---

## 构建插件配置

### Maven Compiler Plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version>
    <configuration>
        <source>${java.version}</source>
        <target>${java.version}</target>
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
    </configuration>
</plugin>
```

### Spring Boot Maven Plugin
```xml
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
```

---

## 依赖范围规范

### scope分类
| scope | 说明 | 使用场景 |
|-------|------|---------|
| compile | 编译和运行时都需要 | 核心依赖 |
| provided | 编译时需要，运行时不需要 | Lombok、Servlet API |
| runtime | 运行时需要，编译时不需要 | JDBC驱动 |
| test | 测试时需要 | 测试框架、H2数据库 |
| system | 系统路径依赖（不推荐） | 避免使用 |

### 示例
```xml
<!-- compile - 核心依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- provided - 编译时需要 -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>

<!-- test - 测试时需要 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 版本管理最佳实践

### 实践1：使用properties统一版本
```xml
<properties>
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
</properties>

<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>${mybatis-plus.version}</version>
</dependency>
```

### 实践2：使用dependencyManagement集中管理
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-bom</artifactId>
            <version>${mybatis-plus.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 实践3：继承Spring Boot Parent
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.14</version>
</parent>
```

---

## 依赖冲突处理

### 冲突检测
```bash
# 查看依赖树
mvn dependency:tree

# 查看冲突依赖
mvn dependency:tree -Dverbose
```

### 冲突解决
```xml
<!-- 排除冲突依赖 -->
<dependency>
    <groupId>com.some.library</groupId>
    <artifactId>some-library</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

---

## 版本升级原则

### 原则1：小版本升级谨慎
```
升级前：
1. 查看CHANGELOG
2. 检查兼容性
3. 测试验证
4. 分支提交
```

### 原则2：大版本升级慎重
```
升级前：
1. 详细阅读迁移文档
2. 创建迁移分支
3. 全面测试验证
4. 逐步迁移
```

### 原则3：安全补丁及时升级
```
安全补丁：
1. 立即升级
2. 测试验证
3. 紧急发布
```

---

## 验证清单

### 依赖管理必须验证
- [ ] 版本统一管理
- [ ] 依赖范围正确
- [ ] 无版本冲突
- [ ] 无冗余依赖
- [ ] 插件配置正确
- [ ] 构建成功

---

## 详细规则引用

- 技术栈规范：查看 `23-tech-stack.md`
- MapStruct使用指南：查看 `25-mapstruct-guide.md`
- Lombok使用指南：查看 `26-lombok-guide.md`