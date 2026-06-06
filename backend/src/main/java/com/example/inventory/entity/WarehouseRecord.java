package com.example.inventory.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("warehouse_record")
public class WarehouseRecord {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String batchNo;
    private String productName;
    private BigDecimal inventoryQuantity;
    private BigDecimal shippedQuantity;
    private BigDecimal actualTotalQuantity;
    private String clientInfo;
    private LocalDate recordDate;
    private LocalDateTime createTime;
}
