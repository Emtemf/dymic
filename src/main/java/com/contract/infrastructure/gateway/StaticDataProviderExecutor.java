package com.contract.infrastructure.gateway;

import com.contract.application.template.dto.OptionDataDTO;
import com.contract.domain.shared.types.ConfigJson;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.gateway.DataProviderExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaticDataProviderExecutor implements DataProviderExecutor {
    @Override
    public boolean supports(String providerType) {
        return "STATIC".equals(providerType);
    }

    @Override
    public List<OptionDataDTO> execute(DataProvider provider) {
        return new ConfigJson(provider.getConfigJson()).parseOptions();
    }
}
