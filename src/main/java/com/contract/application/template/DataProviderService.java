package com.contract.application.template;

import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.application.template.convert.DataProviderConverter;
import com.contract.common.exception.BizException;
import com.contract.common.util.JsonbUtils;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.repository.DataProviderRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据提供方应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataProviderService {
    private final DataProviderRepository repository;
    private final DataProviderConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public DataProviderDTO create(DataProviderCreateDTO dto) {
        if (repository.existsByProviderCode(dto.getProviderCode())) {
            throw new BizException("数据提供方编码已存在：" + dto.getProviderCode());
        }

        DataProvider provider = converter.toDomain(dto);
        provider.setId(idGenerator.nextId());
        provider.setStatus("ENABLED");
        provider.setCreatedAt(LocalDateTime.now());
        provider.setUpdatedAt(LocalDateTime.now());

        if (dto.getConfigJson() != null) {
            provider.setConfigJson(dto.getConfigJson());
        }

        if (dto.getCacheEnabled() != null) {
            provider.setCacheEnabled(dto.getCacheEnabled());
        } else {
            provider.setCacheEnabled(0);
        }

        if (dto.getCacheTtlSeconds() != null) {
            provider.setCacheTtlSeconds(dto.getCacheTtlSeconds());
        }

        repository.save(provider);

        DataProviderDTO result = converter.toDTO(provider);
        result.setConfigJson(dto.getConfigJson());
        return result;
    }

    public DataProviderDTO getById(Long id) {
        DataProvider provider = repository.findById(id);
        if (provider == null) {
            throw new BizException("数据提供方不存在：" + id);
        }

        DataProviderDTO result = converter.toDTO(provider);
        // 转换 configJson
        if (provider.getConfigJson() != null) {
            if (provider.getConfigJson() instanceof String) {
                result.setConfigJson(JsonbUtils.fromJsonMap((String) provider.getConfigJson()));
            } else if (provider.getConfigJson() instanceof Map) {
                result.setConfigJson((Map<String, Object>) provider.getConfigJson());
            }
        }
        return result;
    }

    public List<DataProviderDTO> list() {
        List<DataProvider> providers = repository.findAll();
        List<DataProviderDTO> results = converter.toDTOList(providers);

        // 转换每个 provider 的 configJson
        for (int i = 0; i < results.size(); i++) {
            DataProvider provider = providers.get(i);
            DataProviderDTO dto = results.get(i);
            if (provider.getConfigJson() != null) {
                if (provider.getConfigJson() instanceof String) {
                    dto.setConfigJson(JsonbUtils.fromJsonMap((String) provider.getConfigJson()));
                } else if (provider.getConfigJson() instanceof Map) {
                    dto.setConfigJson((Map<String, Object>) provider.getConfigJson());
                }
            }
        }
        return results;
    }

    @Transactional
    public DataProviderDTO update(Long id, DataProviderUpdateDTO dto) {
        DataProvider provider = repository.findById(id);
        if (provider == null) {
            throw new BizException("数据提供方不存在：" + id);
        }

        converter.updateDomainFromDTO(dto, provider);
        provider.setUpdatedAt(LocalDateTime.now());

        if (dto.getConfigJson() != null) {
            provider.setConfigJson(dto.getConfigJson());
        }

        repository.update(provider);

        DataProviderDTO result = converter.toDTO(provider);
        result.setConfigJson(dto.getConfigJson());
        return result;
    }
}