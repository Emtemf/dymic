# 新会话启动提示

---

```
我需要继续实施 COLLAPSE和GRID组件修复。

设计文档：docs/superpowers/specs/2026-06-08-collapse-grid-fix-design.md
交接文档：docs/superpowers/handover/2026-06-08-collapse-grid-fix-handover.md

核心方案：方案B（纯树形结构）
- propsJson 只存 ID 引用：panels[i].children = ["comp_123"]
- 通过 parent_id 构建树形结构
- 后端零改动

改动文件：
1. config-api.js (extractLayoutNodes + buildComponentTree)
2. preview-renderer.js (renderCollapseComponent + renderGridComponent)
3. drag-drop.js (getDropTarget + addComponentToTarget + handleDragOver)

实施顺序：按 Batch 分批执行（见交接文档第3节）
E2E验证：Chrome DevTools + SQL + JSONB（见交接文档第4节）

请阅读交接文档后，按 Batch 顺序开始实施。
```

---

**直接复制上述提示，在新会话启动时发送即可开始实施。**