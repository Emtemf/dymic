package com.contract.application.template;

import com.contract.application.template.convert.DataProviderConverter;
import com.contract.application.template.dto.BusinessDataSourceRequest;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.common.exception.BizException;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.repository.DataProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DataSourceConfigService {
    private final DataProviderRepository repository;
    private final DataProviderConverter converter;

    @Transactional
    public DataProviderDTO create(BusinessDataSourceRequest request) {
        DataProvider provider;
        if ("STATIC".equals(request.getProviderType())) {
            provider = DataProvider.createStaticOptions(request.getProviderName(), request.getConfigJson());
        } else if ("DICT".equals(request.getProviderType())) {
            provider = DataProvider.createDict(extractDictType(request.getConfigJson()), request.getProviderName());
        } else {
            throw new BizException("业务配置仅支持 STATIC/DICT");
        }
        return converter.toDTO(repository.save(provider));
    }

    public List<DataProviderDTO> listBusinessConfigs() {
        return converter.toDTOList(repository.findByCategory("BUSINESS"));
    }

    private String extractDictType(String configJson) {
        if (configJson == null || !configJson.contains("dictType")) {
            throw new BizException("字典配置缺少 dictType");
        }
        try {
            int start = configJson.indexOf("\"dictType\":\"") + 12;
            int end = configJson.indexOf("\"", start);
            return configJson.substring(start, end);
        } catch (Exception e) {
            throw new BizException("字典配置格式无效");
        }
    }
}
