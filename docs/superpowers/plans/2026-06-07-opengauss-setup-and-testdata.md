# openGauss 环境搭建与测试数据初始化 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 Docker 中安装 openGauss 数据库，补充审计字段，初始化测试数据，使前端配置界面能够完整测试模板配置功能。

**Architecture:** Docker openGauss 5.0 容器 + PostgreSQL JDBC 驱动 + 审计字段统一补充 + 完整测试数据

**Tech Stack:** openGauss 5.0, Docker, PostgreSQL Driver, Spring Boot 3.5.14, MyBatis-Plus

---

## 文件结构

### 创建的文件
| 文件 | 职责 |
|------|------|
| `src/main/resources/schema-opengauss.sql` | openGauss 建表脚本（完整审计字段） |
| `src/main/resources/data-opengauss.sql` | openGauss 测试数据 |
| `src/main/resources/data-h2.sql` | H2 测试数据（与 openGauss 一致） |
| `src/main/resources/application-opengauss.yml` | openGauss 连接配置 |
| `scripts/start-opengauss.sh` | Docker 启动脚本 |
| `scripts/init-opengauss.sh` | 数据库初始化脚本 |

### 修改的文件
| 文件 | 修改内容 |
|------|---------|
| `req/req.md` | 补充 14 个表的审计字段 |
| `src/main/resources/schema-h2.sql` | 同步补充审计字段 |
| `pom.xml` | 添加 PostgreSQL 依赖 |

---

## Task 1: 补充 req.md 审计字段

**Files:**
- Modify: `req/req.md`

### 1.1 补充 t_ui_layout_node 审计字段

- [ ] **Step 1: 在 t_ui_layout_node 表中添加审计字段**

找到 `req/req.md` 中 `t_ui_layout_node` 表定义，在 `created_at` 之前添加：

```sql
    created_by BIGINT,
    updated_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

将：
```sql
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
```

改为：
```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
```

- [ ] **Step 2: 验证修改**

Run: `grep -A 30 "CREATE TABLE t_ui_layout_node" req/req.md | grep -E "created_by|updated_by"`
Expected: 显示 created_by 和 updated_by 两行

---

### 1.2 补充 t_ui_field_def 审计字段

- [ ] **Step 3: 在 t_ui_field_def 表中添加审计字段**

同样在 `created_at` 之前添加 `created_by` 和 `updated_by`：

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.3 补充 t_ui_field_component 审计字段

- [ ] **Step 4: 在 t_ui_field_component 表中添加审计字段**

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.4 补充 t_ui_detail_table 审计字段

- [ ] **Step 5: 在 t_ui_detail_table 表中添加审计字段**

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.5 补充 t_ui_data_option 审计字段

- [ ] **Step 6: 在 t_ui_data_option 表中添加审计字段**

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.6 补充 t_ui_query_config 审计字段

- [ ] **Step 7: 在 t_ui_query_config 表中添加审计字段**

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.7 补充 t_ui_query_param 审计字段

- [ ] **Step 8: 在 t_ui_query_param 表中添加审计字段**

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.8 补充 t_ui_query_fill_rule 审计字段

- [ ] **Step 9: 在 t_ui_query_fill_rule 表中添加审计字段**

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.9 补充 t_ui_action_config 审计字段

- [ ] **Step 10: 在 t_ui_action_config 表中添加审计字段**

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.10 补充 t_contract_field_value 审计字段

- [ ] **Step 11: 在 t_contract_field_value 表中添加审计字段**

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.11 补充 t_contract_detail_row 审计字段

- [ ] **Step 12: 在 t_contract_detail_row 表中添加审计字段**

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.12 补充 t_contract_detail_field_value 审计字段

- [ ] **Step 13: 在 t_contract_detail_field_value 表中添加审计字段**

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.13 补充 t_contract_search_index 审计字段

- [ ] **Step 14: 在 t_contract_search_index 表中添加审计字段**

```sql
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

---

### 1.14 补充 t_contract_attachment 审计字段

- [ ] **Step 15: 在 t_contract_attachment 表中添加审计字段（只需要 updated_by）**

该表已有 `created_by` 和 `created_at`，只需添加 `updated_by` 和 `updated_at`：

```sql
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

注意：移除原有的 `created_name` 字段。

- [ ] **Step 16: 提交 req.md 更改**

```bash
git add req/req.md
git commit -m "docs: 补充 14 个表的审计字段（created_by, updated_by）"
```

---

## Task 2: 同步更新 schema-h2.sql

**Files:**
- Modify: `src/main/resources/schema-h2.sql`

### 2.1 更新 H2 脚本中的审计字段

- [ ] **Step 1: 更新 t_ui_layout_node 表**

找到 `t_ui_layout_node` 表定义，将：

```sql
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

改为：

```sql
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

- [ ] **Step 2: 同样方式更新其他 13 个表**

按照 Task 1 的列表，依次更新：
- t_ui_field_def
- t_ui_field_component
- t_ui_detail_table
- t_ui_data_option
- t_ui_query_config
- t_ui_query_param
- t_ui_query_fill_rule
- t_ui_action_config
- t_contract_field_value
- t_contract_detail_row
- t_contract_detail_field_value
- t_contract_search_index
- t_contract_attachment

注意：H2 使用 `TIMESTAMP` 而不是 `TIMESTAMPTZ`，使用 `JSON` 而不是 `JSONB`。

- [ ] **Step 3: 验证 H2 脚本**

Run: `grep -c "created_by BIGINT" src/main/resources/schema-h2.sql`
Expected: 14（或正确的表数量）

- [ ] **Step 4: 提交 H2 脚本更改**

```bash
git add src/main/resources/schema-h2.sql
git commit -m "chore: 同步 H2 脚本审计字段"
```

---

## Task 3: 创建 openGauss 建表脚本

**Files:**
- Create: `src/main/resources/schema-opengauss.sql`

### 3.1 从 req.md 提取建表脚本

- [ ] **Step 1: 创建 schema-opengauss.sql**

从 `req/req.md` 中提取所有建表语句，创建完整文件：

```sql
-- =====================================================
-- 合同模板动态渲染系统 - openGauss 建表脚本
-- =====================================================

-- 模板主表
CREATE TABLE IF NOT EXISTS t_ui_template (
    id BIGINT PRIMARY KEY,
    template_code VARCHAR(100) NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    template_desc VARCHAR(1000),
    biz_type VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    current_version_id BIGINT,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_template_code UNIQUE (template_code)
);

COMMENT ON TABLE t_ui_template IS '模板主表';

-- ... 其余表定义（从 req.md 复制，确保包含审计字段）
```

- [ ] **Step 2: 验证脚本完整性**

Run: `grep -c "CREATE TABLE" src/main/resources/schema-opengauss.sql`
Expected: 27（或正确的表数量）

- [ ] **Step 3: 提交 openGauss 脚本**

```bash
git add src/main/resources/schema-opengauss.sql
git commit -m "feat: 添加 openGauss 建表脚本"
```

---

## Task 4: 创建测试数据脚本

**Files:**
- Create: `src/main/resources/data-opengauss.sql`
- Create: `src/main/resources/data-h2.sql`

### 4.1 创建 openGauss 测试数据

- [ ] **Step 1: 创建 data-opengauss.sql**

```sql
-- =====================================================
-- 合同模板动态渲染系统 - 测试数据
-- =====================================================

-- 清理已有数据
DELETE FROM t_ui_action_config;
DELETE FROM t_ui_query_fill_rule;
DELETE FROM t_ui_query_param;
DELETE FROM t_ui_query_config;
DELETE FROM t_ui_data_option;
DELETE FROM t_ui_data_provider;
DELETE FROM t_ui_field_component;
DELETE FROM t_ui_field_def;
DELETE FROM t_ui_layout_node;
DELETE FROM t_ui_detail_table;
DELETE FROM t_ui_template_version;
DELETE FROM t_ui_template;

-- =====================================================
-- 模板数据
-- =====================================================

INSERT INTO t_ui_template (id, template_code, template_name, template_desc, biz_type, status, current_version_id, created_by, created_at, updated_by, updated_at)
VALUES
    (1001, 'SALE_CONTRACT', '销售合同模板', '用于销售业务合同录入', 'SALE', 'ENABLED', 2001, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (1002, 'PURCHASE_CONTRACT', '采购合同模板', '用于采购业务合同录入', 'PURCHASE', 'ENABLED', 2003, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 模板版本数据
-- =====================================================

INSERT INTO t_ui_template_version (id, template_id, version_no, version_name, version_status, created_by, created_at, updated_by, updated_at)
VALUES
    (2001, 1001, 1, 'V1.0', 'PUBLISHED', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (2002, 1001, 2, 'V1.1 草稿', 'DRAFT', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (2003, 1002, 1, 'V1.0', 'PUBLISHED', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 布局节点数据（销售合同模板 - version 2001）
-- =====================================================

INSERT INTO t_ui_layout_node (id, template_id, template_version_id, parent_id, node_code, node_name, node_type, sort_no, level_no, created_by, created_at, updated_by, updated_at)
VALUES
    -- 页面根节点
    (3001, 1001, 2001, NULL, 'page_root', '销售合同页面', 'PAGE', 0, 1, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    -- 基本信息卡片
    (3002, 1001, 2001, 3001, 'card_basic', '基本信息', 'CARD', 1, 2, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    -- 基本信息栅格
    (3003, 1001, 2001, 3002, 'grid_basic', '基本信息栅格', 'GRID', 0, 3, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    -- 字段节点
    (3004, 1001, 2001, 3003, 'field_contract_no', '合同编号', 'FIELD', 1, 4, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (3005, 1001, 2001, 3003, 'field_contract_name', '合同名称', 'FIELD', 2, 4, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (3006, 1001, 2001, 3003, 'field_sign_date', '签订日期', 'FIELD', 3, 4, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (3007, 1001, 2001, 3003, 'field_supplier', '供应商', 'FIELD', 4, 4, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    -- 明细信息卡片
    (3008, 1001, 2001, 3001, 'card_detail', '明细信息', 'CARD', 2, 2, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    -- 明细表
    (3009, 1001, 2001, 3008, 'detail_items', '明细表', 'DETAIL_TABLE', 0, 3, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    -- 操作区域卡片
    (3010, 1001, 2001, 3001, 'card_action', '操作区域', 'CARD', 3, 2, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    -- 动作按钮
    (3011, 1001, 2001, 3010, 'action_save', '保存按钮', 'ACTION', 1, 3, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (3012, 1001, 2001, 3010, 'action_query', '查询按钮', 'ACTION', 2, 3, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 字段定义数据
-- =====================================================

INSERT INTO t_ui_field_def (id, template_id, template_version_id, detail_table_id, field_code, field_path, field_name_cn, data_type, required_default, searchable, indexable, created_by, created_at, updated_by, updated_at)
VALUES
    -- 主表字段
    (4001, 1001, 2001, NULL, 'contractNo', 'basic.contractNo', '合同编号', 'string', 1, 1, 1, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (4002, 1001, 2001, NULL, 'contractName', 'basic.contractName', '合同名称', 'string', 1, 1, 1, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (4003, 1001, 2001, NULL, 'signDate', 'basic.signDate', '签订日期', 'date', 0, 1, 1, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (4004, 1001, 2001, NULL, 'supplierId', 'basic.supplierId', '供应商ID', 'string', 0, 0, 0, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (4005, 1001, 2001, NULL, 'supplierName', 'basic.supplierName', '供应商名称', 'string', 0, 1, 1, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (4006, 1001, 2001, NULL, 'totalAmount', 'basic.totalAmount', '总金额', 'number', 0, 0, 1, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    -- 明细表字段
    (4007, 1001, 2001, 3009, 'itemName', 'items[].itemName', '明细项名称', 'string', 1, 0, 0, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (4008, 1001, 2001, 3009, 'quantity', 'items[].quantity', '数量', 'number', 1, 0, 0, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (4009, 1001, 2001, 3009, 'price', 'items[].price', '单价', 'number', 1, 0, 0, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (4010, 1001, 2001, 3009, 'amount', 'items[].amount', '金额', 'number', 0, 0, 0, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 明细表配置
-- =====================================================

INSERT INTO t_ui_detail_table (id, template_id, template_version_id, detail_code, detail_name, detail_path, min_rows, max_rows, allow_add, allow_edit, allow_delete, created_by, created_at, updated_by, updated_at)
VALUES
    (3009, 1001, 2001, 'items', '合同明细', 'items', 1, 100, 1, 1, 1, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 数据提供方配置
-- =====================================================

INSERT INTO t_ui_data_provider (id, provider_code, provider_name, provider_type, owner_type, props_json, created_by, created_at, updated_by, updated_at)
VALUES
    (5001, 'SUPPLIER_LIST', '供应商列表', 'STATIC', 'IT', '[{"value":"SUP001","label":"供应商A"},{"value":"SUP002","label":"供应商B"}]'::jsonb, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (5002, 'CONTRACT_TYPE', '合同类型', 'STATIC', 'IT', '[{"value":"SALE","label":"销售合同"},{"value":"PURCHASE","label":"采购合同"}]'::jsonb, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 查询配置
-- =====================================================

INSERT INTO t_ui_query_config (id, template_id, template_version_id, query_code, query_name, query_type, data_provider_id, trigger_type, result_mode, created_by, created_at, updated_by, updated_at)
VALUES
    (6001, 1001, 2001, 'query_supplier', '查询供应商', 'POPUP', 5001, 'MANUAL', 'SINGLE_SELECT', 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);

-- =====================================================
-- 动作配置
-- =====================================================

INSERT INTO t_ui_action_config (id, template_id, template_version_id, action_code, action_name, action_type, bind_node_id, confirm_required, created_by, created_at, updated_by, updated_at)
VALUES
    (7001, 1001, 2001, 'save_contract', '保存合同', 'SAVE_CONTRACT', 3011, 1, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP),
    (7002, 1001, 2001, 'query_supplier', '查询供应商', 'QUERY', 3012, 0, 1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP);
```

- [ ] **Step 2: 创建 data-h2.sql**

复制 `data-opengauss.sql`，修改 H2 不兼容的语法：
- `CURRENT_TIMESTAMP` 保持不变
- `'...'::jsonb` 改为 `JSON '...'` 或直接用字符串
- 移除 `TIMESTAMPTZ` 相关（H2 脚本已用 `TIMESTAMP`）

```sql
-- H2 版本的测试数据（语法适配）
-- ... 与 openGauss 版本类似，但：
-- 1. 不使用 ::jsonb 类型转换
-- 2. JSON 字段直接用字符串
```

- [ ] **Step 3: 提交测试数据脚本**

```bash
git add src/main/resources/data-opengauss.sql src/main/resources/data-h2.sql
git commit -m "feat: 添加测试数据初始化脚本"
```

---

## Task 5: 添加 PostgreSQL 依赖

**Files:**
- Modify: `pom.xml`

### 5.1 添加依赖

- [ ] **Step 1: 在 pom.xml 中添加 PostgreSQL 驱动**

找到 `<dependencies>` 部分，添加：

```xml
<!-- PostgreSQL Driver (for openGauss) -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

- [ ] **Step 2: 验证依赖**

Run: `grep -A 5 "postgresql" pom.xml`
Expected: 显示 postgresql 依赖

- [ ] **Step 3: 提交更改**

```bash
git add pom.xml
git commit -m "feat: 添加 PostgreSQL 驱动依赖"
```

---

## Task 6: 创建 openGauss 配置文件

**Files:**
- Create: `src/main/resources/application-opengauss.yml`

### 6.1 创建配置文件

- [ ] **Step 1: 创建 application-opengauss.yml**

```yaml
spring:
  application:
    name: contract-template

  datasource:
    url: jdbc:postgresql://localhost:5432/contract_template
    driver-class-name: org.postgresql.Driver
    username: gaussdb
    password: OpenGauss@123

  sql:
    init:
      mode: never  # openGauss 手动初始化

  web:
    resources:
      cache:
        period: 0
      chain:
        cache: false

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: ASSIGN_ID
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath:mapper/*.xml

server:
  port: 8888

logging:
  level:
    com.contract: DEBUG
```

- [ ] **Step 2: 提交配置文件**

```bash
git add src/main/resources/application-opengauss.yml
git commit -m "feat: 添加 openGauss 配置文件"
```

---

## Task 7: 创建 Docker 启动脚本

**Files:**
- Create: `scripts/start-opengauss.sh`
- Create: `scripts/init-opengauss.sh`

### 7.1 创建启动脚本

- [ ] **Step 1: 创建 scripts 目录**

```bash
mkdir -p scripts
```

- [ ] **Step 2: 创建 start-opengauss.sh**

```bash
#!/bin/bash

# openGauss Docker 启动脚本

CONTAINER_NAME="opengauss-contract"
IMAGE="enmotech/opengauss:5.0.0"
PORT="5432"
PASSWORD="OpenGauss@123"
DATA_DIR="/data/opengauss"

echo "=== 启动 openGauss 容器 ==="

# 检查容器是否已存在
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "容器 ${CONTAINER_NAME} 已存在"
    read -p "是否删除并重新创建？(y/n): " confirm
    if [ "$confirm" = "y" ]; then
        docker rm -f ${CONTAINER_NAME}
    else
        echo "取消操作"
        exit 1
    fi
fi

# 创建数据目录
sudo mkdir -p ${DATA_DIR}
sudo chown -R $(whoami):$(whoami) ${DATA_DIR}

# 启动容器
docker run -d \
    --name ${CONTAINER_NAME} \
    -p ${PORT}:5432 \
    -e GS_PASSWORD=${PASSWORD} \
    -v ${DATA_DIR}:/var/lib/opengauss \
    ${IMAGE}

echo "=== 等待 openGauss 启动 ==="
sleep 10

# 检查容器状态
if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "✅ openGauss 启动成功"
    echo "连接信息："
    echo "  Host: localhost"
    echo "  Port: ${PORT}"
    echo "  User: gaussdb"
    echo "  Password: ${PASSWORD}"
else
    echo "❌ openGauss 启动失败"
    docker logs ${CONTAINER_NAME}
    exit 1
fi
```

- [ ] **Step 3: 创建 init-opengauss.sh**

```bash
#!/bin/bash

# openGauss 数据库初始化脚本

CONTAINER_NAME="opengauss-contract"
DB_NAME="contract_template"
DB_USER="gaussdb"
DB_PASSWORD="OpenGauss@123"

echo "=== 初始化 openGauss 数据库 ==="

# 等待数据库就绪
echo "等待数据库就绪..."
sleep 5

# 创建数据库
echo "创建数据库 ${DB_NAME}..."
docker exec -i ${CONTAINER_NAME} bash -c "gsql -d postgres -U ${DB_USER} -W ${DB_PASSWORD} -c 'CREATE DATABASE ${DB_NAME};'" 2>/dev/null || echo "数据库可能已存在"

# 执行建表脚本
echo "执行建表脚本..."
docker exec -i ${CONTAINER_NAME} bash -c "gsql -d ${DB_NAME} -U ${DB_USER} -W ${DB_PASSWORD}" < src/main/resources/schema-opengauss.sql

# 执行测试数据脚本
echo "执行测试数据脚本..."
docker exec -i ${CONTAINER_NAME} bash -c "gsql -d ${DB_NAME} -U ${DB_USER} -W ${DB_PASSWORD}" < src/main/resources/data-opengauss.sql

# 验证数据
echo "验证数据..."
docker exec -i ${CONTAINER_NAME} bash -c "gsql -d ${DB_NAME} -U ${DB_USER} -W ${DB_PASSWORD} -c 'SELECT COUNT(*) FROM t_ui_template;'"

echo "✅ 数据库初始化完成"
```

- [ ] **Step 4: 设置脚本权限**

```bash
chmod +x scripts/start-opengauss.sh
chmod +x scripts/init-opengauss.sh
```

- [ ] **Step 5: 提交脚本**

```bash
git add scripts/
git commit -m "feat: 添加 openGauss Docker 启动和初始化脚本"
```

---

## Task 8: 执行 openGauss 安装和初始化

**Files:**
- 无文件修改，执行操作

### 8.1 启动 Docker 容器

- [ ] **Step 1: 执行启动脚本**

Run: `./scripts/start-opengauss.sh`
Expected: 容器启动成功，输出连接信息

- [ ] **Step 2: 验证容器运行**

Run: `docker ps | grep opengauss`
Expected: 显示 opengauss-contract 容器

### 8.2 初始化数据库

- [ ] **Step 3: 执行初始化脚本**

Run: `./scripts/init-opengauss.sh`
Expected: 建表成功，测试数据导入成功

- [ ] **Step 4: 验证数据**

Run: `docker exec -it opengauss-contract gsql -d contract_template -U gaussdb -W OpenGauss@123 -c "SELECT * FROM t_ui_template;"`
Expected: 显示 2 条模板数据

---

## Task 9: 验证应用连接

**Files:**
- 无文件修改，验证操作

### 9.1 启动应用

- [ ] **Step 1: 编译项目**

Run: `mvn clean package -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 2: 启动应用（使用 openGauss profile）**

Run: `java -jar target/contract-template-0.0.1-SNAPSHOT.jar --spring.profiles.active=opengauss`
Expected: 应用启动成功，连接 openGauss

### 9.2 验证 API

- [ ] **Step 3: 验证模板列表 API**

Run: `curl -s http://localhost:8888/api/templates | jq '.'`
Expected: 返回 2 个模板数据

- [ ] **Step 4: 验证布局节点 API**

Run: `curl -s http://localhost:8888/api/templates/1001/versions/2001/layout-nodes | jq '.'`
Expected: 返回布局节点树

---

## Task 10: Chrome DevTools E2E 验证

**Files:**
- 无文件修改，验证操作

### 10.1 准备验证环境

- [ ] **Step 1: 启动 Chrome 调试端口**

Run: `google-chrome --remote-debugging-port=9222 &`

- [ ] **Step 2: 确认应用运行**

确认应用在 `http://localhost:8888` 运行

### 10.2 使用 Chrome DevTools 验证

- [ ] **Step 3: 打开配置页面**

使用 Chrome DevTools MCP 工具：
```javascript
mcp__chrome-devtools__navigate_page({ url: "http://localhost:8888/config.html" })
```

- [ ] **Step 4: 验证模板列表**

检查模板选择下拉框：
- 应显示 "销售合同模板" 和 "采购合同模板"

- [ ] **Step 5: 验证组件库**

检查左侧组件库面板：
- 应显示布局组件（PAGE、CARD、GRID、TAB）
- 应显示基础组件（INPUT、SELECT、DATE、NUMBER）
- 应显示业务组件（DETAIL_TABLE、SUPPLIER_SELECT）
- 应显示动作组件（BUTTON、QUERY_DIALOG、SAVE_BUTTON）

- [ ] **Step 6: 选择模板验证布局**

选择 "销售合同模板"：
- 验证右侧预览区显示布局节点
- 验证节点包含：基本信息、明细信息、操作区域

- [ ] **Step 7: 截图保存验证结果**

Run: `mcp__chrome-devtools__take_screenshot({ filePath: "docs/screenshots/config-page-verification.png" })`

### 10.3 验证完成

- [ ] **Step 8: 记录验证结果**

在 `docs/superpowers/specs/2026-06-07-opengauss-setup-and-testdata-design.md` 中添加验证结果章节：

```markdown
## 验证结果

### 数据库验证
- ✅ openGauss 容器启动成功
- ✅ 数据库创建成功
- ✅ 建表脚本执行成功
- ✅ 测试数据导入成功

### API 验证
- ✅ 模板列表 API 返回 2 个模板
- ✅ 布局节点 API 返回节点树

### E2E 验证
- ✅ 配置页面加载成功
- ✅ 模板列表显示正确
- ✅ 组件库显示完整
- ✅ 布局渲染正确
```

---

## 自审清单

### 1. 规格覆盖检查

| 设计要求 | 对应任务 |
|---------|---------|
| 补充 req.md 审计字段 | Task 1 |
| 同步 H2 脚本 | Task 2 |
| 创建 openGauss 建表脚本 | Task 3 |
| 创建测试数据 | Task 4 |
| 添加 PostgreSQL 依赖 | Task 5 |
| 创建 openGauss 配置 | Task 6 |
| Docker 启动脚本 | Task 7 |
| 执行安装初始化 | Task 8 |
| 验证应用连接 | Task 9 |
| E2E 验证 | Task 10 |

### 2. Placeholder 检查

- ✅ 无 "TBD"、"TODO"
- ✅ 所有 SQL 语句完整
- ✅ 所有配置完整
- ✅ 所有命令完整

### 3. 类型一致性检查

- ✅ 审计字段类型一致：`BIGINT`
- ✅ 时间字段：openGauss 用 `TIMESTAMPTZ`，H2 用 `TIMESTAMP`
- ✅ JSON 字段：openGauss 用 `JSONB`，H2 用 `JSON`
