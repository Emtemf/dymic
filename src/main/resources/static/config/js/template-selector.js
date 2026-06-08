/**
 * Template/Version selection flow — 3-step entry before designer
 */
const TemplateSelector = {
    state: {
        step: 1,
        templateId: null,
        versionId: null,
        templates: [],
        versions: []
    },

    init() {
        const params = new URLSearchParams(window.location.search);
        const tid = params.get('templateId');
        const vid = params.get('versionId');
        if (tid && vid) {
            this.state.templateId = parseInt(tid);
            this.state.versionId = parseInt(vid);
            this.state.step = 3;
            this.showDesigner();
            return;
        }
        this.loadTemplates();
    },

    async loadTemplates() {
        try {
            const resp = await fetch('/api/templates');
            const result = await resp.json();
            if (result.success) {
                this.state.templates = result.data || [];
                this.render();
            }
        } catch (e) {
            console.error('加载模板列表失败:', e);
        }
    },

    async loadVersions(templateId) {
        try {
            const resp = await fetch('/api/templates/' + templateId + '/versions');
            const result = await resp.json();
            if (result.success) {
                this.state.versions = result.data || [];
                this.render();
            }
        } catch (e) {
            console.error('加载版本列表失败:', e);
        }
    },

    render() {
        const selectorPanel = document.getElementById('selectorPanel');
        const designerPanel = document.getElementById('designerPanel');
        if (!selectorPanel) return;

        if (this.state.step === 3) {
            selectorPanel.style.display = 'none';
            designerPanel.style.display = '';
            return;
        }

        selectorPanel.style.display = '';
        designerPanel.style.display = 'none';

        if (this.state.step === 1) {
            this.renderTemplateList();
        } else if (this.state.step === 2) {
            this.renderVersionList();
        }
    },

    renderTemplateList() {
        document.getElementById('selectorBreadcrumb').textContent = '模板管理';
        document.getElementById('selectorPanelTitle').textContent = '模板列表';

        const listHtml = this.state.templates.map(t => {
            const isActive = this.state.templateId === t.id;
            const statusClass = t.status === 'ENABLED' ? 'sel-badge-green' : 'sel-badge-gray';
            const statusText = t.status === 'ENABLED' ? '启用' : '停用';
            return '<div class="sel-item' + (isActive ? ' sel-active' : '') + '" onclick="TemplateSelector.selectTemplate(' + t.id + ')">' +
                '<div class="sel-item-title">' + t.templateName + '</div>' +
                '<div class="sel-item-sub">' + t.templateCode + '</div>' +
                '<div class="sel-item-badge ' + statusClass + '">' + statusText + '</div>' +
                '</div>';
        }).join('');

        document.getElementById('selectorList').innerHTML = listHtml || '<div class="sel-empty">暂无模板</div>';

        const tpl = this.state.templateId ? this.state.templates.find(t => t.id === this.state.templateId) : null;
        if (tpl) {
            this.renderTemplateDetail(tpl);
        } else {
            document.getElementById('selectorContent').innerHTML =
                '<div class="sel-empty-state"><div class="sel-empty-icon">📋</div><p>从左侧选择一个模板开始配置</p></div>';
        }
    },

    renderTemplateDetail(tpl) {
        const statusClass = tpl.status === 'ENABLED' ? 'sel-badge-green' : 'sel-badge-gray';
        const statusText = tpl.status === 'ENABLED' ? '启用' : '停用';
        document.getElementById('selectorContent').innerHTML =
            '<h3 style="margin-bottom:16px">' + tpl.templateName + '</h3>' +
            '<div class="sel-detail-grid">' +
            '<div class="sel-detail-card"><div class="sel-detail-label">模板编码</div><div class="sel-detail-value">' + tpl.templateCode + '</div></div>' +
            '<div class="sel-detail-card"><div class="sel-detail-label">状态</div><div class="sel-detail-value"><span class="sel-item-badge ' + statusClass + '">' + statusText + '</span></div></div>' +
            '</div>' +
            '<div style="margin-top:20px;display:flex;gap:8px">' +
            '<button class="btn btn-primary" onclick="TemplateSelector.goToVersions(' + tpl.id + ')">选择版本 →</button>' +
            '</div>';
    },

    renderVersionList() {
        const tpl = this.state.templates.find(t => t.id === this.state.templateId);
        document.getElementById('selectorBreadcrumb').innerHTML =
            '<span style="cursor:pointer" onclick="TemplateSelector.goToStep(1)">模板管理</span> / ' + (tpl ? tpl.templateName : '');
        document.getElementById('selectorPanelTitle').textContent = '版本列表';

        const listHtml = this.state.versions.map(v => {
            const isActive = this.state.versionId === v.id;
            const statusClass = v.versionStatus === 'PUBLISHED' ? 'sel-badge-green' : 'sel-badge-orange';
            const statusText = v.versionStatus === 'PUBLISHED' ? '已发布' : '草稿';
            return '<div class="sel-item' + (isActive ? ' sel-active' : '') + '" onclick="TemplateSelector.selectVersion(' + v.id + ')">' +
                '<div class="sel-item-title">' + (v.versionName || 'V' + v.versionNo) + '</div>' +
                '<div class="sel-item-sub">版本号: ' + v.versionNo + '</div>' +
                '<div class="sel-item-badge ' + statusClass + '">' + statusText + '</div>' +
                '</div>';
        }).join('');

        document.getElementById('selectorList').innerHTML = listHtml || '<div class="sel-empty">暂无版本</div>';

        document.getElementById('selectorContentHeader').innerHTML =
            '<span style="font-size:14px;font-weight:600">' + (tpl ? tpl.templateName : '') + ' - 选择版本</span>' +
            '<button class="btn btn-sm" onclick="TemplateSelector.goToStep(1)">← 返回模板列表</button>';

        const ver = this.state.versionId ? this.state.versions.find(v => v.id === this.state.versionId) : null;
        if (ver) {
            this.renderVersionDetail(ver);
        } else {
            document.getElementById('selectorContent').innerHTML =
                '<div class="sel-empty-state"><div class="sel-empty-icon">📊</div><p>从左侧选择一个版本</p><p style="font-size:12px;color:#999">草稿版本可编辑，已发布版本只读查看</p></div>';
        }
    },

    renderVersionDetail(ver) {
        const isDraft = ver.versionStatus === 'DRAFT';
        const statusClass = isDraft ? 'sel-badge-orange' : 'sel-badge-green';
        const statusText = isDraft ? '草稿' : '已发布';
        document.getElementById('selectorContent').innerHTML =
            '<h3 style="margin-bottom:16px">' + (ver.versionName || 'V' + ver.versionNo) + '</h3>' +
            '<div class="sel-detail-grid">' +
            '<div class="sel-detail-card"><div class="sel-detail-label">版本状态</div><div class="sel-detail-value"><span class="sel-item-badge ' + statusClass + '">' + statusText + '</span></div></div>' +
            '<div class="sel-detail-card"><div class="sel-detail-label">版本号</div><div class="sel-detail-value">' + ver.versionNo + '</div></div>' +
            '</div>' +
            '<div style="margin-top:24px;display:flex;gap:8px">' +
            '<button class="btn btn-primary" onclick="TemplateSelector.enterDesigner()">' + (isDraft ? '编辑配置 →' : '查看配置 →') + '</button>' +
            (isDraft ? '<button class="btn btn-success" onclick="TemplateSelector.publishVersion(' + ver.id + ')">发布此版本</button>' : '') +
            '</div>';
    },

    selectTemplate(id) {
        this.state.templateId = id;
        this.state.versionId = null;
        this.renderTemplateList();
    },

    async goToVersions(templateId) {
        this.state.templateId = templateId;
        this.state.versionId = null;
        this.state.step = 2;
        await this.loadVersions(templateId);
    },

    selectVersion(id) {
        this.state.versionId = id;
        this.renderVersionList();
    },

    goToStep(step) {
        this.state.step = step;
        if (step <= 1) {
            this.state.templateId = null;
            this.state.versionId = null;
        }
        if (step === 1) {
            this.state.versionId = null;
        }
        this.render();
    },

    enterDesigner() {
        this.state.step = 3;
        const url = new URL(window.location);
        url.searchParams.set('templateId', this.state.templateId);
        url.searchParams.set('versionId', this.state.versionId);
        window.history.pushState({}, '', url);
        this.showDesigner();
    },

    showDesigner() {
        const selectorPanel = document.getElementById('selectorPanel');
        const designerPanel = document.getElementById('designerPanel');
        if (selectorPanel) selectorPanel.style.display = 'none';
        if (designerPanel) designerPanel.style.display = '';

        // Update DesignerState
        if (typeof DesignerState !== 'undefined') {
            DesignerState.templateId = this.state.templateId;
            DesignerState.versionId = this.state.versionId;
        }

        // Show info bar
        var infoBar = document.getElementById('designerInfoBar');
        if (infoBar) {
            infoBar.style.display = '';
            var infoText = document.getElementById('designerInfoText');
            if (infoText) {
                infoText.textContent = '当前模板ID: ' + this.state.templateId + ' | 版本ID: ' + this.state.versionId;
            }
        }

        // Initialize designer
        if (typeof initDesigner === 'function') {
            initDesigner();
        }
    },

    async publishVersion(versionId) {
        if (!confirm('确认发布此版本？')) return;
        try {
            const resp = await fetch('/api/templates/versions/' + versionId + '/publish', { method: 'POST' });
            const result = await resp.json();
            if (result.success) {
                alert('发布成功');
                await this.loadVersions(this.state.templateId);
            } else {
                alert('发布失败: ' + (result.message || '未知错误'));
            }
        } catch (e) {
            alert('发布失败: ' + e.message);
        }
    }
};
