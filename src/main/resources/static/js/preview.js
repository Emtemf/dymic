/**
 * 预览界面渲染器 - 开发验证工具
 *
 * 功能：
 * 1. 调用Schema API获取完整配置树
 * 2. 递归渲染布局节点树
 * 3. 根据组件类型渲染不同输入控件
 * 4. 实时更新数据面板
 * 5. 验证动作按钮功能
 */

// 全局渲染器实例
let renderer;

class PreviewRenderer {
    constructor(templateId, versionId) {
        this.templateId = templateId;
        this.versionId = versionId;
        this.schema = null;
        this.formData = {};
        this.fieldCount = 0;
        this.componentCount = 0;
        this.actionCount = 0;
        this.nodeCount = 0;
    }

    /**
     * 加载Schema并渲染表单
     */
    async loadAndRender() {
        try {
            // 调用Schema API获取完整配置树
            const response = await axios.get(
                `/api/templates/${this.templateId}/versions/${this.versionId}/schema`
            );

            this.schema = response.data.data;

            // 显示模板和版本信息
            document.getElementById('templateName').textContent =
                this.schema.templateName || '未命名模板';
            document.getElementById('versionNumber').textContent =
                `v${this.schema.versionNumber || '1'}`;

            // 统计信息
            this.countSchemaElements(this.schema.layoutNodes);

            // 更新Schema信息面板
            this.updateSchemaInfoPanel();

            // 渲染表单
            this.renderForm(this.schema.layoutNodes);

            console.log('Schema加载成功:', this.schema);
            console.log('统计信息:', {
                nodes: this.nodeCount,
                fields: this.fieldCount,
                components: this.componentCount,
                actions: this.actionCount
            });

        } catch (error) {
            this.showError('加载Schema失败: ' + (error.response?.data?.message || error.message));
            console.error('加载Schema失败:', error);
        }
    }

    /**
     * 统计Schema元素数量（递归）
     */
    countSchemaElements(nodes) {
        if (!nodes || nodes.length === 0) return;

        nodes.forEach(node => {
            this.nodeCount++;

            // 统计组件数量
            if (node.components) {
                this.componentCount += node.components.length;

                // 统计字段数量（通过组件引用）
                node.components.forEach(component => {
                    if (component.fieldDefId) {
                        this.fieldCount++;
                    }
                });
            }

            // 统计动作数量
            if (node.actions) {
                this.actionCount += node.actions.length;
            }

            // 递归统计子节点
            if (node.children) {
                this.countSchemaElements(node.children);
            }
        });
    }

    /**
     * 更新Schema信息面板
     */
    updateSchemaInfoPanel() {
        document.getElementById('totalNodes').textContent = this.nodeCount;
        document.getElementById('totalFields').textContent = this.fieldCount;
        document.getElementById('totalComponents').textContent = this.componentCount;
        document.getElementById('totalActions').textContent = this.actionCount;
    }

    /**
     * 渲染表单（递归渲染节点树）
     */
    renderForm(nodes) {
        const container = document.getElementById('formContainer');
        container.innerHTML = '';  // 清空容器

        if (!nodes || nodes.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">📝</div>
                    <p class="empty-state-text">暂无配置节点</p>
                    <button class="btn btn-primary" onclick="window.location.href='/config.html'">
                        前往配置
                    </button>
                </div>
            `;
            return;
        }

        // 递归渲染节点树
        nodes.forEach(node => {
            const nodeElement = this.renderNode(node);
            if (nodeElement) {
                container.appendChild(nodeElement);
            }
        });

        // 绑定数据变化监听
        this.bindDataListeners();

        // 更新数据面板显示
        this.updateDataPanel();

        this.showToast('success', '表单渲染完成');
    }

    /**
     * 渲染单个节点（递归）
     */
    renderNode(node) {
        if (node.nodeType === 'CARD_CONTAINER') {
            // 卡片容器
            const card = document.createElement('div');
            card.className = 'card mb-3';
            card.setAttribute('data-node-id', node.id);
            card.setAttribute('data-node-code', node.nodeCode);

            // 卡片头部
            const cardHeader = document.createElement('div');
            cardHeader.className = 'card-header';
            cardHeader.innerHTML = `
                <h5>${node.nodeName || '未命名节点'}</h5>
                <small class="text-muted">节点编码: ${node.nodeCode || '-'}</small>
            `;
            card.appendChild(cardHeader);

            // 卡片主体
            const cardBody = document.createElement('div');
            cardBody.className = 'card-body';

            // 渲染字段组件
            if (node.components && node.components.length > 0) {
                const fieldsContainer = document.createElement('div');
                fieldsContainer.className = 'fields-container';

                node.components.forEach(component => {
                    const fieldElement = this.renderFieldComponent(component);
                    if (fieldElement) {
                        fieldsContainer.appendChild(fieldElement);
                    }
                });

                cardBody.appendChild(fieldsContainer);
            }

            // 递归渲染子节点
            if (node.children && node.children.length > 0) {
                const childrenContainer = document.createElement('div');
                childrenContainer.className = 'children-container';

                node.children.forEach(child => {
                    const childElement = this.renderNode(child);
                    if (childElement) {
                        childrenContainer.appendChild(childElement);
                    }
                });

                cardBody.appendChild(childrenContainer);
            }

            // 渲染动作按钮
            if (node.actions && node.actions.length > 0) {
                const actionsContainer = document.createElement('div');
                actionsContainer.className = 'actions-container';

                node.actions.forEach(action => {
                    const actionElement = this.renderAction(action);
                    if (actionElement) {
                        actionsContainer.appendChild(actionElement);
                    }
                });

                cardBody.appendChild(actionsContainer);
            }

            card.appendChild(cardBody);
            return card;
        }

        // 其他节点类型可以扩展
        return null;
    }

    /**
     * 渲染字段组件
     */
    renderFieldComponent(component) {
        // 从schema中查找字段定义
        const fieldDef = this.schema.fieldDefs?.find(f => f.id === component.fieldDefId);

        if (!fieldDef) {
            console.warn('未找到字段定义:', component.fieldDefId);
            return null;
        }

        const div = document.createElement('div');
        div.className = 'mb-3';
        div.setAttribute('data-component-id', component.id);

        const labelText = component.labelName || fieldDef.fieldNameCn || fieldDef.fieldPath;
        const placeholder = component.placeholder || '';
        const required = component.required || false;

        // 添加必填标记
        const labelClass = required ? 'form-label required-label' : 'form-label';

        // 根据组件类型渲染不同的输入控件
        switch (component.componentType) {
            case 'TEXT_INPUT':
                div.innerHTML = `
                    <label class="${labelClass}">${labelText}</label>
                    <input type="text"
                           class="form-control"
                           id="field_${fieldDef.id}"
                           data-field-path="${fieldDef.fieldPath}"
                           data-field-id="${fieldDef.id}"
                           placeholder="${placeholder}"
                           ${required ? 'required' : ''}>
                `;
                break;

            case 'TEXTAREA':
                div.innerHTML = `
                    <label class="${labelClass}">${labelText}</label>
                    <textarea class="form-control"
                              id="field_${fieldDef.id}"
                              data-field-path="${fieldDef.fieldPath}"
                              data-field-id="${fieldDef.id}"
                              placeholder="${placeholder}"
                              rows="3"
                              ${required ? 'required' : ''}></textarea>
                `;
                break;

            case 'MONEY_INPUT':
                div.innerHTML = `
                    <label class="${labelClass}">${labelText}</label>
                    <div class="input-group">
                        <span class="input-group-text">¥</span>
                        <input type="number"
                               class="form-control"
                               id="field_${fieldDef.id}"
                               data-field-path="${fieldDef.fieldPath}"
                               data-field-id="${fieldDef.id}"
                               step="0.01"
                               min="0"
                               placeholder="${placeholder || '请输入金额'}"
                               ${required ? 'required' : ''}>
                    </div>
                `;
                break;

            case 'NUMBER_INPUT':
                div.innerHTML = `
                    <label class="${labelClass}">${labelText}</label>
                    <input type="number"
                           class="form-control"
                           id="field_${fieldDef.id}"
                           data-field-path="${fieldDef.fieldPath}"
                           data-field-id="${fieldDef.id}"
                           placeholder="${placeholder}"
                           ${required ? 'required' : ''}>
                `;
                break;

            case 'DATE_PICKER':
                div.innerHTML = `
                    <label class="${labelClass}">${labelText}</label>
                    <input type="date"
                           class="form-control"
                           id="field_${fieldDef.id}"
                           data-field-path="${fieldDef.fieldPath}"
                           data-field-id="${fieldDef.id}"
                           ${required ? 'required' : ''}>
                `;
                break;

            case 'SELECT':
                div.innerHTML = `
                    <label class="${labelClass}">${labelText}</label>
                    <select class="form-select"
                            id="field_${fieldDef.id}"
                            data-field-path="${fieldDef.fieldPath}"
                            data-field-id="${fieldDef.id}"
                            ${required ? 'required' : ''}>
                        <option value="">请选择</option>
                        ${this.renderSelectOptions(component.dataProviderId)}
                    </select>
                `;
                break;

            case 'RADIO':
                div.innerHTML = `
                    <label class="${labelClass}">${labelText}</label>
                    <div class="radio-group"
                         id="field_${fieldDef.id}"
                         data-field-path="${fieldDef.fieldPath}"
                         data-field-id="${fieldDef.id}">
                        ${this.renderRadioOptions(component.dataProviderId, fieldDef.id, required)}
                    </div>
                `;
                break;

            case 'CHECKBOX':
                div.innerHTML = `
                    <label class="${labelClass}">${labelText}</label>
                    <div class="checkbox-group"
                         id="field_${fieldDef.id}"
                         data-field-path="${fieldDef.fieldPath}"
                         data-field-id="${fieldDef.id}">
                        ${this.renderCheckboxOptions(component.dataProviderId, fieldDef.id, required)}
                    </div>
                `;
                break;

            case 'FILE_UPLOAD':
                div.innerHTML = `
                    <label class="${labelClass}">${labelText}</label>
                    <input type="file"
                           class="form-control"
                           id="field_${fieldDef.id}"
                           data-field-path="${fieldDef.fieldPath}"
                           data-field-id="${fieldDef.id}"
                           ${required ? 'required' : ''}>
                `;
                break;

            default:
                // 默认使用文本输入
                div.innerHTML = `
                    <label class="${labelClass}">${labelText}</label>
                    <input type="text"
                           class="form-control"
                           id="field_${fieldDef.id}"
                           data-field-path="${fieldDef.fieldPath}"
                           data-field-id="${fieldDef.id}"
                           placeholder="${placeholder}"
                           ${required ? 'required' : ''}>
                `;
                console.log('使用默认组件类型:', component.componentType);
        }

        return div;
    }

    /**
     * 渲染下拉选项
     */
    renderSelectOptions(dataProviderId) {
        if (!dataProviderId) {
            return `
                <option value="option1">选项1</option>
                <option value="option2">选项2</option>
                <option value="option3">选项3</option>
            `;
        }

        // 查找数据源配置
        const datasource = this.schema.dataSources?.find(d => d.id === dataProviderId);

        if (datasource && datasource.providerType === 'STATIC') {
            try {
                const data = JSON.parse(datasource.configJson || '{}');
                const options = Array.isArray(data) ? data : (data.options || data.children || []);
                if (Array.isArray(options) && options.length > 0) {
                    return options.map(opt =>
                        `<option value="${opt.value}">${opt.label}</option>`
                    ).join('');
                }
            } catch (e) {
                console.warn('解析静态数据源失败:', e);
            }
        }

        // 默认示例选项
        return `
            <option value="option1">选项1（示例）</option>
            <option value="option2">选项2（示例）</option>
        `;
    }

    /**
     * 渲染Radio选项
     */
    renderRadioOptions(dataProviderId, fieldId, required) {
        const options = this.getOptionsFromDatasource(dataProviderId);

        return options.map(opt => `
            <label>
                <input type="radio"
                       name="radio_${fieldId}"
                       value="${opt.value}"
                       data-field-path-group="${fieldId}"
                       ${required ? 'required' : ''}>
                ${opt.label}
            </label>
        `).join('');
    }

    /**
     * 渲染Checkbox选项
     */
    renderCheckboxOptions(dataProviderId, fieldId, required) {
        const options = this.getOptionsFromDatasource(dataProviderId);

        return options.map(opt => `
            <label>
                <input type="checkbox"
                       name="checkbox_${fieldId}"
                       value="${opt.value}"
                       data-field-path-group="${fieldId}">
                ${opt.label}
            </label>
        `).join('');
    }

    /**
     * 从数据源获取选项
     */
    getOptionsFromDatasource(dataProviderId) {
        if (!dataProviderId) {
            return [
                { value: 'option1', label: '选项1' },
                { value: 'option2', label: '选项2' },
                { value: 'option3', label: '选项3' }
            ];
        }

        const datasource = this.schema.dataSources?.find(d => d.id === dataProviderId);

        if (datasource && datasource.providerType === 'STATIC') {
            try {
                const data = JSON.parse(datasource.configJson || '{}');
                const options = Array.isArray(data) ? data : (data.options || data.children || []);
                if (Array.isArray(options)) {
                    return options;
                }
            } catch (e) {
                console.warn('解析数据源失败:', e);
            }
        }

        // 默认示例
        return [
            { value: 'option1', label: '选项1（示例）' },
            { value: 'option2', label: '选项2（示例）' }
        ];
    }

    /**
     * 渲染动作按钮
     */
    renderAction(action) {
        const button = document.createElement('button');

        // 根据动作类型设置按钮样式
        let btnClass = 'btn btn-primary me-2';
        if (action.actionType === 'SAVE_BUTTON') {
            btnClass = 'btn btn-success me-2';
        } else if (action.actionType === 'QUERY_BUTTON') {
            btnClass = 'btn btn-info me-2';
        } else if (action.actionType === 'DELETE_BUTTON') {
            btnClass = 'btn btn-danger me-2';
        }

        button.className = btnClass;
        button.setAttribute('data-action-id', action.id);
        button.setAttribute('data-action-code', action.actionCode);
        button.setAttribute('data-action-type', action.actionType);
        button.textContent = action.actionName || '按钮';

        // 绑定点击事件
        button.addEventListener('click', () => {
            this.handleAction(action);
        });

        return button;
    }

    /**
     * 处理动作（验证按钮功能）
     */
    handleAction(action) {
        console.log('=== 动作执行 ===');
        console.log('动作ID:', action.id);
        console.log('动作编码:', action.actionCode);
        console.log('动作类型:', action.actionType);
        console.log('动作名称:', action.actionName);

        if (action.actionType === 'SAVE_BUTTON') {
            this.validateSaveAction();
        } else if (action.actionType === 'QUERY_BUTTON') {
            this.validateQueryAction(action);
        } else if (action.actionType === 'DELETE_BUTTON') {
            this.validateDeleteAction(action);
        } else {
            this.showToast('info', `动作执行: ${action.actionName}`);
        }
    }

    /**
     * 验证保存功能（模拟保存按钮）
     */
    validateSaveAction() {
        console.log('=== 验证保存功能 ===');
        console.log('当前formData:', this.formData);

        // 验证自动生成的字段编码
        Object.keys(this.formData).forEach(fieldPath => {
            console.log(`字段路径: ${fieldPath} = ${this.formData[fieldPath]}`);
        });

        // 验证字段数量
        console.log('已填写字段数量:', Object.keys(this.formData).length);

        this.showToast('success', '保存功能验证通过');

        // 显示详细数据
        this.showDataDetail('保存验证', this.formData);
    }

    /**
     * 验证查询功能（模拟查询按钮）
     */
    validateQueryAction(action) {
        console.log('=== 验证查询功能 ===');
        console.log('查询按钮编码:', action.actionCode);

        // 验证查询按钮绑定
        if (action.bindQueryId) {
            console.log('查询按钮已绑定查询ID:', action.bindQueryId);
            this.showToast('info', `查询已绑定: Query ID = ${action.bindQueryId}`);
        } else {
            console.log('查询按钮未绑定查询（预留功能）');
            this.showToast('info', '查询按钮未绑定查询');
        }
    }

    /**
     * 验证删除功能
     */
    validateDeleteAction(action) {
        console.log('=== 验证删除功能 ===');
        console.log('删除按钮编码:', action.actionCode);

        this.showToast('warning', '删除功能验证（仅模拟）');
    }

    /**
     * 绑定数据变化监听（实时更新数据面板）
     */
    bindDataListeners() {
        const inputs = document.querySelectorAll('[data-field-path]');

        inputs.forEach(input => {
            // change事件
            input.addEventListener('change', (e) => {
                this.updateFormData(e.target);
                this.updateDataPanel();
            });

            // input事件（实时更新）
            input.addEventListener('input', (e) => {
                this.updateFormData(e.target);
                this.updateDataPanel();
            });
        });

        // Radio/Checkbox组特殊处理
        const radioInputs = document.querySelectorAll('input[type="radio"][data-field-path-group]');
        radioInputs.forEach(input => {
            input.addEventListener('change', (e) => {
                const groupId = e.target.getAttribute('data-field-path-group');
                const selectedValue = e.target.value;

                // 更新formData
                const fieldDef = this.schema.fieldDefs?.find(f => f.id === groupId);
                if (fieldDef) {
                    this.formData[fieldDef.fieldPath] = selectedValue;
                }

                this.updateDataPanel();
            });
        });

        const checkboxInputs = document.querySelectorAll('input[type="checkbox"][data-field-path-group]');
        checkboxInputs.forEach(input => {
            input.addEventListener('change', (e) => {
                const groupId = e.target.getAttribute('data-field-path-group');

                // 获取所有选中值
                const checkedValues = [];
                document.querySelectorAll(`input[type="checkbox"][data-field-path-group="${groupId}"]:checked`)
                    .forEach(cb => checkedValues.push(cb.value));

                // 更新formData
                const fieldDef = this.schema.fieldDefs?.find(f => f.id === groupId);
                if (fieldDef) {
                    this.formData[fieldDef.fieldPath] = checkedValues;
                }

                this.updateDataPanel();
            });
        });
    }

    /**
     * 更新formData对象
     */
    updateFormData(input) {
        const fieldPath = input.getAttribute('data-field-path');
        if (!fieldPath) return;

        const value = input.value;

        // 如果是文件类型，存储文件信息
        if (input.type === 'file') {
            if (input.files.length > 0) {
                this.formData[fieldPath] = {
                    fileName: input.files[0].name,
                    fileSize: input.files[0].size,
                    fileType: input.files[0].type
                };
            } else {
                delete this.formData[fieldPath];
            }
        } else {
            this.formData[fieldPath] = value;
        }
    }

    /**
     * 更新数据面板显示
     */
    updateDataPanel() {
        const jsonDisplay = document.getElementById('formDataJson');
        jsonDisplay.textContent = JSON.stringify(this.formData, null, 2);
    }

    /**
     * 保存表单数据（模拟）
     */
    saveFormData() {
        console.log('=== 保存表单数据 ===');
        console.log('formData:', JSON.stringify(this.formData, null, 2));

        // 验证必填字段
        const requiredFields = document.querySelectorAll('[required]');
        let allValid = true;

        requiredFields.forEach(field => {
            if (!field.value || field.value.trim() === '') {
                field.classList.add('field-error');
                allValid = false;
            } else {
                field.classList.remove('field-error');
            }
        });

        if (!allValid) {
            this.showToast('error', '请填写所有必填字段');
            return;
        }

        // 模拟保存成功
        this.showToast('success', '表单数据保存成功');

        // 显示详细数据
        this.showDataDetail('表单数据', this.formData);
    }

    /**
     * 重置表单
     */
    resetForm() {
        // 清空所有输入
        document.querySelectorAll('[data-field-path]').forEach(input => {
            input.value = '';
            input.classList.remove('field-error');
        });

        // 清空radio/checkbox
        document.querySelectorAll('input[type="radio"], input[type="checkbox"]').forEach(input => {
            input.checked = false;
        });

        // 清空formData
        this.formData = {};

        // 更新数据面板
        this.updateDataPanel();

        this.showToast('info', '表单已重置');
    }

    /**
     * 复制表单数据
     */
    copyFormData() {
        const jsonData = JSON.stringify(this.formData, null, 2);

        // 使用Clipboard API
        if (navigator.clipboard) {
            navigator.clipboard.writeText(jsonData)
                .then(() => {
                    this.showToast('success', 'JSON数据已复制');
                })
                .catch(err => {
                    this.showToast('error', '复制失败: ' + err.message);
                });
        } else {
            // 备用方案
            const textarea = document.createElement('textarea');
            textarea.value = jsonData;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
            this.showToast('success', 'JSON数据已复制');
        }
    }

    /**
     * 显示Toast提示
     */
    showToast(type, message) {
        const toastContainer = document.getElementById('toastContainer');

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;

        let iconClass = 'success-icon';
        let iconSymbol = '✓';

        if (type === 'error') {
            iconClass = 'error-icon';
            iconSymbol = '';
        } else if (type === 'info') {
            iconClass = 'info-icon';
            iconSymbol = 'ℹ';
        } else if (type === 'warning') {
            iconClass = 'warning-icon';
            iconSymbol = '⚠';
        }

        toast.innerHTML = `
            <span class="${iconClass}">${iconSymbol}</span>
            <span class="toast-body">${message}</span>
        `;

        toastContainer.appendChild(toast);

        // 3秒后自动消失
        setTimeout(() => {
            toast.classList.add('fade-out');
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, 3000);
    }

    /**
     * 显示错误信息
     */
    showError(message) {
        const container = document.getElementById('formContainer');
        container.innerHTML = `
            <div class="error-state">
                <h5>加载失败</h5>
                <p>${message}</p>
                <button class="btn btn-primary mt-3" onclick="renderer.retry()">
                    重试
                </button>
                <button class="btn btn-outline-secondary mt-3 ms-2" onclick="window.location.href='/config.html'">
                    前往配置
                </button>
            </div>
        `;

        this.showToast('error', message);
    }

    /**
     * 重试加载
     */
    retry() {
        this.loadAndRender();
    }

    /**
     * 显示数据详情模态框
     */
    showDataDetail(title, data) {
        console.log(`${title}:`, data);

        // 创建一个简单的模态框显示详细数据
        const modalHtml = `
            <div class="modal fade" id="dataDetailModal" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">${title}</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <pre style="background: #f8f9fa; padding: 15px; border-radius: 5px;">
${JSON.stringify(data, null, 2)}
                            </pre>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // 移除旧模态框
        const oldModal = document.getElementById('dataDetailModal');
        if (oldModal) {
            oldModal.remove();
        }

        // 添加新模态框
        document.body.insertAdjacentHTML('beforeend', modalHtml);

        // 显示模态框
        const modal = new bootstrap.Modal(document.getElementById('dataDetailModal'));
        modal.show();

        // 关闭后移除
        document.getElementById('dataDetailModal').addEventListener('hidden.bs.modal', () => {
            document.getElementById('dataDetailModal').remove();
        });
    }
}

// 页面加载后自动初始化
document.addEventListener('DOMContentLoaded', () => {
    // 从URL参数获取templateId和versionId
    const urlParams = new URLSearchParams(window.location.search);
    const templateId = urlParams.get('templateId') || '1';
    const versionId = urlParams.get('versionId') || '1';

    console.log('初始化预览界面:', { templateId, versionId });

    // 初始化渲染器
    renderer = new PreviewRenderer(templateId, versionId);
    renderer.loadAndRender();
});