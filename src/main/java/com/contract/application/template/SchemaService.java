package com.contract.application.template;

import com.contract.application.template.dto.*;
import com.contract.domain.template.Template;
import com.contract.domain.template.TemplateVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Schema聚合服务
 * 获取完整配置树用于前端渲染
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaService {
    private final LayoutNodeService layoutNodeService;
    private final FieldDefService fieldDefService;
    private final FieldComponentService fieldComponentService;
    private final ActionConfigService actionConfigService;
    private final TemplateVersionService templateVersionService;
    private final TemplateService templateService;

    /**
     * 获取完整配置树（用于前端渲染）
     *
     * @param templateId 模板ID
     * @param versionId  版本ID
     * @return 完整配置树DTO
     */
    @Transactional(readOnly = true)
    public SchemaDTO getSchema(Long templateId, Long versionId) {
        log.info("Getting schema for templateId={}, versionId={}", templateId, versionId);

        // 1. 获取模板和版本基本信息
        Template template = templateService.getById(templateId);
        TemplateVersion version = templateVersionService.getById(versionId);

        // 2. 获取所有配置数据（平铺列表）
        List<LayoutNodeDTO> nodes = layoutNodeService.listByVersionId(versionId);
        List<FieldDefDTO> fieldDefs = fieldDefService.listByVersionId(versionId);
        List<FieldComponentDTO> components = fieldComponentService.listByVersionId(versionId);
        List<ActionConfigDTO> actions = actionConfigService.listByVersionId(versionId);

        // 3. 构建嵌套树结构
        List<LayoutNodeTreeDTO> nodeTree = buildNodeTree(nodes, components, actions);

        // 4. 构建SchemaDTO
        SchemaDTO schema = SchemaDTO.builder()
            .templateId(templateId)
            .templateVersionId(versionId)
            .templateCode(template.getTemplateCode())
            .templateName(template.getTemplateName())
            .templateDesc(template.getTemplateDesc())
            .bizType(template.getBizType())
            .versionNo(version.getVersionNo())
            .versionName(version.getVersionName())
            .versionStatus(version.getVersionStatus())
            .layoutNodes(nodeTree)
            .fieldDefs(fieldDefs)
            .fieldComponents(components)
            .actionConfigs(actions)
            .build();

        log.info("Schema built successfully: templateId={}, nodes={}, fieldDefs={}, components={}, actions={}",
            templateId, nodes.size(), fieldDefs.size(), components.size(), actions.size());

        return schema;
    }

    /**
     * 批量保存配置（替换式保存）
     */
    @Transactional
    public SchemaDTO saveSchema(Long templateId, Long versionId, SchemaSaveDTO dto) {
        log.info("Saving schema for templateId={}, versionId={}", templateId, versionId);

        // 1. 删除旧数据（按外键依赖反序）
        actionConfigService.deleteByVersionId(versionId);
        fieldComponentService.deleteByVersionId(versionId);
        fieldDefService.deleteByVersionId(versionId);
        layoutNodeService.deleteByVersionId(versionId);

        // 2. 插入新数据（按外键依赖顺序）
        if (dto.getLayoutNodes() != null) {
            for (SchemaSaveDTO.LayoutNodeSaveItem item : dto.getLayoutNodes()) {
                LayoutNodeCreateDTO createDTO = new LayoutNodeCreateDTO();
                createDTO.setParentId(item.getParentId());
                createDTO.setNodeType(item.getNodeType());
                createDTO.setNodeName(item.getNodeName() != null ? item.getNodeName() : item.getNodeType());
                createDTO.setSortNo(item.getSortNo() != null ? item.getSortNo() : 0);
                createDTO.setLevelNo(item.getLevelNo());
                createDTO.setGridX(item.getGridX());
                createDTO.setGridY(item.getGridY());
                createDTO.setGridW(item.getGridW());
                createDTO.setGridH(item.getGridH());
                createDTO.setRowNo(item.getRowNo());
                createDTO.setColNo(item.getColNo());
                createDTO.setColSpan(item.getColSpan());
                createDTO.setRowSpan(item.getRowSpan());
                createDTO.setBindType(item.getBindType());
                createDTO.setBindRefId(item.getBindRefId());
                createDTO.setVisibleRule(item.getVisibleRule());
                createDTO.setReadonlyRule(item.getReadonlyRule());
                createDTO.setPropsJson(item.getPropsJson());
                layoutNodeService.create(templateId, versionId, createDTO);
            }
        }

        if (dto.getFieldDefs() != null) {
            for (SchemaSaveDTO.FieldDefSaveItem item : dto.getFieldDefs()) {
                FieldDefCreateDTO createDTO = new FieldDefCreateDTO();
                createDTO.setFieldNameCn(item.getFieldNameCn());
                createDTO.setDataType(item.getDataType());
                createDTO.setLayoutNodeId(item.getLayoutNodeId());
                createDTO.setPlaceholder(item.getPlaceholder());
                createDTO.setSortNo(item.getSortNo());
                if (item.getRequiredDefault() != null && item.getRequiredDefault() == 1) {
                    createDTO.setRequired(true);
                }
                fieldDefService.create(templateId, versionId, createDTO);
            }
        }

        if (dto.getFieldComponents() != null) {
            for (SchemaSaveDTO.FieldComponentSaveItem item : dto.getFieldComponents()) {
                FieldComponentCreateRequest createDTO = new FieldComponentCreateRequest();
                createDTO.setLayoutNodeId(item.getLayoutNodeId());
                createDTO.setFieldDefId(item.getFieldDefId());
                createDTO.setComponentType(item.getComponentType());
                createDTO.setLabelName(item.getLabelName());
                createDTO.setPlaceholder(item.getPlaceholder());
                createDTO.setComponentProps(item.getComponentProps());
                createDTO.setSortNo(item.getSortNo() != null ? item.getSortNo() : 0);
                createDTO.setDataProviderId(item.getDataProviderId());
                fieldComponentService.create(templateId, versionId, createDTO);
            }
        }

        if (dto.getActionConfigs() != null) {
            for (SchemaSaveDTO.ActionConfigSaveItem item : dto.getActionConfigs()) {
                if (item.getBindNodeId() == null) continue;
                ActionConfigCreateRequest createDTO = new ActionConfigCreateRequest();
                createDTO.setActionName(item.getActionName());
                createDTO.setActionType(item.getActionType());
                createDTO.setBindNodeId(item.getBindNodeId());
                createDTO.setSortNo(item.getSortNo() != null ? item.getSortNo() : 0);
                actionConfigService.create(templateId, versionId, createDTO);
            }
        }

        log.info("Schema saved successfully: templateId={}, versionId={}, layoutNodes={}, fieldDefs={}, fieldComponents={}, actionConfigs={}",
            templateId, versionId,
            dto.getLayoutNodes() != null ? dto.getLayoutNodes().size() : 0,
            dto.getFieldDefs() != null ? dto.getFieldDefs().size() : 0,
            dto.getFieldComponents() != null ? dto.getFieldComponents().size() : 0,
            dto.getActionConfigs() != null ? dto.getActionConfigs().size() : 0);

        return getSchema(templateId, versionId);
    }

    /**
     * 构建嵌套节点树（递归）
     */
    private List<LayoutNodeTreeDTO> buildNodeTree(
        List<LayoutNodeDTO> nodes,
        List<FieldComponentDTO> components,
        List<ActionConfigDTO> actions
    ) {
        // 找出根节点（parentId为null）
        List<LayoutNodeDTO> rootNodes = nodes.stream()
            .filter(n -> n.getParentId() == null)
            .sorted(Comparator.comparing(LayoutNodeDTO::getSortNo))
            .collect(Collectors.toList());

        // 递归构建树
        return rootNodes.stream()
            .map(node -> buildTreeNode(node, nodes, components, actions))
            .collect(Collectors.toList());
    }

    /**
     * 构建单个树节点（递归）
     */
    private LayoutNodeTreeDTO buildTreeNode(
        LayoutNodeDTO node,
        List<LayoutNodeDTO> allNodes,
        List<FieldComponentDTO> components,
        List<ActionConfigDTO> actions
    ) {
        // 找出该节点的所有组件
        List<FieldComponentDTO> nodeComponents = components.stream()
            .filter(c -> node.getId().equals(c.getLayoutNodeId()))
            .sorted(Comparator.comparing(FieldComponentDTO::getSortNo))
            .collect(Collectors.toList());

        // 找出该节点的所有动作
        List<ActionConfigDTO> nodeActions = actions.stream()
            .filter(a -> node.getId().equals(a.getBindNodeId()))
            .sorted(Comparator.comparing(ActionConfigDTO::getSortNo))
            .collect(Collectors.toList());

        // 找出该节点的所有子节点
        List<LayoutNodeDTO> children = allNodes.stream()
            .filter(n -> n.getParentId() != null && node.getId().equals(n.getParentId()))
            .sorted(Comparator.comparing(LayoutNodeDTO::getSortNo))
            .collect(Collectors.toList());

        // 递归构建子节点树
        List<LayoutNodeTreeDTO> childTree = children.stream()
            .map(child -> buildTreeNode(child, allNodes, components, actions))
            .collect(Collectors.toList());

        // 构建当前节点树
        return LayoutNodeTreeDTO.builder()
            .id(node.getId())
            .nodeCode(node.getNodeCode())
            .nodePath(node.getNodePath())
            .nodeName(node.getNodeName())
            .nodeType(node.getNodeType())
            .sortNo(node.getSortNo())
            .components(nodeComponents)
            .actions(nodeActions)
            .children(childTree)
            .gridX(node.getGridX())
            .gridY(node.getGridY())
            .gridW(node.getGridW())
            .gridH(node.getGridH())
            .rowNo(node.getRowNo())
            .colNo(node.getColNo())
            .colSpan(node.getColSpan())
            .rowSpan(node.getRowSpan())
            .visibleRule(node.getVisibleRule())
            .readonlyRule(node.getReadonlyRule())
            .propsJson(node.getPropsJson())
            .build();
    }
}