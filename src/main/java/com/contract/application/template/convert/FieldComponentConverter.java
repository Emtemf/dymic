package com.contract.application.template.convert;

import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.domain.template.FieldComponent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(componentModel = "spring")
public interface FieldComponentConverter {
    FieldComponentDTO toDTO(FieldComponent domain);
    FieldComponent toDomain(FieldComponentDTO dto);

    @NullValuePropertyMappingStrategy(NullValuePropertyMappingStrategy.IGNORE)
    void updateDomainFromDTO(FieldComponentDTO dto, @MappingTarget FieldComponent domain);

    List<FieldComponentDTO> toDTOList(List<FieldComponent> domains);
}