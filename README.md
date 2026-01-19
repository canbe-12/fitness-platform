# Fitness Platform (健身管理平台)

## 📖 项目简介
Fitness Platform 是一个功能全面的个人健身管理系统，旨在帮助用户科学地记录、追踪和分析健身数据。系统集成了训练记录、饮食管理、身体数据监控、计划制定以及综合健康报表等功能，通过直观的数据可视化图表，帮助用户更好地掌握自己的健康状况和健身进度。

## ✨ 核心功能
*   **🏋️ 训练管理**：记录每日训练动作、组数、重量和次数，支持自定义训练计划。
*   **🥗 饮食追踪**：记录每日餐饮摄入，自动计算热量及营养素（蛋白质/碳水/脂肪）分布。
*   **📈 身体趋势**：可视化展示体重、BMI 等身体指标的变化趋势，支持线性回归趋势分析。
*   **📊 综合报表**：生成周/月度健康报告，多维度分析训练负荷与饮食平衡。
*   **📅 计划制定**：制定每日训练和饮食任务，通过打卡机制督促执行。
*   **🛡️ 用户系统**：基于 JWT 的安全认证体系，支持用户注册、登录及个人信息管理。

## 🛠️ 技术栈

### 前端 (Frontend)
*   **核心框架**: [Vue 3](https://vuejs.org/) (Composition API)
*   **构建工具**: [Vite](https://vitejs.dev/)
*   **UI 组件库**: [Element Plus](https://element-plus.org/)
*   **数据可视化**: [ECharts](https://echarts.apache.org/) / Vue-ECharts
*   **状态管理**: [Pinia](https://pinia.vuejs.org/)
*   **路由管理**: [Vue Router](https://router.vuejs.org/)
*   **HTTP 客户端**: [Axios](https://axios-http.com/)
*   **工具库**: Day.js (日期处理)

### 后端 (Backend)
*   **核心框架**: [Spring Boot 3.3.2](https://spring.io/projects/spring-boot)
*   **开发语言**: Java 17
*   **持久层**: Spring Data JPA
*   **数据库**: MySQL 8.0+
*   **安全认证**: Spring Security + JWT (JSON Web Token)
*   **API 文档**: SpringDoc OpenAPI (Swagger UI)
*   **工具库**: Lombok

## 📂 项目结构
```
fitness-platform/
├── backend/                    # 后端工程目录
│   └── fitness-platform-backend/
│       ├── src/main/java/      # Java 源码
│       ├── src/main/resources/ # 配置文件及 SQL 脚本
│       └── pom.xml             # Maven 依赖配置
├── frontend/                   # 前端工程目录
│   └── fitness-platform-frontend/
│       ├── src/                # Vue 源码 (api, components, pages, stores...)
│       ├── package.json        # NPM 依赖配置
│       └── vite.config.ts      # Vite 配置
└── README.md                   # 项目说明文件
```

## 🚀 快速开始

### 环境准备
*   **Java**: JDK 17+
*   **Node.js**: v20.19.0+ (推荐使用 pnpm 或 npm)
*   **Database**: MySQL 8.0+

### 1. 后端启动
1.  进入后端目录：
    ```bash
    cd backend/fitness-platform-backend
    ```
2.  创建数据库（默认库名为 `fitness_platform`）：
    ```sql
    CREATE DATABASE fitness_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    ```
3.  配置数据库连接：
    修改 `src/main/resources/application.properties` 中的数据库用户名和密码：
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/fitness_platform?useSSL=false&serverTimezone=UTC
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    ```
4.  运行项目：
    ```bash
    ./mvnw spring-boot:run
    ```
    *后端服务将启动在 `http://localhost:8080`*

### 2. 前端启动
1.  进入前端目录：
    ```bash
    cd frontend/fitness-platform-frontend
    ```
2.  安装依赖：
    ```bash
    npm install
    # 或者使用 pnpm
    pnpm install
    ```
3.  启动开发服务器：
    ```bash
    npm run dev
    ```
    *前端页面将访问地址通常为 `http://localhost:5173`*

