package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryFillRule;
import com.contract.infrastructure.persistence.entity.QueryFillRuleEntity;
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
public class EntityQueryFillRuleConverterImpl implements EntityQueryFillRuleConverter {

    @Override
    public QueryFillRule toDomain(QueryFillRuleEntity entity) {
        if ( entity == null ) {
            return null;
        }

        QueryFillRule.QueryFillRuleBuilder queryFillRule = QueryFillRule.builder();

        queryFillRule.id( entity.getId() );
        queryFillRule.queryConfigId( entity.getQueryConfigId() );
        queryFillRule.sourceField( entity.getSourceField() );
        queryFillRule.targetScope( entity.getTargetScope() );
        queryFillRule.targetPath( entity.getTargetPath() );
        queryFillRule.fillMode( entity.getFillMode() );
        queryFillRule.transformJson( entity.getTransformJson() );
        queryFillRule.sortNo( entity.getSortNo() );
        queryFillRule.createdAt( entity.getCreatedAt() );
        queryFillRule.updatedAt( entity.getUpdatedAt() );

        return queryFillRule.build();
    }

    @Override
    public QueryFillRuleEntity toEntity(QueryFillRule domain) {
        if ( domain == null ) {
            return null;
        }

        QueryFillRuleEntity.QueryFillRuleEntityBuilder queryFillRuleEntity = QueryFillRuleEntity.builder();

        queryFillRuleEntity.id( domain.getId() );
        queryFillRuleEntity.queryConfigId( domain.getQueryConfigId() );
        queryFillRuleEntity.sourceField( domain.getSourceField() );
        queryFillRuleEntity.targetScope( domain.getTargetScope() );
        queryFillRuleEntity.targetPath( domain.getTargetPath() );
        queryFillRuleEntity.fillMode( domain.getFillMode() );
        queryFillRuleEntity.transformJson( domain.getTransformJson() );
        queryFillRuleEntity.sortNo( domain.getSortNo() );
        queryFillRuleEntity.createdAt( domain.getCreatedAt() );
        queryFillRuleEntity.updatedAt( domain.getUpdatedAt() );

        return queryFillRuleEntity.build();
    }

    @Override
    public List<QueryFillRule> toDomainList(List<QueryFillRuleEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<QueryFillRule> list = new ArrayList<QueryFillRule>( entities.size() );
        for ( QueryFillRuleEntity queryFillRuleEntity : entities ) {
            list.add( toDomain( queryFillRuleEntity ) );
        }

        return list;
    }
}
