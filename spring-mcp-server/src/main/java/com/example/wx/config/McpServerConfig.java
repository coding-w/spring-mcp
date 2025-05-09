package com.example.wx.config;

import com.example.wx.service.ToolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;


/**
 * @author wangxiang
 * @description
 * @create 2025/4/20 19:47
 */
@Configuration
public class McpServerConfig {
    // STDIO transport
    @Bean
    @ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "stdio")
    public StdioServerTransportProvider stdioServerTransportProvider() {
        return new StdioServerTransportProvider();
    }

    // SSE transport
    @Bean
    @ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "sse")
    public WebFluxSseServerTransportProvider sseServerTransportProvider() {
        return new WebFluxSseServerTransportProvider(new ObjectMapper(), "/mcp/message");
    }

    // Router function for SSE transport used by Spring WebFlux to start an HTTP
    // server.
    @Bean
    @ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "sse")
    public RouterFunction<?> mcpRouterFunction(WebFluxSseServerTransportProvider transportProvider) {
        return transportProvider.getRouterFunction();
    }
    @Bean
    public ToolService toolService() {
        return new ToolService();
    }

    @Bean
    public ToolCallbackProvider serverTools(ToolService toolService) {
        return MethodToolCallbackProvider.builder().toolObjects(toolService).build();
    }

    @Bean
    public McpSyncServer mcpServer(McpServerTransportProvider transportProvider, ToolService toolService) { // @formatter:off

        // Configure server capabilities with resource support
        var capabilities = McpSchema.ServerCapabilities.builder()
                .tools(true) // Tool support with list changes notifications
                .logging() // Logging support
                .build();

        return McpServer.sync(transportProvider)
                .serverInfo("计算器 Server", "1.0.0")
                .capabilities(capabilities)
                .tools(McpToolUtils.toSyncToolSpecifications(ToolCallbacks.from(toolService))) // Add @Tools
                .tools()
                .build(); // @formatter:on
    } // @formatter:on
}
