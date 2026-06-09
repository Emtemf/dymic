package com.contract.domain.template;

import com.contract.common.util.PinyinUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 字段定义实体
 *
 * ===== 领域统一业务语言 =====
 *
 * 【身份标识】
 * - 由 id 唯一标识
 *
 * 【业务规则】
 * 1. 字段路径唯一
 * 2. 字段类型必须有效
 * 3. fieldCode自动生成（拼音驼峰）
 * 4. fieldPath自动生成（父路径 + fieldCode）
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDef {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long detailTableId;       // 明细表ID（如果是明细表字段）

    private String fieldCode;         // 自动生成: heTongMingCheng
    private String fieldPath;         // 自动生成: basicInfo.heTongMingCheng
    private String fieldNameCn;       // 业务名称: 合同名称
    private String fieldNameEn;       // 英文名称（可选）

    private String dataType;          // TEXT/NUMBER/DATE/DATETIME/MONEY/BOOL/JSON
    private String valueType;         // SINGLE/ARRAY/OBJECT

    private Integer requiredDefault;  // 0=可选, 1=必填
    private Integer searchable;       // 是否支持搜索（0/1）
    private Integer indexable;        // 是否建立索引（0/1）
    private String searchIndexColumn; // 搜索索引列名

    private String defaultValue;      // 默认值
    private String validateRule;      // JSON格式验证规则
    private String propsJson;         // 扩展属性

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    /**
     * 自动生成字段编码
     * 规则：fieldNameCn（中文）→ fieldCode（拼音驼峰）
     * 示例："合同名称" → "heTongMingCheng"
     */
    public void generateFieldCode() {
        if (this.fieldNameCn != null && !this.fieldNameCn.trim().isEmpty()) {
            this.fieldCode = PinyinUtils.generateFieldCode(this.fieldNameCn);
        }
    }

    /**
     * 自动生成字段路径
     * 规则：parentNodePath + "." + fieldCode
     * 示例："basicInfo" + "." + "heTongMingCheng" → "basicInfo.heTongMingCheng"
     *
     * @param parentNodePath 父节点路径
     */
    public void generateFieldPath(String parentNodePath) {
        if (this.fieldCode != null) {
            this.fieldPath = PinyinUtils.generateFieldPath(parentNodePath, this.fieldCode);
        }
    }

    /**
     * 创建字段定义（工厂方法）
     */
    public static FieldDef create(String displayName, String dataType) {
        FieldDef fieldDef = new FieldDef();
        fieldDef.setFieldNameCn(displayName);
        fieldDef.setDataType(dataType);
        fieldDef.setValueType("SINGLE");
        fieldDef.setRequiredDefault(0);
        fieldDef.setSearchable(0);
        fieldDef.setIndexable(0);
        fieldDef.generateFieldCode();
        return fieldDef;
    }
}