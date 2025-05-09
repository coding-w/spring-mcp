package com.example.wx;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Scanner;

/**
 * @author wangxiang
 * @description
 * @create 2025/5/8 23:12
 */
@SpringBootApplication
public class SnakeClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(SnakeClientApplication.class, args);
    }

    @Bean
    public CommandLineRunner predefinedQuestions(
            ChatClient.Builder chatClientBuilder,
            List<McpSyncClient> mcpSyncClients,
            ConfigurableApplicationContext context) {
        return args -> {
            var chatClient = chatClientBuilder
                    .defaultSystem("""
                            你是个人小助手.
                            如果用户问贪吃蛇问题请使用snake-server mcp相关工具回答；
                            如果用户问题与上述无关，提醒用户“问题不在处理范围内”，并告诉用户你有哪些能力
                            """)
                    .defaultTools(new SyncMcpToolCallbackProvider(mcpSyncClients))
                    .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                    .build();
            System.out.println("""
                    🤖AI:贪吃蛇助手 MCP Client
                    'quit' 退出
                    """);
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("\n用户: ");
                String input = scanner.nextLine();
                while (!input.equals("quit")) {
                    System.out.println("\n🤖AI: " +
                            chatClient.prompt(input) // Get the user input
                                    .call()
                                    .content());

                    System.out.print("\n用户: ");
                    input = scanner.nextLine();
                }
            }
            context.close();
        };
    }
}