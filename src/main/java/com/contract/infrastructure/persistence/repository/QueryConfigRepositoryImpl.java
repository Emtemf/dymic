package com.contract.infrastructure.persistence.repository;

import com.contract.domain.template.QueryConfig;
import com.contract.domain.template.repository.QueryConfigRepository;
import com.contract.infrastructure.persistence.convert.EntityQueryConfigConverter;
import com.contract.infrastructure.persistence.entity.QueryConfigEntity;
import com.contract.infrastructure.persistence.mapper.QueryConfigMapper;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QueryConfigRepositoryImpl implements QueryConfigRepository {
    private final QueryConfigMapper mapper;
    private final EntityQueryConfigConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public QueryConfig save(QueryConfig config) {
        QueryConfigEntity entity = converter.toEntity(config);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        mapper.insert(entity);
        return converter.toDomain(entity);
    }

    @Override
    public QueryConfig findById(Long id) {
        QueryConfigEntity entity = mapper.findById(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<QueryConfig> findByTemplateVersionId(Long versionId) {
        return converter.toDomainList(mapper.findByTemplateVersionId(versionId));
    }

    @Override
    public void update(QueryConfig config) {
        mapper.update(converter.toEntity(config));
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
