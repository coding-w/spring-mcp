package com.example.wx.utils;

import com.example.wx.domain.GameState;
import com.example.wx.domain.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author wangxiang
 * @description
 * @create 2025/5/7 09:48
 */
public class SnakeMoveUtils {

    private final static Integer gridSize = 20;
    private final static Integer canvasSize = 400;

    public static String calculateDirection(GameState state) {
        Position head = state.getSnake().get(0);
        Position food = state.getFood();
        String currentDir = state.getDirection();
        List<String> possibleDirs = new ArrayList<>();
        switch (currentDir) {
            case "up":
                possibleDirs.addAll(Arrays.asList("up", "left", "right"));
                break;
            case "down":
                possibleDirs.addAll(Arrays.asList("down", "left", "right"));
                break;
            case "left":
                possibleDirs.addAll(Arrays.asList("up", "down", "left"));
                break;
            case "right":
                possibleDirs.addAll(Arrays.asList("up", "down", "right"));
        }
        List<String> safeDirs = possibleDirs.stream().filter(dir -> {
            Position newHead = moveHead(head, dir);
            return !isCollision(newHead, state.getSnake());
        }).toList();
        return !safeDirs.isEmpty() ? findBestDirection(head, food, safeDirs) : possibleDirs.get(0);
    }

    public static Position moveHead(Position head, String dir) {
        return switch (dir) {
            case "up" -> new Position(head.getX(), head.getY() - gridSize);
            case "down" -> new Position(head.getX(), head.getY() + gridSize);
            case "left" -> new Position(head.getX() - gridSize, head.getY());
            case "right" -> new Position(head.getX() + gridSize, head.getY());
            default -> new Position(head.getX(), head.getY());
        };
    }

    public static Boolean isCollision(Position newHead, List<Position> snake) {
        // 撞墙判断
        if (newHead.getX() < 0 || newHead.getX() >= canvasSize || newHead.getY() < 0 || newHead.getY() >= canvasSize) {
            return true;
        }
        return snake.stream().anyMatch(segment ->
                segment.getX() == newHead.getX() && segment.getY() == newHead.getY()
        );
    }

    public static String findBestDirection(Position head, Position food, List<String> directions) {
        return directions.stream()
                .min(Comparator.comparingDouble(dir -> {
                    Position newHead = moveHead(head, dir);
                    int dx = food.getX() - newHead.getX();
                    int dy = food.getY() - newHead.getY();
                    return Math.sqrt(dx * dx + dy * dy);
                }))
                .orElse(directions.get(0));
    }
}
