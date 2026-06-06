package com.contract.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 拼音转换工具测试
 */
class PinyinUtilsTest {

    @Test
    void testToPinyinCamelCase() {
        // 测试驼峰格式转换（完整拼音）
        assertEquals("jibenxinxi", PinyinUtils.toPinyinCamelCase("基本信息"));
        assertEquals("hetongmingcheng", PinyinUtils.toPinyinCamelCase("合同名称"));
        assertEquals("", PinyinUtils.toPinyinCamelCase(""));
        assertEquals("", PinyinUtils.toPinyinCamelCase(null));
    }

    @Test
    void testToPinyinSnakeCase() {
        // 测试下划线格式转换（完整拼音）
        assertEquals("ji_ben_xin_xi", PinyinUtils.toPinyinSnakeCase("基本信息"));
        assertEquals("he_tong_ming_cheng", PinyinUtils.toPinyinSnakeCase("合同名称"));
        assertEquals("", PinyinUtils.toPinyinSnakeCase(""));
        assertEquals("", PinyinUtils.toPinyinSnakeCase(null));
    }

    @Test
    void testToPinyinAbbreviation() {
        // 测试首字母缩写格式
        assertEquals("jbxx", PinyinUtils.toPinyinAbbreviation("基本信息"));
        assertEquals("htmc", PinyinUtils.toPinyinAbbreviation("合同名称"));
        assertEquals("", PinyinUtils.toPinyinAbbreviation(""));
        assertEquals("", PinyinUtils.toPinyinAbbreviation(null));
    }

    @Test
    void testGenerateNodeCode() {
        // 测试节点编码生成（使用首字母缩写）
        assertEquals("jbxx", PinyinUtils.generateNodeCode("基本信息"));
        assertEquals("httk", PinyinUtils.generateNodeCode("合同条款"));
    }

    @Test
    void testGenerateNodePath() {
        // 测试节点路径生成
        assertEquals("jbxx", PinyinUtils.generateNodePath(null, "jbxx"));
        assertEquals("jbxx", PinyinUtils.generateNodePath("", "jbxx"));
        assertEquals("root.jbxx", PinyinUtils.generateNodePath("root", "jbxx"));
        assertEquals("root.jbxx.htmc",
            PinyinUtils.generateNodePath("root.jbxx", "htmc"));
    }

    @Test
    void testGenerateFieldCode() {
        // 测试字段编码生成（使用首字母缩写）
        assertEquals("htmc", PinyinUtils.generateFieldCode("合同名称"));
        assertEquals("htje", PinyinUtils.generateFieldCode("合同金额"));
    }

    @Test
    void testGenerateFieldPath() {
        // 测试字段路径生成
        assertEquals("htmc", PinyinUtils.generateFieldPath(null, "htmc"));
        assertEquals("jbxx.htmc",
            PinyinUtils.generateFieldPath("jbxx", "htmc"));
        assertEquals("root.jbxx.htmc",
            PinyinUtils.generateFieldPath("root.jbxx", "htmc"));
    }

    @Test
    void testGenerateActionCode() {
        // 测试动作编码生成（使用首字母缩写）
        assertEquals("bc", PinyinUtils.generateActionCode("保存"));
        assertEquals("cx", PinyinUtils.generateActionCode("查询"));
    }
}