# Domain Object Flow

## Vertical Call Chain
Controller Req/Rsp -> Application DTO -> Domain Aggregate/Value Object -> Infrastructure Entity/XML

## Aggregate Roots
- Template
- TemplateVersion
- DataProvider

## Value Objects
- ConfigJson
- AuditInfo

## Rule
Infrastructure entities never cross into Domain callers.
Application DTOs never enter Mapper/XML directly.
