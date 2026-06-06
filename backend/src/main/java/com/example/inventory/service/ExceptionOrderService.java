package com.example.inventory.service;

import com.example.inventory.entity.ExceptionOrder;
import com.example.inventory.mapper.ExceptionOrderMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExceptionOrderService {

    private final ExceptionOrderMapper mapper;

    public ExceptionOrderService(ExceptionOrderMapper mapper) {
        this.mapper = mapper;
    }

    public ExceptionOrder createOrder(String productName, LocalDate recordDate,
                                       BigDecimal theoreticalYield, BigDecimal declaredQuantity,
                                       BigDecimal actualTotalQuantity, String exceptionDescription,
                                       String creatorName) {
        ExceptionOrder existing = mapper.selectOneByQuery(QueryWrapper.create()
                .where(ExceptionOrder::getRecordDate).eq(recordDate)
                .and(ExceptionOrder::getProductName).eq(productName));
        if (existing != null) {
            throw new RuntimeException("该记录已创建工单: " + existing.getOrderNo());
        }

        ExceptionOrder order = new ExceptionOrder();
        order.setProductName(productName);
        order.setRecordDate(recordDate);
        order.setTheoreticalYield(theoreticalYield);
        order.setDeclaredQuantity(declaredQuantity);
        order.setActualTotalQuantity(actualTotalQuantity);
        order.setExceptionDescription(exceptionDescription);
        order.setCreatorName(creatorName);
        order.setStatus("待处理");
        order.setCreateTime(LocalDateTime.now());

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = mapper.selectCountByQuery(QueryWrapper.create()
                .where(ExceptionOrder::getOrderNo).like("EXC-" + dateStr));
        String orderNo = String.format("EXC-%s-%03d", dateStr, count + 1);
        order.setOrderNo(orderNo);

        mapper.insert(order);
        return order;
    }

    public ExceptionOrder getById(Long id) {
        return mapper.selectOneById(id);
    }

    public ExceptionOrder getByRecordDateAndProductName(LocalDate recordDate, String productName) {
        return mapper.selectOneByQuery(QueryWrapper.create()
                .where(ExceptionOrder::getRecordDate).eq(recordDate)
                .and(ExceptionOrder::getProductName).eq(productName));
    }

    public List<ExceptionOrder> listOrders(String status, String exceptionType,
                                            String productName, String creatorName,
                                            LocalDate startDate, LocalDate endDate) {
        QueryWrapper qw = QueryWrapper.create()
                .where(ExceptionOrder::getStatus).eq(status, status != null && !status.isEmpty())
                .and(ExceptionOrder::getExceptionType).eq(exceptionType, exceptionType != null && !exceptionType.isEmpty())
                .and(ExceptionOrder::getProductName).like(productName, productName != null && !productName.isEmpty())
                .and(ExceptionOrder::getCreatorName).like(creatorName, creatorName != null && !creatorName.isEmpty())
                .and(ExceptionOrder::getCreateTime).ge(startDate != null ? startDate.atStartOfDay() : null, startDate != null)
                .and(ExceptionOrder::getCreateTime).le(endDate != null ? endDate.plusDays(1).atStartOfDay() : null, endDate != null)
                .orderBy(ExceptionOrder::getCreateTime, false);
        return mapper.selectListByQuery(qw);
    }

    public ExceptionOrder updateOrder(Long id, String exceptionType, String why1, String why2,
                                       String why3, String why4, String why5,
                                       String improvementMeasure, String responsiblePerson,
                                       LocalDate planCompletionDate, String verificationResult,
                                       String status) {
        ExceptionOrder order = mapper.selectOneById(id);
        if (order == null) {
            throw new RuntimeException("工单不存在");
        }

        if ("已关闭".equals(status) && (verificationResult == null || verificationResult.trim().isEmpty())) {
            throw new RuntimeException("关闭工单前必须填写验证结果");
        }

        order.setExceptionType(exceptionType);
        order.setWhy1(why1);
        order.setWhy2(why2);
        order.setWhy3(why3);
        order.setWhy4(why4);
        order.setWhy5(why5);
        order.setImprovementMeasure(improvementMeasure);
        order.setResponsiblePerson(responsiblePerson);
        order.setPlanCompletionDate(planCompletionDate);
        order.setVerificationResult(verificationResult);
        order.setStatus(status);

        mapper.update(order);
        return order;
    }

    public long countByStatus(String status) {
        return mapper.selectCountByQuery(QueryWrapper.create()
                .where(ExceptionOrder::getStatus).eq(status));
    }

    public long countThisMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return mapper.selectCountByQuery(QueryWrapper.create()
                .where(ExceptionOrder::getCreateTime).ge(startOfMonth.atStartOfDay()));
    }

    public List<ExceptionOrder> listRecent30Days() {
        LocalDate start = LocalDate.now().minusDays(29);
        return mapper.selectListByQuery(QueryWrapper.create()
                .where(ExceptionOrder::getCreateTime).ge(start.atStartOfDay())
                .orderBy(ExceptionOrder::getCreateTime, true));
    }
}
