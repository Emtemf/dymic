# 合同模板动态渲染系统

## 当前状态

该项目当前已经完成一轮以“完整项目交付 + 公司内迁移约束”为目标的重构与验证，重点包括：

- Controller / Application / Domain / Infrastructure 垂直分层收口
- Controller Request 模型迁移到 adapter 层
- 持久化从 MyBatis-Plus 迁移到原生 MyBatis
- Mapper XML 统一为 `xxx.opengauss.xml`
- 基础设施数据实体保留 `Entity` 后缀，XML 文件名不带 `Entity`
- 统一数据源查询、树形数据源执行、Schema 聚合链路打通
- E2E 证据骨架、人工 double-check 清单、JSONB/性能说明已落地

## 启动方式

### 本地开发

推荐直接使用 Maven：

```bash
mvn spring-boot:run
```

默认端口见 `src/main/resources/application.yml`。

### 指定端口启动

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8890
```

## 关键入口

- 模板设计器入口页：`/config/template-designer.html`
- 数据源管理页：`/config/data-source.html`
- 统一数据源查询：`/api/v2/ui/data-sources/query`
- 统一数据源执行：`/api/v2/ui/data-sources/{id}/execute`

> 注意：`/config/template-designer.html` 不是直接可拖拽配置的页面。它会先展示模板/版本选择面板，只有在**已有模板且已有版本**的前提下，选中模板和版本后才会进入真正的设计器操作区。

## 验证方式

### 1. 自动化测试

全量测试：

```bash
mvn test -q
```

### 2. 人工复核

人工 double-check 清单在：

- `docs/evidence/dynamic-template-audit/manual-checklist.md`

### 3. E2E / 证据包

证据包目录：

- `docs/evidence/dynamic-template-audit/`

重点内容：

- 浏览器场景：`browser-flows/`
- 截图证据：`screenshots/`
- JSONB / SQL 说明：`sql-and-xml/`
- 性能说明：`performance/`
- 迁移检查表：`file-notes/migration-checklist.md`

### 4. E2E 基线

E2E 覆盖基线文档在：

- `docs/superpowers/e2e/README.md`

## 架构约束

当前实现遵循以下硬约束：

- 禁止使用 MyBatis-Plus
- 只能使用原生 MyBatis + XML
- XML 命名采用 `xxx.opengauss.xml`
- adapter 层仅持有 `Req/Rsp`
- application 层仅持有 DTO 和薄层编排
- domain 层承载聚合根、值对象、仓储接口和领域语义
- infrastructure 层承载数据实体、仓储实现、外部实现
- 转换统一优先使用 MapStruct

## 迁移说明

若需要迁移到公司内环境，请优先检查：

- `docs/evidence/dynamic-template-audit/file-notes/migration-checklist.md`
- `docs/evidence/dynamic-template-audit/file-notes/global-conformance.md`

## 注意事项

- 浏览器截图只能作为**修复后的通过证据**，不能替代修复
- 如果配置、保存回显、嵌套展示、预览结果不一致，必须先修复再补截图
- 如果模板列表为空或没有版本，设计器主链路 E2E 不能算完成；必须先补齐“模板 → 版本 → 设计器”这段前置链路
- 工作区中的 `.omc/`、`.claude/worktrees/` 等内容不属于交付物
