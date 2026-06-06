package com.contract.domain.template.repository;

import com.contract.domain.template.LayoutNode;
import java.util.List;

/**
 * 布局节点仓储接口
 */
public interface LayoutNodeRepository {
    /**
     * 保存布局节点
     */
    LayoutNode save(LayoutNode node);

    /**
     * 根据ID查询布局节点
     */
    LayoutNode findById(Long id);

    /**
     * 根据模板版本ID查询所有布局节点
     */
    List<LayoutNode> findByVersionId(Long versionId);

    /**
     * 根据父节点ID查询子节点列表
     */
    List<LayoutNode> findByParentId(Long parentId);

    /**
     * 更新布局节点
     */
    void update(LayoutNode node);

    /**
     * 根据节点编码查询布局节点
     */
    LayoutNode findByNodeCode(String nodeCode);

    /**
     * 删除布局节点
     */
    void deleteById(Long id);
}