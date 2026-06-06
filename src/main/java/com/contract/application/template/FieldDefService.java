package com.contract.application.template;

import com.contract.application.template.dto.FieldDefDTO;
import com.contract.application.template.dto.FieldDefCreateDTO;
import com.contract.application.template.dto.FieldDefCreateResult;
import com.contract.application.template.dto.FieldDefUpdateDTO;
import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.convert.FieldDefConverter;
import com.contract.common.exception.BizException;
import com.contract.common.util.ChineseToPinyin;
import com.contract.domain.template.FieldDef;
import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.repository.FieldDefRepository;
import com.contract.domain.template.repository.LayoutNodeRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 字段定义应用服务
 * 关键特性:创建字段定义时,同时创建字段组件绑定(一体化)
 */
@Service
@RequiredArgsConstructor
public class FieldDefService {
    private final FieldDefRepository repository;
    private final LayoutNodeRepository layoutNodeRepository;
    private final FieldComponentService fieldComponentService;
    private final FieldDefConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 创建字段定义(一体化:同时创建字段组件绑定)
     */
    @Transactional
    public FieldDefCreateResult create(Long templateId, Long versionId, FieldDefCreateDTO dto) {
        // 1. 获取布局节点
        LayoutNode layoutNode = layoutNodeRepository.findById(dto.getLayoutNodeId());
        if (layoutNode == null) {
            throw new BizException("布局节点不存在：" + dto.getLayoutNodeId());
        }

        // 2. 创建字段定义
        FieldDef fieldDef = new FieldDef();
        fieldDef.setId(idGenerator.nextId());
        fieldDef.setTemplateId(templateId);
        fieldDef.setTemplateVersionId(versionId);
        fieldDef.setLayoutNodeId(dto.getLayoutNodeId());

        // 自动生成 fieldCode 和 fieldPath
        fieldDef.setFieldCode(generateFieldCode(dto.getDisplayName()));
        fieldDef.setFieldPath(generateFieldPath(layoutNode.getNodePath(), dto.getDisplayName()));

        // 自动检测数据类型
        fieldDef.setDataType(detectDataType(dto.getComponentType()));
        fieldDef.setFieldNameCn(dto.getDisplayName());
        fieldDef.setRequiredDefault(dto.getRequired() ? 1 : 0);

        fieldDef.setCreatedAt(LocalDateTime.now());
        fieldDef.setUpdatedAt(LocalDateTime.now());

        repository.save(fieldDef);

        // 3. 同时创建字段组件绑定
        FieldComponentDTO fieldComponent = fieldComponentService.create(
            templateId, versionId, fieldDef.getId(), dto.getLayoutNodeId(), dto
        );

        // 4. 返回结果
        FieldDefDTO fieldDefDTO = converter.toDTO(fieldDef);
        return FieldDefCreateResult.builder()
            .fieldDef(fieldDefDTO)
            .fieldComponent(fieldComponent)
            .build();
    }

    public FieldDefDTO getById(Long id) {
        FieldDef fieldDef = repository.findById(id);
        if (fieldDef == null) {
            throw new BizException("字段定义不存在：" + id);
        }
        return converter.toDTO(fieldDef);
    }

    public List<FieldDefDTO> listByVersionId(Long versionId) {
        List<FieldDef> fieldDefs = repository.findByVersionId(versionId);
        return converter.toDTOList(fieldDefs);
    }

    @Transactional
    public FieldDefDTO update(Long id, FieldDefUpdateDTO dto) {
        FieldDef fieldDef = repository.findById(id);
        if (fieldDef == null) {
            throw new BizException("字段定义不存在：" + id);
        }

        if (dto.getFieldNameCn() != null) {
            fieldDef.setFieldNameCn(dto.getFieldNameCn());
        }
        if (dto.getRequiredDefault() != null) {
            fieldDef.setRequiredDefault(dto.getRequiredDefault());
        }

        fieldDef.setUpdatedAt(LocalDateTime.now());
        repository.update(fieldDef);
        return converter.toDTO(fieldDef);
    }

    /**
     * 自动生成字段编码: contractName
     */
    private String generateFieldCode(String displayName) {
        return ChineseToPinyin.toPinyin(displayName).toLowerCase();
    }

    /**
     * 自动生成字段路径: basicInfo.contractName
     */
    private String generateFieldPath(String nodePath, String displayName) {
        return nodePath + "." + generateFieldCode(displayName);
    }

    /**
     * 自动检测数据类型: INPUT → TEXT, NUMBER → NUMBER
     */
    private String detectDataType(String componentType) {
        switch (componentType) {
            case "INPUT":
                return "TEXT";
            case "NUMBER":
                return "NUMBER";
            case "DATE":
                return "DATE";
            case "MONEY":
                return "NUMBER";
            case "SELECT":
                return "TEXT";
            default:
                return "TEXT";
        }
    }
}