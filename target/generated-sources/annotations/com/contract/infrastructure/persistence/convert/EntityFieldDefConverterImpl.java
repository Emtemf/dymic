package com.contract.infrastructure.persistence.convert;

import com.contract.adapter.persistence.entity.FieldDefEntity;
import com.contract.domain.template.FieldDef;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-06T20:31:49+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class EntityFieldDefConverterImpl implements EntityFieldDefConverter {

    @Override
    public FieldDef toDomain(FieldDefEntity entity) {
        if ( entity == null ) {
            return null;
        }

        FieldDef fieldDef = new FieldDef();

        fieldDef.setId( entity.getId() );
        fieldDef.setTemplateId( entity.getTemplateId() );
        fieldDef.setTemplateVersionId( entity.getTemplateVersionId() );
        fieldDef.setFieldCode( entity.getFieldCode() );
        fieldDef.setFieldPath( entity.getFieldPath() );
        fieldDef.setFieldNameCn( entity.getFieldNameCn() );
        fieldDef.setDataType( entity.getDataType() );
        fieldDef.setRequiredDefault( entity.getRequiredDefault() );
        fieldDef.setCreatedAt( entity.getCreatedAt() );
        fieldDef.setUpdatedAt( entity.getUpdatedAt() );

        return fieldDef;
    }

    @Override
    public FieldDefEntity toEntity(FieldDef domain) {
        if ( domain == null ) {
            return null;
        }

        FieldDefEntity fieldDefEntity = new FieldDefEntity();

        fieldDefEntity.setId( domain.getId() );
        fieldDefEntity.setTemplateId( domain.getTemplateId() );
        fieldDefEntity.setTemplateVersionId( domain.getTemplateVersionId() );
        fieldDefEntity.setFieldCode( domain.getFieldCode() );
        fieldDefEntity.setFieldPath( domain.getFieldPath() );
        fieldDefEntity.setFieldNameCn( domain.getFieldNameCn() );
        fieldDefEntity.setDataType( domain.getDataType() );
        fieldDefEntity.setRequiredDefault( domain.getRequiredDefault() );
        fieldDefEntity.setCreatedAt( domain.getCreatedAt() );
        fieldDefEntity.setUpdatedAt( domain.getUpdatedAt() );

        return fieldDefEntity;
    }

    @Override
    public List<FieldDef> toDomainList(List<FieldDefEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<FieldDef> list = new ArrayList<FieldDef>( entities.size() );
        for ( FieldDefEntity fieldDefEntity : entities ) {
            list.add( toDomain( fieldDefEntity ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDomain(FieldDef domain, FieldDefEntity entity) {
        if ( domain == null ) {
            return;
        }

        if ( domain.getTemplateId() != null ) {
            entity.setTemplateId( domain.getTemplateId() );
        }
        if ( domain.getTemplateVersionId() != null ) {
            entity.setTemplateVersionId( domain.getTemplateVersionId() );
        }
        if ( domain.getFieldCode() != null ) {
            entity.setFieldCode( domain.getFieldCode() );
        }
        if ( domain.getFieldPath() != null ) {
            entity.setFieldPath( domain.getFieldPath() );
        }
        if ( domain.getFieldNameCn() != null ) {
            entity.setFieldNameCn( domain.getFieldNameCn() );
        }
        if ( domain.getDataType() != null ) {
            entity.setDataType( domain.getDataType() );
        }
        if ( domain.getRequiredDefault() != null ) {
            entity.setRequiredDefault( domain.getRequiredDefault() );
        }
        if ( domain.getUpdatedAt() != null ) {
            entity.setUpdatedAt( domain.getUpdatedAt() );
        }
    }
}
