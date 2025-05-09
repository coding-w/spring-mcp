package com.example.wx.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangxiang
 * @description
 * @create 2025/5/6 17:54
 */
@Component
public class WebSocketSessionManager {

    // 存储所有在线 session
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 添加 session
    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    // 移除 session
    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
    }

    // 广播消息
    public void broadcast(String message) {
        TextMessage textMessage = new TextMessage(message);
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}