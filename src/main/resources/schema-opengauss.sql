-- ============================================================
-- 合同模板动态渲染系统 - openGauss 数据库建表脚本
-- ============================================================
-- 版本: V1.0
-- 数据库: openGauss
-- 创建日期: 2026-06-07
-- 说明: 包含模板配置、数据提供方、合同数据、外部集成四大部分
-- ============================================================


-- ============================================================
-- 第一部分：模板配置相关表
-- ============================================================

-- 1. 模板主表
-- 说明：表示一个业务模板，不直接存布局内容
CREATE TABLE t_ui_template (
    id BIGINT PRIMARY KEY,
    template_code VARCHAR(100) NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    template_desc VARCHAR(1000),
    biz_type VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    current_version_id BIGINT,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_template_code UNIQUE (template_code)
);

COMMENT ON TABLE t_ui_template IS '模板主表，表示一个业务模板，不直接存布局内容';
COMMENT ON COLUMN t_ui_template.id IS '主键，雪花ID';
COMMENT ON COLUMN t_ui_template.template_code IS '模板编码，全局唯一，例如 PURCHASE_CONTRACT';
COMMENT ON COLUMN t_ui_template.current_version_id IS '当前发布版本ID，指向t_ui_template_version.id';

-- 2. 模板版本表
-- 说明：草稿、发布和历史版本都在这里
CREATE TABLE t_ui_template_version (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    version_no INTEGER NOT NULL,
    version_name VARCHAR(200),
    version_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    publish_time TIMESTAMPTZ,
    publish_by BIGINT,
    schema_hash VARCHAR(128),
    remark VARCHAR(1000),
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_template_version UNIQUE (template_id, version_no)
);

CREATE INDEX idx_ui_template_version_status ON t_ui_template_version (template_id, version_status);

COMMENT ON TABLE t_ui_template_version IS '模板版本表，草稿、发布和历史版本都在这里';
COMMENT ON COLUMN t_ui_template_version.version_status IS '版本状态：DRAFT草稿、PUBLISHED已发布、DISABLED停用';

-- 3. 布局节点树
-- 说明：表达页面、容器、卡片、Tab、字段位置、明细表、弹窗等
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

    visible_rule JSONB,
    readonly_rule JSONB,
    props_json JSONB,

    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_layout_node_code UNIQUE (template_version_id, node_code)
);

CREATE INDEX idx_ui_layout_parent ON t_ui_layout_node (template_version_id, parent_id, sort_no);
CREATE INDEX idx_ui_layout_bind ON t_ui_layout_node (bind_type, bind_ref_id);

COMMENT ON TABLE t_ui_layout_node IS '布局节点树，表达页面、容器、卡片、Tab、字段位置、明细表、弹窗等';
COMMENT ON COLUMN t_ui_layout_node.node_type IS '节点类型：PAGE、GRID、ROW、COL、CARD、TABS、TAB_PANE、MODAL、FIELD、DETAIL_TABLE、QUERY、ACTION等';
COMMENT ON COLUMN t_ui_layout_node.bind_type IS '绑定类型：FIELD_DEF、DETAIL_TABLE、QUERY_CONFIG、ACTION_CONFIG、CUSTOM_COMPONENT';

-- 4. 字段定义表
-- 说明：描述合同统一数据里的字段路径、类型和查询索引属性
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
    validate_rule JSONB,
    props_json JSONB,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_field_path UNIQUE (template_version_id, field_path)
);

CREATE INDEX idx_ui_field_version ON t_ui_field_def (template_version_id);
CREATE INDEX idx_ui_field_detail ON t_ui_field_def (detail_table_id);

COMMENT ON TABLE t_ui_field_def IS '字段定义表，描述合同统一数据里的字段路径、类型和查询索引属性';
COMMENT ON COLUMN t_ui_field_def.field_path IS '统一数据路径，例如 basic.contractNo、items[].itemName';
COMMENT ON COLUMN t_ui_field_def.detail_table_id IS '如果字段属于明细表，关联t_ui_detail_table.id';
COMMENT ON COLUMN t_ui_field_def.searchable IS '是否允许作为查询条件';
COMMENT ON COLUMN t_ui_field_def.indexable IS '保存合同时是否写入字段索引表';

-- 5. 字段组件绑定表
-- 说明：定义某个布局节点上的字段用什么组件展示
CREATE TABLE t_ui_field_component (
    id BIGINT PRIMARY KEY,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    layout_node_id BIGINT NOT NULL,
    field_def_id BIGINT NOT NULL,
    component_type VARCHAR(50) NOT NULL,
    label_name VARCHAR(200),
    placeholder VARCHAR(300),
    required_rule JSONB,
    readonly_rule JSONB,
    visible_rule JSONB,
    component_props JSONB,
    data_provider_id BIGINT,
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_field_component UNIQUE (template_version_id, layout_node_id, field_def_id)
);

CREATE INDEX idx_ui_field_component_node ON t_ui_field_component (layout_node_id);
CREATE INDEX idx_ui_field_component_field ON t_ui_field_component (field_def_id);

COMMENT ON TABLE t_ui_field_component IS '字段组件绑定表，定义某个布局节点上的字段用什么组件展示';
COMMENT ON COLUMN t_ui_field_component.data_provider_id IS '下拉、远程搜索等组件可直接引用数据提供方';

-- 6. 明细表配置
-- 说明：定义合同JSON中的数组区域和增删改行为
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
    props_json JSONB,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_detail_code UNIQUE (template_version_id, detail_code)
);

COMMENT ON TABLE t_ui_detail_table IS '明细表配置，定义合同JSON中的数组区域和增删改行为';
COMMENT ON COLUMN t_ui_detail_table.detail_path IS '明细数组路径，例如 items';
COMMENT ON COLUMN t_ui_detail_table.delete_mode IS '删除模式，V1.0使用MARK_IN_DRAFT，最终保存时过滤或归档';

-- 7. 查询配置表
-- 说明：定义某个查询组件如何取数
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
    props_json JSONB,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_query_code UNIQUE (template_version_id, query_code)
);

CREATE INDEX idx_ui_query_version ON t_ui_query_config (template_version_id);
CREATE INDEX idx_ui_query_provider ON t_ui_query_config (data_provider_id);

COMMENT ON TABLE t_ui_query_config IS '查询配置表，定义某个查询组件如何取数';
COMMENT ON COLUMN t_ui_query_config.data_provider_id IS '引用t_ui_data_provider.id，说明该查询的数据来自哪里';

-- 8. 查询参数绑定表
-- 说明：定义查询参数从页面draft、当前行、固定值或系统变量获取
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
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_query_param UNIQUE (query_config_id, param_name)
);

COMMENT ON TABLE t_ui_query_param IS '查询参数绑定表，定义查询参数从页面draft、当前行、固定值或系统变量获取';
COMMENT ON COLUMN t_ui_query_param.bind_source IS 'FORM、CURRENT_ROW、FIXED、SYSTEM、USER_INPUT';

-- 9. 查询结果回填规则表
-- 说明：定义查询结果如何回填到页面draft
CREATE TABLE t_ui_query_fill_rule (
    id BIGINT PRIMARY KEY,
    query_config_id BIGINT NOT NULL,
    source_field VARCHAR(300) NOT NULL,
    target_scope VARCHAR(50) NOT NULL DEFAULT 'FORM',
    target_path VARCHAR(500) NOT NULL,
    fill_mode VARCHAR(50) NOT NULL DEFAULT 'OVERWRITE',
    transform_json JSONB,
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ui_query_fill_rule ON t_ui_query_fill_rule (query_config_id, sort_no);

COMMENT ON TABLE t_ui_query_fill_rule IS '查询结果回填规则配置，不保存运行时数据';
COMMENT ON COLUMN t_ui_query_fill_rule.target_scope IS 'FORM父表单、CURRENT_ROW当前明细行、MODAL_DRAFT弹窗草稿';
COMMENT ON COLUMN t_ui_query_fill_rule.fill_mode IS 'OVERWRITE覆盖、KEEP_IF_NOT_EMPTY非空不覆盖、APPEND追加';

-- 10. 动作配置表
-- 说明：定义按钮或组件动作，如查询、打开弹窗、保存等
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
    before_rule JSONB,
    after_rule JSONB,
    props_json JSONB,
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_action_code UNIQUE (template_version_id, action_code)
);

COMMENT ON TABLE t_ui_action_config IS '动作配置表，定义按钮或组件动作，如查询、打开弹窗、保存弹窗草稿、外层保存';
COMMENT ON COLUMN t_ui_action_config.action_type IS 'OPEN_MODAL、QUERY、FILL、ADD_DETAIL_ROW、EDIT_DETAIL_ROW、MARK_DELETE_ROW、SAVE_MODAL_DRAFT、SAVE_CONTRACT等';


-- ============================================================
-- 第二部分：数据提供方相关表
-- ============================================================

-- 11. 数据提供方配置表
-- 说明：由IT维护，供查询组件和下拉组件取数
CREATE TABLE t_ui_data_provider (
    id BIGINT PRIMARY KEY,
    provider_code VARCHAR(100) NOT NULL,
    provider_name VARCHAR(200) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    data_source_category VARCHAR(20) NOT NULL DEFAULT 'IT',
    owner_type VARCHAR(50) NOT NULL DEFAULT 'IT',
    request_method VARCHAR(20),
    request_url VARCHAR(1000),
    platform_api_code VARCHAR(200),
    request_mapping_json JSONB,
    response_mapping_json JSONB,
    timeout_ms INTEGER NOT NULL DEFAULT 5000,
    cacheable SMALLINT NOT NULL DEFAULT 0,
    cache_ttl_seconds INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    props_json JSONB,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_data_provider_code UNIQUE (provider_code)
);

COMMENT ON TABLE t_ui_data_provider IS '数据提供方配置，由IT维护，供查询组件和下拉组件取数';
COMMENT ON COLUMN t_ui_data_provider.provider_type IS 'STATIC、DICT、HTTP、PLATFORM_API、INTERNAL_QUERY';
COMMENT ON COLUMN t_ui_data_provider.request_url IS 'HTTP类型的数据来源地址，前端不可见';
COMMENT ON COLUMN t_ui_data_provider.platform_api_code IS '平台集成类型的服务编码';

-- 12. 静态选项表
-- 说明：provider_type为STATIC或DICT时使用
CREATE TABLE t_ui_data_option (
    id BIGINT PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    option_value VARCHAR(200) NOT NULL,
    option_label VARCHAR(200) NOT NULL,
    parent_value VARCHAR(200),
    sort_no INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    ext_json JSONB,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ui_data_option UNIQUE (provider_id, option_value)
);

CREATE INDEX idx_ui_data_option_provider ON t_ui_data_option (provider_id, sort_no);

COMMENT ON TABLE t_ui_data_option IS '静态选项表，provider_type为STATIC或DICT时使用';


-- ============================================================
-- 第三部分：合同数据相关表
-- ============================================================

-- 13. 合同主表
-- 说明：保存当前合同状态和当前快照指针
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
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_contract_template ON t_contract (template_id, template_version_id);
CREATE INDEX idx_contract_no ON t_contract (contract_no);
CREATE INDEX idx_contract_source ON t_contract (source_system_code, source_biz_id);

COMMENT ON TABLE t_contract IS '合同主表，保存当前合同状态和当前快照指针';
COMMENT ON COLUMN t_contract.data_version IS '乐观锁版本号，防止并发覆盖';

-- 14. 合同数据快照表
-- 说明：合同完整JSON快照，每次外层保存生成一条
CREATE TABLE t_contract_data_snapshot (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    snapshot_no INTEGER NOT NULL,
    template_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    canonical_data JSONB NOT NULL,
    source_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    source_message_id BIGINT,
    save_reason VARCHAR(500),
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_contract_snapshot_no UNIQUE (contract_id, snapshot_no)
);

CREATE INDEX idx_contract_snapshot_contract ON t_contract_data_snapshot (contract_id, snapshot_no DESC);

COMMENT ON TABLE t_contract_data_snapshot IS '合同完整JSON快照，每次外层保存生成一条';
COMMENT ON COLUMN t_contract_data_snapshot.canonical_data IS '统一合同JSON数据，作为回显和历史追溯的权威数据';

-- 15. 合同字段值索引表
-- 说明：用于动态字段查询，不作为完整数据来源
CREATE TABLE t_contract_field_value (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    field_def_id BIGINT NOT NULL,
    field_path VARCHAR(500) NOT NULL,
    value_text TEXT,
    value_number NUMERIC(24, 6),
    value_date DATE,
    value_datetime TIMESTAMPTZ,
    value_bool SMALLINT,
    value_json JSONB,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_contract_field_text ON t_contract_field_value (field_path, value_text);
CREATE INDEX idx_contract_field_number ON t_contract_field_value (field_path, value_number);
CREATE INDEX idx_contract_field_date ON t_contract_field_value (field_path, value_date);
CREATE INDEX idx_contract_field_contract ON t_contract_field_value (contract_id, snapshot_id);

COMMENT ON TABLE t_contract_field_value IS '合同字段值索引表，用于动态字段查询，不作为完整数据来源';

-- 16. 合同明细行当前投影表
-- 说明：用于明细查询和回显辅助，权威数据仍是快照JSON
CREATE TABLE t_contract_detail_row (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    template_version_id BIGINT NOT NULL,
    detail_table_id BIGINT NOT NULL,
    detail_code VARCHAR(100) NOT NULL,
    row_uid VARCHAR(100) NOT NULL,
    row_no INTEGER NOT NULL,
    row_data JSONB NOT NULL,
    row_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_contract_detail_row_uid UNIQUE (contract_id, detail_code, row_uid)
);

CREATE INDEX idx_contract_detail_row ON t_contract_detail_row (contract_id, detail_code, row_no);

COMMENT ON TABLE t_contract_detail_row IS '合同明细行当前投影，用于明细查询和回显辅助，权威数据仍是快照JSON';

-- 17. 合同明细字段索引表
-- 说明：用于明细行内动态字段查询
CREATE TABLE t_contract_detail_field_value (
    id BIGINT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    detail_row_id BIGINT NOT NULL,
    snapshot_id BIGINT NOT NULL,
    detail_code VARCHAR(100) NOT NULL,
    row_uid VARCHAR(100) NOT NULL,
    field_def_id BIGINT NOT NULL,
    field_path VARCHAR(500) NOT NULL,
    value_text TEXT,
    value_number NUMERIC(24, 6),
    value_date DATE,
    value_datetime TIMESTAMPTZ,
    value_bool SMALLINT,
    value_json JSONB,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_detail_field_text ON t_contract_detail_field_value (detail_code, field_path, value_text);
CREATE INDEX idx_detail_field_number ON t_contract_detail_field_value (detail_code, field_path, value_number);
CREATE INDEX idx_detail_field_contract ON t_contract_detail_field_value (contract_id, detail_code);

COMMENT ON TABLE t_contract_detail_field_value IS '明细字段索引表，用于明细行内动态字段查询';

-- 18. 合同查询宽表
-- 说明：只存高频列表字段，便于列表筛选和排序
CREATE TABLE t_contract_search_index (
    contract_id BIGINT PRIMARY KEY,
    contract_no VARCHAR(100),
    contract_name VARCHAR(300),
    supplier_id VARCHAR(100),
    supplier_name VARCHAR(300),
    contract_type VARCHAR(100),
    total_amount NUMERIC(24, 6),
    currency VARCHAR(20),
    sign_date DATE,
    effective_date DATE,
    expire_date DATE,
    contract_status VARCHAR(50),
    source_system_code VARCHAR(100),
    current_snapshot_id BIGINT NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contract_search_supplier ON t_contract_search_index (supplier_name);
CREATE INDEX idx_contract_search_status ON t_contract_search_index (contract_status);
CREATE INDEX idx_contract_search_date ON t_contract_search_index (sign_date);

COMMENT ON TABLE t_contract_search_index IS '当前合同查询宽表，只存高频列表字段，便于列表筛选和排序';

-- 19. 合同附件表
-- 说明：保存附件与合同或字段路径的关系
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
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_contract_attachment ON t_contract_attachment (contract_id, biz_path);

COMMENT ON TABLE t_contract_attachment IS '合同附件表，保存附件与合同或字段路径的关系';


-- ============================================================
-- 第四部分：外部集成相关表
-- ============================================================

-- 20. 外部系统表
-- 说明：由IT维护
CREATE TABLE t_ext_system (
    id BIGINT PRIMARY KEY,
    system_code VARCHAR(100) NOT NULL,
    system_name VARCHAR(200) NOT NULL,
    system_type VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    remark VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ext_system_code UNIQUE (system_code)
);

COMMENT ON TABLE t_ext_system IS '外部系统登记表，由IT维护';

-- 21. 外部消息入库表
-- 说明：防止重复处理外部推送
CREATE TABLE t_ext_message_inbox (
    id BIGINT PRIMARY KEY,
    ext_system_id BIGINT NOT NULL,
    external_msg_id VARCHAR(200) NOT NULL,
    source_biz_id VARCHAR(200),
    message_type VARCHAR(100) NOT NULL,
    raw_payload JSONB NOT NULL,
    process_status VARCHAR(50) NOT NULL DEFAULT 'RECEIVED',
    error_message TEXT,
    received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ext_message UNIQUE (ext_system_id, external_msg_id)
);

CREATE INDEX idx_ext_message_status ON t_ext_message_inbox (process_status, received_at);

COMMENT ON TABLE t_ext_message_inbox IS '外部消息幂等入库表，防止重复处理外部推送';

-- 22. 外部数据映射主表
-- 说明：定义某类外部消息如何映射到统一合同数据
CREATE TABLE t_ext_data_mapping (
    id BIGINT PRIMARY KEY,
    ext_system_id BIGINT NOT NULL,
    mapping_code VARCHAR(100) NOT NULL,
    mapping_name VARCHAR(200) NOT NULL,
    message_type VARCHAR(100) NOT NULL,
    target_template_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    sample_payload JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ext_mapping_code UNIQUE (ext_system_id, mapping_code)
);

COMMENT ON TABLE t_ext_data_mapping IS '外部数据映射主表，定义某类外部消息如何映射到统一合同数据';

-- 23. 外部字段映射表
-- 说明：将外部报文字段映射到统一合同字段路径
CREATE TABLE t_ext_data_mapping_field (
    id BIGINT PRIMARY KEY,
    mapping_id BIGINT NOT NULL,
    source_path VARCHAR(500) NOT NULL,
    target_path VARCHAR(500) NOT NULL,
    data_type VARCHAR(50),
    required SMALLINT NOT NULL DEFAULT 0,
    transform_rule JSONB,
    default_value VARCHAR(1000),
    sort_no INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ext_mapping_field ON t_ext_data_mapping_field (mapping_id, sort_no);

COMMENT ON TABLE t_ext_data_mapping_field IS '外部字段映射表，将外部报文字段映射到统一合同字段路径';

-- 24. 外部对比记录表
-- 说明：保存外部统一数据和当前合同数据的整体差异
CREATE TABLE t_ext_compare_record (
    id BIGINT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    contract_id BIGINT,
    compare_type VARCHAR(50) NOT NULL DEFAULT 'EXTERNAL_TO_CURRENT',
    compare_status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    external_data JSONB NOT NULL,
    current_data JSONB,
    diff_summary JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_ext_compare_record IS '外部数据对比记录，保存外部统一数据和当前合同数据的整体差异';

-- 25. 外部对比明细表
-- 说明：逐字段展示差异和用户选择状态
CREATE TABLE t_ext_compare_item (
    id BIGINT PRIMARY KEY,
    compare_record_id BIGINT NOT NULL,
    field_path VARCHAR(500) NOT NULL,
    field_name_cn VARCHAR(200),
    external_value TEXT,
    current_value TEXT,
    diff_type VARCHAR(50) NOT NULL,
    accept_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_ext_compare_item ON t_ext_compare_item (compare_record_id, field_path);

COMMENT ON TABLE t_ext_compare_item IS '外部数据对比明细，逐字段展示差异和用户选择状态';


-- ============================================================
-- 第五部分：公共基础表（可选）
-- ============================================================

-- 26. 雪花ID Worker分配表（可选）
-- 说明：公共雪花ID worker分配表，不属于合同模板业务ER
CREATE TABLE t_sys_snowflake_worker (
    id BIGINT PRIMARY KEY,
    app_code VARCHAR(100) NOT NULL,
    instance_code VARCHAR(200) NOT NULL,
    worker_id INTEGER NOT NULL,
    datacenter_id INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ENABLED',
    last_heartbeat_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_snowflake_worker UNIQUE (app_code, instance_code),
    CONSTRAINT uk_snowflake_worker_id UNIQUE (datacenter_id, worker_id)
);

COMMENT ON TABLE t_sys_snowflake_worker IS '公共雪花ID worker分配表，不属于合同模板业务ER';


-- ============================================================
-- 建表脚本结束
-- ============================================================
