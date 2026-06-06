package com.example.inventory.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("supplier_recovery_record")
public class SupplierRecoveryRecord {
    @Id(keyType = KeyType.Auto)
    private Long id;
    private Long supplierId;
    private String recoveryReason;
    private String certificationMaterial;
    private String approverName;
    private LocalDateTime createTime;
}
