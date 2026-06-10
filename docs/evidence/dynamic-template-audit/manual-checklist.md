# Manual Double Check Checklist

## How to use this checklist
1. Start the app on the target port
2. Open the pages listed below in order
3. Compare actual pages against the saved screenshots in `docs/evidence/dynamic-template-audit/screenshots/`
4. Mark any mismatch immediately
5. If preview, save/reopen, nested display, or configured properties diverge, treat it as a defect and fix before accepting the build

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
