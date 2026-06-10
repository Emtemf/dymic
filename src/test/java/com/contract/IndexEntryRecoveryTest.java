package com.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexEntryRecoveryTest {
    @Test
    void indexPageShouldHandleExistingTemplateAndVersionState() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/static/index.html"));
        assertTrue(html.contains("ContractAPI.template.list"));
        assertTrue(html.contains("ContractAPI.version.list"));
        assertTrue(html.contains("updateStatus()"));
    }

    @Test
    void indexPageShouldInitializeApiBeforeRecoveringState() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/static/index.html"));
        assertTrue(html.contains("await API.initialize()"));
        assertTrue(html.contains("apiBaseUrlInput.value = API.baseUrl"));
        assertTrue(html.contains("http://localhost:8888/api"));
    }

    @Test
    void indexPageShouldRecoverExistingVersionWhenCreateReturnsDuplicate() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/static/index.html"));
        assertTrue(html.contains("版本号已存在"));
        assertTrue(html.contains("ContractAPI.version.list(String(templateData.id))"));
        assertTrue(html.contains("versionData = versions[0]"));
        assertTrue(html.contains("publishVersionBtn.disabled = false"));
    }
}
