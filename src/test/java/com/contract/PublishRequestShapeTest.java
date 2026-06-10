package com.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PublishRequestShapeTest {
    @Test
    void apiPostShouldAllowBodylessPublishCalls() throws Exception {
        String apiJs = Files.readString(Path.of("src/main/resources/static/js/api.js"));
        assertTrue(apiJs.contains("const requestInit = {"));
        assertTrue(apiJs.contains("if (data !== undefined && data !== null)"));
        assertTrue(apiJs.contains("requestInit.body = JSON.stringify(data)"));
        assertTrue(apiJs.contains("fetch(fullUrl, requestInit)"));
    }
}
