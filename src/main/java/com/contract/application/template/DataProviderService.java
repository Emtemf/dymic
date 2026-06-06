package com.contract.application.template;

import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.application.template.convert.DataProviderConverter;
import com.contract.common.exception.BizException;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.repository.DataProviderRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

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

        repository.save(provider);
        return converter.toDTO(provider);
    }

    public DataProviderDTO getById(Long id) {
        DataProvider provider = repository.findById(id);
        if (provider == null) {
            throw new BizException("数据提供方不存在：" + id);
        }
        return converter.toDTO(provider);
    }

    public List<DataProviderDTO> list() {
        List<DataProvider> providers = repository.findAll();
        return converter.toDTOList(providers);
    }

    @Transactional
    public DataProviderDTO update(Long id, DataProviderUpdateDTO dto) {
        DataProvider provider = repository.findById(id);
        if (provider == null) {
            throw new BizException("数据提供方不存在：" + id);
        }

        converter.updateDomainFromDTO(dto, provider);
        provider.setUpdatedAt(LocalDateTime.now());
        repository.update(provider);
        return converter.toDTO(provider);
    }
}