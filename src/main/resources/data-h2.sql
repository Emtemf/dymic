-- ============================================================
-- 合同模板动态渲染系统 - H2 测试数据脚本
-- ============================================================
-- 版本: V1.0
-- 数据库: H2 (MySQL兼容模式)
-- 创建日期: 2026-06-07
-- 说明: 包含模板配置、数据提供方、查询配置、动作配置等测试数据
-- ============================================================


-- ============================================================
-- 第一部分：模板配置相关测试数据
-- ============================================================

-- 1. 模板主表数据（2个）
INSERT INTO t_ui_template
    (id, template_code, template_name, template_desc, biz_type, status, current_version_id, created_by, updated_by, created_at, updated_at, is_deleted)
VALUES
    (1001, 'SALE_CONTRACT', '销售合同模板', '用于销售业务合同录入', 'SALE', 'ENABLED', 2001, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (1002, 'PURCHASE_CONTRACT', '采购合同模板', '用于采购业务合同录入', 'PURCHASE', 'ENABLED', 2003, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 2. 模板版本表数据（3个）
INSERT INTO t_ui_template_version
    (id, template_id, version_no, version_name, version_status, publish_time, publish_by, schema_hash, remark, created_by, updated_by, created_at, updated_at, is_deleted)
VALUES
    (2001, 1001, 1, 'V1.0 初始版本', 'PUBLISHED', CURRENT_TIMESTAMP, 1, 'hash_v1_1001', '销售合同模板初始发布版本', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2002, 1001, 2, 'V2.0 增强版本', 'DRAFT', NULL, NULL, 'hash_v2_1001', '销售合同模板草稿版本', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (2003, 1002, 1, 'V1.0 初始版本', 'PUBLISHED', CURRENT_TIMESTAMP, 1, 'hash_v1_1002', '采购合同模板初始发布版本', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 3. 布局节点树数据（12个）
INSERT INTO t_ui_layout_node
    (id, template_id, template_version_id, parent_id, node_code, node_name, node_type, sort_no, level_no, node_path, grid_x, grid_y, grid_w, grid_h, row_no, col_no, col_span, row_span, bind_type, bind_ref_id, visible_rule, readonly_rule, props_json, created_by, updated_by, created_at, updated_at, is_deleted)
VALUES
    -- 3001: PAGE 根节点
    (3001, 1001, 2001, NULL, 'PAGE_ROOT', '销售合同页面', 'PAGE', 0, 1, '/PAGE_ROOT', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, JSON '{"title": "销售合同录入"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 3002: CARD 基本信息卡片
    (3002, 1001, 2001, 3001, 'CARD_BASIC', '基本信息', 'CARD', 0, 2, '/PAGE_ROOT/CARD_BASIC', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, JSON '{"title": "基本信息", "collapsible": true}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 3003: GRID 栅格
    (3003, 1001, 2001, 3002, 'GRID_BASIC', '基本信息栅格', 'GRID', 0, 3, '/PAGE_ROOT/CARD_BASIC/GRID_BASIC', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, JSON '{"columns": 2}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 3004: FIELD 字段节点 - 合同编号
    (3004, 1001, 2001, 3003, 'FIELD_CONTRACT_NO', '合同编号', 'FIELD', 0, 4, '/PAGE_ROOT/CARD_BASIC/GRID_BASIC/FIELD_CONTRACT_NO', NULL, NULL, NULL, NULL, 0, 0, 1, 1, 'FIELD_DEF', 4001, NULL, NULL, JSON '{"labelWidth": 120}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 3005: FIELD 字段节点 - 合同名称
    (3005, 1001, 2001, 3003, 'FIELD_CONTRACT_NAME', '合同名称', 'FIELD', 1, 4, '/PAGE_ROOT/CARD_BASIC/GRID_BASIC/FIELD_CONTRACT_NAME', NULL, NULL, NULL, NULL, 0, 1, 1, 1, 'FIELD_DEF', 4002, NULL, NULL, JSON '{"labelWidth": 120}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 3006: FIELD 字段节点 - 签订日期
    (3006, 1001, 2001, 3003, 'FIELD_SIGN_DATE', '签订日期', 'FIELD', 2, 4, '/PAGE_ROOT/CARD_BASIC/GRID_BASIC/FIELD_SIGN_DATE', NULL, NULL, NULL, NULL, 1, 0, 1, 1, 'FIELD_DEF', 4003, NULL, NULL, JSON '{"labelWidth": 120}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 3007: FIELD 字段节点 - 供应商
    (3007, 1001, 2001, 3003, 'FIELD_SUPPLIER', '供应商', 'FIELD', 3, 4, '/PAGE_ROOT/CARD_BASIC/GRID_BASIC/FIELD_SUPPLIER', NULL, NULL, NULL, NULL, 1, 1, 1, 1, 'FIELD_DEF', 4004, NULL, NULL, JSON '{"labelWidth": 120}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 3008: CARD 明细信息
    (3008, 1001, 2001, 3001, 'CARD_DETAIL', '明细信息', 'CARD', 1, 2, '/PAGE_ROOT/CARD_DETAIL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, JSON '{"title": "明细信息", "collapsible": false}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 3009: DETAIL_TABLE 明细表
    (3009, 1001, 2001, 3008, 'DETAIL_TABLE_ITEMS', '明细表', 'DETAIL_TABLE', 0, 3, '/PAGE_ROOT/CARD_DETAIL/DETAIL_TABLE_ITEMS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'DETAIL_TABLE', 3009, NULL, NULL, JSON '{"showIndex": true, "stripe": true}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 3010: CARD 操作区域
    (3010, 1001, 2001, 3001, 'CARD_ACTIONS', '操作区域', 'CARD', 2, 2, '/PAGE_ROOT/CARD_ACTIONS', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, JSON '{"title": "操作", "bordered": false}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 3011: ACTION 保存按钮
    (3011, 1001, 2001, 3010, 'ACTION_SAVE', '保存按钮', 'ACTION', 0, 3, '/PAGE_ROOT/CARD_ACTIONS/ACTION_SAVE', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ACTION_CONFIG', 7001, NULL, NULL, JSON '{"buttonType": "primary", "icon": "save"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 3012: ACTION 查询按钮
    (3012, 1001, 2001, 3010, 'ACTION_QUERY', '查询按钮', 'ACTION', 1, 3, '/PAGE_ROOT/CARD_ACTIONS/ACTION_QUERY', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'ACTION_CONFIG', 7002, NULL, NULL, JSON '{"buttonType": "default", "icon": "search"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 4. 明细表配置数据（1个）
INSERT INTO t_ui_detail_table
    (id, template_id, template_version_id, detail_code, detail_name, detail_path, row_key_strategy, min_rows, max_rows, allow_add, allow_edit, allow_delete, delete_mode, props_json, created_by, updated_by, created_at, updated_at, is_deleted)
VALUES
    (3009, 1001, 2001, 'ITEMS', '合同明细', 'items', 'CLIENT_UUID', 0, 100, 1, 1, 1, 'MARK_IN_DRAFT', JSON '{"showTotal": true, "showSerialNumber": true}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 5. 字段定义表数据（10个）
INSERT INTO t_ui_field_def
    (id, template_id, template_version_id, detail_table_id, field_code, field_path, field_name_cn, field_name_en, data_type, value_type, required_default, searchable, indexable, search_index_column, default_value, validate_rule, props_json, created_by, updated_by, created_at, updated_at, is_deleted)
VALUES
    -- 4001-4006: 主表字段
    (4001, 1001, 2001, NULL, 'CONTRACT_NO', 'basic.contractNo', '合同编号', 'contractNo', 'STRING', 'SINGLE', 1, 1, 1, 'contract_no', NULL, JSON '{"pattern": "^[A-Z0-9-]+$", "maxLength": 50}', JSON '{"placeholder": "请输入合同编号"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4002, 1001, 2001, NULL, 'CONTRACT_NAME', 'basic.contractName', '合同名称', 'contractName', 'STRING', 'SINGLE', 1, 1, 1, 'contract_name', NULL, JSON '{"maxLength": 200}', JSON '{"placeholder": "请输入合同名称"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4003, 1001, 2001, NULL, 'SIGN_DATE', 'basic.signDate', '签订日期', 'signDate', 'DATE', 'SINGLE', 1, 1, 1, 'sign_date', NULL, NULL, JSON '{"placeholder": "请选择签订日期"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4004, 1001, 2001, NULL, 'SUPPLIER_NAME', 'basic.supplierName', '供应商名称', 'supplierName', 'STRING', 'SINGLE', 0, 1, 1, 'supplier_name', NULL, JSON '{"maxLength": 200}', JSON '{"placeholder": "请选择供应商"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4005, 1001, 2001, NULL, 'TOTAL_AMOUNT', 'basic.totalAmount', '合同金额', 'totalAmount', 'DECIMAL', 'SINGLE', 0, 1, 1, 'total_amount', NULL, JSON '{"min": 0, "max": 999999999.99, "precision": 2}', JSON '{"placeholder": "请输入合同金额"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4006, 1001, 2001, NULL, 'REMARK', 'basic.remark', '备注', 'remark', 'STRING', 'SINGLE', 0, 0, 0, NULL, NULL, JSON '{"maxLength": 500}', JSON '{"placeholder": "请输入备注"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 4007-4010: 明细表字段
    (4007, 1001, 2001, 3009, 'ITEM_NAME', 'items[].itemName', '明细名称', 'itemName', 'STRING', 'ARRAY', 1, 0, 0, NULL, NULL, JSON '{"maxLength": 200}', JSON '{"placeholder": "请输入明细名称"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4008, 1001, 2001, 3009, 'ITEM_QUANTITY', 'items[].quantity', '数量', 'quantity', 'DECIMAL', 'ARRAY', 1, 0, 0, NULL, NULL, JSON '{"min": 0, "max": 999999.99, "precision": 2}', JSON '{"placeholder": "请输入数量"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4009, 1001, 2001, 3009, 'ITEM_UNIT_PRICE', 'items[].unitPrice', '单价', 'unitPrice', 'DECIMAL', 'ARRAY', 1, 0, 0, NULL, NULL, JSON '{"min": 0, "max": 999999.99, "precision": 2}', JSON '{"placeholder": "请输入单价"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (4010, 1001, 2001, 3009, 'ITEM_AMOUNT', 'items[].amount', '金额', 'amount', 'DECIMAL', 'ARRAY', 0, 0, 0, NULL, NULL, JSON '{"min": 0, "max": 999999.99, "precision": 2}', JSON '{"placeholder": "金额（自动计算）"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 6. 字段组件绑定表数据（10个）
INSERT INTO t_ui_field_component
    (id, template_id, template_version_id, layout_node_id, field_def_id, component_type, label_name, placeholder, required_rule, readonly_rule, visible_rule, component_props, data_provider_id, sort_no, created_by, updated_by, created_at, updated_at, is_deleted)
VALUES
    -- 4001-4006: 主表字段组件绑定
    (5001, 1001, 2001, 3004, 4001, 'INPUT', '合同编号', '请输入合同编号', NULL, NULL, NULL, JSON '{"maxLength": 50, "showWordLimit": true}', NULL, 0, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5002, 1001, 2001, 3005, 4002, 'INPUT', '合同名称', '请输入合同名称', NULL, NULL, NULL, JSON '{"maxLength": 200, "showWordLimit": true}', NULL, 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5003, 1001, 2001, 3006, 4003, 'DATE_PICKER', '签订日期', '请选择签订日期', NULL, NULL, NULL, JSON '{"format": "YYYY-MM-DD", "valueFormat": "YYYY-MM-DD"}', NULL, 2, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5004, 1001, 2001, 3007, 4004, 'SELECT', '供应商', '请选择供应商', NULL, NULL, NULL, JSON '{"filterable": true, "remote": true}', 5001, 3, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5005, 1001, 2001, NULL, 4005, 'INPUT_NUMBER', '合同金额', '请输入合同金额', NULL, NULL, NULL, JSON '{"min": 0, "max": 999999999.99, "precision": 2}', NULL, 4, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5006, 1001, 2001, NULL, 4006, 'TEXTAREA', '备注', '请输入备注', NULL, NULL, NULL, JSON '{"rows": 3, "maxLength": 500}', NULL, 5, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 4007-4010: 明细表字段组件绑定
    (5007, 1001, 2001, NULL, 4007, 'INPUT', '明细名称', '请输入明细名称', NULL, NULL, NULL, JSON '{"maxLength": 200}', NULL, 0, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5008, 1001, 2001, NULL, 4008, 'INPUT_NUMBER', '数量', '请输入数量', NULL, NULL, NULL, JSON '{"min": 0, "max": 999999.99, "precision": 2}', NULL, 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5009, 1001, 2001, NULL, 4009, 'INPUT_NUMBER', '单价', '请输入单价', NULL, NULL, NULL, JSON '{"min": 0, "max": 999999.99, "precision": 2}', NULL, 2, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (5010, 1001, 2001, NULL, 4010, 'INPUT_NUMBER', '金额', '金额（自动计算）', NULL, NULL, NULL, JSON '{"min": 0, "max": 999999.99, "precision": 2, "disabled": true}', NULL, 3, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


-- ============================================================
-- 第二部分：数据提供方相关测试数据
-- ============================================================

-- 7. 数据提供方配置表数据（2个）
-- Note: H2 uses simplified schema for t_ui_data_provider (no separate request_mapping_json/response_mapping_json)
INSERT INTO t_ui_data_provider
    (id, provider_code, provider_name, provider_type, config_json, cache_enabled, cache_ttl_seconds, is_temporary, status, created_by, updated_by, created_at, updated_at, is_deleted)
VALUES
    -- 5001: 供应商查询数据源
    (5001, 'SUPPLIER_QUERY', '供应商查询', 'HTTP',
     JSON '{"url": "http://api.example.com/supplier/query", "method": "POST", "timeout": 5000, "requestMapping": {"pageNum": "$.pageNum", "pageSize": "$.pageSize", "supplierName": "$.supplierName"}, "responseMapping": {"list": "$.data.list", "total": "$.data.total"}}',
     1, 300, 0, 'ENABLED', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 5002: 合同类型数据源
    (5002, 'CONTRACT_TYPE', '合同类型', 'STATIC',
     JSON '{"options": [{"value": "PURCHASE", "label": "采购合同"}, {"value": "SALE", "label": "销售合同"}, {"value": "LEASE", "label": "租赁合同"}]}',
     1, 86400, 0, 'ENABLED', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- Note: H2 schema uses simplified t_ui_data_provider with config_json, no separate t_ui_data_option table needed


-- ============================================================
-- 第三部分：查询配置相关测试数据
-- ============================================================

-- 8. 查询配置表数据（1个）
INSERT INTO t_ui_query_config
    (id, template_id, template_version_id, query_code, query_name, query_type, data_provider_id, trigger_type, result_mode, bind_node_id, page_size, props_json, created_by, updated_by, created_at, updated_at, is_deleted)
VALUES
    -- 6001: 供应商查询配置
    (6001, 1001, 2001, 'SUPPLIER_QUERY', '供应商查询', 'REMOTE_SEARCH', 5001, 'MANUAL', 'SINGLE_SELECT', 3007, 20, JSON '{"title": "供应商查询", "width": "800px"}', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 9. 查询参数绑定表数据（为供应商查询配置参数）
INSERT INTO t_ui_query_param
    (id, query_config_id, param_name, param_label, bind_source, bind_path, component_type, required, default_value, sort_no, created_by, updated_by, created_at, updated_at, is_deleted)
VALUES
    (9001, 6001, 'supplierName', '供应商名称', 'USER_INPUT', NULL, 'INPUT', 0, NULL, 0, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (9002, 6001, 'pageNum', '页码', 'FIXED', NULL, NULL, 0, '1', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (9003, 6001, 'pageSize', '每页数量', 'FIXED', NULL, NULL, 0, '20', 2, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 10. 查询结果回填规则表数据（为供应商查询配置回填规则）
INSERT INTO t_ui_query_fill_rule
    (id, query_config_id, source_field, target_scope, target_path, fill_mode, transform_json, sort_no, created_by, updated_by, created_at, updated_at, is_deleted)
VALUES
    (10001, 6001, 'supplierName', 'FORM', 'basic.supplierName', 'OVERWRITE', NULL, 0, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (10002, 6001, 'supplierId', 'FORM', 'basic.supplierId', 'OVERWRITE', NULL, 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    (10003, 6001, 'address', 'FORM', 'basic.supplierAddress', 'OVERWRITE', NULL, 2, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


-- ============================================================
-- 第四部分：动作配置相关测试数据
-- ============================================================

-- 11. 动作配置表数据（2个）
INSERT INTO t_ui_action_config
    (id, template_id, template_version_id, action_code, action_name, action_type, bind_node_id, bind_query_id, confirm_required, confirm_text, before_rule, after_rule, props_json, sort_no, created_by, updated_by, created_at, updated_at, is_deleted)
VALUES
    -- 7001: 保存按钮
    (7001, 1001, 2001, 'SAVE_CONTRACT', '保存合同', 'SAVE_CONTRACT', 3011, NULL, 0, NULL, NULL, NULL, JSON '{"apiPath": "/api/contract/save", "method": "POST"}', 0, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

    -- 7002: 查询按钮
    (7002, 1001, 2001, 'QUERY_SUPPLIER', '查询供应商', 'QUERY', 3012, 6001, 0, NULL, NULL, NULL, JSON '{"modalTitle": "供应商查询"}', 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);


-- ============================================================
-- 测试数据插入完成
-- ============================================================

-- 统计信息
-- 模板数据：2个
-- 版本数据：3个
-- 布局节点：12个
-- 明细表配置：1个
-- 字段定义：10个
-- 字段组件绑定：10个
-- 数据提供方：2个
-- 查询配置：1个
-- 查询参数：3个
-- 查询回填规则：3个
-- 动作配置：2个
