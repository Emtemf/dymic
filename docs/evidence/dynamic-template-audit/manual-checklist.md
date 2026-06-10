# Manual Double Check Checklist

## How to use this checklist
1. Start the app on the target port
2. Confirm there is at least one template and one version available before entering the designer
3. Open the pages listed below in order
4. Compare actual pages against the saved screenshots in `docs/evidence/dynamic-template-audit/screenshots/`
5. Mark any mismatch immediately
6. If preview, save/reopen, nested display, or configured properties diverge, treat it as a defect and fix before accepting the build

## Required preconditions
- The template list in `/config/template-designer.html` is not empty
- The selected template has at least one version
- The designer panel is reachable after choosing template and version

## Required scenarios
- Template CRUD
- Version CRUD and publish
- Layout node CRUD
- Field component CRUD
- Data provider CRUD
- Contract save/echo
- 21 component render scenarios
- Representative nested combinations
- Query fill-back
- Preview consistency

## Current baseline screenshots
- `screenshots/template-designer-8890.png`
- `screenshots/data-source-8890.png`
- `screenshots/unified-query-8890.md`

## Evidence rule
Every scenario above must have at least one screenshot. Complex scenarios must have multiple step screenshots. Screenshots are valid only after the scenario has been fixed to match the configured result; if the UI, save/reopen state, nested display, or preview result is wrong, fix it first, then recapture the screenshots.
