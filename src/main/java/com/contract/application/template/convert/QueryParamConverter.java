package com.contract.application.template.convert;

import com.contract.application.template.dto.QueryParamDTO;
import com.contract.domain.template.QueryParam;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface QueryParamConverter {
    QueryParamDTO toDTO(QueryParam domain);
    QueryParam toDomain(QueryParamDTO dto);
    List<QueryParamDTO> toDTOList(List<QueryParam> domains);
}
