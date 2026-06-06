package com.contract.application.template.convert;

import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.domain.template.FieldComponent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-06T23:27:54+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class FieldComponentConverterImpl implements FieldComponentConverter {

    @Override
    public FieldComponentDTO toDTO(FieldComponent domain) {
        if ( domain == null ) {
            return null;
        }

        FieldComponentDTO fieldComponentDTO = new FieldComponentDTO();

        fieldComponentDTO.setId( domain.getId() );
        fieldComponentDTO.setTemplateId( domain.getTemplateId() );
        fieldComponentDTO.setTemplateVersionId( domain.getTemplateVersionId() );
        fieldComponentDTO.setFieldDefId( domain.getFieldDefId() );
        fieldComponentDTO.setLayoutNodeId( domain.getLayoutNodeId() );
        fieldComponentDTO.setComponentType( domain.getComponentType() );
        fieldComponentDTO.setLabelName( domain.getLabelName() );
        fieldComponentDTO.setPlaceholder( domain.getPlaceholder() );
        fieldComponentDTO.setSortNo( domain.getSortNo() );
        fieldComponentDTO.setComponentProps( domain.getComponentProps() );
        fieldComponentDTO.setDataProviderId( domain.getDataProviderId() );
        fieldComponentDTO.setRequiredRule( domain.getRequiredRule() );
        fieldComponentDTO.setVisibleRule( domain.getVisibleRule() );
        fieldComponentDTO.setReadonlyRule( domain.getReadonlyRule() );

        return fieldComponentDTO;
    }

    @Override
    public FieldComponent toDomain(FieldComponentDTO dto) {
        if ( dto == null ) {
            return null;
        }

        FieldComponent fieldComponent = new FieldComponent();

        fieldComponent.setId( dto.getId() );
        fieldComponent.setTemplateId( dto.getTemplateId() );
        fieldComponent.setTemplateVersionId( dto.getTemplateVersionId() );
        fieldComponent.setFieldDefId( dto.getFieldDefId() );
        fieldComponent.setLayoutNodeId( dto.getLayoutNodeId() );
        fieldComponent.setComponentType( dto.getComponentType() );
        fieldComponent.setLabelName( dto.getLabelName() );
        fieldComponent.setPlaceholder( dto.getPlaceholder() );
        fieldComponent.setSortNo( dto.getSortNo() );
        fieldComponent.setComponentProps( dto.getComponentProps() );
        fieldComponent.setDataProviderId( dto.getDataProviderId() );
        fieldComponent.setRequiredRule( dto.getRequiredRule() );
        fieldComponent.setVisibleRule( dto.getVisibleRule() );
        fieldComponent.setReadonlyRule( dto.getReadonlyRule() );

        return fieldComponent;
    }

    @Override
    public void updateDomainFromDTO(FieldComponentDTO dto, FieldComponent domain) {
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
        if ( dto.getFieldDefId() != null ) {
            domain.setFieldDefId( dto.getFieldDefId() );
        }
        if ( dto.getLayoutNodeId() != null ) {
            domain.setLayoutNodeId( dto.getLayoutNodeId() );
        }
        if ( dto.getComponentType() != null ) {
            domain.setComponentType( dto.getComponentType() );
        }
        if ( dto.getLabelName() != null ) {
            domain.setLabelName( dto.getLabelName() );
        }
        if ( dto.getPlaceholder() != null ) {
            domain.setPlaceholder( dto.getPlaceholder() );
        }
        if ( dto.getSortNo() != null ) {
            domain.setSortNo( dto.getSortNo() );
        }
        if ( dto.getComponentProps() != null ) {
            domain.setComponentProps( dto.getComponentProps() );
        }
        if ( dto.getDataProviderId() != null ) {
            domain.setDataProviderId( dto.getDataProviderId() );
        }
        if ( dto.getRequiredRule() != null ) {
            domain.setRequiredRule( dto.getRequiredRule() );
        }
        if ( dto.getVisibleRule() != null ) {
            domain.setVisibleRule( dto.getVisibleRule() );
        }
        if ( dto.getReadonlyRule() != null ) {
            domain.setReadonlyRule( dto.getReadonlyRule() );
        }
    }

    @Override
    public List<FieldComponentDTO> toDTOList(List<FieldComponent> domains) {
        if ( domains == null ) {
            return null;
        }

        List<FieldComponentDTO> list = new ArrayList<FieldComponentDTO>( domains.size() );
        for ( FieldComponent fieldComponent : domains ) {
            list.add( toDTO( fieldComponent ) );
        }

        return list;
    }
}
