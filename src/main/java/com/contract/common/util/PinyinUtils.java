package com.contract.common.util;

import cn.hutool.extra.pinyin.engine.pinyin4j.Pinyin4jEngine;

/**
 * 拼音转换工具
 * 用于自动生成 nodeCode、fieldCode、actionCode
 */
public class PinyinUtils {

    private static final Pinyin4jEngine engine = new Pinyin4jEngine();

    /**
     * 中文转拼音（驼峰格式）
     *
     * 示例：
     * - "基本信息" → "jiBenXinXi"
     * - "合同名称" → "heTongMingCheng"
     *
     * @param chinese 中文字符串
     * @return 拼音驼峰格式
     */
    public static String toPinyinCamelCase(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return "";
        }

        // 使用Hutool拼音工具，获取完整拼音（分隔符为空）
        String pinyin = engine.getPinyin(chinese, "");

        // 转换为驼峰格式：首字母小写
        if (pinyin.length() > 0) {
            return pinyin.substring(0, 1).toLowerCase() + pinyin.substring(1);
        }

        return pinyin;
    }

    /**
     * 中文转拼音首字母（缩写格式）
     *
     * 示例：
     * - "基本信息" → "jbxx"
     * - "合同名称" → "htmc"
     *
     * @param chinese 中文字符串
     * @return 拼音首字母缩写（全小写）
     */
    public static String toPinyinAbbreviation(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return "";
        }

        // 使用Hutool拼音工具，获取拼音首字母，无分隔符
        return engine.getFirstLetter(chinese, "").toLowerCase();
    }

    /**
     * 中文转拼音（下划线格式）
     *
     * 示例：
     * - "基本信息" → "ji_ben_xin_xi"
     * - "合同名称" → "he_tong_ming_cheng"
     *
     * @param chinese 中文字符串
     * @return 拼音下划线格式
     */
    public static String toPinyinSnakeCase(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return "";
        }

        // 使用Hutool拼音工具，完整拼音，下划线分隔
        return engine.getPinyin(chinese, "_").toLowerCase();
    }

    /**
     * 生成节点编码（首字母缩写格式）
     *
     * @param nodeName 节点名称（中文）
     * @return nodeCode（拼音首字母缩写）
     */
    public static String generateNodeCode(String nodeName) {
        return toPinyinAbbreviation(nodeName);
    }

    /**
     * 生成节点路径（拼接父节点路径）
     *
     * @param parentNodePath 父节点路径
     * @param nodeCode 当前节点编码
     * @return nodePath（完整路径）
     */
    public static String generateNodePath(String parentNodePath, String nodeCode) {
        if (parentNodePath == null || parentNodePath.isEmpty()) {
            return nodeCode;
        }
        return parentNodePath + "." + nodeCode;
    }

    /**
     * 生成字段编码（首字母缩写格式）
     *
     * @param fieldName 字段名称（中文）
     * @return fieldCode（拼音首字母缩写）
     */
    public static String generateFieldCode(String fieldName) {
        return toPinyinAbbreviation(fieldName);
    }

    /**
     * 生成字段路径（拼接父节点路径）
     *
     * @param parentNodePath 父节点路径
     * @param fieldCode 字段编码
     * @return fieldPath（完整路径）
     */
    public static String generateFieldPath(String parentNodePath, String fieldCode) {
        if (parentNodePath == null || parentNodePath.isEmpty()) {
            return fieldCode;
        }
        return parentNodePath + "." + fieldCode;
    }

    /**
     * 生成动作编码（首字母缩写格式）
     *
     * @param actionName 动作名称（中文）
     * @return actionCode（拼音首字母缩写）
     */
    public static String generateActionCode(String actionName) {
        return toPinyinAbbreviation(actionName);
    }
}