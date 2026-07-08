# 金融交易欺诈风险监控可视化大屏

基于 Spring Boot + ECharts + OceanBase 的实时风控指标监控系统。

## 项目背景

金融交易场景中，欺诈行为往往表现为短时间内高频交易、金额突变、夜间异常操作等时序特征。本系统通过提取用户行为画像，构建多维风险评分规则，并利用可视化大屏实现风险实时监控。

## 技术栈

- **后端**：Spring Boot 2.7 + MyBatis
- **数据库**：OceanBase / MySQL
- **前端可视化**：ECharts 5.x
- **构建工具**：Maven 3.8+

## 核心功能

- 📈 交易趋势实时监控（折线图）
- 🗺️ 风险用户地区分布（地图热力）
- 🔔 欺诈预警实时排行（滚动列表）
- 🔍 多维度筛选（时间/风险等级/交易类型）

## 如何运行

### 环境要求
- JDK 11+
- Maven 3.8+
- OceanBase / MySQL 5.7+

### 配置数据库
在 `src/main/resources/application.properties` 中修改数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fraud_db
spring.datasource.username=root
spring.datasource.password=你的密码
```

### 启动项目
```bash
# 编译打包
mvn clean package

# 运行
java -jar target/fraud-monitor-dashboard.jar
```

或直接在 IDE 中运行 `Application.java` 主类。

访问：`http://localhost:8080`

## 性能优化

- OceanBase 分区索引优化（按日期分区），复杂查询响应速度提升约 40%
- ECharts 数据按需加载，减少首屏渲染时间

## 文件目录

```
├── src/main/java/com/xxx/fraud/
│   ├── controller/      # API 控制器
│   ├── service/         # 业务逻辑
│   ├── mapper/          # 数据访问层
│   └── entity/          # 数据实体
├── src/main/resources/
│   ├── static/          # 前端静态资源（CSS/JS）
│   ├── templates/       # HTML 模板
│   └── application.properties
├── pom.xml              # Maven 依赖
└── README.md
