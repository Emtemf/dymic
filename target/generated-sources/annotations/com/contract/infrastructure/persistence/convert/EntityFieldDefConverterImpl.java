package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.FieldDef;
import com.contract.infrastructure.persistence.entity.FieldDefEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T22:49:23+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class EntityFieldDefConverterImpl implements EntityFieldDefConverter {

    @Override
    public FieldDef toDomain(FieldDefEntity entity) {
        if ( entity == null ) {
            return null;
        }

        FieldDef.FieldDefBuilder fieldDef = FieldDef.builder();

        fieldDef.id( entity.getId() );
        fieldDef.templateId( entity.getTemplateId() );
        fieldDef.templateVersionId( entity.getTemplateVersionId() );
        fieldDef.detailTableId( entity.getDetailTableId() );
        fieldDef.fieldCode( entity.getFieldCode() );
        fieldDef.fieldPath( entity.getFieldPath() );
        fieldDef.fieldNameCn( entity.getFieldNameCn() );
        fieldDef.fieldNameEn( entity.getFieldNameEn() );
        fieldDef.dataType( entity.getDataType() );
        fieldDef.valueType( entity.getValueType() );
        fieldDef.requiredDefault( entity.getRequiredDefault() );
        fieldDef.searchable( entity.getSearchable() );
        fieldDef.indexable( entity.getIndexable() );
        fieldDef.searchIndexColumn( entity.getSearchIndexColumn() );
        fieldDef.defaultValue( entity.getDefaultValue() );
        fieldDef.validateRule( entity.getValidateRule() );
        fieldDef.propsJson( entity.getPropsJson() );
        fieldDef.createdAt( entity.getCreatedAt() );
        fieldDef.updatedAt( entity.getUpdatedAt() );
        fieldDef.isDeleted( entity.getIsDeleted() );

        return fieldDef.build();
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
        fieldDefEntity.setDetailTableId( domain.getDetailTableId() );
        fieldDefEntity.setFieldCode( domain.getFieldCode() );
        fieldDefEntity.setFieldPath( domain.getFieldPath() );
        fieldDefEntity.setFieldNameCn( domain.getFieldNameCn() );
        fieldDefEntity.setFieldNameEn( domain.getFieldNameEn() );
        fieldDefEntity.setDataType( domain.getDataType() );
        fieldDefEntity.setValueType( domain.getValueType() );
        fieldDefEntity.setRequiredDefault( domain.getRequiredDefault() );
        fieldDefEntity.setSearchable( domain.getSearchable() );
        fieldDefEntity.setIndexable( domain.getIndexable() );
        fieldDefEntity.setSearchIndexColumn( domain.getSearchIndexColumn() );
        fieldDefEntity.setDefaultValue( domain.getDefaultValue() );
        fieldDefEntity.setValidateRule( domain.getValidateRule() );
        fieldDefEntity.setPropsJson( domain.getPropsJson() );
        fieldDefEntity.setCreatedAt( domain.getCreatedAt() );
        fieldDefEntity.setUpdatedAt( domain.getUpdatedAt() );
        fieldDefEntity.setIsDeleted( domain.getIsDeleted() );

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
        if ( domain.getDetailTableId() != null ) {
            entity.setDetailTableId( domain.getDetailTableId() );
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
        if ( domain.getFieldNameEn() != null ) {
            entity.setFieldNameEn( domain.getFieldNameEn() );
        }
        if ( domain.getDataType() != null ) {
            entity.setDataType( domain.getDataType() );
        }
        if ( domain.getValueType() != null ) {
            entity.setValueType( domain.getValueType() );
        }
        if ( domain.getRequiredDefault() != null ) {
            entity.setRequiredDefault( domain.getRequiredDefault() );
        }
        if ( domain.getSearchable() != null ) {
            entity.setSearchable( domain.getSearchable() );
        }
        if ( domain.getIndexable() != null ) {
            entity.setIndexable( domain.getIndexable() );
        }
        if ( domain.getSearchIndexColumn() != null ) {
            entity.setSearchIndexColumn( domain.getSearchIndexColumn() );
        }
        if ( domain.getDefaultValue() != null ) {
            entity.setDefaultValue( domain.getDefaultValue() );
        }
        if ( domain.getValidateRule() != null ) {
            entity.setValidateRule( domain.getValidateRule() );
        }
        if ( domain.getPropsJson() != null ) {
            entity.setPropsJson( domain.getPropsJson() );
        }
        if ( domain.getUpdatedAt() != null ) {
            entity.setUpdatedAt( domain.getUpdatedAt() );
        }
        if ( domain.getIsDeleted() != null ) {
            entity.setIsDeleted( domain.getIsDeleted() );
        }
    }
}
