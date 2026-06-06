package com.contract.application.template.convert;

import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.domain.template.DataProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

/**
 * 数据提供方 DTO <-> Domain 转换器
 */
@Mapper(componentModel = "spring")
public interface DataProviderConverter {
    @Mapping(target = "configJson", ignore = true)
    DataProviderDTO toDTO(DataProvider domain);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdName", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedName", ignore = true)
    @Mapping(target = "configJson", ignore = true)
    DataProvider toDomain(DataProviderCreateDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "providerCode", ignore = true)
    @Mapping(target = "providerType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdName", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedName", ignore = true)
    @Mapping(target = "configJson", ignore = true)
    void updateDomainFromDTO(DataProviderUpdateDTO dto, @MappingTarget DataProvider domain);

    @Mapping(target = "configJson", ignore = true)
    List<DataProviderDTO> toDTOList(List<DataProvider> domains);
}