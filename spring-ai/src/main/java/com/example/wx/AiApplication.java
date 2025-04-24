package com.example.wx;

import com.example.wx.service.ChainWorkflow;
import com.example.wx.service.EvaluatorOptimizer;
import com.example.wx.service.OrchestratorWorkers;
import com.example.wx.service.ParallelizationWorkflow;
import com.example.wx.service.RoutingWorkflow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;


/**
 * @author wangxiang
 * @description
 * @create 2025/4/20 16:31
 */
@SpringBootApplication
public class AiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.defaultSystem("中文回答").build();
    }


    @Bean
    public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {
        return args -> {
            // chain(chatClientBuilder);
            // parallel(chatClientBuilder);
            // routing(chatClientBuilder);
            // orchestrator(chatClientBuilder.defaultSystem("中文回答"));
            evaluator(chatClientBuilder);
        };
    }

    private void evaluator(ChatClient.Builder chatClientBuilder) {
        EvaluatorOptimizer.RefinedResponse refinedResponse = new EvaluatorOptimizer(chatClientBuilder.build()).loop("""
                        <user input>
                        实现一个 Java 中的栈（Stack），要求：
                        1. 实现 push(x)
                        2. 实现 pop()
                        3. 实现 getMin()
                        所有操作的时间复杂度必须为 O(1)。
                        所有类的内部字段必须为 private，并在使用时以 this. 作为前缀。
                        </user input>
                        """);
        System.out.println("最后输出:\n " + refinedResponse);
    }

    private void orchestrator(ChatClient.Builder chatClientBuilder) {
        new OrchestratorWorkers(chatClientBuilder.build())
                .process("为一个新的环保水瓶写一篇产品描述");
    }

    private void routing(ChatClient.Builder chatClientBuilder) {
        Map<String, String> supportRoutes = Map.of(
                "billing", // 计费相关
                """
                    你是一个计费支持专员。请遵循以下指南：
                    1. 始终以“计费支持回复:”开头
                    2. 首先确认用户具体的计费问题
                    3. 清楚地解释任何费用或差异
                    4. 列出明确的后续步骤及时间表
                    5. 如有需要，最后提供付款选项
        
                    保持专业但友好的语气。
        
                    输入：
                    """,

                "technical", // 技术支持
                """
                    你是一个技术支持工程师。请遵循以下指南：
                    1. 始终以“技术支持回复:”开头
                    2. 列出解决问题的具体步骤
                    3. 如有相关，提供系统要求
                    4. 提供常见问题的替代解决方案
                    5. 如有需要，最后说明升级支持的路径
        
                    使用清晰、编号的步骤和技术细节。
        
                    输入：
                    """,

                "account", // 账户相关
                """
                    你是一个账户安全专员。请遵循以下指南：
                    1. 始终以“账户支持回复:”开头
                    2. 优先处理账户安全与身份验证问题
                    3. 提供清晰的账户恢复/变更步骤
                    4. 包含安全提示与警告
                    5. 设置明确的解决时间预期
        
                    保持严肃、注重安全的语气。
        
                    输入：
                    """,

                "product", // 产品相关
                """
                    你是一个产品专员。请遵循以下指南：
                    1. 始终以“产品支持回复:”开头
                    2. 重点介绍功能使用与最佳实践
                    3. 包含具体的使用示例
                    4. 链接相关文档部分
                    5. 推荐可能有帮助的相关功能
        
                    语气应具启发性和鼓励性。
        
                    输入：
                    """);

        List<String> tickets = List.of(
                """
                    主题：无法访问我的账户
                    信息：你好，我过去一小时一直尝试登录，但总是提示“密码无效”。
                    我确定我输入的是正确的密码。你能帮我找回访问权限吗？这很紧急，我今天必须提交一份报告。
                    - John""",

                """
                    主题：我的信用卡上出现了意外扣费
                    信息：你好，我刚刚发现我的信用卡被你们公司扣了.99美元，但我记得我用的是.99的套餐。
                    你能解释一下这个扣费吗？如果是错误的请帮我调整一下，谢谢！
                    - Sarah""",

                """
                    主题：如何导出数据？
                    信息：我需要将所有项目数据导出为 Excel。我查阅了文档，但找不到如何批量导出。
                    这可以实现吗？如果可以，你能一步步告诉我该怎么做吗？
                    祝好，
                    Mike""");

        var routerWorkflow = new RoutingWorkflow(chatClientBuilder.build());

        int i = 1;
        for (String ticket : tickets) {
            System.out.println("\nTicket " + i++);
            System.out.println("------------------------------------------------------------");
            System.out.println(ticket);
            System.out.println("------------------------------------------------------------");
            System.out.println(routerWorkflow.route(ticket, supportRoutes));
        }

    }

    private void parallel(ChatClient.Builder chatClientBuilder) {
        List<String> parallelResponse = new ParallelizationWorkflow(chatClientBuilder.build())
                .parallel(
                        """
                                分析市场变化将如何影响这个利益相关者群体。
                                提供具体的影响和建议的行动。
                                格式要有清晰的章节和优先级。
                                """,
                        List.of(
                                """
                                        客户:
                                        -对价格敏感
                                        -想要更好的技术
                                        -环境问题
                                        """,
                                """
                                        员工:
                                        -工作保障担忧
                                        -需要新技能
                                        -想要明确的方向
                                        """,
                                """
                                        投资者:
                                        -期待增长
                                        -成本控制
                                        -风险关注
                                        """,
                                """
                                        供应商:
                                        -容量限制
                                        -价格压力
                                        -技术转型
                                        """
                        ),
                        4

                );
        System.out.println(parallelResponse);
    }

    private void chain(ChatClient.Builder chatClientBuilder) {
        String report = """
            第三季业绩总结：
            本季度我们的客户满意度得分上升到92分。
            收入比去年增长了45%。
            目前我们在主要市场的市场份额为23%。
            客户流失率从8%下降到5%。
            新用户获取成本为每用户43美元。
            产品采用率提升至78%。
            员工满意度为87分。
            营业利润率提高至34%。
            """;
        new ChainWorkflow(chatClientBuilder.build()).chain(report);
    }
}
