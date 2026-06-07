package com.contract.infrastructure.persistence.repository;

import com.contract.domain.template.QueryFillRule;
import com.contract.domain.template.repository.QueryFillRuleRepository;
import com.contract.infrastructure.persistence.convert.EntityQueryFillRuleConverter;
import com.contract.infrastructure.persistence.entity.QueryFillRuleEntity;
import com.contract.infrastructure.persistence.mapper.QueryFillRuleMapper;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QueryFillRuleRepositoryImpl implements QueryFillRuleRepository {
    private final QueryFillRuleMapper mapper;
    private final EntityQueryFillRuleConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public QueryFillRule save(QueryFillRule rule) {
        QueryFillRuleEntity entity = converter.toEntity(rule);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        mapper.insert(entity);
        return converter.toDomain(entity);
    }

    @Override
    public List<QueryFillRule> findByQueryConfigId(Long queryConfigId) {
        return converter.toDomainList(mapper.findByQueryConfigId(queryConfigId));
    }

    @Override
    public void deleteByQueryConfigId(Long queryConfigId) {
        mapper.deleteByQueryConfigId(queryConfigId);
    }
}
