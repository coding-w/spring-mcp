package com.example.wx.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangxiang
 * @description
 * @create 2025/4/20 16:34
 */
@RestController
public class ChatController {

    @Resource
    private ChatClient chatClient;

    @GetMapping("/test")
    public String test() {
        return chatClient.prompt("做一个自我介绍吧").call().content();
    }
}
