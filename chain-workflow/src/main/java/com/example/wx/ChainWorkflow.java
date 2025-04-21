package com.example.wx;

import org.springframework.ai.chat.client.ChatClient;

import javax.swing.*;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/21 22:22
 */
public class ChainWorkflow {
    private static final String[] DEFAULT_SYSTEM_PROMPTS = {

            // Step 1
            """
                仅从文本中提取数值及其相关度量。
                在新行上将每个格式设置为“value: metric”。
                示例格式:
                92：顾客满意
                45%：收入增长""",
            // Step 2
            """
                在可能的情况下将所有数值转换为百分比。
                如果不是百分比或点数，则转换为小数（例如，92点-> 92%）。
                每行保留一个号码。
                示例格式:
                92%：客户满意度
                45%：收入增长""",
            // Step 3
            """
                按数值降序对所有行进行排序。
                每行保持“value: metric”格式。
                例子:
                92%：客户满意度
                87%：员工满意度""",
            // Step 4
            """
                将排序后的数据格式化为带有列的markdown表：
                | Metric | Value |
                |:--|--:|
                | 客户满意度 | 92% |"""
    };

    private final ChatClient chatClient;

    private final String[] systemPrompts;

    public ChainWorkflow(ChatClient chatClient) {
        this(chatClient, DEFAULT_SYSTEM_PROMPTS);
    }

    public ChainWorkflow(ChatClient chatClient, String[] systemPrompts) {
        this.chatClient = chatClient;
        this.systemPrompts = systemPrompts;
    }

    public String chain(String userInput) {

        int step = 0;
        String response = userInput;
        System.out.printf("\nSTEP %d:\n %s\n", step++, response);

        for (String prompt : systemPrompts) {

            // 1. Compose the input using the response from the previous step.
            String input = String.format("{%s}\n {%s}", prompt, response);

            // 2. Call the chat client with the new input and get the new response.
            response = chatClient.prompt(input).call().content();

            System.out.println(String.format("\nSTEP %s:\n %s", step++, response));
        }

        return response;
    }
}
