package com.example.inventory.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("raw_material_record")
public class RawMaterialRecord {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String materialId;
    private String productName;
    private BigDecimal theoreticalYield;
    private LocalDate recordDate;
    private LocalDateTime createTime;
}
