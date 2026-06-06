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

        // 调用后端API保存
        const response = await fetch('/api/template/config', {
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
            DesignerState.templateId = result.templateId;
            DesignerState.templateVersion = result.version;
            showNotification('配置保存成功', 'success');
        } else {
            throw new Error('保存失败');
        }
    } catch (error) {
        console.error('保存配置失败:', error);
        showNotification('保存失败: ' + error.message, 'error');
    }
}

/**
 * 加载配置
 */
async function loadConfig(templateId) {
    try {
        const response = await fetch(`/api/template/config/${templateId}`);

        if (response.ok) {
            const result = await response.json();
            DesignerState.templateConfig = result.config || { rootComponent: null };
            DesignerState.templateId = templateId;
            DesignerState.templateVersion = result.version;

            // 更新模板名称显示
            document.getElementById('templateName').textContent = result.name || '未命名模板';

            // 渲染预览
            renderPreview();

            showNotification('配置加载成功', 'success');
        } else {
            throw new Error('加载失败');
        }
    } catch (error) {
        console.error('加载配置失败:', error);
        showNotification('加载失败: ' + error.message, 'error');
    }
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
        return { valid: false, message: '组件编码不能为空' };
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
            return `<button>${component.text || '按钮'}</button>`;
        case 'SAVE_BUTTON':
            return `<button style="background: #10b981;">${component.text || '保存'}</button>`;
        case 'SUBMIT_BUTTON':
            return `<button style="background: #f59e0b;">${component.text || '提交'}</button>`;
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
            DesignerState.templateId = state.templateId || null;
            DesignerState.templateVersion = state.templateVersion || '1.0.0';
        }
    } catch (error) {
        console.error('恢复状态失败:', error);
    }
}