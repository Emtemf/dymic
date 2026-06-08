package com.contract.adapter.rsp;

import lombok.Builder;
import lombok.Getter;
import java.time.OffsetDateTime;

@Getter
@Builder
public class TemplateGetRsp {
    private Long id;
    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String bizType;
    private String status;
    private Long currentVersionId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
