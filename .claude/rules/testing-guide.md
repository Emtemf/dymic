---
name: testing-guide
description: Comprehensive testing guide including H2 database configuration, mock strategies, Chrome DevTools MCP usage, and test data initialization
---

# Testing Guide

## H2 Database Configuration

### Schema Configuration

**schema-h2.sql (src/test/resources/schema-h2.sql):**

```sql
-- H2-compatible schema for testing
-- Differences from production (openGauss):
-- 1. Use AUTO_INCREMENT instead of SERIAL
-- 2. Use VARCHAR2 instead of VARCHAR for compatibility
-- 3. JSONB is mapped to JSON in H2

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    total_amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shipping_address TEXT,
    metadata JSON, -- H2 uses JSON, production uses JSONB
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(19,2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    attributes JSON, -- H2 uses JSON, production uses JSONB
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for testing
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_products_name ON products(name);
```

**test-data.sql (src/test/resources/test-data.sql):**

```sql
-- Clear existing data
DELETE FROM orders;
DELETE FROM products;
DELETE FROM users;

-- Insert test users
INSERT INTO users (id, username, email, password, status, created_at, updated_at)
VALUES
    (1, 'john_doe', 'john@example.com', '$2a$10$encodedpassword1', 'ACTIVE', '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
    (2, 'jane_smith', 'jane@example.com', '$2a$10$encodedpassword2', 'ACTIVE', '2024-01-02 11:00:00', '2024-01-02 11:00:00'),
    (3, 'bob_wilson', 'bob@example.com', '$2a$10$encodedpassword3', 'INACTIVE', '2024-01-03 12:00:00', '2024-01-03 12:00:00');

-- Insert test products
INSERT INTO products (id, name, description, price, stock_quantity, attributes, created_at, updated_at)
VALUES
    (1, 'Laptop Pro', 'High-performance laptop', 1299.99, 50, '{"brand": "TechCorp", "warranty": "2 years"}', '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
    (2, 'Wireless Mouse', 'Ergonomic wireless mouse', 49.99, 200, '{"brand": "TechCorp", "color": "black"}', '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
    (3, 'USB-C Hub', 'Multi-port adapter', 79.99, 150, '{"ports": 7, "brand": "ConnectAll"}', '2024-01-02 11:00:00', '2024-01-02 11:00:00');

-- Insert test orders
INSERT INTO orders (id, user_id, order_number, total_amount, status, shipping_address, metadata, created_at, updated_at)
VALUES
    (1, 1, 'ORD-2024-001', 1349.98, 'COMPLETED', '123 Main St, City', '{"paymentMethod": "CREDIT_CARD"}', '2024-01-05 14:00:00', '2024-01-05 14:00:00'),
    (2, 1, 'ORD-2024-002', 79.99, 'PENDING', '456 Oak Ave, Town', '{"paymentMethod": "DEBIT_CARD"}', '2024-01-06 15:00:00', '2024-01-06 15:00:00'),
    (3, 2, 'ORD-2024-003', 1299.99, 'PROCESSING', '789 Pine Rd, Village', '{"paymentMethod": "CREDIT_CARD"}', '2024-01-07 16:00:00', '2024-01-07 16:00:00');

-- Reset auto-increment counters
ALTER TABLE users ALTER COLUMN id RESTART WITH 4;
ALTER TABLE products ALTER COLUMN id RESTART WITH 4;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 4;
```

### Application Configuration for Testing

**application-test.yml (src/test/resources/application-test.yml):**

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE
    username: sa
    password:
  h2:
    console:
      enabled: true
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none # Use schema-h2.sql instead
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  sql:
    init:
      mode: always
      schema-locations: classpath:schema-h2.sql
      data-locations: classpath:test-data.sql

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

logging:
  level:
    org.springframework.jdbc.datasource.init: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Test Base Class

```java
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected TestEntityManager entityManager;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Ensure test data is loaded
        assertTestDataLoaded();
    }

    private void assertTestDataLoaded() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users",
            Integer.class
        );
        assertThat(count).isGreaterThanOrEqualTo(3);
    }

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    protected <T> T saveAndFlush(T entity) {
        T saved = entityManager.persist(entity);
        entityManager.flush();
        return saved;
    }
}
```

---

## Mock Strategies

### Mockito Configuration

**pom.xml Dependencies:**

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

**Mock Test Configuration:**

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        User entity = new User();
        entity.setUsername(dto.getUsername());
        entity.setEmail(dto.getEmail());

        User savedEntity = new User();
        savedEntity.setId(1L);
        savedEntity.setUsername(dto.getUsername());
        savedEntity.setEmail(dto.getEmail());

        UserDTO expectedDTO = new UserDTO();
        expectedDTO.setId(1L);
        expectedDTO.setUsername(dto.getUsername());
        expectedDTO.setEmail(dto.getEmail());

        when(userMapper.toEntity(dto)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(savedEntity);
        when(userMapper.toDTO(savedEntity)).thenReturn(expectedDTO);

        // When
        UserDTO result = userService.createUser(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userMapper).toEntity(dto);
        verify(userRepository).save(entity);
        verify(userMapper).toDTO(savedEntity);
        verify(emailService).sendWelcomeEmail(dto.getEmail());
    }
}
```

### When to Mock

**Always Mock:**

1. **External Services:**
   ```java
   @Mock
   private PaymentGatewayClient paymentGatewayClient;

   @Mock
   private SmsService smsService;

   @Mock
   private ThirdPartyApiService thirdPartyApiService;
   ```

2. **Time-Dependent Operations:**
   ```java
   @Mock
   private Clock clock;

   @BeforeEach
   void setUp() {
       LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
       when(clock.instant()).thenReturn(fixedTime.atZone(ZoneId.systemDefault()).toInstant());
   }
   ```

3. **Non-Deterministic Operations:**
   ```java
   @Mock
   private RandomIdGenerator idGenerator;

   @Test
   void shouldGenerateOrderNumber() {
       when(idGenerator.generate()).thenReturn("ORD-12345");
       String orderNumber = orderService.generateOrderNumber();
       assertThat(orderNumber).isEqualTo("ORD-12345");
   }
   ```

4. **Database Operations in Unit Tests:**
   ```java
   @Mock
   private UserRepository userRepository;

   @Test
   void shouldFindUserById() {
       User user = new User();
       user.setId(1L);
       user.setUsername("testuser");

       when(userRepository.findById(1L)).thenReturn(Optional.of(user));

       UserDTO result = userService.findById(1L);
       assertThat(result.getUsername()).isEqualTo("testuser");
   }
   ```

**Never Mock:**

1. **Data Structures:**
   ```java
   // ❌ WRONG: Don't mock data structures
   @Mock
   private List<OrderItem> items;

   // ✅ CORRECT: Use real instances
   List<OrderItem> items = new ArrayList<>();
   items.add(new OrderItem(...));
   ```

2. **Mappers (Use Real Instances):**
   ```java
   // ✅ Use @Spy or real instance for mappers
   @Spy
   private UserMapper userMapper = UserMapper.INSTANCE;
   ```

3. **Entities/DTOs:**
   ```java
   // ✅ Always use real entity/DTO instances
   User user = new User();
   user.setId(1L);
   user.setUsername("testuser");
   ```

### Mock Verification Patterns

```java
@Test
void shouldVerifyMockInteractions() {
    // Verify method called once
    verify(userRepository).findById(1L);

    // Verify method called multiple times
    verify(userRepository, times(2)).save(any(User.class));

    // Verify method never called
    verify(userRepository, never()).delete(anyLong());

    // Verify method called with specific argument
    verify(emailService).sendEmail(eq("test@example.com"), anyString());

    // Capture and assert argument
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();
    assertThat(savedUser.getUsername()).isEqualTo("testuser");

    // Verify interaction order
    InOrder inOrder = inOrder(userRepository, emailService);
    inOrder.verify(userRepository).save(any(User.class));
    inOrder.verify(emailService).sendWelcomeEmail(anyString());
}
```

---

## Chrome DevTools MCP Usage

### Available MCP Commands

**Page Navigation and Management:**

```typescript
// Navigate to URL
mcp__chrome-devtools__navigate_page(url: "http://localhost:8080/login")

// Take screenshot
mcp__chrome-devtools__take_screenshot()

// Get page snapshot (accessibility tree)
mcp__chrome-devtools__take_snapshot()

// List all open pages
mcp__chrome-devtools__list_pages()

// Select specific page
mcp__chrome-devtools__select_page(pageId: "page-123")

// Open new page
mcp__chrome-devtools__new_page(url: "http://localhost:8080")

// Close page
mcp__chrome-devtools__close_page(pageId: "page-123")
```

**Element Interaction:**

```typescript
// Click element
mcp__chrome-devtools__click(selector: "#submit-button")

// Fill form field
mcp__chrome-devtools__fill(selector: "#username", value: "testuser")

// Fill entire form
mcp__chrome-devtools__fill_form(formData: {
    "#username": "testuser",
    "#password": "password123",
    "#email": "test@example.com"
})

// Type text (with delay)
mcp__chrome-devtools__type_text(selector: "#search-input", text: "search query", delay: 50)

// Hover over element
mcp__chrome-devtools__hover(selector: "#menu-item")

// Press key
mcp__chrome-devtools__press_key(key: "Enter")

// Drag element
mcp__chrome-devtools__drag(
    fromSelector: "#draggable-item",
    toSelector: "#drop-zone"
)

// Upload file
mcp__chrome-devtools__upload_file(
    selector: "#file-input",
    filePath: "/path/to/file.pdf"
)
```

**Network and Console:**

```typescript
// List network requests
mcp__chrome-devtools__list_network_requests(filter: {
    urlPattern: "/api/*",
    method: "POST"
})

// Get specific network request
mcp__chrome-devtools__get_network_request(requestId: "request-123")

// List console messages
mcp__chrome-devtools__list_console_messages(filter: {
    level: "error"
})

// Get console message details
mcp__chrome-devtools__get_console_message(messageId: "msg-123")
```

**Performance and Diagnostics:**

```typescript
// Run Lighthouse audit
mcp__chrome-devtools__lighthouse_audit(categories: ["performance", "accessibility"])

// Start performance trace
mcp__chrome-devtools__performance_start_trace()

// Stop performance trace
mcp__chrome-devtools__performance_stop_trace()

// Analyze performance insight
mcp__chrome-devtools__performance_analyze_insight()

// Take heap snapshot
mcp__chrome-devtools__take_heapsnapshot()
```

**Advanced Features:**

```typescript
// Execute JavaScript
mcp__chrome-devtools__evaluate_script(script: "return document.title")

// Emulate device
mcp__chrome-devtools__emulate(device: "iPhone X", viewport: {
    width: 375,
    height: 812,
    deviceScaleFactor: 3
})

// Resize page
mcp__chrome-devtools__resize_page(width: 1920, height: 1080)

// Wait for element
mcp__chrome-devtools__wait_for(selector: ".loading-spinner", options: {
    timeout: 5000,
    visible: true
})

// Handle dialog (alert, confirm, prompt)
mcp__chrome-devtools__handle_dialog(action: "accept", promptText: "input text")
```

### E2E Test Scripts

**Login Flow Test:**

```java
@Test
void shouldLoginSuccessfully() throws Exception {
    // Navigate to login page
    mcpClient.execute("mcp__chrome-devtools__navigate_page", Map.of(
        "url", "http://localhost:8080/login"
    ));

    // Wait for page to load
    mcpClient.execute("mcp__chrome-devtools__wait_for", Map.of(
        "selector", "#login-form",
        "options", Map.of("timeout", 5000)
    ));

    // Fill login form
    mcpClient.execute("mcp__chrome-devtools__fill_form", Map.of(
        "formData", Map.of(
            "#username", "testuser",
            "#password", "password123"
        )
    ));

    // Click submit button
    mcpClient.execute("mcp__chrome-devtools__click", Map.of(
        "selector", "#login-button"
    ));

    // Wait for redirect to dashboard
    mcpClient.execute("mcp__chrome-devtools__wait_for", Map.of(
        "selector", ".dashboard-container",
        "options", Map.of("timeout", 10000)
    ));

    // Verify successful login
    String title = mcpClient.execute("mcp__chrome-devtools__evaluate_script", Map.of(
        "script", "return document.title"
    ));

    assertThat(title).contains("Dashboard");

    // Take screenshot for evidence
    mcpClient.execute("mcp__chrome-devtools__take_screenshot");
}
```

**Form Validation Test:**

```java
@Test
void shouldValidateFormFields() throws Exception {
    mcpClient.execute("mcp__chrome-devtools__navigate_page", Map.of(
        "url", "http://localhost:8080/users/new"
    ));

    // Submit empty form to trigger validation
    mcpClient.execute("mcp__chrome-devtools__click", Map.of(
        "selector", "#submit-button"
    ));

    // Check validation errors
    String usernameError = mcpClient.execute("mcp__chrome-devtools__evaluate_script", Map.of(
        "script", "return document.querySelector('#username-error').textContent"
    ));

    assertThat(usernameError).isNotEmpty();

    // Fill invalid email
    mcpClient.execute("mcp__chrome-devtools__fill", Map.of(
        "selector", "#email",
        "value", "invalid-email"
    ));

    mcpClient.execute("mcp__chrome-devtools__click", Map.of(
        "selector", "#submit-button"
    ));

    // Verify email validation
    String emailError = mcpClient.execute("mcp__chrome-devtools__evaluate_script", Map.of(
        "script", "return document.querySelector('#email-error').textContent"
    ));

    assertThat(emailError).contains("valid email");
}
```

**API Integration Test:**

```java
@Test
void shouldSubmitFormAndVerifyAPI() throws Exception {
    // Navigate to order form
    mcpClient.execute("mcp__chrome-devtools__navigate_page", Map.of(
        "url", "http://localhost:8080/orders/new"
    ));

    // Fill order form
    mcpClient.execute("mcp__chrome-devtools__fill_form", Map.of(
        "formData", Map.of(
            "#customer-name", "John Doe",
            "#product-id", "12345",
            "#quantity", "2"
        )
    ));

    // Submit form
    mcpClient.execute("mcp__chrome-devtools__click", Map.of(
        "selector", "#submit-order"
    ));

    // Wait for API call to complete
    Thread.sleep(1000);

    // Check network requests
    List<Map<String, Object>> requests = mcpClient.execute(
        "mcp__chrome-devtools__list_network_requests",
        Map.of("filter", Map.of(
            "urlPattern", "/api/orders",
            "method", "POST"
        ))
    );

    assertThat(requests).hasSize(1);

    // Verify request details
    Map<String, Object> request = requests.get(0);
    String requestBody = (String) request.get("requestBody");

    assertThat(requestBody).contains("John Doe");
    assertThat(requestBody).contains("12345");
}
```

---

## Test Data Initialization Strategies

### Strategy 1: SQL Scripts (Recommended for Integration Tests)

```java
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {
    "classpath:schema-h2.sql",
    "classpath:test-data.sql"
})
class OrderIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldFindOrdersByUserId() {
        List<Order> orders = orderRepository.findByUserId(1L);
        assertThat(orders).hasSize(2);
    }
}
```

### Strategy 2: TestEntityManager (Recommended for JPA Tests)

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Create and persist test data
        User user1 = new User();
        user1.setUsername("testuser1");
        user1.setEmail("test1@example.com");
        user1.setPassword("password");
        entityManager.persist(user1);

        User user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setPassword("password");
        entityManager.persist(user2);

        entityManager.flush();
    }

    @Test
    void shouldFindUserByUsername() {
        Optional<User> found = userRepository.findByUsername("testuser1");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test1@example.com");
    }

    @AfterEach
    void tearDown() {
        entityManager.clear();
    }
}
```

### Strategy 3: Builder Pattern (Recommended for Complex Objects)

```java
public class UserTestDataBuilder {

    public static User.UserBuilder aUser() {
        return User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("encodedPassword")
            .status(UserStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now());
    }

    public static User.UserBuilder anInactiveUser() {
        return aUser()
            .status(UserStatus.INACTIVE);
    }

    public static User.UserBuilder anAdminUser() {
        return aUser()
            .username("admin")
            .email("admin@example.com")
            .role(Role.ADMIN);
    }
}

// Usage in tests
@Test
void shouldActivateInactiveUser() {
    User user = UserTestDataBuilder.anInactiveUser()
        .id(1L)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    userService.activateUser(1L);

    assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
}
```

### Strategy 4: Test Data Factory (Recommended for Reusable Test Data)

```java
public class TestDataFactory {

    public static User createTestUser() {
        return createTestUser("testuser", "test@example.com");
    }

    public static User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    public static Order createTestOrder(User user) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-" + System.currentTimeMillis());
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress("123 Test St");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    public static Product createTestProduct() {
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("29.99"));
        product.setStockQuantity(100);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
}

// Usage in tests
@Test
void shouldProcessOrder() {
    User user = TestDataFactory.createTestUser();
    Product product = TestDataFactory.createTestProduct();
    Order order = TestDataFactory.createTestOrder(user);

    entityManager.persist(user);
    entityManager.persist(product);
    entityManager.persist(order);
    entityManager.flush();
}
```

### Strategy 5: Database Cleanup (Recommended for Test Isolation)

```java
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CleanupIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        cleanupDatabase();
    }

    @AfterEach
    void tearDown() {
        // Clean database after each test (optional)
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("TRUNCATE TABLE products");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }

    @Test
    void shouldCreateOrder() {
        // Test with clean database
    }
}
```

### Strategy 6: Transactional Rollback (Recommended for Unit Tests)

```java
@DataJpaTest
@Transactional
class TransactionalTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        // Transaction will be rolled back after test
    }

    @Test
    void shouldFindSavedUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        // Transaction will be rolled back after test
    }
}
```

### Best Practices for Test Data

1. **Use SQL Scripts for Integration Tests:**
   - Load schema and initial data once
   - Tests should not modify shared data
   - Use separate test data for different scenarios

2. **Use TestEntityManager for JPA Tests:**
   - Create specific test data for each test
   - Clear persistence context after tests
   - Use transactions for test isolation

3. **Use Builders for Complex Objects:**
   - Provide sensible defaults
   - Override only necessary fields
   - Make test data creation expressive

4. **Use Factories for Reusable Data:**
   - Centralize test data creation
   - Ensure consistency across tests
   - Easy to maintain and update

5. **Clean Database Between Tests:**
   - Use @BeforeEach/@AfterEach cleanup
   - Disable foreign key checks for speed
   - Reset auto-increment counters

6. **Use Transactions for Test Isolation:**
   - @Transactional rolls back after each test
   - No cleanup needed
   - Tests are independent