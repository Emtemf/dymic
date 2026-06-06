---
name: testing-controller
description: Controller层验证规则
---

# Controller层验证规则

## 核心原则

**Controller层验证**：使用MockMvc进行集成测试，验证HTTP接口和参数校验。

---

## 测试策略

### 测试范围
- HTTP请求响应验证
- 参数校验验证
- 异常处理验证
- 响应格式验证
- 认证授权验证（如有）

### 测试环境
- 测试框架：Spring Boot Test + MockMvc
- 测试模式：WebMvcTest（仅测试Controller层）
- Mock：Service层Mock

---

## 测试类命名规范

### 命名规则
- 测试类：`{Controller}Test`
- 示例：`TemplateControllerTest`

### 测试方法命名
- 格式：`{httpMethod}_{path}_{scenario}_{expectedResult}`
- 示例：
  - `post_api_templates_shouldReturnCreated_whenValidInput`
  - `get_api_templates_id_shouldReturnOk_whenExists`
  - `post_api_templates_shouldReturnBadRequest_whenInvalidInput`

---

## 测试配置

### 测试基类
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

## 测试示例

### POST请求测试
```java
@DisplayName("Template Controller测试")
class TemplateControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private TemplateService templateService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @DisplayName("POST /api/templates - 创建成功")
    void post_api_templates_shouldReturnCreated_whenValidInput() throws Exception {
        // Given
        CreateTemplateRequest request = CreateTemplateRequest.builder()
            .templateCode("TPL001")
            .templateName("测试模板")
            .description("测试描述")
            .build();
        
        TemplateDTO dto = TemplateDTO.builder()
            .id(1L)
            .templateCode("TPL001")
            .templateName("测试模板")
            .description("测试描述")
            .createTime(LocalDateTime.now())
            .build();
        
        when(templateService.create(any())).thenReturn(dto);
        
        // When & Then
        mockMvc.perform(post("/api/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.templateCode").value("TPL001"))
            .andExpect(jsonPath("$.data.templateName").value("测试模板"));
        
        verify(templateService, times(1)).create(any());
    }
    
    @Test
    @DisplayName("POST /api/templates - 参数校验失败")
    void post_api_templates_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        // Given - 缺少必填字段
        CreateTemplateRequest request = CreateTemplateRequest.builder()
            .templateName("测试模板")
            .build();
        
        // When & Then
        mockMvc.perform(post("/api/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());
        
        verify(templateService, never()).create(any());
    }
}
```

### GET请求测试
```java
@Test
@DisplayName("GET /api/templates/{id} - 查询成功")
void get_api_templates_id_shouldReturnOk_whenExists() throws Exception {
    // Given
    TemplateDTO dto = TemplateDTO.builder()
        .id(1L)
        .templateCode("TPL001")
        .templateName("测试模板")
        .build();
    
    when(templateService.findById(1L)).thenReturn(dto);
    
    // When & Then
    mockMvc.perform(get("/api/templates/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.templateCode").value("TPL001"));
}

@Test
@DisplayName("GET /api/templates/{id} - 不存在")
void get_api_templates_id_shouldReturnNotFound_whenNotExists() throws Exception {
    // Given
    when(templateService.findById(999L)).thenThrow(new TemplateNotFoundException(999L));
    
    // When & Then
    mockMvc.perform(get("/api/templates/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
}
```

### PUT请求测试
```java
@Test
@DisplayName("PUT /api/templates/{id} - 更新成功")
void put_api_templates_id_shouldReturnOk_whenValidInput() throws Exception {
    // Given
    UpdateTemplateRequest request = UpdateTemplateRequest.builder()
        .templateName("更新后的模板")
        .build();
    
    TemplateDTO dto = TemplateDTO.builder()
        .id(1L)
        .templateCode("TPL001")
        .templateName("更新后的模板")
        .build();
    
    when(templateService.update(any())).thenReturn(dto);
    
    // When & Then
    mockMvc.perform(put("/api/templates/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.templateName").value("更新后的模板"));
}
```

### DELETE请求测试
```java
@Test
@DisplayName("DELETE /api/templates/{id} - 删除成功")
void delete_api_templates_id_shouldReturnOk_whenExists() throws Exception {
    // Given
    doNothing().when(templateService).delete(1L);
    
    // When & Then
    mockMvc.perform(delete("/api/templates/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
    
    verify(templateService, times(1)).delete(1L);
}
```

### 分页查询测试
```java
@Test
@DisplayName("GET /api/templates - 分页查询成功")
void get_api_templates_shouldReturnPagedResult_whenValidParams() throws Exception {
    // Given
    List<TemplateDTO> templates = Arrays.asList(
        TemplateDTO.builder().id(1L).templateCode("TPL001").build(),
        TemplateDTO.builder().id(2L).templateCode("TPL002").build()
    );
    
    Page<TemplateDTO> page = new PageImpl<>(templates, PageRequest.of(0, 10), 2);
    
    when(templateService.page(any())).thenReturn(page);
    
    // When & Then
    mockMvc.perform(get("/api/templates")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.content.length()").value(2))
        .andExpect(jsonPath("$.data.totalElements").value(2));
}
```

---

## 统一响应格式验证

### 响应结构
```java
@Test
void response_shouldHaveConsistentFormat() throws Exception {
    // Given
    TemplateDTO dto = TemplateDTO.builder().id(1L).build();
    when(templateService.findById(1L)).thenReturn(dto);
    
    // When & Then
    mockMvc.perform(get("/api/templates/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").exists())
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.message").exists());
}
```

---

## 异常处理验证

### 业务异常
```java
@Test
void shouldReturnBadRequest_whenBusinessException() throws Exception {
    // Given
    when(templateService.findById(999L))
        .thenThrow(new BusinessException("模板不存在"));
    
    // When & Then
    mockMvc.perform(get("/api/templates/999"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("模板不存在"));
}
```

### 系统异常
```java
@Test
void shouldReturnInternalServerError_whenSystemException() throws Exception {
    // Given
    when(templateService.findById(1L))
        .thenThrow(new RuntimeException("系统错误"));
    
    // When & Then
    mockMvc.perform(get("/api/templates/1"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success").value(false));
}
```

---

## 验证清单

### Controller测试必须验证
- [ ] HTTP状态码正确
- [ ] 响应格式统一
- [ ] 参数校验生效
- [ ] Service调用正确
- [ ] 异常处理正确
- [ ] 认证授权正确（如有）

---

## 详细规则引用

- Service层验证：查看 `14-testing-service.md`
- 前端界面验证：查看 `16-testing-frontend.md`
- Mock策略：查看 `19-testing-mock-strategy.md`