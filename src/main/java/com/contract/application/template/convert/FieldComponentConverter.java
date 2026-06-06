package com.contract.application.template.convert;

import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.FieldComponentCreateRequest;
import com.contract.application.template.dto.FieldComponentUpdateDTO;
import com.contract.domain.template.FieldComponent;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FieldComponentConverter {

    FieldComponentDTO toDTO(FieldComponent domain);

    FieldComponent toDomain(FieldComponentDTO dto);

    /**
     * 创建请求转领域对象
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    FieldComponent toDomain(FieldComponentCreateRequest request);

    /**
     * 更新DTO转领域对象（部分更新）
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "layoutNodeId", ignore = true)
    @Mapping(target = "fieldDefId", ignore = true)
    @Mapping(target = "componentType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateFromDTO(FieldComponentUpdateDTO dto, @MappingTarget FieldComponent domain);

    List<FieldComponentDTO> toDTOList(List<FieldComponent> domains);
}