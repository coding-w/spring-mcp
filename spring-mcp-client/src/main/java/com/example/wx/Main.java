package com.example.wx;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Scanner;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/23 17:59
 */
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    // @Bean
    // public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
    //     return chatClientBuilder.defaultSystem("中文回答").build();
    // }

    @Bean
    public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder, List<McpSyncClient> mcpSyncClients) {
        return args -> {
            var chatClient = chatClientBuilder
                    .defaultSystem("""
                            你是个人小助手.
                            如果用户问出行相关请使用amap-maps相关工具回答;
                            如果用户问计算问题请使用demo mcp相关工具回答；
                            如果用户问题与上述无关，提醒用户“问题不在处理范围内”，并告诉用户你有哪些能力
                            """)
                    .defaultTools(new SyncMcpToolCallbackProvider(mcpSyncClients))
                    // .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                    .build();
            for (McpSyncClient client : mcpSyncClients) {
                System.out.println("mcp server => " + client.getServerInfo().name());
            }
            System.out.println("\n我是个人小助手\n");
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print("\n用户: ");
                    System.out.println("\nASSISTANT: " +
                            chatClient.prompt(scanner.nextLine()) // Get the user input
                                    .call()
                                    .content());
                }
            }
        };
    }
}