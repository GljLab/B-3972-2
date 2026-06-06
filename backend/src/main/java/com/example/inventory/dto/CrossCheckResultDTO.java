package com.example.inventory.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CrossCheckResultDTO {
    private String productName;
    private LocalDate recordDate;
    
    private BigDecimal theoreticalYield;
    private BigDecimal declaredQuantity;
    private BigDecimal actualTotalQuantity;
    
    private BigDecimal yieldDiffRate;
    private BigDecimal matchRate;
    
    private List<String> warnings = new ArrayList<>();

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
}
