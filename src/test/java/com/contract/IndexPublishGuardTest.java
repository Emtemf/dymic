package com.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexPublishGuardTest {
    @Test
    void indexPageShouldDisablePublishButtonDuringPublishFlow() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/static/index.html"));
        assertTrue(html.contains("publishVersionBtn.disabled = true"));
        assertTrue(html.contains("versionData.versionStatus = 'PUBLISHED'"));
    }
}
