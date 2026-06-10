package com.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateSelectorContractTest {
    @Test
    void templateSelectorShouldUnwrapTemplateAndVersionLists() throws Exception {
        String js = Files.readString(Path.of("src/main/resources/static/config/js/template-selector.js"));
        assertTrue(js.contains("result.data?.data || result.data || []"));
    }
}
