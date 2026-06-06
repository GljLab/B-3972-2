package com.example.inventory.service;

import com.example.inventory.dto.CrossCheckResultDTO;
import com.example.inventory.entity.RawMaterialRecord;
import com.example.inventory.entity.WarehouseRecord;
import com.example.inventory.entity.WorkshopRecord;
import com.example.inventory.mapper.RawMaterialRecordMapper;
import com.example.inventory.mapper.WarehouseRecordMapper;
import com.example.inventory.mapper.WorkshopRecordMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrossCheckService {

    private final RawMaterialRecordMapper rawMapper;
    private final WorkshopRecordMapper workshopMapper;
    private final WarehouseRecordMapper warehouseMapper;

    public CrossCheckService(RawMaterialRecordMapper rawMapper, WorkshopRecordMapper workshopMapper, WarehouseRecordMapper warehouseMapper) {
        this.rawMapper = rawMapper;
        this.workshopMapper = workshopMapper;
        this.warehouseMapper = warehouseMapper;
    }

    public List<CrossCheckResultDTO> generateReport(LocalDate startDate, LocalDate endDate, String productName, BigDecimal tolerance) {
        
        List<RawMaterialRecord> raws = rawMapper.selectListByQuery(QueryWrapper.create()
                .select()
                .where(RawMaterialRecord::getRecordDate).between(startDate, endDate)
                .and(RawMaterialRecord::getProductName).eq(productName, productName != null && !productName.isEmpty()));

        List<WorkshopRecord> workshops = workshopMapper.selectListByQuery(QueryWrapper.create()
                .select()
                .where(WorkshopRecord::getRecordDate).between(startDate, endDate)
                .and(WorkshopRecord::getProductName).eq(productName, productName != null && !productName.isEmpty()));

        List<WarehouseRecord> warehouses = warehouseMapper.selectListByQuery(QueryWrapper.create()
                .select()
                .where(WarehouseRecord::getRecordDate).between(startDate, endDate)
                .and(WarehouseRecord::getProductName).eq(productName, productName != null && !productName.isEmpty()));

        // Grouping by Product_Name + Date
        Map<String, BigDecimal> theoreticalMap = raws.stream().collect(Collectors.groupingBy(
                r -> r.getProductName() + "_" + r.getRecordDate().toString(),
                Collectors.reducing(BigDecimal.ZERO, RawMaterialRecord::getTheoreticalYield, BigDecimal::add)
        ));

        Map<String, BigDecimal> declaredMap = workshops.stream().collect(Collectors.groupingBy(
                r -> r.getProductName() + "_" + r.getRecordDate().toString(),
                Collectors.reducing(BigDecimal.ZERO, WorkshopRecord::getDeclaredQuantity, BigDecimal::add)
        ));

        Map<String, BigDecimal> actualMap = warehouses.stream().collect(Collectors.groupingBy(
                r -> r.getProductName() + "_" + r.getRecordDate().toString(),
                Collectors.reducing(BigDecimal.ZERO, WarehouseRecord::getActualTotalQuantity, BigDecimal::add)
        ));

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(theoreticalMap.keySet());
        allKeys.addAll(declaredMap.keySet());
        allKeys.addAll(actualMap.keySet());

        List<CrossCheckResultDTO> results = new ArrayList<>();

        for (String key : allKeys) {
            String[] split = key.split("_");
            String pName = split[0];
            LocalDate date = LocalDate.parse(split[1]);

            BigDecimal theoretical = theoreticalMap.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal declared = declaredMap.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal actual = actualMap.getOrDefault(key, BigDecimal.ZERO);

            CrossCheckResultDTO dto = new CrossCheckResultDTO();
            dto.setProductName(pName);
            dto.setRecordDate(date);
            dto.setTheoreticalYield(theoretical);
            dto.setDeclaredQuantity(declared);
            dto.setActualTotalQuantity(actual);

            // Analysis 1: Theoretical vs Actual
            if (theoretical.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal diff = theoretical.subtract(actual).divide(theoretical, 4, RoundingMode.HALF_UP).abs();
                dto.setYieldDiffRate(diff);
                if (diff.compareTo(tolerance) > 0) {
                    dto.addWarning("理论产能与实际库存存在偏差(" + diff.multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP) + "%)，可能存在原材料损耗超标或未记录报废品");
                }
            } else if (actual.compareTo(BigDecimal.ZERO) > 0) {
                dto.addWarning("存在实际库存，但无原材料记录报备");
            }

            // Analysis 2: Salary verification (Actual / Declared)
            if (declared.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal matchRate = actual.divide(declared, 4, RoundingMode.HALF_UP);
                dto.setMatchRate(matchRate);
                if (matchRate.compareTo(new BigDecimal("0.90")) < 0) {
                    dto.addWarning("工人计件数据可能存在虚报(匹配度极低:" + matchRate.multiply(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP) + "%)，建议复核工资核发状态并核查车间生产记录");
                }
            } else if (actual.compareTo(BigDecimal.ZERO) > 0) {
                dto.addWarning("存在实际库存，但车间未申报任何产量");
            }

            results.add(dto);
        }

        results.sort(Comparator.comparing(CrossCheckResultDTO::getRecordDate).reversed());
        return results;
    }
}
