-- =====================================================
-- openGauss JSONB GIN 索引
-- 仅在 openGauss 上执行，不放 schema-h2.sql
-- =====================================================

-- 合同快照：按字段路径查询合同数据（最高频查询）
CREATE INDEX IF NOT EXISTS idx_snapshot_canonical_gin
    ON t_contract_data_snapshot USING GIN (canonical_data jsonb_path_ops);

-- 明细行数据：按明细字段查询
CREATE INDEX IF NOT EXISTS idx_detail_row_data_gin
    ON t_contract_detail_row USING GIN (row_data jsonb_path_ops);

-- 外部消息：按消息内容查询
CREATE INDEX IF NOT EXISTS idx_ext_message_payload_gin
    ON t_ext_message_inbox USING GIN (raw_payload);

-- 数据提供方配置：按配置内容查询
CREATE INDEX IF NOT EXISTS idx_data_provider_config_gin
    ON t_ui_data_provider USING GIN (config_json);
