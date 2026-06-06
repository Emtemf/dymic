---
name: testing-service
description: Service层验证规则
---

# Service层验证规则

## 核心原则

**Service层验证**：使用Mockito进行单元测试，验证业务逻辑和编排。

---

## 测试策略

### 测试范围
- 业务逻辑验证
- 业务编排验证
- 异常处理验证
- 事务边界验证
- DTO转换验证

### 测试环境
- Mock框架：Mockito
- 测试框架：JUnit 5 + Spring Boot Test
- 不依赖数据库

---

## 测试类命名规范

### 命名规则
- 测试类：`{Service}Test`
- 示例：`TemplateServiceTest`

### 测试方法命名
- 格式：`{method}_{scenario}_{expectedResult}`
- 示例：
  - `create_shouldReturnDTO_whenValidInput`
  - `create_shouldThrowException_whenCodeDuplicate`
  - `findById_shouldReturnDTO_whenExists`

---

## Mock策略

### 规则1：只Mock外部依赖
- ✅ Mock Repository
- ✅ Mock 其他Service
- ✅ Mock Gateway
- ❌ 不Mock测试目标类本身
- ❌ 不Mock DTO转换器

### 规则2：使用构造器注入
```java
@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {
    
    @Mock
    private TemplateRepository templateRepository;
    
    @Mock
    private VersionRepository versionRepository;
    
    @InjectMocks
    private TemplateServiceImpl templateService;
    
    // 测试方法
}
```

---

## 测试示例

### 创建操作测试
```java
@DisplayName("Template Service测试")
class TemplateServiceTest {
    
    @Mock
    private TemplateRepository templateRepository;
    
    @Mock
    private TemplateConverter templateConverter;
    
    @InjectMocks
    private TemplateServiceImpl templateService;
    
    @Test
    @DisplayName("创建模板 - 成功")
    void create_shouldReturnDTO_whenValidInput() {
        // Given
        CreateTemplateRequest request = CreateTemplateRequest.builder()
            .templateCode("TPL001")
            .templateName("测试模板")
            .build();
        
        Template template = Template.builder()
            .id(1L)
            .templateCode("TPL001")
            .templateName("测试模板")
            .build();
        
        TemplateDTO dto = TemplateDTO.builder()
            .id(1L)
            .templateCode("TPL001")
            .templateName("测试模板")
            .build();
        
        when(templateConverter.toDomain(request)).thenReturn(template);
        when(templateRepository.save(template)).thenReturn(template);
        when(templateConverter.toDTO(template)).thenReturn(dto);
        
        // When
        TemplateDTO result = templateService.create(request);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TPL001", result.getTemplateCode());
        verify(templateRepository, times(1)).save(template);
    }
    
    @Test
    @DisplayName("创建模板 - 编码重复异常")
    void create_shouldThrowException_whenCodeDuplicate() {
        // Given
        CreateTemplateRequest request = CreateTemplateRequest.builder()
            .templateCode("TPL001")
            .templateName("测试模板")
            .build();
        
        when(templateRepository.existsByTemplateCode("TPL001")).thenReturn(true);
        
        // When & Then
        assertThrows(DuplicateCodeException.class, () -> {
            templateService.create(request);
        });
        
        verify(templateRepository, never()).save(any());
    }
}
```

### 查询操作测试
```java
@Test
@DisplayName("按ID查询 - 存在")
void findById_shouldReturnDTO_whenExists() {
    // Given
    Template template = Template.builder()
        .id(1L)
        .templateCode("TPL001")
        .templateName("测试模板")
        .build();
    
    TemplateDTO dto = TemplateDTO.builder()
        .id(1L)
        .templateCode("TPL001")
        .templateName("测试模板")
        .build();
    
    when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
    when(templateConverter.toDTO(template)).thenReturn(dto);
    
    // When
    TemplateDTO result = templateService.findById(1L);
    
    // Then
    assertNotNull(result);
    assertEquals(1L, result.getId());
}

@Test
@DisplayName("按ID查询 - 不存在异常")
void findById_shouldThrowException_whenNotExists() {
    // Given
    when(templateRepository.findById(999L)).thenReturn(Optional.empty());
    
    // When & Then
    assertThrows(TemplateNotFoundException.class, () -> {
        templateService.findById(999L);
    });
}
```

### 更新操作测试
```java
@Test
@DisplayName("更新模板 - 成功")
void update_shouldReturnDTO_whenValidInput() {
    // Given
    UpdateTemplateRequest request = UpdateTemplateRequest.builder()
        .id(1L)
        .templateName("更新后的模板")
        .build();
    
    Template existing = Template.builder()
        .id(1L)
        .templateCode("TPL001")
        .templateName("原模板")
        .build();
    
    Template updated = Template.builder()
        .id(1L)
        .templateCode("TPL001")
        .templateName("更新后的模板")
        .build();
    
    TemplateDTO dto = TemplateDTO.builder()
        .id(1L)
        .templateCode("TPL001")
        .templateName("更新后的模板")
        .build();
    
    when(templateRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(templateRepository.save(updated)).thenReturn(updated);
    when(templateConverter.toDTO(updated)).thenReturn(dto);
    
    // When
    TemplateDTO result = templateService.update(request);
    
    // Then
    assertEquals("更新后的模板", result.getTemplateName());
    verify(templateRepository, times(1)).save(any());
}
```

---

## 事务测试

### 使用@Transactional测试
```java
@Test
@Transactional
@DisplayName("事务测试 - 回滚")
void createWithVersion_shouldRollback_whenVersionCreateFailed() {
    // Given
    CreateTemplateRequest request = createRequest();
    
    when(templateRepository.save(any())).thenReturn(template);
    when(versionRepository.save(any())).thenThrow(new RuntimeException("版本创建失败"));
    
    // When & Then
    assertThrows(RuntimeException.class, () -> {
        templateService.createWithVersion(request);
    });
    
    // 验证事务回滚
    verify(templateRepository, times(1)).save(any());
    verify(versionRepository, times(1)).save(any());
}
```

---

## 验证清单

### Service测试必须验证
- [ ] 正常流程返回正确结果
- [ ] 异常流程抛出正确异常
- [ ] Mock调用次数和参数正确
- [ ] 业务逻辑正确执行
- [ ] DTO转换正确
- [ ] 异常信息正确

---

## 详细规则引用

- Repository层验证：查看 `13-testing-repository.md`
- Controller层验证：查看 `15-testing-controller.md`
- Mock策略：查看 `19-testing-mock-strategy.md`