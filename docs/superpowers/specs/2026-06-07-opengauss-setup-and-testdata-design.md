# openGauss 环境搭建与测试数据初始化设计

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:writing-plans to create implementation plan after this spec is approved.

**Goal:** 在 Docker 中安装 openGauss 数据库，补充审计字段一致性，初始化测试数据，使前端配置界面能够完整测试模板配置功能。

**Architecture:**
- Docker openGauss 5.0 容器作为数据库
- 应用通过 PostgreSQL 驱动连接 openGauss
- 审计字段（created_by/created_name/created_at, updated_by/updated_name/updated_at）统一补充到所有表
- 测试数据覆盖：模板、版本、布局、字段、数据源

**Tech Stack:**
- openGauss 5.0 LTS (Docker)
- PostgreSQL JDBC Driver
- Spring Boot 3.5.14

---

## 1. 问题背景

### 1.1 当前问题

1. **模板选择列表为空**：H2 内存数据库没有测试数据，前端无法选择模板
2. **组件仓库显示问题**：组件库已定义表单、表格等组件，但需要完整的数据才能验证
3. **审计字段不一致**：部分表缺少 created_by、created_name、updated_by、updated_name 字段

### 1.2 解决方案

1. 使用 Docker 安装 openGauss 数据库
2. 补充 req.md 和 H2 脚本中缺失的审计字段
3. 创建完整的测试数据（模板、版本、布局、字段、数据源）
4. 配置应用连接 openGauss

---

## 2. 审计字段补充

### 2.1 需要补充的表

以下表在 req.md 中缺少完整审计字段，需要补充 `created_by`, `updated_by`：

| 序号 | 表名 | 补充字段 |
|------|------|---------|
| 1 | t_ui_layout_node | created_by, updated_by |
| 2 | t_ui_field_def | created_by, updated_by |
| 3 | t_ui_field_component | created_by, updated_by |
| 4 | t_ui_detail_table | created_by, updated_by |
| 5 | t_ui_data_option | created_by, updated_by |
| 6 | t_ui_query_config | created_by, updated_by |
| 7 | t_ui_query_param | created_by, updated_by |
| 8 | t_ui_query_fill_rule | created_by, updated_by |
| 9 | t_ui_action_config | created_by, updated_by |
| 10 | t_contract_field_value | created_by, updated_by |
| 11 | t_contract_detail_row | created_by, updated_by |
| 12 | t_contract_detail_field_value | created_by, updated_by |
| 13 | t_contract_search_index | created_by, updated_by |
| 14 | t_contract_attachment | updated_by, updated_at |

### 2.2 字段定义

```sql
created_by BIGINT,
created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_by BIGINT,
updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
```

---

## 3. Docker openGauss 配置

### 3.1 容器配置

| 配置项 | 值 |
|-------|-----|
| 镜像 | enmotech/opengauss:5.0.0 |
| 容器名 | opengauss-contract |
| 端口 | 5432:5432 |
| 数据库名 | contract_template |
| 用户名 | gaussdb |
| 密码 | OpenGauss@123 |
| 数据卷 | /data/opengauss:/var/lib/opengauss |

### 3.2 启动命令

```bash
docker run -d --name opengauss-contract \
  -p 5432:5432 \
  -e GS_PASSWORD=OpenGauss@123 \
  -v /data/opengauss:/var/lib/opengauss \
  enmotech/opengauss:5.0.0
```

### 3.3 数据库初始化

1. 进入容器创建数据库：
```bash
docker exec -it opengauss-contract bash
gsql -d postgres -U gaussdb -W OpenGauss@123
CREATE DATABASE contract_template;
```

2. 执行建表脚本：
```bash
gsql -d contract_template -U gaussdb -W OpenGauss@123 -f /path/to/schema-opengauss.sql
```

3. 执行测试数据脚本：
```bash
gsql -d contract_template -U gaussdb -W OpenGauss@123 -f /path/to/data-opengauss.sql
```

---

## 4. 测试数据设计

### 4.1 模板数据

创建 2 个模板：

| ID | template_code | template_name | template_desc | biz_type | status |
|----|---------------|---------------|---------------|----------|--------|
| 1001 | SALE_CONTRACT | 销售合同模板 | 用于销售业务合同录入 | SALE | ENABLED |
| 1002 | PURCHASE_CONTRACT | 采购合同模板 | 用于采购业务合同录入 | PURCHASE | ENABLED |

### 4.2 模板版本数据

每个模板创建 2 个版本：

| ID | template_id | version_no | version_name | version_status |
|----|-------------|------------|--------------|----------------|
| 2001 | 1001 | 1 | V1.0 | PUBLISHED |
| 2002 | 1001 | 2 | V1.1 草稿 | DRAFT |
| 2003 | 1002 | 1 | V1.0 | PUBLISHED |

更新模板的 current_version_id：
- 模板 1001 → current_version_id = 2001
- 模板 1002 → current_version_id = 2003

### 4.3 布局节点数据（销售合同模板）

为销售合同模板（template_version_id = 2001）创建布局：

```
PAGE (node_code: page_root)
├── CARD (node_code: card_basic, node_name: 基本信息)
│   └── GRID (node_code: grid_basic, columns: 2)
│       ├── FIELD (node_code: field_contract_no, field_path: basic.contractNo)
│       ├── FIELD (node_code: field_contract_name, field_path: basic.contractName)
│       ├── FIELD (node_code: field_sign_date, field_path: basic.signDate)
│       └── FIELD (node_code: field_supplier, field_path: basic.supplierName)
├── CARD (node_code: card_detail, node_name: 明细信息)
│   └── DETAIL_TABLE (node_code: detail_items, detail_code: items)
└── CARD (node_code: card_action, node_name: 操作区域)
    ├── ACTION (node_code: action_save, action_type: SAVE)
    └── ACTION (node_code: action_query, action_type: QUERY)
```

节点数据：

| ID | parent_id | node_code | node_name | node_type | sort_no |
|----|-----------|-----------|-----------|-----------|---------|
| 3001 | NULL | page_root | 销售合同页面 | PAGE | 0 |
| 3002 | 3001 | card_basic | 基本信息 | CARD | 1 |
| 3003 | 3002 | grid_basic | 基本信息栅格 | GRID | 0 |
| 3004 | 3003 | field_contract_no | 合同编号 | FIELD | 1 |
| 3005 | 3003 | field_contract_name | 合同名称 | FIELD | 2 |
| 3006 | 3003 | field_sign_date | 签订日期 | FIELD | 3 |
| 3007 | 3003 | field_supplier | 供应商 | FIELD | 4 |
| 3008 | 3001 | card_detail | 明细信息 | CARD | 2 |
| 3009 | 3008 | detail_items | 明细表 | DETAIL_TABLE | 0 |
| 3010 | 3001 | card_action | 操作区域 | CARD | 3 |
| 3011 | 3010 | action_save | 保存按钮 | ACTION | 1 |
| 3012 | 3010 | action_query | 查询按钮 | ACTION | 2 |

### 4.4 字段定义数据

销售合同模板的字段定义：

| ID | detail_table_id | field_code | field_path | field_name_cn | data_type | required_default | searchable | indexable |
|----|-----------------|------------|------------|---------------|-----------|------------------|------------|-----------|
| 4001 | NULL | contractNo | basic.contractNo | 合同编号 | string | 1 | 1 | 1 |
| 4002 | NULL | contractName | basic.contractName | 合同名称 | string | 1 | 1 | 1 |
| 4003 | NULL | signDate | basic.signDate | 签订日期 | date | 0 | 1 | 1 |
| 4004 | NULL | supplierId | basic.supplierId | 供应商ID | string | 0 | 0 | 0 |
| 4005 | NULL | supplierName | basic.supplierName | 供应商名称 | string | 0 | 1 | 1 |
| 4006 | NULL | totalAmount | basic.totalAmount | 总金额 | number | 0 | 0 | 1 |
| 4007 | 3009 | itemName | items[].itemName | 明细项名称 | string | 1 | 0 | 0 |
| 4008 | 3009 | quantity | items[].quantity | 数量 | number | 1 | 0 | 0 |
| 4009 | 3009 | price | items[].price | 单价 | number | 1 | 0 | 0 |
| 4010 | 3009 | amount | items[].amount | 金额 | number | 0 | 0 | 0 |

### 4.5 明细表配置数据

| ID | detail_code | detail_name | detail_path | min_rows | max_rows | allow_add | allow_edit | allow_delete |
|----|-------------|-------------|-------------|----------|----------|-----------|------------|--------------|
| 3009 | items | 合同明细 | items | 1 | 100 | 1 | 1 | 1 |

### 4.6 数据提供方配置

创建静态数据源用于下拉框：

| ID | provider_code | provider_name | provider_type | config_json |
|----|---------------|---------------|---------------|-------------|
| 5001 | SUPPLIER_LIST | 供应商列表 | STATIC | [{"value": "SUP001", "label": "供应商A"}, {"value": "SUP002", "label": "供应商B"}] |
| 5002 | CONTRACT_TYPE | 合同类型 | STATIC | [{"value": "SALE", "label": "销售合同"}, {"value": "PURCHASE", "label": "采购合同"}] |

### 4.7 查询配置

为查询按钮创建查询配置：

| ID | query_code | query_name | query_type | data_provider_id | trigger_type | result_mode |
|----|------------|------------|------------|------------------|--------------|-------------|
| 6001 | query_supplier | 查询供应商 | POPUP | 5001 | MANUAL | SINGLE_SELECT |

### 4.8 动作配置

为按钮创建动作配置：

| ID | action_code | action_name | action_type | bind_node_id | confirm_required |
|----|-------------|-------------|-------------|--------------|------------------|
| 7001 | save_contract | 保存合同 | SAVE_CONTRACT | 3011 | 1 |
| 7002 | query_supplier | 查询供应商 | QUERY | 3012 | 0 |

---

## 5. 应用配置修改

### 5.1 新增依赖

在 pom.xml 中添加 PostgreSQL 驱动：

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 5.2 新增配置文件

创建 `application-opengauss.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/contract_template
    driver-class-name: org.postgresql.Driver
    username: gaussdb
    password: OpenGauss@123

  sql:
    init:
      mode: never  # openGauss 使用脚本初始化，不用 Spring 初始化

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
```

### 5.3 启动命令

```bash
java -jar target/contract-template.jar --spring.profiles.active=opengauss
```

---

## 6. 脚本文件清单

### 6.1 建表脚本

| 文件 | 用途 |
|------|------|
| `src/main/resources/schema-opengauss.sql` | openGauss 建表脚本（从 req.md 提取，补充审计字段） |
| `src/main/resources/schema-h2.sql` | H2 建表脚本（同步补充审计字段） |

### 6.2 数据脚本

| 文件 | 用途 |
|------|------|
| `src/main/resources/data-opengauss.sql` | openGauss 测试数据 |
| `src/main/resources/data-h2.sql` | H2 测试数据（与 openGauss 数据一致） |

---

## 7. 验证方案

### 7.1 数据库验证

1. 连接数据库，验证表创建成功：
```sql
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
```

2. 验证测试数据：
```sql
SELECT * FROM t_ui_template;
SELECT * FROM t_ui_template_version;
SELECT * FROM t_ui_layout_node WHERE template_version_id = 2001;
```

### 7.2 应用验证

1. 启动应用连接 openGauss
2. 访问 `http://localhost:8888/config.html`
3. 验证模板列表显示 2 个模板
4. 选择销售合同模板，验证布局渲染
5. 验证组件库显示所有组件类型

### 7.3 Chrome DevTools E2E 验证

使用 Chrome DevTools MCP 进行完整流程验证：

1. 打开配置界面
2. 选择模板
3. 验证布局节点显示
4. 验证组件库完整
5. 拖拽组件到布局
6. 保存配置
7. 验证数据库记录

---

## 8. 实施步骤

### 阶段 1：审计字段补充
1. 更新 req.md 中的表定义
2. 更新 schema-h2.sql
3. 验证 H2 测试通过

### 阶段 2：Docker openGauss 安装
1. 创建 Docker 容器
2. 创建数据库
3. 执行建表脚本

### 阶段 3：测试数据初始化
1. 创建 data-opengauss.sql
2. 创建 data-h2.sql
3. 执行脚本导入数据

### 阶段 4：应用配置
1. 添加 PostgreSQL 依赖
2. 创建 application-opengauss.yml
3. 验证连接

### 阶段 5：E2E 验证
1. 启动应用
2. Chrome DevTools 完整流程验证
3. 记录验证结果

---

## 9. 回滚方案

如果 openGauss 安装失败，可以：

1. 继续使用 H2 内存数据库
2. 使用 data-h2.sql 初始化测试数据
3. 启动时指定 `--spring.profiles.active=test`

---

## 10. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| Docker 镜像下载慢 | 延迟安装 | 使用国内镜像源 |
| openGauss 兼容性问题 | 连接失败 | 使用 PostgreSQL 驱动兼容模式 |
| 审计字段补充遗漏 | 运行时报错 | 完整对比 req.md 和 H2 脚本 |
| 测试数据不完整 | 验证失败 | 按照完整业务场景设计数据 |
