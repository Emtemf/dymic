package com.contract.application.template.convert;

import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.domain.template.DataProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据提供方 DTO <-> Domain 转换器
 *
 * 注意：DataProvider重构后使用值对象，不能直接用MapStruct映射
 */
@Mapper(componentModel = "spring")
public interface DataProviderConverter {

    /**
     * Domain -> DTO
     */
    default DataProviderDTO toDTO(DataProvider domain) {
        if (domain == null) {
            return null;
        }
        DataProviderDTO dto = new DataProviderDTO();
        dto.setId(domain.getId());
        dto.setProviderCode(domain.getProviderCode());
        dto.setProviderName(domain.getProviderName());
        dto.setProviderType(domain.getProviderType());
        dto.setConfigJson(domain.getConfigJson());
        dto.setCacheEnabled(domain.getCacheEnabled());
        dto.setCacheTtlSeconds(domain.getCacheTtlSeconds());
        dto.setIsTemporary(domain.getIsTemporary());
        dto.setStatus(domain.getStatus());
        dto.setCreatedBy(domain.getCreatedBy());
        dto.setCreatedName(domain.getCreatedName());
        dto.setCreatedAt(domain.getCreatedAt());
        dto.setUpdatedBy(domain.getUpdatedBy());
        dto.setUpdatedName(domain.getUpdatedName());
        dto.setUpdatedAt(domain.getUpdatedAt());
        return dto;
    }

    /**
     * CreateDTO -> Domain（使用静态工厂方法创建）
     */
    default DataProvider toDomain(DataProviderCreateDTO dto) {
        if (dto == null) {
            return null;
        }
        String providerType = dto.getProviderType();
        if ("STATIC".equals(providerType)) {
            return DataProvider.createStaticOptions(
                dto.getProviderName(),
                dto.getConfigJson() != null ? dto.getConfigJson() : "{}"
            );
        } else if ("DICT".equals(providerType)) {
            return DataProvider.createDict(
                extractDictType(dto.getConfigJson()),
                dto.getProviderName()
            );
        } else if ("HTTP".equals(providerType)) {
            return DataProvider.createHttp(
                dto.getProviderName(),
                dto.getConfigJson() != null ? dto.getConfigJson() : "{}"
            );
        } else if ("PLATFORM".equals(providerType)) {
            return DataProvider.createPlatform(
                dto.getProviderName(),
                dto.getConfigJson() != null ? dto.getConfigJson() : "{}"
            );
        } else if ("INTERNAL".equals(providerType)) {
            return DataProvider.createInternal(
                dto.getProviderName(),
                dto.getConfigJson() != null ? dto.getConfigJson() : "{}"
            );
        } else {
            return DataProvider.create(
                dto.getProviderCode(),
                dto.getProviderName(),
                providerType
            );
        }
    }

    /**
     * 从配置JSON中提取字典类型
     */
    private String extractDictType(String configJson) {
        if (configJson == null || !configJson.contains("dictType")) {
            return "UNKNOWN";
        }
        try {
            // 简单解析：提取 "dictType":"XXX"
            int start = configJson.indexOf("\"dictType\":\"") + 12;
            int end = configJson.indexOf("\"", start);
            return configJson.substring(start, end);
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * 批量转换
     */
    default List<DataProviderDTO> toDTOList(List<DataProvider> domains) {
        if (domains == null) {
            return null;
        }
        return domains.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}