#!/bin/bash

# 合同模板动态渲染系统 - 智能启动脚本
# 自动检测端口冲突，选择可用端口

set -e

PROJECT_DIR="/home/wula/IdeaProjects/dymic"
CANDIDATE_PORTS=(8888 8080 9090 9999 9000)
CONFIG_FILE="$PROJECT_DIR/src/main/resources/application.yml"

echo "======================================"
echo "   智能启动脚本"
echo "======================================"
echo ""

# 检查Java版本
echo "✓ 检查Java版本..."
java -version

echo ""

# 检测可用端口
echo "✓ 检测可用端口..."
SELECTED_PORT=""
for port in "${CANDIDATE_PORTS[@]}"; do
    if ! lsof -i :$port >/dev/null 2>&1; then
        SELECTED_PORT=$port
        echo "  端口 $port: ✓ 可用"
        break
    else
        echo "  端口 $port: ✗ 已被占用"
    fi
done

if [ -z "$SELECTED_PORT" ]; then
    echo ""
    echo "❌ 所有候选端口都被占用！"
    echo ""
    echo "候选端口: ${CANDIDATE_PORTS[*]}"
    echo ""
    echo "解决方案："
    echo "  1. 停止占用端口的服务"
    echo "  2. 手动修改 application.yml 中的 server.port"
    echo "  3. 使用IDEA手动启动（可以在配置中设置端口）"
    exit 1
fi

echo ""
echo "✓ 选择端口: $SELECTED_PORT"

# 修改配置文件中的端口
echo "✓ 更新配置文件..."
if grep -q "server:" "$CONFIG_FILE"; then
    # 更新端口配置
    sed -i "s/port: [0-9]\+/port: $SELECTED_PORT/" "$CONFIG_FILE"
    echo "  配置文件已更新: server.port = $SELECTED_PORT"
else
    # 添加端口配置
    echo "server:" >> "$CONFIG_FILE"
    echo "  port: $SELECTED_PORT" >> "$CONFIG_FILE"
    echo "  配置文件已添加: server.port = $SELECTED_PORT"
fi

echo ""

# 检查Maven
echo "✓ 检查构建工具..."
if command -v mvn &> /dev/null; then
    BUILD_CMD="mvn"
    echo "  Maven: ✓ 已安装"
elif [ -f "$HOME/.local/apache-maven-*/bin/mvn" ]; then
    MAVEN_HOME=$(find "$HOME/.local" -name "apache-maven-*" -type d | head -1)
    BUILD_CMD="$MAVEN_HOME/bin/mvn"
    export PATH="$MAVEN_HOME/bin:$PATH"
    echo "  Maven: ✓ 已下载 ($MAVEN_HOME)"
else
    echo ""
    echo "⚠ Maven未安装，尝试下载..."
    echo ""

    MAVEN_VERSION="3.9.6"
    MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz"
    MAVEN_DIR="$HOME/.local/apache-maven-$MAVEN_VERSION"

    echo "下载地址: $MAVEN_URL"
    echo ""

    # 使用wget下载（显示进度）
    if wget --progress=bar:force "$MAVEN_URL" -O "$HOME/.local/maven.tar.gz" 2>&1; then
        echo ""
        echo "✓ 解压Maven..."
        tar -xzf "$HOME/.local/maven.tar.gz" -C "$HOME/.local"

        # 清理
        rm "$HOME/.local/maven.tar.gz"

        BUILD_CMD="$MAVEN_DIR/bin/mvn"
        export PATH="$MAVEN_DIR/bin:$PATH"

        echo "✓ Maven安装完成"
    else
        echo ""
        echo "❌ Maven下载失败"
        echo ""
        echo "请手动安装Maven:"
        echo "  sudo apt-get install -y maven"
        echo ""
        echo "或者使用IntelliJ IDEA启动（推荐）:"
        echo "  1. 打开IDEA"
        echo "  2. File → Open → $PROJECT_DIR"
        echo "  3. 运行 ContractTemplateApplication.java"
        exit 1
    fi
fi

echo ""
echo "======================================"
echo "   开始启动应用"
echo "======================================"
echo ""
echo "访问地址:"
echo "  - 入口页面: http://localhost:$SELECTED_PORT/index.html"
echo "  - 简单界面: http://localhost:$SELECTED_PORT/simple.html"
echo "  - 复杂界面: http://localhost:$SELECTED_PORT/complex.html"
echo "  - H2控制台: http://localhost:$SELECTED_PORT/h2-console"
echo ""
echo "前端会自动检测端口并连接！"
echo ""
echo "按 Ctrl+C 停止应用"
echo ""
echo "======================================"
echo ""

# 进入项目目录
cd "$PROJECT_DIR"

# 启动应用
$BUILD_CMD spring-boot:run