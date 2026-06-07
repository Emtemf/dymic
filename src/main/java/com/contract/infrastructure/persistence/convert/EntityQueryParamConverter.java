package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryParam;
import com.contract.infrastructure.persistence.entity.QueryParamEntity;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityQueryParamConverter {
    QueryParam toDomain(QueryParamEntity entity);
    QueryParamEntity toEntity(QueryParam domain);
    List<QueryParam> toDomainList(List<QueryParamEntity> entities);
}
