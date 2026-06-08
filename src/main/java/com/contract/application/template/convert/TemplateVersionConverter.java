package com.contract.application.template.convert;

import com.contract.application.template.dto.TemplateVersionDTO;
import com.contract.domain.template.TemplateVersion;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TemplateVersion Domain <-> DTO 转换器
 */
@Mapper(componentModel = "spring")
public interface TemplateVersionConverter {

    /**
     * Domain -> DTO
     * versionStatus: enum -> String
     */
    default TemplateVersionDTO toDTO(TemplateVersion version) {
        if (version == null) {
            return null;
        }
        return TemplateVersionDTO.builder()
            .id(version.getId())
            .templateId(version.getTemplateId())
            .versionNo(version.getVersionNo())
            .versionName(version.getVersionName())
            .versionStatus(version.getVersionStatus() != null ? version.getVersionStatus().name() : null)
            .publishTime(version.getPublishTime())
            .publishBy(version.getPublishBy())
            .schemaHash(version.getSchemaHash())
            .remark(version.getRemark())
            .createdBy(version.getCreatedBy())
            .createdName(version.getCreatedName())
            .createdAt(version.getCreatedAt())
            .updatedBy(version.getUpdatedBy())
            .updatedName(version.getUpdatedName())
            .updatedAt(version.getUpdatedAt())
            .build();
    }

    /**
     * Domain List -> DTO List
     */
    default List<TemplateVersionDTO> toDTOList(List<TemplateVersion> domains) {
        if (domains == null) {
            return null;
        }
        return domains.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}
