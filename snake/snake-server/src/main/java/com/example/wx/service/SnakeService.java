package com.example.wx.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.wx.config.WebSocketSessionManager;
import com.example.wx.domain.GameState;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangxiang
 * @description
 * @create 2025/5/5 20:28
 */
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

    @Tool(name = "move_step", description = "贪吃蛇的地图是20*20的格子，每一个格子的长宽是20，根据贪吃蛇的状态，计算蛇的下一步，使蛇移动一步，每,需要精确传入 up,down,left,right 中的一个")
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
