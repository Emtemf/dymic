/**
 * 预览渲染器
 * 负责将组件配置渲染为可视化预览
 */

/**
 * 渲染预览
 */
function renderPreview() {
    const previewContent = document.getElementById('previewContent');
    const dropHint = document.getElementById('dropHint');

    if (!DesignerState.templateConfig.rootComponent) {
        previewContent.innerHTML = '';
        dropHint.classList.remove('hidden');
        return;
    }

    dropHint.classList.add('hidden');
    previewContent.innerHTML = '';

    // 渲染根组件
    const rootElement = renderComponent(DesignerState.templateConfig.rootComponent);
    previewContent.appendChild(rootElement);
}

/**
 * 渲染组件
 */
function renderComponent(component) {
    const def = ComponentLibrary.getComponentDef(component.type);
    if (!def) return null;

    switch (component.type) {
        case 'PAGE':
            return renderPageComponent(component);
        case 'CARD':
            return renderCardComponent(component);
        case 'GRID':
            return renderGridComponent(component);
        case 'ROW':
            return renderRowComponent(component);
        case 'COL':
            return renderColComponent(component);
        case 'TAB':
            return renderTabComponent(component);
        case 'COLLAPSE':
            return renderCollapseComponent(component);
        case 'INPUT':
            return renderInputComponent(component);
        case 'SELECT':
            return renderSelectComponent(component);
        case 'DATE':
            return renderDateComponent(component);
        case 'NUMBER':
            return renderNumberComponent(component);
        case 'MONEY':
            return renderMoneyComponent(component);
        case 'TEXTAREA':
            return renderTextareaComponent(component);
        case 'SUPPLIER_SELECT':
            return renderSupplierSelectComponent(component);
        case 'DETAIL_TABLE':
            return renderDetailTableComponent(component);
        case 'ATTACHMENT':
            return renderAttachmentComponent(component);
        case 'IMAGE':
            return renderImageComponent(component);
        case 'BUTTON':
            return renderButtonComponent(component);
        case 'QUERY_DIALOG':
            return renderQueryDialogComponent(component);
        case 'SAVE_BUTTON':
            return renderSaveButtonComponent(component);
        case 'SUBMIT_BUTTON':
            return renderSubmitButtonComponent(component);
        default:
            return renderDefaultComponent(component);
    }
}

/**
 * 渲染页面组件
 */
function renderPageComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;
    element.style.cssText = `
        min-height: 200px;
        border: 2px dashed #d1d5da;
        background: #fafbfc;
    `;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-file"></i> ${component.name}
        </div>
        <div class="component-preview-content">
            ${component.description || '页面容器'}
        </div>
        <div class="component-children"></div>
    `;

    // 渲染子组件
    if (component.children && component.children.length > 0) {
        const childrenContainer = element.querySelector('.component-children');
        component.children.forEach(child => {
            const childElement = renderComponent(child);
            if (childElement) {
                childrenContainer.appendChild(childElement);
            }
        });
    }

    // 绑定事件
    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染卡片组件
 */
function renderCardComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;
    element.style.cssText = `
        border: 1px solid #e1e4e8;
        background: white;
        margin-bottom: 10px;
    `;

    element.innerHTML = `
        <div class="card-header" style="
            padding: 10px 15px;
            background: #f6f8fa;
            border-bottom: 1px solid #e1e4e8;
            font-weight: 600;
        ">
            <i class="fas fa-square"></i> ${component.title || component.name}
        </div>
        <div class="card-body" style="padding: 15px;">
            <div class="component-children"></div>
        </div>
    `;

    // 渲染子组件
    if (component.children && component.children.length > 0) {
        const childrenContainer = element.querySelector('.component-children');
        component.children.forEach(child => {
            const childElement = renderComponent(child);
            if (childElement) {
                childrenContainer.appendChild(childElement);
            }
        });
    }

    // 绑定事件
    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染栅格组件
 */
function renderGridComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;
    element.style.cssText = `
        background: #f6f8fa;
        padding: 10px;
        margin-bottom: 10px;
    `;

    const columns = component.columns || 2;
    const colWidth = 100 / columns;

    let gridHTML = `
        <div class="component-preview-label" style="margin-bottom: 10px;">
            <i class="fas fa-th"></i> ${component.name} (${columns}列)
        </div>
        <div class="grid-container" style="
            display: grid;
            grid-template-columns: repeat(${columns}, ${colWidth}%);
            gap: ${component.gutter || 16}px;
        ">
    `;

    // 渲染子组件
    if (component.children && component.children.length > 0) {
        component.children.forEach(child => {
            gridHTML += `<div class="grid-item">${renderComponentPreview(child).outerHTML}</div>`;
        });
    } else {
        gridHTML += `<div class="grid-item" style="padding: 20px; text-align: center; color: #b4b4b4;">
            拖拽组件到此处
        </div>`;
    }

    gridHTML += `</div>`;
    element.innerHTML = gridHTML;

    // 绑定事件
    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染Tab组件
 */
function renderTabComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    let tabsHTML = `
        <div class="component-preview-label" style="margin-bottom: 10px;">
            <i class="fas fa-folder"></i> ${component.name}
        </div>
        <div class="tab-header" style="
            display: flex;
            border-bottom: 2px solid #e1e4e8;
            margin-bottom: 10px;
        ">
    `;

    if (component.tabs && component.tabs.length > 0) {
        component.tabs.forEach((tab, index) => {
            tabsHTML += `
                <div class="tab-item ${index === 0 ? 'active' : ''}" style="
                    padding: 8px 16px;
                    cursor: pointer;
                    border-bottom: ${index === 0 ? '2px solid #667eea' : 'none'};
                    margin-bottom: -2px;
                ">${tab.name}</div>
            `;
        });
    }

    tabsHTML += `</div><div class="tab-content" style="min-height: 100px;"></div>`;
    element.innerHTML = tabsHTML;

    // 渲染第一个tab的内容
    if (component.tabs && component.tabs.length > 0 && component.tabs[0].children) {
        const contentContainer = element.querySelector('.tab-content');
        component.tabs[0].children.forEach(child => {
            const childElement = renderComponent(child);
            if (childElement) {
                contentContainer.appendChild(childElement);
            }
        });
    }

    // 绑定事件
    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染折叠面板组件
 */
function renderCollapseComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    let collapseHTML = `
        <div class="component-preview-label" style="margin-bottom: 10px;">
            <i class="fas fa-chevron-down"></i> ${component.name}
        </div>
    `;

    if (component.panels && component.panels.length > 0) {
        component.panels.forEach((panel, index) => {
            collapseHTML += `
                <div class="collapse-panel" style="
                    border: 1px solid #e1e4e8;
                    margin-bottom: -1px;
                ">
                    <div class="panel-header" style="
                        padding: 10px 15px;
                        background: #f6f8fa;
                        cursor: pointer;
                    ">
                        <i class="fas fa-chevron-right"></i> ${panel.name}
                    </div>
                    <div class="panel-content" style="padding: 15px;"></div>
                </div>
            `;
        });
    }

    element.innerHTML = collapseHTML;

    // 渲染第一个面板的内容
    if (component.panels && component.panels.length > 0 && component.panels[0].children) {
        const contentContainer = element.querySelector('.panel-content');
        component.panels[0].children.forEach(child => {
            const childElement = renderComponent(child);
            if (childElement) {
                contentContainer.appendChild(childElement);
            }
        });
    }

    // 绑定事件
    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染输入框组件
 */
function renderInputComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-font"></i> ${component.name}
        </div>
        <div class="component-preview-content">
            <input type="text"
                   placeholder="${component.placeholder || '请输入'}"
                   style="width: 100%; padding: 6px 12px; border: 1px solid #d1d5da; border-radius: 4px;"
                   ${component.readonly ? 'readonly' : ''}>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染下拉框组件
 */
function renderSelectComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-list"></i> ${component.name}
        </div>
        <div class="component-preview-content">
            <select style="width: 100%; padding: 6px 12px; border: 1px solid #d1d5da; border-radius: 4px;"
                    ${component.readonly ? 'disabled' : ''}>
                <option>${component.placeholder || '请选择'}</option>
            </select>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染日期组件
 */
function renderDateComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-calendar"></i> ${component.name}
        </div>
        <div class="component-preview-content">
            <input type="date"
                   style="width: 100%; padding: 6px 12px; border: 1px solid #d1d5da; border-radius: 4px;"
                   ${component.readonly ? 'readonly' : ''}>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染数字组件
 */
function renderNumberComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-sort-numeric-up"></i> ${component.name}
        </div>
        <div class="component-preview-content">
            <input type="number"
                   placeholder="${component.placeholder || '请输入数字'}"
                   min="${component.min || 0}"
                   max="${component.max || 999999}"
                   style="width: 100%; padding: 6px 12px; border: 1px solid #d1d5da; border-radius: 4px;"
                   ${component.readonly ? 'readonly' : ''}>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染金额组件
 */
function renderMoneyComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-dollar-sign"></i> ${component.name}
        </div>
        <div class="component-preview-content" style="display: flex; align-items: center;">
            <span style="padding: 6px 12px; background: #f6f8fa; border: 1px solid #d1d5da; border-right: none; border-radius: 4px 0 0 4px;">
                ${component.currency || 'CNY'}
            </span>
            <input type="number"
                   placeholder="${component.placeholder || '请输入金额'}"
                   style="flex: 1; padding: 6px 12px; border: 1px solid #d1d5da; border-radius: 0 4px 4px 0;"
                   ${component.readonly ? 'readonly' : ''}>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染多行文本组件
 */
function renderTextareaComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-align-left"></i> ${component.name}
        </div>
        <div class="component-preview-content">
            <textarea rows="${component.rows || 4}"
                      placeholder="${component.placeholder || '请输入内容'}"
                      style="width: 100%; padding: 6px 12px; border: 1px solid #d1d5da; border-radius: 4px; resize: vertical;"
                      ${component.readonly ? 'readonly' : ''}></textarea>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染供应商选择组件
 */
function renderSupplierSelectComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-building"></i> ${component.name}
        </div>
        <div class="component-preview-content" style="display: flex; align-items: center;">
            <input type="text"
                   placeholder="${component.placeholder || '请选择供应商'}"
                   style="flex: 1; padding: 6px 12px; border: 1px solid #d1d5da; border-radius: 4px 0 0 4px;"
                   ${component.readonly ? 'readonly' : ''}>
            <button style="padding: 6px 12px; border: 1px solid #667eea; background: #667eea; color: white; border-radius: 0 4px 4px 0; cursor: pointer;">
                <i class="fas fa-search"></i>
            </button>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染明细表组件
 */
function renderDetailTableComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    const columns = component.columns || [];
    const enableAdd = component.enableAdd !== false;
    const maxRows = component.maxRows || 0;

    // Initialize draft data
    if (!window.DesignerState.detailDrafts) window.DesignerState.detailDrafts = {};
    if (!window.DesignerState.detailDrafts[component.id]) {
        window.DesignerState.detailDrafts[component.id] = [];
    }

    let tableHTML = `
        <div class="component-preview-label">
            <i class="fas fa-table"></i> ${component.name || '明细表'}
            <small style="color:#999;margin-left:8px;">(${columns.length}列)</small>
        </div>
        <div class="component-preview-content">
            <table style="width:100%;border-collapse:collapse;margin-bottom:10px;">
                <thead>
                    <tr style="background:#f6f8fa;">
    `;

    columns.forEach(col => {
        tableHTML += `<th style="padding:8px;border:1px solid #e1e4e8;width:${col.width || 100}px;text-align:left;">${col.name}</th>`;
    });
    tableHTML += `<th style="padding:8px;border:1px solid #e1e4e8;width:120px;text-align:center;">操作</th>`;
    tableHTML += `</tr></thead><tbody id="detail_tbody_${component.id}">`;

    tableHTML += `<tr><td colspan="${columns.length + 1}" style="padding:20px;text-align:center;color:#b4b4b4;">暂无数据</td></tr>`;

    tableHTML += `</tbody></table>`;

    if (enableAdd) {
        tableHTML += `
            <button onclick="DetailTableHelper.addRow('${component.id}', ${JSON.stringify(columns).replace(/"/g, '&quot;')}, ${maxRows})"
                    style="padding:4px 12px;border:1px solid #667eea;background:white;color:#667eea;border-radius:4px;cursor:pointer;">
                <i class="fas fa-plus"></i> 添加行
            </button>
        `;
    }

    tableHTML += `</div>`;
    element.innerHTML = tableHTML;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        if (typeof selectComponent === 'function') selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染附件组件
 */
function renderAttachmentComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-paperclip"></i> ${component.name}
        </div>
        <div class="component-preview-content" style="border: 2px dashed #d1d5da; padding: 20px; text-align: center; background: #fafbfc;">
            <i class="fas fa-cloud-upload-alt" style="font-size: 24px; color: #b4b4b4;"></i>
            <p style="margin: 8px 0 0; color: #586069;">点击或拖拽文件上传</p>
            <p style="margin: 4px 0 0; font-size: 12px; color: #b4b4b4;">支持格式: ${component.acceptTypes || '.pdf,.doc,.docx'}</p>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染图片组件
 */
function renderImageComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-image"></i> ${component.name}
        </div>
        <div class="component-preview-content" style="border: 2px dashed #d1d5da; padding: 20px; text-align: center; background: #fafbfc;">
            <i class="fas fa-images" style="font-size: 24px; color: #b4b4b4;"></i>
            <p style="margin: 8px 0 0; color: #586069;">点击或拖拽图片上传</p>
            <p style="margin: 4px 0 0; font-size: 12px; color: #b4b4b4;">支持格式: ${component.acceptTypes || '.jpg,.jpeg,.png,.gif'}</p>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染按钮组件
 */
function renderButtonComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    const buttonColors = {
        primary: '#667eea',
        secondary: '#6c757d',
        success: '#10b981',
        danger: '#ef4444',
        warning: '#f59e0b'
    };

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-hand-pointer"></i> ${component.name}
        </div>
        <div class="component-preview-content">
            <button style="
                padding: 8px 16px;
                background: ${buttonColors[component.buttonType] || buttonColors.primary};
                color: white;
                border: none;
                border-radius: 4px;
                cursor: pointer;
            ">
                ${component.icon ? `<i class="fas ${component.icon}"></i> ` : ''}${component.text || '按钮'}
            </button>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染查询弹窗组件
 */
function renderQueryDialogComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-search"></i> ${component.name}
        </div>
        <div class="component-preview-content">
            <button style="padding: 8px 16px; background: #667eea; color: white; border: none; border-radius: 4px; cursor: pointer;">
                <i class="fas fa-search"></i> ${component.text || '查询'}
            </button>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染保存按钮组件
 */
function renderSaveButtonComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-save"></i> ${component.name}
        </div>
        <div class="component-preview-content">
            <button style="padding: 8px 16px; background: #10b981; color: white; border: none; border-radius: 4px; cursor: pointer;">
                <i class="fas fa-save"></i> ${component.text || '保存'}
            </button>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染提交按钮组件
 */
function renderSubmitButtonComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-check"></i> ${component.name}
        </div>
        <div class="component-preview-content">
            <button style="padding: 8px 16px; background: #f59e0b; color: white; border: none; border-radius: 4px; cursor: pointer;">
                <i class="fas fa-check"></i> ${component.text || '提交'}
            </button>
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染默认组件
 */
function renderDefaultComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    element.innerHTML = `
        <div class="component-preview-label">
            <i class="fas fa-cube"></i> ${component.name}
        </div>
        <div class="component-preview-content">
            未知组件类型: ${component.type}
        </div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染组件预览（用于嵌套场景）
 */
function renderComponentPreview(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;
    element.innerHTML = `
        <div class="component-preview-label">${component.name}</div>
        <div class="component-preview-content">${component.fieldPath || component.description || ''}</div>
    `;

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染行组件
 */
function renderRowComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;
    element.style.cssText = `
        display: flex;
        flex-wrap: wrap;
        margin-bottom: 10px;
        margin-left: -${(component.gutter || 16) / 2}px;
        margin-right: -${(component.gutter || 16) / 2}px;
    `;

    // 渲染子组件（COL）
    if (component.children && component.children.length > 0) {
        component.children.forEach(child => {
            const childElement = renderComponent(child);
            if (childElement) {
                element.appendChild(childElement);
            }
        });
    } else {
        element.innerHTML = `
            <div class="component-preview-label" style="width: 100%; padding: 10px; text-align: center; color: #b4b4b4;">
                <i class="fas fa-grip-lines"></i> ${component.name} - 拖拽列组件到此处
            </div>
        `;
    }

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}

/**
 * 渲染列组件
 */
function renderColComponent(component) {
    const element = document.createElement('div');
    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    const span = component.span || 12;
    const offset = component.offset || 0;
    const widthPercent = (span / 24) * 100;
    const offsetPercent = (offset / 24) * 100;

    element.style.cssText = `
        flex: 0 0 ${widthPercent}%;
        margin-left: ${offsetPercent}%;
        padding-left: 8px;
        padding-right: 8px;
        min-height: 60px;
        border: 1px dashed #d1d5da;
        background: #fafbfc;
        box-sizing: border-box;
    `;

    // 渲染子组件
    if (component.children && component.children.length > 0) {
        const childrenContainer = document.createElement('div');
        childrenContainer.className = 'component-children';
        component.children.forEach(child => {
            const childElement = renderComponent(child);
            if (childElement) {
                childrenContainer.appendChild(childElement);
            }
        });
        element.appendChild(childrenContainer);
    } else {
        element.innerHTML = `
            <div class="component-preview-label" style="padding: 10px; text-align: center; color: #b4b4b4;">
                <i class="fas fa-grip-lines-vertical"></i> ${component.name} (${span}/24列)
            </div>
            <div class="component-children"></div>
        `;
    }

    element.addEventListener('click', (e) => {
        e.stopPropagation();
        selectComponent(component.id);
    });

    return element;
}