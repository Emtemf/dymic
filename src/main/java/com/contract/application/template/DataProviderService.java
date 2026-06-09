package com.contract.application.template;

import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.DataProviderCreateDTO;
import com.contract.application.template.dto.DataProviderUpdateDTO;
import com.contract.application.template.dto.DataProviderCreateRequest;
import com.contract.application.template.convert.DataProviderConverter;
import com.contract.common.exception.BizException;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.repository.DataProviderRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 数据提供方应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataProviderService {
    private final DataProviderRepository repository;
    private final DataProviderConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 业务友好的数据源创建方法
     *
     * 根据业务人员选择的数据源类型自动创建DataProvider：
     * - STATIC: 业务自定义选项（临时DataProvider）
     * - DICT: 字典数据（查询或创建）
     * - HTTP/PLATFORM/INTERNAL: IT已配置，返回已存在的DataProvider
     *
     * @param request 业务友好的创建请求
     * @return DataProviderDTO
     */
    @Transactional
    public DataProviderDTO createFromBusinessRequest(DataProviderCreateRequest request) {
        String dataSourceType = request.getDataSourceType();

        // 判断数据源类型
        switch (dataSourceType) {
            case "STATIC":
                // 业务自定义选项，自动创建临时DataProvider
                return createStaticOptions(request);

            case "DICT":
                // 字典数据，查询或创建
                return findByDictTypeOrCreate(request);

            case "HTTP":
            case "PLATFORM":
            case "INTERNAL":
                // IT已配置，直接返回已存在的DataProvider
                return getExistingDataProvider(request.getDataProviderId(), dataSourceType);

            default:
                throw new BizException("不支持的数据源类型: " + dataSourceType);
        }
    }

    /**
     * 创建静态选项DataProvider（业务自定义）
     */
    @Transactional
    public DataProviderDTO createStaticOptions(DataProviderCreateRequest request) {
        if (request.getStaticOptionsJson() == null || request.getStaticOptionsJson().isEmpty()) {
            throw new BizException("静态选项JSON不能为空");
        }

        DataProvider provider = DataProvider.createStaticOptions(
            request.getDisplayName(),
            request.getStaticOptionsJson()
        );

        repository.save(provider);

        log.info("创建静态选项DataProvider成功: id={}, name={}, isTemporary={}",
            provider.getId(), provider.getProviderName(), provider.isTemporary());

        return converter.toDTO(provider);
    }

    /**
     * 查询或创建字典DataProvider
     */
    @Transactional
    public DataProviderDTO findByDictTypeOrCreate(DataProviderCreateRequest request) {
        if (request.getDictType() == null || request.getDictType().isEmpty()) {
            throw new BizException("字典类型不能为空");
        }

        // 先查询是否存在
        DataProvider existing = repository.findByDictType(request.getDictType());
        if (existing != null) {
            log.info("找到已存在的字典DataProvider: id={}, dictType={}",
                existing.getId(), request.getDictType());
            return converter.toDTO(existing);
        }

        // 不存在，创建新的
        DataProvider provider = DataProvider.createDict(
            request.getDictType(),
            request.getDisplayName()
        );

        repository.save(provider);

        log.info("创建字典DataProvider成功: id={}, dictType={}",
            provider.getId(), request.getDictType());

        return converter.toDTO(provider);
    }

    /**
     * 获取已存在的DataProvider（HTTP/PLATFORM/INTERNAL）
     */
    private DataProviderDTO getExistingDataProvider(Long dataProviderId, String expectedType) {
        if (dataProviderId == null) {
            throw new BizException("数据提供方ID不能为空，请先由IT配置HTTP/PLATFORM/INTERNAL数据源");
        }

        DataProvider provider = repository.findById(dataProviderId);
        if (provider == null) {
            throw new BizException("数据提供方不存在: " + dataProviderId);
        }

        // 验证类型匹配
        if (!provider.getProviderType().equals(expectedType)) {
            throw new BizException("数据提供方类型不匹配，期望: " + expectedType +
                "，实际: " + provider.getProviderType());
        }

        return converter.toDTO(provider);
    }

    /**
     * 根据类型查询DataProvider列表
     *
     * @param providerType 类型：STATIC/DICT/HTTP/PLATFORM/INTERNAL
     * @return 该类型的DataProvider列表
     */
    public List<DataProviderDTO> listByType(String providerType) {
        validateProviderType(providerType);
        List<DataProvider> providers = repository.findByType(providerType);
        return converter.toDTOList(providers);
    }

    /**
     * 查询所有DataProvider（用于前端选择）
     */
    public List<DataProviderDTO> listAll() {
        List<DataProvider> providers = repository.findAll();
        return converter.toDTOList(providers);
    }

    // ============ 原有CRUD方法（保留） ============

    @Transactional
    public DataProviderDTO create(DataProviderCreateDTO dto) {
        if (repository.existsByProviderCode(dto.getProviderCode())) {
            throw new BizException("数据提供方编码已存在：" + dto.getProviderCode());
        }

        DataProvider provider = converter.toDomain(dto);
        repository.save(provider);
        return converter.toDTO(provider);
    }

    public DataProviderDTO getById(Long id) {
        DataProvider provider = repository.findById(id);
        if (provider == null) {
            throw new BizException("数据提供方不存在：" + id);
        }
        return converter.toDTO(provider);
    }

    public List<DataProviderDTO> list() {
        List<DataProvider> providers = repository.findAll();
        return converter.toDTOList(providers);
    }

    @Transactional
    public DataProviderDTO update(Long id, DataProviderUpdateDTO dto) {
        DataProvider existing = repository.findById(id);
        if (existing == null) {
            throw new BizException("数据提供方不存在：" + id);
        }

        // 由于DataProvider是不可变的，需要重新创建
        // TODO: 实现基于UpdateDTO的重建逻辑
        // 暂时抛出异常，等待实现
        throw new BizException("更新功能暂未实现");
    }

    // ============ 验证方法 ============

    /**
     * 验证数据提供方类型有效性
     */
    private void validateProviderType(String providerType) {
        List<String> validTypes = Arrays.asList("STATIC", "DICT", "HTTP", "PLATFORM", "INTERNAL");
        if (!validTypes.contains(providerType)) {
            throw new BizException("无效的数据提供方类型: " + providerType +
                "，有效类型: " + validTypes);
        }
    }
}