/**
 * 预览渲染器
 * 负责将组件配置渲染为可视化预览
 */

function formGroupHTML(label, contentHTML) {
    return `<div style="margin-bottom:15px;">
        <label style="display:block;margin-bottom:5px;font-weight:600;color:#24292e;">${label}</label>
        ${contentHTML}
    </div>`;
}

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

    const rootElement = renderComponent(DesignerState.templateConfig.rootComponent, 'designer');
    previewContent.appendChild(rootElement);
}

/**
 * 渲染组件
 */
function renderComponent(component, mode = 'designer') {
    const def = ComponentLibrary.getComponentDef(component.type);
    if (!def) return null;

    switch (component.type) {
        case 'PAGE':
            return renderPageComponent(component, mode);
        case 'CARD':
            return renderCardComponent(component, mode);
        case 'GRID':
            return renderGridComponent(component, mode);
        case 'ROW':
            return renderRowComponent(component, mode);
        case 'COL':
            return renderColComponent(component, mode);
        case 'TAB':
            return renderTabComponent(component, mode);
        case 'COLLAPSE':
            return renderCollapseComponent(component, mode);
        case 'INPUT':
            return renderInputComponent(component, mode);
        case 'SELECT':
            return renderSelectComponent(component, mode);
        case 'DATE':
            return renderDateComponent(component, mode);
        case 'NUMBER':
            return renderNumberComponent(component, mode);
        case 'MONEY':
            return renderMoneyComponent(component, mode);
        case 'TEXTAREA':
            return renderTextareaComponent(component, mode);
        case 'SUPPLIER_SELECT':
            return renderSupplierSelectComponent(component, mode);
        case 'DETAIL_TABLE':
            return renderDetailTableComponent(component, mode);
        case 'ATTACHMENT':
            return renderAttachmentComponent(component, mode);
        case 'IMAGE':
            return renderImageComponent(component, mode);
        case 'BUTTON':
            return renderButtonComponent(component, mode);
        case 'QUERY_DIALOG':
            return renderQueryDialogComponent(component, mode);
        case 'SAVE_BUTTON':
            return renderSaveButtonComponent(component, mode);
        case 'SUBMIT_BUTTON':
            return renderSubmitButtonComponent(component, mode);
        default:
            return renderDefaultComponent(component, mode);
    }
}

/**
 * 渲染页面组件
 */
function renderPageComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = 'padding: 0;';
    } else {
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
    }

    if (component.children && component.children.length > 0) {
        const container = mode === 'preview' ? element : element.querySelector('.component-children');
        component.children.forEach(child => {
            const childElement = renderComponent(child, mode);
            if (childElement) {
                container.appendChild(childElement);
            }
        });
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}

/**
 * 渲染卡片组件
 */
function renderCardComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = `
            border: 1px solid #e1e4e8;
            border-radius: 8px;
            margin-bottom: 20px;
            background: white;
        `;
        element.innerHTML = `
            <div style="padding:15px 20px;background:#f6f8fa;border-bottom:1px solid #e1e4e8;font-weight:600;">
                ${component.title || component.name}
            </div>
            <div class="card-body" style="padding:20px;"></div>
        `;
    } else {
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
    }

    const container = mode === 'preview'
        ? element.querySelector('.card-body')
        : element.querySelector('.component-children');

    if (component.children && component.children.length > 0 && container) {
        component.children.forEach(child => {
            const childElement = renderComponent(child, mode);
            if (childElement) {
                container.appendChild(childElement);
            }
        });
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}

/**
 * 渲染栅格组件
 */
function renderGridComponent(component, mode = 'designer') {
    const element = document.createElement('div');
    const columns = component.columns || 2;
    const gutter = component.gutter || 16;

    if (mode === 'preview') {
        element.style.cssText = 'margin-bottom:20px;';
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;
        element.style.cssText = `
            background: #f6f8fa;
            padding: 10px;
            margin-bottom: 10px;
        `;

        const labelDiv = document.createElement('div');
        labelDiv.className = 'component-preview-label';
        labelDiv.style.marginBottom = '10px';
        labelDiv.innerHTML = `<i class="fas fa-th"></i> ${component.name} (${columns}列)`;
        element.appendChild(labelDiv);
    }

    const gridContainer = document.createElement('div');
    gridContainer.style.cssText = `
        display: grid;
        grid-template-columns: repeat(${columns}, 1fr);
        gap: ${gutter}px;
    `;

    for (let i = 0; i < columns; i++) {
        const cell = document.createElement('div');

        if (mode === 'preview') {
            cell.style.cssText = 'min-height:1px;';
        } else {
            cell.className = 'grid-cell';
            cell.dataset.gridColumn = i;
            cell.style.cssText = `
                min-height: 80px;
                border: 1px dashed #d1d5da;
                background: white;
                padding: 10px;
            `;
        }

        const columnChildren = (component.children || []).filter(c => c.gridColumn === i);
        if (columnChildren.length > 0) {
            columnChildren.forEach(child => {
                const childElement = renderComponent(child, mode);
                if (childElement) {
                    cell.appendChild(childElement);
                }
            });
        } else if (mode !== 'preview') {
            cell.innerHTML = `<div style="padding:20px;text-align:center;color:#b4b4b4;">
                拖拽组件到第${i + 1}列
            </div>`;
        }

        gridContainer.appendChild(cell);
    }

    element.appendChild(gridContainer);

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}

/**
 * 渲染Tab组件
 */
function renderTabComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = 'margin-bottom:20px;';
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;

        const labelDiv = document.createElement('div');
        labelDiv.className = 'component-preview-label';
        labelDiv.style.marginBottom = '10px';
        labelDiv.innerHTML = `<i class="fas fa-folder"></i> ${component.name}`;
        element.appendChild(labelDiv);
    }

    const headerDiv = document.createElement('div');
    headerDiv.style.cssText = 'display:flex;border-bottom:2px solid #e1e4e8;margin-bottom:10px;';

    if (component.tabs && component.tabs.length > 0) {
        component.tabs.forEach((tab, index) => {
            const tabItem = document.createElement('div');
            tabItem.style.cssText = `
                padding: 8px 16px;
                cursor: pointer;
                border-bottom: ${index === 0 ? '2px solid #667eea' : 'none'};
                margin-bottom: -2px;
                color: ${index === 0 ? '#667eea' : '#586069'};
                font-weight: ${index === 0 ? '600' : 'normal'};
            `;
            tabItem.textContent = tab.name;
            tabItem.dataset.tabIndex = index;
            headerDiv.appendChild(tabItem);
        });

        element.appendChild(headerDiv);

        component.tabs.forEach((tab, index) => {
            const contentDiv = document.createElement('div');
            contentDiv.dataset.tabContent = index;
            contentDiv.style.cssText = `min-height:100px;display:${index === 0 ? 'block' : 'none'};`;

            if (tab.children && tab.children.length > 0) {
                tab.children.forEach(child => {
                    const childComponent = typeof child === 'string' ? findComponentById(child) : child;
                    if (childComponent) {
                        const childElement = renderComponent(childComponent, mode);
                        if (childElement) {
                            contentDiv.appendChild(childElement);
                        }
                    }
                });
            }

            element.appendChild(contentDiv);
        });

        headerDiv.querySelectorAll('[data-tab-index]').forEach(tabItem => {
            tabItem.addEventListener('click', (e) => {
                e.stopPropagation();
                const tabIndex = tabItem.dataset.tabIndex;

                headerDiv.querySelectorAll('[data-tab-index]').forEach((t, i) => {
                    t.style.borderBottom = i == tabIndex ? '2px solid #667eea' : 'none';
                    t.style.color = i == tabIndex ? '#667eea' : '#586069';
                    t.style.fontWeight = i == tabIndex ? '600' : 'normal';
                });

                element.querySelectorAll('[data-tab-content]').forEach((c, i) => {
                    c.style.display = i == tabIndex ? 'block' : 'none';
                });
            });
        });
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}

/**
 * 渲染折叠面板组件
 */
function renderCollapseComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = 'margin-bottom:20px;';
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;
        element.style.cssText = `
            background: #f6f8fa;
            padding: 10px;
            margin-bottom: 10px;
        `;

        const labelDiv = document.createElement('div');
        labelDiv.className = 'component-preview-label';
        labelDiv.style.marginBottom = '10px';
        labelDiv.innerHTML = `<i class="fas fa-chevron-down"></i> ${component.name}`;
        element.appendChild(labelDiv);
    }

    if (component.panels && component.panels.length > 0) {
        component.panels.forEach((panel, index) => {
            const isExpanded = panel.expanded !== false;

            const panelDiv = document.createElement('div');
            panelDiv.style.cssText = mode === 'preview'
                ? 'border:1px solid #e1e4e8;border-radius:4px;margin-bottom:8px;overflow:hidden;'
                : 'border:1px solid #e1e4e8;margin-bottom:-1px;';

            const headerDiv = document.createElement('div');
            headerDiv.style.cssText = `
                padding:10px 15px;
                background:#f6f8fa;
                cursor:pointer;
                display:flex;
                align-items:center;
                gap:8px;
                font-weight:600;
            `;
            headerDiv.innerHTML = `
                <i class="fas fa-chevron-${isExpanded ? 'down' : 'right'}"></i>
                <span>${panel.name}</span>
            `;

            const contentDiv = document.createElement('div');
            contentDiv.style.cssText = `padding:15px;display:${isExpanded ? 'block' : 'none'};`;

            if (panel.children && panel.children.length > 0) {
                panel.children.forEach(child => {
                    const childComponent = typeof child === 'string' ? findComponentById(child) : child;
                    if (childComponent) {
                        const childElement = renderComponent(childComponent, mode);
                        if (childElement) {
                            contentDiv.appendChild(childElement);
                        }
                    }
                });
            }

            headerDiv.addEventListener('click', (e) => {
                e.stopPropagation();
                const isVisible = contentDiv.style.display !== 'none';
                contentDiv.style.display = isVisible ? 'none' : 'block';
                const icon = headerDiv.querySelector('i');
                icon.className = `fas fa-chevron-${isVisible ? 'right' : 'down'}`;
            });

            panelDiv.appendChild(headerDiv);
            panelDiv.appendChild(contentDiv);
            element.appendChild(panelDiv);
        });
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}

/**
 * 渲染输入框组件
 */
function renderInputComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.innerHTML = formGroupHTML(component.name,
            `<input type="text" placeholder="${component.placeholder || '请输入'}"
                   style="width:100%;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px;font-size:14px;"
                   ${component.readonly ? 'readonly' : ''}>`);
        return element;
    }

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
function renderSelectComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.innerHTML = formGroupHTML(component.name,
            `<select style="width:100%;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px;font-size:14px;"
                     ${component.readonly ? 'disabled' : ''}>
                <option>${component.placeholder || '请选择'}</option>
            </select>`);
        return element;
    }

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
function renderDateComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.innerHTML = formGroupHTML(component.name,
            `<input type="date" style="width:100%;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px;font-size:14px;"
                   ${component.readonly ? 'readonly' : ''}>`);
        return element;
    }

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
function renderNumberComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.innerHTML = formGroupHTML(component.name,
            `<input type="number" placeholder="${component.placeholder || '请输入数字'}"
                   min="${component.min || 0}" max="${component.max || 999999}"
                   style="width:100%;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px;font-size:14px;"
                   ${component.readonly ? 'readonly' : ''}>`);
        return element;
    }

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
function renderMoneyComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.innerHTML = formGroupHTML(component.name,
            `<div style="display:flex;align-items:center;">
                <span style="padding:8px 12px;background:#f6f8fa;border:1px solid #d1d5da;border-right:none;border-radius:4px 0 0 4px;">
                    ${component.currency || 'CNY'}
                </span>
                <input type="number" placeholder="${component.placeholder || '请输入金额'}"
                       style="flex:1;padding:8px 12px;border:1px solid #d1d5da;border-radius:0 4px 4px 0;font-size:14px;"
                       ${component.readonly ? 'readonly' : ''}>
            </div>`);
        return element;
    }

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
function renderTextareaComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.innerHTML = formGroupHTML(component.name,
            `<textarea rows="${component.rows || 4}" placeholder="${component.placeholder || '请输入内容'}"
                      style="width:100%;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px;resize:vertical;font-size:14px;"
                      ${component.readonly ? 'readonly' : ''}></textarea>`);
        return element;
    }

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
function renderSupplierSelectComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.innerHTML = formGroupHTML(component.name,
            `<div style="display:flex;align-items:center;">
                <input type="text" placeholder="${component.placeholder || '请选择供应商'}"
                       style="flex:1;padding:8px 12px;border:1px solid #d1d5da;border-radius:4px 0 0 4px;"
                       ${component.readonly ? 'readonly' : ''}>
                <button style="padding:8px 12px;border:1px solid #667eea;background:#667eea;color:white;border-radius:0 4px 4px 0;cursor:pointer;">
                    <i class="fas fa-search"></i>
                </button>
            </div>`);
        return element;
    }

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
function renderDetailTableComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        const columns = component.columns || [];
        const colHeaders = columns.map(c => `<th style="padding:8px;border:1px solid #e1e4e8;text-align:left;background:#f6f8fa;">${c.name}</th>`).join('');
        const colCells = columns.map(c => `<td style="padding:8px;border:1px solid #e1e4e8;">-</td>`).join('');

        element.innerHTML = `
            <div style="margin-bottom:15px;">
                <label style="display:block;margin-bottom:8px;font-weight:600;color:#24292e;">${component.name || '明细表'}</label>
                <table style="width:100%;border-collapse:collapse;margin-bottom:8px;">
                    <thead><tr>${colHeaders}<th style="padding:8px;border:1px solid #e1e4e8;width:80px;text-align:center;background:#f6f8fa;">操作</th></tr></thead>
                    <tbody>
                        <tr>${colCells}<td style="padding:8px;border:1px solid #e1e4e8;text-align:center;color:#999;">暂无</td></tr>
                    </tbody>
                </table>
                ${component.enableAdd !== false ? '<button style="padding:4px 12px;border:1px solid #667eea;background:white;color:#667eea;border-radius:4px;cursor:pointer;font-size:12px;">+ 增加行</button>' : ''}
            </div>
        `;
        return element;
    }

    element.className = 'component-preview';
    element.dataset.componentId = component.id;

    const columns = component.columns || [];
    const enableAdd = component.enableAdd !== false;
    const maxRows = component.maxRows || 0;

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
function renderAttachmentComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.innerHTML = formGroupHTML(component.name,
            `<div style="border:2px dashed #d1d5da;padding:20px;text-align:center;background:#fafbfc;border-radius:4px;">
                <i class="fas fa-cloud-upload-alt" style="font-size:24px;color:#b4b4b4;"></i>
                <p style="margin:8px 0 0;color:#586069;">点击或拖拽文件上传</p>
                <p style="margin:4px 0 0;font-size:12px;color:#b4b4b4;">支持格式: ${component.acceptTypes || '.pdf,.doc,.docx'}</p>
            </div>`);
        return element;
    }

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
function renderImageComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.innerHTML = formGroupHTML(component.name,
            `<div style="border:2px dashed #d1d5da;padding:20px;text-align:center;background:#fafbfc;border-radius:4px;">
                <i class="fas fa-images" style="font-size:24px;color:#b4b4b4;"></i>
                <p style="margin:8px 0 0;color:#586069;">点击或拖拽图片上传</p>
                <p style="margin:4px 0 0;font-size:12px;color:#b4b4b4;">支持格式: ${component.acceptTypes || '.jpg,.jpeg,.png,.gif'}</p>
            </div>`);
        return element;
    }

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
function renderButtonComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    const buttonColors = {
        primary: '#667eea',
        secondary: '#6c757d',
        success: '#10b981',
        danger: '#ef4444',
        warning: '#f59e0b'
    };

    if (mode === 'preview') {
        element.style.cssText = 'margin-bottom:15px;';
        element.innerHTML = `
            <button style="padding:8px 16px;background:${buttonColors[component.buttonType] || buttonColors.primary};color:white;border:none;border-radius:4px;cursor:pointer;font-size:14px;">
                ${component.icon ? `<i class="fas ${component.icon}"></i> ` : ''}${component.text || component.name || '按钮'}
            </button>
        `;
        return element;
    }

    element.className = 'component-preview';
    element.dataset.componentId = component.id;

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
function renderQueryDialogComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = 'margin-bottom:15px;';
        element.innerHTML = `
            <button style="padding:8px 16px;background:#667eea;color:white;border:none;border-radius:4px;cursor:pointer;font-size:14px;">
                <i class="fas fa-search"></i> ${component.text || component.name || '查询'}
            </button>
        `;
        return element;
    }

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
function renderSaveButtonComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = 'margin-bottom:15px;';
        element.innerHTML = `
            <button style="padding:8px 16px;background:#10b981;color:white;border:none;border-radius:4px;cursor:pointer;font-size:14px;">
                <i class="fas fa-save"></i> ${component.text || component.name || '保存'}
            </button>
        `;
        return element;
    }

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
function renderSubmitButtonComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = 'margin-bottom:15px;';
        element.innerHTML = `
            <button style="padding:8px 16px;background:#f59e0b;color:white;border:none;border-radius:4px;cursor:pointer;font-size:14px;">
                <i class="fas fa-check"></i> ${component.text || component.name || '提交'}
            </button>
        `;
        return element;
    }

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
function renderDefaultComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.innerHTML = `<div style="padding:8px;color:#999;font-size:12px;">未知组件: ${component.type}</div>`;
        return element;
    }

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
function renderRowComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    if (mode === 'preview') {
        element.style.cssText = `
            display: flex;
            flex-wrap: wrap;
            margin-bottom: 15px;
        `;
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;
        element.style.cssText = `
            display: flex;
            flex-wrap: wrap;
            margin-bottom: 10px;
            margin-left: -${(component.gutter || 16) / 2}px;
            margin-right: -${(component.gutter || 16) / 2}px;
        `;
    }

    if (component.children && component.children.length > 0) {
        component.children.forEach(child => {
            const childElement = renderComponent(child, mode);
            if (childElement) {
                element.appendChild(childElement);
            }
        });
    } else if (mode !== 'preview') {
        element.innerHTML = `
            <div class="component-preview-label" style="width: 100%; padding: 10px; text-align: center; color: #b4b4b4;">
                <i class="fas fa-grip-lines"></i> ${component.name} - 拖拽列组件到此处
            </div>
        `;
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}

/**
 * 渲染列组件
 */
function renderColComponent(component, mode = 'designer') {
    const element = document.createElement('div');

    const span = component.span || 12;
    const offset = component.offset || 0;
    const widthPercent = (span / 24) * 100;
    const offsetPercent = (offset / 24) * 100;

    if (mode === 'preview') {
        element.style.cssText = `
            flex: 0 0 ${widthPercent}%;
            margin-left: ${offsetPercent}%;
            padding-left: 8px;
            padding-right: 8px;
            box-sizing: border-box;
        `;
    } else {
        element.className = 'component-preview';
        element.dataset.componentId = component.id;
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
    }

    if (component.children && component.children.length > 0) {
        const childrenContainer = document.createElement('div');
        childrenContainer.className = 'component-children';
        component.children.forEach(child => {
            const childElement = renderComponent(child, mode);
            if (childElement) {
                childrenContainer.appendChild(childElement);
            }
        });
        element.appendChild(childrenContainer);
    } else if (mode !== 'preview') {
        element.innerHTML = `
            <div class="component-preview-label" style="padding: 10px; text-align: center; color: #b4b4b4;">
                <i class="fas fa-grip-lines-vertical"></i> ${component.name} (${span}/24列)
            </div>
            <div class="component-children"></div>
        `;
    }

    if (mode !== 'preview') {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            selectComponent(component.id);
        });
    }

    return element;
}
