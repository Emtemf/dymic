# 合同模板动态渲染系统：需求补充文档 V1.0

> 本文档是对 req.md 的补充，明确技术栈、开发策略、测试方案和前端 Demo 范围。

---

## 1. 技术栈确认

### 1.1 后端技术栈

```yaml
Java: 21
Spring Boot: 3.5.14
ORM: MyBatis-Plus 3.5.5
数据库（生产）: openGauss
数据库（测试）: H2 内存数据库（兼容 PostgreSQL 模式）
JSON 处理: Jackson + JSONB 工具封装
ID 生成: 雪花算法（自研或 Hutool）
构建工具: Maven
```

### 1.2 前端技术栈（Demo）

```yaml
HTML: 原生 HTML5
CSS: frontend-design 样式库（CDN 引入）
JS: 原生 JavaScript ES6+
构建: 无构建工具，直接静态文件
HTTP: Fetch API
```

### 1.3 开发环境

```yaml
IDE: IntelliJ IDEA
版本控制: Git
本地数据库: H2 内存数据库（单元测试）
生产数据库: openGauss（部署环境）
```

---

## 2. 数据源配置简化方案

### 2.1 设计理念

对后端应用来说，数据源的本质是：**根据配置规则，从某个来源获取数据，统一返回列表格式**。

无论来源是：
- 静态选项（写死的）
- 字典（数据库字典表）
- HTTP 接口（外部系统）
- 平台集成（公司平台）
- 内部查询（本系统数据）

**都是 IT 配置和维护的，前端和业务代码不关心具体来源。**

### 2.2 表结构简化

**合并 `t_ui_data_provider` 和 `t_ui_data_option` 为一张表**：

```sql
CREATE TABLE t_ui_data_provider (
    id BIGINT PRIMARY KEY,
    provider_code VARCHAR(100) NOT NULL,
    provider_name VARCHAR(200) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    config_json JSONB NOT NULL,
    cache_enabled SMALLINT NOT NULL DEFAULT 0,
    cache_ttl_seconds INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_name VARCHAR(100),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_data_provider_code UNIQUE (provider_code)
);

COMMENT ON TABLE t_ui_data_provider IS '数据提供方配置，统一管理静态选项、字典、HTTP、平台集成、内部查询等数据来源';
COMMENT ON COLUMN t_ui_data_provider.provider_type IS 'STATIC静态选项、DICT字典、HTTP外部接口、PLATFORM平台集成、INTERNAL内部查询';
COMMENT ON COLUMN t_ui_data_provider.config_json IS '不同类型的配置内容：STATIC存options数组、HTTP存url/method/headers等、PLATFORM存apiCode等';
```

### 2.3 config_json 结构示例

```json
// STATIC 静态选项
{
  "options": [
    {"value": "CNY", "label": "人民币", "sort": 1},
    {"value": "USD", "label": "美元", "sort": 2},
    {"value": "EUR", "label": "欧元", "sort": 3}
  ]
}

// DICT 字典（引用公司字典表）
{
  "dictType": "CONTRACT_TYPE",
  "dictTable": "sys_dict_item",
  "filterConditions": {"status": "ENABLED"}
}

// HTTP 外部接口
{
  "url": "http://api.example.com/suppliers",
  "method": "GET",
  "headers": {"Authorization": "Bearer ${token}"},
  "timeout": 5000,
  "requestMapping": {
    "keyword": "keyword",
    "page": "page",
    "size": "size"
  },
  "responseMapping": {
    "dataPath": "data.list",
    "totalPath": "data.total",
    "fields": [
      {"source": "id", "target": "supplierId"},
      {"source": "name", "target": "supplierName"}
    ]
  }
}

// PLATFORM 平台集成
{
  "apiCode": "ORG_SERVICE",
  "apiName": "组织服务",
  "queryParams": {"orgType": "COMPANY"},
  "responseMapping": {
    "fields": [
      {"source": "orgId", "target": "orgId"},
      {"source": "orgName", "target": "orgName"}
    ]
  }
}

// INTERNAL 内部查询
{
  "queryType": "CONTRACT_LIST",
  "conditions": [
    {"field": "contractStatus", "operator": "EQ", "value": "ACTIVE"}
  ],
  "fields": [
    {"source": "contractNo", "target": "contractNo"},
    {"source": "contractName", "target": "contractName"}
  ]
}
```

### 2.4 Java 实现架构

```java
// 统一接口
public interface DataProviderExecutor {
    List<Map<String, Object>> execute(DataProvider provider, Map<String, Object> params);
}

// 不同类型的执行器
@Component
public class StaticDataProviderExecutor implements DataProviderExecutor {
    // 从 config_json 解析 options 数组
}

@Component
public class DictDataProviderExecutor implements DataProviderExecutor {
    // 查询公司字典表
}

@Component
public class HttpDataProviderExecutor implements DataProviderExecutor {
    // 调用外部 HTTP 接口
}

@Component
public class PlatformDataProviderExecutor implements DataProviderExecutor {
    // 调用公司平台集成服务
}

@Component
public class InternalDataProviderExecutor implements DataProviderExecutor {
    // 执行内部查询逻辑
}

// 执行器工厂
@Component
public class DataProviderExecutorFactory {
    private Map<String, DataProviderExecutor> executors;

    public DataProviderExecutor getExecutor(String providerType) {
        return executors.get(providerType);
    }
}
```

---

## 3. 数据库兼容性策略

### 3.1 混合策略设计

**核心原则**：
- **标准 SQL**：通用功能使用标准 SQL，由 MyBatis-Plus 自动适配方言
- **特性封装**：openGauss 特有功能（如 JSONB 查询）单独封装为工具类
- **测试隔离**：H2 使用 PostgreSQL 模式，兼容大部分 openGauss 语法

### 3.2 JSONB 处理策略

openGauss 和 H2（PostgreSQL 模式）都支持 JSONB，但有细微差异。

**封装方案**：

```java
@Component
public class JsonbHelper {

    /**
     * JSONB 查询：提取字段值
     * PostgreSQL/openGauss: canonical_data->>'basic.contractNo'
     */
    public String extractField(String jsonbColumn, String jsonPath) {
        return String.format("%s->>'%s'", jsonbColumn, jsonPath);
    }

    /**
     * JSONB 查询：路径存在判断
     * PostgreSQL/openGauss: canonical_data ? 'basic'
     */
    public String pathExists(String jsonbColumn, String jsonPath) {
        return String.format("%s ? '%s'", jsonbColumn, jsonPath);
    }

    /**
     * JSONB 更新：设置字段值
     * PostgreSQL/openGauss: jsonb_set(column, '{path}', 'value')
     */
    public String setField(String jsonbColumn, String jsonPath, String value) {
        return String.format("jsonb_set(%s, '{%s}', '%s')", jsonbColumn, jsonPath, value);
    }
}
```

### 3.3 MyBatis 配置

```yaml
# application.yml（生产）
spring:
  datasource:
    url: jdbc:opengauss://host:port/database
    driver-class-name: org.opengauss.Driver

mybatis-plus:
  configuration:
    database-id: opengauss

# application-test.yml（测试）
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver

mybatis-plus:
  configuration:
    database-id: h2
```

### 3.4 SQL 兼容性处理

```xml
<!-- MyBatis XML 示例 -->
<select id="findContractByField" resultType="Contract">
    <if test="_databaseId == 'opengauss'">
        SELECT * FROM t_contract
        WHERE canonical_data->>'basic.contractNo' = #{contractNo}
    </if>
    <if test="_databaseId == 'h2'">
        SELECT * FROM t_contract
        WHERE JSON_PATH_QUERY(canonical_data, '$.basic.contractNo') = #{contractNo}
    </if>
</select>
```

---

## 4. 测试策略

### 4.1 单元测试（H2 内存数据库）

**测试范围**：
- 模板配置 CRUD
- 字段定义和组件绑定
- 数据提供方执行逻辑
- 合同保存和回显逻辑
- 明细表增删改逻辑
- 外部数据映射和对比逻辑

**测试配置**：

```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TemplateServiceTest {

    @Autowired
    private TemplateService templateService;

    @Test
    void testCreateTemplate() {
        // 测试创建模板
        Template template = templateService.createTemplate(...);
        assertNotNull(template.getId());
    }
}
```

### 4.2 集成测试（可选 Testcontainers）

如果需要在 CI 环境验证 openGauss 特性：

```java
@Testcontainers
@SpringBootTest
class OpenGaussIntegrationTest {

    @Container
    static PostgreSQLContainer<?> openGauss =
        new PostgreSQLContainer<>("opengauss/opengauss:latest");

    @Test
    void testJsonbQuery() {
        // 测试真实的 JSONB 查询语法
    }
}
```

### 4.3 测试数据初始化

```sql
-- test-data.sql（单元测试初始化）
INSERT INTO t_ui_template (id, template_code, template_name, status)
VALUES (1, 'PURCHASE_CONTRACT', '采购合同模板', 'ENABLED');

INSERT INTO t_ui_template_version (id, template_id, version_no, version_status)
VALUES (100, 1, 1, 'PUBLISHED');

-- 更多初始化数据...
```

---

## 5. 前端 Demo 完整流程

### 5.1 Demo 功能范围

**完整端到端流程**：

1. **模板配置界面**（简单版）
   - 创建模板
   - 配置布局节点（单页、卡片、栅格）
   - 配置字段定义
   - 配置组件绑定
   - 配置数据提供方
   - 配置查询和回填规则
   - 发布模板版本

2. **合同新增界面**
   - 选择模板
   - 渲染动态表单
   - 执行查询选择（如供应商选择）
   - 明细表增删改
   - 弹窗编辑明细行
   - 保存合同

3. **合同编辑界面**
   - 打开已有合同
   - 回显数据
   - 修改字段
   - 保存合同

4. **合同列表界面**
   - 查询合同列表
   - 筛选和排序
   - 打开编辑

### 5.2 前端文件结构

```text
demo/
├── index.html              # 入口页面（模板列表）
├── template-config.html    # 模板配置界面
├── contract-new.html       # 合同新增界面
├── contract-edit.html      # 合同编辑界面
├── contract-list.html      # 合同列表界面
├── css/
│   └── frontend-design.css # 样式库（CDN 或本地）
├── js/
│   ├── api.js              # HTTP 请求封装
│   ├── template-api.js     # 模板配置相关 API
│   ├── contract-api.js     # 合同相关 API
│   ├── render.js           # 动态渲染逻辑
│   ├── form.js             # 表单处理逻辑
│   ├── detail-table.js     # 明细表逻辑
│   └── modal.js            # 弹窗逻辑
└── lib/
    └── frontend-design.min.js # 样式库 JS（如果有）
```

### 5.3 简单界面示例（单页卡片表单）

```html
<!-- contract-new.html -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>合同新增</title>
    <link rel="stylesheet" href="css/frontend-design.css">
</head>
<body>
    <div class="page-container">
        <h1>新增采购合同</h1>

        <!-- 基本信息 卡片 -->
        <div class="card">
            <div class="card-header">基本信息</div>
            <div class="card-body">
                <div class="form-field">
                    <label>合同编号</label>
                    <input type="text" id="contractNo" placeholder="自动生成">
                </div>
                <div class="form-field">
                    <label>合同名称</label>
                    <input type="text" id="contractName" required>
                </div>
                <div class="form-field">
                    <label>合同类型</label>
                    <select id="contractType">
                        <!-- 动态加载选项 -->
                    </select>
                </div>
                <div class="form-field">
                    <label>供应商</label>
                    <input type="text" id="supplierName" readonly>
                    <button onclick="openSupplierSelect()">选择供应商</button>
                </div>
            </div>
        </div>

        <!-- 金额信息 卡片 -->
        <div class="card">
            <div class="card-header">金额信息</div>
            <div class="card-body">
                <div class="form-field">
                    <label>合同金额</label>
                    <input type="number" id="totalAmount" step="0.01">
                </div>
                <div class="form-field">
                    <label>币种</label>
                    <select id="currency">
                        <!-- 动态加载选项 -->
                    </select>
                </div>
            </div>
        </div>

        <!-- 商品明细表 -->
        <div class="card">
            <div class="card-header">商品明细</div>
            <div class="card-body">
                <table id="detailTable">
                    <thead>
                        <tr>
                            <th>商品名称</th>
                            <th>数量</th>
                            <th>单价</th>
                            <th>金额</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- 动态渲染明细行 -->
                    </tbody>
                </table>
                <button onclick="addDetailRow()">新增明细行</button>
            </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-bar">
            <button onclick="saveContract()">保存</button>
            <button onclick="resetForm()">重置</button>
        </div>
    </div>

    <!-- 供应商选择弹窗 -->
    <div id="supplierModal" class="modal" style="display:none;">
        <div class="modal-content">
            <div class="modal-header">选择供应商</div>
            <div class="modal-body">
                <input type="text" id="supplierKeyword" placeholder="输入关键字">
                <button onclick="searchSupplier()">查询</button>
                <table id="supplierList">
                    <thead>
                        <tr>
                            <th>供应商编码</th>
                            <th>供应商名称</th>
                            <th>统一社会信用代码</th>
                            <th>选择</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- 动态渲染查询结果 -->
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script src="js/api.js"></script>
    <script src="js/contract-api.js"></script>
    <script src="js/render.js"></script>
    <script src="js/form.js"></script>
    <script src="js/detail-table.js"></script>
    <script src="js/modal.js"></script>

    <script>
        // 页面初始化
        document.addEventListener('DOMContentLoaded', async () => {
            // 加载模板 schema
            const schema = await ContractApi.getTemplateSchema('PURCHASE_CONTRACT');

            // 渲染动态表单
            RenderEngine.renderForm(schema);

            // 加载静态选项
            await loadOptions();
        });

        // 加载选项数据
        async function loadOptions() {
            // 加载币种选项
            const currencies = await ContractApi.getDataProvider('CURRENCY');
            renderSelect('currency', currencies);

            // 加载合同类型选项
            const types = await ContractApi.getDataProvider('CONTRACT_TYPE');
            renderSelect('contractType', types);
        }

        // 打开供应商选择弹窗
        function openSupplierSelect() {
            ModalHelper.open('supplierModal');
        }

        // 查询供应商
        async function searchSupplier() {
            const keyword = document.getElementById('supplierKeyword').value;
            const result = await ContractApi.executeQuery('SUPPLIER_QUERY', {keyword});
            renderSupplierList(result.rows);
        }

        // 渲染供应商列表
        function renderSupplierList(rows) {
            const tbody = document.querySelector('#supplierList tbody');
            tbody.innerHTML = rows.map(row => `
                <tr>
                    <td>${row.supplierId}</td>
                    <td>${row.supplierName}</td>
                    <td>${row.creditCode}</td>
                    <td><button onclick="selectSupplier('${row.supplierId}', '${row.supplierName}')">选择</button></td>
                </tr>
            `).join('');
        }

        // 选择供应商并回填
        function selectSupplier(id, name) {
            document.getElementById('supplierId').value = id;
            document.getElementById('supplierName').value = name;
            ModalHelper.close('supplierModal');
        }

        // 新增明细行
        function addDetailRow() {
            DetailTableHelper.addRow({
                _row_uid: generateUUID(),
                _row_op: 'ADD',
                itemName: '',
                quantity: 0,
                price: 0
            });
        }

        // 保存合同
        async function saveContract() {
            const draftData = FormHelper.collectFormData();
            const detailRows = DetailTableHelper.collectRows();

            draftData.items = detailRows;

            const result = await ContractApi.saveContract(draftData);
            if (result.success) {
                alert('保存成功');
                window.location.href = 'contract-list.html';
            } else {
                alert('保存失败：' + result.message);
            }
        }
    </script>
</body>
</html>
```

### 5.4 复杂界面示例（栅格 + Tab + 明细表）

```html
<!-- contract-edit-complex.html -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>合同编辑（复杂界面）</title>
    <link rel="stylesheet" href="css/frontend-design.css">
    <style>
        .grid-container { display: grid; grid-template-columns: 16fr 8fr; gap: 20px; }
        .left-panel { grid-column: 1; }
        .right-panel { grid-column: 2; }
        .tabs { display: flex; border-bottom: 1px solid #ddd; }
        .tab-item { padding: 10px 20px; cursor: pointer; }
        .tab-item.active { border-bottom: 2px solid #1890ff; }
        .tab-content { display: none; }
        .tab-content.active { display: block; }
    </style>
</head>
<body>
    <div class="page-container">
        <h1>编辑采购合同</h1>

        <!-- 24 栅格布局 -->
        <div class="grid-container">
            <!-- 左侧 16 栅格 -->
            <div class="left-panel">
                <div class="card">
                    <div class="card-header">基本信息</div>
                    <div class="card-body">
                        <!-- 字段组件 -->
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">商品明细</div>
                    <div class="card-body">
                        <table id="detailTable">
                            <!-- 明细表 -->
                        </table>
                    </div>
                </div>
            </div>

            <!-- 右侧 8 栅格 -->
            <div class="right-panel">
                <!-- Tab 容器 -->
                <div class="tabs">
                    <div class="tab-item active" onclick="switchTab('attachments')">附件</div>
                    <div class="tab-item" onclick="switchTab('risks')">风险提示</div>
                    <div class="tab-item" onclick="switchTab('history')">历史记录</div>
                </div>

                <!-- Tab 内容 -->
                <div id="attachments" class="tab-content active">
                    <div class="card">
                        <div class="card-body">
                            <!-- 附件列表 -->
                        </div>
                    </div>
                </div>

                <div id="risks" class="tab-content">
                    <div class="card">
                        <div class="card-body">
                            <!-- 风险提示 -->
                        </div>
                    </div>
                </div>

                <div id="history" class="tab-content">
                    <div class="card">
                        <div class="card-body">
                            <!-- 历史记录 -->
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-bar">
            <button onclick="saveContract()">保存</button>
            <button onclick="cancelEdit()">取消</button>
        </div>
    </div>

    <!-- 明细行编辑弹窗 -->
    <div id="detailRowModal" class="modal" style="display:none;">
        <div class="modal-content">
            <div class="modal-header">编辑明细行</div>
            <div class="modal-body">
                <div class="form-field">
                    <label>商品名称</label>
                    <input type="text" id="modal_itemName">
                </div>
                <div class="form-field">
                    <label>数量</label>
                    <input type="number" id="modal_quantity">
                </div>
                <div class="form-field">
                    <label>单价</label>
                    <input type="number" id="modal_price" step="0.01">
                </div>
            </div>
            <div class="modal-footer">
                <button onclick="saveModalDraft()">保存</button>
                <button onclick="closeModalDraft()">取消</button>
            </div>
        </div>
    </div>

    <script>
        // 切换 Tab
        function switchTab(tabId) {
            document.querySelectorAll('.tab-item').forEach(item => item.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

            event.target.classList.add('active');
            document.getElementById(tabId).classList.add('active');
        }

        // 编辑明细行
        function editDetailRow(rowUid) {
            const row = DetailTableHelper.getRowByUid(rowUid);
            document.getElementById('modal_itemName').value = row.itemName;
            document.getElementById('modal_quantity').value = row.quantity;
            document.getElementById('modal_price').value = row.price;

            ModalHelper.open('detailRowModal', {rowUid});
        }

        // 弹窗内保存（不落库，只合并到父页面 draft）
        function saveModalDraft() {
            const context = ModalHelper.getContext();
            const rowUid = context.rowUid;

            const updatedRow = {
                _row_uid: rowUid,
                _row_op: 'UPDATE',
                itemName: document.getElementById('modal_itemName').value,
                quantity: parseFloat(document.getElementById('modal_quantity').value),
                price: parseFloat(document.getElementById('modal_price').value)
            };

            // 合并回父页面明细表 draft
            DetailTableHelper.updateRow(rowUid, updatedRow);

            ModalHelper.close('detailRowModal');
        }
    </script>
</body>
</html>
```

---

## 6. 并发控制策略（补充到 req.md）

### 6.1 V1.0：乐观锁 + 行级锁

```java
@Service
public class ContractService {

    @Transactional
    public SaveResult saveContract(SaveCommand cmd) {
        // 1. 查询合同并加行级锁
        Contract contract = contractMapper.selectByIdForUpdate(cmd.getContractId());

        // 2. 乐观锁版本检查
        if (contract.getDataVersion() != cmd.getDataVersion()) {
            throw new ConcurrentModificationException("合同已被他人修改，请刷新后重新编辑");
        }

        // 3. 保存逻辑...
        contract.increaseDataVersion();

        // 4. 更新合同
        contractMapper.updateById(contract);

        return new SaveResult(contract.getId(), contract.getDataVersion());
    }
}
```

```xml
<!-- MyBatis Mapper -->
<select id="selectByIdForUpdate" resultType="Contract">
    SELECT * FROM t_contract WHERE id = #{id} FOR UPDATE
</select>
```

### 6.2 V1.2：Redis 分布式锁（可选）

```java
@Service
public class ContractService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional
    public SaveResult saveContract(SaveCommand cmd) {
        String lockKey = "lock:contract:" + cmd.getContractId();

        // 1. 尝试获取分布式锁
        Boolean locked = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, cmd.getUserId(), 30, TimeUnit.SECONDS);

        if (!locked) {
            throw new ContractLockedException("合同正在被他人编辑，请稍后再试");
        }

        try {
            // 2. 行级锁 + 乐观锁
            Contract contract = contractMapper.selectByIdForUpdate(cmd.getContractId());
            if (contract.getDataVersion() != cmd.getDataVersion()) {
                throw new ConcurrentModificationException("合同已被他人修改");
            }

            // 3. 保存逻辑...
            return new SaveResult(contract.getId(), contract.getDataVersion());
        } finally {
            // 4. 释放锁
            redisTemplate.delete(lockKey);
        }
    }
}
```

---

## 7. 外部数据处理策略（补充到 req.md）

### 7.1 IT 驱动、用户无感知

**核心流程**：

```text
外部系统推送消息 -> 幂等入库 -> IT 配置的映射规则自动转换 
-> 自动生成对比记录 -> IT 验证映射关系 -> 自动更新合同快照
-> 保留原始 JSON 和映射后 JSON 用于溯源
```

**关键点**：
- 用户不参与确认过程
- IT 负责维护和验证映射规则
- 对比记录主要用于 IT 调优映射规则和溯源审计

### 7.2 数据保存策略

```sql
-- t_ext_message_inbox 增加 JSON 字段
ALTER TABLE t_ext_message_inbox ADD COLUMN mapped_data JSONB;
ALTER TABLE t_ext_message_inbox ADD COLUMN mapping_id BIGINT;

COMMENT ON COLUMN t_ext_message_inbox.raw_payload IS '外部原始报文';
COMMENT ON COLUMN t_ext_message_inbox.mapped_data IS '映射后的统一合同JSON';
COMMENT ON COLUMN t_ext_message_inbox.mapping_id IS '使用的映射规则ID';
```

### 7.3 Java 实现流程

```java
@Service
public class ExternalDataService {

    @Transactional
    public void receiveAndProcess(ExternalMessage message) {
        // 1. 幂等检查
        if (messageInboxMapper.exists(message.getExtSystemId(), message.getExternalMsgId())) {
            return; // 已处理，跳过
        }

        // 2. 入库原始报文
        MessageInbox inbox = new MessageInbox();
        inbox.setExtSystemId(message.getExtSystemId());
        inbox.setExternalMsgId(message.getExternalMsgId());
        inbox.setRawPayload(message.getRawPayload());
        inbox.setProcessStatus("RECEIVED");
        messageInboxMapper.insert(inbox);

        // 3. 查找映射规则
        Mapping mapping = mappingMapper.findActive(
            message.getExtSystemId(),
            message.getMessageType()
        );

        // 4. 执行映射转换
        JsonObject mappedData = mappingEngine.transform(
            message.getRawPayload(),
            mapping
        );
        inbox.setMappedData(mappedData);
        inbox.setMappingId(mapping.getId());

        // 5. 自动对比（生成对比记录）
        Contract contract = contractMapper.findBySourceBizId(message.getSourceBizId());
        JsonObject currentData = contract == null
            ? JsonObject.empty()
            : snapshotMapper.getById(contract.getCurrentSnapshotId()).getCanonicalData();

        CompareRecord compareRecord = diffEngine.compareAndSave(
            inbox.getId(),
            contract.getId(),
            mappedData,
            currentData
        );

        // 6. 更新处理状态
        inbox.setProcessStatus("MAPPED");
        inbox.setProcessedAt(Instant.now());
        messageInboxMapper.updateById(inbox);

        // 7. IT 验证通过后，调用此方法自动更新合同
        // （由 IT 手动触发或定时任务自动执行）
        if (mapping.getAutoApply()) {
            applyExternalData(inbox.getId());
        }
    }

    @Transactional
    public void applyExternalData(Long inboxId) {
        MessageInbox inbox = messageInboxMapper.selectById(inboxId);

        // 使用映射后的数据更新合同
        Contract contract = contractMapper.findBySourceBizId(inbox.getSourceBizId());
        if (contract == null) {
            // 新建合同
            contract = contractService.createContractFromExternal(inbox.getMappedData());
        } else {
            // 更新合同
            contractService.updateContractFromExternal(
                contract.getId(),
                inbox.getMappedData()
            );
        }

        inbox.setProcessStatus("APPLIED");
        inbox.setProcessedAt(Instant.now());
        messageInboxMapper.updateById(inbox);
    }
}
```

---

## 8. 规则配置的混合存储策略

### 8.1 独立列存储（高频查询）

```sql
-- t_ui_field_def 增加
ALTER TABLE t_ui_field_def ADD COLUMN required_default SMALLINT DEFAULT 0;
ALTER TABLE t_ui_field_def ADD COLUMN searchable SMALLINT DEFAULT 0;
ALTER TABLE t_ui_field_def ADD COLUMN indexable SMALLINT DEFAULT 0;

COMMENT ON COLUMN t_ui_field_def.required_default IS '是否必填，高频查询字段，独立列';
COMMENT ON COLUMN t_ui_field_def.searchable IS '是否可作为查询条件，高频查询字段';
COMMENT ON COLUMN t_ui_field_def.indexable IS '是否写入字段索引表，高频查询字段';
```

### 8.2 JSONB 存储（复杂规则）

```sql
-- t_ui_field_component 增加
ALTER TABLE t_ui_field_component ADD COLUMN visible_rule JSONB;
ALTER TABLE t_ui_field_component ADD COLUMN readonly_rule JSONB;
ALTER TABLE t_ui_field_component ADD COLUMN required_rule JSONB;

COMMENT ON COLUMN t_ui_field_component.visible_rule IS '可见性规则，复杂条件表达式，JSONB存储';
COMMENT ON COLUMN t_ui_field_component.readonly_rule IS '只读规则，复杂条件表达式';
COMMENT ON COLUMN t_ui_field_component.required_rule IS '动态必填规则，可能依赖其他字段值';
```

### 8.3 规则 JSONB 结构示例

```json
// visible_rule 示例
{
  "type": "CONDITION",
  "condition": {
    "field": "basic.contractType",
    "operator": "EQ",
    "value": "PURCHASE"
  },
  "fallback": false  // 条件不满足时的默认可见性
}

// readonly_rule 示例
{
  "type": "OR",
  "conditions": [
    {
      "field": "basic.contractStatus",
      "operator": "EQ",
      "value": "SIGNED"
    },
    {
      "field": "basic.isFromExternal",
      "operator": "EQ",
      "value": true
    }
  ],
  "fallback": false
}

// required_rule 示例
{
  "type": "CONDITION",
  "condition": {
    "field": "money.totalAmount",
    "operator": "GT",
    "value": 100000
  },
  "message": "金额超过10万必须填写审批意见"
}
```

---

## 9. 配置端迭代路线

### 9.1 V1.0：表单化配置界面

**功能清单**：
- 模板列表页
  - 查看模板列表
  - 创建新模板
  - 停用模板

- 模板版本管理页
  - 创建草稿版本
  - 发布版本
  - 查看版本历史

- 布局节点配置页
  - 选择模板版本
  - 逐层添加布局节点
  - 选择节点类型（PAGE、CARD、GRID、ROW、COL、TAB 等）
  - 设置节点属性（栅格位置、样式等）
  - 绑定字段、明细表、查询或动作

- 字段定义配置页
  - 添加字段
  - 设置字段路径、类型、名称
  - 设置是否必填、可查询、入索引
  - 配置校验规则

- 组件绑定配置页
  - 选择布局节点
  - 选择字段
  - 选择组件类型（INPUT、SELECT、DATE 等）
  - 配置组件属性
  - 配置可见性、只读、动态必填规则

- 数据提供方配置页
  - 添加数据提供方
  - 选择类型（STATIC、DICT、HTTP、PLATFORM、INTERNAL）
  - 配置 config_json
  - 测试数据提供方（模拟查询）

- 查询配置页
  - 添加查询配置
  - 选择数据提供方
  - 配置查询参数
  - 配置回填规则

- 明细表配置页
  - 添加明细表
  - 配置明细表属性
  - 配置明细表字段

- 动作配置页
  - 添加按钮动作
  - 配置动作类型（打开弹窗、查询、保存等）

### 9.2 V1.1：增强配置体验

- 布局节点树可视化预览
- 字段批量导入（从已有模板复制）
- 查询配置测试功能
- 组件实时预览

### 9.3 V2.0：拖拽式设计器

- 可视化拖拽布局
- 可视化拖拽字段
- 实时渲染预览
- 模板版本对比

---

## 10. API 接口清单（补充）

### 10.1 模板配置接口

```yaml
# 模板主表
POST   /api/templates                 # 创建模板
GET    /api/templates                 # 模板列表
GET    /api/templates/{id}            # 模板详情
PUT    /api/templates/{id}            # 更新模板
DELETE /api/templates/{id}            # 停用模板

# 模板版本
POST   /api/templates/{id}/versions   # 创建版本
GET    /api/templates/{id}/versions   # 版本列表
GET    /api/templates/versions/{id}   # 版本详情
POST   /api/templates/versions/{id}/publish  # 发布版本

# 布局节点
POST   /api/templates/versions/{id}/nodes   # 创建节点
GET    /api/templates/versions/{id}/nodes   # 节点树
PUT    /api/templates/nodes/{id}            # 更新节点
DELETE /api/templates/nodes/{id}            # 删除节点

# 字段定义
POST   /api/templates/versions/{id}/fields  # 创建字段
GET    /api/templates/versions/{id}/fields  # 字段列表
PUT    /api/templates/fields/{id}           # 更新字段
DELETE /api/templates/fields/{id}           # 删除字段

# 组件绑定
POST   /api/templates/versions/{id}/components  # 创建绑定
GET    /api/templates/versions/{id}/components  # 组件列表
PUT    /api/templates/components/{id}           # 更新绑定
DELETE /api/templates/components/{id}           # 删除绑定

# 数据提供方
POST   /api/data-providers            # 创建数据提供方
GET    /api/data-providers            # 数据提供方列表
GET    /api/data-providers/{id}       # 数据提供方详情
PUT    /api/data-providers/{id}       # 更新数据提供方
DELETE /api/data-providers/{id}       # 删除数据提供方
POST   /api/data-providers/{id}/test  # 测试数据提供方

# 查询配置
POST   /api/templates/versions/{id}/queries    # 创建查询配置
GET    /api/templates/versions/{id}/queries    # 查询配置列表
PUT    /api/templates/queries/{id}             # 更新查询配置
DELETE /api/templates/queries/{id}             # 删除查询配置

# 查询回填规则
POST   /api/templates/queries/{id}/fill-rules  # 创建回填规则
GET    /api/templates/queries/{id}/fill-rules  # 回填规则列表
PUT    /api/templates/fill-rules/{id}          # 更新回填规则
DELETE /api/templates/fill-rules/{id}          # 删除回填规则

# 明细表配置
POST   /api/templates/versions/{id}/details    # 创建明细表
GET    /api/templates/versions/{id}/details    # 明细表列表
PUT    /api/templates/details/{id}             # 更新明细表
DELETE /api/templates/details/{id}             # 删除明细表

# 动作配置
POST   /api/templates/versions/{id}/actions    # 创建动作
GET    /api/templates/versions/{id}/actions    # 动作列表
PUT    /api/templates/actions/{id}             # 更新动作
DELETE /api/templates/actions/{id}             # 删除动作
```

### 10.2 合同业务接口

```yaml
# 渲染
GET    /api/render/new?templateCode=xxx   # 渲染新增页面 schema
GET    /api/render/edit?contractId=xxx    # 渲染编辑页面 schema

# 合同
POST   /api/contracts                     # 新增合同
GET    /api/contracts                     # 合同列表
GET    /api/contracts/{id}                # 合同详情
PUT    /api/contracts/{id}                # 更新合同（外层保存）
DELETE /api/contracts/{id}                # 删除合同

# 查询执行
POST   /api/ui/query/execute              # 执行组件查询

# 外部数据
POST   /api/ext/messages/receive          # 接收外部消息
GET    /api/ext/messages                  # 外部消息列表
GET    /api/ext/messages/{id}             # 外部消息详情
POST   /api/ext/messages/{id}/apply       # 应用外部数据到合同
```

---

## 11. 开发路线图（按优先级）

### 11.1 第一阶段：核心数据结构和 API

**目标**：建立数据库表结构和基础 CRUD API

**内容**：
- 创建数据库表（SQLite 测试环境）
- 实现模板主表和版本 CRUD
- 实现布局节点 CRUD
- 实现字段定义 CRUD
- 实现组件绑定 CRUD
- 实现数据提供方 CRUD
- 实现查询配置 CRUD
- 实现明细表配置 CRUD

**验证**：
- 单元测试覆盖 CRUD 逻辑
- H2 内存数据库测试通过

### 11.2 第二阶段：渲染和查询逻辑

**目标**：实现动态渲染和组件查询

**内容**：
- 实现 schema 组装逻辑
- 实现数据提供方执行器（STATIC、DICT、HTTP、PLATFORM、INTERNAL）
- 实现查询执行逻辑
- 实现查询参数绑定和回填规则解析
- 实现前端 Demo（合同新增页面）

**验证**：
- 端到端测试：配置模板 -> 渲染页面 -> 执行查询 -> 回填数据

### 11.3 第三阶段：合同保存和回显

**目标**：实现合同完整生命周期

**内容**：
- 实现合同新增保存逻辑
- 实现合同编辑回显逻辑
- 实现合同快照和索引重建
- 实现明细表保存逻辑
- 实现并发控制（乐观锁 + 行级锁）
- 实现前端 Demo（合同编辑页面）

**验证**：
- 端到端测试：新增合同 -> 保存 -> 编辑 -> 回显 -> 保存

### 11.4 第四阶段：外部数据集成

**目标**：实现外部数据接入和处理

**内容**：
- 实现外部消息接收和幂等入库
- 实现映射规则配置和执行
- 实现自动对比逻辑
- 实现溯源数据保存
- 实现自动应用外部数据

**验证**：
- 端到端测试：外部推送消息 -> 映射转换 -> 对比 -> 自动应用

### 11.5 第五阶段：配置界面完善

**目标**：完成配置端完整流程

**内容**：
- 实现模板配置界面
- 实现布局节点配置界面
- 实现字段和组件配置界面
- 实现查询配置界面
- 实现数据提供方配置界面
- 实现版本发布流程

**验证**：
- 完整配置流程测试：创建模板 -> 配置布局 -> 配置字段 -> 配置查询 -> 发布版本 -> 合同录入

---

## 12. 总结

本文档补充了以下关键内容：

1. **技术栈**：Java 21 + Spring Boot 3.5.14 + MyBatis-Plus + openGauss（生产）/ H2（测试）
2. **数据源简化**：合并 `t_ui_data_provider` 和 `t_ui_data_option`，使用统一 `config_json` 配置
3. **数据库兼容**：混合策略，标准 SQL + JSONB 特性封装
4. **测试策略**：单元测试（H2 内存数据库） + 可选集成测试（Testcontainers）
5. **前端 Demo**：原生 HTML/JS，完整端到端流程
6. **并发控制**：V1.0 乐观锁 + 行级锁，V1.2 Redis 分布式锁
7. **外部数据**：IT 驱动、用户无感知、保留溯源数据
8. **规则配置**：高频查询字段独立列，复杂规则 JSONB
9. **配置端迭代**：V1.0 表单化配置，V2.0 拖拽式设计器
10. **开发路线**：按优先级分 5 个阶段实施

---

**下一步**：基于本补充文档和原 req.md，创建详细的实现计划（使用 writing-plans skill）。