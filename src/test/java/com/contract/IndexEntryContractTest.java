package com.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexEntryContractTest {
    @Test
    void indexPageShouldUseBackendTemplateFieldNames() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/static/index.html"));
        assertTrue(html.contains("bizType"));
        assertTrue(html.contains("templateDesc"));
        assertFalse(html.contains("templateType,"));
        assertFalse(html.contains("description,"));
    }

    @Test
    void indexPageShouldUseBackendVersionFieldNames() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/static/index.html"));
        assertTrue(html.contains("versionNo"));
        assertTrue(html.contains("versionName"));
        assertFalse(html.contains("versionNumber"));
    }

    @Test
    void indexPageShouldUnwrapApiResponseData() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/static/index.html"));
        assertTrue(html.contains("result.data.data"));
    }
}
