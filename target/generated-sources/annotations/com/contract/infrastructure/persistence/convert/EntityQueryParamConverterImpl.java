package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryParam;
import com.contract.infrastructure.persistence.entity.QueryParamEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-08T01:30:18+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class EntityQueryParamConverterImpl implements EntityQueryParamConverter {

    @Override
    public QueryParam toDomain(QueryParamEntity entity) {
        if ( entity == null ) {
            return null;
        }

        QueryParam.QueryParamBuilder queryParam = QueryParam.builder();

        queryParam.id( entity.getId() );
        queryParam.queryConfigId( entity.getQueryConfigId() );
        queryParam.paramName( entity.getParamName() );
        queryParam.paramLabel( entity.getParamLabel() );
        queryParam.bindSource( entity.getBindSource() );
        queryParam.bindPath( entity.getBindPath() );
        queryParam.componentType( entity.getComponentType() );
        queryParam.required( entity.getRequired() );
        queryParam.defaultValue( entity.getDefaultValue() );
        queryParam.sortNo( entity.getSortNo() );
        queryParam.createdAt( entity.getCreatedAt() );
        queryParam.updatedAt( entity.getUpdatedAt() );

        return queryParam.build();
    }

    @Override
    public QueryParamEntity toEntity(QueryParam domain) {
        if ( domain == null ) {
            return null;
        }

        QueryParamEntity.QueryParamEntityBuilder queryParamEntity = QueryParamEntity.builder();

        queryParamEntity.id( domain.getId() );
        queryParamEntity.queryConfigId( domain.getQueryConfigId() );
        queryParamEntity.paramName( domain.getParamName() );
        queryParamEntity.paramLabel( domain.getParamLabel() );
        queryParamEntity.bindSource( domain.getBindSource() );
        queryParamEntity.bindPath( domain.getBindPath() );
        queryParamEntity.componentType( domain.getComponentType() );
        queryParamEntity.required( domain.getRequired() );
        queryParamEntity.defaultValue( domain.getDefaultValue() );
        queryParamEntity.sortNo( domain.getSortNo() );
        queryParamEntity.createdAt( domain.getCreatedAt() );
        queryParamEntity.updatedAt( domain.getUpdatedAt() );

        return queryParamEntity.build();
    }

    @Override
    public List<QueryParam> toDomainList(List<QueryParamEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<QueryParam> list = new ArrayList<QueryParam>( entities.size() );
        for ( QueryParamEntity queryParamEntity : entities ) {
            list.add( toDomain( queryParamEntity ) );
        }

        return list;
    }
}
