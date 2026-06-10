package com.contract.application.template;

import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataSourceQueryDTO;
import com.contract.application.template.dto.OptionDataDTO;
import com.contract.common.exception.BizException;
import com.contract.domain.shared.types.ConfigJson;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.repository.DataProviderRepository;
import com.contract.infrastructure.gateway.DataProviderExecutorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataSourceQueryFacadeService {
    private final DataSourceConfigService dataSourceConfigService;
    private final DataProviderService dataProviderService;
    private final DataProviderRepository repository;
    private final DataProviderExecutorFactory executorFactory;

    public List<DataSourceQueryDTO> queryAll() {
        List<DataSourceQueryDTO> result = new ArrayList<>();
        result.addAll(dataSourceConfigService.listBusinessConfigs().stream().map(this::toQueryDTO).toList());
        result.addAll(dataProviderService.listITConfigs().stream().map(this::toQueryDTO).toList());
        return result;
    }

    public List<DataSourceQueryDTO> queryByType(String providerType) {
        return queryAll().stream()
            .filter(item -> providerType.equals(item.getProviderType()))
            .toList();
    }

    public List<OptionDataDTO> executeQuery(Long providerId) {
        DataProvider provider = repository.findById(providerId);
        if (provider == null) {
            throw new BizException("数据源不存在: " + providerId);
        }
        return executorFactory.getExecutor(provider.getProviderType()).execute(provider);
    }

    private DataSourceQueryDTO toQueryDTO(DataProviderDTO dto) {
        return DataSourceQueryDTO.builder()
            .id(dto.getId())
            .providerCode(dto.getProviderCode())
            .providerName(dto.getProviderName())
            .providerType(dto.getProviderType())
            .dataSourceCategory(dto.getDataSourceCategory())
            .configSource(dto.getDataSourceCategory())
            .configJson(dto.getConfigJson())
            .isTree(isTreeConfig(dto.getConfigJson()))
            .build();
    }

    private boolean isTreeConfig(String configJson) {
        if (configJson == null || configJson.isBlank()) {
            return false;
        }
        return new ConfigJson(configJson).isTreeStructure();
    }
}
