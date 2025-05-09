package com.example.wx.config;

import com.example.wx.service.SnakeService;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wangxiang
 * @description
 * @create 2025/5/7 12:27
 */
@Configuration
public class McpServerConfig {
    // STDIO transport
    @Bean
    public StdioServerTransportProvider stdioServerTransportProvider() {
        return new StdioServerTransportProvider();
    }

    @Bean
    public SnakeService snakeService(WebSocketSessionManager webSocketSessionManager) {
        return new SnakeService(webSocketSessionManager);
    }

    @Bean
    public ToolCallbackProvider serverTools(SnakeService snakeService) {
        return MethodToolCallbackProvider.builder().toolObjects(snakeService).build();
    }

    @Bean
    public McpSyncServer mcpServer(McpServerTransportProvider transportProvider, SnakeService snakeService) { // @formatter:off

        // Configure server capabilities with resource support
        var capabilities = McpSchema.ServerCapabilities.builder()
                .tools(true) // Tool support with list changes notifications
                .logging() // Logging support
                .build();

        return McpServer.sync(transportProvider)
                .serverInfo("贪吃蛇决策 Server", "1.0.0")
                .capabilities(capabilities)
                .tools(McpToolUtils.toSyncToolSpecifications(ToolCallbacks.from(snakeService))) // Add @Tools
                .tools()
                .build();
    }

}
