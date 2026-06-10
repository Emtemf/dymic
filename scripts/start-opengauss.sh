#!/bin/bash

# openGauss Docker容器启动脚本

CONTAINER_NAME="opengauss-contract"
DB_PORT="5432"
DB_USER="gaussdb"
DB_PASSWORD="OpenGauss@123"
DB_NAME="postgres"
DATA_DIR="/home/wula/.local/opengauss-data"
IMAGE="enmotech/opengauss:5.0.0"

echo "======================================"
echo "启动 openGauss Docker 容器"
echo "======================================"

mkdir -p "$DATA_DIR"

if docker ps -a --format '{{.Names}}' | grep -qx "$CONTAINER_NAME"; then
    echo "容器已存在，直接启动..."
    docker start "$CONTAINER_NAME" >/dev/null
else
    echo "创建并启动容器..."
    docker run -d \
        --name "$CONTAINER_NAME" \
        --privileged=true \
        -p "$DB_PORT:5432" \
        -e GS_PASSWORD="$DB_PASSWORD" \
        -v "$DATA_DIR:/var/lib/opengauss" \
        "$IMAGE" >/dev/null
fi

echo "等待数据库启动..."
sleep 10

docker ps --format '{{.Names}}\t{{.Status}}\t{{.Ports}}' | grep "$CONTAINER_NAME"

echo ""
echo "======================================"
echo "openGauss 启动成功！"
echo "======================================"
echo ""
echo "镜像："
echo "  $IMAGE"
echo ""
echo "连接信息："
echo "  主机: localhost"
echo "  端口: $DB_PORT"
echo "  用户: $DB_USER"
echo "  密码: $DB_PASSWORD"
echo "  数据库: contract_template（初始化后） / postgres（初始）"
echo ""
echo "连接命令："
echo "  docker exec -it $CONTAINER_NAME bash"
echo "  export LD_LIBRARY_PATH=/usr/local/opengauss/lib:\$LD_LIBRARY_PATH"
echo "  /usr/local/opengauss/bin/gsql -d $DB_NAME -U $DB_USER -W $DB_PASSWORD"
echo ""
echo "JDBC URL："
echo "  jdbc:postgresql://localhost:$DB_PORT/contract_template?options=-c%20TimeZone=UTC"
echo ""