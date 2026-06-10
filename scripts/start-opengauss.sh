#!/bin/bash

# openGauss Docker容器启动脚本

# 容器名称
CONTAINER_NAME="opengauss-contract"

# 数据库配置
DB_PORT="5432"
DB_USER="gaussdb"
DB_PASSWORD="OpenGauss@123"
DB_NAME="postgres"

# 数据持久化目录
DATA_DIR="/home/wula/.local/opengauss-data"

echo "======================================"
echo "启动openGauss Docker容器"
echo "======================================"

# 创建数据目录
mkdir -p "$DATA_DIR"

# 检查是否已有容器
if docker ps -a | grep -q "$CONTAINER_NAME"; then
    echo "容器已存在，删除旧容器..."
    docker rm -f "$CONTAINER_NAME"
fi

# 启动容器
echo "启动openGauss容器..."
docker run -d \
    --name "$CONTAINER_NAME" \
    --privileged=true \
    -p "$DB_PORT:5432" \
    -e GS_PASSWORD="$DB_PASSWORD" \
    -v "$DATA_DIR:/var/lib/opengauss" \
    opengauss/opengauss:latest

# 等待容器启动
echo "等待数据库启动..."
sleep 10

# 检查容器状态
docker ps | grep "$CONTAINER_NAME"

echo ""
echo "======================================"
echo "openGauss 启动成功！"
echo "======================================"
echo ""
echo "连接信息："
echo "  主机: localhost"
echo "  端口: $DB_PORT"
echo "  用户: $DB_USER"
echo "  密码: $DB_PASSWORD"
echo "  数据库: contract_template（初始化后） / postgres（初始）"
echo ""
echo "连接命令："
echo "  docker exec -it $CONTAINER_NAME gsql -d $DB_NAME -U $DB_USER -W $DB_PASSWORD"
echo ""
echo "JDBC URL："
echo "  jdbc:postgresql://localhost:$DB_PORT/contract_template"
echo ""