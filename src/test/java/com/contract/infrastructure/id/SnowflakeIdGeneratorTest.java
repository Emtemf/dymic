package com.contract.infrastructure.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeIdGeneratorTest {

    @Test
    void testGenerateId() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        long id = generator.nextId();
        assertTrue(id > 0);
    }

    @Test
    void testGenerateMultipleIds() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        long id1 = generator.nextId();
        long id2 = generator.nextId();
        assertNotEquals(id1, id2);
    }

    @Test
    void testGenerateIdShouldBeUnique() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
        long id1 = generator.nextId();
        long id2 = generator.nextId();
        long id3 = generator.nextId();
        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
    }
}
