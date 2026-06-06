package com.example.inventory.controller;

import com.example.inventory.entity.RawMaterialRecord;
import com.example.inventory.entity.WarehouseRecord;
import com.example.inventory.entity.WorkshopRecord;
import com.example.inventory.mapper.RawMaterialRecordMapper;
import com.example.inventory.mapper.WarehouseRecordMapper;
import com.example.inventory.mapper.WorkshopRecordMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@CrossOrigin(origins = "*")
public class RecordController {

    private final RawMaterialRecordMapper rawMapper;
    private final WorkshopRecordMapper workshopMapper;
    private final WarehouseRecordMapper warehouseMapper;

    public RecordController(RawMaterialRecordMapper r, WorkshopRecordMapper ws, WarehouseRecordMapper wh) {
        this.rawMapper = r;
        this.workshopMapper = ws;
        this.warehouseMapper = wh;
    }

    @PostMapping("/raw-materials")
    public ResponseEntity<?> addRawMaterial(@RequestBody RawMaterialRecord record) {
        record.setCreateTime(LocalDateTime.now());
        rawMapper.insert(record);
        return ResponseEntity.ok(Map.of("code", 200, "msg", "Saved successfully"));
    }

    @PostMapping("/raw-materials/batch")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> addRawMaterialBatch(@RequestBody List<RawMaterialRecord> records) {
        if (records == null || records.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", "records must not be empty"));
        }

        LocalDateTime now = LocalDateTime.now();
        for (RawMaterialRecord record : records) {
            record.setCreateTime(now);
            rawMapper.insert(record);
        }
        return ResponseEntity.ok(Map.of("code", 200, "msg", "Batch saved successfully", "count", records.size()));
    }

    @PostMapping("/workshop")
    public ResponseEntity<?> addWorkshopRecord(@RequestBody WorkshopRecord record) {
        record.setCreateTime(LocalDateTime.now());
        workshopMapper.insert(record);
        return ResponseEntity.ok(Map.of("code", 200, "msg", "Saved successfully"));
    }

    @PostMapping("/workshop/batch")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> addWorkshopBatch(@RequestBody List<WorkshopRecord> records) {
        if (records == null || records.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", "records must not be empty"));
        }

        LocalDateTime now = LocalDateTime.now();
        for (WorkshopRecord record : records) {
            record.setCreateTime(now);
            workshopMapper.insert(record);
        }
        return ResponseEntity.ok(Map.of("code", 200, "msg", "Batch saved successfully", "count", records.size()));
    }

    @PostMapping("/warehouse")
    public ResponseEntity<?> addWarehouseRecord(@RequestBody WarehouseRecord record) {
        record.setCreateTime(LocalDateTime.now());
        record.setActualTotalQuantity(null); // Explicitly null to skip in insertSelective
        warehouseMapper.insertSelective(record);
        return ResponseEntity.ok(Map.of("code", 200, "msg", "Saved successfully"));
    }

    @PostMapping("/warehouse/batch")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> addWarehouseBatch(@RequestBody List<WarehouseRecord> records) {
        if (records == null || records.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", "records must not be empty"));
        }

        LocalDateTime now = LocalDateTime.now();
        for (WarehouseRecord record : records) {
            record.setCreateTime(now);
            record.setActualTotalQuantity(null); // Explicitly null to skip in insertSelective
            warehouseMapper.insertSelective(record);
        }
        return ResponseEntity.ok(Map.of("code", 200, "msg", "Batch saved successfully", "count", records.size()));
    }
}
