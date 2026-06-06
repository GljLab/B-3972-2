package com.example.inventory.controller;

import com.example.inventory.entity.Supplier;
import com.example.inventory.entity.SupplierScoreRecord;
import com.example.inventory.entity.ExceptionOrder;
import com.example.inventory.service.SupplierService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createSupplier(@RequestBody Map<String, Object> body) {
        try {
            Supplier supplier = new Supplier();
            supplier.setSupplierName((String) body.get("supplierName"));
            supplier.setContactName((String) body.get("contactName"));
            supplier.setContactPhone((String) body.get("contactPhone"));
            supplier.setSupplyType((String) body.get("supplyType"));
            supplier.setRemark((String) body.get("remark"));

            if (body.get("cooperationStartDate") != null && !body.get("cooperationStartDate").toString().isEmpty()) {
                supplier.setCooperationStartDate(LocalDate.parse(body.get("cooperationStartDate").toString()));
            }

            Supplier result = service.createSupplier(supplier);
            return ResponseEntity.ok(Map.of("code", 200, "data", result, "msg", "供应商创建成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSupplier(@PathVariable Long id) {
        Supplier supplier = service.getById(id);
        if (supplier == null) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "msg", "供应商不存在"));
        }
        long exceptionCount = service.getExceptionCount(id);
        Map<String, Object> data = new HashMap<>();
        data.put("supplier", supplier);
        data.put("exceptionCount", exceptionCount);
        return ResponseEntity.ok(Map.of("code", 200, "data", data, "msg", "success"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSupplier(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Supplier supplier = new Supplier();
            supplier.setSupplierName((String) body.get("supplierName"));
            supplier.setContactName((String) body.get("contactName"));
            supplier.setContactPhone((String) body.get("contactPhone"));
            supplier.setSupplyType((String) body.get("supplyType"));
            supplier.setRemark((String) body.get("remark"));

            if (body.get("cooperationStartDate") != null && !body.get("cooperationStartDate").toString().isEmpty()) {
                supplier.setCooperationStartDate(LocalDate.parse(body.get("cooperationStartDate").toString()));
            }

            Supplier result = service.updateSupplier(id, supplier);
            return ResponseEntity.ok(Map.of("code", 200, "data", result, "msg", "供应商更新成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSupplier(@PathVariable Long id) {
        try {
            service.deleteSupplier(id);
            return ResponseEntity.ok(Map.of("code", 200, "msg", "供应商删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listSuppliers(
            @RequestParam(required = false) String supplyType,
            @RequestParam(required = false) String creditLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<Supplier> all = service.listSuppliers(supplyType, creditLevel, status, keyword);
        
        List<Map<String, Object>> resultList = new java.util.ArrayList<>();
        for (Supplier s : all) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", s.getId());
            item.put("supplierCode", s.getSupplierCode());
            item.put("supplierName", s.getSupplierName());
            item.put("contactName", s.getContactName());
            item.put("contactPhone", s.getContactPhone());
            item.put("supplyType", s.getSupplyType());
            item.put("cooperationStartDate", s.getCooperationStartDate());
            item.put("creditScore", s.getCreditScore());
            item.put("status", s.getStatus());
            item.put("remark", s.getRemark());
            item.put("createTime", s.getCreateTime());
            item.put("exceptionCount", service.getExceptionCount(s.getId()));
            resultList.add(item);
        }

        int total = resultList.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Map<String, Object>> pageData = fromIndex < total ? resultList.subList(fromIndex, toIndex) : List.of();
        
        return ResponseEntity.ok(Map.of("code", 200, "data", 
            Map.of("list", pageData, "total", total, "page", page, "pageSize", pageSize), 
            "msg", "success"));
    }

    @GetMapping("/by-type/{supplyType}")
    public ResponseEntity<?> listBySupplyType(@PathVariable String supplyType) {
        List<Supplier> suppliers = service.listBySupplyType(supplyType);
        return ResponseEntity.ok(Map.of("code", 200, "data", suppliers, "msg", "success"));
    }

    @PostMapping("/{id}/deduct-score")
    public ResponseEntity<?> deductScore(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long exceptionOrderId = body.get("exceptionOrderId") != null ? 
                    Long.parseLong(body.get("exceptionOrderId").toString()) : null;
            Integer deductScore = Integer.parseInt(body.get("deductScore").toString());
            String responsibilityDescription = (String) body.get("responsibilityDescription");
            String operatorName = (String) body.get("operatorName");

            Map<String, Object> result = service.deductScore(id, exceptionOrderId, deductScore,
                    responsibilityDescription, operatorName);
            return ResponseEntity.ok(Map.of("code", 200, "data", result, "msg", "扣分成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", e.getMessage()));
        }
    }

    @PostMapping("/{id}/apply-recovery")
    public ResponseEntity<?> applyRecovery(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String recoveryReason = (String) body.get("recoveryReason");
            String certificationMaterial = (String) body.get("certificationMaterial");
            String approverName = (String) body.get("approverName");

            Supplier result = service.applyRecovery(id, recoveryReason, certificationMaterial, approverName);
            return ResponseEntity.ok(Map.of("code", 200, "data", result, "msg", "信用恢复申请成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", e.getMessage()));
        }
    }

    @GetMapping("/{id}/score-records")
    public ResponseEntity<?> getScoreRecords(@PathVariable Long id) {
        List<SupplierScoreRecord> records = service.getScoreRecords(id);
        return ResponseEntity.ok(Map.of("code", 200, "data", records, "msg", "success"));
    }

    @GetMapping("/{id}/exception-orders")
    public ResponseEntity<?> getExceptionOrders(
            @PathVariable Long id,
            @RequestParam(required = false) String status) {
        List<ExceptionOrder> orders = service.getExceptionOrders(id, status);
        return ResponseEntity.ok(Map.of("code", 200, "data", orders, "msg", "success"));
    }

    @GetMapping("/{id}/credit-trend")
    public ResponseEntity<?> getCreditTrend(@PathVariable Long id) {
        List<Map<String, Object>> trend = service.getCreditTrend(id);
        return ResponseEntity.ok(Map.of("code", 200, "data", trend, "msg", "success"));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> stats = service.getDashboardStats();
        return ResponseEntity.ok(Map.of("code", 200, "data", stats, "msg", "success"));
    }

    @GetMapping("/dashboard/credit-distribution")
    public ResponseEntity<?> getCreditDistribution() {
        Map<String, Object> distribution = service.getCreditDistribution();
        return ResponseEntity.ok(Map.of("code", 200, "data", distribution, "msg", "success"));
    }

    @GetMapping("/dashboard/risk-top10")
    public ResponseEntity<?> getRiskTop10() {
        List<Map<String, Object>> top10 = service.getRiskTop10();
        return ResponseEntity.ok(Map.of("code", 200, "data", top10, "msg", "success"));
    }

    @GetMapping("/recommend-for-exception/{exceptionOrderId}")
    public ResponseEntity<?> recommendSupplierForException(@PathVariable Long exceptionOrderId) {
        Long supplierId = service.recommendSupplierForException(exceptionOrderId);
        Map<String, Object> data = new HashMap<>();
        data.put("recommendedSupplierId", supplierId);
        if (supplierId != null) {
            Supplier supplier = service.getById(supplierId);
            data.put("supplier", supplier);
        }
        return ResponseEntity.ok(Map.of("code", 200, "data", data, "msg", "success"));
    }
}
