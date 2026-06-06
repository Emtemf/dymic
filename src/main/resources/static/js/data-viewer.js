// data-viewer.js - 数据验证界面核心逻辑

/**
 * 数据验证工具类
 * 用于验证数据库中的配置数据是否正确入库
 */
class DataViewer {
    constructor(templateId, versionId) {
        this.templateId = templateId;
        this.versionId = versionId;
        this.layoutNodes = [];
        this.fieldDefs = [];
        this.fieldComponents = [];

        // 更新顶部信息显示
        this.updateTemplateInfo();
    }

    /**
     * 更新顶部模板信息显示
     */
    updateTemplateInfo() {
        const infoElement = document.getElementById('templateInfo');
        infoElement.textContent = `模板ID: ${this.templateId} | 版本ID: ${this.versionId}`;
    }

    /**
     * 加载所有数据（并行调用API）
     */
    async loadAllData() {
        try {
            this.showLoadingState();

            // 并行调用3个API
            const [layoutNodesResult, fieldDefsResult, fieldComponentsResult] = await Promise.all([
                this.loadLayoutNodes(),
                this.loadFieldDefs(),
                this.loadFieldComponents()
            ]);

            // 解析API响应
            this.layoutNodes = this.parseApiResponse(layoutNodesResult);
            this.fieldDefs = this.parseApiResponse(fieldDefsResult);
            this.fieldComponents = this.parseApiResponse(fieldComponentsResult);

            // 渲染数据表格
            this.renderLayoutNodes(this.layoutNodes);
            this.renderFieldDefs(this.fieldDefs);
            this.renderFieldComponents(this.fieldComponents);

            // 更新统计信息
            this.updateStats(this.layoutNodes, this.fieldDefs, this.fieldComponents);

            // 验证数据完整性
            this.validateDataIntegrity(this.layoutNodes, this.fieldDefs, this.fieldComponents);

            this.showSuccess('数据加载完成');

        } catch (error) {
            this.showError('加载数据失败: ' + error.message);
            this.clearLoadingState();
        }
    }

    /**
     * 解析API响应数据
     */
    parseApiResponse(response) {
        if (response && response.data) {
            // 支持两种响应格式:
            // 1. { data: [...] } - 直接数组
            // 2. { data: { success: true, data: [...] } } - 嵌套格式
            if (response.data.success !== undefined && response.data.data !== undefined) {
                return response.data.data || [];
            }
            return response.data || [];
        }
        return [];
    }

    /**
     * 显示加载状态
     */
    showLoadingState() {
        const loadingHtml = `
            <tr class="loading-row">
                <td colspan="7" class="text-center">
                    <span class="loading-spinner"></span> 加载中...
                </td>
            </tr>
        `;

        document.getElementById('layoutNodesTable').innerHTML = loadingHtml;
        document.getElementById('fieldDefsTable').innerHTML = loadingHtml.replace('colspan="7"', 'colspan="6"');
        document.getElementById('fieldComponentsTable').innerHTML = loadingHtml;
    }

    /**
     * 清除加载状态（显示空数据）
     */
    clearLoadingState() {
        const emptyHtml = `
            <tr class="empty-row">
                <td colspan="7" class="text-center">暂无数据</td>
            </tr>
        `;

        document.getElementById('layoutNodesTable').innerHTML = emptyHtml;
        document.getElementById('fieldDefsTable').innerHTML = emptyHtml.replace('colspan="7"', 'colspan="6"');
        document.getElementById('fieldComponentsTable').innerHTML = emptyHtml;

        // 清空计数
        document.getElementById('layoutNodeCount').textContent = '0';
        document.getElementById('fieldDefCount').textContent = '0';
        document.getElementById('fieldComponentCount').textContent = '0';

        document.getElementById('totalNodes').textContent = '0';
        document.getElementById('totalFields').textContent = '0';
        document.getElementById('totalComponents').textContent = '0';
    }

    /**
     * 加载布局节点数据
     */
    async loadLayoutNodes() {
        const response = await axios.get(
            `/api/templates/${this.templateId}/versions/${this.versionId}/layout-nodes`
        );
        return response;
    }

    /**
     * 加载字段定义数据
     */
    async loadFieldDefs() {
        const response = await axios.get(
            `/api/templates/${this.templateId}/versions/${this.versionId}/field-defs`
        );
        return response;
    }

    /**
     * 加载字段组件数据
     */
    async loadFieldComponents() {
        const response = await axios.get(
            `/api/templates/${this.templateId}/versions/${this.versionId}/field-components`
        );
        return response;
    }

    /**
     * 渲染布局节点表格
     */
    renderLayoutNodes(nodes) {
        const tbody = document.getElementById('layoutNodesTable');
        tbody.innerHTML = '';

        if (!nodes || nodes.length === 0) {
            tbody.innerHTML = `<tr class="empty-row"><td colspan="7" class="text-center">暂无布局节点数据</td></tr>`;
            document.getElementById('layoutNodeCount').textContent = '0';
            return;
        }

        nodes.forEach(node => {
            const row = document.createElement('tr');
            row.setAttribute('data-id', node.id);

            // 创建单元格
            const cells = [
                this.createCell(node.id, 'id-cell'),
                this.createCell(node.nodeCode || '-', 'code-cell'),
                this.createCell(node.nodeName || '-', 'name-cell'),
                this.createCell(node.nodeType || '-', 'type-cell'),
                this.createCell(node.nodePath || '-', 'path-cell'),
                this.createCell(node.parentId || '根节点', 'parent-cell'),
                this.createCell(node.sortNo || '-', 'sort-cell')
            ];

            cells.forEach(cell => row.appendChild(cell));

            // 高亮自动生成的编码（以'jiben'开头的节点）
            if (node.nodeCode && node.nodeCode.startsWith('jiben')) {
                const codeCell = row.querySelector('.code-cell');
                if (codeCell) {
                    codeCell.classList.add('auto-generated');
                }
            }

            tbody.appendChild(row);
        });

        // 更新计数
        document.getElementById('layoutNodeCount').textContent = nodes.length;
    }

    /**
     * 渲染字段定义表格
     */
    renderFieldDefs(fieldDefs) {
        const tbody = document.getElementById('fieldDefsTable');
        tbody.innerHTML = '';

        if (!fieldDefs || fieldDefs.length === 0) {
            tbody.innerHTML = `<tr class="empty-row"><td colspan="6" class="text-center">暂无字段定义数据</td></tr>`;
            document.getElementById('fieldDefCount').textContent = '0';
            return;
        }

        fieldDefs.forEach(field => {
            const row = document.createElement('tr');
            row.setAttribute('data-id', field.id);

            // 创建单元格
            const cells = [
                this.createCell(field.id, 'id-cell'),
                this.createCell(field.fieldCode || '-', 'code-cell'),
                this.createCell(field.fieldNameCn || field.fieldName || '-', 'name-cell'),
                this.createCell(field.dataType || '-', 'type-cell'),
                this.createCell(field.fieldPath || '-', 'path-cell'),
                this.createCell(field.layoutNodeId || '-', 'node-cell')
            ];

            cells.forEach(cell => row.appendChild(cell));

            // 高亮自动生成的编码（以'hetong'开头的字段）
            if (field.fieldCode && field.fieldCode.startsWith('hetong')) {
                const codeCell = row.querySelector('.code-cell');
                if (codeCell) {
                    codeCell.classList.add('auto-generated');
                }
            }

            tbody.appendChild(row);
        });

        // 更新计数
        document.getElementById('fieldDefCount').textContent = fieldDefs.length;
    }

    /**
     * 渲染字段组件表格
     */
    renderFieldComponents(components) {
        const tbody = document.getElementById('fieldComponentsTable');
        tbody.innerHTML = '';

        if (!components || components.length === 0) {
            tbody.innerHTML = `<tr class="empty-row"><td colspan="7" class="text-center">暂无字段组件数据</td></tr>`;
            document.getElementById('fieldComponentCount').textContent = '0';
            return;
        }

        components.forEach(component => {
            const row = document.createElement('tr');
            row.setAttribute('data-id', component.id);

            // 创建单元格
            const cells = [
                this.createCell(component.id, 'id-cell'),
                this.createCell(component.componentType || '-', 'type-cell'),
                this.createCell(component.labelName || '-', 'label-cell'),
                this.createCell(component.layoutNodeId || '-', 'node-cell'),
                this.createCell(component.fieldDefId || '-', 'field-cell'),
                this.createCell(component.dataProviderId || '无', 'source-cell'),
                this.createCell(component.sortNo || '-', 'sort-cell')
            ];

            cells.forEach(cell => row.appendChild(cell));

            // 高亮有数据源绑定的组件
            if (component.dataProviderId) {
                const sourceCell = row.querySelector('.source-cell');
                if (sourceCell) {
                    sourceCell.classList.add('has-data-source');
                }
            }

            tbody.appendChild(row);
        });

        // 更新计数
        document.getElementById('fieldComponentCount').textContent = components.length;
    }

    /**
     * 创建单元格
     */
    createCell(value, className = '') {
        const cell = document.createElement('td');
        cell.textContent = value;
        if (className) {
            cell.className = className;
        }
        return cell;
    }

    /**
     * 更新统计信息
     */
    updateStats(nodes, fieldDefs, components) {
        const nodeCount = nodes ? nodes.length : 0;
        const fieldCount = fieldDefs ? fieldDefs.length : 0;
        const componentCount = components ? components.length : 0;

        document.getElementById('totalNodes').textContent = nodeCount;
        document.getElementById('totalFields').textContent = fieldCount;
        document.getElementById('totalComponents').textContent = componentCount;
    }

    /**
     * 验证数据完整性（验证自动生成字段）
     */
    validateDataIntegrity(nodes, fieldDefs, components) {
        const issues = [];

        // 验证节点编码生成正确性
        if (nodes && nodes.length > 0) {
            nodes.forEach(node => {
                if (!node.nodeCode) {
                    issues.push(`节点 ${node.id} (${node.nodeName || '未知'}) 缺少 nodeCode`);
                }
                if (!node.nodePath) {
                    issues.push(`节点 ${node.id} (${node.nodeName || '未知'}) 缺少 nodePath`);
                }
            });
        }

        // 验证字段编码生成正确性
        if (fieldDefs && fieldDefs.length > 0) {
            fieldDefs.forEach(field => {
                if (!field.fieldCode) {
                    issues.push(`字段 ${field.id} (${field.fieldNameCn || field.fieldName || '未知'}) 缺少 fieldCode`);
                }
                if (!field.fieldPath) {
                    issues.push(`字段 ${field.id} (${field.fieldNameCn || field.fieldName || '未知'}) 缺少 fieldPath`);
                }
            });
        }

        // 验证组件绑定正确性
        if (components && components.length > 0) {
            components.forEach(component => {
                if (!component.layoutNodeId) {
                    issues.push(`组件 ${component.id} 缺少 layoutNodeId 绑定`);
                }
                if (!component.fieldDefId) {
                    issues.push(`组件 ${component.id} 缺少 fieldDefId 绑定`);
                }
            });
        }

        // 显示验证结果
        const integrityElement = document.getElementById('dataIntegrity');
        integrityElement.className = 'stat-value';

        if (issues.length === 0) {
            integrityElement.textContent = '验证通过';
            integrityElement.classList.add('status-ok');
        } else {
            integrityElement.textContent = `发现问题 ${issues.length}个`;
            integrityElement.classList.add('status-error');

            // 显示详细问题
            this.showValidationIssues(issues);
        }
    }

    /**
     * 显示验证问题详情
     */
    showValidationIssues(issues) {
        const issueList = document.getElementById('issueList');
        issueList.innerHTML = '';

        issues.forEach(issue => {
            const li = document.createElement('li');
            li.textContent = issue;
            issueList.appendChild(li);
        });

        document.getElementById('validationModal').style.display = 'flex';
    }

    /**
     * 关闭验证问题模态框
     */
    closeValidationModal() {
        document.getElementById('validationModal').style.display = 'none';
    }

    /**
     * 刷新数据
     */
    async refreshData() {
        this.showWarning('正在刷新数据...');
        await this.loadAllData();
    }

    /**
     * 显示成功消息
     */
    showSuccess(message) {
        this.showMessage(message, 'success');
    }

    /**
     * 显示错误消息
     */
    showError(message) {
        this.showMessage(message, 'error');
    }

    /**
     * 显示警告消息
     */
    showWarning(message) {
        this.showMessage(message, 'warning');
    }

    /**
     * 显示消息提示
     */
    showMessage(message, type) {
        const container = document.getElementById('messageContainer');
        const toast = document.createElement('div');
        toast.className = `message-toast ${type}`;
        toast.textContent = message;

        container.appendChild(toast);

        // 3秒后自动消失
        setTimeout(() => {
            toast.style.animation = 'slideIn 0.3s ease-out reverse';
            setTimeout(() => {
                container.removeChild(toast);
            }, 300);
        }, 3000);
    }
}

// 全局DataViewer实例
let dataViewerInstance = null;

/**
 * 初始化页面
 */
function initializePage() {
    const urlParams = new URLSearchParams(window.location.search);
    const templateId = urlParams.get('templateId');
    const versionId = urlParams.get('versionId');

    // 验证参数有效性
    if (!templateId || !versionId) {
        console.error('缺少必要参数: templateId 或 versionId');
        document.getElementById('templateInfo').textContent = '参数缺失 - 请从配置界面进入';

        // 显示空数据状态
        const tbody = document.getElementById('layoutNodesTable');
        tbody.innerHTML = `<tr class="error-row"><td colspan="7" class="text-center">请提供 templateId 和 versionId 参数</td></tr>`;
        return;
    }

    // 创建DataViewer实例并加载数据
    dataViewerInstance = new DataViewer(templateId, versionId);
    dataViewerInstance.loadAllData();
}

/**
 * 刷新数据（全局函数）
 */
function refreshData() {
    if (dataViewerInstance) {
        dataViewerInstance.refreshData();
    } else {
        console.error('DataViewer实例未初始化');
    }
}

/**
 * 关闭验证模态框（全局函数）
 */
function closeValidationModal() {
    if (dataViewerInstance) {
        dataViewerInstance.closeValidationModal();
    }
}

// 页面加载完成后自动初始化
document.addEventListener('DOMContentLoaded', initializePage);

// 导出模块（用于测试）
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { DataViewer };
}