package com.example.wx.config;

import com.example.wx.service.SnakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author wangxiang
 * @description
 * @create 2025/5/6 17:39
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketSessionManager sessionManager;

    @Autowired
    private SnakeService snakeService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册处理器，允许跨域访问
        registry.addHandler(new MyWebSocketHandler(sessionManager, snakeService), "/ws")
                .setAllowedOrigins("*");
    }
}