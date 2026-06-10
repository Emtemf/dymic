package com.contract.infrastructure.gateway;

import com.contract.domain.template.gateway.DataProviderExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataProviderExecutorFactory {
    private final List<DataProviderExecutor> executors;

    public DataProviderExecutor getExecutor(String providerType) {
        return executors.stream()
            .filter(executor -> executor.supports(providerType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("不支持的数据源类型: " + providerType));
    }
}
