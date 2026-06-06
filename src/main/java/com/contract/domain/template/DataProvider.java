package com.contract.domain.template;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 数据提供方领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataProvider {
    private Long id;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String configJson;
    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
    private Integer isTemporary;       // 是否临时数据源（0/1）
    private String status;
    private Long createdBy;
    private String createdName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

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
        DataProvider provider = new DataProvider();
        provider.setProviderName(displayName);
        provider.setProviderType("STATIC");
        provider.setConfigJson(optionsJson);
        provider.setIsTemporary(1);         // 临时数据源
        provider.setCacheEnabled(0);        // 不缓存
        provider.setCacheTtlSeconds(0);
        provider.setStatus("ENABLED");
        provider.generateProviderCode();
        return provider;
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
        DataProvider provider = new DataProvider();
        provider.setProviderName(displayName);
        provider.setProviderType("DICT");
        provider.setConfigJson("{\"dictType\":\"" + dictType + "\"}");
        provider.setIsTemporary(0);         // 非临时，持久化
        provider.setCacheEnabled(1);        // 启用缓存
        provider.setCacheTtlSeconds(3600);  // 缓存1小时
        provider.setStatus("ENABLED");
        provider.generateProviderCode();
        return provider;
    }

    /**
     * 创建HTTP接口DataProvider（IT配置）
     *
     * @param displayName 显示名称
     * @param httpConfigJson HTTP配置JSON
     * @return DataProvider实例
     */
    public static DataProvider createHttp(String displayName, String httpConfigJson) {
        DataProvider provider = new DataProvider();
        provider.setProviderName(displayName);
        provider.setProviderType("HTTP");
        provider.setConfigJson(httpConfigJson);
        provider.setIsTemporary(0);
        provider.setCacheEnabled(1);
        provider.setCacheTtlSeconds(300);   // 缓存5分钟
        provider.setStatus("ENABLED");
        provider.generateProviderCode();
        return provider;
    }

    /**
     * 创建平台接口DataProvider（IT配置）
     *
     * @param displayName 显示名称
     * @param platformConfigJson 平台配置JSON
     * @return DataProvider实例
     */
    public static DataProvider createPlatform(String displayName, String platformConfigJson) {
        DataProvider provider = new DataProvider();
        provider.setProviderName(displayName);
        provider.setProviderType("PLATFORM");
        provider.setConfigJson(platformConfigJson);
        provider.setIsTemporary(0);
        provider.setCacheEnabled(1);
        provider.setCacheTtlSeconds(600);   // 缓存10分钟
        provider.setStatus("ENABLED");
        provider.generateProviderCode();
        return provider;
    }

    /**
     * 创建内部查询DataProvider（IT配置）
     *
     * @param displayName 显示名称
     * @param internalConfigJson 内部查询配置JSON
     * @return DataProvider实例
     */
    public static DataProvider createInternal(String displayName, String internalConfigJson) {
        DataProvider provider = new DataProvider();
        provider.setProviderName(displayName);
        provider.setProviderType("INTERNAL");
        provider.setConfigJson(internalConfigJson);
        provider.setIsTemporary(0);
        provider.setCacheEnabled(1);
        provider.setCacheTtlSeconds(300);
        provider.setStatus("ENABLED");
        provider.generateProviderCode();
        return provider;
    }

    /**
     * 基础创建方法（保留原有兼容性）
     */
    public static DataProvider create(String providerCode, String providerName, String providerType) {
        DataProvider provider = new DataProvider();
        provider.setProviderCode(providerCode);
        provider.setProviderName(providerName);
        provider.setProviderType(providerType);
        provider.setIsTemporary(0);
        provider.setCacheEnabled(0);
        provider.setCacheTtlSeconds(0);
        provider.setStatus("ENABLED");
        return provider;
    }

    /**
     * 自动生成providerCode（基于名称）
     */
    public void generateProviderCode() {
        if (this.providerName != null && this.providerCode == null) {
            // 简单编码生成：时间戳 + 名称hash
            String timestamp = String.valueOf(System.currentTimeMillis() % 100000);
            String nameHash = Integer.toHexString(this.providerName.hashCode() % 1000);
            this.providerCode = "PROV_" + this.providerType + "_" + timestamp + "_" + nameHash;
        }
    }

    /**
     * 判断是否为临时数据源
     */
    public boolean isTemporary() {
        return this.isTemporary != null && this.isTemporary == 1;
    }

    /**
     * 判断是否需要缓存
     */
    public boolean needsCache() {
        return this.cacheEnabled != null && this.cacheEnabled == 1;
    }
}