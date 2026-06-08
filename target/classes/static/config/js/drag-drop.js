/**
 * 拖拽功能
 * 处理组件的拖拽添加、排序、嵌套等功能
 */

let draggedComponent = null;
let dragPlaceholder = null;

/**
 * 开始拖拽组件
 */
function handleDragStart(event) {
    const componentType = event.target.dataset.type;
    const componentDef = ComponentLibrary.getComponentDef(componentType);

    if (!componentDef) {
        event.preventDefault();
        return;
    }

    draggedComponent = ComponentLibrary.createComponent(componentType);

    event.dataTransfer.effectAllowed = 'copy';
    event.dataTransfer.setData('text/plain', componentType);

    // 设置拖拽图像
    const dragImage = document.createElement('div');
    dragImage.className = 'drag-image';
    dragImage.innerHTML = `<i class="fas ${componentDef.icon}"></i> ${componentDef.name}`;
    dragImage.style.cssText = `
        position: absolute;
        top: -1000px;
        left: -1000px;
        padding: 8px 12px;
        background: #667eea;
        color: white;
        border-radius: 4px;
        font-size: 13px;
    `;
    document.body.appendChild(dragImage);
    event.dataTransfer.setDragImage(dragImage, 0, 0);

    setTimeout(() => document.body.removeChild(dragImage), 0);
}

/**
 * 拖拽经过目标区域
 */
function handleDragOver(event) {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'copy';

    const previewCanvas = document.getElementById('previewCanvas');
    if (!previewCanvas.classList.contains('drag-over')) {
        previewCanvas.classList.add('drag-over');
    }

    // 隐藏提示
    const dropHint = document.getElementById('dropHint');
    if (dropHint) {
        dropHint.classList.add('hidden');
    }

    // 清除之前的 grid-cell 高亮
    document.querySelectorAll('.grid-cell').forEach(el => {
        el.style.background = 'white';
        el.style.border = '1px dashed #d1d5da';
    });

    // 高亮当前悬停的 grid-cell
    const dropTarget = getDropTarget(event);
    if (dropTarget && dropTarget.type === 'grid-cell') {
        dropTarget.element.style.background = 'rgba(102,126,234,0.1)';
        dropTarget.element.style.border = '2px dashed #667eea';
    }
}

/**
 * 拖拽离开目标区域
 */
function handleDragLeave(event) {
    const previewCanvas = document.getElementById('previewCanvas');

    // 检查是否真的离开了预览区
    const rect = previewCanvas.getBoundingClientRect();
    const x = event.clientX;
    const y = event.clientY;

    if (x < rect.left || x > rect.right || y < rect.top || y > rect.bottom) {
        previewCanvas.classList.remove('drag-over');

        // 如果预览区没有组件，显示提示
        const previewContent = document.getElementById('previewContent');
        if (previewContent.children.length === 0) {
            const dropHint = document.getElementById('dropHint');
            if (dropHint) {
                dropHint.classList.remove('hidden');
            }
        }
    }
}

/**
 * 放下组件
 */
function handleDrop(event) {
    event.preventDefault();

    const previewCanvas = document.getElementById('previewCanvas');
    previewCanvas.classList.remove('drag-over');

    if (!draggedComponent) {
        return;
    }

    // 获取放置位置
    const dropTarget = getDropTarget(event);

    // 添加组件
    if (dropTarget) {
        addComponentToTarget(draggedComponent, dropTarget);
    } else {
        // 默认添加到根节点
        addComponentToRoot(draggedComponent);
    }

    // 清理
    draggedComponent = null;
}

/**
 * 获取放置目标
 */
function getDropTarget(event) {
    const previewContent = document.getElementById('previewContent');
    const elements = document.elementsFromPoint(event.clientX, event.clientY);

    for (const element of elements) {
        if (element === previewContent) continue;

        // 优先级1：grid-cell（GRID 的网格单元格）
        const gridCell = element.closest('.grid-cell');
        if (gridCell) {
            const gridColumn = gridCell.dataset.gridColumn;
            const gridContainer = gridCell.closest('.component-preview');

            if (gridContainer) {
                const gridId = gridContainer.dataset.componentId;
                const gridComponent = findComponentById(gridId);

                if (gridComponent) {
                    return {
                        type: 'grid-cell',
                        component: gridComponent,
                        gridColumn: parseInt(gridColumn),
                        element: gridCell
                    };
                }
            }
        }

        // 优先级2：collapse-panel（COLLAPSE 的面板）
        const collapsePanel = element.closest('.collapse-panel');
        if (collapsePanel) {
            const panelIndex = collapsePanel.dataset.panelIndex;
            const collapseContainer = collapsePanel.closest('.component-preview');

            if (collapseContainer) {
                const collapseId = collapseContainer.dataset.componentId;
                const collapseComponent = findComponentById(collapseId);

                if (collapseComponent) {
                    return {
                        type: 'collapse-panel',
                        component: collapseComponent,
                        panelIndex: parseInt(panelIndex),
                        element: collapsePanel
                    };
                }
            }
        }

        // 优先级3：tab-content（TAB 的页签）
        const tabContent = element.closest('.tab-content');
        if (tabContent) {
            const tabContainer = tabContent.closest('.component-preview');

            if (tabContainer) {
                const tabId = tabContainer.dataset.componentId;
                const tabComponent = findComponentById(tabId);

                if (tabComponent) {
                    const activeTab = tabContainer.querySelector('.tab-item.active');
                    const tabIndex = activeTab ? parseInt(activeTab.dataset.tabIndex || 0) : 0;

                    return {
                        type: 'tab-panel',
                        component: tabComponent,
                        tabIndex: tabIndex,
                        element: tabContent
                    };
                }
            }
        }

        // 优先级4：普通容器
        const componentPreview = element.closest('.component-preview');
        if (componentPreview) {
            const componentId = componentPreview.dataset.componentId;
            const component = findComponentById(componentId);

            if (component && ComponentLibrary.types[component.type]?.isContainer) {
                return {
                    type: 'append',
                    component: component,
                    element: componentPreview
                };
            }
        }
    }

    return null;
}

/**
 * 添加组件到目标容器
 */
function addComponentToTarget(component, target) {
    if (target.type === 'grid-cell') {
        component.gridColumn = target.gridColumn;
        component.gridRow = 0;
        component.parentId = target.component.id;

        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);

        renderPreview();
        selectComponent(component.id);
        saveState();

    } else if (target.type === 'collapse-panel') {
        if (!target.component.panels[target.panelIndex].children) {
            target.component.panels[target.panelIndex].children = [];
        }
        target.component.panels[target.panelIndex].children.push(component.id);

        component.parentId = target.component.id;

        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);

        renderPreview();
        selectComponent(component.id);
        saveState();

    } else if (target.type === 'tab-panel') {
        if (!target.component.tabs[target.tabIndex].children) {
            target.component.tabs[target.tabIndex].children = [];
        }
        target.component.tabs[target.tabIndex].children.push(component.id);

        component.parentId = target.component.id;

        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);

        renderPreview();
        selectComponent(component.id);
        saveState();

    } else if (target.type === 'append') {
        if (!target.component.children) {
            target.component.children = [];
        }
        target.component.children.push(component);

        renderPreview();
        selectComponent(component.id);
        saveState();
    }
}

/**
 * 添加组件到根节点
 */
function addComponentToRoot(component) {
    // 如果没有根节点，创建一个页面组件
    if (!DesignerState.templateConfig.rootComponent) {
        const pageComponent = ComponentLibrary.createComponent('PAGE');
        pageComponent.children = [component];
        DesignerState.templateConfig.rootComponent = pageComponent;
    } else {
        if (!DesignerState.templateConfig.rootComponent.children) {
            DesignerState.templateConfig.rootComponent.children = [];
        }
        DesignerState.templateConfig.rootComponent.children.push(component);
    }

    // 刷新预览
    renderPreview();
    selectComponent(component.id);

    // 保存状态
    saveState();

    // 显示成功提示
    showNotification(`已添加组件: ${component.name}`, 'success');
}

/**
 * 查找组件
 */
function findComponentById(componentId, component = DesignerState.templateConfig.rootComponent) {
    if (!component) return null;

    if (component.id === componentId) {
        return component;
    }

    if (component.children) {
        for (const child of component.children) {
            const found = findComponentById(componentId, child);
            if (found) return found;
        }
    }

    if (component.tabs) {
        for (const tab of component.tabs) {
            if (tab.children) {
                for (const child of tab.children) {
                    const found = findComponentById(componentId, child);
                    if (found) return found;
                }
            }
        }
    }

    if (component.panels) {
        for (const panel of component.panels) {
            if (panel.children) {
                for (const child of panel.children) {
                    const found = findComponentById(componentId, child);
                    if (found) return found;
                }
            }
        }
    }

    return null;
}

/**
 * 删除组件
 */
function deleteComponent(componentId) {
    const root = DesignerState.templateConfig.rootComponent;
    if (!root) return;

    // 如果删除的是根节点
    if (root.id === componentId) {
        DesignerState.templateConfig.rootComponent = null;
        renderPreview();
        clearPropertyPanel();
        saveState();
        showNotification('已删除组件', 'success');
        return;
    }

    // 递归删除
    if (deleteComponentFromParent(componentId, root)) {
        renderPreview();
        clearPropertyPanel();
        saveState();
        showNotification('已删除组件', 'success');
    }
}

/**
 * 从父节点删除组件
 */
function deleteComponentFromParent(componentId, parent) {
    if (parent.children) {
        const index = parent.children.findIndex(c => c.id === componentId);
        if (index !== -1) {
            parent.children.splice(index, 1);
            return true;
        }

        for (const child of parent.children) {
            if (deleteComponentFromParent(componentId, child)) {
                return true;
            }
        }
    }

    if (parent.tabs) {
        for (const tab of parent.tabs) {
            if (deleteComponentFromParent(componentId, tab)) {
                return true;
            }
        }
    }

    if (parent.panels) {
        for (const panel of parent.panels) {
            if (deleteComponentFromParent(componentId, panel)) {
                return true;
            }
        }
    }

    return false;
}

/**
 * 移动组件（上移/下移）
 */
function moveComponent(componentId, direction) {
    const parent = findParentComponent(componentId);
    if (!parent || !parent.children) return;

    const index = parent.children.findIndex(c => c.id === componentId);
    if (index === -1) return;

    const newIndex = direction === 'up' ? index - 1 : index + 1;
    if (newIndex < 0 || newIndex >= parent.children.length) return;

    // 交换位置
    [parent.children[index], parent.children[newIndex]] =
    [parent.children[newIndex], parent.children[index]];

    renderPreview();
    selectComponent(componentId);
    saveState();
}

/**
 * 查找父组件
 */
function findParentComponent(componentId, component = DesignerState.templateConfig.rootComponent, parent = null) {
    if (!component) return null;

    if (component.id === componentId) {
        return parent;
    }

    if (component.children) {
        for (const child of component.children) {
            const found = findParentComponent(componentId, child, component);
            if (found) return found;
        }
    }

    if (component.tabs) {
        for (const tab of component.tabs) {
            if (tab.children) {
                for (const child of tab.children) {
                    const found = findParentComponent(componentId, child, tab);
                    if (found) return found;
                }
            }
        }
    }

    if (component.panels) {
        for (const panel of component.panels) {
            if (panel.children) {
                for (const child of panel.children) {
                    const found = findParentComponent(componentId, child, panel);
                    if (found) return found;
                }
            }
        }
    }

    return null;
}

/**
 * 复制组件
 */
function duplicateComponent(componentId) {
    const component = findComponentById(componentId);
    if (!component) return;

    // 深拷贝组件
    const newComponent = JSON.parse(JSON.stringify(component));
    newComponent.id = ComponentLibrary.generateId();
    newComponent.name = component.name + ' (副本)';
    newComponent.code = component.code + '_copy';

    // 添加到同级
    const parent = findParentComponent(componentId);
    if (parent && parent.children) {
        const index = parent.children.findIndex(c => c.id === componentId);
        parent.children.splice(index + 1, 0, newComponent);
    }

    renderPreview();
    selectComponent(newComponent.id);
    saveState();
    showNotification(`已复制组件: ${newComponent.name}`, 'success');
}

/**
 * 显示通知
 */
function showNotification(message, type = 'info') {
    // 创建通知元素
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="fas ${type === 'success' ? 'fa-check-circle' : type === 'error' ? 'fa-exclamation-circle' : 'fa-info-circle'}"></i>
        <span>${message}</span>
    `;

    notification.style.cssText = `
        position: fixed;
        top: 80px;
        right: 20px;
        padding: 12px 20px;
        background: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#667eea'};
        color: white;
        border-radius: 6px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        display: flex;
        align-items: center;
        gap: 8px;
        z-index: 3000;
        animation: slideIn 0.3s ease;
    `;

    document.body.appendChild(notification);

    // 3秒后移除
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => document.body.removeChild(notification), 300);
    }, 3000);
}

// 添加动画样式
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
`;
document.head.appendChild(style);