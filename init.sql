SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 原材料录入表
CREATE TABLE IF NOT EXISTS `raw_material_record` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `material_id` VARCHAR(50) COMMENT '原材料流水ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
  `theoretical_yield` DECIMAL(10,2) NOT NULL COMMENT '理论产出数量(基于BOM)',
  `record_date` DATE NOT NULL COMMENT '记录日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原材料维度记录';

-- 车间维度记录表
CREATE TABLE IF NOT EXISTS `workshop_record` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `workshop_id` VARCHAR(50) NOT NULL COMMENT '车间ID',
  `worker_id` VARCHAR(50) NOT NULL COMMENT '工人ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
  `declared_quantity` DECIMAL(10,2) NOT NULL COMMENT '申报生产总数',
  `record_date` DATE NOT NULL COMMENT '生产日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车间申报维度记录';

-- 库房库存记录表
CREATE TABLE IF NOT EXISTS `warehouse_record` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `batch_no` VARCHAR(100) NOT NULL COMMENT '批次号',
  `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
  `inventory_quantity` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '盘点数量',
  `shipped_quantity` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '已发货数量',
  `actual_total_quantity` DECIMAL(10,2) GENERATED ALWAYS AS (inventory_quantity + shipped_quantity) STORED COMMENT '实际库存总量',
  `client_info` VARCHAR(255) COMMENT '甲方信息',
  `record_date` DATE NOT NULL COMMENT '盘点/发货日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库房发货维度记录';

-- 插入演示数据 (Seed)
INSERT INTO `raw_material_record` (`product_name`, `theoretical_yield`, `record_date`) VALUES
('产品A', 1000.00, CURDATE()),
('产品B', 500.00, CURDATE()),
('产品A', 1100.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
('产品B', 600.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY));

INSERT INTO `workshop_record` (`workshop_id`, `worker_id`, `product_name`, `declared_quantity`, `record_date`) VALUES
('W_01', 'Worker_001', '产品A', 980.00, CURDATE()),
('W_02', 'Worker_002', '产品B', 480.00, CURDATE()),
('W_01', 'Worker_001', '产品A', 1150.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
('W_02', 'Worker_002', '产品B', 590.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY));

INSERT INTO `warehouse_record` (`batch_no`, `product_name`, `inventory_quantity`, `shipped_quantity`, `client_info`, `record_date`) VALUES
('BATCH_260227_01', '产品A', 800.00, 50.00, '京东物流', CURDATE()),
('BATCH_260227_02', '产品B', 450.00, 20.00, '顺丰速运', CURDATE()),
('BATCH_260226_01', '产品A', 1000.00, 100.00, '京东物流', DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
('BATCH_260226_02', '产品B', 550.00, 30.00, '顺丰速运', DATE_SUB(CURDATE(), INTERVAL 1 DAY));

-- 异常工单表
CREATE TABLE IF NOT EXISTS `exception_order` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '工单编号 EXC-YYYYMMDD-序号',
  `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
  `record_date` DATE NOT NULL COMMENT '业务日期',
  `theoretical_yield` DECIMAL(10,2) COMMENT '原材料理论产量',
  `declared_quantity` DECIMAL(10,2) COMMENT '车间申报产量',
  `actual_total_quantity` DECIMAL(10,2) COMMENT '库房实际产量',
  `exception_description` TEXT COMMENT '异常描述(完整预警文字)',
  `exception_type` VARCHAR(50) COMMENT '异常分类',
  `status` VARCHAR(20) NOT NULL DEFAULT '待处理' COMMENT '工单状态: 待处理/处理中/已关闭',
  `creator_name` VARCHAR(100) NOT NULL COMMENT '创建人姓名',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `why1` TEXT COMMENT '为什么1: 表面原因',
  `why2` TEXT COMMENT '为什么2: 深入一层',
  `why3` TEXT COMMENT '为什么3: 继续追问',
  `why4` TEXT COMMENT '为什么4: 接近本质',
  `why5` TEXT COMMENT '为什么5: 根本原因',
  `improvement_measure` TEXT COMMENT '改善措施描述',
  `responsible_person` VARCHAR(100) COMMENT '处理负责人姓名',
  `plan_completion_date` DATE COMMENT '计划完成日期',
  `verification_result` TEXT COMMENT '验证结果',
  UNIQUE KEY `uk_date_product` (`record_date`, `product_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常工单';

-- 供应商表
CREATE TABLE IF NOT EXISTS `supplier` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `supplier_code` VARCHAR(20) NOT NULL UNIQUE COMMENT '供应商编码 SUP+6位流水号',
  `supplier_name` VARCHAR(100) NOT NULL UNIQUE COMMENT '供应商名称',
  `contact_name` VARCHAR(50) COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `supply_type` VARCHAR(30) NOT NULL COMMENT '供应类型: RAW_MATERIAL-原材料供应商, OUTSOURCED-外包车间承包商',
  `cooperation_start_date` DATE NOT NULL COMMENT '合作起始日期',
  `credit_score` INT NOT NULL DEFAULT 100 COMMENT '当前信用分',
  `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态: NORMAL-正常合作, OBSERVATION-观察期, BLACKLIST-黑名单',
  `remark` TEXT COMMENT '备注信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商档案';

-- 供应商扣分历史表
CREATE TABLE IF NOT EXISTS `supplier_score_record` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
  `exception_order_id` BIGINT COMMENT '关联异常工单ID',
  `exception_order_no` VARCHAR(50) COMMENT '异常工单编号',
  `deduct_score` INT NOT NULL COMMENT '扣分值',
  `score_after` INT NOT NULL COMMENT '扣分后信用分',
  `responsibility_description` TEXT COMMENT '责任说明',
  `operator_name` VARCHAR(50) NOT NULL COMMENT '操作人姓名',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_supplier_id (`supplier_id`),
  INDEX idx_exception_order_id (`exception_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商扣分历史';

-- 供应商信用恢复记录表
CREATE TABLE IF NOT EXISTS `supplier_recovery_record` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `supplier_id` BIGINT NOT NULL COMMENT '供应商ID',
  `recovery_reason` TEXT NOT NULL COMMENT '恢复理由',
  `certification_material` TEXT COMMENT '整改证明材料',
  `approver_name` VARCHAR(50) NOT NULL COMMENT '审批人姓名',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_supplier_id (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商信用恢复记录';

-- 扩展原材料记录表，添加供应商关联
ALTER TABLE `raw_material_record` ADD COLUMN `supplier_id` BIGINT COMMENT '供应商ID';
ALTER TABLE `raw_material_record` ADD INDEX idx_supplier_id (`supplier_id`);

-- 扩展车间记录表，添加外包商关联
ALTER TABLE `workshop_record` ADD COLUMN `supplier_id` BIGINT COMMENT '外包商ID';
ALTER TABLE `workshop_record` ADD INDEX idx_supplier_id (`supplier_id`);

-- 扩展异常工单表，添加责任归属字段
ALTER TABLE `exception_order` ADD COLUMN `responsible_supplier_id` BIGINT COMMENT '责任供应商ID';
ALTER TABLE `exception_order` ADD COLUMN `responsibility_description` TEXT COMMENT '责任说明';
ALTER TABLE `exception_order` ADD COLUMN `deduct_score` INT COMMENT '扣分值';
ALTER TABLE `exception_order` ADD INDEX idx_responsible_supplier_id (`responsible_supplier_id`);

-- 插入供应商演示数据
INSERT INTO `supplier` (`supplier_code`, `supplier_name`, `contact_name`, `contact_phone`, `supply_type`, `cooperation_start_date`, `credit_score`, `status`, `remark`) VALUES
('SUP000001', '金鼎原料有限公司', '张经理', '13800138001', 'RAW_MATERIAL', '2024-01-15', 92, 'NORMAL', '主要原材料供应商'),
('SUP000002', '恒信材料科技', '李总', '13800138002', 'RAW_MATERIAL', '2024-03-20', 78, 'NORMAL', '包装材料供应商'),
('SUP000003', '旺达外包服务', '王主管', '13800138003', 'OUTSOURCED', '2024-02-10', 65, 'OBSERVATION', '曾出现质量问题，处于观察期'),
('SUP000004', '精益制造车间', '赵厂长', '13800138004', 'OUTSOURCED', '2024-05-01', 85, 'NORMAL', '精密加工外包'),
('SUP000005', '顺发原料厂', '刘经理', '13800138005', 'RAW_MATERIAL', '2024-06-15', 25, 'BLACKLIST', '多次出现原材料纯度不达标'),
('SUP000006', '华宇外包团队', '陈队', '13800138006', 'OUTSOURCED', '2024-04-12', 55, 'NORMAL', '组装工序外包'),
('SUP000007', '鑫源材料公司', '孙总', '13800138007', 'RAW_MATERIAL', '2024-07-20', 95, 'NORMAL', '高品质原料供应商'),
('SUP000008', '众诚外包车间', '周主任', '13800138008', 'OUTSOURCED', '2024-08-05', 35, 'NORMAL', '需加强质量监控');

SET FOREIGN_KEY_CHECKS = 1;
