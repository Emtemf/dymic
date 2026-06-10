/**
 * 配置API
 * 处理配置的保存、加载、导入、导出等功能
 */

/**
 * 保存配置
 */
async function saveConfig() {
    try {
        const config = DesignerState.templateConfig;

        normalizeTemplateConfig(config);

        const validation = validateConfig(config);
        if (!validation.valid) {
            showNotification(validation.message, 'error');
            return;
        }

        const urlParams = new URLSearchParams(window.location.search);
        const templateId = urlParams.get('templateId') || DesignerState.templateId || '1001';
        const versionId = urlParams.get('versionId') || DesignerState.versionId || '2001';

        const saveData = {
            layoutNodes: extractLayoutNodes(config.rootComponent),
            fieldDefs: extractFieldDefs(config),
            fieldComponents: extractFieldComponents(config),
            queryConfigs: config.queryConfigs || [],
            actionConfigs: config.actionConfigs || []
        };

        // 调用后端API保存
        const response = await fetch(`/api/templates/${templateId}/versions/${versionId}/schema`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(saveData)
        });

        if (response.ok) {
            const result = await response.json();
            showNotification('配置保存成功', 'success');
            console.log('保存结果:', result);
        } else {
            const errorText = await response.text();
            throw new Error(`保存失败: ${response.status} ${errorText}`);
        }
    } catch (error) {
        console.error('保存配置失败:', error);
        showNotification('保存失败: ' + error.message, 'error');
    }
}

/**
 * 从组件树提取布局节点
 */
function extractLayoutNodes(component, parentId = null, result = [], usedCodes = {}, visitedNodeIds = new Set()) {
    if (!component) return result;

    const componentKey = String(component.id || component.code || '');
    if (componentKey && visitedNodeIds.has(componentKey)) {
        return result;
    }
    if (componentKey) {
        visitedNodeIds.add(componentKey);
    }

    let code = component.code || component.type.toLowerCase();
    if (usedCodes[code] !== undefined) {
        usedCodes[code]++;
        code = code + '_' + usedCodes[code];
    } else {
        usedCodes[code] = 0;
    }

    const propsObj = {};
    if (component.width !== undefined) propsObj.width = component.width;
    if (component.height !== undefined) propsObj.height = component.height;
    if (component.span !== undefined) propsObj.span = component.span;
    if (component.offset !== undefined) propsObj.offset = component.offset;
    if (component.gutter !== undefined) propsObj.gutter = component.gutter;
    if (component.type === 'GRID' && component.columns !== undefined) {
        propsObj.columns = component.columns;
    }
    if (component.type === 'DETAIL_TABLE' && component.columns !== undefined) {
        propsObj.columns = component.columns;
    }
    if (component.panels !== undefined) propsObj.panels = component.panels;
    if (component.tabs !== undefined) propsObj.tabs = component.tabs;
    if (component.gridColumn !== undefined) propsObj.gridColumn = component.gridColumn;
    if (component.gridRow !== undefined) propsObj.gridRow = component.gridRow;

    const node = {
        id: component.id,
        nodeCode: code,
        nodeName: component.name,
        nodeType: component.type,
        parentId: parentId,
        sortNo: result.length,
        propsJson: Object.keys(propsObj).length > 0 ? JSON.stringify(propsObj) : '{}'
    };
    result.push(node);

    (component.children || []).forEach(child => {
        extractLayoutNodes(child, component.id, result, usedCodes, visitedNodeIds);
    });

    (component.panels || []).forEach(panel => {
        (panel.children || []).forEach(child => {
            const nestedComponent = typeof child === 'string' ? null : child;
            if (nestedComponent) {
                extractLayoutNodes(nestedComponent, component.id, result, usedCodes, visitedNodeIds);
            }
        });
    });

    (component.tabs || []).forEach(tab => {
        (tab.children || []).forEach(child => {
            const nestedComponent = typeof child === 'string' ? null : child;
            if (nestedComponent) {
                extractLayoutNodes(nestedComponent, component.id, result, usedCodes, visitedNodeIds);
            }
        });
    });

    return result;
}

/**
 * 递归遍历组件树
 */
function traverseComponents(component, visitor) {
    const visitedNodeIds = new Set();

    function walk(node) {
        if (!node) {
            return;
        }
        const componentKey = String(node.id || node.code || '');
        if (componentKey && visitedNodeIds.has(componentKey)) {
            return;
        }
        if (componentKey) {
            visitedNodeIds.add(componentKey);
        }

        visitor(node);
        (node.children || []).forEach(walk);
        (node.tabs || []).forEach(tab => (tab.children || []).forEach(walk));
        (node.panels || []).forEach(panel => (panel.children || []).forEach(walk));
    }

    walk(component);
}

function createUniqueGeneratedFieldPath(basePath, usedFieldPaths) {
    const normalizedBasePath = basePath || 'field';
    let candidate = normalizedBasePath;
    let index = 1;
    while (usedFieldPaths.has(candidate)) {
        index += 1;
        candidate = `${normalizedBasePath}${index}`;
    }
    usedFieldPaths.add(candidate);
    return candidate;
}

function normalizeFieldDataType(dataType) {
    if (!dataType) {
        return null;
    }

    switch (String(dataType).toUpperCase()) {
        case 'TEXT':
        case 'STRING':
            return 'string';
        case 'NUMBER':
        case 'DECIMAL':
            return 'number';
        case 'DATE':
        case 'DATETIME':
            return 'date';
        case 'ARRAY':
            return 'array';
        default:
            return String(dataType).toLowerCase();
    }
}

function parseJsonObject(raw) {
    if (!raw) {
        return null;
    }
    try {
        const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw;
        return parsed && typeof parsed === 'object' ? parsed : null;
    } catch (error) {
        return null;
    }
}

function applySchemaFieldBindings(rootComponent, schemaData) {
    if (!rootComponent || !schemaData) {
        return;
    }

    const fieldDefByLayoutNodeId = new Map(
        (schemaData.fieldDefs || [])
            .filter(fieldDef => fieldDef.layoutNodeId != null)
            .map(fieldDef => [String(fieldDef.layoutNodeId), fieldDef])
    );
    const fieldDefById = new Map(
        (schemaData.fieldDefs || [])
            .filter(fieldDef => fieldDef.id != null)
            .map(fieldDef => [String(fieldDef.id), fieldDef])
    );

    traverseComponents(rootComponent, component => {
        const fieldComponent = Array.isArray(component.components) ? component.components[0] : null;
        const fieldDef = fieldDefByLayoutNodeId.get(String(component.id))
            || (fieldComponent?.fieldDefId != null ? fieldDefById.get(String(fieldComponent.fieldDefId)) : null);

        if (fieldDef) {
            component.fieldPath = fieldDef.fieldPath || component.fieldPath;
            component.dataType = normalizeFieldDataType(fieldDef.dataType) || component.dataType;
        }

        if (!fieldComponent) {
            return;
        }

        if (fieldComponent.labelName) {
            component.name = fieldComponent.labelName;
        }
        if (fieldComponent.placeholder) {
            component.placeholder = fieldComponent.placeholder;
        }
        if (fieldComponent.requiredRule) {
            component.required = true;
            component.requiredRule = fieldComponent.requiredRule;
        }
        if (fieldComponent.readonlyRule) {
            component.readonly = true;
            component.readonlyRule = fieldComponent.readonlyRule;
        }
        if (fieldComponent.visibleRule) {
            component.visibleRule = fieldComponent.visibleRule;
        }
        if (fieldComponent.dataProviderId != null) {
            component.dataProviderId = fieldComponent.dataProviderId;
        }

        const componentProps = parseJsonObject(fieldComponent.componentProps);
        if (componentProps) {
            Object.assign(component, componentProps);
        }
    });
}

function extractFieldComponentProps(component) {
    const excludedKeys = new Set([
        'id', 'type', 'name', 'code', 'description', 'children', 'tabs', 'panels',
        'width', 'height', 'span', 'offset', 'gutter', 'gridColumn', 'gridRow',
        'fieldPath', 'dataType', 'placeholder', 'required', 'readonly',
        'requiredRule', 'readonlyRule', 'visibleRule', 'dataProviderId', 'components', 'actions', 'parentId'
    ]);

    return Object.fromEntries(
        Object.entries(component)
            .filter(([key, value]) => !excludedKeys.has(key) && value !== undefined && value !== null && value !== '')
    );
}

function normalizeRequiredRule(required) {
    return required ? '{"required":true}' : null;
}

function normalizeReadonlyRule(readonly) {
    return readonly ? '{"readonly":true}' : null;
}

function normalizeVisibleRule(visibleRule) {
    return visibleRule || null;
}

function normalizeRequiredDefault(required) {
    return required ? 1 : 0;
}

function normalizeFieldSortNo(component, fallbackSortNo) {
    return component.sortNo != null ? component.sortNo : fallbackSortNo;
}

function normalizeFieldLabel(component) {
    return component.name || component.code || component.type;
}

function normalizeFieldPlaceholder(component) {
    return component.placeholder || null;
}

function normalizeFieldDataTypeForSave(component) {
    return component.dataType || 'string';
}

function normalizeFieldComponentTypeForSave(component) {
    return component.type;
}

function normalizeTemplateConfig(config) {
    if (!config || !config.rootComponent) {
        return;
    }

    const usedFieldPaths = new Set();
    traverseComponents(config.rootComponent, component => {
        if (component.fieldPath && String(component.fieldPath).trim()) {
            usedFieldPaths.add(String(component.fieldPath).trim());
        }
    });

    traverseComponents(config.rootComponent, component => {
        if (!component.type) {
            return;
        }
        if (!component.code || !String(component.code).trim()) {
            component.code = ComponentLibrary.createComponentCode(component.type, component.id);
        }
        if (!ComponentLibrary.requiresFieldPath(component.type)) {
            return;
        }
        if (!component.fieldPath || !String(component.fieldPath).trim()) {
            component.fieldPath = createUniqueGeneratedFieldPath(
                ComponentLibrary.createFieldPath(component.code),
                usedFieldPaths
            );
        } else {
            component.fieldPath = String(component.fieldPath).trim();
        }
        if (!component.dataType) {
            component.dataType = ComponentLibrary.getComponentDef(component.type)?.defaultConfig?.dataType || 'string';
        }
    });
}

/**
 * 提取字段定义
 */
function extractFieldDefs(config) {
    const fieldDefs = [];
    const processedPaths = new Set();

    traverseComponents(config.rootComponent, component => {
        if (component.fieldPath && !processedPaths.has(component.fieldPath)) {
            processedPaths.add(component.fieldPath);
            fieldDefs.push({
                id: component.id + '_field',
                layoutNodeId: component.id,
                fieldCode: component.code,
                fieldPath: component.fieldPath,
                fieldNameCn: normalizeFieldLabel(component),
                dataType: normalizeFieldDataTypeForSave(component),
                placeholder: normalizeFieldPlaceholder(component),
                requiredDefault: normalizeRequiredDefault(component.required),
                sortNo: normalizeFieldSortNo(component, fieldDefs.length)
            });
        }
    });

    return fieldDefs;
}

/**
 * 提取字段组件绑定
 */
function extractFieldComponents(config) {
    const components = [];

    traverseComponents(config.rootComponent, component => {
        if (component.fieldPath) {
            components.push({
                id: component.id + '_comp',
                fieldDefId: component.id + '_field',
                layoutNodeId: component.id,
                componentType: normalizeFieldComponentTypeForSave(component),
                labelName: normalizeFieldLabel(component),
                placeholder: normalizeFieldPlaceholder(component),
                requiredRule: normalizeRequiredRule(component.required),
                readonlyRule: normalizeReadonlyRule(component.readonly),
                visibleRule: normalizeVisibleRule(component.visibleRule),
                componentProps: JSON.stringify(extractFieldComponentProps(component)),
                dataProviderId: component.dataProviderId ? Number(component.dataProviderId) : null,
                sortNo: normalizeFieldSortNo(component, components.length)
            });
        }
    });

    return components;
}

/**
 * 加载配置
 */
async function loadConfig(templateId, versionId) {
    try {
        // 如果没有传入参数，从 URL 获取
        if (!templateId || !versionId) {
            const urlParams = new URLSearchParams(window.location.search);
            templateId = templateId || urlParams.get('templateId');
            versionId = versionId || urlParams.get('versionId');
        }

        if (!templateId || !versionId) {
            console.log('缺少 templateId 或 versionId，使用本地存储状态');
            restoreState();
            return;
        }

        const response = await fetch(`/api/templates/${templateId}/versions/${versionId}/schema`);

        if (response.ok) {
            const result = await response.json();

            // 保存到状态
            DesignerState.templateId = templateId;
            DesignerState.versionId = versionId;

            const schemaData = result.data || result;
            const rootComponent = buildComponentTree(schemaData);
            applySchemaFieldBindings(rootComponent, schemaData);

            DesignerState.templateConfig = {
                rootComponent: rootComponent,
                fieldDefs: schemaData.fieldDefs || [],
                fieldComponents: schemaData.fieldComponents || [],
                queryConfigs: schemaData.queryConfigs || [],
                actionConfigs: schemaData.actionConfigs || [],
                rules: schemaData.rules || [],
                dataProviders: schemaData.dataProviders || []
            };
            normalizeTemplateConfig(DesignerState.templateConfig);

            document.getElementById('templateName').textContent = schemaData.templateName || '已加载模板';

            renderPreview();
            saveState();

            showNotification('配置加载成功', 'success');
        } else {
            throw new Error(`加载失败: ${response.status}`);
        }
    } catch (error) {
        console.error('加载配置失败:', error);
        showNotification('加载失败: ' + error.message, 'error');
        // 失败时尝试恢复本地状态
        restoreState();
    }
}

/**
 * 从布局节点构建组件树
 */
function buildComponentTree(schemaData) {
    const layoutNodes = schemaData.layoutNodes || [];

    if (layoutNodes.length === 0) return null;

    const componentMap = {};

    function convertNode(node) {
        let props = parsePropsJson(node.propsJson);

        const component = {
            id: String(node.id),
            type: node.nodeType,
            name: node.nodeName,
            code: node.nodeCode,
            parentId: node.parentId,
            visibleRule: node.visibleRule,
            readonlyRule: node.readonlyRule,
            components: node.components || [],
            actions: node.actions || [],
            ...props,
            children: (node.children || []).map(c => convertNode(c))
        };

        componentMap[String(node.id)] = component;
        return component;
    }

    layoutNodes.forEach(node => convertNode(node));

    // 合并 ID 引用：panels[i].children 从 ID 字符串替换为完整对象
    // 如果 panels/tabs 子项为空但 component.children 有数据，将 children 分配到第一个面板/页签
    Object.values(componentMap).forEach(component => {
        if (component.panels && component.panels.length > 0) {
            component.panels.forEach(panel => {
                if (panel.children && panel.children.length > 0 && typeof panel.children[0] === 'string') {
                    panel.children = panel.children.map(id => componentMap[id]).filter(Boolean);
                }
            });
            // 兜底：panels 全空但 children 有数据 → 放入第一个面板
            const hasAnyPanelChild = component.panels.some(p => p.children && p.children.length > 0);
            if (!hasAnyPanelChild && component.children && component.children.length > 0) {
                component.panels[0].children = [...component.children];
            }
        }
        if (component.tabs && component.tabs.length > 0) {
            component.tabs.forEach(tab => {
                if (tab.children && tab.children.length > 0 && typeof tab.children[0] === 'string') {
                    tab.children = tab.children.map(id => componentMap[id]).filter(Boolean);
                }
            });
            const hasAnyTabChild = component.tabs.some(t => t.children && t.children.length > 0);
            if (!hasAnyTabChild && component.children && component.children.length > 0) {
                component.tabs[0].children = [...component.children];
            }
        }
    });

    return layoutNodes[0] ? componentMap[String(layoutNodes[0].id)] : null;
}

/**
 * Parse propsJson handling multi-encoding and malformed JSON
 */
function parsePropsJson(raw) {
    if (!raw) return {};

    let parsed = raw;
    let iterations = 0;
    const maxIterations = 5;

    // Keep parsing until we get a non-string object
    while (typeof parsed === 'string' && iterations < maxIterations) {
        iterations++;
        try {
            parsed = JSON.parse(parsed);
        } catch(e) {
            // Try to fix common malformations:
            // 1. Extra trailing braces: {"columns":[...]}}
            // 2. Extra quotes wrapping

            let fixed = parsed.trim();

            // Remove trailing extra closing braces/brackets
            // Count opening vs closing and remove excess
            let openBraces = (fixed.match(/\{/g) || []).length;
            let closeBraces = (fixed.match(/\}/g) || []).length;
            let openBrackets = (fixed.match(/\[/g) || []).length;
            let closeBrackets = (fixed.match(/\]/g) || []).length;

            // Remove excess closing braces
            while (closeBraces > openBraces && fixed.endsWith('}')) {
                fixed = fixed.slice(0, -1);
                closeBraces--;
            }
            // Remove excess closing brackets
            while (closeBrackets > openBrackets && fixed.endsWith(']')) {
                fixed = fixed.slice(0, -1);
                closeBrackets--;
            }

            try {
                parsed = JSON.parse(fixed);
                break;
            } catch(e2) {
                // If still failing, return empty object
                console.warn('propsJson parse failed after fix attempt:', e2.message, 'raw:', raw.substring(0, 100));
                return {};
            }
        }
    }

    if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
        return parsed;
    }

    return {};
}

/**
 * 导出配置
 */
function exportConfig() {
    try {
        const config = DesignerState.templateConfig;
        const json = JSON.stringify(config, null, 2);

        // 创建下载链接
        const blob = new Blob([json], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `template-config-${Date.now()}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        showNotification('配置导出成功', 'success');
    } catch (error) {
        console.error('导出配置失败:', error);
        showNotification('导出失败: ' + error.message, 'error');
    }
}

/**
 * 导入配置
 */
function importConfig() {
    // 创建文件输入
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';

    input.onchange = async (e) => {
        try {
            const file = e.target.files[0];
            if (!file) return;

            const text = await file.text();
            const config = JSON.parse(text);

            normalizeTemplateConfig(config);

            const validation = validateConfig(config);
            if (!validation.valid) {
                showNotification(validation.message, 'error');
                return;
            }

            DesignerState.templateConfig = config;
            renderPreview();
            saveState();

            showNotification('配置导入成功', 'success');
        } catch (error) {
            console.error('导入配置失败:', error);
            showNotification('导入失败: ' + error.message, 'error');
        }
    };

    input.click();
}

/**
 * 验证配置
 */
function validateConfig(config) {
    if (!config) {
        return { valid: false, message: '配置不能为空' };
    }

    normalizeTemplateConfig(config);

    // 验证根组件
    if (config.rootComponent) {
        const result = validateComponent(config.rootComponent);
        if (!result.valid) {
            return result;
        }
    }

    return { valid: true };
}

/**
 * 验证组件
 */
function validateComponent(component) {
    if (!component.type) {
        return { valid: false, message: '组件类型不能为空' };
    }

    const def = ComponentLibrary.getComponentDef(component.type);
    if (!def) {
        return { valid: false, message: `未知的组件类型: ${component.type}` };
    }

    if (!component.name || !component.name.trim()) {
        return { valid: false, message: '组件名称不能为空' };
    }

    if (!component.code || !component.code.trim()) {
        // Auto-generate code from type + timestamp instead of blocking
        component.code = (component.type || 'comp').toLowerCase() + '_' + Date.now();
    }

    // 验证子组件
    if (component.children && component.children.length > 0) {
        for (const child of component.children) {
            const result = validateComponent(child);
            if (!result.valid) {
                return result;
            }
        }
    }

    if (component.tabs) {
        for (const tab of component.tabs) {
            if (tab.children && tab.children.length > 0) {
                for (const child of tab.children) {
                    const result = validateComponent(child);
                    if (!result.valid) {
                        return result;
                    }
                }
            }
        }
    }

    if (component.panels) {
        for (const panel of component.panels) {
            if (panel.children && panel.children.length > 0) {
                for (const child of panel.children) {
                    const result = validateComponent(child);
                    if (!result.valid) {
                        return result;
                    }
                }
            }
        }
    }

    return { valid: true };
}

/**
 * 预览模板
 */
function previewTemplate() {
    const config = DesignerState.templateConfig;

    const validation = validateConfig(config);
    if (!validation.valid) {
        showNotification(validation.message, 'error');
        return;
    }

    const modal = document.getElementById('previewModal');
    const content = document.getElementById('previewModalContent');
    content.innerHTML = '';

    if (config.rootComponent) {
        const rootElement = renderComponent(config.rootComponent, 'preview');
        if (rootElement) {
            content.appendChild(rootElement);
        }
    }

    modal.classList.add('active');
}

/**
 * 发布模板
 */
async function publishTemplate() {
    try {
        const config = DesignerState.templateConfig;

        // 验证配置
        const validation = validateConfig(config);
        if (!validation.valid) {
            showNotification(validation.message, 'error');
            return;
        }

        if (!confirm('确定要发布此模板吗？发布后将创建新版本。')) {
            return;
        }

        // 调用后端API发布
        const response = await fetch('/api/template/publish', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                templateId: DesignerState.templateId,
                version: DesignerState.templateVersion,
                config: config
            })
        });

        if (response.ok) {
            const result = await response.json();
            DesignerState.templateVersion = result.version;
            showNotification('模板发布成功，版本: ' + result.version, 'success');
        } else {
            throw new Error('发布失败');
        }
    } catch (error) {
        console.error('发布模板失败:', error);
        showNotification('发布失败: ' + error.message, 'error');
    }
}

/**
 * 关闭模态框
 */
function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
    }
}

/**
 * 保存状态到本地存储
 */
function saveState() {
    try {
        const state = {
            templateConfig: DesignerState.templateConfig,
            templateId: DesignerState.templateId,
            templateVersion: DesignerState.templateVersion
        };
        localStorage.setItem('designer_state', JSON.stringify(state));
    } catch (error) {
        console.error('保存状态失败:', error);
    }
}

/**
 * 恢复状态
 */
function restoreState() {
    try {
        const stateStr = localStorage.getItem('designer_state');
        if (stateStr) {
            const state = JSON.parse(stateStr);
            const restoredConfig = state.templateConfig || { rootComponent: null };
            DesignerState.templateConfig = {
                ...restoredConfig,
                rules: restoredConfig.rules || [],
                dataProviders: restoredConfig.dataProviders || []
            };
            normalizeTemplateConfig(DesignerState.templateConfig);
            // Only restore templateId from localStorage if not already set from URL params
            if (!DesignerState.templateId && state.templateId) {
                DesignerState.templateId = state.templateId;
            }
            DesignerState.templateVersion = state.templateVersion || '1.0.0';
        }
    } catch (error) {
        console.error('恢复状态失败:', error);
    }
}

/**
 * 加载统一数据源列表
 */
async function loadUnifiedDataSources() {
    const response = await fetch('/api/v2/ui/data-sources/query');
    const result = await response.json();
    if (!result.success) {
        throw new Error(result.message || '加载统一数据源失败');
    }
    return result.data || [];
}
