# 合同模板动态渲染系统

## 项目愿景
合同模板动态渲染系统：配置驱动、动态渲染、数据比对

## 架构核心规则
1. 四层架构：Controller → Service → DomainService → Repository
2. 依赖倒置：接口在domain层，实现在infrastructure层

## 命名核心规则
Google风格：包名小写点分隔，类名大驼峰，方法名小驼峰动词开头

## 验证核心规则
分层验证：Repository(H2) → Service(Mock) → Controller(MockMvc) → 前端
验证优先级：模板配置→查询配置→明细表

## 技术栈核心规则
Java 21 + Spring Boot 3.5.14 + MyBatis-Plus 3.5.5 + MapStruct + Lombok

## 详细规则
查看 .claude/rules/ 目录下的规则文件（渐进式披露）