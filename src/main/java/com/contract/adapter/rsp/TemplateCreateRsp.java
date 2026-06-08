package com.contract.adapter.rsp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemplateCreateRsp {
    private Long id;
    private String templateCode;
    private String templateName;
    private String status;
}
