package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryConfig;
import com.contract.infrastructure.persistence.entity.QueryConfigEntity;
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
public class EntityQueryConfigConverterImpl implements EntityQueryConfigConverter {

    @Override
    public QueryConfig toDomain(QueryConfigEntity entity) {
        if ( entity == null ) {
            return null;
        }

        QueryConfig.QueryConfigBuilder queryConfig = QueryConfig.builder();

        queryConfig.id( entity.getId() );
        queryConfig.templateId( entity.getTemplateId() );
        queryConfig.templateVersionId( entity.getTemplateVersionId() );
        queryConfig.queryCode( entity.getQueryCode() );
        queryConfig.queryName( entity.getQueryName() );
        queryConfig.queryType( entity.getQueryType() );
        queryConfig.dataProviderId( entity.getDataProviderId() );
        queryConfig.triggerType( entity.getTriggerType() );
        queryConfig.resultMode( entity.getResultMode() );
        queryConfig.bindNodeId( entity.getBindNodeId() );
        queryConfig.pageSize( entity.getPageSize() );
        queryConfig.propsJson( entity.getPropsJson() );
        queryConfig.createdAt( entity.getCreatedAt() );
        queryConfig.updatedAt( entity.getUpdatedAt() );

        return queryConfig.build();
    }

    @Override
    public QueryConfigEntity toEntity(QueryConfig domain) {
        if ( domain == null ) {
            return null;
        }

        QueryConfigEntity.QueryConfigEntityBuilder queryConfigEntity = QueryConfigEntity.builder();

        queryConfigEntity.id( domain.getId() );
        queryConfigEntity.templateId( domain.getTemplateId() );
        queryConfigEntity.templateVersionId( domain.getTemplateVersionId() );
        queryConfigEntity.queryCode( domain.getQueryCode() );
        queryConfigEntity.queryName( domain.getQueryName() );
        queryConfigEntity.queryType( domain.getQueryType() );
        queryConfigEntity.dataProviderId( domain.getDataProviderId() );
        queryConfigEntity.triggerType( domain.getTriggerType() );
        queryConfigEntity.resultMode( domain.getResultMode() );
        queryConfigEntity.bindNodeId( domain.getBindNodeId() );
        queryConfigEntity.pageSize( domain.getPageSize() );
        queryConfigEntity.propsJson( domain.getPropsJson() );
        queryConfigEntity.createdAt( domain.getCreatedAt() );
        queryConfigEntity.updatedAt( domain.getUpdatedAt() );

        return queryConfigEntity.build();
    }

    @Override
    public List<QueryConfig> toDomainList(List<QueryConfigEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<QueryConfig> list = new ArrayList<QueryConfig>( entities.size() );
        for ( QueryConfigEntity queryConfigEntity : entities ) {
            list.add( toDomain( queryConfigEntity ) );
        }

        return list;
    }
}
