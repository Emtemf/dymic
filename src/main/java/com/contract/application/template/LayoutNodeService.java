package com.contract.application.template;

import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.LayoutNodeCreateDTO;
import com.contract.application.template.dto.LayoutNodeUpdateDTO;
import com.contract.application.template.convert.LayoutNodeConverter;
import com.contract.common.exception.BizException;
import com.contract.common.util.JsonbUtils;
import com.contract.common.util.ChineseToPinyin;
import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.repository.LayoutNodeRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 布局节点应用服务
 */
@Service
@RequiredArgsConstructor
public class LayoutNodeService {
    private final LayoutNodeRepository repository;
    private final LayoutNodeConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 创建布局节点
     */
    @Transactional
    public LayoutNodeDTO create(Long templateId, Long versionId, LayoutNodeCreateDTO dto) {
        LayoutNode node = converter.toDomain(dto);
        node.setId(idGenerator.nextId());
        node.setTemplateId(templateId);
        node.setTemplateVersionId(versionId);
        node.setNodeName(dto.getNodeName());

        // 使用前端提供的 nodeCode，或自动生成
        if (dto.getNodeCode() != null && !dto.getNodeCode().isBlank()) {
            node.setNodeCode(dto.getNodeCode());
        } else {
            node.setNodeCode(generateNodeCode(dto.getNodeType(), dto.getNodeName()));
        }
        node.setNodePath(generateNodePath(dto.getParentId(), dto.getNodeName()));

        // 处理 JSONB 字段
        if (dto.getVisibleRule() != null) {
            node.setVisibleRule(dto.getVisibleRule());
        }
        if (dto.getReadonlyRule() != null) {
            node.setReadonlyRule(dto.getReadonlyRule());
        }
        if (dto.getPropsJson() != null) {
            node.setPropsJson(dto.getPropsJson());
        }

        node.setSortNo(dto.getSortNo() != null ? dto.getSortNo() : 0);
        node.setLevelNo(dto.getLevelNo() != null ? dto.getLevelNo() : 1);
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        node.setIsDeleted(0);

        repository.save(node);
        return converter.toDTO(node);
    }

    /**
     * 根据ID查询布局节点
     */
    public LayoutNodeDTO getById(Long id) {
        LayoutNode node = repository.findById(id);
        if (node == null) {
            throw new BizException("布局节点不存在：" + id);
        }
        return converter.toDTO(node);
    }

    /**
     * 根据模板版本ID查询布局节点列表
     */
    public List<LayoutNodeDTO> listByVersionId(Long versionId) {
        List<LayoutNode> nodes = repository.findByVersionId(versionId);
        return converter.toDTOList(nodes);
    }

    /**
     * 更新布局节点
     */
    @Transactional
    public LayoutNodeDTO update(Long id, LayoutNodeUpdateDTO dto) {
        LayoutNode node = repository.findById(id);
        if (node == null) {
            throw new BizException("布局节点不存在：" + id);
        }

        converter.updateDomainFromDTO(dto, node);
        node.setUpdatedAt(LocalDateTime.now());

        // 处理 JSONB 字段
        if (dto.getVisibleRule() != null) {
            node.setVisibleRule(dto.getVisibleRule());
        }
        if (dto.getReadonlyRule() != null) {
            node.setReadonlyRule(dto.getReadonlyRule());
        }
        if (dto.getPropsJson() != null) {
            node.setPropsJson(dto.getPropsJson());
        }

        repository.update(node);
        return converter.toDTO(node);
    }

    /**
     * 自动生成节点编码：card_basicInfo
     */
    private String generateNodeCode(String nodeType, String displayName) {
        String pinyin = ChineseToPinyin.toPinyin(displayName);
        String typePrefix = nodeType.toLowerCase();
        return typePrefix + "_" + pinyin;
    }

    /**
     * 自动生成节点路径：basicInfo 或 parentPath.fieldName
     */
    private String generateNodePath(Long parentId, String displayName) {
        String fieldName = ChineseToPinyin.toPinyin(displayName);

        if (parentId == null) {
            return fieldName;
        } else {
            LayoutNode parent = repository.findById(parentId);
            if (parent == null) {
                throw new BizException("父节点不存在：" + parentId);
            }
            return parent.getNodePath() + "." + fieldName;
        }
    }

    /**
     * 删除布局节点
     */
    @Transactional
    public void deleteById(Long id) {
        LayoutNode node = repository.findById(id);
        if (node == null) {
            throw new BizException("布局节点不存在：" + id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public void deleteByVersionId(Long versionId) {
        repository.deleteByTemplateVersionId(versionId);
    }
}