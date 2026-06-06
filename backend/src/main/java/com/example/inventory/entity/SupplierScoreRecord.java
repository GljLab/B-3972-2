package com.example.inventory.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("supplier_score_record")
public class SupplierScoreRecord {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long supplierId;
    private Long exceptionOrderId;
    private String exceptionOrderNo;
    private Integer deductScore;
    private Integer scoreAfter;
    private String responsibilityDescription;
    private String operatorName;
    private LocalDateTime createTime;
}
