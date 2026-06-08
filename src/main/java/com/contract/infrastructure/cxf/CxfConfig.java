package com.contract.infrastructure.cxf;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CXF JAX-RS configuration.
 *
 * <p>The CXF servlet path is set via {@code cxf.path} in application.yml.
 * The auto-configuration from {@code cxf-spring-boot-starter-jaxrs} handles
 * servlet registration, SpringBus creation, and component scanning for
 * {@code @Component} JAX-RS resources when {@code cxf.jaxrs.component-scan}
 * is enabled.</p>
 */
@Configuration
public class CxfConfig {

    @Bean
    public JacksonJsonProvider jacksonJsonProvider() {
        return new JacksonJsonProvider();
    }
}
