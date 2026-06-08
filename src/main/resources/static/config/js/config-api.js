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

        // 验证配置
        const validation = validateConfig(config);
        if (!validation.valid) {
            showNotification(validation.message, 'error');
            return;
        }

        // 获取模板ID和版本ID（从URL参数或状态中）
        const urlParams = new URLSearchParams(window.location.search);
        const templateId = urlParams.get('templateId') || DesignerState.templateId || '1001';
        const versionId = urlParams.get('versionId') || DesignerState.versionId || '2001';

        // 构建保存数据
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
function extractLayoutNodes(component, parentId = null, result = [], usedCodes = {}) {
    if (!component) return result;

    let code = component.code || component.type.toLowerCase();
    if (usedCodes[code] !== undefined) {
        usedCodes[code]++;
        code = code + '_' + usedCodes[code];
    } else {
        usedCodes[code] = 0;
    }

    // Build props object with only defined values
    const propsObj = {};
    if (component.span !== undefined) propsObj.span = component.span;
    if (component.offset !== undefined) propsObj.offset = component.offset;
    if (component.gutter !== undefined) propsObj.gutter = component.gutter;
    if (component.columns !== undefined && component.columns !== null) propsObj.columns = component.columns;

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

    // 递归处理子组件
    if (component.children && component.children.length > 0) {
        component.children.forEach(child => {
            extractLayoutNodes(child, component.id, result, usedCodes);
        });
    }

    return result;
}

/**
 * 提取字段定义
 */
function extractFieldDefs(config) {
    const fieldDefs = [];
    const processedPaths = new Set();

    function collectFields(component) {
        if (!component) return;

        if (component.fieldPath && !processedPaths.has(component.fieldPath)) {
            processedPaths.add(component.fieldPath);
            fieldDefs.push({
                id: component.id + '_field',
                layoutNodeId: component.id,
                fieldCode: component.code,
                fieldPath: component.fieldPath,
                fieldNameCn: component.name,
                dataType: component.dataType || 'string'
            });
        }

        if (component.children) {
            component.children.forEach(collectFields);
        }
    }

    collectFields(config.rootComponent);
    return fieldDefs;
}

/**
 * 提取字段组件绑定
 */
function extractFieldComponents(config) {
    const components = [];

    function collectComponents(component) {
        if (!component) return;

        if (component.fieldPath) {
            components.push({
                id: component.id + '_comp',
                fieldDefId: component.id + '_field',
                layoutNodeId: component.id,
                componentType: component.type,
                propsJson: JSON.stringify({
                    placeholder: component.placeholder,
                    required: component.required,
                    readonly: component.readonly
                })
            });
        }

        if (component.children) {
            component.children.forEach(collectComponents);
        }
    }

    collectComponents(config.rootComponent);
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

            // 从后端数据重建组件树
            DesignerState.templateConfig = {
                rootComponent: buildComponentTree(result.data || result),
                fieldDefs: result.data?.fieldDefs || [],
                queryConfigs: result.data?.queryConfigs || [],
                actionConfigs: result.data?.actionConfigs || []
            };

            // 更新模板名称显示
            document.getElementById('templateName').textContent = result.data?.templateName || '已加载模板';

            // 渲染预览
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
    const fieldComponents = schemaData.fieldComponents || [];

    if (layoutNodes.length === 0) return null;

    // API returns a tree structure — convert directly to component format
    function convertNode(node) {
        let props = {};
        if (node.propsJson) {
            props = parsePropsJson(node.propsJson);
        }
        return {
            id: String(node.id),
            type: node.nodeType,
            name: node.nodeName,
            code: node.nodeCode,
            ...props,
            children: (node.children || []).map(c => convertNode(c))
        };
    }

    // Use the first root node
    return convertNode(layoutNodes[0]);
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

            // 验证配置
            const validation = validateConfig(config);
            if (!validation.valid) {
                showNotification(validation.message, 'error');
                return;
            }

            // 加载配置
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

    // 验证配置
    const validation = validateConfig(config);
    if (!validation.valid) {
        showNotification(validation.message, 'error');
        return;
    }

    // 打开预览窗口
    const modal = document.getElementById('previewModal');
    const frame = document.getElementById('previewFrame');

    modal.classList.add('active');

    // 生成预览HTML
    const previewHTML = generatePreviewHTML(config);
    frame.srcdoc = previewHTML;
}

/**
 * 生成预览HTML
 */
function generatePreviewHTML(config) {
    return `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>模板预览</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 20px; background: #f5f7fa; }
        .preview-container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: 600; color: #24292e; }
        .form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px 12px; border: 1px solid #d1d5da; border-radius: 4px; font-size: 14px; }
        .form-row { display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px; }
        .card { border: 1px solid #e1e4e8; border-radius: 8px; margin-bottom: 20px; }
        .card-header { padding: 15px 20px; background: #f6f8fa; border-bottom: 1px solid #e1e4e8; font-weight: 600; }
        .card-body { padding: 20px; }
        button { padding: 8px 16px; background: #667eea; color: white; border: none; border-radius: 4px; cursor: pointer; }
        button:hover { background: #5568d3; }
    </style>
</head>
<body>
    <div class="preview-container">
        ${renderComponentHTML(config.rootComponent)}
    </div>
    <script>
        // 模拟数据
        const formData = {};

        // 数据绑定
        function bindData(fieldPath, value) {
            formData[fieldPath] = value;
        }
    </script>
</body>
</html>
    `;
}

/**
 * 渲染组件HTML
 */
function renderComponentHTML(component) {
    if (!component) return '';

    switch (component.type) {
        case 'PAGE':
            return component.children ? component.children.map(c => renderComponentHTML(c)).join('') : '';
        case 'CARD':
            return `
                <div class="card">
                    ${component.title ? `<div class="card-header">${component.title}</div>` : ''}
                    <div class="card-body">
                        ${component.children ? component.children.map(c => renderComponentHTML(c)).join('') : ''}
                    </div>
                </div>
            `;
        case 'GRID':
            const colWidth = 100 / (component.columns || 2);
            return `
                <div style="display: grid; grid-template-columns: repeat(${component.columns || 2}, ${colWidth}%); gap: ${component.gutter || 16}px; margin-bottom: 20px;">
                    ${component.children ? component.children.map(c => `<div>${renderComponentHTML(c)}</div>`).join('') : ''}
                </div>
            `;
        case 'ROW':
            return `
                <div style="display: flex; flex-wrap: wrap; margin-bottom: 15px;">
                    ${component.children ? component.children.map(c => renderComponentHTML(c)).join('') : ''}
                </div>
            `;
        case 'COL':
            const colSpan = component.span || 12;
            const colWidthPercent = (colSpan / 24) * 100;
            return `
                <div style="flex: 0 0 ${colWidthPercent}%; padding: 0 8px;">
                    ${component.children ? component.children.map(c => renderComponentHTML(c)).join('') : ''}
                </div>
            `;
        case 'INPUT':
            return `
                <div class="form-group">
                    <label>${component.name}</label>
                    <input type="text" placeholder="${component.placeholder || ''}"
                           onchange="bindData('${component.fieldPath}', this.value)">
                </div>
            `;
        case 'SELECT':
            return `
                <div class="form-group">
                    <label>${component.name}</label>
                    <select onchange="bindData('${component.fieldPath}', this.value)">
                        <option value="">${component.placeholder || '请选择'}</option>
                    </select>
                </div>
            `;
        case 'DATE':
            return `
                <div class="form-group">
                    <label>${component.name}</label>
                    <input type="date" onchange="bindData('${component.fieldPath}', this.value)">
                </div>
            `;
        case 'NUMBER':
            return `
                <div class="form-group">
                    <label>${component.name}</label>
                    <input type="number" placeholder="${component.placeholder || ''}"
                           min="${component.min || 0}" max="${component.max || 999999}"
                           onchange="bindData('${component.fieldPath}', this.value)">
                </div>
            `;
        case 'MONEY':
            return `
                <div class="form-group">
                    <label>${component.name}</label>
                    <div style="display: flex; align-items: center;">
                        <span style="padding: 8px 12px; background: #f6f8fa; border: 1px solid #d1d5da; border-right: none; border-radius: 4px 0 0 4px;">
                            ${component.currency || 'CNY'}
                        </span>
                        <input type="number" style="flex: 1; border-radius: 0 4px 4px 0;"
                               onchange="bindData('${component.fieldPath}', this.value)">
                    </div>
                </div>
            `;
        case 'TEXTAREA':
            return `
                <div class="form-group">
                    <label>${component.name}</label>
                    <textarea rows="${component.rows || 4}" placeholder="${component.placeholder || ''}"
                              onchange="bindData('${component.fieldPath}', this.value)"></textarea>
                </div>
            `;
        case 'BUTTON':
            return `<button>${component.text || component.name || '按钮'}</button>`;
        case 'SAVE_BUTTON':
            return `<button style="background: #10b981;">${component.text || '保存'}</button>`;
        case 'SUBMIT_BUTTON':
            return `<button style="background: #f59e0b;">${component.text || '提交'}</button>`;
        case 'DETAIL_TABLE': {
            const columns = component.columns || [];
            const colHeaders = columns.map(c => `<th style="border:1px solid #ddd;padding:8px;">${c.name}</th>`).join('');
            const colCells = columns.map(c => `<td style="border:1px solid #ddd;padding:8px;">{{${c.code}}}</td>`).join('');
            return `
                <div class="form-group">
                    <label style="font-weight:600;margin-bottom:8px;">${component.name || '明细表'}</label>
                    <table style="width:100%;border-collapse:collapse;margin-bottom:8px;">
                        <thead><tr style="background:#f6f8fa;">${colHeaders}</tr></thead>
                        <tbody>
                            <tr>${colCells}</tr>
                        </tbody>
                    </table>
                    ${component.enableAdd !== false ? '<button style="font-size:12px;padding:4px 12px;">+ 增加行</button>' : ''}
                </div>
            `;
        }
        default:
            return '';
    }
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
            DesignerState.templateConfig = state.templateConfig || { rootComponent: null };
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