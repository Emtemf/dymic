package com.contract.domain.template.gateway;

import com.contract.application.template.dto.OptionDataDTO;
import com.contract.domain.template.DataProvider;

import java.util.List;

public interface DataProviderExecutor {
    boolean supports(String providerType);

    List<OptionDataDTO> execute(DataProvider provider);
}
