-- =====================================================
-- 合同模板动态渲染系统 - H2 数据库初始化脚本
-- =====================================================

-- =====================================================
-- 模板配置侧表结构
-- =====================================================

-- 模板主表
CREATE TABLE t_ui_template (
    id BIGINT PRIMARY KEY,
    template_code VARCHAR(100) NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    template_desc VARCHAR(1000),
    biz_type VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    current_version_id BIGINT,
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_name VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_template_code UNIQUE (template_code)
);

COMMENT ON TABLE t_ui_template IS '模板主表';

-- 模板版本表
CREATE TABLE t_ui_template_version (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    version_no INTEGER NOT NULL,
    version_name VARCHAR(200),
    version_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    publish_time TIMESTAMP,
    publish_by BIGINT,
    schema_hash VARCHAR(128),
    remark VARCHAR(1000),
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_name VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_template_version UNIQUE (template_id, version_no)
);

CREATE INDEX idx_ui_template_version_status ON t_ui_template_version (template_id, version_status);

COMMENT ON TABLE t_ui_template_version IS '模板版本表';

-- 布局节点树
CREATE TABLE t_ui_layout_node (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    parent_id BIGINT,
    node_code VARCHAR(100) NOT NULL,
    node_name VARCHAR(200),
    node_type VARCHAR(50) NOT NULL,
    sort_no INTEGER NOT NULL DEFAULT 0,
    level_no INTEGER NOT NULL DEFAULT 1,
    node_path VARCHAR(1000),
    grid_x INTEGER,
    grid_y INTEGER,
    grid_w INTEGER,
    grid_h INTEGER,
    row_no INTEGER,
    col_no INTEGER,
    col_span INTEGER,
    row_span INTEGER,
    bind_type VARCHAR(50),
    bind_ref_id BIGINT,
    visible_rule JSON,
    readonly_rule JSON,
    props_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_layout_node_code UNIQUE (template_version_id, node_code)
);

CREATE INDEX idx_ui_layout_parent ON t_ui_layout_node (template_version_id, parent_id, sort_no);
CREATE INDEX idx_ui_layout_bind ON t_ui_layout_node (bind_type, bind_ref_id);

COMMENT ON TABLE t_ui_layout_node IS '布局节点树';

-- 字段定义表
CREATE TABLE t_ui_field_def (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    detail_table_id BIGINT,
    field_code VARCHAR(100) NOT NULL,
    field_path VARCHAR(500) NOT NULL,
    field_name_cn VARCHAR(200) NOT NULL,
    field_name_en VARCHAR(200),
    data_type VARCHAR(50) NOT NULL,
    value_type VARCHAR(50) NOT NULL DEFAULT 'SINGLE',
    required_default SMALLINT NOT NULL DEFAULT 0,
    searchable SMALLINT NOT NULL DEFAULT 0,
    indexable SMALLINT NOT NULL DEFAULT 0,
    search_index_column VARCHAR(100),
    default_value VARCHAR(1000),
    validate_rule JSON,
    props_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_field_path UNIQUE (template_version_id, field_path)
);

CREATE INDEX idx_ui_field_version ON t_ui_field_def (template_version_id);
CREATE INDEX idx_ui_field_detail ON t_ui_field_def (detail_table_id);

COMMENT ON TABLE t_ui_field_def IS '字段定义表';

-- 字段组件绑定表
CREATE TABLE t_ui_field_component (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    layout_node_id BIGINT NOT NULL,
    field_def_id BIGINT NOT NULL,
    component_type VARCHAR(50) NOT NULL,
    label_name VARCHAR(200),
    placeholder VARCHAR(300),
    required_rule JSON,
    readonly_rule JSON,
    visible_rule JSON,
    component_props JSON,
    data_provider_id BIGINT,
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_field_component UNIQUE (template_version_id, layout_node_id, field_def_id)
);

CREATE INDEX idx_ui_field_component_node ON t_ui_field_component (layout_node_id);
CREATE INDEX idx_ui_field_component_field ON t_ui_field_component (field_def_id);

COMMENT ON TABLE t_ui_field_component IS '字段组件绑定表';

-- 数据提供方配置（简化版，合并了 t_ui_data_option）
CREATE TABLE t_ui_data_provider (
    id BIGINT PRIMARY KEY,
    provider_code VARCHAR(100) NOT NULL,
    provider_name VARCHAR(200) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    config_json JSON NOT NULL,
    cache_enabled SMALLINT NOT NULL DEFAULT 0,
    cache_ttl_seconds INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_name VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_data_provider_code UNIQUE (provider_code)
);

COMMENT ON TABLE t_ui_data_provider IS '数据提供方配置';

-- =====================================================
-- 合同数据侧表结构
-- =====================================================

-- 合同主表
CREATE TABLE t_contract (
    id BIGINT PRIMARY KEY,
    contract_no VARCHAR(100),
    contract_name VARCHAR(300),
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    current_snapshot_id BIGINT,
    contract_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    data_version INTEGER NOT NULL DEFAULT 0,
    source_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    source_system_code VARCHAR(100),
    source_biz_id VARCHAR(200),
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_name VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_contract_template ON t_contract (template_id, template_version_id);
CREATE INDEX idx_contract_no ON t_contract (contract_no);
CREATE INDEX idx_contract_source ON t_contract (source_system_code, source_biz_id);

COMMENT ON TABLE t_contract IS '合同主表';

-- 合同快照
CREATE TABLE t_contract_data_snapshot (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    snapshot_no INTEGER NOT NULL,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    canonical_data JSON NOT NULL,
    source_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    source_message_id BIGINT,
    save_reason VARCHAR(500),
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_contract_snapshot_no UNIQUE (contract_id, snapshot_no)
);

CREATE INDEX idx_contract_snapshot_contract ON t_contract_data_snapshot (contract_id, snapshot_no DESC);

COMMENT ON TABLE t_contract_data_snapshot IS '合同完整JSON快照';

-- 字段值索引
CREATE TABLE t_contract_field_value (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    field_def_id BIGINT NOT NULL,
    field_path VARCHAR(500) NOT NULL,
    value_text CLOB,
    value_number DECIMAL(24, 6),
    value_date DATE,
    value_datetime TIMESTAMP,
    value_bool SMALLINT,
    value_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_contract_field_text ON t_contract_field_value (field_path, value_text);
CREATE INDEX idx_contract_field_number ON t_contract_field_value (field_path, value_number);
CREATE INDEX idx_contract_field_date ON t_contract_field_value (field_path, value_date);
CREATE INDEX idx_contract_field_contract ON t_contract_field_value (contract_id, snapshot_id);

COMMENT ON TABLE t_contract_field_value IS '合同字段值索引表';

-- 明细行当前投影
CREATE TABLE t_contract_detail_row (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    detail_table_id BIGINT NOT NULL,
    detail_code VARCHAR(100) NOT NULL,
    row_uid VARCHAR(100) NOT NULL,
    row_no INTEGER NOT NULL,
    row_data JSON NOT NULL,
    row_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_contract_detail_row_uid UNIQUE (contract_id, detail_code, row_uid)
);

CREATE INDEX idx_contract_detail_row ON t_contract_detail_row (contract_id, detail_code, row_no);

COMMENT ON TABLE t_contract_detail_row IS '合同明细行当前投影';

-- 明细字段索引
CREATE TABLE t_contract_detail_field_value (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    detail_row_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    detail_code VARCHAR(100) NOT NULL,
    row_uid VARCHAR(100) NOT NULL,
    field_def_id BIGINT NOT NULL,
    field_path VARCHAR(500) NOT NULL,
    value_text CLOB,
    value_number DECIMAL(24, 6),
    value_date DATE,
    value_datetime TIMESTAMP,
    value_bool SMALLINT,
    value_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_detail_field_text ON t_contract_detail_field_value (detail_code, field_path, value_text);
CREATE INDEX idx_detail_field_number ON t_contract_detail_field_value (detail_code, field_path, value_number);
CREATE INDEX idx_detail_field_contract ON t_contract_detail_field_value (contract_id, detail_code);

COMMENT ON TABLE t_contract_detail_field_value IS '明细字段索引表';

-- 当前查询宽表
CREATE TABLE t_contract_search_index (
    contract_id BIGINT PRIMARY KEY,
    contract_no VARCHAR(100),
    contract_name VARCHAR(300),
    supplier_id VARCHAR(100),
    supplier_name VARCHAR(300),
    contract_type VARCHAR(100),
    total_amount DECIMAL(24, 6),
    currency VARCHAR(20),
    sign_date DATE,
    effective_date DATE,
    expire_date DATE,
    contract_status VARCHAR(50),
    source_system_code VARCHAR(100),
    current_snapshot_id BIGINT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contract_search_supplier ON t_contract_search_index (supplier_name);
CREATE INDEX idx_contract_search_status ON t_contract_search_index (contract_status);
CREATE INDEX idx_contract_search_date ON t_contract_search_index (sign_date);

COMMENT ON TABLE t_contract_search_index IS '当前合同查询宽表';

-- 附件表
CREATE TABLE t_contract_attachment (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    snapshot_id BIGINT,
    file_id VARCHAR(200) NOT NULL,
    file_name VARCHAR(300) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    biz_path VARCHAR(500),
    created_by BIGINT,
    created_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_contract_attachment ON t_contract_attachment (contract_id, biz_path);

COMMENT ON TABLE t_contract_attachment IS '合同附件表';

-- =====================================================
-- 查询配置表结构
-- =====================================================

-- 查询配置
CREATE TABLE t_ui_query_config (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    query_code VARCHAR(100) NOT NULL,
    query_name VARCHAR(200) NOT NULL,
    query_type VARCHAR(50) NOT NULL,
    data_provider_id BIGINT NOT NULL,
    trigger_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    result_mode VARCHAR(50) NOT NULL DEFAULT 'SINGLE_SELECT',
    bind_node_id BIGINT,
    page_size INTEGER DEFAULT 20,
    props_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_query_code UNIQUE (template_version_id, query_code)
);

CREATE INDEX idx_ui_query_version ON t_ui_query_config (template_version_id);
CREATE INDEX idx_ui_query_provider ON t_ui_query_config (data_provider_id);

COMMENT ON TABLE t_ui_query_config IS '查询配置表';

-- 查询参数绑定
CREATE TABLE t_ui_query_param (
    id BIGINT PRIMARY KEY,
    query_config_id BIGINT NOT NULL,
    param_name VARCHAR(100) NOT NULL,
    param_label VARCHAR(200),
    bind_source VARCHAR(50) NOT NULL,
    bind_path VARCHAR(500),
    component_type VARCHAR(50),
    required SMALLINT NOT NULL DEFAULT 0,
    default_value VARCHAR(1000),
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_query_param UNIQUE (query_config_id, param_name)
);

COMMENT ON TABLE t_ui_query_param IS '查询参数绑定表';

-- 查询回填规则
CREATE TABLE t_ui_query_fill_rule (
    id BIGINT PRIMARY KEY,
    query_config_id BIGINT NOT NULL,
    source_field VARCHAR(300) NOT NULL,
    target_scope VARCHAR(50) NOT NULL DEFAULT 'FORM',
    target_path VARCHAR(500) NOT NULL,
    fill_mode VARCHAR(50) NOT NULL DEFAULT 'OVERWRITE',
    transform_json JSON,
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ui_query_fill_rule ON t_ui_query_fill_rule (query_config_id, sort_no);

COMMENT ON TABLE t_ui_query_fill_rule IS '查询结果回填规则配置';

-- 明细表配置
CREATE TABLE t_ui_detail_table (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    detail_code VARCHAR(100) NOT NULL,
    detail_name VARCHAR(200) NOT NULL,
    detail_path VARCHAR(500) NOT NULL,
    row_key_strategy VARCHAR(50) NOT NULL DEFAULT 'CLIENT_UUID',
    min_rows INTEGER DEFAULT 0,
    max_rows INTEGER,
    allow_add SMALLINT NOT NULL DEFAULT 1,
    allow_edit SMALLINT NOT NULL DEFAULT 1,
    allow_delete SMALLINT NOT NULL DEFAULT 1,
    delete_mode VARCHAR(50) NOT NULL DEFAULT 'MARK_IN_DRAFT',
    props_json JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_detail_code UNIQUE (template_version_id, detail_code)
);

COMMENT ON TABLE t_ui_detail_table IS '明细表配置';

-- 动作配置
CREATE TABLE t_ui_action_config (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    action_code VARCHAR(100) NOT NULL,
    action_name VARCHAR(200) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    bind_node_id BIGINT,
    bind_query_id BIGINT,
    confirm_required SMALLINT NOT NULL DEFAULT 0,
    confirm_text VARCHAR(500),
    before_rule JSON,
    after_rule JSON,
    props_json JSON,
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_action_code UNIQUE (template_version_id, action_code)
);

COMMENT ON TABLE t_ui_action_config IS '动作配置表';

-- =====================================================
-- 外部集成表结构
-- =====================================================

-- 外部系统表
CREATE TABLE t_ext_system (
    id BIGINT PRIMARY KEY,
    system_code VARCHAR(100) NOT NULL,
    system_name VARCHAR(200) NOT NULL,
    system_type VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ext_system_code UNIQUE (system_code)
);

COMMENT ON TABLE t_ext_system IS '外部系统登记表';

-- 外部消息入库
CREATE TABLE t_ext_message_inbox (
    id BIGINT PRIMARY KEY,
    ext_system_id BIGINT NOT NULL,
    external_msg_id VARCHAR(200) NOT NULL,
    source_biz_id VARCHAR(200),
    message_type VARCHAR(100) NOT NULL,
    raw_payload JSON NOT NULL,
    mapped_data JSON,
    mapping_id BIGINT,
    process_status VARCHAR(50) NOT NULL DEFAULT 'RECEIVED',
    error_message CLOB,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ext_message UNIQUE (ext_system_id, external_msg_id)
);

CREATE INDEX idx_ext_message_status ON t_ext_message_inbox (process_status, received_at);

COMMENT ON TABLE t_ext_message_inbox IS '外部消息幂等入库表';

-- 外部映射主表
CREATE TABLE t_ext_data_mapping (
    id BIGINT PRIMARY KEY,
    ext_system_id BIGINT NOT NULL,
    mapping_code VARCHAR(100) NOT NULL,
    mapping_name VARCHAR(200) NOT NULL,
    message_type VARCHAR(100) NOT NULL,
    target_template_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    sample_payload JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ext_mapping_code UNIQUE (ext_system_id, mapping_code)
);

COMMENT ON TABLE t_ext_data_mapping IS '外部数据映射主表';

-- 外部字段映射
CREATE TABLE t_ext_data_mapping_field (
    id BIGINT PRIMARY KEY,
    mapping_id BIGINT NOT NULL,
    source_path VARCHAR(500) NOT NULL,
    target_path VARCHAR(500) NOT NULL,
    data_type VARCHAR(50),
    required SMALLINT NOT NULL DEFAULT 0,
    transform_rule JSON,
    default_value VARCHAR(1000),
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ext_mapping_field ON t_ext_data_mapping_field (mapping_id, sort_no);

COMMENT ON TABLE t_ext_data_mapping_field IS '外部字段映射表';

-- 外部对比记录
CREATE TABLE t_ext_compare_record (
    id BIGINT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    contract_id BIGINT,
    compare_type VARCHAR(50) NOT NULL DEFAULT 'EXTERNAL_TO_CURRENT',
    compare_status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    external_data JSON NOT NULL,
    current_data JSON,
    diff_summary JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_ext_compare_record IS '外部数据对比记录';

-- 外部对比明细
CREATE TABLE t_ext_compare_item (
    id BIGINT PRIMARY KEY,
    compare_record_id BIGINT NOT NULL,
    field_path VARCHAR(500) NOT NULL,
    field_name_cn VARCHAR(200),
    external_value CLOB,
    current_value CLOB,
    diff_type VARCHAR(50) NOT NULL,
    accept_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ext_compare_item ON t_ext_compare_item (compare_record_id, field_path);

COMMENT ON TABLE t_ext_compare_item IS '外部数据对比明细';
