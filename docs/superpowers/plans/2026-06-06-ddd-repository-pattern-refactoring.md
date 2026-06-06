# DDD Repository Pattern Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor from direct Mapper usage to DDD Repository pattern, moving Entity-Domain conversion from Service to Repository layer and establishing proper dependency inversion.

**Architecture:** Create Repository interfaces in the domain layer that define operations on domain models. Implement repositories in the infrastructure layer using existing MyBatis Mappers, encapsulating all Entity-Domain conversion. Service layer depends only on Repository interfaces.

**Tech Stack:** Spring Boot, MyBatis Plus, Lombok, JUnit 5

---

## File Structure

**New Files (4):**
- `src/main/java/com/contract/domain/template/repository/TemplateRepository.java` - Domain repository interface
- `src/main/java/com/contract/domain/template/repository/TemplateVersionRepository.java` - Domain repository interface
- `src/main/java/com/contract/infrastructure/persistence/repository/TemplateRepositoryImpl.java` - Repository implementation
- `src/main/java/com/contract/infrastructure/persistence/repository/TemplateVersionRepositoryImpl.java` - Repository implementation

**Modified Files (2):**
- `src/main/java/com/contract/application/template/TemplateService.java` - Inject TemplateRepository instead of TemplateMapper
- `src/main/java/com/contract/application/template/TemplateVersionService.java` - Inject TemplateVersionRepository instead of TemplateVersionMapper

**Test Files (No Changes):**
- Existing tests remain unchanged - they test Service layer behavior which stays the same

---

### Task 1: Create TemplateRepository Interface

**Files:**
- Create: `src/main/java/com/contract/domain/template/repository/TemplateRepository.java`

- [ ] **Step 1: Create TemplateRepository interface in domain layer**

```java
package com.contract.domain.template.repository;

import com.contract.domain.template.Template;

/**
 * 模板仓储接口
 * 定义模板的持久化操作，基于领域模型
 */
public interface TemplateRepository {

    /**
     * 保存模板
     *
     * @param template 模板领域对象
     * @return 保存后的模板（包含ID）
     */
    Template save(Template template);

    /**
     * 根据ID查询模板
     *
     * @param id 模板ID
     * @return 模板领域对象，不存在则返回null
     */
    Template findById(Long id);

    /**
     * 根据模板编码查询模板
     *
     * @param templateCode 模板编码
     * @return 模板领域对象，不存在则返回null
     */
    Template findByTemplateCode(String templateCode);

    /**
     * 检查模板编码是否存在
     *
     * @param templateCode 模板编码
     * @return true-存在，false-不存在
     */
    boolean existsByTemplateCode(String templateCode);

    /**
     * 更新模板
     *
     * @param template 模板领域对象
     */
    void update(Template template);
}
```

Run: No execution needed (interface only)

---

### Task 2: Create TemplateVersionRepository Interface

**Files:**
- Create: `src/main/java/com/contract/domain/template/repository/TemplateVersionRepository.java`

- [ ] **Step 1: Create TemplateVersionRepository interface in domain layer**

```java
package com.contract.domain.template.repository;

import com.contract.domain.template.TemplateVersion;

/**
 * 模板版本仓储接口
 * 定义模板版本的持久化操作，基于领域模型
 */
public interface TemplateVersionRepository {

    /**
     * 保存版本
     *
     * @param version 版本领域对象
     * @return 保存后的版本（包含ID）
     */
    TemplateVersion save(TemplateVersion version);

    /**
     * 根据ID查询版本
     *
     * @param id 版本ID
     * @return 版本领域对象，不存在则返回null
     */
    TemplateVersion findById(Long id);

    /**
     * 更新版本
     *
     * @param version 版本领域对象
     */
    void update(TemplateVersion version);

    /**
     * 检查版本号是否已存在
     *
     * @param templateId 模板ID
     * @param versionNo 版本号
     * @return true-存在，false-不存在
     */
    boolean existsByTemplateIdAndVersionNo(Long templateId, Integer versionNo);

    /**
     * 查找模板的当前发布版本
     * 查询条件：template_id = ? AND version_status = 'PUBLISHED' ORDER BY version_no DESC LIMIT 1
     *
     * @param templateId 模板ID
     * @return 当前发布版本，不存在则返回null
     */
    TemplateVersion findCurrentVersion(Long templateId);
}
```

Run: No execution needed (interface only)

---

### Task 3: Create TemplateRepositoryImpl

**Files:**
- Create: `src/main/java/com/contract/infrastructure/persistence/repository/TemplateRepositoryImpl.java`

- [ ] **Step 1: Create TemplateRepositoryImpl with Entity-Domain conversion**

```java
package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.adapter.persistence.entity.TemplateEntity;
import com.contract.adapter.persistence.mapper.TemplateMapper;
import com.contract.domain.template.Template;
import com.contract.domain.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 模板仓储实现
 * 负责领域模型与持久化实体之间的转换
 */
@Repository
@RequiredArgsConstructor
public class TemplateRepositoryImpl implements TemplateRepository {

    private final TemplateMapper templateMapper;

    @Override
    public Template save(Template template) {
        TemplateEntity entity = toEntity(template);
        templateMapper.insert(entity);
        template.setId(entity.getId());
        return template;
    }

    @Override
    public Template findById(Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public Template findByTemplateCode(String templateCode) {
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getTemplateCode, templateCode);
        TemplateEntity entity = templateMapper.selectOne(wrapper);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public boolean existsByTemplateCode(String templateCode) {
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getTemplateCode, templateCode);
        return templateMapper.selectCount(wrapper) > 0;
    }

    @Override
    public void update(Template template) {
        TemplateEntity entity = toEntity(template);
        templateMapper.updateById(entity);
    }

    /**
     * 领域模型转实体
     */
    private TemplateEntity toEntity(Template template) {
        TemplateEntity entity = new TemplateEntity();
        entity.setId(template.getId());
        entity.setTemplateCode(template.getTemplateCode());
        entity.setTemplateName(template.getTemplateName());
        entity.setTemplateDesc(template.getTemplateDesc());
        entity.setBizType(template.getBizType());
        entity.setStatus(template.getStatus());
        entity.setCurrentVersionId(template.getCurrentVersionId());
        entity.setCreatedBy(template.getCreatedBy());
        entity.setCreatedName(template.getCreatedName());
        entity.setCreatedAt(template.getCreatedAt());
        entity.setUpdatedBy(template.getUpdatedBy());
        entity.setUpdatedName(template.getUpdatedName());
        entity.setUpdatedAt(template.getUpdatedAt());
        return entity;
    }

    /**
     * 实体转领域模型
     */
    private Template toDomain(TemplateEntity entity) {
        Template template = new Template();
        template.setId(entity.getId());
        template.setTemplateCode(entity.getTemplateCode());
        template.setTemplateName(entity.getTemplateName());
        template.setTemplateDesc(entity.getTemplateDesc());
        template.setBizType(entity.getBizType());
        template.setStatus(entity.getStatus());
        template.setCurrentVersionId(entity.getCurrentVersionId());
        template.setCreatedBy(entity.getCreatedBy());
        template.setCreatedName(entity.getCreatedName());
        template.setCreatedAt(entity.getCreatedAt());
        template.setUpdatedBy(entity.getUpdatedBy());
        template.setUpdatedName(entity.getUpdatedName());
        template.setUpdatedAt(entity.getUpdatedAt());
        return template;
    }
}
```

Run: No execution needed (implementation class)

---

### Task 4: Create TemplateVersionRepositoryImpl

**Files:**
- Create: `src/main/java/com/contract/infrastructure/persistence/repository/TemplateVersionRepositoryImpl.java`

- [ ] **Step 1: Create TemplateVersionRepositoryImpl with Entity-Domain conversion**

```java
package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.adapter.persistence.entity.TemplateVersionEntity;
import com.contract.adapter.persistence.mapper.TemplateVersionMapper;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 模板版本仓储实现
 * 负责领域模型与持久化实体之间的转换
 */
@Repository
@RequiredArgsConstructor
public class TemplateVersionRepositoryImpl implements TemplateVersionRepository {

    private final TemplateVersionMapper versionMapper;

    @Override
    public TemplateVersion save(TemplateVersion version) {
        TemplateVersionEntity entity = toEntity(version);
        versionMapper.insert(entity);
        version.setId(entity.getId());
        return version;
    }

    @Override
    public TemplateVersion findById(Long id) {
        TemplateVersionEntity entity = versionMapper.selectById(id);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public void update(TemplateVersion version) {
        TemplateVersionEntity entity = toEntity(version);
        versionMapper.updateById(entity);
    }

    @Override
    public boolean existsByTemplateIdAndVersionNo(Long templateId, Integer versionNo) {
        LambdaQueryWrapper<TemplateVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateVersionEntity::getTemplateId, templateId)
               .eq(TemplateVersionEntity::getVersionNo, versionNo);
        return versionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public TemplateVersion findCurrentVersion(Long templateId) {
        TemplateVersionEntity entity = versionMapper.findCurrentVersion(templateId);
        return entity != null ? toDomain(entity) : null;
    }

    /**
     * 领域模型转实体
     */
    private TemplateVersionEntity toEntity(TemplateVersion version) {
        TemplateVersionEntity entity = new TemplateVersionEntity();
        entity.setId(version.getId());
        entity.setTemplateId(version.getTemplateId());
        entity.setVersionNo(version.getVersionNo());
        entity.setVersionName(version.getVersionName());
        entity.setVersionStatus(version.getVersionStatus());
        entity.setPublishTime(version.getPublishTime());
        entity.setPublishBy(version.getPublishBy());
        entity.setSchemaHash(version.getSchemaHash());
        entity.setRemark(version.getRemark());
        entity.setCreatedBy(version.getCreatedBy());
        entity.setCreatedName(version.getCreatedName());
        entity.setCreatedAt(version.getCreatedAt());
        entity.setUpdatedBy(version.getUpdatedBy());
        entity.setUpdatedName(version.getUpdatedName());
        entity.setUpdatedAt(version.getUpdatedAt());
        return entity;
    }

    /**
     * 实体转领域模型
     */
    private TemplateVersion toDomain(TemplateVersionEntity entity) {
        TemplateVersion version = new TemplateVersion();
        version.setId(entity.getId());
        version.setTemplateId(entity.getTemplateId());
        version.setVersionNo(entity.getVersionNo());
        version.setVersionName(entity.getVersionName());
        version.setVersionStatus(entity.getVersionStatus());
        version.setPublishTime(entity.getPublishTime());
        version.setPublishBy(entity.getPublishBy());
        version.setSchemaHash(entity.getSchemaHash());
        version.setRemark(entity.getRemark());
        version.setCreatedBy(entity.getCreatedBy());
        version.setCreatedName(entity.getCreatedName());
        version.setCreatedAt(entity.getCreatedAt());
        version.setUpdatedBy(entity.getUpdatedBy());
        version.setUpdatedName(entity.getUpdatedName());
        version.setUpdatedAt(entity.getUpdatedAt());
        return version;
    }
}
```

Run: No execution needed (implementation class)

---

### Task 5: Refactor TemplateService to use TemplateRepository

**Files:**
- Modify: `src/main/java/com/contract/application/template/TemplateService.java`

- [ ] **Step 1: Update TemplateService imports and dependency**

Replace the import and field:
```java
// Remove this import
import com.contract.adapter.persistence.mapper.TemplateMapper;

// Add this import
import com.contract.domain.template.repository.TemplateRepository;

// Change the field from
private final TemplateMapper templateMapper;

// To
private final TemplateRepository templateRepository;
```

- [ ] **Step 2: Refactor createTemplate method**

```java
@Transactional
public Template createTemplate(String templateCode, String templateName, String templateDesc, String bizType) {
    // 检查编码是否已存在
    if (templateRepository.existsByTemplateCode(templateCode)) {
        throw new BizException("模板编码已存在：" + templateCode);
    }

    Template template = Template.create(templateCode, templateName, templateDesc, bizType);
    template.setCreatedAt(LocalDateTime.now());
    template.setUpdatedAt(LocalDateTime.now());

    return templateRepository.save(template);
}
```

- [ ] **Step 3: Refactor getById method**

```java
public Template getById(Long id) {
    Template template = templateRepository.findById(id);
    if (template == null) {
        throw new BizException("模板不存在：" + id);
    }
    return template;
}
```

- [ ] **Step 4: Refactor getByCode method**

```java
public Template getByCode(String templateCode) {
    Template template = templateRepository.findByTemplateCode(templateCode);
    if (template == null) {
        throw new BizException("模板不存在：" + templateCode);
    }
    return template;
}
```

- [ ] **Step 5: Refactor disable method**

```java
@Transactional
public void disable(Long id) {
    Template template = templateRepository.findById(id);
    if (template == null) {
        throw new BizException("模板不存在：" + id);
    }
    template.disable();
    template.setUpdatedAt(LocalDateTime.now());
    templateRepository.update(template);
}
```

- [ ] **Step 6: Refactor enable method**

```java
@Transactional
public void enable(Long id) {
    Template template = templateRepository.findById(id);
    if (template == null) {
        throw new BizException("模板不存在：" + id);
    }
    template.enable();
    template.setUpdatedAt(LocalDateTime.now());
    templateRepository.update(template);
}
```

- [ ] **Step 7: Refactor updateCurrentVersion method**

```java
@Transactional
public void updateCurrentVersion(Long templateId, Long versionId) {
    Template template = templateRepository.findById(templateId);
    if (template == null) {
        throw new BizException("模板不存在：" + templateId);
    }
    template.setCurrentVersion(versionId);
    template.setUpdatedAt(LocalDateTime.now());
    templateRepository.update(template);
}
```

- [ ] **Step 8: Remove toDomain method**

Delete the entire `toDomain` method (lines 103-119) - conversion logic now in repository

- [ ] **Step 9: Verify final TemplateService**

The complete refactored TemplateService should look like:

```java
package com.contract.application.template;

import com.contract.common.exception.BizException;
import com.contract.domain.template.Template;
import com.contract.domain.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;

    @Transactional
    public Template createTemplate(String templateCode, String templateName, String templateDesc, String bizType) {
        // 检查编码是否已存在
        if (templateRepository.existsByTemplateCode(templateCode)) {
            throw new BizException("模板编码已存在：" + templateCode);
        }

        Template template = Template.create(templateCode, templateName, templateDesc, bizType);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());

        return templateRepository.save(template);
    }

    public Template getById(Long id) {
        Template template = templateRepository.findById(id);
        if (template == null) {
            throw new BizException("模板不存在：" + id);
        }
        return template;
    }

    public Template getByCode(String templateCode) {
        Template template = templateRepository.findByTemplateCode(templateCode);
        if (template == null) {
            throw new BizException("模板不存在：" + templateCode);
        }
        return template;
    }

    @Transactional
    public void disable(Long id) {
        Template template = templateRepository.findById(id);
        if (template == null) {
            throw new BizException("模板不存在：" + id);
        }
        template.disable();
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.update(template);
    }

    @Transactional
    public void enable(Long id) {
        Template template = templateRepository.findById(id);
        if (template == null) {
            throw new BizException("模板不存在：" + id);
        }
        template.enable();
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.update(template);
    }

    /**
     * 更新模板的当前版本ID
     *
     * @param templateId 模板ID
     * @param versionId 版本ID
     */
    @Transactional
    public void updateCurrentVersion(Long templateId, Long versionId) {
        Template template = templateRepository.findById(templateId);
        if (template == null) {
            throw new BizException("模板不存在：" + templateId);
        }
        template.setCurrentVersion(versionId);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.update(template);
    }
}
```

---

### Task 6: Refactor TemplateVersionService to use TemplateVersionRepository

**Files:**
- Modify: `src/main/java/com/contract/application/template/TemplateVersionService.java`

- [ ] **Step 1: Update TemplateVersionService imports and dependency**

Replace the import and field:
```java
// Remove this import
import com.contract.adapter.persistence.mapper.TemplateVersionMapper;

// Add this import
import com.contract.domain.template.repository.TemplateVersionRepository;

// Change the field from
private final TemplateVersionMapper versionMapper;

// To
private final TemplateVersionRepository versionRepository;
```

- [ ] **Step 2: Refactor createDraft method**

```java
@Transactional
public TemplateVersion createDraft(Long templateId, Integer versionNo, String versionName) {
    log.info("Creating draft version for template: {}, versionNo: {}", templateId, versionNo);

    // 检查模板是否存在
    templateService.getById(templateId);

    // 检查版本号是否已存在
    if (versionRepository.existsByTemplateIdAndVersionNo(templateId, versionNo)) {
        throw new BizException("版本号已存在：" + versionNo);
    }

    // 使用领域模型创建草稿版本
    TemplateVersion version = TemplateVersion.createDraft(templateId, versionNo, versionName);
    version.setCreatedAt(LocalDateTime.now());
    version.setUpdatedAt(LocalDateTime.now());

    return versionRepository.save(version);
}
```

- [ ] **Step 3: Refactor getById method**

```java
public TemplateVersion getById(Long id) {
    TemplateVersion version = versionRepository.findById(id);
    if (version == null) {
        throw new BizException("版本不存在：" + id);
    }
    return version;
}
```

- [ ] **Step 4: Refactor publish method**

```java
@Transactional
public void publish(Long versionId, Long publishBy) {
    log.info("Publishing version: {}, by user: {}", versionId, publishBy);

    TemplateVersion version = versionRepository.findById(versionId);
    if (version == null) {
        throw new BizException("版本不存在：" + versionId);
    }

    // 检查是否为草稿状态
    if (!version.isDraft()) {
        throw new BizException("只有草稿状态才能发布");
    }

    // 使用领域模型发布版本
    version.publish(publishBy);
    version.setUpdatedAt(LocalDateTime.now());
    versionRepository.update(version);

    // 更新模板的当前版本ID
    templateService.updateCurrentVersion(version.getTemplateId(), versionId);

    log.info("Version published successfully: {}", versionId);
}
```

- [ ] **Step 5: Refactor findCurrentVersion method**

```java
public TemplateVersion findCurrentVersion(Long templateId) {
    return versionRepository.findCurrentVersion(templateId);
}
```

- [ ] **Step 6: Remove toDomain method**

Delete the entire `toDomain` method (lines 137-155) - conversion logic now in repository

- [ ] **Step 7: Verify final TemplateVersionService**

The complete refactored TemplateVersionService should look like:

```java
package com.contract.application.template;

import com.contract.common.exception.BizException;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 模板版本服务层
 * 负责模板版本的创建、发布、查询等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateVersionService {

    private final TemplateVersionRepository versionRepository;
    private final TemplateService templateService;

    /**
     * 创建草稿版本
     *
     * @param templateId 模板ID
     * @param versionNo 版本号
     * @param versionName 版本名称
     * @return 创建的版本对象
     */
    @Transactional
    public TemplateVersion createDraft(Long templateId, Integer versionNo, String versionName) {
        log.info("Creating draft version for template: {}, versionNo: {}", templateId, versionNo);

        // 检查模板是否存在
        templateService.getById(templateId);

        // 检查版本号是否已存在
        if (versionRepository.existsByTemplateIdAndVersionNo(templateId, versionNo)) {
            throw new BizException("版本号已存在：" + versionNo);
        }

        // 使用领域模型创建草稿版本
        TemplateVersion version = TemplateVersion.createDraft(templateId, versionNo, versionName);
        version.setCreatedAt(LocalDateTime.now());
        version.setUpdatedAt(LocalDateTime.now());

        return versionRepository.save(version);
    }

    /**
     * 按ID查询版本
     *
     * @param id 版本ID
     * @return 版本对象
     */
    public TemplateVersion getById(Long id) {
        TemplateVersion version = versionRepository.findById(id);
        if (version == null) {
            throw new BizException("版本不存在：" + id);
        }
        return version;
    }

    /**
     * 发布版本
     * 只有草稿状态才能发布
     *
     * @param versionId 版本ID
     * @param publishBy 发布人ID
     */
    @Transactional
    public void publish(Long versionId, Long publishBy) {
        log.info("Publishing version: {}, by user: {}", versionId, publishBy);

        TemplateVersion version = versionRepository.findById(versionId);
        if (version == null) {
            throw new BizException("版本不存在：" + versionId);
        }

        // 检查是否为草稿状态
        if (!version.isDraft()) {
            throw new BizException("只有草稿状态才能发布");
        }

        // 使用领域模型发布版本
        version.publish(publishBy);
        version.setUpdatedAt(LocalDateTime.now());
        versionRepository.update(version);

        // 更新模板的当前版本ID
        templateService.updateCurrentVersion(version.getTemplateId(), versionId);

        log.info("Version published successfully: {}", versionId);
    }

    /**
     * 查找模板的当前发布版本
     *
     * @param templateId 模板ID
     * @return 当前发布版本，如果没有则返回null
     */
    public TemplateVersion findCurrentVersion(Long templateId) {
        return versionRepository.findCurrentVersion(templateId);
    }
}
```

---

### Task 7: Run Tests to Verify Refactoring

**Files:**
- Test: All existing tests in `src/test/java/com/contract/application/template/`

- [ ] **Step 1: Run all application tests**

Run: `cd /home/wula/IdeaProjects/dymic && mvn test -Dtest=TemplateServiceTest,TemplateVersionServiceTest`

Expected: All tests pass (GREEN)

- [ ] **Step 2: Run full test suite**

Run: `cd /home/wula/IdeaProjects/dymic && mvn test`

Expected: All tests pass (GREEN)

---

### Task 8: Commit Refactoring Changes

**Files:**
- All modified files

- [ ] **Step 1: Stage all changes**

Run: `cd /home/wula/IdeaProjects/dymic && git add src/main/java/com/contract/domain/template/repository/ src/main/java/com/contract/infrastructure/persistence/repository/ src/main/java/com/contract/application/template/TemplateService.java src/main/java/com/contract/application/template/TemplateVersionService.java`

- [ ] **Step 2: Create commit with detailed message**

Run: `cd /home/wula/IdeaProjects/dymic && git commit -m "$(cat <<'EOF'
refactor: implement DDD repository pattern for template module

Refactored from direct Mapper usage to DDD Repository pattern to better
align with Domain-Driven Design principles and dependency inversion.

Changes:
- Created TemplateRepository interface in domain layer
- Created TemplateVersionRepository interface in domain layer
- Implemented TemplateRepositoryImpl in infrastructure layer
- Implemented TemplateVersionRepositoryImpl in infrastructure layer
- Refactored TemplateService to use TemplateRepository
- Refactored TemplateVersionService to use TemplateVersionRepository
- Moved Entity-Domain conversion logic from Service to Repository layer

Architecture improvements:
- Service layer now depends only on domain repository interfaces
- Repository implementations encapsulate persistence details
- Entity-Domain conversion isolated in infrastructure layer
- Better separation of concerns and testability

All existing tests pass without modification.
EOF
)"`

Expected: Commit created successfully

- [ ] **Step 3: Verify commit**

Run: `cd /home/wula/IdeaProjects/dymic && git log --oneline -1`

Expected: Show the refactoring commit

---

## Summary

This refactoring establishes proper DDD layered architecture:
- **Domain Layer**: Repository interfaces define operations on domain models
- **Infrastructure Layer**: Repository implementations use Mappers and handle Entity-Domain conversion
- **Application Layer**: Services depend only on Repository interfaces, following dependency inversion

**Benefits:**
1. Clean separation of concerns
2. Domain layer has no infrastructure dependencies
3. Service layer is simpler and more focused on business logic
4. Repository implementations can be easily swapped (e.g., for testing)
5. Entity-Domain conversion is centralized in one place

**No test changes required** - Service layer API remains the same.