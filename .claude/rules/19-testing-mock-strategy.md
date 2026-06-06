---
name: testing-mock-strategy
description: Mock策略规则
---

# Mock策略规则

## 核心原则

**分层Mock**：根据测试层级选择合适的Mock策略，隔离外部依赖，专注测试目标。

---

## Mock策略总览

| 测试层级 | Mock策略 | 说明 |
|---------|---------|------|
| Repository层 | 不Mock，使用H2 | 测试SQL和数据访问 |
| Service层 | Mock Repository | 测试业务逻辑 |
| Controller层 | Mock Service | 测试HTTP接口 |

---

## Service层Mock策略

### Mock对象
- ✅ Mock Repository
- ✅ Mock 其他Service
- ✅ Mock Gateway
- ✅ Mock 外部依赖

### 不Mock对象
- ❌ 不Mock 测试目标类本身
- ❌ 不Mock DTO/Request/Response
- ❌ 不Mock Converter（如MapStruct）

### Mock框架配置
```java
@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {
    
    @Mock
    private TemplateRepository templateRepository;
    
    @Mock
    private VersionRepository versionRepository;
    
    @Mock
    private TemplateConverter templateConverter;
    
    @InjectMocks
    private TemplateServiceImpl templateService;
    
    // 测试方法
}
```

---

## Controller层Mock策略

### Mock对象
- ✅ Mock Service
- ✅ Mock 认证信息（如有）

### 不Mock对象
- ❌ 不Mock 测试目标Controller
- ❌ 不Mock Request/Response
- ❌ 不Mock 参数校验

### Mock框架配置
```java
@WebMvcTest(TemplateController.class)
@ActiveProfiles("test")
class TemplateControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TemplateService templateService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 测试方法
}
```

---

## Mockito使用规范

### 基本Mock操作

#### when().thenReturn()
```java
// 模拟返回值
when(templateRepository.findById(1L))
    .thenReturn(Optional.of(template));

// 模拟返回空
when(templateRepository.findById(999L))
    .thenReturn(Optional.empty());

// 模拟抛出异常
when(templateRepository.findById(1L))
    .thenThrow(new RuntimeException("数据库错误"));
```

#### doNothing().when()
```java
// 模拟void方法
doNothing().when(templateRepository).delete(1L);

// 模拟void方法抛出异常
doThrow(new RuntimeException("删除失败"))
    .when(templateRepository).delete(1L);
```

#### verify()
```java
// 验证调用次数
verify(templateRepository, times(1)).save(any());
verify(templateRepository, never()).delete(any());
verify(templateRepository, atLeast(1)).findById(any());
verify(templateRepository, atMost(2)).save(any());

// 验证调用参数
verify(templateRepository).save(argThat(t -> 
    t.getTemplateCode().equals("TPL001")
));
```

### 高级Mock操作

#### 参数匹配器
```java
// any() - 匹配任意参数
when(templateRepository.findById(any())).thenReturn(Optional.of(template));

// eq() - 精确匹配
when(templateRepository.findById(eq(1L))).thenReturn(Optional.of(template));

// argThat() - 自定义匹配
when(templateRepository.save(argThat(t -> 
    t.getTemplateCode() != null
))).thenReturn(template);
```

#### 连续调用
```java
// 第一次调用返回A，第二次返回B
when(templateRepository.count())
    .thenReturn(0L)
    .thenReturn(1L);
```

#### Spy（部分Mock）
```java
// 使用真实对象，部分方法Mock
TemplateService spyService = spy(new TemplateServiceImpl());
when(spyService.findById(1L)).thenReturn(Optional.of(template));
```

---

## Mock最佳实践

### 实践1：每个测试独立Mock
```java
@Test
void testSomething() {
    // Given - 在每个测试中独立设置Mock
    when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
    
    // When
    TemplateDTO result = templateService.findById(1L);
    
    // Then
    assertNotNull(result);
}
```

### 实践2：使用@Captor捕获参数
```java
@Captor
private ArgumentCaptor<Template> templateCaptor;

@Test
void testSave() {
    // When
    templateService.create(request);
    
    // Then
    verify(templateRepository).save(templateCaptor.capture());
    Template saved = templateCaptor.getValue();
    assertEquals("TPL001", saved.getTemplateCode());
}
```

### 实践3：使用BDD风格
```java
@Test
void testFindById() {
    // Given
    given(templateRepository.findById(1L)).willReturn(Optional.of(template));
    
    // When
    TemplateDTO result = templateService.findById(1L);
    
    // Then
    then(templateRepository).should().findById(1L);
    assertNotNull(result);
}
```

### 实践4：重置Mock状态
```java
@BeforeEach
void setUp() {
    // 重置Mock状态
    Mockito.reset(templateRepository);
}
```

---

## Mock陷阱与解决

### 陷阱1：Mock私有方法
```java
// 错误：无法Mock私有方法
when(templateService.privateMethod()).thenReturn(result);

// 解决：重构为可Mock的公共方法
when(templateService.publicMethod()).thenReturn(result);
```

### 陷阱2：Mock静态方法
```java
// 错误：Mockito无法Mock静态方法
when(UtilClass.staticMethod()).thenReturn(result);

// 解决：使用Mockito Inline或PowerMock
try (MockedStatic<UtilClass> mocked = mockStatic(UtilClass.class)) {
    mocked.when(UtilClass::staticMethod).thenReturn(result);
    // 测试逻辑
}
```

### 陷阱3：Mock final类
```java
// 错误：Mockito无法Mock final类
when(finalClass.method()).thenReturn(result);

// 解决：使用Mockito Inline或重构
```

### 陷阱4：Mock链式调用
```java
// 错误：链式Mock
when(a.getB().getC()).thenReturn(value);

// 正确：分步Mock
B b = mock(B.class);
when(a.getB()).thenReturn(b);
when(b.getC()).thenReturn(value);
```

---

## 验证清单

### Mock测试必须验证
- [ ] Mock设置正确
- [ ] 返回值符合预期
- [ ] 调用次数正确
- [ ] 调用参数正确
- [ ] 异常处理正确
- [ ] 无未Mock的依赖

---

## 详细规则引用

- Repository层验证：查看 `13-testing-repository.md`
- Service层验证：查看 `14-testing-service.md`
- Controller层验证：查看 `15-testing-controller.md`
- H2数据库配置：查看 `18-testing-h2-database.md`