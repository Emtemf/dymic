#!/bin/bash

# IntelliJ IDEA 启动脚本

PROJECT_DIR="/home/wula/IdeaProjects/dymic"

echo "======================================"
echo "使用IntelliJ IDEA启动项目"
echo "======================================"
echo ""

# 检查IDEA是否安装
if command -v idea &> /dev/null; then
    echo "✓ 找到IntelliJ IDEA命令行工具"
    echo ""
    echo "启动命令："
    echo "  idea $PROJECT_DIR"
    echo ""
    echo "执行启动..."
    idea "$PROJECT_DIR"
elif [ -f "/opt/idea/bin/idea.sh" ]; then
    echo "✓ 找到IDEA安装目录"
    /opt/idea/bin/idea.sh "$PROJECT_DIR"
elif [ -f "$HOME/.local/share/JetBrains/IntelliJIdea*/bin/idea.sh" ]; then
    IDEA_SH=$(find "$HOME/.local/share/JetBrains" -name "idea.sh" | head -1)
    echo "✓ 找到IDEA: $IDEA_SH"
    "$IDEA_SH" "$PROJECT_DIR"
else
    echo "⚠ 未找到IntelliJ IDEA"
    echo ""
    echo "请手动操作："
    echo "  1. 打开IDEA"
    echo "  2. File → Open"
    echo "  3. 选择: $PROJECT_DIR"
    echo "  4. 运行 ContractTemplateApplication.java"
fi