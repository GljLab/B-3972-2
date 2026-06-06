package com.example.inventory.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("supplier")
public class Supplier {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String supplierCode;
    private String supplierName;
    private String contactName;
    private String contactPhone;
    private String supplyType;
    private LocalDate cooperationStartDate;
    private Integer creditScore;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
