package com.contract.infrastructure.persistence.repository;

import com.contract.domain.template.QueryParam;
import com.contract.domain.template.repository.QueryParamRepository;
import com.contract.infrastructure.persistence.convert.EntityQueryParamConverter;
import com.contract.infrastructure.persistence.entity.QueryParamEntity;
import com.contract.infrastructure.persistence.mapper.QueryParamMapper;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QueryParamRepositoryImpl implements QueryParamRepository {
    private final QueryParamMapper mapper;
    private final EntityQueryParamConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public QueryParam save(QueryParam param) {
        QueryParamEntity entity = converter.toEntity(param);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        mapper.insert(entity);
        return converter.toDomain(entity);
    }

    @Override
    public List<QueryParam> findByQueryConfigId(Long queryConfigId) {
        return converter.toDomainList(mapper.findByQueryConfigId(queryConfigId));
    }

    @Override
    public void deleteByQueryConfigId(Long queryConfigId) {
        mapper.deleteByQueryConfigId(queryConfigId);
    }
}
