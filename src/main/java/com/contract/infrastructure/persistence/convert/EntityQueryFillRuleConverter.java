package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.QueryFillRule;
import com.contract.infrastructure.persistence.entity.QueryFillRuleEntity;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityQueryFillRuleConverter {
    QueryFillRule toDomain(QueryFillRuleEntity entity);
    QueryFillRuleEntity toEntity(QueryFillRule domain);
    List<QueryFillRule> toDomainList(List<QueryFillRuleEntity> entities);
}
