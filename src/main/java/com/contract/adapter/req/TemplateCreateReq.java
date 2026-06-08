package com.contract.adapter.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateCreateReq {
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;
    @NotBlank(message = "模板名称不能为空")
    private String templateName;
    private String templateDesc;
    private String bizType;
}
