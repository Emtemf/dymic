#!/bin/bash

# openGauss 数据库初始化脚本

CONTAINER_NAME="opengauss-contract"
DB_NAME="contract_template"
DB_USER="gaussdb"
DB_PASSWORD="OpenGauss@123"

echo "=== 初始化 openGauss 数据库 ==="

# 等待数据库就绪
echo "等待数据库就绪..."
sleep 5

# 创建数据库
echo "创建数据库 ${DB_NAME}..."
docker exec -i ${CONTAINER_NAME} bash -c "gsql -d postgres -U ${DB_USER} -W ${DB_PASSWORD} -c 'CREATE DATABASE ${DB_NAME};'" 2>/dev/null || echo "数据库可能已存在"

# 执行建表脚本
echo "执行建表脚本..."
docker exec -i ${CONTAINER_NAME} bash -c "gsql -d ${DB_NAME} -U ${DB_USER} -W ${DB_PASSWORD}" < src/main/resources/schema-opengauss.sql

# 执行测试数据脚本
echo "执行测试数据脚本..."
docker exec -i ${CONTAINER_NAME} bash -c "gsql -d ${DB_NAME} -U ${DB_USER} -W ${DB_PASSWORD}" < src/main/resources/data-opengauss.sql

# 验证数据
echo "验证数据..."
docker exec -i ${CONTAINER_NAME} bash -c "gsql -d ${DB_NAME} -U ${DB_USER} -W ${DB_PASSWORD} -c 'SELECT COUNT(*) FROM t_ui_template;'"

echo "✅ 数据库初始化完成"
