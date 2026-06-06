/**
 * 属性配置面板
 * 处理组件属性的显示和编辑
 */

/**
 * 选中组件
 */
function selectComponent(componentId) {
    // 取消之前的选中
    const previousSelected = document.querySelector('.component-preview.selected');
    if (previousSelected) {
        previousSelected.classList.remove('selected');
    }

    // 选中当前组件
    const element = document.querySelector(`[data-component-id="${componentId}"]`);
    if (element) {
        element.classList.add('selected');
    }

    // 查找组件配置
    const component = findComponentById(componentId);
    if (!component) return;

    // 更新状态
    DesignerState.selectedComponent = component;

    // 切换到属性配置Tab
    switchTab('properties');

    // 显示属性表单
    showPropertyForm(component);

    // 显示规则配置
    showRuleForm(component);

    // 显示数据源配置
    showDataSourceForm(component);
}

/**
 * 显示属性表单
 */
function showPropertyForm(component) {
    const emptyState = document.getElementById('emptyPropertyState');
    const propertyForm = document.getElementById('propertyForm');

    emptyState.style.display = 'none';
    propertyForm.style.display = 'block';

    // 填充基本属性
    document.getElementById('propName').value = component.name || '';
    document.getElementById('propCode').value = component.code || '';
    document.getElementById('propDescription').value = component.description || '';

    // 填充样式属性
    document.getElementById('propWidth').value = component.width || '';
    document.getElementById('propHeight').value = component.height || '';
    document.getElementById('propSpan').value = component.span || 24;
    document.getElementById('propOffset').value = component.offset || 0;

    // 填充数据绑定
    document.getElementById('propFieldPath').value = component.fieldPath || '';
    document.getElementById('propDataType').value = component.dataType || 'string';

    // 显示/隐藏数据绑定区域
    const dataBindingSection = document.getElementById('dataBindingSection');
    const def = ComponentLibrary.getComponentDef(component.type);
    if (def && (def.category === 'layout' || def.category === 'action')) {
        dataBindingSection.style.display = 'none';
    } else {
        dataBindingSection.style.display = 'block';
    }

    // 渲染组件特有配置
    renderComponentSpecificConfig(component);
}

/**
 * 渲染组件特有配置
 */
function renderComponentSpecificConfig(component) {
    const container = document.getElementById('componentSpecificConfig');
    container.innerHTML = '';

    const def = ComponentLibrary.getComponentDef(component.type);
    if (!def) return;

    // 根据组件类型渲染特有配置
    switch (component.type) {
        case 'INPUT':
            renderInputSpecificConfig(container, component);
            break;
        case 'SELECT':
            renderSelectSpecificConfig(container, component);
            break;
        case 'DATE':
            renderDateSpecificConfig(container, component);
            break;
        case 'NUMBER':
            renderNumberSpecificConfig(container, component);
            break;
        case 'MONEY':
            renderMoneySpecificConfig(container, component);
            break;
        case 'TEXTAREA':
            renderTextareaSpecificConfig(container, component);
            break;
        case 'DETAIL_TABLE':
            renderDetailTableSpecificConfig(container, component);
            break;
        case 'ATTACHMENT':
            renderAttachmentSpecificConfig(container, component);
            break;
        case 'IMAGE':
            renderImageSpecificConfig(container, component);
            break;
        case 'BUTTON':
            renderButtonSpecificConfig(container, component);
            break;
        case 'CARD':
            renderCardSpecificConfig(container, component);
            break;
        case 'GRID':
            renderGridSpecificConfig(container, component);
            break;
        case 'TAB':
            renderTabSpecificConfig(container, component);
            break;
        case 'COLLAPSE':
            renderCollapseSpecificConfig(container, component);
            break;
    }
}

/**
 * 输入框特有配置
 */
function renderInputSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>占位提示</label>
            <input type="text" id="configPlaceholder" value="${component.placeholder || ''}"
                   onchange="updateProperty('placeholder', this.value)">
        </div>
        <div class="form-group">
            <label>最大长度</label>
            <input type="number" id="configMaxLength" value="${component.maxLength || 200}"
                   onchange="updateProperty('maxLength', parseInt(this.value))">
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configRequired" ${component.required ? 'checked' : ''}
                       onchange="updateProperty('required', this.checked)">
                必填
            </label>
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configReadonly" ${component.readonly ? 'checked' : ''}
                       onchange="updateProperty('readonly', this.checked)">
                只读
            </label>
        </div>
    `;
}

/**
 * 下拉框特有配置
 */
function renderSelectSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>占位提示</label>
            <input type="text" id="configPlaceholder" value="${component.placeholder || ''}"
                   onchange="updateProperty('placeholder', this.value)">
        </div>
        <div class="form-group">
            <label>数据源类型</label>
            <select id="configDataSourceType" onchange="updateProperty('dataSourceType', this.value)">
                <option value="static" ${component.dataSourceType === 'static' ? 'selected' : ''}>静态数据</option>
                <option value="dict" ${component.dataSourceType === 'dict' ? 'selected' : ''}>字典</option>
                <option value="http" ${component.dataSourceType === 'http' ? 'selected' : ''}>HTTP接口</option>
            </select>
        </div>
        <div class="form-group">
            <label>数据源ID</label>
            <input type="text" id="configDataSourceId" value="${component.dataSourceId || ''}"
                   onchange="updateProperty('dataSourceId', this.value)">
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configMultiple" ${component.multiple ? 'checked' : ''}
                       onchange="updateProperty('multiple', this.checked)">
                多选
            </label>
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configRequired" ${component.required ? 'checked' : ''}
                       onchange="updateProperty('required', this.checked)">
                必填
            </label>
        </div>
    `;
}

/**
 * 日期特有配置
 */
function renderDateSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>占位提示</label>
            <input type="text" id="configPlaceholder" value="${component.placeholder || ''}"
                   onchange="updateProperty('placeholder', this.value)">
        </div>
        <div class="form-group">
            <label>日期格式</label>
            <input type="text" id="configFormat" value="${component.format || 'YYYY-MM-DD'}"
                   onchange="updateProperty('format', this.value)">
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configShowTime" ${component.showTime ? 'checked' : ''}
                       onchange="updateProperty('showTime', this.checked)">
                显示时间
            </label>
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configRequired" ${component.required ? 'checked' : ''}
                       onchange="updateProperty('required', this.checked)">
                必填
            </label>
        </div>
    `;
}

/**
 * 数字特有配置
 */
function renderNumberSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>占位提示</label>
            <input type="text" id="configPlaceholder" value="${component.placeholder || ''}"
                   onchange="updateProperty('placeholder', this.value)">
        </div>
        <div class="form-group">
            <label>最小值</label>
            <input type="number" id="configMin" value="${component.min || 0}"
                   onchange="updateProperty('min', parseFloat(this.value))">
        </div>
        <div class="form-group">
            <label>最大值</label>
            <input type="number" id="configMax" value="${component.max || 999999}"
                   onchange="updateProperty('max', parseFloat(this.value))">
        </div>
        <div class="form-group">
            <label>精度（小数位数）</label>
            <input type="number" id="configPrecision" value="${component.precision || 2}"
                   onchange="updateProperty('precision', parseInt(this.value))">
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configRequired" ${component.required ? 'checked' : ''}
                       onchange="updateProperty('required', this.checked)">
                必填
            </label>
        </div>
    `;
}

/**
 * 金额特有配置
 */
function renderMoneySpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>占位提示</label>
            <input type="text" id="configPlaceholder" value="${component.placeholder || ''}"
                   onchange="updateProperty('placeholder', this.value)">
        </div>
        <div class="form-group">
            <label>货币</label>
            <select id="configCurrency" onchange="updateProperty('currency', this.value)">
                <option value="CNY" ${component.currency === 'CNY' ? 'selected' : ''}>人民币 (CNY)</option>
                <option value="USD" ${component.currency === 'USD' ? 'selected' : ''}>美元 (USD)</option>
                <option value="EUR" ${component.currency === 'EUR' ? 'selected' : ''}>欧元 (EUR)</option>
                <option value="JPY" ${component.currency === 'JPY' ? 'selected' : ''}>日元 (JPY)</option>
            </select>
        </div>
        <div class="form-group">
            <label>精度（小数位数）</label>
            <input type="number" id="configPrecision" value="${component.precision || 2}"
                   onchange="updateProperty('precision', parseInt(this.value))">
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configRequired" ${component.required ? 'checked' : ''}
                       onchange="updateProperty('required', this.checked)">
                必填
            </label>
        </div>
    `;
}

/**
 * 多行文本特有配置
 */
function renderTextareaSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>占位提示</label>
            <input type="text" id="configPlaceholder" value="${component.placeholder || ''}"
                   onchange="updateProperty('placeholder', this.value)">
        </div>
        <div class="form-group">
            <label>行数</label>
            <input type="number" id="configRows" value="${component.rows || 4}"
                   onchange="updateProperty('rows', parseInt(this.value))">
        </div>
        <div class="form-group">
            <label>最大长度</label>
            <input type="number" id="configMaxLength" value="${component.maxLength || 500}"
                   onchange="updateProperty('maxLength', parseInt(this.value))">
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configRequired" ${component.required ? 'checked' : ''}
                       onchange="updateProperty('required', this.checked)">
                必填
            </label>
        </div>
    `;
}

/**
 * 明细表特有配置
 */
function renderDetailTableSpecificConfig(container, component) {
    let columnsHTML = '';
    if (component.columns && component.columns.length > 0) {
        component.columns.forEach((col, index) => {
            columnsHTML += `
                <div class="column-item" style="padding: 8px; background: white; border: 1px solid #e1e4e8; margin-bottom: 8px; border-radius: 4px;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span>${col.name} (${col.code})</span>
                        <button class="btn btn-sm btn-danger" onclick="removeColumn(${index})">删除</button>
                    </div>
                </div>
            `;
        });
    }

    container.innerHTML = `
        <div class="form-group">
            <label>列配置</label>
            ${columnsHTML}
            <button class="btn btn-sm btn-secondary" onclick="addColumn()">
                <i class="fas fa-plus"></i> 添加列
            </button>
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configEnableAdd" ${component.enableAdd ? 'checked' : ''}
                       onchange="updateProperty('enableAdd', this.checked)">
                允许添加行
            </label>
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configEnableDelete" ${component.enableDelete ? 'checked' : ''}
                       onchange="updateProperty('enableDelete', this.checked)">
                允许删除行
            </label>
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configEnableEdit" ${component.enableEdit ? 'checked' : ''}
                       onchange="updateProperty('enableEdit', this.checked)">
                允许编辑
            </label>
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configShowSummary" ${component.showSummary ? 'checked' : ''}
                       onchange="updateProperty('showSummary', this.checked)">
                显示合计行
            </label>
        </div>
    `;
}

/**
 * 附件特有配置
 */
function renderAttachmentSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>最大数量</label>
            <input type="number" id="configMaxCount" value="${component.maxCount || 10}"
                   onchange="updateProperty('maxCount', parseInt(this.value))">
        </div>
        <div class="form-group">
            <label>最大大小(MB)</label>
            <input type="number" id="configMaxSize" value="${(component.maxSize || 10485760) / 1048576}"
                   onchange="updateProperty('maxSize', parseFloat(this.value) * 1048576)">
        </div>
        <div class="form-group">
            <label>允许格式</label>
            <input type="text" id="configAcceptTypes" value="${component.acceptTypes || '.pdf,.doc,.docx'}"
                   onchange="updateProperty('acceptTypes', this.value)">
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configRequired" ${component.required ? 'checked' : ''}
                       onchange="updateProperty('required', this.checked)">
                必填
            </label>
        </div>
    `;
}

/**
 * 图片特有配置
 */
function renderImageSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>最大数量</label>
            <input type="number" id="configMaxCount" value="${component.maxCount || 5}"
                   onchange="updateProperty('maxCount', parseInt(this.value))">
        </div>
        <div class="form-group">
            <label>最大大小(MB)</label>
            <input type="number" id="configMaxSize" value="${(component.maxSize || 5242880) / 1048576}"
                   onchange="updateProperty('maxSize', parseFloat(this.value) * 1048576)">
        </div>
        <div class="form-group">
            <label>允许格式</label>
            <input type="text" id="configAcceptTypes" value="${component.acceptTypes || '.jpg,.jpeg,.png,.gif'}"
                   onchange="updateProperty('acceptTypes', this.value)">
        </div>
        <div class="form-group">
            <label>
                <input type="checkbox" id="configRequired" ${component.required ? 'checked' : ''}
                       onchange="updateProperty('required', this.checked)">
                必填
            </label>
        </div>
    `;
}

/**
 * 按钮特有配置
 */
function renderButtonSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>按钮文本</label>
            <input type="text" id="configText" value="${component.text || ''}"
                   onchange="updateProperty('text', this.value)">
        </div>
        <div class="form-group">
            <label>按钮类型</label>
            <select id="configType" onchange="updateProperty('type', this.value)">
                <option value="primary" ${component.type === 'primary' ? 'selected' : ''}>主要按钮</option>
                <option value="secondary" ${component.type === 'secondary' ? 'selected' : ''}>次要按钮</option>
                <option value="success" ${component.type === 'success' ? 'selected' : ''}>成功按钮</option>
                <option value="danger" ${component.type === 'danger' ? 'selected' : ''}>危险按钮</option>
                <option value="warning" ${component.type === 'warning' ? 'selected' : ''}>警告按钮</option>
            </select>
        </div>
        <div class="form-group">
            <label>图标类名</label>
            <input type="text" id="configIcon" value="${component.icon || ''}"
                   placeholder="如: fa-save"
                   onchange="updateProperty('icon', this.value)">
        </div>
        <div class="form-group">
            <label>动作类型</label>
            <select id="configActionType" onchange="updateProperty('actionType', this.value)">
                <option value="custom" ${component.actionType === 'custom' ? 'selected' : ''}>自定义</option>
                <option value="submit" ${component.actionType === 'submit' ? 'selected' : ''}>提交</option>
                <option value="reset" ${component.actionType === 'reset' ? 'selected' : ''}>重置</option>
            </select>
        </div>
    `;
}

/**
 * 卡片特有配置
 */
function renderCardSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>卡片标题</label>
            <input type="text" id="configTitle" value="${component.title || ''}"
                   onchange="updateProperty('title', this.value)">
        </div>
    `;
}

/**
 * 栅格特有配置
 */
function renderGridSpecificConfig(container, component) {
    container.innerHTML = `
        <div class="form-group">
            <label>列数</label>
            <input type="number" id="configColumns" value="${component.columns || 2}"
                   min="1" max="24"
                   onchange="updateProperty('columns', parseInt(this.value))">
        </div>
        <div class="form-group">
            <label>间距(px)</label>
            <input type="number" id="configGutter" value="${component.gutter || 16}"
                   onchange="updateProperty('gutter', parseInt(this.value))">
        </div>
    `;
}

/**
 * Tab特有配置
 */
function renderTabSpecificConfig(container, component) {
    let tabsHTML = '';
    if (component.tabs && component.tabs.length > 0) {
        component.tabs.forEach((tab, index) => {
            tabsHTML += `
                <div class="tab-item-config" style="padding: 8px; background: white; border: 1px solid #e1e4e8; margin-bottom: 8px; border-radius: 4px;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span>${tab.name}</span>
                        <button class="btn btn-sm btn-danger" onclick="removeTab(${index})">删除</button>
                    </div>
                </div>
            `;
        });
    }

    container.innerHTML = `
        <div class="form-group">
            <label>Tab页签</label>
            ${tabsHTML}
            <button class="btn btn-sm btn-secondary" onclick="addTab()">
                <i class="fas fa-plus"></i> 添加Tab
            </button>
        </div>
    `;
}

/**
 * 折叠面板特有配置
 */
function renderCollapseSpecificConfig(container, component) {
    let panelsHTML = '';
    if (component.panels && component.panels.length > 0) {
        component.panels.forEach((panel, index) => {
            panelsHTML += `
                <div class="panel-item-config" style="padding: 8px; background: white; border: 1px solid #e1e4e8; margin-bottom: 8px; border-radius: 4px;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span>${panel.name}</span>
                        <button class="btn btn-sm btn-danger" onclick="removePanel(${index})">删除</button>
                    </div>
                </div>
            `;
        });
    }

    container.innerHTML = `
        <div class="form-group">
            <label>面板</label>
            ${panelsHTML}
            <button class="btn btn-sm btn-secondary" onclick="addPanel()">
                <i class="fas fa-plus"></i> 添加面板
            </button>
        </div>
    `;
}

/**
 * 更新属性
 */
function updateProperty(property, value) {
    if (!DesignerState.selectedComponent) return;

    DesignerState.selectedComponent[property] = value;
    renderPreview();
    saveState();
}

/**
 * 清空属性面板
 */
function clearPropertyPanel() {
    const emptyState = document.getElementById('emptyPropertyState');
    const propertyForm = document.getElementById('propertyForm');

    emptyState.style.display = 'block';
    propertyForm.style.display = 'none';

    DesignerState.selectedComponent = null;
}

/**
 * 添加列（明细表）
 */
function addColumn() {
    if (!DesignerState.selectedComponent) return;

    if (!DesignerState.selectedComponent.columns) {
        DesignerState.selectedComponent.columns = [];
    }

    DesignerState.selectedComponent.columns.push({
        name: '新列',
        code: 'new_column',
        width: 100
    });

    renderComponentSpecificConfig(DesignerState.selectedComponent);
    renderPreview();
    saveState();
}

/**
 * 删除列（明细表）
 */
function removeColumn(index) {
    if (!DesignerState.selectedComponent) return;

    DesignerState.selectedComponent.columns.splice(index, 1);
    renderComponentSpecificConfig(DesignerState.selectedComponent);
    renderPreview();
    saveState();
}

/**
 * 添加Tab
 */
function addTab() {
    if (!DesignerState.selectedComponent) return;

    if (!DesignerState.selectedComponent.tabs) {
        DesignerState.selectedComponent.tabs = [];
    }

    DesignerState.selectedComponent.tabs.push({
        name: '新Tab',
        code: 'new_tab_' + Date.now(),
        children: []
    });

    renderComponentSpecificConfig(DesignerState.selectedComponent);
    renderPreview();
    saveState();
}

/**
 * 删除Tab
 */
function removeTab(index) {
    if (!DesignerState.selectedComponent) return;

    DesignerState.selectedComponent.tabs.splice(index, 1);
    renderComponentSpecificConfig(DesignerState.selectedComponent);
    renderPreview();
    saveState();
}

/**
 * 添加面板
 */
function addPanel() {
    if (!DesignerState.selectedComponent) return;

    if (!DesignerState.selectedComponent.panels) {
        DesignerState.selectedComponent.panels = [];
    }

    DesignerState.selectedComponent.panels.push({
        name: '新面板',
        code: 'new_panel_' + Date.now(),
        children: []
    });

    renderComponentSpecificConfig(DesignerState.selectedComponent);
    renderPreview();
    saveState();
}

/**
 * 删除面板
 */
function removePanel(index) {
    if (!DesignerState.selectedComponent) return;

    DesignerState.selectedComponent.panels.splice(index, 1);
    renderComponentSpecificConfig(DesignerState.selectedComponent);
    renderPreview();
    saveState();
}