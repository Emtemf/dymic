package com.contract.application.template;

import com.contract.application.template.dto.ActionConfigDTO;
import com.contract.application.template.dto.ActionConfigCreateRequest;
import com.contract.application.template.dto.ActionConfigUpdateRequest;
import com.contract.application.template.convert.ActionConfigConverter;
import com.contract.common.exception.BizException;
import com.contract.domain.template.ActionConfig;
import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.repository.ActionConfigRepository;
import com.contract.domain.template.repository.LayoutNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 动作配置应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActionConfigService {

    private final ActionConfigRepository repository;
    private final ActionConfigConverter converter;
    private final LayoutNodeRepository layoutNodeRepository;

    /**
     * 创建动作配置（自动生成actionCode）
     *
     * @param templateId 模板ID
     * @param versionId  版本ID
     * @param request    创建请求
     * @return 创建的动作配置DTO
     */
    @Transactional
    public ActionConfigDTO create(Long templateId, Long versionId, ActionConfigCreateRequest request) {
        // 验证动作类型有效性
        if (!ActionConfig.isValidActionType(request.getActionType())) {
            throw new BizException("不支持的动作类型: " + request.getActionType());
        }

        // 验证绑定节点存在
        LayoutNode node = layoutNodeRepository.findById(request.getBindNodeId());
        if (node == null) {
            throw new BizException("绑定节点不存在: " + request.getBindNodeId());
        }

        // 创建动作配置
        ActionConfig config = converter.toDomain(request);
        config.setTemplateId(templateId);
        config.setTemplateVersionId(versionId);

        // 自动生成actionCode（拼音驼峰）
        config.generateActionCode();

        // 设置默认值
        if (config.getConfirmRequired() == null) {
            config.setConfirmRequired(0);
        }
        if (config.getSortNo() == null) {
            config.setSortNo(0);
        }

        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());

        ActionConfig saved = repository.save(config);
        log.info("创建动作配置成功: id={}, templateId={}, versionId={}, actionCode={}, actionType={}",
            saved.getId(), templateId, versionId, saved.getActionCode(), saved.getActionType());

        return converter.toDTO(saved);
    }

    /**
     * 根据ID查询动作配置
     *
     * @param id 动作配置ID
     * @return 动作配置DTO
     */
    public ActionConfigDTO getById(Long id) {
        ActionConfig config = repository.findById(id);
        if (config == null) {
            throw new BizException("动作配置不存在：" + id);
        }
        return converter.toDTO(config);
    }

    /**
     * 根据版本ID查询动作配置列表
     *
     * @param versionId 版本ID
     * @return 动作配置列表
     */
    public List<ActionConfigDTO> listByVersionId(Long versionId) {
        List<ActionConfig> configs = repository.findByTemplateVersionId(versionId);
        return converter.toDTOList(configs);
    }

    /**
     * 根据绑定节点ID查询动作配置列表
     *
     * @param bindNodeId 绑定节点ID
     * @return 动作配置列表
     */
    public List<ActionConfigDTO> listByBindNodeId(Long bindNodeId) {
        List<ActionConfig> allConfigs = repository.findByTemplateVersionId(
            layoutNodeRepository.findById(bindNodeId).getTemplateVersionId()
        );
        return allConfigs.stream()
            .filter(c -> c.getBindNodeId().equals(bindNodeId))
            .map(converter::toDTO)
            .toList();
    }

    /**
     * 更新动作配置
     *
     * @param id      动作配置ID
     * @param request 更新请求
     * @return 更新后的动作配置DTO
     */
    @Transactional
    public ActionConfigDTO update(Long id, ActionConfigUpdateRequest request) {
        ActionConfig config = repository.findById(id);
        if (config == null) {
            throw new BizException("动作配置不存在：" + id);
        }

        converter.updateFromDTO(request, config);

        // 如果actionName更新，重新生成actionCode
        if (request.getActionName() != null && !request.getActionName().equals(config.getActionName())) {
            config.generateActionCode();
        }

        config.setUpdatedAt(LocalDateTime.now());

        repository.update(config);
        log.info("更新动作配置成功: id={}, actionCode={}", id, config.getActionCode());

        return converter.toDTO(config);
    }

    /**
     * 删除动作配置
     *
     * @param id 动作配置ID
     */
    @Transactional
    public void delete(Long id) {
        ActionConfig config = repository.findById(id);
        if (config == null) {
            throw new BizException("动作配置不存在：" + id);
        }

        repository.deleteById(id);
        log.info("删除动作配置成功: id={}", id);
    }
}