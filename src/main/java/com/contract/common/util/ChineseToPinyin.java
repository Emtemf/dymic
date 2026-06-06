package com.contract.common.util;

import cn.hutool.extra.pinyin.PinyinUtil;

/**
 * 中文转拼音工具类
 * 用于自动生成字段编码、节点编码等技术字段
 */
public class ChineseToPinyin {

    /**
     * 将中文转换为拼音（首字母大写）
     * 例如: "合同名称" -> "HeTongMingCheng"
     *
     * @param chinese 中文文本
     * @return 拼音字符串
     */
    public static String toPinyin(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return "";
        }

        // 使用 hutool 的拼音工具
        String pinyin = PinyinUtil.getPinyin(chinese);

        // 转换为驼峰格式（每个单词首字母大写）
        StringBuilder result = new StringBuilder();
        String[] words = pinyin.split(" ");

        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                if (i == 0) {
                    // 第一个单词小写开头
                    result.append(words[i].toLowerCase());
                } else {
                    // 其他单词首字母大写
                    result.append(capitalize(words[i]));
                }
            }
        }

        return result.toString();
    }

    /**
     * 将中文转换为拼音首字母
     * 例如: "合同名称" -> "htmc"
     *
     * @param chinese 中文文本
     * @return 拼音首字母
     */
    public static String toFirstLetter(String chinese) {
        if (chinese == null || chinese.trim().isEmpty()) {
            return "";
        }

        return PinyinUtil.getFirstLetter(chinese, "");
    }

    /**
     * 首字母大写
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}