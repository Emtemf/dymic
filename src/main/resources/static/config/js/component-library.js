/**
 * 组件库定义
 * 定义所有可用的组件类型及其默认配置
 */

const ComponentLibrary = {
    // 组件类型定义
    types: {
        // 布局组件
        PAGE: {
            name: '页面',
            icon: 'fa-file',
            category: 'layout',
            isContainer: true,
            defaultConfig: {
                name: '新建页面',
                code: 'page_1',
                description: '',
                width: '100%',
                height: 'auto',
                children: []
            }
        },
        CARD: {
            name: '卡片',
            icon: 'fa-square',
            category: 'layout',
            isContainer: true,
            defaultConfig: {
                name: '新建卡片',
                code: 'card_1',
                description: '',
                width: '100%',
                height: 'auto',
                title: '卡片标题',
                children: []
            }
        },
        GRID: {
            name: '栅格',
            icon: 'fa-th',
            category: 'layout',
            isContainer: true,
            defaultConfig: {
                name: '栅格布局',
                code: 'grid_1',
                description: '',
                columns: 2, // 列数
                gutter: 16, // 间距
                children: []
            }
        },
        ROW: {
            name: '行',
            icon: 'fa-grip-lines',
            category: 'layout',
            isContainer: true,
            defaultConfig: {
                name: '行',
                code: 'row_1',
                description: '',
                gutter: 16,  // 列间距
                children: []
            }
        },
        COL: {
            name: '列',
            icon: 'fa-grip-lines-vertical',
            category: 'layout',
            isContainer: true,
            defaultConfig: {
                name: '列',
                code: 'col_1',
                description: '',
                span: 12,    // 栅格列数 (24栅格系统，12=半行)
                offset: 0,   // 偏移量
                children: []
            }
        },
        TAB: {
            name: 'Tab页签',
            icon: 'fa-folder',
            category: 'layout',
            isContainer: true,
            defaultConfig: {
                name: 'Tab页签',
                code: 'tab_1',
                description: '',
                tabs: [
                    { name: '页签1', code: 'tab1', children: [] },
                    { name: '页签2', code: 'tab2', children: [] }
                ]
            }
        },
        COLLAPSE: {
            name: '折叠面板',
            icon: 'fa-chevron-down',
            category: 'layout',
            isContainer: true,
            defaultConfig: {
                name: '折叠面板',
                code: 'collapse_1',
                description: '',
                panels: [
                    { name: '面板1', code: 'panel1', children: [] }
                ]
            }
        },

        // 基础组件
        INPUT: {
            name: '输入框',
            icon: 'fa-font',
            category: 'basic',
            isContainer: false,
            defaultConfig: {
                name: '输入框',
                code: 'input_1',
                description: '',
                width: '100%',
                span: 24,
                offset: 0,
                fieldPath: '',
                dataType: 'string',
                placeholder: '请输入',
                required: false,
                readonly: false,
                maxLength: 200
            }
        },
        SELECT: {
            name: '下拉框',
            icon: 'fa-list',
            category: 'basic',
            isContainer: false,
            defaultConfig: {
                name: '下拉框',
                code: 'select_1',
                description: '',
                width: '100%',
                span: 24,
                offset: 0,
                fieldPath: '',
                dataType: 'string',
                placeholder: '请选择',
                dataSourceType: 'STATIC', // STATIC, DICT, HTTP, PLATFORM, INTERNAL
                dataProviderId: '',
                multiple: false,
                required: false,
                readonly: false
            }
        },
        DATE: {
            name: '日期选择',
            icon: 'fa-calendar',
            category: 'basic',
            isContainer: false,
            defaultConfig: {
                name: '日期选择',
                code: 'date_1',
                description: '',
                width: '100%',
                span: 24,
                offset: 0,
                fieldPath: '',
                dataType: 'date',
                placeholder: '请选择日期',
                format: 'YYYY-MM-DD',
                showTime: false,
                required: false,
                readonly: false
            }
        },
        NUMBER: {
            name: '数字输入',
            icon: 'fa-sort-numeric-up',
            category: 'basic',
            isContainer: false,
            defaultConfig: {
                name: '数字输入',
                code: 'number_1',
                description: '',
                width: '100%',
                span: 24,
                offset: 0,
                fieldPath: '',
                dataType: 'number',
                placeholder: '请输入数字',
                min: 0,
                max: 999999,
                precision: 2,
                required: false,
                readonly: false
            }
        },
        MONEY: {
            name: '金额输入',
            icon: 'fa-dollar-sign',
            category: 'basic',
            isContainer: false,
            defaultConfig: {
                name: '金额输入',
                code: 'money_1',
                description: '',
                width: '100%',
                span: 24,
                offset: 0,
                fieldPath: '',
                dataType: 'number',
                placeholder: '请输入金额',
                currency: 'CNY',
                precision: 2,
                min: 0,
                max: 999999999,
                required: false,
                readonly: false
            }
        },
        TEXTAREA: {
            name: '多行文本',
            icon: 'fa-align-left',
            category: 'basic',
            isContainer: false,
            defaultConfig: {
                name: '多行文本',
                code: 'textarea_1',
                description: '',
                width: '100%',
                span: 24,
                offset: 0,
                fieldPath: '',
                dataType: 'string',
                placeholder: '请输入内容',
                rows: 4,
                maxLength: 500,
                required: false,
                readonly: false
            }
        },

        // 业务组件
        SUPPLIER_SELECT: {
            name: '供应商选择',
            icon: 'fa-building',
            category: 'business',
            isContainer: false,
            defaultConfig: {
                name: '供应商选择',
                code: 'supplier_select_1',
                description: '',
                width: '100%',
                span: 24,
                offset: 0,
                fieldPath: 'supplierId',
                dataType: 'string',
                placeholder: '请选择供应商',
                dialogTitle: '选择供应商',
                displayField: 'name',
                valueField: 'id',
                required: false,
                readonly: false
            }
        },
        DETAIL_TABLE: {
            name: '明细表',
            icon: 'fa-table',
            category: 'business',
            isContainer: true,
            defaultConfig: {
                name: '明细表',
                code: 'detail_table_1',
                description: '',
                width: '100%',
                fieldPath: 'details',
                dataType: 'array',
                columns: [
                    { name: '序号', code: 'index', width: 60 },
                    { name: '名称', code: 'name', width: 200 },
                    { name: '数量', code: 'quantity', width: 100 },
                    { name: '单价', code: 'price', width: 100 },
                    { name: '金额', code: 'amount', width: 100 }
                ],
                enableAdd: true,
                enableDelete: true,
                enableEdit: true,
                showSummary: true
            }
        },
        ATTACHMENT: {
            name: '附件上传',
            icon: 'fa-paperclip',
            category: 'business',
            isContainer: false,
            defaultConfig: {
                name: '附件上传',
                code: 'attachment_1',
                description: '',
                width: '100%',
                span: 24,
                offset: 0,
                fieldPath: 'attachments',
                dataType: 'array',
                maxCount: 10,
                maxSize: 10 * 1024 * 1024, // 10MB
                acceptTypes: '.pdf,.doc,.docx,.xls,.xlsx',
                required: false
            }
        },
        IMAGE: {
            name: '图片上传',
            icon: 'fa-image',
            category: 'business',
            isContainer: false,
            defaultConfig: {
                name: '图片上传',
                code: 'image_1',
                description: '',
                width: '100%',
                span: 24,
                offset: 0,
                fieldPath: 'images',
                dataType: 'array',
                maxCount: 5,
                maxSize: 5 * 1024 * 1024, // 5MB
                acceptTypes: '.jpg,.jpeg,.png,.gif',
                required: false
            }
        },

        // 动作组件
        BUTTON: {
            name: '按钮',
            icon: 'fa-hand-pointer',
            category: 'action',
            isContainer: false,
            defaultConfig: {
                name: '按钮',
                code: 'button_1',
                description: '',
                width: 'auto',
                span: 6,
                offset: 0,
                text: '按钮',
                buttonType: 'primary', // primary, secondary, success, danger, warning
                icon: '',
                actionType: 'custom', // custom, submit, reset
                actionScript: ''
            }
        },
        QUERY_DIALOG: {
            name: '查询弹窗',
            icon: 'fa-search',
            category: 'action',
            isContainer: false,
            defaultConfig: {
                name: '查询弹窗',
                code: 'query_dialog_1',
                description: '',
                width: 'auto',
                span: 6,
                offset: 0,
                text: '查询',
                dialogTitle: '查询条件',
                dialogWidth: 800,
                queryFields: [],
                targetField: ''
            }
        },
        SAVE_BUTTON: {
            name: '保存按钮',
            icon: 'fa-save',
            category: 'action',
            isContainer: false,
            defaultConfig: {
                name: '保存按钮',
                code: 'save_button_1',
                description: '',
                width: 'auto',
                span: 6,
                offset: 0,
                text: '保存',
                confirmMessage: '确定保存吗？',
                apiEndpoint: '/api/contract/save'
            }
        },
        SUBMIT_BUTTON: {
            name: '提交按钮',
            icon: 'fa-check',
            category: 'action',
            isContainer: false,
            defaultConfig: {
                name: '提交按钮',
                code: 'submit_button_1',
                description: '',
                width: 'auto',
                span: 6,
                offset: 0,
                text: '提交',
                confirmMessage: '确定提交吗？',
                apiEndpoint: '/api/contract/submit'
            }
        }
    },

    // 组件分类
    categories: {
        layout: { name: '布局组件', icon: 'fa-th-large' },
        basic: { name: '基础组件', icon: 'fa-edit' },
        business: { name: '业务组件', icon: 'fa-briefcase' },
        action: { name: '动作组件', icon: 'fa-mouse-pointer' }
    },

    // 获取组件定义
    getComponentDef(type) {
        return this.types[type] || null;
    },

    // 创建组件实例
    createComponent(type) {
        const def = this.getComponentDef(type);
        if (!def) {
            throw new Error(`未知的组件类型: ${type}`);
        }

        const instance = {
            id: this.generateId(),
            type: type,
            ...JSON.parse(JSON.stringify(def.defaultConfig))
        };

        if (!instance.code || instance.code.endsWith('_1')) {
            instance.code = this.createComponentCode(type, instance.id);
        }
        if (this.requiresFieldPath(type) && !instance.fieldPath) {
            instance.fieldPath = this.createFieldPath(instance.code);
        }

        return instance;
    },

    requiresFieldPath(type) {
        const def = this.getComponentDef(type);
        return !!def && def.category !== 'layout' && def.category !== 'action';
    },

    createComponentCode(type, id) {
        const token = String(id || '').replace(/^comp_/, '').split('_')[0] || Date.now();
        return `${(type || 'comp').toLowerCase()}_${token}`;
    },

    createFieldPath(code) {
        const parts = String(code || '')
            .replace(/[^a-zA-Z0-9]+/g, ' ')
            .trim()
            .split(/\s+/)
            .filter(Boolean);
        if (parts.length === 0) {
            return 'field';
        }
        return parts[0].toLowerCase() + parts.slice(1)
            .map(part => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
            .join('');
    },

    // 生成唯一ID
    generateId() {
        return 'comp_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    },

    // 获取分类下的组件列表
    getComponentsByCategory(category) {
        return Object.keys(this.types)
            .filter(type => this.types[type].category === category)
            .map(type => ({
                type,
                ...this.types[type]
            }));
    },

    // 验证组件配置
    validateComponent(component) {
        const def = this.getComponentDef(component.type);
        if (!def) {
            return { valid: false, message: `未知的组件类型: ${component.type}` };
        }

        if (!component.name) {
            return { valid: false, message: '组件名称不能为空' };
        }
        if (!component.code) {
            component.code = (component.type || 'comp').toLowerCase() + '_' + Date.now();
        }

        // 检查字段路径（如果有）
        if (def.category !== 'layout' && def.category !== 'action' && !component.fieldPath) {
            return { valid: false, message: '数据绑定字段路径不能为空' };
        }

        return { valid: true };
    }
};

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ComponentLibrary;
}