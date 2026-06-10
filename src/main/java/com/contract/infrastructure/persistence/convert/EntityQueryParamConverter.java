package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryParam;
import com.contract.infrastructure.persistence.entity.QueryParamEntity;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityQueryParamConverter {
    QueryParam toDomain(QueryParamEntity entity);
    QueryParamEntity toEntity(QueryParam domain);
    List<QueryParam> toDomainList(List<QueryParamEntity> entities);

    default LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
