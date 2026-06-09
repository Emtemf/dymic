# DDD 领域驱动设计规范

> **版本**：V1.0
> **日期**：2026-06-09
> **来源**：Martin Fowler、Wikipedia DDD、Eric Evans《领域驱动设计》

---

## 一、核心概念

### 1.1 聚合（Aggregate）

**定义**：领域对象的集群，可以被视为一个单独的单元。

**特征**：
- 是领域概念（如订单），不是通用的集合类
- 是数据存储传输的基本元素
- 请求加载或保存时应针对整个聚合
- 事务不应跨越聚合边界

**示例**：
```
订单聚合：
├── Order（聚合根）
├── OrderItem（实体，聚合内部）
└── ShippingAddress（值对象，聚合内部）
```

### 1.2 聚合根（Aggregate Root）

**定义**：聚合中将有一个组件对象作为聚合根。

**特征**：
- 来自聚合外部的任何引用都应仅指向聚合根
- 根对象负责确保整个聚合的完整性
- 根对象检查聚合内变更的一致性
- 外部对象不允许持有对聚合内部对象的引用

**Java实现规范**：
```java
/**
 * 订单聚合根
 * 
 * ===== 领域统一业务语言 =====
 * 
 * 【状态流转】
 *   创建 → PENDING → CONFIRMED → SHIPPED → DELIVERED
 *                ↘ CANCELLED
 * 
 * 【聚合边界】
 * - Order是聚合根
 * - 包含实体：OrderItem
 * - 包含值对象：ShippingAddress、Money
 * 
 * 【业务规则】
 * 1. 订单总金额 = sum(订单项金额)
 * 2. 订单项数量 > 0
 * 3. 只有PENDING状态才能取消
 */
public class Order {  // 聚合根
    private OrderId id;              // 值对象：唯一标识
    private OrderStatus status;      // 值对象：状态
    private List<OrderItem> items;   // 实体：订单项（聚合内部）
    private ShippingAddress address; // 值对象：收货地址
    private Money totalAmount;       // 值对象：总金额
    
    private Order() {}  // 私有构造器，强制使用工厂方法
    
    // 工厂方法：创建订单
    public static Order create(CustomerId customerId, List<OrderItem> items) {
        Order order = new Order();
        order.id = OrderId.generate();
        order.status = OrderStatus.PENDING;
        order.items = new ArrayList<>(items);  // 防御性拷贝
        order.totalAmount = calculateTotal(items);
        return order;
    }
    
    // 业务方法：确认订单
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new BizException("只有待确认状态才能确认订单");
        }
        status = OrderStatus.CONFIRMED;
    }
    
    // 业务方法：取消订单
    public void cancel() {
        if (status != OrderStatus.PENDING) {
            throw new BizException("只有待确认状态才能取消订单");
        }
        status = OrderStatus.CANCELLED;
    }
    
    // 业务方法：添加订单项
    public void addItem(OrderItem item) {
        items.add(item);
        totalAmount = calculateTotal(items);  // 重新计算总金额
    }
    
    // 内部方法：计算总金额
    private static Money calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(OrderItem::getAmount)
            .reduce(Money.ZERO, Money::add);
    }
    
    // Getters（不暴露内部集合的可变性）
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);  // 返回不可变视图
    }
}
```

**关键原则**：
1. **私有构造器**：强制使用工厂方法创建，确保业务规则
2. **业务方法**：业务逻辑在聚合根内部，不暴露setter
3. **防御性拷贝**：不暴露内部集合的可变性
4. **状态流转**：在聚合根内部维护状态一致性

---

### 1.3 实体（Entity）

**定义**：由其身份标识定义的对象，而不是由其属性定义。

**特征**：
- 有唯一标识（ID）
- 有生命周期
- 可变性（属性可以改变）
- 相等性由ID决定，而非属性值

**Java实现规范**：
```java
/**
 * 订单项实体
 * 
 * 【身份标识】
 * - 由 OrderItemId 唯一标识
 * - 相等性由ID决定
 * 
 * 【生命周期】
 * - 随订单创建而创建
 * - 随订单删除而删除
 */
public class OrderItem {  // 实体（聚合内部）
    private OrderItemId id;        // 值对象：唯一标识
    private ProductId productId;   // 值对象：产品引用
    private int quantity;          // 属性：数量
    private Money price;           // 值对象：单价
    
    // 私有构造器
    private OrderItem() {}
    
    // 工厂方法
    public static OrderItem create(ProductId productId, int quantity, Money price) {
        if (quantity <= 0) {
            throw new BizException("数量必须大于0");
        }
        OrderItem item = new OrderItem();
        item.id = OrderItemId.generate();
        item.productId = productId;
        item.quantity = quantity;
        item.price = price;
        return item;
    }
    
    // 业务方法：修改数量
    public void changeQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new BizException("数量必须大于0");
        }
        this.quantity = newQuantity;
    }
    
    // 计算金额
    public Money getAmount() {
        return price.multiply(quantity);
    }
    
    // 相等性由ID决定
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem that = (OrderItem) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

**关键原则**：
1. **唯一标识**：有ID字段
2. **相等性**：由ID决定，而非属性
3. **业务方法**：业务逻辑在实体内部
4. **生命周期**：随聚合根创建/删除

---

### 1.4 值对象（Value Object）

**定义**：通过其属性的值来定义相等性的对象，无概念性身份。

**特征**：
- 无唯一标识（没有ID）
- 不可变（Immutable）
- 相等性由属性值决定
- 可自由替换（不影响业务）

**Java实现规范**：
```java
/**
 * 金额值对象
 * 
 * 【不可变性】
 * - 所有属性final
 * - 无setter方法
 * - 运算返回新实例
 * 
 * 【相等性】
 * - 由属性值决定
 */
public final class Money {  // 值对象
    private final BigDecimal amount;  // 金额
    private final Currency currency;  // 货币
    
    public static final Money ZERO = new Money(BigDecimal.ZERO, Currency.CNY);
    
    public Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }
    
    // 运算方法：返回新实例
    public Money add(Money other) {
        if (currency != other.currency) {
            throw new BizException("货币不一致");
        }
        return new Money(amount.add(other.amount), currency);
    }
    
    public Money multiply(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }
    
    // 相等性由属性值决定
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money that = (Money) o;
        return Objects.equals(amount, that.amount) 
            && Objects.equals(currency, that.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}

/**
 * 订单ID值对象
 */
public final class OrderId {  // 值对象（ID类型）
    private final Long value;
    
    public OrderId(Long value) {
        if (value == null || value <= 0) {
            throw new BizException("订单ID无效");
        }
        this.value = value;
    }
    
    public static OrderId generate() {
        return new OrderId(IdGenerator.nextId());
    }
    
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderId)) return false;
        OrderId that = (OrderId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

/**
 * 订单状态值对象（枚举）
 */
public enum OrderStatus {  // 值对象（枚举）
    PENDING("待确认"),
    CONFIRMED("已确认"),
    SHIPPED("已发货"),
    DELIVERED("已送达"),
    CANCELLED("已取消");
    
    private final String displayName;
    
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

**关键原则**：
1. **不可变**：所有属性final，无setter
2. **运算返回新实例**：add、multiply等方法返回新对象
3. **相等性**：由属性值决定
4. **无ID**：没有唯一标识

---

### 1.5 领域服务（Domain Service）

**定义**：当某部分功能在概念上不属于任何对象时，将其表达为服务。

**特征**：
- 无状态（Stateless）
- 表达领域概念
- 操作多个聚合
- 放在领域层

**使用场景**：
1. 跨聚合的业务操作
2. 复杂的业务规则
3. 不自然属于任何实体或值对象的操作

**Java实现规范**：
```java
/**
 * 订单领域服务
 * 
 * 【职责】
 * - 跨聚合操作（订单 + 库存 + 支付）
 * - 复杂业务规则
 * 
 * 【无状态】
 * - 不持有任何状态
 * - 依赖Repository接口
 */
@Service
public class OrderDomainService {
    
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentGateway paymentGateway;
    
    public OrderDomainService(
        OrderRepository orderRepository,
        InventoryRepository inventoryRepository,
        PaymentGateway paymentGateway
    ) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentGateway = paymentGateway;
    }
    
    /**
     * 下单（跨聚合操作）
     * 
     * @param order 订单聚合根
     * @param payment 支付信息
     */
    @Transactional
    public void placeOrder(Order order, PaymentInfo payment) {
        // 1. 检查库存（跨聚合）
        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId());
            if (!inventory.canReserve(item.getQuantity())) {
                throw new BizException("库存不足：" + item.getProductId());
            }
        }
        
        // 2. 扣减库存（跨聚合）
        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId());
            inventory.reserve(item.getQuantity());
            inventoryRepository.save(inventory);
        }
        
        // 3. 处理支付（外部系统）
        PaymentResult result = paymentGateway.process(payment);
        if (!result.isSuccess()) {
            throw new BizException("支付失败：" + result.getMessage());
        }
        
        // 4. 确认订单
        order.confirm();
        orderRepository.save(order);
    }
    
    /**
     * 计算订单折扣（复杂业务规则）
     */
    public Money calculateDiscount(Order order, Customer customer) {
        // 复杂的折扣计算逻辑
        Money baseAmount = order.getTotalAmount();
        
        // VIP客户折扣
        if (customer.isVIP()) {
            return baseAmount.multiply(0.1);  // 10%折扣
        }
        
        // 大额订单折扣
        if (baseAmount.compareTo(Money.of(1000)) > 0) {
            return baseAmount.multiply(0.05);  // 5%折扣
        }
        
        return Money.ZERO;
    }
}
```

**关键原则**：
1. **无状态**：不持有任何状态
2. **领域概念**：方法名表达业务含义
3. **跨聚合操作**：操作多个聚合
4. **依赖倒置**：依赖Repository接口，不依赖实现

---

### 1.6 Repository（仓储）

**定义**：用于从数据存储中检索领域对象的对象。

**特征**：
- 接口定义在领域层
- 实现放在基础设施层
- 按聚合根操作（加载/保存整个聚合）
- 隐藏数据访问细节

**Java实现规范**：
```java
// ===== 领域层：接口定义 =====
/**
 * 订单仓储接口
 * 
 * 【位置】领域层
 * 【职责】定义聚合根的持久化操作
 */
public interface OrderRepository {
    // 保存聚合根
    Order save(Order order);
    
    // 按ID查询聚合根
    Optional<Order> findById(OrderId id);
    
    // 按客户ID查询聚合根列表
    List<Order> findByCustomerId(CustomerId customerId);
    
    // 删除聚合根
    void delete(Order order);
}

// ===== 基础设施层：接口实现 =====
/**
 * 订单仓储实现
 * 
 * 【位置】基础设施层
 * 【职责】实现持久化操作，隐藏数据访问细节
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {
    
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderConverter orderConverter;  // MapStruct
    
    public OrderRepositoryImpl(
        OrderMapper orderMapper,
        OrderItemMapper orderItemMapper,
        OrderConverter orderConverter
    ) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderConverter = orderConverter;
    }
    
    @Override
    @Transactional
    public Order save(Order order) {
        // 1. 转换为Entity
        OrderEntity entity = orderConverter.toEntity(order);
        
        // 2. 保存订单主表
        if (entity.getId() == null) {
            orderMapper.insert(entity);
        } else {
            orderMapper.updateById(entity);
        }
        
        // 3. 保存订单项（聚合内部）
        for (OrderItem item : order.getItems()) {
            OrderItemEntity itemEntity = orderConverter.toItemEntity(item, entity.getId());
            if (itemEntity.getId() == null) {
                orderItemMapper.insert(itemEntity);
            } else {
                orderItemMapper.updateById(itemEntity);
            }
        }
        
        // 4. 返回聚合根
        return orderConverter.toDomain(entity);
    }
    
    @Override
    public Optional<Order> findById(OrderId id) {
        // 1. 查询订单主表
        OrderEntity entity = orderMapper.selectById(id.getValue());
        if (entity == null) {
            return Optional.empty();
        }
        
        // 2. 查询订单项（加载整个聚合）
        List<OrderItemEntity> itemEntities = orderItemMapper.selectByOrderId(id.getValue());
        
        // 3. 转换为聚合根
        Order order = orderConverter.toDomain(entity, itemEntities);
        return Optional.of(order);
    }
}
```

**关键原则**：
1. **接口在领域层**：domain.repository包
2. **实现在基础设施层**：infrastructure.repository包
3. **按聚合操作**：加载/保存整个聚合
4. **隐藏细节**：不暴露SQL、Entity等细节

---

## 二、分层架构

### 2.1 四层架构

```
┌─────────────────────────────────────────┐
│           Adapter Layer                 │  接口层（适配器）
│  Controller / Resource / Req / Rsp      │  - HTTP入口
│                                         │  - 参数校验
│                                         │  - DTO转换
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│          Application Layer              │  应用层（编排）
│  AppService / DTO / Converter           │  - 业务编排
│                                         │  - 事务边界
│                                         │  - DTO转换
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│            Domain Layer                 │  领域层（核心）
│  Aggregate / Entity / ValueObject       │  - 业务规则
│  DomainService / Repository接口         │  - 领域对象
│                                         │  - Repository接口
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│        Infrastructure Layer             │  基础设施层（技术）
│  RepositoryImpl / Mapper / Entity       │  - Repository实现
│  GatewayImpl / Config                   │  - 数据访问
│                                         │  - 外部系统
└─────────────────────────────────────────┘
```

### 2.2 各层职责

| 层 | 职责 | 允许 | 不允许 |
|----|------|------|--------|
| Adapter | HTTP入口、参数校验 | 调用ApplicationService | 包含业务逻辑 |
| Application | 业务编排、事务边界 | 调用DomainService/Repository | 直接访问数据库 |
| Domain | 业务规则、领域对象 | 定义Repository接口 | 依赖基础设施 |
| Infrastructure | 技术实现 | 实现Repository接口 | 包含业务逻辑 |

### 2.3 依赖方向

```
Adapter → Application → Domain ← Infrastructure
```

**关键原则**：
- **依赖倒置**：Domain定义接口，Infrastructure实现
- **单向依赖**：上层依赖下层，下层不依赖上层
- **领域层独立**：不依赖任何技术框架

---

## 三、MapStruct 转换规范

### 3.1 转换方向

```
Adapter层：Req → DTO
Application层：DTO ↔ Domain
Infrastructure层：Domain ↔ Entity
```

### 3.2 MapStruct 配置

```java
/**
 * 订单转换器（基础设施层）
 * 
 * 【职责】Domain ↔ Entity 转换
 * 【位置】infrastructure.convert包
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderConverter {
    
    OrderConverter INSTANCE = Mappers.getMapper(OrderConverter.class);
    
    // Domain → Entity
    @Mapping(target = "orderItems", ignore = true)  // 单独处理
    OrderEntity toEntity(Order domain);
    
    // Entity → Domain（需要reconstitute）
    default Order toDomain(OrderEntity entity, List<OrderItemEntity> itemEntities) {
        return Order.reconstitute(
            new OrderId(entity.getId()),
            OrderStatus.valueOf(entity.getStatus()),
            // ... 其他字段
        );
    }
    
    // 值对象映射
    default Money toMoney(String amount, String currency) {
        return new Money(new BigDecimal(amount), Currency.valueOf(currency));
    }
    
    default String toAmount(Money money) {
        return money.getAmount().toString();
    }
}
```

---

## 四、状态流转规范

### 4.1 状态流转文档模板

每个聚合根必须有状态流转说明：

```java
/**
 * 订单聚合根
 * 
 * ===== 领域统一业务语言 =====
 * 
 * 【状态流转】
 *   创建 → PENDING → CONFIRMED → SHIPPED → DELIVERED
 *                ↘ CANCELLED
 *   
 * 【状态转换规则】
 * - PENDING → CONFIRMED：确认订单（用户操作）
 * - PENDING → CANCELLED：取消订单（用户操作）
 * - CONFIRMED → SHIPPED：发货（系统操作）
 * - SHIPPED → DELIVERED：送达（系统操作）
 * 
 * 【业务规则】
 * 1. 只有PENDING状态才能取消
 * 2. 只有PENDING状态才能确认
 * 3. 订单项数量 > 0
 * 4. 总金额 = sum(订单项金额)
 * 
 * 【聚合边界】
 * - Order是聚合根
 * - 包含实体：OrderItem
 * - 包含值对象：ShippingAddress、Money
 */
public class Order {
    // ...
}
```

### 4.2 状态枚举

```java
/**
 * 订单状态值对象
 */
public enum OrderStatus {
    PENDING("待确认"),
    CONFIRMED("已确认"),
    SHIPPED("已发货"),
    DELIVERED("已送达"),
    CANCELLED("已取消");
    
    private final String displayName;
    
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    // 状态转换规则
    public boolean canConfirm() {
        return this == PENDING;
    }
    
    public boolean canCancel() {
        return this == PENDING;
    }
    
    public boolean canShip() {
        return this == CONFIRMED;
    }
}
```

---

## 五、业务方法契约

### 5.1 契约规范

每个业务方法必须有前置条件和后置条件说明：

```java
/**
 * 确认订单
 * 
 * 【前置条件】
 * - status == PENDING
 * - items.size() > 0
 * 
 * 【后置条件】
 * - status == CONFIRMED
 * - confirmTime != null
 * 
 * 【不变量】
 * - totalAmount不变
 * - items不变
 * 
 * @throws BizException 如果状态不是PENDING
 */
public void confirm() {
    if (status != OrderStatus.PENDING) {
        throw new BizException("只有待确认状态才能确认订单");
    }
    this.status = OrderStatus.CONFIRMED;
    this.confirmTime = OffsetDateTime.now();
}
```

---

## 六、检查清单

### 6.1 聚合根检查清单

- [ ] 有状态流转说明
- [ ] 有聚合边界说明
- [ ] 有业务规则说明
- [ ] 私有构造器 + 工厂方法
- [ ] 业务方法有契约说明
- [ ] 不暴露内部集合可变性
- [ ] equals/hashCode由ID决定

### 6.2 实体检查清单

- [ ] 有唯一标识（ID）
- [ ] 有生命周期说明
- [ ] equals/hashCode由ID决定
- [ ] 业务方法在实体内部

### 6.3 值对象检查清单

- [ ] 无ID字段
- [ ] 不可变（所有属性final）
- [ ] 无setter方法
- [ ] 运算返回新实例
- [ ] equals/hashCode由属性值决定

### 6.4 领域服务检查清单

- [ ] 无状态
- [ ] 方法名表达业务含义
- [ ] 操作多个聚合
- [ ] 依赖Repository接口

### 6.5 Repository检查清单

- [ ] 接口在领域层
- [ ] 实现在基础设施层
- [ ] 按聚合根操作
- [ ] 隐藏数据访问细节

---

## 七、参考资料

- [Martin Fowler - DDD Aggregate](https://martinfowler.com/bliki/DDD_Aggregate.html)
- [Martin Fowler - Value Object](https://martinfowler.com/bliki/ValueObject.html)
- [Wikipedia - Domain-driven design](https://en.wikipedia.org/wiki/Domain-driven_design)
- Eric Evans《领域驱动设计：软件核心复杂性应对之道》
