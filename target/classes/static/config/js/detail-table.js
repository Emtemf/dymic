/**
 * 明细表交互模块
 * 处理增行、删行、弹窗编辑、行状态管理
 */

const DetailTableHelper = {
    generateRowUid() {
        return 'row_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    },

    getTableDraft(tableId) {
        if (!window.DesignerState || !window.DesignerState.detailDrafts) {
            window.DesignerState.detailDrafts = {};
        }
        if (!window.DesignerState.detailDrafts[tableId]) {
            window.DesignerState.detailDrafts[tableId] = [];
        }
        return window.DesignerState.detailDrafts[tableId];
    },

    addRow(tableId, columns, maxRows) {
        const rows = this.getTableDraft(tableId);

        if (maxRows && rows.length >= maxRows) {
            alert('已达到最大行数限制（' + maxRows + '行）');
            return;
        }

        const newRow = {
            _row_uid: this.generateRowUid(),
            _row_op: 'ADD'
        };

        if (columns) {
            columns.forEach(col => {
                newRow[col.code] = col.type === 'number' ? 0 : '';
            });
        }

        rows.push(newRow);
        this.renderTableBody(tableId, columns);
    },

    deleteRow(tableId, rowUid) {
        const rows = this.getTableDraft(tableId);
        const row = rows.find(r => r._row_uid === rowUid);
        if (row) {
            if (row._row_op === 'ADD') {
                const idx = rows.indexOf(row);
                rows.splice(idx, 1);
            } else {
                row._row_op = 'DELETE';
            }
            this.renderTableBody(tableId, this._getColumns(tableId));
        }
    },

    restoreRow(tableId, rowUid) {
        const rows = this.getTableDraft(tableId);
        const row = rows.find(r => r._row_uid === rowUid);
        if (row && row._row_op === 'DELETE') {
            row._row_op = 'NONE';
            this.renderTableBody(tableId, this._getColumns(tableId));
        }
    },

    editRow(tableId, rowUid) {
        const rows = this.getTableDraft(tableId);
        const row = rows.find(r => r._row_uid === rowUid);
        if (!row) return;

        const columns = this._getColumns(tableId);
        const modal = document.getElementById('detailRowModal');
        const modalBody = document.getElementById('detailRowModalBody');

        let formHtml = '';
        columns.forEach(col => {
            const value = row[col.code] !== undefined ? row[col.code] : '';
            const inputType = col.type === 'number' ? 'number' : 'text';
            formHtml += `
                <div style="margin-bottom: 12px;">
                    <label style="display:block;margin-bottom:4px;font-weight:600;">${col.name}</label>
                    <input type="${inputType}" id="modal_field_${col.code}" value="${value}"
                           style="width:100%;padding:6px 10px;border:1px solid #d1d5da;border-radius:4px;">
                </div>
            `;
        });

        modalBody.innerHTML = formHtml;
        modal.dataset.tableId = tableId;
        modal.dataset.rowUid = rowUid;
        modal.style.display = 'flex';
    },

    saveModalDraft() {
        const modal = document.getElementById('detailRowModal');
        const tableId = modal.dataset.tableId;
        const rowUid = modal.dataset.rowUid;

        const rows = this.getTableDraft(tableId);
        const row = rows.find(r => r._row_uid === rowUid);
        if (!row) return;

        const columns = this._getColumns(tableId);
        columns.forEach(col => {
            const input = document.getElementById('modal_field_' + col.code);
            if (input) {
                row[col.code] = col.type === 'number' ? parseFloat(input.value) || 0 : input.value;
            }
        });

        if (row._row_op !== 'ADD') {
            row._row_op = 'UPDATE';
        }

        modal.style.display = 'none';
        this.renderTableBody(tableId, columns);
    },

    closeModal() {
        document.getElementById('detailRowModal').style.display = 'none';
    },

    renderTableBody(tableId, columns) {
        const rows = this.getTableDraft(tableId);
        const tbody = document.getElementById('detail_tbody_' + tableId);
        if (!tbody) return;

        if (rows.length === 0) {
            tbody.innerHTML = `<tr><td colspan="${(columns ? columns.length : 0) + 1}" style="padding:20px;text-align:center;color:#b4b4b4;">暂无数据</td></tr>`;
            return;
        }

        tbody.innerHTML = rows.map(row => {
            const isDeleted = row._row_op === 'DELETE';
            const rowStyle = isDeleted ? 'opacity:0.4;text-decoration:line-through;' : '';

            let cells = '';
            if (columns) {
                columns.forEach(col => {
                    const val = row[col.code] !== undefined ? row[col.code] : '';
                    cells += `<td style="padding:6px 10px;border:1px solid #e1e4e8;${rowStyle}">${val}</td>`;
                });
            }

            let actions = '';
            if (isDeleted) {
                actions = `<button onclick="DetailTableHelper.restoreRow('${tableId}','${row._row_uid}')" style="padding:2px 8px;color:#f59e0b;border:1px solid #f59e0b;background:white;border-radius:3px;cursor:pointer;">撤销删除</button>`;
            } else {
                actions = `
                    <button onclick="DetailTableHelper.editRow('${tableId}','${row._row_uid}')" style="padding:2px 8px;color:#667eea;border:1px solid #667eea;background:white;border-radius:3px;cursor:pointer;margin-right:4px;">编辑</button>
                    <button onclick="DetailTableHelper.deleteRow('${tableId}','${row._row_uid}')" style="padding:2px 8px;color:#ef4444;border:1px solid #ef4444;background:white;border-radius:3px;cursor:pointer;">删除</button>
                `;
            }

            return `<tr>${cells}<td style="padding:6px 10px;border:1px solid #e1e4e8;">${actions}</td></tr>`;
        }).join('');
    },

    _getColumns(tableId) {
        if (!window.DesignerState || !window.DesignerState.templateConfig) return [];
        const root = window.DesignerState.templateConfig.rootComponent;
        if (!root) return [];
        const comp = this._findComponent(root, tableId);
        return comp ? (comp.columns || []) : [];
    },

    _findComponent(component, id) {
        if (!component) return null;
        if (component.id === id) return component;
        if (component.children) {
            for (const child of component.children) {
                const found = this._findComponent(child, id);
                if (found) return found;
            }
        }
        if (component.tabs) {
            for (const tab of component.tabs) {
                if (tab.children) {
                    for (const child of tab.children) {
                        const found = this._findComponent(child, id);
                        if (found) return found;
                    }
                }
            }
        }
        return null;
    }
};
