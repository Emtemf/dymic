#!/bin/bash

# 合同模板动态渲染系统一键启动脚本
# 自动下载Maven Wrapper并启动应用

set -e

PROJECT_DIR="/home/wula/IdeaProjects/dymic"
MAVEN_VERSION="3.9.6"
MAVEN_DIR="$HOME/.local/apache-maven-$MAVEN_VERSION"

echo "======================================"
echo "合同模板动态渲染系统启动脚本"
echo "======================================"

# 检查Java版本
echo ""
echo "✓ 检查Java版本..."
java -version

# 检查Maven是否已安装
if command -v mvn &> /dev/null; then
    echo "✓ Maven已安装"
    MVN_CMD="mvn"
elif [ -f "$MAVEN_DIR/bin/mvn" ]; then
    echo "✓ Maven已下载到用户目录"
    MVN_CMD="$MAVEN_DIR/bin/mvn"
else
    echo ""
    echo "⚠ Maven未安装，开始下载..."
    echo ""

    # 创建目录
    mkdir -p "$HOME/.local"
    mkdir -p "$HOME/.m2/repository"

    # 下载Maven
    MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz"
    echo "下载地址: $MAVEN_URL"
    echo ""

    if command -v wget &> /dev/null; then
        wget -q "$MAVEN_URL" -O "$HOME/.local/maven.tar.gz"
    elif command -v curl &> /dev/null; then
        curl -sL "$MAVEN_URL" -o "$HOME/.local/maven.tar.gz"
    else
        echo "❌ 错误：需要 wget 或 curl 来下载Maven"
        echo ""
        echo "请手动安装Maven："
        echo "  sudo apt-get install -y maven"
        echo ""
        echo "或者安装wget："
        echo "  sudo apt-get install -y wget"
        exit 1
    fi

    # 解压Maven
    echo "✓ 解压Maven..."
    tar -xzf "$HOME/.local/maven.tar.gz" -C "$HOME/.local"

    # 清理
    rm "$HOME/.local/maven.tar.gz"

    MVN_CMD="$MAVEN_DIR/bin/mvn"
    echo "✓ Maven下载完成"
fi

echo ""
echo "======================================"
echo "开始启动Spring Boot应用"
echo "======================================"
echo ""

# 进入项目目录
cd "$PROJECT_DIR"

# 编译并启动
echo "✓ 开始编译项目..."
echo ""

$MVN_CMD clean compile

echo ""
echo "✓ 编译完成，启动应用..."
echo ""
echo "访问地址："
echo "  - 入口页面: http://localhost:8080/index.html"
echo "  - 简单界面: http://localhost:8080/simple.html"
echo "  - 复杂界面: http://localhost:8080/complex.html"
echo "  - H2控制台: http://localhost:8080/h2-console"
echo ""
echo "按 Ctrl+C 停止应用"
echo ""
echo "======================================"
echo ""

# 启动应用
$MVN_CMD spring-boot:run