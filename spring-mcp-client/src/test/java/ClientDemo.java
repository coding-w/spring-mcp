import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/23 21:53
 */
public class ClientDemo {
    public static void main(String[] args) {
        var stdioParams = ServerParameters.builder("java")
                .args("-Dspring.ai.mcp.server.stdio=true",
                        "-Dspring.main.banner-mode=off",
                        "-Dspring.main.web-application-type=none",
                        "-Dserver.port=8003",
                        "-Dlogging.pattern.console=",
                        "-jar",
                        "/Users/wx/workspace/tvi/zz/spring-mcp/spring-mcp-server/target/spring-mcp-server.jar"
                ).build();

        var transport = new StdioClientTransport(stdioParams);
        var client = McpClient.sync(transport).build();

        client.initialize();

        // List and demonstrate tools
        McpSchema.ListToolsResult toolsList = client.listTools();
        System.out.println("Available Tools = " + toolsList);
        //
        // McpSchema.CallToolResult weatherForcastResult = client.callTool(new McpSchema.CallToolRequest("getWeatherForecastByLocation",
        //         Map.of("latitude", "47.6062", "longitude", "-122.3321")));
        // System.out.println("Weather Forcast: " + weatherForcastResult);
        //
        // McpSchema.CallToolResult alertResult = client.callTool(new McpSchema.CallToolRequest("getAlerts", Map.of("state", "NY")));
        // System.out.println("Alert Response = " + alertResult);

        client.closeGracefully();
    }
}
