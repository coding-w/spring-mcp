import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/20 17:10
 */
public class McpServerTest {
    public static void main(String[] args) {
        var stdioParams = ServerParameters.builder("java")
                .args("-jar", "/Users/wx/workspace/tvi/zz/spring-mcp/spring-mcp-server/target/spring-mcp-server.jar")
                .build();

        var stdioTransport = new StdioClientTransport(stdioParams);

        var mcpClient = McpClient.sync(stdioTransport).build();

        mcpClient.initialize();

        McpSchema.ListToolsResult toolsList = mcpClient.listTools();

        // McpSchema.CallToolResult weather = mcpClient.callTool(
        //         new McpSchema.CallToolRequest("getWeatherForecastByLocation",
        //                 Map.of("latitude", "47.6062", "longitude", "-122.3321")));
        //
        // McpSchema.CallToolResult alert = mcpClient.callTool(
        //         new McpSchema.CallToolRequest("getAlerts", Map.of("state", "NY")));

        System.out.println(toolsList);
        mcpClient.closeGracefully();
    }
}
