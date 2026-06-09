package com.contract;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ArchitectureDependencyTest {
    @Test
    void pomShouldNotContainMybatisPlusStarter() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        assertFalse(pom.contains("mybatis-plus-spring-boot3-starter"));
    }

    @Test
    void mapperXmlFileNamesShouldNotContainEntity() throws Exception {
        try (var stream = Files.list(Path.of("src/main/resources/mapper"))) {
            assertFalse(stream.anyMatch(path -> path.getFileName().toString().contains("Entity")));
        }
    }

    @Test
    void repositoryImplementationsShouldNotUseMybatisPlusImports() throws Exception {
        try (var paths = Files.walk(Path.of("src/main/java/com/contract/infrastructure/persistence/repository"))) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                String content = Files.readString(path);
                assertFalse(content.contains("com.baomidou"), path.toString());
                assertFalse(content.contains("LambdaQueryWrapper"), path.toString());
            }
        }
    }
}
