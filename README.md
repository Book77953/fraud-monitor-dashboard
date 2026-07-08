# Fraud Monitor Dashboard

基于 ECharts 和 OceanBase 的金融交易欺诈风险实时监控可视化大屏

## 项目背景

金融交易场景中，欺诈行为往往表现为短时间内高频交易、金额突变、夜间异常操作等时序特征。本系统通过提取用户行为画像，构建多维风险评分规则，并利用可视化大屏实现风险实时监控。

## 核心功能

- **交易趋势监控**：实时展示交易量走势，识别异常峰值
- **风险用户分布**：按地区/时段展示高风险用户聚集情况
- **预警实时排行**：触发预警规则的交易按风险等级排序
- **多维筛选**：支持按时间、风险等级、交易类型下钻分析

## 技术架构

- **数据层**：OceanBase / MySQL 存储十万级交易流水数据
- **特征工程**：Pandas 提取时序行为特征（交易频次、金额突变率、夜间交易占比）
- **可视化层**：ECharts 绘制折线图、热力图、排行榜
- **后端服务**：Flask 提供数据查询 API，支持动态筛选

## 性能优化

- OceanBase 分区索引优化，按日期分区存储，查询响应速度提升约 40%
- 前端按需加载数据，减少首屏渲染时间

## 如何运行

```bash
git clone https://github.com/Book77953/fraud-monitor-dashboard.git
cd fraud-monitor-dashboard
pip install -r requirements.txt
python app.py
# 访问 http://localhost:5000
```

## 文件目录

```
├── data/               # 样例交易数据（脱敏）
├── notebooks/          # 特征工程与EDA分析
├── app/                # Flask 后端服务
│   └── app.py
├── static/             # 前端静态资源
│   ├── css/
│   └── js/             # ECharts 图表渲染
├── templates/          # HTML 大屏页面
│   └── dashboard.html
├── requirements.txt
└── README.md
