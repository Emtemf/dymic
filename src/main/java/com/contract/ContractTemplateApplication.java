package com.contract;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.contract.adapter.persistence.mapper")
public class ContractTemplateApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContractTemplateApplication.class, args);
    }
}