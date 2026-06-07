/**
 * config.js - 模板配置界面核心逻辑
 *
 * 功能：
 * 1. 实时保存（500ms延迟）
 * 2. 乐观更新（立即更新UI，后台异步保存）
 * 3. 拖放逻辑
 * 4. 属性面板编辑
 * 5. 业务友好的数据源选择
 */

// ==================== 配置管理器 ====================

class ConfigManager {
    constructor() {
        this.templateId = null;
        this.versionId = null;
        this.saveTimer = null;
        this.pendingChanges = new Map();
        this.configTree = null;
        this.selectedElement = null;
        this.dataProviders = [];
        this.nodeSortNo = 0;
        this.fieldSortNo = 0;
        this.actionSortNo = 0;
    }

    /**
     * 实时保存（500ms延迟）
     */
    scheduleSave(type, id, changes) {
        const key = `${type}_${id}`;
        this.pendingChanges.set(key, { type, id, changes });

        if (this.saveTimer) {
            clearTimeout(this.saveTimer);
        }

        this.saveTimer = setTimeout(() => {
            this.flushPendingChanges();
        }, 500);

        this.showSaveStatus('saving', '正在保存...');
    }

    /**
     * 执行所有待保存的变更
     */
    async flushPendingChanges() {
        const changes = Array.from(this.pendingChanges.entries());
        this.pendingChanges.clear();

        if (changes.length === 0) return;

        for (const [key, { type, id, changes }] of changes) {
            try {
                await this.saveElement(type, id, changes);
                console.log(`[${type}] ${id} 保存成功`);
            } catch (error) {
                console.error(`[${type}] ${id} 保存失败:`, error);
                this.showNotification('保存失败: ' + error.message, 'error');
                this.showSaveStatus('error', '保存失败');
                return;
            }
        }

        this.showSaveStatus('saved', '已保存');
    }

    /**
     * 保存元素
     */
    async saveElement(type, id, data) {
        let url;
        switch (type) {
            case 'node':
                url = `/api/templates/${this.templateId}/versions/${this.versionId}/layout-nodes/${id}`;
                break;
            case 'fieldDef':
                url = `/api/templates/${this.templateId}/versions/${this.versionId}/field-defs/${id}`;
                break;
            case 'fieldComponent':
                url = `/api/templates/${this.templateId}/versions/${this.versionId}/field-components/${id}`;
                break;
            case 'action':
                url = `/api/templates/${this.templateId}/versions/${this.versionId}/action-configs/${id}`;
                break;
            default:
                throw new Error('未知的元素类型: ' + type);
        }

        const response = await axios.put(url, data);
        return response.data.data;
    }

    /**
     * 乐观更新
     */
    optimisticUpdate(type, id, changes) {
        // 1. 立即更新UI
        this.updateCanvasElement(type, id, changes);

        // 2. 后台异步保存
        this.scheduleSave(type, id, changes);
    }

    /**
     * 更新画布元素
     */
    updateCanvasElement(type, id, changes) {
        const element = document.querySelector(`[data-${type}-id="${id}"]`);
        if (!element) return;

        if (type === 'node') {
            if (changes.nodeName) {
                element.querySelector('.node-name').textContent = changes.nodeName;
            }
        } else if (type === 'fieldComponent') {
            if (changes.labelName) {
                element.querySelector('.field-label').textContent = changes.labelName;
            }
        }
    }

    /**
     * 显示保存状态
     */
    showSaveStatus(state, text) {
        const status = document.getElementById('saveStatus');
        const indicator = status.querySelector('.save-indicator');
        const textEl = status.querySelector('.save-text');

        indicator.className = 'save-indicator ' + state;
        textEl.textContent = text;

        status.classList.add('visible');

        if (state === 'saved' || state === 'error') {
            setTimeout(() => {
                status.classList.remove('visible');
            }, 3000);
        }
    }

    /**
     * 显示通知消息
     */
    showNotification(message, type) {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.innerHTML = `
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
            <span>${message}</span>
        `;
        document.body.appendChild(notification);

        setTimeout(() => {
            notification.remove();
        }, 3000);
    }
}

// ==================== 模板/版本选择 ====================

/**
 * 选择模板
 */
async function selectTemplate() {
    const modal = document.getElementById('templateModal');
    modal.classList.add('active');

    // 加载模板列表
    try {
        const response = await axios.get('/api/templates');
        const templates = response.data.data || [];

        const select = document.getElementById('templateSelect');
        select.innerHTML = '<option value="">请选择模板</option>';

        templates.forEach(t => {
            // 使用字符串ID避免JavaScript精度丢失
            select.innerHTML += `<option value="${String(t.id)}">${t.templateCode} - ${t.templateName}</option>`;
        });
    } catch (error) {
        configManager.showNotification('加载模板列表失败', 'error');
    }
}

/**
 * 加载模板版本
 */
async function loadTemplateVersions() {
    const templateId = document.getElementById('templateSelect').value;
    if (!templateId) return;

    try {
        const response = await axios.get(`/api/templates/${templateId}/versions`);
        const versions = response.data.data || [];

        const select = document.getElementById('versionSelect');
        select.innerHTML = '<option value="">请选择版本</option>';

        versions.forEach(v => {
            // 使用字符串ID避免JavaScript精度丢失
            select.innerHTML += `<option value="${String(v.id)}">版本 ${v.versionNo} (${v.versionStatus})</option>`;
        });
    } catch (error) {
        configManager.showNotification('加载版本列表失败', 'error');
    }
}

/**
 * 确认选择模板
 */
async function confirmTemplate() {
    const templateId = document.getElementById('templateSelect').value;
    const versionId = document.getElementById('versionSelect').value;

    if (!templateId || !versionId) {
        configManager.showNotification('请选择模板和版本', 'error');
        return;
    }

    configManager.templateId = templateId;
    configManager.versionId = versionId;

    // 更新显示
    const templateName = document.getElementById('templateSelect').selectedOptions[0].text;
    const versionName = document.getElementById('versionSelect').selectedOptions[0].text;
    document.getElementById('templateInfo').textContent = `${templateName} | ${versionName}`;

    // 关闭弹窗
    closeModal('templateModal');

    // 加载配置树
    await loadConfigTree();

    // 加载数据源列表
    await loadDataProviders();

    configManager.showNotification('模板加载成功', 'success');
}

/**
 * 加载配置树
 */
async function loadConfigTree() {
    try {
        const response = await axios.get(
            `/api/templates/${configManager.templateId}/versions/${configManager.versionId}/schema`
        );
        configManager.configTree = response.data.data;

        renderNodeTree();
    } catch (error) {
        configManager.showNotification('加载配置树失败', 'error');
        console.error(error);
    }
}

/**
 * 加载数据源列表
 */
async function loadDataProviders() {
    try {
        const response = await axios.get('/api/data-providers');
        configManager.dataProviders = response.data.data || [];
    } catch (error) {
        console.error('加载数据源列表失败:', error);
    }
}

// ==================== 节点树渲染 ====================

/**
 * 渲染节点树
 */
function renderNodeTree() {
    const container = document.getElementById('nodeTree');
    const placeholder = document.getElementById('canvasPlaceholder');

    if (!configManager.configTree || !configManager.configTree.layoutNodes || configManager.configTree.layoutNodes.length === 0) {
        placeholder.style.display = 'flex';
        container.innerHTML = '';
        return;
    }

    placeholder.style.display = 'none';
    container.innerHTML = '';

    // 渲染根节点
    configManager.configTree.layoutNodes.forEach(node => {
        const nodeElement = createNodeElement(node);
        container.appendChild(nodeElement);
    });
}

/**
 * 创建节点元素
 */
function createNodeElement(node) {
    const div = document.createElement('div');
    div.className = 'layout-node';
    div.setAttribute('data-node-id', node.id);

    div.innerHTML = `
        <div class="node-header" onclick="selectNode('${node.id}')">
            <i class="fas fa-square node-icon"></i>
            <span class="node-name">${node.nodeName}</span>
            <span class="node-code">${node.nodeCode}</span>
            <div class="node-actions">
                <button onclick="event.stopPropagation(); deleteNode('${node.id}')" title="删除">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        </div>
        <div class="node-content">
            <div class="field-list" id="fields-${node.id}">
                ${renderFields(node.children, node.components)}
            </div>
            <div class="action-list" id="actions-${node.id}">
                ${renderActions(node.actions || [])}
            </div>
        </div>
    `;

    // 绑定拖放事件
    const content = div.querySelector('.node-content');
    content.addEventListener('dragover', allowDrop);
    content.addEventListener('drop', handleDrop.bind(null, node.id));

    return div;
}

/**
 * 渲染字段列表
 */
function renderFields(fields, components) {
    // 使用components数组（从schema返回）
    const items = components || fields || [];
    if (items.length === 0) return '';

    return items.map(comp => {
        // components数组中的每个元素就是FieldComponent，包含componentType
        return `
            <div class="field-item" data-field-id="${comp.fieldDefId}" data-component-id="${comp.id}" onclick="selectField('${comp.fieldDefId}', '${comp.id}')">
                <i class="fas fa-${getFieldIcon(comp.componentType)} field-icon"></i>
                <span class="field-label">${comp.labelName || '未命名字段'}</span>
                <span class="field-type">${getFieldTypeName(comp.componentType)}</span>
            </div>
        `;
    }).join('');
}

/**
 * 渲染动作按钮列表
 */
function renderActions(actions) {
    if (!actions || actions.length === 0) return '';

    return actions.map(action => `
        <div class="action-item" data-action-id="${action.id}" onclick="selectAction('${action.id}')">
            <i class="fas fa-${action.actionType === 'SAVE_BUTTON' || action.actionType === 'SAVE' ? 'save' : 'search'}"></i>
            <span>${action.actionName}</span>
        </div>
    `).join('');
}

/**
 * 获取字段图标
 */
function getFieldIcon(type) {
    const icons = {
        'TEXT': 'font',
        'TEXT_INPUT': 'font',
        'NUMBER': 'sort-numeric-up',
        'NUMBER_INPUT': 'sort-numeric-up',
        'MONEY': 'dollar-sign',
        'MONEY_INPUT': 'dollar-sign',
        'DATE': 'calendar',
        'DATE_PICKER': 'calendar',
        'SELECT': 'list'
    };
    return icons[type] || 'font';
}

/**
 * 获取字段类型名称
 */
function getFieldTypeName(type) {
    const names = {
        'TEXT': '文本框',
        'TEXT_INPUT': '文本框',
        'NUMBER': '数字框',
        'NUMBER_INPUT': '数字框',
        'MONEY': '金额框',
        'MONEY_INPUT': '金额框',
        'DATE': '日期选择',
        'DATE_PICKER': '日期选择',
        'SELECT': '下拉框'
    };
    return names[type] || '文本框';
}

// ==================== 添加元素 ====================

/**
 * 添加根卡片
 */
async function addRootCard() {
    if (!configManager.templateId || !configManager.versionId) {
        configManager.showNotification('请先选择模板和版本', 'error');
        return;
    }

    try {
        const nodeName = '卡片' + (configManager.configTree?.layoutNodes?.length + 1 || 1);
        const response = await axios.post(
            `/api/templates/${configManager.templateId}/versions/${configManager.versionId}/layout-nodes`,
            {
                nodeName: nodeName,
                nodeType: 'CARD_CONTAINER',
                parentId: null,
                sortNo: ++configManager.nodeSortNo
            }
        );

        const newNode = response.data.data;

        // 更新配置树
        if (!configManager.configTree) {
            configManager.configTree = { layoutNodes: [], actionConfigs: [] };
        }
        configManager.configTree.layoutNodes.push(newNode);

        // 乐观更新：立即渲染
        const nodeElement = createNodeElement(newNode);
        document.getElementById('nodeTree').appendChild(nodeElement);
        document.getElementById('canvasPlaceholder').style.display = 'none';

        configManager.showNotification('卡片添加成功', 'success');
    } catch (error) {
        configManager.showNotification('添加卡片失败: ' + error.message, 'error');
    }
}

/**
 * 处理拖放
 */
function handleDrop(nodeId, event) {
    event.preventDefault();

    const componentType = event.dataTransfer.getData('componentType');
    if (!componentType) return;

    createComponent(nodeId, componentType);
}

/**
 * 允许拖放
 */
function allowDrop(event) {
    event.preventDefault();
}

/**
 * 创建组件
 */
async function createComponent(nodeId, componentType) {
    if (!configManager.templateId || !configManager.versionId) {
        configManager.showNotification('请先选择模板和版本', 'error');
        return;
    }

    try {
        if (componentType === 'CARD_CONTAINER' || componentType === 'SEPARATOR') {
            // 创建布局节点
            const nodeName = getComponentDisplayName(componentType);
            const response = await axios.post(
                `/api/templates/${configManager.templateId}/versions/${configManager.versionId}/layout-nodes`,
                {
                    nodeName: nodeName,
                    nodeType: componentType,
                    parentId: nodeId,
                    sortNo: ++configManager.nodeSortNo
                }
            );

            const newNode = response.data.data;

            // 乐观更新：立即添加到画布
            const nodeElement = createNodeElement(newNode);
            document.getElementById('nodeTree').appendChild(nodeElement);

            configManager.showNotification('组件添加成功', 'success');
        } else if (componentType === 'SAVE_BUTTON' || componentType === 'QUERY_BUTTON') {
            // 创建动作配置
            const response = await axios.post(
                `/api/templates/${configManager.templateId}/versions/${configManager.versionId}/action-configs`,
                {
                    actionName: getComponentDisplayName(componentType),
                    actionType: componentType,
                    bindNodeId: nodeId,
                    sortNo: ++configManager.actionSortNo
                }
            );

            const newAction = response.data.data;

            // 乐观更新：立即添加到节点
            const actionList = document.getElementById(`actions-${nodeId}`);
            const actionElement = document.createElement('div');
            actionElement.className = 'action-item';
            actionElement.setAttribute('data-action-id', newAction.id);
            actionElement.onclick = () => selectAction(newAction.id);
            actionElement.innerHTML = `
                <i class="fas fa-${componentType === 'SAVE_BUTTON' ? 'save' : 'search'}"></i>
                <span>${newAction.actionName}</span>
            `;
            actionList.appendChild(actionElement);

            configManager.showNotification('动作添加成功', 'success');
        } else {
            // 创建字段定义 + 字段组件
            const fieldNameCn = getComponentDisplayName(componentType);

            // 1. 创建字段定义
            const fieldDefResponse = await axios.post(
                `/api/templates/${configManager.templateId}/versions/${configManager.versionId}/field-defs`,
                {
                    fieldNameCn: fieldNameCn,
                    dataType: getDataType(componentType),
                    layoutNodeId: nodeId,
                    required: false
                }
            );

            const result = fieldDefResponse.data.data;
            const fieldDef = result.fieldDef;
            const fieldComponent = result.fieldComponent;

            // 乐观更新：立即添加字段到节点
            const fieldList = document.getElementById(`fields-${nodeId}`);
            const fieldElement = document.createElement('div');
            fieldElement.className = 'field-item';
            fieldElement.setAttribute('data-field-id', fieldDef.id);
            fieldElement.onclick = () => selectField(fieldDef.id);
            fieldElement.innerHTML = `
                <i class="fas fa-${getFieldIcon(componentType)} field-icon"></i>
                <span class="field-label">${fieldNameCn}</span>
                <span class="field-type">${getFieldTypeName(componentType)}</span>
            `;
            fieldList.appendChild(fieldElement);

            configManager.showNotification('字段添加成功', 'success');
        }
    } catch (error) {
        configManager.showNotification('添加组件失败: ' + error.message, 'error');
    }
}

/**
 * 获取组件显示名称
 */
function getComponentDisplayName(type) {
    const names = {
        'TEXT_INPUT': '文本框',
        'NUMBER_INPUT': '数字框',
        'MONEY_INPUT': '金额框',
        'DATE_PICKER': '日期选择',
        'SELECT': '下拉框',
        'CARD_CONTAINER': '卡片容器',
        'SEPARATOR': '分隔线',
        'SAVE_BUTTON': '保存按钮',
        'QUERY_BUTTON': '查询按钮'
    };
    return names[type] || type;
}

/**
 * 获取数据类型
 */
function getDataType(componentType) {
    const types = {
        'TEXT_INPUT': 'TEXT',
        'NUMBER_INPUT': 'NUMBER',
        'MONEY_INPUT': 'MONEY',
        'DATE_PICKER': 'DATE',
        'SELECT': 'TEXT'
    };
    return types[componentType] || 'TEXT';
}

// ==================== 选择元素 ====================

/**
 * 选择节点
 */
async function selectNode(nodeId) {
    configManager.selectedElement = { type: 'node', id: nodeId };

    // 更新选中状态
    document.querySelectorAll('.node-header').forEach(el => el.classList.remove('selected'));
    document.querySelector(`[data-node-id="${nodeId}"] > .node-header`)?.classList.add('selected');

    // 加载节点属性
    const node = configManager.configTree.layoutNodes.find(n => String(n.id) === String(nodeId));
    if (!node) return;

    renderNodePropertyPanel(node);
}

/**
 * 选择字段
 */
async function selectField(fieldDefId, componentId) {
    configManager.selectedElement = { type: 'fieldComponent', id: componentId, fieldDefId: fieldDefId };

    // 更新选中状态
    document.querySelectorAll('.field-item').forEach(el => el.classList.remove('selected'));
    document.querySelector(`[data-component-id="${componentId}"]`)?.classList.add('selected');

    // 从schema中找到字段定义和组件
    let fieldDef = null;
    let fieldComponent = null;

    // 从fieldDefs数组查找
    if (configManager.configTree.fieldDefs) {
        fieldDef = configManager.configTree.fieldDefs.find(fd => String(fd.id) === String(fieldDefId));
    }

    // 从fieldComponents数组查找
    if (configManager.configTree.fieldComponents) {
        fieldComponent = configManager.configTree.fieldComponents.find(fc => String(fc.id) === String(componentId));
    }

    if (!fieldDef || !fieldComponent) {
        console.error('Field not found:', { fieldDefId, componentId });
        return;
    }

    renderFieldPropertyPanel(fieldDef, fieldComponent);
}

/**
 * 选择动作
 */
async function selectAction(actionId) {
    configManager.selectedElement = { type: 'action', id: actionId };

    // 更新选中状态
    document.querySelectorAll('.action-item').forEach(el => el.classList.remove('selected'));
    document.querySelector(`[data-action-id="${actionId}"]`)?.classList.add('selected');

    // 从actionConfigs数组查找
    let action = null;
    if (configManager.configTree.actionConfigs) {
        action = configManager.configTree.actionConfigs.find(a => String(a.id) === String(actionId));
    }

    // 如果没找到，尝试从节点中查找
    if (!action && configManager.configTree.layoutNodes) {
        configManager.configTree.layoutNodes.forEach(node => {
            const found = node.actions?.find(a => String(a.id) === String(actionId));
            if (found) action = found;
        });
    }

    if (!action) return;

    renderActionPropertyPanel(action);
}

// ==================== 属性面板渲染 ====================

/**
 * 渲染节点属性面板
 */
function renderNodePropertyPanel(node) {
    const placeholder = document.getElementById('propertiesPlaceholder');
    const form = document.getElementById('propertyForm');

    placeholder.style.display = 'none';
    form.style.display = 'block';

    form.innerHTML = `
        <div class="property-section">
            <h4><i class="fas fa-info-circle"></i> 基本信息</h4>
            <div class="form-group">
                <label>节点名称</label>
                <input type="text" value="${node.nodeName}" onchange="updateNodeProperty('${node.id}', 'nodeName', this.value)">
            </div>
            <div class="form-group">
                <label>节点编码（自动生成）</label>
                <input type="text" value="${node.nodeCode}" class="read-only-field" readonly>
            </div>
            <div class="form-group">
                <label>节点路径（自动生成）</label>
                <input type="text" value="${node.nodePath}" class="read-only-field" readonly>
            </div>
        </div>
        <div class="property-section">
            <h4><i class="fas fa-th-large"></i> 布局配置</h4>
            <div class="form-group">
                <label>节点类型</label>
                <select onchange="updateNodeProperty('${node.id}', 'nodeType', this.value)">
                    <option value="CARD_CONTAINER" ${node.nodeType === 'CARD_CONTAINER' ? 'selected' : ''}>卡片容器</option>
                    <option value="SEPARATOR" ${node.nodeType === 'SEPARATOR' ? 'selected' : ''}>分隔线</option>
                </select>
            </div>
        </div>
    `;
}

/**
 * 渲染字段属性面板
 */
function renderFieldPropertyPanel(fieldDef, fieldComponent) {
    const placeholder = document.getElementById('propertiesPlaceholder');
    const form = document.getElementById('propertyForm');

    placeholder.style.display = 'none';
    form.style.display = 'block';

    // 组件类型映射（数据库存储值 -> 显示值）
    const componentType = fieldComponent?.componentType || 'TEXT';

    form.innerHTML = `
        <div class="property-section">
            <h4><i class="fas fa-info-circle"></i> 基本信息</h4>
            <div class="form-group">
                <label>字段标签</label>
                <input type="text" value="${fieldComponent?.labelName || ''}" onchange="updateFieldComponentProperty('${fieldComponent?.id}', 'labelName', this.value)">
            </div>
            <div class="form-group">
                <label>字段名称（自动生成）</label>
                <input type="text" value="${fieldDef.fieldNameCn}" class="read-only-field" readonly>
            </div>
            <div class="form-group">
                <label>字段编码（自动生成）</label>
                <input type="text" value="${fieldDef.fieldCode || ''}" class="read-only-field" readonly>
            </div>
            <div class="form-group">
                <label>字段路径（自动生成）</label>
                <input type="text" value="${fieldDef.fieldPath || ''}" class="read-only-field" readonly>
            </div>
        </div>
        <div class="property-section">
            <h4><i class="fas fa-cogs"></i> 组件配置</h4>
            <div class="form-group">
                <label>组件类型</label>
                <select onchange="updateFieldComponentProperty('${fieldComponent?.id}', 'componentType', this.value)">
                    <option value="TEXT" ${componentType === 'TEXT' ? 'selected' : ''}>文本框</option>
                    <option value="NUMBER" ${componentType === 'NUMBER' ? 'selected' : ''}>数字框</option>
                    <option value="MONEY" ${componentType === 'MONEY' ? 'selected' : ''}>金额框</option>
                    <option value="DATE" ${componentType === 'DATE' ? 'selected' : ''}>日期选择</option>
                    <option value="SELECT" ${componentType === 'SELECT' ? 'selected' : ''}>下拉框</option>
                </select>
            </div>
            <div class="form-group">
                <label>提示文字</label>
                <input type="text" value="${fieldComponent?.placeholder || ''}" onchange="updateFieldComponentProperty('${fieldComponent?.id}', 'placeholder', this.value)">
            </div>
            <div class="form-checkbox">
                <input type="checkbox" ${fieldDef.requiredDefault ? 'checked' : ''} onchange="updateFieldDefProperty('${fieldDef.id}', 'requiredDefault', this.checked)">
                <label>是否必填</label>
            </div>
        </div>
        ${componentType === 'SELECT' ? `
        <div class="property-section">
            <h4><i class="fas fa-database"></i> 数据源配置</h4>
            <div class="form-group">
                <label>数据源</label>
                <button class="btn btn-outline-primary btn-sm" onclick="showDataSourceDialog('${fieldComponent?.id}')">
                    <i class="fas fa-link"></i> 选择数据源
                </button>
                ${fieldComponent?.dataProviderId ? `<span style="margin-left: 8px;">已绑定</span>` : ''}
            </div>
        </div>
        ` : ''}
    `;
}

/**
 * 渲染动作属性面板
 */
function renderActionPropertyPanel(action) {
    const placeholder = document.getElementById('propertiesPlaceholder');
    const form = document.getElementById('propertyForm');

    placeholder.style.display = 'none';
    form.style.display = 'block';

    form.innerHTML = `
        <div class="property-section">
            <h4><i class="fas fa-info-circle"></i> 基本信息</h4>
            <div class="form-group">
                <label>动作名称</label>
                <input type="text" value="${action.actionName}" onchange="updateActionProperty('${action.id}', 'actionName', this.value)">
            </div>
            <div class="form-group">
                <label>动作类型</label>
                <select onchange="updateActionProperty('${action.id}', 'actionType', this.value)">
                    <option value="SAVE_BUTTON" ${action.actionType === 'SAVE_BUTTON' ? 'selected' : ''}>保存按钮</option>
                    <option value="QUERY_BUTTON" ${action.actionType === 'QUERY_BUTTON' ? 'selected' : ''}>查询按钮</option>
                </select>
            </div>
        </div>
    `;
}

// ==================== 属性更新 ====================

/**
 * 更新节点属性
 */
function updateNodeProperty(nodeId, property, value) {
    configManager.optimisticUpdate('node', String(nodeId), { [property]: value });
}

/**
 * 更新字段定义属性
 */
function updateFieldDefProperty(fieldDefId, property, value) {
    configManager.optimisticUpdate('fieldDef', String(fieldDefId), { [property]: value });
}

/**
 * 更新字段组件属性
 */
function updateFieldComponentProperty(fieldComponentId, property, value) {
    configManager.optimisticUpdate('fieldComponent', String(fieldComponentId), { [property]: value });
}

/**
 * 更新动作属性
 */
function updateActionProperty(actionId, property, value) {
    configManager.optimisticUpdate('action', String(actionId), { [property]: value });
}

// ==================== 数据源选择 ====================

/**
 * 显示数据源选择对话框
 */
function showDataSourceDialog(fieldComponentId) {
    configManager.currentFieldComponentId = fieldComponentId;

    const modal = document.getElementById('dataSourceModal');
    modal.classList.add('active');

    // 加载IT配置的数据源
    const select = document.getElementById('itDataSourceSelect');
    select.innerHTML = '<option value="">请选择</option>';

    configManager.dataProviders.forEach(dp => {
        select.innerHTML += `<option value="${dp.id}">${dp.providerName} (${dp.providerCode})</option>`;
    });
}

/**
 * 确认数据源选择
 */
async function confirmDataSource() {
    const fieldComponentId = configManager.currentFieldComponentId;

    const itDataSourceId = document.getElementById('itDataSourceSelect').value;
    const staticOptions = document.getElementById('staticOptionsJson').value;

    let request = {};

    if (itDataSourceId) {
        // 使用IT配置的数据源
        request.dataProviderId = parseInt(itDataSourceId);
        request.staticOptionsJson = null;
    } else if (staticOptions) {
        // 使用自定义选项
        request.dataProviderId = null;
        request.staticOptionsJson = staticOptions;
    } else {
        configManager.showNotification('请选择数据源或输入自定义选项', 'error');
        return;
    }

    try {
        await axios.put(
            `/api/templates/${configManager.templateId}/versions/${configManager.versionId}/field-components/${fieldComponentId}`,
            request
        );

        closeModal('dataSourceModal');
        configManager.showNotification('数据源设置成功', 'success');

        // 重新加载配置树
        await loadConfigTree();
    } catch (error) {
        configManager.showNotification('数据源设置失败: ' + error.message, 'error');
    }
}

// ==================== 删除操作 ====================

/**
 * 删除节点
 */
async function deleteNode(nodeId) {
    if (!confirm('确定删除此节点及其所有子元素吗？')) return;

    try {
        await axios.delete(
            `/api/templates/${configManager.templateId}/versions/${configManager.versionId}/layout-nodes/${nodeId}`
        );

        // 乐观更新：立即从画布移除
        const element = document.querySelector(`[data-node-id="${nodeId}"]`);
        if (element) element.remove();

        // 重新加载配置树
        await loadConfigTree();

        configManager.showNotification('节点删除成功', 'success');
    } catch (error) {
        configManager.showNotification('删除失败: ' + error.message, 'error');
    }
}

// ==================== 其他操作 ====================

/**
 * 关闭模态框
 */
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.remove('active');
}

/**
 * 保存全部
 */
async function saveAll() {
    await configManager.flushPendingChanges();
    configManager.showNotification('全部保存成功', 'success');
}

/**
 * 预览
 */
function preview() {
    if (!configManager.templateId || !configManager.versionId) {
        configManager.showNotification('请先选择模板和版本', 'error');
        return;
    }

    // 打开预览页面
    window.open(`/preview.html?templateId=${configManager.templateId}&versionId=${configManager.versionId}`, '_blank');
}

/**
 * 查看数据
 */
function viewData() {
    if (!configManager.templateId || !configManager.versionId) {
        configManager.showNotification('请先选择模板和版本', 'error');
        return;
    }

    // 打开数据查看页面
    window.open(`/display/dynamic-display.html?templateId=${configManager.templateId}&versionId=${configManager.versionId}`, '_blank');
}

// ==================== 初始化 ====================

// 创建配置管理器实例
const configManager = new ConfigManager();

// 绑定拖拽事件
document.addEventListener('DOMContentLoaded', () => {
    const componentItems = document.querySelectorAll('.component-item');

    componentItems.forEach(item => {
        item.addEventListener('dragstart', (e) => {
            e.dataTransfer.setData('componentType', item.getAttribute('data-type'));
        });
    });

    // 显示选择模板提示
    configManager.showNotification('请先选择模板和版本', 'info');
});

// 导出函数（供HTML调用）
window.selectTemplate = selectTemplate;
window.loadTemplateVersions = loadTemplateVersions;
window.confirmTemplate = confirmTemplate;
window.addRootCard = addRootCard;
window.selectNode = selectNode;
window.selectField = selectField;
window.selectAction = selectAction;
window.updateNodeProperty = updateNodeProperty;
window.updateFieldDefProperty = updateFieldDefProperty;
window.updateFieldComponentProperty = updateFieldComponentProperty;
window.updateActionProperty = updateActionProperty;
window.showDataSourceDialog = showDataSourceDialog;
window.confirmDataSource = confirmDataSource;
window.deleteNode = deleteNode;
window.closeModal = closeModal;
window.saveAll = saveAll;
window.preview = preview;
window.viewData = viewData;