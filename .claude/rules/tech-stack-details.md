---
name: tech-stack-details
description: Detailed configuration and usage rules for MapStruct, Lombok, MyBatis-Plus, and JSONB handling for openGauss and H2
---

# Tech Stack Details

## MapStruct Configuration

### Maven Dependencies

**pom.xml:**

```xml
<dependencies>
    <!-- MapStruct Core -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>

    <!-- MapStruct Processor -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>1.5.5.Final</version>
        <scope>provided</scope>
    </dependency>

    <!-- Lombok (must be before mapstruct-processor) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <!-- Lombok MUST come first -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>1.18.30</version>
                    </path>
                    <!-- MapStruct processor second -->
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.5.5.Final</version>
                    </path>
                    <!-- Lombok-MapStruct binding -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>0.2.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Mapper Interface Definition

**Basic Mapper:**

```java
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Entity → DTO (outbound)
    UserDTO toDTO(User entity);

    // DTO → Entity (inbound)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserCreateDTO dto);

    // Partial update
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UserUpdateDTO dto, @MappingTarget User entity);

    // List mapping
    List<UserDTO> toDTOList(List<User> entities);

    // Page mapping
    default Page<UserDTO> toDTOPage(Page<User> entityPage) {
        return entityPage.map(this::toDTO);
    }
}
```

**Complex Mapping:**

```java
@Mapper(
    componentModel = "spring",
    uses = {OrderItemMapper.class, AddressMapper.class}
)
public interface OrderMapper {

    // Nested object mapping
    @Mapping(source = "items", target = "orderItems")
    @Mapping(source = "shippingAddress", target = "address")
    OrderDTO toDTO(Order entity);

    // Custom mapping for calculated field
    @Mapping(target = "totalAmount", expression = "java(calculateTotal(dto.getItems()))")
    @Mapping(target = "orderNumber", expression = "java(generateOrderNumber())")
    Order toEntity(OrderCreateDTO dto);

    default BigDecimal calculateTotal(List<OrderItemDTO> items) {
        return items.stream()
            .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    // Enum mapping
    @ValueMapping(source = "PENDING_PAYMENT", target = "PENDING")
    @ValueMapping(source = "PAID", target = "PROCESSING")
    @ValueMapping(source = "SHIPPED", target = "COMPLETED")
    OrderStatusDTO toDTO(OrderStatus status);
}
```

**MapStruct Best Practices:**

1. **Always use `@Mapper(componentModel = "spring")` for Spring integration**
2. **Use `unmappedTargetPolicy = ReportingPolicy.IGNORE` only for legacy code**
3. **Always check for null with `nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS`**
4. **Use `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` for partial updates**
5. **Never expose entities to controllers - always use DTOs**
6. **Use `uses` to inject other mappers for nested objects**
7. **Use `@AfterMapping` for complex logic that can't be expressed in mappings**

```java
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "totalItems", ignore = true)
    OrderDTO toDTO(Order entity);

    @AfterMapping
    default void calculateTotalItems(@MappingTarget OrderDTO dto, Order entity) {
        int totalItems = entity.getItems().stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
        dto.setTotalItems(totalItems);
    }
}
```

---

## Lombok Configuration

### Allowed Annotations

**Data Class Annotations:**

```java
@Getter                     // Generate getters for all fields
@Setter                     // Generate setters for all fields
@AllArgsConstructor          // Generate constructor with all fields
@NoArgsConstructor          // Generate no-args constructor
@RequiredArgsConstructor    // Generate constructor for final fields
@Builder                    // Generate builder pattern
@SuperBuilder              // Builder for inheritance
```

**Utility Annotations:**

```java
@Slf4j                      // Generate logger: log.info("message")
@ToString                  // Generate toString() method
@EqualsAndHashCode         // Generate equals() and hashCode()
@FieldDefaults            // Set field visibility and defaults
```

**Exception Annotations:**

```java
@Slf4j
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
        log.error("Business exception occurred: {}", message);
    }
}
```

**Builder Pattern:**

```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}

// Usage
UserDTO dto = UserDTO.builder()
    .id(1L)
    .username("testuser")
    .email("test@example.com")
    .createdAt(LocalDateTime.now())
    .build();
```

**Entity with Builder:**

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### Forbidden Annotations

**❌ NEVER USE:**

```java
@Data                      // Generates @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
                           // PROBLEM: Generates equals/hashCode based on all fields, breaks JPA entities

@Value                     // Immutable variant of @Data
                           // PROBLEM: Same as @Data for JPA entities

@FieldNameConstants        // Generates constants for field names
                           // PROBLEM: Unnecessary complexity

@With                      // Generates withX() methods for immutable objects
                           // PROBLEM: Conflicts with JPA setters
```

**Why @Data is Forbidden:**

```java
// ❌ WRONG: @Data on JPA entity
@Entity
@Data
public class Order {
    @Id
    private Long id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();
}

// PROBLEM: equals() and hashCode() include 'items' field
// This causes infinite recursion and incorrect behavior with JPA

// ✅ CORRECT: Use specific annotations
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private Long id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    // Custom equals/hashCode excluding collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

### Lombok Configuration File

**lombok.config (project root):**

```properties
# Enable delombok for generated sources
lombok.anyConstructor.addConstructorProperties=true

# Configure logging
lombok.log.fieldName=log
lombok.log.fieldIsStatic=true

# Configure builder
lombok.builder.className=Builder

# Configure toString
lombok.toString.callSuper=CALL
lombok.toString.includeFieldNames=true

# Configure equals/hashCode
lombok.equalsAndHashCode.callSuper=CALL
lombok.equalsAndHashCode.doNotUseGetters=true

# Field defaults
lombok.fieldDefaults.defaultPrivate=true
lombok.fieldDefaults.defaultFinal=false
```

---

## MyBatis-Plus Configuration

### Maven Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- For pagination -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-jsqlparser</artifactId>
        <version>3.5.5</version>
    </dependency>
</dependencies>
```

### Configuration

**application.yml:**

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      # Primary key configuration
      id-type: auto
      # Logic delete configuration
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      # Table prefix
      table-prefix: t_
      # Field strategy
      insert-strategy: not_null
      update-strategy: not_null
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.example.entity
```

### Pagination Plugin

**Configuration Class:**

```java
@Configuration
@MapperScan("com.example.mapper")
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // Pagination plugin (MUST be first)
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        paginationInterceptor.setMaxLimit(1000L); // Maximum page size
        paginationInterceptor.setOverflow(false); // Don't return first page when page > total pages
        interceptor.addInnerInterceptor(paginationInterceptor);

        // Optimistic lock plugin
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // SQL performance plugin (for development)
        interceptor.addInnerInterceptor(new IllegalSQLInnerInterceptor());

        return interceptor;
    }

    @Bean
    public IdentifierGenerator identifierGenerator() {
        return new CustomIdentifierGenerator();
    }
}
```

### Pagination Usage

**Repository Layer:**

```java
@Repository
public interface UserRepository extends BaseMapper<User> {
    // BaseMapper provides:
    // - insert(T entity)
    // - deleteById(Serializable id)
    // - updateById(T entity)
    // - selectById(Serializable id)
    // - selectPage(Page<T> page, Wrapper<T> queryWrapper)
}

// Custom pagination query
@Repository
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT o.*, u.username FROM orders o " +
            "LEFT JOIN users u ON o.user_id = u.id " +
            "WHERE o.status = #{status} " +
            "ORDER BY o.created_at DESC")
    IPage<OrderWithUserDTO> selectOrdersWithUser(
        Page<OrderWithUserDTO> page,
        @Param("status") String status
    );
}
```

**Service Layer:**

```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    public Page<UserDTO> findUsers(UserQueryDTO queryDTO) {
        // Create pagination object
        Page<User> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        // Build query wrapper
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
            .like(StringUtils.isNotBlank(queryDTO.getUsername()), User::getUsername, queryDTO.getUsername())
            .eq(StringUtils.isNotBlank(queryDTO.getEmail()), User::getEmail, queryDTO.getEmail())
            .eq(queryDTO.getStatus() != null, User::getStatus, queryDTO.getStatus())
            .orderByDesc(User::getCreatedAt);

        // Execute pagination query
        Page<User> userPage = userRepository.selectPage(page, queryWrapper);

        // Convert to DTO
        return userPage.convert(userMapper::toDTO);
    }
}
```

**Controller Layer:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUsers(UserQueryDTO queryDTO) {
        Page<UserDTO> page = userService.findUsers(queryDTO);
        return ResponseEntity.ok(page);
    }
}
```

### Logical Deletion

**Entity Configuration:**

```java
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    // Logical delete field
    @TableLogic
    @Column(name = "deleted", nullable = false)
    private Integer deleted = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

**SQL Generation:**

```java
// When you call userRepository.deleteById(1L):
// Generated SQL: UPDATE users SET deleted = 1 WHERE id = 1

// When you call userRepository.selectById(1L):
// Generated SQL: SELECT * FROM users WHERE id = 1 AND deleted = 0

// When you call userRepository.selectList(null):
// Generated SQL: SELECT * FROM users WHERE deleted = 0
```

**Custom Delete Logic:**

```java
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public void deleteOrder(Long orderId) {
        // Check if order can be deleted
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            throw new NotFoundException("Order not found");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BusinessException("CANNOT_DELETE_COMPLETED_ORDER",
                "Cannot delete completed order");
        }

        // Logical delete (MyBatis-Plus handles this)
        orderRepository.deleteById(orderId);

        // Also update inventory
        restoreInventory(order);
    }
}
```

### Wrapper Query Examples

**Complex Query:**

```java
public List<OrderDTO> findComplexOrders(OrderQueryDTO query) {
    LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

    // Basic conditions
    wrapper.eq(query.getUserId() != null, Order::getUserId, query.getUserId())
           .eq(query.getStatus() != null, Order::getStatus, query.getStatus())
           .like(StringUtils.isNotBlank(query.getOrderNumber()), Order::getOrderNumber, query.getOrderNumber());

    // Date range
    wrapper.between(query.getStartDate() != null && query.getEndDate() != null,
                    Order::getCreatedAt,
                    query.getStartDate(),
                    query.getEndDate());

    // Amount range
    wrapper.ge(query.getMinAmount() != null, Order::getTotalAmount, query.getMinAmount())
           .le(query.getMaxAmount() != null, Order::getTotalAmount, query.getMaxAmount());

    // IN clause
    if (CollectionUtils.isNotEmpty(query.getStatusList())) {
        wrapper.in(Order::getStatus, query.getStatusList());
    }

    // NOT IN clause
    if (CollectionUtils.isNotEmpty(query.getExcludeUserIds())) {
        wrapper.notIn(Order::getUserId, query.getExcludeUserIds());
    }

    // Nested conditions
    wrapper.and(w -> w
        .eq(Order::getStatus, OrderStatus.PENDING)
        .or()
        .eq(Order::getStatus, OrderStatus.PROCESSING)
    );

    // Order by
    wrapper.orderByDesc(Order::getCreatedAt);

    return orderMapper.selectList(wrapper).stream()
        .map(orderMapper::toDTO)
        .collect(Collectors.toList());
}
```

**Update with Wrapper:**

```java
@Transactional
public void bulkUpdateOrderStatus(List<Long> orderIds, OrderStatus newStatus) {
    LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
    updateWrapper.in(Order::getId, orderIds)
                 .set(Order::getStatus, newStatus)
                 .set(Order::getUpdatedAt, LocalDateTime.now());

    orderRepository.update(null, updateWrapper);
}
```

---

## JSONB Handling

### openGauss Configuration

**Entity Definition:**

```java
@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "jsonb")
    private String attributes; // Stored as JSONB string

    @Column(columnDefinition = "jsonb")
    private String metadata; // Stored as JSONB string

    @Column(nullable = false)
    private BigDecimal price;
}
```

**JSONB Utility Class:**

```java
public class JsonbUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Object to JSONB string
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    // JSONB string to Object
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON to object", e);
        }
    }

    // JSONB string to List
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON to list", e);
        }
    }
}
```

**Repository Usage:**

```java
@Repository
public interface ProductRepository extends BaseMapper<Product> {

    // Query JSONB field using native SQL
    @Select("SELECT * FROM products WHERE attributes::jsonb->>'brand' = #{brand}")
    List<Product> findByBrand(@Param("brand") String brand);

    @Select("SELECT * FROM products WHERE attributes::jsonb->'specs'->>'color' = #{color}")
    List<Product> findByColor(@Param("color") String color);

    @Select("SELECT * FROM products WHERE CAST(attributes::jsonb->>'price' AS DECIMAL) BETWEEN #{min} AND #{max}")
    List<Product> findByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    // Update JSONB field
    @Update("UPDATE products SET attributes = jsonb_set(attributes::jsonb, '{price}', #{price}::jsonb) WHERE id = #{id}")
    int updatePrice(@Param("id") Long id, @Param("price") String price);
}
```

**Service Usage:**

```java
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public ProductDTO createProduct(ProductCreateDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());

        // Convert DTO attributes to JSONB
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("brand", dto.getBrand());
        attributes.put("color", dto.getColor());
        attributes.put("warranty", dto.getWarranty());
        product.setAttributes(JsonbUtils.toJson(attributes));

        Product saved = productRepository.insert(product);
        return productMapper.toDTO(saved);
    }

    public List<ProductDTO> findByBrand(String brand) {
        return productRepository.findByBrand(brand).stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }
}
```

### H2 Configuration for Testing

**Schema Differences:**

```sql
-- Production (openGauss)
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    attributes JSONB, -- JSONB type
    metadata JSONB,    -- JSONB type
    price DECIMAL(19,2) NOT NULL
);

-- Testing (H2)
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    attributes JSON, -- JSON type (H2 doesn't have JSONB)
    metadata JSON,    -- JSON type
    price DECIMAL(19,2) NOT NULL
);
```

**H2 JSON Functions:**

```java
@Repository
public interface ProductRepository extends BaseMapper<Product> {

    // Production query
    @Select("SELECT * FROM products WHERE attributes::jsonb->>'brand' = #{brand}")
    List<Product> findByBrand(@Param("brand") String brand);

    // H2-compatible query (use JSON functions)
    @Select("SELECT * FROM products WHERE JSON_GET(attributes, '$.brand') = #{brand}")
    List<Product> findByBrandH2(@Param("brand") String brand);
}
```

**Profile-Specific Repository:**

```java
// Production repository
@Repository
@Profile("!test")
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> findByBrand(String brand) {
        Query query = entityManager.createNativeQuery(
            "SELECT * FROM products WHERE attributes::jsonb->>'brand' = :brand",
            Product.class
        );
        query.setParameter("brand", brand);
        return query.getResultList();
    }
}

// Test repository
@Repository
@Profile("test")
public class ProductRepositoryTestImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> findByBrand(String brand) {
        // Use H2 JSON functions
        Query query = entityManager.createNativeQuery(
            "SELECT * FROM products WHERE JSON_GET(attributes, '$.brand') = :brand",
            Product.class
        );
        query.setParameter("brand", brand);
        return query.getResultList();
    }
}
```

**Alternative: JSONB in Java:**

```java
// Instead of using JSONB queries, parse in Java
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductDTO> findByBrand(String brand) {
        // Load all products and filter in Java
        List<Product> allProducts = productRepository.selectList(null);

        return allProducts.stream()
            .filter(product -> {
                Map<String, Object> attributes = JsonbUtils.fromJson(
                    product.getAttributes(),
                    new TypeReference<Map<String, Object>>() {}
                );
                return brand.equals(attributes.get("brand"));
            })
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    // For better performance, cache products
    @Cacheable(value = "products", key = "#brand")
    public List<ProductDTO> findByBrandCached(String brand) {
        return findByBrand(brand);
    }
}
```

### JSONB Best Practices

1. **Use String type for JSONB fields in entities**
2. **Convert to/from objects in service layer**
3. **Use Jackson ObjectMapper for JSON processing**
4. **Test with H2 using JSON type instead of JSONB**
5. **Consider caching frequently queried JSONB data**
6. **Use native queries for complex JSONB operations**
7. **Validate JSON structure before saving to database**
8. **Handle null JSONB fields gracefully**

```java
// JSONB validation
public void validateProductAttributes(String attributes) {
    if (StringUtils.isBlank(attributes)) {
        return; // Allow null
    }

    try {
        Map<String, Object> map = JsonbUtils.fromJson(attributes,
            new TypeReference<Map<String, Object>>() {});

        // Validate required fields
        if (!map.containsKey("brand")) {
            throw new ValidationException("Brand is required");
        }

        // Validate field types
        Object warranty = map.get("warranty");
        if (warranty != null && !(warranty instanceof String)) {
            throw new ValidationException("Warranty must be a string");
        }
    } catch (Exception e) {
        throw new ValidationException("Invalid JSON format: " + e.getMessage());
    }
}
```