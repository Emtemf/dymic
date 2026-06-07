package com.contract.application.template.convert;

import com.contract.application.template.dto.QueryConfigCreateRequest;
import com.contract.application.template.dto.QueryConfigDTO;
import com.contract.application.template.dto.QueryConfigUpdateRequest;
import com.contract.domain.template.QueryConfig;
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
public class QueryConfigConverterImpl implements QueryConfigConverter {

    @Override
    public QueryConfigDTO toDTO(QueryConfig domain) {
        if ( domain == null ) {
            return null;
        }

        QueryConfigDTO.QueryConfigDTOBuilder queryConfigDTO = QueryConfigDTO.builder();

        queryConfigDTO.id( domain.getId() );
        queryConfigDTO.templateId( domain.getTemplateId() );
        queryConfigDTO.templateVersionId( domain.getTemplateVersionId() );
        queryConfigDTO.queryCode( domain.getQueryCode() );
        queryConfigDTO.queryName( domain.getQueryName() );
        queryConfigDTO.queryType( domain.getQueryType() );
        queryConfigDTO.dataProviderId( domain.getDataProviderId() );
        queryConfigDTO.triggerType( domain.getTriggerType() );
        queryConfigDTO.resultMode( domain.getResultMode() );
        queryConfigDTO.bindNodeId( domain.getBindNodeId() );
        queryConfigDTO.pageSize( domain.getPageSize() );
        queryConfigDTO.propsJson( domain.getPropsJson() );

        return queryConfigDTO.build();
    }

    @Override
    public QueryConfig toDomain(QueryConfigCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        QueryConfig.QueryConfigBuilder queryConfig = QueryConfig.builder();

        queryConfig.queryName( request.getQueryName() );
        queryConfig.queryType( request.getQueryType() );
        queryConfig.dataProviderId( request.getDataProviderId() );
        queryConfig.triggerType( request.getTriggerType() );
        queryConfig.resultMode( request.getResultMode() );
        queryConfig.bindNodeId( request.getBindNodeId() );
        queryConfig.pageSize( request.getPageSize() );
        queryConfig.propsJson( request.getPropsJson() );

        return queryConfig.build();
    }

    @Override
    public void updateFromDTO(QueryConfigUpdateRequest request, QueryConfig domain) {
        if ( request == null ) {
            return;
        }

        if ( request.getQueryName() != null ) {
            domain.setQueryName( request.getQueryName() );
        }
        if ( request.getQueryType() != null ) {
            domain.setQueryType( request.getQueryType() );
        }
        if ( request.getDataProviderId() != null ) {
            domain.setDataProviderId( request.getDataProviderId() );
        }
        if ( request.getTriggerType() != null ) {
            domain.setTriggerType( request.getTriggerType() );
        }
        if ( request.getResultMode() != null ) {
            domain.setResultMode( request.getResultMode() );
        }
        if ( request.getBindNodeId() != null ) {
            domain.setBindNodeId( request.getBindNodeId() );
        }
        if ( request.getPageSize() != null ) {
            domain.setPageSize( request.getPageSize() );
        }
        if ( request.getPropsJson() != null ) {
            domain.setPropsJson( request.getPropsJson() );
        }
    }

    @Override
    public List<QueryConfigDTO> toDTOList(List<QueryConfig> domains) {
        if ( domains == null ) {
            return null;
        }

        List<QueryConfigDTO> list = new ArrayList<QueryConfigDTO>( domains.size() );
        for ( QueryConfig queryConfig : domains ) {
            list.add( toDTO( queryConfig ) );
        }

        return list;
    }
}
