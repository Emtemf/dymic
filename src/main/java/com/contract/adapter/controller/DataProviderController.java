package com.contract.adapter.controller;

import com.contract.adapter.controller.request.DataProviderCreateReq;
import com.contract.application.template.DataProviderService;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据提供方 Controller
 */
@RestController
@RequestMapping({"/api/data-providers", "/api/v2/it/data-providers"})
@RequiredArgsConstructor
public class DataProviderController {
    private final DataProviderService service;

    @PostMapping
    public Result<DataProviderDTO> create(@Valid @RequestBody DataProviderCreateReq req) {
        DataProviderCreateDTO dto = new DataProviderCreateDTO();
        dto.setProviderCode(req.getProviderCode());
        dto.setProviderName(req.getProviderName());
        dto.setProviderType(req.getProviderType());
        dto.setConfigJson(req.getConfigJson());
        DataProviderDTO result = service.create(dto);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<DataProviderDTO> getById(@PathVariable Long id) {
        DataProviderDTO result = service.getById(id);
        return Result.ok(result);
    }

    @GetMapping
    public Result<List<DataProviderDTO>> list() {
        List<DataProviderDTO> result = service.list();
        return Result.ok(result);
    }

    @PutMapping("/{id}")
    public Result<DataProviderDTO> update(@PathVariable Long id, @RequestBody DataProviderUpdateDTO dto) {
        DataProviderDTO result = service.update(id, dto);
        return Result.ok(result);
    }
}
