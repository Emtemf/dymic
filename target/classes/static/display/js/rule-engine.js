/**
 * 规则引擎
 * 处理动态显隐、必填、只读等规则
 */

class RuleEngine {
    constructor(rules) {
        this.rules = rules || [];
        this.context = {};
    }

    /**
     * 设置上下文数据
     */
    setContext(data) {
        this.context = { data: data };
    }

    /**
     * 执行所有规则
     */
    executeAll() {
        const results = {};

        this.rules.forEach(rule => {
            if (!rule.enabled) {
                results[rule.id] = { executed: false };
                return;
            }

            try {
                const result = this.evaluateExpression(rule.expression);
                results[rule.id] = {
                    executed: true,
                    result: result,
                    error: null
                };
            } catch (error) {
                results[rule.id] = {
                    executed: true,
                    result: null,
                    error: error.message
                };
            }
        });

        return results;
    }

    /**
     * 执行单个规则
     */
    execute(ruleId) {
        const rule = this.rules.find(r => r.id === ruleId);
        if (!rule) {
            return null;
        }

        if (!rule.enabled) {
            return { executed: false };
        }

        try {
            const result = this.evaluateExpression(rule.expression);
            return {
                executed: true,
                result: result,
                error: null
            };
        } catch (error) {
            return {
                executed: true,
                result: null,
                error: error.message
            };
        }
    }

    /**
     * 执行组件规则
     */
    executeComponentRule(componentId, ruleType) {
        const rule = this.rules.find(r =>
            r.componentId === componentId && r.type === ruleType
        );

        if (!rule) {
            return null;
        }

        return this.execute(rule.id);
    }

    /**
     * 执行表达式
     */
    evaluateExpression(expression) {
        // 预处理表达式
        const processedExpression = this.preprocessExpression(expression);

        // 创建安全的执行环境
        const sandbox = this.createSandbox();

        // 执行表达式
        try {
            const func = new Function('context', `with(context) { return ${processedExpression}; }`);
            return func(sandbox);
        } catch (error) {
            console.error('表达式执行错误:', error);
            throw error;
        }
    }

    /**
     * 预处理表达式
     */
    preprocessExpression(expression) {
        // 替换字段引用
        // data.field -> context.data.field
        let processed = expression;

        // 处理点号访问
        processed = processed.replace(/(\w+)\.(\w+)/g, (match, obj, prop) => {
            if (obj !== 'context' && obj !== 'Math' && obj !== 'Date') {
                return `context.data.${obj}.${prop}`;
            }
            return match;
        });

        // 处理直接字段访问
        processed = processed.replace(/(?<!\.)(\w+)(?!\.)/g, (match) => {
            if (match !== 'true' && match !== 'false' && match !== 'null' && match !== 'undefined') {
                if (!isNaN(match)) {
                    return match; // 数字
                }
                if (match.startsWith('"') || match.startsWith("'")) {
                    return match; // 字符串
                }
                return `context.data.${match}`;
            }
            return match;
        });

        return processed;
    }

    /**
     * 创建沙箱环境
     */
    createSandbox() {
        return {
            data: this.context.data || {},
            Math: Math,
            Date: Date,
            // 禁止访问危险对象
            window: undefined,
            document: undefined,
            eval: undefined,
            Function: undefined
        };
    }

    /**
     * 添加规则
     */
    addRule(rule) {
        this.rules.push(rule);
    }

    /**
     * 删除规则
     */
    removeRule(ruleId) {
        this.rules = this.rules.filter(r => r.id !== ruleId);
    }

    /**
     * 更新规则
     */
    updateRule(ruleId, updates) {
        const rule = this.rules.find(r => r.id === ruleId);
        if (rule) {
            Object.assign(rule, updates);
        }
    }

    /**
     * 验证表达式
     */
    validateExpression(expression) {
        try {
            // 尝试编译表达式
            const processed = this.preprocessExpression(expression);
            new Function('context', `with(context) { return ${processed}; }`);
            return { valid: true, error: null };
        } catch (error) {
            return { valid: false, error: error.message };
        }
    }

    /**
     * 获取组件的所有规则
     */
    getComponentRules(componentId) {
        return this.rules.filter(r => r.componentId === componentId);
    }

    /**
     * 获取规则类型
     */
    getRuleTypes() {
        return {
            visibility: '可见性规则',
            required: '必填规则',
            readonly: '只读规则',
            validation: '验证规则'
        };
    }

    /**
     * 应用规则到DOM
     */
    applyToDOM(componentId, ruleType, result) {
        const component = document.getElementById(`component_${componentId}`);
        if (!component) return;

        switch (ruleType) {
            case 'visibility':
                this.applyVisibility(component, result);
                break;
            case 'required':
                this.applyRequired(component, result);
                break;
            case 'readonly':
                this.applyReadonly(component, result);
                break;
            case 'validation':
                this.applyValidation(component, result);
                break;
        }
    }

    /**
     * 应用可见性规则
     */
    applyVisibility(component, visible) {
        if (visible) {
            component.style.display = 'block';
            component.removeAttribute('data-hidden');
        } else {
            component.style.display = 'none';
            component.setAttribute('data-hidden', 'true');
        }
    }

    /**
     * 应用必填规则
     */
    applyRequired(component, required) {
        const input = component.querySelector('input, select, textarea');
        if (!input) return;

        input.required = required;
        input.classList.toggle('required', required);

        // 更新标签
        const label = component.querySelector('label');
        if (label) {
            const requiredSpan = label.querySelector('.required');
            if (required && !requiredSpan) {
                label.innerHTML += '<span class="required">*</span>';
            } else if (!required && requiredSpan) {
                requiredSpan.remove();
            }
        }
    }

    /**
     * 应用只读规则
     */
    applyReadonly(component, readonly) {
        const input = component.querySelector('input, select, textarea');
        if (!input) return;

        if (input.tagName === 'SELECT') {
            input.disabled = readonly;
        } else {
            input.readOnly = readonly;
        }

        input.classList.toggle('readonly', readonly);

        // 添加只读样式
        if (readonly) {
            input.style.background = '#f6f8fa';
            input.style.cursor = 'not-allowed';
        } else {
            input.style.background = '';
            input.style.cursor = '';
        }
    }

    /**
     * 应用验证规则
     */
    applyValidation(component, validation) {
        const input = component.querySelector('input, select, textarea');
        if (!input) return;

        if (validation.valid) {
            input.classList.remove('error');
            component.classList.remove('has-error');

            // 移除错误提示
            const errorTip = component.querySelector('.error-tip');
            if (errorTip) {
                errorTip.remove();
            }
        } else {
            input.classList.add('error');
            component.classList.add('has-error');

            // 添加错误提示
            let errorTip = component.querySelector('.error-tip');
            if (!errorTip) {
                errorTip = document.createElement('div');
                errorTip.className = 'error-tip';
                component.appendChild(errorTip);
            }
            errorTip.textContent = validation.message || '验证失败';
        }
    }

    /**
     * 创建示例规则
     */
    createExampleRules() {
        return [
            {
                id: 'rule_visibility_1',
                componentId: 'component_001',
                type: 'visibility',
                expression: 'data.status === "approved"',
                enabled: true,
                description: '当状态为"已审批"时显示'
            },
            {
                id: 'rule_required_1',
                componentId: 'component_002',
                type: 'required',
                expression: 'data.amount > 10000',
                enabled: true,
                description: '当金额大于10000时必填'
            },
            {
                id: 'rule_readonly_1',
                componentId: 'component_003',
                type: 'readonly',
                expression: 'data.status !== "draft"',
                enabled: true,
                description: '当状态不是"草稿"时只读'
            }
        ];
    }
}

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = RuleEngine;
}