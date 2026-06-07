package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.infrastructure.persistence.entity.LayoutNodeEntity;
import com.contract.infrastructure.persistence.mapper.LayoutNodeMapper;
import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.repository.LayoutNodeRepository;
import com.contract.infrastructure.persistence.convert.EntityLayoutNodeConverter;
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

    @Override
    public LayoutNode save(LayoutNode node) {
        LayoutNodeEntity entity = converter.toEntity(node);
        mapper.insert(entity);
        node.setId(entity.getId());
        return node;
    }

    @Override
    public LayoutNode findById(Long id) {
        LayoutNodeEntity entity = mapper.selectById(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<LayoutNode> findByVersionId(Long versionId) {
        LambdaQueryWrapper<LayoutNodeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LayoutNodeEntity::getTemplateVersionId, versionId);
        wrapper.orderByAsc(LayoutNodeEntity::getSortNo);
        List<LayoutNodeEntity> entities = mapper.selectList(wrapper);
        return converter.toDomainList(entities);
    }

    @Override
    public List<LayoutNode> findByParentId(Long parentId) {
        LambdaQueryWrapper<LayoutNodeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LayoutNodeEntity::getParentId, parentId);
        wrapper.orderByAsc(LayoutNodeEntity::getSortNo);
        List<LayoutNodeEntity> entities = mapper.selectList(wrapper);
        return converter.toDomainList(entities);
    }

    @Override
    public void update(LayoutNode node) {
        LayoutNodeEntity entity = converter.toEntity(node);
        mapper.updateById(entity);
    }

    @Override
    public LayoutNode findByNodeCode(String nodeCode) {
        LambdaQueryWrapper<LayoutNodeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LayoutNodeEntity::getNodeCode, nodeCode);
        LayoutNodeEntity entity = mapper.selectOne(wrapper);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public void deleteByTemplateVersionId(Long versionId) {
        LambdaQueryWrapper<LayoutNodeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LayoutNodeEntity::getTemplateVersionId, versionId);
        mapper.delete(wrapper);
    }
}