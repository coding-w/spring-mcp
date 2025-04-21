package com.example.wx;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/21 22:17
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
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
    @Bean
    public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {
        return args -> {
            new ChainWorkflow(chatClientBuilder.build()).chain(report);
        };
    }
}