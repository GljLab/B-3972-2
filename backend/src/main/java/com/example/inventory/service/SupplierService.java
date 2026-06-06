package com.example.inventory.service;

import com.example.inventory.entity.*;
import com.example.inventory.mapper.*;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.paginate.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierMapper supplierMapper;
    private final SupplierScoreRecordMapper scoreRecordMapper;
    private final SupplierRecoveryRecordMapper recoveryRecordMapper;
    private final ExceptionOrderMapper exceptionOrderMapper;
    private final RawMaterialRecordMapper rawMaterialRecordMapper;
    private final WorkshopRecordMapper workshopRecordMapper;

    public SupplierService(SupplierMapper supplierMapper,
                           SupplierScoreRecordMapper scoreRecordMapper,
                           SupplierRecoveryRecordMapper recoveryRecordMapper,
                           ExceptionOrderMapper exceptionOrderMapper,
                           RawMaterialRecordMapper rawMaterialRecordMapper,
                           WorkshopRecordMapper workshopRecordMapper) {
        this.supplierMapper = supplierMapper;
        this.scoreRecordMapper = scoreRecordMapper;
        this.recoveryRecordMapper = recoveryRecordMapper;
        this.exceptionOrderMapper = exceptionOrderMapper;
        this.rawMaterialRecordMapper = rawMaterialRecordMapper;
        this.workshopRecordMapper = workshopRecordMapper;
    }

    private String generateSupplierCode() {
        long count = supplierMapper.selectCountByQuery(QueryWrapper.create());
        return String.format("SUP%06d", count + 1);
    }

    public Supplier createSupplier(Supplier supplier) {
        Supplier existing = supplierMapper.selectOneByQuery(QueryWrapper.create()
                .where(Supplier::getSupplierName).eq(supplier.getSupplierName()));
        if (existing != null) {
            throw new RuntimeException("供应商名称已存在");
        }

        supplier.setSupplierCode(generateSupplierCode());
        supplier.setCreditScore(100);
        supplier.setStatus("NORMAL");
        supplier.setCooperationStartDate(
                supplier.getCooperationStartDate() != null ? supplier.getCooperationStartDate() : LocalDate.now()
        );
        supplier.setCreateTime(LocalDateTime.now());
        supplier.setUpdateTime(LocalDateTime.now());

        supplierMapper.insert(supplier);
        return supplier;
    }

    public Supplier getById(Long id) {
        return supplierMapper.selectOneById(id);
    }

    public Supplier updateSupplier(Long id, Supplier supplier) {
        Supplier existing = supplierMapper.selectOneById(id);
        if (existing == null) {
            throw new RuntimeException("供应商不存在");
        }

        if (!existing.getSupplierName().equals(supplier.getSupplierName())) {
            Supplier nameCheck = supplierMapper.selectOneByQuery(QueryWrapper.create()
                    .where(Supplier::getSupplierName).eq(supplier.getSupplierName()));
            if (nameCheck != null) {
                throw new RuntimeException("供应商名称已存在");
            }
        }

        existing.setSupplierName(supplier.getSupplierName());
        existing.setContactName(supplier.getContactName());
        existing.setContactPhone(supplier.getContactPhone());
        existing.setSupplyType(supplier.getSupplyType());
        existing.setCooperationStartDate(supplier.getCooperationStartDate());
        existing.setRemark(supplier.getRemark());
        existing.setUpdateTime(LocalDateTime.now());

        supplierMapper.update(existing);
        return existing;
    }

    public void deleteSupplier(Long id) {
        Supplier supplier = supplierMapper.selectOneById(id);
        if (supplier == null) {
            throw new RuntimeException("供应商不存在");
        }
        supplierMapper.deleteById(id);
    }

    public List<Supplier> listSuppliers(String supplyType, String creditLevel, String status, String keyword) {
        QueryWrapper qw = QueryWrapper.create()
                .where(Supplier::getSupplyType).eq(supplyType, supplyType != null && !supplyType.isEmpty() && !"ALL".equals(supplyType))
                .and(Supplier::getStatus).eq(status, status != null && !status.isEmpty() && !"ALL".equals(status))
                .and(Supplier::getSupplierName).like(keyword, keyword != null && !keyword.isEmpty())
                .or(Supplier::getSupplierCode).like(keyword, keyword != null && !keyword.isEmpty())
                .orderBy(Supplier::getCreateTime, false);

        List<Supplier> suppliers = supplierMapper.selectListByQuery(qw);

        if (creditLevel != null && !creditLevel.isEmpty() && !"ALL".equals(creditLevel)) {
            suppliers = suppliers.stream()
                    .filter(s -> getCreditLevel(s.getCreditScore()).equals(creditLevel))
                    .collect(Collectors.toList());
        }

        return suppliers;
    }

    public List<Supplier> listBySupplyType(String supplyType) {
        return supplierMapper.selectListByQuery(QueryWrapper.create()
                .where(Supplier::getSupplyType).eq(supplyType)
                .orderBy(Supplier::getSupplierName, true));
    }

    private String getCreditLevel(Integer score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 70) return "GOOD";
        if (score >= 50) return "AVERAGE";
        if (score >= 30) return "WARNING";
        return "DANGER";
    }

    @Transactional
    public Map<String, Object> deductScore(Long supplierId, Long exceptionOrderId, Integer deductScore,
                                           String responsibilityDescription, String operatorName) {
        Supplier supplier = supplierMapper.selectOneById(supplierId);
        if (supplier == null) {
            throw new RuntimeException("供应商不存在");
        }

        ExceptionOrder order = null;
        String orderNo = null;
        if (exceptionOrderId != null) {
            order = exceptionOrderMapper.selectOneById(exceptionOrderId);
            if (order == null) {
                throw new RuntimeException("异常工单不存在");
            }
            orderNo = order.getOrderNo();
        }

        int newScore = Math.max(0, supplier.getCreditScore() - deductScore);
        supplier.setCreditScore(newScore);

        String newStatus = supplier.getStatus();
        if (newScore <= 30 && !"BLACKLIST".equals(supplier.getStatus())) {
            newStatus = "BLACKLIST";
            supplier.setStatus("BLACKLIST");
        }
        supplier.setUpdateTime(LocalDateTime.now());
        supplierMapper.update(supplier);

        SupplierScoreRecord record = new SupplierScoreRecord();
        record.setSupplierId(supplierId);
        record.setExceptionOrderId(exceptionOrderId);
        record.setExceptionOrderNo(orderNo);
        record.setDeductScore(deductScore);
        record.setScoreAfter(newScore);
        record.setResponsibilityDescription(responsibilityDescription);
        record.setOperatorName(operatorName);
        record.setCreateTime(LocalDateTime.now());
        scoreRecordMapper.insert(record);

        if (order != null) {
            order.setResponsibleSupplierId(supplierId);
            order.setResponsibilityDescription(responsibilityDescription);
            order.setDeductScore(deductScore);
            exceptionOrderMapper.update(order);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("supplierId", supplierId);
        result.put("newScore", newScore);
        result.put("newStatus", newStatus);
        result.put("deductScore", deductScore);
        result.put("becameBlacklist", newScore <= 30 && !"BLACKLIST".equals(newStatus));
        return result;
    }

    @Transactional
    public Supplier applyRecovery(Long supplierId, String recoveryReason, String certificationMaterial, String approverName) {
        Supplier supplier = supplierMapper.selectOneById(supplierId);
        if (supplier == null) {
            throw new RuntimeException("供应商不存在");
        }
        if (!"BLACKLIST".equals(supplier.getStatus())) {
            throw new RuntimeException("只有黑名单供应商才能申请信用恢复");
        }

        supplier.setStatus("OBSERVATION");
        supplier.setCreditScore(60);
        supplier.setUpdateTime(LocalDateTime.now());
        supplierMapper.update(supplier);

        SupplierRecoveryRecord record = new SupplierRecoveryRecord();
        record.setSupplierId(supplierId);
        record.setRecoveryReason(recoveryReason);
        record.setCertificationMaterial(certificationMaterial);
        record.setApproverName(approverName);
        record.setCreateTime(LocalDateTime.now());
        recoveryRecordMapper.insert(record);

        return supplier;
    }

    public List<SupplierScoreRecord> getScoreRecords(Long supplierId) {
        return scoreRecordMapper.selectListByQuery(QueryWrapper.create()
                .where(SupplierScoreRecord::getSupplierId).eq(supplierId)
                .orderBy(SupplierScoreRecord::getCreateTime, false));
    }

    public List<ExceptionOrder> getExceptionOrders(Long supplierId, String status) {
        QueryWrapper qw = QueryWrapper.create()
                .where(ExceptionOrder::getResponsibleSupplierId).eq(supplierId)
                .and(ExceptionOrder::getStatus).eq(status, status != null && !status.isEmpty() && !"ALL".equals(status))
                .orderBy(ExceptionOrder::getCreateTime, false);
        return exceptionOrderMapper.selectListByQuery(qw);
    }

    public List<Map<String, Object>> getCreditTrend(Long supplierId) {
        List<SupplierScoreRecord> records = scoreRecordMapper.selectListByQuery(QueryWrapper.create()
                .where(SupplierScoreRecord::getSupplierId).eq(supplierId)
                .orderBy(SupplierScoreRecord::getCreateTime, true));

        LocalDate now = LocalDate.now();
        List<Map<String, Object>> trend = new ArrayList<>();

        Supplier supplier = supplierMapper.selectOneById(supplierId);
        int currentScore = supplier != null ? supplier.getCreditScore() : 100;

        Map<String, Integer> monthScoreMap = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i).withDayOfMonth(1);
            String monthKey = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            monthScoreMap.put(monthKey, null);
        }

        int lastKnownScore = currentScore;
        List<String> months = new ArrayList<>(monthScoreMap.keySet());
        Collections.reverse(months);

        Map<String, List<SupplierScoreRecord>> monthRecordsMap = new HashMap<>();
        for (SupplierScoreRecord record : records) {
            String monthKey = record.getCreateTime().toLocalDate()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
            monthRecordsMap.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(record);
        }

        for (String month : months) {
            List<SupplierScoreRecord> monthRecords = monthRecordsMap.getOrDefault(month, new ArrayList<>());
            int score = lastKnownScore;
            List<Map<String, Object>> events = new ArrayList<>();

            for (SupplierScoreRecord record : monthRecords) {
                score = score + record.getDeductScore();
                Map<String, Object> event = new HashMap<>();
                event.put("time", record.getCreateTime());
                event.put("deductScore", record.getDeductScore());
                event.put("description", record.getResponsibilityDescription());
                event.put("orderNo", record.getExceptionOrderNo());
                events.add(event);
            }

            monthScoreMap.put(month, score);
            lastKnownScore = score;

            Map<String, Object> item = new HashMap<>();
            item.put("month", month);
            item.put("score", score);
            item.put("events", events);
            trend.add(item);
        }

        Collections.reverse(trend);
        return trend;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalCount = supplierMapper.selectCountByQuery(QueryWrapper.create());
        long blacklistCount = supplierMapper.selectCountByQuery(QueryWrapper.create()
                .where(Supplier::getStatus).eq("BLACKLIST"));

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        long exceptionCountThisMonth = exceptionOrderMapper.selectCountByQuery(QueryWrapper.create()
                .where(ExceptionOrder::getCreateTime).ge(startOfMonth.atStartOfDay())
                .and(ExceptionOrder::getResponsibleSupplierId).isNotNull());

        List<Supplier> allSuppliers = supplierMapper.selectAll();
        double avgScore = allSuppliers.stream()
                .mapToInt(Supplier::getCreditScore)
                .average()
                .orElse(0.0);

        stats.put("totalSuppliers", totalCount);
        stats.put("blacklistSuppliers", blacklistCount);
        stats.put("monthlyExceptionCount", exceptionCountThisMonth);
        stats.put("averageCreditScore", Math.round(avgScore * 100.0) / 100.0);

        return stats;
    }

    public Map<String, Object> getCreditDistribution() {
        List<Supplier> allSuppliers = supplierMapper.selectAll();
        Map<String, Object> distribution = new LinkedHashMap<>();

        int excellent = 0, good = 0, average = 0, warning = 0, danger = 0;
        for (Supplier s : allSuppliers) {
            String level = getCreditLevel(s.getCreditScore());
            switch (level) {
                case "EXCELLENT": excellent++; break;
                case "GOOD": good++; break;
                case "AVERAGE": average++; break;
                case "WARNING": warning++; break;
                case "DANGER": danger++; break;
            }
        }

        int total = allSuppliers.size();
        distribution.put("EXCELLENT", Map.of("count", excellent, "name", "优秀", "percent", total == 0 ? 0 : Math.round(excellent * 100.0 / total)));
        distribution.put("GOOD", Map.of("count", good, "name", "良好", "percent", total == 0 ? 0 : Math.round(good * 100.0 / total)));
        distribution.put("AVERAGE", Map.of("count", average, "name", "一般", "percent", total == 0 ? 0 : Math.round(average * 100.0 / total)));
        distribution.put("WARNING", Map.of("count", warning, "name", "警告", "percent", total == 0 ? 0 : Math.round(warning * 100.0 / total)));
        distribution.put("DANGER", Map.of("count", danger, "name", "危险", "percent", total == 0 ? 0 : Math.round(danger * 100.0 / total)));

        return distribution;
    }

    public List<Map<String, Object>> getRiskTop10() {
        List<Supplier> suppliers = supplierMapper.selectListByQuery(QueryWrapper.create()
                .orderBy(Supplier::getCreditScore, true)
                .limit(10));

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate start30Days = LocalDate.now().minusDays(30);

        for (int i = 0; i < suppliers.size(); i++) {
            Supplier s = suppliers.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("rank", i + 1);
            item.put("id", s.getId());
            item.put("supplierCode", s.getSupplierCode());
            item.put("supplierName", s.getSupplierName());
            item.put("creditScore", s.getCreditScore());
            item.put("status", s.getStatus());

            long recent30DaysCount = exceptionOrderMapper.selectCountByQuery(QueryWrapper.create()
                    .where(ExceptionOrder::getResponsibleSupplierId).eq(s.getId())
                    .and(ExceptionOrder::getCreateTime).ge(start30Days.atStartOfDay()));
            item.put("recent30DaysExceptions", recent30DaysCount);

            List<SupplierScoreRecord> records = scoreRecordMapper.selectListByQuery(QueryWrapper.create()
                    .where(SupplierScoreRecord::getSupplierId).eq(s.getId()));
            int totalDeduct = records.stream().mapToInt(SupplierScoreRecord::getDeductScore).sum();
            item.put("totalDeductScore", totalDeduct);

            result.add(item);
        }

        return result;
    }

    public Long recommendSupplierForException(Long exceptionOrderId) {
        ExceptionOrder order = exceptionOrderMapper.selectOneById(exceptionOrderId);
        if (order == null) {
            return null;
        }

        String desc = order.getExceptionDescription() != null ? order.getExceptionDescription() : "";
        String type = order.getExceptionType() != null ? order.getExceptionType() : "";
        boolean isRawMaterial = desc.contains("原材料") || desc.contains("损耗") || type.contains("原材料") || type.contains("损耗");
        boolean isWorkshop = desc.contains("计件") || desc.contains("虚报") || type.contains("计件") || type.contains("虚报");

        LocalDate recordDate = order.getRecordDate();
        String productName = order.getProductName();

        if (isRawMaterial) {
            List<RawMaterialRecord> records = rawMaterialRecordMapper.selectListByQuery(QueryWrapper.create()
                    .where(RawMaterialRecord::getRecordDate).eq(recordDate)
                    .and(RawMaterialRecord::getProductName).eq(productName)
                    .and(RawMaterialRecord::getSupplierId).isNotNull());
            if (!records.isEmpty()) {
                return records.get(0).getSupplierId();
            }
        }

        if (isWorkshop) {
            List<WorkshopRecord> records = workshopRecordMapper.selectListByQuery(QueryWrapper.create()
                    .where(WorkshopRecord::getRecordDate).eq(recordDate)
                    .and(WorkshopRecord::getProductName).eq(productName)
                    .and(WorkshopRecord::getSupplierId).isNotNull());
            if (!records.isEmpty()) {
                return records.get(0).getSupplierId();
            }
        }

        return null;
    }

    public long getExceptionCount(Long supplierId) {
        return exceptionOrderMapper.selectCountByQuery(QueryWrapper.create()
                .where(ExceptionOrder::getResponsibleSupplierId).eq(supplierId));
    }
}
