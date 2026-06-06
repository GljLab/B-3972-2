package com.example.inventory.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("workshop_record")
public class WorkshopRecord {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String workshopId;
    private String workerId;
    private String productName;
    private BigDecimal declaredQuantity;
    private LocalDate recordDate;
    private Long supplierId;
    private LocalDateTime createTime;
}
