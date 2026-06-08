package com.contract.application.template.convert;

import com.contract.application.template.dto.FieldDefDTO;
import com.contract.domain.template.FieldDef;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T22:52:41+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class FieldDefConverterImpl implements FieldDefConverter {

    @Override
    public FieldDefDTO toDTO(FieldDef domain) {
        if ( domain == null ) {
            return null;
        }

        FieldDefDTO fieldDefDTO = new FieldDefDTO();

        fieldDefDTO.setId( domain.getId() );
        fieldDefDTO.setTemplateId( domain.getTemplateId() );
        fieldDefDTO.setTemplateVersionId( domain.getTemplateVersionId() );
        fieldDefDTO.setFieldCode( domain.getFieldCode() );
        fieldDefDTO.setFieldPath( domain.getFieldPath() );
        fieldDefDTO.setFieldNameCn( domain.getFieldNameCn() );
        fieldDefDTO.setDataType( domain.getDataType() );
        fieldDefDTO.setRequiredDefault( domain.getRequiredDefault() );

        return fieldDefDTO;
    }

    @Override
    public FieldDef toDomain(FieldDefDTO dto) {
        if ( dto == null ) {
            return null;
        }

        FieldDef.FieldDefBuilder fieldDef = FieldDef.builder();

        fieldDef.id( dto.getId() );
        fieldDef.templateId( dto.getTemplateId() );
        fieldDef.templateVersionId( dto.getTemplateVersionId() );
        fieldDef.fieldCode( dto.getFieldCode() );
        fieldDef.fieldPath( dto.getFieldPath() );
        fieldDef.fieldNameCn( dto.getFieldNameCn() );
        fieldDef.dataType( dto.getDataType() );
        fieldDef.requiredDefault( dto.getRequiredDefault() );

        return fieldDef.build();
    }

    @Override
    public void updateDomainFromDTO(FieldDefDTO dto, FieldDef domain) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            domain.setId( dto.getId() );
        }
        if ( dto.getTemplateId() != null ) {
            domain.setTemplateId( dto.getTemplateId() );
        }
        if ( dto.getTemplateVersionId() != null ) {
            domain.setTemplateVersionId( dto.getTemplateVersionId() );
        }
        if ( dto.getFieldCode() != null ) {
            domain.setFieldCode( dto.getFieldCode() );
        }
        if ( dto.getFieldPath() != null ) {
            domain.setFieldPath( dto.getFieldPath() );
        }
        if ( dto.getFieldNameCn() != null ) {
            domain.setFieldNameCn( dto.getFieldNameCn() );
        }
        if ( dto.getDataType() != null ) {
            domain.setDataType( dto.getDataType() );
        }
        if ( dto.getRequiredDefault() != null ) {
            domain.setRequiredDefault( dto.getRequiredDefault() );
        }
    }

    @Override
    public List<FieldDefDTO> toDTOList(List<FieldDef> domains) {
        if ( domains == null ) {
            return null;
        }

        List<FieldDefDTO> list = new ArrayList<FieldDefDTO>( domains.size() );
        for ( FieldDef fieldDef : domains ) {
            list.add( toDTO( fieldDef ) );
        }

        return list;
    }
}
