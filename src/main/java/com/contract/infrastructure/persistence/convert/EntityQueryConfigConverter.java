package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryConfig;
import com.contract.infrastructure.persistence.entity.QueryConfigEntity;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityQueryConfigConverter {
    QueryConfig toDomain(QueryConfigEntity entity);
    QueryConfigEntity toEntity(QueryConfig domain);
    List<QueryConfig> toDomainList(List<QueryConfigEntity> entities);
}
