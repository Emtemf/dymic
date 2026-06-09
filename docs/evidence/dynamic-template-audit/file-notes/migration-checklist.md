# Migration Checklist

- [ ] MyBatis-Plus removed
- [ ] Raw MyBatis XML present for all persistence modules
- [ ] XML files named `xxx.opengauss.xml`
- [ ] XML file names do not contain `Entity`
- [ ] Infrastructure entity classes retain `Entity` suffix
- [ ] Controller request/response models live in adapter layer
- [ ] Application layer contains DTOs only
- [ ] Domain layer contains aggregates/value objects/repository interfaces
- [ ] Browser E2E evidence attached
- [ ] JSONB and MVP-scale performance evidence attached
- [ ] Manual double-check checklist attached
