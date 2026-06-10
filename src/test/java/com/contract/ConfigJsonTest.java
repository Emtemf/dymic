package com.contract;

import com.contract.application.template.dto.OptionDataDTO;
import com.contract.domain.shared.types.ConfigJson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigJsonTest {

    @Test
    void parseTreeOptionsShouldReturnTreeNodes() {
        ConfigJson configJson = new ConfigJson("""
            {
              "structure": "tree",
              "children": [
                {
                  "value": "BJ",
                  "label": "北京",
                  "children": [
                    {"value": "HD", "label": "海淀"}
                  ]
                }
              ]
            }
            """);

        assertTrue(configJson.isTreeStructure());
        List<OptionDataDTO> options = configJson.parseOptions();
        assertEquals(1, options.size());
        assertEquals("BJ", options.getFirst().getValue());
        assertEquals(1, options.getFirst().getChildren().size());
        assertEquals("HD", options.getFirst().getChildren().getFirst().getValue());
    }
}
