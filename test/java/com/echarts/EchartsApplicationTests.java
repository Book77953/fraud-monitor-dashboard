package com.echarts;

import com.echarts.common.Result;
import com.echarts.entity.Transaction;
import com.echarts.mapper.TransactionMapper;
import com.echarts.service.EchartsAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 项目全链路测试类（Controller + Service + Mapper）
 * 修复点：嵌套Service测试类显式指定SpringBoot启动类，确保Bean扫描成功
 */
@SpringBootTest(classes = EchartsApplication.class)  // 主测试类指定启动类，加载完整上下文
@AutoConfigureMockMvc  // 自动配置MockMvc，用于Controller测试
class EchartsApplicationTests {

    // ==================== Controller层测试（模拟HTTP请求，不依赖真实数据库） ====================
    @Autowired
    private MockMvc mockMvc;  // 模拟HTTP请求的核心对象

    @Autowired
    private WebApplicationContext webContext;  // Web上下文，用于构建MockMvc

    @MockBean  // Mock Service层，隔离Controller与Service的依赖
    private EchartsAnalysisService echartsAnalysisService;

    @Autowired
    private ObjectMapper objectMapper;  // JSON序列化/反序列化工具

    /**
     * 测试前初始化MockMvc
     */
    @BeforeEach
    void setUp() {
        // 基于Web上下文构建MockMvc，确保能识别Controller
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
    }

    /**
     * 测试基础统计接口（/api/echarts/base-stats）
     */
    @Test
    void testBaseStatsController() throws Exception {
        // 1. 模拟Service层返回数据（无需真实查询数据库）
        Map<String, Object> mockData = new HashMap<>();
        mockData.put("totalCount", 1000);       // 总交易数
        mockData.put("normalCount", 920);       // 正常交易数
        mockData.put("abnormalCount", 80);      // 异常交易数
        mockData.put("abnormalRate", "8.00");   // 异常率（8.00%）
        when(echartsAnalysisService.calcBaseStats()).thenReturn(mockData);

        // 2. 发送GET请求，验证响应结果
        mockMvc.perform(get("/api/echarts/base-stats")  // 接口路径
                        .contentType(MediaType.APPLICATION_JSON))  // 请求类型JSON
                .andExpect(status().isOk())  // 验证响应状态码200
                .andExpect(jsonPath("$.code").value(0))  // 验证返回码0（成功）
                .andExpect(jsonPath("$.data.totalCount").value(1000))  // 验证总交易数
                .andExpect(jsonPath("$.data.abnormalRate").value("8.00"))  // 验证异常率
                .andDo(result -> System.out.println("基础统计接口返回：" + result.getResponse().getContentAsString()));
    }

    /**
     * 测试交易类型分布接口（/api/echarts/type-dist）
     */
    @Test
    void testTypeDistController() throws Exception {
        // 1. 模拟Service返回的交易类型分布数据
        List<Map<String, Object>> mockList = new ArrayList<>();
        Map<String, Object> atmData = new HashMap<>();
        atmData.put("type", "ATM");           // 交易类型：ATM取款
        atmData.put("normal_count", 300);     // ATM正常交易数
        atmData.put("fraud_count", 20);       // ATM异常交易数
        mockList.add(atmData);

        Map<String, Object> onlineData = new HashMap<>();
        onlineData.put("type", "Online");     // 交易类型：线上交易
        onlineData.put("normal_count", 620);  // 线上正常交易数
        onlineData.put("fraud_count", 60);    // 线上异常交易数
        mockList.add(onlineData);

        when(echartsAnalysisService.calcTypeDist()).thenReturn(mockList);

        // 2. 发送请求并验证
        mockMvc.perform(get("/api/echarts/type-dist")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].type").value("ATM"))
                .andExpect(jsonPath("$.data[0].fraud_count").value(20))
                .andExpect(jsonPath("$.data[1].type").value("Online"))
                .andExpect(jsonPath("$.data[1].fraud_count").value(60));
    }

    // ==================== Mapper层测试（专注数据库交互，仅加载MyBatis相关上下文） ====================
    @Nested
    @MybatisTest
    class TransactionMapperTests {

        @Autowired
        private TransactionMapper transactionMapper;

        @Test
        void testGetBaseStats() {
            Map<String, Number> stats = transactionMapper.getBaseStats();
            System.out.println("Mapper测试 - 总交易数：" + stats.get("total_count"));
            System.out.println("Mapper测试 - 异常交易数：" + stats.get("fraud_count"));

            // 修复：将Number转成long类型后再比较
            assert stats != null : "基础统计查询结果为空";
            assert stats.get("total_count").longValue() >= 0 : "总交易数不能为负数";
            assert stats.get("fraud_count").longValue() >= 0 : "异常交易数不能为负数";
        }

        /**
         * 测试批量插入（batchInsert）
         */
        @Test
        void testBatchInsert() {
            // 1. 构造测试数据
            List<Transaction> testList = new ArrayList<>();
            Transaction trans = new Transaction();
            trans.setTransactionId(10001);       // 交易ID（根据实体类字段类型调整，若为Long则加L）
            trans.setUserId("101");                // 用户ID
            trans.setAmount("3500.0");              // 交易金额
            trans.setTransactionType("Online");   // 交易类型
            trans.setMerchantCategory("Shopping");// 商户类别
            trans.setCountry("US");               // 国家
            trans.setHour("19");                    // 交易小时
            trans.setDeviceRiskScore("0.85");       // 设备风险评分
            trans.setIpRiskScore("0.78");           // IP风险评分
            trans.setIsFraud("1");                  // 1=异常交易
            testList.add(trans);

            // 2. 执行批量插入
            transactionMapper.batchInsert(testList);

            // 3. 验证插入结果（查询异常交易数是否增加）
            Map<String, Number> stats = transactionMapper.getBaseStats();
            assert stats.get("fraud_count").longValue() >= 1L : "批量插入异常交易失败";
            System.out.println("Mapper测试 - 批量插入成功，当前异常交易数：" + stats.get("fraud_count"));
        }
    }

    // ==================== Service层测试（模拟Mapper，专注业务逻辑） ====================
    @Nested
    @SpringBootTest(classes = EchartsApplication.class)
    class EchartsAnalysisServiceTests {

        @MockBean
        private TransactionMapper transactionMapper;

        @Autowired
        private EchartsAnalysisService echartsAnalysisService;

        @Test
        void testCalcBaseStats() {
            // 修复：将mockMap类型改为Map<String, Number>
            Map<String, Number> mockMap = new HashMap<>();
            mockMap.put("normal_count", 850);  // Integer是Number的子类，可直接放入
            mockMap.put("fraud_count", 150);
            mockMap.put("total_count", 1000);
            // 现在类型匹配，不会报错
            when(transactionMapper.getBaseStats()).thenReturn(mockMap);

            Map<String, Object> result = echartsAnalysisService.calcBaseStats();

            assert result.get("abnormalRate").equals("15.00") : "异常率计算错误";
            assert result.get("totalCount").equals(1000) : "总交易数传递错误";
            System.out.println("Service测试 - 基础统计结果：" + result);
        }

        /**
         * 测试金额区间分布计算（calcAmountRangeDist）
         */
        @Test
        void testCalcAmountRangeDist() {
            // 1. 模拟Mapper返回的正常/异常交易金额区间数据
            List<Map<String, Object>> normalDist = new ArrayList<>();
            Map<String, Object> normalRange1 = new HashMap<>();
            normalRange1.put("amount_range", "0-1000");
            normalRange1.put("count", 500);
            normalDist.add(normalRange1);

            List<Map<String, Object>> fraudDist = new ArrayList<>();
            Map<String, Object> fraudRange1 = new HashMap<>();
            fraudRange1.put("amount_range", "0-1000");
            fraudRange1.put("count", 120);
            fraudDist.add(fraudRange1);

            when(transactionMapper.getAmountRangeDist("0")).thenReturn(normalDist);  // 正常交易（is_fraud=0）
            when(transactionMapper.getAmountRangeDist("1")).thenReturn(fraudDist);  // 异常交易（is_fraud=1）

            // 2. 执行Service方法
            Map<String, List<Map<String, Object>>> result = echartsAnalysisService.calcAmountRangeDist();

            // 3. 验证结果
            assert result.containsKey("normalAmountDist") : "缺少正常交易金额分布数据";
            assert result.containsKey("abnormalAmountDist") : "缺少异常交易金额分布数据";
            assert result.get("normalAmountDist").get(0).get("count").equals(500) : "正常交易区间数据错误";
            assert result.get("abnormalAmountDist").get(0).get("count").equals(120) : "异常交易区间数据错误";
            System.out.println("Service测试 - 金额区间分布结果：" + result);
        }
    }
}