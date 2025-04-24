package com.example.wx.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/22 20:51
 */
public class RoutingWorkflow {

    private final ChatClient chatClient;

    public RoutingWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public record RoutingResponse(String reasoning, String selection){}

    public String route(String input, Map<String, String> routes) {
        Assert.notNull(input, "Input text cannot be null");
        Assert.notEmpty(routes, "Routes map cannot be null or empty");

        // 为输入确定适当的路由
        String routeKey = determineRoute(input, routes.keySet());

        // 获取正确的提示词
        String selectedPrompt = routes.get(routeKey);

        if (selectedPrompt == null) {
            throw new IllegalArgumentException("Selected route '" + routeKey + "' not found in routes map");
        }

        // 使用选定的提示符处理输入
        return chatClient.prompt(selectedPrompt + "\nInput: " + input).call().content();
    }


    private String determineRoute(String input, Iterable<String> availableRoutes) {
        System.out.println("\n可用的路由: " + availableRoutes);

        String selectorPrompt = String.format("""
                分析输入并从以下选项中选择最合适的一项：%s
                首先解释你的推理过程，然后以以下 JSON 格式提供你的选择：

                \\{
                    "reasoning": "简要解释为什么这张票应该路由到一个特定的团队。
                                  考虑关键术语、用户意图和紧急程度。",
                    "selection": "选择一个可用的路由"
                \\}

                Input: %s""", availableRoutes, input);

        RoutingResponse routingResponse = chatClient.prompt(selectorPrompt).call().entity(RoutingResponse.class);

        System.out.printf("Routing Analysis:%s\nSelected route: %s\n",
                routingResponse.reasoning(), routingResponse.selection());

        return routingResponse.selection();
    }
}
