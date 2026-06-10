package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryConfig;
import com.contract.infrastructure.persistence.entity.QueryConfigEntity;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityQueryConfigConverter {
    QueryConfig toDomain(QueryConfigEntity entity);
    QueryConfigEntity toEntity(QueryConfig domain);
    List<QueryConfig> toDomainList(List<QueryConfigEntity> entities);

    default LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
