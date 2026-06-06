---
name: tech-stack
description: 技术栈规范
---

# 技术栈规范

## 核心原则

**技术栈选择**：基于成熟稳定、团队熟悉、生态完善的原则，选择V1.0版本的技术栈。

---

## 后端技术栈

### 核心框架
| 技术 | 版本 | 说明 |
|-----|------|------|
| Java | 21 LTS | 长期支持版本 |
| Spring Boot | 3.5.14 | 最新稳定版本 |
| MyBatis-Plus | 3.5.5 | ORM框架增强版 |

### 工具库
| 技术 | 版本 | 说明 |
|-----|------|------|
| MapStruct | 1.6.3 | 对象转换工具 |
| Lombok | 1.18.36 | 代码简化工具 |
| Hutool | 6.0.0 | Java工具类库 |
| Jackson | 默认 | JSON处理 |
| SLF4J + Logback | 默认 | 日志框架 |

### 测试框架
| 技术 | 版本 | 说明 |
|-----|------|------|
| JUnit 5 | 默认 | 单元测试框架 |
| Mockito | 默认 | Mock框架 |
| Spring Boot Test | 默认 | 集成测试框架 |
| H2 Database | 2.3.232 | 测试数据库 |

---

## 前端技术栈

### 核心选择
| 技术 | 版本 | 说明 |
|-----|------|------|
| HTML/CSS/JS | 原生 | 基础技术 |
| Alpine.js | 3.x（可选） | 轻量级框架 |
| Vue 3（可选） | 3.x | 渐进式框架 |

### UI组件
| 技术 | 版本 | 说明 |
|-----|------|------|
| Bootstrap | 5.3 | UI框架（推荐） |
| Element Plus | 2.x（可选） | Vue组件库 |

### 工具库
| 技术 | 版本 | 说明 |
|-----|------|------|
| Axios | 1.x | HTTP客户端 |
| Lodash | 4.x | JS工具库 |
| Day.js | 1.x | 日期处理 |

---

## 数据库技术栈

### 开发环境
| 技术 | 版本 | 说明 |
|-----|------|------|
| H2 Database | 2.3.232 | 内存数据库（测试） |
| MySQL兼容模式 | - | H2配置 |

### 生产环境
| 技术 | 版本 | 说明 |
|-----|------|------|
| MySQL | 8.0 | 生产数据库 |

---

## 开发工具

### IDE
| 工具 | 说明 |
|-----|------|
| IntelliJ IDEA | Java开发IDE |
| VS Code | 前端开发IDE |

### 构建工具
| 技术 | 版本 | 说明 |
|-----|------|------|
| Maven | 3.9.x | 项目构建 |
| npm | 10.x | 前端包管理 |

### 版本控制
| 工具 | 说明 |
|-----|------|
| Git | 版本控制 |
| GitHub/GitLab | 代码托管 |

---

## 版本选择原则

### 原则1：优先LTS版本
```
Java 21 LTS - 长期支持，稳定性高
Spring Boot 3.x - 最新稳定版，生态完善
```

### 原则2：团队熟悉优先
```
MyBatis-Plus - 团队熟悉，文档完善
MapStruct - 团队熟悉，减少样板代码
Lombok - 团队熟悉，提高开发效率
```

### 原则3：生态完善优先
```
JUnit 5 - 生态完善，主流选择
Mockito - Spring Boot默认集成
H2 - MySQL兼容，测试友好
```

### 原则4：轻量优先（前端）
```
Alpine.js - 轻量级，适合简单交互
Bootstrap - 无需构建，直接使用
原生JS - 零依赖，最大灵活性
```

---

## 技术栈决策依据

### 后端决策
| 选择 | 原因 |
|-----|------|
| Java 21 | LTS版本，长期支持，稳定性高 |
| Spring Boot 3.5.14 | 最新稳定版，虚拟线程支持，性能提升 |
| MyBatis-Plus 3.5.5 | 国内主流ORM，插件丰富，文档完善 |
| MapStruct | 类型安全转换，编译期检查，零反射 |
| Lombok | 减少样板代码，提高开发效率 |
| Hutool | 工具类丰富，减少重复实现 |

### 前端决策
| 选择 | 原因 |
|-----|------|
| 原生HTML/CSS/JS | 零学习成本，最大灵活性 |
| Alpine.js（可选） | 轻量级，适合简单交互，学习成本低 |
| Bootstrap | 无需构建，直接使用，响应式支持 |
| Axios | 主流HTTP客户端，拦截器支持 |

### 测试决策
| 选择 | 原因 |
|-----|------|
| JUnit 5 | 主流测试框架，Spring Boot默认 |
| Mockito | Spring Boot默认集成，主流Mock框架 |
| H2 | 内存数据库，MySQL兼容，测试友好 |

---

## 版本兼容性

### Spring Boot兼容性
```
Spring Boot 3.5.14 需要：
- Java 17+（推荐Java 21）
- MyBatis-Plus 3.5.x
- Lombok 1.18.x
- MapStruct 1.5+
```

### MyBatis-Plus兼容性
```
MyBatis-Plus 3.5.5 支持：
- Spring Boot 3.x
- Java 17+
- MySQL 8.0
- H2 2.x
```

---

## 详细规则引用

- 依赖管理规范：查看 `24-dependency-management.md`
- MapStruct使用指南：查看 `25-mapstruct-guide.md`
- Lombok使用指南：查看 `26-lombok-guide.md`
- 前端技术选择：查看 `27-frontend-tech.md`