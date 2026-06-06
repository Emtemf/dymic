package com.contract;

import com.contract.application.template.DataProviderService;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DataProviderServiceTest {
    @Autowired
    private DataProviderService service;

    @Test
    void testCreateDataProvider() {
        DataProviderCreateDTO dto = new DataProviderCreateDTO();
        dto.setProviderCode("SUPPLIER_LIST");
        dto.setProviderName("供应商列表");
        dto.setProviderType("HTTP");

        Map<String, Object> config = new HashMap<>();
        config.put("url", "http://api.example.com/suppliers");
        dto.setConfigJson(config);

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
        dto1.setConfigJson(new HashMap<>());
        service.create(dto1);

        DataProviderCreateDTO dto2 = new DataProviderCreateDTO();
        dto2.setProviderCode("DUPLICATE_TEST");
        dto2.setProviderName("测试2");
        dto2.setProviderType("STATIC");
        dto2.setConfigJson(new HashMap<>());

        assertThrows(BizException.class, () -> service.create(dto2));
    }

    @Test
    void testGetById() {
        DataProviderCreateDTO dto = new DataProviderCreateDTO();
        dto.setProviderCode("GET_TEST");
        dto.setProviderName("查询测试");
        dto.setProviderType("STATIC");
        dto.setConfigJson(new HashMap<>());

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
        dto.setConfigJson(new HashMap<>());

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
        dto.setConfigJson(new HashMap<>());
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
}