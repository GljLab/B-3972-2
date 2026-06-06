# 三方校验式库存与工资核算系统 (Inventory & Salary Cross-Check System)

## 🛠 技术栈 (Tech Stack)
- **Frontend**: Vue 3 + Vite + Pinia + Vue Router + Element Plus + Tailwind CSS + ECharts
- **Backend**: Spring Boot 3 + MyBatis-Flex + Java 17
- **Database**: MySQL 8.0

## 🎯 业务目标
系统解决制造行业核心痛点：无法准确核定外包车间工人产量与仓库实际库存的真实性。
通过打通：
1. **原材料领料 (基于BOM折算生产理论值)**
2. **车间工序上报 (实际员工录入计件)**
3. **库房与发货 (期末库存真实值 + 发货数据)**
三个维度，进行"产品+日期"颗粒度的智能聚合交叉核销，预警差异。

## 🚀 启动指南 (How to Run)
1. 确保您的开发机已正确安装 **Docker** 及 **Docker Compose**。
2. 保持网络畅通（推荐在中国大陆网络环境运行，已内置所有加速源及淘宝 npm registry）。
3. 请终端导航到项目的根目录（即 `docker-compose.yml` 所在目录）。
4. 在根目录执行一条命令：
   ```bash
   docker compose up --build -d
   ```
5. 等待镜像拉取、后端 Maven (`pom.xml`) 下载依赖和前端 Node.js 构建完成（由于配置了国内镜像，取决于网速一般 2~5 分钟）。

## 🔗 服务地址 (Services)
- **前端系统入口**: [http://localhost:3000](http://localhost:3000) (在浏览器中打开使用)
- 后端 API 地址: `http://localhost:8080/api/` (前端容器已配好跨域网关 Nginx 转发模式)
- 数据库连接: `localhost:3306` (用户名: `root` / 密码: `root`)

## ✨ 功能一览
- **数据库 Seed 填充**：项目在启动阶段（第一次执行 `init.sql`），已经向 `inventory_system` 数据库中打入了 2 月份的多条各维度的交叉测试数据。您可以立刻进入 [业务校验大盘] 看见可视化预警红线与图表。
- **动态阈值配置**：支持对误差率进行容差匹配并高亮（`user_rule` 规范，UI 直接控制）。
- **组件库美学**：使用 Vue3 的 Setup 语法结合 TailwindCSS/ElementPlus 以及 `echarts` 的响应式仪表盘。
- **异常边界捕获**：API 请求错误和断网自动由 Vue/Axios 全局 ErrorHandler 捕获拦截，保证高可用体验。

