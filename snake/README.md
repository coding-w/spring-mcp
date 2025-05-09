# MCP 实战 --> AI玩转贪吃蛇

`MCP` 更加便捷的扩展了 `LLM` 的能力，使得 `AI` 发展更加迅猛。本篇主要为了学习`MCP`的应用，实现了让`AI`去玩贪吃蛇，使用 `Java` 实现了 **MCP Server** 和 **MCP Client** 的编码。其他文章如下：

## thinking

### 基础版

1. 使用 `html` 实现通过上下左右版本的贪吃蛇
2. 贪吃蛇可以自动寻找‘食物’
3. 将贪吃蛇自动寻路的逻辑分离开，使用`WebSocket`进行路径规划

一个简易版的贪吃蛇已经完成了，这个代码可以尝试用`AI`生成

### MCP 版本
需要实现四个工具，让 AI 控制游戏的开始和结束  
- `start_game` 开始游戏
- `end_game` 结束游戏
- `auto_path_find` 自动寻找
- `get_state` 获取当前游戏状态
- `move_step` 手动移动

流程图如下

![](../images/img_2.png)

具体过程如下：
1. 服务启动，客户端连接服务端并获取其能力集，初始化完成，等待用户指令
2. 用户给 AI 一个任务，输入：开一局贪吃蛇吧，当得分大于100 分时停止！
    - 注：可以选择手动移动还是自动寻址
3. 客户端接收到指令，发送给 AI，一并发送的还有可用的工具集
4. AI 分析用户意图，判断用户要玩贪吃蛇，看到工具集里有控制游戏的状态
5. 于是告诉MCP Client，本次调用的工具是****调用完把结果返回给我
6. MCP Client 使用 Call Tool 去调用这两个工具
7. MCP Server 收到消息，要我开始游戏，给连接的贪吃蛇游戏客户端，发出指令，开始游戏
8. 贪吃蛇游戏汇报当前状态，游戏已开始，当前蛇在哪，食物在哪，蛇的方向，当前得分
9. MCP Server接收到贪吃蛇状态，记录下来，并告诉 MCP Client 当前游戏开始了，蛇的状态是...
10. MCP Client 获取到第一个工具 start_game 的结果后，发起第二次工具调用get_state，服务端再返回当前贪吃蛇最新的状态
11. MCP Client 拿到所有的数据后，发送给 AI
    - 如果是自动寻址，则循环获取 get_stat，直到得分大于100分，调用end_game通知游戏客户端停止
    - 如果是手动移动，需要让大模型判断如何移动，通知Client，然后调用 MCP Server具体的工具，再通知到游戏客户端，一直循环...直到完成任务为止

## 编码

### 基础版贪吃蛇

```html
<!DOCTYPE html>
<html>
<head>
    <title>贪吃蛇-豪华尊享版</title>
    <style>
        canvas {
            border: 3px solid #2c3e50;
            border-radius: 10px;
            background: linear-gradient(145deg, #ecf0f1, #dfe6e9);
        }
        #score-panel {
            font-size: 24px;
            margin: 15px 0;
            color: #2c3e50;
            font-family: Arial, sans-serif;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
        }
        body {
            display: flex;
            flex-direction: column;
            align-items: center;
            background: #bdc3c7;
            min-height: 100vh;
            margin: 0;
            padding-top: 20px;
        }
    </style>
</head>
<body>
<div id="score-panel">得分: 0</div>
<canvas id="gameCanvas" width="400" height="400"></canvas>
<script type="module">
    // 连接 snake-server
    const socket = new WebSocket('ws://localhost:8080/ws');
    socket.onmessage = (event) => {
        console.log('[WS Received]', event.data);
        const data = JSON.parse(event.data);

        // 处理方向指令
        if (data.type === 'direction') {
            switch(data.direction) {
                case'left': if (dx !== gridSize) { dx = -gridSize; dy = 0; } break;
                case'up': if (dy !== gridSize) { dx = 0; dy = -gridSize; } break;
                case'right': if (dx !== -gridSize) { dx = gridSize; dy = 0; } break;
                case'down': if (dy !== -gridSize) { dx = 0; dy = gridSize; } break;
            }
            gameStep(); // 执行一步
        }
        // 处理游戏开始指令
        else if (data.type === 'start') {
            if (!gameStarted) {
                gameStarted = true;
                initGame();
                // 发送状态到服务端
                sendStateToServer();
            }
        }
        // 处理游戏结束指令
        else if (data.type === 'end') {
            // 发送状态到服务端
            sendStateToServer();
            if (gameStarted) {
                gameOver();
            }
        }
        // 获取状态
        else if (data.type === 'get_state') {
            // 发送状态到服务端
            sendStateToServer();
        }
    };

    const canvas = document.getElementById('gameCanvas');
    const ctx = canvas.getContext('2d');
    const gridSize = 20;
    const initialSpeed = 150;

    // 颜色配置
    const colors = {
        snakeHead: '#3498db',
        snakeBody: '#2980b9',
        food: '#e74c3c',
        foodGlow: 'rgba(231, 76, 60, 0.4)',
        eye: '#FFFFFF'
    };
    let snake = [];
    let food = {};
    let dx = gridSize;
    let dy = 0;
    let score = 0;
    let gameStarted = false;
    let autoMove = false; // 新增自动移动控制开关
    let isSetting =  false; // 定时器注入开关
    let gameLoop;
    function initGame(){
        isSetting = false;
        snake = [
            {x: 5 * gridSize, y: 5 * gridSize},
            {x: 4 * gridSize, y: 5 * gridSize},
            {x: 3 * gridSize, y: 5 * gridSize}
        ];
        dx = gridSize;
        dy = 0;
        score = 0;
        document.getElementById('score-panel').textContent = `得分: ${score}`;
        generateFood();
        draw();
    }
    function generateFood(){
        food = {
            x: Math.floor(Math.random() * (canvas.width/gridSize)) * gridSize,
            y: Math.floor(Math.random() * (canvas.height/gridSize)) * gridSize,
            glow: 0// 新增发光动画状态
        };
        while(snake.some(s => s.x === food.x && s.y === food.y)) generateFood();
    }
    function drawSnake(){
        snake.forEach((segment, index) => {
            const isHead = index === 0;
            const radius = gridSize/2 * (isHead ? 0.9 : 0.8);

            // 身体渐变
            const gradient = ctx.createLinearGradient(
                segment.x, segment.y,
                segment.x + gridSize, segment.y + gridSize
            );
            gradient.addColorStop(0, isHead ? colors.snakeHead : colors.snakeBody);
            gradient.addColorStop(1, isHead ? lightenColor(colors.snakeHead, 20) : lightenColor(colors.snakeBody, 20));

            // 绘制身体
            ctx.beginPath();
            ctx.roundRect(
                segment.x + 1, segment.y + 1,
                gridSize - 2, gridSize - 2,
                isHead ? 8 : 6
            );
            ctx.fillStyle = gradient;
            ctx.shadowColor = 'rgba(0,0,0,0.2)';
            ctx.shadowBlur = 5;
            ctx.fill();

            // 绘制蛇头的眼睛
            if (isHead) {
                const eyeOffset = gridSize / 5;
                const eyeSize = gridSize / 5;
                const eyeX = segment.x + gridSize / 2 - eyeOffset;
                const eyeY = segment.y + gridSize / 2 - eyeOffset;

                // 左眼
                ctx.beginPath();
                ctx.arc(eyeX, eyeY, eyeSize, 0, Math.PI * 2);
                ctx.fillStyle = colors.eye;
                ctx.fill();

                // 右眼
                ctx.beginPath();
                ctx.arc(eyeX + 2 * eyeOffset, eyeY, eyeSize, 0, Math.PI * 2);
                ctx.fillStyle = colors.eye;
                ctx.fill();
            }

        });
    }
    function drawFood(){
        // 发光动画
        food.glow = (food.glow + 0.05) % (Math.PI * 2);
        const glowSize = Math.sin(food.glow) * 3;

        // 外发光
        ctx.beginPath();
        ctx.arc(
            food.x + gridSize/2,
            food.y + gridSize/2,
            gridSize/2 + glowSize,
            0, Math.PI * 2
        );
        ctx.fillStyle = colors.foodGlow;
        ctx.fill();

        // 食物主体
        ctx.beginPath();
        ctx.arc(
            food.x + gridSize/2,
            food.y + gridSize/2,
            gridSize/2 - 2,
            0, Math.PI * 2
        );
        const gradient = ctx.createRadialGradient(
            food.x + gridSize/2, food.y + gridSize/2, 0,
            food.x + gridSize/2, food.y + gridSize/2, gridSize/2
        );
        gradient.addColorStop(0, lightenColor(colors.food, 20));
        gradient.addColorStop(1, colors.food);
        ctx.fillStyle = gradient;
        ctx.fill();
    }
    function draw(){
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // 绘制网格背景
        drawGrid();

        drawSnake();
        drawFood();
    }
    function drawGrid(){
        ctx.strokeStyle = 'rgba(0,0,0,0.05)';
        ctx.lineWidth = 0.5;
        for(let x = 0; x < canvas.width; x += gridSize) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, canvas.height);
            ctx.stroke();
        }
        for(let y = 0; y < canvas.height; y += gridSize) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(canvas.width, y);
            ctx.stroke();
        }
    }
    function lightenColor(hex, percent){
        const num = parseInt(hex.replace('#',''), 16),
            amt = Math.round(2.55 * percent),
            R = (num >> 16) + amt,
            G = (num >> 8 & 0x00FF) + amt,
            B = (num & 0x0000FF) + amt;
        return `#${(1 << 24 | (R<255?R<1?0:R:255) << 16 | (G<255?G<1?0:G:255) << 8 | (B<255?B<1?0:B:255)).toString(16).slice(1)}`;
    }
    function gameStep() {
        const head = {x: snake[0].x + dx, y: snake[0].y + dy};

        if (head.x < 0 || head.x >= canvas.width ||
            head.y < 0 || head.y >= canvas.height ||
            snake.some(segment => segment.x === head.x && segment.y === head.y)) {
            gameOver();
            return;
        }
        snake.unshift(head);
        if (head.x === food.x && head.y === food.y) {
            score += 10;
            document.getElementById('score-panel').textContent = `得分: ${score}`;
            generateFood();
        } else {
            snake.pop();
        }
        // 发送状态到服务端
        sendStateToServer();
        draw();
    }
    // 修改后的游戏结束逻辑
    function gameOver() {
        clearInterval(gameLoop);
        gameStarted = false;
        autoMove = false;
        alert(`游戏结束！得分: ${score}`);
        initGame(); // 游戏结束后立即重置状态
    }
    function sendStateToServer() {
        let x = dx
        let y = dy
        // 发送状态到服务端
        const state = {
            type: 'state',
            snake: snake,
            food: food,
            nextDirection: { x, y },
            score: score
        };
        if (socket.readyState === WebSocket.OPEN) {
            socket.send(JSON.stringify(state));
        }
    }
    // 键盘事件监听
    document.addEventListener('keydown', (e) => {
        if (!gameStarted) {
            gameStarted = true;
            initGame();
        }

        switch(e.key) {
            case'ArrowLeft':
                if (dx !== gridSize) { dx = -gridSize; dy = 0; }
                break;
            case'ArrowUp':
                if (dy !== gridSize) { dx = 0; dy = -gridSize; }
                break;
            case'ArrowRight':
                if (dx !== -gridSize) { dx = gridSize; dy = 0; }
                break;
            case'ArrowDown':
                if (dy !== -gridSize) { dx = 0; dy = gridSize; }
                break;
        }
        move();
    });
    function move() {
        // 当自动开启，且没有设置定时器时，设置定时器，并将定时器标志位置为 true
        if(autoMove && !isSetting) {
            gameLoop = setInterval(gameStep, initialSpeed);
            isSetting = true;
        }else {
            gameStep();
        }
    }
    // 初始化首次显示
    initGame();
</script>
</body>
</html>
```

Java 实现的 WebSocket 核心代码如下
```java
public class MyWebSocketHandler extends TextWebSocketHandler {
    private final WebSocketSessionManager sessionManager;

    public MyWebSocketHandler(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * 连接建立后触发
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionManager.addSession(session);
        JSONObject jsonObject = new JSONObject();
        // 开始游戏
        jsonObject.put("type", "start");
        jsonObject.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(jsonObject.toJSONString()));
    }

    /**
     * 收到消息时触发
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        GameState data = JSONObject.parseObject(payload, GameState.class);
        GameState gameState = snakeService.getGameState();
        if (Objects.equals(data.getType(), "state")) {
            gameState.setSnake(data.getSnake());
            gameState.setScore(data.getScore());
            gameState.setFood(data.getFood());
            gameState.setType(data.getType());
            Position nextDirection = data.getNextDirection();
            gameState.setDirection(
                    nextDirection.getX() > 0 ? "right" : nextDirection.getX() < 0 ? "left" :
                    nextDirection.getY() > 0 ? "down" : "up");
            // 自动寻址
            if (gameState.isAutoPathFind()) {
                String direction = SnakeMoveUtils.calculateDirection(gameState);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "direction");
                jsonObject.put("direction", direction);
                jsonObject.put("timestamp", System.currentTimeMillis());
                session.sendMessage(new TextMessage(jsonObject.toJSONString()));
                Thread.sleep(100);
            }
        }
        System.out.println("收到消息：" + message.getPayload());
    }

    /**
     * 连接关闭时触发
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // todo ..
    }
}
```

上述就是基础版的贪吃蛇详细代码见文章末

### MCP 版本贪吃蛇
核心工具代码如下

```java
public class SnakeService {

    private final GameState gameState;

    private final static List<String> directions = List.of("left", "right", "up", "down");

    private final WebSocketSessionManager webSocketSessionManager;

    public record Res(List<Content> content) {

    }
    public record Content(String type, String text) {
    }

    public GameState getGameState() {
        return gameState;
    }

    public SnakeService(WebSocketSessionManager webSocketSessionManager) {
        this.gameState = new GameState();
        this.webSocketSessionManager = webSocketSessionManager;
    }

    @Tool(name = "move_step", description = "使蛇移动一步,需要精确传入 up,down,left,right 中的一个")
    public String moveStep(@ToolParam(description = "蛇移动的方向 up,down,left,right") String direction) {
        if (!directions.contains(direction)) {
            throw new IllegalArgumentException("direction参数错误");
        }
        gameState.setDirection(direction);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "direction");
        jsonObject.put("direction", direction);
        jsonObject.put("timestamp", System.currentTimeMillis());
        webSocketSessionManager.broadcast(jsonObject.toJSONString());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String gameStateJson = JSON.toJSONString(gameState);
        String message = String.format("方向已更新,当前状态为：%s", gameStateJson);
        ArrayList<Content> list = new ArrayList<>();
        list.add(new Content("text", message));
        Res res = new Res(list);
        return JSON.toJSONString(res);
    }

    @Tool(name = "get_state", description = "获取当前游戏状态")
    public String getState() {
        String gameStateJson = JSON.toJSONString(gameState);
        ArrayList<Content> list = new ArrayList<>();
        list.add(new Content("text", gameStateJson));
        Res res = new Res(list);
        return JSON.toJSONString(res);
    }

    @Tool(name = "auto_path_find", description = "开启自动移动")
    public String autoPathFind() {
        gameState.setAutoPathFind(Boolean.TRUE);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "get_state");
        jsonObject.put("timestamp", System.currentTimeMillis());
        webSocketSessionManager.broadcast(jsonObject.toJSONString());
        String gameStateJson = JSON.toJSONString(gameState);
        String message = String.format("自动移动已激活! 当前状态为：%s", gameStateJson);
        ArrayList<Content> list = new ArrayList<>();
        list.add(new Content("text", message));
        Res res = new Res(list);
        return JSON.toJSONString(res);
    }

    @Tool(name = "start_game", description = "开始新游戏")
    public String startGame() {
        gameState.setGameStarted(Boolean.TRUE);
        webSocketSessionManager.broadcast(JSON.toJSONString(new Content("start", null)));
        // 模拟 300ms 延迟（可选）
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String gameStateJson = JSON.toJSONString(gameState);
        String message = String.format("游戏已开始，当前状态为：%s", gameStateJson);
        ArrayList<Content> list = new ArrayList<>();
        list.add(new Content("text", message));
        Res res = new Res(list);
        return JSON.toJSONString(res);
    }

    @Tool(name = "end_game", description = "结束当前游戏")
    public String endGame() {
        gameState.setGameStarted(Boolean.FALSE);
        gameState.setAutoPathFind(Boolean.FALSE);
        webSocketSessionManager.broadcast(JSON.toJSONString(new Content("end", null)));
        ArrayList<Content> list = new ArrayList<>();
        list.add(new Content("text", "游戏已结束"));
        Res res = new Res(list);
        return JSON.toJSONString(res);
    }
}
```

工具声明如下所示

```java
@Configuration
public class McpServerConfig {
    @Bean
    public ToolCallbackProvider serverTools(SnakeService snakeService) {
        return MethodToolCallbackProvider.builder().toolObjects(snakeService).build();
    }

    @Bean
    public McpSyncServer mcpServer(McpServerTransportProvider transportProvider, SnakeService snakeService) {
        var capabilities = McpSchema.ServerCapabilities.builder()
                .tools(true)
                .logging()
                .build();

        return McpServer.sync(transportProvider)
                .serverInfo("贪吃蛇决策 Server", "1.0.0")
                .capabilities(capabilities)
                .tools(McpToolUtils.toSyncToolSpecifications(ToolCallbacks.from(snakeService)))
                .tools()
                .build();
    }
}
```

到这里MCP Server部分的代码就结束了。打包打包打包，上Cline(这里先使用Cline试试我们的mcp server)，不会Cline配置的同学，见下面这篇文章

[](../README.md)

配置完成如下
![](../images/images-2025-05-07-23-50-07.png)

在输入框中输入“开始一局贪吃蛇，自动移动，当分数达到100分，结束游戏”，执行结果如下

![](../images/2025-05-08T14_45_45.413Z-146808.gif)

### MCP Client

mcp server如下
```json
{
  "mcpServers": {
    "snake-server": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dserver.port=8080",
        "-Dspring.main.banner-mode=off",
        "-Dlogging.pattern.console=",
        "-jar",
        "/Users/wx/workspace/tvi/zz/spring-mcp/snake/snake-server/target/snake-server.jar"
      ],
      "env": {
      }
    }
  }
}
```
windows配置如下
```json
{
  "mcpServers": {
    "demo": {
      "command": "cmd",
      "args": [
        "/c",
        "java",
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dserver.port=8080",
        "-Dspring.main.banner-mode=off",
        "-Dlogging.pattern.console=",
        "-jar",
        "E:\\idea\\learn\\spring-mcp\\spring-mcp-server\\target\\****"
      ],
      "env": {
      }
    }
  }
}
```

核心代码如下
```java
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
```
效果如下
```text
用户: 游玩一局贪吃蛇，手动自行，请你根据状态计算下一步移动方案，并移动贪吃蛇

🤖AI: 蛇已向右移动一步！当前状态更新如下：

- **蛇的位置**: [(120, 100), (100, 100), (80, 100)]
- **食物位置**: (220, 300)
- **当前方向**: 右 (right)
- **分数**: 0

接下来，我将继续根据状态计算下一步移动方案。是否需要继续移动？

用户: 是

🤖AI: 根据当前游戏状态：

- **蛇的位置**: [(120, 100), (100, 100), (80, 100)]
- **食物位置**: (220, 300)
- **当前方向**: 右 (right)
- **分数**: 0

下一步建议继续向右移动以接近食物。是否执行？

用户: 是

🤖AI: 蛇已向右移动一步！当前状态更新如下：

- **蛇的位置**: [(140, 100), (120, 100), (100, 100)]
- **食物位置**: (220, 300)
- **当前方向**: 右 (right)
- **分数**: 0

是否需要继续移动？

用户: 是

🤖AI: 根据当前游戏状态：

- **蛇的位置**: [(140, 100), (120, 100), (100, 100)]
- **食物位置**: (220, 300)
- **当前方向**: 右 (right)
- **分数**: 0

下一步建议继续向右移动以接近食物。是否执行？

用户: 是

🤖AI: 蛇已向右移动一步！当前状态更新如下：

- **蛇的位置**: [(160, 100), (140, 100), (120, 100)]
- **食物位置**: (220, 300)
- **当前方向**: 右 (right)
- **分数**: 0

是否需要继续移动？

用户: 是

🤖AI: 根据当前游戏状态：

- **蛇的位置**: [(160, 100), (140, 100), (120, 100)]
- **食物位置**: (220, 300)
- **当前方向**: 右 (right)
- **分数**: 0

下一步建议继续向右移动以接近食物。是否执行？

用户: 是

🤖AI: 蛇已向右移动一步！当前状态更新如下：

- **蛇的位置**: [(180, 100), (160, 100), (140, 100)]
- **食物位置**: (220, 300)
- **当前方向**: 右 (right)
- **分数**: 0

是否需要继续移动？

用户: 是

🤖AI: 根据当前游戏状态：

- **蛇的位置**: [(180, 100), (160, 100), (140, 100)]
- **食物位置**: (220, 300)
- **当前方向**: 右 (right)
- **分数**: 0

下一步建议继续向右移动以接近食物。是否执行？

用户: 开启自动移动，100分后结束游戏

🤖AI: 自动移动已激活！蛇将自动移动直到达到100分。游戏结束后会通知您。请稍候！
```

该文是使用Java学习和应用MCP相关知识，源文章：

实践中的问题. 
1. `web-application-type`类型问题，`WebSocket`和`SSE`默认只不一样，需要做兼容，本文采用的是`stdio`
    - `WebSocket` 默认是`SERVLET`
    - `SSE` 默认是`REACTIVE`
2. Java MCP SDK 将调用方法都封装好了，中间交互过程没有源文章清洗，后续看看底层调用过程

其他问题

如何提升做事效率，做事效率好低，这篇文章都拖了好久好久...


   