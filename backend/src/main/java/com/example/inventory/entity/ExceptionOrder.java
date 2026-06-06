package com.example.inventory.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table("exception_order")
public class ExceptionOrder {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private String orderNo;
    private String productName;
    private LocalDate recordDate;
    private BigDecimal theoreticalYield;
    private BigDecimal declaredQuantity;
    private BigDecimal actualTotalQuantity;
    private String exceptionDescription;
    private String exceptionType;
    private String status;
    private String creatorName;
    private LocalDateTime createTime;
    private String why1;
    private String why2;
    private String why3;
    private String why4;
    private String why5;
    private String improvementMeasure;
    private String responsiblePerson;
    private LocalDate planCompletionDate;
    private String verificationResult;
}
