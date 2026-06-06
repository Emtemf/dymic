package com.contract.application.template;

import com.contract.domain.template.TemplateVersion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TemplateVersionServiceTest {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private TemplateVersionService versionService;

    @Test
    void testCreateDraftVersion() {
        // 先创建模板
        Long templateId = templateService.createTemplate(
            "VERSION_TEST",
            "版本测试",
            "测试版本",
            "TEST"
        ).getId();

        // 创建草稿版本
        TemplateVersion version = versionService.createDraft(templateId, 1, "V1.0");

        assertNotNull(version.getId());
        assertEquals(templateId, version.getTemplateId());
        assertEquals(1, version.getVersionNo());
        assertEquals("V1.0", version.getVersionName());
        assertEquals("DRAFT", version.getVersionStatus());
        assertNull(version.getPublishTime());
        assertNull(version.getPublishBy());
    }

    @Test
    void testCreateDraftVersionWithDuplicateVersionNo() {
        // 先创建模板
        Long templateId = templateService.createTemplate(
            "DUP_VERSION_TEST",
            "重复版本测试",
            "测试重复版本号",
            "TEST"
        ).getId();

        // 创建第一个版本
        versionService.createDraft(templateId, 1, "V1.0");

        // 尝试创建相同版本号的版本，应该抛出异常
        assertThrows(Exception.class, () -> {
            versionService.createDraft(templateId, 1, "V1.0-Duplicate");
        });
    }

    @Test
    void testPublishVersion() {
        // 创建模板和草稿版本
        Long templateId = templateService.createTemplate(
            "PUBLISH_TEST",
            "发布测试",
            "测试发布",
            "TEST"
        ).getId();

        Long versionId = versionService.createDraft(templateId, 1, "V1.0").getId();

        // 发布版本
        versionService.publish(versionId, 1001L);

        // 验证发布后的状态
        TemplateVersion published = versionService.getById(versionId);
        assertEquals("PUBLISHED", published.getVersionStatus());
        assertNotNull(published.getPublishTime());
        assertEquals(1001L, published.getPublishBy());
    }

    @Test
    void testPublishNonDraftVersion() {
        // 创建模板和版本
        Long templateId = templateService.createTemplate(
            "PUBLISH_FAIL_TEST",
            "发布失败测试",
            "测试非草稿发布",
            "TEST"
        ).getId();

        Long versionId = versionService.createDraft(templateId, 1, "V1.0").getId();

        // 先发布一次
        versionService.publish(versionId, 1001L);

        // 尝试再次发布已发布的版本，应该抛出异常
        assertThrows(Exception.class, () -> {
            versionService.publish(versionId, 1002L);
        });
    }

    @Test
    void testGetById() {
        // 创建模板和版本
        Long templateId = templateService.createTemplate(
            "GET_VERSION_TEST",
            "查询版本测试",
            "测试查询",
            "TEST"
        ).getId();

        Long versionId = versionService.createDraft(templateId, 1, "V1.0").getId();

        // 查询版本
        TemplateVersion version = versionService.getById(versionId);

        assertNotNull(version);
        assertEquals(versionId, version.getId());
        assertEquals("V1.0", version.getVersionName());
    }

    @Test
    void testGetByIdNotFound() {
        // 查询不存在的版本，应该抛出异常
        assertThrows(Exception.class, () -> {
            versionService.getById(999999L);
        });
    }

    @Test
    void testFindCurrentVersion() {
        // 创建模板和多个版本
        Long templateId = templateService.createTemplate(
            "CURRENT_VERSION_TEST",
            "当前版本测试",
            "测试查找当前版本",
            "TEST"
        ).getId();

        // 创建并发布版本1
        Long version1Id = versionService.createDraft(templateId, 1, "V1.0").getId();
        versionService.publish(version1Id, 1001L);

        // 创建但不发布版本2
        versionService.createDraft(templateId, 2, "V2.0");

        // 创建并发布版本3
        Long version3Id = versionService.createDraft(templateId, 3, "V3.0").getId();
        versionService.publish(version3Id, 1001L);

        // 查找当前发布版本，应该是版本3
        TemplateVersion currentVersion = versionService.findCurrentVersion(templateId);
        assertNotNull(currentVersion);
        assertEquals(3, currentVersion.getVersionNo());
        assertEquals("PUBLISHED", currentVersion.getVersionStatus());
    }

    @Test
    void testFindCurrentVersionNoPublished() {
        // 创建模板和草稿版本（不发布）
        Long templateId = templateService.createTemplate(
            "NO_PUBLISHED_TEST",
            "无发布版本测试",
            "测试没有发布版本",
            "TEST"
        ).getId();

        versionService.createDraft(templateId, 1, "V1.0");

        // 查找当前发布版本，应该返回null
        TemplateVersion currentVersion = versionService.findCurrentVersion(templateId);
        assertNull(currentVersion);
    }

    @Test
    void testCreateDraftWithNonExistentTemplate() {
        // 尝试为不存在的模板创建版本，应该抛出异常
        assertThrows(Exception.class, () -> {
            versionService.createDraft(999999L, 1, "V1.0");
        });
    }
}