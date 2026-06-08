package com.contract.adapter.rsp;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TemplateListRsp {
    private List<TemplateGetRsp> templates;
    private Integer total;
}
