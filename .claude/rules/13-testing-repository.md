---
name: testing-repository
description: Repository层验证规则
---

# Repository层验证规则

## 核心原则

**Repository层验证**：使用H2内存数据库进行集成测试，验证SQL和数据访问逻辑。

---

## 测试策略

### 测试范围
- CRUD操作验证
- 复杂查询验证
- 分页查询验证
- 批量操作验证
- 事务验证

### 测试环境
- 数据库：H2内存数据库（MySQL兼容模式）
- ORM：MyBatis-Plus
- 测试框架：Spring Boot Test + JUnit 5

---

## 测试类命名规范

### 命名规则
- 测试类：`{Repository}Test`
- 示例：`TemplateRepositoryTest`

### 测试方法命名
- 格式：`{method}_{scenario}_{expectedResult}`
- 示例：
  - `save_shouldPersistEntity_whenValidInput`
  - `findById_shouldReturnEntity_whenExists`
  - `findById_shouldReturnEmpty_whenNotExists`

---

## 测试配置

### H2数据库配置
```yaml
# application-test.yml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    username: sa
    password:
  h2:
    console:
      enabled: true
  sql:
    init:
      mode: always
      schema-locations: classpath:schema-h2.sql
```

### 测试基类
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public abstract class RepositoryTestBase {
    @Autowired
    protected DataSource dataSource;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
    }
}
```

---

## 测试示例

### CRUD测试
```java
@DisplayName("Template Repository测试")
class TemplateRepositoryTest extends RepositoryTestBase {
    
    @Autowired
    private TemplateRepository templateRepository;
    
    @Test
    @DisplayName("保存模板 - 成功")
    void save_shouldPersistEntity_whenValidInput() {
        // Given
        Template template = Template.builder()
            .templateCode("TPL001")
            .templateName("测试模板")
            .build();
        
        // When
        Template saved = templateRepository.save(template);
        
        // Then
        assertNotNull(saved.getId());
        assertEquals("TPL001", saved.getTemplateCode());
        assertEquals("测试模板", saved.getTemplateName());
    }
    
    @Test
    @DisplayName("按ID查询 - 存在")
    void findById_shouldReturnEntity_whenExists() {
        // Given
        Template template = createAndSaveTemplate();
        
        // When
        Optional<Template> found = templateRepository.findById(template.getId());
        
        // Then
        assertTrue(found.isPresent());
        assertEquals(template.getId(), found.get().getId());
    }
    
    @Test
    @DisplayName("按ID查询 - 不存在")
    void findById_shouldReturnEmpty_whenNotExists() {
        // When
        Optional<Template> found = templateRepository.findById(99999L);
        
        // Then
        assertFalse(found.isPresent());
    }
}
```

### 查询测试
```java
@Test
@DisplayName("按编码查询 - 存在")
void findByCode_shouldReturnEntity_whenExists() {
    // Given
    Template template = createAndSaveTemplate("TPL001");
    
    // When
    Optional<Template> found = templateRepository.findByTemplateCode("TPL001");
    
    // Then
    assertTrue(found.isPresent());
    assertEquals("TPL001", found.get().getTemplateCode());
}

@Test
@DisplayName("条件查询 - 多条件")
void query_shouldReturnList_whenMultipleConditions() {
    // Given
    createAndSaveTemplates(10);
    TemplateCondition condition = TemplateCondition.builder()
        .status("ACTIVE")
        .templateName("测试")
        .build();
    
    // When
    List<Template> results = templateRepository.query(condition);
    
    // Then
    assertFalse(results.isEmpty());
    results.forEach(t -> {
        assertEquals("ACTIVE", t.getStatus());
        assertTrue(t.getTemplateName().contains("测试"));
    });
}
```

### 分页测试
```java
@Test
@DisplayName("分页查询 - 正确分页")
void page_shouldReturnPagedResults_whenValidPageRequest() {
    // Given
    createAndSaveTemplates(25);
    PageRequest pageRequest = PageRequest.of(0, 10);
    
    // When
    Page<Template> page = templateRepository.findAll(pageRequest);
    
    // Then
    assertEquals(10, page.getContent().size());
    assertEquals(25, page.getTotalElements());
    assertEquals(3, page.getTotalPages());
}
```

---

## 测试数据准备

### 使用@Sql注解
```java
@Test
@Sql(scripts = "classpath:test-data-template.sql")
@DisplayName("使用SQL脚本准备数据")
void testWithPreparedData() {
    // 测试逻辑
}
```

### 使用测试工厂
```java
public class TemplateTestFactory {
    public static Template createTemplate() {
        return Template.builder()
            .templateCode("TPL001")
            .templateName("测试模板")
            .status("ACTIVE")
            .createTime(LocalDateTime.now())
            .build();
    }
    
    public static List<Template> createTemplates(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> Template.builder()
                .templateCode("TPL" + String.format("%03d", i))
                .templateName("测试模板" + i)
                .status("ACTIVE")
                .createTime(LocalDateTime.now())
                .build())
            .collect(Collectors.toList());
    }
}
```

---

## 验证清单

### Repository测试必须验证
- [ ] 保存操作返回正确ID
- [ ] 查询操作返回正确数据
- [ ] 更新操作正确修改数据
- [ ] 删除操作正确移除数据
- [ ] 分页查询返回正确分页信息
- [ ] 条件查询返回符合条件的数据
- [ ] 空结果返回空集合而非null

---

## 详细规则引用

- Service层验证：查看 `14-testing-service.md`
- H2数据库配置：查看 `18-testing-h2-database.md`
- Mock策略：查看 `19-testing-mock-strategy.md`