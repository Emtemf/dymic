/**
 * 动态渲染引擎
 * 根据模板配置动态渲染界面
 */

class DynamicRenderer {
    constructor(templateId, version) {
        this.templateId = templateId;
        this.version = version;
        this.config = null;
        this.formData = {};
        this.rules = [];
        this.dataProviders = [];
    }

    /**
     * 加载模板配置
     */
    async loadConfig() {
        try {
            const response = await fetch(`/api/template/config/${this.templateId}?version=${this.version}`);
            if (response.ok) {
                const result = await response.json();
                this.config = result.config;
                this.rules = result.config.rules || [];
                this.dataProviders = result.config.dataProviders || [];

                return true;
            } else {
                throw new Error('加载模板配置失败');
            }
        } catch (error) {
            console.error('加载模板配置失败:', error);
            return false;
        }
    }

    /**
     * 渲染模板
     */
    async render(containerId) {
        const container = document.getElementById(containerId);
        if (!container) {
            throw new Error('容器不存在');
        }

        // 加载配置
        const loaded = await this.loadConfig();
        if (!loaded) {
            container.innerHTML = '<div class="error">加载模板配置失败</div>';
            return;
        }

        // 渲染根组件
        if (this.config.rootComponent) {
            const html = this.renderComponent(this.config.rootComponent);
            container.innerHTML = html;
        } else {
            container.innerHTML = '<div class="empty">模板配置为空</div>';
        }

        // 应用规则
        this.applyRules();

        // 绑定事件
        this.bindEvents();
    }

    /**
     * 渲染组件
     */
    renderComponent(component, depth = 0) {
        if (!component) return '';

        switch (component.type) {
            case 'PAGE':
                return this.renderPage(component, depth);
            case 'CARD':
                return this.renderCard(component, depth);
            case 'GRID':
                return this.renderGrid(component, depth);
            case 'TAB':
                return this.renderTab(component, depth);
            case 'COLLAPSE':
                return this.renderCollapse(component, depth);
            case 'INPUT':
                return this.renderInput(component, depth);
            case 'SELECT':
                return this.renderSelect(component, depth);
            case 'DATE':
                return this.renderDate(component, depth);
            case 'NUMBER':
                return this.renderNumber(component, depth);
            case 'MONEY':
                return this.renderMoney(component, depth);
            case 'TEXTAREA':
                return this.renderTextarea(component, depth);
            case 'SUPPLIER_SELECT':
                return this.renderSupplierSelect(component, depth);
            case 'DETAIL_TABLE':
                return this.renderDetailTable(component, depth);
            case 'ATTACHMENT':
                return this.renderAttachment(component, depth);
            case 'IMAGE':
                return this.renderImage(component, depth);
            case 'BUTTON':
                return this.renderButton(component, depth);
            case 'QUERY_DIALOG':
                return this.renderQueryDialog(component, depth);
            case 'SAVE_BUTTON':
                return this.renderSaveButton(component, depth);
            case 'SUBMIT_BUTTON':
                return this.renderSubmitButton(component, depth);
            default:
                return '';
        }
    }

    /**
     * 渲染页面
     */
    renderPage(component, depth) {
        let html = '<div class="page-container">';
        if (component.children && component.children.length > 0) {
            component.children.forEach(child => {
                html += this.renderComponent(child, depth + 1);
            });
        }
        html += '</div>';
        return html;
    }

    /**
     * 渲染卡片
     */
    renderCard(component, depth) {
        let html = `
            <div class="card" id="card_${component.id}">
                <div class="card-header">
                    ${component.title || component.name}
                </div>
                <div class="card-body">
        `;

        if (component.children && component.children.length > 0) {
            component.children.forEach(child => {
                html += this.renderComponent(child, depth + 1);
            });
        }

        html += `
                </div>
            </div>
        `;

        return html;
    }

    /**
     * 渲染栅格
     */
    renderGrid(component, depth) {
        const columns = component.columns || 2;
        const gutter = component.gutter || 16;
        const colWidth = 100 / columns;

        let html = `
            <div class="grid-container" style="
                display: grid;
                grid-template-columns: repeat(${columns}, ${colWidth}%);
                gap: ${gutter}px;
            ">
        `;

        if (component.children && component.children.length > 0) {
            component.children.forEach(child => {
                html += `<div class="grid-item">${this.renderComponent(child, depth + 1)}</div>`;
            });
        }

        html += '</div>';
        return html;
    }

    /**
     * 渲染Tab
     */
    renderTab(component, depth) {
        let html = `
            <div class="tab-container" id="tab_${component.id}">
                <div class="tab-header">
        `;

        if (component.tabs && component.tabs.length > 0) {
            component.tabs.forEach((tab, index) => {
                html += `
                    <div class="tab-item ${index === 0 ? 'active' : ''}"
                         onclick="switchTab('${component.id}', ${index})">
                        ${tab.name}
                    </div>
                `;
            });
        }

        html += '</div><div class="tab-content">';

        if (component.tabs && component.tabs.length > 0) {
            component.tabs.forEach((tab, index) => {
                html += `
                    <div class="tab-pane ${index === 0 ? 'active' : ''}" id="tab_${component.id}_${index}">
                `;
                if (tab.children && tab.children.length > 0) {
                    tab.children.forEach(child => {
                        html += this.renderComponent(child, depth + 1);
                    });
                }
                html += '</div>';
            });
        }

        html += '</div></div>';
        return html;
    }

    /**
     * 渲染折叠面板
     */
    renderCollapse(component, depth) {
        let html = `
            <div class="collapse-container" id="collapse_${component.id}">
        `;

        if (component.panels && component.panels.length > 0) {
            component.panels.forEach((panel, index) => {
                html += `
                    <div class="collapse-panel">
                        <div class="panel-header" onclick="togglePanel('${component.id}', ${index})">
                            <i class="fas fa-chevron-right"></i>
                            ${panel.name}
                        </div>
                        <div class="panel-content ${index === 0 ? 'expanded' : ''}" id="panel_${component.id}_${index}">
                `;
                if (panel.children && panel.children.length > 0) {
                    panel.children.forEach(child => {
                        html += this.renderComponent(child, depth + 1);
                    });
                }
                html += '</div></div>';
            });
        }

        html += '</div>';
        return html;
    }

    /**
     * 渲染输入框
     */
    renderInput(component, depth) {
        const required = component.required ? '<span class="required">*</span>' : '';
        return `
            <div class="form-group" id="component_${component.id}">
                <label>${component.name}${required}</label>
                <input type="text"
                       id="input_${component.id}"
                       data-field="${component.fieldPath}"
                       placeholder="${component.placeholder || ''}"
                       maxlength="${component.maxLength || 200}"
                       ${component.readonly ? 'readonly' : ''}
                       onchange="updateFormData('${component.fieldPath}', this.value)"
                       class="${component.required ? 'required' : ''}">
            </div>
        `;
    }

    /**
     * 渲染下拉框
     */
    renderSelect(component, depth) {
        const required = component.required ? '<span class="required">*</span>' : '';

        // 加载选项数据
        let options = '';
        if (component.dataSourceId) {
            // TODO: 从数据源加载选项
            options = '<option value="">请选择</option>';
        } else {
            options = `<option value="">${component.placeholder || '请选择'}</option>`;
        }

        return `
            <div class="form-group" id="component_${component.id}">
                <label>${component.name}${required}</label>
                <select id="select_${component.id}"
                        data-field="${component.fieldPath}"
                        ${component.multiple ? 'multiple' : ''}
                        ${component.readonly ? 'disabled' : ''}
                        onchange="updateFormData('${component.fieldPath}', this.value)"
                        class="${component.required ? 'required' : ''}">
                    ${options}
                </select>
            </div>
        `;
    }

    /**
     * 渲染日期
     */
    renderDate(component, depth) {
        const required = component.required ? '<span class="required">*</span>' : '';
        return `
            <div class="form-group" id="component_${component.id}">
                <label>${component.name}${required}</label>
                <input type="date"
                       id="date_${component.id}"
                       data-field="${component.fieldPath}"
                       ${component.readonly ? 'readonly' : ''}
                       onchange="updateFormData('${component.fieldPath}', this.value)"
                       class="${component.required ? 'required' : ''}">
            </div>
        `;
    }

    /**
     * 渲染数字
     */
    renderNumber(component, depth) {
        const required = component.required ? '<span class="required">*</span>' : '';
        return `
            <div class="form-group" id="component_${component.id}">
                <label>${component.name}${required}</label>
                <input type="number"
                       id="number_${component.id}"
                       data-field="${component.fieldPath}"
                       placeholder="${component.placeholder || ''}"
                       min="${component.min || 0}"
                       max="${component.max || 999999}"
                       step="0.01"
                       ${component.readonly ? 'readonly' : ''}
                       onchange="updateFormData('${component.fieldPath}', this.value)"
                       class="${component.required ? 'required' : ''}">
            </div>
        `;
    }

    /**
     * 渲染金额
     */
    renderMoney(component, depth) {
        const required = component.required ? '<span class="required">*</span>' : '';
        return `
            <div class="form-group" id="component_${component.id}">
                <label>${component.name}${required}</label>
                <div class="money-input">
                    <span class="currency">${component.currency || 'CNY'}</span>
                    <input type="number"
                           id="money_${component.id}"
                           data-field="${component.fieldPath}"
                           placeholder="${component.placeholder || ''}"
                           min="${component.min || 0}"
                           max="${component.max || 999999999}"
                           step="0.01"
                           ${component.readonly ? 'readonly' : ''}
                           onchange="updateFormData('${component.fieldPath}', this.value)"
                           class="${component.required ? 'required' : ''}">
                </div>
            </div>
        `;
    }

    /**
     * 渲染多行文本
     */
    renderTextarea(component, depth) {
        const required = component.required ? '<span class="required">*</span>' : '';
        return `
            <div class="form-group" id="component_${component.id}">
                <label>${component.name}${required}</label>
                <textarea id="textarea_${component.id}"
                          data-field="${component.fieldPath}"
                          placeholder="${component.placeholder || ''}"
                          rows="${component.rows || 4}"
                          maxlength="${component.maxLength || 500}"
                          ${component.readonly ? 'readonly' : ''}
                          onchange="updateFormData('${component.fieldPath}', this.value)"
                          class="${component.required ? 'required' : ''}"></textarea>
            </div>
        `;
    }

    /**
     * 渲染供应商选择
     */
    renderSupplierSelect(component, depth) {
        const required = component.required ? '<span class="required">*</span>' : '';
        return `
            <div class="form-group" id="component_${component.id}">
                <label>${component.name}${required}</label>
                <div class="supplier-select">
                    <input type="text"
                           id="supplier_${component.id}"
                           data-field="${component.fieldPath}"
                           placeholder="${component.placeholder || ''}"
                           ${component.readonly ? 'readonly' : ''}
                           onchange="updateFormData('${component.fieldPath}', this.value)">
                    <button onclick="openSupplierDialog('${component.id}', '${component.dialogTitle}')">
                        <i class="fas fa-search"></i>
                    </button>
                </div>
            </div>
        `;
    }

    /**
     * 渲染明细表
     */
    renderDetailTable(component, depth) {
        let columnsHTML = '';
        if (component.columns && component.columns.length > 0) {
            component.columns.forEach(col => {
                columnsHTML += `<th style="width: ${col.width}px;">${col.name}</th>`;
            });
        }

        return `
            <div class="detail-table" id="detail_${component.id}">
                <table>
                    <thead>
                        <tr>${columnsHTML}</tr>
                    </thead>
                    <tbody id="detail_body_${component.id}">
                        <!-- 动态添加行 -->
                    </tbody>
                    ${component.showSummary ? '<tfoot><tr class="summary-row"></tr></tfoot>' : ''}
                </table>
                <div class="detail-actions">
                    ${component.enableAdd ? `<button onclick="addDetailRow('${component.id}')"><i class="fas fa-plus"></i> 添加行</button>` : ''}
                    ${component.enableDelete ? `<button onclick="deleteDetailRow('${component.id}')"><i class="fas fa-trash"></i> 删除行</button>` : ''}
                </div>
            </div>
        `;
    }

    /**
     * 渲染附件
     */
    renderAttachment(component, depth) {
        const required = component.required ? '<span class="required">*</span>' : '';
        return `
            <div class="form-group" id="component_${component.id}">
                <label>${component.name}${required}</label>
                <div class="attachment-upload">
                    <div class="upload-area" onclick="triggerUpload('${component.id}')">
                        <i class="fas fa-cloud-upload-alt"></i>
                        <p>点击或拖拽文件上传</p>
                        <p class="upload-tip">支持格式: ${component.acceptTypes || '.pdf,.doc,.docx,.xls,.xlsx'}</p>
                    </div>
                    <input type="file" id="file_${component.id}"
                           accept="${component.acceptTypes || '.pdf,.doc,.docx,.xls,.xlsx'}"
                           multiple
                           onchange="handleFileUpload('${component.id}', '${component.fieldPath}', this.files)"
                           style="display: none;">
                    <div class="file-list" id="file_list_${component.id}"></div>
                </div>
            </div>
        `;
    }

    /**
     * 渲染图片
     */
    renderImage(component, depth) {
        const required = component.required ? '<span class="required">*</span>' : '';
        return `
            <div class="form-group" id="component_${component.id}">
                <label>${component.name}${required}</label>
                <div class="image-upload">
                    <div class="upload-area" onclick="triggerUpload('${component.id}')">
                        <i class="fas fa-images"></i>
                        <p>点击或拖拽图片上传</p>
                        <p class="upload-tip">支持格式: ${component.acceptTypes || '.jpg,.jpeg,.png,.gif'}</p>
                    </div>
                    <input type="file" id="file_${component.id}"
                           accept="${component.acceptTypes || '.jpg,.jpeg,.png,.gif'}"
                           multiple
                           onchange="handleFileUpload('${component.id}', '${component.fieldPath}', this.files)"
                           style="display: none;">
                    <div class="image-preview" id="image_preview_${component.id}"></div>
                </div>
            </div>
        `;
    }

    /**
     * 渲染按钮
     */
    renderButton(component, depth) {
        const buttonColors = {
            primary: '#667eea',
            secondary: '#6c757d',
            success: '#10b981',
            danger: '#ef4444',
            warning: '#f59e0b'
        };

        const color = buttonColors[component.type] || buttonColors.primary;

        return `
            <button class="action-button" style="background: ${color}"
                    onclick="executeAction('${component.id}', '${component.actionType}')">
                ${component.icon ? `<i class="fas ${component.icon}"></i>` : ''}
                ${component.text || '按钮'}
            </button>
        `;
    }

    /**
     * 渲染查询弹窗
     */
    renderQueryDialog(component, depth) {
        return `
            <button class="action-button" onclick="openQueryDialog('${component.id}')">
                <i class="fas fa-search"></i> ${component.text || '查询'}
            </button>
        `;
    }

    /**
     * 渲染保存按钮
     */
    renderSaveButton(component, depth) {
        return `
            <button class="action-button save-button" onclick="saveData('${component.apiEndpoint}')">
                <i class="fas fa-save"></i> ${component.text || '保存'}
            </button>
        `;
    }

    /**
     * 渲染提交按钮
     */
    renderSubmitButton(component, depth) {
        return `
            <button class="action-button submit-button" onclick="submitData('${component.apiEndpoint}')">
                <i class="fas fa-check"></i> ${component.text || '提交'}
            </button>
        `;
    }

    /**
     * 更新表单数据
     */
    updateFormData(fieldPath, value) {
        // 支持嵌套路径，如 supplier.name
        const parts = fieldPath.split('.');
        let target = this.formData;

        for (let i = 0; i < parts.length - 1; i++) {
            const part = parts[i];
            if (!target[part]) {
                target[part] = {};
            }
            target = target[part];
        }

        target[parts[parts.length - 1]] = value;

        // 应用规则
        this.applyRules();
    }

    /**
     * 应用规则
     */
    applyRules() {
        this.rules.forEach(rule => {
            if (!rule.enabled) return;

            const component = document.getElementById(`component_${rule.componentId}`);
            if (!component) return;

            try {
                const result = this.evaluateExpression(rule.expression);

                switch (rule.type) {
                    case 'visibility':
                        component.style.display = result ? 'block' : 'none';
                        break;
                    case 'required':
                        const input = component.querySelector('input, select, textarea');
                        if (input) {
                            input.required = result;
                            const label = component.querySelector('label');
                            if (label) {
                                const requiredSpan = label.querySelector('.required');
                                if (result && !requiredSpan) {
                                    label.innerHTML += '<span class="required">*</span>';
                                } else if (!result && requiredSpan) {
                                    requiredSpan.remove();
                                }
                            }
                        }
                        break;
                    case 'readonly':
                        const field = component.querySelector('input, select, textarea');
                        if (field) {
                            field.readOnly = result;
                            field.disabled = result;
                        }
                        break;
                }
            } catch (error) {
                console.error('规则执行错误:', error);
            }
        });
    }

    /**
     * 执行表达式
     */
    evaluateExpression(expression) {
        // 创建安全的执行环境
        const context = { data: this.formData };

        // 替换表达式中的字段引用
        const processedExpression = expression.replace(/(\w+)\.(\w+)/g, 'data.$1.$2');

        // 执行表达式
        return eval(processedExpression);
    }

    /**
     * 绑定事件
     */
    bindEvents() {
        // 全局函数绑定
        window.updateFormData = this.updateFormData.bind(this);
        window.switchTab = this.switchTab.bind(this);
        window.togglePanel = this.togglePanel.bind(this);
        window.openSupplierDialog = this.openSupplierDialog.bind(this);
        window.addDetailRow = this.addDetailRow.bind(this);
        window.deleteDetailRow = this.deleteDetailRow.bind(this);
        window.triggerUpload = this.triggerUpload.bind(this);
        window.handleFileUpload = this.handleFileUpload.bind(this);
        window.saveData = this.saveData.bind(this);
        window.submitData = this.submitData.bind(this);
    }

    /**
     * 切换Tab
     */
    switchTab(componentId, tabIndex) {
        // 更新Tab头部
        const tabHeader = document.querySelector(`#tab_${componentId} .tab-header`);
        const tabItems = tabHeader.querySelectorAll('.tab-item');
        tabItems.forEach((item, index) => {
            item.classList.toggle('active', index === tabIndex);
        });

        // 更新Tab内容
        const tabContent = document.querySelector(`#tab_${componentId} .tab-content`);
        const tabPanes = tabContent.querySelectorAll('.tab-pane');
        tabPanes.forEach((pane, index) => {
            pane.classList.toggle('active', index === tabIndex);
        });
    }

    /**
     * 切换面板
     */
    togglePanel(componentId, panelIndex) {
        const panel = document.getElementById(`panel_${componentId}_${panelIndex}`);
        const header = panel.previousElementSibling;

        panel.classList.toggle('expanded');
        const icon = header.querySelector('i');
        icon.classList.toggle('fa-chevron-right');
        icon.classList.toggle('fa-chevron-down');
    }

    /**
     * 打开供应商选择弹窗
     */
    openSupplierDialog(componentId, title) {
        // TODO: 实现供应商选择弹窗
        alert(`打开供应商选择弹窗: ${title}`);
    }

    /**
     * 添加明细行
     */
    addDetailRow(componentId) {
        const tbody = document.getElementById(`detail_body_${componentId}`);
        const rowIndex = tbody.children.length;

        // 创建新行
        const row = document.createElement('tr');
        row.innerHTML = `<td>${rowIndex + 1}</td>`;

        // TODO: 根据配置添加其他列

        tbody.appendChild(row);
    }

    /**
     * 删除明细行
     */
    deleteDetailRow(componentId) {
        const tbody = document.getElementById(`detail_body_${componentId}`);
        const rows = tbody.querySelectorAll('tr');

        if (rows.length > 0) {
            // 删除选中的行或最后一行
            const selectedRow = tbody.querySelector('tr.selected');
            if (selectedRow) {
                selectedRow.remove();
            } else {
                rows[rows.length - 1].remove();
            }
        }
    }

    /**
     * 触发文件上传
     */
    triggerUpload(componentId) {
        const fileInput = document.getElementById(`file_${componentId}`);
        fileInput.click();
    }

    /**
     * 处理文件上传
     */
    handleFileUpload(componentId, fieldPath, files) {
        // TODO: 实现文件上传逻辑
        console.log('文件上传:', files);
    }

    /**
     * 保存数据
     */
    async saveData(apiEndpoint) {
        try {
            const response = await fetch(apiEndpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(this.formData)
            });

            if (response.ok) {
                alert('保存成功');
            } else {
                throw new Error('保存失败');
            }
        } catch (error) {
            alert('保存失败: ' + error.message);
        }
    }

    /**
     * 提交数据
     */
    async submitData(apiEndpoint) {
        if (!confirm('确定提交吗？')) return;

        try {
            const response = await fetch(apiEndpoint, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(this.formData)
            });

            if (response.ok) {
                alert('提交成功');
            } else {
                throw new Error('提交失败');
            }
        } catch (error) {
            alert('提交失败: ' + error.message);
        }
    }
}

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = DynamicRenderer;
}