# Business Template E2E

Baseline: docs/superpowers/e2e/README.md

Covered scenarios:
- 05-data-provider-crud
- 06-contract-crud
- data-source query fill-back
- save and echo

Current verification on port 8890:
- `GET /api/v2/ui/data-sources/query` returned 3 data sources
- BUSINESS sources: `区域树`, `城市选择`
- IT source: `供应商接口`
- `区域树` returned `isTree=true`
- `GET /api/v2/ui/data-sources/7470263681562054656/execute` returned nested children payload

Evidence:
- screenshot: `screenshots/data-source-8890.png`
- browser eval capture: `screenshots/unified-query-8890.md`

Every scenario must include screenshot evidence.
