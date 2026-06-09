package com.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidencePackTest {
    @Test
    void browserFlowEvidenceFilesShouldExist() {
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/browser-flows/designer-e2e.md")));
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/browser-flows/business-template-e2e.md")));
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/browser-flows/component-library-e2e.md")));
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/browser-flows/nested-combinations-e2e.md")));
    }

    @Test
    void jsonbAndPerformanceEvidenceShouldExist() {
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/sql-and-xml/jsonb-query-path.md")));
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/performance/mvp-scale.md")));
    }

    @Test
    void migrationChecklistShouldExist() {
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/file-notes/migration-checklist.md")));
    }

    @Test
    void domainFlowDocumentShouldExist() {
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/file-notes/domain-object-flow.md")));
    }

    @Test
    void readmeShouldExist() {
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/README.md")));
        assertTrue(Files.exists(Path.of("docs/evidence/dynamic-template-audit/manual-checklist.md")));
    }
}
