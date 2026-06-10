package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryFillRule;
import com.contract.infrastructure.persistence.entity.QueryFillRuleEntity;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityQueryFillRuleConverter {
    QueryFillRule toDomain(QueryFillRuleEntity entity);
    QueryFillRuleEntity toEntity(QueryFillRule domain);
    List<QueryFillRule> toDomainList(List<QueryFillRuleEntity> entities);

    default LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
