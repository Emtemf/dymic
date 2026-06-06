---
name: architecture-details
description: Detailed architectural rules for dependency inversion, object transformation, transaction boundaries, and inter-layer communication
---

# Architecture Details

## Dependency Inversion Principle

### Why Dependency Inversion

**Core Rationale:**
- Decouples high-level business logic from low-level implementation details
- Enables easy testing by mocking dependencies
- Facilitates component replacement without modifying business logic
- Improves code maintainability and scalability

**Implementation Rules:**

```java
// CORRECT: High-level module depends on abstraction
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
}

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}

// WRONG: High-level module depends on concrete implementation
@Service
public class OrderService {
    private final OrderRepositoryImpl orderRepository = new OrderRepositoryImpl();
}
```

**Mandatory Abstractions:**
1. Repository interfaces for all data access
2. Service interfaces for complex business logic
3. External API client interfaces
4. File storage interfaces
5. Message queue interfaces

### Dependency Injection Rules

```java
// Constructor injection (PREFERRED)
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
}

// Field injection (NOT ALLOWED)
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository; // ❌ FORBIDDEN
}
```

---

## Object Transformation Rules

### MapStruct Configuration

**Maven Dependencies (pom.xml):**

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

**Mapper Interface Definition:**

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

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(UserCreateDTO dto);

    @Mapping(target = "password", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(UserUpdateDTO dto, @MappingTarget User entity);

    List<UserDTO> toDTOList(List<User> entities);
}
```

### Transformation Direction Rules

**Mandatory Transformation Paths:**

```
Controller Layer:
  Request → DTO → Service
  Service → DTO → Response

Service Layer:
  DTO → Entity → Repository
  Repository → Entity → DTO

External API:
  External Response → DTO → Service
  Service → DTO → External Request
```

**Transformation Rules:**

1. **Inbound (Controller → Service):**
   - Always validate DTO before transformation
   - Use `@Valid` annotation on DTO parameters
   - Convert DTO to Entity ONLY in Service layer

2. **Outbound (Service → Controller):**
   - Never expose Entity to Controller
   - Always convert Entity to DTO
   - Use DTO for pagination responses

3. **Entity-to-Entity:**
   - NEVER convert between entities
   - Use DTO for complex object graphs
   - Use Entity only for persistence operations

**Forbidden Patterns:**

```java
// ❌ NEVER expose Entity directly to Controller
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) { // User is Entity
    return userService.findById(id);
}

// ✅ CORRECT: Always use DTO
@GetMapping("/users/{id}")
public UserDTO getUser(@PathVariable Long id) {
    return userService.findById(id);
}
```

---

## Transaction Boundaries

### When to Use @Transactional

**Mandatory Transaction Annotation:**

```java
// 1. Service methods that modify data
@Service
public class OrderService {
    @Transactional
    public OrderDTO createOrder(OrderCreateDTO dto) {
        // Multiple database operations
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        // Update order status, refund payment, notify user
    }
}

// 2. Methods with multiple repository calls
@Transactional
public void transferStock(Long fromWarehouseId, Long toWarehouseId, Long productId, int quantity) {
    warehouseRepository.decreaseStock(fromWarehouseId, productId, quantity);
    warehouseRepository.increaseStock(toWarehouseId, productId, quantity);
    logRepository.saveTransferLog(fromWarehouseId, toWarehouseId, productId, quantity);
}
```

**When NOT to Use @Transactional:**

```java
// 1. Read-only methods (unless you need consistent snapshot)
public UserDTO findById(Long id) {
    return userRepository.findById(id)
        .map(userMapper::toDTO)
        .orElseThrow(() -> new NotFoundException("User not found"));
}

// 2. Simple single-repository calls
public List<UserDTO> findAll() {
    return userMapper.toDTOList(userRepository.findAll());
}

// 3. Methods calling external APIs only
public void sendNotification(Long userId, String message) {
    UserDTO user = findById(userId);
    emailService.send(user.getEmail(), message);
}
```

### Transaction Propagation Rules

```java
// DEFAULT: REQUIRED (use existing or create new)
@Transactional
public void processOrder(Long orderId) {
    validateOrder(orderId);
    updateInventory(orderId);
    processPayment(orderId);
}

// REQUIRES_NEW: Always create new transaction (for audit logs)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void saveAuditLog(AuditLog log) {
    auditLogRepository.save(log);
}

// NESTED: For optional sub-operations
@Transactional
public void placeOrder(OrderDTO dto) {
    Order order = createOrder(dto);

    @Transactional(propagation = Propagation.NESTED)
    void reserveInventory(Long orderId) {
        inventoryService.reserve(orderId);
    }
}
```

### Transaction Isolation Levels

```java
// Use READ_COMMITTED for most cases
@Transactional(isolation = Isolation.READ_COMMITTED)
public void updateBalance(Long userId, BigDecimal amount) {
    User user = userRepository.findById(userId);
    user.setBalance(user.getBalance().add(amount));
    userRepository.save(user);
}

// Use SERIALIZABLE for critical financial operations
@Transactional(isolation = Isolation.SERIALIZABLE)
public void transferMoney(Long fromUserId, Long toUserId, BigDecimal amount) {
    decreaseBalance(fromUserId, amount);
    increaseBalance(toUserId, amount);
}
```

---

## Inter-Layer Communication Rules

### DTO Passing Rules

**Between Controller and Service:**

```java
// Controller Layer
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        OrderDTO created = orderService.createOrder(dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        OrderDTO order = orderService.findById(id);
        return ResponseEntity.ok(order);
    }
}

// Service Layer
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderDTO createOrder(OrderCreateDTO dto) {
        validateOrder(dto);
        Order order = orderMapper.toEntity(dto);
        Order saved = orderRepository.save(order);
        return orderMapper.toDTO(saved);
    }

    public OrderDTO findById(Long id) {
        return orderRepository.findById(id)
            .map(orderMapper::toDTO)
            .orElseThrow(() -> new NotFoundException("Order not found"));
    }
}
```

**DTO Naming Conventions:**

```java
// Create operations
public class UserCreateDTO {
    @NotBlank
    private String username;

    @Email
    private String email;

    @NotBlank
    private String password;
}

// Update operations
public class UserUpdateDTO {
    @Email
    private String email;

    @Size(min = 8, max = 100)
    private String password; // Optional for updates
}

// Response operations
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    // Never include password in response
}

// List/Search operations
public class UserQueryDTO {
    private String username;
    private String email;
    private Integer page = 0;
    private Integer size = 20;
}
```

### Exception Passing Rules

**Exception Handling Strategy:**

```java
// 1. Define custom exceptions in domain layer
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

// 2. Throw exceptions in Service layer
@Service
public class UserService {
    public UserDTO findById(Long id) {
        return userRepository.findById(id)
            .map(userMapper::toDTO)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessException("USER_ALREADY_ACTIVE", "User is already active");
        }

        user.setStatus(UserStatus.ACTIVE);
    }
}

// 3. Handle exceptions in Controller layer
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        ErrorResponse error = new ErrorResponse("NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse error = new ErrorResponse(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}

// 4. Standard error response format
public class ErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    // constructors, getters
}
```

**Exception Flow Rules:**

1. **Repository Layer:**
   - Catch only database-specific exceptions (DataAccessException)
   - Re-throw as domain exceptions
   - Never expose database details to upper layers

2. **Service Layer:**
   - Throw domain exceptions (NotFoundException, BusinessException)
   - Never catch and swallow exceptions silently
   - Log exceptions before re-throwing if necessary

3. **Controller Layer:**
   - Never catch business exceptions
   - Use @RestControllerAdvice for global handling
   - Return appropriate HTTP status codes

**HTTP Status Code Mapping:**

```java
// NotFoundException → 404 NOT_FOUND
// BusinessException → 400 BAD_REQUEST
// ValidationException → 400 BAD_REQUEST
// DataAccessException → 500 INTERNAL_SERVER_ERROR
// AuthenticationException → 401 UNAUTHORIZED
// AccessDeniedException → 403 FORBIDDEN
```

---

## Validation Rules

### DTO Validation Annotations

```java
public class OrderCreateDTO {
    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 200)
    private String shippingAddress;

    @NotEmpty
    private List<@Valid OrderItemDTO> items;

    @Pattern(regexp = "^(CASH|CREDIT_CARD|DEBIT_CARD)$")
    private String paymentMethod;
}

public class OrderItemDTO {
    @NotNull
    private Long productId;

    @Min(1)
    @Max(100)
    private Integer quantity;

    @DecimalMin(value = "0.01")
    private BigDecimal price;
}
```

### Custom Validators

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface Phone {
    String message() default "Invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || PHONE_PATTERN.matcher(value).matches();
    }
}
```