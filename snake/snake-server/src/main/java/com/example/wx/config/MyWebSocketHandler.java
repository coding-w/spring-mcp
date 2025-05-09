package com.example.wx.config;

import com.alibaba.fastjson2.JSONObject;
import com.example.wx.domain.GameState;
import com.example.wx.domain.Position;
import com.example.wx.service.SnakeService;
import com.example.wx.utils.SnakeMoveUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;

/**
 * @author wangxiang
 * @description
 * @create 2025/5/6 17:40
 */
public class MyWebSocketHandler extends TextWebSocketHandler {
    private final WebSocketSessionManager sessionManager;

    private final SnakeService snakeService;

    public MyWebSocketHandler(WebSocketSessionManager sessionManager, SnakeService snakeService) {
        this.sessionManager = sessionManager;
        this.snakeService = snakeService;
    }

    /**
     * 连接建立后触发
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessionManager.addSession(session);
        // System.out.println("连接建立：" + session.getId());
        // JSONObject jsonObject = new JSONObject();
        // jsonObject.put("type", "start");
        // jsonObject.put("timestamp", System.currentTimeMillis());
        // session.sendMessage(new TextMessage(jsonObject.toJSONString()));
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
        // System.out.println("收到消息：" + message.getPayload());
    }

    /**
     * 连接关闭时触发
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionManager.removeSession(session);
        // System.out.println("连接关闭：" + session.getId());
    }
}
