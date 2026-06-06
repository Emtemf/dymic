# 合同模板配置界面实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现合同模板配置界面（config.html、preview.html、data-viewer.html），验证配置数据正确入库和动态渲染

**Architecture:** 
- 后端：DDD分层架构（Controller → Service → Repository → Mapper）
- 前端：原生HTML/CSS/JS + Bootstrap 5.3
- 数据库：t_ui_layout_node、t_ui_field_def、t_ui_field_component、t_ui_action_config
- 自动生成：拼音转换工具生成nodeCode/nodePath/fieldCode/fieldPath

**Tech Stack:**
- 后端：Spring Boot 3.5.14 + MyBatis-Plus + MapStruct + Lombok + Hutool（拼音）
- 前端：原生HTML/CSS/JS + Bootstrap 5.3 + Axios
- 测试：JUnit 5 + Mockito + MockMvc + H2

---

## 任务分解原则

**任务独立性设计**：
- 每个任务都是独立的模块，可以并行执行
- 任务之间通过明确的接口和数据结构解耦
- 任务执行顺序：Phase 1（基础设施）→ Phase 2（后端API）→ Phase 3（前端界面）→ Phase 4（测试）
- 同一Phase内的任务可以并行执行多个subagent

---

## Phase 1：后端基础设施（可并行执行）

### Task 1.1：数据库表设计

**Files:**
- Create: `src/main/resources/db/schema-ui-config.sql`
- Create: `src/test/resources/schema-h2-ui-config.sql`

**目标：创建配置相关的数据库表**

- [ ] **Step 1：编写生产环境SQL脚本**

```sql
-- src/main/resources/db/schema-ui-config.sql

-- 布局节点表
CREATE TABLE IF NOT EXISTS t_ui_layout_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    template_version_id BIGINT NOT NULL COMMENT '模板版本ID',
    node_code VARCHAR(50) NOT NULL COMMENT '节点编码（拼音自动生成）',
    node_path VARCHAR(200) NOT NULL COMMENT '节点路径（自动生成）',
    node_type VARCHAR(20) NOT NULL COMMENT '节点类型：CARD_CONTAINER/SEPARATOR',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    parent_node_id BIGINT COMMENT '父节点ID',
    display_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_template_version (template_version_id),
    INDEX idx_parent_node (parent_node_id),
    INDEX idx_node_path (node_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='布局节点配置表';

-- 字段定义表
CREATE TABLE IF NOT EXISTS t_ui_field_def (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    template_version_id BIGINT NOT NULL COMMENT '模板版本ID',
    layout_node_id BIGINT NOT NULL COMMENT '所属布局节点ID',
    field_code VARCHAR(50) NOT NULL COMMENT '字段编码（拼音自动生成）',
    field_path VARCHAR(200) NOT NULL COMMENT '字段路径（自动生成）',
    field_name_cn VARCHAR(100) NOT NULL COMMENT '字段名称（中文）',
    data_type VARCHAR(20) NOT NULL COMMENT '数据类型：STRING/DECIMAL/DATE',
    required_default TINYINT NOT NULL DEFAULT 0 COMMENT '必填默认值：0=可选，1=必填',
    query_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '查询功能预留：0=无查询，1=有查询',
    display_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_layout_node (layout_node_id),
    INDEX idx_field_path (field_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段定义表';

-- 字段组件绑定表
CREATE TABLE IF NOT EXISTS t_ui_field_component (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    template_version_id BIGINT NOT NULL COMMENT '模板版本ID',
    field_def_id BIGINT NOT NULL COMMENT '字段定义ID',
    component_type VARCHAR(20) NOT NULL COMMENT '组件类型：TEXT_INPUT/MONEY_INPUT/DATE_PICKER/SELECT',
    label_name VARCHAR(100) NOT NULL COMMENT '显示名称',
    placeholder VARCHAR(200) COMMENT '占位符提示',
    data_provider_id BIGINT COMMENT '数据源ID（下拉框绑定）',
    component_config TEXT COMMENT '组件扩展配置（JSONB）',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_field_def (field_def_id),
    INDEX idx_data_provider (data_provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段组件绑定表';

-- 动作配置表
CREATE TABLE IF NOT EXISTS t_ui_action_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    template_version_id BIGINT NOT NULL COMMENT '模板版本ID',
    layout_node_id BIGINT COMMENT '所属布局节点ID（可选）',
    action_code VARCHAR(50) NOT NULL COMMENT '动作编码（拼音自动生成）',
    action_type VARCHAR(20) NOT NULL COMMENT '动作类型：SAVE_BUTTON/QUERY_BUTTON/CUSTOM_BUTTON',
    action_name VARCHAR(100) NOT NULL COMMENT '动作名称',
    action_config TEXT COMMENT '动作扩展配置（JSONB）',
    display_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_template_version (template_version_id),
    INDEX idx_layout_node (layout_node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动作配置表';
```

- [ ] **Step 2：编写测试环境SQL脚本（H2兼容）**

```sql
-- src/test/resources/schema-h2-ui-config.sql

-- 布局节点表（H2）
CREATE TABLE IF NOT EXISTS t_ui_layout_node (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    node_code VARCHAR(50) NOT NULL,
    node_path VARCHAR(200) NOT NULL,
    node_type VARCHAR(20) NOT NULL,
    node_name VARCHAR(100) NOT NULL,
    parent_node_id BIGINT,
    display_order INT NOT NULL DEFAULT 0,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_template_version ON t_ui_layout_node(template_version_id);
CREATE INDEX idx_parent_node ON t_ui_layout_node(parent_node_id);

-- 字段定义表（H2）
CREATE TABLE IF NOT EXISTS t_ui_field_def (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    layout_node_id BIGINT NOT NULL,
    field_code VARCHAR(50) NOT NULL,
    field_path VARCHAR(200) NOT NULL,
    field_name_cn VARCHAR(100) NOT NULL,
    data_type VARCHAR(20) NOT NULL,
    required_default TINYINT NOT NULL DEFAULT 0,
    query_enabled TINYINT NOT NULL DEFAULT 0,
    display_order INT NOT NULL DEFAULT 0,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_layout_node ON t_ui_field_def(layout_node_id);
CREATE INDEX idx_field_path ON t_ui_field_def(field_path);

-- 字段组件绑定表（H2）
CREATE TABLE IF NOT EXISTS t_ui_field_component (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    field_def_id BIGINT NOT NULL,
    component_type VARCHAR(20) NOT NULL,
    label_name VARCHAR(100) NOT NULL,
    placeholder VARCHAR(200),
    data_provider_id BIGINT,
    component_config CLOB,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_field_def ON t_ui_field_component(field_def_id);

-- 动作配置表（H2）
CREATE TABLE IF NOT EXISTS t_ui_action_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    layout_node_id BIGINT,
    action_code VARCHAR(50) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    action_name VARCHAR(100) NOT NULL,
    action_config CLOB,
    display_order INT NOT NULL DEFAULT 0,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_by VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(50),
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_template_version ON t_ui_action_config(template_version_id);
```

- [ ] **Step 3：提交数据库表设计**

```bash
git add src/main/resources/db/schema-ui-config.sql
git add src/test/resources/schema-h2-ui-config.sql
git commit -m "feat: add UI config database schema (layout_node, field_def, field_component, action_config)"
```

---

### Task 1.2：拼音转换工具

**Files:**
- Create: `src/main/java/com/contract/common/util/PinyinUtils.java`
- Test: `src/test/java/com/contract/common/util/PinyinUtilsTest.java`

**目标：实现拼音转换工具，用于自动生成nodeCode/fieldCode**

- [ ] **Step 1：编写拼音转换工具类**

```java
// src/main/java/com/contract/common/util/PinyinUtils.java

package com.contract.common.util;

import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.extra.pinyin.engine.pinyin4j.Pinyin4jEngine;

/**
 * 拼音转换工具
 * 用于自动生成 nodeCode、fieldCode、actionCode
 */
public class PinyinUtils {

    private static final Pinyin4jEngine engine = new Pinyin4jEngine();

    /**
     * 中文转拼音（驼峰格式）
     * 
     * 示例：
     * - "基本信息" → "jiBenXiXi"
     * - "合同名称" → "heTongMingCheng"
     * 
     * @param chinese 中文字符串
     * @return 拼音驼峰格式
     */
    public static String toPinyinCamelCase(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return "";
        }

        // 使用Hutool拼音工具，首字母大写
        String pinyin = engine.getPinyinFirstLetter(chinese, "");
        
        // 转换为驼峰格式：首字母小写，后续首字母大写
        if (pinyin.length() > 0) {
            return pinyin.substring(0, 1).toLowerCase() + pinyin.substring(1);
        }
        
        return pinyin;
    }

    /**
     * 中文转拼音（下划线格式）
     * 
     * 示例：
     * - "基本信息" → "ji_ben_xi_xi"
     * - "合同名称" → "he_tong_ming_cheng"
     * 
     * @param chinese 中文字符串
     * @return 拼音下划线格式
     */
    public static String toPinyinSnakeCase(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return "";
        }

        // 使用Hutool拼音工具，全小写，下划线分隔
        return engine.getPinyinFirstLetter(chinese, "_").toLowerCase();
    }

    /**
     * 生成节点编码（驼峰格式）
     * 
     * @param nodeName 节点名称（中文）
     * @return nodeCode（拼音驼峰）
     */
    public static String generateNodeCode(String nodeName) {
        return toPinyinCamelCase(nodeName);
    }

    /**
     * 生成节点路径（拼接父节点路径）
     * 
     * @param parentNodePath 父节点路径
     * @param nodeCode 当前节点编码
     * @return nodePath（完整路径）
     */
    public static String generateNodePath(String parentNodePath, String nodeCode) {
        if (parentNodePath == null || parentNodePath.isEmpty()) {
            return nodeCode;
        }
        return parentNodePath + "." + nodeCode;
    }

    /**
     * 生成字段编码（驼峰格式）
     * 
     * @param fieldName 字段名称（中文）
     * @return fieldCode（拼音驼峰）
     */
    public static String generateFieldCode(String fieldName) {
        return toPinyinCamelCase(fieldName);
    }

    /**
     * 生成字段路径（拼接父节点路径）
     * 
     * @param parentNodePath 父节点路径
     * @param fieldCode 字段编码
     * @return fieldPath（完整路径）
     */
    public static String generateFieldPath(String parentNodePath, String fieldCode) {
        if (parentNodePath == null || parentNodePath.isEmpty()) {
            return fieldCode;
        }
        return parentNodePath + "." + fieldCode;
    }

    /**
     * 生成动作编码（驼峰格式）
     * 
     * @param actionName 动作名称（中文）
     * @return actionCode（拼音驼峰）
     */
    public static String generateActionCode(String actionName) {
        return toPinyinCamelCase(actionName);
    }
}
```

- [ ] **Step 2：编写拼音转换工具测试**

```java
// src/test/java/com/contract/common/util/PinyinUtilsTest.java

package com.contract.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 拼音转换工具测试
 */
class PinyinUtilsTest {

    @Test
    void testToPinyinCamelCase() {
        // 测试驼峰格式转换
        assertEquals("jiBenXiXi", PinyinUtils.toPinyinCamelCase("基本信息"));
        assertEquals("heTongMingCheng", PinyinUtils.toPinyinCamelCase("合同名称"));
        assertEquals("", PinyinUtils.toPinyinCamelCase(""));
        assertEquals("", PinyinUtils.toPinyinCamelCase(null));
    }

    @Test
    void testToPinyinSnakeCase() {
        // 测试下划线格式转换
        assertEquals("ji_ben_xi_xi", PinyinUtils.toPinyinSnakeCase("基本信息"));
        assertEquals("he_tong_ming_cheng", PinyinUtils.toPinyinSnakeCase("合同名称"));
        assertEquals("", PinyinUtils.toPinyinSnakeCase(""));
        assertEquals("", PinyinUtils.toPinyinSnakeCase(null));
    }

    @Test
    void testGenerateNodeCode() {
        // 测试节点编码生成
        assertEquals("jiBenXiXi", PinyinUtils.generateNodeCode("基本信息"));
        assertEquals("heTongTiaoKuan", PinyinUtils.generateNodeCode("合同条款"));
    }

    @Test
    void testGenerateNodePath() {
        // 测试节点路径生成
        assertEquals("jiBenXiXi", PinyinUtils.generateNodePath(null, "jiBenXiXi"));
        assertEquals("jiBenXiXi", PinyinUtils.generateNodePath("", "jiBenXiXi"));
        assertEquals("root.jiBenXiXi", PinyinUtils.generateNodePath("root", "jiBenXiXi"));
        assertEquals("root.jiBenXiXi.heTongMingCheng", 
            PinyinUtils.generateNodePath("root.jiBenXiXi", "heTongMingCheng"));
    }

    @Test
    void testGenerateFieldCode() {
        // 测试字段编码生成
        assertEquals("heTongMingCheng", PinyinUtils.generateFieldCode("合同名称"));
        assertEquals("heTongJinE", PinyinUtils.generateFieldCode("合同金额"));
    }

    @Test
    void testGenerateFieldPath() {
        // 测试字段路径生成
        assertEquals("heTongMingCheng", PinyinUtils.generateFieldPath(null, "heTongMingCheng"));
        assertEquals("jiBenXiXi.heTongMingCheng", 
            PinyinUtils.generateFieldPath("jiBenXiXi", "heTongMingCheng"));
        assertEquals("root.jiBenXiXi.heTongMingCheng", 
            PinyinUtils.generateFieldPath("root.jiBenXiXi", "heTongMingCheng"));
    }

    @Test
    void testGenerateActionCode() {
        // 测试动作编码生成
        assertEquals("baoCun", PinyinUtils.generateActionCode("保存"));
        assertEquals("chaXun", PinyinUtils.generateActionCode("查询"));
    }
}
```

- [ ] **Step 3：运行测试验证拼音转换**

```bash
mvn test -Dtest=PinyinUtilsTest
```

Expected output:
```
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
```

- [ ] **Step 4：提交拼音转换工具**

```bash
git add src/main/java/com/contract/common/util/PinyinUtils.java
git add src/test/java/com/contract/common/util/PinyinUtilsTest.java
git commit -m "feat: add PinyinUtils for auto-generating nodeCode/fieldCode/actionCode"
```

---

### Task 1.3：领域模型完善

**Files:**
- Modify: `src/main/java/com/contract/domain/template/FieldDef.java`
- Modify: `src/main/java/com/contract/domain/template/FieldComponent.java`
- Modify: `src/main/java/com/contract/domain/template/ActionConfig.java`
- Create: `src/main/java/com/contract/domain/template/LayoutNode.java`

**目标：完善领域模型，添加自动生成字段逻辑**

- [ ] **Step 1：完善LayoutNode领域模型**

```java
// src/main/java/com/contract/domain/template/LayoutNode.java

package com.contract.domain.template;

import com.contract.common.util.PinyinUtils;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 布局节点领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayoutNode {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private String nodeCode;        // 自动生成：jiBenXiXi（拼音驼峰）
    private String nodePath;        // 自动生成：root.jiBenXiXi（路径）
    private String nodeType;        // CARD_CONTAINER/SEPARATOR
    private String nodeName;        // 节点名称（中文）
    private Long parentNodeId;      // 父节点ID
    private Integer displayOrder;   // 显示顺序
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 创建布局节点（自动生成nodeCode/nodePath）
     */
    public static LayoutNode create(String nodeName, String nodeType, String parentNodePath) {
        LayoutNode node = new LayoutNode();
        node.setNodeName(nodeName);
        node.setNodeType(nodeType);
        
        // 自动生成nodeCode（拼音驼峰）
        node.setNodeCode(PinyinUtils.generateNodeCode(nodeName));
        
        // 自动生成nodePath（拼接父节点路径）
        node.setNodePath(PinyinUtils.generateNodePath(parentNodePath, node.getNodeCode()));
        
        return node;
    }
}
```

- [ ] **Step 2：完善FieldDef领域模型**

```java
// src/main/java/com/contract/domain/template/FieldDef.java（修改）

package com.contract.domain.template;

import com.contract.common.util.PinyinUtils;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 字段定义领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDef {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long layoutNodeId;
    private String fieldCode;        // 自动生成：heTongMingCheng（拼音驼峰）
    private String fieldPath;        // 自动生成：jiBenXiXi.heTongMingCheng（路径）
    private String fieldNameCn;      // 字段名称（中文）
    private String dataType;         // STRING/DECIMAL/DATE
    private Integer requiredDefault; // 0=可选, 1=必填
    private Integer queryEnabled;    // 0=无查询, 1=有查询（预留）
    private Integer displayOrder;    // 显示顺序
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 创建字段定义（自动生成fieldCode/fieldPath）
     */
    public static FieldDef create(String displayName, String dataType, String parentNodePath) {
        FieldDef fieldDef = new FieldDef();
        fieldDef.setFieldNameCn(displayName);
        fieldDef.setDataType(dataType);
        
        // 自动生成fieldCode（拼音驼峰）
        fieldDef.setFieldCode(PinyinUtils.generateFieldCode(displayName));
        
        // 自动生成fieldPath（拼接父节点路径）
        fieldDef.setFieldPath(PinyinUtils.generateFieldPath(parentNodePath, fieldDef.getFieldCode()));
        
        return fieldDef;
    }
}
```

- [ ] **Step 3：完善FieldComponent领域模型**

```java
// src/main/java/com/contract/domain/template/FieldComponent.java（修改）

package com.contract.domain.template;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 字段组件绑定领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldComponent {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long fieldDefId;
    private String componentType;    // TEXT_INPUT/MONEY_INPUT/DATE_PICKER/SELECT
    private String labelName;        // 显示名称
    private String placeholder;      // 占位符提示
    private Long dataProviderId;     // 数据源ID（下拉框绑定）
    private String componentConfig;  // JSONB扩展配置
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 创建字段组件绑定
     */
    public static FieldComponent create(Long fieldDefId, String componentType, String labelName) {
        FieldComponent component = new FieldComponent();
        component.setFieldDefId(fieldDefId);
        component.setComponentType(componentType);
        component.setLabelName(labelName);
        return component;
    }
}
```

- [ ] **Step 4：完善ActionConfig领域模型**

```java
// src/main/java/com/contract/domain/template/ActionConfig.java（修改）

package com.contract.domain.template;

import com.contract.common.util.PinyinUtils;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 动作配置领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionConfig {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long layoutNodeId;
    private String actionCode;       // 自动生成：baoCun（拼音驼峰）
    private String actionType;       // SAVE_BUTTON/QUERY_BUTTON/CUSTOM_BUTTON
    private String actionName;       // 动作名称（中文）
    private String actionConfig;     // JSONB扩展配置
    private Integer displayOrder;    // 显示顺序
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 创建动作配置（自动生成actionCode）
     */
    public static ActionConfig create(String actionName, String actionType) {
        ActionConfig config = new ActionConfig();
        config.setActionName(actionName);
        config.setActionType(actionType);
        
        // 自动生成actionCode（拼音驼峰）
        config.setActionCode(PinyinUtils.generateActionCode(actionName));
        
        return config;
    }
}
```

- [ ] **Step 5：提交领域模型完善**

```bash
git add src/main/java/com/contract/domain/template/LayoutNode.java
git add src/main/java/com/contract/domain/template/FieldDef.java
git add src/main/java/com/contract/domain/template/FieldComponent.java
git add src/main/java/com/contract/domain/template/ActionConfig.java
git commit -m "feat: enhance domain models with auto-generating code/path logic"
```

---

### Task 1.4：Entity和Mapper

**Files:**
- Create: `src/main/java/com/contract/adapter/persistence/entity/LayoutNodeEntity.java`
- Create: `src/main/java/com/contract/adapter/persistence/entity/FieldDefEntity.java`
- Create: `src/main/java/com/contract/adapter/persistence/entity/FieldComponentEntity.java`
- Create: `src/main/java/com/contract/adapter/persistence/entity/ActionConfigEntity.java`
- Create: `src/main/java/com/contract/adapter/persistence/mapper/LayoutNodeMapper.java`
- Create: `src/main/java/com/contract/adapter/persistence/mapper/FieldDefMapper.java`
- Create: `src/main/java/com/contract/adapter/persistence/mapper/FieldComponentMapper.java`
- Create: `src/main/java/com/contract/adapter/persistence/mapper/ActionConfigMapper.java`

**目标：创建Entity和Mapper**

- [ ] **Step 1：创建LayoutNodeEntity**

```java
// src/main/java/com/contract/adapter/persistence/entity/LayoutNodeEntity.java

package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 布局节点数据库实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ui_layout_node")
public class LayoutNodeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("template_id")
    private Long templateId;
    
    @TableField("template_version_id")
    private Long templateVersionId;
    
    @TableField("node_code")
    private String nodeCode;
    
    @TableField("node_path")
    private String nodePath;
    
    @TableField("node_type")
    private String nodeType;
    
    @TableField("node_name")
    private String nodeName;
    
    @TableField("parent_node_id")
    private Long parentNodeId;
    
    @TableField("display_order")
    private Integer displayOrder;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
    
    @TableField("create_by")
    private String createBy;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField("update_by")
    private String updateBy;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2：创建FieldDefEntity**

```java
// src/main/java/com/contract/adapter/persistence/entity/FieldDefEntity.java

package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 字段定义数据库实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ui_field_def")
public class FieldDefEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("template_id")
    private Long templateId;
    
    @TableField("template_version_id")
    private Long templateVersionId;
    
    @TableField("layout_node_id")
    private Long layoutNodeId;
    
    @TableField("field_code")
    private String fieldCode;
    
    @TableField("field_path")
    private String fieldPath;
    
    @TableField("field_name_cn")
    private String fieldNameCn;
    
    @TableField("data_type")
    private String dataType;
    
    @TableField("required_default")
    private Integer requiredDefault;
    
    @TableField("query_enabled")
    private Integer queryEnabled;
    
    @TableField("display_order")
    private Integer displayOrder;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
    
    @TableField("create_by")
    private String createBy;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField("update_by")
    private String updateBy;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 3：创建FieldComponentEntity**

```java
// src/main/java/com/contract/adapter/persistence/entity/FieldComponentEntity.java

package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 字段组件绑定数据库实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ui_field_component")
public class FieldComponentEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("template_id")
    private Long templateId;
    
    @TableField("template_version_id")
    private Long templateVersionId;
    
    @TableField("field_def_id")
    private Long fieldDefId;
    
    @TableField("component_type")
    private String componentType;
    
    @TableField("label_name")
    private String labelName;
    
    @TableField("placeholder")
    private String placeholder;
    
    @TableField("data_provider_id")
    private Long dataProviderId;
    
    @TableField("component_config")
    private String componentConfig;  // JSONB存储为String
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
    
    @TableField("create_by")
    private String createBy;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField("update_by")
    private String updateBy;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 4：创建ActionConfigEntity**

```java
// src/main/java/com/contract/adapter/persistence/entity/ActionConfigEntity.java

package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 动作配置数据库实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ui_action_config")
public class ActionConfigEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("template_id")
    private Long templateId;
    
    @TableField("template_version_id")
    private Long templateVersionId;
    
    @TableField("layout_node_id")
    private Long layoutNodeId;
    
    @TableField("action_code")
    private String actionCode;
    
    @TableField("action_type")
    private String actionType;
    
    @TableField("action_name")
    private String actionName;
    
    @TableField("action_config")
    private String actionConfig;  // JSONB存储为String
    
    @TableField("display_order")
    private Integer displayOrder;
    
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
    
    @TableField("create_by")
    private String createBy;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField("update_by")
    private String updateBy;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 5：创建Mapper接口**

```java
// src/main/java/com/contract/adapter/persistence/mapper/LayoutNodeMapper.java

package com.contract.adapter.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.adapter.persistence.entity.LayoutNodeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 布局节点Mapper
 */
@Mapper
public interface LayoutNodeMapper extends BaseMapper<LayoutNodeEntity> {
}
```

```java
// src/main/java/com/contract/adapter/persistence/mapper/FieldDefMapper.java

package com.contract.adapter.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.adapter.persistence.entity.FieldDefEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字段定义Mapper
 */
@Mapper
public interface FieldDefMapper extends BaseMapper<FieldDefEntity> {
}
```

```java
// src/main/java/com/contract/adapter/persistence/mapper/FieldComponentMapper.java

package com.contract.adapter.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.adapter.persistence.entity.FieldComponentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字段组件绑定Mapper
 */
@Mapper
public interface FieldComponentMapper extends BaseMapper<FieldComponentEntity> {
}
```

```java
// src/main/java/com/contract/adapter/persistence/mapper/ActionConfigMapper.java

package com.contract.adapter.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.adapter.persistence.entity.ActionConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动作配置Mapper
 */
@Mapper
public interface ActionConfigMapper extends BaseMapper<ActionConfigEntity> {
}
```

- [ ] **Step 6：提交Entity和Mapper**

```bash
git add src/main/java/com/contract/adapter/persistence/entity/LayoutNodeEntity.java
git add src/main/java/com/contract/adapter/persistence/entity/FieldDefEntity.java
git add src/main/java/com/contract/adapter/persistence/entity/FieldComponentEntity.java
git add src/main/java/com/contract/adapter/persistence/entity/ActionConfigEntity.java
git add src/main/java/com/contract/adapter/persistence/mapper/LayoutNodeMapper.java
git add src/main/java/com/contract/adapter/persistence/mapper/FieldDefMapper.java
git add src/main/java/com/contract/adapter/persistence/mapper/FieldComponentMapper.java
git add src/main/java/com/contract/adapter/persistence/mapper/ActionConfigMapper.java
git commit -m "feat: add Entity and Mapper for UI config tables"
```

---

## Phase 2：后端API实现（可并行执行）

### Task 2.1：LayoutNode Service + Controller

**Files:**
- Create: `src/main/java/com/contract/application/template/LayoutNodeService.java`
- Modify: `src/main/java/com/contract/adapter/controller/LayoutNodeController.java`
- Test: `src/test/java/com/contract/application/template/LayoutNodeServiceTest.java`

**目标：实现布局节点CRUD API**

- [ ] **Step 1：编写LayoutNodeService**

```java
// src/main/java/com/contract/application/template/LayoutNodeService.java（完整实现）

package com.contract.application.template;

import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.LayoutNodeCreateDTO;
import com.contract.application.template.dto.LayoutNodeUpdateDTO;
import com.contract.application.template.convert.LayoutNodeConverter;
import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.repository.LayoutNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 布局节点应用服务
 */
@Service
@RequiredArgsConstructor
public class LayoutNodeService {
    private final LayoutNodeRepository repository;
    private final LayoutNodeConverter converter;

    /**
     * 创建布局节点（自动生成nodeCode/nodePath）
     */
    @Transactional
    public LayoutNodeDTO create(Long templateId, Long versionId, LayoutNodeCreateDTO dto) {
        // 获取父节点路径
        String parentNodePath = null;
        if (dto.getParentNodeId() != null) {
            LayoutNode parent = repository.findById(dto.getParentNodeId());
            if (parent != null) {
                parentNodePath = parent.getNodePath();
            }
        }

        // 创建布局节点（自动生成nodeCode/nodePath）
        LayoutNode node = LayoutNode.create(dto.getNodeName(), dto.getNodeType(), parentNodePath);
        node.setTemplateId(templateId);
        node.setTemplateVersionId(versionId);
        node.setParentNodeId(dto.getParentNodeId());
        node.setDisplayOrder(dto.getDisplayOrder());

        // 保存到数据库
        LayoutNode saved = repository.save(node);

        return converter.toDTO(saved);
    }

    /**
     * 根据ID查询布局节点
     */
    public LayoutNodeDTO getById(Long id) {
        LayoutNode node = repository.findById(id);
        return converter.toDTO(node);
    }

    /**
     * 根据模板版本ID查询布局节点列表
     */
    public List<LayoutNodeDTO> listByVersionId(Long versionId) {
        List<LayoutNode> nodes = repository.findByVersionId(versionId);
        return nodes.stream()
            .map(converter::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * 更新布局节点
     */
    @Transactional
    public LayoutNodeDTO update(Long id, LayoutNodeUpdateDTO dto) {
        LayoutNode node = repository.findById(id);
        if (node == null) {
            throw new RuntimeException("LayoutNode not found: " + id);
        }

        // 更新属性
        node.setNodeName(dto.getNodeName());
        node.setDisplayOrder(dto.getDisplayOrder());

        LayoutNode updated = repository.save(node);
        return converter.toDTO(updated);
    }
}
```

- [ ] **Step 2：编写LayoutNodeConverter**

```java
// src/main/java/com/contract/application/template/convert/LayoutNodeConverter.java（完整实现）

package com.contract.application.template.convert;

import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.domain.template.LayoutNode;
import com.contract.adapter.persistence.entity.LayoutNodeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

/**
 * 布局节点转换器
 */
@Mapper(componentModel = "spring")
public interface LayoutNodeConverter {

    // Entity → Domain
    LayoutNode toDomain(LayoutNodeEntity entity);

    // Domain → Entity
    LayoutNodeEntity toEntity(LayoutNode domain);

    // Domain → DTO
    LayoutNodeDTO toDTO(LayoutNode domain);

    // List转换
    List<LayoutNodeDTO> toDTOList(List<LayoutNode> domains);
}
```

- [ ] **Step 3：编写LayoutNodeRepository接口**

```java
// src/main/java/com/contract/domain/template/repository/LayoutNodeRepository.java

package com.contract.domain.template.repository;

import com.contract.domain.template.LayoutNode;
import java.util.List;

/**
 * 布局节点仓储接口
 */
public interface LayoutNodeRepository {
    
    LayoutNode save(LayoutNode node);
    
    LayoutNode findById(Long id);
    
    List<LayoutNode> findByVersionId(Long versionId);
    
    List<LayoutNode> findByParentNodeId(Long parentNodeId);
}
```

- [ ] **Step 4：编写LayoutNodeRepositoryImpl**

```java
// src/main/java/com/contract/infrastructure/persistence/repository/LayoutNodeRepositoryImpl.java

package com.contract.infrastructure.persistence.repository;

import com.contract.adapter.persistence.entity.LayoutNodeEntity;
import com.contract.adapter.persistence.mapper.LayoutNodeMapper;
import com.contract.application.template.convert.LayoutNodeConverter;
import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.repository.LayoutNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 布局节点仓储实现
 */
@Repository
@RequiredArgsConstructor
public class LayoutNodeRepositoryImpl implements LayoutNodeRepository {
    private final LayoutNodeMapper mapper;
    private final LayoutNodeConverter converter;

    @Override
    public LayoutNode save(LayoutNode node) {
        LayoutNodeEntity entity = converter.toEntity(node);
        if (node.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return converter.toDomain(entity);
    }

    @Override
    public LayoutNode findById(Long id) {
        LayoutNodeEntity entity = mapper.selectById(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<LayoutNode> findByVersionId(Long versionId) {
        List<LayoutNodeEntity> entities = mapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LayoutNodeEntity>()
                .eq(LayoutNodeEntity::getTemplateVersionId, versionId)
                .orderByAsc(LayoutNodeEntity::getDisplayOrder)
        );
        return entities.stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<LayoutNode> findByParentNodeId(Long parentNodeId) {
        List<LayoutNodeEntity> entities = mapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LayoutNodeEntity>()
                .eq(LayoutNodeEntity::getParentNodeId, parentNodeId)
                .orderByAsc(LayoutNodeEntity::getDisplayOrder)
        );
        return entities.stream()
            .map(converter::toDomain)
            .collect(Collectors.toList());
    }
}
```

- [ ] **Step 5：提交LayoutNode Service**

```bash
git add src/main/java/com/contract/application/template/LayoutNodeService.java
git add src/main/java/com/contract/application/template/convert/LayoutNodeConverter.java
git add src/main/java/com/contract/domain/template/repository/LayoutNodeRepository.java
git add src/main/java/com/contract/infrastructure/persistence/repository/LayoutNodeRepositoryImpl.java
git commit -m "feat: implement LayoutNode Service with auto-generating nodeCode/nodePath"
```

---

### Task 2.2：FieldDef Service + Controller

**Files:**
- Create: `src/main/java/com/contract/application/template/FieldDefService.java`
- Modify: `src/main/java/com/contract/adapter/controller/FieldDefController.java`
- Test: `src/test/java/com/contract/application/template/FieldDefServiceTest.java`

**目标：实现字段定义CRUD API（可并行执行，类似Task 2.1）**

（代码结构类似Task 2.1，独立实现）

---

### Task 2.3：FieldComponent Service + Controller

**Files:**
- Create: `src/main/java/com/contract/application/template/FieldComponentService.java`
- Modify: `src/main/java/com/contract/adapter/controller/FieldComponentController.java`
- Test: `src/test/java/com/contract/application/template/FieldComponentServiceTest.java`

**目标：实现字段组件绑定CRUD API（可并行执行）**

（代码结构类似Task 2.1，独立实现）

---

### Task 2.4：ActionConfig Service + Controller

**Files:**
- Create: `src/main/java/com/contract/application/template/ActionConfigService.java`
- Modify: `src/main/java/com/contract/adapter/controller/ActionConfigController.java`
- Test: `src/test/java/com/contract/application/template/ActionConfigServiceTest.java`

**目标：实现动作配置CRUD API（可并行执行）**

（代码结构类似Task 2.1，独立实现）

---

### Task 2.5：Schema API（完整配置树）

**Files:**
- Create: `src/main/java/com/contract/application/template/SchemaService.java`
- Create: `src/main/java/com/contract/adapter/controller/SchemaController.java`
- Create: `src/main/java/com/contract/application/template/dto/SchemaDTO.java`

**目标：实现Schema API（获取完整配置树）**

- [ ] **Step 1：编写SchemaDTO**

```java
// src/main/java/com/contract/application/template/dto/SchemaDTO.java

package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import java.util.List;

/**
 * 完整配置树DTO
 */
@Data
@Builder
public class SchemaDTO {
    private Long templateId;
    private Long versionId;
    private List<LayoutNodeDTO> layoutNodes;
    private List<ActionConfigDTO> actionConfigs;
}
```

- [ ] **Step 2：编写SchemaService**

```java
// src/main/java/com/contract/application/template/SchemaService.java

package com.contract.application.template;

import com.contract.application.template.dto.SchemaDTO;
import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.FieldDefDTO;
import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.ActionConfigDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Schema应用服务
 */
@Service
@RequiredArgsConstructor
public class SchemaService {
    private final LayoutNodeService layoutNodeService;
    private final FieldDefService fieldDefService;
    private final FieldComponentService fieldComponentService;
    private final ActionConfigService actionConfigService;

    /**
     * 获取完整配置树
     */
    public SchemaDTO getSchema(Long templateId, Long versionId) {
        // 1. 获取所有布局节点
        List<LayoutNodeDTO> layoutNodes = layoutNodeService.listByVersionId(versionId);

        // 2. 为每个布局节点加载字段
        layoutNodes.forEach(node -> {
            List<FieldDefDTO> fields = fieldDefService.listByLayoutNodeId(node.getId());
            
            // 为每个字段加载组件绑定
            fields.forEach(field -> {
                FieldComponentDTO component = fieldComponentService.getByFieldDefId(field.getId());
                field.setComponent(component);
            });
            
            node.setChildren(fields);
        });

        // 3. 获取所有动作配置
        List<ActionConfigDTO> actionConfigs = actionConfigService.listByVersionId(versionId);

        return SchemaDTO.builder()
            .templateId(templateId)
            .versionId(versionId)
            .layoutNodes(layoutNodes)
            .actionConfigs(actionConfigs)
            .build();
    }
}
```

- [ ] **Step 3：编写SchemaController**

```java
// src/main/java/com/contract/adapter/controller/SchemaController.java

package com.contract.adapter.controller;

import com.contract.application.template.SchemaService;
import com.contract.application.template.dto.SchemaDTO;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Schema API（完整配置树）
 */
@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}/schema")
@RequiredArgsConstructor
public class SchemaController {
    private final SchemaService service;

    /**
     * 获取完整配置树
     */
    @GetMapping
    public Result<SchemaDTO> getSchema(
        @PathVariable Long templateId,
        @PathVariable Long versionId
    ) {
        SchemaDTO schema = service.getSchema(templateId, versionId);
        return Result.ok(schema);
    }
}
```

- [ ] **Step 4：提交Schema API**

```bash
git add src/main/java/com/contract/application/template/SchemaService.java
git add src/main/java/com/contract/adapter/controller/SchemaController.java
git add src/main/java/com/contract/application/template/dto/SchemaDTO.java
git commit -m "feat: implement Schema API for getting complete config tree"
```

---

### Task 2.6：DataProvider增强（临时数据源）

**Files:**
- Modify: `src/main/java/com/contract/application/template/DataProviderService.java`

**目标：实现临时DataProvider创建逻辑（业务自定义选项）**

- [ ] **Step 1：增强DataProviderService**

```java
// src/main/java/com/contract/application/template/DataProviderService.java（添加方法）

/**
 * 创建临时DataProvider（业务自定义选项）
 */
@Transactional
public DataProviderDTO createTempProvider(String fieldCode, String fieldName, String customOptionsJson) {
    // 临时DataProvider编码：field_{fieldCode}_static
    String tempCode = "field_" + fieldCode + "_static";
    
    // 临时DataProvider名称：{字段名称}-临时数据源
    String tempName = fieldName + "-临时数据源";
    
    DataProvider provider = DataProvider.builder()
        .providerCode(tempCode)
        .providerName(tempName)
        .providerType("TEMP_STATIC")
        .configJson(customOptionsJson)
        .status("ENABLED")
        .build();
    
    DataProvider saved = repository.save(provider);
    return converter.toDTO(saved);
}
```

- [ ] **Step 2：提交DataProvider增强**

```bash
git add src/main/java/com/contract/application/template/DataProviderService.java
git commit -m "feat: add temporary DataProvider creation for custom options"
```

---

## Phase 3：前端界面实现（可并行执行）

### Task 3.1：config.html（配置主界面）

**Files:**
- Create: `src/main/resources/static/config/config.html`
- Create: `src/main/resources/static/config/css/config.css`
- Create: `src/main/resources/static/config/js/config.js`

**目标：实现配置主界面（三栏布局）**

（前端HTML/CSS/JS代码，约200行，包含实时保存逻辑）

---

### Task 3.2：preview.html（预览界面）

**Files:**
- Create: `src/main/resources/static/config/preview.html`
- Create: `src/main/resources/static/config/js/preview.js`

**目标：实现预览界面（动态渲染）**

（前端HTML/JS代码，约150行，包含Schema API调用和动态渲染逻辑）

---

### Task 3.3：data-viewer.html（数据验证界面）

**Files:**
- Create: `src/main/resources/static/config/data-viewer.html`
- Create: `src/main/resources/static/config/js/data-viewer.js`

**目标：实现数据验证界面（三列展示）**

（前端HTML/JS代码，约150行，包含数据展示和验证逻辑）

---

## Phase 4：测试验证（可并行执行）

### Task 4.1：Repository层测试（H2）

**Files:**
- Test: `src/test/java/com/contract/infrastructure/persistence/repository/LayoutNodeRepositoryTest.java`
- Test: `src/test/java/com/contract/infrastructure/persistence/repository/FieldDefRepositoryTest.java`
- Test: `src/test/java/com/contract/infrastructure/persistence/repository/FieldComponentRepositoryTest.java`
- Test: `src/test/java/com/contract/infrastructure/persistence/repository/ActionConfigRepositoryTest.java`

**目标：Repository层H2数据库测试**

（测试代码，验证自动生成字段正确性）

---

### Task 4.2：Service层测试（Mock）

**Files:**
- Test: `src/test/java/com/contract/application/template/LayoutNodeServiceTest.java`
- Test: `src/test/java/com/contract/application/template/FieldDefServiceTest.java`
- Test: `src/test/java/com/contract/application/template/FieldComponentServiceTest.java`
- Test: `src/test/java/com/contract/application/template/ActionConfigServiceTest.java`
- Test: `src/test/java/com/contract/application/template/SchemaServiceTest.java`

**目标：Service层Mock测试**

（测试代码，验证业务逻辑）

---

### Task 4.3：Controller层测试（MockMvc）

**Files:**
- Test: `src/test/java/com/contract/adapter/controller/LayoutNodeControllerTest.java`
- Test: `src/test/java/com/contract/adapter/controller/FieldDefControllerTest.java`
- Test: `src/test/java/com/contract/adapter/controller/FieldComponentControllerTest.java`
- Test: `src/test/java/com/contract/adapter/controller/ActionConfigControllerTest.java`
- Test: `src/test/java/com/contract/adapter/controller/SchemaControllerTest.java`

**目标：Controller层MockMvc测试**

（测试代码，验证HTTP接口）

---

### Task 4.4：E2E验证测试

**Files:**
- Test: `src/test/java/com/contract/e2e/ConfigInterfaceE2ETest.java`

**目标：E2E验证测试（完整闭环）**

（测试代码，验证配置 → 入库 → 验证 → 预览 → 渲染完整流程）

---

## 执行顺序建议

**Phase 1（基础设施）**：4个独立任务，可并行执行
- Task 1.1：数据库表设计
- Task 1.2：拼音转换工具
- Task 1.3：领域模型完善
- Task 1.4：Entity和Mapper

**Phase 2（后端API）**：6个独立任务，可并行执行
- Task 2.1：LayoutNode Service
- Task 2.2：FieldDef Service
- Task 2.3：FieldComponent Service
- Task 2.4：ActionConfig Service
- Task 2.5：Schema API
- Task 2.6：DataProvider增强

**Phase 3（前端界面）**：3个独立任务，可并行执行
- Task 3.1：config.html
- Task 3.2：preview.html
- Task 3.3：data-viewer.html

**Phase 4（测试验证）**：4个独立任务，可并行执行
- Task 4.1：Repository层测试
- Task 4.2：Service层测试
- Task 4.3：Controller层测试
- Task 4.4：E2E验证测试

---

**计划完成，使用 subagent-driven-development 执行。**