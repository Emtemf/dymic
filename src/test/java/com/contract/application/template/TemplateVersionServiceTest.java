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

    private static String suffix() {
        return "_" + System.nanoTime();
    }

    @Test
    void testCreateDraftVersion() {
        Long templateId = templateService.createTemplate(
            "VERSION_TEST" + suffix(),
            "版本测试",
            "测试版本",
            "TEST"
        ).getId();

        // createTemplate auto-creates version_no=1, so create version_no=2
        TemplateVersion version = versionService.createDraft(templateId, 2, "V2.0");

        assertNotNull(version.getId());
        assertEquals(templateId, version.getTemplateId());
        assertEquals(2, version.getVersionNo());
        assertEquals("V2.0", version.getVersionName());
        assertEquals("DRAFT", version.getVersionStatus());
        assertNull(version.getPublishTime());
        assertNull(version.getPublishBy());
    }

    @Test
    void testCreateDraftVersionWithDuplicateVersionNo() {
        Long templateId = templateService.createTemplate(
            "DUP_VERSION_TEST" + suffix(),
            "重复版本测试",
            "测试重复版本号",
            "TEST"
        ).getId();

        // createTemplate auto-creates version_no=1, so trying to create another is duplicate
        assertThrows(Exception.class, () -> {
            versionService.createDraft(templateId, 1, "V1.0-Duplicate");
        });
    }

    @Test
    void testPublishVersion() {
        Long templateId = templateService.createTemplate(
            "PUBLISH_TEST" + suffix(),
            "发布测试",
            "测试发布",
            "TEST"
        ).getId();

        // createTemplate auto-creates version_no=1, publish that one
        Long versionId = versionService.createDraft(templateId, 2, "V2.0").getId();

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
        Long templateId = templateService.createTemplate(
            "PUBLISH_FAIL_TEST" + suffix(),
            "发布失败测试",
            "测试非草稿发布",
            "TEST"
        ).getId();

        Long versionId = versionService.createDraft(templateId, 2, "V2.0").getId();

        // 先发布一次
        versionService.publish(versionId, 1001L);

        // 尝试再次发布已发布的版本，应该抛出异常
        assertThrows(Exception.class, () -> {
            versionService.publish(versionId, 1002L);
        });
    }

    @Test
    void testGetById() {
        Long templateId = templateService.createTemplate(
            "GET_VERSION_TEST" + suffix(),
            "查询版本测试",
            "测试查询",
            "TEST"
        ).getId();

        Long versionId = versionService.createDraft(templateId, 2, "V2.0").getId();

        // 查询版本
        TemplateVersion version = versionService.getById(versionId);

        assertNotNull(version);
        assertEquals(versionId, version.getId());
        assertEquals("V2.0", version.getVersionName());
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
            "CURRENT_VERSION_TEST" + suffix(),
            "当前版本测试",
            "测试查找当前版本",
            "TEST"
        ).getId();

        // createTemplate auto-creates version_no=1, publish it
        Long version1Id = versionService.createDraft(templateId, 2, "V2.0").getId();
        versionService.publish(version1Id, 1001L);

        // create but don't publish version3
        versionService.createDraft(templateId, 3, "V3.0");

        // create and publish version4
        Long version4Id = versionService.createDraft(templateId, 4, "V4.0").getId();
        versionService.publish(version4Id, 1001L);

        // find current published version — should be version4
        TemplateVersion currentVersion = versionService.findCurrentVersion(templateId);
        assertNotNull(currentVersion);
        assertEquals(4, currentVersion.getVersionNo());
        assertEquals("PUBLISHED", currentVersion.getVersionStatus());
    }

    @Test
    void testFindCurrentVersionNoPublished() {
        // 创建模板和草稿版本（不发布）
        Long templateId = templateService.createTemplate(
            "NO_PUBLISHED_TEST" + suffix(),
            "无发布版本测试",
            "测试没有发布版本",
            "TEST"
        ).getId();

        // createTemplate auto-creates version_no=1 (DRAFT), don't publish it

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