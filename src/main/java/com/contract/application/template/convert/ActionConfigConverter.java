package com.contract.application.template.convert;

import com.contract.application.template.dto.ActionConfigDTO;
import com.contract.application.template.dto.ActionConfigCreateRequest;
import com.contract.application.template.dto.ActionConfigUpdateRequest;
import com.contract.domain.template.ActionConfig;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import java.util.List;

/**
 * ActionConfig Domain <-> DTO 转换器
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActionConfigConverter {

    /**
     * Domain -> DTO
     */
    ActionConfigDTO toDTO(ActionConfig domain);

    /**
     * Domain List -> DTO List
     */
    List<ActionConfigDTO> toDTOList(List<ActionConfig> domains);

    /**
     * CreateRequest -> Domain
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "actionCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    ActionConfig toDomain(ActionConfigCreateRequest request);

    /**
     * 更新 Domain（部分更新）
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "actionCode", ignore = true)
    @Mapping(target = "actionType", ignore = true)
    @Mapping(target = "bindNodeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateFromDTO(ActionConfigUpdateRequest request, @MappingTarget ActionConfig domain);
}