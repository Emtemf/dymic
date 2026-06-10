package com.contract.application.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionDataDTO {
    private String value;
    private String label;
    private List<OptionDataDTO> children;
}
