package com.contract.application.template.convert;

import com.contract.application.template.dto.QueryFillRuleDTO;
import com.contract.domain.template.QueryFillRule;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface QueryFillRuleConverter {
    QueryFillRuleDTO toDTO(QueryFillRule domain);
    QueryFillRule toDomain(QueryFillRuleDTO dto);
    List<QueryFillRuleDTO> toDTOList(List<QueryFillRule> domains);
}
