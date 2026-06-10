package com.contract;

import com.contract.application.template.DataSourceConfigService;
import com.contract.application.template.DataSourceQueryFacadeService;
import com.contract.application.template.DataProviderService;
import com.contract.application.template.dto.BusinessDataSourceRequest;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataSourceQueryDTO;
import com.contract.application.template.dto.OptionDataDTO;
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
class DataSourceQueryFacadeServiceTest {

    @Autowired
    private DataSourceQueryFacadeService service;

    @Autowired
    private DataSourceConfigService dataSourceConfigService;

    @Autowired
    private DataProviderService dataProviderService;

    @Test
    void queryAllShouldMergeBusinessAndItConfigs() {
        BusinessDataSourceRequest businessRequest = BusinessDataSourceRequest.builder()
            .providerName("城市选择")
            .providerType("STATIC")
            .configJson("[{\"value\":\"BJ\",\"label\":\"北京\"}]")
            .build();
        dataSourceConfigService.create(businessRequest);

        DataProviderCreateDTO itRequest = new DataProviderCreateDTO();
        itRequest.setProviderCode("IT_SUPPLIER_API");
        itRequest.setProviderName("供应商接口");
        itRequest.setProviderType("HTTP");
        itRequest.setConfigJson("{\"url\":\"http://api.example.com/suppliers\",\"method\":\"GET\"}");
        dataProviderService.create(itRequest);

        List<DataSourceQueryDTO> result = service.queryAll();

        assertNotNull(result);
        assertTrue(result.stream().anyMatch(item -> "BUSINESS".equals(item.getConfigSource())));
        assertTrue(result.stream().anyMatch(item -> "IT".equals(item.getConfigSource())));
    }

    @Test
    void queryAllShouldMarkTreeStaticConfigAsTree() {
        BusinessDataSourceRequest treeRequest = BusinessDataSourceRequest.builder()
            .providerName("区域树")
            .providerType("STATIC")
            .configJson("{\"structure\":\"tree\",\"children\":[{\"value\":\"BJ\",\"label\":\"北京\",\"children\":[{\"value\":\"HD\",\"label\":\"海淀\"}]}]}")
            .build();
        DataProviderDTO provider = dataSourceConfigService.create(treeRequest);

        List<DataSourceQueryDTO> result = service.queryAll();

        DataSourceQueryDTO treeConfig = result.stream()
            .filter(item -> provider.getId().equals(item.getId()))
            .findFirst()
            .orElseThrow();
        assertTrue(treeConfig.getIsTree());
    }

    @Test
    void executeQueryShouldReturnStaticOptions() {
        BusinessDataSourceRequest request = BusinessDataSourceRequest.builder()
            .providerName("合同类型")
            .providerType("STATIC")
            .configJson("[{\"value\":\"PURCHASE\",\"label\":\"采购合同\"},{\"value\":\"SALE\",\"label\":\"销售合同\"}]")
            .build();
        DataProviderDTO provider = dataSourceConfigService.create(request);

        List<OptionDataDTO> result = service.executeQuery(provider.getId());

        assertFalse(result.isEmpty());
        assertEquals("PURCHASE", result.getFirst().getValue());
    }
}
