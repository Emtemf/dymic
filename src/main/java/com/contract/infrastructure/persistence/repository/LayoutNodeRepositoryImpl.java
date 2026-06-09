package com.contract.infrastructure.persistence.repository;

import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.repository.LayoutNodeRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import com.contract.infrastructure.persistence.convert.EntityLayoutNodeConverter;
import com.contract.infrastructure.persistence.entity.LayoutNodeEntity;
import com.contract.infrastructure.persistence.mapper.LayoutNodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 布局节点仓储实现
 */
@Repository
@RequiredArgsConstructor
public class LayoutNodeRepositoryImpl implements LayoutNodeRepository {
    private final LayoutNodeMapper mapper;
    private final EntityLayoutNodeConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public LayoutNode save(LayoutNode node) {
        LayoutNodeEntity entity = converter.toEntity(node);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        mapper.insertLayoutNode(entity);
        node.setId(entity.getId());
        return node;
    }

    @Override
    public LayoutNode findById(Long id) {
        LayoutNodeEntity entity = mapper.selectByIdValue(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<LayoutNode> findByVersionId(Long versionId) {
        List<LayoutNodeEntity> entities = mapper.selectByVersionId(versionId);
        return converter.toDomainList(entities);
    }

    @Override
    public List<LayoutNode> findByParentId(Long parentId) {
        List<LayoutNodeEntity> entities = mapper.selectByParentId(parentId);
        return converter.toDomainList(entities);
    }

    @Override
    public void update(LayoutNode node) {
        LayoutNodeEntity entity = converter.toEntity(node);
        mapper.updateLayoutNode(entity);
    }

    @Override
    public LayoutNode findByNodeCode(String nodeCode) {
        LayoutNodeEntity entity = mapper.selectByNodeCode(nodeCode);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteByIdValue(id);
    }

    @Override
    public void deleteByTemplateVersionId(Long versionId) {
        mapper.physicalDeleteByVersionId(versionId);
    }
}
