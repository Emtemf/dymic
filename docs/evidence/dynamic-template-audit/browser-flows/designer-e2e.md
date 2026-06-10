# Designer E2E

Baseline: docs/superpowers/e2e/README.md

Covered scenarios:
- 01-template-crud
- 02-version-crud
- 03-layout-node-crud
- 04-field-component-crud

Actual entry conditions:
- open `/config/template-designer.html`
- confirm the left template list is not empty
- select a template
- confirm the version list is not empty
- select a version
- only after that does the designer panel become a valid configuration entry

Current verification on port 8888:
- page shell opened successfully: `/config/template-designer.html`
- selector panel is present
- current state showed `暂无模板`, so dynamic component configuration could not be considered fully verified from this page alone
- this means the missing precondition chain (template -> version -> designer) must be treated as part of the E2E path

Evidence:
- screenshot: `screenshots/template-designer-8890.png`

For every scenario record:
- page entry
- exact browser actions
- expected result
- actual result
- screenshot file names
