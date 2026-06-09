package com.contract.application.template;

import com.contract.domain.template.Template;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TemplateServiceTest {

    @Autowired
    private TemplateService templateService;

    @Test
    void testCreateTemplate() {
        Template template = templateService.createTemplate(
            "PURCHASE_CONTRACT",
            "采购合同模板",
            "用于采购业务",
            "PURCHASE"
        );

        assertNotNull(template.getId());
        assertEquals("PURCHASE_CONTRACT", template.getTemplateCodeValue());
        assertEquals("ENABLED", template.getStatus().name());
    }

    @Test
    void testGetTemplateByCode() {
        templateService.createTemplate("TEST_CONTRACT", "测试合同", "测试用", "TEST");

        Template template = templateService.getByCode("TEST_CONTRACT");
        assertNotNull(template);
        assertEquals("测试合同", template.getTemplateNameValue());
    }

    @Test
    void testDisableTemplate() {
        Template template = templateService.createTemplate(
            "DISABLE_TEST_" + System.nanoTime(),
            "停用测试",
            "测试停用",
            "TEST"
        );

        templateService.disable(template.getIdValue());

        Template disabled = templateService.getById(template.getIdValue());
        assertEquals("DISABLED", disabled.getStatus().name());
    }

    @Test
    void testEnableTemplate() {
        Template template = templateService.createTemplate(
            "ENABLE_TEST_" + System.nanoTime(),
            "启用测试",
            "测试启用",
            "TEST"
        );

        templateService.disable(template.getIdValue());
        Template disabled = templateService.getById(template.getIdValue());
        assertEquals("DISABLED", disabled.getStatus().name());

        templateService.enable(template.getIdValue());
        Template enabled = templateService.getById(template.getIdValue());
        assertEquals("ENABLED", enabled.getStatus().name());
    }

    @Test
    void testCreateTemplateWithDuplicateCode() {
        templateService.createTemplate("DUPLICATE_TEST", "测试重复", "测试", "TEST");

        assertThrows(Exception.class, () -> {
            templateService.createTemplate("DUPLICATE_TEST", "重复模板", "测试重复", "TEST");
        });
    }

    @Test
    void testGetTemplateByNonExistentId() {
        assertThrows(Exception.class, () -> {
            templateService.getById(999999L);
        });
    }

    @Test
    void testGetTemplateByNonExistentCode() {
        assertThrows(Exception.class, () -> {
            templateService.getByCode("NON_EXISTENT_CODE");
        });
    }
}
