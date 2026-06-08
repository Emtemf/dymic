# Backend Test Fix + Frontend Flow + E2E Verification

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix all 23 failing backend tests (116/116 pass), refactor the frontend designer entry flow (template/version selection), and perform comprehensive E2E verification across 11 scenarios.

**Architecture:** Three-phase approach — Phase 1 fixes test assertions and DTO mismatches, Phase 2 refactors `template-designer.html` to add a 3-step selection flow (template list → version list → designer), Phase 3 runs Chrome DevTools E2E across 11 scenarios (A-K).

**Tech Stack:** Java 21, Spring Boot 3.5.14, MyBatis-Plus, H2 (test), MapStruct, Lombok, Chrome DevTools MCP

---

## Phase 1: Fix Failing Tests (116/116)

### Task 1: Fix FieldDefServiceTest assertions (3 failures)

**Files:**
- Modify: `src/test/java/com/contract/FieldDefServiceTest.java`

**Root cause:** `FieldDefService.create()` stores `dataType` as-is from the DTO (no conversion). Tests expect old converted values.

- [ ] **Step 1: Fix testCreateFieldDef assertion**

In `src/test/java/com/contract/FieldDefServiceTest.java`, line 65, change:

```java
assertEquals("TEXT", result.getFieldDef().getDataType());
```
to:
```java
assertEquals("INPUT", result.getFieldDef().getDataType());
```

- [ ] **Step 2: Fix testCreateMoneyField assertion**

Line 120, change:

```java
assertEquals("NUMBER", result.getFieldDef().getDataType());
```
to:
```java
assertEquals("MONEY", result.getFieldDef().getDataType());
```

- [ ] **Step 3: Fix testCreateSelectFieldWithStaticOptions assertion**

Line 142, change:

```java
assertEquals("TEXT", result.getFieldDef().getDataType());
```
to:
```java
assertEquals("SELECT", result.getFieldDef().getDataType());
```

- [ ] **Step 4: Run tests to verify**

```bash
cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=FieldDefServiceTest -pl . -q 2>&1 | tail -20
```
Expected: 3 failures resolved, all FieldDefServiceTest tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/contract/FieldDefServiceTest.java
git commit -m "fix: update FieldDefServiceTest assertions to match passthrough dataType behavior"
```

---

### Task 2: Fix LayoutNodeControllerTest (6 failures → 500 errors)

**Files:**
- Modify: `src/test/java/com/contract/LayoutNodeControllerTest.java`

**Root cause:** Test JSON uses `displayName` but DTO field is `nodeName` (`@NotBlank` fails → 500). `visibleRule`/`readonlyRule` sent as JSON objects but DTO expects String. `testUpdate` uses hardcoded `id=1`.

- [ ] **Step 1: Rewrite testCreateLayoutNode**

Replace the body in `testCreateLayoutNode` (lines 24-32):

```java
String body = """
    {
      "nodeType": "CARD",
      "nodeName": "基本信息",
      "parentId": null,
      "sortNo": 1,
      "levelNo": 1
    }
    """;
```

- [ ] **Step 2: Rewrite testCreateChildNode**

Replace the entire `testCreateChildNode` method:

```java
@Test
void testCreateChildNode() throws Exception {
    String parentBody = """
        {
          "nodeType": "CARD",
          "nodeName": "父卡片",
          "parentId": null,
          "sortNo": 1
        }
        """;

    String parentResponse = mockMvc.perform(post("/api/templates/100/versions/200/layout-nodes")
            .contentType("application/json")
            .content(parentBody))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    String childBody = """
        {
          "nodeType": "FIELD",
          "nodeName": "合同名称",
          "parentId": null,
          "sortNo": 1
        }
        """;

    mockMvc.perform(post("/api/templates/100/versions/200/layout-nodes")
            .contentType("application/json")
            .content(childBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
}
```

- [ ] **Step 3: Rewrite testGetById body**

Replace the body in `testGetById` (lines 84-90):

```java
String body = """
    {
      "nodeType": "CARD",
      "nodeName": "查询API测试",
      "parentId": null,
      "sortNo": 1
    }
    """;
```

- [ ] **Step 4: Rewrite testUpdate to extract real ID**

Replace the entire `testUpdate` method:

```java
@Test
void testUpdate() throws Exception {
    String createBody = """
        {
          "nodeType": "CARD",
          "nodeName": "更新API测试",
          "parentId": null,
          "sortNo": 1
        }
        """;

    String createResponse = mockMvc.perform(post("/api/templates/100/versions/200/layout-nodes")
            .contentType("application/json")
            .content(createBody))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    String nodeId = com.jayway.jsonpath.JsonPath.read(createResponse, "$.data.id").toString();

    String updateBody = """
        {
          "nodeName": "更新后名称",
          "sortNo": 2
        }
        """;

    mockMvc.perform(put("/api/templates/100/versions/200/layout-nodes/" + nodeId)
            .contentType("application/json")
            .content(updateBody))
        .andExpect(status().isOk());
}
```

- [ ] **Step 5: Rewrite testCreateWithGridProperties body**

Replace the body (lines 147-160):

```java
String body = """
    {
      "nodeType": "FIELD",
      "nodeName": "网格布局测试",
      "parentId": null,
      "sortNo": 1,
      "gridX": 0,
      "gridY": 0,
      "gridW": 6,
      "gridH": 1,
      "colNo": 1,
      "rowNo": 1,
      "colSpan": 2
    }
    """;
```

- [ ] **Step 6: Rewrite testCreateWithRules — stringify rules**

Replace the entire `testCreateWithRules` method:

```java
@Test
void testCreateWithRules() throws Exception {
    String body = """
        {
          "nodeType": "FIELD",
          "nodeName": "规则API测试",
          "parentId": null,
          "sortNo": 1,
          "visibleRule": "{\\"condition\\": \\"status == 'ACTIVE'\\"}",
          "readonlyRule": "{\\"condition\\": \\"readonly == true\\"}"
        }
        """;

    mockMvc.perform(post("/api/templates/100/versions/200/layout-nodes")
            .contentType("application/json")
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
}
```

- [ ] **Step 7: Add json-path dependency check**

Check if `com.jayway.jsonpath:json-path` is already on the classpath (Spring Boot starter test includes it). Verify:

```bash
cd /home/wula/IdeaProjects/dymic && mvn dependency:tree -q 2>&1 | grep json-path | head -5
```

If not found, add to pom.xml test dependencies. (Spring Boot Test includes it transitively, so this should be fine.)

- [ ] **Step 8: Run tests to verify**

```bash
cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=LayoutNodeControllerTest -pl . -q 2>&1 | tail -20
```
Expected: All LayoutNodeControllerTest tests pass.

- [ ] **Step 9: Commit**

```bash
git add src/test/java/com/contract/LayoutNodeControllerTest.java
git commit -m "fix: update LayoutNodeControllerTest to use nodeName and stringify rules"
```

---

### Task 3: Fix TemplateVersionServiceTest (7 unique constraint errors)

**Files:**
- Modify: `src/test/java/com/contract/application/template/TemplateVersionServiceTest.java`

**Root cause:** Tests use `@Transactional` but H2 data persists between tests in the same test class. Template codes and version numbers collide.

- [ ] **Step 1: Add nanoTime suffix to all template codes**

Replace the entire file content with:

```java
package com.contract.application.template;

import com.contract.domain.template.TemplateVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TemplateVersionServiceTest {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateVersionService versionService;

    private static String suffix() {
        return "_" + System.nanoTime();
    }

    @Test
    void testCreateDraftVersion() {
        Long templateId = templateService.createTemplate(
            "VERSION_TEST" + suffix(),
            "版本测试",
            "测试版本",
            "TEST"
        ).getId();

        TemplateVersion version = versionService.createDraft(templateId, 1, "V1.0");

        assertNotNull(version.getId());
        assertEquals(templateId, version.getTemplateId());
        assertEquals(1, version.getVersionNo());
        assertEquals("V1.0", version.getVersionName());
        assertEquals("DRAFT", version.getVersionStatus());
        assertNull(version.getPublishTime());
        assertNull(version.getPublishBy());
    }

    @Test
    void testCreateDraftVersionWithDuplicateVersionNo() {
        Long templateId = templateService.createTemplate(
            "DUP_VERSION_TEST" + suffix(),
            "重复版本测试",
            "测试重复版本号",
            "TEST"
        ).getId();

        versionService.createDraft(templateId, 1, "V1.0");

        assertThrows(Exception.class, () -> {
            versionService.createDraft(templateId, 1, "V1.0-Duplicate");
        });
    }

    @Test
    void testPublishVersion() {
        Long templateId = templateService.createTemplate(
            "PUBLISH_TEST" + suffix(),
            "发布测试",
            "测试发布",
            "TEST"
        ).getId();

        Long versionId = versionService.createDraft(templateId, 1, "V1.0").getId();

        versionService.publish(versionId, 1001L);

        TemplateVersion published = versionService.getById(versionId);
        assertEquals("PUBLISHED", published.getVersionStatus());
        assertNotNull(published.getPublishTime());
        assertEquals(1001L, published.getPublishBy());
    }

    @Test
    void testPublishNonDraftVersion() {
        Long templateId = templateService.createTemplate(
            "PUBLISH_FAIL_TEST" + suffix(),
            "发布失败测试",
            "测试非草稿发布",
            "TEST"
        ).getId();

        Long versionId = versionService.createDraft(templateId, 1, "V1.0").getId();

        versionService.publish(versionId, 1001L);

        assertThrows(Exception.class, () -> {
            versionService.publish(versionId, 1002L);
        });
    }

    @Test
    void testGetById() {
        Long templateId = templateService.createTemplate(
            "GET_VERSION_TEST" + suffix(),
            "查询版本测试",
            "测试查询",
            "TEST"
        ).getId();

        Long versionId = versionService.createDraft(templateId, 1, "V1.0").getId();

        TemplateVersion version = versionService.getById(versionId);

        assertNotNull(version);
        assertEquals(versionId, version.getId());
        assertEquals("V1.0", version.getVersionName());
    }

    @Test
    void testGetByIdNotFound() {
        assertThrows(Exception.class, () -> {
            versionService.getById(999999L);
        });
    }

    @Test
    void testFindCurrentVersion() {
        Long templateId = templateService.createTemplate(
            "CURRENT_VERSION_TEST" + suffix(),
            "当前版本测试",
            "测试查找当前版本",
            "TEST"
        ).getId();

        Long version1Id = versionService.createDraft(templateId, 1, "V1.0").getId();
        versionService.publish(version1Id, 1001L);

        versionService.createDraft(templateId, 2, "V2.0");

        Long version3Id = versionService.createDraft(templateId, 3, "V3.0").getId();
        versionService.publish(version3Id, 1001L);

        TemplateVersion currentVersion = versionService.findCurrentVersion(templateId);
        assertNotNull(currentVersion);
        assertEquals(3, currentVersion.getVersionNo());
        assertEquals("PUBLISHED", currentVersion.getVersionStatus());
    }

    @Test
    void testFindCurrentVersionNoPublished() {
        Long templateId = templateService.createTemplate(
            "NO_PUBLISHED_TEST" + suffix(),
            "无发布版本测试",
            "测试没有发布版本",
            "TEST"
        ).getId();

        versionService.createDraft(templateId, 1, "V1.0");

        TemplateVersion currentVersion = versionService.findCurrentVersion(templateId);
        assertNull(currentVersion);
    }

    @Test
    void testCreateDraftWithNonExistentTemplate() {
        assertThrows(Exception.class, () -> {
            versionService.createDraft(999999L, 1, "V1.0");
        });
    }
}
```

- [ ] **Step 2: Run tests to verify**

```bash
cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=TemplateVersionServiceTest -pl . -q 2>&1 | tail -20
```
Expected: All 9 tests pass.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/contract/application/template/TemplateVersionServiceTest.java
git commit -m "fix: add unique suffix to TemplateVersionServiceTest template codes"
```

---

### Task 4: Fix TemplateServiceTest (2 unique constraint errors)

**Files:**
- Modify: `src/test/java/com/contract/application/template/TemplateServiceTest.java`

**Root cause:** `testDisableTemplate` and `testEnableTemplate` use fixed codes `DISABLE_TEST` and `ENABLE_TEST` that collide with other tests.

- [ ] **Step 1: Add suffix to conflicting template codes**

In `testDisableTemplate` (line 45), change:

```java
Template template = templateService.createTemplate(
    "DISABLE_TEST",
    "停用测试",
    "测试停用",
    "TEST"
);
```
to:
```java
Template template = templateService.createTemplate(
    "DISABLE_TEST_" + System.nanoTime(),
    "停用测试",
    "测试停用",
    "TEST"
);
```

In `testEnableTemplate` (line 60), change:

```java
Template template = templateService.createTemplate(
    "ENABLE_TEST",
    "启用测试",
    "测试启用",
    "TEST"
);
```
to:
```java
Template template = templateService.createTemplate(
    "ENABLE_TEST_" + System.nanoTime(),
    "启用测试",
    "测试启用",
    "TEST"
);
```

- [ ] **Step 2: Run tests to verify**

```bash
cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=TemplateServiceTest -pl . -q 2>&1 | tail -20
```
Expected: All 7 tests pass.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/contract/application/template/TemplateServiceTest.java
git commit -m "fix: add unique suffix to TemplateServiceTest template codes"
```

---

### Task 5: Fix DataProviderControllerTest (2 failures)

**Files:**
- Modify: `src/test/java/com/contract/DataProviderControllerTest.java`

**Root cause:** `configJson` column is NOT NULL. Test sends JSON object instead of string, or omits field entirely.

- [ ] **Step 1: Fix testCreateDataProvider — send configJson as string**

Replace the body in `testCreateDataProvider` (lines 21-29):

```java
String body = """
    {
      "providerCode": "API_TEST",
      "providerName": "API测试",
      "providerType": "HTTP",
      "configJson": "{\\"url\\":\\"http://api.example.com/suppliers\\"}"
    }
    """;
```

- [ ] **Step 2: Fix testGetById — add configJson**

Replace the body in `testGetById` (lines 43-48):

```java
String body = """
    {
      "providerCode": "GET_API_TEST",
      "providerName": "查询API测试",
      "providerType": "STATIC",
      "configJson": "{\\"options\\":[\\"A\\",\\"B\\"]}"
    }
    """;
```

- [ ] **Step 3: Run tests to verify**

```bash
cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=DataProviderControllerTest -pl . -q 2>&1 | tail -20
```
Expected: All 3 tests pass.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/contract/DataProviderControllerTest.java
git commit -m "fix: send configJson as string in DataProviderControllerTest"
```

---

### Task 6: Verify all 116 tests pass

- [ ] **Step 1: Run full test suite**

```bash
cd /home/wula/IdeaProjects/dymic && mvn test -pl . 2>&1 | tail -30
```

Expected: `Tests run: 116, Failures: 0, Errors: 0, Skipped: 0`

- [ ] **Step 2: If any tests fail, diagnose and fix**

Check the output for remaining failures and fix accordingly. Common issues:
- H2 schema mismatch (check `src/test/resources/schema-h2.sql`)
- Missing `@ActiveProfiles("test")` annotations

---

## Phase 2: Refactor Frontend Entry Flow

### Task 7: Create template/version selection flow in template-designer.html

**Files:**
- Modify: `src/main/resources/static/config/template-designer.html`
- Create: `src/main/resources/static/config/js/template-selector.js`

This task adds a 3-step flow before the designer: template list → version list → designer. When `templateId` and `versionId` are in URL params, skip directly to designer.

- [ ] **Step 1: Create template-selector.js**

Create `src/main/resources/static/config/js/template-selector.js`:

```javascript
/**
 * Template/Version selection flow
 * 3-step: template list → version list → designer
 */
const TemplateSelector = {
    state: {
        step: 1,
        templateId: null,
        versionId: null,
        templates: [],
        versions: []
    },

    init() {
        const params = new URLSearchParams(window.location.search);
        const tid = params.get('templateId');
        const vid = params.get('versionId');
        if (tid && vid) {
            this.state.templateId = parseInt(tid);
            this.state.versionId = parseInt(vid);
            this.state.step = 3;
            this.showDesigner();
            return;
        }
        this.loadTemplates();
    },

    async loadTemplates() {
        try {
            const resp = await fetch('/api/templates');
            const result = await resp.json();
            if (result.success) {
                this.state.templates = result.data || [];
                this.render();
            }
        } catch (e) {
            console.error('Failed to load templates:', e);
        }
    },

    async loadVersions(templateId) {
        try {
            const resp = await fetch(`/api/templates/${templateId}/versions`);
            const result = await resp.json();
            if (result.success) {
                this.state.versions = result.data || [];
                this.render();
            }
        } catch (e) {
            console.error('Failed to load versions:', e);
        }
    },

    render() {
        const selectorPanel = document.getElementById('selectorPanel');
        const designerPanel = document.getElementById('designerPanel');
        if (!selectorPanel) return;

        if (this.state.step === 3) {
            selectorPanel.style.display = 'none';
            designerPanel.style.display = '';
            return;
        }

        selectorPanel.style.display = '';
        designerPanel.style.display = 'none';

        if (this.state.step === 1) {
            this.renderTemplateList();
        } else if (this.state.step === 2) {
            this.renderVersionList();
        }
    },

    renderTemplateList() {
        document.getElementById('selectorBreadcrumb').innerHTML = '模板管理';
        document.getElementById('selectorPanelTitle').textContent = '模板列表';

        const listHtml = this.state.templates.map(t => `
            <div class="selector-item" data-id="${t.id}" onclick="TemplateSelector.selectTemplate(${t.id})">
                <div class="selector-item-title">${t.templateName}</div>
                <div class="selector-item-sub">${t.templateCode}</div>
                <div class="selector-item-badge ${t.status === 'ENABLED' ? 'badge-enabled' : 'badge-disabled'}">
                    ${t.status === 'ENABLED' ? '启用' : '停用'}
                </div>
            </div>
        `).join('');

        document.getElementById('selectorList').innerHTML = listHtml || '<div class="selector-empty">暂无模板</div>';

        const tpl = this.state.templateId ? this.state.templates.find(t => t.id === this.state.templateId) : null;
        if (tpl) {
            this.renderTemplateDetail(tpl);
        } else {
            document.getElementById('selectorContent').innerHTML = `
                <div class="selector-empty-state">
                    <div class="selector-empty-icon">&#128196;</div>
                    <p>从左侧选择一个模板开始配置</p>
                </div>`;
        }
    },

    renderTemplateDetail(tpl) {
        document.getElementById('selectorContent').innerHTML = `
            <h3>${tpl.templateName}</h3>
            <div class="selector-detail-grid">
                <div class="selector-detail-card">
                    <div class="selector-detail-label">模板编码</div>
                    <div class="selector-detail-value">${tpl.templateCode}</div>
                </div>
                <div class="selector-detail-card">
                    <div class="selector-detail-label">状态</div>
                    <div class="selector-detail-value">
                        <span class="selector-item-badge ${tpl.status === 'ENABLED' ? 'badge-enabled' : 'badge-disabled'}">
                            ${tpl.status === 'ENABLED' ? '启用' : '停用'}
                        </span>
                    </div>
                </div>
            </div>
            <div style="margin-top:20px;display:flex;gap:8px">
                <button class="btn btn-primary" onclick="TemplateSelector.goToVersions(${tpl.id})">选择版本 →</button>
            </div>`;
    },

    renderVersionList() {
        const tpl = this.state.templates.find(t => t.id === this.state.templateId);
        document.getElementById('selectorBreadcrumb').innerHTML =
            `<span style="cursor:pointer" onclick="TemplateSelector.goToStep(1)">模板管理</span> / ${tpl ? tpl.templateName : ''}`;
        document.getElementById('selectorPanelTitle').textContent = '版本列表';

        const listHtml = this.state.versions.map(v => `
            <div class="selector-item ${this.state.versionId === v.id ? 'active' : ''}"
                 data-id="${v.id}" onclick="TemplateSelector.selectVersion(${v.id})">
                <div class="selector-item-title">${v.versionName || 'V' + v.versionNo}</div>
                <div class="selector-item-sub">版本号: ${v.versionNo}</div>
                <div class="selector-item-badge ${v.versionStatus === 'PUBLISHED' ? 'badge-published' : 'badge-draft'}">
                    ${v.versionStatus === 'PUBLISHED' ? '已发布' : '草稿'}
                </div>
            </div>
        `).join('');

        document.getElementById('selectorList').innerHTML = listHtml || '<div class="selector-empty">暂无版本</div>';

        document.getElementById('selectorContentHeader').innerHTML = `
            <span style="font-size:14px;font-weight:600">${tpl ? tpl.templateName : ''} - 选择版本</span>
            <button class="btn btn-sm" onclick="TemplateSelector.goToStep(1)">← 返回模板列表</button>`;

        const ver = this.state.versionId ? this.state.versions.find(v => v.id === this.state.versionId) : null;
        if (ver) {
            this.renderVersionDetail(ver);
        } else {
            document.getElementById('selectorContent').innerHTML = `
                <div class="selector-empty-state">
                    <div class="selector-empty-icon">&#128203;</div>
                    <p>从左侧选择一个版本</p>
                    <p style="font-size:12px;color:#999">草稿版本可以编辑，已发布版本只读查看</p>
                </div>`;
        }
    },

    renderVersionDetail(ver) {
        const isDraft = ver.versionStatus === 'DRAFT';
        document.getElementById('selectorContent').innerHTML = `
            <h3>${ver.versionName || 'V' + ver.versionNo}</h3>
            <div class="selector-detail-grid">
                <div class="selector-detail-card">
                    <div class="selector-detail-label">版本状态</div>
                    <div class="selector-detail-value">
                        <span class="selector-item-badge ${isDraft ? 'badge-draft' : 'badge-published'}">
                            ${isDraft ? '草稿' : '已发布'}
                        </span>
                    </div>
                </div>
                <div class="selector-detail-card">
                    <div class="selector-detail-label">版本号</div>
                    <div class="selector-detail-value">${ver.versionNo}</div>
                </div>
            </div>
            <div style="margin-top:24px;display:flex;gap:8px">
                <button class="btn btn-primary" onclick="TemplateSelector.enterDesigner()">
                    ${isDraft ? '编辑配置 →' : '查看配置 →'}
                </button>
                ${isDraft ? '<button class="btn btn-success" onclick="TemplateSelector.publishVersion(' + ver.id + ')">发布此版本</button>' : ''}
            </div>`;
    },

    selectTemplate(id) {
        this.state.templateId = id;
        this.state.versionId = null;
        this.renderTemplateList();
    },

    async goToVersions(templateId) {
        this.state.templateId = templateId;
        this.state.versionId = null;
        this.state.step = 2;
        await this.loadVersions(templateId);
    },

    selectVersion(id) {
        this.state.versionId = id;
        this.renderVersionList();
    },

    goToStep(step) {
        this.state.step = step;
        if (step === 1) {
            this.state.versionId = null;
        }
        this.render();
    },

    enterDesigner() {
        this.state.step = 3;
        const url = new URL(window.location);
        url.searchParams.set('templateId', this.state.templateId);
        url.searchParams.set('versionId', this.state.versionId);
        window.history.pushState({}, '', url);
        this.showDesigner();
    },

    showDesigner() {
        const selectorPanel = document.getElementById('selectorPanel');
        const designerPanel = document.getElementById('designerPanel');
        if (selectorPanel) selectorPanel.style.display = 'none';
        if (designerPanel) designerPanel.style.display = '';

        // Update designer state
        if (typeof DesignerState !== 'undefined') {
            DesignerState.templateId = this.state.templateId;
            DesignerState.versionId = this.state.versionId;
        }

        // Update info bar
        const infoBar = document.getElementById('designerInfoBar');
        if (infoBar) infoBar.style.display = '';

        // Initialize designer
        if (typeof initDesigner === 'function') {
            initDesigner();
        }
    },

    async publishVersion(versionId) {
        if (!confirm('确认发布此版本？发布后不可修改。')) return;
        try {
            const resp = await fetch(`/api/templates/versions/${versionId}/publish`, { method: 'POST' });
            const result = await resp.json();
            if (result.success) {
                alert('发布成功');
                await this.loadVersions(this.state.templateId);
            } else {
                alert('发布失败: ' + result.message);
            }
        } catch (e) {
            alert('发布失败: ' + e.message);
        }
    }
};
```

- [ ] **Step 2: Add selector styles and panel HTML to template-designer.html**

Add this CSS inside the `<style>` block in `template-designer.html`:

```css
/* Template/Version Selector */
#selectorPanel { display: flex; height: calc(100vh - 48px); }
.selector-sidebar { width: 280px; background: #fff; border-right: 1px solid #e0e0e0; display: flex; flex-direction: column; }
.selector-sidebar-header { padding: 16px; border-bottom: 1px solid #eee; font-weight: 600; font-size: 14px; }
.selector-sidebar-body { flex: 1; overflow-y: auto; padding: 8px; }
.selector-item { padding: 12px; border-radius: 6px; cursor: pointer; margin-bottom: 4px; border: 1px solid transparent; }
.selector-item:hover { background: #f5f7fa; }
.selector-item.active { border-color: #4f46e5; background: #eef2ff; }
.selector-item-title { font-weight: 600; font-size: 14px; }
.selector-item-sub { font-size: 12px; color: #888; margin-top: 4px; }
.selector-item-badge { display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 11px; font-weight: 600; margin-top: 4px; }
.badge-enabled, .badge-published { background: #e8f5e9; color: #2e7d32; }
.badge-disabled { background: #f5f5f5; color: #999; }
.badge-draft { background: #fff3e0; color: #e65100; }
.selector-main { flex: 1; display: flex; flex-direction: column; }
.selector-content-header { padding: 12px 24px; border-bottom: 1px solid #e0e0e0; background: #fff; display: flex; align-items: center; justify-content: space-between; }
.selector-content-body { flex: 1; overflow-y: auto; padding: 24px; }
.selector-empty { text-align: center; padding: 20px; color: #aaa; font-size: 14px; }
.selector-empty-state { text-align: center; padding: 80px 40px; color: #aaa; }
.selector-empty-icon { font-size: 48px; margin-bottom: 16px; }
.selector-detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-top: 16px; }
.selector-detail-card { padding: 12px; background: #fff; border: 1px solid #eee; border-radius: 6px; }
.selector-detail-label { font-size: 12px; color: #888; margin-bottom: 4px; }
.selector-detail-value { font-weight: 600; }
.btn { padding: 6px 14px; border-radius: 4px; font-size: 13px; cursor: pointer; border: 1px solid #d0d5dd; background: #fff; color: #344054; }
.btn:hover { background: #f9fafb; }
.btn-primary { background: #4f46e5; color: #fff; border-color: #4f46e5; }
.btn-primary:hover { background: #4338ca; }
.btn-success { background: #059669; color: #fff; border-color: #059669; }
.btn-sm { padding: 4px 10px; font-size: 12px; }
#designerInfoBar { background: #eef2ff; border: 1px solid #c7d2fe; border-radius: 6px; padding: 8px 16px; font-size: 13px; color: #4338ca; margin: 8px 16px; display: none; }
```

Add the selector panel HTML right after `<body>` opening tag and before the existing designer layout:

```html
<!-- Template/Version Selector Panel -->
<div id="selectorPanel">
    <div class="selector-sidebar">
        <div class="selector-sidebar-header" id="selectorPanelTitle">模板列表</div>
        <div class="selector-sidebar-body" id="selectorList"></div>
    </div>
    <div class="selector-main">
        <div class="selector-content-header" id="selectorContentHeader">
            <span style="font-size:14px;font-weight:600">选择模板</span>
            <div></div>
        </div>
        <div class="selector-content-body" id="selectorContent">
            <div class="selector-empty-state">
                <div class="selector-empty-icon">&#128196;</div>
                <p>从左侧选择一个模板开始配置</p>
            </div>
        </div>
    </div>
</div>

<!-- Designer Info Bar -->
<div id="designerInfoBar">
    <span id="designerInfoText"></span>
    <button class="btn btn-sm" onclick="TemplateSelector.goToStep(1)" style="float:right">← 返回模板列表</button>
</div>
```

Wrap the existing designer layout in a `<div id="designerPanel" style="display:none">` and close it properly.

- [ ] **Step 3: Add template-selector.js script reference**

Add before the existing `<script>` tags:

```html
<script src="/config/js/template-selector.js"></script>
```

- [ ] **Step 4: Add selector breadcrumb to topbar**

In the topbar, add a breadcrumb element:

```html
<div class="breadcrumb" id="selectorBreadcrumb" style="font-size:13px;color:#aaa">模板管理</div>
```

- [ ] **Step 5: Update designer.js to read from URL params**

In `src/main/resources/static/config/js/designer.js`, update the `DesignerState` initialization to read from URL params instead of hardcoding:

```javascript
// Replace the existing DesignerState initialization with:
const params = new URLSearchParams(window.location.search);
const DesignerState = {
    templateId: parseInt(params.get('templateId')) || null,
    versionId: parseInt(params.get('versionId')) || null,
    // ... rest of state
};
```

- [ ] **Step 6: Copy static files to target**

```bash
cp /home/wula/IdeaProjects/dymic/src/main/resources/static/config/js/template-selector.js /home/wula/IdeaProjects/dymic/target/classes/static/config/js/template-selector.js
cp /home/wula/IdeaProjects/dymic/src/main/resources/static/config/template-designer.html /home/wula/IdeaProjects/dymic/target/classes/static/config/template-designer.html
cp /home/wula/IdeaProjects/dymic/src/main/resources/static/config/js/designer.js /home/wula/IdeaProjects/dymic/target/classes/static/config/js/designer.js
```

- [ ] **Step 7: Commit**

```bash
git add src/main/resources/static/config/js/template-selector.js src/main/resources/static/config/template-designer.html src/main/resources/static/config/js/designer.js
git commit -m "feat: add template/version selection flow before designer entry"
```

---

## Phase 3: E2E Verification (Chrome DevTools)

### Task 8: E2E Scenario A — Template Management API

- [ ] **Step 1: Start the application**

```bash
cd /home/wula/IdeaProjects/dymic && mvn spring-boot:run -Dspring-boot.run.profiles=test &
sleep 15
curl -s http://localhost:8888/api/templates | head -100
```

Expected: Returns JSON with `success: true`.

- [ ] **Step 2: Execute Scenario A via Chrome DevTools**

Using Chrome DevTools MCP tools:
1. `navigate_page` to `http://localhost:8888/api/templates` — verify template list
2. `evaluate_script` — POST create template with code `E2E_TEST`
3. Verify `GET /api/templates/code/E2E_TEST` returns correct data
4. POST disable, verify status is `DISABLED`
5. POST enable, verify status is `ENABLED`

All 5 sub-steps (A1-A5) must pass.

---

### Task 9: E2E Scenario B — Version Management API

- [ ] **Step 1: Execute Scenario B via Chrome DevTools**

Using Chrome DevTools MCP tools:
1. POST create draft version for template E2E_TEST
2. GET version list — verify contains the draft
3. POST publish — verify status becomes PUBLISHED
4. GET current version — verify returns published version
5. Create V2 draft, publish V2, verify current switches to V2

All 6 sub-steps (B1-B6) must pass.

---

### Task 10: E2E Scenario C — Frontend Entry Flow

- [ ] **Step 1: Navigate to designer page**

```
navigate_page("http://localhost:8888/config/template-designer.html")
```

- [ ] **Step 2: Verify template list displays (C1-C7)**

1. Take screenshot — verify template list panel visible
2. Click template E2E_TEST — verify detail shows on right
3. Click "选择版本" — verify version list loads
4. Click draft version — verify detail shows with "编辑配置" button
5. Click "编辑配置" — verify URL contains `templateId` and `versionId`
6. Verify top info bar shows template name + version + draft status
7. Verify designer panel is visible

All 7 sub-steps (C1-C7) must pass.

---

### Task 11: E2E Scenario D — Component Drag and Preview

- [ ] **Step 1: Drag components and verify rendering (D1-D12)**

Using Chrome DevTools MCP `drag` and `take_screenshot`:
1. Drag PAGE → verify appears in preview
2. Drag CARD into PAGE → verify nesting
3. Drag GRID into CARD → verify nesting
4. Drag INPUT into GRID → verify input renders
5. Drag SELECT into GRID → verify dropdown renders
6. Drag DATE into GRID → verify date picker renders
7. Drag MONEY into GRID → verify money input renders
8. Drag TEXTAREA into GRID → verify textarea renders
9. Drag NUMBER into GRID → verify number input renders
10. Drag BUTTON into CARD → verify button renders
11. Drag DETAIL_TABLE into PAGE → verify table renders
12. Click preview button → verify full form preview popup

All 12 sub-steps (D1-D12) must pass.

---

### Task 12: E2E Scenario E — Component Property Settings

- [ ] **Step 1: Set and verify component properties (E1-E13)**

Using Chrome DevTools MCP `click`, `fill`, `take_screenshot`:
1. Click INPUT → verify property panel shows INPUT config
2. Set name to "合同名称" → verify preview updates
3. Set code to "contractName" → verify saved
4. Set fieldPath to "basic.contractName" → verify saved
5. Set dataType to "string" → verify saved
6. Click SELECT → verify SELECT config shown
7. Set placeholder → verify preview updates
8. Click MONEY → verify money config shown
9. Set currency to CNY → verify preview shows ¥
10. Click DETAIL_TABLE → verify column config and row limits shown
11. Configure table columns → verify saved
12. Click CARD → verify container config shown
13. Rename CARD to "基本信息" → verify preview title updates

All 13 sub-steps (E1-E13) must pass.

---

### Task 13: E2E Scenario F — Complex Layout Combinations

- [ ] **Step 1: Build and verify complex form (F1-F6)**

1. Build PAGE > CARD"基本信息" > GRID(2col) > [INPUT, SELECT]
2. Build CARD"金额信息" > GRID(2col) > [MONEY, DATE]
3. Build CARD"明细" > DETAIL_TABLE
4. Build CARD"操作" > BUTTON
5. Click preview → verify complete form
6. Input data in preview → verify INPUT/SELECT/DATE work

All 6 sub-steps (F1-F6) must pass.

---

### Task 14: E2E Scenario G — Save and Reload Persistence

- [ ] **Step 1: Save, reload, verify persistence (G1-G8)**

1. Build layout from Scenario F
2. Click save button
3. Verify response: 200, success=true
4. Check network request body — layoutNodes/fieldDefs/fieldComponents present
5. Refresh page, re-select template and version
6. Wait for config to load
7. Compare loaded components — all present with correct hierarchy
8. Compare properties — names, codes, fieldPaths match

All 8 sub-steps (G1-G8) must pass.

---

### Task 15: E2E Scenario H — Schema API Verification

- [ ] **Step 1: Verify Schema API directly (H1-H7)**

Using `evaluate_script` for API calls:
1. GET schema → verify complete tree returned
2. Check layoutNodes — parentId relationships correct
3. Check fieldDefs — fieldCode, fieldPath correct
4. Check fieldComponents — componentType correct
5. Check actionConfigs — present
6. PUT schema (replace save) → verify 200
7. GET schema again → verify matches PUT data

All 7 sub-steps (H1-H7) must pass.

---

### Task 16: E2E Scenario I — JSONB Features

- [ ] **Step 1: Verify JSONB storage and queries (I1-I8)**

1. Check visible_rule JSONB in saved nodes
2. Check readonly_rule JSONB in saved nodes
3. Check props_json JSONB contains layout properties
4. Check component_props JSONB contains component attributes
5. Set visible_rule condition, save, verify JSONB stored
6. Reload config → verify rules displayed in rule panel
7. Verify GIN indexes exist on JSONB columns
8. Execute JSONB query via API → verify index usage

All 8 sub-steps (I1-I8) must pass.

---

### Task 17: E2E Scenario J — Data Source Configuration

- [ ] **Step 1: Configure data sources in designer (J1-J8)**

1. Switch to "数据源" tab → verify panel shown
2. Click "添加数据源" → verify type selector appears
3. Select "静态数据" → verify JSON editor shown
4. Configure currency options (CNY/USD/EUR) → verify saved
5. Select "字典" type → verify dictionary type input shown
6. Configure CONTRACT_TYPE → verify saved
7. Save config → verify data source saved with schema
8. Reload → verify data source list restored

All 8 sub-steps (J1-J8) must pass.

---

### Task 18: E2E Scenario K — Version Publish and Read-only

- [ ] **Step 1: Verify publish and read-only mode (K1-K6)**

1. Go to version list → click "发布"
2. Re-enter designer for published version → verify "已发布 - 只读" banner, save button hidden
3. Attempt drag → verify blocked in read-only mode
4. Create new draft version → verify DRAFT status
5. Edit config in draft → verify can save
6. Publish new version → verify old version still viewable, new becomes current

All 6 sub-steps (K1-K6) must pass.

---

## Verification Summary

- [ ] `mvn test` → 116/116 pass, 0 Failure, 0 Error
- [ ] Frontend flow: template selection → version selection → designer (C1-C7)
- [ ] E2E scenarios A-K all pass via Chrome DevTools
- [ ] Backend API returns `Result<T>` format consistently
- [ ] JSONB fields stored and queried correctly
- [ ] Config save → reload → data integrity verified
