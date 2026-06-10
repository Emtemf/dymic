package com.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexPublishContractTest {
    @Test
    void indexPageShouldUseBackendPublishEndpointShape() throws Exception {
        String apiJs = Files.readString(Path.of("src/main/resources/static/js/contract-api.js"));
        assertTrue(apiJs.contains("/templates/versions/${versionId}/publish"));
    }

    @Test
    void indexPageShouldTreatTemplateAndVersionIdsAsStrings() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/static/index.html"));
        assertTrue(html.contains("String(templateData.id)"));
        assertTrue(html.contains("String(versionData.id)"));
        assertFalse(html.contains("Number(templateData.id)"));
        assertFalse(html.contains("Number(versionData.id)"));
    }
}
