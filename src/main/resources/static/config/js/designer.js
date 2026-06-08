/**
 * 设计器核心逻辑
 * 管理设计器状态、事件处理、功能协调
 */

// 从 URL 参数初始化
const _urlParams = new URLSearchParams(window.location.search);

// 设计器状态
const DesignerState = {
    templateId: _urlParams.get('templateId') || null,
    versionId: _urlParams.get('versionId') || null,
    templateVersion: '1.0.0',
    templateConfig: {
        rootComponent: null,
        fieldDefs: [],
        rules: [],
        dataProviders: []
    },
    selectedComponent: null,
    zoom: 100
};

/**
 * 初始化设计器
 */
function initDesigner() {
    // 恢复状态
    restoreState();

    // 渲染预览
    renderPreview();

    // 绑定事件
    bindEvents();

    // 加载模板（如果有）
    if (DesignerState.templateId) {
        loadConfig(DesignerState.templateId);
    }
}

/**
 * 绑定事件
 */
function bindEvents() {
    // 点击空白区域取消选中
    document.getElementById('previewCanvas').addEventListener('click', (e) => {
        if (e.target.id === 'previewCanvas' || e.target.id === 'previewContent') {
            clearPropertyPanel();
        }
    });

    // 键盘快捷键
    document.addEventListener('keydown', (e) => {
        // Ctrl+S 保存
        if (e.ctrlKey && e.key === 's') {
            e.preventDefault();
            saveConfig();
        }

        // Delete 删除选中组件
        if (e.key === 'Delete' && DesignerState.selectedComponent) {
            e.preventDefault();
            deleteSelectedComponent();
        }

        // Ctrl+C 复制
        if (e.ctrlKey && e.key === 'c' && DesignerState.selectedComponent) {
            e.preventDefault();
            duplicateComponent(DesignerState.selectedComponent.id);
        }

        // Ctrl+Z 撤销（暂未实现）
        if (e.ctrlKey && e.key === 'z') {
            e.preventDefault();
            // TODO: 实现撤销功能
        }
    });

    // 阻止拖拽到其他区域
    document.addEventListener('dragover', (e) => {
        if (!e.target.closest('#previewCanvas')) {
            e.preventDefault();
        }
    });

    document.addEventListener('drop', (e) => {
        if (!e.target.closest('#previewCanvas')) {
            e.preventDefault();
        }
    });
}

/**
 * Tab切换
 */
function switchTab(tabName) {
    // 更新Tab按钮状态
    const tabBtns = document.querySelectorAll('.tab-btn');
    tabBtns.forEach(btn => {
        btn.classList.remove('active');
        if (btn.onclick.toString().includes(tabName)) {
            btn.classList.add('active');
        }
    });

    // 更新Tab内容状态
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => {
        content.classList.remove('active');
    });

    const targetTab = document.getElementById(tabName + 'Tab');
    if (targetTab) {
        targetTab.classList.add('active');
    }
}

/**
 * 删除选中的组件
 */
function deleteSelectedComponent() {
    if (!DesignerState.selectedComponent) {
        showNotification('请先选择组件', 'warning');
        return;
    }

    if (confirm(`确定删除组件 "${DesignerState.selectedComponent.name}" 吗？`)) {
        deleteComponent(DesignerState.selectedComponent.id);
    }
}

/**
 * 显示规则配置
 */
function showRuleForm(component) {
    const emptyState = document.getElementById('emptyRuleState');
    const ruleForm = document.getElementById('ruleForm');

    // 检查是否有数据绑定
    const def = ComponentLibrary.getComponentDef(component.type);
    if (!def || (def.category === 'layout' || def.category === 'action')) {
        emptyState.style.display = 'block';
        ruleForm.style.display = 'none';
        return;
    }

    emptyState.style.display = 'none';
    ruleForm.style.display = 'block';

    // 查找现有规则
    const rules = DesignerState.templateConfig.rules.filter(r => r.componentId === component.id);

    // 可见性规则
    const visibilityRule = rules.find(r => r.type === 'visibility');
    if (visibilityRule) {
        document.getElementById('enableVisibility').checked = true;
        document.getElementById('visibilityConfig').style.display = 'block';
        document.getElementById('visibilityExpr').value = visibilityRule.expression || '';
    } else {
        document.getElementById('enableVisibility').checked = false;
        document.getElementById('visibilityConfig').style.display = 'none';
    }

    // 必填规则
    const requiredRule = rules.find(r => r.type === 'required');
    if (requiredRule) {
        document.getElementById('enableRequired').checked = true;
        document.getElementById('requiredConfig').style.display = 'block';
        document.getElementById('requiredExpr').value = requiredRule.expression || '';
    } else {
        document.getElementById('enableRequired').checked = false;
        document.getElementById('requiredConfig').style.display = 'none';
    }

    // 只读规则
    const readonlyRule = rules.find(r => r.type === 'readonly');
    if (readonlyRule) {
        document.getElementById('enableReadonly').checked = true;
        document.getElementById('readonlyConfig').style.display = 'block';
        document.getElementById('readonlyExpr').value = readonlyRule.expression || '';
    } else {
        document.getElementById('enableReadonly').checked = false;
        document.getElementById('readonlyConfig').style.display = 'none';
    }
}

/**
 * 切换规则区域
 */
function toggleRuleSection(ruleType, enabled) {
    const configDiv = document.getElementById(ruleType + 'Config');
    if (enabled) {
        configDiv.style.display = 'block';
    } else {
        configDiv.style.display = 'none';
        // 移除规则
        removeRule(ruleType);
    }
}

/**
 * 更新规则
 */
function updateRule(ruleType, field, value) {
    if (!DesignerState.selectedComponent) return;

    const componentId = DesignerState.selectedComponent.id;

    // 查找现有规则
    const existingRule = DesignerState.templateConfig.rules.find(
        r => r.componentId === componentId && r.type === ruleType
    );

    if (existingRule) {
        existingRule[field] = value;
    } else {
        // 创建新规则
        DesignerState.templateConfig.rules.push({
            id: 'rule_' + Date.now(),
            componentId: componentId,
            type: ruleType,
            expression: value,
            enabled: true
        });
    }

    saveState();
}

/**
 * 移除规则
 */
function removeRule(ruleType) {
    if (!DesignerState.selectedComponent) return;

    const componentId = DesignerState.selectedComponent.id;

    DesignerState.templateConfig.rules = DesignerState.templateConfig.rules.filter(
        r => !(r.componentId === componentId && r.type === ruleType)
    );

    saveState();
}

/**
 * 测试表达式
 */
function testExpression(ruleType) {
    const expression = document.getElementById(ruleType + 'Expr').value;

    if (!expression) {
        showNotification('请输入表达式', 'warning');
        return;
    }

    try {
        // 创建模拟数据
        const testData = {
            status: 'approved',
            amount: 10000,
            supplier: {
                name: '测试供应商',
                code: 'SUP001'
            }
        };

        // 执行表达式
        const result = eval(expression);

        showNotification(`表达式测试结果: ${result}`, 'success');
    } catch (error) {
        showNotification('表达式错误: ' + error.message, 'error');
    }
}

/**
 * 显示数据源配置
 */
function showDataSourceForm(component) {
    const container = document.getElementById('dataSourcesList');
    container.innerHTML = '';

    if (!DesignerState.templateConfig.dataProviders) {
        DesignerState.templateConfig.dataProviders = [];
    }

    // 渲染数据源列表
    DesignerState.templateConfig.dataProviders.forEach((provider, index) => {
        const item = document.createElement('div');
        item.className = 'data-source-item';
        item.innerHTML = `
            <div class="data-source-info">
                <div class="data-source-name">${provider.name}</div>
                <div class="data-source-type">${provider.type} - ${provider.id}</div>
            </div>
            <div class="data-source-actions">
                <button class="btn btn-sm btn-secondary" onclick="editDataSource(${index})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="removeDataSource(${index})">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;
        container.appendChild(item);
    });
}

/**
 * 添加数据源
 */
function addDataSource() {
    const modal = document.getElementById('dataSourceModal');
    modal.classList.add('active');

    // 清空配置
    document.getElementById('dataSourceType').value = '';
    document.getElementById('dataSourceConfig').innerHTML = '';
}

/**
 * 加载数据源选项
 */
function loadDataSourceOptions() {
    const type = document.getElementById('dataSourceType').value;
    const container = document.getElementById('dataSourceConfig');

    if (!type) {
        container.innerHTML = '';
        return;
    }

    // 根据类型渲染配置
    switch (type) {
        case 'static':
            container.innerHTML = `
                <div class="form-group">
                    <label>数据源名称</label>
                    <input type="text" id="dsName" value="静态数据源">
                </div>
                <div class="form-group">
                    <label>数据(JSON格式)</label>
                    <textarea id="dsData" rows="4">[
                        {"value": "1", "label": "选项1"},
                        {"value": "2", "label": "选项2"}
                    ]</textarea>
                </div>
            `;
            break;
        case 'dict':
            container.innerHTML = `
                <div class="form-group">
                    <label>数据源名称</label>
                    <input type="text" id="dsName" value="字典数据源">
                </div>
                <div class="form-group">
                    <label>字典类型</label>
                    <input type="text" id="dsDictType" placeholder="如: CONTRACT_TYPE">
                </div>
            `;
            break;
        case 'http':
            container.innerHTML = `
                <div class="form-group">
                    <label>数据源名称</label>
                    <input type="text" id="dsName" value="HTTP数据源">
                </div>
                <div class="form-group">
                    <label>接口URL</label>
                    <input type="text" id="dsUrl" placeholder="如: /api/suppliers/list">
                </div>
                <div class="form-group">
                    <label>请求方法</label>
                    <select id="dsMethod">
                        <option value="GET">GET</option>
                        <option value="POST">POST</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>数据映射</label>
                    <textarea id="dsMapping" rows="2">
                        {
                            "valueField": "id",
                            "labelField": "name"
                        }
                    </textarea>
                </div>
            `;
            break;
        case 'platform':
            container.innerHTML = `
                <div class="form-group">
                    <label>数据源名称</label>
                    <input type="text" id="dsName" value="平台数据源">
                </div>
                <div class="form-group">
                    <label>服务名称</label>
                    <input type="text" id="dsService" placeholder="如: supplier-service">
                </div>
                <div class="form-group">
                    <label>接口名称</label>
                    <input type="text" id="dsInterface" placeholder="如: querySupplierList">
                </div>
            `;
            break;
        case 'internal':
            container.innerHTML = `
                <div class="form-group">
                    <label>数据源名称</label>
                    <input type="text" id="dsName" value="内部数据源">
                </div>
                <div class="form-group">
                    <label>内部服务ID</label>
                    <input type="text" id="dsInternalId" placeholder="如: internal_001">
                </div>
            `;
            break;
    }
}

/**
 * 确认数据源
 */
function confirmDataSource() {
    const type = document.getElementById('dataSourceType').value;

    if (!type) {
        showNotification('请选择数据源类型', 'warning');
        return;
    }

    const name = document.getElementById('dsName').value;
    if (!name) {
        showNotification('请输入数据源名称', 'warning');
        return;
    }

    // 创建数据源配置
    const provider = {
        id: 'provider_' + Date.now(),
        name: name,
        type: type,
        config: {}
    };

    // 根据类型收集配置
    switch (type) {
        case 'static':
            provider.config.data = JSON.parse(document.getElementById('dsData').value);
            break;
        case 'dict':
            provider.config.dictType = document.getElementById('dsDictType').value;
            break;
        case 'http':
            provider.config.url = document.getElementById('dsUrl').value;
            provider.config.method = document.getElementById('dsMethod').value;
            provider.config.mapping = JSON.parse(document.getElementById('dsMapping').value);
            break;
        case 'platform':
            provider.config.service = document.getElementById('dsService').value;
            provider.config.interface = document.getElementById('dsInterface').value;
            break;
        case 'internal':
            provider.config.internalId = document.getElementById('dsInternalId').value;
            break;
    }

    // 添加到列表
    DesignerState.templateConfig.dataProviders.push(provider);

    // 刷新显示
    showDataSourceForm(DesignerState.selectedComponent);
    closeModal('dataSourceModal');
    saveState();

    showNotification('数据源添加成功', 'success');
}

/**
 * 编辑数据源
 */
function editDataSource(index) {
    const provider = DesignerState.templateConfig.dataProviders[index];

    const modal = document.getElementById('dataSourceModal');
    modal.classList.add('active');

    document.getElementById('dataSourceType').value = provider.type;
    loadDataSourceOptions();

    setTimeout(() => {
        document.getElementById('dsName').value = provider.name;

        // 根据类型填充配置
        switch (provider.type) {
            case 'static':
                document.getElementById('dsData').value = JSON.stringify(provider.config.data, null, 2);
                break;
            case 'dict':
                document.getElementById('dsDictType').value = provider.config.dictType;
                break;
            case 'http':
                document.getElementById('dsUrl').value = provider.config.url;
                document.getElementById('dsMethod').value = provider.config.method;
                document.getElementById('dsMapping').value = JSON.stringify(provider.config.mapping, null, 2);
                break;
            case 'platform':
                document.getElementById('dsService').value = provider.config.service;
                document.getElementById('dsInterface').value = provider.config.interface;
                break;
            case 'internal':
                document.getElementById('dsInternalId').value = provider.config.internalId;
                break;
        }
    }, 100);
}

/**
 * 删除数据源
 */
function removeDataSource(index) {
    if (confirm('确定删除此数据源吗？')) {
        DesignerState.templateConfig.dataProviders.splice(index, 1);
        showDataSourceForm(DesignerState.selectedComponent);
        saveState();
        showNotification('数据源删除成功', 'success');
    }
}

/**
 * 缩放功能
 */
function zoomIn() {
    DesignerState.zoom = Math.min(150, DesignerState.zoom + 10);
    applyZoom();
}

function zoomOut() {
    DesignerState.zoom = Math.max(50, DesignerState.zoom - 10);
    applyZoom();
}

function resetZoom() {
    DesignerState.zoom = 100;
    applyZoom();
}

function applyZoom() {
    const previewCanvas = document.getElementById('previewCanvas');
    previewCanvas.style.transform = `scale(${DesignerState.zoom / 100})`;
    previewCanvas.style.transformOrigin = 'top center';
}

// Auto-init disabled when TemplateSelector handles the flow
// TemplateSelector.init() calls initDesigner() when entering designer (step 3)
// document.addEventListener('DOMContentLoaded', initDesigner);