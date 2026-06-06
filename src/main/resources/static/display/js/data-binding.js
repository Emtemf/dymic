/**
 * 数据绑定系统
 * 处理字段数据的双向绑定
 */

class DataBinding {
    constructor() {
        this.data = {};
        this.watchers = {};
        this.fieldComponents = {};
    }

    /**
     * 初始化数据
     */
    initData(initialData) {
        this.data = initialData || {};
        this.notifyAll();
    }

    /**
     * 获取数据
     */
    getData() {
        return JSON.parse(JSON.stringify(this.data));
    }

    /**
     * 获取字段值
     */
    getField(fieldPath) {
        const parts = fieldPath.split('.');
        let value = this.data;

        for (const part of parts) {
            if (value === null || value === undefined) {
                return undefined;
            }
            value = value[part];
        }

        return value;
    }

    /**
     * 设置字段值
     */
    setField(fieldPath, value) {
        const parts = fieldPath.split('.');
        let target = this.data;

        for (let i = 0; i < parts.length - 1; i++) {
            const part = parts[i];
            if (!target[part]) {
                target[part] = {};
            }
            target = target[part];
        }

        const oldValue = target[parts[parts.length - 1]];
        target[parts[parts.length - 1]] = value;

        // 触发监听器
        this.notify(fieldPath, oldValue, value);
    }

    /**
     * 批量设置数据
     */
    setData(data) {
        this.data = data;
        this.notifyAll();
    }

    /**
     * 监听字段变化
     */
    watch(fieldPath, callback) {
        if (!this.watchers[fieldPath]) {
            this.watchers[fieldPath] = [];
        }
        this.watchers[fieldPath].push(callback);

        // 返回取消监听的函数
        return () => {
            this.watchers[fieldPath] = this.watchers[fieldPath].filter(cb => cb !== callback);
        };
    }

    /**
     * 触发字段监听器
     */
    notify(fieldPath, oldValue, newValue) {
        if (oldValue === newValue) return;

        const watchers = this.watchers[fieldPath];
        if (watchers && watchers.length > 0) {
            watchers.forEach(callback => {
                try {
                    callback(newValue, oldValue, fieldPath);
                } catch (error) {
                    console.error('监听器执行错误:', error);
                }
            });
        }

        // 触发父字段监听器
        const parts = fieldPath.split('.');
        for (let i = parts.length - 1; i > 0; i--) {
            const parentPath = parts.slice(0, i).join('.');
            const parentWatchers = this.watchers[parentPath];
            if (parentWatchers && parentWatchers.length > 0) {
                const parentValue = this.getField(parentPath);
                parentWatchers.forEach(callback => {
                    try {
                        callback(parentValue, null, parentPath);
                    } catch (error) {
                        console.error('父字段监听器执行错误:', error);
                    }
                });
            }
        }
    }

    /**
     * 触发所有监听器
     */
    notifyAll() {
        Object.keys(this.watchers).forEach(fieldPath => {
            const value = this.getField(fieldPath);
            this.notify(fieldPath, null, value);
        });
    }

    /**
     * 注册字段组件
     */
    registerComponent(componentId, fieldPath) {
        this.fieldComponents[componentId] = fieldPath;

        // 监听字段变化，更新组件
        this.watch(fieldPath, (newValue) => {
            this.updateComponentValue(componentId, newValue);
        });
    }

    /**
     * 更新组件值
     */
    updateComponentValue(componentId, value) {
        const component = document.getElementById(componentId);
        if (!component) return;

        const input = component.querySelector('input, select, textarea');
        if (!input) return;

        if (input.type === 'checkbox') {
            input.checked = Boolean(value);
        } else if (input.tagName === 'SELECT' && input.multiple) {
            // 多选下拉框
            const values = Array.isArray(value) ? value : [];
            Array.from(input.options).forEach(option => {
                option.selected = values.includes(option.value);
            });
        } else {
            input.value = value || '';
        }
    }

    /**
     * 从组件获取值
     */
    getValueFromComponent(componentId) {
        const component = document.getElementById(componentId);
        if (!component) return undefined;

        const input = component.querySelector('input, select, textarea');
        if (!input) return undefined;

        if (input.type === 'checkbox') {
            return input.checked;
        } else if (input.tagName === 'SELECT' && input.multiple) {
            // 多选下拉框
            return Array.from(input.selectedOptions).map(option => option.value);
        } else if (input.type === 'number' || input.type === 'range') {
            const value = parseFloat(input.value);
            return isNaN(value) ? undefined : value;
        } else if (input.type === 'date') {
            return input.value ? new Date(input.value) : undefined;
        } else {
            return input.value;
        }
    }

    /**
     * 绑定组件事件
     */
    bindComponentEvent(componentId, fieldPath) {
        const component = document.getElementById(componentId);
        if (!component) return;

        const input = component.querySelector('input, select, textarea');
        if (!input) return;

        // 绑定change事件
        input.addEventListener('change', () => {
            const value = this.getValueFromComponent(componentId);
            this.setField(fieldPath, value);
        });

        // 绑定input事件（实时更新）
        if (input.tagName === 'INPUT' && input.type === 'text') {
            input.addEventListener('input', () => {
                const value = this.getValueFromComponent(componentId);
                this.setField(fieldPath, value);
            });
        }
    }

    /**
     * 双向绑定组件
     */
    bindTwoWay(componentId, fieldPath) {
        // 注册组件
        this.registerComponent(componentId, fieldPath);

        // 绑定事件
        this.bindComponentEvent(componentId, fieldPath);

        // 初始化值
        const value = this.getField(fieldPath);
        if (value !== undefined) {
            this.updateComponentValue(componentId, value);
        }
    }

    /**
     * 验证字段
     */
    validateField(fieldPath, rules) {
        const value = this.getField(fieldPath);
        const errors = [];

        rules.forEach(rule => {
            if (!rule.enabled) return;

            try {
                const valid = this.evaluateRule(rule, value);
                if (!valid) {
                    errors.push({
                        rule: rule.type,
                        message: rule.message || '验证失败'
                    });
                }
            } catch (error) {
                console.error('验证规则执行错误:', error);
            }
        });

        return {
            valid: errors.length === 0,
            errors: errors
        };
    }

    /**
     * 执行验证规则
     */
    evaluateRule(rule, value) {
        switch (rule.type) {
            case 'required':
                return value !== null && value !== undefined && value !== '';
            case 'min':
                return value >= rule.value;
            case 'max':
                return value <= rule.value;
            case 'minLength':
                return (value || '').length >= rule.value;
            case 'maxLength':
                return (value || '').length <= rule.value;
            case 'pattern':
                return new RegExp(rule.value).test(value);
            case 'custom':
                // 自定义表达式
                return this.evaluateCustomExpression(rule.expression, value);
            default:
                return true;
        }
    }

    /**
     * 执行自定义表达式
     */
    evaluateCustomExpression(expression, value) {
        try {
            const func = new Function('value', 'data', `return ${expression};`);
            return func(value, this.data);
        } catch (error) {
            console.error('自定义表达式执行错误:', error);
            return false;
        }
    }

    /**
     * 清空数据
     */
    clear() {
        this.data = {};
        this.notifyAll();
    }

    /**
     * 重置数据
     */
    reset(initialData) {
        this.data = initialData || {};
        this.notifyAll();
    }

    /**
     * 导出数据
     */
    export() {
        return JSON.stringify(this.data, null, 2);
    }

    /**
     * 导入数据
     */
    import(jsonString) {
        try {
            const data = JSON.parse(jsonString);
            this.setData(data);
            return { success: true };
        } catch (error) {
            return { success: false, error: error.message };
        }
    }

    /**
     * 计算字段
     */
    compute(computedField, dependencies, expression) {
        // 监听依赖字段
        dependencies.forEach(depField => {
            this.watch(depField, () => {
                this.recompute(computedField, dependencies, expression);
            });
        });

        // 初始计算
        this.recompute(computedField, dependencies, expression);
    }

    /**
     * 重新计算字段
     */
    recompute(computedField, dependencies, expression) {
        const values = dependencies.map(dep => this.getField(dep));

        try {
            const func = new Function('...args', `return ${expression};`);
            const result = func(...values);
            this.setField(computedField, result);
        } catch (error) {
            console.error('计算字段执行错误:', error);
        }
    }

    /**
     * 获取字段路径列表
     */
    getFieldPaths() {
        const paths = [];

        const traverse = (obj, currentPath) => {
            if (!obj || typeof obj !== 'object') return;

            Object.keys(obj).forEach(key => {
                const path = currentPath ? `${currentPath}.${key}` : key;
                paths.push(path);

                if (typeof obj[key] === 'object' && obj[key] !== null) {
                    traverse(obj[key], path);
                }
            });
        };

        traverse(this.data, '');

        return paths;
    }
}

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = DataBinding;
}