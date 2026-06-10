package com.contract.infrastructure.gateway;

import com.contract.application.template.dto.OptionDataDTO;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.gateway.DataProviderExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DictDataProviderExecutor implements DataProviderExecutor {
    @Override
    public boolean supports(String providerType) {
        return "DICT".equals(providerType);
    }

    @Override
    public List<OptionDataDTO> execute(DataProvider provider) {
        return List.of();
    }
}
