package com.contract.application.template.convert;

import com.contract.application.template.dto.FieldDefDTO;
import com.contract.domain.template.FieldDef;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

/**
 * FieldDef Domain ↔ DTO 转换器
 */
@Mapper(componentModel = "spring")
public interface FieldDefConverter {
    FieldDefDTO toDTO(FieldDef domain);
    FieldDef toDomain(FieldDefDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDomainFromDTO(FieldDefDTO dto, @MappingTarget FieldDef domain);

    List<FieldDefDTO> toDTOList(List<FieldDef> domains);
}