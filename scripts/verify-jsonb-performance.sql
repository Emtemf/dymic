-- =====================================================
-- openGauss JSONB 查询性能验证脚本
-- 步骤1: 先执行建表和数据初始化
-- 步骤2: 执行无索引查询（记录耗时）
-- 步骤3: 执行 GIN 索引创建
-- 步骤4: 执行有索引查询（记录耗时）
-- =====================================================

-- 清理旧数据
DELETE FROM t_contract_data_snapshot;

-- 插入 1000 条测试数据
INSERT INTO t_contract_data_snapshot (id, contract_id, snapshot_no, template_id, template_version_id, canonical_data, source_type, created_at)
SELECT
    gs * 1000 + 1,
    gs,
    1,
    1,
    1,
    ('{"basic": {"contractNo": "HT-' || gs || '", "contractName": "测试合同' || gs || '", "supplierName": "供应商' || (gs % 100) || '"}, "money": {"totalAmount": ' || (gs * 1000) || '.00, "currency": "CNY"}}')::jsonb,
    'MANUAL',
    NOW() - (1000 - gs) * INTERVAL '1 hour'
FROM generate_series(1, 1000) AS gs;

-- 无索引查询：按 supplierName 查询
EXPLAIN ANALYZE
SELECT id, contract_id, canonical_data->>'basic' as basic
FROM t_contract_data_snapshot
WHERE canonical_data @> '{"basic": {"supplierName": "供应商5"}}'::jsonb;

-- 无索引查询：按 contractNo 查询
EXPLAIN ANALYZE
SELECT id, contract_id
FROM t_contract_data_snapshot
WHERE canonical_data->>'basic.contractNo' = 'HT-500';

-- 创建 GIN 索引
CREATE INDEX IF NOT EXISTS idx_snapshot_canonical_gin
    ON t_contract_data_snapshot USING GIN (canonical_data jsonb_path_ops);

-- 有索引查询：按 supplierName 查询（@> 操作符）
EXPLAIN ANALYZE
SELECT id, contract_id, canonical_data->>'basic' as basic
FROM t_contract_data_snapshot
WHERE canonical_data @> '{"basic": {"supplierName": "供应商5"}}'::jsonb;

-- 有索引查询：按 contractNo 查询
EXPLAIN ANALYZE
SELECT id, contract_id
FROM t_contract_data_snapshot
WHERE canonical_data->>'basic'.'contractNo' = 'HT-500';
