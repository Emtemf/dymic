# 合同模板动态渲染系统

## 项目定位

这是一个**配置驱动、动态渲染、数据比对**的一体化平台。

系统目标：
- 业务人员通过模板配置定义合同结构
- 系统根据配置动态渲染合同录入界面
- IT 配置数据源与查询回填能力
- 支持外部数据映射、对比与结果应用

明确边界：
- 不是低代码平台
- 不是给业务人员直接交付的拖拽设计器产品
- 可视化配置界面仅用于**开发验证 / E2E 验证**

当前前端入口与 `req` / `CLAUDE.md` 一致：
- 真实入口：`/index.html`
- 设计器页：`/config/template-designer.html`
- 数据源页：`/config/data-source.html`
- 动态展示页：`/display/dynamic-display.html`

---

## 当前默认运行方式

默认数据库已经切换为 **openGauss**。

- 默认运行配置：`src/main/resources/application.yml`
- 测试配置：`src/main/resources/application-test.yml`
- H2 现在只用于单元测试 / 测试 profile

默认应用启动端口：`8888`

默认 API 基础地址：

```text
http://localhost:8888/api
```

---

## 本地启动

### 1. 启动 openGauss

```bash
bash scripts/start-opengauss.sh
```

### 2. 初始化数据库

```bash
bash scripts/init-opengauss.sh
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

启动成功后访问：

- 首页：`http://localhost:8888/index.html`
- 设计器：`http://localhost:8888/config/template-designer.html`

---

## 数据库说明

### 默认运行库

默认运行使用：

```text
jdbc:postgresql://localhost:5432/contract_template?options=-c%20TimeZone=UTC
```

连接信息：
- Host: `localhost`
- Port: `5432`
- Database: `contract_template`
- Username: `gaussdb`
- Password: `OpenGauss@123`

### 测试库

`application-test.yml` 仍使用 H2：

```text
jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
```

---

## UTC 时间策略

为兼容 openGauss `TIMESTAMPTZ`，当前项目默认按 **UTC** 处理数据库审计时间：

- 数据库连接已显式指定 `TimeZone=UTC`
- openGauss 持久化实体使用 `OffsetDateTime`
- 业务写入时间统一按 UTC 生成
- 接口返回中当前已可看到 `...Z` 结尾的 UTC 时间字符串

---

## 与需求文档一致的验证路径

根据 `req/req.md` 与 `CLAUDE.md`，最小闭环应按下面顺序验证：

1. 创建模板
2. 创建版本
3. 发布版本
4. 进入设计器配置
5. 保存 schema
6. 新增 / 编辑合同
7. 回显合同数据

说明：
- `template-designer.html` 不是独立业务主入口
- 应优先从 `index.html` 开始
- 可视化设计器用于开发验证，不是业务交付界面

---

## 已完成的关键修复

### 设计器 schema 链路

已修复：
- schema 加载 / 归一化 / 保存链路不一致
- 新增组件时 `fieldPath` 为空
- URL 带 `templateId/versionId` 时初始化顺序错误
- 设计器恢复本地状态时 `rules/dataProviders` 缺省导致异常
- 首页 API 端口探测不跟随当前实例

### openGauss 默认运行

已修复：
- 默认配置切到 openGauss
- openGauss 启动 / 初始化脚本与默认配置对齐
- 默认模板列表 / 版本列表接口在高斯下恢复正常
- 版本表 mapper 与 openGauss schema 字段不一致问题
- UTC / `TIMESTAMPTZ` 基础兼容链路已打通

---

## 验证命令

### 配置 / 契约测试

```bash
mvn -q -Dtest=ApiAutoDetectContractTest,ComponentLibraryContractTest,ConfigApiContractTest,DefaultDatabaseProfileContractTest,OpenGaussTimeMappingContractTest test
```

### 编译

```bash
mvn -q -DskipTests compile
```

### 默认高斯接口烟雾验证

```bash
curl http://localhost:8888/api/templates
curl http://localhost:8888/api/templates/1001/versions
```

---

## 注意事项

- `.omc/`、`.claude/worktrees/`、`.idea/` 不属于交付物
- 若重启电脑后接口报数据库连接错误，优先确认 openGauss 容器是否启动
- 若再次执行初始化脚本，注意测试数据脚本中的脏数据或重复数据问题
- 设计器与首页静态资源已做缓存规避版本号，但浏览器强缓存时仍建议刷新页面

---

## 当前建议使用方式

开发 / 验证统一使用：

```bash
bash scripts/start-opengauss.sh
bash scripts/init-opengauss.sh
mvn spring-boot:run
```

测试仍然使用：

```bash
mvn test
```
