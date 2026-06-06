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
        // 测试节点编码生成（使用驼峰格式，完整拼音）
        assertEquals("jibenxinxi", PinyinUtils.generateNodeCode("基本信息"));
        assertEquals("hetongtiaokuan", PinyinUtils.generateNodeCode("合同条款"));
    }

    @Test
    void testGenerateNodePath() {
        // 测试节点路径生成（使用驼峰格式编码）
        assertEquals("jibenxinxi", PinyinUtils.generateNodePath(null, "jibenxinxi"));
        assertEquals("jibenxinxi", PinyinUtils.generateNodePath("", "jibenxinxi"));
        assertEquals("root.jibenxinxi", PinyinUtils.generateNodePath("root", "jibenxinxi"));
        assertEquals("root.jibenxinxi.hetongmingcheng",
            PinyinUtils.generateNodePath("root.jibenxinxi", "hetongmingcheng"));
    }

    @Test
    void testGenerateFieldCode() {
        // 测试字段编码生成（使用驼峰格式，完整拼音）
        assertEquals("hetongmingcheng", PinyinUtils.generateFieldCode("合同名称"));
        assertEquals("hetongjine", PinyinUtils.generateFieldCode("合同金额"));
    }

    @Test
    void testGenerateFieldPath() {
        // 测试字段路径生成（使用驼峰格式编码）
        assertEquals("hetongmingcheng", PinyinUtils.generateFieldPath(null, "hetongmingcheng"));
        assertEquals("jibenxinxi.hetongmingcheng",
            PinyinUtils.generateFieldPath("jibenxinxi", "hetongmingcheng"));
        assertEquals("root.jibenxinxi.hetongmingcheng",
            PinyinUtils.generateFieldPath("root.jibenxinxi", "hetongmingcheng"));
    }

    @Test
    void testGenerateActionCode() {
        // 测试动作编码生成（使用驼峰格式，完整拼音）
        assertEquals("baocun", PinyinUtils.generateActionCode("保存"));
        assertEquals("chaxun", PinyinUtils.generateActionCode("查询"));
    }
}