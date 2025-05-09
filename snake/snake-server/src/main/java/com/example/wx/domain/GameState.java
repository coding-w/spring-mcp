package com.example.wx.domain;

/**
 * @author wangxiang
 * @description
 * @create 2025/5/5 21:09
 */
import java.util.List;
import java.util.ArrayList;

public class GameState {
    private String type;

    // 蛇的身体，由多个坐标点组成
    private List<Position> snake;

    // 食物的位置
    private Position food;
    private Position nextDirection;

    // 得分
    private int score;

    // 方向：up, down, left, right
    private String direction;

    // 游戏是否已开始
    private boolean gameStarted;

    // 是否启用自动寻路
    private boolean autoPathFind;

    // 构造函数
    public GameState() {
        this.snake = new ArrayList<>();
        snake.add(new Position(5, 5));
        snake.add(new Position(4, 5));
        snake.add(new Position(3, 5));
        this.nextDirection = new Position(7, 5);
        this.food = new Position(10, 10);
        this.score = 0;
        this.direction = "right"; // 默认方向
        this.gameStarted = false;
        this.autoPathFind = false;
    }

    // Getter 和 Setter


    public Position getNextDirection() {
        return nextDirection;
    }

    public void setNextDirection(Position nextDirection) {
        this.nextDirection = nextDirection;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Position> getSnake() {
        return snake;
    }

    public void setSnake(List<Position> snake) {
        this.snake = snake;
    }

    public Position getFood() {
        return food;
    }

    public void setFood(Position food) {
        this.food = food;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean isAutoPathFind() {
        return autoPathFind;
    }

    public void setAutoPathFind(boolean autoPathFind) {
        this.autoPathFind = autoPathFind;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "snake=" + snake +
                ", food=" + food +
                ", score=" + score +
                ", direction='" + direction + '\'' +
                ", gameStarted=" + gameStarted +
                ", autoPathFind=" + autoPathFind +
                '}';
    }
}