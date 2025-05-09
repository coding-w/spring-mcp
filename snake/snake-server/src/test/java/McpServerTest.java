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
                .args("-Dspring.ai.mcp.server.stdio=true",
                        "-Dspring.main.banner-mode=off",
                        "-Dserver.port=8003",
                        "-Dlogging.pattern.console=",
                        "-jar",
                        "/Users/wx/workspace/tvi/zz/spring-mcp/snake/snake-server/target/snake-server.jar"
                )
                .build();

        var stdioTransport = new StdioClientTransport(stdioParams);

        var mcpClient = McpClient.sync(stdioTransport).build();

        mcpClient.initialize();

        McpSchema.ListToolsResult toolsList = mcpClient.listTools();

        System.out.println(toolsList);
        mcpClient.closeGracefully();
    }
}
