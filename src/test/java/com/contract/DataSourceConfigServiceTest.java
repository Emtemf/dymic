package com.contract;

import com.contract.application.template.DataSourceConfigService;
import com.contract.application.template.dto.BusinessDataSourceRequest;
import com.contract.application.template.dto.DataProviderDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataSourceConfigServiceTest {

    @Autowired
    private DataSourceConfigService service;

    @Test
    void createStaticBusinessConfigShouldPersistBusinessCategory() {
        BusinessDataSourceRequest request = BusinessDataSourceRequest.builder()
            .providerName("城市选择")
            .providerType("STATIC")
            .configJson("[{\"value\":\"BJ\",\"label\":\"北京\"}]")
            .build();

        DataProviderDTO result = service.create(request);

        assertNotNull(result.getId());
        assertEquals("STATIC", result.getProviderType());
        assertEquals("BUSINESS", result.getDataSourceCategory());
    }

    @Test
    void listBusinessConfigsShouldOnlyReturnBusinessProviders() {
        BusinessDataSourceRequest request = BusinessDataSourceRequest.builder()
            .providerName("行业字典")
            .providerType("DICT")
            .configJson("{\"dictType\":\"INDUSTRY_TYPE\"}")
            .build();

        service.create(request);

        List<DataProviderDTO> result = service.listBusinessConfigs();

        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(item -> "BUSINESS".equals(item.getDataSourceCategory())));
    }
}
