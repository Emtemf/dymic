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

    // 递归处理子组件
    if (component.children && component.children.length > 0) {
        component.children.forEach(child => {
            extractLayoutNodes(child, component.id, result, usedCodes);
        });
    }

    // 递归处理 panels 子组件（COLLAPSE）
    if (component.panels && component.panels.length > 0) {
        component.panels.forEach(panel => {
            if (panel.children && panel.children.length > 0) {
                panel.children.forEach(child => {
                    const c = typeof child === 'string' ? null : child;
                    if (c) extractLayoutNodes(c, component.id, result, usedCodes);
                });
            }
        });
    }

    // 递归处理 tabs 子组件（TAB）
    if (component.tabs && component.tabs.length > 0) {
        component.tabs.forEach(tab => {
            if (tab.children && tab.children.length > 0) {
                tab.children.forEach(child => {
                    const c = typeof child === 'string' ? null : child;
                    if (c) extractLayoutNodes(c, component.id, result, usedCodes);
                });
            }
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
            ...props,
            children: (node.children || []).map(c => convertNode(c))
        };

        componentMap[String(node.id)] = component;
        return component;
    }

    layoutNodes.forEach(node => convertNode(node));

    // 合并 ID 引用：panels[i].children 从 ID 字符串替换为完整对象
    Object.values(componentMap).forEach(component => {
        if (component.panels) {
            component.panels.forEach(panel => {
                if (panel.children && panel.children.length > 0 && typeof panel.children[0] === 'string') {
                    panel.children = panel.children.map(id => componentMap[id]).filter(Boolean);
                }
            });
        }
        if (component.tabs) {
            component.tabs.forEach(tab => {
                if (tab.children && tab.children.length > 0 && typeof tab.children[0] === 'string') {
                    tab.children = tab.children.map(id => componentMap[id]).filter(Boolean);
                }
            });
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