package window;

import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/20 20:48
 */
public class ClientSse {
    public static void main(String[] args) {
        var transport = new WebFluxSseClientTransport(WebClient.builder().baseUrl("http://localhost:8080"));

        new SampleClient(transport).run();
    }

}
