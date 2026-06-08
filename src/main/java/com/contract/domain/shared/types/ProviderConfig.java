package com.contract.domain.shared.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderConfig {
    private String providerType;
    private Map<String, Object> staticOptions;
    private String dictCode;
    private String httpUrl;
    private String httpMethod;
    private Map<String, String> httpHeaders;
    private List<StaticOption> options;

    public ProviderConfig() {}

    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }
    public Map<String, Object> getStaticOptions() { return staticOptions; }
    public void setStaticOptions(Map<String, Object> staticOptions) { this.staticOptions = staticOptions; }
    public String getDictCode() { return dictCode; }
    public void setDictCode(String dictCode) { this.dictCode = dictCode; }
    public String getHttpUrl() { return httpUrl; }
    public void setHttpUrl(String httpUrl) { this.httpUrl = httpUrl; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public Map<String, String> getHttpHeaders() { return httpHeaders; }
    public void setHttpHeaders(Map<String, String> httpHeaders) { this.httpHeaders = httpHeaders; }
    public List<StaticOption> getOptions() { return options; }
    public void setOptions(List<StaticOption> options) { this.options = options; }

    public static class StaticOption {
        private String label;
        private String value;
        private List<StaticOption> children;
        public StaticOption() {}
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public List<StaticOption> getChildren() { return children; }
        public void setChildren(List<StaticOption> children) { this.children = children; }
    }
}
