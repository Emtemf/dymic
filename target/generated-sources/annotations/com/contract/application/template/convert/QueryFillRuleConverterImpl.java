package com.contract.application.template.convert;

import com.contract.application.template.dto.QueryFillRuleDTO;
import com.contract.domain.template.QueryFillRule;
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
public class QueryFillRuleConverterImpl implements QueryFillRuleConverter {

    @Override
    public QueryFillRuleDTO toDTO(QueryFillRule domain) {
        if ( domain == null ) {
            return null;
        }

        QueryFillRuleDTO.QueryFillRuleDTOBuilder queryFillRuleDTO = QueryFillRuleDTO.builder();

        queryFillRuleDTO.id( domain.getId() );
        queryFillRuleDTO.queryConfigId( domain.getQueryConfigId() );
        queryFillRuleDTO.sourceField( domain.getSourceField() );
        queryFillRuleDTO.targetScope( domain.getTargetScope() );
        queryFillRuleDTO.targetPath( domain.getTargetPath() );
        queryFillRuleDTO.fillMode( domain.getFillMode() );
        queryFillRuleDTO.transformJson( domain.getTransformJson() );
        queryFillRuleDTO.sortNo( domain.getSortNo() );

        return queryFillRuleDTO.build();
    }

    @Override
    public QueryFillRule toDomain(QueryFillRuleDTO dto) {
        if ( dto == null ) {
            return null;
        }

        QueryFillRule.QueryFillRuleBuilder queryFillRule = QueryFillRule.builder();

        queryFillRule.id( dto.getId() );
        queryFillRule.queryConfigId( dto.getQueryConfigId() );
        queryFillRule.sourceField( dto.getSourceField() );
        queryFillRule.targetScope( dto.getTargetScope() );
        queryFillRule.targetPath( dto.getTargetPath() );
        queryFillRule.fillMode( dto.getFillMode() );
        queryFillRule.transformJson( dto.getTransformJson() );
        queryFillRule.sortNo( dto.getSortNo() );

        return queryFillRule.build();
    }

    @Override
    public List<QueryFillRuleDTO> toDTOList(List<QueryFillRule> domains) {
        if ( domains == null ) {
            return null;
        }

        List<QueryFillRuleDTO> list = new ArrayList<QueryFillRuleDTO>( domains.size() );
        for ( QueryFillRule queryFillRule : domains ) {
            list.add( toDTO( queryFillRule ) );
        }

        return list;
    }
}
