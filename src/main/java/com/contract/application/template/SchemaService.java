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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 批量保存配置（替换式保存，带前端ID→数据库ID映射）
     */
    @Transactional
    public SchemaDTO saveSchema(Long templateId, Long versionId, SchemaSaveDTO dto) {
        log.info("Saving schema for templateId={}, versionId={}", templateId, versionId);

        Map<String, Long> nodeIdMap = new HashMap<>();
        Map<String, Long> fieldDefIdMap = new HashMap<>();
        Map<String, FieldComponentDTO> autoCreatedComponents = new HashMap<>();

        // 1. 删除旧数据（按外键依赖反序）
        actionConfigService.deleteByVersionId(versionId);
        fieldComponentService.deleteByVersionId(versionId);
        fieldDefService.deleteByVersionId(versionId);
        layoutNodeService.deleteByVersionId(versionId);

        // 2. 创建布局节点（先父后子，建立ID映射）
        if (dto.getLayoutNodes() != null) {
            // 按层级排序：parentId为null的排前面
            List<SchemaSaveDTO.LayoutNodeSaveItem> sorted = dto.getLayoutNodes().stream()
                .sorted((a, b) -> {
                    if (a.getParentId() == null && b.getParentId() != null) return -1;
                    if (a.getParentId() != null && b.getParentId() == null) return 1;
                    return 0;
                })
                .collect(Collectors.toList());

            for (SchemaSaveDTO.LayoutNodeSaveItem item : sorted) {
                LayoutNodeCreateDTO createDTO = new LayoutNodeCreateDTO();
                if (item.getParentId() != null && nodeIdMap.containsKey(item.getParentId())) {
                    createDTO.setParentId(nodeIdMap.get(item.getParentId()));
                }
                createDTO.setNodeCode(item.getNodeCode());
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
                createDTO.setVisibleRule(item.getVisibleRule());
                createDTO.setReadonlyRule(item.getReadonlyRule());
                createDTO.setPropsJson(item.getPropsJson());

                LayoutNodeDTO created = layoutNodeService.create(templateId, versionId, createDTO);
                if (item.getId() != null) {
                    nodeIdMap.put(item.getId(), created.getId());
                }
            }
        }

        // 3. 创建字段定义（会自动创建字段组件绑定，避免重复）
        if (dto.getFieldDefs() != null) {
            for (SchemaSaveDTO.FieldDefSaveItem item : dto.getFieldDefs()) {
                FieldDefCreateDTO createDTO = new FieldDefCreateDTO();
                if (item.getLayoutNodeId() != null && nodeIdMap.containsKey(item.getLayoutNodeId())) {
                    createDTO.setLayoutNodeId(nodeIdMap.get(item.getLayoutNodeId()));
                }
                createDTO.setFieldNameCn(item.getFieldNameCn());
                createDTO.setDataType(item.getDataType());
                createDTO.setPlaceholder(item.getPlaceholder());
                createDTO.setSortNo(item.getSortNo());
                if (item.getRequiredDefault() != null && item.getRequiredDefault() == 1) {
                    createDTO.setRequired(true);
                }

                FieldDefCreateResult result = fieldDefService.create(templateId, versionId, createDTO);
                if (item.getId() != null) {
                    fieldDefIdMap.put(item.getId(), result.getFieldDef().getId());
                    if (result.getFieldComponent() != null) {
                        autoCreatedComponents.put(item.getId(), result.getFieldComponent());
                    }
                }
            }
        }

        // 4. 更新自动创建的字段组件（补充前端传入的额外属性）
        if (dto.getFieldComponents() != null) {
            for (SchemaSaveDTO.FieldComponentSaveItem item : dto.getFieldComponents()) {
                FieldComponentDTO autoComponent = item.getFieldDefId() != null
                    ? autoCreatedComponents.get(item.getFieldDefId()) : null;
                if (autoComponent != null) {
                    FieldComponentUpdateDTO updateDTO = new FieldComponentUpdateDTO();
                    boolean needUpdate = false;
                    if (item.getLabelName() != null) {
                        updateDTO.setLabelName(item.getLabelName());
                        needUpdate = true;
                    }
                    if (item.getPlaceholder() != null) {
                        updateDTO.setPlaceholder(item.getPlaceholder());
                        needUpdate = true;
                    }
                    if (item.getComponentProps() != null) {
                        updateDTO.setComponentProps(item.getComponentProps());
                        needUpdate = true;
                    }
                    if (item.getRequiredRule() != null) {
                        updateDTO.setRequiredRule(item.getRequiredRule());
                        needUpdate = true;
                    }
                    if (needUpdate) {
                        fieldComponentService.update(autoComponent.getId(), updateDTO);
                    }
                }
            }
        }

        // 5. 创建动作配置（映射bindNodeId）
        if (dto.getActionConfigs() != null) {
            for (SchemaSaveDTO.ActionConfigSaveItem item : dto.getActionConfigs()) {
                if (item.getBindNodeId() == null) continue;
                ActionConfigCreateRequest createDTO = new ActionConfigCreateRequest();
                createDTO.setActionName(item.getActionName());
                createDTO.setActionType(item.getActionType());
                if (nodeIdMap.containsKey(item.getBindNodeId())) {
                    createDTO.setBindNodeId(nodeIdMap.get(item.getBindNodeId()));
                }
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