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

SET FOREIGN_KEY_CHECKS = 1;
