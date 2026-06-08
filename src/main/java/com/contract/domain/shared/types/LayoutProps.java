package com.contract.domain.shared.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LayoutProps {
    private Integer span;
    private Integer offset;
    private Integer gutter;
    private Integer columns;
    private List<PanelDef> panels;
    private List<TabDef> tabs;
    private Integer gridColumn;
    private Integer gridRow;

    public LayoutProps() {}

    public Integer getSpan() { return span; }
    public void setSpan(Integer span) { this.span = span; }
    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }
    public Integer getGutter() { return gutter; }
    public void setGutter(Integer gutter) { this.gutter = gutter; }
    public Integer getColumns() { return columns; }
    public void setColumns(Integer columns) { this.columns = columns; }
    public List<PanelDef> getPanels() { return panels; }
    public void setPanels(List<PanelDef> panels) { this.panels = panels; }
    public List<TabDef> getTabs() { return tabs; }
    public void setTabs(List<TabDef> tabs) { this.tabs = tabs; }
    public Integer getGridColumn() { return gridColumn; }
    public void setGridColumn(Integer gridColumn) { this.gridColumn = gridColumn; }
    public Integer getGridRow() { return gridRow; }
    public void setGridRow(Integer gridRow) { this.gridRow = gridRow; }

    public static class PanelDef {
        private String title;
        private List<String> children;
        public PanelDef() {}
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<String> getChildren() { return children; }
        public void setChildren(List<String> children) { this.children = children; }
    }

    public static class TabDef {
        private String title;
        private List<String> children;
        public TabDef() {}
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<String> getChildren() { return children; }
        public void setChildren(List<String> children) { this.children = children; }
    }
}
