package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.FieldComponent;
import com.contract.infrastructure.persistence.entity.FieldComponentEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T23:02:37+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class EntityFieldComponentConverterImpl implements EntityFieldComponentConverter {

    @Override
    public FieldComponent toDomain(FieldComponentEntity entity) {
        if ( entity == null ) {
            return null;
        }

        FieldComponent.FieldComponentBuilder fieldComponent = FieldComponent.builder();

        fieldComponent.id( entity.getId() );
        fieldComponent.templateId( entity.getTemplateId() );
        fieldComponent.templateVersionId( entity.getTemplateVersionId() );
        fieldComponent.layoutNodeId( entity.getLayoutNodeId() );
        fieldComponent.fieldDefId( entity.getFieldDefId() );
        fieldComponent.componentType( entity.getComponentType() );
        fieldComponent.labelName( entity.getLabelName() );
        fieldComponent.placeholder( entity.getPlaceholder() );
        fieldComponent.sortNo( entity.getSortNo() );
        fieldComponent.requiredRule( entity.getRequiredRule() );
        fieldComponent.readonlyRule( entity.getReadonlyRule() );
        fieldComponent.visibleRule( entity.getVisibleRule() );
        fieldComponent.componentProps( entity.getComponentProps() );
        fieldComponent.dataProviderId( entity.getDataProviderId() );
        fieldComponent.createdAt( entity.getCreatedAt() );
        fieldComponent.updatedAt( entity.getUpdatedAt() );
        fieldComponent.isDeleted( entity.getIsDeleted() );

        return fieldComponent.build();
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
        fieldComponentEntity.setIsDeleted( domain.getIsDeleted() );

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
        if ( domain.getIsDeleted() != null ) {
            entity.setIsDeleted( domain.getIsDeleted() );
        }
    }
}
