package com.contract.infrastructure.persistence.convert;

import com.contract.adapter.persistence.entity.FieldComponentEntity;
import com.contract.domain.template.FieldComponent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-06T20:35:32+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class EntityFieldComponentConverterImpl implements EntityFieldComponentConverter {

    @Override
    public FieldComponent toDomain(FieldComponentEntity entity) {
        if ( entity == null ) {
            return null;
        }

        FieldComponent fieldComponent = new FieldComponent();

        fieldComponent.setId( entity.getId() );
        fieldComponent.setTemplateId( entity.getTemplateId() );
        fieldComponent.setTemplateVersionId( entity.getTemplateVersionId() );
        fieldComponent.setFieldDefId( entity.getFieldDefId() );
        fieldComponent.setLayoutNodeId( entity.getLayoutNodeId() );
        fieldComponent.setComponentType( entity.getComponentType() );
        fieldComponent.setLabelName( entity.getLabelName() );
        fieldComponent.setPlaceholder( entity.getPlaceholder() );
        fieldComponent.setSortNo( entity.getSortNo() );
        fieldComponent.setComponentProps( entity.getComponentProps() );
        fieldComponent.setDataProviderId( entity.getDataProviderId() );
        fieldComponent.setRequiredRule( entity.getRequiredRule() );
        fieldComponent.setVisibleRule( entity.getVisibleRule() );
        fieldComponent.setReadonlyRule( entity.getReadonlyRule() );
        fieldComponent.setCreatedAt( entity.getCreatedAt() );
        fieldComponent.setUpdatedAt( entity.getUpdatedAt() );

        return fieldComponent;
    }

    @Override
    public FieldComponentEntity toEntity(FieldComponent domain) {
        if ( domain == null ) {
            return null;
        }

        FieldComponentEntity fieldComponentEntity = new FieldComponentEntity();

        fieldComponentEntity.setId( domain.getId() );
        fieldComponentEntity.setTemplateId( domain.getTemplateId() );
        fieldComponentEntity.setTemplateVersionId( domain.getTemplateVersionId() );
        fieldComponentEntity.setLayoutNodeId( domain.getLayoutNodeId() );
        fieldComponentEntity.setFieldDefId( domain.getFieldDefId() );
        fieldComponentEntity.setComponentType( domain.getComponentType() );
        fieldComponentEntity.setLabelName( domain.getLabelName() );
        fieldComponentEntity.setPlaceholder( domain.getPlaceholder() );
        fieldComponentEntity.setRequiredRule( domain.getRequiredRule() );
        fieldComponentEntity.setReadonlyRule( domain.getReadonlyRule() );
        fieldComponentEntity.setVisibleRule( domain.getVisibleRule() );
        fieldComponentEntity.setComponentProps( domain.getComponentProps() );
        fieldComponentEntity.setDataProviderId( domain.getDataProviderId() );
        fieldComponentEntity.setSortNo( domain.getSortNo() );
        fieldComponentEntity.setCreatedAt( domain.getCreatedAt() );
        fieldComponentEntity.setUpdatedAt( domain.getUpdatedAt() );

        return fieldComponentEntity;
    }

    @Override
    public List<FieldComponent> toDomainList(List<FieldComponentEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<FieldComponent> list = new ArrayList<FieldComponent>( entities.size() );
        for ( FieldComponentEntity fieldComponentEntity : entities ) {
            list.add( toDomain( fieldComponentEntity ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDomain(FieldComponent domain, FieldComponentEntity entity) {
        if ( domain == null ) {
            return;
        }

        if ( domain.getTemplateId() != null ) {
            entity.setTemplateId( domain.getTemplateId() );
        }
        if ( domain.getTemplateVersionId() != null ) {
            entity.setTemplateVersionId( domain.getTemplateVersionId() );
        }
        if ( domain.getLayoutNodeId() != null ) {
            entity.setLayoutNodeId( domain.getLayoutNodeId() );
        }
        if ( domain.getFieldDefId() != null ) {
            entity.setFieldDefId( domain.getFieldDefId() );
        }
        if ( domain.getComponentType() != null ) {
            entity.setComponentType( domain.getComponentType() );
        }
        if ( domain.getLabelName() != null ) {
            entity.setLabelName( domain.getLabelName() );
        }
        if ( domain.getPlaceholder() != null ) {
            entity.setPlaceholder( domain.getPlaceholder() );
        }
        if ( domain.getRequiredRule() != null ) {
            entity.setRequiredRule( domain.getRequiredRule() );
        }
        if ( domain.getReadonlyRule() != null ) {
            entity.setReadonlyRule( domain.getReadonlyRule() );
        }
        if ( domain.getVisibleRule() != null ) {
            entity.setVisibleRule( domain.getVisibleRule() );
        }
        if ( domain.getComponentProps() != null ) {
            entity.setComponentProps( domain.getComponentProps() );
        }
        if ( domain.getDataProviderId() != null ) {
            entity.setDataProviderId( domain.getDataProviderId() );
        }
        if ( domain.getSortNo() != null ) {
            entity.setSortNo( domain.getSortNo() );
        }
        if ( domain.getUpdatedAt() != null ) {
            entity.setUpdatedAt( domain.getUpdatedAt() );
        }
    }
}
