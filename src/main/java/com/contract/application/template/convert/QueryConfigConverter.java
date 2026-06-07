package com.contract.application.template.convert;

import com.contract.application.template.dto.QueryConfigDTO;
import com.contract.application.template.dto.QueryConfigCreateRequest;
import com.contract.application.template.dto.QueryConfigUpdateRequest;
import com.contract.domain.template.QueryConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring", uses = {QueryParamConverter.class, QueryFillRuleConverter.class})
public interface QueryConfigConverter {
    QueryConfigDTO toDTO(QueryConfig domain);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "queryCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    QueryConfig toDomain(QueryConfigCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "queryCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromDTO(QueryConfigUpdateRequest request, @MappingTarget QueryConfig domain);

    List<QueryConfigDTO> toDTOList(List<QueryConfig> domains);
}
