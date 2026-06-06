package com.example.inventory.controller;

import com.example.inventory.dto.CrossCheckResultDTO;
import com.example.inventory.service.CrossCheckService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*") // For development convenience
public class CrossCheckController {

    private final CrossCheckService crossCheckService;

    public CrossCheckController(CrossCheckService crossCheckService) {
        this.crossCheckService = crossCheckService;
    }

    @GetMapping("/cross-check")
    public ResponseEntity<?> getCrossCheckReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false, defaultValue = "0.05") BigDecimal tolerance
    ) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        List<CrossCheckResultDTO> report = crossCheckService.generateReport(startDate, endDate, productName, tolerance);
        return ResponseEntity.ok(Map.of("code", 200, "data", report, "msg", "success"));
    }
}
