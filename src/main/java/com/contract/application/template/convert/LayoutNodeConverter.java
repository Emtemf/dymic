package com.contract.application.template.convert;

import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.LayoutNodeCreateDTO;
import com.contract.application.template.dto.LayoutNodeUpdateDTO;
import com.contract.domain.template.LayoutNode;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

/**
 * LayoutNode Domain ↔ DTO 转换器
 */
@Mapper(componentModel = "spring")
public interface LayoutNodeConverter {
    /**
     * Domain → DTO
     */
    LayoutNodeDTO toDTO(LayoutNode domain);

    /**
     * Domain List → DTO List
     */
    List<LayoutNodeDTO> toDTOList(List<LayoutNode> domains);

    /**
     * CreateDTO → Domain
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "nodeCode", ignore = true)
    @Mapping(target = "nodePath", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LayoutNode toDomain(LayoutNodeCreateDTO dto);

    /**
     * 更新 Domain（部分更新）
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "nodeCode", ignore = true)
    @Mapping(target = "nodeType", ignore = true)
    @Mapping(target = "nodePath", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateDomainFromDTO(LayoutNodeUpdateDTO dto, @MappingTarget LayoutNode domain);
}