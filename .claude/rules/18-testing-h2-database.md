---
name: testing-h2-database
description: H2数据库配置规则
---

# H2数据库配置规则

## 核心原则

**测试数据库**：使用H2内存数据库进行Repository层测试，MySQL兼容模式。

---

## H2数据库特点

### 优势
- 轻量级内存数据库，启动快速
- 无需安装，零配置
- 支持MySQL兼容模式
- 自动建表，测试后自动清理

### 限制
- 不支持所有MySQL特性
- 部分函数行为不同
- 需要注意兼容性配置

---

## 配置文件

### application-test.yml
```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    username: sa
    password:
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  sql:
    init:
      mode: always
      schema-locations: classpath:schema-h2.sql
      data-locations: classpath:data-h2.sql
  
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### H2 URL参数说明

| 参数 | 说明 | 推荐值 |
|-----|------|-------|
| `MODE=MySQL` | MySQL兼容模式 | 必须 |
| `DATABASE_TO_LOWER=TRUE` | 数据库名小写 | 推荐 |
| `CASE_INSENSITIVE_IDENTIFIERS=TRUE` | 标识符大小写不敏感 | 推荐 |
| `DB_CLOSE_DELAY=-1` | 延迟关闭 | 必须 |
| `DB_CLOSE_ON_EXIT=FALSE` | 退出时不关闭 | 推荐 |

---

## Schema脚本

### schema-h2.sql
```sql
-- 模板表
CREATE TABLE IF NOT EXISTS t_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(50) NOT NULL UNIQUE,
    template_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version INT NOT NULL DEFAULT 1,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(50),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 版本表
CREATE TABLE IF NOT EXISTS t_template_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    version_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    config_json TEXT,
    published_time DATETIME,
    published_by VARCHAR(50),
    deleted TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(50),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES t_template(id)
);

-- 合同表
CREATE TABLE IF NOT EXISTS t_contract (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_code VARCHAR(50) NOT NULL UNIQUE,
    contract_name VARCHAR(100) NOT NULL,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT,
    contract_data JSON,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(50),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES t_template(id)
);

-- 创建索引
CREATE INDEX idx_template_code ON t_template(template_code);
CREATE INDEX idx_template_status ON t_template(status);
CREATE INDEX idx_version_template_id ON t_template_version(template_id);
CREATE INDEX idx_contract_template_id ON t_contract(template_id);
```

---

## Data脚本

### data-h2.sql
```sql
-- 初始化测试数据
INSERT INTO t_template (template_code, template_name, description, status, version, create_by)
VALUES 
    ('TPL001', '销售合同模板', '销售合同模板描述', 'PUBLISHED', 1, 'admin'),
    ('TPL002', '采购合同模板', '采购合同模板描述', 'DRAFT', 1, 'admin'),
    ('TPL003', '租赁合同模板', '租赁合同模板描述', 'PUBLISHED', 1, 'admin');

INSERT INTO t_template_version (template_id, version_number, version_status, config_json, create_by)
VALUES 
    (1, 1, 'PUBLISHED', '{"fields": []}', 'admin'),
    (2, 1, 'DRAFT', '{"fields": []}', 'admin'),
    (3, 1, 'PUBLISHED', '{"fields": []}', 'admin');
```

---

## 测试配置类

### Repository测试基类
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"})
public abstract class RepositoryTestBase {
    
    @Autowired
    protected DataSource dataSource;
    
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    
    @BeforeEach
    void setUp() {
        // 每个测试前清理数据
        cleanTestData();
    }
    
    @AfterEach
    void tearDown() {
        // 每个测试后清理数据
        cleanTestData();
    }
    
    protected void cleanTestData() {
        jdbcTemplate.execute("DELETE FROM t_contract");
        jdbcTemplate.execute("DELETE FROM t_template_version");
        jdbcTemplate.execute("DELETE FROM t_template WHERE template_code LIKE 'TEST_%'");
    }
    
    protected void executeSql(String sql) {
        jdbcTemplate.execute(sql);
    }
    
    protected <T> T queryForObject(String sql, Class<T> type) {
        return jdbcTemplate.queryForObject(sql, type);
    }
}
```

---

## 兼容性注意事项

### MySQL与H2差异

| 特性 | MySQL | H2 | 解决方案 |
|-----|-------|-----|---------|
| 自增主键 | AUTO_INCREMENT | AUTO_INCREMENT | 兼容 |
| JSON类型 | JSON | JSON/TEXT | 使用TEXT或配置 |
| 布尔类型 | BOOLEAN/TINYINT | BOOLEAN | 使用TINYINT |
| 日期函数 | NOW() | NOW() | 兼容 |
| 字符串连接 | CONCAT() | CONCAT() | 兼容 |
| LIMIT语法 | LIMIT n | LIMIT n | 兼容 |
| 分页 | LIMIT n OFFSET m | LIMIT n OFFSET m | 兼容 |

### 不兼容场景处理

```sql
-- MySQL特有函数，需要转换
-- MySQL: IFNULL(a, b)
-- H2: IFNULL(a, b) 或 COALESCE(a, b)
SELECT IFNULL(name, 'default') FROM t_template;

-- MySQL: DATE_FORMAT(date, '%Y-%m-%d')
-- H2: FORMATDATETIME(date, 'yyyy-MM-dd')
SELECT FORMATDATETIME(create_time, 'yyyy-MM-dd') FROM t_template;
```

---

## H2控制台访问

### 启用H2控制台
```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

### 访问方式
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
User: sa
Password: (空)
```

---

## 验证清单

### H2配置必须验证
- [ ] 数据源配置正确
- [ ] MySQL兼容模式启用
- [ ] Schema脚本执行成功
- [ ] 测试数据初始化成功
- [ ] 测试后数据清理成功
- [ ] 兼容性问题已处理

---

## 详细规则引用

- Repository层验证：查看 `13-testing-repository.md`
- Mock策略：查看 `19-testing-mock-strategy.md`
- Service层验证：查看 `14-testing-service.md`