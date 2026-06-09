# Global Conformance Check

## Design and pattern checks
- Vertical call chain remains Controller -> Application -> Domain -> Infrastructure
- Adapter layer carries Req/Rsp only
- Application layer carries DTOs and orchestration only
- Domain layer keeps aggregates, value objects, repository interfaces
- Infrastructure layer keeps persistence entities, XML mappers, repository implementations

## MapStruct check
- Application converters use MapStruct for DTO <-> Domain transitions where applicable
- Infrastructure converters use MapStruct or explicit converter components for Entity <-> Domain transitions
- No controller performs ad-hoc deep object mapping beyond protocol-edge flattening
