package com.contract.application.template.convert;

import com.contract.application.template.dto.QueryParamDTO;
import com.contract.domain.template.QueryParam;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T21:33:07+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class QueryParamConverterImpl implements QueryParamConverter {

    @Override
    public QueryParamDTO toDTO(QueryParam domain) {
        if ( domain == null ) {
            return null;
        }

        QueryParamDTO.QueryParamDTOBuilder queryParamDTO = QueryParamDTO.builder();

        queryParamDTO.id( domain.getId() );
        queryParamDTO.queryConfigId( domain.getQueryConfigId() );
        queryParamDTO.paramName( domain.getParamName() );
        queryParamDTO.paramLabel( domain.getParamLabel() );
        queryParamDTO.bindSource( domain.getBindSource() );
        queryParamDTO.bindPath( domain.getBindPath() );
        queryParamDTO.componentType( domain.getComponentType() );
        queryParamDTO.required( domain.getRequired() );
        queryParamDTO.defaultValue( domain.getDefaultValue() );
        queryParamDTO.sortNo( domain.getSortNo() );

        return queryParamDTO.build();
    }

    @Override
    public QueryParam toDomain(QueryParamDTO dto) {
        if ( dto == null ) {
            return null;
        }

        QueryParam.QueryParamBuilder queryParam = QueryParam.builder();

        queryParam.id( dto.getId() );
        queryParam.queryConfigId( dto.getQueryConfigId() );
        queryParam.paramName( dto.getParamName() );
        queryParam.paramLabel( dto.getParamLabel() );
        queryParam.bindSource( dto.getBindSource() );
        queryParam.bindPath( dto.getBindPath() );
        queryParam.componentType( dto.getComponentType() );
        queryParam.required( dto.getRequired() );
        queryParam.defaultValue( dto.getDefaultValue() );
        queryParam.sortNo( dto.getSortNo() );

        return queryParam.build();
    }

    @Override
    public List<QueryParamDTO> toDTOList(List<QueryParam> domains) {
        if ( domains == null ) {
            return null;
        }

        List<QueryParamDTO> list = new ArrayList<QueryParamDTO>( domains.size() );
        for ( QueryParam queryParam : domains ) {
            list.add( toDTO( queryParam ) );
        }

        return list;
    }
}
