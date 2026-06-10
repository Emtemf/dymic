package com.contract.application.template;

import com.contract.application.template.convert.QueryConfigConverter;
import com.contract.application.template.convert.QueryParamConverter;
import com.contract.application.template.convert.QueryFillRuleConverter;
import com.contract.application.template.dto.*;
import com.contract.common.exception.BizException;
import com.contract.domain.template.QueryConfig;
import com.contract.domain.template.QueryParam;
import com.contract.domain.template.QueryFillRule;
import com.contract.domain.template.repository.QueryConfigRepository;
import com.contract.domain.template.repository.QueryParamRepository;
import com.contract.domain.template.repository.QueryFillRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryConfigService {
    private final QueryConfigRepository repository;
    private final QueryParamRepository paramRepository;
    private final QueryFillRuleRepository fillRuleRepository;
    private final QueryConfigConverter converter;
    private final QueryParamConverter paramConverter;
    private final QueryFillRuleConverter fillRuleConverter;

    @Transactional
    public QueryConfigDTO create(Long templateId, Long versionId, QueryConfigCreateRequest request) {
        // 创建查询配置
        QueryConfig config = converter.toDomain(request);
        config.setTemplateId(templateId);
        config.setTemplateVersionId(versionId);

        // 生成queryCode（如果没有提供）
        if (config.getQueryCode() == null || config.getQueryCode().isBlank()) {
            config.setQueryCode("query_" + System.currentTimeMillis());
        }

        config.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        config.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

        QueryConfig saved = repository.save(config);

        // 保存查询参数
        if (request.getParams() != null && !request.getParams().isEmpty()) {
            for (QueryParamDTO paramDTO : request.getParams()) {
                QueryParam param = paramConverter.toDomain(paramDTO);
                param.setQueryConfigId(saved.getId());
                param.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
                param.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                paramRepository.save(param);
            }
        }

        // 保存回填规则
        if (request.getFillRules() != null && !request.getFillRules().isEmpty()) {
            for (QueryFillRuleDTO ruleDTO : request.getFillRules()) {
                QueryFillRule rule = fillRuleConverter.toDomain(ruleDTO);
                rule.setQueryConfigId(saved.getId());
                rule.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
                rule.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                fillRuleRepository.save(rule);
            }
        }

        log.info("创建查询配置成功: id={}, templateId={}, versionId={}", saved.getId(), templateId, versionId);

        return getById(saved.getId());
    }

    public QueryConfigDTO getById(Long id) {
        QueryConfig config = repository.findById(id);
        if (config == null) {
            throw new BizException("查询配置不存在：" + id);
        }

        QueryConfigDTO dto = converter.toDTO(config);

        // 加载查询参数
        List<QueryParam> params = paramRepository.findByQueryConfigId(id);
        dto.setParams(paramConverter.toDTOList(params));

        // 加载回填规则
        List<QueryFillRule> rules = fillRuleRepository.findByQueryConfigId(id);
        dto.setFillRules(fillRuleConverter.toDTOList(rules));

        return dto;
    }

    public List<QueryConfigDTO> listByVersionId(Long versionId) {
        List<QueryConfig> configs = repository.findByTemplateVersionId(versionId);
        return converter.toDTOList(configs);
    }

    @Transactional
    public QueryConfigDTO update(Long id, QueryConfigUpdateRequest request) {
        QueryConfig config = repository.findById(id);
        if (config == null) {
            throw new BizException("查询配置不存在：" + id);
        }

        converter.updateFromDTO(request, config);
        config.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        repository.update(config);

        // 更新查询参数（先删除后新增）
        if (request.getParams() != null) {
            paramRepository.deleteByQueryConfigId(id);
            for (QueryParamDTO paramDTO : request.getParams()) {
                QueryParam param = paramConverter.toDomain(paramDTO);
                param.setQueryConfigId(id);
                param.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
                param.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                paramRepository.save(param);
            }
        }

        // 更新回填规则（先删除后新增）
        if (request.getFillRules() != null) {
            fillRuleRepository.deleteByQueryConfigId(id);
            for (QueryFillRuleDTO ruleDTO : request.getFillRules()) {
                QueryFillRule rule = fillRuleConverter.toDomain(ruleDTO);
                rule.setQueryConfigId(id);
                rule.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
                rule.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
                fillRuleRepository.save(rule);
            }
        }

        log.info("更新查询配置成功: id={}", id);

        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        QueryConfig config = repository.findById(id);
        if (config == null) {
            throw new BizException("查询配置不存在：" + id);
        }

        // 删除关联的参数和规则
        paramRepository.deleteByQueryConfigId(id);
        fillRuleRepository.deleteByQueryConfigId(id);

        repository.deleteById(id);
        log.info("删除查询配置成功: id={}", id);
    }
}
