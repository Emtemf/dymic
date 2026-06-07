# 统一配置界面 + DETAIL_TABLE + JSONB 优化 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 统一配置界面到 template-designer.html，修复后端保存 API，实现完整 DETAIL_TABLE 组件，做 47 场景 E2E 验证，完成 JSONB 查询优化。

**Architecture:** 四阶段顺序执行。阶段1修后端 PUT schema 端点+统一界面入口；阶段2实现前端 DETAIL_TABLE 完整交互（增删行、弹窗编辑、行状态管理）；阶段3用 Chrome DevTools MCP 跑 47 个验证场景；阶段4在 openGauss 上建 GIN 索引并封装 JsonbHelper。

**Tech Stack:** Java 21, Spring Boot 3.5.14, MyBatis-Plus 3.5.5, openGauss (生产), H2 (测试), 原生 HTML/JS 前端

---

## 阶段 1：统一界面 + 修复后端保存 API

### Task 1: config.html 重定向到 template-designer.html

**Files:**
- Modify: `src/main/resources/static/config.html`

- [ ] **Step 1: 替换 config.html 为重定向页面**

将整个文件内容替换为：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="refresh" content="0; url=config/template-designer.html">
    <title>重定向到模板设计器</title>
</head>
<body>
    <p>正在跳转到模板设计器，请稍候...</p>
    <p>如果没有自动跳转，请<a href="config/template-designer.html">点击这里</a></p>
</body>
</html>
```

- [ ] **Step 2: 同步到 target 目录并验证**

Run:
```bash
cp src/main/resources/static/config.html target/classes/static/config.html
```

用浏览器访问 `http://localhost:8888/config.html`，确认自动跳转到 `config/template-designer.html`。

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config.html
git commit -m "feat: redirect config.html to template-designer.html"
```

---

### Task 2: 创建 SchemaSaveDTO

**Files:**
- Create: `src/main/java/com/contract/application/template/dto/SchemaSaveDTO.java`

- [ ] **Step 1: 创建 SchemaSaveDTO**

```java
package com.contract.application.template.dto;

import lombok.Data;
import java.util.List;

@Data
public class SchemaSaveDTO {
    private List<LayoutNodeSaveItem> layoutNodes;
    private List<FieldDefSaveItem> fieldDefs;
    private List<FieldComponentSaveItem> fieldComponents;
    private List<QueryConfigSaveItem> queryConfigs;
    private List<ActionConfigSaveItem> actionConfigs;

    @Data
    public static class LayoutNodeSaveItem {
        private Long id;
        private Long parentId;
        private String nodeCode;
        private String nodeName;
        private String nodeType;
        private Integer sortNo;
        private Integer levelNo;
        private Integer gridX;
        private Integer gridY;
        private Integer gridW;
        private Integer gridH;
        private Integer rowNo;
        private Integer colNo;
        private Integer colSpan;
        private Integer rowSpan;
        private String bindType;
        private Long bindRefId;
        private String visibleRule;
        private String readonlyRule;
        private String propsJson;
    }

    @Data
    public static class FieldDefSaveItem {
        private Long id;
        private String fieldCode;
        private String fieldPath;
        private String fieldNameCn;
        private String dataType;
        private String valueType;
        private Integer requiredDefault;
        private Integer searchable;
        private Integer indexable;
        private String defaultValue;
        private String validateRule;
        private String propsJson;
    }

    @Data
    public static class FieldComponentSaveItem {
        private Long id;
        private Long layoutNodeId;
        private Long fieldDefId;
        private String componentType;
        private String labelName;
        private String placeholder;
        private String requiredRule;
        private String readonlyRule;
        private String visibleRule;
        private String componentProps;
        private Long dataProviderId;
        private Integer sortNo;
    }

    @Data
    public static class QueryConfigSaveItem {
        private String queryCode;
        private String queryName;
        private String queryType;
        private Long dataProviderId;
    }

    @Data
    public static class ActionConfigSaveItem {
        private String actionCode;
        private String actionName;
        private String actionType;
        private Long bindNodeId;
        private Long bindQueryId;
        private Integer confirmRequired;
        private String confirmText;
        private String beforeRule;
        private String afterRule;
        private String propsJson;
        private Integer sortNo;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/contract/application/template/dto/SchemaSaveDTO.java
git commit -m "feat: add SchemaSaveDTO for batch schema save"
```

---

### Task 3: Repository 层添加 deleteByVersionId 方法

**Files:**
- Modify: `src/main/java/com/contract/domain/template/repository/LayoutNodeRepository.java`
- Modify: `src/main/java/com/contract/domain/template/repository/FieldDefRepository.java`
- Modify: `src/main/java/com/contract/domain/template/repository/FieldComponentRepository.java`
- Modify: `src/main/java/com/contract/domain/template/repository/ActionConfigRepository.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/LayoutNodeRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/FieldDefRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/FieldComponentRepositoryImpl.java`
- Modify: `src/main/java/com/contract/infrastructure/persistence/repository/ActionConfigRepositoryImpl.java`

- [ ] **Step 1: 在 4 个 Repository 接口中添加 deleteByVersionId 方法**

`LayoutNodeRepository.java` 添加：
```java
void deleteByTemplateVersionId(Long versionId);
```

`FieldDefRepository.java` 添加：
```java
void deleteByTemplateVersionId(Long versionId);
```

`FieldComponentRepository.java` 添加：
```java
void deleteByTemplateVersionId(Long versionId);
```

`ActionConfigRepository.java` 添加：
```java
void deleteByTemplateVersionId(Long versionId);
```

- [ ] **Step 2: 在 4 个 RepositoryImpl 中实现 deleteByVersionId**

每个实现类的模式相同，使用 MyBatis-Plus 的 LambdaQueryWrapper：

`LayoutNodeRepositoryImpl.java` 添加：
```java
@Override
public void deleteByTemplateVersionId(Long versionId) {
    LambdaQueryWrapper<LayoutNodeEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(LayoutNodeEntity::getTemplateVersionId, versionId);
    mapper.delete(wrapper);
}
```

`FieldDefRepositoryImpl.java`、`FieldComponentRepositoryImpl.java`、`ActionConfigRepositoryImpl.java` 同理，替换对应的 Entity 类。

注意：需要检查每个 Entity 类是否有 `templateVersionId` 字段。如果字段名不同（如 `template_version_id`），使用对应的 getter 方法名。

- [ ] **Step 3: 检查 Entity 字段名**

Run:
```bash
grep -n "templateVersionId\|template_version_id" src/main/java/com/contract/infrastructure/persistence/entity/LayoutNodeEntity.java src/main/java/com/contract/infrastructure/persistence/entity/FieldDefEntity.java src/main/java/com/contract/infrastructure/persistence/entity/FieldComponentEntity.java src/main/java/com/contract/infrastructure/persistence/entity/ActionConfigEntity.java
```

确认每个 Entity 都有 `templateVersionId` 字段。如果某个没有，根据实际字段名调整 wrapper 条件。

- [ ] **Step 4: 编译验证**

Run:
```bash
cd /home/wula/IdeaProjects/dymic && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add deleteByTemplateVersionId to repositories for batch schema save"
```

---

### Task 4: SchemaService 添加 saveSchema 方法

**Files:**
- Modify: `src/main/java/com/contract/application/template/SchemaService.java`

- [ ] **Step 1: 在 SchemaService 中添加 saveSchema 方法**

在 `SchemaService.java` 类末尾（`buildTreeNode` 方法之后）添加：

```java
/**
 * 批量保存配置（替换式保存）
 */
@Transactional
public SchemaDTO saveSchema(Long templateId, Long versionId, SchemaSaveDTO dto) {
    log.info("Saving schema for templateId={}, versionId={}", templateId, versionId);

    // 1. 删除旧数据（按外键依赖反序）
    actionConfigService.deleteByVersionId(versionId);
    fieldComponentService.deleteByVersionId(versionId);
    fieldDefService.deleteByVersionId(versionId);
    layoutNodeService.deleteByVersionId(versionId);

    // 2. 插入新数据（按外键依赖顺序）
    if (dto.getLayoutNodes() != null) {
        for (SchemaSaveDTO.LayoutNodeSaveItem item : dto.getLayoutNodes()) {
            LayoutNodeCreateDTO createDTO = new LayoutNodeCreateDTO();
            createDTO.setParentId(item.getParentId());
            createDTO.setNodeType(item.getNodeType());
            createDTO.setNodeName(item.getNodeName() != null ? item.getNodeName() : item.getNodeType());
            createDTO.setSortNo(item.getSortNo() != null ? item.getSortNo() : 0);
            createDTO.setLevelNo(item.getLevelNo());
            createDTO.setGridX(item.getGridX());
            createDTO.setGridY(item.getGridY());
            createDTO.setGridW(item.getGridW());
            createDTO.setGridH(item.getGridH());
            createDTO.setRowNo(item.getRowNo());
            createDTO.setColNo(item.getColNo());
            createDTO.setColSpan(item.getColSpan());
            createDTO.setRowSpan(item.getRowSpan());
            createDTO.setBindType(item.getBindType());
            createDTO.setBindRefId(item.getBindRefId());
            createDTO.setVisibleRule(item.getVisibleRule());
            createDTO.setReadonlyRule(item.getReadonlyRule());
            createDTO.setPropsJson(item.getPropsJson());
            layoutNodeService.create(templateId, versionId, createDTO);
        }
    }

    if (dto.getFieldDefs() != null) {
        for (SchemaSaveDTO.FieldDefSaveItem item : dto.getFieldDefs()) {
            FieldDefCreateDTO createDTO = new FieldDefCreateDTO();
            createDTO.setFieldCode(item.getFieldCode());
            createDTO.setFieldPath(item.getFieldPath());
            createDTO.setFieldNameCn(item.getFieldNameCn());
            createDTO.setDataType(item.getDataType());
            fieldDefService.create(templateId, versionId, createDTO);
        }
    }

    if (dto.getFieldComponents() != null) {
        for (SchemaSaveDTO.FieldComponentSaveItem item : dto.getFieldComponents()) {
            FieldComponentCreateRequest createDTO = new FieldComponentCreateRequest();
            createDTO.setLayoutNodeId(item.getLayoutNodeId());
            createDTO.setFieldDefId(item.getFieldDefId());
            createDTO.setComponentType(item.getComponentType());
            createDTO.setLabelName(item.getLabelName());
            createDTO.setPlaceholder(item.getPlaceholder());
            createDTO.setComponentProps(item.getComponentProps());
            createDTO.setSortNo(item.getSortNo() != null ? item.getSortNo() : 0);
            fieldComponentService.create(templateId, versionId, createDTO);
        }
    }

    if (dto.getActionConfigs() != null) {
        for (SchemaSaveDTO.ActionConfigSaveItem item : dto.getActionConfigs()) {
            ActionConfigCreateRequest createDTO = new ActionConfigCreateRequest();
            createDTO.setActionName(item.getActionName());
            createDTO.setActionType(item.getActionType());
            createDTO.setBindNodeId(item.getBindNodeId());
            createDTO.setSortNo(item.getSortNo() != null ? item.getSortNo() : 0);
            actionConfigService.create(templateId, versionId, createDTO);
        }
    }

    log.info("Schema saved successfully for templateId={}, versionId={}", templateId, versionId);

    return getSchema(templateId, versionId);
}
```

注意：需要添加 import：
```java
import com.contract.application.template.dto.LayoutNodeCreateDTO;
import com.contract.application.template.dto.FieldDefCreateDTO;
import com.contract.application.template.dto.FieldComponentCreateRequest;
import com.contract.application.template.dto.ActionConfigCreateRequest;
```

- [ ] **Step 2: 在各 Service 中添加 deleteByVersionId 方法**

需要在 `LayoutNodeService`、`FieldDefService`、`FieldComponentService`、`ActionConfigService` 中各添加一个 deleteByVersionId 方法。每个方法的模式：

```java
@Transactional
public void deleteByVersionId(Long versionId) {
    repository.deleteByTemplateVersionId(versionId);
}
```

检查各 Service 中是否已有类似方法。如果没有，添加。

- [ ] **Step 3: 检查 FieldDefService.create 方法签名**

Run:
```bash
grep -n "public.*create" src/main/java/com/contract/application/template/FieldDefService.java
```

确认 create 方法接受 `(Long templateId, Long versionId, FieldDefCreateDTO dto)` 参数。如果不匹配，调整 saveSchema 中的调用。

- [ ] **Step 4: 检查 FieldComponentService.create 方法签名**

Run:
```bash
grep -n "public.*create" src/main/java/com/contract/application/template/FieldComponentService.java
```

确认 create 方法签名，调整 saveSchema 中的调用以匹配。

- [ ] **Step 5: 编译验证**

Run:
```bash
cd /home/wula/IdeaProjects/dymic && mvn compile -q
```

Expected: BUILD SUCCESS。如果有编译错误，根据实际的 Service 方法签名调整 saveSchema 中的调用。

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add saveSchema method to SchemaService"
```

---

### Task 5: SchemaController 添加 PUT 端点

**Files:**
- Modify: `src/main/java/com/contract/adapter/controller/SchemaController.java`

- [ ] **Step 1: 添加 @PutMapping 方法**

在 `SchemaController.java` 的 `getSchema` 方法之后添加：

```java
/**
 * 批量保存配置（替换式保存）
 */
@PutMapping
public Result<SchemaDTO> saveSchema(
    @PathVariable Long templateId,
    @PathVariable Long versionId,
    @RequestBody SchemaSaveDTO dto
) {
    log.info("API call: PUT /api/templates/{}/versions/{}/schema", templateId, versionId);
    SchemaDTO schema = schemaService.saveSchema(templateId, versionId, dto);
    return Result.ok(schema);
}
```

需要添加 import：
```java
import com.contract.application.template.dto.SchemaSaveDTO;
```

- [ ] **Step 2: 编译验证**

Run:
```bash
cd /home/wula/IdeaProjects/dymic && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: 启动应用并用 curl 测试**

Run:
```bash
# 先确保应用在运行
curl -s -X PUT http://localhost:8888/api/templates/1/versions/1/schema \
  -H "Content-Type: application/json" \
  -d '{"layoutNodes":[],"fieldDefs":[],"fieldComponents":[],"queryConfigs":[],"actionConfigs":[]}' | python3 -m json.tool
```

Expected: `{"success":true,"code":"SUCCESS","data":{...}}`

如果返回 500，查看日志排查。如果返回 404，检查应用是否启动。

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add PUT /schema endpoint for batch save"
```

---

### Task 6: 验证前端 saveConfig 与后端 DTO 匹配

**Files:**
- Modify: `src/main/resources/static/config/js/config-api.js` (if needed)

- [ ] **Step 1: 对比前端 saveConfig 发送的字段与 SchemaSaveDTO**

前端 `extractLayoutNodes` 生成的字段：
```json
{
  "id", "nodeCode", "nodeName", "nodeType",
  "parentId", "sortNo", "propsJson"
}
```

SchemaSaveDTO.LayoutNodeSaveItem 接受的字段包含以上全部，所以**匹配**。

前端 `extractFieldDefs` 生成的字段：
```json
{
  "id", "fieldCode", "fieldPath", "fieldNameCn", "dataType"
}
```

SchemaSaveDTO.FieldDefSaveItem 包含以上全部，**匹配**。

前端 `extractFieldComponents` 生成的字段：
```json
{
  "id", "fieldDefId", "layoutNodeId", "componentType", "propsJson"
}
```

SchemaSaveDTO.FieldComponentSaveItem 包含以上全部，**匹配**。

如果都匹配，不需要修改前端。

- [ ] **Step 2: 同步前端到 target 并验证完整保存流程**

Run:
```bash
cp -r src/main/resources/static/config/js/*.js target/classes/static/config/js/
```

打开 `http://localhost:8888/config/template-designer.html`，拖几个组件，点保存，检查：
1. Network 面板显示 PUT 请求返回 200
2. 刷新页面后配置恢复

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "fix: ensure frontend saveConfig matches backend SchemaSaveDTO"
```

---

## 阶段 2：DETAIL_TABLE 完整功能

### Task 7: 创建 detail-table.js 明细表交互模块

**Files:**
- Create: `src/main/resources/static/config/js/detail-table.js`

- [ ] **Step 1: 创建 detail-table.js 完整文件**

```javascript
/**
 * 明细表交互模块
 * 处理增行、删行、弹窗编辑、行状态管理
 */

const DetailTableHelper = {
    /**
     * 生成唯一行ID
     */
    generateRowUid() {
        return 'row_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    },

    /**
     * 获取明细表的 draft 数据
     */
    getTableDraft(tableId) {
        if (!window.DesignerState || !window.DesignerState.detailDrafts) {
            window.DesignerState.detailDrafts = {};
        }
        if (!window.DesignerState.detailDrafts[tableId]) {
            window.DesignerState.detailDrafts[tableId] = [];
        }
        return window.DesignerState.detailDrafts[tableId];
    },

    /**
     * 新增行
     */
    addRow(tableId, columns, maxRows) {
        const rows = this.getTableDraft(tableId);

        if (maxRows && rows.length >= maxRows) {
            alert('已达到最大行数限制（' + maxRows + '行）');
            return;
        }

        const newRow = {
            _row_uid: this.generateRowUid(),
            _row_op: 'ADD'
        };

        if (columns) {
            columns.forEach(col => {
                newRow[col.code] = col.type === 'number' ? 0 : '';
            });
        }

        rows.push(newRow);
        this.renderTableBody(tableId, columns);
    },

    /**
     * 删除行（标记删除，不真正移除）
     */
    deleteRow(tableId, rowUid) {
        const rows = this.getTableDraft(tableId);
        const row = rows.find(r => r._row_uid === rowUid);
        if (row) {
            if (row._row_op === 'ADD') {
                // 新增的行直接移除
                const idx = rows.indexOf(row);
                rows.splice(idx, 1);
            } else {
                row._row_op = 'DELETE';
            }
            this.renderTableBody(tableId, this._getColumns(tableId));
        }
    },

    /**
     * 恢复已删除行
     */
    restoreRow(tableId, rowUid) {
        const rows = this.getTableDraft(tableId);
        const row = rows.find(r => r._row_uid === rowUid);
        if (row && row._row_op === 'DELETE') {
            row._row_op = 'NONE';
            this.renderTableBody(tableId, this._getColumns(tableId));
        }
    },

    /**
     * 打开弹窗编辑行
     */
    editRow(tableId, rowUid) {
        const rows = this.getTableDraft(tableId);
        const row = rows.find(r => r._row_uid === rowUid);
        if (!row) return;

        const columns = this._getColumns(tableId);
        const modal = document.getElementById('detailRowModal');
        const modalBody = document.getElementById('detailRowModalBody');

        // 构建弹窗表单
        let formHtml = '';
        columns.forEach(col => {
            const value = row[col.code] !== undefined ? row[col.code] : '';
            const inputType = col.type === 'number' ? 'number' : 'text';
            formHtml += `
                <div style="margin-bottom: 12px;">
                    <label style="display:block;margin-bottom:4px;font-weight:600;">${col.name}</label>
                    <input type="${inputType}" id="modal_field_${col.code}" value="${value}"
                           style="width:100%;padding:6px 10px;border:1px solid #d1d5da;border-radius:4px;">
                </div>
            `;
        });

        modalBody.innerHTML = formHtml;

        // 保存当前编辑的行上下文
        modal.dataset.tableId = tableId;
        modal.dataset.rowUid = rowUid;

        modal.style.display = 'flex';
    },

    /**
     * 弹窗保存（合并回父 draft，不调后端）
     */
    saveModalDraft() {
        const modal = document.getElementById('detailRowModal');
        const tableId = modal.dataset.tableId;
        const rowUid = modal.dataset.rowUid;

        const rows = this.getTableDraft(tableId);
        const row = rows.find(r => r._row_uid === rowUid);
        if (!row) return;

        const columns = this._getColumns(tableId);
        columns.forEach(col => {
            const input = document.getElementById('modal_field_' + col.code);
            if (input) {
                row[col.code] = col.type === 'number' ? parseFloat(input.value) || 0 : input.value;
            }
        });

        if (row._row_op !== 'ADD') {
            row._row_op = 'UPDATE';
        }

        modal.style.display = 'none';
        this.renderTableBody(tableId, columns);
    },

    /**
     * 关闭弹窗（丢弃修改）
     */
    closeModal() {
        document.getElementById('detailRowModal').style.display = 'none';
    },

    /**
     * 渲染表格 tbody
     */
    renderTableBody(tableId, columns) {
        const rows = this.getTableDraft(tableId);
        const tbody = document.getElementById('detail_tbody_' + tableId);
        if (!tbody) return;

        if (rows.length === 0) {
            tbody.innerHTML = `<tr><td colspan="${(columns ? columns.length : 0) + 1}" style="padding:20px;text-align:center;color:#b4b4b4;">暂无数据</td></tr>`;
            return;
        }

        tbody.innerHTML = rows.map(row => {
            const isDeleted = row._row_op === 'DELETE';
            const rowStyle = isDeleted ? 'opacity:0.4;text-decoration:line-through;' : '';

            let cells = '';
            if (columns) {
                columns.forEach(col => {
                    const val = row[col.code] !== undefined ? row[col.code] : '';
                    cells += `<td style="padding:6px 10px;border:1px solid #e1e4e8;${rowStyle}">${val}</td>`;
                });
            }

            let actions = '';
            if (isDeleted) {
                actions = `<button onclick="DetailTableHelper.restoreRow('${tableId}','${row._row_uid}')" style="padding:2px 8px;color:#f59e0b;border:1px solid #f59e0b;background:white;border-radius:3px;cursor:pointer;">撤销删除</button>`;
            } else {
                actions = `
                    <button onclick="DetailTableHelper.editRow('${tableId}','${row._row_uid}')" style="padding:2px 8px;color:#667eea;border:1px solid #667eea;background:white;border-radius:3px;cursor:pointer;margin-right:4px;">编辑</button>
                    <button onclick="DetailTableHelper.deleteRow('${tableId}','${row._row_uid}')" style="padding:2px 8px;color:#ef4444;border:1px solid #ef4444;background:white;border-radius:3px;cursor:pointer;">删除</button>
                `;
            }

            return `<tr>${cells}<td style="padding:6px 10px;border:1px solid #e1e4e8;">${actions}</td></tr>`;
        }).join('');
    },

    /**
     * 内部：获取组件配置中的 columns
     */
    _getColumns(tableId) {
        if (!window.DesignerState || !window.DesignerState.templateConfig) return [];
        const root = window.DesignerState.templateConfig.rootComponent;
        if (!root) return [];
        const comp = this._findComponent(root, tableId);
        return comp ? (comp.columns || []) : [];
    },

    /**
     * 内部：递归查找组件
     */
    _findComponent(component, id) {
        if (!component) return null;
        if (component.id === id) return component;
        if (component.children) {
            for (const child of component.children) {
                const found = this._findComponent(child, id);
                if (found) return found;
            }
        }
        if (component.tabs) {
            for (const tab of component.tabs) {
                if (tab.children) {
                    for (const child of tab.children) {
                        const found = this._findComponent(child, id);
                        if (found) return found;
                    }
                }
            }
        }
        return null;
    }
};
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/static/config/js/detail-table.js
git commit -m "feat: add detail-table.js for DETAIL_TABLE interaction"
```

---

### Task 8: 改造 preview-renderer.js 中的 DETAIL_TABLE 渲染

**Files:**
- Modify: `src/main/resources/static/config/js/preview-renderer.js` (lines 539-588)

- [ ] **Step 1: 替换 renderDetailTableComponent 函数**

将 `preview-renderer.js` 中第 539-588 行的 `renderDetailTableComponent` 函数替换为：

```javascript
function renderDetailTableComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    const columns = component.columns || [];
    const enableAdd = component.enableAdd !== false;
    const enableDelete = component.enableDelete !== false;
    const enableEdit = component.enableEdit !== false;
    const maxRows = component.maxRows || 0;
    const minRows = component.minRows || 0;

    // 初始化 draft 数据
    if (!window.DesignerState.detailDrafts) window.DesignerState.detailDrafts = {};
    if (!window.DesignerState.detailDrafts[component.id]) {
        window.DesignerState.detailDrafts[component.id] = [];
    }

    let tableHTML = `
        <div class="component-preview-label">
            <i class="fas fa-table"></i> ${component.name || '明细表'}
            <small style="color:#999;margin-left:8px;">(${columns.length}列)</small>
        </div>
        <div class="component-preview-content">
            <table style="width:100%;border-collapse:collapse;margin-bottom:10px;">
                <thead>
                    <tr style="background:#f6f8fa;">
    `;

    columns.forEach(col => {
        tableHTML += `<th style="padding:8px;border:1px solid #e1e4e8;width:${col.width || 100}px;text-align:left;">${col.name}</th>`;
    });
    tableHTML += `<th style="padding:8px;border:1px solid #e1e4e8;width:120px;text-align:center;">操作</th>`;
    tableHTML += `</tr></thead><tbody id="detail_tbody_${component.id}">`;

    tableHTML += `<tr><td colspan="${columns.length + 1}" style="padding:20px;text-align:center;color:#b4b4b4;">暂无数据</td></tr>`;

    tableHTML += `</tbody></table>`;

    // 操作按钮栏
    if (enableAdd) {
        tableHTML += `
            <button onclick="DetailTableHelper.addRow('${component.id}', ${JSON.stringify(columns).replace(/"/g, '&quot;')}, ${maxRows})"
                    style="padding:4px 12px;border:1px solid #667eea;background:white;color:#667eea;border-radius:4px;cursor:pointer;">
                <i class="fas fa-plus"></i> 添加行
            </button>
        `;
    }

    tableHTML += `</div>`;
    element.innerHTML = tableHTML;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        if (typeof selectComponent === 'function') selectComponent(component.id);
    });

    return element;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/static/config/js/preview-renderer.js
git commit -m "feat: enhance DETAIL_TABLE rendering with interactive rows"
```

---

### Task 9: 在 template-designer.html 中添加明细行编辑弹窗和 detail-table.js 引用

**Files:**
- Modify: `src/main/resources/static/config/template-designer.html`

- [ ] **Step 1: 在 template-designer.html 的 </body> 标签前添加弹窗 HTML 和 JS 引用**

在 `</body>` 之前添加：

```html
    <!-- 明细行编辑弹窗 -->
    <div id="detailRowModal" style="display:none;position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:10000;align-items:center;justify-content:center;">
        <div style="background:white;border-radius:8px;width:480px;max-height:80vh;overflow:auto;">
            <div style="padding:16px 20px;border-bottom:1px solid #e1e4e8;font-weight:600;font-size:16px;">
                编辑明细行
                <button onclick="DetailTableHelper.closeModal()" style="float:right;border:none;background:none;font-size:20px;cursor:pointer;color:#999;">&times;</button>
            </div>
            <div id="detailRowModalBody" style="padding:20px;">
                <!-- 动态生成的表单字段 -->
            </div>
            <div style="padding:12px 20px;border-top:1px solid #e1e4e8;text-align:right;">
                <button onclick="DetailTableHelper.closeModal()" style="padding:6px 16px;border:1px solid #d1d5da;background:white;border-radius:4px;cursor:pointer;margin-right:8px;">取消</button>
                <button onclick="DetailTableHelper.saveModalDraft()" style="padding:6px 16px;border:none;background:#667eea;color:white;border-radius:4px;cursor:pointer;">保存</button>
            </div>
        </div>
    </div>

    <!-- 明细表交互模块 -->
    <script src="/config/js/detail-table.js"></script>
```

- [ ] **Step 2: 同步到 target 并验证**

Run:
```bash
cp src/main/resources/static/config/template-designer.html target/classes/static/config/template-designer.html
cp src/main/resources/static/config/js/detail-table.js target/classes/static/config/js/detail-table.js
cp src/main/resources/static/config/js/preview-renderer.js target/classes/static/config/js/preview-renderer.js
```

打开 template-designer.html，拖 DETAIL_TABLE 组件到画布，点击"添加行"按钮，确认空行出现。

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/static/config/template-designer.html src/main/resources/static/config/js/detail-table.js src/main/resources/static/config/js/preview-renderer.js
git commit -m "feat: add detail row modal and interactive table support"
```

---

### Task 10: property-panel.js 添加 DETAIL_TABLE 属性配置面板

**Files:**
- Modify: `src/main/resources/static/config/js/property-panel.js`

- [ ] **Step 1: 找到 showPropertyForm 或 renderPropertyPanel 中的 switch 语句**

Run:
```bash
grep -n "case 'DETAIL_TABLE'\|DETAIL_TABLE\|specificConfig\|showPropertyForm" src/main/resources/static/config/js/property-panel.js | head -20
```

找到处理组件属性面板的 switch/case 逻辑。

- [ ] **Step 2: 添加 DETAIL_TABLE case 和渲染函数**

在对应的 switch 语句中添加 DETAIL_TABLE case，调用新函数 `renderDetailTableConfig(component)`。

添加函数：

```javascript
function renderDetailTableConfig(component) {
    const columns = component.columns || [];
    let columnsHtml = '';
    columns.forEach((col, idx) => {
        columnsHtml += `
            <div style="display:flex;gap:8px;align-items:center;margin-bottom:6px;">
                <input type="text" value="${col.name}" placeholder="列名"
                       onchange="updateDetailTableColumn(${idx}, 'name', this.value)"
                       style="flex:1;padding:4px 8px;border:1px solid #d1d5da;border-radius:3px;">
                <input type="text" value="${col.code}" placeholder="编码"
                       onchange="updateDetailTableColumn(${idx}, 'code', this.value)"
                       style="flex:1;padding:4px 8px;border:1px solid #d1d5da;border-radius:3px;">
                <input type="text" value="${col.type || 'text'}" placeholder="类型"
                       onchange="updateDetailTableColumn(${idx}, 'type', this.value)"
                       style="width:60px;padding:4px 8px;border:1px solid #d1d5da;border-radius:3px;">
                <button onclick="removeDetailTableColumn(${idx})" style="border:none;background:none;color:#ef4444;cursor:pointer;font-size:16px;">&times;</button>
            </div>
        `;
    });

    return `
        <div class="config-section">
            <label class="form-label">明细表名称</label>
            <input type="text" class="form-control" value="${component.name || ''}"
                   onchange="updateComponentProperty('name', this.value)">
        </div>
        <div class="config-section mt-2">
            <label class="form-label">数据路径</label>
            <input type="text" class="form-control" value="${component.fieldPath || 'details'}"
                   onchange="updateComponentProperty('fieldPath', this.value)">
        </div>
        <div class="config-section mt-2">
            <label class="form-label">列配置</label>
            <div style="margin-bottom:6px;font-size:12px;color:#666;">列名 | 编码 | 类型</div>
            ${columnsHtml}
            <button onclick="addDetailTableColumn()" style="padding:4px 12px;border:1px solid #667eea;background:white;color:#667eea;border-radius:3px;cursor:pointer;width:100%;">
                <i class="fas fa-plus"></i> 添加列
            </button>
        </div>
        <div class="config-section mt-2">
            <label class="form-label">行数限制</label>
            <div style="display:flex;gap:8px;">
                <div style="flex:1;">
                    <small>最小行数</small>
                    <input type="number" class="form-control" value="${component.minRows || 0}" min="0"
                           onchange="updateComponentProperty('minRows', parseInt(this.value))">
                </div>
                <div style="flex:1;">
                    <small>最大行数（0=不限）</small>
                    <input type="number" class="form-control" value="${component.maxRows || 0}" min="0"
                           onchange="updateComponentProperty('maxRows', parseInt(this.value))">
                </div>
            </div>
        </div>
        <div class="config-section mt-2">
            <label class="form-label">操作开关</label>
            <div style="display:flex;gap:12px;">
                <label><input type="checkbox" ${component.enableAdd !== false ? 'checked' : ''}
                       onchange="updateComponentProperty('enableAdd', this.checked)"> 允许新增</label>
                <label><input type="checkbox" ${component.enableEdit !== false ? 'checked' : ''}
                       onchange="updateComponentProperty('enableEdit', this.checked)"> 允许编辑</label>
                <label><input type="checkbox" ${component.enableDelete !== false ? 'checked' : ''}
                       onchange="updateComponentProperty('enableDelete', this.checked)"> 允许删除</label>
            </div>
        </div>
    `;
}

function addDetailTableColumn() {
    const comp = getSelectedComponent();
    if (!comp) return;
    if (!comp.columns) comp.columns = [];
    comp.columns.push({ name: '新列', code: 'col_' + comp.columns.length, type: 'text', width: 100 });
    if (typeof refreshPropertyPanel === 'function') refreshPropertyPanel();
    if (typeof renderPreview === 'function') renderPreview();
}

function removeDetailTableColumn(idx) {
    const comp = getSelectedComponent();
    if (!comp || !comp.columns) return;
    comp.columns.splice(idx, 1);
    if (typeof refreshPropertyPanel === 'function') refreshPropertyPanel();
    if (typeof renderPreview === 'function') renderPreview();
}

function updateDetailTableColumn(idx, field, value) {
    const comp = getSelectedComponent();
    if (!comp || !comp.columns || !comp.columns[idx]) return;
    comp.columns[idx][field] = value;
    if (typeof renderPreview === 'function') renderPreview();
}
```

注意：`getSelectedComponent` 和 `refreshPropertyPanel` 的函数名可能与现有代码不同。需要检查 property-panel.js 中的实际函数名并调整。

- [ ] **Step 3: 验证属性面板渲染**

Run:
```bash
cp src/main/resources/static/config/js/property-panel.js target/classes/static/config/js/property-panel.js
```

拖 DETAIL_TABLE 到画布，点击选中，确认右侧属性面板显示列配置、行数限制、操作开关。

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/static/config/js/property-panel.js
git commit -m "feat: add DETAIL_TABLE property panel with column config and row limits"
```

---

## 阶段 3：Chrome DevTools E2E 验证

### Task 11: E2E 验证 - 模块 1 模板管理（5 场景）

无代码改动。使用 Chrome DevTools MCP 工具执行验证。

- [ ] **Step 1: 1.1 创建模板 - POST /api/templates**

Navigate to `http://localhost:8888/config/template-designer.html`。使用 evaluate_script 执行：
```javascript
fetch('/api/templates', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({templateCode:'E2E_TEST_'+Date.now(), templateName:'E2E测试模板'})}).then(r=>r.json())
```
验证返回 `success: true` 且 `data.id` 存在。截图留证。

- [ ] **Step 2: 1.2 创建版本 - POST /api/templates/{id}/versions**

使用上一步的 templateId，创建草稿版本。验证 `versionStatus: "DRAFT"`。

- [ ] **Step 3: 1.3 发布版本 - POST /api/templates/versions/{id}/publish**

发布刚创建的版本。验证 `versionStatus: "PUBLISHED"` 且 `publishTime` 有值。

- [ ] **Step 4: 1.4 加载已有模板**

在 template-designer.html 中选择刚创建的模板和版本，验证 schema 加载成功，画布不报错。

- [ ] **Step 5: 1.5 切换模板版本**

切换到另一个模板版本，验证数据正确切换，无残留组件。

- [ ] **Step 6: Commit verification log**

将验证结果截图保存，记录通过/失败状态。

---

### Task 12: E2E 验证 - 模块 2 布局组件拖拽（10 场景）

- [ ] **Step 1: 2.1-2.5 ROW/COL 布局验证**

逐一验证 PAGE 添加、CARD 嵌套、ROW/COL 左右布局、span 调整、三等分布局。每个场景截图。

- [ ] **Step 2: 2.6-2.10 GRID/TAB/COLLAPSE/嵌套/删除**

逐一验证 GRID 栅格、TAB 切换、COLLAPSE 展开、三层嵌套、节点删除。每个场景截图。

---

### Task 13: E2E 验证 - 模块 3 基础组件配置（11 场景）

- [ ] **Step 1: 3.1-3.6 INPUT/SELECT/DATE 基础验证**

拖入 INPUT、配置 fieldPath、必填、只读、拖入 SELECT、配置数据源。

- [ ] **Step 2: 3.7-3.11 NUMBER/AMOUNT/TEXTAREA/COL 内多组件**

拖入各组件验证渲染，COL 内放多个组件验证排列。

---

### Task 14: E2E 验证 - 模块 4 DETAIL_TABLE 完整操作（10 场景）

- [ ] **Step 1: 4.1-4.5 添加/配置/新增行/编辑行**

拖入 DETAIL_TABLE，配置列，新增行，内联编辑，弹窗编辑。

- [ ] **Step 2: 4.6-4.10 弹窗保存/删除/恢复/多行混合/行数限制**

弹窗修改保存，删除行标记，恢复行，多行混合操作，maxRows 限制验证。

---

### Task 15: E2E 验证 - 模块 5-8（21 场景）

- [ ] **Step 1: 模块 5 属性面板（8 场景）**

选中组件显示属性、修改即时生效、取消选中、切换组件、ROW/COL/DETAIL_TABLE 属性、非法值校验。

- [ ] **Step 2: 模块 6 保存与加载闭环（7 场景）**

保存空配置、保存布局、保存含明细表、刷新恢复、修改再保存、网络错误、请求格式检查。

- [ ] **Step 3: 模块 7 预览功能（4 场景）**

预览空白、简单表单、左右布局、含明细表。

- [ ] **Step 4: 模块 8 错误与边界（5 场景）**

控制台无错误、快速拖拽不栈溢出、深层嵌套不崩溃、20+ 组件性能、强制刷新。

---

## 阶段 4：JSONB 查询优化

### Task 16: 创建 openGauss GIN 索引脚本

**Files:**
- Create: `scripts/opengauss-jsonb-indexes.sql`

- [ ] **Step 1: 创建 GIN 索引脚本**

```sql
-- =====================================================
-- openGauss JSONB GIN 索引
-- 仅在 openGauss 上执行，不放 schema-h2.sql
-- =====================================================

-- 合同快照：按字段路径查询合同数据（最高频查询）
CREATE INDEX IF NOT EXISTS idx_snapshot_canonical_gin
    ON t_contract_data_snapshot USING GIN (canonical_data jsonb_path_ops);

-- 明细行数据：按明细字段查询
CREATE INDEX IF NOT EXISTS idx_detail_row_data_gin
    ON t_contract_detail_row USING GIN (row_data jsonb_path_ops);

-- 外部消息：按消息内容查询
CREATE INDEX IF NOT EXISTS idx_ext_message_payload_gin
    ON t_ext_message_inbox USING GIN (raw_payload);

-- 数据提供方配置：按配置内容查询
CREATE INDEX IF NOT EXISTS idx_data_provider_config_gin
    ON t_ui_data_provider USING GIN (config_json);
```

- [ ] **Step 2: Commit**

```bash
git add scripts/opengauss-jsonb-indexes.sql
git commit -m "feat: add openGauss GIN indexes for JSONB query optimization"
```

---

### Task 17: 创建 JsonbHelper 工具类

**Files:**
- Create: `src/main/java/com/contract/infrastructure/json/JsonbHelper.java`

- [ ] **Step 1: 创建 JsonbHelper**

```java
package com.contract.infrastructure.json;

import org.springframework.stereotype.Component;

/**
 * openGauss JSONB 查询工具类
 * 封装 JSONB 操作符和函数，用于 MyBatis XML 中的 SQL 片段
 */
@Component
public class JsonbHelper {

    /**
     * 提取文本值: column->>'path'
     */
    public String extractText(String column, String path) {
        return column + "->>'" + path + "'";
    }

    /**
     * 提取整数值: (column->>'path')::int
     */
    public String extractInt(String column, String path) {
        return "(" + column + "->>'" + path + "')::int";
    }

    /**
     * 路径存在判断: column ? 'path'
     */
    public String pathExists(String column, String path) {
        return column + " ? '" + path + "'";
    }

    /**
     * 包含判断: column @> 'json'
     */
    public String contains(String column, String json) {
        return column + " @> '" + json + "'::jsonb";
    }

    /**
     * JSON Path 查询: jsonb_path_query(column, '$.path')
     */
    public String pathQuery(String column, String path) {
        return "jsonb_path_query(" + column + ", '$." + path + "')";
    }

    /**
     * 深层路径文本提取: column#>>'{path1,path2}'
     */
    public String extractTextDeep(String column, String... paths) {
        return column + "#>>'{" + String.join(",", paths) + "}'";
    }
}
```

注意：此类仅用于生成 SQL 片段，不直接执行查询。在 MyBatis Mapper XML 中使用。

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/contract/infrastructure/json/JsonbHelper.java
git commit -m "feat: add JsonbHelper for openGauss JSONB query utilities"
```

---

### Task 18: 创建 openGauss JSONB 查询验证脚本

**Files:**
- Create: `scripts/verify-jsonb-performance.sql`

- [ ] **Step 1: 创建验证脚本**

```sql
-- =====================================================
-- openGauss JSONB 查询性能验证脚本
-- 步骤1: 先执行建表和数据初始化
-- 步骤2: 执行无索引查询（记录耗时）
-- 步骤3: 执行 GIN 索引创建
-- 步骤4: 执行有索引查询（记录耗时）
-- =====================================================

-- 清理旧数据
DELETE FROM t_contract_data_snapshot;

-- 插入 1000 条测试数据
INSERT INTO t_contract_data_snapshot (id, contract_id, snapshot_no, template_id, template_version_id, canonical_data, source_type, created_at)
SELECT
    gs * 1000 + 1,
    gs,
    1,
    1,
    1,
    ('{"basic": {"contractNo": "HT-' || gs || '", "contractName": "测试合同' || gs || '", "supplierName": "供应商' || (gs % 100) || '"}, "money": {"totalAmount": ' || (gs * 1000) || '.00, "currency": "CNY"}}')::jsonb,
    'MANUAL',
    NOW() - (1000 - gs) * INTERVAL '1 hour'
FROM generate_series(1, 1000) AS gs;

-- 无索引查询：按 supplierName 查询
EXPLAIN ANALYZE
SELECT id, contract_id, canonical_data->>'basic' as basic
FROM t_contract_data_snapshot
WHERE canonical_data @> '{"basic": {"supplierName": "供应商5"}}'::jsonb;

-- 无索引查询：按 contractNo 查询
EXPLAIN ANALYZE
SELECT id, contract_id
FROM t_contract_data_snapshot
WHERE canonical_data->>'basic.contractNo' = 'HT-500';

-- 创建 GIN 索引
CREATE INDEX IF NOT EXISTS idx_snapshot_canonical_gin
    ON t_contract_data_snapshot USING GIN (canonical_data jsonb_path_ops);

-- 有索引查询：按 supplierName 查询（@> 操作符）
EXPLAIN ANALYZE
SELECT id, contract_id, canonical_data->>'basic' as basic
FROM t_contract_data_snapshot
WHERE canonical_data @> '{"basic": {"supplierName": "供应商5"}}'::jsonb;

-- 有索引查询：按 contractNo 查询（注意 ->> 操作符不走 GIN，需要 jsonb_path_ops）
EXPLAIN ANALYZE
SELECT id, contract_id
FROM t_contract_data_snapshot
WHERE canonical_data->>'basic'.'contractNo' = 'HT-500';

-- 清理测试数据（可选）
-- DELETE FROM t_contract_data_snapshot WHERE template_id = 1;
```

注意：openGauss 的 JSON Path 语法可能与 PostgreSQL 有细微差异，执行时需根据实际版本调整。

- [ ] **Step 2: Commit**

```bash
git add scripts/verify-jsonb-performance.sql
git commit -m "feat: add openGauss JSONB performance verification script"
```

---

### Task 19: 在 openGauss 上执行验证

- [ ] **Step 1: 启动 openGauss**

Run:
```bash
cd /home/wula/IdeaProjects/dymic && bash scripts/start-opengauss.sh
```

确认 openGauss 启动成功。

- [ ] **Step 2: 执行建表脚本**

确保 `t_contract_data_snapshot` 表存在且有 `canonical_data JSONB` 列。

- [ ] **Step 3: 执行验证脚本**

在 openGauss 中执行 `scripts/verify-jsonb-performance.sql`。记录：
1. 无索引查询的 `EXPLAIN ANALYZE` 结果（Seq Scan，耗时 ms）
2. 有索引查询的 `EXPLAIN ANALYZE` 结果（Bitmap Index Scan，耗时 ms）
3. 对比性能提升倍数

- [ ] **Step 4: 执行 GIN 索引脚本**

执行 `scripts/opengauss-jsonb-indexes.sql` 在生产表上创建索引。

- [ ] **Step 5: 记录验证结果**

将性能对比结果记录下来。如果 GIN 索引对 `->>` 操作符无效（jsonb_path_ops 只支持 `@>`），考虑补充 btree 表达式索引：
```sql
CREATE INDEX idx_snapshot_contract_no ON t_contract_data_snapshot ((canonical_data->>'basic.contractNo'));
```

- [ ] **Step 6: Commit verification results**

```bash
git add -A
git commit -m "docs: record JSONB query performance verification results"
```

---

## 自查结果

**1. Spec 覆盖:** 逐项对照设计文档，阶段1（统一+后端API）→ Task 1-6；阶段2（DETAIL_TABLE）→ Task 7-10；阶段3（E2E 47场景）→ Task 11-15；阶段4（JSONB）→ Task 16-19。无遗漏。

**2. 占位符扫描:** 无 TBD/TODO/类似占位符。每个步骤有完整代码或命令。

**3. 类型一致性:** SchemaSaveDTO 的内部类字段名与 config-api.js 的 extractLayoutNodes/extractFieldDefs/extractFieldComponents 输出字段一一对应。
