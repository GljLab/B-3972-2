package com.example.inventory.controller;

import com.example.inventory.entity.ExceptionOrder;
import com.example.inventory.service.ExceptionOrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exception-orders")
@CrossOrigin(origins = "*")
public class ExceptionOrderController {

    private final ExceptionOrderService service;

    public ExceptionOrderController(ExceptionOrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        String productName = (String) body.get("productName");
        LocalDate recordDate = LocalDate.parse((String) body.get("recordDate"));
        BigDecimal theoreticalYield = new BigDecimal(body.get("theoreticalYield").toString());
        BigDecimal declaredQuantity = new BigDecimal(body.get("declaredQuantity").toString());
        BigDecimal actualTotalQuantity = new BigDecimal(body.get("actualTotalQuantity").toString());
        String exceptionDescription = (String) body.get("exceptionDescription");
        String creatorName = (String) body.get("creatorName");

        ExceptionOrder order = service.createOrder(productName, recordDate, theoreticalYield,
                declaredQuantity, actualTotalQuantity, exceptionDescription, creatorName);
        return ResponseEntity.ok(Map.of("code", 200, "data", order, "msg", "工单创建成功"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        ExceptionOrder order = service.getById(id);
        if (order == null) {
            return ResponseEntity.status(404).body(Map.of("code", 404, "msg", "工单不存在"));
        }
        return ResponseEntity.ok(Map.of("code", 200, "data", order, "msg", "success"));
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkExisting(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordDate,
                                            @RequestParam String productName) {
        ExceptionOrder order = service.getByRecordDateAndProductName(recordDate, productName);
        if (order != null) {
            return ResponseEntity.ok(Map.of("code", 200, "data", order, "msg", "success"));
        }
        return ResponseEntity.ok(Map.of("code", 200, "msg", "no existing order"));
    }

    @GetMapping
    public ResponseEntity<?> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String exceptionType,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String creatorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<ExceptionOrder> all = service.listOrders(status, exceptionType, productName, creatorName, startDate, endDate);
        int total = all.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<ExceptionOrder> pageData = fromIndex < total ? all.subList(fromIndex, toIndex) : List.of();
        return ResponseEntity.ok(Map.of("code", 200, "data", Map.of("list", pageData, "total", total, "page", page, "pageSize", pageSize), "msg", "success"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String exceptionType = (String) body.get("exceptionType");
        String why1 = (String) body.get("why1");
        String why2 = (String) body.get("why2");
        String why3 = (String) body.get("why3");
        String why4 = (String) body.get("why4");
        String why5 = (String) body.get("why5");
        String improvementMeasure = (String) body.get("improvementMeasure");
        String responsiblePerson = (String) body.get("responsiblePerson");
        String verificationResult = (String) body.get("verificationResult");
        String status = (String) body.get("status");

        LocalDate planCompletionDate = null;
        if (body.get("planCompletionDate") != null && !body.get("planCompletionDate").toString().isEmpty()) {
            planCompletionDate = LocalDate.parse(body.get("planCompletionDate").toString());
        }

        try {
            ExceptionOrder order = service.updateOrder(id, exceptionType, why1, why2, why3, why4, why5,
                    improvementMeasure, responsiblePerson, planCompletionDate, verificationResult, status);
            return ResponseEntity.ok(Map.of("code", 200, "data", order, "msg", "保存成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long thisMonth = service.countThisMonth();
        long pending = service.countByStatus("待处理");
        long processing = service.countByStatus("处理中");
        long closed = service.countByStatus("已关闭");
        return ResponseEntity.ok(Map.of("code", 200, "data", Map.of(
                "thisMonth", thisMonth,
                "pending", pending,
                "processing", processing,
                "closed", closed
        ), "msg", "success"));
    }

    @GetMapping("/trend")
    public ResponseEntity<?> getTrend() {
        List<ExceptionOrder> orders = service.listRecent30Days();
        Map<String, Long> trend = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime().toLocalDate().toString(),
                        LinkedHashMap::new,
                        Collectors.counting()));
        return ResponseEntity.ok(Map.of("code", 200, "data", trend, "msg", "success"));
    }

    @GetMapping("/distribution")
    public ResponseEntity<?> getDistribution() {
        List<ExceptionOrder> all = service.listOrders(null, null, null, null, null, null);
        Map<String, Long> dist = all.stream()
                .filter(o -> o.getExceptionType() != null && !o.getExceptionType().isEmpty())
                .collect(Collectors.groupingBy(ExceptionOrder::getExceptionType, Collectors.counting()));
        return ResponseEntity.ok(Map.of("code", 200, "data", dist, "msg", "success"));
    }
}
