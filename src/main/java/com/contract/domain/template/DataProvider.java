package com.contract.domain.template;

import com.contract.domain.dataprovider.types.*;
import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.shared.types.ConfigJson;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * 数据提供方聚合根
 *
 * 【聚合边界】
 * - DataProvider是聚合根
 * - 包含配置信息（configJson）
 * - 包含缓存配置（cacheConfig）
 *
 * 【状态流转】
 * ENABLED → DISABLED（停用）
 * DISABLED → ENABLED（启用）
 *
 * 【业务规则】
 * 1. 静态选项和字典数据源由业务人员配置
 * 2. HTTP、平台、内部数据源由IT人员配置
 * 3. 临时数据源不持久化
 * 4. 缓存配置根据数据源类型有默认值
 *
 * 【不变性】
 * - ID创建后不可修改
 * - 编码创建后不可修改
 * - 类型创建后不可修改
 */
@Getter
public class DataProvider {
    private final ProviderId id;
    private final ProviderCode providerCode;
    private final ProviderName providerName;
    private final ProviderType providerType;
    private final ConfigJson configJson;
    private final CacheConfig cacheConfig;
    private final boolean isTemporary;
    private ProviderStatus status;
    private final AuditInfo auditInfo;

    /**
     * 私有构造器（通过工厂方法创建）
     */
    private DataProvider(
        ProviderId id, ProviderCode providerCode, ProviderName providerName,
        ProviderType providerType, ConfigJson configJson, CacheConfig cacheConfig,
        boolean isTemporary, ProviderStatus status, AuditInfo auditInfo
    ) {
        this.id = id;
        this.providerCode = providerCode;
        this.providerName = providerName;
        this.providerType = providerType;
        this.configJson = configJson;
        this.cacheConfig = cacheConfig;
        this.isTemporary = isTemporary;
        this.status = status;
        this.auditInfo = auditInfo;
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建静态选项DataProvider（业务自定义）
     *
     * 使用场景：业务人员在组件配置时选择"静态选项"，输入自定义选项列表
     *
     * @param displayName 显示名称，如"城市选择"
     * @param optionsJson 选项JSON，格式：[{"value":"北京","label":"北京"},{"value":"上海","label":"上海"}]
     * @return DataProvider实例
     */
    public static DataProvider createStaticOptions(String displayName, String optionsJson) {
        ProviderName name = new ProviderName(displayName);
        ConfigJson config = new ConfigJson(optionsJson);
        ProviderCode code = ProviderCode.generate(ProviderType.STATIC);

        return new DataProvider(
            null, // ID由持久化层生成
            code,
            name,
            ProviderType.STATIC,
            config,
            CacheConfig.disabled(), // 静态选项不缓存
            true, // 临时数据源
            ProviderStatus.ENABLED,
            AuditInfo.create()
        );
    }

    /**
     * 创建字典DataProvider
     *
     * 使用场景：业务人员选择"字典数据"，引用系统字典
     *
     * @param dictType 字典类型编码，如"CITY"、"STATUS"
     * @param displayName 显示名称，如"城市字典"
     * @return DataProvider实例
     */
    public static DataProvider createDict(String dictType, String displayName) {
        ProviderName name = new ProviderName(displayName);
        ConfigJson config = ConfigJson.fromDict(dictType);
        ProviderCode code = ProviderCode.generate(ProviderType.DICT);

        return new DataProvider(
            null,
            code,
            name,
            ProviderType.DICT,
            config,
            CacheConfig.enabled(3600), // 字典缓存1小时
            false, // 非临时，持久化
            ProviderStatus.ENABLED,
            AuditInfo.create()
        );
    }

    /**
     * 创建HTTP接口DataProvider（IT配置）
     *
     * @param displayName 显示名称
     * @param httpConfigJson HTTP配置JSON
     * @return DataProvider实例
     */
    public static DataProvider createHttp(String displayName, String httpConfigJson) {
        ProviderName name = new ProviderName(displayName);
        ConfigJson config = new ConfigJson(httpConfigJson);
        ProviderCode code = ProviderCode.generate(ProviderType.HTTP);

        return new DataProvider(
            null,
            code,
            name,
            ProviderType.HTTP,
            config,
            CacheConfig.enabled(300), // HTTP缓存5分钟
            false,
            ProviderStatus.ENABLED,
            AuditInfo.create()
        );
    }

    /**
     * 创建平台接口DataProvider（IT配置）
     *
     * @param displayName 显示名称
     * @param platformConfigJson 平台配置JSON
     * @return DataProvider实例
     */
    public static DataProvider createPlatform(String displayName, String platformConfigJson) {
        ProviderName name = new ProviderName(displayName);
        ConfigJson config = new ConfigJson(platformConfigJson);
        ProviderCode code = ProviderCode.generate(ProviderType.PLATFORM);

        return new DataProvider(
            null,
            code,
            name,
            ProviderType.PLATFORM,
            config,
            CacheConfig.enabled(600), // 平台缓存10分钟
            false,
            ProviderStatus.ENABLED,
            AuditInfo.create()
        );
    }

    /**
     * 创建内部查询DataProvider（IT配置）
     *
     * @param displayName 显示名称
     * @param internalConfigJson 内部查询配置JSON
     * @return DataProvider实例
     */
    public static DataProvider createInternal(String displayName, String internalConfigJson) {
        ProviderName name = new ProviderName(displayName);
        ConfigJson config = new ConfigJson(internalConfigJson);
        ProviderCode code = ProviderCode.generate(ProviderType.INTERNAL);

        return new DataProvider(
            null,
            code,
            name,
            ProviderType.INTERNAL,
            config,
            CacheConfig.enabled(300), // 内部查询缓存5分钟
            false,
            ProviderStatus.ENABLED,
            AuditInfo.create()
        );
    }

    /**
     * 基础创建方法（保留原有兼容性）
     */
    public static DataProvider create(String providerCode, String providerName, String providerType) {
        return new DataProvider(
            null,
            new ProviderCode(providerCode),
            new ProviderName(providerName),
            ProviderType.valueOf(providerType),
            new ConfigJson("{}"),
            CacheConfig.disabled(),
            false,
            ProviderStatus.ENABLED,
            AuditInfo.create()
        );
    }

    /**
     * 从持久化层重建
     */
    public static DataProvider rebuild(
        Long id, String providerCode, String providerName, String providerType,
        String configJson, Integer cacheEnabled, Integer cacheTtlSeconds,
        Integer isTemporary, String status,
        Long createdBy, String createdName, OffsetDateTime createdAt,
        Long updatedBy, String updatedName, OffsetDateTime updatedAt
    ) {
        CacheConfig cacheConfig = cacheEnabled != null && cacheEnabled == 1
            ? CacheConfig.enabled(cacheTtlSeconds != null ? cacheTtlSeconds : 300)
            : CacheConfig.disabled();

        AuditInfo auditInfo = AuditInfo.of(
            createdBy, createdName, createdAt,
            updatedBy, updatedName, updatedAt
        );

        return new DataProvider(
            id != null ? new ProviderId(id) : null,
            new ProviderCode(providerCode),
            new ProviderName(providerName),
            ProviderType.valueOf(providerType),
            new ConfigJson(configJson),
            cacheConfig,
            isTemporary != null && isTemporary == 1,
            ProviderStatus.valueOf(status),
            auditInfo
        );
    }

    // ==================== 业务方法 ====================

    /**
     * 停用数据源
     *
     * @return 新实例
     */
    public DataProvider disable() {
        if (this.status == ProviderStatus.DISABLED) {
            throw new IllegalStateException("数据源已处于停用状态");
        }
        return new DataProvider(
            this.id, this.providerCode, this.providerName,
            this.providerType, this.configJson, this.cacheConfig,
            this.isTemporary, ProviderStatus.DISABLED,
            this.auditInfo.update()
        );
    }

    /**
     * 启用数据源
     *
     * @return 新实例
     */
    public DataProvider enable() {
        if (this.status == ProviderStatus.ENABLED) {
            throw new IllegalStateException("数据源已处于启用状态");
        }
        return new DataProvider(
            this.id, this.providerCode, this.providerName,
            this.providerType, this.configJson, this.cacheConfig,
            this.isTemporary, ProviderStatus.ENABLED,
            this.auditInfo.update()
        );
    }

    /**
     * 判断是否为临时数据源
     */
    public boolean isTemporary() {
        return this.isTemporary;
    }

    /**
     * 判断是否需要缓存
     */
    public boolean needsCache() {
        return this.cacheConfig.isEnabled();
    }

    /**
     * 判断是否为业务配置类型
     */
    public boolean isBusinessConfig() {
        return this.providerType.isBusinessConfig();
    }

    /**
     * 判断是否为IT配置类型
     */
    public boolean isITConfig() {
        return this.providerType.isITConfig();
    }

    /**
     * 获取数据源分类
     */
    public DataSourceCategory getCategory() {
        return DataSourceCategory.fromProviderType(this.providerType);
    }

    // ==================== 向后兼容的便捷方法 ====================

    /**
     * 获取ID值（向后兼容）
     */
    public Long getId() {
        return id != null ? id.getValue() : null;
    }

    /**
     * 获取编码值（向后兼容）
     */
    public String getProviderCode() {
        return providerCode.getValue();
    }

    /**
     * 获取名称值（向后兼容）
     */
    public String getProviderName() {
        return providerName.getValue();
    }

    /**
     * 获取类型值（向后兼容）
     */
    public String getProviderType() {
        return providerType.name();
    }

    /**
     * 获取配置JSON值（向后兼容）
     */
    public String getConfigJson() {
        return configJson.getValue();
    }

    /**
     * 获取缓存启用标志（向后兼容）
     */
    public Integer getCacheEnabled() {
        return cacheConfig.isEnabled() ? 1 : 0;
    }

    /**
     * 获取缓存时间（向后兼容）
     */
    public Integer getCacheTtlSeconds() {
        return cacheConfig.getTtlSeconds();
    }

    /**
     * 获取临时标志（向后兼容）
     */
    public Integer getIsTemporary() {
        return isTemporary ? 1 : 0;
    }

    /**
     * 获取状态值（向后兼容）
     */
    public String getStatus() {
        return status.name();
    }

    /**
     * 获取创建人ID（向后兼容）
     */
    public Long getCreatedBy() {
        return auditInfo != null ? auditInfo.getCreatedBy() : null;
    }

    /**
     * 获取创建人名称（向后兼容）
     */
    public String getCreatedName() {
        return auditInfo != null ? auditInfo.getCreatedName() : null;
    }

    /**
     * 获取创建时间（向后兼容）
     */
    public LocalDateTime getCreatedAt() {
        return auditInfo != null && auditInfo.getCreatedAt() != null
            ? auditInfo.getCreatedAt().toLocalDateTime()
            : null;
    }

    /**
     * 获取更新人ID（向后兼容）
     */
    public Long getUpdatedBy() {
        return auditInfo != null ? auditInfo.getUpdatedBy() : null;
    }

    /**
     * 获取更新人名称（向后兼容）
     */
    public String getUpdatedName() {
        return auditInfo != null ? auditInfo.getUpdatedName() : null;
    }

    /**
     * 获取更新时间（向后兼容）
     */
    public LocalDateTime getUpdatedAt() {
        return auditInfo != null && auditInfo.getUpdatedAt() != null
            ? auditInfo.getUpdatedAt().toLocalDateTime()
            : null;
    }

    /**
     * 向后兼容：自动生成providerCode（基于名称）
     * 注意：重构后此方法不再需要，编码在创建时自动生成
     */
    @Deprecated
    public void generateProviderCode() {
        // 空实现，保持向后兼容
        // 编码在创建时已通过ProviderCode.generate()自动生成
    }
}
