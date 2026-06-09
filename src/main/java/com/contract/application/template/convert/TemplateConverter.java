package com.contract.application.template.convert;

import com.contract.application.template.dto.TemplateDTO;
import com.contract.domain.template.Template;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Template Domain <-> DTO 转换器
 */
@Mapper(componentModel = "spring")
public interface TemplateConverter {

    /**
     * Domain -> DTO
     * Status: enum -> String
     */
    default TemplateDTO toDTO(Template template) {
        if (template == null) {
            return null;
        }
        return TemplateDTO.builder()
            .id(template.getIdValue())
            .templateCode(template.getTemplateCodeValue())
            .templateName(template.getTemplateNameValue())
            .templateDesc(template.getTemplateDescValue())
            .bizType(template.getBizTypeValue())
            .status(template.getStatus() != null ? template.getStatus().name() : null)
            .currentVersionId(template.getCurrentVersionId())
            .createdBy(template.getCreatedBy())
            .createdName(template.getCreatedName())
            .createdAt(template.getCreatedAt())
            .updatedBy(template.getUpdatedBy())
            .updatedName(template.getUpdatedName())
            .updatedAt(template.getUpdatedAt())
            .build();
    }

    /**
     * Domain List -> DTO List
     */
    default List<TemplateDTO> toDTOList(List<Template> domains) {
        if (domains == null) {
            return null;
        }
        return domains.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}
