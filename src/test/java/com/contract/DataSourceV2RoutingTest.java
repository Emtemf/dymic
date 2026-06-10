package com.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class DataSourceV2RoutingTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void get_api_v2_it_data_providers_shouldReturnOk_whenNoData() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/v2/it/data-providers",
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"success\":true"));
    }

    @Test
    void get_api_v2_ui_data_sources_query_shouldReturnOk_whenNoData() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/api/v2/ui/data-sources/query",
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"success\":true"));
    }

    @Test
    void post_api_v2_config_business_data_sources_shouldCreateStaticProvider() {
        String body = """
            {
              "providerName": "城市选择",
              "providerType": "STATIC",
              "configJson": "[{\\\"value\\\":\\\"BJ\\\",\\\"label\\\":\\\"北京\\\"}]"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/v2/config/business-data-sources",
            new HttpEntity<>(body, headers),
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"providerType\":\"STATIC\""));
        assertTrue(response.getBody().contains("\"dataSourceCategory\":\"BUSINESS\""));
    }
}
