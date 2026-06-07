package com.contract.application.template.convert;

import com.contract.application.template.dto.FieldComponentCreateRequest;
import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.FieldComponentUpdateDTO;
import com.contract.domain.template.FieldComponent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T02:17:51+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class FieldComponentConverterImpl implements FieldComponentConverter {

    @Override
    public FieldComponentDTO toDTO(FieldComponent domain) {
        if ( domain == null ) {
            return null;
        }

        FieldComponentDTO.FieldComponentDTOBuilder fieldComponentDTO = FieldComponentDTO.builder();

        fieldComponentDTO.id( domain.getId() );
        fieldComponentDTO.templateId( domain.getTemplateId() );
        fieldComponentDTO.templateVersionId( domain.getTemplateVersionId() );
        fieldComponentDTO.fieldDefId( domain.getFieldDefId() );
        fieldComponentDTO.layoutNodeId( domain.getLayoutNodeId() );
        fieldComponentDTO.componentType( domain.getComponentType() );
        fieldComponentDTO.labelName( domain.getLabelName() );
        fieldComponentDTO.placeholder( domain.getPlaceholder() );
        fieldComponentDTO.sortNo( domain.getSortNo() );
        fieldComponentDTO.requiredRule( domain.getRequiredRule() );
        fieldComponentDTO.readonlyRule( domain.getReadonlyRule() );
        fieldComponentDTO.visibleRule( domain.getVisibleRule() );
        fieldComponentDTO.componentProps( domain.getComponentProps() );
        fieldComponentDTO.dataProviderId( domain.getDataProviderId() );
        fieldComponentDTO.createdAt( domain.getCreatedAt() );
        fieldComponentDTO.updatedAt( domain.getUpdatedAt() );

        return fieldComponentDTO.build();
    }

    @Override
    public FieldComponent toDomain(FieldComponentDTO dto) {
        if ( dto == null ) {
            return null;
        }

        FieldComponent.FieldComponentBuilder fieldComponent = FieldComponent.builder();

        fieldComponent.id( dto.getId() );
        fieldComponent.templateId( dto.getTemplateId() );
        fieldComponent.templateVersionId( dto.getTemplateVersionId() );
        fieldComponent.layoutNodeId( dto.getLayoutNodeId() );
        fieldComponent.fieldDefId( dto.getFieldDefId() );
        fieldComponent.componentType( dto.getComponentType() );
        fieldComponent.labelName( dto.getLabelName() );
        fieldComponent.placeholder( dto.getPlaceholder() );
        fieldComponent.sortNo( dto.getSortNo() );
        fieldComponent.requiredRule( dto.getRequiredRule() );
        fieldComponent.readonlyRule( dto.getReadonlyRule() );
        fieldComponent.visibleRule( dto.getVisibleRule() );
        fieldComponent.componentProps( dto.getComponentProps() );
        fieldComponent.dataProviderId( dto.getDataProviderId() );
        fieldComponent.createdAt( dto.getCreatedAt() );
        fieldComponent.updatedAt( dto.getUpdatedAt() );

        return fieldComponent.build();
    }

    @Override
    public FieldComponent toDomain(FieldComponentCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        FieldComponent.FieldComponentBuilder fieldComponent = FieldComponent.builder();

        fieldComponent.layoutNodeId( request.getLayoutNodeId() );
        fieldComponent.fieldDefId( request.getFieldDefId() );
        fieldComponent.componentType( request.getComponentType() );
        fieldComponent.labelName( request.getLabelName() );
        fieldComponent.placeholder( request.getPlaceholder() );
        fieldComponent.sortNo( request.getSortNo() );
        fieldComponent.requiredRule( request.getRequiredRule() );
        fieldComponent.readonlyRule( request.getReadonlyRule() );
        fieldComponent.visibleRule( request.getVisibleRule() );
        fieldComponent.componentProps( request.getComponentProps() );
        fieldComponent.dataProviderId( request.getDataProviderId() );

        return fieldComponent.build();
    }

    @Override
    public void updateFromDTO(FieldComponentUpdateDTO dto, FieldComponent domain) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getLabelName() != null ) {
            domain.setLabelName( dto.getLabelName() );
        }
        if ( dto.getPlaceholder() != null ) {
            domain.setPlaceholder( dto.getPlaceholder() );
        }
        if ( dto.getRequiredRule() != null ) {
            domain.setRequiredRule( dto.getRequiredRule() );
        }
        if ( dto.getComponentProps() != null ) {
            domain.setComponentProps( dto.getComponentProps() );
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
