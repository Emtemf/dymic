package com.contract;

import com.contract.application.template.DataProviderService;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.common.exception.BizException;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.repository.DataProviderRepository;
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
class DataProviderServiceTest {
    @Autowired
    private DataProviderService service;

    @Autowired
    private DataProviderRepository repository;

    @Test
    void testCreateDataProvider() {
        DataProviderCreateDTO dto = new DataProviderCreateDTO();
        dto.setProviderCode("SUPPLIER_LIST");
        dto.setProviderName("供应商列表");
        dto.setProviderType("HTTP");
        dto.setConfigJson("{\"url\": \"http://api.example.com/suppliers\"}");

        DataProviderDTO result = service.create(dto);

        assertNotNull(result.getId());
        assertEquals("SUPPLIER_LIST", result.getProviderCode());
        assertEquals("ENABLED", result.getStatus());
    }

    @Test
    void testCreateDuplicateCode() {
        DataProviderCreateDTO dto1 = new DataProviderCreateDTO();
        dto1.setProviderCode("DUPLICATE_TEST");
        dto1.setProviderName("测试1");
        dto1.setProviderType("STATIC");
        dto1.setConfigJson("{\"options\":[]}");
        service.create(dto1);

        DataProviderCreateDTO dto2 = new DataProviderCreateDTO();
        dto2.setProviderCode("DUPLICATE_TEST");
        dto2.setProviderName("测试2");
        dto2.setProviderType("STATIC");
        dto2.setConfigJson("{\"options\":[]}");

        assertThrows(BizException.class, () -> service.create(dto2));
    }

    @Test
    void testGetById() {
        DataProviderCreateDTO dto = new DataProviderCreateDTO();
        dto.setProviderCode("GET_TEST");
        dto.setProviderName("查询测试");
        dto.setProviderType("STATIC");
        dto.setConfigJson("{\"test\":true}");

        DataProviderDTO created = service.create(dto);
        DataProviderDTO result = service.getById(created.getId());

        assertEquals("GET_TEST", result.getProviderCode());
    }

    @Test
    void testUpdate() {
        DataProviderCreateDTO dto = new DataProviderCreateDTO();
        dto.setProviderCode("UPDATE_TEST");
        dto.setProviderName("更新测试");
        dto.setProviderType("STATIC");
        dto.setConfigJson("{\"test\":true}");

        DataProviderDTO created = service.create(dto);

        DataProviderUpdateDTO updateDto = new DataProviderUpdateDTO();
        updateDto.setProviderName("更新后名称");

        DataProviderDTO result = service.update(created.getId(), updateDto);
        assertEquals("更新后名称", result.getProviderName());
    }

    @Test
    void testList() {
        DataProviderCreateDTO dto = new DataProviderCreateDTO();
        dto.setProviderCode("LIST_TEST");
        dto.setProviderName("列表测试");
        dto.setProviderType("STATIC");
        dto.setConfigJson("{\"test\":true}");
        service.create(dto);

        List<DataProviderDTO> result = service.list();
        assertTrue(result.size() > 0);
    }

    @Test
    void testGetByIdNotFound() {
        assertThrows(BizException.class, () -> service.getById(999999L));
    }

    @Test
    void testUpdateNotFound() {
        DataProviderUpdateDTO updateDto = new DataProviderUpdateDTO();
        updateDto.setProviderName("更新后名称");

        assertThrows(BizException.class, () -> service.update(999999L, updateDto));
    }


    @Test
    void testFindByCategorySeparatesBusinessAndItConfigs() throws Exception {
        DataProvider businessProvider = repository.save(
            DataProvider.create("BUSINESS_CATEGORY_TEST", "业务分类测试", "STATIC")
        );
        DataProvider itProvider = repository.save(
            DataProvider.create("IT_CATEGORY_TEST", "IT分类测试", "HTTP")
        );

        var method = repository.getClass().getMethod("findByCategory", String.class);

        @SuppressWarnings("unchecked")
        List<DataProvider> businessResults = (List<DataProvider>) method.invoke(repository, "BUSINESS");
        @SuppressWarnings("unchecked")
        List<DataProvider> itResults = (List<DataProvider>) method.invoke(repository, "IT");

        assertTrue(businessResults.stream().anyMatch(p -> "BUSINESS_CATEGORY_TEST".equals(p.getProviderCode())));
        assertTrue(businessResults.stream().allMatch(p -> "BUSINESS".equals(p.getDataSourceCategory())));
        assertTrue(businessResults.stream().noneMatch(p -> "IT_CATEGORY_TEST".equals(p.getProviderCode())));

        assertTrue(itResults.stream().anyMatch(p -> "IT_CATEGORY_TEST".equals(p.getProviderCode())));
        assertTrue(itResults.stream().allMatch(p -> "IT".equals(p.getDataSourceCategory())));
        assertTrue(itResults.stream().noneMatch(p -> "BUSINESS_CATEGORY_TEST".equals(p.getProviderCode())));

        assertNotNull(businessProvider.getId());
        assertNotNull(itProvider.getId());
    }
}