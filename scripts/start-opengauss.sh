#!/bin/bash

# openGauss Docker 启动脚本

CONTAINER_NAME="opengauss-contract"
IMAGE="enmotech/opengauss:5.0.0"
PORT="5432"
PASSWORD="OpenGauss@123"
DATA_DIR="/data/opengauss"

echo "=== 启动 openGauss 容器 ==="

# 检查容器是否已存在
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "容器 ${CONTAINER_NAME} 已存在"
    read -p "是否删除并重新创建？(y/n): " confirm
    if [ "$confirm" = "y" ]; then
        docker rm -f ${CONTAINER_NAME}
    else
        echo "取消操作"
        exit 1
    fi
fi

# 创建数据目录
sudo mkdir -p ${DATA_DIR}
sudo chown -R $(whoami):$(whoami) ${DATA_DIR}

# 启动容器
docker run -d \
    --name ${CONTAINER_NAME} \
    -p ${PORT}:5432 \
    -e GS_PASSWORD=${PASSWORD} \
    -v ${DATA_DIR}:/var/lib/opengauss \
    ${IMAGE}

echo "=== 等待 openGauss 启动 ==="
sleep 10

# 检查容器状态
if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "✅ openGauss 启动成功"
    echo "连接信息："
    echo "  Host: localhost"
    echo "  Port: ${PORT}"
    echo "  User: gaussdb"
    echo "  Password: ${PASSWORD}"
else
    echo "❌ openGauss 启动失败"
    docker logs ${CONTAINER_NAME}
    exit 1
fi
